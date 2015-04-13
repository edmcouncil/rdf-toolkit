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
package org.edmcouncil.util

import java.io.File
import java.nio.file.Path

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.util.FS

import scala.util.Try

/**
 * Wrapper around a JGit Repository
 *
 * @param repository the JGit Repository
 */
class GitRepository private (val repository: org.eclipse.jgit.lib.Repository) {

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

  def sha = {
    //
    // Do the equivalent of "git rev-parse HEAD^{tree}"
    //
    val objectId = repository.resolve("HEAD^{tree}")

    objectId.name
  }

  def shortSha = {
    //
    // Do the equivalent of "git rev-parse --short HEAD^{tree}"
    //
    val objectId = repository.resolve("HEAD^{tree}")
    val reader = repository.newObjectReader
    try {
      reader.abbreviate(objectId, 7).name
    } finally {
      reader.release()
    }
  }
}

/**
 * Opens and returns a JGit Repository wrapped in an instance of the Option[GitRepository] class
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
