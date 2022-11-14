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
package org.edmcouncil.rdf_toolkit.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.edmcouncil.rdf_toolkit.runner.exception.RdfToolkitOptionHandlingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CommandLineArgumentsHandlerTest {

  private static final String[] REQUIRED_ARGS = new String[]{"--source-format", "rdf-xml"};

  @ParameterizedTest
  @MethodSource("getTestParams")
  void runTests(TestParam testParam) throws RdfToolkitOptionHandlingException, FileNotFoundException, ParseException {
    var commandLineArgumentsHandler = new CommandLineArgumentsHandler();
    var allArgs = getArgsWithRequired(testParam.getArgs());
    var rdfToolkitOptions = commandLineArgumentsHandler.handleArguments(allArgs);

    var fieldValue = getFieldValue(rdfToolkitOptions, testParam.getFieldName());

    assertEquals(testParam.getExpectedValue(), fieldValue);
  }

  private String[] getArgsWithRequired(String[] args) {
    int finalLength = REQUIRED_ARGS.length + args.length;
    List<String> argsList = new ArrayList<>(finalLength);
    Collections.addAll(argsList, REQUIRED_ARGS);
    Collections.addAll(argsList, args);
    return argsList.toArray(new String[finalLength]);
  }

  static List<TestParam> getTestParams() {
    return List.of(
        new TestParam(new String[]{"--line-end", "\r\n"}, "lineEnd", "\r\n"),
        new TestParam(new String[]{""}, "lineEnd", "\n"),
        new TestParam(new String[]{"--omit-xmlns-namespace"}, "omitXmlnsNamespace", true),
        new TestParam(new String[]{""}, "omitXmlnsNamespace", false),
        new TestParam(new String[]{"--suppress-named-individuals"}, "suppressNamedIndividuals", true),
        new TestParam(new String[]{""}, "suppressNamedIndividuals", false)
    );
  }

  static Object getFieldValue(RdfToolkitOptions rdfToolkitOptions, String fieldName) {
    try {
      Field field = rdfToolkitOptions.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);

      return field.get(rdfToolkitOptions);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to get field value for field with name " + fieldName, e);
    }
  }

  private static class TestParam {

    private final String[] args;
    private final String fieldName;
    private final Object expectedValue;

    public TestParam(String[] args, String fieldName, Object expectedValue) {
      this.args = args;
      this.fieldName = fieldName;
      this.expectedValue = expectedValue;
    }

    public String[] getArgs() {
      return args;
    }

    public String getFieldName() {
      return fieldName;
    }

    public Object getExpectedValue() {
      return expectedValue;
    }
  }
}
