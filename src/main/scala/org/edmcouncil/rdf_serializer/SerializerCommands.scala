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

import grizzled.slf4j.Logging


/**
 * All parameter info and command info for the Serializer
 */
private class SerializerCommands(
	val params: CommandLineParams
) extends Logging {

  implicit val codec = scala.io.Codec.UTF8

  lazy val input = PotentialFile(params.inputFileName)
  lazy val output = PotentialFile(params.outputFileName)
  lazy val baseDir = PotentialDirectory(params.baseDir)
  lazy val baseUrl = BaseURL(params.baseUrl)

  def validate = {

    var rc = 0

    input.fileName match {
      case Some(fileName) => info(s"Input File: $fileName")
      case _ => error("Input File has not been specified") ; rc = 1
    }
    if (input.fileExists) {
      info("Input File exists")
    } else {
      error("Input File does not exist")
      rc = 1
    }
    if (output.hasName) {
      info(s"Output File: ${output.fileName.get}")
    } else {
      error("Output File has not been specified")
      rc = 1
    }
    if (params.force) {
      info("Force option has been given")
    }
    Option(output.fileExists) match {
      case Some(true) if params.force =>  info("Output File exists")
      case Some(true) if ! params.force => error("Output File exists") ; rc = 1
      case Some(false) => error("Output File does not exist")
      case _ =>
    }
    if (baseDir.hasName && baseUrl.isSpecified) {
      if (!baseDir.directoryExists) {
        error(s"The given base directory does not exist: ${baseDir.name.get}")
        rc = 2
      } else {

      }
    } else {
      error("Specify either both the --base-dir and --base-url option or none")
    }

    rc
  }

}
