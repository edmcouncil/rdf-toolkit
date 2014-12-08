package org.edmcouncil.rdf_serializer

import org.edmcouncil.extension.StringExtensions._
import org.edmcouncil.main.BooterProperties

/**
 * The Main class of the RDF Serializer
 */
object Main extends App {

  sys.exit(MainImpl(args).run)
}

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

  private lazy val optionUsage = options.contains('usage)

  private lazy val optionUnknown = options.contains('unknown)

  private lazy val params = defaultParams.copy(
    verbose = optionVerbose,
    debug = optionDebug,
    force = optionForce,
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
      case option :: tail ⇒
        println(s"""ERROR: Unknown option "$option"!""")
        nextOption(map_ ++ Map('unknown -> true), tail)
    }
  }

  private def showUsage() {

    val sep = "\n                           - "
    def outputFormats = OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")

    println(s"""
      |${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})
      |
      |Usage: ${BooterProperties.name.toLowerCase} [--verbose] [--help] [--debug] [--force]
      |  [--input-file <path>] [--output-file <path>] [--output-format <format>]
      |  [--base-dir <path> --base-url <url>]
      |
      |Where:
      |  --version                show just the version of ${BooterProperties.name} (${BooterProperties.versionFull})
      |  --verbose                switch on verbose logging (sets INFO level logging).
      |  --debug                  switch on debug level logging.
      |  --force                  force output file to be overwritten if it exists.
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
      Serializer(params).run
    }
  }
}


