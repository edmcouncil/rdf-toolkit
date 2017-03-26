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
package org.edmcouncil.rdf_toolkit.command

import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner._
import grizzled.slf4j.Logging
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory

/**
 * Run the reasoner over the Config File (and its imported ontologies)
 *
 * Current reasoner is the built-in (into the OWLAPI) "Structural Reasoner"
 */
object Reasoner {

  private val progressMonitor = new ProgressMonitor()
  private val config = new SimpleConfiguration(progressMonitor)

  private val structuralReasonerFactory = new StructuralReasonerFactory()

  def reasoner(ontology: OWLOntology, factory: OWLReasonerFactory) = {
    val reasoner = factory.createReasoner(ontology, config)
    //
    // Optionally let the reasoner compute the most relevant inferences in advance
    //
    reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS)
    reasoner
  }

  def apply(ontology: OWLOntology) = reasoner(ontology, structuralReasonerFactory)
}

private class ProgressMonitor extends ReasonerProgressMonitor with Logging {

  info(s"-----------------> LoggingReasonerProgressMonitor <-------")

  override def reasonerTaskStarted(taskName: String) = {
    info("Reasoner Task Started: " + taskName)
  }

  override def reasonerTaskStopped() = {
    info("Reasoner Task Stopped")
  }

  //  override def reasonerTaskProgressChanged(value: Int, max: Int) = {
  //    info("Reasoner Task made progress: " + value + "/" + max)
  //  }

  //  override def reasonerTaskBusy() = {
  //    info("Reasoner Task is busy")
  //  }
}
