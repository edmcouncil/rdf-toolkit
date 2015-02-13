package org.edmcouncil.rdf_serializer

/**
 * Test the XmlSorter
 */
class XmlSorterSpec extends UnitSpec {

  "An XmlSorter" must {

    "sort an XML file" in {

      val file = PotentialFile("src/test/resources/wine.rdf")
      val sorter = RdfXmlSorter(file.path.get)

      sorter.printIt()
    }
  }
}
