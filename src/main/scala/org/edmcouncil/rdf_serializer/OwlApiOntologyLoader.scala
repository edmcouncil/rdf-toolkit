package org.edmcouncil.rdf_serializer

import java.io.IOException

import grizzled.slf4j.Logging
import org.semanticweb.owlapi.io.{OWLOntologyCreationIOException, OWLOntologyDocumentSource}
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener.{LoadingFinishedEvent, LoadingStartedEvent}
import org.semanticweb.owlapi.model._

import scala.util.{Failure, Success, Try}

/**
 * Load an Ontology
 */
class OwlApiOntologyLoader(
  ontologyManager: OWLOntologyManager,
  loaderConfiguration: OWLOntologyLoaderConfiguration,
  baseDir: PotentialDirectory,
  baseUrl: BaseURL
) extends Logging {

  val importsToBeResolved: collection.mutable.Set[IRI] = new collection.mutable.HashSet[IRI]

  val missingImportListener = new MissingImportListener() {
    @Override
    def importMissing(event: MissingImportEvent) {
      warn(s"Missing import ${event.getImportedOntologyURI}")
      importsToBeResolved += event.getImportedOntologyURI
    }
  }

  ontologyManager.addMissingImportListener(missingImportListener)

  /**
   * Try to load the given import by deriving the local file name from the IRI or else by
   * loading it from the web
   */
  def tryToLoadMissingImport(iri: IRI): Boolean = {

    val uri = iri.toURI
    val ns = iri.getNamespace

    if (! baseDir.hasName) return false

    val directoryName = baseDir.directoryName.get

    info(s"Trying to load $uri locally from $directoryName")

//    val fileName = tryIfLocalVersionExists(baseDir, iri)

    val resolver = ImportResolver(baseDir, baseUrl, iri.toURI.toString)

    if (resolver.found) {
      debug(s"${resolver.resource.get} does exists, loading it now")
      //
      // "Recursively" call loadOntology again but now for the imported ontology
      //
      loadOntology(resolver.inputDocumentSource.get)
    }

    false
  }

  val loaderListener = new OWLOntologyLoaderListener {

    override def startedLoadingOntology(event: LoadingStartedEvent): Unit = {
      val uri = event.getDocumentIRI.toURI
      // Nothing to do
      info(s"-----------------> Started loading ontology: $uri <-------")
    }

    override def finishedLoadingOntology(event: LoadingFinishedEvent): Unit = {
      val iri = event.getDocumentIRI
      val uri = iri.toURI
      info(s"-----------------> Finished Loading Ontology: $uri <-------")
      if (! event.isSuccessful) {
        event.getException match {
          case ex: OWLOntologyCreationIOException => ex.getCause match {
            case ioException: IOException =>
              if (tryToLoadMissingImport(iri)) {
                info(s"Successfully loaded $uri")
              } else {
                error(s"File I/O Exception: ${ioException.getMessage}")
              }
            case _ => throw ex
          }
          case ex @ _ =>
            error(s"Unknown exception while loading $uri: $ex")
        }
      }

      //      if (event.getException() != null) {
      //        getUnresolvedURIs().add(event.getDocumentIRI().toURI());
      //        event.getException().printStackTrace();
      //        return;
      //      }
      //      try {
      //        if (!uris.containsKey(event.getDocumentIRI().toURI())) {
      //          cacheOntology(event.getDocumentIRI().toURI(),
      //            newFile(), manager.getOntology(event
      //              .getOntologyID()));
      //        }
      //      } catch (UnknownOWLOntologyException e) {
      //        e.printStackTrace();
      //        getUnresolvedURIs().add(event.getDocumentIRI().toURI());
      //      } catch (OWLOntologyStorageException e) {
      //        e.printStackTrace();
      //        getUnresolvedURIs().add(event.getDocumentIRI().toURI());
      //      }

    }
  }

  ontologyManager.addOntologyLoaderListener(loaderListener)

  def loadOntology(input: PotentialFile): OWLOntology = loadOntology(input.inputDocumentSource.get)

  def loadOntology(input: OWLOntologyDocumentSource): OWLOntology = {

    val uriString = input.getDocumentIRI.toURI.toString

    info(s"Loading ontology $uriString")

    val ontTry1 = Try(ontologyManager.loadOntologyFromOntologyDocument(input, loaderConfiguration))

    ontTry1 match {
      case Success(ont) => // Remove the ontology so that we can load a local copy.
        info(s"Successfully loaded $uriString")
        ont
      //        ontologyManager.removeOntology(ont)
      //        info(s"Loading ontology $inputOntologyFileName, second time")
      //        val ontTry2 = Try(ontologyManager.loadOntologyFromOntologyDocument(inputFileDocumentSource, loaderConfiguration))
      //        ontTry2 match {
      //          case Success(ont2) => ont2
      //          case Failure(e) => throw new IllegalStateException(s"Could not load $inputOntologyFileName: $e")
      //        }
      case Failure(exception) => exception match {
        case ex: UnloadableImportException =>
          error(s"importsDeclaration=${ex.getImportsDeclaration.getIRI}")
          throw new IllegalStateException (s"Could not import $uriString")
        case _ => throw new IllegalStateException (s"Could not load $uriString: $exception")
      }
    }
  }
}
