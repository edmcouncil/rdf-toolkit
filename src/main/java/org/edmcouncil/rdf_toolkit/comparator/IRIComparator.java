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

import org.eclipse.rdf4j.model.IRI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator for Sesame IRI objects.
 */
public class IRIComparator implements Comparator<IRI> {

  @Override
  public int compare(IRI iri1, IRI iri2) {
    return compare(iri1, iri2, new ArrayList<>());
  }

  public int compare(IRI iri1, IRI iri2, List<Object> excludedList) {
    if ((iri1 == null) || excludedList.contains(iri1)) {
      if ((iri2 == null) || excludedList.contains(iri2)) {
        return 0; // two null/excluded IRIs are equal
      } else {
        return -1; // null/excluded IRI comes before non-null/excluded IRI
      }
    } else {
      if ((iri2 == null) || excludedList.contains(iri2)) {
        return 1; // non-null/excluded IRI comes before null/excluded IRI
      } else {
        if (iri1 == iri2) {
          return 0;
        } else {
          return iri1.stringValue().compareTo(iri2.stringValue());
        }
      }
    }
  }
}
