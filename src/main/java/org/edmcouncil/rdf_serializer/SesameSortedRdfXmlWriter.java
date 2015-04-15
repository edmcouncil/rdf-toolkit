package org.edmcouncil.rdf_serializer;

import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;

/**
 * Equivalent to Sesame's built-in RDF/XML writer, but the triples are sorted into a consistent order.
 * In order to do the sorting, it must be possible to load all of the RDF statements into memory.
 * NOTE: comments are suppressed, as there isn't a clear way to sort them along with triples.
 */
public class SesameSortedRdfXmlWriter extends SesameSortedRDFWriter {
    // TODO: the 'out' parameter in 'write...' methods is not used, and should be refactored out of the code
    // TODO: many mehtods are not used, this means that SesameSortedRDFWriter should be refactored

    private static final Logger logger = LoggerFactory.getLogger(SesameSortedRdfXmlWriter.class);

    /** Output stream for this RDF/XML writer. */
    // Note: this is an internal Java class, not part of the published API.  But easier than writing our own indenter here.
    private IndentingXMLStreamWriter output = null;

    /** Namespace prefix for the RDF namespace. */
    private String rdfPrefix = "rdf";
    private String xmlPrefix = "xml";

    /**
     * Creates an RDFWriter instance that will write sorted RDF/XML to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF/XML to.
     */
    public SesameSortedRdfXmlWriter(OutputStream out) throws Exception {
        super(out);
        this.output = new IndentingXMLStreamWriter(out);
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF/XML to the supplied writer.
     *
     * @param writer The Writer to write the RDF/XML to.
     */
    public SesameSortedRdfXmlWriter(Writer writer) throws Exception {
        super(writer);
        this.output = new IndentingXMLStreamWriter(writer);
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF/XML to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF/XML to.
     * @param options options for the RDF/XML writer.
     */
    public SesameSortedRdfXmlWriter(OutputStream out, Map<String, Object> options) throws Exception {
        super(out, options);
        String indent = options.containsKey("indent") ? ((String) options.get("indent")) : null;
        this.output = new IndentingXMLStreamWriter(out, "UTF-8", indent);
    }

    /**
     * Creates an RDFWriter instance that will write sorted RDF/XML to the supplied writer.
     *
     * @param writer The Writer to write the RDF/XML to.
     * @param options options for the RDF/XML writer.
     */
    public SesameSortedRdfXmlWriter(Writer writer, Map<String, Object> options) throws Exception {
        super(writer, options);
        String indent = options.containsKey("indent") ? ((String) options.get("indent")) : null;
        this.output = new IndentingXMLStreamWriter(writer, indent);
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        super.startRDF();
    }

    protected void writeHeader(Writer out, SortedTurtleObjectList importList) throws Exception {
        // Get prefixes used for the XML
        rdfPrefix = reverseNamespaceTable.get(RDF_NS_URI);

        // Create a sorted list of namespace prefix mappings.
        TreeSet<String> prefixes = new TreeSet<String>(namespaceTable.keySet());

        // Write the XML prologue <?xml ... ?>
        output.writeStartDocument(output.getXmlEncoding(), "1.0");
        output.writeEOL();

        // Write the DTD subset, if required
        if (useDtdSubset) {
            output.startDTD("rdf:RDF");
            if (namespaceTable.size() > 0) {
                for (String prefix : prefixes) {
                    output.writeDtdEntity(prefix, namespaceTable.get(prefix));
                }
            }
            output.endDTD();
        }

        // Open the root element.
        output.writeStartElement(rdfPrefix, "RDF", RDF_NS_URI); // <rdf:RDF>

        // Write the baseURI, if any.
        if (baseUri != null) {
            output.writeAttribute("xml", XML_NS_URI, "base", baseUri.stringValue());
        }

        // Write the namespace declarations into the root element.
        if (namespaceTable.size() > 0) {
            for (String prefix : prefixes) {
                if (!"xml".equals(prefix)) {
                    output.writeNamespace(prefix, namespaceTable.get(prefix));
                }
            }
        } else { // create RDF namespace at a minimum
            output.writeNamespace(rdfPrefix, RDF_NS_URI);
        }

        output.writeCharacters(""); // force writing of closing angle bracket in root element open tag
        output.writeEOL(); // add extra EOL after root element

    }

    protected void writeSubjectTriples(Writer out, Resource subject) throws Exception {
        SortedTurtlePredicateObjectMap poMap = sortedTripleMap.get(subject);

        // Try to determine whether to use <rdf:Description> or an element based on rdf:type value.
        SortedTurtleObjectList subjectRdfTypes = poMap.get(rdfType); // needed to determine if a type can be used as the XML element name
        URI enclosingElementURI = rdfDescription; // default value
        QName enclosingElementQName = convertUriToQName(enclosingElementURI);
        boolean enclosingElementIsRdfType = false;
        if (subjectRdfTypes.size() == 1) {
            Value subjectRdfTypeValue = (Value) subjectRdfTypes.first();
            if (subjectRdfTypeValue instanceof URI) {
                QName subjectRdfTypeQName = convertUriToQName((URI) subjectRdfTypeValue);
                if (subjectRdfTypeQName != null) {
                    enclosingElementURI = (URI) subjectRdfTypeValue;
                    enclosingElementQName = subjectRdfTypeQName;
                    enclosingElementIsRdfType = true;
                }
            }
        }

        // Write enclosing element.
        // The variation used for "rdf:about", or "rdf:nodeID", depends on settings and also whether the subject is a blank node or not.
        output.writeStartElement(enclosingElementQName.getPrefix(), enclosingElementQName.getLocalPart(), enclosingElementQName.getNamespaceURI());
        if (subject instanceof BNode) {
            output.writeAttribute(reverseNamespaceTable.get(RDF_NS_URI), RDF_NS_URI, "nodeID", blankNodeNameMap.get((BNode) subject));
        } else if (subject instanceof URI) {
            output.startAttribute(reverseNamespaceTable.get(RDF_NS_URI), RDF_NS_URI, "about");
            QName subjectQName = convertUriToQName((URI)subject);
            if ((subjectQName != null) && (subjectQName.getPrefix() != null)) { // if a prefix is defined, write out the subject QName using an entity reference
                output.writeAttributeEntityRef(subjectQName.getPrefix());
                output.writeAttributeCharacters(((URI) subject).getLocalName());
            } else { // just write the whole subject URI
                output.writeAttributeCharacters(subject.toString());
            }
            output.endAttribute();
        } else {
            output.writeAttribute(reverseNamespaceTable.get(RDF_NS_URI), RDF_NS_URI, "about", subject.stringValue()); // this shouldn't occur, but ...
        }

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

        // Close enclosing element.
        output.writeEndElement();
        output.writeEOL();
    }

    protected void writePredicateAndObjectValues(Writer out, URI predicate, SortedTurtleObjectList values) throws Exception {
        // Get prefixes used for the XML
        rdfPrefix = reverseNamespaceTable.get(RDF_NS_URI);
        xmlPrefix = reverseNamespaceTable.get(XML_NS_URI);

        QName predicateQName = convertUriToQName(predicate);
        for (Value value : values) {
            if ((value instanceof BNode) || (value instanceof URI)) {
                output.writeEmptyElement(predicateQName.getPrefix(), predicateQName.getLocalPart(), predicateQName.getNamespaceURI());
            } else {
                output.writeStartElement(predicateQName.getPrefix(), predicateQName.getLocalPart(), predicateQName.getNamespaceURI());
            }
            if (value instanceof BNode) {
                output.writeAttribute(rdfPrefix, RDF_NS_URI, "nodeID", ((BNode)value).getID());
            } else if (value instanceof URI) {
                output.startAttribute(rdfPrefix, RDF_NS_URI, "resource");
                QName uriQName = convertUriToQName((URI) value);
                if (uriQName == null) {
                    output.writeAttributeCharacters(((URI) value).stringValue());
                } else {
                    output.writeAttributeEntityRef(uriQName.getPrefix());
                    output.writeAttributeCharacters(uriQName.getLocalPart());
                }
                output.endAttribute();
            } else if (value instanceof Literal) {
                if (((Literal)value).getDatatype() != null) {
                    output.startAttribute(rdfPrefix, RDF_NS_URI, "datatype");
                    QName datatypeQName = convertUriToQName(((Literal)value).getDatatype());
                    if (datatypeQName == null) {
                        output.writeAttributeCharacters(((Literal)value).getDatatype().stringValue());
                    } else {
                        output.writeAttributeEntityRef(datatypeQName.getPrefix());
                        output.writeAttributeCharacters(datatypeQName.getLocalPart());
                    }
                    output.endAttribute();
                }
                if (((Literal)value).getLanguage() != null) {
                    output.writeAttribute(xmlPrefix, XML_NS_URI, "lang", ((Literal)value).getLanguage());
                }
                output.writeCharacters(value.stringValue());
            } else {
                output.writeCharacters(value.stringValue());
            }
            output.writeEndElement();
        }
    }

    protected void writePredicate(Writer out, URI predicate) throws Exception {
        // TODO: not used
    }

    protected void writeQName(Writer out, QName qname) throws Exception {
        // TODO: not used
    }

    protected void writeUri(Writer out, URI uri) throws Exception {
        // TODO: not used
    }

    protected void writeObject(Writer out, Value value) throws Exception {
        // TODO: not used
    }

    protected void writeObject(Writer out, BNode bnode) throws Exception {
        // TODO: not used
    }

    protected void writeObject(Writer out, URI uri) throws Exception {
        // TODO: not used
    }

    protected void writeObject(Writer out, Literal literal) throws Exception {
        // TODO: not used
    }

    protected void writeString(Writer out, String str) throws Exception {
        // TODO: not used
    }

    protected void writeFooter(Writer out) throws Exception {
        output.writeEndElement(); // </rdf:RDF>
        output.writeEndDocument();
    }

}
