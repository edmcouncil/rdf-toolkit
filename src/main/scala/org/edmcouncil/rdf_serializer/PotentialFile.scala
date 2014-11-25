package org.edmcouncil.rdf_serializer

import java.io.{FileOutputStream, FileInputStream, BufferedInputStream, File}
import java.nio.file.{Paths, Files}

import org.semanticweb.owlapi.io.{OWLOntologyDocumentSource, StreamDocumentSource}

import scala.io.Source

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

  lazy val inputSource = inputStream.map(Source.fromInputStream(_)(codec))
  lazy val inputStreamSource = inputStream.map(new StreamDocumentSource(_))
}

object PotentialFile {

  def apply(name: Option[String]) = new PotentialFile(name)
  def apply(name: String) = new PotentialFile(Some(name))
}