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
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.createTempDir;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.getFileContents;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.getRawRdfDirectory;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.listDirTreeFiles;
import static org.edmcouncil.rdf_toolkit.FileSystemUtils.mkCleanDir;
import static org.edmcouncil.rdf_toolkit.TestComparisonsUtils.compareFiles;
import static org.edmcouncil.rdf_toolkit.TestUtils.doesNotContains;
import static org.edmcouncil.rdf_toolkit.util.Constants.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.util.Constants.SHORT_URI_PREF;
import static org.edmcouncil.rdf_toolkit.util.ShortIriPreferences.PREFIX;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriterFactory;
import org.edmcouncil.rdf_toolkit.RdfFormatter;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

class SortedJsonLdWriterTest extends AbstractSortedWriterTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SortedJsonLdWriterTest.class);
  private static final String JSONLD_PREFIX = "jsonld";
  private static final String JSONLD_STEM = ".jsonld";

  private final ValueFactory valueFactory;

  private File rootOutputDir1;
  private File rootOutputDir2;
  private File rootOutputDir3;

  public SortedJsonLdWriterTest() {
    this.valueFactory = SimpleValueFactory.getInstance();
  }

  @BeforeEach
  public void setUp() {
    rootOutputDir1 = mkCleanDir("target/temp/" + getClass().getName());
    rootOutputDir2 = mkCleanDir("target/temp/" + getClass().getName() + "_2");
    rootOutputDir3 = mkCleanDir("target/temp/" + getClass().getName() + "_3");
  }

  @Test
  void shouldBeAbleToCreateSortedJsonLdWriter() {
    var outWriter = new OutputStreamWriter(System.out);

    var writer1 = new SortedTurtleWriter(System.out);
    assertNotNull(writer1, "failed to create default SortedJsonLdWriter from OutputStream");

    var writer2 = new SortedTurtleWriter(outWriter);
    assertNotNull(writer2, "failed to create default SortedJsonLdWriter from Writer");

    Map<String, Object> writer3Options = Map.of(
        "baseIri", valueFactory.createIRI("http://example.com#"),
        "indent", "\t\t",
        "shortIriPref", PREFIX);
    var writer3 = new SortedJsonLdWriter(System.out, writer3Options);
    assertNotNull(writer3, "failed to create default SortedJsonLdWriter from OutputStream with parameters");

    Map<String, Object> writer4Options = Map.of(
        "baseIri", valueFactory.createIRI("http://example.com#"),
        "indent", "\t\t",
        "shortIriPref", BASE_IRI);
    var writer4 = new SortedTurtleWriter(outWriter, writer4Options);
    assertNotNull(writer4, "failed to create default SortedJsonLdWriter from Writer");
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInJsonLdFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var rootOutputDir = mkCleanDir("target/temp/" + RDFWriter.class.getName());

    var outputDir = createTempDir(rootOutputDir, JSONLD_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1;
      var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir, JSONLD_STEM);
      var outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8);
      var factory = new JSONLDWriterFactory();
      var jsonLdWriter = factory.getWriter(outWriter);
      var rdfFormat = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();

      var inputModel = Rio.parse(new FileReader(sourceFile), "", rdfFormat);
      Rio.write(inputModel, jsonLdWriter);
      outWriter.flush();
      outWriter.close();
    }
    LOGGER.info(
        "An RDFWriter should be able to read various RDF documents and write them in JSON-LD format: {} source files.",
        fileCount);

    assertTrue(fileCount > 0);
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFile() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, JSONLD_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    Map<String, Object> turtleWriterOptions = Map.of("baseIri", baseIri);
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
    Map<String, Object> turtleWriterOptions2 = Map.of("baseIri", baseIri);
    var turtleWriter2 = factory.getWriter(outWriter2, turtleWriterOptions2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        baseIri.stringValue(),
        RDFFormat.JSONLD);
    Rio.write(inputModel2, turtleWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFileWithBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topquadrant-extended-turtle-example.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, JSONLD_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    var jsonLdWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var jsonLdWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFileWithBlankSubjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-17.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, JSONLD_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    var jsonLdWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var jsonLdWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFileWithDirectlyRecursiveBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-14.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, JSONLD_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    var jsonLdWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var jsonLdWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFileWithIndirectlyRecursiveBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-26.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, JSONLD_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    var jsonLdWriter = factory.getWriter(outWriter);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    var jsonLdWriter2 = factory.getWriter(outWriter2);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFilePreferringPrefixOverBaseIri() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_prefix.jsonld");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    Map<String, Object> jsonLdWriterOptions = Map.of(
        "baseIri", baseIri,
        "shortIriPref", ShortIriPreferences.PREFIX);
    var jsonLdWriter = factory.getWriter(outWriter, jsonLdWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();

    var contents1 = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(contents1.contains("countries:AD"), "prefix preference has failed (1a)");
    assertFalse(contents1.contains("#AD"), "prefix preference has failed (1b)");
    assertTrue(contents1.contains("Åland"), "prefix preference file has encoding problem (1)");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> jsonLdWriter2Options = Map.of(
        "baseIri", baseIri,
        "shortIriPref", ShortIriPreferences.PREFIX);
    var jsonLdWriter2 = factory.getWriter(outWriter2, jsonLdWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        baseIri.stringValue(),
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();

    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("countries:AD"), "prefix preference has failed (2a)");
    assertFalse(contents2.contains("#AD"), "prefix preference has failed (2b)");
    assertTrue(contents2.contains("Åland"), "prefix preference file has encoding problem (2)");
  }

  @Test
  void shouldBeAbleToProduceSortedJsonLdFilePreferringBaseIriOverPrefix() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_base_iri.jsonld");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.JSON_LD);
    Map<String, Object> jsonLdWriterOptions = Map.of(
        BASE_IRI, baseIri,
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var jsonLdWriter = factory.getWriter(outWriter, jsonLdWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, jsonLdWriter);
    outWriter.flush();
    outWriter.close();
    var contents1 = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(contents1.contains("#AD"), "base IRI preference has failed (1a)");
    assertFalse(contents1.contains("countries:AD"), "base IRI preference has failed (1b)");
    assertTrue(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> jsonLdWriter2Options = Map.of(
        BASE_IRI, baseIri,
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var jsonLdWriter2 = factory.getWriter(outWriter2, jsonLdWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        baseIri.stringValue(),
        RDFFormat.JSONLD);
    Rio.write(inputModel2, jsonLdWriter2);
    outWriter2.flush();
    outWriter2.close();
    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("#AD"), "base IRI preference has failed (2a)");
    assertFalse(contents2.contains("countries:AD"), "base IRI preference has failed (2b)");
    assertTrue(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)");
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedJsonLdFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var rootOutputDir = createTempDir(rootOutputDir1, JSONLD_PREFIX);

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, rootOutputDir, JSONLD_STEM);
      RdfFormatter.run(
          new String[] {
              "-s", sourceFile.getAbsolutePath(),
              "-t", targetFile.getAbsolutePath(),
              "-tfmt", "json-ld"
          }
      );
      if (targetFile.exists()) {
        fileCount += 1;
      }
    }
    LOGGER.info(
        "A SortedJsonLdWriter should be able to read various RDF documents and write them in sorted JSON-LD format: " +
            "{} source files",
        fileCount);

    assertTrue(
        fileCount > 0,
        String.format("Files from %s should be parsed.", rawRdfDirectory));
  }

  @Test
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInJsonFormatForTwoFiles() throws Exception {
    var rawRdfXmlDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, JSONLD_PREFIX);

    var sourceFile = new File(
        rawRdfXmlDirectory.getPath() + "/fibo/ontology/master/latest/DER/RateDerivatives/IRSwaps.rdf");

    var targetFile1 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir1, JSONLD_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", sourceFile.getAbsolutePath(),
            "-t", targetFile1.getAbsolutePath(),
            "-tfmt", "json-ld"
        }
    );

    var targetFile2 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir2, JSONLD_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", sourceFile.getAbsolutePath(),
            "-t", targetFile2.getAbsolutePath(),
            "-tfmt", "json-ld"
        }
    );

    assertTrue(
        compareFiles(targetFile1, targetFile2, StandardCharsets.UTF_8.name()),
        String.format(
            "File mismatch between targetFile1 (%s) and targetFile2 (%s)",
            targetFile1.getPath(),
            targetFile2.getPath()));
  }

  @Test
  @Disabled // TODO: Test doesn't pass
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInJsonFormatForReserializedFiles() throws Exception {
    var rawRdfXmlDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, JSONLD_PREFIX);
    var outputDir3 = createTempDir(rootOutputDir3, JSONLD_PREFIX);

    var sourceFile = new File(
        rawRdfXmlDirectory.getPath() + "/fibo/ontology/master/latest/DER/RateDerivatives/IRSwaps.rdf");

    var targetFile1 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir1, JSONLD_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", sourceFile.getAbsolutePath(),
            "-t", targetFile1.getAbsolutePath(),
            "-tfmt", "json-ld"
        }
    );

    var targetFile2 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir2, JSONLD_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", targetFile1.getAbsolutePath(),
            "-sfmt", "json-ld",
            "-t", targetFile2.getAbsolutePath(),
            "-tfmt", "json-ld"
        }
    );

    var targetFile3 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir3, JSONLD_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", targetFile2.getAbsolutePath(),
            "-sfmt", "json-ld",
            "-t", targetFile3.getAbsolutePath(),
            "-tfmt", "json-ld"
        }
    );

    assertTrue(
        compareFiles(targetFile1, targetFile3, StandardCharsets.UTF_8.name()),
        String.format(
            "File mismatch between targetFile1 (%s) and targetFile3 (%s)",
            targetFile1.getPath(),
            targetFile3.getPath()));
  }

  @Test
  @Disabled // TODO: Test doesn't pass
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInJsonLdFormat() throws Exception {
    var rawRdfXmlDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, JSONLD_PREFIX);
    var outputDir3 = createTempDir(rootOutputDir3, JSONLD_PREFIX);

    // Serialise sample files as sorted JSON-LD.
    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfXmlDirectory)) {
      fileCount += 1;
      var targetFile = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir1, JSONLD_STEM);
      RdfFormatter.run(
          new String[] {
              "-s", sourceFile.getAbsolutePath(),
              "-t", targetFile.getAbsolutePath(),
              "-tfmt", "json-ld"
          }
      );
    }
    LOGGER.info(
        "A SortedJsonLdWriter should be able to sort RDF triples consistently when writing in JSON-LD format: " +
            "{} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted JSON-LD.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      sourceFile = sourceFile.getCanonicalFile();
      if (doesNotContains(sourceFile, Set.of("_prefix", "_base_iri"))) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2, JSONLD_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-sfmt", "json-ld",
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "json-ld"
            }
        );
        assertTrue(
            compareFiles(sourceFile, targetFile, StandardCharsets.UTF_8.name()),
            "File mismatch between outputDir2 and outputDir3: " + targetFile.getName());
      }
    }
    LOGGER.info(
        "A SortedJsonLdWriter should be able to sort RDF triples consistently when writing in JSON-LD format: " +
            "{} source files",
        fileCount);

    // Check that re-serialising the JSON-LD files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      fileCount += 1;
      var file2 = constructTargetPath(file1, outputDir1, outputDir2);
      assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());

      if (!compareFiles(file1, file2, StandardCharsets.UTF_8.name())) {
        var targetFile = constructTargetPath(file2, outputDir2, outputDir3);
        RdfFormatter.run(
            new String[] {
                "-s", file2.getAbsolutePath(),
                "-sfmt", "json-ld",
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "json-ld"
            }
        );
        assertTrue(targetFile.exists(), "File missing in outputDir3: " + targetFile.getAbsolutePath());
        assertTrue(
            compareFiles(file2, targetFile, StandardCharsets.UTF_8.name()),
            "File mismatch between outputDir2 and outputDir3: " + targetFile.getName());
      }
    }
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenAdditionalSettingsAreNotSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.jsonld");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.jsonld");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "json-ld",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    JsonNode label1Object = getJsonObjectForLabel(content, "label1");
    assertFalse(label1Object.has(JSONLD_LANGUAGE));
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenOverrideLanguageIsSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.jsonld");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.jsonld");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "json-ld",
            "-osl", "fr",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    JsonNode label1Object = getJsonObjectForLabel(content, "label1");
    assertEquals("fr", label1Object.get(JSONLD_LANGUAGE).asText());
    JsonNode label2Object = getJsonObjectForLabel(content, "label2");
    assertEquals("fr", label2Object.get(JSONLD_LANGUAGE).asText());
    JsonNode label5Line = getJsonObjectForLabel(content, "label5");
    assertFalse(label5Line.has(JSONLD_LANGUAGE));
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenUseDefaultIsSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, JSONLD_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.jsonld");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.jsonld");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "json-ld",
            "-udl", "de",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    JsonNode label1Object = getJsonObjectForLabel(content, "label1");
    assertEquals("de", label1Object.get(JSONLD_LANGUAGE).asText());
    JsonNode label2Object = getJsonObjectForLabel(content, "label2");
    assertEquals("en", label2Object.get(JSONLD_LANGUAGE).asText());
    JsonNode label5Line = getJsonObjectForLabel(content, "label5");
    assertFalse(label5Line.has(JSONLD_LANGUAGE));
  }
}