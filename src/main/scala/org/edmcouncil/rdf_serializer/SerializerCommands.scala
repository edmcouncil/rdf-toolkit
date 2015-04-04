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
//package org.edmcouncil.rdf_serializer
//
//import grizzled.slf4j.Logging
//
//
///**
// * All parameter info and command info for the Serializer
// */
//private class SerializerCommands(
//	val params: CommandLineParams
//) extends Logging {
//
//  implicit val codec = scala.io.Codec.UTF8
//
//  lazy val input = PotentialFile(params.inputFileName)
//  lazy val output = PotentialFile(params.outputFileName)
//  lazy val baseDir = PotentialDirectory(params.baseDir)
//  lazy val baseUrl = BaseURL(params.baseUrl)
//
//
//  private def validateInput: Int = {
//    input.fileName match {
//      case Some(fileName) => info(s"Input File: $fileName")
//      case _ => error("Input File has not been specified") ; return 1
//    }
//    if (input.fileExists) {
//      info("Input File exists")
//      0
//    } else {
//      error("Input File does not exist")
//      1
//    }
//  }
//
//  private def validateOutput: Int = {
//    if (output.hasName) {
//      info(s"Output File: ${output.fileName.get}")
//    } else {
//      error("Output File has not been specified")
//      return 1
//    }
//    if (params.force) {
//      info("Force option has been given")
//    }
//    Option(output.fileExists) match {
//      case Some(true) if params.force =>  info("Output File exists")
//      case Some(true) if ! params.force => error("Output File exists") ; return 1
//      case Some(false) => error("Output File does not exist")
//      case _ =>
//    }
//    0
//  }
//
//  private def validateBase: Int = {
//    if (baseDir.hasName && baseUrl.isSpecified) {
//      if (!baseDir.directoryExists) {
//        error(s"The given base directory does not exist: ${baseDir.name.get}") ; return 2
//      } else {
//        info(s"Found the specified base directory: ${baseDir.name}")
//      }
//    } else {
//      error("Specify either both the --base-dir and --base-url option or none") ; return 2
//    }
//    0
//  }
//
//  def validate = {
//
//    var rc = validateInput
//
//    rc = if (rc == 0) validateOutput else rc
//
//    rc = if (rc == 0) validateBase else rc
//
//    rc
//  }
//
//}
