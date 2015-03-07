![EDM Council Logo](etc/image/edmc-logo.jpg)

# rdf-serializer

Code for a command-line stable serializer for RDF.  This will be used in a commit-hook to make sure that all RDF files in the repo are stored in the same way.

See for more information about developing rdf-serializer [this](docs/develop.md) page or [this page](docs/dependencies.md) for information about dependencies.

# issues

The FIBO JIRA server has a separate project for the rdf-serializer: https://jira.edmcouncil.org/browse/RDFSER

Please add your issues, bugs, feature requests, requirements or questions as issues on the JIRA site.

# download

Download the RDF Serializer [here](https://jenkins.edmcouncil.org/job/rdf-serializer-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-serializer.jar)

# usage

Copy the [rdf-serializer.jar](https://jenkins.edmcouncil.org/job/rdf-serializer-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-serializer.jar) file to your local disk.

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
rdf-serializer version 1.0.0-SNAPSHOT-1c2dde6 (2015-02-01T15:57:24.775-0500)

Usage: rdf-serializer [--verbose] [--help] [--debug] [--force]
  [--input-file <path>] [--output-file <path>] [--output-format <format>]
  [--base-dir <path> --base-url <url>]

Where:
  --version                show just the version of rdf-serializer (1.0.0-SNAPSHOT-1c2dde6)
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
  --base-dir <path>        root directory where imported ontologies can be found
  --base-url <url>         the base url of imported ontologies that matches with the <path> specified with
                           the --base-dir option.

```

Note: For XML output syntax, blank nodes will be handled properly.
