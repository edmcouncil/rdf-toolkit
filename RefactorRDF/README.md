__RefactorRDF__ is a utility for performing global changes on a set of RDF or OWL files
in support of refactoring or other global operations.

RefactorRDF is intended to operate across a set of .rdf or .owl files in nested directories.
It is called with a reference to a rule file that defines the operations to perform across 
the set of rdf/owl files. See below for specific rule syntax.
	
# Installation:
	RefactorRDF requires Python and RDFLib, see: 
		https://www.python.org/
		http://rdflib.readthedocs.io/en/stable/gettingstarted.html#
		To use the default "changed.bat" script, FIBO rdf-toolkit.jar
		must be copied to the same directory as the script. See:
			[https://github.com/edmcouncil/rdf-toolkit/]
		Tested with: python 2.7.14 and FIBO .rdf files on MS-Windows
		
# Executing RefactorRDF: 
python RefactorRDF.py <RulesFile> <Directory> 
or with the following command line options (may be abbreviated to first letter):
	-rules <RulesFile>	          - an XML file that defines the refactoring rules.
	-source <Directory>          - directory that is the root of a directory structure 
	                                containing source rdf/owl files
	-destination <Destination>   - directory to write modified RDF/OWL files
	-extension <file extension>  - file extension to scan (Default = rdf & .owl)
	-log <Log file>              - (Default RefactorRDFLog.txt in the <RulesFile> directory)
	-command "command"           - (default "call changed \"%s\"\\n") - post processing command 
	                                in exported batch file, one per changed file (python format string)
	-batch <batch/script file>   - (default ChangedFiles.bat) - exported batch file 
	                                to post-process each ontology
	-format <RDF-Syntax>         - (default "rdf-xml") - RDF file format
	-noise <"trd">               - Log noise detail for log <t>riples, <r>ead, <d>irectory
	-help 
	-help example 	              - show an example rules file
A log of changes is written to the log file and a batch file exported for post processing of each changed ontology.
	


# Format of the rules file (See python RefactorRDF.py -help example ) --

The rules file is an XML document encoded as utf-8
The root of the rules document is an XML element <rules>
which contains a set of individual rules that are applied to every triple in
every file.

## Arguments of the root "rules" element:
	changeSuffix="NAME" that will be appended to every changed file name
		without this argument the files are replaced when changed.
		In all cases, it is recommended to perform these operations on a copy of a set of ontologies.
		or use "-destination <directory>
	
	exclude="+<filename>..." to exclude files from consideration.
		each filename must have "+" preprepared for uniqueness. E.g. "+file1.rdf+dir/file2.rdf"

## The following rules are supported:

### Type: The type rule changes all references to a class or property to another type.
	Format:
		<type from=<from-reference> to="<to-reference> kind=<property-kind> />
			- from-reference: The class or property to be replaced
			- to-reference: The replacement class or property
			- property-kind (optional): either "ObjectProperty" or "DatatypeProperty"
			  as the appropriate type of property for the destination type.
			  all property definitions referencing this type will be changed to
			  the indicated kind.
			  
### Replace: The replace rule changes all references or literals from one value to another
	Format:
		<replace from=<from> to="<to> match=<spo> predicate=<selector> />
			- from: The reference or literal to change from
			- to: The replacement reference or literal, not specifying "to" causes a delete
			- match (optional): Any combination of "s", "p" or "o" (defaults to all - "spo")
				Indicates what should be changed: <s>ubject, <p>redicate and/or <o>object
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			  
### Delete: Delete all mentions of a URI or literal
	Format:
		<delete from=<from> match=<spo> predicate=<selector> />
			- from: The reference or literal to delete
			- match (optional): Any combination of "s", "p" or "o" (defaults to all - "spo")
				Indicates what should be matched: <s>ubject, <p>redicate and/or <o>object
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			
### Edit: The edit rule edits the object of a triple as a string, replacing text.
	Format:
		<edit from=<from> to="<to>  predicate=<selector> />
			- from: The subtext to replace
			- to: The replacement subtext
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			
### Namespace: The namespace rule replaces just the namespace portion of a URI reference. If prefix is specified
	a namespace prefix and import is created for any ontology that is otherwise changed by rules and uses that namespace.
	It will also remove the "from" namespace and import.
	Format:
		<namespace from=<from> to="<to>  prefix=<prefix> />
			- from (optional): The namespace to replace. If not specified, the rule will only set the prefix
			- to: The replacement namespace
			- prefix (optional): The prefix of the replacement namespace.
			- remove-prefix (optional) = "<prefix>" : Removes namespace declaration for prefix 
			  even if this is the only change in the ontology. For ontologies with other changes
			  old prefix will be removed without this attribute.
			- dependencies {adjust|all} - adjust imports based on usage, all will modify ontologies with no other changes

## Rule Processing
		* Rules are processed in order for each triple.
		* All triple rules will stop further rules from firing for a triple unless the rule contains continue="true"
		* After execution the following are output:
			* RefactorRDFLog.txt - a log of all changes (or -log <file>)
			* ChangedFiles.bat - a windows batch file executing "changed" for each file changed to allow for (or -batch <file>)
				post-processing. In FIBO this calls "changed" which then calls the XML Serializer. (or -command "command")
				
	Note that output files are not in "pretty xml" format as this did not work with RDFLIB and 
	the FIBO rdf-toolkit serializer may be used to reformat rdf-xml in a normalized syntax.
