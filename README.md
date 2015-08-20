![EDM Council Logo](etc/image/edmc-logo.jpg)

# rdf-toolkit

The long-term goal of the `rdf-toolkit` is to provide a 'swiss army knife' set of command-line tools for processing RDF and OWL files in any format.

At present, the toolkit contains:
* Sesame serializer

# Sesame serializer

The primary reason for creating this tool was to have a reference implementation of the serializer/formatter that 
creates the FIBO ontologies as they are stored in the [Github FIBO repository](https://github.com/edmcouncil/fibo) 
(which is at this point in time still a private repository). However, this tool is not in any way specific to FIBO, 
it can be used with any set of ontologies or for that matter even "normal" RDF files.

The Sesame serializer reads all RDF formats known to Sesame, and can output Turtle and RDF/XML.  See [Sesame serializer command line documentation](docs/SesameRdfFormatter.md).

It uses Sesame to do the hard work of reading RDF; see ['dependencies'](docs/dependencies.md) for more info about Sesame.

This serializer can be used in a commit-hook to make sure that all RDF files in the a Git repository are serialized (sorted & formatted) in the same way.  See [git hook script directory](etc/git-hook/).

For more information about developing the rdf-toolkit see ['development'](docs/develop.md).  See ['dependencies'](docs/dependencies.md) for information about software dependencies.

# Recommended Output Format

The recommended Output Format for FIBO at this time is RDF/XML because that is the format that the OMG requires for submissions. 
The EDM Council develops the FIBO Ontologies and submits them as RDF/XML, serialized by the Sesame serializer to the OMG. 
That is why we also use RDF/XML in Github itself. There are some issues with that and we're working on resolving them, 
by either "fixing" the generated RDF/XML output, or by eventually migrating to some other format. 
For use in Git we need a format that:

## Requirements for Git-based Ontology Serialization

- As few 'diff-lines' as possible per 'pull request'
- Relative URIs
  - so that Git branch or tag name can become part of the final Ontology Version IRI
  - so that dereferencing from tools like Protege works straight to the GitHub repo
- Readable (RDF/XML is only directly readable by the very few)

# issues

The FIBO JIRA server has a separate project for the Sesame serializer: https://jira.edmcouncil.org/browse/RDFSER

Please add your issues, bugs, feature requests, requirements or questions as issues on the JIRA site.  You will need to sign up for an account, if you haven't already.

# download

[Download the rdf-toolkit](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-toolkit.jar).

# usage

Copy the [rdf-toolkit.jar](https://jenkins.edmcouncil.org/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/scala-2.11/rdf-toolkit.jar) file to your local disk.

* [Sesame serializer documentation](docs/SesameRdfFormatter.md)
