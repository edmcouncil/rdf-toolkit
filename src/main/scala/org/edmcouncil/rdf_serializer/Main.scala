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

import org.edmcouncil.{SerializerApiOWLAPI, SerializerApiSesame}
import org.edmcouncil.extension.StringExtensions._
import org.edmcouncil.main.BooterProperties

/**
 * The Main class of the RDF Serializer
 */
object Main extends App {

  //
  // The whole program is a function returning an Int to the command line, which we need in order to use this
  // program in Bash scripts or Windows scripts.
  //
  sys.exit(MainImpl(args).run)
}

/**
 * Allow for the MainImpl to be executed from tests bypassing Main.
 */
object MainImpl {

  def apply(args: Seq[String]) = new MainImpl(args)
}

class MainImpl private (args : Seq[String]) {

  private type OptionMap = Map[Symbol, Any]

  private val defaultParams = CommandLineParams()

  private lazy val options = nextOption(Map(), args.toList)

  private lazy val optionShowVersion = options.contains('version)

  private lazy val optionVerbose = options.contains('verbose)

  private lazy val optionDebug = options.contains('debug)

  private lazy val optionForce = options.contains('force)

  private lazy val optionApi = if (
    options.contains('api) && options('api).asInstanceOf[String].equalsIgnoreCase("sesame")
  ) SerializerApiSesame else SerializerApiOWLAPI

  private lazy val optionInputFileName = if (options.contains('inputFileName))
    Some(options('inputFileName).asInstanceOf[String])
  else None

  private lazy val optionOutputFileName = if (options.contains('outputFileName))
    Some(options('outputFileName).asInstanceOf[String])
  else None

  private lazy val optionOutputFormatName = if (options.contains('outputFormatName))
    Some(options('outputFormatName).asInstanceOf[String])
  else None

  private lazy val optionBaseDir = if (options.contains('baseDir))
    Some(options('baseDir).asInstanceOf[String])
  else None

  private lazy val optionBaseUrl = if (options.contains('baseUrl))
    Some(options('baseUrl).asInstanceOf[String])
  else None

  private lazy val optionBaseDirUrl = if (options.contains('baseDirUrl))
    Some(options('baseDirUrl).asInstanceOf[String])
  else None

  private lazy val optionUsage = options.contains('usage)

  private lazy val optionUnknown = options.contains('unknown)

  private lazy val params = defaultParams.copy(
    verbose = optionVerbose,
    debug = optionDebug,
    force = optionForce,
    api = optionApi,
    inputFileName = optionInputFileName,
    outputFileName = optionOutputFileName,
    outputFormatName = optionOutputFormatName,
    baseDir = optionBaseDir,
    baseUrl = optionBaseUrl
  )

  private def nextOption(map_ : OptionMap, list_ : List[String]) : OptionMap = {
    list_ match {
      case Nil ⇒ map_
      case "--version" :: tail =>
        nextOption(map_ ++ Map('version -> true), tail)
      case "--verbose" :: tail ⇒
        nextOption(map_ ++ Map('verbose -> true), tail)
      case "--debug" :: tail ⇒
        nextOption(map_ ++ Map('debug -> true), tail)
      case "--force" :: tail ⇒
        nextOption(map_ ++ Map('force -> true), tail)
      case "--help" :: tail ⇒
        nextOption(map_ ++ Map('usage -> true), tail)
      case "--api" :: value :: tail ⇒
        nextOption(map_ ++ Map('api -> value), tail)
      case "--input-file" :: value :: tail =>
        nextOption(map_ ++ Map('inputFileName -> value), tail)
      case "--output-file" :: value :: tail =>
        nextOption(map_ ++ Map('outputFileName -> value), tail)
      case "--output-format" :: value :: tail =>
        nextOption(map_ ++ Map('outputFormatName -> value), tail)
      case "--base-dir" :: value :: tail =>
        nextOption(map_ ++ Map('baseDir -> value), tail)
      case "--base-url" :: value :: tail =>
        nextOption(map_ ++ Map('baseUrl -> value), tail)
      case "--base-dir-url" :: value :: tail =>
        nextOption(map_ ++ Map('baseDirUrl -> value), tail)
      case option :: tail ⇒
        println(s"""ERROR: Unknown option "$option"!""")
        nextOption(map_ ++ Map('unknown -> true), tail)
    }
  }

  private def showUsage() {

    val sep = "\n                           - "
    def outputFormats = if (optionApi == SerializerApiOWLAPI)
      OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")
    else
      SesameRdfFormatter.TARGET_FORMATS.split(",").map(_.trim).mkString(sep, sep, "")

    println(s"""
      |${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})
      |
      |Usage: ${BooterProperties.name.toLowerCase} [--verbose] [--help] [--debug] [--force]
      |  [--api <owlapi|sesame>]
      |  [--input-file <path>] [--output-file <path>] [--output-format <format>]
      |  [--base-dir <path> --base-url <url>]
      |
      |Where:
      |  --version                show just the version of ${BooterProperties.name} (${BooterProperties.versionFull})
      |  --verbose                switch on verbose logging (sets INFO level logging).
      |  --debug                  switch on debug level logging.
      |  --force                  force output file to be overwritten if it exists.
      |  --api <api>              specify whether you want to use the OWLAPI or Sesame. Default is OWLAPI. Output
      |                           formats shown below under 'output-format' will be different depending on this option,
      |                           use '--help --api sesame' to see the formats supported by the Sesame API.
      |  --help                   this help.
      |  --input-file <path>
      |  --output-file <path>
      |  --output-format <format> where <format> is one of (between quotes): $outputFormats
      |  --base-dir <path>        root directory where imported ontologies can be found
      |  --base-url <url>         the base url of imported ontologies that matches with the <path> specified with
      |                           the --base-dir option.

      """.stripMargin
    )
  }

  private def showVersion : Int = {

    println(BooterProperties.versionFull)

    0
  }

  def run : Int = {
    if (optionShowVersion) {
      showVersion
    } else if (optionUnknown || optionUsage || args.length == 0) {
      showUsage()
      if (optionUnknown) 1 else 0
    } else {
      //
      // Run the Serializer as if it were a function that returns an Int
      //
      Serializer(params)
    }
  }
}


