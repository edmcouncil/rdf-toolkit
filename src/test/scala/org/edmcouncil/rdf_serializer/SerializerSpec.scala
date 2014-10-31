package org.edmcouncil.rdf_serializer

import java.io.OutputStream

import org.scalatest._

abstract class UnitSpec
  extends WordSpecLike with Matchers


/**
 * Test the Serializer
 */
class SerializerSpec extends UnitSpec {

  "A Serializer Cli Interface" must {

    Console.withOut(new OutputStream() {
      def write(b: Int) {
        //DO NOTHING
      }
    }) {
      println("This goes to default _error_")
    }

    "return a non-zero exit code when an invalid option is passed" in {
      assert(
        suppressOutput {
          MainImpl(Array("--whatever")).run != 0
        }, "did not return non zero option"
      )
    }

    "accept the --help option" in {
      suppressOutput {
        MainImpl(Array("--help")).run
      } should equal(0)
    }
  }

  "A Serializer" must {

    "convert the wine ontology" in {
      //suppressOutput {
        MainImpl(Array(
          "--input-file", "src/test/resources/wine.rdf",
          "--output-file", "src/test/resources/test-out.rdf",
          "--force"
        )).run should equal (0)
      // }  should equal (0)
    }
  }

  def suppressOutput[T](thunk: => T): T = {

    val bitBucket = new OutputStream() {
      def write(b: Int) {}
    }

    Console.withOut(bitBucket) {
      return thunk
    }
  }
}

