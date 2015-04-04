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
package org.edmcouncil.util

/**
 * The BaseURL is used in combination with the base directory to figure out where
 * imports are located on disk.
 *
 * TODO: Rename to URI, I think we need to also support URNs not just URLs (JG)
 */
class BaseURL private (val url: Option[String]) {
  
  val isSpecified = url.isDefined

  /**
   * @return true if the importUrl is covered by the base URL
   */
  def matchesWith(importUrl: String) = url.exists(importUrl.startsWith)

  def endsWithSlash = url.exists(_.endsWith("/"))

  def endsWithHash = url.exists(_.endsWith("#"))

  def urlEndingWithSlash = if (endsWithSlash || endsWithHash) url else url.map(_ + "/")

  def strip(importUrl: String) = urlEndingWithSlash.map(importUrl.stripPrefix)

  override def toString = url.getOrElse("None")
}

object BaseURL {

  def apply(url: String): BaseURL = apply(Some(url))
  def apply(url: Option[String]) = new BaseURL(url)
}
