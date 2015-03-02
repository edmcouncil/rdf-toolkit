package org.edmcouncil.util

import java.nio.file.{Files, Path, Paths}

object PotentialDirectory {

  def apply(name: Option[String]) = new PotentialDirectoryByName(NormalizePathName(name))
  def apply(name: String) = new PotentialDirectoryByName(Some(name))
  def apply(path: Path) = new PotentialDirectoryByPath(Some(path))
}

/**
 * Convenience class that does all the common conversions for a given Directory
 */
sealed trait PotentialDirectory {

  def name: Option[String]
  def hasName = name.isDefined
  def directoryPath: Option[Path]
  def directoryName: Option[String] = directoryPath.map(_.normalize().toString)
  def directoryExists = directoryPath.filter(
    (path: Path) => Files.exists(path)
  ).isDefined
}

class PotentialDirectoryByName private[util] (val name: Option[String]) extends PotentialDirectory {

  val directoryPath = name.map((name: String) => Paths.get(name))
}

class PotentialDirectoryByPath private[util] (val directoryPath: Option[Path]) extends PotentialDirectory {

  def name = Some(directoryPath.toString)
}
