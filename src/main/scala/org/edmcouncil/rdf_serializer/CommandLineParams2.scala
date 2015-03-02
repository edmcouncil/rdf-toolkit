package org.edmcouncil.rdf_serializer

import java.net.URI
import java.nio.file.Path

import org.clapper.argot.{ArgotConversionException, ArgotConverters, ArgotParser, ArgotUsageException}
import org.edmcouncil.util.{BaseURL, PotentialDirectory, PotentialFile}
import org.edmcouncil.{SerializerApiOWLAPI, SerializerApiSesame, SerializerApi}
import org.edmcouncil.main.BooterProperties

import scala.util.Try

object CommandLineParams2 {

  private val sep = "\n- "
  private def outputFormatsOwlApi = OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")
  private def outputFormatsSesame = SesameRdfFormatter.TARGET_FORMATS.split(",").map(_.trim).mkString(sep, sep, "")

  private def preUsageText = s"${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})"

  def apply(args: Array[String]) = new CommandLineParams2(args)
}

class CommandLineParams2 private (args: Array[String]) {

  import ArgotConverters._
  import CommandLineParams2._

  private val parser = new ArgotParser(
    programName = BooterProperties.name.toLowerCase,
    preUsage = Some(preUsageText),
    outputWidth = 120,
    sortUsage = false
  )

  val help = parser.flag[Boolean](
    List("h", "help"), "show this help"
  )

  def specifiedHelp = help.value.getOrElse(false)

  def showUsage = Try(parser.usage())

  private val versionFlag = parser.flag[Boolean](
    List("version"), s"show just the version of ${BooterProperties.name}: ${BooterProperties.versionFull}"
  )
  def shouldShowVersion = versionFlag.value.getOrElse(false)

  val verbose = parser.flag[Int](
    List("v", "verbose"),
    List("q", "quiet"),
    "Increment (-v, --verbose) or decrement (-q, --quiet) the verbosity level."
  ) { (onOff, opt) =>

    import scala.math

    val currentValue = opt.value.getOrElse(0)
    val newValue = if (onOff) currentValue + 1 else currentValue - 1
    math.max(0, newValue)
  }

  val noError = parser.flag[Boolean](List("n", "noerror"), "Do not abort on error. NIY")

  val forceFlag = parser.flag[Boolean](List("f", "force"), "Force output file to be overwritten if it exists")
  def force = forceFlag.value.getOrElse(false)

  private val apiOption = parser.option[SerializerApi](
    List("api"), "<api>",
    s"Specify whether you want to use the OWLAPI or Sesame. Default is OWLAPI. "
  ) { (s, opt) =>
    if (s.equalsIgnoreCase("sesame")) SerializerApiSesame else SerializerApiOWLAPI
  }
  def api = apiOption.value.getOrElse(SerializerApiOWLAPI)

  // --output-format <format> where <format> is one of (between quotes): $outputFormats
  val outputFormat = parser.option[String](
    List("ofmt", "output-format"),
    "<output-format>",
    s"Output formats for OWLAPI are: $outputFormatsOwlApi\n" +
    s"Output formats for Sesame are: $outputFormatsSesame"
  )

  val baseDirUrls = parser.multiOption[Tuple2[Path, BaseURL]](
    List("base-dir-url", "base-dir-uri", "base"),
    "<base-dir>=<uri>",
    "..."
  ) { (s, opt) =>

    val splitted = s.split('=')
    val pathString = splitted(0)
    val uriString = splitted(1)

    val path = {
      val potentialDirectory = PotentialDirectory(pathString)
      if (! potentialDirectory.directoryExists) throw new ArgotConversionException(
        s"Option $opt: Cannot parse $pathString to an existing directory."
      )
      potentialDirectory.directoryPath.get
    }

    val uri = BaseURL(uriString)

    (path, uri)
  }

  val outputFile = parser.parameter[PotentialFile](
    "output-file",
    "Output file to which to write.",
    optional = false
  ) { (s, opt) =>
    PotentialFile(s)
  }

  val inputFiles = parser.multiParameter[PotentialFile](
    "input-file(s)",
    "Input files to read. You can specify multiple input files, but they will then all be merged into one output file.",
    optional = false
  ) { (s, opt) =>

    val file = PotentialFile(s)
    if (! file.fileExists)
      parser.usage("Input file \"" + s + "\" does not exist.")

    file
  }

  def parse(): Int = try {
    parser.parse(args)
    0
  } catch {
    case e: ArgotUsageException =>
      println(e.message)
      if (specifiedHelp) 0 else 1
  }
}