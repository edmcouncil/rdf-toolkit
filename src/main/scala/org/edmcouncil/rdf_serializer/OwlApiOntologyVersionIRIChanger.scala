package org.edmcouncil.rdf_serializer

import com.google.common.base.Optional
import org.semanticweb.owlapi.model.{IRI, OWLOntology, OWLOntologyID, SetOntologyID}

/**
 * Convenience object that takes care of changing the owl:versionIRI of the given Ontology
 */
object OwlApiOntologyVersionIRIChanger {

  def apply(ontology: OWLOntology, versionIRI: IRI): Unit = {

    val ontologyManager = ontology.getOWLOntologyManager
    val oldOntologyID = ontology.getOntologyID

    //
    // Note that we MUST specify an ontology IRI if we want to specify a version IRI
    //
    val newOntologyID = new OWLOntologyID(oldOntologyID.getOntologyIRI, Optional.fromNullable(versionIRI))
    //
    // Create the change that will set our version IRI
    //
    val setOntologyID = new SetOntologyID(ontology, newOntologyID)
    //
    // Apply the change
    //
    ontologyManager.applyChange(setOntologyID)
  }

}
