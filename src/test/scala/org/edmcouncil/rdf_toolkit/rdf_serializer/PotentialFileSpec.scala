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
package org.edmcouncil.rdf_toolkit.rdf_serializer

import org.edmcouncil.test_util.UnitSpec
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

      info(s"Filename is ${pf.fileName}")
      info(s"Path is ${pf.path}")
      info(s"URI is ${pf.uri}")
      info(s"Path is ${pf.path}")
      pf.printLog()
    }

  }
}
