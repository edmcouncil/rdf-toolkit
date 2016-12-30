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
package org.edmcouncil.serializer

import grizzled.slf4j.Logging
import org.openrdf.rio.{ RDFFormat, RDFWriterRegistry }
import org.semanticweb.owlapi.apibinding.OWLManager
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

  def getOutputDocumentFormatWithName(name: Option[String]): OWLDocumentFormat =
    if (name.isEmpty)
      defaultDocumentFormat
    else
      outputDocumentFormats.find(_.getKey.equalsIgnoreCase(name.get)).getOrElse(defaultDocumentFormat)
}
