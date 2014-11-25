package org.edmcouncil.rdf_serializer

import grizzled.slf4j.Logging
import org.openrdf.rio.RDFWriterRegistry
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.rdf.rdfxml.renderer.XMLWriterPreferences

class OwlApiSerializer(private val commands: SerializerCommands) extends Logging {

  import commands._

  //
  // Ensure that the DOCTYPE rdf:RDF ENTITY section is generated
  //
  XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)

  //
  // Get hold of an ontology manager
  //
  def createOntologyManager = {
    val manager = OWLManager.createOWLOntologyManager
    manager
  }

  //
  // Get Ontology Loader Configuration
  //
  lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT) // Cause the listener to be called
    .setLoadAnnotationAxioms(true)
    .setTreatDublinCoreAsBuiltIn(true)
    .setFollowRedirects(false)

  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()


  private def saveOntology(
    ontologyManager: OWLOntologyManager,
    ontology: OWLOntology,
    format: OWLDocumentFormat
  ): Unit = {

    info(s"Saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
    info(s"In Format: $format")

    ontology.saveOntology(format, output.outputStream.get)

    ontologyManager.removeOntology(ontology)
  }

  private def run = {

    val ontologyManager = createOntologyManager
    val loader = new OwlApiOntologyLoader(ontologyManager, loaderConfiguration)
    val ontology = loader.loadOntology(input)
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
