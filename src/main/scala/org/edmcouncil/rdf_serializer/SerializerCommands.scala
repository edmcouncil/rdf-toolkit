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

    rc
  }

}
