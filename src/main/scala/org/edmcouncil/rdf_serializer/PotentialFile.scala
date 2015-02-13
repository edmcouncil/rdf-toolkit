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