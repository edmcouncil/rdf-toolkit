package org.edmcouncil.rdf_serializer

import java.nio.charset.Charset

import org.edmcouncil.rdf_serializer.SesameSortedTurtleWriter.ShortUriPreferences
import org.openrdf.rio.turtle.{TurtleWriterFactory, TurtleWriter}
import org.slf4j.{LoggerFactory, Logger}

import scala.collection.mutable.ListBuffer
import scala.io.{Codec, BufferedSource}
import scala.language.postfixOps

import java.io._

import org.openrdf.model.impl.URIImpl
import org.openrdf.rio.{RDFFormat, Rio}
import org.scalatest.{Matchers, FlatSpec}

/**
 * ScalaTest tests for the SortedTurtleWriter and SortedTurtleWriterFactory.
 */
class SesameSortedTurtleWriterSpec extends FlatSpec with Matchers /*with OutputSuppressor*/ {

  private val logger = LoggerFactory.getLogger(classOf[SesameSortedTurtleWriterSpec])

  // Set up directories for output files.
  def mkCleanDir(dirPath: String): File = {
    val newDir = new File(dirPath)
    if (newDir exists) { newDir delete () }
    newDir mkdirs ()
    newDir
  }
  val outputDir0 = mkCleanDir(s"target//temp//${classOf[TurtleWriter].getName}")
  val outputDir1 = mkCleanDir(s"target//temp//${this.getClass.getName}")
  val outputDir2 = mkCleanDir(s"target//temp//${this.getClass.getName}_2")

  /** Collects all of the files in a directory tree. */
  def listDirTreeFiles(dir : File): Seq[File] = {
    val result = new ListBuffer[File]();
    if (dir.exists()) {
      if (dir isDirectory) {
        for (file <- dir.listFiles()) {
          result ++= listDirTreeFiles(file)
        }
      } else {
        result += dir // 'dir' is actually a file
      }
    }
    result
  }

  /** Sets the extension part of a filename path, e.g. "ttl". */
  def setFilePathExtension(filePath: String, fileExtension: String): String = {
    if (filePath.contains(".")) {
      s"${filePath.substring(0, filePath.lastIndexOf("."))}.$fileExtension"
    } else {
      s"$filePath.$fileExtension"
    }
  }

  /** Reads the contents of a file into a String. */
  def getFileContents(file: File): String = new BufferedSource(new FileInputStream(file)).mkString

  def getFileContents(file: File, encoding: String): String = new BufferedSource(new FileInputStream(file))(new Codec(Charset.forName(encoding))).mkString

  def compareFiles(file1: File, file2: File, encoding: String): Boolean = {
    logger.debug("CompareFiles:")
    logger.debug(s"   left = ${file1.getAbsolutePath}")
    logger.debug(s"  right = ${file2.getAbsolutePath}")
    val source1Lines = new BufferedSource(new FileInputStream(file1))(new Codec(Charset.forName(encoding))).getLines()
    val source2Lines = new BufferedSource(new FileInputStream(file2))(new Codec(Charset.forName(encoding))).getLines()
    compareStringIterators(source1Lines, source2Lines)
    logger.debug("CompareFiles: done")
    true
  }

  def compareStringIterators(iter1: Iterator[String], iter2: Iterator[String]): Boolean = {
    var lineCount = 0
    while (iter1.hasNext || iter2.hasNext) {
      lineCount += 1
      if (!iter2.hasNext) {
        logger.error(s"left file has more lines than right ($lineCount+): ${iter1.next()}")
        return false
      }
      if (!iter1.hasNext) {
        logger.error(s"right file has more lines than left ($lineCount+): ${iter2.next()}")
        return false
      }
      val line1 = iter1.next()
      val line2 = iter2.next()
      if (!compareStrings(line1, line2, lineCount)) {
        return false
      }
    }
    true
  }

  def compareStrings(str1: String, str2: String, lineCount: Int): Boolean = {
    if ((str1.length >= 1) || (str2.length >= 1)) {
      var index = 0
      while ((str1.length > index) || (str2.length > index)) {
        if (str2.length <= index) {
          logger.error(s"left line ($lineCount) is longer than (${index+1}+) than right: tail = ${str1.substring(index)}")
          return false
        }
        if (str1.length <= index) {
          logger.error(s"right line ($lineCount) is longer than (${index+1}+) than left: tail = ${str2.substring(index)}")
          return false
        }
        var leftCh = str1.charAt(index)
        var rightCh = str2.charAt(index)
        if (leftCh != rightCh) {
          logger.error(s"char mismatch at $lineCount:${index+1} => $leftCh [#${leftCh.toShort}] != $rightCh [#${rightCh.toShort}]")
        }
        index += 1
      }
      true
    } else {
      true
    }
  }

  "A SortedTurtleWriterFactory" should "be able to create a SortedTurtleWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedTurtleWriterFactory()

    val writer1 = new SesameSortedTurtleWriter(System.out)
    assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

    val writer2 = new SesameSortedTurtleWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

    val writer3 = new SesameSortedTurtleWriter(System.out, new URIImpl("http://example.com#"), "\t\t", ShortUriPreferences.prefix)
    assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream with parameters")

    val writer4 = new SesameSortedTurtleWriter(outWriter, new URIImpl("http://example.com#"), "\t\t", ShortUriPreferences.base_uri)
    assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
  }

  "A TurtleWriter" should "be able to read various RDF documents and write them in sorted Turtle format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir0, setFilePathExtension(sourceFile getName, "ttl"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new TurtleWriterFactory()
      val turtleWriter = factory getWriter (outWriter)
      val rdfFormat = Rio getParserFormatForFileName(sourceFile getName, RDFFormat.TURTLE)

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outWriter flush ()
      outWriter close ()
    }
  }

  "A SortedTurtleWriter" should "be able to produce a sorted Turtle file" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outWriter, baseUri, null, null)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2, baseUri, null, null)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank object nodes" in {
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank subject nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-17.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outWriter)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with directly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with indirectly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file preferring prefix over base URI" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_prefix.ttl")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outWriter, baseUri, null, ShortUriPreferences.prefix)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("countries:AD"), "prefix preference has failed (1a)")
    assert(!contents1.contains("#AD"), "prefix preference has failed (1b)")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_prefix.ttl")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2, baseUri, null, ShortUriPreferences.prefix)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("countries:AD"), "prefix preference has failed (2a)")
    assert(!contents2.contains("#AD"), "prefix preference has failed (2b)")
    assert(contents2.contains("Åland"), "prefix preference file has encoding problem (2)")
  }

  it should "be able to produce a sorted Turtle file preferring base URI over prefix" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_base_uri.ttl")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outWriter, baseUri, null, ShortUriPreferences.base_uri)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("#AD"), "base URI preference has failed (1a)")
    assert(!contents1.contains("countries:AD"), "base URI preference has failed (1b)")
    assert(contents1.contains("Åland"), "base URI preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_base_uri.ttl")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2, baseUri, null, ShortUriPreferences.base_uri)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("#AD"), "base URI preference has failed (2a)")
    assert(!contents2.contains("countries:AD"), "base URI preference has failed (2b)")
    assert(contents2.contains("Åland"), "base URI preference file has encoding problem (2)")
  }

  it should "be able to read various RDF documents and write them in sorted Turtle format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }
  }

  it should "be able to sort RDF triples consistently when writing in Turtle format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted Turtle.
    var fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile <- listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 <- listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "should not add/lose RDF triples when writing in Turtle format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted Turtle.
    var fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile <- listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 <- listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "ttl"))
      val rdfFormat1 = Rio getParserFormatForFileName(sourceFile getName, RDFFormat.TURTLE)
      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
      val inputModel2 = Rio parse (new FileReader(targetFile), "", RDFFormat.TURTLE)
      assert(inputModel1.size() === inputModel2.size(), s"ingested triples do not match for: ${sourceFile.getName}")
    }
  }

}
