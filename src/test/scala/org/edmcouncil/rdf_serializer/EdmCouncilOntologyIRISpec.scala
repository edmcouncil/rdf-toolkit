package org.edmcouncil.rdf_serializer

import org.edmcouncil.fibo.{EdmCouncilVersionIRI, EdmCouncilOntologyIRI}

/**
 * Test the EdmCouncilOntologyIRI classes
 */
class EdmCouncilOntologyIRISpec extends UnitSpec {

  import org.edmcouncil._

  "EdmCouncilOntologyIRI" must {

    val fiboIri = EdmCouncilOntologyIRI(s"${edmcFiboNamespaceIRI}FND/Accounting/AboutAccounting/")

    assert(fiboIri.family == "fibo")
    assert(fiboIri.domain == "fnd")
    assert(fiboIri.domainUC == "FND")
    assert(fiboIri.ontology == "AboutAccounting")
    assert(fiboIri.firstModule == "Accounting")
  }

  "EdmCouncilVersionIRI" must {

    val fiboIri = EdmCouncilVersionIRI(s"${edmcFiboNamespaceIRI}FND/pink/Accounting/AboutAccounting/")

    assert(fiboIri.family == "fibo")
    assert(fiboIri.version == "pink")
    assert(fiboIri.domain == "fnd")
    assert(fiboIri.domainUC == "FND")
    assert(fiboIri.ontology == "AboutAccounting")
    assert(fiboIri.firstModule == "Accounting")
  }

}
