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
package org.edmcouncil.rdf_toolkit;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Equivalent to Sesame's built-in RDF writer, but the triples are sorted into a consistent order.
 * In order to do the sorting, it must be possible to load all of the RDF statements into memory.
 * NOTE: comments are suppressed, as there isn't a clear way to sort them along with triples.
 */
public abstract class SesameSortedRDFWriter extends AbstractRDFWriter {
    // TODO: add common methods for "eol and increase indent", "eol and decrease indent" and "eol with same indent" and refactor using these

//    private static final Logger logger = LoggerFactory.getLogger(SesameSortedRDFWriter.class);

    /** Whether the character is a "name character", as defined in the XML namespaces spec.  Characters above Unicode FFFF are included. */
    public static boolean isNameChar(char ch) {
        if ('-' == ch) return true;
        if ('.' == ch) return true;
        if ('_' == ch) return true;
        if ('\\' == ch) return true;
        if (':' == ch) return true;
        if (('0' <= ch) && (ch <= '9')) return true;
        if (('A' <= ch) && (ch <= 'Z')) return true;
        if (('a' <= ch) && (ch <= 'z')) return true;
        if (('\u00C0' <= ch) && (ch <= '\u00D6')) return true;
        if (('\u00D8' <= ch) && (ch <= '\u00F6')) return true;
        if (('\u00F8' <= ch) && (ch <= '\u02FF')) return true;
        if (('\u0370' <= ch) && (ch <= '\u037D')) return true;
        if (('\u037F' <= ch) && (ch <= '\u1FFF')) return true;
        if (('\u200C' <= ch) && (ch <= '\u200D')) return true;
        if (('\u2070' <= ch) && (ch <= '\u218F')) return true;
        if (('\u2C00' <= ch) && (ch <= '\u2FEF')) return true;
        if (('\u3001' <= ch) && (ch <= '\uD7FF')) return true;
        if (('\uF900' <= ch) && (ch <= '\uFDCF')) return true;
        if (('\uFDF0' <= ch) && (ch <= '\uFFFD')) return true;
        if ('\u00B7' == ch) return true;
        if (('\u0300' <= ch) && (ch <= '\u036F')) return true;
        if (('\u203F' <= ch) && (ch <= '\u2040')) return true;
        return false;
    }

    public static boolean isMultilineString(String str) {
        if (str == null) { return false; }
        for (int idx = 0; idx < str.length(); idx++) {
            switch (str.charAt(idx)) {
                case 0xA: return true;
                case 0xB: return true;
                case 0xC: return true;
                case 0xD: return true;
            }
        }
        return false;
    }

    /**
     * Whether the string is valid as the local part of a prefixed name, as defined in the RDF 1.1 Turtle spec.
     * Doesn't check that backslash escape sequences in the name are correctly formed.
     */
    public static boolean isPrefixedNameLocalPart(String str) {
        if (str == null) return false;
        if (str.length() < 1) return false;
        if ((':' != str.charAt(0)) && !isNameChar(str.charAt(0))) return false; // cannot start with a colon
        for (int idx = 2; idx < str.length(); idx++) {
            if (!isNameChar(str.charAt(idx))) return false;
        }
        return true;
    }

    public enum ShortIriPreferences {
        prefix("prefix"),
        base_iri("base-iri");

        private static final ShortIriPreferences defaultEnum = prefix;

        private String optionValue = null;

        ShortIriPreferences(String optionValue) {
            this.optionValue = optionValue;
        }

        public String getOptionValue() { return optionValue; }

        public static ShortIriPreferences getByOptionValue(String optionValue) {
            if (optionValue == null) { return null; }
            for (ShortIriPreferences sup : ShortIriPreferences.values()) {
                if (optionValue.equals(sup.optionValue)) {
                    return sup;
                }
            }
            return null;
        }

        public static String summarise() {
            ArrayList<String> result = new ArrayList<String>();
            for (ShortIriPreferences sup : ShortIriPreferences.values()) {
                String value = sup.optionValue;
                if (defaultEnum.equals(sup)) {
                    value += " [default]";
                }
                result.add(value);
            }
            return String.join(", ", result);
        }
    }
    
    /** Factory for generating literal values. */
    private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

    /** XML namespace URI. */
    public static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";

    /** XML Schema namespace URI. */
    public static final String XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema#";

    /** RDF namespace URI. */
    public static final String RDF_NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** RDF Schema (RDFS) namespace URI. */
    public static final String RDFS_NS_URI = "http://www.w3.org/2000/01/rdf-schema#";

    /** OWL namespace URI. */
    public static final String OWL_NS_URI = "http://www.w3.org/2002/07/owl#";

    /** rdf:type ('a') URL */
    protected static final IRI rdfType = valueFactory.createIRI(RDF_NS_URI + "type");

    /** rdf:Description URL */
    protected static final IRI rdfDescription = valueFactory.createIRI(RDF_NS_URI + "Description");

    /** rdf:first URL */
    protected static final IRI rdfFirst = valueFactory.createIRI(RDF_NS_URI + "first");

    /** rdf:rest URL */
    protected static final IRI rdfRest = valueFactory.createIRI(RDF_NS_URI + "rest");

    /** rdf:nil URL */
    protected static final IRI rdfNil = valueFactory.createIRI(RDF_NS_URI + "nil");

    /** rdf:parseType URL */
    protected static final IRI rdfParseType = valueFactory.createIRI(RDF_NS_URI + "parseType");

    /** rdf:about URL */
    protected static final IRI rdfAbout = valueFactory.createIRI(RDF_NS_URI + "about");

    /** rdf:resource URL */
    protected static final IRI rdfResource = valueFactory.createIRI(RDF_NS_URI + "resource");

    /** rdfs:label URL */
    protected static final IRI rdfsLabel = valueFactory.createIRI(RDFS_NS_URI + "label");

    /** rdfs:comment URL */
    protected static final IRI rdfsComment = valueFactory.createIRI(RDFS_NS_URI + "comment");

    /** rdfs:subClassOf URL */
    protected static final IRI rdfsSubClassOf = valueFactory.createIRI(RDFS_NS_URI + "subClassOf");

    /** rdfs:subPropertyOf URL */
    protected static final IRI rdfsSubPropertyOf = valueFactory.createIRI(RDFS_NS_URI + "subPropertyOf");

    /** owl:Ontology URL */
    protected static final IRI owlOntology = valueFactory.createIRI(OWL_NS_URI + "Ontology");

    /** owl:imports URL */
    protected static final IRI owlImports = valueFactory.createIRI(OWL_NS_URI + "imports");

    /** owl:sameAs URL */
    protected static final IRI owlSameAs = valueFactory.createIRI(OWL_NS_URI + "sameAs");

    /** owl:NamedIndividual URL */
    protected static final IRI owlNamedIndividual = valueFactory.createIRI(OWL_NS_URI + "NamedIndividual");

    /** owl:Thing URL */
    protected static final IRI owlThing = valueFactory.createIRI(OWL_NS_URI + "Thing");

    /** owl:onProperty URL */
    protected static final IRI owlOnProperty = valueFactory.createIRI(OWL_NS_URI + "onProperty");

    /** owl:onClass URL */
    protected static final IRI owlOnClass = valueFactory.createIRI(OWL_NS_URI + "onClass");

    /** xs:string URL */
    protected static final IRI xsString = valueFactory.createIRI(XML_SCHEMA_NS_URI + "string");

    /** rdf:langString URL */
    protected static final IRI rdfLangString = valueFactory.createIRI(RDF_NS_URI + "langString");

    /** Comparator for TurtleObjectList objects. */
    protected class TurtleObjectListComparator implements Comparator<SortedTurtleObjectList> {
        private ValueComparator valc = null;
        private Class collectionClass = Value.class;

        public TurtleObjectListComparator() {}

        public TurtleObjectListComparator(Class collectionClass) {
            this.collectionClass = collectionClass;
        }

        @Override
        public int compare(SortedTurtleObjectList list1, SortedTurtleObjectList list2) {
            return compare(list1, list2, new ArrayList<Object>());
        }

        public int compare(Collection<Value> list1, Collection<Value> list2, ArrayList<Object> excludedList) {
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

        private int compare(Collection<Value> list1, Iterator<Value> iter1, Collection<Value> list2, Iterator<Value> iter2, ArrayList<Object> excludedList) {
            if (iter1.hasNext()) {
                if (iter2.hasNext()) {
                    Value value1 = iter1.next();
                    Value value2 = iter2.next();
                    if (valc == null) { valc = new ValueComparator(collectionClass); }
                    excludedList.add(list1);
                    excludedList.add(list2);
                    int cmp = valc.compare(value1, value2, excludedList);
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

    /** Comparator for TurtlePredicateObjectMap objects. */
    protected class TurtlePredicateObjectMapComparator implements Comparator<SortedTurtlePredicateObjectMap> {
        private IRIComparator iric = null;
        private TurtleObjectListComparator tolc = null;
        private Class collectionClass = Value.class;

        public TurtlePredicateObjectMapComparator() {}

        public TurtlePredicateObjectMapComparator(Class collectionClass) {
            this.collectionClass = collectionClass;
        }

        @Override
        public int compare(SortedTurtlePredicateObjectMap map1, SortedTurtlePredicateObjectMap map2) {
            return compare(map1, map2, new ArrayList<Object>());
        }

        public int compare(SortedTurtlePredicateObjectMap map1, SortedTurtlePredicateObjectMap map2, ArrayList<Object> excludedList) {
            if ((map1 == null) || excludedList.contains(map1)) {
                if ((map2 == null) || excludedList.contains(map2)) {
                    return 0; // two null/excluded maps are equal
                } else {
                    return -1; // null/excluded map comes before non-null/excluded map
                }
            } else {
                if ((map2 == null) || excludedList.contains(map2)) {
                    return 1; // non-null/excluded map comes before null/excluded map
                } else {
                    if (map1 == map2) {
                        return 0;
                    } else {
                        Iterator<IRI> iter1 = map1.keySet().iterator();
                        Iterator<IRI> iter2 = map2.keySet().iterator();
                        return compare(map1, iter1, map2, iter2, excludedList);
                    }
                }
            }
        }

        private int compare(SortedTurtlePredicateObjectMap map1, Iterator<IRI> iter1, SortedTurtlePredicateObjectMap map2, Iterator<IRI> iter2, ArrayList<Object> excludedList) {
            if (iter1.hasNext()) {
                if (iter2.hasNext()) {
                    IRI key1 = iter1.next();
                    IRI key2 = iter2.next();
                    if (iric == null) { iric = new IRIComparator(); }
                    excludedList.add(map1);
                    excludedList.add(map2);
                    int cmp = iric.compare(key1, key2, excludedList);
                    if (cmp != 0) {
                        return cmp;
                    } else { // predicate keys are the same, so test object values
                        SortedTurtleObjectList values1 = map1.get(key1);
                        SortedTurtleObjectList values2 = map2.get(key2);
                        SortedTurtleObjectList nonBlankValues1 = new SortedTurtleObjectList(collectionClass);
                        SortedTurtleObjectList nonBlankValues2 = new SortedTurtleObjectList(collectionClass);
                        // Leave blank nodes out of the value comparison, unless blank nodes are being inlined.
                        for (Value value : values1) {
                            if (inlineBlankNodes || !(value instanceof BNode)) { // including blank nodes is only feasible when inlining blank nodes, as that implicitly promises no blank node loops
                                nonBlankValues1.add(value);
                            }
                        }
                        for (Value value : values2) {
                            if (inlineBlankNodes || !(value instanceof BNode)) { // including blank nodes is only feasible when inlining blank nodes, as that implicitly promises no blank node loops
                                nonBlankValues2.add(value);
                            }
                        }
                        if (tolc == null) { tolc = new TurtleObjectListComparator(collectionClass); }
                        cmp = tolc.compare(nonBlankValues1, nonBlankValues2, excludedList);
                        if (cmp != 0) {
                            return cmp;
                        } else { // values are the same, try the next predicates in the maps
                            return compare(map1, iter1, map2, iter2, excludedList);
                        }
                    }
                } else { // only iter1 has a next value
                    return 1; // map1 comes after map2
                }
            } else {
                if (iter2.hasNext()) { // only iter2 has a next value
                    return -1; // map1 comes before map2
                } else { // both iterators have no next value
                    return 0;
                }
            }
        }
    }

    /** Comparator for Sesame BNode objects. */
    protected class BNodeComparator implements Comparator<BNode> {
        private TurtlePredicateObjectMapComparator tpomc = null;
        private TurtleObjectListComparator tolc = null;
        private Class collectionClass = Value.class;

        public BNodeComparator() {}

        public BNodeComparator(Class collectionClass) {
            this.collectionClass = collectionClass;
        }

        @Override
        public int compare(BNode bnode1, BNode bnode2) {
            return compare(bnode1, bnode2, new ArrayList<Object>());
        }

        public int compare(BNode bnode1, BNode bnode2, ArrayList<Object> excludedList) {
            if ((bnode1 == null) || excludedList.contains(bnode1) || !unsortedTripleMap.containsKey(bnode1)) {
                if ((bnode2 == null) || excludedList.contains(bnode2) || !unsortedTripleMap.containsKey(bnode2)) {
                    return 0; // two null/excluded blank nodes are equal
                } else {
                    return -1; // null/excluded blank node comes before non-null/excluded blank node
                }
            } else {
                if ((bnode2 == null) || excludedList.contains(bnode2) || !unsortedTripleMap.containsKey(bnode2)) {
                    return 1; // non-null/excluded blank node comes before null/excluded blank node
                } else {
                    if (bnode1 == bnode2) {
                        return 0;
                    } else {
                        if (inlineBlankNodes && isCollection(bnode1, collectionClass)) { // deal with RDF collection blank nodes separately, when inlining blank nodes
                            if (inlineBlankNodes && isCollection(bnode2, collectionClass)) {
                                ArrayList<Value> values1 = getCollectionMembers(bnode1, collectionClass);
                                ArrayList<Value> values2 = getCollectionMembers(bnode2, collectionClass);
                                if (tolc == null) { tolc = new TurtleObjectListComparator(collectionClass); }
                                int cmp = tolc.compare(values1, values2, excludedList);
                                if ((cmp != 0) || inlineBlankNodes) { // cmp = 0 value is only reliable when inlining blank nodes
                                    return cmp;
                                } else { // if all else fails, do a string comparison
                                    return bnode1.stringValue().compareTo(bnode2.stringValue());
                                }
                            } else {
                                return -1; // an RDF collection comes before any other blank node
                            }
                        } else {
                            if (inlineBlankNodes && isCollection(bnode2, collectionClass)) {
                                return 1; // an RDF collection comes before any other blank node
                            } else { // neither blank node is an RDF collection
                                SortedTurtlePredicateObjectMap map1 = unsortedTripleMap.getSorted(bnode1, collectionClass);
                                SortedTurtlePredicateObjectMap map2 = unsortedTripleMap.getSorted(bnode2, collectionClass);
                                if (tpomc == null) { tpomc = new TurtlePredicateObjectMapComparator(collectionClass); }
                                excludedList.add(bnode1);
                                excludedList.add(bnode2);
                                int cmp = tpomc.compare(map1, map2, excludedList);
                                if ((cmp != 0) || inlineBlankNodes) { // cmp = 0 value is only reliable when inlining blank nodes
                                    return cmp;
                                } else { // if all else fails, do a string comparison
                                    return bnode1.stringValue().compareTo(bnode2.stringValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Comparator for Sesame Value objects. */
    protected class ValueComparator implements Comparator<Value> {
        private BNodeComparator bnc = null;
        private Class collectionClass = Value.class;

        public ValueComparator() {}

        public ValueComparator(Class collectionClass) {
            this.collectionClass = collectionClass;
        }

        @Override
        public int compare(Value value1, Value value2) {
            return compare(value1, value2, new ArrayList<Object>());
        }

        public int compare(Value value1, Value value2, ArrayList<Object> excludedList) {
            if ((value1 == null) || excludedList.contains(value1)) {
                if ((value2 == null) || excludedList.contains(value2)) {
                    return 0; // two null/excluded values are equal
                } else {
                    return -1; // null/excluded value comes before non-null/excluded value
                }
            } else {
                if ((value2 == null) || excludedList.contains(value2)) {
                    return 1; // non-null/excluded value comes before null/excluded value
                } else {
                    if (value1 == value2) {
                        return 0;
                    } else {
                        // Order blank nodes so that they come after other values.
                        if (value1 instanceof BNode) {
                            if (value2 instanceof BNode) {
                                if (bnc == null) { bnc = new BNodeComparator(collectionClass); }
                                int cmp = bnc.compare((BNode)value1, (BNode)value2, excludedList);
                                if (cmp != 0) {
                                    return cmp;
                                } else {
                                    return value1.stringValue().compareTo(value2.stringValue()); // to make sure that the sorted blank node list doesn't exclude a blank node just because it has the same content as another
                                }
                            } else {
                                return 1; // blank node value1 comes after value2.
                            }
                        } else {
                            if (value2 instanceof BNode) {
                                return -1; // value1 comes before blank node value2.
                            } else { // compare non-blank-node values.
                                // TODO: support natural ordering of non-string literals
                                if ((value1 instanceof Literal) && (value2 instanceof Literal)) {
                                    return compareSimpleValue((Literal)value1, (Literal)value2);
                                } else {
                                    return compareSimpleValue(value1, value2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected int compareSimpleValue(Literal literal1, Literal literal2) {
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
                } else { // !literal2.getLanguage().isPresent()
                    // neither literal has a language to compare
                }
            }
            if (literal1.getDatatype() != null) {
                if (literal2.getDatatype() != null) {
                    int cmp2 = literal1.getDatatype().stringValue().compareTo(literal2.getDatatype().stringValue());
                    if (cmp2 != 0) {
                        return cmp2;
                    }
                } else { // literal2.getDatatype() == null
                    return -1; // literal1 with data type comes before literal2 without data type
                }
            } else { // literal1.getDatatype() == null
                if (literal2.getDatatype() != null) {
                    return 1; // literal1 without data type comes after literal2 with data type
                } else { // literal2.getDatatype().isPresent() == null
                    // neither literal has a data type to compare
                }
            }
            return 0; // no difference in value, language nor datatype
        }
    }

    protected int compareSimpleValue(Value value1, Value value2) {
        // Use string comparison as the last option.
        return value1.stringValue().compareTo(value2.stringValue());
    }

    /** An unsorted list of RDF object values. */
    protected class UnsortedTurtleObjectList extends HashSet<Value> {
        public SortedTurtleObjectList toSorted(Class collectionClass) {
            SortedTurtleObjectList sortedOList = new SortedTurtleObjectList(collectionClass);
            for (Value value : this) {
                sortedOList.add(value);
            }
            return sortedOList;
        }
    }

    /** A sorted list of RDF object values. */
    protected class SortedTurtleObjectList extends TreeSet<Value> {
        public SortedTurtleObjectList() { super(new ValueComparator()); }
        public SortedTurtleObjectList(Class collectionClass) {
            super(new ValueComparator(collectionClass));
        }
    }

    /** Comparator for Sesame IRI objects. */
    protected class IRIComparator implements Comparator<IRI> {
        @Override
        public int compare(IRI iri1, IRI iri2) {
            return compare(iri1, iri2, new ArrayList<Object>());
        }

        public int compare(IRI iri1, IRI iri2, ArrayList<Object> excludedList) {
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

    /** An unsorted map from predicate IRIs to lists of object values. */
    protected class UnsortedTurtlePredicateObjectMap extends HashMap<IRI, UnsortedTurtleObjectList> {
        public SortedTurtleObjectList getSorted(IRI predicate, Class collectionClass) {
            if (containsKey(predicate)) {
                return get(predicate).toSorted(collectionClass);
            } else {
                return null;
            }
        }

        public SortedTurtlePredicateObjectMap toSorted(Class collectionClass) {
            SortedTurtlePredicateObjectMap sortedPOMap = new SortedTurtlePredicateObjectMap();
            for (IRI predicate : keySet()) {
                sortedPOMap.put(predicate, getSorted(predicate, collectionClass));
            }
            return sortedPOMap;
        }

        public int fullSize() {
            int result = 0;
            for (Resource pred : keySet()) {
                result += get(pred).size();
            }
            return result;
        }
    }

    /** A sorted map from predicate IRIs to lists of object values. */
    protected class SortedTurtlePredicateObjectMap extends TreeMap<IRI, SortedTurtleObjectList> {
        public SortedTurtlePredicateObjectMap() { super(new IRIComparator()); }

        public int fullSize() {
            int result = 0;
            for (Resource pred : keySet()) {
                result += get(pred).size();
            }
            return result;
        }
    }

    /** An unsorted list of RDF IRI values. */
    protected class UnsortedTurtlePredicateList extends HashSet<IRI> {
        public SortedTurtlePredicateList toSorted() {
            SortedTurtlePredicateList sortedPList = new SortedTurtlePredicateList();
            for (IRI predicate : this) {
                sortedPList.add(predicate);
            }
            return sortedPList;
        }
    }

    /** A sorted list of RDF IRI values. */
    protected class SortedTurtlePredicateList extends TreeSet<IRI> {
        public SortedTurtlePredicateList() { super(new IRIComparator()); }
    }

    /** Comparator for Sesame Resource objects. */
    protected class ResourceComparator implements Comparator<Resource> {
        private BNodeComparator bnc = null;
        private IRIComparator iric = null;
        private Class collectionClass = Value.class;

        public ResourceComparator() {}

        public ResourceComparator(Class collectionClass) {
            this.collectionClass = collectionClass;
        }

        @Override
        public int compare(Resource resource1, Resource resource2) {
            return compare(resource1, resource2, new ArrayList<Object>());
        }

        private int compare(Resource resource1, Resource resource2, ArrayList<Object> excludedList) {
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
                    if (resource1 == resource2) {
                        return 0;
                    } else {
                        // Order blank nodes so that they come after other values.
                        if (resource1 instanceof BNode) {
                            if (resource2 instanceof BNode) {
                                if (bnc == null) { bnc = new BNodeComparator(collectionClass); }
                                int cmp = bnc.compare((BNode)resource1, (BNode)resource2, excludedList);
                                if (cmp != 0) {
                                    return cmp;
                                } else {
                                    return resource1.stringValue().compareTo(resource2.stringValue()); // to make sure that the sorted blank node list doesn't exclude a blank node just because it has the same content as another
                                }
                            } else {
                                return 1; // blank node resource1 comes after resource2.
                            }
                        } else {
                            if (resource2 instanceof BNode) {
                                return -1; // resource1 comes before blank node resource2.
                            } else { // compare non-blank-node resources.
                                if ((resource1 instanceof IRI) && (resource2 instanceof IRI)) { // compare IRIs
                                    if (iric == null) { iric = new IRIComparator(); }
                                    return iric.compare((IRI)resource1, (IRI)resource2, excludedList);
                                } else {
                                    return resource1.stringValue().compareTo(resource2.stringValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** An unsorted map from subject resources to predicate/object pairs. */
    protected class UnsortedTurtleSubjectPredicateObjectMap extends HashMap<Resource, UnsortedTurtlePredicateObjectMap> {
        public SortedTurtlePredicateObjectMap getSorted(Resource subject, Class collectionClass) {
            if (containsKey(subject)) {
                return get(subject).toSorted(collectionClass);
            } else {
                return null;
            }
        }

        public SortedTurtleSubjectPredicateObjectMap toSorted(Class collectionClass) {
            SortedTurtleSubjectPredicateObjectMap sortedSPOMap = new SortedTurtleSubjectPredicateObjectMap(collectionClass);
            for (Resource subject : keySet()) {
                sortedSPOMap.put(subject, getSorted(subject, collectionClass));
            }
            return sortedSPOMap;
        }

        public int fullSize() {
            int result = 0;
            for (Resource subj : keySet()) {
                UnsortedTurtlePredicateObjectMap poMap = get(subj);
                if (poMap != null) { result += poMap.fullSize(); }
            }
            return result;
        }
    }

    /** A sorted map from subject resources to predicate/object pairs. */
    protected class SortedTurtleSubjectPredicateObjectMap extends TreeMap<Resource, SortedTurtlePredicateObjectMap> {
        public SortedTurtleSubjectPredicateObjectMap() { super(new ResourceComparator()); }
        public SortedTurtleSubjectPredicateObjectMap(Class collectionClass) {
            super(new ResourceComparator(collectionClass));
        }

        public int fullSize() {
            int result = 0;
            for (Resource subj : keySet()) {
                SortedTurtlePredicateObjectMap poMap = get(subj);
                if (poMap != null) { result += poMap.fullSize(); }
            }
            return result;
        }
    }

    /** An unsorted list of RDF resource values. */
    protected class UnsortedTurtleResourceList extends HashSet<Resource> {
        public SortedTurtleResourceList toSorted(Class collectionClass) {
            SortedTurtleResourceList sortedRList = new SortedTurtleResourceList(collectionClass);
            for (Resource resource : this) {
                sortedRList.add(resource);
            }
            return sortedRList;
        }
    }

    /** A sorted list of RDF resource values. */
    protected class SortedTurtleResourceList extends TreeSet<Resource> {
        public SortedTurtleResourceList() { super(new ResourceComparator()); }
        public SortedTurtleResourceList(Class collectionClass) {
            super(new ResourceComparator(collectionClass));
        }
    }

    /** An unsorted list of RDF blank nodes. */
    // TODO: why is this not a subclass of UnsortedTurtleResourceList?
    protected class UnsortedTurtleBNodeList extends HashSet<BNode> {
        public SortedTurtleBNodeList toSorted(Class collectionClass) {
            SortedTurtleBNodeList sortedBNList = new SortedTurtleBNodeList(collectionClass);
            for (BNode bnode : this) {
                sortedBNList.add(bnode);
            }
            return sortedBNList;
        }
    }

    /** A sorted list of RDF blank nodes. */
    // TODO: why is this not a subclass of SortedTurtleResourceList?
    protected class SortedTurtleBNodeList extends TreeSet<BNode> {
        public SortedTurtleBNodeList() { super(new BNodeComparator()); }
        public SortedTurtleBNodeList(Class collectionClass) {
            super(new BNodeComparator(collectionClass));
        }
    }

    /** Base IRI for the RDF output document. */
    protected IRI baseIri = null;

    /** Preference for prefix or base-IRI based IRI shortening. */
    protected ShortIriPreferences shortIriPreference = ShortIriPreferences.prefix;

    /** Whether to use a DTD subset to allow IRI shortening in RDF/XML */
    protected boolean useDtdSubset = false;

    /** Whether to inline blank nodes */
    protected boolean inlineBlankNodes = false;

    /** Leading comment lines */
    protected String[] leadingComments = null;

    /** Trailing comment lines */
    protected String[] trailingComments = null;

    /** String data type option */
    protected SesameSortedRDFWriterFactory.StringDataTypeOptions stringDataTypeOption = SesameSortedRDFWriterFactory.StringDataTypeOptions.implicit;

    /** Unsorted list of subjects which are OWL ontologies, as they are rendered before other subjects. */
    protected UnsortedTurtleResourceList unsortedOntologies = null;

    /** Sorted list of subjects which are OWL ontologies, as they are rendered before other subjects. */
    protected SortedTurtleResourceList sortedOntologies = null;

    /** Unsorted list of blank nodes, as they are rendered separately from other nodes. */
    protected UnsortedTurtleResourceList unsortedBlankNodes = null;

    /** Sorted list of blank nodes, as they are rendered separately from other nodes. */
    protected SortedTurtleResourceList sortedBlankNodes = null;

    /** Unsorted list of blank nodes that are objects of statements. */
    protected UnsortedTurtleBNodeList objectBlankNodes = null;

    /** Map of serialisation names for blank nodes. */
    protected HashMap<BNode, String> blankNodeNameMap = null;

    /** Unsorted hash map containing triple data. */
    protected UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap = null;

    /** Sorted hash map containing triple data. */
    protected SortedTurtleSubjectPredicateObjectMap sortedTripleMap = null;

    /** All predicates from the input ontology. */
    protected HashSet<IRI> allPredicates = null;

    /** Predicates that are specially rendered before all others. */
    protected ArrayList<IRI> firstPredicates = null;

    /** Comparator for Strings that shorts longer strings first. */
    protected class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            if (str1 == null) { throw new NullPointerException("cannot compare null to String"); }
            if (str2 == null) { throw new NullPointerException("cannot compare String to null"); }
            if (str1.length() > str2.length()) {
                return -1;
            } else if (str1.length() < str2.length()) {
                return 1;
            } else { // if same length
                return str1.compareTo(str2);
            }
        }
    }

    /**
     * Whether the given blank node represents an RDF collection, or not.
     * @param bnode blank node to test as an RDF collection
     * @param collectionClass all collection members must be instances of this class
     * @return whether the blank node is an RDF collection
     */
    protected boolean isCollection(BNode bnode, Class collectionClass) {
        SortedTurtlePredicateObjectMap poMap = unsortedTripleMap.getSorted(bnode, collectionClass);
        if (poMap != null) {
            Set<IRI> predicates = poMap.keySet();
            int firstCount = predicates.contains(rdfFirst) ? 1 : 0;
            int restCount = predicates.contains(rdfRest) ? 1 : 0;
            if (predicates.size() == firstCount + restCount) {
                SortedTurtleObjectList firstValues = poMap.get(rdfFirst);
                for (Value value : firstValues) {
                    if (!collectionClass.isInstance(value)) { return false; } // all collection members must match the collection class type
                }
                if (restCount >= 1) {
                    SortedTurtleObjectList rest = poMap.get(rdfRest);
                    if (rest.size() == 1) {
                        for (Value value : rest) {
                            if (rdfNil.equals(value)) {
                                return true;
                            }
                            if (value instanceof BNode) {
                                return isCollection((BNode)value, collectionClass);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * If the given blank node is an RDF collection, returns the members of the collection.
     * @param bnode blank node which is an RDF collection
     * @param collectionClass all collection members must be instances of this class
     * @return the members of the RDF collection
     */
    protected ArrayList<Value> getCollectionMembers(BNode bnode, Class collectionClass) {
        // An ArrayList is used here, as collection members should be retained in their original order, not sorted.
        ArrayList<Value> members = new ArrayList<Value>();
        if (isCollection(bnode, collectionClass)) {
            SortedTurtlePredicateObjectMap poMap = unsortedTripleMap.getSorted(bnode, collectionClass);
            SortedTurtleObjectList newMembers = poMap.get(rdfFirst);
            if (newMembers != null) {
                for (Value newMember : newMembers) {
                    members.add(newMember);
                }
            }
            SortedTurtleObjectList rest = poMap.get(rdfRest);
            if (rest != null) {
                for (Value nextRest : rest) {
                    if (nextRest instanceof BNode) {
                        ArrayList<Value> newRestMembers = getCollectionMembers((BNode)nextRest, collectionClass);
                        if (newRestMembers != null) {
                            for (Value newMember : newRestMembers) {
                                members.add(newMember);
                            }
                        }
                    }
                }
            }
        }
        return members;
    }

    /** A reverse namespace table with which returns the longest namespace IRIs first.  Key is IRI string, value is prefix string. */
    protected class ReverseNamespaceTable extends TreeMap<String,String> {
        public ReverseNamespaceTable() { super(new StringLengthComparator()); }
    }

    /**
     * Namespace mappings created by the serializer.
     */
    protected Map<String, String> generatedNamespaceTable = null;


    /** Reverse namespace table used to map IRIs to prefixes.  Key is IRI string, value is prefix string. */
    protected ReverseNamespaceTable reverseNamespaceTable = null;

    /** Output stream for this RDF writer. */
    protected Writer out = null;

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    public SesameSortedRDFWriter(OutputStream out) {
        assert out != null : "output stream cannot be null";
        this.out = new OutputStreamWriter(out);
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    public SesameSortedRDFWriter(Writer writer) {
        assert writer != null : "output writer cannot be null";
        this.out = writer;
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     * @param options options for the RDF writer.
     */
    public SesameSortedRDFWriter(OutputStream out, Map<String, Object> options) {
        assert out != null : "output stream cannot be null";
        this.out = new OutputStreamWriter(out);
        if (options.containsKey("baseIri")) {
            this.baseIri = (IRI) options.get("baseIri");
        }
        if (options.containsKey("shortIriPref")) {
            this.shortIriPreference = (ShortIriPreferences) options.get("shortIriPref");
        }
        if (options.containsKey("useDtdSubset")) {
            this.useDtdSubset = (Boolean) options.get("useDtdSubset");
        }
        if (options.containsKey("inlineBlankNodes")) {
            this.inlineBlankNodes = (Boolean) options.get("inlineBlankNodes");
        }
        if (options.containsKey("leadingComments")) {
            this.leadingComments = (String[]) options.get("leadingComments");
        }
        if (options.containsKey("trailingComments")) {
            this.trailingComments = (String[]) options.get("trailingComments");
        }
        if (options.containsKey("stringDataTypeOption")) {
            this.stringDataTypeOption = (SesameSortedRDFWriterFactory.StringDataTypeOptions) options.get("stringDataTypeOption");
        }
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     * @param options options for the RDF writer.
     */
    public SesameSortedRDFWriter(Writer writer, Map<String, Object> options) {
        assert writer != null : "output writer cannot be null";
        this.out = writer;
        if (options.containsKey("baseIri")) {
            this.baseIri = (IRI) options.get("baseIri");
        }
        if (options.containsKey("shortIriPref")) {
            this.shortIriPreference = (ShortIriPreferences) options.get("shortIriPref");
        }
        if (options.containsKey("useDtdSubset")) {
            this.useDtdSubset = (Boolean) options.get("useDtdSubset");
        }
        if (options.containsKey("inlineBlankNodes")) {
            this.inlineBlankNodes = (Boolean) options.get("inlineBlankNodes");
        }
        if (options.containsKey("leadingComments")) {
            this.leadingComments = (String[]) options.get("leadingComments");
        }
        if (options.containsKey("trailingComments")) {
            this.trailingComments = (String[]) options.get("trailingComments");
        }
        if (options.containsKey("stringDataTypeOption")) {
            this.stringDataTypeOption = (SesameSortedRDFWriterFactory.StringDataTypeOptions) options.get("stringDataTypeOption");
        }
    }

    /**
     * Gets the RDF format that this RDFWriter uses.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.TURTLE;
    }

    /**
     * Converts a IRI to a QName, if possible, given the available namespace prefixes.  Returns null if there is no match to a prefix.
     * @param iri The IRI to convert to a QName, if possible.
     * @param useGeneratedPrefixes Whether to use namespace prefixes generated by the serializer.
     * @return The equivalent QName for the IRI, or null if no equivalent.
     */
    protected QName convertIriToQName(IRI iri, boolean useGeneratedPrefixes) {
        String iriString = iri.stringValue();
        for (String iriStem : reverseNamespaceTable.keySet()) {
            if ((iriString.length() > iriStem.length()) && iriString.startsWith(iriStem)) {
                String localPart = iriString.substring(iriStem.length());
                if (isPrefixedNameLocalPart(localPart)) { // to be a value QName, the 'local part' has to be valid
                    String prefix = reverseNamespaceTable.get(iriStem);
                    if (useGeneratedPrefixes || !generatedNamespaceTable.containsKey(prefix)) {
                        return new QName(iriStem, localPart, prefix);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        // Failed to find a match, return null.
        return null;
    }

    protected String convertIriToRelativeIri(IRI iri, boolean useTurtleQuoting) {
        // Note: does not check that the baseIri doesn't terminate in the middle of some IRI of which it really isn't the base.
        if (baseIri != null) {
            String iriString = iri.stringValue();
            String baseIriString = baseIri.stringValue();
            if ((iriString.length() > baseIriString.length()) && iriString.startsWith(baseIriString)) {
                String result = (useTurtleQuoting ? "<" : "") +
                        iriString.substring(baseIriString.length()) +
                        (useTurtleQuoting ? ">" : "");
                return result;
            }
        }
        // Failed to find a match, return null.
        return null;
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        allPredicates = new HashSet<IRI>();
        namespaceTable = new TreeMap<String, String>();
        generatedNamespaceTable = new TreeMap<String, String>();
        unsortedOntologies = new UnsortedTurtleResourceList();
        unsortedBlankNodes = new UnsortedTurtleResourceList();
        blankNodeNameMap = new HashMap<BNode, String>();
        unsortedTripleMap = new UnsortedTurtleSubjectPredicateObjectMap();
        objectBlankNodes = new UnsortedTurtleBNodeList();
    }

    /**
     * Adds a default namespace prefix to the namespace table, if no prefix has been defined.
     * @param namespaceIri The namespace IRI.  Cannot be null.
     * @param defaultPrefix The default prefix to use, if no prefix is yet assigned.  Cannot be null.
     */
    protected void addDefaultNamespacePrefixIfMissing(String namespaceIri, String defaultPrefix) {
        if ((namespaceIri != null) && (defaultPrefix != null)) {
            if (!namespaceTable.containsValue(namespaceIri)) {
                namespaceTable.put(defaultPrefix, namespaceIri);
            }
        }
    }

    /**
     * Checks if all predicate IRIs have a matching namespace prefix, i.e. that they can be converted into QNames.
     * This is needed for RDF/XML.
     * If there is a predicate without a matching namespace prefix, a namespace prefix is created for it.
     */
    protected void addNamespacePrefixesForPredicates() {
        int namespaceIndex = 1;
        for (IRI predicate : allPredicates) {
            String predicateString = predicate.stringValue();
            int namespaceIriEndPos = Math.max(
                predicateString.lastIndexOf("/"),
                predicateString.lastIndexOf("#")
            );
            String namespaceIri = predicateString.substring(0, namespaceIriEndPos+1);
            if (namespaceIri.length() >= 1) {
                if (!namespaceTable.containsValue(namespaceIri)) {
                    String newPrefix = "zzzns" + String.format("%04d", namespaceIndex);
                    namespaceTable.put(newPrefix, namespaceIri);
                    generatedNamespaceTable.put(newPrefix, namespaceIri); // track the namespace mappings created by the serializer
                    namespaceIndex += 1;
                }
            }
        }
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            // !!!! Override method must set values for 'sortedOntologies', 'sortedTripleMap' & 'sortedBlankNodes' before calling this method

            // Create serialisation names for blank nodes.
            StringBuilder blankNodeNamePaddingBuilder = new StringBuilder();
            blankNodeNamePaddingBuilder.append("0");
            int blankNodeCount = unsortedBlankNodes.size();
            while (blankNodeCount > 9) {
                blankNodeCount /= 10;
                blankNodeNamePaddingBuilder.append("0");
            }
            String blankNodeNamePadding = blankNodeNamePaddingBuilder.toString();
            int blankNodeIndex = 0;
            for (Value value : sortedBlankNodes) {
                if (value instanceof BNode) {
                    BNode bnode = (BNode)value;
                    blankNodeIndex++;
                    String blankNodeName = Integer.toString(blankNodeIndex);
                    if (blankNodeName.length() < blankNodeNamePadding.length()) {
                        blankNodeName = blankNodeNamePadding.substring(0, blankNodeNamePadding.length() - blankNodeName.length()) + blankNodeName;
                    }
                    blankNodeName = "blank" + blankNodeName;
                    blankNodeNameMap.put(bnode, blankNodeName);
                }
            }

            // Set up list of predicates that appear first under their subjects.
            firstPredicates = new ArrayList<IRI>(); // predicates that are specially rendered first
            firstPredicates.add(rdfType);
            firstPredicates.add(rdfsSubClassOf);
            firstPredicates.add(rdfsSubPropertyOf);
            firstPredicates.add(owlSameAs);
            firstPredicates.add(rdfsLabel);
            firstPredicates.add(rdfsComment);
            firstPredicates.add(owlOnProperty);
            firstPredicates.add(owlOnClass);

            // Add default namespace prefixes, if they haven't yet been defined.  May fail if these prefixes have already been defined for different namespace IRIs.
            addDefaultNamespacePrefixIfMissing(RDF_NS_URI, "rdf");
            addDefaultNamespacePrefixIfMissing(RDFS_NS_URI, "rdfs");
            addDefaultNamespacePrefixIfMissing(OWL_NS_URI, "owl");
            addDefaultNamespacePrefixIfMissing(XML_SCHEMA_NS_URI, "xs");

            // Add any extra namespaces needed to make all predicates writeable as a QName.  This is especially needed for RDF/XML.
            addNamespacePrefixesForPredicates();

            // Create reverse namespace table.
            reverseNamespaceTable = new ReverseNamespaceTable();
            for (String prefix : namespaceTable.keySet()) {
                String iri = namespaceTable.get(prefix);
                reverseNamespaceTable.put(iri, prefix);
            }

            // Create list of imports
            SortedTurtleObjectList importList = new SortedTurtleObjectList();
            for (Resource subject : sortedOntologies) {
                if (sortedTripleMap.containsKey(subject)) {
                    SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);
                    if (poMap.containsKey(owlImports)) {
                        SortedTurtleObjectList importsOList = poMap.get(owlImports);
                        for (Value value : importsOList) {
                            importList.add(value);
                        }
                    }
                }
            }

            // Write header information, including leading comments.
            writeHeader(out, importList, leadingComments);

            // Track how many of the subjects have been written
            int allSubjectCount = 0;
            for (Resource subject : sortedOntologies) {
                if (!(subject instanceof BNode)) {
                    allSubjectCount++;
                }
            }
            for (Resource subject : sortedTripleMap.keySet()) {
                if (!sortedOntologies.contains(subject) && !(subject instanceof BNode)) {
                    allSubjectCount++;
                }
            }
            for (Resource resource : sortedBlankNodes) {
                if (!inlineBlankNodes || !objectBlankNodes.contains(resource)) {
                    BNode bnode = (BNode)resource;
                    if (unsortedTripleMap.containsKey(bnode)) {
                        allSubjectCount++;
                    }
                }
            }
            int subjectCount = 0;

            // Write out subjects which are unsortedOntologies.
            for (Resource subject : sortedOntologies) {
                if (!(subject instanceof BNode)) {
                    subjectCount++;
                    writeSubjectTriples(out, subject);
                    if (subjectCount < allSubjectCount) { writeSubjectSeparator(out); }
                }
            }

            // Write out all other subjects (not unsortedOntologies; also not blank nodes).
            for (Resource subject : sortedTripleMap.keySet()) {
                if (!sortedOntologies.contains(subject) && !(subject instanceof BNode)) {
                    subjectCount++;
                    writeSubjectTriples(out, subject);
                    if (subjectCount < allSubjectCount) { writeSubjectSeparator(out); }
                }
            }

            // Write out blank nodes that are subjects, if blank nodes are not being inlined or if the blank node is not an object.
            for (Resource resource : sortedBlankNodes) {
                if (!inlineBlankNodes || !objectBlankNodes.contains(resource)) {
                    BNode bnode = (BNode)resource;
                    if (unsortedTripleMap.containsKey(bnode)) {
                        subjectCount++;
                        writeSubjectTriples(out, bnode);
                        if (subjectCount < allSubjectCount) { writeSubjectSeparator(out); }
                    }
                }
            }

            // Write footer information, including any trailing comments.
            writeFooter(out, trailingComments);

            out.flush();
        } catch (Throwable t) {
            throw new RDFHandlerException("unable to generate/write RDF output", t);
        }
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        // Store the predicate.
        allPredicates.add(st.getPredicate());

        // Store the object if it is a blank node.
        if (st.getObject() instanceof BNode) {
            objectBlankNodes.add((BNode) st.getObject());
        }

        // Store the statement in the main 'triple map'.
        UnsortedTurtlePredicateObjectMap poMap = null;
        if (unsortedTripleMap.containsKey(st.getSubject())) {
            poMap = unsortedTripleMap.get(st.getSubject());
        } else {
            poMap = new UnsortedTurtlePredicateObjectMap();
            unsortedTripleMap.put(st.getSubject(), poMap);
        }

        UnsortedTurtleObjectList oList = null;
        if (poMap.containsKey(st.getPredicate())) {
            oList = poMap.get(st.getPredicate());
        } else {
            oList = new UnsortedTurtleObjectList();
            poMap.put(st.getPredicate(), oList);
        }

        if (!oList.contains(st.getObject())) {
            oList.add(st.getObject());
        }

        // Note subjects which are OWL ontologies, as the are handled before other subjects.
        if (st.getPredicate().equals(rdfType) && st.getObject().equals(owlOntology) && !unsortedOntologies.contains((st.getSubject()))) {
            unsortedOntologies.add(st.getSubject());
        }

        // Note subjects & objects which are blank nodes.
        if ((st.getSubject() instanceof BNode) && !unsortedBlankNodes.contains(st.getSubject())) {
            unsortedBlankNodes.add(st.getSubject());
        }
        if ((st.getObject() instanceof BNode) && !unsortedBlankNodes.contains((st.getObject()))) {
            unsortedBlankNodes.add((BNode)st.getObject());
        }
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // NOTE: comments are suppressed, as it isn't clear how to sort them sensibly with triples.
    }

    protected String convertQNameToString(QName qname, boolean useTurtleQuoting) {
        if (qname == null) {
            return "null<QName>";
        } else if (qname.getPrefix() != null) {
            return qname.getPrefix() + ":" + qname.getLocalPart();
        } else {
            return (useTurtleQuoting ? "<" : "") +
                    qname.getNamespaceURI() + qname.getLocalPart() +
                    (useTurtleQuoting ? ">" : "");
        }
    }

    protected String convertIriToString(IRI iri, boolean useGeneratedPrefixes, boolean useTurtleQuoting, boolean useJsonLdQuoting) {
        if (rdfType.equals(iri)) {
            if (useTurtleQuoting) { return "a"; }
            if (useJsonLdQuoting) { return "@type"; }
        }
        if (ShortIriPreferences.prefix.equals(shortIriPreference)) {
            QName qname = convertIriToQName(iri, useGeneratedPrefixes); // return the IRI out as a QName if possible.
            if (qname != null) {
                return convertQNameToString(qname, useTurtleQuoting);
            } else { // return the IRI relative to the base IRI, if possible.
                String relativeIri = convertIriToRelativeIri(iri, useTurtleQuoting);
                if (relativeIri != null) {
                    return relativeIri;
                } else { // return the absolute IRI
                    return (useTurtleQuoting ? "<" : "") +
                            iri.stringValue() +
                            (useTurtleQuoting ? ">" : "");
                }
            }
        }
        if (ShortIriPreferences.base_iri.equals(shortIriPreference)) {
            String relativeIri = convertIriToRelativeIri(iri, useTurtleQuoting); // return the IRI relative to the base URI, if possible.
            if (relativeIri != null) {
                return relativeIri;
            } else {
                QName qname = convertIriToQName(iri, useGeneratedPrefixes); // return the IRI out as a QName if possible.
                if (qname != null) {
                    return convertQNameToString(qname, useTurtleQuoting);
                } else { // return the absolute IRI
                    return (useTurtleQuoting ? "<" : "") +
                            iri.stringValue() +
                            (useTurtleQuoting ? ">" : "");
                }
            }
        }
        return (useTurtleQuoting ? "<" : "") +
                iri.stringValue() +
                (useTurtleQuoting ? ">" : ""); // if nothing else, do this
    }

    protected void compareSortedToUnsortedTripleMap(SortedTurtleSubjectPredicateObjectMap sortedTripleMap, UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap, String label) {
        if (sortedTripleMap.fullSize() != unsortedTripleMap.fullSize()) {
            System.err.println("**** " + label + ": triples unexpectedly lost or gained during sorting: " + sortedTripleMap.fullSize() + " != " + unsortedTripleMap.fullSize());
            System.err.flush();
            if (sortedTripleMap.size() != unsortedTripleMap.size()) {
                System.err.println("**** " + label + ": subjects unexpectedly lost or gained during sorting: " + sortedTripleMap.fullSize() + " != " + unsortedTripleMap.fullSize());
                System.err.flush();
            }
        }
    }

    abstract protected void writeHeader(Writer out, SortedTurtleObjectList importList, String[] leadingComments) throws Exception;

    abstract protected void writeSubjectTriples(Writer out, Resource subject) throws Exception;

    abstract protected void writeSubjectSeparator(Writer out) throws Exception;

    abstract protected void writePredicateAndObjectValues(Writer out, IRI predicate, SortedTurtleObjectList values) throws Exception;

    abstract protected void writeFooter(Writer out, String[] trailingComments) throws Exception;

}
