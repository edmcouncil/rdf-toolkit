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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.SortedTurtlePredicateObjectMap;

/**
 * Comparator for TurtlePredicateObjectMap objects. This comparator uses the following ordering rules:
 * <ul>
 *   <li>if both maps reference to the same spot in the memory, they are equal,</li>
 *   <li>null/excluded map comes before non-null/excluded map,</li>
 *   <li>two null/excluded maps are equal,</li>
 *
 * </ul>
 */
public class TurtlePredicateObjectMapComparator implements Comparator<SortedTurtlePredicateObjectMap> {

  private final IRIComparator iriComparator;
  private TurtleObjectListComparator objectListComparator = null;
  private final Class<Value> collectionClass;
  private final boolean inlineBlankNodes;
  private final ComparisonContext comparisonContext;

  public TurtlePredicateObjectMapComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this.collectionClass = collectionClass;
    this.comparisonContext = comparisonContext;
    this.inlineBlankNodes = comparisonContext.getShouldInlineBlankNodes();
    this.iriComparator = new IRIComparator();
  }

  @Override
  public int compare(SortedTurtlePredicateObjectMap map1, SortedTurtlePredicateObjectMap map2) {
    return compare(map1, map2, new ArrayList<>());
  }

  public int compare(SortedTurtlePredicateObjectMap map1, SortedTurtlePredicateObjectMap map2,
      List<Object> excludedList) {
    if (map1 == map2) {
      return 0;
    }

    if ((map1 == null) || excludedList.contains(map1)) {
      if ((map2 == null) || excludedList.contains(map2)) {
        return 0; // two null/excluded maps are equal
      } else {
        return -1; // null/excluded map comes before non-null/excluded map
      }
    } else {
      if ((map2 == null) || excludedList.contains(map2)) {
        return 1; // non-null/excluded map comes before null/excluded map
      } else {
        Iterator<IRI> iter1 = map1.sortedKeys().iterator();
        Iterator<IRI> iter2 = map2.sortedKeys().iterator();
        return compare(map1, iter1, map2, iter2, excludedList);
      }
    }
  }

  private int compare(SortedTurtlePredicateObjectMap map1, Iterator<IRI> iter1, SortedTurtlePredicateObjectMap map2,
      Iterator<IRI> iter2,
      List<Object> excludedList) {
    if (iter1.hasNext()) {
      if (iter2.hasNext()) {
        IRI key1 = iter1.next();
        IRI key2 = iter2.next();
        excludedList.add(map1);
        excludedList.add(map2);
        int cmp = iriComparator.compare(key1, key2, excludedList);
        if (cmp != 0) {
          return cmp;
        } else { // predicate keys are the same, so test object values
          SortedTurtleObjectList values1 = map1.get(key1);
          SortedTurtleObjectList values2 = map2.get(key2);
          var nonBlankValues1 = new SortedTurtleObjectList(collectionClass, comparisonContext);
          var nonBlankValues2 = new SortedTurtleObjectList(collectionClass, comparisonContext);
          // Leave blank nodes out of the value comparison, unless blank nodes are being inlined.
          for (Value value : values1) {
            // including blank nodes is only feasible when inlining blank nodes, as that implicitly promises no blank
            // node loops
            if (inlineBlankNodes || !(value instanceof BNode)) {
              nonBlankValues1.add(value);
            }
          }
          for (Value value : values2) {
            // including blank nodes is only feasible when inlining blank nodes, as that implicitly promises no blank
            // node loops
            if (inlineBlankNodes || !(value instanceof BNode)) {
              nonBlankValues2.add(value);
            }
          }
          if (objectListComparator == null) {
            objectListComparator = new TurtleObjectListComparator(collectionClass, comparisonContext);
          }
          cmp = objectListComparator.compare(nonBlankValues1, nonBlankValues2, excludedList);
          if (cmp != 0) {
            return cmp;
          } else { // values are the same, try the next predicates in the maps
            return compare(map1, iter1, map2, iter2, excludedList);
          }
        }
      } else { // only iter1 has a next value
        return 1; // map1 comes after map2
      }
    } else {
      if (iter2.hasNext()) { // only iter2 has a next value
        return -1; // map1 comes before map2
      } else { // both iterators have no next value
        return 0;
      }
    }
  }
}
