package org.edmcouncil.rdf_serializer

import java.io.OutputStream

import org.edmcouncil.main.MainImpl

/**
 * Test the Serializer
 */
class SerializerSpec extends UnitSpec {

  def runSilent(args: String*): Int = suppressOutput {
    run(args: _*)
  }
  def run(args: String*): Int = MainImpl(args).run

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
        runSilent("--whatever") != 0, "did not return non-zero option"
      )
    }

    "accept the --help option" in {
      runSilent("--help") should equal(0)
    }
  }

  "A Serializer" must {

    "convert the wine ontology" in {
      runSilent(
        "--force",
        "--abort",
        "--url-replace", "http://www.w3.org/TR/2003/PR-owl-guide-20031209/=http://whatever/",
        "src/test/resources/test-out-wine.rdf",
        "src/test/resources/wine.rdf"
      ) should equal(0)
    }

    /*
     * The wine ontology imports the food ontology (and vice versa), so the --base-dir
     * and --base-url options need to allow the serializer to find the food ontology
     */
    "serialize the wine ontology and support the import of the food ontology" in {
      runSilent(
        "--force",
        "--abort",
        "--base", "src/test/resources=http://www.w3.org/TR/2003/PR-owl-guide-20031209",
        "src/test/resources/test-out-wine.rdf",
        "src/test/resources/wine.rdf"
      ) should equal(0)
    }

    "convert the fibo contracts ontology" in {
      run(
        "--force",
        "--abort",
        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
        "--url-replace", "http://www.omg.org/spec/EDMC-FIBO/=http://spec.edmcouncil.org/fibo/",
        "src/test/resources/test-out-fibo-fnd-contracts.rdf",
        "src/test/resources/fibo-fnd-contracts.rdf"
      ) should equal(0)
    }

    "not generate errors int the output of test-case-001.rdf" in {
      runSilent(
        "--force",
        "--abort",
        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
        "src/test/resources/test-out-test-case-001.rdf",
        "src/test/resources/test-case-001.rdf"
      ) should equal(0)
    }

    "not mess with the blank nodes in FIBO FND Ownership & Control - Control.rdf" in {
      runSilent(
        "--force",
        "--abort",
        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
        "src/test/resources/test-out-fibo-fnd-ownershipandcontrol-control.rdf",
        "src/test/resources/fibo/fnd/OwnershipAndControl/Control.rdf"
      ) should equal(0)
    }

    "do all the imports right in FIBO FND Accounting - AccountingEquity.rdf" in {
      runSilent(
        "--force",
        "--abort",
        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
        "src/test/resources/test-out-fibo-fnd-accounting-equity.rdf",
        "src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf"
      ) should equal(0)
    }
  }
}

