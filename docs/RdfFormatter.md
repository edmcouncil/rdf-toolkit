This is an additional tool shipped with rdf-toolkit. It reformats ontology files on the basis of the rules specified by the run parameters.

# Table of Contents
1. [Run Parameters](#run-parameters)
2. [Examples](#examples)

# Run Parameters
These are the parameters to run org.edmcouncil.rdf_toolkit.RdfFormatter:
```
 -bi,--base-iri <arg>                    set IRI to use as base URI
 -dtd,--use-dtd-subset                   for XML, use a DTD subset in
                                         order to allow prefix-based IRI
                                         shortening
 -h,--help                               print out details of the
                                         command-line arguments for the
                                         program
 -i,--indent <arg>                       sets the indent string.  Default
                                         is a single tab character
 -ibi,--infer-base-iri                   use the OWL ontology IRI as the
                                         base URI.  Ignored if an explicit
                                         base IRI has been set
 -ibn,--inline-blank-nodes               use inline representation for
                                         blank nodes.  NOTE: this will
                                         fail if there are any recursive
                                         relationships involving blank
                                         nodes.  Usually OWL has no such
                                         recursion involving blank nodes.
                                         It also will fail if any blank
                                         nodes are a triple subject but
                                         not a triple object.
 -ip,--iri-pattern <arg>                 set a pattern to replace in all
                                         IRIs (used together with
                                         --iri-replacement)
 -ir,--iri-replacement <arg>             set replacement text used to
                                         replace a matching pattern in all
                                         IRIs (used together with
                                         --iri-pattern)
 -lc,--leading-comment <arg>             sets the text of the leading
                                         comment in the ontology.  Can be
                                         repeated for a multi-line comment
 -osl,--override-string-language <arg>   sets an override language that is
                                         applied to all strings
 -s,--source <arg>                       source (input) RDF file to format
 -sd,--source-directory <arg>            source (input) directory of RDF
                                         files to format.  This is a
                                         directory processing option
 -sdp,--source-directory-pattern <arg>   relative file path pattern
                                         (regular expression) used to
                                         select files to format in the
                                         source directory.  This is a
                                         directory processing option
 -sdt,--string-data-typing <arg>         sets whether string data values
                                         have explicit data types, or not;
                                         one of: explicit, implicit
                                         [default]
 -sfmt,--source-format <arg>             source (input) RDF format; one
                                         of: auto (select by filename)
                                         [default], binary, json-ld
                                         (JSON-LD), n3, n-quads (N-quads),
                                         n-triples (N-triples), rdf-a
                                         (RDF/A), rdf-json (RDF/JSON),
                                         rdf-xml (RDF/XML), trig (TriG),
                                         trix (TriX), turtle (Turtle)
 -sip,--short-iri-priority <arg>         set what takes priority when
                                         shortening IRIs: prefix
                                         [default], base-iri
 -t,--target <arg>                       target (output) RDF file
 -tc,--trailing-comment <arg>            sets the text of the trailing
                                         comment in the ontology.  Can be
                                         repeated for a multi-line comment
 -td,--target-directory <arg>            target (output) directory for
                                         formatted RDF files.  This is a
                                         directory processing option
 -tdp,--target-directory-pattern <arg>   relative file path pattern
                                         (regular expression) used to
                                         construct file paths within the
                                         target directory.  This is a
                                         directory processing option
 -tfmt,--target-format <arg>             target (output) RDF format: one
                                         of: json-ld (JSON-LD), rdf-xml
                                         (RDF/XML), turtle (Turtle)
                                         [default]
 -v,--version                            print out version details
```

# Examples

1. Print out command-line help
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --help`
2. Print out the RDF Toolkit version
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --version`
3. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`)
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle` 
4. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the given base URI for the output Turtle
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source input.ttl --target output.ttl --base-iri http://www.example.com/my-base-iri`
5. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with inline blank nodes (note: assumes no recursive relationships between blank nodes; this is usually the case for OWL)
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source input.ttl --target output.ttl --inline-blank-nodes`
6. Format a Turtle file (`input.ttl`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source input.ttl --source-format turtle --target output.rdf --target-format rdf-xml --use-dtd-subset`
7. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset`
8. Format a RDF/XML file (`input.rdf`) as sorted JSON-LD (`output.jsonld`), using the standard input & standard output
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source-format rdf-xml --target-format json-ld < input.rdf > output.jsonld`
9. Format all of the Turtle files in a directory tree into RDF/XML, preserving the relative directory structure.
`java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.RdfFormatter --source-directory src/test/resources --source-directory-pattern '^(.*)\.ttl$' --source-format turtle --target-directory target/temp --target-directory-pattern '$1.rdf' --target-format rdf-xml`
