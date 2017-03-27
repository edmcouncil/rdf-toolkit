# 20170324 ElieA-L for EDM Council RDF-Toolkit
# Last revision 20170326


# List all folders and sub folder recursively
find . -type d -not -path "*etc*" -not -path "*git*" | xargs -n 1 basename > ~/Desktop/HERE.txt



# Create instances of Class Domain from above list 
cat ~/Desktop/HERE.txt | while read line
do
  echo "# 
# https://spec.edmcouncil.org/rdfkit/artifact-ontology#domain-$line

:domain-$line a owl:NamedIndividual , :Domain .
"
done > ~/Desktop/HERE2.txt
