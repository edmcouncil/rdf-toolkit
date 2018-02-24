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
package javax.util;

import java.util.*;

/** Like java.util.HashMap, but able to return the keys in sorted order. */
public class SortedHashMap<K,V> extends HashMap<K,V> {
    private Comparator<K> comparator = null;
    private List<K> sortedKeys = new ArrayList<K>();

    public SortedHashMap(Comparator<K> comparator) {
        this.comparator = comparator;
    }

    private void updateSortedKeys() {
        sortedKeys = new LinkedList<K>();
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
        LinkedList<K> cloneKeys = new LinkedList<K>();
        cloneKeys.addAll(sortedKeys);
        return cloneKeys;
    }
}
