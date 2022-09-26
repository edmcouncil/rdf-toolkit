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

import java.util.Collections;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating Sesame writers which generate sorted RDF.
 */
public class SortedRdfWriterFactory implements RDFWriterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(SortedRdfWriterFactory.class);

  private TargetFormats targetFormat = TargetFormats.DEFAULT_FORMAT;

  /**
   * Default constructor for new factories.
   */
  public SortedRdfWriterFactory() {
  }

  /**
   * Constructor for new factories which allows selection of the target (output) RDF format.
   *
   * @param rdfFormat target (output) RDF format
   */
  public SortedRdfWriterFactory(TargetFormats rdfFormat) {
    targetFormat = rdfFormat;
  }

  /**
   * Returns the RDF format for this factory.
   */
  @Override
  public RDFFormat getRDFFormat() {
    return Objects
        .requireNonNullElse(targetFormat, TargetFormats.DEFAULT_FORMAT)
        .getRDFFormat();
  }

  /**
   * Returns an RDFWriter instance that will write to the supplied output stream.
   *
   * @param out The OutputStream to write the RDF to.
   */
  @Override
  public RDFWriter getWriter(OutputStream out) {
    try {
      return getOutputStreamByFormat(out, Collections.emptyMap());
    } catch (Exception ex) {
      LOGGER.warn(
          String.format(
              "Exception occurred while creating proper writer for targetFormat '%s' but RDF4J does not allow " +
                  "throwing exception here.", targetFormat.name()),
          ex);
      return null;
    }
  }

  @Override
  public RDFWriter getWriter(OutputStream out, String baseURI) {
    return getWriter(out);
  }

  /**
   * Returns an RDFWriter instance that will write to the supplied writer.
   *
   * @param writer The Writer to write the RDF to.
   */
  @Override
  public RDFWriter getWriter(Writer writer) {
    try {
      return getWriterByFormat(writer, Collections.emptyMap());
    } catch (Exception ex) {
      LOGGER.warn(
          String.format(
              "Exception occurred while creating proper writer for targetFormat '%s' but RDF4J does not allow " +
                  "throwing exception here.", targetFormat.name()),
          ex);
      return null;
    }
  }

  @Override
  public RDFWriter getWriter(Writer writer, String baseURI) {
    return getWriter(writer);
  }

  /**
   * Returns an RDFWriter instance that will write to the supplied output stream.
   *
   * @param out     The OutputStream to write the RDF to.
   * @param options options for the RDF writer.
   */
  public RDFWriter getWriter(OutputStream out, Map<String, Object> options) {
    return getOutputStreamByFormat(out, options);
  }

  /**
   * Returns an RDFWriter instance that will write to the supplied writer.
   *
   * @param writer  The Writer to write the RDF to.
   * @param options options for the RDF writer.
   */
  public RDFWriter getWriter(Writer writer, Map<String, Object> options) {
    return getWriterByFormat(writer, options);
  }

  private RDFWriter getWriterByFormat(Writer writer, Map<String, Object> options) {
    switch (targetFormat) {
      case JSON_LD:
        return new SortedJsonLdWriter(writer, options);
      case RDF_XML:
        return new SortedRdfXmlWriter(writer, options);
      case TURTLE:
        return new SortedTurtleWriter(writer, options);
    }
    return new SortedTurtleWriter(writer, options); // Turtle by default
  }

  private RDFWriter getOutputStreamByFormat(OutputStream out, Map<String, Object> options) {
    switch (targetFormat) {
      case JSON_LD:
        return new SortedJsonLdWriter(out, options);
      case RDF_XML:
        return new SortedRdfXmlWriter(out, options);
      case TURTLE:
        return new SortedTurtleWriter(out, options);
    }
    return new SortedTurtleWriter(out, options); // Turtle by default
  }
}
