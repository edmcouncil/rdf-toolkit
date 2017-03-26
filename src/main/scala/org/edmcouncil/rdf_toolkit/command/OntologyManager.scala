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

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{ OWLOntology, OWLOntologyManager }
import java.io.File

/**
 * Get the OWL API OWLOntologyManager
 */
object OntologyManager {

  private lazy val configFile = ConfigFile()

  private lazy val owlOntologyManager: Option[OWLOntologyManager] = {
    val manager = OWLManager.createOWLOntologyManager
    IRIMapper(manager)
    Option(manager)
  }

  def apply(): Option[OWLOntologyManager] = owlOntologyManager

  private def load(configFile: File): Option[OWLOntology] = {
    if (owlOntologyManager.isDefined) {
      OntologyDocument(owlOntologyManager.get, COMMAND_ONTOLOGY_IRI, "command-ontology.ttl")
      OntologyDocument(owlOntologyManager.get, ARTIFACT_ONTOLOGY_IRI, "artifact-ontology.ttl")
      owlOntologyManager.map(_.loadOntologyFromOntologyDocument(configFile))
    } else None
  }

  def load(): Option[OWLOntology] = configFile.flatMap(configFile ⇒ load(configFile))
}
