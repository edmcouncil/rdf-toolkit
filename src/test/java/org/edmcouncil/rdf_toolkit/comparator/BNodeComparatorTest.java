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

package org.edmcouncil.rdf_toolkit.comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Test;
import java.util.List;

class BNodeComparatorTest extends AbstractComparatorTest {

  @Test
  void shouldReturnZeroForTwoBlankNodesThatAreIdentical() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = 0;

    int actualResult = bnodeComparator.compare(
        valueFactory.createBNode("bnode01"),
        valueFactory.createBNode("bnode01"));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnMinusResultWhereFirstBlankNodeIsNullAndSecondIsNonNull() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = -1;

    int actualResult = bnodeComparator.compare(
        null,
        valueFactory.createBNode("bnode02"));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnPlusResultWhereFirstBlankNodeIsNonNullAndSecondIsNull() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = 1;

    int actualResult = bnodeComparator.compare(
        valueFactory.createBNode("bnode01"),
        null);

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnMinusResultWhereFirstBlankNodeIsExcludedAndSecondIsNonNull() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = -1;

    int actualResult = bnodeComparator.compare(
        valueFactory.createBNode("bnode01"),
        valueFactory.createBNode("bnode02"),
        List.of(valueFactory.createBNode("bnode01")));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnMinusResultWhereFirstBlankNodeRepresentsCollectionAndTheSecondOneDoesNot() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var johnsPets = valueFactory.createBNode("johnsPets");
    RDFCollections.asRDF(List.of("Foo", "Bar", "Baz"), johnsPets, model);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = -1;

    int actualResult = bnodeComparator.compare(
        johnsPets,
        valueFactory.createBNode("bnode02"));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnPlusResultWhereSecondBlankNodeRepresentsCollectionAndTheFirstOneDoesNot() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var johnsPets = valueFactory.createBNode("johnsPets");
    RDFCollections.asRDF(List.of("Foo", "Bar", "Baz"), johnsPets, model);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = 1;

    int actualResult = bnodeComparator.compare(
        valueFactory.createBNode("bnode01"),
        johnsPets);

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void shouldReturnEqualResultWhenTwoCollectionsHaveTheSameContent() {
    var valueFactory = SimpleValueFactory.getInstance();
    var model = prepareModel(valueFactory);
    var johnsPets = valueFactory.createBNode("johnsPets");
    RDFCollections.asRDF(List.of("Foo", "Bar", "Baz"), johnsPets, model);
    var bobsPets = valueFactory.createBNode("bobsPets");
    RDFCollections.asRDF(List.of("Foo", "Bar", "Baz"), bobsPets, model);
    var comparisonContext = prepareComparisonContext(model, true);
    var bnodeComparator = new BNodeComparator(Value.class, comparisonContext);

    var expectedResult = 0;

    int actualResult = bnodeComparator.compare(johnsPets, bobsPets);

    assertEquals(expectedResult, actualResult);
  }

  private Model prepareModel(ValueFactory valueFactory) {
    var cityIri = valueFactory.createIRI(IRI_PREFIX + "city");
    var streetIri = valueFactory.createIRI(IRI_PREFIX + "street");
    var johnAddressNode = valueFactory.createBNode("bnode01");
    var bobAddressNode = valueFactory.createBNode("bnode02");
    var modelBuilder = new ModelBuilder();
    return modelBuilder
        .setNamespace("ex", IRI_PREFIX)
        .setNamespace(FOAF.NS)
        .defaultGraph()
        .add("ex:john", RDF.TYPE, FOAF.PERSON)
        .add("ex:john", "ex:address", johnAddressNode)
        .add(johnAddressNode, cityIri, valueFactory.createLiteral("Paris"))
        .add(johnAddressNode, streetIri, valueFactory.createLiteral("Avenue Victor Hugo"))
        .add("ex:bob", RDF.TYPE, FOAF.PERSON)
        .add("ex:bob", "ex:address", bobAddressNode)
        .add(bobAddressNode, cityIri, valueFactory.createLiteral("Berlin"))
        .add(bobAddressNode, streetIri, valueFactory.createLiteral("Unter den Linden"))
        .build();
  }
}