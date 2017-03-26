package org.edmcouncil.rdf_serializer


import java.io.File
import java.nio.file.Paths

import org.edmcouncil.test.UnitSpec
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

      repo.foreach { (repo) =>
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
