package org.edmcouncil.rdf_serializer

import java.io.File
import java.nio.file.Paths

import org.edmcouncil.util.GitRepository

/**
 * Test the GitRepository object
 */
class GitRepositorySpec extends UnitSpec {

  "A GitRepository" must {
    "be found and opened with any given sub-directory of the repository" in {
      val someDir = Paths.get(new File("src/test/resources").getAbsolutePath)

      val repo = GitRepository(someDir)

      assert(repo.isDefined, s"Could not find git repo for directory $someDir")

      repo.foreach { (repo) =>
        info(s"Git repo is ${repo.baseDirectory}")
        info(s"Git url is ${repo.url}")
      }

      repo.foreach(_.close())
    }
  }
}
