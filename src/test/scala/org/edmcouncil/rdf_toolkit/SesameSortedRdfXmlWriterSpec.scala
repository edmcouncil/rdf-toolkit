package org.edmcouncil.rdf_toolkit

import scala.language.postfixOps

import java.io._
import java.nio.charset.Charset
import java.util

import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriter.ShortUriPreferences
import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriterFactory.TargetFormats
import org.openrdf.model.impl.URIImpl
import org.openrdf.rio.{ RDFWriter, RDFFormat, Rio }
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriterFactory
import org.slf4j.LoggerFactory

import org.scalatest.{ Matchers, FlatSpec }

/**
 * ScalaTest tests for the SesameSortedRdfXmlWriter and SesameSortedRDFWriterFactory.
 */
class SesameSortedRdfXmlWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory.getLogger(classOf[SesameSortedRdfXmlWriterSpec])

  val outputDir0 = mkCleanDir(s"target//temp//${classOf[RDFWriter].getName}")
  val outputDir1 = mkCleanDir(s"target//temp//${this.getClass.getName}")
  val outputDir2 = mkCleanDir(s"target//temp//${this.getClass.getName}_2")

  /** Exclusion list of examples that can't be represented directly in RDF/XML. */
  val rdfXmlExclusionList = List("allemang-test-a.ttl", "allemang-test-b.ttl", "turtle-example-2.ttl", "turtle-example-3.ttl", "turtle-example-4.ttl", "turtle-example-5.ttl", "turtle-example-6.ttl", "turtle-example-9.ttl", "turtle-example-17.ttl", "turtle-example-22.ttl")
  // Note: the 'allemang' test files are in this list because they contain strings that end with a whitespace.
  // It seems that the Sesame RDF/XML parser strips that trailing whitespace away; the Sesame Turtle parser does not.
  // This stops the Sesame formatter from being able to treat the 'allemang' examples in RDF/XML format
  // the same as when it is in Turtle format.
  // The other excluded examples don't have a namespace prefix for all predicates, which is a limitation of RDF/XML.

  /** Exclusion list of examples where the ontology URI is different to the base URI. */
  val rdfXmlInferredBaseUriExclusionList = List("food.rdf", "wine.rdf")

  /** Exclusion list of examples containing inline blank nodes. */
  val rdfXmlInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf")

  "A SortedRDFWriterFactory" should "be able to create a SortedRdfXmlWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory(TargetFormats.rdf_xml)

    val writer1 = new SesameSortedRdfXmlWriter(System.out)
    assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

    val writer2 = new SesameSortedRdfXmlWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

    val writer3Options = new util.HashMap[String, Object]()
    writer3Options.put("baseUri", new URIImpl("http://example.com#"))
    writer3Options.put("indent", "\t\t")
    writer3Options.put("shortUriPref", ShortUriPreferences.prefix)
    val writer3 = new SesameSortedRdfXmlWriter(System.out, writer3Options)
    assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream with parameters")

    val writer4Options = new util.HashMap[String, Object]()
    writer4Options.put("baseUri", new URIImpl("http://example.com#"))
    writer4Options.put("indent", "\t\t")
    writer4Options.put("shortUriPref", ShortUriPreferences.base_uri)
    val writer4 = new SesameSortedRdfXmlWriter(outWriter, writer4Options)
    assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
  }

  "An RDFWriter" should "be able to read various RDF documents and write them in RDF/XML format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir0, setFilePathExtension(sourceFile getName, ".rdf"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new RDFXMLPrettyWriterFactory()
      val turtleWriter = factory getWriter (outWriter)
      val rdfFormat = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.RDFXML)

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outWriter flush ()
      outWriter close ()
    }
  }

  "A SortedRdfXmlWriter" should "be able to produce a sorted RDF/XML file" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("baseUri", baseUri)
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("baseUri", baseUri)
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with blank object nodes" in {
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with directly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file with indirectly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".rdf"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted RDF/XML file preferring prefix over base URI" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_prefix.rdf")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_prefix.rdf")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("Åland"), "prefix preference file has encoding problem (2)")
  }

  it should "be able to produce a sorted RDF/XML file preferring base URI over prefix" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_base_uri.rdf")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("Åland"), "base URI preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_base_uri.rdf")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new InputStreamReader(new FileInputStream(outputFile), "UTF-8"), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
    val contents2 = getFileContents(outputFile2, "UTF-8")
    assert(contents2.contains("Åland"), "base URI preference file has encoding problem (2)")
  }

  it should "be able to read various RDF documents and write them in sorted RDF/XML format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(rdfXmlExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }
  }

  it should "be able to sort RDF triples consistently when writing in RDF/XML format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(rdfXmlExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".rdf"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "should not add/lose RDF triples when writing in RDF/XML format without blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd"
      )
    }

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".rdf"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised RDF/XML files have the same triple count as the matching raw files.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".rdf"))
      val rdfFormat1 = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.TURTLE)
      val rdfFormat2 = Rio getParserFormatForFileName (targetFile getName, RDFFormat.TURTLE)
      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
      val inputModel2 = Rio parse (new FileReader(targetFile), "", rdfFormat2)
      assert(inputModel1.size() === inputModel2.size(), s"ingested triples do not match for: ${sourceFile.getName}")
    }
  }

  it should "be able to produce a sorted RDF/XML file with inline blank nodes" in {
    val inputFile = new File("src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf")
    val baseUri = new URIImpl("http://www.omg.org/spec/EDMC-FIBO/FND/Accounting/AccountingEquity/")
    val outputFile = new File(outputDir1, "AccountingEquity_inline_blank_nodes.rdf")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.rdf_xml)
    val rdfXmlWriterOptions = new util.HashMap[String, Object]()
    rdfXmlWriterOptions.put("baseUri", baseUri)
    rdfXmlWriterOptions.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriterOptions.put("inlineBlankNodes", java.lang.Boolean.TRUE);
    val rdfXmlWriter = factory getWriter (outWriter, rdfXmlWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel, rdfXmlWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, "AccountingEquity_inline_blank_nodes.rdf")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val rdfXmlWriter2Options = new util.HashMap[String, Object]()
    rdfXmlWriter2Options.put("baseUri", baseUri)
    rdfXmlWriter2Options.put("useDtdSubset", java.lang.Boolean.TRUE)
    rdfXmlWriter2Options.put("inlineBlankNodes", java.lang.Boolean.TRUE);
    val rdfXmlWriter2 = factory getWriter (outWriter2, rdfXmlWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel2, rdfXmlWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to sort RDF triples consistently when writing in RDF/XML format with inline blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(rdfXmlExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibn.rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".rdf"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "should not add/lose RDF triples when writing in RDF/XML format with blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "_ibn2.rdf"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "rdf-xml",
        "-dtd",
        "-ibn"
      )
    }

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".rdf"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised RDF/XML files have the same triple count as the matching raw files.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if rdfXmlInlineBlankNodesExclusionList contains sourceFile.getName) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "_ibn2.rdf"))
      val rdfFormat1 = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.RDFXML)
      val rdfFormat2 = Rio getParserFormatForFileName (targetFile getName, RDFFormat.RDFXML)
      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
      val inputModel2 = Rio parse (new FileReader(targetFile), "", rdfFormat2)
      assert(inputModel1.size() === inputModel2.size(), s"ingested triples do not match for: ${sourceFile.getName}")
    }
  }

  it should "be able to read various RDF documents and write them in sorted RDF/XML format with an inferred base URI" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if sourceFile.getName.endsWith(".rdf") && !(rdfXmlExclusionList contains sourceFile.getName) && !(rdfXmlInferredBaseUriExclusionList contains sourceFile.getName)) {
      fileCount += 1

      val sourceReader = new BufferedReader(new FileReader(sourceFile))
      var baseLine1: String = null
      var unfinished = true
      var hasOntologyUri = false
      while (unfinished) {
        val line = sourceReader.readLine()
        if (line == null) {
          unfinished = false
        } else if (line.contains("owl:Ontology")) {
          hasOntologyUri = true
        } else if (baseLine1 == null) {
          if (line.trim.startsWith("xml:base")) {
            baseLine1 = line.trim.replaceAll("\\s*=\\s*", "=")
          }
        }
      }

      if (hasOntologyUri && (baseLine1 != null)) {
        val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibu.rdf"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
          "-tfmt", "rdf-xml",
          "-dtd",
          "-ibu"
        )

        val targetReader = new BufferedReader(new FileReader(targetFile))
        var baseLine2: String = null
        unfinished = true
        while (unfinished) {
          val line = targetReader.readLine()
          if (line == null) {
            unfinished = false
          } else if (baseLine2 == null) {
            if (line.trim.startsWith("xml:base")) {
              baseLine2 = line.trim.replaceAll("\\s*=\\s*", "=")
            }
          }
        }

        assert(baseLine1 === baseLine2, "base URI changed - was ontology URI different to the base URI?")
      }
    }
  }

  "A SesameRdfFormatter" should "be able to do pattern-based URI replacements" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_replaced.rdf")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-up", "^http://topbraid.org/countries",
      "-ur", "http://replaced.example.org/countries"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("<!ENTITY countries \"http://replaced.example.org/countries#\">"), "URI replacement seems to have failed")
  }

  it should "be able to add single-line leading and trailing comments" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_single-comments.rdf")
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
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_multiple-comments.rdf")
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
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_sdt_explicit.rdf")
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

  it should "be able to use set the indent string" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_indent_spaces.rdf")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-i", "  "
    )
    val content = getFileContents(outputFile, "UTF-8")
    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).size
    assert(singleIndentLineCount >= 1, "double-space indent has failed")

    val outputFile2 = new File(outputDir1, "topbraid-countries-ontology_indent_tabs.rdf")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile2 getAbsolutePath,
      "-tfmt", "rdf-xml",
      "-dtd",
      "-i", "\t\t"
    )
    val content2 = getFileContents(outputFile2, "UTF-8")
    val singleIndentLineCount2 = content2.lines.filter(_.matches("^\t\t\\S.*$")).size
    assert(singleIndentLineCount2 >= 1, "double-tab indent has failed")
  }

}
