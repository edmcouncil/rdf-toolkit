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

package org.edmcouncil.rdf_toolkit

import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriter.ShortIriPreferences
import org.eclipse.rdf4j.model.impl.SimpleValueFactory

import scala.collection.JavaConverters._
import scala.language.postfixOps
import java.io._
import java.util

import org.eclipse.rdf4j.model.{ Literal, Statement }
import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriterFactory.TargetFormats
import org.eclipse.rdf4j.rio.{ RDFFormat, RDFWriter, Rio }
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriterFactory
import org.slf4j.LoggerFactory
import org.scalatest.{ FlatSpec, Matchers }

/**
 * ScalaTest tests for the SesameSortedRdfXmlWriter and SesameSortedRDFWriterFactory.
 */
class SesameSortedRdfXmlWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory getLogger classOf[SesameSortedRdfXmlWriterSpec]

  val rootOutputDir0 = mkCleanDir(s"target//temp//${classOf[RDFWriter].getName}")
  val rootOutputDir1 = mkCleanDir(s"target//temp//${this.getClass.getName}")
  val rootOutputDir2 = mkCleanDir(s"target//temp//${this.getClass.getName}_2")

  val valueFactory = SimpleValueFactory getInstance ()

  /** Exclusion list of examples that can't be represented directly in RDF/XML. */
  val rdfXmlExclusionList = List("allemang-test-a.ttl", "allemang-test-b.ttl", "turtle-example-2.ttl", "turtle-example-3.ttl", "turtle-example-4.ttl", "turtle-example-5.ttl", "turtle-example-6.ttl", "turtle-example-9.ttl", "turtle-example-17.ttl", "turtle-example-22.ttl")
  // Note: the 'allemang' test files are in this list because they contain strings that end with a whitespace.
  // It seems that the Sesame RDF/XML parser strips that trailing whitespace away; the Sesame Turtle parser does not.
  // This stops the Sesame formatter from being able to treat the 'allemang' examples in RDF/XML format
  // the same as when it is in Turtle format.
  // The other excluded examples don't have a namespace prefix for all predicates, which is a limitation of RDF/XML.

  /** Exclusion list of examples where the ontology IRI is different to the base IRI. */
  val rdfXmlInferredBaseIriExclusionList = List("food.rdf", "wine.rdf")

  /** Exclusion list of examples containing inline blank nodes. */
  val rdfXmlInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf", "turtle-example-14.ttl", "turtle-example-25.ttl", "turtle-example-26.ttl")

  def substringAfter(str: String, substr: String): String = {
    if ((str == null) || (str.length < 1) || (substr == null) || (substr.length < 1)) { return str }
    val idx = str indexOf substr
    str.substring(idx + substr.length)
  }

  "A SortedRDFWriterFactory" should "be able to create a SortedRdfXmlWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory(TargetFormats.rdf_xml)

    val writer1 = new SesameSortedRdfXmlWriter(System.out)
    assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

    val writer2 = new SesameSortedRdfXmlWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

    val writer3Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.prefix)
    val writer3 = new SesameSortedRdfXmlWriter(System.out, mapAsJavaMap[String, Object](writer3Options))
    assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream with parameters")

    val writer4Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.base_iri)
    val writer4 = new SesameSortedRdfXmlWriter(outWriter, mapAsJavaMap[String, Object](writer4Options))
    assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
  }

  "An RDFWriter" should "be able to read various RDF documents and write them in RDF/XML format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir0 = createTempDir(rootOutputDir0, "rdfxml")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir0, Some(".rdf"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new RDFXMLPrettyWriterFactory()
      val turtleWriter = factory getWriter outWriter
      val rdfFormat = (Rio getParserFormatForFileName (sourceFile getName)).get()

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outWriter flush ()
      outWriter close ()
    }
    logger info s"An RDFWriter should be able to read various RDF documents and write them in RDF/XML format: $fileCount source files"
  }

  "A SortedRdfXmlWriter" should "be able to produce a sorted RDF/XML file" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("baseIri", baseIri)
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("baseIri", baseIri)
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with directly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with indirectly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file preferring prefix over base IRI" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_prefix.rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("Åland"), "prefix preference file has encoding problem (2)")
  }

  it should "be able to produce a sorted RDF/XML file preferring base IRI over prefix" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_base_iri.rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)")
  }

  it should "be able to read various RDF documents and write them in sorted RDF/XML format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }
    logger info s"A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML format: $fileCount source files"
  }

  it should "be able to sort RDF triples consistently when writing in RDF/XML format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }
    logger info s"A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML format: $fileCount source files"

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "not add/lose RDF triples when writing in RDF/XML format without blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }
    logger info s"A SortedRdfXmlWriter should not add/lose RDF triples when writing in RDF/XML format without blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised RDF/XML files have the same triple count as the matching raw files.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some(".rdf"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val rdfFormat2 = (Rio getParserFormatForFileName (targetFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", rdfFormat2)
      // println(s"[info] Comparing ${sourceFile.getAbsolutePath} to ${targetFile.getAbsolutePath} ...")
      assertTriplesMatch(inputModel1, inputModel2)
    }
  }

  it should "be able to produce a sorted RDF/XML file with inline blank nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/fibo/ontology/master/latest/FND/Accounting/AccountingEquity.rdf")
    val baseIri = valueFactory.createIRI("https://spec.edmcouncil.org/fibo/ontology/FND/Accounting/AccountingEquity/")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_inline_blank_nodes.rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("baseIri", baseIri)
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriterOptions.put("inlineBlankNodes", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("baseIri", baseIri)
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriter2Options.put("inlineBlankNodes", java.lang.Boolean.TRUE);
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to sort RDF triples consistently when writing in RDF/XML format with inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn.rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }
    logger info s"A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML format with inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "should not add/lose RDF triples when writing in RDF/XML format with blank nodes (single file)" in {
    // Serialise sample file as sorted RDF/XML.
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/ControlParties.rdf")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_ibn2s.rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriterOptions.put("inlineBlankNodes", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.RDFXML)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriter2Options.put("inlineBlankNodes", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()

    // Check that re-serialising the RDF/XML file has changed nothing.
    assert(outputFile2 exists, s"file missing in outputDir2: ${outputFile2.getAbsolutePath}")
    assert(compareFiles(outputFile, outputFile2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${outputFile.getName}")

    // Check that the re-serialised RDF/XML file has the same triple count as the matching raw file.
    val rdfFormat1 = (Rio getParserFormatForFileName (inputFile getName)).get()
    val rdfFormat2 = (Rio getParserFormatForFileName (outputFile2 getName)).get()
    val inputModel1a = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), "", rdfFormat1)
    val inputModel2a = Rio parse (new InputStreamReader(new FileInputStream(outputFile2), "UTF-8"), "", rdfFormat2)
    // println(s"[single] [info] Comparing ${inputFile.getAbsolutePath} to ${outputFile2.getAbsolutePath} ...")
    assertTriplesMatch(inputModel1a, inputModel2a, DebugState(true, Some("single")))
  }

  it should "not add/lose RDF triples when writing in RDF/XML format with blank nodes (multiple files)" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn2.rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }
    logger info s"A SortedRdfXmlWriter should not add/lose RDF triples when writing in RDF/XML format with blank nodes (multiple files): $fileCount source files"

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn2")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised RDF/XML files have the same triple count as the matching raw files.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some("_ibn2.rdf"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val rdfFormat2 = (Rio getParserFormatForFileName (targetFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", rdfFormat2)
      // println(s"[multi] [info] Comparing ${sourceFile.getAbsolutePath} to ${targetFile.getAbsolutePath} ...") } // TODO: comment out debugging
      assertTriplesMatch(inputModel1, inputModel2, NODEBUG)
    }
  }

  it should "be able to read various RDF documents and write them in sorted RDF/XML format with an inferred base IRI" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains (sourceFile getName)) && !(rdfXmlInferredBaseIriExclusionList contains (sourceFile getName)) && !(ibiExclusionList contains (sourceFile getName))) {
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

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi.rdf"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "rdf-xml",
          "-dtd",
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
    logger info s"A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML format with an inferred base IRI: $fileCount source files"
  }

  it should "be able to read various RDF documents and write them in sorted RDF/XML format with an inferred base IRI and inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(rdfXmlExclusionList contains (sourceFile getName)) && !(rdfXmlInferredBaseIriExclusionList contains (sourceFile getName)) && !(ibiExclusionList contains (sourceFile getName))) {
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

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi_ibn.rdf"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "rdf-xml",
          "-dtd",
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
    logger info s"A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML format with an inferred base IRI and inline blank nodes: $fileCount source files"
  }

  "A SesameRdfFormatter" should "be able to do pattern-based IRI replacements" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_replaced.rdf"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-ip", "^http://topbraid.org/countries",
      "-ir", "http://replaced.example.org/countries"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("<!ENTITY countries \"http://replaced.example.org/countries#\">"), "IRI replacement seems to have failed")
  }

  it should "be able to add single-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_single-comments.rdf"))
    val linePrefix = "## "
    val leadingComment = "Start of --> My New Ontology."
    val trailingComment = "End of --> My New Ontology."
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-lc", leadingComment,
      "-tc", trailingComment
    )
    val content = getFileContents(outputFile, "UTF-8")
    val escapedLeadingComment = SesameSortedRdfXmlWriter.escapeCommentText(leadingComment)
    assert(content.contains(linePrefix + escapedLeadingComment), "leading comment insertion seems to have failed")
    val escapedTrailingComment = SesameSortedRdfXmlWriter.escapeCommentText(trailingComment)
    assert(content.contains(linePrefix + escapedTrailingComment), "trailing comment insertion seems to have failed")
  }

  it should "be able to add multi-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_multiple-comments.rdf"))
    val linePrefix = "## "
    val leadingComments = List("Start of: My New Ontology.", "Version 1.")
    val trailingComments = List("End of: My New Ontology.", "Version 1.")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-lc", leadingComments(0), "-lc", leadingComments(1),
      "-tc", trailingComments(0), "-tc", trailingComments(1)
    )
    val content = getFileContents(outputFile, "UTF-8")
    for (comment ← leadingComments) {
      val escapedComment = SesameSortedRdfXmlWriter.escapeCommentText(comment)
      assert(content.contains(linePrefix + escapedComment), "leading comment insertion seems to have failed")
    }
    for (comment ← trailingComments) {
      val escapedComment = SesameSortedRdfXmlWriter.escapeCommentText(comment)
      assert(content.contains(linePrefix + escapedComment), "trailing comment insertion seems to have failed")
    }
  }

  it should "be able to use explicit data typing for strings" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_sdt_explicit.rdf"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-sdt", "explicit"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("rdf:datatype=\"&xsd;string\""), "explicit string data typing seems to have failed")
  }

  it should "be able to override the language for all strings" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_override_language.rdf"))
    val overrideLanguage = "en-us"
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-osl", overrideLanguage
    )

    // Read in output file & test that all strings have the override language
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputModel = Rio parse (new FileReader(outputFile), baseIri stringValue, RDFFormat.RDFXML)
    for (statement: Statement ← outputModel.iterator.asScala) {
      val obj = statement.getObject;
      if (obj.isInstanceOf[Literal]) {
        val lit = obj.asInstanceOf[Literal]
        if (lit.getLanguage.isPresent) {
          assert(lit.getLanguage.get == overrideLanguage, s"literal language should have been forced to '$overrideLanguage' but was: 'lit.getLanguage.get'")
        } else if (lit.getDatatype.stringValue == xsString) {
          assert(false, s"string literal did not have any language set: ${lit.stringValue}")
        }
      }
    }
  }

  it should "be able to use set the indent string" in {
    val outputDir1 = createTempDir(rootOutputDir1, "rdfxml")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_spaces.rdf"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-i", "  "
    )
    val content = getFileContents(outputFile, "UTF-8")
    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).toArray.size
    assert(singleIndentLineCount >= 1, "double-space indent has failed")

    val outputFile2 = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_tabs.rdf"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile2 getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-i", "\t\t"
    )
    val content2 = getFileContents(outputFile2, "UTF-8")
    val singleIndentLineCount2 = content2.lines.filter(_.matches("^\t\t\\S.*$")).toArray.size
    assert(singleIndentLineCount2 >= 1, "double-tab indent has failed")
  }

}
