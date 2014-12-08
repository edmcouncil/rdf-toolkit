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

  def validateParams = {

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
