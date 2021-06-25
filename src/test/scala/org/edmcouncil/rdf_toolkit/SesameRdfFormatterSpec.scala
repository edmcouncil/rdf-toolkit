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

import org.edmcouncil.rdf_toolkit.io.DirectoryWalker

import java.io.{File, FileInputStream, FileOutputStream, PrintStream}
import java.util.regex.{Matcher, Pattern}
import scala.language.postfixOps
import scala.collection.JavaConverters._
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

class SesameRdfFormatterSpec extends FlatSpec with Matchers with SesameSortedWriterSpecSupport /*with OutputSuppressor*/ {

  override val logger = LoggerFactory getLogger classOf[SesameRdfFormatterSpec]

  val rootOutputDir1 = mkCleanDir(s"target/temp/${this.getClass.getName}")

  def serializeStandardInputToStandardOutput(outputDir: File, inputFile: File, inputFormat: String, outputFormat: String, outputSuffix: String): Unit = {
    val originalIn = System in
    val originalOut = System out

    try {
      val outputFile = constructTargetFile(inputFile, resourceDir, outputDir, Some(outputSuffix))
      System setIn (new FileInputStream(inputFile))
      System setOut (new PrintStream(new FileOutputStream(outputFile)))

      RdfFormatter run Array[String](
        "-sfmt", inputFormat,
        "-tfmt", outputFormat
      )

      assert(outputFile exists, s"file missing in outputDir: ${outputFile.getAbsolutePath}")
      assert(compareFiles(inputFile, outputFile, "UTF-8"), s"file mismatch between inputFile and outputFile: ${inputFile.getName} | ${outputFile.getName}")
    } catch {
      case t: Throwable ⇒ throw t
    } finally {
      System setIn originalIn
      System setOut originalOut
    }
  }

  "A SesameRdfFormatter" should "be able to use the standard input and output" in {
    val inputFile = new File("src/test/resources/other/topbraid-countries-ontology.ttl")
    val outputDir = createTempDir(rootOutputDir1, "turtle")

    serializeStandardInputToStandardOutput(outputDir, inputFile, "turtle", "turtle", ".ttl")
    serializeStandardInputToStandardOutput(outputDir, inputFile, "turtle", "rdf-xml", ".rdf")
    serializeStandardInputToStandardOutput(outputDir, inputFile, "turtle", "json-ld", ".jsonld")
  }

  def processDirectory(format: String, fileExt: String): Unit = {
    val sourceDirPath = "src/test/resources"
    val sourcePatternString = s"^(.*)\\.$fileExt$$"
    val sourcePattern = Pattern.compile(sourcePatternString)
    val targetPatternString = s"$$1.fmt.$fileExt"

    RdfFormatter run Array[String](
      "-sd", sourceDirPath,
      "-sdp", sourcePatternString,
      "-sfmt", format,
      "-td", rootOutputDir1.getAbsolutePath,
      "-tdp", targetPatternString,
      "-tfmt", format
    )

    // Check the generated files
    val dw = new DirectoryWalker(new File(sourceDirPath), sourcePattern);
    for (sourceResult ← dw.pathMatches.asScala) {
      val sourceMatcher = sourcePattern.matcher(sourceResult.getRelativePath)
      val targetRelativePath = sourceMatcher.replaceFirst(targetPatternString)
      val targetFile = new File(rootOutputDir1, targetRelativePath)
      assert(targetFile exists, s"target file not created: ${targetFile.getAbsolutePath}")
      compareFiles(sourceResult getFile, targetFile, "UTF-8")
    }
  }

  it should "be able to process the Turtle files in a directory" in {
    processDirectory("turtle", "ttl")
  }

  it should "be able to process the RDF/XML files in a directory" in {
    processDirectory("rdf-xml", "rdf")
  }

}
