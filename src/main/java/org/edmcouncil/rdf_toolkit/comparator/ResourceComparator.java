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

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator for Resource objects that follows these rules:
 * <ul>
 *   <li>two resources that reference to the same spot in the memory are equal (i.e. resource1 == resource2),</li>
 *   <li>two null or excluded resources are considered equal,</li>
 *   <li>null/excluded resources comes before non-null/excluded resources,</li>
 *   <li>blank nodes come after other types of resources,</li>
 *   <li>if both resources are blank nodes, compare them using <code>BNodeComparator</code>,</li>
 *   <li>if both resources are IRIs, compare them using <code>IRIComparator</code>.</li>
 * </ul>
 */
public class ResourceComparator implements Comparator<Resource> {

  private final BNodeComparator blankNodeComparator;
  private final IRIComparator iriComparator = new IRIComparator();
  private Class<Value> collectionClass = Value.class;

  public ResourceComparator(ComparisonContext comparisonContext) {
    this.blankNodeComparator = new BNodeComparator(collectionClass, comparisonContext);
  }

  public ResourceComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this(comparisonContext);
    this.collectionClass = collectionClass;
  }

  @Override
  public int compare(Resource resource1, Resource resource2) {
    return compare(resource1, resource2, new ArrayList<>());
  }

  private int compare(Resource resource1, Resource resource2, List<Object> excludedList) {
    if (resource1 == resource2) {
      return 0;
    }

    if ((resource1 == null) || excludedList.contains(resource1)) {
      if ((resource2 == null) || excludedList.contains(resource2)) {
        return 0; // two null/excluded resources are equal
      } else {
        return -1; // null/excluded resource comes before non-null/excluded resource
      }
    } else {
      if ((resource2 == null) || excludedList.contains(resource2)) {
        return 1; // non-null/excluded resource comes before null/excluded resource
      } else {
        // Order blank nodes so that they come after other values.
        if (resource1 instanceof BNode) {
          if (resource2 instanceof BNode) {
            int cmp = blankNodeComparator.compare((BNode) resource1, (BNode) resource2, excludedList);
            if (cmp != 0) {
              return cmp;
            } else {
              // to make sure that the sorted blank node list doesn't exclude a blank node just because it has the
              // same content as another
              return resource1.stringValue().compareTo(resource2.stringValue());
            }
          } else {
            return 1; // blank node resource1 comes after resource2 (which is not a blank node).
          }
        } else {
          if (resource2 instanceof BNode) {
            return -1; // resource1 (which is not a blank node) comes before blank node resource2.
          } else { // compare non-blank-node resources.
            if ((resource1 instanceof IRI) && (resource2 instanceof IRI)) { // compare IRIs
              return iriComparator.compare((IRI) resource1, (IRI) resource2, excludedList);
            } else {
              return resource1.stringValue().compareTo(resource2.stringValue());
            }
          }
        }
      }
    }
  }
}