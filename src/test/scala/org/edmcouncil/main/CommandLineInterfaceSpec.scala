package org.edmcouncil.main

import org.edmcouncil.serializer.UnitSpec
import org.rogach.scallop._

/**
  * Test the CommandLineInterface
  */
class CommandLineInterfaceSpec extends UnitSpec {

  "The rdf-toolkit CLI" must {

    "support --help" in {

      val cli = CommandLineInterface(Seq("--help"))

      cli.banner.isDefined should equal(true)
      cli.banner.get should include(BooterProperties.name)
      info(cli.banner.get)
      info(cli.help)
    }

    "support --version and have a banner and such" in {

      val cli = CommandLineInterface(Seq("--version"))

      cli.scallop.vers.isDefined should equal(true)
      info(cli.scallop.vers.get)

      cli.scallop.bann.isDefined should equal(true)
      info(cli.scallop.bann.get)

      cli.isVerbose should equal(false)
    }

    "support --verbose" in {

      val cli = CommandLineInterface(Seq("--verbose"))

      cli.isVerbose should equal(true)
    }

    """support the command "build --help"""" in {

      info("xxx")

      val cli = CommandLineInterface(Seq("build --help"))

      info(s"--verbose is ${cli.isVerbose}")

      cli.isVerbose should equal(false)

      1 + 3 should equal(5)
    }

  }

}
