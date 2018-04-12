# -&- coding: utf-8 -*-
'''
******************************************************************************* 
Copyright (c) 2017-2018 Model Driven Solutions, Inc. 
All rights reserved worldwide. This program and the accompanying materials 
are made available for use under the terms of the The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.	
********************************************************************************
'''
help="""
RefactorRDF is a utility for performing global changes on a set of RDF or OWL files
in support of refactoring or other global operations.

RefactorRDF is intended to operate across a set of .rdf or .owl files in nested directories.
It is called with a reference to a rule file that defines the operations to perform across 
the set of rdf/owl files. See below for specific rule syntax.
	
#Installation:
	RefactorRDF requires Python and RDFLib, see: 
		https://www.python.org/
		http://rdflib.readthedocs.io/en/stable/gettingstarted.html#
		TO use the default "changed.bat" script, FIBO rdf-toolkit.jar
		must be copied to the same directory as the script. See:
			[https://github.com/edmcouncil/rdf-toolkit/]
		Tested with: python 2.7.14 and FIBO .rdf files on MS-Windows
		
#Executing RefactorRDF: 
python RefactorRDF.py <RulesFile> <Directory> 
or with the following command line options (may be abbreviated to first letter):
	--rules <RulesFile>	          - an XML file that defines the refactoring rules.
	--source <Directory>          - directory that is the root of a directory structure 
	                                containing source rdf/owl files
	--destination <Destination>   - directory to write modified RDF/OWL files
	--extension <file extension>  - file extension to scan (Default = rdf & .owl)
	--log <Log file>              - (Default RefactorRDFLog.txt in the <RulesFile> directory)
	--command "command"           - (default "call changed \"%s\"\\n") - post processing command 
	                                in exported batch file, one per changed file (python format string)
	--batch <batch/script file>   - (default ChangedFiles.bat) - exported batch file 
	                                to post-process each ontology
	--format <RDF-Syntax>         - (default "rdf-xml") - RDF file format
	--noise <"trd">               - Log noise detail for log <t>riples, <r>ead, <d>irectory
	--help 
	--help example 	              - show an example rules file
A log of changes is written to the log file and a batch file exported for post processing of each changed ontology.
	


#Format of the rules file (See python RefactorRDF.py -help example ) --

The rules file is an XML document encoded as utf-8
The root of the rules document is an XML element <rules>
which contains a set of individual rules that are applied to every triple in
every file.

##Arguments of the root "rules" element:
	changeSuffix="NAME" that will be appended to every changed file name
		without this argument the files are replaced when changed.
		In all cases, it is recommended to perform these operations on a copy of a set of ontologies.
		or use "-destination <directory>
	
	exclude="+<filename>..." to exclude files from consideration.
		each filename must have "+" preprepared for uniqueness. E.g. "+file1.rdf+dir/file2.rdf"

##The following rules are supported:

	###Type: The type rule changes all references to a class or property to another type.
	Format:
		<type from=<from-reference> to="<to-reference> kind=<property-kind> />
			- from-reference: The class or property to be replaced
			- to-reference: The replacement class or property
			- property-kind (optional): either "ObjectProperty" or "DatatypeProperty"
			  as the appropriate type of property for the destination type.
			  all property definitions referencing this type will be changed to
			  the indicated kind.
			  
	###Replace: The replace rule changes all references or literals from one value to another
	Format:
		<replace from=<from> to="<to> match=<spo> predicate=<selector> />
			- from: The reference or literal to change from
			- to: The replacement reference or literal, not specifying "to" causes a delete
			- match (optional): Any combination of "s", "p" or "o" (defaults to all - "spo")
				Indicates what should be changed: <s>ubject, <p>redicate and/or <o>object
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			  
	###Delete: Delete all mentions of a URI or literal
	Format:
		<delete from=<from> match=<spo> predicate=<selector> />
			- from: The reference or literal to delete
			- match (optional): Any combination of "s", "p" or "o" (defaults to all - "spo")
				Indicates what should be matched: <s>ubject, <p>redicate and/or <o>object
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			
	###Edit: The edit rule edits the object of a triple as a string, replacing text.
	Format:
		<edit from=<from> to="<to>  predicate=<selector> />
			- from: The subtext to replace
			- to: The replacement subtext
			- selector (optional) - restricts replacements to a single property type (defaults to any)
			
	###Namespace: The namespace rule replaces just the namespace portion of a URI reference. If prefix is specified
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

	##Rule Processing
		* Rules are processed in order for each triple.
		* All triple rules will stop further rules from firing for a triple unless the rule contains continue="true"
		* After execution the following are output:
			* RefactorRDFLog.txt - a log of all changes (or -log <file>)
			* ChangedFiles.bat - a windows batch file executing "changed" for each file changed to allow for (or -batch <file>)
				post-processing. In FIBO this calls "changed" which then calls the XML Serializer. (or -command "command")
				
	Note that output files are not in "pretty xml" format as this did not work with RDFLIB and 
	the FIBO rdf-toolkit serializer may be used to reformat rdf-xml in a normalized syntax.
"""
example="""
Example:
<?xml version="1.0"  encoding="utf-8"?>

<!DOCTYPE rdf:RDF [
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY fibo-fnd-pty-rl "https://spec.edmcouncil.org/fibo/FND/Parties/Roles/">
	<!ENTITY fibo-fnd-rel-rel "https://spec.edmcouncil.org/fibo/FND/Relations/Relations/">
	<!ENTITY fibo-fnd-utl-val "https://spec.edmcouncil.org/fibo/FND/Utilities/Values/">
	<!ENTITY fibo-fnd-util-ctx "https://spec.edmcouncil.org/fibo/FND/Utilities/Context/">
	<!ENTITY fibo-fnd-utl-bt "https://spec.edmcouncil.org/fibo/FND/Utilities/BusinessFacingTypes/">
]>

<rules changeSuffix="_CHANGED">
	<type from="&fibo-fnd-plc-fac;Site" to="&xsd;string" kind="DatatypeProperty" />
	<type from="&fibo-fnd-utl-bt;Percentage" to="https://spec.edmcouncil.org/fibo/FND/UtilitiesExt/Values/PercentageValue" kind="ObjectProperty" />
	<replace from="&fibo-fnd-pty-rl;ThingInRole" to="&fibo-fnd-pty-rl;EntityInRole"  match="so"/>
	<edit from="surface of the Earth" to="surface of a planet"  />
	<delete from="&fibo-fnd-pty-rl;DontCare"   />
	<namespace from="&fibo-fnd-rel-rel;" to="&fibo-fnd-util-ctx;" />
	<namespace to="http://www.omg.org/techprocess/ab/SpecificationMetadata/" prefix="sm" dependencies="adjust"/>
</rules>
"""

'''
Note that this is not very efficient code, so what. It is a utility and fast enough. It should also make better
use of python objects, perhaps next time.
'''

import os
import sys
import string

import rdflib as RDFLIB
import time
import xml.etree.ElementTree as ET
import codecs
from rdflib import URIRef, Literal, Namespace, BNode

activedir = {}

RulesFileName ="RefactorRDFRules.xml"
LogFile = None
LogFileName = None
RefactorRules = None
ChangedFileScript = None
changeSuffix = None
ChangedFileCommand = "call changed \"%s\"\n"
outputFormat = "rdf-xml" # Alt "pretty-xml" has issues
reportDetail = "trd" # Log triples, reads and directories
ChangedFileName = None
dependencies = None # By default, don't filter dependencies.
prefixed	= {} #Dictionary of defined namespaces
changes = 0
errors = 0
excluded = "" # Excluded files
globalChange = False
newTriples = None # Triples created for current onotology
removeTriples = None # triples removed for current ontology
suppressNamespace = set() # Not clear how to remove a namespace
newURIs = set() # URIs added by rules, used to filter new namespaces
totalChanges = 0
fileCount = 0
filesChanged = 0

def rdfURI(uri) : # Makes unicode a URI and adds it to new URIs
	global newURIs
	newURIs.add(unicode(uri))

	return URIRef(uri) 


RDFSRange = rdfURI(u"http://www.w3.org/2000/01/rdf-schema#range")
RDFSSeeAlso = rdfURI(u"http://www.w3.org/2000/01/rdf-schema#seeAlso")
RDFType = rdfURI(u"http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
OWLObjectProperty = rdfURI(u"http://www.w3.org/2002/07/owl#ObjectProperty")
OWLDatatypeProperty = rdfURI(u"http://www.w3.org/2002/07/owl#DatatypeProperty")
OWLOnProperty = rdfURI(u"http://www.w3.org/2002/07/owl#onProperty")
OWLOnClass = rdfURI(u"http://www.w3.org/2002/07/owl#onClass")
OWLOnDataRange = rdfURI(u"http://www.w3.org/2002/07/owl#onDataRange")
OWLAllValuesFrom = rdfURI(u"http://www.w3.org/2002/07/owl#allValuesFrom")
OWLSomeValuesFrom = rdfURI(u"http://www.w3.org/2002/07/owl#someValuesFrom")
OWLImports = rdfURI(u"http://www.w3.org/2002/07/owl#imports")
OWLOntology = rdfURI(u"http://www.w3.org/2002/07/owl#Ontology")
OWLVersionIRI = rdfURI(u"http://www.w3.org/2002/07/owl#versionIRI")

ignoreImport = (u"http://www.w3.org/2002/07/owl#", u"http://www.w3.org/2004/02/skos/core#", u"http://www.w3.org/2000/01/rdf-schema#", u"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				u"http://www.w3.org/2001/XMLSchema#", u"http://purl.org/dc/terms/", u"http://www.omg.org/techprocess/ab/SpecificationMetadata/") # Only import if they did previously
				
ignoreImportPredicate = (unicode(OWLImports), # These predicates will not cause a namespace to be imported.
	unicode(OWLVersionIRI), 
	unicode(RDFSSeeAlso), 
	u"http://www.w3.org/2002/07/owl#backwardCompatibleWith" , 
	u"http://www.w3.org/2002/07/owl#priorVersion" , 
	u"http://www.omg.org/techprocess/ab/SpecificationMetadata/specificationURL" , 
	u"http://www.w3.org/2002/07/owl#incompatibleWith")

'''
	RDF rules - call each rule for each triple in every file
'''

#
# Rule to Replace a reference or literal
#
def replaceRDFRule(ontology, name, rule, subject, predicate, obj) :
	global changes, errors

	select = rule.attrib.get(u"predicate")
	if (select and select!=unicode(predicate)) :
		return False
		
	what = rule.attrib.get(u"match")
	if not what:
		what = "spo"
	
	found = False
	fromThis = rule.attrib[u"from"]
	toThat = rule.attrib.get(u"to")
	
	if (u"s" in what) and (fromThis == unicode(subject)) :
		startChange(ontology, name)
		writeLog (u"....Change subject <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, toThat))
		removeTriples.add(( subject, predicate, obj ))
		if toThat :
			newTriples.add( ( asRDF(subject, toThat), predicate, obj ))
		found = True
	elif (u"p" in what) and (fromThis == unicode(predicate)) :
		startChange(ontology, name)
		writeLog (u"....Change predicate <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, toThat))
		removeTriples.add(( subject, predicate, obj ))
		if toThat :
			newTriples.add( ( subject, asRDF(predicate, toThat), obj ))		
		writeLog(u"----Warning Changed property type in use <%s> <%s> <%s>"% subject, predicate, obj)
		errors+=1
		found = True
	elif (u"o" in what) and (fromThis == unicode(obj)) :
		startChange(ontology, name)
		writeLog (u"....Change object <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, toThat))
		removeTriples.add(( subject, predicate, obj ))
		if toThat :
			newTriples.add( ( subject, predicate, asRDF(obj, toThat) ))
		found = True
	return found
#
# Delete all mention of a URI or literal
#
def deleteRDFRule(ontology, name, rule, subject, predicate, obj) :

	select = rule.attrib.get(u"predicate")
	if (select and select!=unicode(predicate)) :
		return False
		
	what = rule.attrib.get(u"match")
	if not what:
		what = u"spo"
	
	found = False
	fromThis = rule.attrib[u"from"]
	
	if (u"s" in what) and (fromThis == unicode(subject)) :
		startChange(ontology, name)
		removeTriples.add(( subject, predicate, obj ))
		found = True
	elif (u"p" in what) and (fromThis == unicode(predicate)) :
		startChange(ontology, name)
		removeTriples.add(( subject, predicate, obj ))
		found = True
	elif (u"o" in what) and (fromThis == unicode(obj)) :
		startChange(ontology, name)
		removeTriples.add(( subject, predicate, obj ))
		found = True
	if found :
		writeLog (u"....Delete <%s> <%s> <%s>  "%( subject, predicate, obj))
		
	return found

#
# Change all referenced from=namespace  to  "to=namespace", optionally define "prefix="
#
def namespaceRDFRule(ontology, name, rule, subject, predicate, obj) :
	global changes, errors
	
	fromThis = rule.attrib.get(u"from")
	if (not fromThis) : # Just defines the prefix
		return False
	
	found = False
	
	if (unicode(subject).startswith(fromThis)) :
		startChange(ontology, name)
		#if (startChange(ontology, name)):
		setPrefix(ontology, rule.attrib[u"to"], rule.attrib.get(u"prefix"), rule.attrib[u"from"])
		newval = ( unicode(subject).replace(fromThis, rule.attrib[u"to"], 1 ) )
		writeLog (u"....Namespace change subject <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, rule.attrib[u"to"]))
		removeTriples.add(( subject, predicate, obj ))
		newTriples.add( (asRDF(subject, newval), predicate, obj) )
		found = True
	if (unicode(predicate).startswith(fromThis)) :
		startChange(ontology, name)
		#if (startChange(ontology, name)):
		setPrefix(ontology, rule.attrib[u"to"], rule.attrib.get(u"prefix"), rule.attrib[u"from"])
		newval = ( unicode(predicate).replace(fromThis, rule.attrib[u"to"], 1 ) )
		writeLog (u"....Namespace change predicate <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, newval))
		removeTriples.add(( subject, predicate, obj ))
		newTriples.add( ( subject, asRDF(predicate, newval), obj ))		
		found = True

	if (unicode(obj).startswith(fromThis)) :
		startChange(ontology, name)
		#if (startChange(ontology, name)):
		setPrefix(ontology, rule.attrib[u"to"], rule.attrib.get(u"prefix"), rule.attrib[u"from"])
		newval = ( unicode(obj).replace(fromThis, rule.attrib[u"to"], 1 ) )
		#newval = rule.attrib[u"to"]+unicode(obj)[len(fromThis):]
		writeLog (u"....Namespace change object <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, newval))
		removeTriples.add(( subject, predicate, obj ))
		if not (unicode(predicate)==unicode(OWLImports) and unicode(obj)==fromThis):# Remove import of "to" unless used (put back by fixup)
			newTriples.add( ( subject, predicate, asRDF(obj, newval)))
		else:
			writeLog (u"....Conditional import %s"%( newval))
			
		found = True
	return found	

#
# Namespace processing after triple rules have been processed
#
def namespaceOntologyRule(ontology, rule, name) : # Executed after ontology has been processed
	global changes, errors, suppressNamespace, dependencies, prefixed, ignoreImport, globalChange
	fromThis = rule.attrib.get(u"from")
	if fromThis:
		suppressNamespace.add(unicode(fromThis))
	if rule.attrib.get(u"prefix") and rule.attrib[u"to"]:
		# Define new prefix
		setPrefix(ontology, rule.attrib[u"to"], rule.attrib.get(u"prefix"), rule.attrib.get(u"from"))
		
	removePrefix = rule.attrib.get(u"remove-prefix")
	# See if ontology contains prefix to be removed
	# This is optional as it does not change triples
	if removePrefix or fromThis:
		for prefix, ns in ontology.namespaces():
			prefix = unicode(prefix)
			ns = unicode(ns)
			if prefix==removePrefix or ns==fromThis:
				if removePrefix :
					startChange(ontology, name)
				suppressNamespace.add(unicode(ns))
				if changes:
					writeLog (u"....remove prefix declaration %s: <%s> "%( prefix, ns ))
					return True
					
	dependencies = rule.attrib.get(u"dependencies")
	if dependencies:
		# Filter out unused imports
		if (unicode(dependencies)==u"all"): # Filter out even if no other changes
			globalChange = True
			startChange(ontology, name)
		# Include all namespaces in those to be filtered
		for prefix, ns in ontology.namespaces():
			if (not unicode(ns) in prefixed) and not unicode(ns) in ignoreImport:
				prefixed[unicode(ns)] = unicode(prefix);
					
	return False

def nullOntologyRule(ontology, rule, name) : # Executed after ontology has been processed
	return False


def typeRDFRule(ontology, name, rule, subject, predicate, obj) :
	global changes, errors
	#print "Property rule for ", subject, predicate, obj
	#writeLog(u"Property:"+rule.attrib[u"from"])
	#writeLog(obj)
		
	found = False
	proptype = rule.attrib.get(u"kind")

	if (rule.attrib[u"from"] == unicode(predicate)) :
		startChange(ontology, name)
		writeLog (u"....Property change predicate <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, rule.attrib[u"to"]))
		newTriples.add( ( subject, asRDF(obj, rdfURI(rule.attrib[u"to"])), obj ))
		if (rule.attrib.get(u"kind")) :
			writeLog(u"----Warning Changed property type in use, inspect <%s> <%s> <%s>"% subject, predicate, obj)
			errors+=1
		return False

	if (rule.attrib[u"from"] == unicode(obj)) :
		startChange(ontology, name)
		writeLog (u"....Change type reference <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, rule.attrib[u"to"]))
		removeTriples.add(( subject, predicate, obj ))
		if ( (proptype==u"DatatypeProperty") and (compareURI(predicate,OWLOnClass))):		
			newTriples.add( (subject, OWLOnDataRange, asRDF(obj, rule.attrib[u"to"]) ) )
		elif ( (proptype==u"ObjectProperty") and (compareURI(predicate,OWLOnDataRange))):
			newTriples.add( (subject, OWLOnClass, asRDF(obj, rule.attrib[u"to"]) ) )
		else:
			if (unicode(predicate)!=unicode(OWLImports) ):# Remove import of "to" unless used (put back by fixup)
				newTriples.add( ( subject, predicate, asRDF(obj, rdfURI(rule.attrib[u"to"])) ))
		found = True
		
	# Handle ObjectProperty & DatatypeProperty conversion----------------------
	if (not found) :
		return False
	if (not proptype) :
		return True
	changeMe = None
	if (compareURI(predicate,RDFSRange)) : # Range
		changeMe = subject 
	elif (compareURI(predicate,OWLSomeValuesFrom) or (compareURI(predicate,OWLAllValuesFrom)) or (compareURI(predicate,OWLOnClass))): #Restriction
		for s,p,o in ontology.triples( (subject, OWLOnProperty, None) ):
			changeMe = o # should only be one
		#if (compareURI(predicate,OWLOnClass)): #		if (proptype=="ObjectProperty") and (compareURI(predicate,OWLOnClass)):

		#	writeLog(u"----Warning: Check removal of onClass restriction <%s> "% (o))
			#for s,p,o in ontology.triples( (subject, predicate, None) ):
			#	removeTriples.add( (s,p,o) )
		#	removeTriples.add( (subject, predicate, obj) )
		#	newTriples.add( (subject, OWLSomeValuesFrom, rdfURI(rule.attrib[u"to"]) ) )
		#	changes += 1
		#	errors+=1
	else:
			writeLog(u"----WarningCould not find property definition for <%s><%s><%s>"% (changeMe, predicate, obj))
			errors+=1
			return True
	if (proptype==u"ObjectProperty") : # Change from datatype to object property
		# Find Datatype property triple
		if (changeMe, RDFType, OWLDatatypeProperty) in ontology :
			removeTriples.add( (changeMe, RDFType, OWLDatatypeProperty) )
			newTriples.add( (changeMe, RDFType, OWLObjectProperty ) )
			changes += 1
			writeLog(u"...... <%s> changed to ObjectProperty"% (changeMe))
		
	elif (proptype==u"DatatypeProperty") : # Change from object to datatype property
		# Find Object property triple
		if (changeMe, RDFType, OWLObjectProperty) in ontology :
			removeTriples.add( (changeMe, RDFType, OWLObjectProperty) )
			newTriples.add( (changeMe, RDFType,OWLDatatypeProperty ) )
			changes += 1
			writeLog(u"...... <%s> changed to DatatypeProperty"% (changeMe))	
			
	else :
			writeLog(u"----Warning could note find type of <%s>"% (changeMe))
			errors+=1			
	return True
	
def editRDFRule(ontology, name, rule, subject, predicate, obj) :
	global changes, errors
	if (rule.attrib[u"from"] in unicode(obj)) :
		select = rule.attrib.get(u"predicate")
		if (select and select!=unicode(predicate)) :
			return
		startChange(ontology, name)
		newValue = unicode(obj).replace(rule.attrib[u"from"],rule.attrib[u"to"])
		writeLog (u"....Replace substring <%s> <%s> <%s> to <%s> "%( subject, predicate, obj, newValue))
		removeTriples.add(( subject, predicate, obj ))
		newTriples.add( ( subject, predicate, asRDF(obj, newValue) ))		
		return True
'''
	Support functions for rules
'''

def startRules(ontology):
	global newTriples, removeTriples, changes, prefixed, suppressNamespace,  newURIs
	newTriples = None
	removeTriples = None
	changes = 0
	prefixed = {}
	suppressNamespace = set()
	newURIs = set()

def startChange(ontology, name) :
	global newTriples, removeTriples, changes
	firstchanged = changes==0
	if (firstchanged):
		writeLog (u"..Ontology <%s> "%( name ))
		newTriples = RDFLIB.Graph()
		removeTriples = RDFLIB.Graph()
	changes+=1
	return firstchanged

def endRules(ontology) :
	global newTriples, removeTriples, changes
	if changes :
		# patch to fix no langString & no lang=
		for s,p,o in ontology:
			if isinstance(o, Literal):
				if unicode(o.datatype)==u"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" or unicode(o.datatype)=="http://www.w3.org/2001/XMLSchema#string":
					if not((s,p,o) in removeTriples):
						removeTriples.add( (s,p,o) )
						newTriples.add( (s,p,asRDF(o,unicode(o)) )) # Suppress langSring w/o lang=
		ontology -= removeTriples # Make changes to target ontology!
		ontology += newTriples



def compareURI(a,b) :
	return unicode(a)==unicode(b)
	
def asRDF(old, new) :
		# Make a new RDF value of the same kind as the original
		
		if (isinstance(old, URIRef)) :
			return rdfURI(new)	

		ln = old.language
		dt = old.datatype
		if unicode(dt)==u"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" and not ln: # Filter out string types as default
			ln = "en" # Default to en
			dt = None
		#if unicode(dt)==u"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" or unicode(dt)=="http://www.w3.org/2001/XMLSchema#string": # Filter out string types as default
		if unicode(dt)=="http://www.w3.org/2001/XMLSchema#string" or ln: # Filter out string types as default
			dt = None
		
		return Literal(new, datatype=dt, lang=ln )
		

		
def setPrefix(ontology, ns, prefix, oldNS) :
	global prefixed, suppressNamespace
	if oldNS:
		suppressNamespace.add(unicode(oldNS)) 		#Get rid of old namespace, not clear how to delete one

	if (prefix and ns and not ns in prefixed) :
		prefixed[ns] = prefix
				
def fixupNamespaces(ontology):
	global prefixed, suppressNamespace, newTriples, removeTriples, changes, errors
	for s,p,o in ontology: # Find everthing used
		newURIs.add(s)
		newURIs.add(p)
		if (not (unicode(p) in ignoreImportPredicate)) and (isinstance(o, URIRef)):# Don't count for imports
			newURIs.add(o)

	ontologyURI = None
	for s,p,o in ontology.triples( (None, RDFType, OWLOntology) ): # Find the referenced ontology
		ontologyURI = unicode(s)
		break
			
	if prefixed:
		# Check that new prefixes are used by new URIs
		for ns, prefix in prefixed.copy().iteritems():
			used = False
			for uri in newURIs:
				if uri.startswith(unicode(ns)): # ns used as prefix
					if not '/' in unicode(uri)[len(ns):] : # Filter out incomplete prefixes
							used = True
							break
			if not used:
				del prefixed[ns]
				suppressNamespace.add(unicode(ns))
		
		# Add namespaces and imports for new prefixes & namespaces

		if not ontologyURI:
			writeLog ("..Warning no ontology URI")
			print ("..Warning no ontology URI")
			errors+=1
			return ontologyURI
	
		# Make imports match intent
		for ns, prefix in prefixed.iteritems():
			#newTriples.add( (s, OWLImports, rdfURI(ns) )) # Import the referenced ontology
			if unicode(ns) in suppressNamespace:
				if (URIRef(ontologyURI), OWLImports, rdfURI(ns) ) in ontology:
					startChange(ontology, ontologyURI)
					ontology.remove ( (URIRef(ontologyURI), OWLImports, URIRef(ns)) )
					removeTriples.add( (URIRef(ontologyURI), OWLImports, URIRef(ns)) )
					writeLog("----Remove import: %s"%(ns))

			else:
				if prefix:
					ontology.namespace_manager.bind(prefix, Namespace(ns), override=True, replace=True )
				if (not (URIRef(ontologyURI), OWLImports, rdfURI(ns) ) in ontology) and (unicode(ns)!=ontologyURI):
					startChange(ontology, ontologyURI)
					ontology.add( (URIRef(ontologyURI), OWLImports, rdfURI(ns) )) # Import the referenced ontology
					newTriples.add( (URIRef(ontologyURI), OWLImports, rdfURI(ns) ))
					writeLog("----Add import: %s"%(ns))
	return ontologyURI	

'''
	Maps to bind XML tags to rules
'''
rdfRules = {"replace":replaceRDFRule, "edit":editRDFRule, "type":typeRDFRule, "namespace":namespaceRDFRule, "delete":deleteRDFRule } # Rules for each RDF triple
ontologyRules = {"replace":nullOntologyRule, "edit":nullOntologyRule, "type":nullOntologyRule, "namespace":namespaceOntologyRule, "delete":nullOntologyRule} # Post process rules
'''
	Methods to find files and process rules
'''

def processRules(ontology, name):
	global changes, prefixed
	startRules(ontology)
	root = RefactorRules.getroot()
	for subject, predicate, obj in ontology :
		for rule in root :
			ruleImpl = rdfRules[rule.tag]
			if (ruleImpl) :
				if ruleImpl(ontology, name, rule, subject, predicate, obj):
					#Rule returns true if fired, stop processing rules for this triple
					if not rule.attrib.get(u"continue"): #Allow rule processing to continue even if rule fired
						break
			else :
				raise Exception(u"Do not understand rule ", rule.tag)	
	endRules(ontology)
	return changes

def processOntologyRules(ontology, name):
	global changes, prefixed
	root = RefactorRules.getroot()
	for rule in root :
		ruleImpl = ontologyRules[rule.tag]
		if (ruleImpl) :
			ruleImpl(ontology, rule, name)
		else :
			raise Exception(u"Do not understand rule ", rule.tag)	
	return changes
'''
	Edit ontology file named "entry" based on rules
'''
def refactorOntology(entry, destpath):
	global ChangedFileScript, totalChanges, fileCount, filesChanged, globalChange
	print "Parse ontology: ",entry
	try: 
		ontology = RDFLIB.Graph()
		result = ontology.parse(entry)
	except Exception, ex:
		print "Error parsing ontology: ", entry
		print ex
		sys.exit(1)
	
	fileCount+=1
	
	startErr = errors
	changes= processRules(ontology, entry)
	changes = processOntologyRules(ontology, entry)
	
	if (changes or globalChange) :
		if destpath:
			writeto = destpath
		else:
			writeto = entry
		if (changeSuffix):
			dot=writeto.rindex(u".") # find the dot in .<file extension>
			left = writeto[:dot]
			right = writeto[dot:]
			writeto = left+changeSuffix+right
			
		baseNS = fixupNamespaces(ontology)
		
		if ((not newTriples and not removeTriples) or (len(newTriples)==0 and len(removeTriples)==0)): #((changes==1) and (dependencies!="all")) or 
			# No real changes
			return 0

		totalChanges += changes
		filesChanged+=1
		if startErr-errors:
			writeLog(u"Made %d changes with %d warnings in: %s"%(changes,startErr-errors, writeto))	
		else :
			writeLog(u"Made %d changes in: %s"%(changes,writeto))	

		#Output RDF file
		try: 
			ontology.serialize(writeto, format=outputFormat, xml_base=baseNS, base=baseNS, suppress=suppressNamespace) # pretty-xml did not work
		except Exception, ex:
			print "Error writing ontology: ", writeto
			print ex
			sys.exit(1)

		ChangedFileScript.write(ChangedFileCommand%(writeto));
		print "+Made %d changes with %d warnings in: %s"%(changes,startErr-errors,writeto)
		if u't' in reportDetail:
			writeLog( "\n-------------added triples---------------" )
			for s,p,o in newTriples:
				writeLog("<%s> <%s> <%s>"%(s,p,o))
			writeLog( "\n-------------removed triples-------------" )
			for s,p,o in removeTriples:
				writeLog("<%s> <%s> <%s>"%(s,p,o))			
			writeLog( "\n-----------------------------------------\n" )
	ontology.close()

	return changes 
	


'''
	Perform "whatToDo" on each selected file in directory structure
'''
ext1 = ".owl"
ext2 = ".rdf"
def refactorDirectory(dirpath, destpath, whatToDo):
	global activedir, URIDictionary, excluded
	
	used = 0
	try:
		entries = os.listdir(dirpath)
		if u'd' in reportDetail:
			writeLog(u"Directory: "+dirpath)
		if destpath and not os.path.exists(destpath):
			os.makedirs(destpath)
	except Exception, ex:
		print "Directory error", dirpath, destpath
		print ex
		sys.exit(1)
		
	dest2 = None

	for entry in entries:
		if destpath:
			dest2 = destpath+os.sep+entry
		if entry.startswith('.') or entry.startswith('~') or ((u"+"+unicode(entry)) in excluded):
			continue
		path = dirpath+os.sep+entry
		if os.path.isdir(path) :
			used += refactorDirectory(path, dest2, whatToDo) # map sub directory
		else:
			if (((string.find(entry, ext1)>0 or string.find(entry, ext2)>0) and not( changeSuffix and ((changeSuffix+".") in entry) ))) :
				mods = whatToDo(path, dest2)
				used = used + mods

	if used:
		activedir[dirpath] = used
	return used

def writeLog(message):
	global LogFile
	LogFile.write(message+"\n")
	
'''
	Load rules file
'''
def loadRules(RulesFileName) :
	global RefactorRules, changeSuffix, excluded
	try:
		RefactorRules = ET.parse(RulesFileName)
	except Exception, ex:
		print "Rules file error:", RulesFileName
		print ex
		sys.exit(1)
		return False
	
	root = RefactorRules.getroot()
	excluded = root.get(u"exclude") # FIles not to process
	if (excluded):
		print "Exclude files: ", excluded
	else:
		excluded = ""
	changeSuffix = root.get(u"changeSuffix")
	return True
'''
	Open rules file and parse arguments
'''
destination = None
def ParseCommandLine():
	global RulesFileName, destination, LogFileName, ext1, ext2, ChangedFileCommand, outputFormat, ChangedFileName, reportDetail
	
	args = sys.argv[1:]
	
	RulesFileName = None
	rootdir = None
	destination = None
	i = 0
	dflt = 1
	while i<len(args):
		arg=unicode(args[i])
		if arg.startswith(u"--h") or arg.startswith(u"-h"): #--help
			if len(args)>(i+1) and args[i+1]=="example":
				print example
			else:
				print help.replace("#","")
			return None
		elif arg.startswith(u"--example") or arg.startswith(u"-example"): #--source directory
			print example
			return None			
		elif arg.startswith(u"--r") or arg.startswith(u"-r"): #--rules file
			RulesFileName = args[i+1]
			i+=1
			dflt = 2
		elif arg.startswith(u"--s") or arg.startswith(u"-s"): #--source directory
			rootdir = args[i+1]
			i+=1
		elif arg.startswith(u"--e") or arg.startswith(u"-e"): #--file extension (dflt .rdf & .owl)
			ext1 = args[i+1]
			ext2 = ext1
			i+=1
		elif arg.startswith(u"--d") or arg.startswith(u"-d"): #--Destination dir
			destination = args[i+1]
			i+=1
		elif arg.startswith(u"--l") or arg.startswith(u"-l"): #--Log file name
			LogFileName = args[i+1]
			i+=1
		elif arg.startswith(u"--c") or arg.startswith(u"-c"): #--Changed file command (goes in exported batch file)
			ChangedFileCommand = args[i+1]
			i+=1
		elif arg.startswith(u"--b") or arg.startswith(u"-b"): #--Changed file batch file (script)
			ChangedFileName = args[i+1]
			i+=1
		elif arg.startswith(u"--f") or arg.startswith(u"-f"): #--Format for RDF file (default rdf-xml)
			outputFormat = args[i+1]
			i+=1			
		elif arg.startswith(u"--n") or arg.startswith(u"-n"): #--Noise to log - "trd" = <t>riples, <r>ead, <d>ir
			reportDetail = unicode(args[i+1])
			i+=1			
		else :
			if dflt==1:
				RulesFileName = arg
				dflt += 1
			elif dflt==2:
				rootdir = arg
				dflt+=1
			else:
				print "Do not understand argument:", arg
				return None
		i+=1

	
	if (not RulesFileName) or (not rootdir):
		print help.replace("#","")
		print "Arguments required"
		return None
	
	RulesFileName = os.path.abspath(RulesFileName)

	return rootdir
	
'''
	Execute RefactorRDF 
'''
def RefactorRDF():
	global LogFile, LogFileName
	global RefactorRules, ChangedFileScript, destination, ChangedFileName, RulesFileName, fileCount, filesChanged

	rootdir =  ParseCommandLine()

	if not rootdir or not loadRules(RulesFileName):
		return
		
	print "Rules file:   ", RulesFileName
	print "Search / replace in directory: ", rootdir

	relto = os.path.dirname(RulesFileName)+os.sep
	if not ChangedFileName:
		ChangedFileName = relto+"ChangedFiles.bat"
	try:
		ChangedFileScript = codecs.open(ChangedFileName, "w")
	except Exception, ex:
		print "Could not write to changed file script:", ChangedFileName
		print ex
		sys.exit(1)
		
	if not LogFileName:
		LogFileName = relto+"RefactorRDFLog.txt"
	try: 
		LogFile = codecs.open(LogFileName, "w", encoding='utf-8')
	except Exception, ex:
		print "Could not write to log file:", LogFileName
		print ex
		sys.exit(1)

	writeLog(u"Time: "+time.asctime( time.localtime(time.time()) ))
	writeLog(u"Refactor directory:  "+rootdir)
	writeLog(u"Refactor rules file: "+RulesFileName)
	refactorDirectory(rootdir, destination, refactorOntology)
	
	result = "Refactor Complete with %d changes and %d warnings in %d files of %d files scanned. \nEnd time=%s"%(totalChanges, errors, filesChanged, fileCount, time.asctime( time.localtime(time.time()) ))
	writeLog(result)
	print
	print (result)
	print(u"Post-process script in: "+ChangedFileName)
	print(u"Change log in: "+LogFileName)
	LogFile.close()
	ChangedFileScript.close()
'''
	Hack - support for exporting all namespaces for entity declarations.
'''

from rdflib.plugins.serializers.xmlwriter import XMLWriter, ESCAPE_ENTITIES
from rdflib.plugins.serializers.rdfxml import XMLSerializer
from rdflib.plugin import register, Serializer
from xml.sax.saxutils import quoteattr, escape


		
class XMLSerializerExtension(XMLSerializer):
	def serialize(self, stream, base=None, encoding=None, **args):
		if 'suppress' in args:
			suppressNamespace = args['suppress']
		else:
			suppressNamespace = None
		self.base = base
		self._XMLSerializer__stream = stream
		self._XMLSerializer__serialized = {}
		encoding = self.encoding
		self.write = write = lambda uni: stream.write(
			uni.encode(encoding, 'replace'))
		# startDocument
		write('<?xml version="1.0" encoding="%s"?>\n' % self.encoding)

		# startRDF
		write('<rdf:RDF\n')

		# If provided, write xml:base attribute for the RDF
		if "xml_base" in args:
			write('   xml:base="%s"\n' % args['xml_base'])
		# TODO:
		# assert(
		#    namespaces["http://www.w3.org/1999/02/22-rdf-syntax-ns#"]=='rdf')
		if suppressNamespace:
			bindings = list(self.store.namespace_manager.namespaces()) # << Does not subset namespaces <<< CHANGE
		else:
			bindings = list(self.__bindings())
		bindings.sort()

		for prefix, namespace in bindings:
			if (not suppressNamespace) or (not unicode(namespace) in suppressNamespace) :# << Suppress namespaces <<< CHANGE
				if prefix:
					write('   xmlns:%s="%s"\n' % (prefix, namespace))
				else:
					write('   xmlns="%s"\n' % namespace)
		write('>\n')

		# write out triples by subject
		for subject in self.store.subjects():
			self.subject(subject, 1)

		# endRDF
		write("</rdf:RDF>\n")

		# Set to None so that the memory can get garbage collected.
		# self.__serialized = None
		del self._XMLSerializer__serialized
		


		
register(
    'rdf-xml', Serializer,
    'RefactorRDF', 'XMLSerializerExtension')

		
	
if __name__ == '__main__':
	RefactorRDF()