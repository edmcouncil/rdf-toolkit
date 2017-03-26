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
package org.edmcouncil.rdf_toolkit.command

import java.io.{ FileNotFoundException, InputStream }

import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.util.SimpleIRIMapper

import org.semanticweb.owlapi.model.IRI
import scala.collection.JavaConverters._

import scala.io.Source
import scala.util.Try

/**
 * Return the IRI Mapper for the rdf-toolkit.ttl ontology aka the config file
 */
object IRIMapper {

  require(ConfigFile.iri.isDefined, "Can't create IRI mapper without config file")

  private val documentIRI = ConfigFile.iri.get

  def getResourceAsStream(resource: String) = {
    getClass.getResourceAsStream(resource)
  }

  def commandOntologyAsStream = getResourceAsStream("command-ontology.ttl")

  def artifactOntologyAsStream = getResourceAsStream("artifact-ontology.ttl")

  def commandOntologyDocumentIRI = IRI.create("rsrc:command-ontology.ttl")
  def artifactOntologyDocumentIRI = IRI.create("rsrc:command-ontology.ttl")

  private def mapper1 = new SimpleIRIMapper(CONFIG_FILE_IRI, documentIRI)

  private def mapper2 = new SimpleIRIMapper(COMMAND_ONTOLOGY_IRI, commandOntologyDocumentIRI)

  private def mapper3 = new SimpleIRIMapper(ARTIFACT_ONTOLOGY_IRI, artifactOntologyDocumentIRI)

  private def mappers = List(mapper1, mapper2, mapper3)

  def apply(ontologyManager: OWLOntologyManager): Unit = {
    val iriMappers = ontologyManager.getIRIMappers
    mappers.foreach(iriMappers.add(_))
  }
}
