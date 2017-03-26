/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Enterprise Data Management Council
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
///*
// * The MIT License (MIT)
// *
// * Copyright (c) 2015 Enterprise Data Management Council
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//package org.edmcouncil.serializer
//
//import grizzled.slf4j.Logging
//import org.edmcouncil.util.PotentialFile
//import org.openrdf.rio.RDFWriterRegistry
//import org.semanticweb.owlapi.apibinding.OWLManager
//import org.semanticweb.owlapi.model._
//
///**
// * Serialize a given RDF or OWL file with the OWLAPI
// */
//class OwlApiSerializer(/* private val params: CommandLineParams */) extends Logging with OnErrorAborter {
//
//  def abortOnError = true // params.abortOnError
//
//  //
//  // Ensure that the DOCTYPE rdf:RDF ENTITY section is generated
//  //
//  //XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)
//
//  //
//  // Get hold of an ontology manager
//  //
//  def createOntologyManager = {
//    val manager = OWLManager.createOWLOntologyManager
//    manager
//  }
//
//  //
//  // Get Ontology Loader Configuration
//  //
//  lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
//    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT) // Cause the listener to be called
//    .setLoadAnnotationAxioms(true)
//    .setTreatDublinCoreAsBuiltIn(true)
//    .setFollowRedirects(false)
//
//  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()
//
//  private def renameURIs(
//    ontologyManager: OWLOntologyManager,
//    ontologies: Set[OWLOntology],
//    format: OWLDocumentFormat) {
//
//    if (/* params.urlReplacePattern.isEmpty */ false) return
//
////    for ((regex, replacement) ← params.urlReplacePattern.get) {
////      new OwlApiUriRenamer(regex, replacement, ontologyManager, ontologies, format)
////    }
//  }
//
//  private def saveOntology(
//    ontologyManager: OWLOntologyManager,
//    ontology: OWLOntology,
//    format: OWLDocumentFormat): Unit = {
//
//    //
//    // Before saving let's first see if we need to rename stuff
//    //
//    renameURIs(ontologyManager, Set(ontology), format)
//
//    info(s"Saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
//    info(s"In Format: $format")
//    //info(s"To File: ${params.outputFile.get.toString}")
//
//    //ontology.saveOntology(format, params.outputFile.get.outputStream.get)
//
//    ontologyManager.removeOntology(ontology)
//  }
//
//  private def mergeOntology(
//    ontologyManager: OWLOntologyManager,
//    format: OWLDocumentFormat): Unit = {
//
//    //    info(s"Merging and saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
//    //    info(s"In Format: $format")
//    //
//    //    ontology.saveOntology(format, params.output.value.get.outputStream.get)
//    //
//    //    ontologyManager.removeOntology(ontology)
//
//    error("Merging of multiple input ontologies is not supported yet") // TODO: Implement merging of ontologies
//  }
//
//  private def run: Int = {
//
//    val ontologyManager = createOntologyManager
//    val loader = new OwlApiOntologyLoader(ontologyManager, loaderConfiguration, params.baseDirUrls.get, params.abortOnError)
//    val outputFormat = OwlApiOutputFormats.getOutputDocumentFormatWithName(params.outputFormat)
//
//    //
//    // Some ontology formats support prefix names and prefix IRIs. In our
//    // case we loaded the pizza ontology from an rdf/xml format, which
//    // supports prefixes. When we save the ontology in the new format we
//    // will copy the prefixes over so that we have nicely abbreviated IRIs
//    // in the new ontology document
//    //
//    def copyPrefixesToOutputFormat(inputFormat: OWLDocumentFormat): Unit = {
//
//      if (inputFormat.isPrefixOWLDocumentFormat) {
//        if (outputFormat.isPrefixOWLDocumentFormat) {
//          val prefixedOutputFormat = outputFormat.asPrefixOWLDocumentFormat
//          //
//          // For some reason the outputFormat, which is also a PrefixManager, remembers the prefixes and namespaces
//          // of the previous save operation, so we have to clear it here first before we copy the inputFormat's
//          // prefixes into it.
//          //
//          prefixedOutputFormat.clear()
//          prefixedOutputFormat.copyPrefixesFrom(inputFormat.asPrefixOWLDocumentFormat)
//        }
//      }
//
//      val inputOntologyMetadata = inputFormat.getOntologyLoaderMetaData
//      outputFormat.setOntologyLoaderMetaData(inputOntologyMetadata.get)
//    }
//
//    def load(input: PotentialFile): OWLOntology = {
//
//      if (!input.fileExists) error(s"Input file does not exist: $input")
//
//      val ontology = loader.loadOntology(input)
//      val ontologyDocumentIRI = ontologyManager.getOntologyDocumentIRI(ontology)
//
//      info(s"Ontology Document IRI: $ontologyDocumentIRI")
//
//      info(s"Ontology ID: ${ontology.getOntologyID}")
//
//      val inputFormat = ontologyManager.getOntologyFormat(ontology)
//
//      info(s"Input Format: $inputFormat")
//
//      copyPrefixesToOutputFormat(inputFormat)
//
//      ontology
//    }
//
//    if (params.inputFiles.length == 1) {
//      val ontology = load(params.inputFiles.head)
//      saveOntology(ontologyManager, ontology, outputFormat)
//    } else {
//      params.inputFiles.foreach(load)
//      mergeOntology(ontologyManager, outputFormat)
//    }
//
//    0
//  }
//}
//
//object OwlApiSerializer {
//
//  def apply(params: CommandLineParams): Int = new OwlApiSerializer(params).run
//}
