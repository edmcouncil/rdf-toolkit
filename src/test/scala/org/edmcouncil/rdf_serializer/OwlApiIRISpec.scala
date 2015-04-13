package org.edmcouncil.rdf_serializer

import org.semanticweb.owlapi.io.ToStringRenderer
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.util.SimpleRenderer

/**
 * Do some tests with the OWLAPI IRI
 */
class OwlApiIRISpec extends UnitSpec {

  import org.edmcouncil._

  val objectRenderer = new SimpleRenderer

  objectRenderer.setPrefix(edmcFiboPrefix, edmcFiboNamespaceIRI.toString)

  val renderer = ToStringRenderer.getInstance

  renderer.setRenderer(objectRenderer)


  "OWLAPI IRI" should {

    "deal with namespace" in {
      val iri = IRI.create(edmcFiboNamespaceIRI.toString, "test")
      info(s"iri = $iri")
      info(s"iri short form = ${iri.getShortForm}")
      info(s"iri as uri = ${iri.toURI}")
      info(s"iri has scheme ${iri.getScheme}")
      info(s"iri has namespace ${iri.getNamespace}")
      info(s"iri has remainder ${Some(iri.getRemainder.orNull)}")
      info(s"iri as quoted string ${iri.toQuotedString}")

      info(s"iri rendering ${renderer.getRendering(iri)}")
    }
  }

}
