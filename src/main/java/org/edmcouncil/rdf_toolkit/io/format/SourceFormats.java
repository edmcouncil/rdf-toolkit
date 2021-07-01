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

package org.edmcouncil.rdf_toolkit.io.format;

import org.eclipse.rdf4j.rio.RDFFormat;
import java.util.ArrayList;

public enum SourceFormats {
  AUTO("auto", "(select by filename)", null),
  BINARY("binary", null, RDFFormat.BINARY),
  JSON_LD("json-ld", "(JSON-LD)", RDFFormat.JSONLD),
  N3("n3", null, RDFFormat.N3),
  N_QUADS("n-quads", "(N-quads)", RDFFormat.NQUADS),
  N_TRIPLES("n-triples", "(N-triples)", RDFFormat.NTRIPLES),
  RDF_A("rdf-a", "(RDF/A)", RDFFormat.RDFA),
  RDF_JSON("rdf-json", "(RDF/JSON)", RDFFormat.RDFJSON),
  RDF_XML("rdf-xml", "(RDF/XML)", RDFFormat.RDFXML),
  TRIG("trig", "(TriG)", RDFFormat.TRIG),
  TRIX("trix", "(TriX)", RDFFormat.TRIX),
  TURTLE("turtle", "(Turtle)", RDFFormat.TURTLE);

  private static final SourceFormats defaultEnum = AUTO;

  private final String optionValue;
  private final String optionComment;
  private final RDFFormat rdfFormat;

  SourceFormats(String optionValue, String optionComment, RDFFormat rdfFormat) {
    this.optionValue = optionValue;
    this.optionComment = optionComment;
    this.rdfFormat = rdfFormat;
  }

  public String getOptionValue() {
    return optionValue;
  }

  public String getOptionComment() {
    return optionComment;
  }

  public RDFFormat getRDFFormat() {
    return rdfFormat;
  }

  public static SourceFormats getByOptionValue(String optionValue) {
    if (optionValue == null) {
      return null;
    }
    for (SourceFormats sfmt : values()) {
      if (optionValue.equals(sfmt.optionValue)) {
        return sfmt;
      }
    }
    return null;
  }

  public static String summarise() {
    ArrayList<String> result = new ArrayList<>();
    for (SourceFormats sfmt : values()) {
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