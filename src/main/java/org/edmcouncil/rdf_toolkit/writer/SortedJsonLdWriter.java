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

import static org.edmcouncil.rdf_toolkit.comparator.ComparisonUtils.getCollectionMembers;
import static org.edmcouncil.rdf_toolkit.comparator.ComparisonUtils.isCollection;
import static org.edmcouncil.rdf_toolkit.util.Constants.INDENT;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.edmcouncil.rdf_toolkit.model.SortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.SortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtleObjectList;
import org.edmcouncil.rdf_toolkit.model.UnsortedTurtlePredicateObjectMap;
import org.edmcouncil.rdf_toolkit.util.Constants;
import org.edmcouncil.rdf_toolkit.util.StringDataTypeOptions;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Equivalent to Sesame's built-in JSON-LD writer, but the triples are sorted into a consistent order.
 * In order to do the sorting, it must be possible to load all of the RDF statements into memory.
 * NOTE: comments are suppressed, as there isn't a clear way to sort them along with triples.
 */
public class SortedJsonLdWriter extends SortedRdfWriter {
    // TODO: check generated files for unnecessary blank lines, and find ways to remove them

    // no need to use namespace prefixes generated by the serializer for JSON-LD.
    private static final boolean useGeneratedPrefixes = false;

    // Turtle allows "values" in RDF collections
    private static final Class<Value> collectionClass = Value.class;

    /** Output stream for this JSON-LD writer. */
    private final IndentingWriter output;

    /**
     * Creates an RDFWriter instance that will write sorted JSON-LD to the supplied output stream.
     *
     * @param out The OutputStream to write the JSON-LD to.
     */
    public SortedJsonLdWriter(OutputStream out) {
        super(out);
        this.output = new IndentingWriter(new OutputStreamWriter(out));
        this.out = this.output;
    }

    /**
     * Creates an RDFWriter instance that will write sorted JSON-LD to the supplied writer.
     *
     * @param writer The Writer to write the JSON-LD to.
     */
    public SortedJsonLdWriter(Writer writer) {
        super(writer);
        this.output = new IndentingWriter(writer);
        this.out = this.output;
    }

    /**
     * Creates an RDFWriter instance that will write sorted JSON-LD to the supplied output stream.
     *
     * @param out The OutputStream to write the JSON-LD to.
     * @param options options for the JSON-LD writer.
     */
    public SortedJsonLdWriter(OutputStream out, Map<String, Object> options) {
        super(out, options);
        this.output = new IndentingWriter(new OutputStreamWriter(out));
        this.out = this.output;
        if (options.containsKey(INDENT)) {
            this.output.setIndentationString((String) options.get(INDENT));
        }
    }

    /**
     * Creates an RDFWriter instance that will write sorted JSON-LD to the supplied writer.
     *
     * @param writer The Writer to write the JSON-LD to.
     * @param options options for the JSON-LD writer.
     */
    public SortedJsonLdWriter(Writer writer, Map<String, Object> options) {
        super(writer, options);
        this.output = new IndentingWriter(writer);
        this.out = this.output;
        if (options.containsKey(INDENT)) {
            this.output.setIndentationString((String) options.get(INDENT));
        }
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        super.startRDF();
        output.setIndentationLevel(0);
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
            // Sort triples, etc.
            sortedOntologies = unsortedOntologies.toSorted(collectionClass, comparisonContext);
            if (sortedOntologies.size() != unsortedOntologies.size()) {
                System.err.printf("**** ontologies unexpectedly lost or gained during sorting: %d != %d%n",
                    sortedOntologies.size(),
                    unsortedOntologies.size());
                System.err.flush();
            }

            sortedTripleMap = unsortedTripleMap.toSorted(collectionClass, comparisonContext);
            compareSortedToUnsortedTripleMap(sortedTripleMap, unsortedTripleMap, "JSON-LD"); // TODO

            sortedBlankNodes = unsortedBlankNodes.toSorted(collectionClass, comparisonContext);
            if (sortedBlankNodes.size() != unsortedBlankNodes.size()) {
                System.err.printf("**** blank nodes unexpectedly lost or gained during sorting: %d != %d%n",
                    sortedBlankNodes.size(),
                    unsortedBlankNodes.size());
                System.err.flush();
            }

            super.endRDF();
        } catch (Exception ex) {
            throw new RDFHandlerException("unable to generate/write RDF output", ex);
        }
    }

    protected void writeHeader(Writer out, SortedTurtleObjectList importList, String[] leadingComments)
        throws Exception {
        // Process leading comments, if any.
        if ((leadingComments != null) && (leadingComments.length >= 1)) {
            System.err.println("#### leading comments ignored - JSON-LD does not support comments");
            System.err.flush();
        }

        // Open list of subject triples
        output.write("[");
        output.writeEOL();
        output.increaseIndentation();
    }

    protected void writeSubjectSeparator(Writer out) throws Exception {
        out.write(",");
        if (out instanceof IndentingWriter) {
            IndentingWriter intendedOutput = (IndentingWriter)out;
            intendedOutput.writeEOL();
        } else {
            out.write("\n");
        }
    }

    private boolean isOntology(Resource subject) {
        UnsortedTurtlePredicateObjectMap poMap = unsortedTripleMap.get(subject);
        if (poMap == null) { return false; }
        UnsortedTurtleObjectList types = poMap.get(Constants.RDF_TYPE);
        if (types == null) { return false; }
        for (Value type : types) {
            if (Constants.owlOntology.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getSubjectPrefixes(Resource subject) {
        final Set<String> prefixes = new HashSet<>();

        // Get subject prefix
        if (subject instanceof IRI) {
            QName subjectQName = convertIriToQName((IRI)subject, useGeneratedPrefixes);
            if (subjectQName != null) {
                prefixes.add(subjectQName.getPrefix());
            }
        }

        // Get predicate & value prefixes
        SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);
        if (poMap != null) {
            for (IRI predicate : poMap.sortedKeys()) {
                QName predicateQName = convertIriToQName(predicate, useGeneratedPrefixes);
                if (predicateQName != null) {
                    prefixes.add(predicateQName.getPrefix());
                }

                SortedTurtleObjectList values = poMap.get(predicate);
                if (values != null) {
                    for (Value value : values) {
                        if (value instanceof IRI) {
                            QName valueQName = convertIriToQName((IRI)value, useGeneratedPrefixes);
                            if (valueQName != null) {
                                prefixes.add(valueQName.getPrefix());
                            }
                        }
                        if (inlineBlankNodes && (value instanceof BNode)) {
                            prefixes.addAll(getSubjectPrefixes((BNode)value));
                        }
                    }
                }
            }
        }

        return prefixes;
    }

    protected void writeSubjectTriples(Writer out, Resource subject) throws Exception {
        SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);
        if (poMap == null) {
            poMap = new SortedTurtlePredicateObjectMap();
        }

        out.write("{");
        if (out instanceof IndentingWriter) {
            IndentingWriter intendedOutput = (IndentingWriter)out;
            intendedOutput.writeEOL();
            intendedOutput.increaseIndentation();
        } else {
            out.write("\n");
        }
        out.write("\"@id\" : \"");
        if (subject instanceof BNode) {
            out.write("_:" + blankNodeNameMap.get(subject));
        } else {
            writeIri(out, (IRI) subject);
        }
        out.write("\"");
        if (poMap.size() > 0) {
            out.write(",");
        }
        if (out instanceof IndentingWriter) {
            IndentingWriter intendedOutput = (IndentingWriter)out;
            intendedOutput.writeEOL();
        } else {
            out.write("\n");
        }

        // Write predicate/object pairs rendered first.
        for (IRI predicate : firstPredicates) {
            if (poMap.containsKey(predicate)) {
                SortedTurtleObjectList values = poMap.get(predicate);
                // make a copy so we don't delete anything from the original
                if (values != null) {
                    values = (SortedTurtleObjectList) values.clone();
                }
                List<Value> valuesList = new ArrayList<>();
                if (! values.isEmpty()) {
                    if (predicate == Constants.RDF_TYPE) {
                        for (IRI preferredType : preferredRdfTypes) {
                            if (values.contains(preferredType)) {
                                valuesList.add(preferredType);
                                values.remove(preferredType);
                            }
                        }
                    }

                    valuesList.addAll(values);
                }
                if (! valuesList.isEmpty()) {
                    writePredicateAndObjectValues(out, predicate, valuesList);
                    out.write(",");
                    if (out instanceof IndentingWriter) {
                        IndentingWriter intendedOutput = (IndentingWriter) out;
                        intendedOutput.writeEOL();
                    } else {
                        out.write("\n");
                    }
                }
            }
        }

        // Write other predicate/object pairs.
        for (IRI predicate : poMap.sortedKeys()) {
            if (!firstPredicates.contains(predicate)) {
                SortedTurtleObjectList values = poMap.get(predicate);
                writePredicateAndObjectValues(out, predicate, values);
                out.write(",");
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter) out;
                    output.writeEOL();
                } else {
                    out.write("\n");
                }
            }
        }

        // Write context
        Set<String> prefixes = getSubjectPrefixes(subject);
        out.write("\"@context\" : {");
        if (out instanceof IndentingWriter) {
            IndentingWriter output = (IndentingWriter)out;
            output.writeEOL();
            output.increaseIndentation();
        } else {
            out.write("\n");
        }

        // For an ontology, add the base URI to the context.
        if (isOntology(subject)) {
            if (baseIri != null) {
                out.write("\"@base\" : \"" + baseIri + "\"");
                if (! prefixes.isEmpty()) {
                    out.write(",");
                }
            }
            if (out instanceof IndentingWriter) {
                IndentingWriter intendedOutput = (IndentingWriter)out;
                intendedOutput.writeEOL();
            } else {
                out.write("\n");
            }
        }

        int prefixCount = 0;
        for (String prefix : prefixes) {
            prefixCount++;
            out.write("\"" + prefix + "\" : \"" + namespaceTable.get(prefix) + "\"");
            if (prefixCount < prefixes.size()) { out.write(","); }
            if (out instanceof IndentingWriter) {
                IndentingWriter intendedOutput = (IndentingWriter)out;
                intendedOutput.writeEOL();
            } else {
                out.write("\n");
            }
        }

        if (out instanceof IndentingWriter) {
            IndentingWriter intendedOutput = (IndentingWriter)out;
            intendedOutput.decreaseIndentation();
            out.write("}");
            intendedOutput.writeEOL();
        } else {
            out.write("}\n");
        }

        // Close statement
        if (out instanceof IndentingWriter) {
            IndentingWriter intendedOutput = (IndentingWriter)out;
            intendedOutput.decreaseIndentation();
            out.write("}");
        } else {
            out.write("}");
        }
    }

    private String convertIriToString(IRI iri) throws Exception {
        return convertIriToString(iri, useGeneratedPrefixes, false, true);
    }

    protected void writePredicateAndObjectValues(Writer out, IRI predicate, Collection<Value> values) throws Exception {
        final boolean isRdfTypePredicate = Constants.RDF_TYPE.equals(predicate);
        out.write("\"");
        writePredicate(out, predicate);
        out.write("\" : ");
        if (values.size() == 1) {
            Object value = values.toArray()[0];
            if (isRdfTypePredicate) {
                writeObject(out, (IRI) value, true);
            } else {
                writeObject(out, (Value) value);
            }
        } else if (values.size() > 1) {
            out.write("[");
            if (out instanceof IndentingWriter) {
                IndentingWriter intendedOutput = (IndentingWriter)out;
                intendedOutput.writeEOL();
                intendedOutput.increaseIndentation();
            } else {
                out.write("\n");
            }
            int numValues = values.size();
            int valueIndex = 0;
            for (Value value : values) {
                valueIndex += 1;
                if (isRdfTypePredicate) {
                    writeObject(out, (IRI)value, true);
                } else {
                    writeObject(out, value);
                }
                if (valueIndex < numValues) { out.write(","); }
                if (out instanceof IndentingWriter) {
                    IndentingWriter intendedOutput = (IndentingWriter)out;
                    intendedOutput.writeEOL();
                } else {
                    out.write("\n");
                }
            }
            if (out instanceof IndentingWriter) {
                IndentingWriter intendedOutput = (IndentingWriter)out;
                intendedOutput.writeEOL();
                intendedOutput.decreaseIndentation();
            } else {
                out.write("\n");
            }
            out.write("]");
        }
    }

    protected void writePredicate(Writer out, IRI predicate) throws Exception {
        out.write(convertVerbIriToString(predicate, useGeneratedPrefixes,
                                         false, true));
    }

    protected void writeIri(Writer out, IRI iri) throws Exception {
        out.write(convertIriToString(iri));
    }

    protected void writeObject(Writer out, Value value) throws Exception {
        if (value instanceof BNode) {
            writeObject(out, (BNode) value);
        } else if (value instanceof IRI) {
            writeObject(out, (IRI)value);
        } else if (value instanceof Literal) {
            writeObject(out, (Literal)value);
        } else {
            out.write("\"" + value.stringValue() + "\"");
        }
    }

    protected void writeObject(Writer out, BNode bnode) throws Exception {
        if (inlineBlankNodes) {
            if (isCollection(comparisonContext, bnode, collectionClass)) {
                // Open braces
                out.write("{");
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.writeEOL();
                    output.increaseIndentation();
                } else {
                    out.write("\n");
                }

                // Write collection members
                out.write("\"@list\" : [");
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.writeEOL();
                    output.increaseIndentation();
                } else {
                    out.write("\n");
                }
                List<Value> members = getCollectionMembers(unsortedTripleMap, bnode, collectionClass, comparisonContext);
                int memberIndex = 0;
                for (Value member : members) {
                    memberIndex++;
                    writeObject(out, member);
                    if (memberIndex < members.size()) {
                        out.write(",");
                        if (out instanceof IndentingWriter) {
                            IndentingWriter output = (IndentingWriter)out;
                            output.writeEOL();
                        } else {
                            out.write("\n");
                        }
                    }
                }
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.writeEOL();
                    output.decreaseIndentation();
                } else {
                    out.write("\n");
                }
                out.write("]");

                // Close braces
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.decreaseIndentation();
                    out.write("}");
                } else {
                    out.write("}");
                }
            } else { // not a collection
                SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(bnode);
                if (poMap == null) { poMap = new SortedTurtlePredicateObjectMap(); }

                // Open braces
                out.write("{");
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.writeEOL();
                    output.increaseIndentation();
                } else {
                    out.write("\n");
                }

                // Write predicate/object pairs rendered first.
                int predicateIndex = 0;
                for (IRI predicate : firstPredicates) {
                    if (poMap.containsKey(predicate)) {
                        predicateIndex++;
                        SortedTurtleObjectList values = poMap.get(predicate);
                        writePredicateAndObjectValues(out, predicate, values);
                        if (predicateIndex < poMap.size()) {
                            out.write(",");
                        }
                        if (out instanceof IndentingWriter) {
                            IndentingWriter output = (IndentingWriter)out;
                            output.writeEOL();
                        } else {
                            out.write("\n");
                        }
                    }
                }

                // Write other predicate/object pairs.
                for (IRI predicate : poMap.sortedKeys()) {
                    if (!firstPredicates.contains(predicate)) {
                        predicateIndex++;
                        SortedTurtleObjectList values = poMap.get(predicate);
                        writePredicateAndObjectValues(out, predicate, values);
                        if (predicateIndex < poMap.size()) {
                            out.write(",");
                        }
                        if (out instanceof IndentingWriter) {
                            IndentingWriter output = (IndentingWriter)out;
                            output.writeEOL();
                        } else {
                            out.write("\n");
                        }
                    }
                }

                // Close braces
                if (out instanceof IndentingWriter) {
                    IndentingWriter output = (IndentingWriter)out;
                    output.decreaseIndentation();
                    out.write("}");
                } else {
                    out.write("}");
                }
            }
        } else { // no inlining of blank nodes
            if (unsortedTripleMap.containsKey(bnode)) {
                out.write("{ \"@id\" : \"_:" + blankNodeNameMap.get(bnode) + "\" }");
            } else {
                System.out.println("**** blank node not a subject: " + bnode.stringValue()); System.out.flush();
                out.write("{ }"); // last resort - this should never happen
            }
        }
    }

    protected void writeObject(Writer out, IRI iri) throws Exception {
        writeObject(out, iri, false);
    }

    protected void writeObject(Writer out, IRI iri, boolean isRdfType) throws Exception {
        out.write(isRdfType ? "\"" : "{ \"@id\" : \"");
        writeIri(out, iri);
        out.write(isRdfType ? "\"" : "\" }");
    }

    protected void writeObject(Writer out, Literal literal) throws Exception {
        if (literal == null) {
            out.write("null<Literal>");
        } else if (literal.getLanguage().isPresent() || ((overrideStringLanguage != null) && (literal.getDatatype().stringValue().equals(Constants.xsString.stringValue())))) {
            out.write("{");
            if (out instanceof IndentingWriter) {
                var indentingWriter = (IndentingWriter) out;
                indentingWriter.writeEOL();
                indentingWriter.increaseIndentation();
            } else {
                out.write("\n");
            }

            String lang = overrideStringLanguage == null ?
                literal.getLanguage().orElse(overrideStringLanguage) :
                overrideStringLanguage;

            out.write("\"@language\" : \"" + lang + "\",");
            if (out instanceof IndentingWriter) {
                var output = (IndentingWriter) out;
                output.writeEOL();
            } else {
                out.write("\n");
            }

            out.write("\"@value\" : \"" + escapeString(literal.stringValue()) + "\"");
            if (out instanceof IndentingWriter) {
                var indentingWriter = (IndentingWriter) out;
                indentingWriter.writeEOL();
            } else {
                out.write("\n");
            }

            if (out instanceof IndentingWriter) {
                var indentingWriter = (IndentingWriter) out;
                indentingWriter.decreaseIndentation();
                out.write("}");
            } else {
                out.write("}");
            }
        } else if (literal.getDatatype() != null) {
            boolean useExplicit = (stringDataTypeOption == StringDataTypeOptions.EXPLICIT) || !(Constants.xsString.equals(literal.getDatatype()) || Constants.rdfLangString.equals(literal.getDatatype()));
            if (useExplicit) {
                out.write("{");
                if (out instanceof IndentingWriter) {
                    var indentingWriter = (IndentingWriter) out;
                    indentingWriter.writeEOL();
                    indentingWriter.increaseIndentation();
                } else {
                    out.write("\n");
                }

                out.write("\"@type\" : \"");
                writeIri(out, literal.getDatatype());
                out.write("\",");
                if (out instanceof IndentingWriter) {
                    var indentingWriter = (IndentingWriter) out;
                    indentingWriter.writeEOL();
                } else {
                    out.write("\n");
                }

                out.write("\"@value\" : ");
                writeString(out, literal.stringValue());

                if (out instanceof IndentingWriter) {
                    var indentingWriter = (IndentingWriter) out;
                    indentingWriter.decreaseIndentation();
                    indentingWriter.writeEOL();
                    out.write("}");
                } else {
                    out.write("\n}");
                }
            } else {
                writeString(out, literal.stringValue());
            }
        } else {
            writeString(out, literal.stringValue());
        }
    }

    protected void writeString(Writer out, String str) throws Exception {
        // Note that JSON does not support multi-line strings, unlike Turtle
        if (str == null) { return; }
        out.write("\"");
        out.write(escapeString(str));
        out.write("\"");
    }

    protected void writeFooter(Writer out, String[] trailingComments) throws Exception {
        // Write closing bracket for subject list.
        output.writeEOL();
        output.decreaseIndentation();
        output.write("]");
        output.writeEOL();

        // Process trailing comments, if any.
        if ((trailingComments != null) && (trailingComments.length >= 1)) {
            System.err.println("#### trailing comments ignored - JSON-LD does not support comments");
            System.err.flush();
        }
    }

    private String escapeString(String str) { // JSON does not support multi-line strings, different to Turtle
        final char SPACE = ' ';
        final char UNESCAPED_BACKSLASH = '\\';
        if (str == null) { return null; }
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (ch < SPACE) {
                sb.append(UNESCAPED_BACKSLASH);
                sb.append('u');
                sb.append(String.format("%04x", (short)ch));
            } else {
                switch (ch) {
                    case '\n': sb.append(UNESCAPED_BACKSLASH); sb.append('n'); break;
                    case '\r': sb.append(UNESCAPED_BACKSLASH); sb.append('r'); break;
                    case '"': sb.append(UNESCAPED_BACKSLASH); sb.append('"'); break;
                    case '\\': sb.append(UNESCAPED_BACKSLASH); sb.append(UNESCAPED_BACKSLASH); break;
                    default: sb.append(ch);
                }
            }
        }
        return sb.toString();
    }
}