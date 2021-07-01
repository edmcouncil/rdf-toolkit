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
package org.edmcouncil.rdf_toolkit.io;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFParser;
import org.eclipse.rdf4j.rio.helpers.ContextStatementCollector;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.sail.memory.model.MemValueFactory;
import java.io.IOException;
import java.io.InputStream;

public class RdfModelReader {

  private final AbstractRDFParser parser;
  private final ValueFactory valueFactory;

  public RdfModelReader(RDFFormat rdfFormat) {
    this.valueFactory = new MemValueFactory();

    switch (rdfFormat.getName()) {
      case "RDF/XML":
        this.parser =  new RDFXMLParser(valueFactory);
        break;
      case "Turtle":
        this.parser = new TurtleParser(valueFactory);
        break;
      case "JSON-LD":
        this.parser = new JSONLDParser(valueFactory);
        break;
      default:
        throw new IllegalStateException("Not supported format: " + rdfFormat.getName());
    }
  }
  
  public Model read(InputStream sourceInputStream, String baseIriString) throws IOException {
    Model result = new LinkedHashModel();
    parser.setRDFHandler(new ContextStatementCollector(result, valueFactory));
    parser.parse(sourceInputStream, baseIriString);
    return result;
  }
}