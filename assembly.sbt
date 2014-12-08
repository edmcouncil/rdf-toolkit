//
// The sbt-assembly specific commands which can be used to construct an "uber-jar" (rdf-serializer.jar) that contains
// everything to run the RDF serializer from the command line like this:
//
// java -jar rdf-serializer.jar
//
// https://github.com/sbt/sbt-assembly
//
// This is similar to what the Maven shade plugin creates:
// http://maven.apache.org/plugins/maven-shade-plugin
//
// Alternative SBT plugins that do the same or are similar:
//
// - https://github.com/sbt/sbt-onejar
// - https://github.com/nuttycom/sbt-proguard-plugin
//
import AssemblyKeys._ // put this at the top of the file

assemblySettings

//
// The name of the uber-jar
//
jarName in assembly := "rdf-serializer.jar"

//
// To skip the test during assembly:
//
test in assembly := {}

//
// To set an explicit main class:
//
mainClass in assembly := Some("org.edmcouncil.rdf_serializer.Main")

//
// Prepend the following shell script to the jar:
//
// #!/usr/bin/env sh
// exec java -jar "$0" "$@"
//
assemblyOption in assembly ~= { _.copy(prependShellScript = Some(defaultShellScript)) }

//
// Set the merge strategy for duplicates:
//
mergeStrategy in assembly <<= (mergeStrategy in assembly) {
  (old) => {
    case PathList("javax", "servlet", xs @ _*)         	=> MergeStrategy.first
    case PathList("META-INF", "maven", "com.google.guava", "guava", xs @ _*) => MergeStrategy.first
    case PathList("META-INF", "sun-jaxb.episode", xs @ _*) => MergeStrategy.first
    case PathList("org", "apache", "commons", "logging", xs @ _*) => MergeStrategy.first
    case "booter.properties" 							=> MergeStrategy.first
    case x => old(x)
  }
}
