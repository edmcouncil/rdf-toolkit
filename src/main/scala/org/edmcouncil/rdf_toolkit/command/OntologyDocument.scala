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

import org.semanticweb.owlapi.model.{ IRI, OWLOntology, OWLOntologyManager }

import scala.util.Try
import scala.io.{ BufferedSource, Codec, Source }

/**
 * Helper class for loading ontologies
 */
private class OntologyDocument(ontologyManager: OWLOntologyManager, ontologyIRI: IRI) {

}

private trait OntologyDocumentLoaderFromResource {

  def getResourceAsStream(resource: String): InputStream =
    Try(fromResource(resource)).getOrElse(throw new FileNotFoundException(resource))

  def fromResource(resource: String, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader): InputStream =
    classLoader.getResourceAsStream(resource)

}

private class OntologyDocumentFromResource(ontologyManager: OWLOntologyManager, ontologyIRI: IRI, resource: String)
  extends OntologyDocument(ontologyManager, COMMAND_ONTOLOGY_IRI) with OntologyDocumentLoaderFromResource {

  val inputStream = getResourceAsStream(resource)

  def load(): OWLOntology = {
    ontologyManager.loadOntologyFromOntologyDocument(inputStream)
  }
}

object OntologyDocument {

  def apply(ontologyManager: OWLOntologyManager, ontologyIRI: IRI, resource: String): OWLOntology =
    new OntologyDocumentFromResource(ontologyManager, ontologyIRI, resource).load()
}
