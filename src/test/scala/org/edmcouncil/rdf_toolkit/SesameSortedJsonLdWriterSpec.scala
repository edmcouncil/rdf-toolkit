package org.edmcouncil.rdf_toolkit

import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriter.ShortIriPreferences
import org.openrdf.rio.jsonld.{ JSONLDWriterFactory, JSONLDWriter }
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.language.postfixOps

import java.io._

import org.openrdf.model.impl.SimpleValueFactory
import org.openrdf.rio.{ RDFFormat, Rio }
import org.scalatest.{ Matchers, FlatSpec }

/**
  * ScalaTest tests for the SesameSortedJsonLdWriter and SesameSortedRDFWriterFactory.
  */
class SesameSortedJsonLdWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory getLogger classOf[SesameSortedJsonLdWriterSpec]

  val outputDir0 = mkCleanDir(s"target//temp//${classOf[JSONLDWriter].getName}")
  val outputDir1 = mkCleanDir(s"target//temp//${this.getClass.getName}")
  val outputDir2 = mkCleanDir(s"target//temp//${this.getClass.getName}_2")

  val valueFactory = SimpleValueFactory getInstance ()

  /** Exclusion list of examples containing inline blank nodes. */
  val jsonldInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf")

  "A SortedRDFWriterFactory" should "be able to create a SortedJsonLdWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)

    val writer1 = new SesameSortedJsonLdWriter(System.out)
    assert(writer1 != null, "failed to create default SortedJsonLdWriter from OutputStream")

    val writer2 = new SesameSortedJsonLdWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedJsonLdWriter from Writer")

    val writer3Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.prefix)
    val writer3 = new SesameSortedJsonLdWriter(System.out, writer3Options)
    assert(writer3 != null, "failed to create default SortedJsonLdWriter from OutputStream with parameters")

    val writer4Options = Map("baseIri" -> valueFactory.createIRI("http://example.com#"), "indent" -> "\t\t", "shortIriPref" -> ShortIriPreferences.base_iri)
    val writer4 = new SesameSortedJsonLdWriter(outWriter, writer4Options)
    assert(writer4 != null, "failed to create default SortedJsonLdWriter from Writer")
  }

  "A JSONLDWriter" should "be able to read various RDF documents and write them in JSON-LD format" in {
    val rawRdfDirectory = new File("src/test/resources")
    assert(rawRdfDirectory isDirectory, "raw RDF directory is not a directory")
    assert(rawRdfDirectory exists, "raw RDF directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir0, setFilePathExtension(sourceFile getName, ".jsonld"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new JSONLDWriterFactory()
      val jsonLdWriter = factory getWriter (outWriter)
      val rdfFormat = (Rio getParserFormatForFileName (sourceFile getName)).get()

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, jsonLdWriter)
      outWriter flush ()
      outWriter close ()
    }
  }

  "A SortedJsonLdWriter" should "be able to produce a sorted JSON-LD file" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri)
    val jsonLdWriter = factory getWriter (outWriter, jsonLdWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri)
    val jsonLdWriter2 = factory getWriter (outWriter2, jsonLdWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseIri stringValue, RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with blank object nodes" in {
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with blank subject nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-17.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter (outWriter)

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with directly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-14.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file with indirectly recursive blank object nodes" in {
    val inputFile = new File("src/test/resources/rdf_turtle_spec/turtle-example-26.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".jsonld"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriter = factory getWriter outWriter

    val inputModel = Rio parse (new FileReader(inputFile), "", RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2 = factory getWriter outWriter2

    val inputModel2 = Rio parse (new FileReader(outputFile), "", RDFFormat.JSONLD)
    Rio write (inputModel2, jsonLdWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted JSON-LD file preferring prefix over base IRI" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_prefix.jsonld")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val jsonLdWriter = factory getWriter (outWriter, jsonLdWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("countries:AD"), "prefix preference has failed (1a)")
    assert(!contents1.contains("#AD"), "prefix preference has failed (1b)")
    assert(contents1.contains("Åland"), "prefix preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_prefix.jsonld")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.prefix)
    val jsonLdWriter2 = factory getWriter (outWriter2, jsonLdWriter2Options)

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
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseIri = valueFactory.createIRI("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_base_iri.jsonld")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
    val jsonLdWriterOptions = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val jsonLdWriter = factory getWriter (outWriter, jsonLdWriterOptions)

    val inputModel = Rio parse (new InputStreamReader(new FileInputStream(inputFile), "UTF-8"), baseIri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, jsonLdWriter)
    outWriter flush ()
    outWriter close ()
    val contents1 = getFileContents(outputFile, "UTF-8")
    assert(contents1.contains("#AD"), "base IRI preference has failed (1a)")
    assert(!contents1.contains("countries:AD"), "base IRI preference has failed (1b)")
    assert(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)")

    val outputFile2 = new File(outputDir2, "topbraid-countries-ontology_base_iri.jsonld")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val jsonLdWriter2Options = Map("baseIri" -> baseIri, "shortIriPref" -> ShortIriPreferences.base_iri)
    val jsonLdWriter2 = factory getWriter (outWriter2, jsonLdWriter2Options)

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
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }
  }

  it should "be able to sort RDF triples consistently when writing in JSON-LD format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted JSON-LD.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".jsonld"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-sfmt", "json-ld",
        "-t", targetFile getAbsolutePath,
        "-tfmt", "json-ld"
      )
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0
    for (file1 ← listDirTreeFiles(outputDir1)) {
      fileCount += 1
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

//  it should "should not add/lose RDF triples when writing in Turtle format without inline blank nodes" in {
//    val rawTurtleDirectory = new File("src/test/resources")
//    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
//    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")
//
//    // Serialise sample files as sorted Turtle
//    var fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
//      fileCount += 1
//      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".jsonld"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath
//      )
//    }
//
//    // Re-serialise the sorted files, again as sorted Turtle.
//    fileCount = 0
//    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_iri")) {
//      fileCount += 1
//      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".jsonld"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath
//      )
//    }
//
//    // Check that re-serialising the Turtle files has changed nothing.
//    fileCount = 0
//    for (file1 ← listDirTreeFiles(outputDir1)) {
//      fileCount += 1
//      val file2 = new File(outputDir2, file1 getName)
//      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
//      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
//    }
//
//    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
//    fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
//      fileCount += 1
//      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".jsonld"))
//      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
//      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
//      val inputModel2 = Rio parse (new FileReader(targetFile), "", RDFFormat.TURTLE)
//      assertTriplesMatch(inputModel1, inputModel2)
//    }
//  }
//
//  it should "be able to produce a sorted Turtle file with inline blank nodes" in {
//    val inputFile = new File("src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf")
//    val baseIri = valueFactory.createIRI("http://www.omg.org/spec/EDMC-FIBO/FND/Accounting/AccountingEquity/")
//    val outputFile = new File(outputDir1, "AccountingEquity_inline_blank_nodes.ttl")
//    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
//    val factory = new SesameSortedRDFWriterFactory(SesameSortedRDFWriterFactory.TargetFormats.json_ld)
//    val turtleWriterOptions = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
//    val turtleWriter = factory getWriter (outWriter, turtleWriterOptions)
//
//    val inputModel = Rio parse (new FileReader(inputFile), baseIri stringValue, RDFFormat.RDFXML)
//    Rio write (inputModel, turtleWriter)
//    outWriter flush ()
//    outWriter close ()
//
//    val outputFile2 = new File(outputDir2, "AccountingEquity_inline_blank_nodes.ttl")
//    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
//    val turtleWriter2Options = Map[String, AnyRef]("baseIri" -> baseIri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
//    val turtleWriter2 = factory getWriter (outWriter2, turtleWriter2Options)
//
//    val inputModel2 = Rio parse (new FileReader(outputFile), baseIri stringValue, RDFFormat.TURTLE)
//    Rio write (inputModel2, turtleWriter2)
//    outWriter2 flush ()
//    outWriter2 close ()
//  }
//
//  it should "be able to sort RDF triples consistently when writing in Turtle format with inline blank nodes" in {
//    val rawTurtleDirectory = new File("src/test/resources")
//    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
//    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")
//
//    // Serialise sample files as sorted Turtle.
//    var fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
//      fileCount += 1
//      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibn.ttl"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath,
//        "-ibn"
//      )
//    }
//
//    // Re-serialise the sorted files, again as sorted Turtle.
//    fileCount = 0
//    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
//      fileCount += 1
//      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".jsonld"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath,
//        "-ibn"
//      )
//    }
//
//    // Check that re-serialising the Turtle files has changed nothing.
//    fileCount = 0
//    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn")) {
//      fileCount += 1
//      val file2 = new File(outputDir2, file1 getName)
//      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
//      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
//    }
//  }
//
//  it should "should not add/lose RDF triples when writing in Turtle format with inline blank nodes" in {
//    val rawTurtleDirectory = new File("src/test/resources")
//    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
//    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")
//
//    // Serialise sample files as sorted Turtle
//    var fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if turtleInlineBlankNodesExclusionList contains sourceFile.getName) {
//      fileCount += 1
//      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "_ibn2.ttl"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath,
//        "-ibn"
//      )
//    }
//
//    // Re-serialise the sorted files, again as sorted Turtle.
//    fileCount = 0
//    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
//      fileCount += 1
//      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".jsonld"))
//      SesameRdfFormatter run Array[String](
//        "-s", sourceFile getAbsolutePath,
//        "-t", targetFile getAbsolutePath,
//        "-ibn"
//      )
//    }
//
//    // Check that re-serialising the Turtle files has changed nothing.
//    fileCount = 0
//    for (file1 ← listDirTreeFiles(outputDir1) if file1.getName.contains("_ibn2")) {
//      fileCount += 1
//      val file2 = new File(outputDir2, file1 getName)
//      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
//      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
//    }
//
//    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
//    fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if turtleInlineBlankNodesExclusionList contains sourceFile.getName) {
//      fileCount += 1
//      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "_ibn2.ttl"))
//      val rdfFormat1 = (Rio getParserFormatForFileName (sourceFile getName)).get()
//      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
//      val inputModel2 = Rio parse (new FileReader(targetFile), "", RDFFormat.TURTLE)
//      assertTriplesMatch(inputModel1, inputModel2)
//    }
//  }
//
//  it should "be able to read various RDF documents and write them in sorted Turtle format with an inferred base IRI" in {
//    val rawTurtleDirectory = new File("src/test/resources")
//    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
//    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")
//
//    var fileCount = 0
//    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if sourceFile.getName.endsWith(".jsonld")) {
//      fileCount += 1
//
//      val sourceReader = new BufferedReader(new FileReader(sourceFile))
//      var baseLine1: String = null
//      var unfinished = true
//      var hasOntologyIri = false
//      while (unfinished) {
//        val line = sourceReader.readLine()
//        if (line == null) {
//          unfinished = false
//        } else if (line.contains("owl:Ontology")) {
//          hasOntologyIri = true
//        } else if (baseLine1 == null) {
//          if (line.startsWith("# baseURI:")) {
//            baseLine1 = line
//          } else if (line.startsWith("@base")) {
//            baseLine1 = line
//          }
//        }
//      }
//
//      if (hasOntologyIri && (baseLine1 != null)) {
//        val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibu.ttl"))
//        SesameRdfFormatter run Array[String](
//          "-s", sourceFile getAbsolutePath,
//          "-t", targetFile getAbsolutePath,
//          "-ibi"
//        )
//
//        val targetReader = new BufferedReader(new FileReader(targetFile))
//        var baseLine2: String = null
//        unfinished = true
//        while (unfinished) {
//          val line = targetReader.readLine()
//          if (line == null) {
//            unfinished = false
//          } else if (baseLine2 == null) {
//            if (line.startsWith("# baseURI:")) {
//              baseLine2 = line
//            } else if (line.startsWith("@base")) {
//              baseLine2 = line
//            }
//          }
//        }
//
//        assert(baseLine1 === baseLine2, "base IRI changed - was ontology IRI different to the base IRI?")
//      }
//    }
//  }
//
//  "A SesameRdfFormatter" should "be able to do pattern-based IRI replacements" in {
//    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
//    val outputFile = new File(outputDir1, "topbraid-countries-ontology_replaced.ttl")
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile getAbsolutePath,
//      "-ip", "^http://topbraid.org/countries",
//      "-ir", "http://replaced.example.org/countries"
//    )
//    val content = getFileContents(outputFile, "UTF-8")
//    assert(content.contains("@prefix countries: <http://replaced.example.org/countries#> ."), "IRI replacement seems to have failed")
//  }
//
//  it should "be able to add single-line leading and trailing comments" in {
//    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
//    val outputFile = new File(outputDir1, "topbraid-countries-ontology_single-comments.ttl")
//    val linePrefix = "## "
//    val leadingComment = "Start of: My New Ontology."
//    val trailingComment = "End of: My New Ontology."
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile getAbsolutePath,
//      "-lc", leadingComment,
//      "-tc", trailingComment
//    )
//    val content = getFileContents(outputFile, "UTF-8")
//    assert(content.contains(linePrefix + leadingComment), "leading comment insertion seems to have failed")
//    assert(content.contains(linePrefix + trailingComment), "trailing comment insertion seems to have failed")
//  }
//
//  it should "be able to add multi-line leading and trailing comments" in {
//    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
//    val outputFile = new File(outputDir1, "topbraid-countries-ontology_multiple-comments.ttl")
//    val linePrefix = "## "
//    val leadingComments = List("Start of: My New Ontology.", "Version 1.")
//    val trailingComments = List("End of: My New Ontology.", "Version 1.")
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile getAbsolutePath,
//      "-lc", leadingComments(0), "-lc", leadingComments(1),
//      "-tc", trailingComments(0), "-tc", trailingComments(1)
//    )
//    val content = getFileContents(outputFile, "UTF-8")
//    for (comment ← leadingComments) {
//      assert(content.contains(linePrefix + comment), "leading comment insertion seems to have failed")
//    }
//    for (comment ← trailingComments) {
//      assert(content.contains(linePrefix + comment), "trailing comment insertion seems to have failed")
//    }
//  }
//
//  it should "be able to use explicit data typing for strings" in {
//    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
//    val outputFile = new File(outputDir1, "topbraid-countries-ontology_sdt-explicit.ttl")
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile getAbsolutePath,
//      "-sdt", "explicit"
//    )
//    val content = getFileContents(outputFile, "UTF-8")
//    assert(content.contains("^^xsd:string"), "explicit string data typing seems to have failed")
//  }
//
//  it should "be able to use set the indent string" in {
//    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
//    val outputFile = new File(outputDir1, "topbraid-countries-ontology_indent_spaces.ttl")
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile getAbsolutePath,
//      "-i", "  "
//    )
//    val content = getFileContents(outputFile, "UTF-8")
//    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).size
//    assert(singleIndentLineCount >= 1, "double-space indent has failed")
//
//    val outputFile2 = new File(outputDir1, "topbraid-countries-ontology_indent_tabs.ttl")
//    SesameRdfFormatter run Array[String](
//      "-s", inputFile getAbsolutePath,
//      "-t", outputFile2 getAbsolutePath,
//      "-i", "\t\t"
//    )
//    val content2 = getFileContents(outputFile2, "UTF-8")
//    val singleIndentLineCount2 = content2.lines.filter(_.matches("^\t\t\\S.*$")).size
//    assert(singleIndentLineCount2 >= 1, "double-tab indent has failed")
//  }

}
