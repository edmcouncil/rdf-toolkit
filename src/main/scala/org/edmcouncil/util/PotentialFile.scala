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

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.file.{Files, Paths}

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Ref, Repository}
import org.semanticweb.owlapi.io.{OWLOntologyDocumentSource, StreamDocumentSource}
import org.semanticweb.owlapi.model.IRI

import scala.collection.JavaConverters._
import scala.io.Source

/**
 * Convenience class that does all the common conversions for a given file.
 *
 * This is not a really efficient class, it keeps everything it creates in its lazy instance variables.
 * It also does not close the File, you have to do that yourself.
 */
class PotentialFile private (name: Option[String]) {

  import org.edmcouncil.extension.StringExtensions._

  implicit val codec = scala.io.Codec.UTF8

  /**
   * @return true when the name of the PotentialFile is actually specified
   */
  def hasName = name.isDefined

  /**
   * The file as a File
   */
  lazy val file = name.map((fileName: String) ⇒ new File(fileName.asValidPathName))

  /**
   * The absolute full path of the file as a String
   */
  lazy val fileName = file.map(_.getAbsolutePath)

  /**
   * The absolute full path of the file as a Path
   */
  lazy val path = fileName.map(Paths.get(_))

  /**
   * The directory of the file as a Path
   */
  lazy val directory = path.map(_.getParent)

  lazy val fileExists = fileName.filter((fileName: String) ⇒ Files.exists(Paths.get(fileName))).isDefined

  /**
   * The absolute full path of the file as an URI
   */
  lazy val uri = path.map(_.toUri)

  /**
   * The absolute full path of the file as an IRI
   */
  lazy val iri = uri.map(IRI.create)

  /**
   * The file as an InputStream
   */
  lazy val inputStream = file.map((file: File) ⇒ new BufferedInputStream(new FileInputStream(file)))

  /**
   * The file as an OutputStream
   */
  lazy val outputStream = file.map((file: File) ⇒ new FileOutputStream(file))

  lazy val inputSource = inputStream.map(Source.fromInputStream(_)(codec))
  lazy val inputDocumentSource: Option[OWLOntologyDocumentSource] = inputStream.map(new StreamDocumentSource(_, iri.orNull))

  /**
   * The git repo in which the file resides
   */
  lazy val gitRepo = directory.flatMap(GitRepository(_))

  //lazy val lastLog = git.map((git: Git) => git.log().add(gitRepo.resolve(Constants.HEAD)).addPath())

  def printLog(): Unit = {
    if (gitRepo.isDefined) {
      val call = gitRepo.get.git.tagList().call().asScala
      for (ref ← call) {
        println(s"Tag: $ref ${ref.getName} ${ref.getObjectId.getName}")

        PotentialFile.listReflog(gitRepo.get.repository, ref)
      }
    }
  }

  override def toString: String = {
    if (hasName) {
      name.get + (
        if (fileName.isDefined) {
          s"==${fileName.get}" + (
            if (fileExists) "->(exists)" else "->(does not exist)"
          )
        } else s"==no absolute path"
      )
    } else "no name"
  }
}

object PotentialFile {

  def apply(name: Option[String]) = new PotentialFile(NormalizePathName(name))
  def apply(name: String) = new PotentialFile(Some(name))

  private def listReflog(repository: Repository, ref: Ref): Unit = {
    /*
     * Ref head = repository.getRef(ref.getName());
     * RevWalk walk = new RevWalk(repository);
     * RevCommit commit = walk.parseCommit(head.getObjectId());
     */
    val call = new Git(repository).reflog().setRef(ref.getName).call().asScala

    for (reflog ← call) {
      println(s"Reflog: $reflog")
    }
  }
}

