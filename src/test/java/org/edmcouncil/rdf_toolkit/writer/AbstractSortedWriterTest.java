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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

abstract public class AbstractSortedWriterTest {

  protected static final String JSONLD_LANGUAGE = "@language";
  protected static final String JSONLD_VALUE = "@value";

  private static final String RDFS_LABEL_SHORT = "rdfs:label";

  public InputStreamReader prepareInputStream(File sourceFile) throws FileNotFoundException {
    return new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8);
  }

  protected String getTrimmedLineContainingString(String content, String string) {
    return content.lines()
        .filter(line -> line.contains(string))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new)
        .trim();
  }

  protected JsonNode getJsonObjectForLabel(String content, String label) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode root = objectMapper.readTree(content);
    JsonNode arrayOfLabels = root.get(1).get(RDFS_LABEL_SHORT);
    for (JsonNode arrayOfLabel : arrayOfLabels) {
      if (arrayOfLabel.get(JSONLD_VALUE).asText().equals(label)) {
        return arrayOfLabel;
      }
    }
    return null;
  }
}
