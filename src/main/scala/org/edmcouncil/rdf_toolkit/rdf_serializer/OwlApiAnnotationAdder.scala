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
package org.edmcouncil.rdf_toolkit.rdf_serializer

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._

/**
 * Convenience object adding an Annotation to a given ontology
 */
object OwlApiAnnotationAdder {

  val df = OWLManager.getOWLDataFactory // Gets a global data factory that can be used to create OWL API objects

  def apply(ontology: OWLOntology, annotationProperty: OWLAnnotationProperty, annotationValue: OWLAnnotationValue): Unit = {
    //
    // Get the OntologyManager of the given Ontology
    //
    val ontologyManager = ontology.getOWLOntologyManager
    //
    // Ask the OWLDataFactory to create the OWLAnnotation with the given Property/Predicate and Value
    //
    val annotation = df.getOWLAnnotation(annotationProperty, annotationValue)
    //
    // Now we can add the Annotation as an ontology annotation and ask the OntologyManager to apply the change
    //
    ontologyManager.applyChange(new AddOntologyAnnotation(ontology, annotation))
  }

  def apply(ontology: OWLOntology, annotationPropertyIRI: IRI, annotationValue: OWLAnnotationValue): Unit = {
    //
    // Ask the OWLDataFactory to create a new AnnotationProperty with the given value
    //
    val annotationProperty = df.getOWLAnnotationProperty(annotationPropertyIRI)

    apply(ontology, annotationProperty, annotationValue)
  }

  /**
   * Add an Annotation to the given Ontology with the given predicate and string literal value
   */
  def apply(ontology: OWLOntology, annotationPropertyIRI: IRI, annotationValue: String): Unit =
    apply(ontology, annotationPropertyIRI, df.getOWLLiteral(annotationValue))

  /**
   * Add an Annotation to the given Ontology with the given predicate and string literal value
   */
  def apply(ontology: OWLOntology, annotationPropertyIRI: IRI, annotationValue: IRI): Unit =
    apply(ontology, annotationPropertyIRI, annotationValue)
}
