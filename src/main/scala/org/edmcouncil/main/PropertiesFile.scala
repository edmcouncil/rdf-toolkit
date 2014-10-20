package org.edmcouncil.main

import grizzled.slf4j.Logging

class PropertiesFile(private val inputStream: java.io.InputStream) extends Logging {

  lazy private val source = {

    val s = scala.io.Source.fromInputStream(inputStream)(scala.io.Codec.UTF8)

    require(s != null)

    for (line <- s.getLines())
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
      case ex: Throwable => {
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
