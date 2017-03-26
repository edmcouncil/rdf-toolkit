package org.edmcouncil.rdf_serializer

import org.edmcouncil.fibo.{EdmCouncilVersionIRI, EdmCouncilOntologyIRI}
import org.edmcouncil.test.UnitSpec

/**
 * Test the EdmCouncilOntologyIRI classes
 */
class EdmCouncilOntologyIRISpec extends UnitSpec {

  import org.edmcouncil._

  "EdmCouncilOntologyIRI" should {

    val fiboIri = EdmCouncilOntologyIRI(s"${edmcFiboNamespaceIRI}FND/Accounting/AboutAccounting/")

    "test 1" in {

      assert(fiboIri.family == "fibo")
      assert(fiboIri.domain == "fnd")
      assert(fiboIri.domainUC == "FND")
      assert(fiboIri.ontology == "AboutAccounting")
      assert(fiboIri.firstModule == "Accounting")
    }
  }

  "EdmCouncilVersionIRI" should {

    val fiboIri = EdmCouncilVersionIRI(s"${edmcFiboNamespaceIRI}FND/pink/Accounting/AboutAccounting/")

    "test 2" in {
      assert(fiboIri.family == "fibo")
      assert(fiboIri.version == "pink")
      assert(fiboIri.domain == "fnd")
      assert(fiboIri.domainUC == "FND")
      assert(fiboIri.ontology == "AboutAccounting")
      assert(fiboIri.firstModule == "Accounting")

      val fiboNewIri = fiboIri.withVersion("yellow")

      assert(fiboNewIri.family == "fibo")
      assert(fiboNewIri.version == "yellow")
      assert(fiboNewIri.domain == "fnd")
      assert(fiboNewIri.domainUC == "FND")
      assert(fiboNewIri.ontology == "AboutAccounting")
      assert(fiboNewIri.firstModule == "Accounting")
    }
  }

}
