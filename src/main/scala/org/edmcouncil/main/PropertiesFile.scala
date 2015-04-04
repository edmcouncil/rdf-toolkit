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
package org.edmcouncil.main

import grizzled.slf4j.Logging

class PropertiesFile(private val inputStream: java.io.InputStream) extends Logging {

  lazy private val source = {

    val s = scala.io.Source.fromInputStream(inputStream)(scala.io.Codec.UTF8)

    require(s != null)

    for (line ← s.getLines())
      println(line)

    s
  }

  lazy val properties = {

    import scala.collection.JavaConverters._

    val is = inputStream

    require(is != null)

    val props = new java.util.Properties()
    try {
      props.load(is)
    } catch {
      case ex: Throwable ⇒ {
        error(s"Some exception: ${ex.getMessage} class=${ex.getClass.getName}")
      }
    }
    val map = props.asScala

    /*
    println(s"Read the following keys and values from $fileName:")
    for((key, value) <- map) println(key + "\t= " + value)
    println("----")
    */

    map
  }
}

object PropertiesFile {

  private def inputStream(fileName: String) = getClass.getClassLoader.getResourceAsStream(fileName)

  def apply(fileName: String) = {

    var is = inputStream(fileName)

    if (is == null)
      is = inputStream("./target/scala-2.11/resource_managed/main/booter.properties")

    require(is != null, fileName + " needs to be found on the classpath")
    require(is.available() > 0)

    new PropertiesFile(is)
  }
}
