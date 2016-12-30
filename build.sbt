import sbt.Keys.scalaVersion

organization := "org.edmcouncil"

name := "rdf-toolkit"

version := "1.0.4-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

bintrayResolverSettings

libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value

val owlApiVersion = "4.0.1"

val rdf4jVersion = "2.1.4"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5" withSources()

libraryDependencies += "commons-validator" % "commons-validator" % "1.5.1"

//
// OWLAPI Model Interfaces And Utilities
//
libraryDependencies += ("net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources())
                        .exclude("com.fasterxml.jackson.core", "jackson-core")

libraryDependencies += "net.sourceforge.owlapi" % "owlapi-apibinding" % owlApiVersion withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.3.0" withSources()

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.22" withSources()

libraryDependencies += "org.clapper" %% "avsl" % "1.0.13" withSources()

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test" withSources()

libraryDependencies += "org.ow2.easywsdl" % "easywsdl-tool-java2wsdl" % "2.3"

//
// Explicit loading of jackson-core to prevent merge issue in sbt-assembly
//
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1"

//
// RDF4F Binding And Config
//
libraryDependencies += "org.eclipse.rdf4j" % "rdf4j-runtime" % rdf4jVersion

//
// Apache Command-line Argument Handling Library used in Tony's Java code
//
libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

//
// Argot Command-Line Argument Handling used in the Scala code
//
libraryDependencies += "org.clapper" %% "argot" % "1.0.3"

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
// [1] org.edmcouncil.rdf-toolkir.SesameRdfFormatter
// [2] org.edmcouncil.rdf-toolkit.Main
//
mainClass in Compile := Some("org.edmcouncil.main.Main")

val `rdf-toolkit` = project.in(file(".")).enablePlugins(AutomateHeaderPlugin)
