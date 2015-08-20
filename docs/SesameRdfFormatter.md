# usage: org.edmcouncil.rdf_toolkit.SesameRdfFormatter

```
 -bu,--base-uri <arg>              set URI to use as base URI
 -dtd,--use-dtd-subset             for XML, use a DTD subset in order to
                                   allow prefix-based URI shortening
 -h,--help                         print out details of the command-line
                                   arguments for the program
 -ibn,--inline-blank-nodes         use inline representation for blank
                                   nodes.  NOTE: this will fail if there
                                   are any recursive relationships
                                   involving blank nodes.  Usually OWL has
                                   no such recursion involving blank
                                   nodes.  It also will fail if any blank
                                   nodes are a triple subject but not a
                                   triple object.
 -ibu,--infer-base-uri             use the OWL ontology URI as the base
                                   URI.  Ignored if an explicit base URI
                                   has been set
 -s,--source <arg>                 source (input) RDF file to format
 -sfmt,--source-format <arg>       source (input) RDF format; one of: auto
                                   (select by filename) [default], binary,
                                   json-ld (JSON-LD), n3, n-quads
                                   (N-quads), n-triples (N-triples), rdf-a
                                   (RDF/A), rdf-json (RDF/JSON), rdf-xml
                                   (RDF/XML), trig (TriG), trix (TriX),
                                   turtle (Turtle)
 -sup,--short-uri-priority <arg>   set what takes priority when shortening
                                   URIs: prefix [default], base-uri
 -t,--target <arg>                 target (output) RDF file
 -tfmt,--target-format <arg>       target (output) RDF format: one of:
                                   rdf-xml (RDF/XML), turtle (Turtle)
                                   [default]
 -up,--uri-pattern <arg>           set a pattern to replace in all URIs
                                   (used together with --uri-replacement)
 -ur,--uri-replacement <arg>       set replacement text used to replace a
                                   matching pattern in all URIs (used
                                   together with --uri-pattern)
```

# Examples:

1. Print out command-line help
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --help`
2. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.ttl --target-format turtle`
3. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the default source/target formats (Turtle for both)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl`
4. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the given base URI for the output Turtle
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --base-uri http://www.example.com/my-base-uri`
5. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the given base URI for the output Turtle, and use the base URI in preference to prefixes for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --base-uri http://www.example.com/my-base-uri --short-uri-priority base-uri`
6. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with inline blank nodes (note: assumes no recursive relationships between blank nodes; this is usually the case for OWL)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --inline-blank-nodes`
7. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), with the OWL ontology URI used as the base URI
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --infer-base-uri`
8. Format a Turtle file (`input.ttl`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --source-format turtle --target output.rdf --target-format rdf-xml --use-dtd-subset`
9. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset`
10. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening and inline blank nodes (note: assumes no recursive relationships between blank nodes; this is usually the case for OWL)
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --inline-blank-nodes`
11. Format an RDF/XML file (`input.rdf`) as sorted RDF/XML (`output.rdf`), using entity references for URL shortening, with the OWL ontology URI used as the base URI
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.rdf --source-format rdf-xml --target output.rdf --target-format rdf-xml --use-dtd-subset --infer-base-uri`
12. Format a Turtle file (`input.ttl`) as sorted Turtle (`output.ttl`), using the default source/target formats (Turtle for both), and replacing 'www.example.com' in URIs with 'www.example.org'
  * `java -cp rdf-toolkit.jar org.edmcouncil.rdf_toolkit.SesameRdfFormatter --source input.ttl --target output.ttl --uri-pattern www.example.com --uri-replacement www.example.org`
