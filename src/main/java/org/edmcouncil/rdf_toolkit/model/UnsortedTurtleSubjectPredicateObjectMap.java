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

package org.edmcouncil.rdf_toolkit.model;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.comparator.ComparisonContext;
import java.util.HashMap;

/**
 * An unsorted map from subject resources to predicate/object pairs.
 */
public class UnsortedTurtleSubjectPredicateObjectMap extends HashMap<Resource, UnsortedTurtlePredicateObjectMap> {

  public SortedTurtlePredicateObjectMap getSorted(Resource subject,
                                                  Class<Value> collectionClass,
                                                  ComparisonContext comparisonContext) {
    if (containsKey(subject)) {
      return get(subject).toSorted(collectionClass, comparisonContext);
    } else {
      return null;
    }
  }

  public SortedTurtleSubjectPredicateObjectMap toSorted(Class<Value> collectionClass,
                                                        ComparisonContext comparisonContext) {
    var sortedSPOMap = new SortedTurtleSubjectPredicateObjectMap(collectionClass, comparisonContext);
    for (Resource subject : keySet()) {
      sortedSPOMap.put(subject, getSorted(subject, collectionClass, comparisonContext));
    }
    return sortedSPOMap;
  }

  public int fullSize() {
    var result = 0;
    for (UnsortedTurtlePredicateObjectMap value : values()) {
      result += value.fullSize();
    }
    return result;
  }

  public boolean checkValid() {
    for (Resource subject : keySet()) {
      if ((get(subject) == null) || !get(subject).checkValid()) {
        return false;
      }
    }
    return true;
  }
}