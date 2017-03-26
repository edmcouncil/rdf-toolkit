/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Enterprise Data Management Council
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
//package org.edmcouncil.rdf_toolkit.rdf_serializer
//
//import java.io.OutputStream
//
//import org.edmcouncil.main.MainImpl
//import org.edmcouncil.test.UnitSpec
//
///**
// * Test the Serializer
// */
//class SerializerSpec extends UnitSpec {
//
//  import org.edmcouncil._
//
//  def runSilent(args: String*): Int = suppressOutput {
//    run(args: _*)
//  }
//  def run(args: String*): Int = MainImpl(args).run
//
//  val resourceDir = "src/test/resources"
//  val fiboDir = s"$resourceDir/fibo"
//  val fiboNewDir = s"$fiboDir-with-placeholder-iri"
//  val specMetaDir = s"$fiboNewDir/etc/testing/data"
//
//  "A Serializer Cli Interface" must {
//
//    Console.withOut(new OutputStream() {
//      def write(b: Int) {
//        //DO NOTHING
//      }
//    }) {
//      println("This goes to default _error_")
//    }
//
//    "return a non-zero exit code when an invalid option is passed" in {
//      assert(
//        runSilent("--whatever") != 0, "did not return non-zero option"
//      )
//    }
//
//    "accept the --help option" in {
//      runSilent("--help") should equal(0)
//    }
//  }
//
//  "A Serializer" must {
//
//    "convert the wine ontology" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--url-replace", "http://www.w3.org/TR/2003/PR-owl-guide-20031209/=http://whatever/",
//        s"$resourceDir/test-out-wine.rdf",
//        s"$resourceDir/wine.rdf"
//      ) should equal (0)
//    }
//
//    /*
//     * The wine ontology imports the food ontology (and vice versa), so the --base-dir
//     * and --base-url options need to allow the serializer to find the food ontology
//     */
//    "serialize the wine ontology and support the import of the food ontology" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", s"$fiboDir=http://www.w3.org/TR/2003/PR-owl-guide-20031209",
//        s"$resourceDir/test-out-wine.rdf",
//        s"$resourceDir/wine.rdf"
//      ) should equal (0)
//    }
//
//    "convert the fibo contracts ontology" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", s"$fiboDir=$omgFiboNamespaceIRI",
//        "--base", s"$specMetaDir=http://www.omg.org/techprocess/ab/",
//        "--url-replace", s"$omgFiboNamespaceIRI=$edmcFiboNamespaceIRI",
//        s"$resourceDir/test-out-fibo-fnd-contracts.rdf",
//        s"$fiboDir-fnd-contracts.rdf"
//      ) should equal (0)
//    }
//
//    "not generate errors int the output of test-case-001.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", s"$fiboDir=$omgFiboNamespaceIRI",
//        "--base", s"$specMetaDir=http://www.omg.org/techprocess/ab/",
//        s"$resourceDir/test-out-test-case-001.rdf",
//        s"$resourceDir/test-case-001.rdf"
//      ) should equal (0)
//    }
//
//    "not mess with the blank nodes in FIBO FND Ownership & Control - Control.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", s"$fiboDir=$omgFiboNamespaceIRI",
//        "--base", s"$specMetaDir=http://www.omg.org/techprocess/ab/",
//        s"$resourceDir/test-out-fibo-fnd-ownershipandcontrol-control.rdf",
//        s"$fiboDir/fnd/OwnershipAndControl/Control.rdf"
//      ) should equal (0)
//    }
//
//    "do all the imports right in FIBO FND Accounting - AccountingEquity.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", s"$fiboDir=$omgFiboNamespaceIRI",
//        "--base", s"$specMetaDir=http://www.omg.org/techprocess/ab/",
//        s"$resourceDir/test-out-fibo-fnd-accounting-equity.rdf",
//        s"$fiboDir/fnd/Accounting/AccountingEquity.rdf"
//      ) should equal (0)
//    }
//
//    "publish a FIBO ontology with all the right annotations and versionIRI" in {
//      run(
//        "--force",
//        "--abort",
//        "--publish",
//        "--base", s"$fiboNewDir=$edmcFiboNamespaceIRI",
//        "--base", s"$specMetaDir=http://www.omg.org/techprocess/ab/",
//        s"$resourceDir/test-out-fibo-fnd-accounting-equity-published.rdf",
//        s"$fiboNewDir/fnd/Accounting/AccountingEquity.rdf"
//      ) should equal (0)
//    }
//  }
//}
//
