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

package org.edmcouncil.extension

import scala.language.postfixOps

object StringExtensions {

  val strippedRegex = """((^)|(\n))\s*\|"""r

  implicit class ExtendedString2(string: String) {

    def uncapitalize: String = org.apache.commons.lang3.StringUtils.uncapitalize(string)

    def urlEncode: String = java.net.URLEncoder.encode(string, "UTF-8")
    def urlDecode: String = java.net.URLDecoder.decode(string, "UTF-8")
  }

  /**
   * Provides Safe type casting for a strings.
   * @param str The string to be safely cast to a type.
   */
  implicit class SafeCaster(str: String) {
    /**
     * Safely Converts a string value to a boolean, without throwing an exception.
     *
     * @return true IF AND ONLY IF the lowercase, trimmed version of the string equals "true" OR
     *  the trimmed version of the string is exactly equal to "1". Otherwise it returns false.
     *  This means strings such as "1", "True", "TRUE", and "TrUe  " will all evaluate to true.
     */
    def toBooleanSafe: Boolean = {
      val normalized = str.trim.toLowerCase
      if (normalized == "true" || normalized == "1") true else false
    }

    /**
     * Safely Converts a string value to a positive integer, without throwing an exception.
     *
     * @return str.toInt IF AND ONLY IF the string only contains digits, otherwise -1.
     */
    def toPositiveIntSafe: Int = if ("""^\d+$""".r.findAllIn(str).isEmpty) -1 else str.toInt

    /**
     * Does the same as above but without magic numbers.
     *
     * @return positive integer or None
     */
    def toPositiveIntegerSafe: Option[Int] =
      if ("""^\d+$""".r.findAllIn(str).isEmpty) None else Some(str.toInt)
  }

  implicit class PathExpander(string: String) {

    def asValidPathName = {

      if (string.startsWith("~" + java.io.File.separator)) {
        System.getProperty("user.home") + string.substring(1)
      } else string
    }
  }
}
