package org.edmcouncil.rdf_serializer

import org.edmcouncil.main.BooterProperties

/**
 * Allow for the MainImpl to be executed from tests bypassing Main.
 */
object MainImplNew {

  def apply(args: Array[String]) = new MainImplNew(args)

  def apply(args: Seq[String]) = new MainImplNew(args.toArray)
}

/**
 * The "real" Main of the RDF Serializer.
 */
class MainImplNew(args: Array[String]) {

  val params = CommandLineParams2(args)

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





