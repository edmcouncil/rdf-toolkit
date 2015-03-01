/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 *
 * The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package org.edmcouncil.rdf_serializer

import grizzled.slf4j.Logging
import org.openrdf.rio.RDFWriterRegistry
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.rdf.rdfxml.renderer.XMLWriterPreferences

/**
 * Serialize a given RDF or OWL file with the OWLAPI
 */
class OwlApiSerializer(private val commands: SerializerCommands) extends Logging {

  import commands._

  //
  // Ensure that the DOCTYPE rdf:RDF ENTITY section is generated
  //
  XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)

  //
  // Get hold of an ontology manager
  //
  def createOntologyManager = {
    val manager = OWLManager.createOWLOntologyManager
    manager
  }

  //
  // Get Ontology Loader Configuration
  //
  lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT) // Cause the listener to be called
    .setLoadAnnotationAxioms(true)
    .setTreatDublinCoreAsBuiltIn(true)
    .setFollowRedirects(false)

  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()


  private def saveOntology(
    ontologyManager: OWLOntologyManager,
    ontology: OWLOntology,
    format: OWLDocumentFormat
  ): Unit = {

    info(s"Saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
    info(s"In Format: $format")

    ontology.saveOntology(format, output.outputStream.get)

    ontologyManager.removeOntology(ontology)
  }

  private def run: Int = {

    val rc1 = commands.validate
    if (rc1 > 0) return rc1

    val ontologyManager = createOntologyManager
    val loader = new OwlApiOntologyLoader(ontologyManager, loaderConfiguration, commands.baseDir, commands.baseUrl)
    val ontology = loader.loadOntology(input)
    val ontologyDocumentIRI = ontologyManager.getOntologyDocumentIRI(ontology)

    info(s"Ontology Document IRI: $ontologyDocumentIRI")

    info(s"Ontology ID: ${ontology.getOntologyID}")

    val inputFormat = ontologyManager.getOntologyFormat(ontology)

    info(s"Input Format: $inputFormat")

    val outputFormat = OwlApiOutputFormats.getOutputDocumentFormatWithName(params.outputFormatName)

    //
    // Some ontology formats support prefix names and prefix IRIs. In our
    // case we loaded the pizza ontology from an rdf/xml format, which
    // supports prefixes. When we save the ontology in the new format we
    // will copy the prefixes over so that we have nicely abbreviated IRIs
    // in the new ontology document
    //
    if (inputFormat.isPrefixOWLOntologyFormat) {
      if (outputFormat.isPrefixOWLOntologyFormat) {
        val prefixedOutputFormat = outputFormat.asPrefixOWLOntologyFormat()
        //
        // For some reason the outputFormat, which is also a PrefixManager, remembers the prefixes and namespaces
        // of the previous save operation, so we have to clear it here first before we copy the inputFormat's
        // prefixes into it.
        //
        prefixedOutputFormat.clear()
        prefixedOutputFormat.copyPrefixesFrom(inputFormat.asPrefixOWLOntologyFormat())
      }
    }

    val inputOntologyMetadata = inputFormat.getOntologyLoaderMetaData
    outputFormat.setOntologyLoaderMetaData(inputOntologyMetadata)

    saveOntology(ontologyManager, ontology, outputFormat)

    0
  }
}

object OwlApiSerializer {

  def apply(commands: SerializerCommands): Int = new OwlApiSerializer(commands).run
}
