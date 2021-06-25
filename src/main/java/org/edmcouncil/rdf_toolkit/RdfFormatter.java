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

import org.edmcouncil.rdf_toolkit.runner.RdfToolkitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * RDF formatter that formats in a consistent order, friendly for version control systems.
 * Should not be used with files that are too large to be fully loaded into memory for sorting.
 * Run with "--help" option for help.
 */
public class RdfFormatter {

  private static final Logger logger = LoggerFactory.getLogger(RdfFormatter.class);

  /**
   * Main method for running the RDF formatter. Run with "--help" option for help.
   */
  public static void main(String[] args) {
    try {
      run(args);
      System.exit(0);
    } catch (Exception ex) {
      logger.error("{}: stopped by unexpected exception: ", RdfFormatter.class.getSimpleName());
      logger.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
      var stackTraceWriter = new StringWriter();
      ex.printStackTrace(new PrintWriter(stackTraceWriter));
      logger.error(stackTraceWriter.toString());
      System.exit(1);
    }
  }

  /**
   * Main method, but throws exceptions for use from inside other Java code.
   */
  public static void run(String[] args) throws Exception {
    var rdfToolkitRunner = new RdfToolkitRunner();
    rdfToolkitRunner.run(args);
  }
}