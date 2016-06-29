organization := "org.edmcouncil"

name := "rdf-toolkit"

version := "1.5.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

javacOptions ++= Seq("-Xlint:unchecked")

Seq(bintrayResolverSettings:_*)

//val owlApiVersion = "4.0.1"
val sesameVersion = "4.1.2"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.8"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

libraryDependencies += "commons-validator" % "commons-validator" % "1.5.1"

//
// OWLAPI Model Interfaces And Utilities
//
//libraryDependencies += "net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources()

//libraryDependencies += ("net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources())
//                        .exclude("com.fasterxml.jackson.core", "jackson-core")

//libraryDependencies += "net.sourceforge.owlapi" % "owlapi-apibinding" % owlApiVersion withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.4"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.21"

libraryDependencies += "org.clapper" %% "avsl" % "1.0.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

//libraryDependencies += "org.ow2.easywsdl" % "easywsdl-tool-java2wsdl" % "2.3"

//
// Explicit loading of jackson-core to prevent merge issue in sbt-assembly
//
//libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1"

//
// Sesame Binding And Config
//
libraryDependencies += "org.openrdf.sesame" % "sesame-runtime" % sesameVersion

//
// Apache Command-line Argument Handling Library used in Tony's Java code
//
libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"

//
// jline console utilities
//
libraryDependencies += "jline" % "jline" % "2.14.2"

//
// Argot Command-Line Argument Handling used in the Scala code
//
libraryDependencies += "org.clapper" %% "argot" % "1.0.4"

//
// Generate booter.properties, see class org.edmcouncil.main.BooterProperties
//
resourceGenerators in Compile <+= (
  resourceManaged in Compile, organization, name, version, scalaVersion
) map {
  (dir, o, n, v, s) => BooterPropertiesGenerator(dir, o, n, v, s)
}

fork in run := true

resolvers += JavaNet2Repository

resolvers += "http://weblab.ow2.org/" at "http://weblab.ow2.org/release-repository"

//
// Select the main class. Let it be the Scala main, not the Java main (SesameRdfSerializer, which can still
// be selected on the command line seperately. This prevents the following prompt:
//
// Multiple main classes detected, select one to run:
//
// [1] org.edmcouncil.rdf_toolkit.SesameRdfFormatter
// [2] org.edmcouncil.rdf_toolkit.Main
//
mainClass in Compile := Some("org.edmcouncil.rdf_toolkit.SesameRdfFormatter")