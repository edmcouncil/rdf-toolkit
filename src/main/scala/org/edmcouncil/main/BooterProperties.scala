package org.edmcouncil.main

/**
 * Loads booter.properties which contains some generated SBT values
 */
object BooterProperties {

  val properties = PropertiesFile("booter.properties").properties

  val organization = properties.getOrElse("booter.organization", "unknown")
  val name = properties.getOrElse("booter.name", "unknown")
  val version = properties.getOrElse("booter.version", "0")
  val systemRelease = properties.getOrElse("booter.git.hash.short", "0")
  val systemHash = properties.getOrElse("booter.git.hash.long", "0")
  val systemBranch = properties.getOrElse("booter.git.branch", "unknown")
  val scalaVersion = properties.getOrElse("booter.scala.version", "0")
  val gitHashLong = properties.getOrElse("booter.git.hash.long", "0")
  val gitHashShort = properties.getOrElse("booter.git.hash.short", "0")
  val gitBranch = properties.getOrElse("booter.git.branch", "0")
  val generatedAt = properties.getOrElse("booter.generated.at", "0")

  def versionFull = s"$version-$systemRelease"

  def nameWithVersion = s"$name $versionFull"
}
