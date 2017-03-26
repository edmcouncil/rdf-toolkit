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
//package org.edmcouncil.rdf_serializer
//
//import grizzled.slf4j.Logging
////import org.edmcouncil.main.CommandLineParams
//import org.edmcouncil.util.{GitRepository, PotentialFile}
//import org.openrdf.rio.RDFWriterRegistry
//import org.semanticweb.owlapi.apibinding.OWLManager
//import org.semanticweb.owlapi.model._
//import org.semanticweb.owlapi.rdf.rdfxml.renderer.XMLWriterPreferences
//
///**
// * Serialize a given RDF or OWL file with the OWLAPI
// */
//class OwlApiSerializer(private val params: CommandLineParams) extends Logging with OnErrorAborter {
//
//  def abortOnError = params.abortOnError
//
//  //
//  // Ensure that the DOCTYPE rdf:RDF ENTITY section is generated
//  //
//  XMLWriterPreferences.getInstance().setUseNamespaceEntities(true)
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
//  private lazy val loaderConfiguration = new OWLOntologyLoaderConfiguration()
//    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT) // Cause the listener to be called
//    .setLoadAnnotationAxioms(true)
//    .setTreatDublinCoreAsBuiltIn(true)
//    .setFollowRedirects(false)
//
//  lazy val writerFormatRegistry = RDFWriterRegistry.getInstance()
//
//  /**
//   * Perform the IRI rename if the urlReplacePattern has been specified
//   */
//  private def renameURIs(
//    ontologyManager: OWLOntologyManager,
//    ontologies: Set[OWLOntology],
//    format: OWLDocumentFormat) {
//    if (!params.urlReplacePattern.hasValue) return
//
//    for ((regex, replacement) ← params.urlReplacePattern.value) {
//      new OwlApiUriRenamer(regex, replacement, ontologyManager, ontologies, format)
//    }
//  }
//
//  /**
//   * Prepare for publishing the Ontology.
//   */
//  private def prepareForPublication(
//    ontologyManager: OWLOntologyManager,
//    ontologies: Set[OWLOntology],
//    format: OWLDocumentFormat,
//    gitRepo: Option[GitRepository]
//  ) {
//
//    if (!params.performPublish) return
//
//    for (ontology ← ontologies) {
//      new OwlApiOntologyPublisher(ontology, format, gitRepo)
//    }
//  }
//
//  private def saveOntology(
//    ontologyManager: OWLOntologyManager,
//    ontology: OWLOntology,
//    format: OWLDocumentFormat,
//    gitRepo: Option[GitRepository]
//  ): Unit = {
//
//    //
//    // Before saving let's first see if we need to rename stuff
//    //
//    renameURIs(ontologyManager, Set(ontology), format)
//
//    //
//    // Before saving let's first see if we need to publish stuff
//    //
//    prepareForPublication(ontologyManager, Set(ontology), format, gitRepo)
//
//    info(s"Saving ontology: ${ontology.getOntologyID.getOntologyIRI.get}")
//    if (ontology.getOntologyID.getVersionIRI.isPresent) {
//      info(s"with versionIRI: ${ontology.getOntologyID.getVersionIRI.get}")
//    }
//    info(s"In Format: $format")
//    info(s"To File: ${params.outputFile.value.get.toString}")
//
//    ontology.saveOntology(format, params.outputFile.value.get.outputStream.get)
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
//    val loader = new OwlApiOntologyLoader(ontologyManager, loaderConfiguration, params.baseDirUrls.value, params.abortOnError)
//    val outputFormat = OwlApiOutputFormats.getOutputDocumentFormatWithName(params.outputFormat.value)
//
//    //
//    // Some ontology formats support prefix names and prefix IRIs. In our
//    // case we loaded the pizza ontology from an rdf/xml format, which
//    // supports prefixes. When we save the ontology in the new format we
//    // will copy the prefixes over so that we have nicely abbreviated IRIs
//    // in the new ontology document
//    //
//    def copyPrefixesToOutputFormat(inputFormat: OWLDocumentFormat, ontology: OWLOntology): Unit = {
//
//      if (inputFormat.isPrefixOWLOntologyFormat) {
//        if (outputFormat.isPrefixOWLOntologyFormat) {
//          val prefixedOutputFormat = outputFormat.asPrefixOWLOntologyFormat()
//          //
//          // For some reason the outputFormat, which is also a PrefixManager, remembers the prefixes and namespaces
//          // of the previous save operation, so we have to clear it here first before we copy the inputFormat's
//          // prefixes into it.
//          //
//          prefixedOutputFormat.clear()
//          prefixedOutputFormat.copyPrefixesFrom(inputFormat.asPrefixOWLOntologyFormat())
//          OwlApiSetupRenderer(prefixedOutputFormat, ontology)
//        } else {
//          OwlApiSetupRenderer(inputFormat, ontology)
//        }
//      }
//
//      val inputOntologyMetadata = inputFormat.getOntologyLoaderMetaData
//      outputFormat.setOntologyLoaderMetaData(inputOntologyMetadata)
//    }
//
//    def load(input: PotentialFile): (OWLOntology, Option[GitRepository]) = {
//
//      if (!input.fileExists) error(s"Input file does not exist: $input")
//
//      val gitRepo = input.directory.flatMap(GitRepository(_))
//
//      info(s"git repo is $gitRepo")
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
//      copyPrefixesToOutputFormat(inputFormat, ontology)
//
//      (ontology, gitRepo)
//    }
//
//    if (params.inputFiles.value.length == 1) {
//      val (ontology, gitRepo) = load(params.inputFiles.value.head)
//      saveOntology(ontologyManager, ontology, outputFormat, gitRepo)
//    } else {
//      params.inputFiles.value.foreach(load)
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
