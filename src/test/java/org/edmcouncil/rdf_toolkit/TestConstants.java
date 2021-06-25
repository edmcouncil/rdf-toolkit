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

package org.edmcouncil.rdf_toolkit;

import static org.edmcouncil.rdf_toolkit.util.Constants.XML_SCHEMA_NS_URI;
import java.util.Set;

public class TestConstants {

  public static final String XS_STRING = XML_SCHEMA_NS_URI + "string";

  /**
   * Exclusion list for tests related to inferring the base IRI of an ontology.
   */
  public static final Set<String> IBI_EXCLUSION_SET = Set.of("food.rdf", "wine.rdf");

  public static final Set<String> JSON_LD_INLINE_BLANK_NODES_EXCLUSION_SET = Set.of(
      "allemang-FunctionalEntities.rdf",
      "turtle-example-14.ttl",
      "turtle-example-25.ttl",
      "turtle-example-26.ttl");

  public static final Set<String> TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET = Set.of(
      "allemang-FunctionalEntities.rdf",
      "turtle-example-14.ttl",
      "turtle-example-25.ttl",
      "turtle-example-26.ttl");

  /**
   * Exclusion list of examples where the ontology IRI is different to the base IRI.
   */
  public static final Set<String> INFERRED_BASE_IRI_EXCLUSION_SET = Set.of("food.rdf", "wine.rdf");

  /**
   * Return exclusion set of examples that can't be represented directly in RDF/XML.
   */
  public static final Set<String> RDFXML_EXCLUSION_SET = Set.of(
      "allemang-test-a.ttl",
      "allemang-test-b.ttl",
      "turtle-example-2.ttl",
      "turtle-example-3.ttl",
      "turtle-example-4.ttl",
      "turtle-example-5.ttl",
      "turtle-example-6.ttl",
      "turtle-example-9.ttl",
      "turtle-example-17.ttl",
      "turtle-example-22.ttl");

  public static final Set<String> INLINE_BLANK_NODES_EXCLUSION_SET = Set.of(
      "allemang-FunctionalEntities.rdf",
      "turtle-example-14.ttl",
      "turtle-example-25.ttl",
      "turtle-example-26.ttl");
}