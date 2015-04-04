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
