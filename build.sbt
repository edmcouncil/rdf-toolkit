organization := "org.edmcouncil"

name := "rdf-serializer"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked")

seq(bintrayResolverSettings:_*)

val owlApiVersion = "4.0.1"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1" withSources()

libraryDependencies += "commons-validator" % "commons-validator" % "1.4.0"

//
// OWLAPI Model Interfaces And Utilities
//
libraryDependencies += "net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources()

//
// OWLAPI Binding And Config
//
libraryDependencies += "net.sourceforge.owlapi" % "owlapi-apibinding" % owlApiVersion withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources()

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.1" withSources()

libraryDependencies += "org.clapper" %% "avsl" % "1.0.2" withSources()

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources()

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test" withSources()

libraryDependencies += "org.ow2.easywsdl" % "easywsdl-tool-java2wsdl" % "2.3"

//
// Explicit loading of jackson-core to prevent merge issue in sbt-assembly
//
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1"

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

