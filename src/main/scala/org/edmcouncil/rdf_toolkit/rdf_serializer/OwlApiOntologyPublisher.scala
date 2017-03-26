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
package org.edmcouncil.rdf_serializer

import grizzled.slf4j.Logging
import org.edmcouncil.fibo.EdmCouncilVersionIRI
import org.edmcouncil.util.GitRepository
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

import scala.collection.JavaConverters._

/**
 * Do all the hard work of preparing an Ontology for publishing
 */
class OwlApiOntologyPublisher(ontology: OWLOntology, format: OWLDocumentFormat, gitRepo: Option[GitRepository]) extends Logging {

  import org.edmcouncil._

  val ontologyManager = ontology.getOWLOntologyManager

  val ontologyID = ontology.getOntologyID
  val ontologyIRI = Option(ontologyID.getOntologyIRI.get)
  val oldVersionIRI = Option(ontologyID.getVersionIRI.get)
  val defaultDocumentIRI = Option(ontologyID.getDefaultDocumentIRI.get)

  val addLicense = true // make configurable some day

  def checkOntology(): Unit = {
    if (ontologyIRI.isDefined) {
      info(s"Publishing Ontology $ontologyIRI")
    } else {
      error(s"Ontology can not be published because it has no ontology IRI")
      assert(assertion = false)
    }

    if (oldVersionIRI.isDefined) {
      warn(s"The source has version IRI $oldVersionIRI, a git based source is not supposed to have a versionIRI")
    }
    if (defaultDocumentIRI.isDefined) {
      info(s"The source has default document IRI $defaultDocumentIRI")
    }
  }

  def newVersionIRI = {

    val shortSha = gitRepo.map(_.shortSha).getOrElse("NOTGITBASED")
    val ontIRI = ontologyIRI.get

    if (ontIRI.getNamespace.startsWith(edmcFiboNamespaceIRI.toString)) {
      //
      // Ok, so we're dealing with a FIBO ontology, well then we have a format for the versionIRI:
      //
      // https://spec.edmcouncil.org/<family>/<tag|branch>/<domain>/<module1..n>/<ontology>
      //
      // See https://wiki.edmcouncil.org/x/FgAM
      //
      EdmCouncilVersionIRI(ontIRI, shortSha).iri
    } else {
      //
      // TODO: Make up a better version IRI for non-fibo ontologies
      //
      IRI.create(ontIRI.toString, s"$shortSha/")
    }
  }

  def modifyOntology(): Unit = {

    OwlApiOntologyVersionIRIChanger(ontology, newVersionIRI)

    OwlApiAnnotationAdder(ontology, OWLRDFVocabulary.RDFS_COMMENT.getIRI, "**** THIS IS A COMMENT *****")

    val annotationProperty = OwlApiAnnotationAdder.df.getRDFSSeeAlso

    OwlApiAnnotationAdder(ontology, annotationProperty, IRI.create(edmcFiboNamespaceIRI.toString, "test"))

    for (annotation <- ontology.getAnnotations.asScala) {
      info(s"Annotation: $annotation")
    }
  }

  //OwlApiSetupRenderer(format, ontology)
  checkOntology()
  modifyOntology()
}

