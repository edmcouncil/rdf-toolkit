/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 *
 * The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package org.edmcouncil.rdf_serializer

import java.io.{FileOutputStream, FileInputStream, BufferedInputStream, File}
import java.nio.file.{Path, Paths, Files}

import org.semanticweb.owlapi.io.{OWLOntologyDocumentSource, StreamDocumentSource}

import scala.io.Source
import scala.util.Properties

/**
 * Convenience class that does all the common conversions for a given file
 */
class PotentialFile(name: Option[String]) {

  import org.edmcouncil.extension.StringExtensions._

  implicit val codec = scala.io.Codec.UTF8

  def hasName = name.isDefined

  lazy val file = name.map((fileName: String) => new File(fileName.asValidPathName))
  lazy val fileName = file.map(_.getAbsolutePath)
  lazy val fileExists = fileName.filter((fileName: String) => Files.exists(Paths.get(fileName))).isDefined
  lazy val inputStream = file.map((file: File) => new BufferedInputStream(new FileInputStream(file)))
  lazy val outputStream = file.map((file: File) => new FileOutputStream(file))

  val path = name.map((name: String) => Paths.get(name))

  lazy val inputSource = inputStream.map(Source.fromInputStream(_)(codec))
  lazy val inputDocumentSource = inputStream.map(new StreamDocumentSource(_))
}

object PotentialFile {

  def apply(name: Option[String]) = new PotentialFile(NormalizePathName(name))
  def apply(name: String) = new PotentialFile(Some(name))
}

/**
 * Convenience class that does all the common conversions for a given Directory
 */
class PotentialDirectory(val name: Option[String]) {

  def hasName = name.isDefined
  val directoryPath = name.map((name: String) => Paths.get(name))
  val directoryName = directoryPath.map(_.normalize().toString)
  def directoryExists = directoryPath.filter(
    (path: Path) => Files.exists(path)
  ).isDefined
}

object PotentialDirectory {

  def apply(name: Option[String]) = new PotentialDirectory(NormalizePathName(name))
  def apply(name: String) = new PotentialDirectory(Some(name))
}

object NormalizePathName {

  def normalize(name: String): String = {
    //
    // Check for unix paths that start with ~/. We do not support the tilde on Windows so no need
    // to use File.sep here
    //
    if (name.startsWith("~/")) {
      val home = Properties.envOrNone("HOME")
      if (home.isEmpty) {
        throw new RuntimeException(s"Can not interpret the given path name $name because HOME is not defined")
      }
      return normalize(s"${home.get}/${name.substring(2)}")
    }
    name
  }

  def apply(name: Option[String]) = name.map(normalize)
}
