package org.edmcouncil.rdf_serializer

import org.openrdf.rio.turtle.{TurtleWriterFactory, TurtleWriter}

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource
import scala.language.postfixOps

import java.io._

import org.openrdf.model.impl.URIImpl
import org.openrdf.rio.{RDFFormat, Rio}
import org.scalatest.{Matchers, FlatSpec}
import grizzled.slf4j.Logging

/**
 * ScalaTest tests for the SortedTurtleWriter and SortedTurtleWriterFactory.
 */
class SesameSortedTurtleWriterSpec extends FlatSpec with Matchers with OutputSuppressor {

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

  "A SortedTurtleWriterFactory" should "be able to create a SortedTurtleWriter" in {
    suppressOutput {
      val outWriter = new OutputStreamWriter(System.out)
      val factory = new SesameSortedTurtleWriterFactory()

      val writer1 = new SesameSortedTurtleWriter(System.out)
      assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

      val writer2 = new SesameSortedTurtleWriter(outWriter)
      assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

      val writer3 = new SesameSortedTurtleWriter(System.out, new URIImpl("http://example.com#"), "\t\t")
      assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream wit parameters")

      val writer4 = new SesameSortedTurtleWriter(outWriter, new URIImpl("http://example.com#"), "\t\t")
      assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
    }
  }

  "A TurtleWriter" should "be able to read various RDF documents and write them in sorted Turtle format" ignore {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile <- listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir0, setFilePathExtension(sourceFile getName, "ttl"))
      val outStream = new FileOutputStream(targetFile)
      val factory = new TurtleWriterFactory()
      val turtleWriter = factory getWriter (outStream)
      val rdfFormat = Rio getParserFormatForFileName(sourceFile getName, RDFFormat.TURTLE)

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outStream flush ()
      outStream close ()
    }
  }

  "A SortedTurtleWriter" should "be able to produce a sorted Turtle file" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outStream = new FileOutputStream(outputFile)
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outStream, baseUri, null)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outStream flush ()
    outStream close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outStream2 = new FileOutputStream(outputFile2)
    val turtleWriter2 = factory getWriter (outStream2, baseUri, null)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outStream2 flush ()
    outStream2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank object nodes" in {
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outStream = new FileOutputStream(outputFile)
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outStream

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outStream flush ()
    outStream close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outStream2 = new FileOutputStream(outputFile2)
    val turtleWriter2 = factory getWriter (outStream2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outStream2 flush ()
    outStream2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank subject nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-17.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outStream = new FileOutputStream(outputFile)
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter (outStream)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outStream flush ()
    outStream close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outStream2 = new FileOutputStream(outputFile2)
    val turtleWriter2 = factory getWriter (outStream2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outStream2 flush ()
    outStream2 close ()
  }

  it should "be able to produce a sorted Turtle file with directly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outStream = new FileOutputStream(outputFile)
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outStream

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outStream flush ()
    outStream close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outStream2 = new FileOutputStream(outputFile2)
    val turtleWriter2 = factory getWriter (outStream2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outStream2 flush ()
    outStream2 close ()
  }

  it should "be able to produce a sorted Turtle file with indirectly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, "ttl"))
    val outStream = new FileOutputStream(outputFile)
    val factory = new SesameSortedTurtleWriterFactory()
    val turtleWriter = factory getWriter outStream

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outStream flush ()
    outStream close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outStream2 = new FileOutputStream(outputFile2)
    val turtleWriter2 = factory getWriter (outStream2)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outStream2 flush ()
    outStream2 close ()
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

  it should "be able to sort RDF triples consistently when writing in Turtle format" ignore {
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
    for (sourceFile <- listDirTreeFiles(outputDir1)) {
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
      val contents1 = getFileContents(file1)
      val contents2 = getFileContents(file2)
      assert(contents1 === contents2, s"match failed for file: ${file1 getAbsolutePath}")
    }
  }

  it should "should not add/lose RDF triples when writing in Turtle format" ignore {
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
    for (sourceFile <- listDirTreeFiles(outputDir1)) {
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
      val contents1 = getFileContents(file1)
      val contents2 = getFileContents(file2)
      assert(contents1 === contents2, s"match failed for file: ${file1 getAbsolutePath}")
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
