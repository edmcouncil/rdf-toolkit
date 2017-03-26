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
//package org.edmcouncil.rdf_toolkit.rdf_serializer
//
//import org.semanticweb.owlapi.io.ToStringRenderer
//import org.semanticweb.owlapi.model.{OWLDocumentFormat, OWLOntology}
//import org.semanticweb.owlapi.util.SimpleRenderer
//
///**
// * Initialize the default ToStringRenderer with all the prefixes that we found in the input Ontology, including
// * the imported Ontologies.
// *
// * This only affects the log output by the way.
// */
//object OwlApiSetupRenderer {
//
//  def apply(format: OWLDocumentFormat, ontology: OWLOntology): Unit = {
//
//    val ontologyManager = ontology.getOWLOntologyManager
//
//    //
//    // When the given format supports prefixes then use the prefix manager associated with that format
//    //
//    val prefixOntologyFormat = if (format.isPrefixOWLOntologyFormat) Some(format.asPrefixOWLOntologyFormat) else None
//
//    val objectRenderer = new SimpleRenderer
//
//    if (prefixOntologyFormat.isDefined)
//      objectRenderer.setPrefixesFromOntologyFormat(ontology, ontologyManager, true)
//
//    ToStringRenderer.getInstance().setRenderer(objectRenderer)
//  }
//}
