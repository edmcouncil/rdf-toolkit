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

//package org.edmcouncil.main
//
//import java.nio.file.Path
//
//import org.backuity.clist.{ Cli, Command, args, opt }
//import org.backuity.clist.util.{ Read, ReadMultiple }
//import org.edmcouncil.serializer.{ OwlApiOutputFormats, SesameRdfFormatter }
//import org.edmcouncil.util.{ BaseURL, PotentialDirectory, PotentialFile }
//
//import scala.collection.immutable.HashMap
//import scala.util.matching.Regex
//
///**
// * Created by jgeluk on 02/01/2017.
// */
//object CommandLineInterfaceOld {
//
//  private val spacing = "                           "
//  private val sep = s"\n$spacing - "
//  private def outputFormatsOwlApi = OwlApiOutputFormats.outputDocumentFormatNames.mkString(sep, sep, "")
//  private def outputFormatsSesame = SesameRdfFormatter.TARGET_FORMATS.split(",").map(_.trim).mkString(sep, sep, "")
//
//  private def preUsageText = s"${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})"
//
//  def run(args: Array[String]): Int =
//    Cli.parse(args)
//      .withProgramName(BooterProperties.name)
//      .version(BooterProperties.version)
//      .withCommands(SerializeCommand, BuildCommand).get.run()
//
//}
//
//trait CustomOptionTypes {
//
//  implicit val potentialFileRead = Read.reads[PotentialFile]("a file") { PotentialFile(_) }
//
//  implicit val regexRead = Read.reads[Regex]("a regular expression") { _.r }
//
//  implicit val pathRead = Read.reads[Path]("a path") { str ⇒
//    val potentialDirectory = PotentialDirectory(str)
//    if (!potentialDirectory.exists)
//      throw new IllegalArgumentException(s"Cannot parse $str to an existing directory.")
//    potentialDirectory.path.get
//  }
//  implicit val baseUrlRead: Read[BaseURL] = Read.reads[BaseURL]("read base url") { BaseURL(_) }
//
//  implicit val nameRead = ReadMultiple.mapReadMultiple[Regex, String]
//
//}
//
//sealed trait GeneralOptions { this: Command ⇒
//
//  var help = opt[Boolean](default = false, description = "prints this help text")
//
//  var version = opt[Boolean](default = false, description = s"show just the version of ${BooterProperties.name}: ${BooterProperties.versionFull}")
//
//  var abort = opt[Boolean](default = false, description = "Abort on error")
//
//  var verbose = opt[Boolean](default = false, abbrev = "v", description = "Verbose logging")
//
//  def run(): Int
//}
//
//object SerializeCommand extends Command(
//  name = "serialize",
//  description = "copy a given input RDF file (or files) and rewrite or \"serialize\" it (in the same or another format) to a new file"
//) with GeneralOptions with CustomOptionTypes {
//
//  import CommandLineInterfaceOld._
//  import ReadMultiple._
//
//  def run(): Int = 2
//
//  var outputFile = opt[Option[PotentialFile]](default = None)
//
//  //  opt[PotentialFile]("output-file")
//  //    .text("Output file to which to write.")
//  //    .valueName("<file>")
//  //    .optional()
//  //    .foreach(x ⇒ outputFile = Some(x))
//
//  //
//  // `opt`, `arg` and `args` are scala macros that will extract the name of the member
//  // to use it as the option/arguments name.
//  //
//  // Here for instance the member `showAll` will be turned into the option `--show-all`
//  //  //
//  //  var `url-replace` = opt[Map[Regex, String]](
//  //    description = "Replace any part of a Subject, Predicate or Object URI that matches " +
//  //      "with the given <pattern> with the given <replacement string>"
//  //  )
//
//  /*
//  opt[Map[Regex, String]]("url-replace")
//    .valueName("<pattern>=<replacement string>,<pattern>=<replacement string>,..")
//    .text("Replace any part of a Subject, Predicate or Object URI that matches with the given <pattern> with the given <replacement string>")
//    .foreach(x ⇒ urlReplacePattern = Some(x))
//
//  opt[PotentialFile]("output-file")
//    .text("Output file to which to write.")
//    .valueName("<file>")
//    .optional()
//    .foreach(x ⇒ outputFile = Some(x))
//
//  opt[Map[BaseURL, Path]]("base-dir-url")
//    .valueName("<uri>=<base-dir>,<uri>=<base-dir>,..")
//    .text("...todo...")
//    .optional()
//    .foreach(x ⇒ baseDirUrls = Some(x))
//
//  opt[String]("output-format")
//    .valueName("<output-format>")
//    .text(
//      s"Output formats for OWLAPI are: $outputFormatsOwlApi\n" +
//        s"${spacing}Output formats for RDF4J are: $outputFormatsSesame"
//    )
//    .foreach(x ⇒ outputFormat = Some(x))
//
//  opt[Seq[PotentialFile]]("input-files")
//    .required()
//    .valueName("<file>")
//    .text("Input files to read. You can specify multiple input files, but they will then all be merged into one output file.")
//    .foreach(potentialFileSeq ⇒ Some(potentialFileSeq))
//    .validate(potentialFileSeq ⇒
//      success /* TODO
//          potentialFileSeq.foreach { potentialFile =>
//            if (potentialFile.fileExists)
//              success
//            else
//              failure(s"Input file ${potentialFile.fileName.get} does not exist.")
//          } */
//    )
//*/
//}
//
//object BuildCommand extends Command(
//  name = "build",
//  description = "build all artifacts"
//) with GeneralOptions with CustomOptionTypes {
//
//  def run(): Int = 2
//}
