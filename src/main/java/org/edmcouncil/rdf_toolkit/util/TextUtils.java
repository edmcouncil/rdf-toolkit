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

public class TextUtils {

  private TextUtils() {}

  /**
   * Whether the character is a "name character", as defined in the XML namespaces spec.  Characters above Unicode FFFF are included.
   */
  public static boolean isNameChar(char ch) {
    if ('-' == ch) {
      return true;
    }
    if ('.' == ch) {
      return true;
    }
    if ('_' == ch) {
      return true;
    }
    if ('\\' == ch) {
      return true;
    }
    if (':' == ch) {
      return true;
    }
    if (isDigit(ch)) {
      return true;
    }
    if (isUpperCaseLetter(ch)) {
      return true;
    }
    if (isLowerCaseLetter(ch)) {
      return true;
    }
    if (('\u00C0' <= ch) && (ch <= '\u00D6')) {
      return true;
    }
    if (('\u00D8' <= ch) && (ch <= '\u00F6')) {
      return true;
    }
    if (('\u00F8' <= ch) && (ch <= '\u02FF')) {
      return true;
    }
    if (('\u0370' <= ch) && (ch <= '\u037D')) {
      return true;
    }
    if (('\u037F' <= ch) && (ch <= '\u1FFF')) {
      return true;
    }
    if (('\u200C' <= ch) && (ch <= '\u200D')) {
      return true;
    }
    if (('\u2070' <= ch) && (ch <= '\u218F')) {
      return true;
    }
    if (('\u2C00' <= ch) && (ch <= '\u2FEF')) {
      return true;
    }
    if (('\u3001' <= ch) && (ch <= '\uD7FF')) {
      return true;
    }
    if (('\uF900' <= ch) && (ch <= '\uFDCF')) {
      return true;
    }
    if (('\uFDF0' <= ch) && (ch <= '\uFFFD')) {
      return true;
    }
    if ('\u00B7' == ch) {
      return true;
    }
    if (('\u0300' <= ch) && (ch <= '\u036F')) {
      return true;
    }
    return ('\u203F' <= ch) && (ch <= '\u2040');
  }

  public static boolean isNotNameChar(char ch) {
    return !isNameChar(ch);
  }

  public static boolean isMultilineString(String str) {
    if (str == null) {
      return false;
    }
    for (var idx = 0; idx < str.length(); idx++) {
      switch (str.charAt(idx)) {
        case 0xA:
        case 0xB:
        case 0xC:
        case 0xD:
          return true;
        default:
          // Do nothing
      }
    }
    return false;
  }

  /**
   * Whether the string is valid as the local part of a prefixed name, as defined in the RDF 1.1 Turtle spec.
   * Doesn't check that backslash escape sequences in the name are correctly formed.
   */
  public static boolean isPrefixedNameLocalPart(String str) {
    if (str == null) {
      return false;
    }
    if (str.length() < 1) {
      return false;
    }
    if ((':' != str.charAt(0)) && isNotNameChar(str.charAt(0))) {
      return false; // cannot start with a colon
    }
    for (var idx = 2; idx < str.length(); idx++) {
      if (isNotNameChar(str.charAt(idx))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isDigit(char ch) {
    return '0' <= ch && ch <= '9';
  }

  private static boolean isUpperCaseLetter(char ch) {
    return 'A' <= ch && ch <= 'Z';
  }

  private static boolean isLowerCaseLetter(char ch) {
    return 'a' <= ch && ch <= 'z';
  }
}
