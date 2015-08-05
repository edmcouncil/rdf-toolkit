package org.edmcouncil.rdf_serializer

import org.edmcouncil.util.{ PotentialDirectory, BaseURL }
import org.semanticweb.owlapi.model.IRI

/**
 * Test the ImportResolver
 */
class ImportResolverSpec extends UnitSpec {

  "An ImportResolver" must {

    suppressOutput {

      val baseDir = PotentialDirectory("src/test")
      val baseUrl = BaseURL("http://whatever.com/")
      val testImportUrl = "http://whatever.com/resources/wine/"
      val testImportIri = IRI.create(testImportUrl)
      val resolver = ImportResolver(baseDir, baseUrl, testImportIri)

      "remainder of test import url is resources/wine" in {
        assert(resolver.remainderOfImportUrl.get.equals("resources/wine/"))
      }

      "import should be found" in {
        assert(resolver.shouldBeFound)
      }

      "find the wine ontology given a base directory and base URL" in {
        assert(resolver.resource.isDefined)
      }
    }
  }
}
