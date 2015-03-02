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

package org.edmcouncil.util

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.file.{Files, Paths}

import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model.IRI

import scala.io.Source

/**
 * Convenience class that does all the common conversions for a given file
 */
class PotentialFile private (name: Option[String]) {

  import org.edmcouncil.extension.StringExtensions._

  implicit val codec = scala.io.Codec.UTF8

  def hasName = name.isDefined

  lazy val file = name.map((fileName: String) => new File(fileName.asValidPathName))
  lazy val fileName = file.map(_.getAbsolutePath)
  lazy val path = fileName.map(Paths.get(_))
  lazy val fileExists = fileName.filter((fileName: String) => Files.exists(Paths.get(fileName))).isDefined
  lazy val uri = path.map(_.toUri)
  lazy val iri = uri.map(IRI.create)
  lazy val inputStream = file.map((file: File) => new BufferedInputStream(new FileInputStream(file)))
  lazy val outputStream = file.map((file: File) => new FileOutputStream(file))

  lazy val inputSource = inputStream.map(Source.fromInputStream(_)(codec))
  lazy val inputDocumentSource = inputStream.map(new StreamDocumentSource(_, iri.orNull))
}

object PotentialFile {

  def apply(name: Option[String]) = new PotentialFile(NormalizePathName(name))
  def apply(name: String) = new PotentialFile(Some(name))
}
