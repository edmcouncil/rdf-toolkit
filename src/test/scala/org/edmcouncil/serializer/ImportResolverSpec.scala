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
package org.edmcouncil.serializer

import org.edmcouncil.util.{ PotentialDirectory, BaseURL }
import org.semanticweb.owlapi.model.IRI

/**
 * Test the ImportResolver
 */
class ImportResolverSpec extends UnitSpec {

  "An ImportResolver" must {

    suppressOutput {

      val baseDir = PotentialDirectory("src/test")
      val baseUrl = BaseURL("http://whatever.com/")
      val testImportUrl = "http://whatever.com/resources/wine/"
      val testImportIri = IRI.create(testImportUrl)
      val resolver = ImportResolver(baseDir, baseUrl, testImportIri)

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
}
