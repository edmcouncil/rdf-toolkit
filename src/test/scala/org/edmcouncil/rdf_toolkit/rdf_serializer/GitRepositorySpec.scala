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

import java.io.File
import java.nio.file.Paths

import org.edmcouncil.test_util.UnitSpec
import org.edmcouncil.util.GitRepository

/**
 * Test the GitRepository object
 */
class GitRepositorySpec extends UnitSpec {

  val resourcesDir = "src/test/resources"
  val someDir = Paths.get(new File(resourcesDir).getAbsolutePath)

  "A GitRepository" must {

    val repo = GitRepository(someDir)

    "be found and opened with any given sub-directory of the repository" in {

      assert(repo.isDefined, s"Could not find git repo for directory $someDir")

      repo.foreach { (repo) ⇒
        info(s"Git repo is ${repo.baseDirectory}")
        info(s"Git url is ${repo.url}")
      }

    }

    "return the short SHA of the current HEAD" in {

      assert(repo.isDefined, s"Could not find git repo for directory $someDir")

      val shortSha = repo.get.shortSha

      //info(s"shortSha: $shortSha")

      assert(shortSha.length == 7)
    }

    repo.foreach(_.close())
  }
}
