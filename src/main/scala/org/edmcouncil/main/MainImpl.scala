package org.edmcouncil.main

import org.edmcouncil.rdf_serializer.Serializer

/**
 * Allow for the MainImpl to be executed from tests bypassing Main.
 */
object MainImpl {

  def apply(args: Array[String]) = new MainImpl(args)

  def apply(args: Seq[String]) = new MainImpl(args.toArray)
}

/**
 * The "real" Main of the RDF Serializer.
 */
class MainImpl(args: Array[String]) {

  val params = CommandLineParams(args)

  private def run2 : Int = if (params.shouldShowVersion) {
    println(BooterProperties.versionFull)
    0
  } else if (params.specifiedHelp) {
    params.showUsage
    0
  } else {
    //
    // Run the Serializer as if it were a function that returns an Int
    //
    Serializer(params)
  }

  def run : Int = {
    val rc = params.parse()
    if (rc == 0) run2 else rc
  }
}





