package org.edmcouncil.rdf_serializer;

import info.aduna.io.IndentingWriter;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFWriterBase;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Equivalent to Sesame's built-in Turtle writer, but the triples are sorted into a consistent order.
 * In order to do the sorting, it must be possible to load all of the RDF statements into memory.
 * NOTE: comments are suppressed, as there isn't a clear way to sort them along with triples.
 */
public class SesameSortedTurtleWriter extends RDFWriterBase {

    /** XML Schema namespace URI. */
    public static final String XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema#";

    /** RDF namespace URI. */
    public static final String RDF_NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** RDF Schema (RDFS) namespace URI. */
    public static final String RDFS_NS_URI = "http://www.w3.org/2000/01/rdf-schema#";

    /** OWL namespace URI. */
    public static final String OWL_NS_URI = "http://www.w3.org/2002/07/owl#";

    /** rdf:type ('a') URL */
    private static final URI rdfType = new URIImpl(RDF_NS_URI + "type");

    /** rdfs:label URL */
    private static final URI rdfsLabel = new URIImpl(RDFS_NS_URI + "label");

    /** rdfs:comment URL */
    private static final URI rdfsComment = new URIImpl(RDFS_NS_URI + "comment");

    /** rdfs:subClassOf URL */
    private static final URI rdfsSubClassOf = new URIImpl(RDFS_NS_URI + "subClassOf");

    /** rdfs:subPropertyOf URL */
    private static final URI rdfsSubPropertyOf = new URIImpl(RDFS_NS_URI + "subPropertyOf");

    /** owl:Ontology URL */
    private static final URI owlOntology = new URIImpl(OWL_NS_URI + "Ontology");

    /** owl:imports URL */
    private static final URI owlImports = new URIImpl(OWL_NS_URI + "imports");

    /** owl:sameAs URL */
    private static final URI owlSameAs = new URIImpl(OWL_NS_URI + "sameAs");

    /** xs:string URL */
    private static final URI xsString = new URIImpl(XML_SCHEMA_NS_URI + "string");

    /** Comparator for TurtleObjectList objects. */
    private class TurtleObjectListComparator implements Comparator<SortedTurtleObjectList> {
        private ValueComparator valc = null;

        @Override
        public int compare(SortedTurtleObjectList list1, SortedTurtleObjectList list2) {
            return compare(list1, list2, new ArrayList<Object>());
        }

        public int compare(SortedTurtleObjectList list1, SortedTurtleObjectList list2, ArrayList<Object> excludedList) {
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

        private int compare(SortedTurtleObjectList list1, Iterator<Value> iter1, SortedTurtleObjectList list2, Iterator<Value> iter2, ArrayList<Object> excludedList) {
            if (iter1.hasNext()) {
                if (iter2.hasNext()) {
                    Value value1 = iter1.next();
                    Value value2 = iter2.next();
                    if (valc == null) { valc = new ValueComparator(); }
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
    private class TurtlePredicateObjectMapComparator implements Comparator<SortedTurtlePredicateObjectMap> {
        private URIComparator uric = null;
        private TurtleObjectListComparator tolc = null;

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
                        Iterator<URI> iter1 = map1.keySet().iterator();
                        Iterator<URI> iter2 = map2.keySet().iterator();
                        return compare(map1, iter1, map2, iter2, excludedList);
                    }
                }
            }
        }

        private int compare(SortedTurtlePredicateObjectMap map1, Iterator<URI> iter1, SortedTurtlePredicateObjectMap map2, Iterator<URI> iter2, ArrayList<Object> excludedList) {
            if (iter1.hasNext()) {
                if (iter2.hasNext()) {
                    URI key1 = iter1.next();
                    URI key2 = iter2.next();
                    if (uric == null) { uric = new URIComparator(); }
                    excludedList.add(map1);
                    excludedList.add(map2);
                    int cmp = uric.compare(key1, key2, excludedList);
                    if (cmp != 0) {
                        return cmp;
                    } else { // predicate keys are the same, so test object values
                        SortedTurtleObjectList values1 = map1.get(key1);
                        SortedTurtleObjectList values2 = map2.get(key2);
                        SortedTurtleObjectList nonBlankValues1 = new SortedTurtleObjectList();
                        SortedTurtleObjectList nonBlankValues2 = new SortedTurtleObjectList();
                        // Leave blank nodes out of the value comparison.
                        for (Value value : values1) {
                            if (!(value instanceof BNode)) {
                                nonBlankValues1.add(value);
                            }
                        }
                        for (Value value : values2) {
                            if (!(value instanceof BNode)) {
                                nonBlankValues2.add(value);
                            }
                        }
                        if (tolc == null) { tolc = new TurtleObjectListComparator(); }
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
    private class BNodeComparator implements Comparator<BNode> {
        private TurtlePredicateObjectMapComparator tpomc = null;

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
                        SortedTurtlePredicateObjectMap map1 = unsortedTripleMap.getSorted(bnode1);
                        SortedTurtlePredicateObjectMap map2 = unsortedTripleMap.getSorted(bnode2);
                        if (tpomc == null) { tpomc = new TurtlePredicateObjectMapComparator(); }
                        excludedList.add(bnode1);
                        excludedList.add(bnode2);
                        int cmp = tpomc.compare(map1, map2, excludedList);
                        if (cmp != 0) {
                            return cmp;
                        } else {
                            // Nothing left to do but test string values
                            return bnode1.stringValue().compareTo(bnode2.stringValue());
                        }
                    }
                }
            }
        }
    }

    /** Comparator for Sesame Value objects. */
    private class ValueComparator implements Comparator<Value> {
        private BNodeComparator bnc = null;

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
                                if (bnc == null) { bnc = new BNodeComparator(); }
                                return bnc.compare((BNode)value1, (BNode)value2, excludedList);
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

    private int compareSimpleValue(Literal literal1, Literal literal2) {
        int cmp = literal1.stringValue().compareTo(literal2.stringValue());
        if (cmp != 0) {
            return cmp;
        } else {
            if (literal1.getLanguage() == null) {
                if (literal2.getLanguage() == null) {
                    if (literal1.getDatatype() == null) {
                        if (literal2.getDatatype() == null) {
                            return 0; // no language or data type difference
                        } else {
                            return 1; // literal1 with no data type comes after literal2 with data type
                        }
                    } else {
                        if (literal2.getDatatype() == null) {
                            return -1; // literal1 with data type comes before literal2 without data type
                        } else {
                            cmp = literal1.getDatatype().stringValue().compareTo(literal2.getDatatype().stringValue());
                            if (cmp != 0) {
                                return cmp; // datatypes are different
                            } else {
                                return 0; // no language or data type difference
                            }
                        }
                    }
                } else {
                    return 1; // literal1 without language comes after literal2 with language
                }
            } else {
                if (literal2.getLanguage() == null) {
                    return -1; // literal1 with language comes after literal2 without language
                } else {
                    cmp = literal1.getLanguage().compareTo(literal2.getLanguage());
                    if (cmp != 0) {
                        return cmp; // languages are different
                    } else {
                        return 0; // no language or data type difference
                    }
                }
            }
        }
    }

    private int compareSimpleValue(Value value1, Value value2) {
        // Use string comparison as the last option.
        return value1.stringValue().compareTo(value2.stringValue());
    }

    /** An unsorted list of RDF object values. */
    private class UnsortedTurtleObjectList extends HashSet<Value> {
        public SortedTurtleObjectList toSorted() {
            SortedTurtleObjectList sortedOList = new SortedTurtleObjectList();
            for (Value value : this) {
                sortedOList.add(value);
            }
            return sortedOList;
        }
    }

    /** A sorted list of RDF object values. */
    private class SortedTurtleObjectList extends TreeSet<Value> {
        public SortedTurtleObjectList() { super(new ValueComparator()); }
    }

    /** Comparator for Sesame URI objects. */
    private class URIComparator implements Comparator<URI> {
        @Override
        public int compare(URI uri1, URI uri2) {
            return compare(uri1, uri2, new ArrayList<Object>());
        }

        public int compare(URI uri1, URI uri2, ArrayList<Object> excludedList) {
            if ((uri1 == null) || excludedList.contains(uri1)) {
                if ((uri2 == null) || excludedList.contains(uri2)) {
                    return 0; // two null/excluded URIs are equal
                } else {
                    return -1; // null/excluded URI comes before non-null/excluded URI
                }
            } else {
                if ((uri2 == null) || excludedList.contains(uri2)) {
                    return 1; // non-null/excluded URI comes before null/excluded URI
                } else {
                    if (uri1 == uri2) {
                        return 0;
                    } else {
                        return uri1.stringValue().compareTo(uri2.stringValue());
                    }
                }
            }
        }
    }

    /** An unsorted map from predicate URIs to lists of object values. */
    private class UnsortedTurtlePredicateObjectMap extends HashMap<URI, UnsortedTurtleObjectList> {
        public SortedTurtleObjectList getSorted(URI predicate) {
            if (containsKey(predicate)) {
                return get(predicate).toSorted();
            } else {
                return null;
            }
        }

        public SortedTurtlePredicateObjectMap toSorted() {
            SortedTurtlePredicateObjectMap sortedPOMap = new SortedTurtlePredicateObjectMap();
            for (URI predicate : keySet()) {
                sortedPOMap.put(predicate, getSorted(predicate));
            }
            return sortedPOMap;
        }
    }

    /** A sorted map from predicate URIs to lists of object values. */
    private class SortedTurtlePredicateObjectMap extends TreeMap<URI, SortedTurtleObjectList> {
        public SortedTurtlePredicateObjectMap() { super(new URIComparator()); }
    }

    /** An unsorted list of RDF URI values. */
    private class UnsortedTurtlePredicateList extends HashSet<URI> {
        public SortedTurtlePredicateList toSorted() {
            SortedTurtlePredicateList sortedPList = new SortedTurtlePredicateList();
            for (URI predicate : this) {
                sortedPList.add(predicate);
            }
            return sortedPList;
        }
    }

    /** A sorted list of RDF URI values. */
    private class SortedTurtlePredicateList extends TreeSet<URI> {
        public SortedTurtlePredicateList() { super(new URIComparator()); }
    }

    /** Comparator for Sesame Resource objects. */
    private class ResourceComparator implements Comparator<Resource> {
        private BNodeComparator bnc = null;
        private URIComparator uric = null;

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
                                if (bnc == null) { bnc = new BNodeComparator(); }
                                return bnc.compare((BNode)resource1, (BNode)resource2, excludedList);
                            } else {
                                return 1; // blank node resource1 comes after resource2.
                            }
                        } else {
                            if (resource2 instanceof BNode) {
                                return -1; // resource1 comes before blank node resource2.
                            } else { // compare non-blank-node resources.
                                if ((resource1 instanceof URI) && (resource2 instanceof URI)) { // compare URIs
                                    if (uric == null) { uric = new URIComparator(); }
                                    return uric.compare((URI)resource1, (URI)resource2, excludedList);
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
    private class UnsortedTurtleSubjectPredicateObjectMap extends HashMap<Resource, UnsortedTurtlePredicateObjectMap> {
        public SortedTurtlePredicateObjectMap getSorted(Resource subject) {
            if (containsKey(subject)) {
                return get(subject).toSorted();
            } else {
                return null;
            }
        }

        public SortedTurtleSubjectPredicateObjectMap toSorted() {
            SortedTurtleSubjectPredicateObjectMap sortedSPOMap = new SortedTurtleSubjectPredicateObjectMap();
            for (Resource subject : keySet()) {
                sortedSPOMap.put(subject, getSorted(subject));
            }
            return sortedSPOMap;
        }
    }

    /** A sorted map from subject resources to predicate/object pairs. */
    private class SortedTurtleSubjectPredicateObjectMap extends TreeMap<Resource, SortedTurtlePredicateObjectMap> {
        public SortedTurtleSubjectPredicateObjectMap() { super(new ResourceComparator()); }
    }

    /** An unsorted list of RDF resource values. */
    private class UnsortedTurtleResourceList extends HashSet<Resource> {
        public SortedTurtleResourceList toSorted() {
            SortedTurtleResourceList sortedRList = new SortedTurtleResourceList();
            for (Resource resource : this) {
                sortedRList.add(resource);
            }
            return sortedRList;
        }
    }

    /** A sorted list of RDF resource values. */
    private class SortedTurtleResourceList extends TreeSet<Resource> {
        public SortedTurtleResourceList() { super(new ResourceComparator()); }
    }

    /** An unsorted list of RDF blank nodes. */
    private class UnsortedTurtleBNodeList extends HashSet<BNode> {
        public SortedTurtleBNodeList toSorted() {
            SortedTurtleBNodeList sortedBNList = new SortedTurtleBNodeList();
            for (BNode bnode : this) {
                sortedBNList.add(bnode);
            }
            return sortedBNList;
        }
    }

    /** A sorted list of RDF blank nodes. */
    private class SortedTurtleBNodeList extends TreeSet<BNode> {
        public SortedTurtleBNodeList() { super(new BNodeComparator()); }
    }

    /** Output stream for this Turtle writer. */
    private IndentingWriter output = null;

    /** Base URI for the Turtle output document. */
    private URI baseUri = null;

    /** Unsorted list of subjects which are OWL ontologies, as they are rendered before other subjects. */
    private UnsortedTurtleResourceList unsortedOntologies = null;

    /** Sorted list of subjects which are OWL ontologies, as they are rendered before other subjects. */
    private SortedTurtleResourceList sortedOntologies = null;

    /** Unsorted list of blank nodes, as they are rendered separately from other nodes. */
    private UnsortedTurtleResourceList unsortedBlankNodes = null;

    /** Sorted list of blank nodes, as they are rendered separately from other nodes. */
    private SortedTurtleResourceList sortedBlankNodes = null;

    /** Map of serialisation names for blank nodes. */
    private HashMap<BNode, String> blankNodeNameMap = null;

    /** Unsorted hash map containing triple data. */
    private UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap = null;

    /** Sorted hash map containing triple data. */
    private SortedTurtleSubjectPredicateObjectMap sortedTripleMap = null;

    /** Predicates that are specially rendered before all others. */
    private ArrayList<URI> firstPredicates = null;

    /** Comparator for Strings that shorts longer strings first. */
    private class StringLengthComparator implements Comparator<String> {
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

    /** A reverse namespace table with which returns the longest namespace URIs first.  Key is URI string, value is prefix string. */
    private class ReverseNamespaceTable extends TreeMap<String,String> {
        public ReverseNamespaceTable() { super(new StringLengthComparator()); }
    }

    /** Reverse namespace table used to map URIs to prefixes.  Key is URI string, value is prefix string. */
    private ReverseNamespaceTable reverseNamespaceTable = null;

    /**
     * Creates an RDFWriter instance that will write sorted Turtle to the supplied output stream.
     *
     * @param out The OutputStream to write the Turtle to.
     */
    public SesameSortedTurtleWriter(OutputStream out) {
        assert out != null : "output stream cannot be null";
        this.output = new IndentingWriter(new OutputStreamWriter(out));
    }

    /**
     * Creates an RDFWriter instance that will write sorted Turtle to the supplied writer.
     *
     * @param writer The Writer to write the Turtle to.
     */
    public SesameSortedTurtleWriter(Writer writer) {
        assert writer != null : "output writer cannot be null";
        this.output = new IndentingWriter(writer);
    }

    /**
     * Creates an RDFWriter instance that will write sorted Turtle to the supplied output stream.
     *
     * @param out The OutputStream to write the Turtle to.
     * @param baseUri The base URI for the Turtel, or null.
     * @param indent The indentation string to use when formatting the Turtle output.
     */
    public SesameSortedTurtleWriter(OutputStream out, URI baseUri, String indent) {
        assert out != null : "output stream cannot be null";
        this.output = new IndentingWriter(new OutputStreamWriter(out));
        if (indent != null) { this.output.setIndentationString(indent); }
        this.baseUri = baseUri;
    }

    /**
     * Creates an RDFWriter instance that will write sorted Turtle to the supplied writer.
     *
     * @param writer The Writer to write the Turtle to.
     * @param baseUri The base URI for the Turtel, or null.
     * @param indent The indentation string to use when formatting the Turtle output, or null.
     */
    public SesameSortedTurtleWriter(Writer writer, URI baseUri, String indent) {
        assert writer != null : "output writer cannot be null";
        this.output = new IndentingWriter(writer);
        if (indent != null) { this.output.setIndentationString(indent); }
        this.baseUri = baseUri;
    }

    /**
     * Gets the RDF format that this RDFWriter uses.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.TURTLE;
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        output.setIndentationLevel(0);
        namespaceTable = new TreeMap<String, String>();
        unsortedOntologies = new UnsortedTurtleResourceList();
        unsortedBlankNodes = new UnsortedTurtleResourceList();
        blankNodeNameMap = new HashMap<BNode, String>();
        unsortedTripleMap = new UnsortedTurtleSubjectPredicateObjectMap();
    }

    /**
     * Adds a default namespace prefix to the namespace table, if no prefix has been defined.
     * @param namespaceUri The namespace URI.  Cannot be null.
     * @param defaultPrefix The default prefix to use, if no prefix is yet assigned.  Cannot be null.
     */
    private void addDefaultNamespacePrefixIfMissing(String namespaceUri, String defaultPrefix) {
        if ((namespaceUri != null) && (defaultPrefix != null)) {
            if (!namespaceTable.containsValue(namespaceUri)) {
                namespaceTable.put(defaultPrefix, namespaceUri);
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
            // Sort triples, etc.
            sortedOntologies = unsortedOntologies.toSorted();
            sortedTripleMap = unsortedTripleMap.toSorted();
            sortedBlankNodes = unsortedBlankNodes.toSorted();

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
            firstPredicates = new ArrayList<URI>(); // predicates that are specially rendered first
            firstPredicates.add(rdfType);
            firstPredicates.add(rdfsSubClassOf);
            firstPredicates.add(rdfsSubPropertyOf);
            firstPredicates.add(owlSameAs);
            firstPredicates.add(rdfsLabel);
            firstPredicates.add(rdfsComment);

            // Add default namespace prefixes, if they haven't yet been defined.  May fail if these prefixes have already been defined for different namespace URIs.
            addDefaultNamespacePrefixIfMissing(RDF_NS_URI, "rdf");
            addDefaultNamespacePrefixIfMissing(RDFS_NS_URI, "rdfs");
            addDefaultNamespacePrefixIfMissing(OWL_NS_URI, "owl");
            addDefaultNamespacePrefixIfMissing(XML_SCHEMA_NS_URI, "xs");

            // Create reverse namespace table.
            reverseNamespaceTable = new ReverseNamespaceTable();
            for (String prefix : namespaceTable.keySet()) {
                String uri = namespaceTable.get(prefix);
                reverseNamespaceTable.put(uri, prefix);
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

            // Write TopBraid-specific special comments, if any.
            if ((baseUri != null) || (importList.size() >= 1)) {
                // Write the baseURI, if any.
                if (baseUri != null) {
                    output.write("# baseURI: " + baseUri); output.writeEOL();
                }
                // Write ontology imports, if any.
                for (Value anImport : importList) {
                    output.write("# imports: " + anImport.stringValue()); output.writeEOL();
                }
                output.writeEOL();
            }

            // Write the baseURI, if any.
            if (baseUri != null) {
                output.write("@base <" + baseUri + "> ."); output.writeEOL();
            }

            // Write out prefixes and namespaces URIs.
            if (namespaceTable.size() > 0) {
                TreeSet<String> prefixes = new TreeSet<String>(namespaceTable.keySet());
                for (String prefix : prefixes) {
                    output.write("@prefix " + prefix + ": <" + namespaceTable.get(prefix) + "> ."); output.writeEOL();
                }
                output.writeEOL();
            }

            // Write out subjects which are unsortedOntologies.
            for (Resource subject : sortedOntologies) {
                if (!(subject instanceof BNode)) {
                    writeSubjectTriples(output, subject);
                }
            }

            // Write out all other subjects (not unsortedOntologies; also not blank nodes).
            for (Resource subject : sortedTripleMap.keySet()) {
                if (!sortedOntologies.contains(subject) && !(subject instanceof BNode)) {
                    writeSubjectTriples(output, subject);
                }
            }

            // Write out blank nodes that are subjects.
            for (Resource resource : sortedBlankNodes) {
                BNode bnode = (BNode)resource;
                if (unsortedTripleMap.containsKey(bnode)) {
                    writeSubjectTriples(output, bnode);
                }
            }

            output.flush();
        } catch (Throwable t) {
            throw new RDFHandlerException("unable to generate/write Turtle output", t);
        } finally {
        }
    }

    private void writeQName(IndentingWriter out, QName qname) throws Exception {
        if (qname == null) {
            out.write("null<QName>");
        } else if (qname.getPrefix() != null) {
            out.write(qname.getPrefix() + ":" + qname.getLocalPart());
        } else {
            out.write("<" + qname.getNamespaceURI() + qname.getLocalPart() + ">");
        }
    }

    private void writeSubjectTriples(IndentingWriter out, Resource subject) throws Exception {
        SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);
        QName qname = null; // try to write the subject out as a QName if possible.
        if (subject instanceof URI) {
            qname = convertUriToQName((URI)subject);
        }
        if (qname != null) {
            writeQName(out, qname);
        } else if (subject instanceof BNode) {
            out.write("_:" + blankNodeNameMap.get((BNode)subject));
        } else {
            out.write("<" + subject.stringValue() + ">");
        }
        out.writeEOL();
        out.increaseIndentation();

        // Write predicate/object pairs rendered first.
        for (URI predicate : firstPredicates) {
            if (poMap.containsKey(predicate)) {
                SortedTurtleObjectList values = poMap.get(predicate);
                writePredicateAndObjectValues(out, predicate, values);
            }
        }

        // Write other predicate/object pairs.
        for (URI predicate : poMap.keySet()) {
            if (!firstPredicates.contains(predicate)) {
                SortedTurtleObjectList values = poMap.get(predicate);
                writePredicateAndObjectValues(out, predicate, values);
            }
        }

        // Close statement
        out.write("."); out.writeEOL();

        out.decreaseIndentation();
        out.writeEOL(); // blank line
    }

    private void writePredicateAndObjectValues(IndentingWriter out, URI predicate, SortedTurtleObjectList values) throws Exception {
        writePredicate(out, predicate);
        if (values.size() == 1) {
            writeObject(out, values.first());
            out.write(";");
            out.writeEOL();
        } else if (values.size() > 1) {
            out.writeEOL();
            out.increaseIndentation();
            int numValues = values.size();
            int valueIndex = 0;
            for (Value value : values) {
                valueIndex += 1;
                writeObject(out, value);
                if (valueIndex < numValues) { out.write(","); }
                out.writeEOL();
            }
            out.write(";");
            out.writeEOL();
            out.decreaseIndentation();
        }
    }

    private void writePredicate(IndentingWriter out, URI predicate) throws Exception {
        writeUri(out, predicate);
    }

    /**
     * Converts a URI to a QName, if possible, given the available namespace prefixes.  Returns null if there is no match to a prefix.
     * @param uri The URI to convert to a QName, if possible.
     * @return The equivalent QName for the URI, or null if no equivalent.
     */
    private QName convertUriToQName(URI uri) {
        String uriString = uri.stringValue();
        for (String uriStem : reverseNamespaceTable.keySet()) {
            if ((uriString.length() > uriStem.length()) && uriString.startsWith(uriStem)) {
                String localPart = uriString.substring(uriStem.length());
                if (isPrefixedNameLocalPart(localPart)) { // to be a value QName, the 'local part' has to be valid
                    return new QName(uriStem, localPart, reverseNamespaceTable.get(uriStem));
                } else {
                    return null;
                }
            }
        }
        // Failed to find a match, return null.
        return null;
    }

    private void writeUri(IndentingWriter out, URI uri) throws Exception {
        if(rdfType.equals(uri)) {
            out.write("a ");
        } else {
            QName qname = convertUriToQName(uri); // write the URI out as a QName if possible.
            if (qname != null) {
                writeQName(out, qname);
                out.write(" ");
            } else { // write out the URI relative to the base URI, if possible.
                String uriString = uri.stringValue();
                if (baseUri != null) {
                    String baseUriString = baseUri.stringValue();
                    if ((uriString.length() > baseUriString.length()) && uriString.startsWith(baseUriString)) {
                        out.write("#" + uriString.substring(baseUriString.length()) + " ");
                        return;
                    }
                }
                out.write("<" + uri.stringValue() + "> ");
            }
        }
    }

    private void writeObject(IndentingWriter out, Value value) throws Exception {
        if (value instanceof BNode) {
            writeObject(out, (BNode) value);
        } else if (value instanceof URI) {
            writeObject(out, (URI)value);
        } else if (value instanceof Literal) {
            writeObject(out, (Literal)value);
        } else {
            out.write("\"" + value.stringValue() + "\"");
            out.write(" ");
        }
    }

    private void writeObject(IndentingWriter out, BNode bnode) throws Exception {
        if (unsortedTripleMap.containsKey(bnode)) {
            out.write("_:" + blankNodeNameMap.get(bnode) + " ");
        } else {
            out.write("[] ");
        }
    }

    private void writeObject(IndentingWriter out, URI uri) throws Exception {
        writeUri(out, uri);
    }

    private void writeObject(IndentingWriter out, Literal literal) throws Exception {
        if (literal == null) {
            out.write("null<Literal> ");
        } else if (literal.getLanguage() != null) {
            writeString(out, literal.stringValue());
            out.write("@" + literal.getLanguage() + " ");
        } else if (literal.getDatatype() != null) {
            writeString(out, literal.stringValue());
            out.write("^^");
            writeUri(out, literal.getDatatype());
        } else {
            writeString(out, literal.stringValue());
        }
    }

    private String escapeString(String str) {
        if (str == null) { return null; }
        return str.replaceAll("\\\\", "\\\\\\\\");
    }

    private boolean isMultilineString(String str) {
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

    private void writeString(IndentingWriter out, String str) throws Exception {
        if (str == null) { return; }
        if (isMultilineString(str)) { // multi-line string
            if (str.contains("\"")) { // string contains double quote chars
                if (str.contains("'")) { // string contains both single and double quote chars
                    out.write("\"\"\"");
                    out.write(escapeString(str).replaceAll("\"", "\\\\\""));
                    out.write("\"\"\"");
                } else { // string contains double quote chars but no single quote chars
                    out.write("'''");
                    out.write(escapeString(str));
                    out.write("'''");
                }
            } else { // string has no double quote chars
                out.write("\"\"\"");
                out.write(escapeString(str));
                out.write("\"\"\"");
            }
        } else { // single-line string
            if (str.contains("\"")) { // string contains double quote chars
                if (str.contains("'")) { // string contains both single and double quote chars
                    out.write("\"");
                    out.write(escapeString(str).replaceAll("\"", "\\\\\""));
                    out.write("\"");
                } else { // string contains double quote chars but no single quote chars
                    out.write("'");
                    out.write(escapeString(str));
                    out.write("'");
                }
            } else { // string has no double quote chars
                out.write("\"");
                out.write(escapeString(str));
                out.write("\"");
            }
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
        if (st.getPredicate().equals(rdfType) && st.getObject().equals(owlOntology)) {
            unsortedOntologies.add(st.getSubject());
        }

        // Note subjects & objects which are blank nodes.
        if (st.getSubject() instanceof BNode) {
            unsortedBlankNodes.add((BNode)st.getSubject());
        }
        if (st.getObject() instanceof BNode) {
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

    /** Whether the character is a "name character", as defined in the XML namespaces spec.  Characters above Unicode FFFF are included. */
    public boolean isNameChar(char ch) {
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

    /**
     * Whether the string is valid as the local part of a prefixed name, as defined in the RDF 1.1 Turtle spec.
     * Doesn't check that backslash escape sequences in the name are correctly formed.
     */
    public boolean isPrefixedNameLocalPart(String str) {
        if (str == null) return false;
        if (str.length() < 1) return false;
        if ((':' != str.charAt(0)) && !isNameChar(str.charAt(0))) return false; // cannot start with a colon
        for (int idx = 2; idx < str.length(); idx++) {
            if (!isNameChar(str.charAt(idx))) return false;
        }
        return true;
    }

}
