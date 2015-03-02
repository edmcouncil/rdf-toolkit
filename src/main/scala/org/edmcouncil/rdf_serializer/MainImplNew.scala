package org.edmcouncil.rdf_serializer

import java.io.File
import java.net.URI
import java.nio.file.{Files, Paths, Path}
import org.clapper.argot.{ArgotConversionException, ArgotUsageException, ArgotConverters, ArgotParser}
import org.edmcouncil.SerializerApiOWLAPI
import org.edmcouncil.main.BooterProperties

/**
 * Allow for the MainImpl to be executed from tests bypassing Main.
 */
object MainImplNew {

  def apply(args: Array[String]) = new MainImplNew(args)
}

/**
 * The "real" Main of the RDF Serializer.
 */
class MainImplNew(args: Array[String]) {

  val params = CommandLineParams2(args)

  def run : Int = {
    //  Serializer(params)
    0
  }
}


object CommandLineParams2 {

  private val sep = "\n- "
  private def outputFormatsOwlApi = OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")
  private def outputFormatsSesame = SesameRdfFormatter.TARGET_FORMATS.split(",").map(_.trim).mkString(sep, sep, "")

  private def preUsageText = s"${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})"

  def apply(args: Array[String]) = {
    try {
      new CommandLineParams2(args)
    }
    catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
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

  val version = parser.flag[Boolean](
    List("version"), s"show just the version of ${BooterProperties.name}: ${BooterProperties.versionFull}"
  )

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

  val noError = parser.flag[Boolean](List("n", "noerror"), "Do not abort on error.")

  val force = parser.flag[Boolean](List("f", "force"), "Force output file to be overwritten if it exists")

  val api = parser.option[String](
    List("api"), "<api>",
    s"Specify whether you want to use the OWLAPI or Sesame. Default is OWLAPI. "

  )

  // --output-format <format> where <format> is one of (between quotes): $outputFormats
  val outputFormat = parser.option[String](
    List("ofmt", "output-format"),
    "<output-format>",
    s"Output formats for OWLAPI are: $outputFormatsOwlApi" +
      s"\nOutput formats for Sesame are: $outputFormatsSesame"
  )

  val baseDirUrls = parser.option[Tuple2[Path, URI]](
    List("base-dir-url", "base-dir-uri"),
    "<base-dir>=<uri>",
    "..."
  ) { (s, opt) =>

    val splitted = s.split('=')
    val pathString = splitted(0)
    val uriString = splitted(1)

    val path = {
      val potentialDirectory = PotentialDirectory(pathString)
      if (! potentialDirectory.directoryExists) throw new ArgotConversionException(
        "Option \"" + opt + "\": " +
          "Cannot parse \"" + pathString + "\" to an existing directory."
      )
      potentialDirectory.directoryPath.get
    }

    val uri = new URI(uriString)

    (path, uri)
  }
  
  val output = parser.parameter[String](
    "output-file",
    "Output file to which to write.",
    optional = false
  )

  val inputFiles = parser.multiParameter[File](
    "input-file",
    "Input files to read. If not specified, use stdin.",
    optional = true
  ) { (s, opt) =>

    val file = new File(s)
    if (! file.exists)
      parser.usage("Input file \"" + s + "\" does not exist.")

    file
  }

  parser.parse(args)
}
