package org.edmcouncil.rdf_serializer

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
