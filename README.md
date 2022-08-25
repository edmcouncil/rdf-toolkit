<img src="https://spec.edmcouncil.org/fibo/htmlpages/develop/latest/img/logo.66a988fe.png" width="150" align="right"/>

# rdf-toolkit

The `rdf-toolkit` is a command-line 'swiss army knife' tool for reading and writing RDF and OWL files in whatever format.

The primary reason for creating this tool was to have a reference implementation of the toolkit/formatter that 
creates the FIBO ontologies as they are stored in the [Github FIBO repository](https://github.com/edmcouncil/fibo) 
(which is at this point in time still a private repository). However, this tool is not in any way specific to FIBO, 
it can be used with any set of ontologies or for that matter even "normal" RDF files.

It currently uses RDF4J to do the hard work, see [this page](docs/dependencies.md) for more info about those products.

This will be used in a commit-hook to make sure that all RDF files in the repo are stored in the same way.

See for more information about developing rdf-toolkit [this](docs/develop.md) page or [this page](docs/dependencies.md) for information about dependencies.

# Recommended Output Format

The recommended Output Format at this time is RDF/XML because that is the format that the OMG requires for submissions. 
The EDM Council develops the FIBO Ontologies and submits them as RDF/XML, serialized by the `rdf-toolkit` to the OMG. 
So that is why we also use RDF/XML in Github itself. There are some issues with that and we're working on resolving that, 
by either "fixing" the RDF/XML output generated by RDF4J, or by eventually migrating to some other format. 
For use in Git we need a format that:

## Requirements for Git-based Ontology Serialization

- As few 'diff-lines' as possible per 'pull request'
- Relative URIs
  - so that Git branch or tag name can become part of the final Ontology Version IRI
  - so that dereferencing from tools like Protege, straight to the GitHub repo would work
- Readable (RDF/XML is only readable by the very few)

# Issues

The FIBO JIRA server has a separate project for the rdf-toolkit: https://jira.edmcouncil.org/browse/RDFSER

Please add your issues, bugs, feature requests, requirements or questions as issues on the JIRA site.

# Download

Download the RDF Toolkit [here](https://jenkins.edmcouncil.org/view/rdf-toolkit/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/rdf-toolkit.jar)

# Usage

Download the `rdf-toolkit.jar` file mentioned in the Download section above to your computer.

## Linux or Mac OS X

On Linux or Mac OS X you can execute the rdf-toolkit as follows:

1. Open a Terminal
2. Type the name of the `rdf-toolkit.jar` file on the command prompt and supply the `--help` option:
```
$ java -jar rdf-toolkit.jar --help
```

## Windows

1. Open a Command Shell by going to the Start menu and type `cmd.exe`.
2. Ensure that Java is installed by typing `java -version` on the command line. The Java version should be at least 1.7 (i.e. Java 7).
3. Then launch the rdf-toolkit's help function as follows:
```
java -jar rdf-toolkit.jar --help
```

# --help

The current `--help` option gives the following information:

```
usage: RdfFormatter (rdf-toolkit version 1.11.0)
 -bi,--base-iri <arg>                    set IRI to use as base URI
 -dtd,--use-dtd-subset                   for XML, use a DTD subset in order to allow prefix-based
                                         IRI shortening
 -h,--help                               print out details of the command-line arguments for the
                                         program
 -i,--indent <arg>                       sets the indent string.  Default is a single tab character
 -ibi,--infer-base-iri                   use the OWL ontology IRI as the base URI.  Ignored if an
                                         explicit base IRI has been set
 -ibn,--inline-blank-nodes               use inline representation for blank nodes.  NOTE: this will
                                         fail if there are any recursive relationships involving
                                         blank nodes.  Usually OWL has no such recursion involving
                                         blank nodes.  It also will fail if any blank nodes are a
                                         triple subject but not a triple object.
 -ip,--iri-pattern <arg>                 set a pattern to replace in all IRIs (used together with
                                         --iri-replacement)
 -ir,--iri-replacement <arg>             set replacement text used to replace a matching pattern in
                                         all IRIs (used together with --iri-pattern)
 -lc,--leading-comment <arg>             sets the text of the leading comment in the ontology.  Can
                                         be repeated for a multi-line comment
 -ln,--line-end <arg>                    sets the end-line character(s); supported characters: \n
                                         (LF), \r (CR). Default is the LF character
 -osl,--override-string-language <arg>   sets an override language that is applied to all strings
 -oxn,--omit-xmlns-namespace             omits xmlns namespace
 -s,--source <arg>                       source (input) RDF file to format
 -sd,--source-directory <arg>            source (input) directory of RDF files to format.  This is a
                                         directory processing option
 -sdp,--source-directory-pattern <arg>   relative file path pattern (regular expression) used to
                                         select files to format in the source directory.  This is a
                                         directory processing option
 -sdt,--string-data-typing <arg>         sets whether string data values have explicit data types,
                                         or not; one of: explicit, implicit [default]
 -sfmt,--source-format <arg>             source (input) RDF format; one of: auto (select by
                                         filename) [default], binary, json-ld (JSON-LD), n3, n-quads
                                         (N-quads), n-triples (N-triples), rdf-a (RDF/A), rdf-json
                                         (RDF/JSON), rdf-xml (RDF/XML), trig (TriG), trix (TriX),
                                         turtle (Turtle)
 -sip,--short-iri-priority <arg>         set what takes priority when shortening IRIs: prefix
                                         [default], base-iri
 -t,--target <arg>                       target (output) RDF file
 -tc,--trailing-comment <arg>            sets the text of the trailing comment in the ontology.  Can
                                         be repeated for a multi-line comment
 -td,--target-directory <arg>            target (output) directory for formatted RDF files.  This is
                                         a directory processing option
 -tdp,--target-directory-pattern <arg>   relative file path pattern (regular expression) used to
                                         construct file paths within the target directory.  This is
                                         a directory processing option
 -tfmt,--target-format <arg>             target (output) RDF format: one of: json-ld (JSON-LD),
                                         rdf-xml (RDF/XML), turtle (Turtle) [default]
 -v,--version                            print out version details
```

* [Sesame serializer documentation](docs/RdfFormatter.md)
