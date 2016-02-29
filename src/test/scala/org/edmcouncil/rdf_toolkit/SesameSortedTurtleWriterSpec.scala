package org.edmcouncil.rdf_toolkit

import java.nio.charset.Charset

import org.edmcouncil.rdf_toolkit.SesameSortedRDFWriter.ShortUriPreferences
import org.openrdf.rio.turtle.{ TurtleWriterFactory, TurtleWriter }
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.io.{ Codec, BufferedSource }
import scala.language.postfixOps

import java.io._

import org.openrdf.model.impl.URIImpl
import org.openrdf.rio.{ RDFFormat, Rio }
import org.scalatest.{ Matchers, FlatSpec }

/**
 * ScalaTest tests for the SesameSortedTurtleWriter and SesameSortedRDFWriterFactory.
 */
class SesameSortedTurtleWriterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory.getLogger(classOf[SesameSortedTurtleWriterSpec])

  val outputDir0 = mkCleanDir(s"target//temp//${classOf[TurtleWriter].getName}")
  val outputDir1 = mkCleanDir(s"target//temp//${this.getClass.getName}")
  val outputDir2 = mkCleanDir(s"target//temp//${this.getClass.getName}_2")

  /** Exclusion list of examples containing inline blank nodes. */
  val turtleInlineBlankNodesExclusionList = List("allemang-FunctionalEntities.rdf")

  "A SortedRDFWriterFactory" should "be able to create a SortedTurtleWriter" in {
    val outWriter = new OutputStreamWriter(System.out)
    val factory = new SesameSortedRDFWriterFactory()

    val writer1 = new SesameSortedTurtleWriter(System.out)
    assert(writer1 != null, "failed to create default SortedTurtleWriter from OutputStream")

    val writer2 = new SesameSortedTurtleWriter(outWriter)
    assert(writer2 != null, "failed to create default SortedTurtleWriter from Writer")

    val writer3Options = Map("baseUri" -> new URIImpl("http://example.com#"), "indent" -> "\t\t", "shortUriPref" -> ShortUriPreferences.prefix)
    val writer3 = new SesameSortedTurtleWriter(System.out, writer3Options)
    assert(writer3 != null, "failed to create default SortedTurtleWriter from OutputStream with parameters")

    val writer4Options = Map("baseUri" -> new URIImpl("http://example.com#"), "indent" -> "\t\t", "shortUriPref" -> ShortUriPreferences.base_uri)
    val writer4 = new SesameSortedTurtleWriter(outWriter, writer4Options)
    assert(writer4 != null, "failed to create default SortedTurtleWriter from Writer")
  }

  "A TurtleWriter" should "be able to read various RDF documents and write them in Turtle format" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir0, setFilePathExtension(sourceFile getName, ".ttl"))
      val outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")
      val factory = new TurtleWriterFactory()
      val turtleWriter = factory getWriter (outWriter)
      val rdfFormat = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.TURTLE)

      val inputModel = Rio parse (new FileReader(sourceFile), "", rdfFormat)
      Rio write (inputModel, turtleWriter)
      outWriter flush ()
      outWriter close ()
    }
  }

  "A SortedTurtleWriter" should "be able to produce a sorted Turtle file" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val baseUri = new URIImpl("http://topbraid.org/countries")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseUri" -> baseUri)
    val turtleWriter = factory getWriter (outWriter, turtleWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, outputFile getName)
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map("baseUri" -> baseUri)
    val turtleWriter2 = factory getWriter (outWriter2, turtleWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to produce a sorted Turtle file with blank object nodes" in {
    val inputFile = new File("src/test/resources/other/topquadrant-extended-turtle-example.ttl")
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
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
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
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
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
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
    val outputFile = new File(outputDir1, setFilePathExtension(inputFile getName, ".ttl"))
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
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
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseUri" -> baseUri, "shortUriPref" -> ShortUriPreferences.prefix)
    val turtleWriter = factory getWriter (outWriter, turtleWriterOptions)

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
    val turtleWriter2Options = Map("baseUri" -> baseUri, "shortUriPref" -> ShortUriPreferences.prefix)
    val turtleWriter2 = factory getWriter (outWriter2, turtleWriter2Options)

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
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map("baseUri" -> baseUri, "shortUriPref" -> ShortUriPreferences.base_uri)
    val turtleWriter = factory getWriter (outWriter, turtleWriterOptions)

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
    val turtleWriter2Options = Map("baseUri" -> baseUri, "shortUriPref" -> ShortUriPreferences.base_uri)
    val turtleWriter2 = factory getWriter (outWriter2, turtleWriter2Options)

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
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".ttl"))
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
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
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

  it should "should not add/lose RDF triples when writing in Turtle format without inline blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted Turtle
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, ".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if !sourceFile.getName.contains("_prefix") && !sourceFile.getName.contains("_base_uri")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath
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

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if !(turtleInlineBlankNodesExclusionList contains sourceFile.getName)) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".ttl"))
      val rdfFormat1 = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.TURTLE)
      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
      val inputModel2 = Rio parse (new FileReader(targetFile), "", RDFFormat.TURTLE)
      assert(inputModel1.size() === inputModel2.size(), s"ingested triples do not match for: ${sourceFile.getName}")
    }
  }

  it should "be able to produce a sorted Turtle file with inline blank nodes" in {
    val inputFile = new File("src/test/resources/fibo/fnd/Accounting/AccountingEquity.rdf")
    val baseUri = new URIImpl("http://www.omg.org/spec/EDMC-FIBO/FND/Accounting/AccountingEquity/")
    val outputFile = new File(outputDir1, "AccountingEquity_inline_blank_nodes.ttl")
    val outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")
    val factory = new SesameSortedRDFWriterFactory()
    val turtleWriterOptions = Map[String, AnyRef]("baseUri" -> baseUri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val turtleWriter = factory getWriter (outWriter, turtleWriterOptions)

    val inputModel = Rio parse (new FileReader(inputFile), baseUri stringValue, RDFFormat.RDFXML)
    Rio write (inputModel, turtleWriter)
    outWriter flush ()
    outWriter close ()

    val outputFile2 = new File(outputDir2, "AccountingEquity_inline_blank_nodes.ttl")
    val outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8")
    val turtleWriter2Options = Map[String, AnyRef]("baseUri" -> baseUri, "inlineBlankNodes" -> java.lang.Boolean.TRUE)
    val turtleWriter2 = factory getWriter (outWriter2, turtleWriter2Options)

    val inputModel2 = Rio parse (new FileReader(outputFile), baseUri stringValue, RDFFormat.TURTLE)
    Rio write (inputModel2, turtleWriter2)
    outWriter2 flush ()
    outWriter2 close ()
  }

  it should "be able to sort RDF triples consistently when writing in Turtle format with inline blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted Turtle.
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory)) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibn.ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".ttl"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }
  }

  it should "should not add/lose RDF triples when writing in Turtle format with inline blank nodes" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    // Serialise sample files as sorted Turtle
    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if turtleInlineBlankNodesExclusionList contains sourceFile.getName) {
      fileCount += 1
      val targetFile = new File(outputDir1, setFilePathExtension(sourceFile getName, "_ibn2.ttl"))
      SesameRdfFormatter run Array[String](
        "-s", sourceFile getAbsolutePath,
        "-t", targetFile getAbsolutePath,
        "-ibn"
      )
    }

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(outputDir1) if sourceFile.getName.contains("_ibn2")) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, ".ttl"))
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
      val file2 = new File(outputDir2, file1 getName)
      assert(file2 exists, s"file missing in outputDir2: ${file2.getName}")
      assert(compareFiles(file1, file2, "UTF-8"), s"file mismatch between outputDir1 and outputDir2: ${file1.getName}")
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if turtleInlineBlankNodesExclusionList contains sourceFile.getName) {
      fileCount += 1
      val targetFile = new File(outputDir2, setFilePathExtension(sourceFile getName, "_ibn2.ttl"))
      val rdfFormat1 = Rio getParserFormatForFileName (sourceFile getName, RDFFormat.RDFXML)
      val inputModel1 = Rio parse (new FileReader(sourceFile), "", rdfFormat1)
      val inputModel2 = Rio parse (new FileReader(targetFile), "", RDFFormat.TURTLE)
      assert(inputModel1.size() === inputModel2.size(), s"ingested triples do not match for: ${sourceFile.getName}")
    }
  }

  it should "be able to read various RDF documents and write them in sorted Turtle format with an inferred base URI" in {
    val rawTurtleDirectory = new File("src/test/resources")
    assert(rawTurtleDirectory isDirectory, "raw turtle directory is not a directory")
    assert(rawTurtleDirectory exists, "raw turtle directory does not exist")

    var fileCount = 0
    for (sourceFile ← listDirTreeFiles(rawTurtleDirectory) if sourceFile.getName.endsWith(".ttl")) {
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
          if (line.startsWith("# baseURI:")) {
            baseLine1 = line
          } else if (line.startsWith("@base")) {
            baseLine1 = line
          }
        }
      }

      if (hasOntologyUri && (baseLine1 != null)) {
        val targetFile = new File(outputDir1, setFilePathExtension(sourceFile.getName, "_ibu.ttl"))
        SesameRdfFormatter run Array[String](
          "-s", sourceFile getAbsolutePath,
          "-t", targetFile getAbsolutePath,
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
            if (line.startsWith("# baseURI:")) {
              baseLine2 = line
            } else if (line.startsWith("@base")) {
              baseLine2 = line
            }
          }
        }

        assert(baseLine1 === baseLine2, "base URI changed - was ontology URI different to the base URI?")
      }
    }
  }

  "A SesameRdfFormatter" should "be able to do pattern-based URI replacements" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_replaced.ttl")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-up", "^http://topbraid.org/countries",
      "-ur", "http://replaced.example.org/countries"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("@prefix countries: <http://replaced.example.org/countries#> ."), "URI replacement seems to have failed")
  }

  it should "be able to add single-line leading and trailing comments" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_single-comments.ttl")
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
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_multiple-comments.ttl")
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
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_sdt-explicit.ttl")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-sdt", "explicit"
    )
    val content = getFileContents(outputFile, "UTF-8")
    assert(content.contains("^^xsd:string"), "explicit string data typing seems to have failed")
  }

  it should "be able to use set the indent string" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputFile = new File(outputDir1, "topbraid-countries-ontology_indent_spaces.ttl")
    SesameRdfFormatter run Array[String](
      "-s", inputFile getAbsolutePath,
      "-t", outputFile getAbsolutePath,
      "-i", "  "
    )
    val content = getFileContents(outputFile, "UTF-8")
    val singleIndentLineCount = content.lines.filter(_.matches("^  \\S.*$")).size
    assert(singleIndentLineCount >= 1, "double-space indent has failed")

    val outputFile2 = new File(outputDir1, "topbraid-countries-ontology_indent_tabs.ttl")
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
