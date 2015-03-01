organization := "org.edmcouncil"

name := "rdf-serializer"

version := "1.0.1-SESAME-MERGE"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

seq(bintrayResolverSettings:_*)

val owlApiVersion = "4.0.1"
val sesameVersion = "2.8.0"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1" withSources()

libraryDependencies += "commons-validator" % "commons-validator" % "1.4.0"

//
// OWLAPI Model Interfaces And Utilities
//
//libraryDependencies += "net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources()

libraryDependencies += ("net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources())
                        .exclude("com.fasterxml.jackson.core", "jackson-core")

libraryDependencies += "net.sourceforge.owlapi" % "owlapi-apibinding" % owlApiVersion withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources()

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.1" withSources()

libraryDependencies += "org.clapper" %% "avsl" % "1.0.2" withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources()

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources()

libraryDependencies += "org.ow2.easywsdl" % "easywsdl-tool-java2wsdl" % "2.3"

//
// Explicit loading of jackson-core to prevent merge issue in sbt-assembly
//
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1"

//
// Sesame Binding And Config
//
libraryDependencies += "org.openrdf.sesame" % "sesame-runtime" % sesameVersion

//
// Apache Command-line Argument Handling Library
//
libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

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
// [1] org.edmcouncil.rdf_serializer.SesameRdfFormatter
// [2] org.edmcouncil.rdf_serializer.Main
//
mainClass in Compile := Some("org.edmcouncil.rdf_serializer.Main")