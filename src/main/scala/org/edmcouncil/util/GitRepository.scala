package org.edmcouncil.util

import java.io.File
import java.nio.file.Path

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.util.FS

import scala.util.Try

class GitRepository(val repository: org.eclipse.jgit.lib.Repository) {

  lazy val git = new Git(repository)

  def baseDirectory = repository.getDirectory.toPath.getParent
  def config = repository.getConfig

  /**
   * @return the git remote URL that the repository uses, if any. Also assumes that that remote is called origin and
   * not something else.
   *
   * TODO: Need to find a generic way to get the remote url.
   */
  def url = config.getString("remote", "origin", "url")

  def close() = repository.close()
}

/**
 * Opens and returns a JGit Repository
 */
object GitRepository {

  private val builder = new FileRepositoryBuilder()

  /**
   * Starts from the supplied directory path and scans up through the parent
   * directory tree until a Git repository is found.
   */
  private def findGitDir(directory: Path) = Some(builder.findGitDir(directory.toFile).getGitDir)

  private def repositoryFor(gitBaseDir: File) = repositoryInCacheFor(gitBaseDir).map(new GitRepository(_))

  private def repositoryInCacheFor(gitBaseDir: File) =
    Try(RepositoryCache.open(RepositoryCache.FileKey.lenient(gitBaseDir, FS.DETECTED))).toOption

  /**
   * Opens and returns a JGit Repository for the given directory, walks the directory tree up to find the base
   * directory of the git repository.
   *
   * @param directory Path of one of the directories in the git repository
   * @return Option[Repository]
   */
  def apply(directory: Path) = findGitDir(directory).flatMap(repositoryFor)
}
