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
package org.edmcouncil.rdf_toolkit.util;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@SuppressWarnings("unused")
public class Constants {

  // Names of options identifiers
  public static final String BASE_IRI = "baseIri";
  public static final String INDENT = "indent";
  public static final String SHORT_URI_PREF = "shortUriPref";
  public static final String USE_DTD_SUBSET = "useDtdSubset";
  public static final String INLINE_BLANK_NODES = "inlineBlankNodes";
  public static final String LEADING_COMMENTS = "leadingComments";
  public static final String TRAILING_COMMENTS = "trailingComments";
  public static final String STRING_DATA_TYPE_OPTION = "stringDataTypeOption";
  public static final String OVERRIDE_STRING_LANGUAGE = "overrideStringLanguage";
  public static final String LINE_END = "lineEnd";
  public static final String OMIT_XMLNS_NAMESPACE = "omitXmlnsNamespace";
  public static final String SUPPRESS_NAMED_INDIVIDUALS = "suppressNamedIndividuals";
  public static final String USE_DEFAULT_LANGUAGE = "useDefaultLanguage";

  /**
   * Factory for generating literal values.
   */
  public static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  /**
   * XML namespace URI.
   */
  public static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";

  /**
   * XML Schema namespace URI.
   */
  public static final String XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema#";

  /**
   * xs:string URL
   */
  public static final IRI xsString = VALUE_FACTORY.createIRI(XML_SCHEMA_NS_URI + "string");

  /**
   * RDF namespace URI.
   */
  public static final String RDF_NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /**
   * rdf:langString URL
   */
  public static final IRI rdfLangString = VALUE_FACTORY.createIRI(RDF_NS_URI + "langString");

  /**
   * rdf:resource URL
   */
  public static final IRI rdfResource = VALUE_FACTORY.createIRI(RDF_NS_URI + "resource");

  /**
   * rdf:about URL
   */
  public static final IRI rdfAbout = VALUE_FACTORY.createIRI(RDF_NS_URI + "about");

  /**
   * rdf:parseType URL
   */
  public static final IRI rdfParseType = VALUE_FACTORY.createIRI(RDF_NS_URI + "parseType");

  /**
   * rdf:nil URL
   */
  public static final IRI rdfNil = VALUE_FACTORY.createIRI(RDF_NS_URI + "nil");

  /**
   * rdf:rest URL
   */
  public static final IRI rdfRest = VALUE_FACTORY.createIRI(RDF_NS_URI + "rest");

  /**
   * rdf:first URL
   */
  public static final IRI rdfFirst = VALUE_FACTORY.createIRI(RDF_NS_URI + "first");

  /**
   * rdf:Description URL
   */
  public static final IRI rdfDescription = VALUE_FACTORY.createIRI(RDF_NS_URI + "Description");

  /**
   * rdf:type ('a') URL
   */
  public static final IRI RDF_TYPE = VALUE_FACTORY.createIRI(RDF_NS_URI + "type");

  /**
   * RDF Schema (RDFS) namespace URI.
   */
  public static final String RDFS_NS_URI = "http://www.w3.org/2000/01/rdf-schema#";

  /**
   * rdfs:subPropertyOf URL
   */
  public static final IRI RDFS_SUB_PROPERTY_OF = VALUE_FACTORY.createIRI(RDFS_NS_URI + "subPropertyOf");

  /**
   * rdfs:subClassOf URL
   */
  public static final IRI RDFS_SUB_CLASS_OF = VALUE_FACTORY.createIRI(RDFS_NS_URI + "subClassOf");

  /**
   * rdfs:comment URL
   */
  public static final IRI RDFS_COMMENT = VALUE_FACTORY.createIRI(RDFS_NS_URI + "comment");

  /**
   * rdfs:label URL
   */
  public static final IRI RDFS_LABEL = VALUE_FACTORY.createIRI(RDFS_NS_URI + "label");

  /**
   * OWL namespace URI.
   */
  public static final String OWL_NS_URI = "http://www.w3.org/2002/07/owl#";

  /**
   * owl:onClass URL
   */
  public static final IRI OWL_ON_CLASS = VALUE_FACTORY.createIRI(OWL_NS_URI + "onClass");

  /**
   * owl:onProperty URL
   */
  public static final IRI OWL_ON_PROPERTY = VALUE_FACTORY.createIRI(OWL_NS_URI + "onProperty");

  /**
   * owl:ObjectProperty URL
   */
  public static final IRI owlObjectProperty = VALUE_FACTORY.createIRI(OWL_NS_URI + "ObjectProperty");

  /**
   * owl:DatatypeProperty URL
   */
  public static final IRI owlDatatypeProperty = VALUE_FACTORY.createIRI(OWL_NS_URI + "DatatypeProperty");

  /**
   * owl:Thing URL
   */
  public static final IRI owlThing = VALUE_FACTORY.createIRI(OWL_NS_URI + "Thing");

  /**
   * owl:NamedIndividual URL
   */
  public static final IRI owlNamedIndividual = VALUE_FACTORY.createIRI(OWL_NS_URI + "NamedIndividual");

  /**
   * owl:sameAs URL
   */
  public static final IRI OWL_SAME_AS = VALUE_FACTORY.createIRI(OWL_NS_URI + "sameAs");

  /**
   * owl:imports URL
   */
  public static final IRI OWL_IMPORTS = VALUE_FACTORY.createIRI(OWL_NS_URI + "imports");

  /**
   * owl:Ontology URL
   */
  public static final IRI owlOntology = VALUE_FACTORY.createIRI(OWL_NS_URI + "Ontology");
}
