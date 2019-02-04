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

//package org.edmcouncil.serializer
//
//import java.io.OutputStream
//
//import org.edmcouncil.main.MainImpl
//
///**
// * Test the Serializer
// */
//class SerializerSpec extends UnitSpec {
//
//  def runSilent(args: String*): Int = 1 /* suppressOutput {
//    run(args: _*)
//  } */
//  def run(args: String*): Int = 1 // MainImpl(args).run
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
//    "return 1 when an invalid option is passed" in {
//      runSilent("--whatever") shouldEqual 1
//    }
//
//    "accept the --help option" in {
//      runSilent("--help") shouldEqual 0
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
//        "src/test/resources/test-out-wine.rdf",
//        "src/test/resources/wine.rdf"
//      ) should equal(0)
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
//        "--base", "src/test/resources=http://www.w3.org/TR/2003/PR-owl-guide-20031209",
//        "src/test/resources/test-out-wine.rdf",
//        "src/test/resources/wine.rdf"
//      ) should equal(0)
//    }
//
//    "convert the fibo contracts ontology" in {
//      run(
//        "--force",
//        "--abort",
//        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
//        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
//        "--url-replace", "http://www.omg.org/spec/EDMC-FIBO/=http://spec.edmcouncil.org/fibo/",
//        "src/test/resources/test-out-fibo-fnd-contracts.rdf",
//        "src/test/resources/fibo-fnd-contracts.rdf"
//      ) should equal(0)
//    }
//
//    "not generate errors int the output of test-case-001.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
//        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
//        "src/test/resources/test-out-test-case-001.rdf",
//        "src/test/resources/test-case-001.rdf"
//      ) should equal(0)
//    }
//
//    "not mess with the blank nodes in FIBO FND Ownership & Control - Control.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
//        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
//        "src/test/resources/test-out-fibo-fnd-ownershipandcontrol-control.rdf",
//        "src/test/resources/fibo/fnd/OwnershipAndControl/Control.rdf"
//      ) should equal(0)
//    }
//
//    "do all the imports right in FIBO FND Accounting - AccountingEquity.rdf" in {
//      runSilent(
//        "--force",
//        "--abort",
//        "--base", "src/test/resources/fibo=http://www.omg.org/spec/EDMC-FIBO",
//        "--base", "src/test/resources/fibo/etc/testing/data=http://www.omg.org/techprocess/ab/",
//        "src/test/resources/test-out-fibo-fnd-accounting-equity.rdf",
//        "src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf"
//      ) should equal(0)
//    }
//  }
//}
