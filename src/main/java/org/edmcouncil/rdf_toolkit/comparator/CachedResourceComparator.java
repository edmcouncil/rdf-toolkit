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

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.model.Pair;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Cached comparator for Sesame Resource objects.
 */
public class CachedResourceComparator implements Comparator<Resource> {

  private final Map<Pair<Resource, Resource>, Integer> cache = new HashMap<>();
  private final ResourceComparator comparator;

  public CachedResourceComparator(Class<Value> collectionClass, ComparisonContext comparisonContext) {
    this.comparator = new ResourceComparator(collectionClass, comparisonContext);
  }

  @Override
  public int compare(Resource resource1, Resource resource2) {
    Pair<Resource, Resource> key = new Pair<>(resource1, resource2);
    if (cache.containsKey(key)) {
      return cache.get(key);
    } else {
      int result = comparator.compare(resource1, resource2);
      cache.put(key, result);
      return result;
    }
  }
}