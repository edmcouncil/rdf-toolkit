/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 *
 * The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package org.edmcouncil.rdf_serializer

import java.io.IOException
import java.nio.file.Path

import grizzled.slf4j.Logging
import org.edmcouncil.util.{BaseURL, PotentialDirectory, PotentialFile}
import org.semanticweb.owlapi.io.{OWLOntologyCreationIOException, OWLOntologyDocumentSource}
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener.{LoadingFinishedEvent, LoadingStartedEvent}
import org.semanticweb.owlapi.model._

import scala.util.{Failure, Success, Try}

trait OnErrorAborter extends Logging {

  def abortOnError: Boolean

  override def error(msg: => Any) = {
    super.error(msg)
    if (abortOnError) throw new IllegalStateException(s"Aborted: $msg (you used --abort switch)")
  }
}

/**
 * Load an Ontology
 */
class OwlApiOntologyLoader(
  ontologyManager: OWLOntologyManager,
  loaderConfiguration: OWLOntologyLoaderConfiguration,
  baseDirUrls: Seq[(Path, BaseURL)],
  val abortOnError: Boolean
) extends Logging with OnErrorAborter {

  /**
   * For every missing import specified by iri, try to find a corresponding base directory and if found, create an
   * ImportResolver for it which will try to find the given import in the given base directory.
   */
  private def findImportResolver(iri: IRI): Option[ImportResolver] = {

    def findIt(basePathUri: (Path, BaseURL)): Boolean = {
      val (basePath, baseUri) = basePathUri
      info(s"Does $baseUri match with ${iri.toURI.toString}?")
      baseUri.matchesWith(iri.toURI.toString)
    }

    def mapIt(basePathUri: (Path, BaseURL)): ImportResolver = {
      val (basePath, baseUri) = basePathUri
      ImportResolver(PotentialDirectory(basePath), baseUri, iri)
    }

    baseDirUrls.find(findIt).map(mapIt)
  }

  /**
   * Try to load the given import by deriving the local file name from the IRI or else by
   * loading it from the web.
   *
   * "Recursively" call loadOntology again but now for the imported ontology
   */
  private def tryToLoadMissingImport(iri: IRI): Boolean = findImportResolver(iri).map(loadOntology).isDefined

  val loaderListener = new OWLOntologyLoaderListener {

    override def startedLoadingOntology(event: LoadingStartedEvent): Unit = {
      val uri = event.getDocumentIRI.toURI
      // Nothing to do
      info(s"-----------------> Started loading ontology:  $uri <-------")
    }

    override def finishedLoadingOntology(event: LoadingFinishedEvent): Unit = {
      val iri = event.getDocumentIRI
      val uri = iri.toURI
      if (event.isSuccessful) {
        info(s"-----------------> Finished loading ontology: $uri <-------")
      } else {
        event.getException match {
          case ex: OWLOntologyCreationIOException => ex.getCause match {
            case ioException: IOException =>
              if (! tryToLoadMissingImport(iri)) {
                error(s"Could not load missing import: $uri")
              }
            case _ => throw ex
          }
          case ex @ _ =>
            error(s"Unknown exception while loading $uri: $ex")
        }
      }
    }
  }

  ontologyManager.addOntologyLoaderListener(loaderListener)

  def loadOntology(resolver: ImportResolver): OWLOntology = loadOntology(resolver.inputDocumentSource.get)

  def loadOntology(input: PotentialFile): OWLOntology = loadOntology(input.inputDocumentSource.get)

  def loadOntology(input: OWLOntologyDocumentSource): OWLOntology = {

    val uriString = input.getDocumentIRI.toURI.toString

    info(s"Loading ontology $uriString")

    val ontTry1 = Try(ontologyManager.loadOntologyFromOntologyDocument(input, loaderConfiguration))

    ontTry1 match {
      case Success(ont) =>
        info(s"Successfully loaded $uriString")
        ont
      case Failure(exception) => exception match {
        case ex: UnloadableImportException =>
          error(s"Could not import $uriString. importsDeclaration=${ex.getImportsDeclaration.getIRI}")
          null
        case _ =>
          error(s"Could not load $uriString: $exception")
          null
      }
    }
  }
}
