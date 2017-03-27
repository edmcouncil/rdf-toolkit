# 20170324 ElieA-L for EDM Council RDF-Toolkit
# Last revision 20170326

# List all "rdf" files in a folder and all its subfolders recursively. Display filename.extension. Redirect output to ~/Desktop/HERE.txt
find . -maxdepth 1 -type f -name "*.rdf" -not -path "*etc*" | xargs -n 1 basename > ~/Desktop/HERE.txt

# Remove .rdf from each line in ~/Desktop/HERE.txt. Redirect output to ~/Desktop/HERE2.txt
sed -e 's/\.rdf$//' ~/Desktop/HERE.txt > ~/Desktop/HERE2.txt

# Open ~/Desktop/HERE2.txt, for every line, create instance on Ontology, instance of OntologyFile, link with object property ontologyIsInOntologyFile and link to domain (folder name) via hasDirectDomain
cat ~/Desktop/HERE2.txt | while read line
do
  echo "# 
# https://spec.edmcouncil.org/rdfkit/artifact-ontology#$line

:$line a owl:NamedIndividual , :Ontology ;
	:ontologyIsInOntologyFile :$line.rdf ;
	:hasDirectDomain :domain-${PWD/*\//} .
	
# 
# https://spec.edmcouncil.org/rdfkit/artifact-ontology#$line.rdf 

:$line.rdf a owl:NamedIndividual , :OntologyFile ."

done >> ~/Desktop/HERE3.txt
