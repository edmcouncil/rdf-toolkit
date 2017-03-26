//package org.edmcouncil.rdf_serializer
//
//import org.edmcouncil.test.UnitSpec
//import org.semanticweb.owlapi.io.{OWLObjectRenderer, ToStringRenderer}
//import org.semanticweb.owlapi.model.IRI
//import org.semanticweb.owlapi.util.SimpleRenderer
//
///**
// * Do some tests with the OWLAPI IRI
// */
//class OwlApiIRISpec extends UnitSpec {
//
//  import org.edmcouncil._
//
//  val OWLObjectRenderer renderer = ToStringRenderer.getInstance
//
//  renderer.get.setPrefix(edmcFiboPrefix, edmcFiboNamespaceIRI.toString)
//
//
//  "OWLAPI IRI" should {
//
//    "deal with namespace" in {
//      val iri = IRI.create(edmcFiboNamespaceIRI.toString, "test")
//      info(s"iri = $iri")
//      info(s"iri short form = ${iri.getShortForm}")
//      info(s"iri as uri = ${iri.toURI}")
//      info(s"iri has scheme ${iri.getScheme}")
//      info(s"iri has namespace ${iri.getNamespace}")
//      info(s"iri has remainder ${Some(iri.getRemainder.orNull)}")
//      info(s"iri as quoted string ${iri.toQuotedString}")
//
//      info(s"iri rendering ${renderer.getRendering(iri)}")
//    }
//  }
//
//}
