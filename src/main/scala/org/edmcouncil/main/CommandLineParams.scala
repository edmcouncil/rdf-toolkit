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
package org.edmcouncil.main

import java.nio.file.Path

import org.clapper.argot.{ ArgotConversionException, ArgotUsageException }
import org.edmcouncil.{ SerializerApi, SerializerApiOWLAPI }
import org.edmcouncil.serializer.OwlApiOutputFormats
import org.edmcouncil.rdf_toolkit.{ SesameRdfFormatter, SesameSortedRDFWriterFactory }
import org.edmcouncil.util.{ BaseURL, PotentialDirectory, PotentialFile }
import scopt.{ OptionDef, Read }

import scala.util.matching.Regex

/**
 * scopt based Command Line Interface
 */
object CommandLineParams {

  private val spacing = "                           "
  private val sep = s"\n$spacing - "
  private def outputFormatsOwlApi = OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")
  private def outputFormatsSesame = SesameSortedRDFWriterFactory.TargetFormats.values.mkString(sep, sep, "")

  private def preUsageText = s"${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})"

  def apply(args: Array[String]) = new CommandLineParams(args)

}
class CommandLineParams private (args: Array[String]) {

  import CommandLineParams._

  var shouldShowVersion = false
  var abortOnError = false
  var urlReplacePattern: Option[Map[Regex, String]] = None
  var outputFile: Option[PotentialFile] = None
  var baseDirUrls: Option[Map[BaseURL, Path]] = None
  var outputFormat: Option[String] = None
  var inputFiles: Seq[PotentialFile] = Seq()

  def reads[A](f: String ⇒ A): Read[A] = new Read[A] {
    val arity = 1
    val reads = f
  }

  implicit val regexRead: Read[Regex] = reads { _.r }
  implicit val potentialFileRead: Read[PotentialFile] = reads { PotentialFile(_) }
  implicit val pathRead: Read[Path] = reads { string: String ⇒
    val potentialDirectory = PotentialDirectory(string)
    if (!potentialDirectory.exists) throw new IllegalArgumentException(
      s"Cannot parse $string to an existing directory."
    )
    potentialDirectory.path.get
  }
  implicit val baseUrlRead: Read[BaseURL] = reads { BaseURL(_) }

  private val parser = new scopt.OptionParser[Unit](BooterProperties.name) {
    head(BooterProperties.name, BooterProperties.version)

    override def showUsageOnError = true

    help("help")
      .text("prints this help text")

    opt[Unit]('v', "version")
      .text(s"show just the version of ${BooterProperties.name}: ${BooterProperties.versionFull}")
      .foreach(x ⇒ shouldShowVersion = true)

    opt[Unit]('a', "abort")
      .text("Abort on error")
      .foreach(x ⇒ abortOnError = true)

    cmd("serialize")
      .text("copy a given input RDF file (or files) and rewrite or \"serialize\" it (in the same or another format) to a new file")
      .children(serializationOptions)

    private def serializationOptions: OptionDef[_, Unit] = {
      opt[Map[Regex, String]]("url-replace")
        .valueName("<pattern>=<replacement string>,<pattern>=<replacement string>,..")
        .text("Replace any part of a Subject, Predicate or Object URI that matches with the given <pattern> with the given <replacement string>")
        .foreach(x ⇒ urlReplacePattern = Some(x))

      opt[PotentialFile]("output-file")
        .text("Output file to which to write.")
        .valueName("<file>")
        .optional()
        .foreach(x ⇒ outputFile = Some(x))

      opt[Map[BaseURL, Path]]("base-dir-url")
        .valueName("<uri>=<base-dir>,<uri>=<base-dir>,..")
        .text("...todo...")
        .optional()
        .foreach(x ⇒ baseDirUrls = Some(x))

      opt[String]("output-format")
        .valueName("<output-format>")
        .text(
          s"Output formats for OWLAPI are: $outputFormatsOwlApi\n" +
            s"${spacing}Output formats for RDF4J are: $outputFormatsSesame"
        )
        .foreach(x ⇒ outputFormat = Some(x))

      opt[Seq[PotentialFile]]("input-files")
        .required()
        .valueName("<file>")
        .text("Input files to read. You can specify multiple input files, but they will then all be merged into one output file.")
        .foreach(potentialFileSeq ⇒ Some(potentialFileSeq))
        .validate(potentialFileSeq ⇒
          success /* TODO
          potentialFileSeq.foreach { potentialFile =>
            if (potentialFile.fileExists)
              success
            else
              failure(s"Input file ${potentialFile.fileName.get} does not exist.")
          } */
        )
    }

  }

  def api: SerializerApi = SerializerApiOWLAPI

  def parse(): Int = try {
    if (parser.parse(args)) 0 else 1
  } catch {
    case e: Throwable ⇒
      println(e)
      1
  }

}
