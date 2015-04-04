/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Enterprise Data Management Council
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.edmcouncil.rdf_serializer

import grizzled.slf4j.Logging
import org.semanticweb.owlapi.io.RDFParserMetaData
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.util.{ OWLOntologyIRIChanger, OWLEntityRenamer, OWLEntityURIConverter, OWLEntityURIConverterStrategy }

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import scala.util.matching.Regex

/**
 * Convenience Class that combines all the rename methods of the OwlApiSerializer
 */
class OwlApiUriRenamer(
    regex: Regex,
    replacement: String,
    ontologyManager: OWLOntologyManager,
    ontologies: Set[OWLOntology],
    format: OWLDocumentFormat) extends Logging {

  renameMetadata()

  renameUris()

  renameOntologyIRIs()

  def renameOneString(from: String)(function: (String, String) ⇒ Unit): Unit = {
    val to = regex.replaceFirstIn(from, replacement)
    if (from != to) {
      function(from, to)
    }
  }

  def renameOneIri(from: IRI)(function: (IRI, IRI) ⇒ Unit): IRI = {
    val fromString = from.toURI.toString
    var toIRI: Option[IRI] = None
    renameOneString(fromString) { (fromString_, to) ⇒
      toIRI = Some(IRI.create(to))
      function(from, toIRI.get)
    }
    toIRI.getOrElse(from)
  }

  def renameMetadata() {

    if (format.isPrefixOWLOntologyFormat) {
      val prefixedFormat = format.asPrefixOWLOntologyFormat()
      val map = prefixedFormat.getPrefixName2PrefixMap.asScala
      for ((prefix, ns) ← map) {
        renameOneString(ns) { (from, to) ⇒
          info(s"Renaming namespace for prefix '$prefix' from $from to $to")
          prefixedFormat.setPrefix(prefix, to)
        }
      }
    }

    val inputOntologyMetadata = format.getOntologyLoaderMetaData

    inputOntologyMetadata match {
      case metadata: RDFParserMetaData ⇒ renameRDFParserMetaData(metadata)
    }

    format.setOntologyLoaderMetaData(inputOntologyMetadata)
  }

  def renameRDFParserMetaData(metadata: RDFParserMetaData): Unit = {

    //    info (s"renameRDFParserMetaData: RDF triple count is ${metadata.getTripleCount}")
    //
    //    val unparsedTriples = metadata.getUnparsedTriples.asScala
    //
    //    unparsedTriples.foreach { (triple) =>
    //      info(s"Unparsed Triple: $triple") // TODO: See if we should rename URIs here too
    //    }
    //
    //    val headerState = metadata.getHeaderState
    //
    //    info(s"Header Status is $headerState")
    //
    //    for ( (entry) <- metadata.getGuessedDeclarations.entries().asScala) {
    //      val iri = entry.getKey
    //      val clazz = entry.getValue
    //      info (s"Guessed Declaration for $iri is $clazz")
    //    }
  }

  def renameUris(): Unit = {

    info(s"Renaming URIs: $regex = $replacement")

    val strategy = new OWLEntityURIConverterStrategy {

      def getConvertedIRI(entity: OWLEntity): IRI = {
        renameOneIri(entity.getIRI) { (from, to) ⇒
          info(s"Convert $from to $to")
        }
      }
    }
    //
    // The OWLEntityURIConvert performs a bulk conversion/translation of entity URIs.
    //
    val converter = new OWLEntityURIConverter(ontologyManager, ontologies.asJava, strategy)

    ontologyManager.applyChanges(converter.getChanges)
  }

  def renameOntologyIRIs(): Unit = {
    val renamer = new OWLOntologyIRIChanger(ontologyManager)

    for (ontology ← ontologies) {
      val id = ontology.getOntologyID
      val iri = Some(id.getOntologyIRI.orNull())
      if (iri.isDefined) {
        renameOneIri(iri.get) { (from, to) ⇒
          info(s"Ontology IRI rename $from to $to")
          val changes = renamer.getChanges(ontology, to).asScala
          for (change ← changes) {
            info(s"Change: $change")
          }
          ontologyManager.applyChanges(changes.asJava)
        }
      }
    }
  }
}
