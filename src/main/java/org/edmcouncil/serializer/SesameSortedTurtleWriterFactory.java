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
package org.edmcouncil.serializer;

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
