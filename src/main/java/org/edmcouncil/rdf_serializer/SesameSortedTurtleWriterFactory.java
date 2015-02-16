package org.edmcouncil.rdf_serializer;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Factory class for creating Sesame writers which generate sorted Turtle.
 */
public class SesameSortedTurtleWriterFactory implements RDFWriterFactory {
    /**
     * Returns the RDF format for this factory.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.TURTLE;
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(OutputStream out) {
        return new SesameSortedTurtleWriter(out);
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(Writer writer) {
        return new SesameSortedTurtleWriter(writer);
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     * @param baseUri The base URI for the Turtel, or null.
     * @param indent The indentation string to use when formatting the Turtle output.
     */
    public RDFWriter getWriter(OutputStream out, URI baseUri, String indent) {
        return new SesameSortedTurtleWriter(out, baseUri, indent);
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     * @param baseUri The base URI for the Turtel, or null.
     * @param indent The indentation string to use when formatting the Turtle output.
     */
    public RDFWriter getWriter(Writer writer, URI baseUri, String indent) {
        return new SesameSortedTurtleWriter(writer, baseUri, indent);
    }
}
