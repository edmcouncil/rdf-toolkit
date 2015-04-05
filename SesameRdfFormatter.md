# usage: SesameRdfFormatter

```
 -bu,--base-uri <arg>              set URI to use as base URI
 -dtd,--use-dtd-subset <arg>       for XML, use a DTD subset in order to
                                   allow prefix-based URI shortening
 -h,--help                         print out details of the command-line
                                   arguments for the program
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
 -tfmt,--target-format <arg>       source (input) RDF format: one of:
                                   rdf-xml (RDF/XML), turtle (Turtle)
                                   [default]
 -up,--uri-pattern <arg>           set a pattern to replace in all URIs
                                   (used together with --uri-replacement)
 -ur,--uri-replacement <arg>       set replacement text used to replace a
                                   matching pattern in all URIs (used
                                   together with --uri-pattern)
```
