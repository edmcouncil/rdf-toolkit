package org.edmcouncil.rdf_serializer

import java.io.OutputStream

import org.scalatest._

/**
 * Test the Serializer
 */
class SerializerSpec extends UnitSpec {

  def run(args: String*): Int = suppressOutput {
    MainImplOld(args).run
  }
  def run2(args: String*): Int = MainImplOld(args).run

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
        run("--whatever") != 0, "did not return non zero option"
      )
    }

    "accept the --help option" in {
      run("--help") should equal(0)
    }

  }

  "A Serializer" must {

    "convert the wine ontology" in {
      run(
        "--input-file", "src/test/resources/wine.rdf",
        "--output-file", "src/test/resources/test-out-wine.rdf",
        "--force"
      ) should equal (0)
    }

    /*
     * The wine ontology imports the food ontology (and vice versa), so the --base-dir
     * and --base-url options need to allow the serializer to find the food ontology
     */
    "serialize the wine ontology and support the import of the food ontology" in {
      run(
        "--input-file", "src/test/resources/wine.rdf",
        "--output-file", "src/test/resources/test-out-wine.rdf",
        "--force",
        "--base-dir", "src/test/resources",
        "--base-url", "http://www.w3.org/TR/2003/PR-owl-guide-20031209"
      ) should equal (0)
    }

    "convert the fibo contracts ontology" in {
      run(
        "--input-file", "src/test/resources/fibo-fnd-contracts.rdf",
        "--output-file", "src/test/resources/test-out-fibo-fnd-contracts.rdf",
        "--force"
      ) should equal (0)
    }

    "not generate errors int the output of test-case-001.rdf" in {
      run(
        "--input-file", "src/test/resources/test-case-001.rdf",
        "--output-file", "src/test/resources/test-out-test-case-001.rdf",
        "--force"
      ) should equal (0)
    }

    "not mess with the blank nodes in FIBO FND Ownership & Control - Control.rdf" in {
      run(
        "--input-file", "src/test/resources/fibo/fnd/OwnershipAndControl/Control.rdf",
        "--output-file", "src/test/resources/test-out-fibo-fnd-ownershipandcontrol-control.rdf",
        "--base-dir", "src/test/resources/fibo",
        "--base-url", "http://www.omg.org/spec/EDMC-FIBO",
        "--force"
      ) should equal (0)
    }

    "Do all the imports right in FIBO FND Accounting - AccountingEquity.rdf" in {
      run2(
        "--input-file", "src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf",
        "--output-file", "src/test/resources/test-out-fibo-fnd-accounting-equity.rdf",
        "--base-dir", "src/test/resources/fibo",
        "--base-url", "http://www.omg.org/spec/EDMC-FIBO",
        "--force"
      ) should equal (0)
    }
  }
}

