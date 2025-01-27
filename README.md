# Table of Contents
1. [Introduction](#introduction)
1. [Rationale](#rationale)
1. [Requirements](#requirements)
1. [Usage](#usage)
1. [Serialisation Algorithm Explained](#serialisation-algorithm-explained)

# Introduction

The `RDF Toolkit` is a 'swiss army knife' tool for reading and writing RDF files in multiple formats.

The primary reason for creating this tool was to have a reference serializer for the FIBO ontologies as they are stored in [Github FIBO repository](https://github.com/edmcouncil/fibo). However, the tool is not in any way specific to FIBO, and it can be used with any ontology or RDF file. In this capacity, it can be used in a commit-hook to make sure that all RDF files in the repo are stored in the same way.

# Rationale

## Minimize the ontology review effort 

For the purposes of git-based version control of ontology files, we want to have as few differences between commits as possible.
Most ontology editors can encode RDF graphs, including OWL ontologies, in several formats, including the W3C normative RDF exchange formats (syntaxes): RDF/XML, Turtle and JSON-LD. However, even these normative formats are not canonical. Therefore, an editor tool may change many aspects of how ontology entities are serialized every time the ontology is saved (such as adding/changing comments or changing the order and organization of statements) leading to difficulties in analyzing actual changes in the underlying semantics.

## Handle intelligent IRIs

We want to be able to include actionable information as part of IRIs, e.g., git tags, and then deference them from ontology tools like Protege.

## Recommended output format

The recommended output formats are RDF/XML or Turtle, because these are the formats that the [OMG](https://www.omg.org) allows for submissions. Several EDM Council projects, including the Financial Industry Business Ontology (FIBO), Identification of Medicinal Products (IDMP-O), and Industrial Ontologies Foundry (IOF), use and contribute to ontologies that are standardized by OMG and elsewhere. We submit these for review to the relevant stakeholder organization as RDF/XML and Turtle, serialized by the `RDF Toolkit`. Because RDF/XML was the original serialization recommended by the W3C, we initially designed the serializer to support that, but as others became recommendations, we added support for them as well. Some of our other tools leverage RDF/XML as the primary serialization for historical reasons, but the toolkit supports any of these and can translate between them. 

# Requirements
To run `RDF Toolkit` one needs JAVA JRE installed version 11+.

# Usage

## Download RDF Toolkit

Download the `RDF Toolkit` binary from [here](https://jenkins.edmcouncil.org/view/rdf-toolkit/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/rdf-toolkit.jar).

## Run RDF Toolkit

One can use `RDF Toolkit` as a standalone application, which can be run from the command line, or as a part of the git commit mechanism.

### Standalone RDF Toolkit Application

You can use `RDF Toolkit` to serialize any RDF file to a required format whether it is inside a version controlled folder or not. To find its current options, run this command in your operating system's command-line interface (e.g., Terminal in Linux and Mac OS, Command Shell in Windows, etc.):
```
java -jar rdf-toolkit.jar --help
```
For instance, if you want to serialise an RDF/XML file 'example.rdf', which is located in C:/source_ontologies/ folder into a Turtle file 'example.ttl' in C:/serialised_ontologies/ folder, you may use the following command:
```
java -jar rdf-toolkit.jar --source C:/source_ontologies/example.rdf --target C:/serialised_ontologies/example.ttl  --infer-base-iri 
```

#### RDF Toolkit options

```
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
 -sni,--suppress-named-individuals       suppresses all instances of owl:NamedIndividual
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
 -udl, --use-default-language <arg>      default language tag for untagged strings
 -v,--version                            print out version details
```

### RDF Toolkit For Git

You can use `RDF Toolkit` as a built-in serialization tool that is launched each time you add a commit to your local Git repository. This will guarantee that every commit you do will re-write your RDF/OWL files in a consistent way that can be compared and merged with work done by other FIBO collaborators. 

#### Setup
Actually, in order to use this `RDF Toolkit` in this capacity, you will also need a [pre-commit file](https://raw.githubusercontent.com/edmcouncil/rdf-toolkit/develop/etc/git-hook/pre-commit).

You need to copy these two files:
* [pre-commit](https://raw.githubusercontent.com/edmcouncil/rdf-toolkit/develop/etc/git-hook/pre-commit) (no file extension)
* [rdf-toolkit.jar](https://jenkins.edmcouncil.org/view/rdf-toolkit/job/rdf-toolkit-build/lastSuccessfulBuild/artifact/target/rdf-toolkit.jar)

to the .git/hooks/ folder inside your local Git repository. The example below shows the location of this folder in a local Git repository for FIBO:

<img src="https://user-images.githubusercontent.com/11171688/182847941-f4ab97ee-a7bf-447f-8515-f9feac68c471.png" width="250"/>

Make sure that:
- you have set the environment variable JAVA_HOME to the location of your Java
  - if you have not because you don't know how to do it, ask uncle Google, e.g., by visiting https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux
  - if you want to point the `RDF Toolkit` to a different Java version, uncomment the following line in the pre-commit file pointing to the required path: `# export RDF_TOOLKIT_JAVA_HOME=<path_to_java>`
- name of the pre-commit file is just 'pre-commit' - your file browser might want to append a suffix like .txt to it
- you update these files from time to time since they both may be changed
- in an *nix-similar system (e.g., Linux, macOS, etc.) you may need to make the pre-commit file executable - see, for example, https://www.warp.dev/terminus/chmod-x.
  - if the file gets quarantined, you may need to follow this hint https://stackoverflow.com/a/60082378/2596664.

#### Run 

You don't have to do anything to run `RDF Toolkit` in this mode because every commit in Git will start it for you.
When it runs properly, you should be able to see in your git console messages that look like the ones below:

```
rdf-toolkit: sesame-serializer: This is the pre-commit hook
rdf-toolkit: sesame-serializer: java_home = /Library/Java/JavaVirtualMachines/jdk-16.0.1.jdk/Contents/Home/
rdf-toolkit: sesame-serializer: whichJava = /Library/Java/JavaVirtualMachines/jdk-16.0.1.jdk/Contents/Home//bin/java
rdf-toolkit: sesame-serializer: Found rdf-toolkit: /***/.git/hooks/rdf-toolkit.jar
rdf-toolkit: sesame-serializer: Launching the sesame-serializer with --source DE/CarControl.rdf
...
+ rc=0
+ set +x
rdf-toolkit: sesame-serializer: Re-adding potentially re-serialized file to git staging area: VC/VehicleParts.rdf
[auto-83_missing_definitions d6f582e] more changes after review of definitions
 3 files changed, 17 insertions(+), 17 deletions(-)
```

# Serialisation Algorithm Explained

To ensure that an ontology is always serialized in the same manner, the serialization algorithm has been implemented in the RDF Toolkit. The following description attempts to give a short overview of this algorithm.

Serialization has three consecutive stages:
1. Serialization of the header
2. Triples' serialization
3. Serialization of the footer

## Header

This depends on the type of the serialization format, e.g. in the case of RDF/XML, the XML starting tag is used followed by the DTD declaration (if it is enabled).

Then all namespaces are written alphanumerically.

Optionally, leading comments are serialized.

## Triples

First, we write triples whose subject is the ontology, including "import triples", and then all other triples - starting with the subset of those that do not contain blank nodes.

As a rule, all triples are sorted as follows:
1. First, we sort all triples by their subjects.
2. Then, within each set of triples with the same subject, we sort by predicates.
3. Finally, within each set of triples with the same subject and predicate, we sort by objects.

This sorting of triples presupposes that all resources within an ontology are sorted first.

### Resource Sorting

* By default, all resources are sorted alphanumerically - with the following exceptions:
    * All blank nodes come after IRIs.
    * All RDF collections come before any other blank node.
    * Serialization names for blank nodes are prepared using the pattern `blankXXX`, where `XXX` represents the ordinal number of the given blank node, e.g. `001`, `010`, `999`. The number of `X`s in this pattern is based on the number of blank nodes.

## Footer

This part depends on the serialisation used, e.g., in the case of RDF/XML it is one line:

```</rdf:RDF>```
