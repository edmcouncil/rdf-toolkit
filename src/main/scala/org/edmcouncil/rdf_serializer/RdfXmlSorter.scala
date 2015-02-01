package org.edmcouncil.rdf_serializer

import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document

class RdfXmlSorter private (input: Path) {


  def xmlDocument: Document = {

    val file = input.toFile
    val dbFactory = DocumentBuilderFactory.newInstance
    val dBuilder = dbFactory.newDocumentBuilder()
    val doc = dBuilder.parse(file)

    //
    // optional, but recommended
    //
    // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    //
    doc.getDocumentElement.normalize()
    doc
  }

  def sortedAsString = org.ow2.easywsdl.tooling.java2wsdl.util.XMLSorter.sort(xmlDocument)

  def printIt() = {
    print(sortedAsString)
  }

}

object RdfXmlSorter {

  def apply(path: Path) = new RdfXmlSorter(path)
}
