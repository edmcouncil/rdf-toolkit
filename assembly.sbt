import AssemblyKeys._ // put this at the top of the file

assemblySettings

// your assembly settings here

jarName in assembly := "rdf-serializer.jar"

test in assembly := {}

mainClass in assembly := Some("org.edmcouncil.rdf_serializer.Main")