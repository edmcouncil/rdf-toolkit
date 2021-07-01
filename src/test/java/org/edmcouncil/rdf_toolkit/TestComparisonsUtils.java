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

import static org.edmcouncil.rdf_toolkit.FileSystemUtils.readFileLines;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TestComparisonsUtils {

  private static final Map<IRI, String> EXPANDED_QNAMES = new HashMap<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(TestComparisonsUtils.class);

  public static boolean compareFiles(File file1, File file2, String encoding) throws IOException {
    // MAYBE: Log debugging info
    var source1Lines = readFileLines(file1, encoding).iterator();
    var source2Lines = readFileLines(file2, encoding).iterator();
    return compareStringIterators(source1Lines, source2Lines, file1, file2);
  }

  /**
   * Returns the base IRI if the line appears to contain a base IRI declaration.
   */
  public static Optional<String> getBaseIri(String line) {
    if (line.startsWith("# baseURI: ")) { // Turtle - TopBraid Composer
      return Optional.of(line.substring("# baseURI: ".length()));
    } else if (line.startsWith("@base ")) { // Turtle
      return Optional.of(line.replaceAll("@base\\s+<([^>]+)>.*", "$1"));
    } else if (line.contains("xml:base")) { // RDF/XML
      return Optional.of(line.replaceAll(".*xml:base\\s*=\\s*[\"']([^\"']+)[\"'].*", "$1"));
    } else if (line.contains("\"@base\"")) { // JSON-LD
      return Optional.of(line.replaceAll(".*\"@base\"\\s*:\\s*\"([^\"]+)\".*", "$1"));
    } else {
      return Optional.empty();
    }
  }

  public static void assertTriplesMatch(Model model1, Model model2) {
    var maxWarnings = 3;

    var unmatchedTriples1to2 = new HashSet<Statement>();

    for (var st1 : model1) { // for each triple in model1, does it exist in model2?
      var triplesMatch1to2 = false;
      for (var st2 : model2) {
        if (!triplesMatch1to2) {
          if (triplesMatch(st1, st2, model1.getNamespaces(), model2.getNamespaces())) {
            triplesMatch1to2 = true;
          }
        }
      }

      // TODO: Add debugging logs
      if (!triplesMatch1to2) {
        unmatchedTriples1to2.add(st1);
        if (unmatchedTriples1to2.size() <= maxWarnings) {
          for (var st2Alt : model2) {
            if ((st1.getSubject() == st2Alt.getSubject()) ||
                (st1.getSubject() instanceof BNode && st2Alt.getSubject() instanceof BNode) ||
                (st1.getSubject() instanceof IRI && st2Alt.getSubject() instanceof IRI &&
                    (expandQNameToFullIriString(asInstanceOf(st1.getSubject(), IRI.class), model1.getNamespaces()))
                        .equals(expandQNameToFullIriString(asInstanceOf(st2Alt.getSubject(), IRI.class), model2.getNamespaces())))) {
              if ((st1.getPredicate() == st2Alt.getPredicate() ||
                  (expandQNameToFullIriString(st1.getPredicate(), model1.getNamespaces())
                      .equals(expandQNameToFullIriString(st2Alt.getPredicate(), model2.getNamespaces()))))) {
                LOGGER.info("Possible object match: {}", st2Alt.getObject().stringValue());
              }
            }
          }
        }
      }
    }

    var unmatchedTriples2to1 = new HashSet<Statement>();
    for (var st2 : model2) { // for each triple in model2, does it exist in model1?
      var triplesMatch2to1 = false;
      for (var st1Alt : model1) {
        if (!triplesMatch2to1) {
          if (triplesMatch(st2, st1Alt, model1.getNamespaces(), model2.getNamespaces())) {
            triplesMatch2to1 = true;
          }
        }
      }

      if (!triplesMatch2to1) {
        unmatchedTriples2to1.add(st2);
        if (unmatchedTriples2to1.size() <= maxWarnings) {
          System.out.println("unmatched triple 2 to 1 [" + unmatchedTriples2to1.size() + ": " + st2 + "]");
        }
      }
    }

    assertTrue(
        unmatchedTriples1to2.isEmpty() && unmatchedTriples2to1.isEmpty(),
        String.format(
            "found unmatched triples: [%d], [%d]",
            unmatchedTriples1to2.size() / model1.size(),
            unmatchedTriples2to1.size() / model2.size()));
  }

  public static <T> T asInstanceOf(Object resource, Class<T> instanceType) {
    return instanceType.cast(resource);
  }

  private static boolean compareStringIterators(Iterator<String> iter1, Iterator<String> iter2, File file1, File file2) {
    var lineCount = 0;
    while (iter1.hasNext() || iter2.hasNext()) {
      lineCount += 1;
      if (!iter2.hasNext()) {
        LOGGER.error("Left file [{}] has more lines than right [{}] ({}+): {}",
            file1.getName(), file2.getName(), lineCount, iter1.next());
        return false;
      }
      if (!iter1.hasNext()) {
        LOGGER.error("Right file [{}] has more lines than left [{}] ({}+): {}",
            file2.getName(), file1.getName(), lineCount, iter2.next());
        return false;
      }
      var line1 = iter1.next();
      var line2 = iter2.next();
      if (!compareStrings(line1, line2, lineCount, file1, file2)) {
        LOGGER.error("line1 ({}): {}\nline2 ({}): {}", file1.getName(), line1, file2.getName(), line2);
        return false;
      }
    }
    return true;
  }

  private static boolean compareStrings(String str1, String str2, int lineCount, File file1, File file2) {
    if (str1.length() >= 1 || str2.length() >= 1) {
      var index = 0;
      while (str1.length() > index || str2.length() > index) {
        if (str2.length() <= index) {
          LOGGER.error("Left line [{}] ({}) is longer than [{}] ({}).\n\tLeft: {}\n\tRight: {}",
              file1.getName(), str1.length(), file2.getName(), str2.length(), str1, str2);
          return false;
        }
        if (str1.length() <= index) {
          LOGGER.error("Right line [{}] ({}) is longer than [{}] ({}).\n\tLeft: {}\n\tRight: {}",
              file1.getName(), str1.length(), file2.getName(), str2.length(), str1, str2);
          return false;
        }
        var leftCh = str1.charAt(index);
        var rightCh = str2.charAt(index);
        if (leftCh != rightCh) {
          LOGGER.error("Char mismatch at {}:{} => [{}] {} != [{}] {}",
              lineCount, index + 1, file1.getName(), leftCh, file2.getName(), rightCh);
          return false;
        }
        index += 1;
      }
    }
    return true;
  }

  private static boolean triplesMatch(Statement st1, Statement st2, Set<Namespace> nsset1, Set<Namespace> nsset2) {
    if ((st1.getSubject().equals(st2.getSubject())) ||
        (st1.getSubject() instanceof BNode && st2.getSubject() instanceof BNode) ||
        (st1.getSubject() instanceof IRI && st2.getSubject() instanceof IRI &&
            (expandQNameToFullIriString(asInstanceOf(st1.getSubject(), IRI.class), nsset1).equals(expandQNameToFullIriString(asInstanceOf(st2.getSubject(), IRI.class), nsset2))))) {
      if ((st1.getPredicate() == st2.getPredicate()) ||
          (expandQNameToFullIriString(st1.getPredicate(), nsset1).equals(expandQNameToFullIriString(st2.getPredicate(), nsset2)))) {
        if (st1.getObject() instanceof Literal && st2.getObject() instanceof Literal) {
          return st1.getObject().stringValue().replaceAll("\\s+", " ").trim()
              .equals(st2.getObject().stringValue().replaceAll("\\s+", " ").trim());
        } else if (st1.getObject() instanceof IRI && st2.getObject() instanceof IRI) {
          return expandQNameToFullIriString(asInstanceOf(st1.getObject(), IRI.class), nsset1).equals(expandQNameToFullIriString(asInstanceOf(st2.getObject(), IRI.class), nsset2));
        } else if ((st1.getObject() == st2.getObject()) || (st1.getObject() instanceof BNode && st2.getObject() instanceof BNode)) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private static String expandQNameToFullIriString(IRI iri, Set<Namespace> nss) {
    if (EXPANDED_QNAMES.containsKey(iri)) {
      return EXPANDED_QNAMES.get(iri);
    } else {
      var iriString = iri.stringValue();
      for (var ns : nss) {
        var prefixStr = ns.getPrefix() + ":";
        if (iriString.startsWith(prefixStr)) {
          var expandedQName = ns.getName() + iri.getLocalName();
          EXPANDED_QNAMES.put(iri, expandedQName);
          return expandedQName;
        }
      }
      EXPANDED_QNAMES.put(iri, iriString);
      return iriString;
    }
  }
}