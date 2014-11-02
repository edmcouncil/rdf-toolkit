package org.edmcouncil.rdf_serializer

import java.io.File
import javax.annotation.Nonnull

import com.google.inject.{Injector, Guice}
import org.openrdf.rio.{RDFFormat, RDFWriterRegistry}
import org.semanticweb.owlapi.{OWLAPIServiceLoaderModule, OWLAPIParsersModule}
import org.semanticweb.owlapi.formats.{TurtleDocumentFormatFactory, OWLXMLDocumentFormatFactory, RDFXMLDocumentFormatFactory}
import org.semanticweb.owlapi.io.{OWLOntologyLoaderMetaData, StreamDocumentSource}
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.apibinding.OWLManager

import grizzled.slf4j.Logging
import org.semanticweb.owlapi.oboformat.OWLAPIOBOModule
import org.semanticweb.owlapi.rdf.rdfxml.renderer.XMLWriterPreferences
import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule


import scala.util.{Failure, Success, Try}

class OwlApiSerializer(private val commands: SerializerCommands) extends Logging {

  import commands._

  //
  // Ensure that the DOCTYPE rdf:RDF ENTITY section is generated
  //
  XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)


  //
  // Get hold of an ontology manager
  //
  def createOntologyManager = OWLManager.createOWLOntologyManager

  //
  // Get Ontology Loader Configuration
  //
  lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
    .setFollowRedirects(false)

  lazy val inputFileDocumentSource = new StreamDocumentSource(inputFileStream.get)

  def inputOntologyFileName = inputFileName.get

  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()

  //
  // Load the input file stream as an ontology, and do that twice, for some reason that works better,
  // according to the example given in the OWLAPI documentation "Remove the ontology so that we can load
  // a local copy"
  //
  private def loadOntology(ontologyManager: OWLOntologyManager): OWLOntology = {

    info(s"Loading ontology $inputOntologyFileName")

    val ontTry1 = Try(ontologyManager.loadOntologyFromOntologyDocument(inputFileDocumentSource, loaderConfiguration))

    ontTry1 match {
      case Success(ont) => // Remove the ontology so that we can load a local copy.
        info(s"Successfully loaded $inputOntologyFileName")
        ontologyManager.removeOntology(ont)
        info(s"Loading ontology $inputOntologyFileName, second time")
        val ontTry2 = Try(ontologyManager.loadOntologyFromOntologyDocument(inputFileDocumentSource, loaderConfiguration))
        ontTry2 match {
          case Success(ont2) => ont2
          case Failure(e) => throw new IllegalStateException(s"Could not load $inputOntologyFileName: $e")
        }
      case Failure(e) => throw new IllegalStateException(s"Could not load $inputOntologyFileName: $e")
    }
  }

  private def saveOntology(
    ontologyManager: OWLOntologyManager,
    ontology: OWLOntology,
    format: OWLDocumentFormat
  ): Unit = {

    info(s"Saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
    info(s"In Format: $format")

    ontology.saveOntology(format, outputFileStream.get)

    ontologyManager.removeOntology(ontology)
  }

  private def run = {

    val ontologyManager = createOntologyManager
    val ontology = loadOntology(ontologyManager)
    val ontologyDocumentIRI = ontologyManager.getOntologyDocumentIRI(ontology)

    info(s"Ontology Document IRI: $ontologyDocumentIRI")

    info(s"Ontology ID: ${ontology.getOntologyID}")

    val inputFormat = ontologyManager.getOntologyFormat(ontology)

    info(s"Input Format: $inputFormat")

    val outputFormat = OwlApiOutputFormats.getOutputDocumentFormatWithName(params.outputFormatName)

    //
    // Some ontology formats support prefix names and prefix IRIs. In our
    // case we loaded the pizza ontology from an rdf/xml format, which
    // supports prefixes. When we save the ontology in the new format we
    // will copy the prefixes over so that we have nicely abbreviated IRIs
    // in the new ontology document
    //
    if (inputFormat.isPrefixOWLOntologyFormat) {
      if (outputFormat.isPrefixOWLOntologyFormat) {
        val prefixedOutputFormat = outputFormat.asPrefixOWLOntologyFormat()
        //
        // For some reason the outputFormat, which is also a PrefixManager, remembers the prefixes and namespaces
        // of the previous save operation, so we have to clear it here first before we copy the inputFormat's
        // prefixes into it.
        //
        prefixedOutputFormat.clear()
        prefixedOutputFormat.copyPrefixesFrom(inputFormat.asPrefixOWLOntologyFormat())
      }
    }

    val inputOntologyMetadata = inputFormat.getOntologyLoaderMetaData
    outputFormat.setOntologyLoaderMetaData(inputOntologyMetadata)

    saveOntology(ontologyManager, ontology, outputFormat)

    0
  }
}

object OwlApiSerializer {

  def apply(params: SerializerCommands): Int = new OwlApiSerializer(params).run
}
