package org.edmcouncil.rdf_serializer

import java.io.File

import org.openrdf.rio.{RDFFormat, RDFWriterRegistry}
import org.semanticweb.owlapi.formats.{TurtleDocumentFormatFactory, OWLXMLDocumentFormatFactory, RDFXMLDocumentFormatFactory}
import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.apibinding.OWLManager

import grizzled.slf4j.Logging
import org.semanticweb.owlapi.rdf.rdfxml.renderer.XMLWriterPreferences


import scala.util.{Failure, Success, Try}

class OwlApiSerializer(private val commands: SerializerCommands) extends Logging {

  import commands._

  XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)


  //
  // Get hold of an ontology manager
  //
  lazy val ontologyManager = OWLManager.createOWLOntologyManager

  //
  // Get Ontology Loader Configuration
  //
  lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
    .setFollowRedirects(false)

  lazy val inputFileDocumentSource = new StreamDocumentSource(inputFileStream.get)

  def inputOntologyFileName = inputFileName.get

  //
  // Load the input file stream as an ontology, and do that twice, for some reason that works better,
  // according to the example given in the OWLAPI documentation "Remove the ontology so that we can load
  // a local copy"
  //
  lazy val ontology = {
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

  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()

  lazy val outputFormat = OwlApiOutputFormats.getOutputDocumentFormatWithName(params.outputFormatName)

  lazy val ontologyDocumentIRI = ontologyManager.getOntologyDocumentIRI(ontology)

  def load(): Unit = ontology

  def save(): Unit = ontology.saveOntology(outputFormat, outputFileStream.get)

  private def run = {

    info(s"Ontology Document IRI: $ontologyDocumentIRI")

    info(s"Ontology ID: ${ontology.getOntologyID}")

    load()

    save()

    /*

    // We can always obtain the location where an ontology was loaded from;
    // for this test, though, since the ontology was loaded from a string,
    // this does not return a file
    IRI documentIRI = manager.getOntologyDocumentIRI(localPizza);
    // Remove the ontology again so we can reload it later
    manager.removeOntology(pizzaOntology);
    // In cases where a local copy of one of more ontologies is used, an
    // ontology IRI mapper can be used to provide a redirection mechanism.
    // This means that ontologies can be loaded as if they were located on
    // the web. In this example, we simply redirect the loading from
    // http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl to
    // our local copy
    // above.
    // iri and file here are used as examples
    IRI iri = IRI
      .create("http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl");
    File file = folder.newFile();
    manager.getIRIMappers().add(new SimpleIRIMapper(iri, IRI.create(file)));
    // Load the ontology as if we were loading it from the web (from its
    // ontology IRI)
    IRI pizzaOntologyIRI = IRI
      .create("http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl");
    OWLOntology redirectedPizza = manager.loadOntology(pizzaOntologyIRI);
    IRI pizza = manager.getOntologyDocumentIRI(redirectedPizza);
    // Note that when imports are loaded an ontology manager will be
    // searched for mappings

    */
    0
  }

}

object OwlApiSerializer {

  def apply(params: SerializerCommands): Int = new OwlApiSerializer(params).run
}
