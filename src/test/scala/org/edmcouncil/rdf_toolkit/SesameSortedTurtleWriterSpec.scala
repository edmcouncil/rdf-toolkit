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
 */ ; /*
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
package org.edmcouncil.rdf_toolkit

import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriter.ShortIriPreferences
import org.eclipse.rdf4j.rio.turtle.{ TurtleWriter, TurtleWriterFactory }
import org.slf4j.{ Logger, LoggerFactory }

import scala.collection.JavaConverters._
import scala.language.postfixOps
import java.io._

import org.clapper.avsl.{ LogLevel, StandardLogger }
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.{ RDFFormat, Rio }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * ScalaTest tests for the SesameSortedTurtleWriter and SesameSortedRDFWriterFactory.
 */
class SesameSortedTurtleWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory getLogger classOf[SesameSortedTurtleWriterSpec]

  val rootOutputDir0 = mkCleanDir(s"target/temp/${classOf[TurtleWriter].getName}")
  val rootOutputDir1 = mkCleanDir(s"target/temp/${this.getClass.getName}")
  val rootOutputDir2 = mkCleanDir(s"target/temp/${this.getClass.getName}_2")

  val valueFactory = SimpleValueFactory getInstance ()

  /** Exclusion list of examples containing inline blank nodes. */
  val turtleInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf", "turtle-example-14.ttl", "turtle-example-25.ttl", "turtle-example-26.ttl")

  "A SortedRDFWriterFactory" should "be able to create a SortedTurtleWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory()

    val writer1 = new SesameSortedTurtleWriter(System.out)
    assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

    val writer2 = new SesameSortedTurtleWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

    val writer3Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.prefix)
    val writer3 = new SesameSortedTurtleWriter(System.out, mapAsJavaMap[String, Object](writer3Options))
    assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream with parameters")

    val writer4Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.base_iri)
    val writer4 = new SesameSortedTurtleWriter(outWriter, mapAsJavaMap[String, Object](writer4Options))
    assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
  }

  "A TurtleWriter" should "be able to read various RDF documents and write them in Turtle format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir0 = createTempDir(rootOutputDir0, "turtle")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir0, Some(".ttl"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new TurtleWriterFactory()
      val turtleWriter = factory getWriter (outWriter)
      val rdfFormat = (Rio getParserFormatForFileName (sourceFile getName)).get()

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outWriter flush ()
      outWriter close ()
    }
    logger info s"A TurtleWriter should be able to read various RDF documents and write them in Turtle format: $fileCount source files"
  }

  "A SortedTurtleWriter" should "be able to produce a sorted Turtle file" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseIri" -> baseIri)
    val turtleWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](turtleWriterOptions))

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map("baseIri" -> baseIri)
    val turtleWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](turtleWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank subject nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-17.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriter = factory getWriter (outWriter)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with directly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with indirectly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2 = factory getWriter (outWriter2)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file preferring prefix over base IRI" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_prefix.ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val turtleWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](turtleWriterOptions))

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("countries:AD"), "prefix preference has failed (1a)")
    assert(!contents1.contains("#AD"), "prefix preference has failed (1b)")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2, Some("_prefix.ttl"))
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val turtleWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](turtleWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("countries:AD"), "prefix preference has failed (2a)")
    assert(!contents2.contains("#AD"), "prefix preference has failed (2b)")
    assert(contents2.contains("Åland"), "prefix preference file has encoding problem (2)")
  }

  it should "be able to produce a sorted Turtle file preferring base IRI over prefix" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_base_iri.ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val turtleWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](turtleWriterOptions))

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("#AD"), "base IRI preference has failed (1a)")
    assert(!contents1.contains("countries:AD"), "base IRI preference has failed (1b)")
    assert(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val turtleWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](turtleWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("#AD"), "base IRI preference has failed (2a)")
    assert(!contents2.contains("countries:AD"), "base IRI preference has failed (2b)")
    assert(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)")
  }

  it should "be able to read various RDF documents and write them in sorted Turtle format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }
    logger info s"A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle format: $fileCount source files"
  }

  it should "be able to sort RDF triples consistently when writing in Turtle format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted Turtle.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }
    logger info s"A SortedTurtleWriter should be able to sort RDF triples consistently when writing in Turtle format: $fileCount source files"

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2)
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if !file1.getName.contains("_prefix") && !file1.getName.contains("_base_iri")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "not add/lose RDF triples when writing in Turtle format without inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted Turtle
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }
    logger info s"A SortedTurtleWriter should not add/lose RDF triples when writing in Turtle format without inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if !file1.getName.contains("_prefix") && !file1.getName.contains("_base_iri")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some(".ttl"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", RDFFormat.TURTLE)
      // println(s"[info] Comparing ${sourceFile.getAbsolutePath} to ${targetFile.getAbsolutePath} ...") // TODO: remove debugging
      assertTriplesMatch(inputModel1, inputModel2)
    }
  }

  it should "be able to produce a sorted Turtle file with inline blank nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/fibo/ontology/master/latest/FND/Accounting/AccountingEquity.rdf")
    val baseIri = valueFactory.createIRI("https://spec.edmcouncil.org/fibo/ontology/FND/Accounting/AccountingEquity/")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_inline_blank_nodes.ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val turtleWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](turtleWriterOptions))

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val turtleWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](turtleWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to sort RDF triples consistently when writing in Turtle format with inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted Turtle.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn.ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }
    logger info s"A SortedTurtleWriter should be able to sort RDF triples consistently when writing in Turtle format with inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "not add/lose RDF triples when writing in Turtle format with inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted Turtle
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn2.ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }
    logger info s"A SortedTurtleWriter should not add/lose RDF triples when writing in Turtle format with inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn2")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some("_ibn2.ttl"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", RDFFormat.TURTLE)
      assertTriplesMatch(inputModel1, inputModel2)
    }
  }

  it should "be able to read various RDF documents and write them in sorted Turtle format with an inferred base IRI" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(ibiExclusionList contains (sourceFile getName))) {
      val sourceReader = new BufferedReader(new FileReader(sourceFile))
      var baseIri1: Option[String] = None
      var unfinished = true
      var hasOntologyIri = false
      while (unfinished) {
        val line = sourceReader.readLine()
        if (line == null) {
          unfinished = false
        } else if (line.contains("owl:Ontology")) {
          hasOntologyIri = true
        } else if (baseIri1.isEmpty) {
          baseIri1 = getBaseIri(line)
        }
      }

      if (hasOntologyIri && baseIri1.isDefined) {
        fileCount += 1

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi.ttl"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "turtle",
          "-ibi"
        )

        val targetReader = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"))
        var baseIri2: Option[String] = None
        unfinished = true
        while (unfinished) {
          val line = targetReader.readLine()
          if (line == null) {
            unfinished = false
          } else if (baseIri2.isEmpty) {
            baseIri2 = getBaseIri(line)
          }
        }

        assert(baseIri1 === baseIri2, "base IRI changed - was ontology IRI different to the base IRI?")
      }
    }
    logger info s"A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle format with an inferred base IRI: $fileCount source files"
  }

  it should "be able to read various RDF documents and write them in sorted Turtle format with an inferred base IRI and inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(ibiExclusionList contains (sourceFile getName))) {
      val sourceReader = new BufferedReader(new FileReader(sourceFile))
      var baseIri1: Option[String] = None
      var unfinished = true
      var hasOntologyIri = false
      while (unfinished) {
        val line = sourceReader.readLine()
        if (line == null) {
          unfinished = false
        } else if (line.contains("owl:Ontology")) {
          hasOntologyIri = true
        } else if (baseIri1.isEmpty) {
          baseIri1 = getBaseIri(line)
        }
      }

      if (hasOntologyIri && baseIri1.isDefined) {
        fileCount += 1

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi_ibn.ttl"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "turtle",
          "-ibi",
          "-ibn"
        )

        val targetReader = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"))
        var baseIri2: Option[String] = None
        unfinished = true
        while (unfinished) {
          val line = targetReader.readLine()
          if (line == null) {
            unfinished = false
          } else if (baseIri2.isEmpty) {
            baseIri2 = getBaseIri(line)
          }
        }

        assert(baseIri1 === baseIri2, "base IRI changed - was ontology IRI different to the base IRI?")
      }
    }
    logger info s"A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle format with an inferred base IRI and inline blank nodes: $fileCount source files"
  }

  "A SesameRdfFormatter" should "be able to do pattern-based IRI replacements" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_replaced.ttl"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-ip", "^http://topbraid.org/countries",
      "-ir", "http://replaced.example.org/countries"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("@prefix countries: <http://replaced.example.org/countries#> ."), "IRI replacement seems to have failed")
  }

  it should "be able to add single-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_single-comments.ttl"))
    val linePrefix = "## "
    val leadingComment = "Start of: My New Ontology."
    val trailingComment = "End of: My New Ontology."
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-lc", leadingComment,
      "-tc", trailingComment
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains(linePrefix + leadingComment), "leading comment insertion seems to have failed")
    assert(content.contains(linePrefix + trailingComment), "trailing comment insertion seems to have failed")
  }

  it should "be able to add multi-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_multiple-comments.ttl"))
    val linePrefix = "## "
    val leadingComments = List("Start of: My New Ontology.", "Version 1.")
    val trailingComments = List("End of: My New Ontology.", "Version 1.")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-lc", leadingComments(0), "-lc", leadingComments(1),
      "-tc", trailingComments(0), "-tc", trailingComments(1)
    )
    val content = getFileContents(outputFile, "UTF-8")
    for (comment ← leadingComments) {
      assert(content.contains(linePrefix + comment), "leading comment insertion seems to have failed")
    }
    for (comment ← trailingComments) {
      assert(content.contains(linePrefix + comment), "trailing comment insertion seems to have failed")
    }
  }

  it should "be able to use explicit data typing for strings" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_sdt-explicit.ttl"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-sdt", "explicit"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("^^xsd:string"), "explicit string data typing seems to have failed")
  }

  it should "be able to use set the indent string" in {
    val outputDir1 = createTempDir(rootOutputDir1, "turtle")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_spaces.ttl"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-i", "  "
    )
    val content = getFileContents(outputFile, "UTF-8")
    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).size
    assert(singleIndentLineCount >= 1, "double-space indent has failed")

    val outputFile2 = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_tabs.ttl"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile2 getAbsolutePath,
      "-i", "\t\t"
    )
    val content2 = getFileContents(outputFile2, "UTF-8")
    val singleIndentLineCount2 = content2.lines.filter(_.matches("^\t\t\\S.*$")).size
    assert(singleIndentLineCount2 >= 1, "double-tab indent has failed")
  }

}
