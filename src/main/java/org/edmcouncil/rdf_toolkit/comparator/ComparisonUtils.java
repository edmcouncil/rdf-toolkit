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
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.SortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleSubjectPredicateObjectMap;
import org.edmcouncil.rdf_toolkit.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ComparisonUtils {

  private ComparisonUtils() {
  }

  public static int compareSimpleValue(Literal literal1, Literal literal2) {
    // TODO: support natural ordering of non-string literals
    int cmp = literal1.stringValue().compareTo(literal2.stringValue());
    if (cmp != 0) {
      return cmp;
    } else {
      if (literal1.getLanguage().isPresent()) {
        if (literal2.getLanguage().isPresent()) {
          int cmp2 = literal1.getLanguage().toString().compareTo(literal2.getLanguage().toString());
          if (cmp2 != 0) {
            return cmp2;
          }
        } else { // !literal2.getLanguage().isPresent()
          return -1; // literal1 with language comes before literal2 without language
        }
      } else { // !literal1.getLanguage().isPresent()
        if (literal2.getLanguage().isPresent()) {
          return 1; // literal1 without language comes after literal2 with language
        } // !literal2.getLanguage().isPresent(); neither literal has a language to compare
      }
      if (literal1.getDatatype() != null) {
        if (literal2.getDatatype() != null) {
          return literal1.getDatatype().stringValue().compareTo(literal2.getDatatype().stringValue());
        } else { // literal2.getDatatype() == null
          return -1; // literal1 with data type comes before literal2 without data type
        }
      } else { // literal1.getDatatype() == null
        if (literal2.getDatatype() != null) {
          return 1; // literal1 without data type comes after literal2 with data type
        } // literal2.getDatatype().isPresent() == null; neither literal has a data type to compare
      }
      return 0; // no difference in value, language nor datatype
    }
  }

  public static int compareSimpleValue(Value value1, Value value2) {
    // Use string comparison as the last option.
    return value1.stringValue().compareTo(value2.stringValue());
  }

  /**
   * If the given blank node is an RDF collection, returns the members of the collection.
   *
   * @param bnode           blank node which is an RDF collection
   * @param collectionClass all collection members must be instances of this class
   * @return the members of the RDF collection
   */
  public static List<Value> getCollectionMembers(UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap,
      BNode bnode,
      Class<Value> collectionClass,
      ComparisonContext comparisonContext) {
    // An ArrayList is used here, as collection members should be retained in their original order, not sorted.
    List<Value> members = new ArrayList<>();
    if (isCollection(comparisonContext, bnode, collectionClass)) {
      SortedTurtlePredicateObjectMap poMap = unsortedTripleMap.getSorted(bnode, collectionClass, comparisonContext);
      SortedTurtleObjectList newMembers = poMap.get(Constants.rdfFirst);
      if (newMembers != null) {
        members.addAll(newMembers);
      }
      SortedTurtleObjectList rest = poMap.get(Constants.rdfRest);
      if (rest != null) {
        for (Value nextRest : rest) {
          if (nextRest instanceof BNode) {
            List<Value> newRestMembers = getCollectionMembers(
                unsortedTripleMap,
                (BNode) nextRest,
                collectionClass,
                comparisonContext);
            members.addAll(newRestMembers);
          }
        }
      }
    }
    return members;
  }

  /**
   * Whether the given blank node represents an RDF collection, or not.
   *
   * @param bnode           blank node to test as an RDF collection
   * @param collectionClass all collection members must be instances of this class
   * @return whether the blank node is an RDF collection
   */
  public static boolean isCollection(ComparisonContext comparisonContext,
      BNode bnode,
      Class<Value> collectionClass) {
    var unsortedTripleMap = comparisonContext.getUnsortedTripleMap();
    var poMap = unsortedTripleMap.getSorted(bnode, collectionClass, comparisonContext);
    if (poMap != null) {
      Set<IRI> predicates = poMap.keySet();
      int firstCount = predicates.contains(Constants.rdfFirst) ? 1 : 0;
      int restCount = predicates.contains(Constants.rdfRest) ? 1 : 0;
      int typeCount = predicates.contains(Constants.RDF_TYPE) ? 1 : 0;
      if (predicates.size() == firstCount + restCount + typeCount) {
        SortedTurtleObjectList firstValues = poMap.get(Constants.rdfFirst);
        if (firstValues == null) {
          for (Object poMapKey : poMap.keySet()) {
            firstValues = poMap.get(poMapKey);
          }
        }
        for (Value value : firstValues) {
          // all collection members must match the collection class type
          if (!collectionClass.isInstance(value)) {
            return false;
          }
        }
        if (restCount >= 1) {
          SortedTurtleObjectList rest = poMap.get(Constants.rdfRest);
          if (rest.size() == 1) {
            for (Value value : rest) {
              if (Constants.rdfNil.equals(value)) {
                return true;
              }
              if (value instanceof BNode) {
                return isCollection(comparisonContext, (BNode) value, collectionClass);
              }
            }
          }
        }
      }
    }
    return false;
  }
}