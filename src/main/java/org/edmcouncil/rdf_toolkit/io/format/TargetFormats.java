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

public enum TargetFormats {
  JSON_LD("json-ld", "(JSON-LD)", RDFFormat.JSONLD),
  RDF_XML("rdf-xml", "(RDF/XML)", RDFFormat.RDFXML),
  TURTLE("turtle", "(Turtle)", RDFFormat.TURTLE);

  public static final TargetFormats DEFAULT_FORMAT = TURTLE;

  private final String optionValue;
  private final String optionComment;
  private final RDFFormat rdfFormat;

  TargetFormats(String optionValue, String optionComment, RDFFormat rdfFormat) {
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

  public static TargetFormats getByOptionValue(String optionValue) {
    if (optionValue == null) {
      return null;
    }
    for (TargetFormats tfmt : values()) {
      if (optionValue.equals(tfmt.optionValue)) {
        return tfmt;
      }
    }
    return null;
  }

  public static String summarise() {
    ArrayList<String> result = new ArrayList<>();
    for (TargetFormats tfmt : values()) {
      String value = tfmt.optionValue;
      if (tfmt.optionComment != null) {
        value += " " + tfmt.optionComment;
      }
      if (DEFAULT_FORMAT.equals(tfmt)) {
        value += " [default]";
      }
      result.add(value);
    }
    return String.join(", ", result);
  }
}