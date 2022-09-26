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

package org.edmcouncil.rdf_toolkit.util;

import java.util.ArrayList;
import java.util.List;

public enum StringDataTypeOptions {
  EXPLICIT("explicit"),
  IMPLICIT("implicit");

  public static final StringDataTypeOptions DEFAULT_STRING_DATA_TYPE = IMPLICIT;

  private final String optionValue;

  StringDataTypeOptions(String optionValue) {
    this.optionValue = optionValue;
  }

  public String getOptionValue() {
    return optionValue;
  }

  public static StringDataTypeOptions getByOptionValue(String optionValue) {
    if (optionValue == null) {
      return null;
    }
    for (StringDataTypeOptions dataTypeOption : StringDataTypeOptions.values()) {
      if (optionValue.equals(dataTypeOption.optionValue)) {
        return dataTypeOption;
      }
    }
    return null;
  }

  public static String summarise() {
    List<String> result = new ArrayList<>();
    for (StringDataTypeOptions dataTypeOption : StringDataTypeOptions.values()) {
      String value = dataTypeOption.optionValue;
      if (DEFAULT_STRING_DATA_TYPE.equals(dataTypeOption)) {
        value += " [default]";
      }
      result.add(value);
    }
    return String.join(", ", result);
  }
}