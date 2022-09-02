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

import static org.edmcouncil.rdf_toolkit.comparator.ComparisonUtils.getCollectionMembers;
import static org.edmcouncil.rdf_toolkit.comparator.ComparisonUtils.isCollection;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.model.SortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleSubjectPredicateObjectMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator for BNode objects that follows these rules:
 * <ul>
 *   <li>two resources that reference to the same spot in the memory are equal (i.e. resource1 == resource2),</li>
 *   <li>two null or excluded blank nodes are considered equal,</li>
 *   <li>null/excluded blank nodes comes before non-null/excluded blank nodes,</li>
 *   <li>an RDF collection comes before any other blank node,</li>
 *   <li>if both blank nodes are collections, compare them using <code>TurtleObjectListComparator</code>,</li>
 *   <li>if neither blank node is a collection, compare them using <code>TurtleObjectListComparator</code>.</li>
 * </ul>
 */
public class BNodeComparator implements Comparator<BNode> {

  private final Class<Value> collectionClass;
  private final ComparisonContext comparisonContext;
  private final TurtleObjectListComparator objectListComparator;
  private final TurtlePredicateObjectMapComparator predicateObjectMapComparator;
  private final UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap;
  private final boolean inlineBlankNodes;

  public BNodeComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this.collectionClass = collectionClass;
    this.comparisonContext = comparisonContext;
    this.unsortedTripleMap = comparisonContext.getUnsortedTripleMap();
    this.inlineBlankNodes = comparisonContext.getShouldInlineBlankNodes();
    this.objectListComparator = new TurtleObjectListComparator(collectionClass, comparisonContext);
    this.predicateObjectMapComparator =
        new TurtlePredicateObjectMapComparator(collectionClass, comparisonContext);
  }

  @Override
  public int compare(BNode bnode1, BNode bnode2) {
    return compare(bnode1, bnode2, new ArrayList<>());
  }

  public int compare(BNode bnode1, BNode bnode2, List<Object> excludedList) {
    if (bnode1 == bnode2) {
      return 0;
    }

    if ((bnode1 == null) || excludedList.contains(bnode1) || !unsortedTripleMap.containsKey(bnode1)) {
      if ((bnode2 == null) || excludedList.contains(bnode2) || !unsortedTripleMap.containsKey(bnode2)) {
        return 0; // two null/excluded blank nodes are equal
      } else {
        return -1; // null/excluded blank node comes before non-null/excluded blank node
      }
    } else {
      if ((bnode2 == null) || excludedList.contains(bnode2) || !unsortedTripleMap.containsKey(bnode2)) {
        return 1; // non-null/excluded blank node comes after null/excluded blank node
      }
    }

    if (inlineBlankNodes) {
      return compareBlankNodesWithInlining(bnode1, bnode2, excludedList);
    } else {
      return compareBlankNodesWithoutInlining(bnode1, bnode2, excludedList);
    }
  }

  private int compareBlankNodesWithInlining(BNode bnode1, BNode bnode2, List<Object> excludedList) {
    // We assume here that both blank nodes are non-null/excluded
    assert bnode1 != null && bnode2 != null && !excludedList.contains(bnode1) && !excludedList.contains(bnode2);

    // deal with RDF collection blank nodes separately, when inlining blank nodes
    if (isCollection(comparisonContext, bnode1, collectionClass)) {
      if (isCollection(comparisonContext, bnode2, collectionClass)) {
        List<Value> values1 = getCollectionMembers(unsortedTripleMap, bnode1, collectionClass, comparisonContext);
        List<Value> values2 = getCollectionMembers(unsortedTripleMap, bnode2, collectionClass, comparisonContext);

        return objectListComparator.compare(values1, values2, excludedList);
      } else {
        return -1; // an RDF collection comes before any other blank node
      }
    } else {
      if (isCollection(comparisonContext, bnode2, collectionClass)) {
        return 1; // an RDF collection comes before any other blank node
      } else { // neither blank node is an RDF collection
        SortedTurtlePredicateObjectMap map1 = unsortedTripleMap.getSorted(bnode1, collectionClass, comparisonContext);
        SortedTurtlePredicateObjectMap map2 = unsortedTripleMap.getSorted(bnode2, collectionClass, comparisonContext);

        excludedList.add(bnode1);
        excludedList.add(bnode2);

        return predicateObjectMapComparator.compare(map1, map2, excludedList);
      }
    }
  }

  private int compareBlankNodesWithoutInlining(BNode bnode1, BNode bnode2, List<Object> excludedList) {
    // We assume here that both blank nodes are non-null/excluded
    if (bnode1 == null || bnode2 == null || excludedList.contains(bnode1) || excludedList.contains(bnode2)) {
      throw new IllegalStateException("value1 and value2 should not be null or in the excluded list.");
    }

    // Deal with RDF collection blank nodes separately, when inlining blank nodes
    if (isCollection(comparisonContext, bnode1, collectionClass)) {
      if (isCollection(comparisonContext, bnode2, collectionClass)) {
        List<Value> values1 = getCollectionMembers(unsortedTripleMap, bnode1, collectionClass, comparisonContext);
        List<Value> values2 = getCollectionMembers(unsortedTripleMap, bnode2, collectionClass, comparisonContext);

        int cmp = objectListComparator.compare(values1, values2, excludedList);
        if (cmp != 0) { // cmp = 0 value is only reliable when inlining blank nodes
          return cmp;
        } else { // if all else fails, do a string comparison
          return bnode1.stringValue().compareTo(bnode2.stringValue()); // TODO
        }
      } else {
        return -1; // an RDF collection comes before any other blank node
      }
    } else {
      if (isCollection(comparisonContext, bnode2, collectionClass)) {
        return 1; // an RDF collection comes before any other blank node
      } else { // neither blank node is an RDF collection
        SortedTurtlePredicateObjectMap map1 = unsortedTripleMap.getSorted(bnode1, collectionClass, comparisonContext);
        SortedTurtlePredicateObjectMap map2 = unsortedTripleMap.getSorted(bnode2, collectionClass, comparisonContext);

        excludedList.add(bnode1);
        excludedList.add(bnode2);

        int cmp = predicateObjectMapComparator.compare(map1, map2, excludedList);
        if (cmp != 0) { // cmp = 0 value is only reliable when inlining blank nodes
          return cmp;
        } else { // if all else fails, do a string comparison
          return bnode1.stringValue().compareTo(bnode2.stringValue());
        }
      }
    }
  }
}