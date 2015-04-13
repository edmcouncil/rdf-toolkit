package org

import org.semanticweb.owlapi.model.IRI

/**
 * Constants
 */
package object edmcouncil {

  val edmcBaseIRI = IRI.create("https://spec.edmcouncil.org/")
  val omgBaseIRI = IRI.create("http://www.omg.org/")

  val edmcFiboNamespaceIRI = IRI.create(s"${edmcBaseIRI.toString}fibo/")
  val omgFiboNamespaceIRI = IRI.create(s"${omgBaseIRI.toString}spec/EDMC-FIBO/")
  val omgSpecificationMetadataBaseIRI = IRI.create(s"${omgBaseIRI.toString}techprocess/ab/SpecificationMetadata/")

  val edmcFiboPrefix = "edmc-fibo"
  val omgFiboPrefix = "omg-fibo"
  val omgSpecificationMetadataPrefix = "sm"
}
