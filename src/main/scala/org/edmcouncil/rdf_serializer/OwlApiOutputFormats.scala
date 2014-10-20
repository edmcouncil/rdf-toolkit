package org.edmcouncil.rdf_serializer

import grizzled.slf4j.Logging
import org.openrdf.rio.{RDFFormat, RDFWriterRegistry}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.{OWLXMLDocumentFormatFactory, RDFXMLDocumentFormatFactory}
import org.semanticweb.owlapi.model.OWLDocumentFormat
import scala.collection.JavaConverters._

object OwlApiOutputFormats extends Logging {

  val rdfWriterRegistry = RDFWriterRegistry.getInstance()

  val formats = rdfWriterRegistry.getKeys.asScala.toSet

  def formatNames = formats.map(_.getName)

  lazy val defaultFormat = getFormatWithName(Some("RDF/XML"))

  def getFormatWithName(name: Option[String]): RDFFormat = if (name.isEmpty) defaultFormat
  else formats.find(_.getName.equalsIgnoreCase(name.get)).getOrElse(defaultFormat)

  //
  // Get hold of an ontology manager
  //
  lazy val ontologyManager = OWLManager.createOWLOntologyManager


  lazy val ontologyStorerFactories = ontologyManager.getOntologyStorers.asScala.toSet

  lazy val outputDocumentFormatFactories = ontologyStorerFactories.map(_.getFormatFactory)

  lazy val outputDocumentFormatNames = outputDocumentFormatFactories.map(_.getKey)

  lazy val outputDocumentFormats = outputDocumentFormatFactories.map(_.createFormat())

  lazy val defaultDocumentFormat = getOutputDocumentFormatWithName(Some("RDF/XML Syntax"))

  def getOutputDocumentFormatWithName(name: Option[String]): OWLDocumentFormat = if (name.isEmpty) defaultDocumentFormat
  else outputDocumentFormats.find(_.getKey.equalsIgnoreCase(name.get)).getOrElse(defaultDocumentFormat)
}
