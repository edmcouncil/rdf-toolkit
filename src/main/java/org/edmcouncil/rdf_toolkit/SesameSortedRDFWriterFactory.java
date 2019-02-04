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

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Factory class for creating Sesame writers which generate sorted RDF.
 */
public class SesameSortedRDFWriterFactory implements RDFWriterFactory {

    public enum SourceFormats {
        auto("auto", "(select by filename)", null),
        binary("binary", null, RDFFormat.BINARY),
        json_ld("json-ld", "(JSON-LD)", RDFFormat.JSONLD),
        n3("n3", null, RDFFormat.N3),
        n_quads("n-quads", "(N-quads)", RDFFormat.NQUADS),
        n_triples("n-triples", "(N-triples)", RDFFormat.NTRIPLES),
        rdf_a("rdf-a", "(RDF/A)", RDFFormat.RDFA),
        rdf_json("rdf-json", "(RDF/JSON)", RDFFormat.RDFJSON),
        rdf_xml("rdf-xml", "(RDF/XML)", RDFFormat.RDFXML),
        trig("trig", "(TriG)", RDFFormat.TRIG),
        trix("trix", "(TriX)", RDFFormat.TRIX),
        turtle("turtle", "(Turtle)", RDFFormat.TURTLE);

        private static final SourceFormats defaultEnum = auto;

        private String optionValue = null;
        private String optionComment = null;
        private RDFFormat rdfFormat = null;

        SourceFormats(String optionValue, String optionComment, RDFFormat rdfFormat) {
            this.optionValue = optionValue;
            this.optionComment = optionComment;
            this.rdfFormat = rdfFormat;
        }

        public String getOptionValue() { return optionValue; }

        public String getOptionComment() { return optionComment; }

        public RDFFormat getRDFFormat() { return rdfFormat; }

        public static SourceFormats getByOptionValue(String optionValue) {
            if (optionValue == null) { return null; }
            for (SourceFormats sfmt : SourceFormats.values()) {
                if (optionValue.equals(sfmt.optionValue)) {
                    return sfmt;
                }
            }
            return null;
        }

        public static String summarise() {
            ArrayList<String> result = new ArrayList<String>();
            for (SourceFormats sfmt : SourceFormats.values()) {
                String value = sfmt.optionValue;
                if (sfmt.optionComment != null) {
                    value += " " + sfmt.optionComment;
                }
                if (defaultEnum.equals(sfmt)) {
                    value += " [default]";
                }
                result.add(value);
            }
            return String.join(", ", result);
        }

    }

    public enum TargetFormats {
        json_ld("json-ld", "(JSON-LD)", RDFFormat.JSONLD),
        rdf_xml("rdf-xml", "(RDF/XML)", RDFFormat.RDFXML),
        turtle("turtle", "(Turtle)", RDFFormat.TURTLE);

        private static final TargetFormats defaultEnum = turtle;

        private String optionValue = null;
        private String optionComment = null;
        private RDFFormat rdfFormat = null;

        TargetFormats(String optionValue, String optionComment, RDFFormat rdfFormat) {
            this.optionValue = optionValue;
            this.optionComment = optionComment;
            this.rdfFormat = rdfFormat;
        }

        public String getOptionValue() { return optionValue; }

        public String getOptionComment() { return optionComment; }

        public RDFFormat getRDFFormat() { return rdfFormat; }

        public static TargetFormats getByOptionValue(String optionValue) {
            if (optionValue == null) { return null; }
            for (TargetFormats tfmt : TargetFormats.values()) {
                if (optionValue.equals(tfmt.optionValue)) {
                    return tfmt;
                }
            }
            return null;
        }

        public static String summarise() {
            ArrayList<String> result = new ArrayList<String>();
            for (TargetFormats tfmt : TargetFormats.values()) {
                String value = tfmt.optionValue;
                if (tfmt.optionComment != null) {
                    value += " " + tfmt.optionComment;
                }
                if (defaultEnum.equals(tfmt)) {
                    value += " [default]";
                }
                result.add(value);
            }
            return String.join(", ", result);
        }
    }

    private TargetFormats targetFormat = TargetFormats.defaultEnum;

    public enum StringDataTypeOptions {
        explicit("explicit"),
        implicit("implicit");

        private static final StringDataTypeOptions defaultEnum = implicit;

        private String optionValue = null;

        StringDataTypeOptions(String optionValue) { this.optionValue = optionValue; }

        public String getOptionValue() { return optionValue; }

        public static StringDataTypeOptions getByOptionValue(String optionValue) {
            if (optionValue == null) { return null; }
            for (StringDataTypeOptions dataTypeOption : StringDataTypeOptions.values()) {
                if (optionValue.equals(dataTypeOption.optionValue)) {
                    return dataTypeOption;
                }
            }
            return null;
        }

        public static String summarise() {
            ArrayList<String> result = new ArrayList<String>();
            for (StringDataTypeOptions dataTypeOption : StringDataTypeOptions.values()) {
                String value = dataTypeOption.optionValue;
                if (defaultEnum.equals(dataTypeOption)) {
                    value += " [default]";
                }
                result.add(value);
            }
            return String.join(", ", result);
        }
    }

    /**
     * Default constructor for new factories.
     */
    public SesameSortedRDFWriterFactory() {}

    /**
     * Constructor for new factories which allows selection of the target (output) RDF format.
     *
     * @param rdfFormat target (output) RDF format
     */
    public SesameSortedRDFWriterFactory(TargetFormats rdfFormat) {
        targetFormat = rdfFormat;
    }

    /**
     * Returns the RDF format for this factory.
     */
    @Override
    public RDFFormat getRDFFormat() {
        if (targetFormat.getRDFFormat() == null) {
            return targetFormat.getRDFFormat();
        } else {
            return TargetFormats.defaultEnum.getRDFFormat();
        }
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(OutputStream out) {
        try {
            switch (targetFormat) {
                case json_ld:
                    return new SesameSortedJsonLdWriter(out);
                case rdf_xml:
                    return new SesameSortedRdfXmlWriter(out);
                case turtle:
                    return new SesameSortedTurtleWriter(out);
            }
            return new SesameSortedTurtleWriter(out); // Turtle by default
        } catch (Throwable t) {
            return null; // Sesame API doesn't allow us to throw an exception
        }
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(Writer writer) {
        try {
            switch (targetFormat) {
                case json_ld: return new SesameSortedJsonLdWriter(writer);
                case rdf_xml: return new SesameSortedRdfXmlWriter(writer);
                case turtle: return new SesameSortedTurtleWriter(writer);
            }
            return new SesameSortedTurtleWriter(writer); // Turtle by default
        } catch (Throwable t) {
            return null; // Sesame API doesn't allow us to throw an exception
        }
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output stream.
     *
     * @param out The OutputStream to write the RDF to.
     * @param options options for the RDF writer.
     */
    public RDFWriter getWriter(OutputStream out, Map<String, Object> options) throws Exception {
        switch (targetFormat) {
            case json_ld: return new SesameSortedJsonLdWriter(out, options);
            case rdf_xml: return new SesameSortedRdfXmlWriter(out, options);
            case turtle: return new SesameSortedTurtleWriter(out, options);
        }
        return new SesameSortedTurtleWriter(out, options); // Turtle by default
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     * @param options options for the RDF writer.
     */
    public RDFWriter getWriter(Writer writer, Map<String, Object> options) throws Exception {
        switch (targetFormat) {
            case json_ld: return new SesameSortedJsonLdWriter(writer, options);
            case rdf_xml: return new SesameSortedRdfXmlWriter(writer, options);
            case turtle: return new SesameSortedTurtleWriter(writer, options);
        }
        return new SesameSortedTurtleWriter(writer, options); // Turtle by default
    }
}
