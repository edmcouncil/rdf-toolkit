rdf-serializer
==============


Code for a command-line stable serializer for RDF.  This will be used in a commit-hook to make sure that all RDF files
in the repo are stored in the same way.


Requirements are being gathered at a [wiki page](https://github.com/edmcouncil/rdf-serializer/wiki/Requirements)


# build

This project is being build and packaged on the EDM Council Jenkins Server by [this job](http://54.210.211.22/job/rdf-serializer-build/).

The current build status is: 
[![Build Status](http://54.210.211.22/buildStatus/icon?job=rdf-serializer-build)](http://54.210.211.22/job/rdf-serializer-build/)

The RDF Serializer is packaged as one "fat jar" or "uber jar" which can be downloaded from the Jenkins server:

- http://54.210.211.22/job/rdf-serializer-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-serializer.jar

# usage
=====

Copy the rdf-serializer.jar file to your local disk.

## Linux or Mac OS X

On Linux or Mac OS X you can execute the rdf-serializer
as follows:

1. Open a Terminal
2. Type the name of the rdf-serializer.jar file on the command prompt and supply the --help option:
```
>rdf-serializer.jar --help
```

## Windows

1. Open a Command Shell by going to the Start menu and type cmd.exe
2. Ensure that Java is installed by typing "java -version" on the command line, which should result in
   either version 1.7 or 1.8.
3. Then launch the rdf-serializer's help function as follows:
```
C:/>java -jar rdf-serializer.jar --help
```

# --help

The current "--help" option gives the following information:

```
rdf-serializer version 1.0.0-SNAPSHOT-e6d6d9e (2014-11-01T21:29:32.877-0400)

Usage: rdf-serializer [--verbose] [--help]

Where:
  --version                show just the version of rdf-serializer (1.0.0-SNAPSHOT-e6d6d9e)
  --verbose                switch on verbose logging (sets INFO level logging).
  --debug                  switch on debug level logging.
  --force                  force output file to be overwritten if it exists.
  --help                   this help.
  --input-file <path>
  --output-file <path>
  --output-format <format> where <format> is one of (between quotes):
                           - TriX
                           - OWL Functional Syntax
                           - OBO Format
                           - KRSS2 Syntax
                           - OWL/XML Syntax
                           - RDF/XML
                           - RDF/XML Syntax
                           - RDF/JSON
                           - N3
                           - Manchester OWL Syntax
                           - BinaryRDF
                           - N-Triples
                           - JSON-LD
                           - N-Quads
                           - TriG
                           - Turtle
                           - Turtle Syntax
                           - LaTeX Syntax
```

Note: For XML output syntax, blank nodes will be handled properly.
