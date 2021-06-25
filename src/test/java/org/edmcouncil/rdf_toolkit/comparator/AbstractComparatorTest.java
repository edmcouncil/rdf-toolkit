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

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleSubjectPredicateObjectMap;

abstract public class AbstractComparatorTest {

  public static final String IRI_PREFIX = "http://example.com/ont1/";

  protected ComparisonContext prepareComparisonContext(Model model, boolean shouldInlineBlankNodes) {
    var tripleMap = new UnsortedTurtleSubjectPredicateObjectMap();
    for (Statement statement : model) {
      Resource subject = statement.getSubject();
      if (!tripleMap.containsKey(subject)) {
        tripleMap.put(subject, new UnsortedTurtlePredicateObjectMap());
      }
      addStatement(tripleMap.get(subject), statement);
    }
    return new ComparisonContext(shouldInlineBlankNodes, tripleMap);
  }

  protected void addStatement(UnsortedTurtlePredicateObjectMap predicateObjectMap, Statement statement) {
    var predicate = statement.getPredicate();
    if (!predicateObjectMap.containsKey(predicate)) {
      predicateObjectMap.put(predicate, new UnsortedTurtleObjectList());
    }
    predicateObjectMap.get(predicate).add(statement.getObject());
  }
}