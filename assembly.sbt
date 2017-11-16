//
// The sbt-assembly specific commands which can be used to construct an "uber-jar" (rdf-toolkit.jar) that contains
// everything to run the RDF toolkit from the command line like this:
//
// java -jar rdf-toolkit.jar
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

//
// The name of the uber-jar
//
assemblyJarName in assembly := "rdf-toolkit.jar"

//
// To skip the test during assembly:
//
test in assembly := {}

//
// To set an explicit main class:
//
// mainClass in assembly := Some("org.edmcouncil.main.Main")
mainClass in assembly := Some("org.edmcouncil.rdf_toolkit.SesameRdfFormatter")

//
// Prepend the following shell script to the jar:
//
// #!/usr/bin/env sh
// exec java -jar "$0" "$@"
//
val shellScript: Seq[String] = Seq("#!/usr/bin/env sh", """exec java -Xmx1G -jar "$0" "$@"""") // "
assemblyOption in assembly ~= { _.copy(prependShellScript = Some(shellScript)) }


//
// Set the merge strategy for duplicates:
//
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         	=> MergeStrategy.first
  /*
  case PathList("META-INF", "maven", "com.google.guava", "guava", xs @ _*) => MergeStrategy.first
  case PathList("META-INF", "maven", "com.fasterxml.jackson.core", "jackson-core", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "maven", "commons-codec", "commons-codec", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "maven", "org.openrdf.sesame", xs @ _*) => MergeStrategy.last
  */
  case PathList("META-INF", "maven", xs @ _*) => MergeStrategy.discard
  case PathList("META-INF", "sun-jaxb.episode", xs @ _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", xs @ _*) => MergeStrategy.first
  case PathList("com", xs @ _*) => MergeStrategy.last
  case PathList("info", xs @ _*) => MergeStrategy.last
  case PathList("org", xs @ _*) => MergeStrategy.last
  case "booter.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
