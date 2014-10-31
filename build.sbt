organization := "org.edmcouncil"

name := "rdf-serializer"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked")

val owlApiVersion = "4.0.0"

libraryDependencies += "net.sourceforge.owlapi" % "owlapi-api" % owlApiVersion withSources()

libraryDependencies += "net.sourceforge.owlapi" % "owlapi-apibinding" % owlApiVersion withSources()

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1" withSources()

seq(bintrayResolverSettings:_*)

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.1"

libraryDependencies += "org.clapper" %% "avsl" % "1.0.2"

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

//
// Generate booter.properties, see class org.edmcouncil.main.BooterProperties
//
resourceGenerators in Compile <+= (
  resourceManaged in Compile, organization, name, version, scalaVersion
) map {
  (dir, o, n, v, s) => BooterPropertiesGenerator(dir, o, n, v, s)
}

fork in run := true
