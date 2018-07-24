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
import org.eclipse.rdf4j.rio.jsonld.{ JSONLDWriter, JSONLDWriterFactory }
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.language.postfixOps
import java.io._

import org.eclipse.rdf4j.model.{ Literal, Statement }
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.{ RDFFormat, Rio }
import org.scalatest.{ FlatSpec, Matchers }

/**
 * ScalaTest tests for the SesameSortedJsonLdWriter and SesameSortedRDFWriterFactory.
 */
class SesameSortedJsonLdWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory getLogger classOf[SesameSortedJsonLdWriterSpec]

  val rootOutputDir0 = mkCleanDir(s"target/temp/${classOf[JSONLDWriter].getName}")
  val rootOutputDir1 = mkCleanDir(s"target/temp/${this.getClass.getName}")
  val rootOutputDir2 = mkCleanDir(s"target/temp/${this.getClass.getName}_2")
  val rootOutputDir3 = mkCleanDir(s"target/temp/${this.getClass.getName}_3")

  val valueFactory = SimpleValueFactory getInstance ()

  /** Exclusion list of examples containing inline blank nodes. */
  val jsonldInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf", "turtle-example-14.ttl", "turtle-example-25.ttl", "turtle-example-26.ttl")

  "A SortedRDFWriterFactory" should "be able to create a SortedJsonLdWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)

    val writer1 = new SesameSortedJsonLdWriter(System.out)
    assert(writer1 != null, "failed to create default SortedJsonLdWriter from OutputStream")

    val writer2 = new SesameSortedJsonLdWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedJsonLdWriter from Writer")

    val writer3Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.prefix)
    val writer3 = new SesameSortedJsonLdWriter(System.out, mapAsJavaMap[String, Object](writer3Options))
    assert(writer3 != null, "failed to create default SortedJsonLdWriter from OutputStream with parameters")

    val writer4Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.base_iri)
    val writer4 = new SesameSortedJsonLdWriter(outWriter, mapAsJavaMap[String, Object](writer4Options))
    assert(writer4 != null, "failed to create default SortedJsonLdWriter from Writer")
  }

  "A JSONLDWriter" should "be able to read various RDF documents and write them in JSON-LD format" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir0 = createTempDir(rootOutputDir0, "jsonld")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir0, Some(".jsonld"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new JSONLDWriterFactory()
      val jsonLdWriter = factory getWriter (outWriter)
      val rdfFormat = (Rio getParserFormatForFileName (sourceFile getName)).get()

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, jsonLdWriter)
      outWriter flush ()
      outWriter close ()
    }
    logger info s"A JSONLDWriter should be able to read various RDF documents and write them in JSON-LD format: $fileCount source files"
  }

  "A SortedJsonLdWriter" should "be able to produce a sorted JSON-LD file" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri)
    val jsonLdWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](jsonLdWriterOptions))

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri)
    val jsonLdWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](jsonLdWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with blank subject nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-17.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter (outWriter)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with directly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with indirectly recursive blank object nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some(".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file preferring prefix over base IRI" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_prefix.jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val jsonLdWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](jsonLdWriterOptions))

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("countries:AD"), "prefix preference has failed (1a)")
    assert(!contents1.contains("#AD"), "prefix preference has failed (1b)")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val jsonLdWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](jsonLdWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("countries:AD"), "prefix preference has failed (2a)")
    assert(!contents2.contains("#AD"), "prefix preference has failed (2b)")
    assert(contents2.contains("Åland"), "prefix preference file has encoding problem (2)")
  }

  it should "be able to produce a sorted JSON-LD file preferring base IRI over prefix" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_base_iri.jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val jsonLdWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](jsonLdWriterOptions))

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("#AD"), "base IRI preference has failed (1a)")
    assert(!contents1.contains("countries:AD"), "base IRI preference has failed (1b)")
    assert(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)")

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val jsonLdWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](jsonLdWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("#AD"), "base IRI preference has failed (2a)")
    assert(!contents2.contains("countries:AD"), "base IRI preference has failed (2b)")
    assert(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)")
  }

  it should "be able to read various RDF documents and write them in sorted JSON-LD format" in {
    val rawJsonLdDirectory = resourceDir
    assert(rawJsonLdDirectory isDirectory, "raw JSON-LD directory is not a directory")
    assert(rawJsonLdDirectory exists, "raw JSON-LD directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawJsonLdDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, resourceDir, outputDir1, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }
    logger info s"A SortedJsonLdWriter should be able to read various RDF documents and write them in sorted JSON-LD format: $fileCount source files"
  }

  it should "be able to sort RDF triples consistently when writing in JSON-LD format" in {
    val rawJsonLdDirectory = resourceDir
    assert(rawJsonLdDirectory isDirectory, "raw JSON-LD directory is not a directory")
    assert(rawJsonLdDirectory exists, "raw JSON-LD directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val outputDir3 = createDir(rootOutputDir3, outputDir1 getName)

    // Serialise sample files as sorted JSON-LD.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawJsonLdDirectory)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, resourceDir, outputDir1, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }
    logger info s"A SortedJsonLdWriter should be able to sort RDF triples consistently when writing in JSON-LD format: $fileCount source files"

    // Re-serialise the sorted files, again as sorted JSON-LD.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-sfmt", "json-ld",
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }

    // Check that re-serialising the JSON-LD files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      if (!compareFiles(file1, file2, "UTF-8", false)) {
        logger.info(s"Retrying: $file2 getName")
        val targetFile = constructTargetFile(file2, outputDir2, outputDir3)
        SesameRdfFormatter run Array[String](
          "-s", file2 getAbsolutePath,
          "-sfmt", "json-ld",
          "-t", targetFile getAbsolutePath,
          "-tfmt", "json-ld"
        )
        assert(targetFile exists, s"file missing in outputDir3: ${targetFile.getAbsolutePath}")
        assert(compareFiles(file2, targetFile, "UTF-8"), s"file mismatch between outputDir2 and outputDir3: ${file2.getName}")
      }
    }
  }

  it should "not add/lose RDF triples when writing in JSON-LD format without inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted JSON-LD
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(jsonldInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }
    logger info s"A SortedJsonLdWriter should not add/lose RDF triples when writing in JSON-LD format without inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted JSON-LD.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-sfmt", "json-ld",
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }

    // Check that re-serialising the JSON-LD files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised JSON-LD file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(jsonldInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some(".jsonld"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", RDFFormat.JSONLD)
      // println(s"[info] Comparing ${sourceFile.getAbsolutePath} to ${targetFile.getAbsolutePath} ...") // TODO: remove debugging
      assertTriplesMatch(inputModel1, inputModel2)
    }
  }

  it should "be able to produce a sorted JSON-LD file with inline blank nodes" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)
    val inputFile = new File("src/test/resources/fibo/ontology/master/latest/FND/Accounting/AccountingEquity.rdf")
    val baseIri = valueFactory.createIRI("https://spec.edmcouncil.org/fibo/ontology/FND/Accounting/AccountingEquity/")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_inline_blank_nodes.jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val jsonLdWriter = factory getWriter (outWriter, mapAsJavaMap[String, Object](jsonLdWriterOptions))

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = constructTargetFile(outputFile, outputDir1, outputDir2)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val jsonWriter2 = factory getWriter (outWriter2, mapAsJavaMap[String, Object](jsonLdWriter2Options))

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseIri stringValue, RDFFormat.JSONLD)
    Rio write (inputModel2, jsonWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to sort RDF triples consistently when writing in JSON-LD format with inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted JSON-LD.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(jsonldInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn.jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld",
        "-ibn"
      )
    }
    logger info s"A SortedJsonLdWriter should be able to sort RDF triples consistently when writing in JSON-LD format with inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted JSON-LD.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-sfmt", "json-ld",
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld",
        "-ibn"
      )
    }

    // Check that re-serialising the JSON-LD files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "not add/lose RDF triples when writing in JSON-LD format with inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val outputDir2 = createDir(rootOutputDir2, outputDir1 getName)

    // Serialise sample files as sorted JSON-LD
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(jsonldInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibn2.jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld",
        "-ibn"
      )
    }
    logger info s"A SortedJsonLdWriter should not add/lose RDF triples when writing in JSON-LD format with inline blank nodes: $fileCount source files"

    // Re-serialise the sorted files, again as sorted JSON-LD.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, outputDir1, outputDir2, Some(".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-sfmt", "json-ld",
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld",
        "-ibn"
      )
    }

    // Check that re-serialising the JSON-LD files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn2")) {
      fileCount += 1
      val file2 = constructTargetFile(file1, outputDir1, outputDir2)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getAbsolutePath}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised JSON-LD file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory) if !(jsonldInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir2, Some("_ibn2.jsonld"))
      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
      val inputModel1 = Rio parse (new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"), "", rdfFormat1)
      val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(targetFile), "UTF-8"), "", RDFFormat.JSONLD)
      assertTriplesMatch(inputModel1, inputModel2)
    }
  }

  it should "be able to read various RDF documents and write them in sorted JSON-LD format with an inferred base IRI" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")

    val baseSignature = "\"@base\"" // TODO: set this properlyfor JSON-LD

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

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi.jsonld"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "json-ld",
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
    logger info s"A SortedJsonLdWriter should be able to read various RDF documents and write them in sorted JSON-LD format with an inferred base IRI: $fileCount source files"
  }

  it should "be able to read various RDF documents and write them in sorted JSON-LD format with an inferred base IRI and inline blank nodes" in {
    val rawRdfDirectory = resourceDir
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")

    val baseSignature = "\"@base\"" // TODO: set this properlyfor JSON-LD

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

        val targetFile = constructTargetFile(sourceFile, rawRdfDirectory, outputDir1, Some("_ibi_ibn.jsonld"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "json-ld",
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
    logger info s"A SortedJsonLdWriter should be able to read various RDF documents and write them in sorted JSON-LD format with an inferred base IRI and inline blank nodes: $fileCount source files"
  }

  "A SesameRdfFormatter" should "be able to do pattern-based IRI replacements" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_replaced.jsonld"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-ip", "^http://topbraid.org/countries",
      "-ir", "http://replaced.example.org/countries"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("\"countries\" : \"http://replaced.example.org/countries#\""), "IRI replacement seems to have failed")
  }

  it should "*not* add single-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_single-comments.jsonld"))
    val leadingComment = "Start of: My New Ontology."
    val trailingComment = "End of: My New Ontology."
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-lc", leadingComment,
      "-tc", trailingComment
    )
    val content = getFileContents(outputFile, "UTF-8")
    // JSON doesn't support comments, so we don't expect these comments to be added.
    assert(!content.contains(leadingComment), "leading comment insertion was not suppressed!")
    assert(!content.contains(trailingComment), "trailing comment insertion was not suppressed!")
  }

  it should "*not* add multi-line leading and trailing comments" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_multiple-comments.jsonld"))
    val leadingComments = List("Start of: My New Ontology.", "Version 1.")
    val trailingComments = List("End of: My New Ontology.", "Version 1.")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-lc", leadingComments(0), "-lc", leadingComments(1),
      "-tc", trailingComments(0), "-tc", trailingComments(1)
    )
    val content = getFileContents(outputFile, "UTF-8")
    // JSON doesn't support comments, so we don't expect these comments to be added.
    for (comment ← leadingComments) {
      assert(!content.contains(comment), "leading comment insertion was not suppressed")
    }
    for (comment ← trailingComments) {
      assert(!content.contains(comment), "trailing comment insertion was not suppressed")
    }
  }

  it should "be able to use explicit data typing for strings" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_sdt-explicit.jsonld"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-sdt", "explicit"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("\"@type\" : \"xsd:string\""), "explicit string data typing seems to have failed")
  }

  it should "be able to override the language for all strings" in {
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_override_language.jsonld"))
    val overrideLanguage = "en-us"
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-osl", overrideLanguage
    )

    // Read in output file & test that all strings have the override language
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputModel = Rio parse (new FileReader(outputFile), baseIri stringValue, RDFFormat.JSONLD)
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
    val outputDir1 = createTempDir(rootOutputDir1, "jsonld")
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_spaces.jsonld"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "json-ld",
      "-i", "  "
    )
    val content = getFileContents(outputFile, "UTF-8")
    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).size
    assert(singleIndentLineCount >= 1, "double-space indent has failed")

    val outputFile2 = constructTargetFile(inputFile, resourceDir, outputDir1, Some("_indent_tabs.jsonld"))
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile2 getAbsolutePath,
      "-tfmt", "json-ld",
      "-i", "\t\t"
    )
    val content2 = getFileContents(outputFile2, "UTF-8")
    val singleIndentLineCount2 = content2.lines.filter(_.matches("^\t\t\\S.*$")).size
    assert(singleIndentLineCount2 >= 1, "double-tab indent has failed")
  }

}
