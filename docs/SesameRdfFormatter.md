# usage: org.edmcouncil.rdf_toolkit.SesameRdfFormatter

```
 -bi,--base-iri <arg>              set IRI to use as base URI
 -dtd,--use-dtd-subset             for XML, use a DTD subset in order to
                                   allow prefix-based IRI shortening
 -h,--help                         print out details of the command-line
                                   arguments for the program
 -i,--indent <arg>                 sets the indent string.  Default is a
                                   single tab character
 -ibi,--infer-base-iri             use the OWL ontology IRI as the base
                                   URI.  Ignored if an explicit base IRI
                                   has been set
 -ibn,--inline-blank-nodes         use inline representation for blank
                                   nodes.  NOTE: this will fail if there
                                   are any recursive relationships
                                   involving blank nodes.  Usually OWL has
                                   no such recursion involving blank
                                   nodes.  It also will fail if any blank
                                   nodes are a triple subject but not a
                                   triple object.
 -ip,--iri-pattern <arg>           set a pattern to replace in all IRIs
                                   (used together with --iri-replacement)
 -ir,--iri-replacement <arg>       set replacement text used to replace a
                                   matching pattern in all IRIs (used
                                   together with --iri-pattern)
 -lc,--leading-comment <arg>       sets the text of the leading comment in
                                   the ontology.  Can be repeated for a
                                   multi-line comment
 -s,--source <arg>                 source (input) RDF file to format
 -sdt,--string-data-typing <arg>   sets whether string data values have
                                   explicit data types, or not; one of:
                                   explicit, implicit [default]
 -sfmt,--source-format <arg>       source (input) RDF format; one of: auto
                                   (select by filename) [default], binary,
                                   json-ld (JSON-LD), n3, n-quads
                                   (N-quads), n-triples (N-triples), rdf-a
                                   (RDF/A), rdf-json (RDF/JSON), rdf-xml
                                   (RDF/XML), trig (TriG), trix (TriX),
                                   turtle (Turtle)
 -sip,--short-iri-priority <arg>   set what takes priority when shortening
                                   IRIs: prefix [default], base-iri
 -t,--target <arg>                 target (output) RDF file
 -tc,--trailing-comment <arg>      sets the text of the trailing comment
                                   in the ontology.  Can be repeated for a
                                   multi-line comment
 -tfmt,--target-format <arg>       target (output) RDF format: one of:
                                   json-ld (JSON-LD), rdf-xml (RDF/XML),
                                   turtle (Turtle) [default]
 -v,--version                      print out version details
```

# Examples:

1. Print out command-line help
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --help`
2. Print out the RDF Toolkit version
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --version`
3. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle`
4. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the default source/target formats (Turtle for both)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl`
5. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the given base URI for the output Turtle
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --base-iri http://www.example.com/my-base-iri`
6. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the given base URI for the output Turtle, and use the base URI in preference to prefixes for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --base-iri http://www.example.com/my-base-iri --short-iri-priority base-iri`
7. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with inline blank nodes (note: assumes no recursive relationships between blank nodes; this is usually the case for OWL)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --inline-blank-nodes`
8. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with the OWL ontology URI used as the base URI
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --infer-base-iri`
9. Format a Turtle file (`input.ttl`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.rdf --target-format rdf-xml --use-dtd-subset`
10. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with a leading and a trailing comment
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle --leading-comment "Start of my ontology" --trailing-comment "End of my ontology"`
11. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with a multi-line leading and a trailing comments
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle -lc "Start of my ontology" -lc "Version 1" -tc "End of my ontology" -tc "Version 1"`
12. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`) with explicit data typing for strings
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle --string-data-typing explicit`
13. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`) with two spaces as the indent string
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle --indent "  "`
14. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset`
15. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening and inline blank nodes (note: assumes no recursive relationships between blank nodes; this is usually the case for OWL)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --inline-blank-nodes`
16. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening, with the OWL ontology URI used as the base URI
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --infer-base-iri`
17. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the default source/target formats (Turtle for both), and replacing 'www.example.com' in URIs with 'www.example.org'
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --iri-pattern www.example.com --iri-replacement www.example.org`
18. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening, with a leading and a trailing comment
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --leading-comment "Start of my ontology" --trailing-comment "End of my ontology"`
19. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening, with a multi-line leading and a trailing comments
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset -lc "Start of my ontology" -lc "Version 1" -tc "End of my ontology" -tc "Version 1"`
20. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening and with explicit data typing for strings
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --string-data-typing explicit`
21. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening and with two spaces as the indent string
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --indent "  "`
22. Format a RDF/XML file (`input.rdf`) as sorted JSON-LD (`output.jsonld`), using the standard input & standard output
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source-format rdf-xml --target-format json-ld < input.rdf > output.jsonld`
