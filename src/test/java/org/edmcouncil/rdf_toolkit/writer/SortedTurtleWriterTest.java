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

package org.edmcouncil.rdf_toolkit.writer;

import static org.edmcouncil.rdf_toolkit.FileSystemUtils.constructTargetPath;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.createDir;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.createTempDir;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.getFileContents;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.getRawRdfDirectory;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.listDirTreeFiles;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.mkCleanDir;
import static org.edmcouncil.rdf_toolkit.TestComparisonsUtils.asInstanceOf;
import static org.edmcouncil.rdf_toolkit.TestComparisonsUtils.assertTriplesMatch;
import static org.edmcouncil.rdf_toolkit.TestComparisonsUtils.compareFiles;
import static org.edmcouncil.rdf_toolkit.TestComparisonsUtils.getBaseIri;
import static org.edmcouncil.rdf_toolkit.TestConstants.IBI_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.XS_STRING;
import static org.edmcouncil.rdf_toolkit.util.Constants.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.util.Constants.INDENT;
import static org.edmcouncil.rdf_toolkit.util.Constants.SHORT_URI_PREF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;
import org.edmcouncil.rdf_toolkit.FileSystemUtils;
import org.edmcouncil.rdf_toolkit.RdfFormatter;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class SortedTurtleWriterTest extends AbstractSortedWriterTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SortedTurtleWriterTest.class);
  private static final String TURTLE_PREFIX = "turtle";
  private static final String TURTLE_STEM = ".ttl";

  private final ValueFactory valueFactory;

  private File rootOutputDir1;
  private File rootOutputDir2;

  public SortedTurtleWriterTest() {
    this.valueFactory = SimpleValueFactory.getInstance();
  }

  @BeforeEach
  public void setUp() {
    rootOutputDir1 = mkCleanDir("target/temp/" + getClass().getName() + "_1");
    rootOutputDir2 = mkCleanDir("target/temp/" + getClass().getName() + "_2");
  }

  @Test
  void shouldBeAbleToCreateSortedTurtleWriter() {
    var outWriter = new OutputStreamWriter(System.out);

    var writer1 = new SortedTurtleWriter(System.out);
    assertNotNull(writer1, "failed to create default SortedTurtleWriter from OutputStream");

    var writer2 = new SortedTurtleWriter(outWriter);
    assertNotNull(writer2, "failed to create default SortedTurtleWriter from Writer");

    Map<String, Object> writer3Options = Map.of(
        BASE_IRI, valueFactory.createIRI("http://example.com#"),
        INDENT, "\t\t",
        SHORT_URI_PREF, ShortIriPreferences.PREFIX);
    var writer3 = new SortedTurtleWriter(System.out, writer3Options);
    assertNotNull(writer3, "failed to create default SortedTurtleWriter from OutputStream with parameters");

    Map<String, Object> writer4Options = Map.of(
        BASE_IRI, valueFactory.createIRI("http://example.com#"),
        INDENT, "\t\t",
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var writer4 = new SortedTurtleWriter(outWriter, writer4Options);
    assertNotNull(writer4, "failed to create default SortedTurtleWriter from Writer");
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInTurtleFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir = createTempDir(rootOutputDir1, TURTLE_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1;
      var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir, TURTLE_STEM);
      var outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8);
      var factory = new TurtleWriterFactory();
      var turtleWriter = factory.getWriter(outWriter);
      var rdfFormat = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();

      var inputModel = Rio.parse(new FileReader(sourceFile), "", rdfFormat);
      Rio.write(inputModel, turtleWriter);
      outWriter.flush();
      outWriter.close();
    }
    LOGGER.info(
        "An RDFWriter should be able to read various RDF documents and write them in Turtle format: {} source files.",
        fileCount);

    assertTrue(fileCount > 0);
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFile() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriterOptions = new HashMap<String, Object>();
    turtleWriterOptions.put("baseIri", baseIri);
    var turtleWriter = factory.getWriter(outWriter, turtleWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var turtleWriterOptions2 = new HashMap<String, Object>();
    turtleWriterOptions2.put("baseIri", baseIri);
    var turtleWriter2 = factory.getWriter(outWriter2, turtleWriterOptions2);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFileWithBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topquadrant-extended-turtle-example.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var turtleWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFileWithBlankSubjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-17.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var turtleWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFileWithDirectlyRecursiveBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-14.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var turtleWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFileWithIndirectlyRecursiveBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-26.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var turtleWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFilePreferringPrefixOverBaseIri() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_prefix.ttl");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    Map<String, Object> turtleWriterOptions = Map.of(
        "baseIri", baseIri,
        "shortIriPref", ShortIriPreferences.PREFIX);
    var turtleWriter = factory.getWriter(outWriter, turtleWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();

    var contents1 = getFileContents(outputFile, "UTF-8");
    assertTrue(contents1.contains("countries:AD"), "prefix preference has failed (1a)");
    assertFalse(contents1.contains("#AD"), "prefix preference has failed (1b)");
    assertTrue(contents1.contains("Åland"), "prefix preference file has encoding problem (1)");

    var outputFile2 = FileSystemUtils.constructTargetPath(outputFile, outputDir1, outputDir2, "_prefix.ttl");
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> turtleWriter2Options = Map.of(
        BASE_IRI, baseIri,
        SHORT_URI_PREF, ShortIriPreferences.PREFIX);

    var turtleWriter2 = factory.getWriter(outWriter2, turtleWriter2Options);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();

    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("countries:AD"), "prefix preference has failed (2a)");
    assertFalse(contents2.contains("#AD"), "prefix preference has failed (2b)");
    assertTrue(contents2.contains("Åland"), "prefix preference file has encoding problem (2)");
  }

  @Test
  void shouldOnlyUseRdfTypeContractionForPredicate() throws Exception {
    var outBuffer = new ByteArrayOutputStream();
    var outWriter = new OutputStreamWriter(outBuffer, StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    var turtleWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(
        new StringReader(
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
            + "rdf:type rdf:type rdf:Property ."
            + "<prop:list> a rdf:List; rdf:first rdf:type; rdf:rest rdf:nil ."
        ), RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();

    var contents1 = outBuffer.toString();
    assertTrue(contents1.contains("rdf:type\n\ta rdf:Property"), "Contraction applied to predicate, not subject");
    assertTrue(contents1.contains("rdf:first rdf:type"), "Contraction not applied to object");
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFilePreferringBaseIriOverPrefix() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile =
        FileSystemUtils.constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, "_base_iri.ttl");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    Map<String, Object> turtleWriterOptions = Map.of(
        BASE_IRI, baseIri,
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var turtleWriter = factory.getWriter(outWriter, turtleWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    var contents1 = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(contents1.contains("#AD"), "base IRI preference has failed (1a)");
    assertFalse(contents1.contains("countries:AD"), "base IRI preference has failed (1b)");
    assertTrue(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> turtleWriter2Options = Map.of(
        BASE_IRI, baseIri,
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, turtleWriter2Options);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("#AD"), "base IRI preference has failed (2a)");
    assertFalse(contents2.contains("countries:AD"), "base IRI preference has failed (2b)");
    assertTrue(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)");
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedTurtleFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var rootOutputDir = createTempDir(rootOutputDir1, TURTLE_PREFIX);

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, rootOutputDir, TURTLE_STEM);
      RdfFormatter.run(
          new String[] {
              "-s", sourceFile.getAbsolutePath(),
              "-t", targetFile.getAbsolutePath(),
              "-tfmt", "turtle"
          }
      );
      if (targetFile.exists()) {
        fileCount += 1;
      }
    }
    LOGGER.info(
        "A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle format: " +
            "{} source files",
        fileCount);

    assertTrue(
        fileCount > 0,
        String.format("Files from %s should be parsed.", rawRdfDirectory));
  }

  @Test
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInTurtleFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    // Serialise sample files as sorted Turtle.
    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1;
      var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
      RdfFormatter.run(
          new String[] {
              "-s", sourceFile.getAbsolutePath(),
              "-t", targetFile.getAbsolutePath()
          }
      );
    }
    LOGGER.info(
        "A SortedTurtleWriter should be able to sort RDF triples consistently when writing in Turtle format: " +
            "{} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (!sourceFile.getName().contains("_prefix") && !sourceFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath()
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in Turtle format: " +
            "{} source files",
        fileCount);

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0;
    for (var targetFile : listDirTreeFiles(outputDir1)) {
      if (!targetFile.getName().contains("_prefix") && !targetFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var file2 = constructTargetPath(targetFile, outputDir1, outputDir2);
        assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());
        assertTrue(
            compareFiles(targetFile, file2, "UTF-8"),
            "File mismatch between outputDir1 and outputDir2: " + targetFile.getName());
      }
    }
  }

  @Test
  void shouldNotAddOrLoseRdfTriplesWhenWritingInTurtleFormatWithoutInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, TURTLE_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath()
            }
        );
      }
    }
    LOGGER.info(
        "A SortedTurtleWriter should not add/lose RDF triples when writing in Turtle format without inline blank " +
            "nodes: {} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (!sourceFile.getName().contains("_prefix") && !sourceFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, outputDir1, outputDir2, TURTLE_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath()
            }
        );
      }
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0;
    for (var targetFile : listDirTreeFiles(outputDir1)) {
      if (!targetFile.getName().contains("_prefix") && !targetFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var file2 = constructTargetPath(targetFile, outputDir1, outputDir2);
        assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());
        assertTrue(
            compareFiles(targetFile, file2, StandardCharsets.UTF_8.name()),
            "File mismatch between outputDir1 and outputDir2: " + targetFile.getName());
      }
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir2, TURTLE_STEM);
        var rdfFormat1 = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();
        var rdfFormat2 = Rio.getParserFormatForFileName(targetFile.getName()).orElseThrow();
        var inputModel1 = Rio.parse(prepareInputStream(sourceFile), "", rdfFormat1);
        var inputModel2 = Rio.parse(prepareInputStream(targetFile), "", rdfFormat2);

        assertTriplesMatch(inputModel1, inputModel2);
      }
    }
  }

  @Test
  void shouldBeAbleToProduceSortedTurtleFileWithInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());
    var inputFile =
        new File(rawRdfDirectory.getPath() + "/fibo/ontology/master/latest/FND/Accounting/AccountingEquity.rdf");
    var baseIri = valueFactory.createIRI("https://spec.edmcouncil.org/fibo/ontology/FND/Accounting/AccountingEquity/");
    var outputFile =
        FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_inline_blank_nodes.ttl");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.TURTLE);
    Map<String, Object> turtleWriterOptions = Map.of(
        "baseIri", baseIri,
        "inlineBlankNodes", true);
    var turtleWriter = factory.getWriter(outWriter, turtleWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.RDFXML);
    Rio.write(inputModel, turtleWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(outputFile.exists(), "Target file should exists");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> turtleWriter2Options = Map.of(
        "baseIri", baseIri,
        "inlineBlankNodes", true);
    var turtleWriter2 = factory.getWriter(outWriter2, turtleWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        baseIri.stringValue(),
        RDFFormat.TURTLE);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(outputFile2.exists(), "Target file should exists");
  }

  @Test
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInTurtleFormatWithInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibn.ttl");
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-ibn"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedTurtleWriter should be able to sort RDF triples consistently when writing in Turtle format with " +
            "inline blank nodes: {} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (sourceFile.getName().contains("_ibn")) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, outputDir1, outputDir2, TURTLE_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-ibn"
            }
        );
      }
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      if (file1.getName().contains("_ibn")) {
        fileCount += 1;
        var file2 = constructTargetPath(file1, outputDir1, outputDir2);
        assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());
        assertTrue(
            compareFiles(file1, file2, StandardCharsets.UTF_8.name()),
            "File mismatch between outputDir1 and outputDir2: " + file1.getName());
      }
    }
  }

  @Test
  void shouldNotBeAddOrLoseRdfTriplesWhenWritingInTurtleFormatWithInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    // Serialise sample files as sorted Turtle
    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibn2.ttl");
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-ibn"
            }
        );
      }
    }
    LOGGER.info("A SortedTurtleWriter should not add/lose RDF triples when writing in Turtle format with inline " +
            "blank nodes: {} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted Turtle.
    fileCount = 0;
    for (var sourceFile : listDirTreeFiles(outputDir1)) {
      if (sourceFile.getName().contains("_ibn2")) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, outputDir1, outputDir2, TURTLE_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-ibn"
            }
        );
      }
    }

    // Check that re-serialising the Turtle files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      if (file1.getName().contains("_ibn2")) {
        fileCount += 1;
        var file2 = constructTargetPath(file1, outputDir1, outputDir2);
        assertTrue(file2.exists(), "file missing in outputDir2: " + file2.getAbsolutePath());
        assertTrue(
            compareFiles(file1, file2, "UTF-8"),
            "file mismatch between outputDir1 and outputDir2: " + file1.getName());
      }
    }

    // Check that the re-serialised Turtle file have the same triple count as the matching raw files
    fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!TURTLE_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir2, "_ibn2.ttl");
        var rdfFormat1 = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();
        var inputModel1 = Rio.parse(prepareInputStream(sourceFile), "", rdfFormat1);
        var inputModel2 = Rio.parse(prepareInputStream(targetFile), "", RDFFormat.TURTLE);
        assertTriplesMatch(inputModel1, inputModel2);
      }
    }
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedTurtleFormatWithInferredBaseIri() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!IBI_EXCLUSION_SET.contains(sourceFile.getName())) {
        var sourceReader = new BufferedReader(new FileReader(sourceFile));
        Optional<String> baseIri1 = Optional.empty();
        var unfinished = true;
        var hasOntologyIri = false;
        while (unfinished) {
          var line = sourceReader.readLine();
          if (line == null) {
            unfinished = false;
          } else if (line.contains("owl:Ontology")) {
            hasOntologyIri = true;
          } else if (baseIri1.isEmpty()) {
            baseIri1 = getBaseIri(line);
          }
        }

        if (hasOntologyIri && baseIri1.isPresent()) {
          fileCount += 1;

          var targetFile = FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibi.ttl");
          RdfFormatter.run(
              new String[] {
                  "-s", sourceFile.getAbsolutePath(),
                  "-t", targetFile.getAbsolutePath(),
                  "-tfmt", "turtle",
                  "-ibi"
              }
          );

          var targetReader = new BufferedReader(prepareInputStream(targetFile));
          Optional<String> baseIri2 = Optional.empty();
          unfinished = true;
          while (unfinished) {
            var line = targetReader.readLine();
            if (line == null) {
              unfinished = false;
            } else if (baseIri2.isEmpty()) {
              baseIri2 = getBaseIri(line);
            }
          }

          assertEquals(baseIri1, baseIri2, "base IRI changed - was ontology IRI different to the base IRI?");
        }
      }
    }

    LOGGER.info("A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle " +
        "format with an inferred base IRI: {} source files", fileCount);
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedTurtleFormatWithInferredBaseIriAndInlineBlankNodes()
      throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!IBI_EXCLUSION_SET.contains(sourceFile.getName())) {
        var sourceReader = new BufferedReader(new FileReader(sourceFile));
        Optional<String> baseIri1 = Optional.empty();
        var unfinished = true;
        var hasOntologyIri = false;
        while (unfinished) {
          var line = sourceReader.readLine();
          if (line == null) {
            unfinished = false;
          } else if (line.contains("owl:Ontology")) {
            hasOntologyIri = true;
          } else if (baseIri1.isEmpty()) {
            baseIri1 = getBaseIri(line);
          }
        }

        if (hasOntologyIri && baseIri1.isPresent()) {
          fileCount += 1;

          var targetFile =
              FileSystemUtils.constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibi_ibn.ttl");
          RdfFormatter.run(
              new String[] {
                  "-s", sourceFile.getAbsolutePath(),
                  "-t", targetFile.getAbsolutePath(),
                  "-tfmt", "turtle",
                  "-ibi",
                  "-ibn"
              }
          );

          var targetReader = new BufferedReader(prepareInputStream(targetFile));
          Optional<String> baseIri2 = Optional.empty();
          unfinished = true;
          while (unfinished) {
            var line = targetReader.readLine();
            if (line == null) {
              unfinished = false;
            } else if (baseIri2.isEmpty()) {
              baseIri2 = getBaseIri(line);
            }
          }

          assertEquals(baseIri1, baseIri2, "base IRI changed - was ontology IRI different to the base IRI?");
        }
      }
    }

    LOGGER.info("A SortedTurtleWriter should be able to read various RDF documents and write them in sorted Turtle " +
            "format with an inferred base IRI and inline blank nodes: {} source files",
        fileCount);
  }

  @Test
  void shouldBeAbleToDoPatternBasedIriReplacements() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_replaced.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-ip", "^http://topbraid.org/countries",
            "-ir", "http://replaced.example.org/countries"
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(
        content.contains("@prefix countries: <http://replaced.example.org/countries#> ."),
        "IRI replacement seems to have failed");
  }

  @Test
  void shouldBeAbleToAddSingleLineLeadingAndTrailingComments() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile =
        FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_single-comments.ttl");
    var linePrefix = "## ";
    var leadingComment = "Start of: My New Ontology.";
    var trailingComment = "End of: My New Ontology.";
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-lc", leadingComment,
            "-tc", trailingComment
        }
    );

    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());

    var escapedLeadingComment = SortedRdfXmlWriter.escapeCommentText(leadingComment);
    assertTrue(
        content.contains(linePrefix + escapedLeadingComment),
        "leading comment insertion seems to have failed");

    var escapedTrailingComment = SortedRdfXmlWriter.escapeCommentText(trailingComment);
    assertTrue(
        content.contains(linePrefix + escapedTrailingComment),
        "trailing comment insertion seems to have failed");
  }

  @Test
  void shouldBeAbleToAddMultiLineLeadingAndTrailingComments() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_multiple-comments.ttl");
    var linePrefix = "## ";
    var leadingComment = List.of("Start of: My New Ontology.", "Version 1.");
    var trailingComment = List.of("End of: My New Ontology.", "Version 1.");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-lc", leadingComment.get(0), "-lc", leadingComment.get(1),
            "-tc", trailingComment.get(0), "-tc", trailingComment.get(1)
        }
    );

    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    for (var comment : leadingComment) {
      assertTrue(
          content.contains(linePrefix + comment),
          "leading comment insertion seems to have failed");
    }
    for (var comment : trailingComment) {
      assertTrue(
          content.contains(linePrefix + comment),
          "trailing comment insertion seems to have failed");
    }
  }

  @Test
  void shouldBeAbleToUseExplicitDataTypingForStrings() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_sdt-explicit.ttl");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-sdt", "explicit"
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(
        content.contains("^^xsd:string"),
        "explicit string data typing seems to have failed");
  }

  @Test
  void shouldBeAbleToOverrideTheLanguageForAllStrings() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_override_language.ttl");
    var overrideLanguage = "en-us";
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-osl", overrideLanguage
        }
    );

    // Read in output file & test that all strings have the override language
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputModel = Rio.parse(new FileReader(outputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    for (var statement : outputModel) {
      var obj = statement.getObject();
      if (obj instanceof Literal) {
        var lit = asInstanceOf(obj, Literal.class);
        if (lit.getLanguage().isPresent()) {
          assertEquals(
              lit.getLanguage().get(),
              overrideLanguage,
              String.format(
                  "literal language should have been forced to '%s' but was: '%s'",
                  overrideLanguage,
                  lit.getLanguage().get()));
        } else if (XS_STRING.equals(lit.getDatatype().stringValue())) {
          fail("string literal did not have any language set: " + lit.stringValue());
        }
      }
    }
  }

  @Test
  void shouldBeAbleToUseSetTheIndentString() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, TURTLE_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");

    var outputFile = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_indent_spaces.ttl");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "turtle",
            "-i", "  "
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    var singleIndentLineCount = content.lines().filter(line -> line.matches("^  \\S.*$")).count();
    assertTrue(singleIndentLineCount >= 1, "double-space indent has failed");

    var outputFile2 = FileSystemUtils.constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_indent_tabs.ttl");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile2.getAbsolutePath(),
            "-tfmt", "turtle",
            "-i", "\t\t"
        }
    );
    var content2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    var singleIndentLineCount2 = content2.lines().filter(line -> line.matches("^\t\t\\S.*$")).count();
    assertTrue(singleIndentLineCount2 >= 1, "double-tab indent has failed");
  }
}