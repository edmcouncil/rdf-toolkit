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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Like java.util.HashMap, but able to return the keys in sorted order.
 */
public class SortedHashMap<K, V> extends HashMap<K, V> {

  private final transient Comparator<K> comparator;
  private transient List<K> sortedKeys = new ArrayList<>();

  public SortedHashMap(Comparator<K> comparator) {
    this.comparator = comparator;
  }

  private void updateSortedKeys() {
    sortedKeys = new LinkedList<>();
    sortedKeys.addAll(keySet());
    sortedKeys.sort(comparator);
  }

  @Override
  public V put(K key, V value) {
    V result = super.put(key, value);
    updateSortedKeys();
    return result;
  }

  @Override
  public boolean remove(Object key, Object value) {
    boolean result = super.remove(key, value);
    updateSortedKeys();
    return result;
  }

  public Iterable<K> sortedKeys() {
    return new LinkedList<>(sortedKeys);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SortedHashMap)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    SortedHashMap<?, ?> that = (SortedHashMap<?, ?>) o;
    return Objects.equals(comparator, that.comparator) && Objects.equals(sortedKeys, that.sortedKeys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), comparator, sortedKeys);
  }
}
