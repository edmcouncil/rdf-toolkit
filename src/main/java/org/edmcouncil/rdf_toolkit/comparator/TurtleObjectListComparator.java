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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleObjectList;

/**
 * Comparator for TurtleObjectList objects.
 */
public class TurtleObjectListComparator implements Comparator<SortedTurtleObjectList> {

  private final ValueComparator valueComparator;

  public TurtleObjectListComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this.valueComparator = new ValueComparator(collectionClass, comparisonContext);
  }

  @Override
  public int compare(SortedTurtleObjectList list1, SortedTurtleObjectList list2) {
    return compare(list1, list2, new ArrayList<>());
  }

  public int compare(Collection<Value> list1, Collection<Value> list2, List<Object> excludedList) {
    if ((list1 == null) || excludedList.contains(list1)) {
      if ((list2 == null) || excludedList.contains(list2)) {
        return 0; // two null/excluded lists are equal
      } else {
        return -1; // null/excluded list comes before non-null/excluded list
      }
    } else {
      if ((list2 == null) || excludedList.contains(list2)) {
        return 1; // non-null/excluded list comes before null/excluded list
      } else {
        if (list1 == list2) {
          return 0;
        } else {
          Iterator<Value> iter1 = list1.iterator();
          Iterator<Value> iter2 = list2.iterator();
          return compare(list1, iter1, list2, iter2, excludedList);
        }
      }
    }
  }

  private int compare(Collection<Value> list1, Iterator<Value> iter1,
                      Collection<Value> list2, Iterator<Value> iter2,
                      List<Object> excludedList) {
    if (iter1.hasNext()) {
      if (iter2.hasNext()) {
        var value1 = iter1.next();
        var value2 = iter2.next();

        excludedList.add(list1);
        excludedList.add(list2);
        int cmp = valueComparator.compare(value1, value2, excludedList);
        if (cmp != 0) {
          return cmp;
        } else { // values are the same, try the next values in the lists
          return compare(list1, iter1, list2, iter2, excludedList);
        }
      } else { // only iter1 has a next value
        return 1; // list1 comes after list2
      }
    } else {
      if (iter2.hasNext()) { // only iter2 has a next value
        return -1; // list1 comes before list2
      } else { // both iterators have no next value
        return 0;
      }
    }
  }
}