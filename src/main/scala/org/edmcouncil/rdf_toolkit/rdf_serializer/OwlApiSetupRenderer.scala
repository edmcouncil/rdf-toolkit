//package org.edmcouncil.rdf_serializer
//
//import org.semanticweb.owlapi.io.ToStringRenderer
//import org.semanticweb.owlapi.model.{OWLDocumentFormat, OWLOntology}
//import org.semanticweb.owlapi.util.SimpleRenderer
//
///**
// * Initialize the default ToStringRenderer with all the prefixes that we found in the input Ontology, including
// * the imported Ontologies.
// *
// * This only affects the log output by the way.
// */
//object OwlApiSetupRenderer {
//
//  def apply(format: OWLDocumentFormat, ontology: OWLOntology): Unit = {
//
//    val ontologyManager = ontology.getOWLOntologyManager
//
//    //
//    // When the given format supports prefixes then use the prefix manager associated with that format
//    //
//    val prefixOntologyFormat = if (format.isPrefixOWLOntologyFormat) Some(format.asPrefixOWLOntologyFormat) else None
//
//    val objectRenderer = new SimpleRenderer
//
//    if (prefixOntologyFormat.isDefined)
//      objectRenderer.setPrefixesFromOntologyFormat(ontology, ontologyManager, true)
//
//    ToStringRenderer.getInstance().setRenderer(objectRenderer)
//  }
//}
