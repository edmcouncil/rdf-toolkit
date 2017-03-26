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
package org.edmcouncil.rdf_toolkit.command

import java.io.File
import java.nio.file.{ Files, Path, Paths }
import org.semanticweb.owlapi.model.IRI

object ConfigFile {

  private lazy val configFile: Option[File] = {
    val currentRelativePath = Paths.get("")
    findConfigFileInDirectory(currentRelativePath).map(_.toFile)
  }

  /**
   * Iteratively scan all directories up to the root directory to find the config file
   */
  private def findConfigFileInDirectory(directory: Path): Option[Path] = {
    if (Files.exists(directory) && Files.isDirectory(directory)) {
      val configFilePath = directory.resolve(CONFIG_FILE_NAME)
      if (Files.exists(configFilePath) && Files.isRegularFile(configFilePath)) {
        return Some(configFilePath)
      } else {
        findConfigFileInDirectory(directory.getParent)
      }
    }
    None
  }

  /**
   * @return the config file if found, otherwise None
   */
  def apply(): Option[File] = configFile

  /**
   * @return the root directory as an Option[File]
   */
  def rootDirectory = configFile.flatMap(file ⇒ Option(file.getAbsoluteFile.getParentFile))

  /**
   * @return the OWL Document IRI for the config file
   */
  def iri = configFile.map(IRI.create)

}
