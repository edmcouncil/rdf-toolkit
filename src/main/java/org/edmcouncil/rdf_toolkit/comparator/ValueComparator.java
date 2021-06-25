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

import static org.edmcouncil.rdf_toolkit.comparator.ComparisonUtils.compareSimpleValue;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator for Sesame Value objects.
 *
 * This comparator uses the following rules for ordering <code>Value</code> items:
 * <ul>
 *   <li>two values that reference to the same spot in the memory (v1 == v2) are considered equal,</li>
 *   <li>null/excluded value comes before non-null/excluded value,</li>
 *   <li>two null/excluded values are equal,</li>
 *   <li>if both values are blank nodes, {@link BNodeComparator} is used,</li>
 *   <li>blank nodes comes after other values,</li>
 *   <li>if both values are literal, {@link ComparisonUtils#compareSimpleValue(Literal, Literal)},</li>
 *   <li>in the other case, {@link ComparisonUtils#compareSimpleValue(Value, Value)}.</li>
 * </ul>
 */
public class ValueComparator implements Comparator<Value> {

  private final Class<Value> collectionClass;
  private final ComparisonContext comparisonContext;
  private BNodeComparator blankNodeComparator;

  public ValueComparator(ComparisonContext comparisonContext) {
    this(Value.class, comparisonContext);
  }

  public ValueComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this.collectionClass = collectionClass;
    this.comparisonContext = comparisonContext;
  }

  @Override
  public int compare(Value value1, Value value2) {
    return compare(value1, value2, new ArrayList<>());
  }

  public int compare(Value value1, Value value2, List<Object> excludedList) {
    if (value1 == value2) {
      return 0;
    }

    if ((value1 == null) || excludedList.contains(value1)) {
      if ((value2 == null) || excludedList.contains(value2)) {
        return 0; // two null/excluded values are equal
      } else {
        return -1; // null/excluded value comes before non-null/excluded value
      }
    } else {
      if ((value2 == null) || excludedList.contains(value2)) {
        return 1; // non-null/excluded value comes before null/excluded value
      }
    }

    return compareExistingValues(value1, value2, excludedList);
  }

  public int compareExistingValues(Value value1, Value value2, List<Object> excludedList) {
    // We assume here that both blank nodes are non-null/excluded
    if (value1 == null || value2 == null || excludedList.contains(value1) || excludedList.contains(value2)) {
      throw new IllegalStateException("value1 and value2 should not be null or in the excluded list.");
    }

    // Order blank nodes so that they come after other values.
    if (value1 instanceof BNode) {
      if (value2 instanceof BNode) {
        return compareTwoBlankNodes((BNode) value1, (BNode) value2, excludedList);
      } else {
        return 1; // blank node value1 comes after value2.
      }
    } else {
      if (value2 instanceof BNode) {
        return -1; // value1 comes before blank node value2.
      } else { // compare non-blank-node values.
        if ((value1 instanceof Literal) && (value2 instanceof Literal)) {
          return compareSimpleValue((Literal) value1, (Literal) value2);
        } else {
          return compareSimpleValue(value1, value2);
        }
      }
    }
  }

  private int compareTwoBlankNodes(BNode value1, BNode value2, List<Object> excludedList) {
    if (this.blankNodeComparator == null) {
      this.blankNodeComparator = new BNodeComparator(collectionClass, comparisonContext);
    }
    int cmp = blankNodeComparator.compare(value1, value2, excludedList);
    if (cmp != 0) {
      return cmp;
    } else {
      // to make sure that the sorted blank node list doesn't exclude a blank node just because it has the
      // same content as another
      return value1.stringValue().compareTo(value2.stringValue());
    }
  }
}