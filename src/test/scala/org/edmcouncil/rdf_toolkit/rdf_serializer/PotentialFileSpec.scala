package org.edmcouncil.rdf_serializer

import org.edmcouncil.test.UnitSpec
import org.edmcouncil.util.PotentialFile

/**
 * Test the PotentialFile class
 */
class PotentialFileSpec extends UnitSpec {

  "A PotentialFile" must {
    "Accept a given partial path name for a file" in {
      val name = "src/test/resources/food.rdf"
      val pf = PotentialFile(name)
      assert(pf.fileExists, s"File $name does not exist")

      info (s"Filename is ${pf.fileName}")
      info (s"Path is ${pf.path}")
      info (s"URI is ${pf.uri}")
      info (s"Path is ${pf.path}")
      pf.printLog()
    }

  }
}
