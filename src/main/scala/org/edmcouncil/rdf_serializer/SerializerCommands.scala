package org.edmcouncil.rdf_serializer

import java.io._

import grizzled.slf4j.Logging

import scala.io.Source

/**
 * All parameter info and command info for the Serializer
 */
private class SerializerCommands(
	val params: CommandLineParams
) extends Logging {

  import org.edmcouncil.extension.StringExtensions._

  implicit val codec = scala.io.Codec.UTF8

  //
  // Input
  //
  lazy val hasInputFileName = params.inputFileName.isDefined
  lazy val inputFile = params.inputFileName.map((fileName: String) => new File(fileName.asValidPathName))
  lazy val inputFileExists = inputFile.map(_.exists)
  lazy val inputFileStream = inputFile.map((file: File) => new BufferedInputStream(new FileInputStream(file)))

  lazy val inputFileAsSource = inputFileStream.map(Source.fromInputStream(_)(codec))

  def inputFileName = inputFile.map(_.getAbsolutePath)

  //
  // Output
  //
  lazy val outputFile = params.outputFileName.map((fileName: String) => new File(fileName.asValidPathName))
  lazy val outputFileExists = outputFile.map(_.exists)
  lazy val outputFileStream = outputFile.map((file: File) => new FileOutputStream(file))

  def outputFileName = outputFile.map(_.getAbsolutePath)


  def validateParams = {

    var rc = 0

    inputFileName match {
      case Some(fileName) => info(s"Input File: $fileName")
      case _ => error("Input File has not been specified") ; rc = 1
    }
    inputFileExists match {
      case Some(true) => info("Input File exists")
      case Some(false) => error("Input File does not exist") ; rc = 1
      case _ =>
    }
    outputFileName match {
      case Some(fileName) => info(s"Output File: $fileName")
      case _ => error("Output File has not been specified") ; rc = 1
    }
    if (params.force) {
      info("Force option has been given")
    }
    outputFileExists match {
      case Some(true) if params.force =>  info("Output File exists")
      case Some(true) if ! params.force => error("Output File exists") ; rc = 1
      case Some(false) => error("Output File does not exist")
      case _ =>
    }

    rc
  }

}
