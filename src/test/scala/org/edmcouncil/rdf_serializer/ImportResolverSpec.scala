package org.edmcouncil.rdf_serializer

/**
 * Test the ImportResolver
 */
class ImportResolverSpec extends UnitSpec {

  "An ImportResolver" must {

    val baseDir = PotentialDirectory("src/test")
    val baseUrl = BaseURL("http://whatever.com/")
    val testImportUrl = "http://whatever.com/resources/wine/"
    val resolver = ImportResolver(baseDir, baseUrl, testImportUrl)

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
