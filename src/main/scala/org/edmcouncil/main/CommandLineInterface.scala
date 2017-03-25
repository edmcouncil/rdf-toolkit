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

import org.rogach.scallop.exceptions._
import org.rogach.scallop.{ CliOption, Scallop }

private object CommandLineInterfaceSerializeCommand {

  private def banner = s"""
    |RDF serializer (aka formatter) that formats in a consistent order, friendly for version control systems like Git.
    |Should not be used with files that are too large to be fully loaded into memory for sorting.
    |Run with "--help" option for help.
  """

  def apply(): Scallop = {
    Scallop()
      .banner(banner.stripMargin)
  }

}

private object CommandLineInterfaceBuildCommand {

  private def genBanner = s"""
  """

  private def subBuilderCommandBuildOntology(): Scallop = {
    Scallop()
      .banner("test 123")
  }

  def apply(): Scallop = {
    Scallop()
      .addSubBuilder(Seq("ontology"), subBuilderCommandBuildOntology)
  }

}

/**
 * Command Line Interface based on Scallop
 */
object CommandLineInterface {

  private def preUsageText = s"${BooterProperties.name} version ${BooterProperties.versionFull} (${BooterProperties.generatedAt})"

  private def generalBanner = s"""
    |
    |${preUsageText}
    |
    |Usage: ${BooterProperties.name} [<option1>..<n>] [<command>] ...
    |
    |The ${BooterProperties.name} supports the following options and commands:
    |
    |Options:
  """

  private def footer =
    s"""
      |${BooterProperties.copyright}
      |
      |${BooterProperties.licenses}
    """.stripMargin

  def apply(args: Seq[String]): CommandLineInterface = {

    val scallop = Scallop(args)
      .version(BooterProperties.version)
      .banner(generalBanner.stripMargin)
      .footer(footer)
      .toggle("verbose", default = () ⇒ Some(true), short = 'v')
      //      .addSubBuilder(Seq("serialize"), CommandLineInterfaceSerializeCommand())
      .addSubBuilder(Seq("build"), CommandLineInterfaceBuildCommand())

    //    args: Seq[String] = Nil,
    //    opts: List[CliOption] = Nil,
    //    mainOpts: List[String] = Nil,
    //    vers: Option[String] = None,
    //    bann: Option[String] = None,
    //    foot: Option[String] = None,
    //    descr: String = "",
    //    helpWidth: Option[Int] = None,
    //    shortSubcommandsHelp: Boolean = false,
    //    appendDefaultToDescription: Boolean = false,
    //    subbuilders: List[(String, Scallop)] = Nil,
    //    var parent: Option[Scallop] = None

    try {
      scallop.verify
    } catch {
      case Help(command) ⇒ println(s"command=${command}")
      case e @ Version   ⇒ println("version")

    }

    new CommandLineInterface(scallop)

  }
}

class CommandLineInterface(val scallop: Scallop) {

  def banner = scallop.bann
  def help = scallop.help

  def isVerbose = scallop.get[Boolean]("verbose").get

  def run() = ???
}
