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

package org.edmcouncil.rdf_toolkit.writer;

import static org.edmcouncil.rdf_toolkit.util.Constants.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.util.Constants.INLINE_BLANK_NODES;
import static org.edmcouncil.rdf_toolkit.util.Constants.LEADING_COMMENTS;
import static org.edmcouncil.rdf_toolkit.util.Constants.OVERRIDE_STRING_LANGUAGE;
import static org.edmcouncil.rdf_toolkit.util.Constants.SHORT_URI_PREF;
import static org.edmcouncil.rdf_toolkit.util.Constants.STRING_DATA_TYPE_OPTION;
import static org.edmcouncil.rdf_toolkit.util.Constants.TRAILING_COMMENTS;
import static org.edmcouncil.rdf_toolkit.util.Constants.USE_DTD_SUBSET;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.edmcouncil.rdf_toolkit.comparator.ComparisonContext;
import org.edmcouncil.rdf_toolkit.model.ReverseNamespaceTable;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.SortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleResourceList;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleSubjectPredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleBNodeList;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleResourceList;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleSubjectPredicateObjectMap;
import org.edmcouncil.rdf_toolkit.util.Constants;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.edmcouncil.rdf_toolkit.util.StringDataTypeOptions;
import org.edmcouncil.rdf_toolkit.util.TextUtils;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;

/**
 * Equivalent to Sesame's built-in RDF writer, but the triples are sorted into a consistent order.
 * In order to do the sorting, it must be possible to load all of the RDF statements into memory.
 * NOTE: comments are suppressed, as there isn't a clear way to sort them along with triples.
 */
public abstract class SortedRdfWriter extends AbstractRDFWriter {
    // TODO: add common methods for "eol and increase indent", "eol and decrease indent" and "eol with same indent" and
    //  refactor using these

    /** Preferred rdf:type values when rendering RDF. */
    protected static final List<IRI> preferredRdfTypes = Arrays.asList(
        Constants.owlNamedIndividual,
        Constants.owlDatatypeProperty,
        Constants.owlObjectProperty);

    /**
     * Contains data that is relevant in comparisons across different comparators.
     */
    protected ComparisonContext comparisonContext;

    /** Base IRI for the RDF output document. */
    protected IRI baseIri = null;

    /** Preference for prefix or base-IRI based IRI shortening. */
    protected ShortIriPreferences shortIriPreference = ShortIriPreferences.PREFIX;

    /** Whether to use a DTD subset to allow IRI shortening in RDF/XML */
    protected boolean useDtdSubset = false;

    /** Whether to inline blank nodes */
    protected boolean inlineBlankNodes = false;

    /** Leading comment lines */
    protected String[] leadingComments = null;

    /** Trailing comment lines */
    protected String[] trailingComments = null;

    /** String data type option */
    protected StringDataTypeOptions stringDataTypeOption = StringDataTypeOptions.IMPLICIT;

    /** Override string language */
    protected String overrideStringLanguage = null;

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
    protected List<IRI> firstPredicates = null;

    /**
     * Namespace mappings created by the serializer.
     */
    protected Map<String, String> generatedNamespaceTable = null;

    /** Reverse namespace table used to map IRIs to prefixes.  Key is IRI string, value is prefix string. */
    protected ReverseNamespaceTable reverseNamespaceTable = null;

    /** Output stream for this RDF writer. */
    protected Writer out;

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    protected SortedRdfWriter(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    protected SortedRdfWriter(Writer writer) {
        this(writer, new HashMap<>());
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     * @param options options for the RDF writer.
     */
    protected SortedRdfWriter(OutputStream out, Map<String, Object> options) {
        this(new OutputStreamWriter(out), options);
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     * @param options options for the RDF writer.
     */
    protected SortedRdfWriter(Writer writer, Map<String, Object> options) {
        if (writer == null) {
            throw new IllegalStateException("Writer object can't be null!");
        }
        this.out = writer;
        processOptions(options);
    }

    private void processOptions(Map<String, Object> options) {
        if (options.containsKey(BASE_IRI)) {
            this.baseIri = (IRI) options.get(BASE_IRI);
        }
        if (options.containsKey(SHORT_URI_PREF)) {
            this.shortIriPreference = (ShortIriPreferences) options.get(SHORT_URI_PREF);
        }
        if (options.containsKey(USE_DTD_SUBSET)) {
            this.useDtdSubset = (Boolean) options.get(USE_DTD_SUBSET);
        }
        if (options.containsKey(INLINE_BLANK_NODES)) {
            this.inlineBlankNodes = (Boolean) options.get(INLINE_BLANK_NODES);
        }
        if (options.containsKey(LEADING_COMMENTS)) {
            this.leadingComments = (String[]) options.get(LEADING_COMMENTS);
        }
        if (options.containsKey(TRAILING_COMMENTS)) {
            this.trailingComments = (String[]) options.get(TRAILING_COMMENTS);
        }
        if (options.containsKey(STRING_DATA_TYPE_OPTION)) {
            this.stringDataTypeOption = (StringDataTypeOptions) options.get(STRING_DATA_TYPE_OPTION);
        }
        if (options.containsKey(OVERRIDE_STRING_LANGUAGE)) {
            this.overrideStringLanguage = (String) options.get(OVERRIDE_STRING_LANGUAGE);
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
            String prefix = reverseNamespaceTable.get(iriStem);
            if ((iriString.length() > iriStem.length()) && iriString.startsWith(iriStem)) {
                String localPart = iriString.substring(iriStem.length());
                if (TextUtils.isPrefixedNameLocalPart(localPart)) { // to be a value QName, the 'local part' has to be valid
                    if (useGeneratedPrefixes || !generatedNamespaceTable.containsKey(prefix)) {
                        return new QName(iriStem, localPart, prefix);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else if (iriString.startsWith(String.format("%s:", prefix))) {
                return new QName(iriStem, iriString.substring(iriString.indexOf(':')+1), prefix);
            }
        }
        // Failed to find a match, return null.
        return null;
    }

    protected String convertIriToRelativeIri(IRI iri, boolean useTurtleQuoting) throws Exception {
        // Note: does not check that the baseIri doesn't terminate in the middle of some IRI of which it really isn't the base.
        if (baseIri != null) {
            String iriString = iri.stringValue();
            String baseIriString = baseIri.stringValue();
            String relativeIriString = (new URI(baseIriString)).relativize(new URI(iriString)).toString();
            String result = String.format("%s%s%s",
                    useTurtleQuoting ? "<" : "",
                    relativeIriString.length() >= 1 ? relativeIriString : iriString, // avoid zero-length relative IRIs
                    useTurtleQuoting ? ">" : ""
            );
            return result;
        }
        // Failed to find a match, return null.
        return String.format("%s%s%s",
                useTurtleQuoting ? "<" : "",
                iri.stringValue(),
                useTurtleQuoting ? ">" : ""
        );
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        allPredicates = new HashSet<>();
        namespaceTable = new TreeMap<>();
        generatedNamespaceTable = new TreeMap<>();
        unsortedOntologies = new UnsortedTurtleResourceList();
        unsortedBlankNodes = new UnsortedTurtleResourceList();
        blankNodeNameMap = new HashMap<>();
        unsortedTripleMap = new UnsortedTurtleSubjectPredicateObjectMap();
        objectBlankNodes = new UnsortedTurtleBNodeList();
        comparisonContext = new ComparisonContext(inlineBlankNodes, unsortedTripleMap);
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
            String namespaceIri = predicateString.substring(0, namespaceIriEndPos + 1);
            if (namespaceIri.length() >= 1) {
                if (!namespaceTable.containsValue(namespaceIri)) {
                    String newPrefix = "zzzns" + String.format("%04d", namespaceIndex); // TODO zzzns?
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
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            // !!!! Override method must set values for 'sortedOntologies', 'sortedTripleMap' & 'sortedBlankNodes' before calling this method

            // Create serialisation names for blank nodes.
            String blankNodeNamePadding = prepareBlankNodeNamePadding(unsortedBlankNodes.size());

            int blankNodeIndex = 0;
            for (Value value : sortedBlankNodes) {
                if (value instanceof BNode) {
                    BNode bnode = (BNode) value;
                    blankNodeIndex++;
                    String blankNodeName = Integer.toString(blankNodeIndex);
                    if (blankNodeName.length() < blankNodeNamePadding.length()) {
                        blankNodeName = blankNodeNamePadding.substring(0, blankNodeNamePadding.length() - blankNodeName.length()) + blankNodeName;
                    }
                    blankNodeName = "blank" + blankNodeName;
                    blankNodeNameMap.put(bnode, blankNodeName);
                }
            }

            populateListOfFirstPredicates();

            // Add default namespace prefixes, if they haven't yet been defined.  May fail if these prefixes have
            // already been defined for different namespace IRIs.
            addDefaultNamespacePrefixIfMissing(Constants.RDF_NS_URI, "rdf");
            addDefaultNamespacePrefixIfMissing(Constants.RDFS_NS_URI, "rdfs");
            addDefaultNamespacePrefixIfMissing(Constants.OWL_NS_URI, "owl");
            addDefaultNamespacePrefixIfMissing(Constants.XML_SCHEMA_NS_URI, "xs");

            // Add any extra namespaces needed to make all predicates writeable as a QName.  This is especially needed for RDF/XML.
            addNamespacePrefixesForPredicates();

            // Create reverse namespace table.
            reverseNamespaceTable = new ReverseNamespaceTable();
            for (String prefix : namespaceTable.keySet()) {
                String iri = namespaceTable.get(prefix);
                reverseNamespaceTable.put(iri, prefix);
            }

            // Create list of imports
            SortedTurtleObjectList importList = new SortedTurtleObjectList(comparisonContext);
            for (Resource subject : sortedOntologies) {
                if (sortedTripleMap.containsKey(subject)) {
                    SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);
                    if (poMap.containsKey(Constants.OWL_IMPORTS)) {
                        SortedTurtleObjectList importsOList = poMap.get(Constants.OWL_IMPORTS);
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
            for (Resource subject : sortedTripleMap.sortedKeys()) {
                if (!sortedOntologies.contains(subject) && !(subject instanceof BNode)) {
                    allSubjectCount++;
                }
            }
            for (Resource resource : sortedBlankNodes) {
                if (!inlineBlankNodes || !objectBlankNodes.contains(resource)) {
                    BNode bnode = (BNode) resource;
                    if (unsortedTripleMap.containsKey(bnode)) {
                        allSubjectCount++;
                    }
                }
            }

            // Write out subjects which are unsortedOntologies.
            // TODO Above comment is probably misleading. Here we write down triples by a subject
            int subjectCount = 0;
            for (Resource subject : sortedOntologies) {
                if (!(subject instanceof BNode)) {
                    subjectCount++;
                    writeSubjectTriples(out, subject);
                    if (subjectCount < allSubjectCount) {
                        writeSubjectSeparator(out);
                    }
                }
            }

            // Write out all other subjects (not unsortedOntologies; also not blank nodes).
            for (Resource subject : sortedTripleMap.sortedKeys()) {
                if (!sortedOntologies.contains(subject) && !(subject instanceof BNode)) {
                    subjectCount++;
                    writeSubjectTriples(out, subject);
                    if (subjectCount < allSubjectCount) { writeSubjectSeparator(out); }
                }
            }

            // Write out blank nodes that are subjects, if blank nodes are not being inlined or if the blank node is not an object.
            for (Resource resource : sortedBlankNodes) {
                if (!inlineBlankNodes || !objectBlankNodes.contains(resource)) {
                    BNode bnode = (BNode) resource;
                    if (unsortedTripleMap.containsKey(bnode)) {
                        subjectCount++;
                        writeSubjectTriples(out, bnode);
                        if (subjectCount < allSubjectCount) {
                            writeSubjectSeparator(out);
                        }
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

    private String prepareBlankNodeNamePadding(int numberOfBlankNodes) {
        StringBuilder blankNodeNamePaddingBuilder = new StringBuilder();
        blankNodeNamePaddingBuilder.append("0");
        int blankNodeCount = numberOfBlankNodes;
        while (blankNodeCount > 9) {
            blankNodeCount /= 10;
            blankNodeNamePaddingBuilder.append("0");
        }
        return blankNodeNamePaddingBuilder.toString();
    }

    private void populateListOfFirstPredicates() {
        // Set up list of predicates that appear first under their subjects.
        firstPredicates = new ArrayList<>(); // predicates that are specially rendered first
        firstPredicates.add(Constants.RDF_TYPE);
        firstPredicates.add(Constants.RDFS_SUB_CLASS_OF);
        firstPredicates.add(Constants.RDFS_SUB_PROPERTY_OF);
        firstPredicates.add(Constants.OWL_SAME_AS);
        firstPredicates.add(Constants.RDFS_LABEL);
        firstPredicates.add(Constants.RDFS_COMMENT);
        firstPredicates.add(Constants.OWL_ON_PROPERTY);
        firstPredicates.add(Constants.OWL_ON_CLASS);
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
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
        if (st.getPredicate().equals(Constants.RDF_TYPE) &&
            st.getObject().equals(Constants.owlOntology) &&
            !unsortedOntologies.contains((st.getSubject()))) {
            unsortedOntologies.add(st.getSubject());
        }

        // Note subjects & objects which are blank nodes.
        if (st.getSubject() instanceof BNode) {
            unsortedBlankNodes.add(st.getSubject());
        }
        if ((st.getObject() instanceof BNode) && !unsortedBlankNodes.contains(((BNode) st.getObject()))) {
            unsortedBlankNodes.add((BNode)st.getObject());
        }
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
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

    protected String convertVerbIriToString(IRI iri,
                                        boolean useGeneratedPrefixes,
                                        boolean useTurtleQuoting,
                                        boolean useJsonLdQuoting) throws Exception {
        if (Constants.RDF_TYPE.equals(iri)) {
            if (useTurtleQuoting) { return "a"; }
            if (useJsonLdQuoting) { return "@type"; }
        }
        return convertIriToString(iri, useGeneratedPrefixes,
                                  useTurtleQuoting, useJsonLdQuoting);
    }

    protected String convertIriToString(IRI iri,
                                        boolean useGeneratedPrefixes,
                                        boolean useTurtleQuoting,
                                        boolean useJsonLdQuoting) throws Exception {
        if (ShortIriPreferences.PREFIX.equals(shortIriPreference)) {
            // return the IRI out as a QName if possible.
            QName qname = convertIriToQName(iri, useGeneratedPrefixes);
            if (qname != null) {
                return convertQNameToString(qname, useTurtleQuoting);
            } else { // return the IRI relative to the base IRI, if possible.
                return convertIriToRelativeIri(iri, useTurtleQuoting);
            }
        }
        if (ShortIriPreferences.BASE_IRI.equals(shortIriPreference)) {
            // return the IRI relative to the base URI, if possible.
            String relativeIri = convertIriToRelativeIri(iri, useTurtleQuoting);

            // check if the relative URI is shortened, or not
            if (!relativeIri.contains(iri.stringValue())) {
                return relativeIri;
            } else {
                // return the IRI out as a QName if possible.
                QName qname = convertIriToQName(iri, useGeneratedPrefixes);
                if (qname != null) {
                    return convertQNameToString(qname, useTurtleQuoting);
                } else { // return the absolute IRI
                    return String.format("%s%s%s",
                            useTurtleQuoting ? "<" : "",
                            iri.stringValue(),
                            useTurtleQuoting ? ">" : ""
                    );
                }
            }
        }
        return String.format("%s%s%s",
                useTurtleQuoting ? "<" : "",
                iri.stringValue(),
                useTurtleQuoting ? ">" : ""
        ); // if nothing else, do this
    }

    /** Compares a sorted triple map to the unsorted triple map from which it was created,
     *  to check that no triples were lost.  Prints detailed information is a triple loss is detected.
     */
    protected void compareSortedToUnsortedTripleMap(SortedTurtleSubjectPredicateObjectMap sortedTripleMap,
                                                    UnsortedTurtleSubjectPredicateObjectMap unsortedTripleMap,
                                                    String label) {
        if (sortedTripleMap.fullSize() != unsortedTripleMap.fullSize()) {
            unsortedTripleMap.toSorted(Value.class, comparisonContext); // generate BN-to-BN debugging
            if (sortedTripleMap.size() != unsortedTripleMap.size()) {
                System.err.println("**** " + label + ": subjects unexpectedly lost or gained during sorting: " + sortedTripleMap.fullSize() + " != " + unsortedTripleMap.fullSize());
            }
        }
    }

    protected abstract void writeHeader(Writer out, SortedTurtleObjectList importList, String[] leadingComments)
        throws Exception;

    protected abstract void writeSubjectTriples(Writer out, Resource subject) throws Exception;

    protected abstract void writeSubjectSeparator(Writer out) throws Exception;

    protected abstract void writePredicateAndObjectValues(Writer out, IRI predicate, Collection<Value> values)
        throws Exception;

    protected abstract void writeFooter(Writer out, String[] trailingComments) throws Exception;
}