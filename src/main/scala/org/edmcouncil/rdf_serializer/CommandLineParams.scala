package org.edmcouncil.rdf_serializer

/**
 * This case class represents the collection of settings that can be supplied via the command line.
 */
case class CommandLineParams(
  verbose: Boolean = false,
  debug : Boolean = false,
  force: Boolean = false,
  inputFileName: Option[String] = None,
  outputFileName: Option[String] = None,
  outputFormatName: Option[String] = None,
  baseDir: Option[String] = None,
  baseUrl: Option[String] = None
)
