package org.edmcouncil.rdf_serializer

import org.edmcouncil.util.PotentialFile

/**
 * Test the XmlSorter
 *
 * TODO: This test is NOT complete!!
 */
class XmlSorterSpec extends UnitSpec {

  "An XmlSorter" must {

    "sort an XML file (this test is not finished)" in {

      suppressOutput {

        val file = PotentialFile("src/test/resources/wine.rdf")
        val sorter = RdfXmlSorter(file.path.get)

        sorter.printIt()
      }
    }
  }
}
