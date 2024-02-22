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

import static org.edmcouncil.rdf_toolkit.FileSystemUtils.RESOURCE_DIR;
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
import static org.edmcouncil.rdf_toolkit.TestConstants.INFERRED_BASE_IRI_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.INLINE_BLANK_NODES_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.JSON_LD_INLINE_BLANK_NODES_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.RDFXML_EXCLUSION_SET;
import static org.edmcouncil.rdf_toolkit.TestConstants.XS_STRING;
import static org.edmcouncil.rdf_toolkit.util.Constants.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.util.Constants.INDENT;
import static org.edmcouncil.rdf_toolkit.util.Constants.INLINE_BLANK_NODES;
import static org.edmcouncil.rdf_toolkit.util.Constants.SHORT_URI_PREF;
import static org.edmcouncil.rdf_toolkit.util.Constants.USE_DTD_SUBSET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriterFactory;
import org.edmcouncil.rdf_toolkit.RdfFormatter;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class SortedRdfXmlWriterTest extends AbstractSortedWriterTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SortedRdfXmlWriterTest.class);
  private static final String RDFXML_PREFIX = "rdfxml";
  private static final String RDFXML_STEM = ".rdf";

  private final SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

  private File rootOutputDir1;
  private File rootOutputDir2;

  @BeforeEach
  public void setUp() {
    rootOutputDir1 = mkCleanDir("target/temp/" + getClass().getName() + "_1");
    rootOutputDir2 = mkCleanDir("target/temp/" + getClass().getName() + "_2");
  }

  @Test
  void shouldBeAbleToCreateSortedRdfXmlWriter() {
    var outWriter = new OutputStreamWriter(System.out);

    var writer1 = new SortedRdfXmlWriter(System.out);
    assertNotNull(writer1, "failed to create default SortedRdfXmlWriter from OutputStream");

    var writer2 = new SortedRdfXmlWriter(outWriter);
    assertNotNull(writer2, "failed to create default SortedRdfXmlWriter from Writer");

    Map<String, Object> writerOptions3 = Map.of(
        BASE_IRI, valueFactory.createIRI("http://example.com#"),
        INDENT, "\t\t",
        SHORT_URI_PREF, ShortIriPreferences.PREFIX);
    var writer3 = new SortedRdfXmlWriter(System.out, writerOptions3);
    assertNotNull(writer3, "failed to create default SortedRdfXmlWriter from OutputStream with parameters");

    Map<String, Object> writerOptions4 = Map.of(
        BASE_IRI, valueFactory.createIRI("http://example.com#"),
        INDENT, "\t\t",
        SHORT_URI_PREF, ShortIriPreferences.BASE_IRI);
    var writer4 = new SortedRdfXmlWriter(outWriter, writerOptions4);
    assertNotNull(writer4, "failed to create default SortedRdfXmlWriter from Writer with parameters");
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInRdfXmlFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var rootOutputDir = mkCleanDir("target/temp/" + RDFWriter.class.getName());

    var outputDir = createTempDir(rootOutputDir, RDFXML_PREFIX);

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      fileCount += 1;
      var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir, RDFXML_STEM);
      var outWriter = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8.name());
      var factory = new RDFXMLPrettyWriterFactory();
      var rdfXmlWriter = factory.getWriter(outWriter);
      var rdfFormat = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();

      var inputModel = Rio.parse(new FileReader(sourceFile), "", rdfFormat);
      Rio.write(inputModel, rdfXmlWriter);
      outWriter.flush();
      outWriter.close();
    }
    LOGGER.info(
        "An RDFWriter should be able to read various RDF documents and write them in RDF/XML format: {} source files.",
        fileCount);

    assertTrue(fileCount > 0);
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFile() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, RDFXML_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object>  rdfXmlWriterOptions = Map.of(
        BASE_IRI, baseIri,
        USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object>  rdfXmlWriter2Options = Map.of(
        BASE_IRI, baseIri,
        USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        baseIri.stringValue(),
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFileWithBlankObjectNodes() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/other/topquadrant-extended-turtle-example.ttl");
    var outputFile =
        constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, RDFXML_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(
        USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8);
    Map<String, Object> rdfXmlWriter2Options = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8),
        "",
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFileWithDirectlyRecursiveBlankObjectNodes() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/rdf_turtle_spec/turtle-example-14.ttl");
    var outputFile =
        constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, RDFXML_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(prepareInputStream(outputFile), "", RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFileWithIndirectlyRecursiveBlankObjectNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory.getPath() + "/rdf_turtle_spec/turtle-example-26.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, RDFXML_STEM);
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8.name()),
        "",
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFilePreferringPrefixOverBaseIri() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, "_prefix.rdf");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    var contents1 = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(contents1.contains("Åland"), "prefix preference file has encoding problem (1)");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8.name()),
        baseIri.stringValue(),
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("Åland"), "prefix preference file has encoding problem (2)");
    outWriter2.flush();
    outWriter2.close();
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFilePreferringBaseIriOverPrefix() throws Exception {
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(getRawRdfDirectory().getPath() + "/other/topbraid-countries-ontology.ttl");
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputFile = constructTargetPath(inputFile, getRawRdfDirectory(), outputDir1, "_base_iri.rdf");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.TURTLE);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    var contents1 = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(contents1.contains("Åland"), "base IRI preference file has encoding problem (1)");

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(USE_DTD_SUBSET, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8.name()),
        baseIri.stringValue(),
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    var contents2 = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    assertTrue(contents2.contains("Åland"), "base IRI preference file has encoding problem (2)");
    outWriter2.flush();
    outWriter2.close();
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedRdfXmlFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var rootOutputDir = createTempDir(rootOutputDir1, RDFXML_PREFIX);

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, rootOutputDir, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd"
            }
        );
        assertTrue(
            targetFile.exists(),
            String.format("File '%s' should exist.", targetFile.getAbsolutePath()));
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML " +
            "format: {} source files",
        fileCount);
  }

  @Test
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInRdfXmlFormatForTwoFiles() throws Exception {
    var rawRdfXmlDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, RDFXML_PREFIX);

    var sourceFile = new File(
        rawRdfXmlDirectory.getPath() + "/fibo/ontology/master/latest/DER/RateDerivatives/IRSwaps.rdf");

    var targetFile1 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir1, RDFXML_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", sourceFile.getAbsolutePath(),
            "-t", targetFile1.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd"
        }
    );

    var targetFile2 = constructTargetPath(sourceFile, rawRdfXmlDirectory, outputDir2, RDFXML_STEM);
    RdfFormatter.run(
        new String[] {
            "-s", sourceFile.getAbsolutePath(),
            "-t", targetFile2.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd"
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
  void shouldBeAbleToSortRdfTriplesConsistentlyWhenWritingInRdfXmlFormat() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML " +
            "format: {} source files",
        fileCount);

    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (!sourceFile.getName().contains("_prefix") && !sourceFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML " +
            "format: {} source files",
        fileCount);

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      fileCount += 1;
      var file2 = constructTargetPath(file1, outputDir1, outputDir2);
      assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());
      assertTrue(
          compareFiles(file1, file2, StandardCharsets.UTF_8.name()),
          "File mismatch between outputDir1 and outputDir2: " + file1.getName());
    }
  }

  @Test
  void shouldNotAddLoseRdfTriplesWhenWritingInRdfXmlFormatWithoutBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName()) &&
          !JSON_LD_INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML " +
            "format: {} source files",
        fileCount);

    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (!sourceFile.getName().contains("_prefix") && !sourceFile.getName().contains("_base_iri")) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML " +
            "format: {} source files",
        fileCount);

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      fileCount += 1;
      var file2 = constructTargetPath(file1, outputDir1, outputDir2);
      assertTrue(file2.exists(), "File missing in outputDir2: " + file2.getAbsolutePath());
      assertTrue(
          compareFiles(file1, file2, StandardCharsets.UTF_8.name()),
          "File mismatch between outputDir1 and outputDir2: " + file1.getName());
    }

    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName()) &&
          !INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir2, RDFXML_STEM);
        var rdfFormat1 = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();
        var rdfFormat2 = Rio.getParserFormatForFileName(targetFile.getName()).orElseThrow();
        var inputModel1 = Rio.parse(prepareInputStream(sourceFile), "", rdfFormat1);
        var inputModel2 = Rio.parse(prepareInputStream(targetFile), "", rdfFormat2);

        assertTriplesMatch(inputModel1, inputModel2);
      }
    }
  }

  @Test
  void shouldBeAbleToProduceSortedRdfXmlFileWithInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());
    var inputFile = new File(rawRdfDirectory + "/fibo/ontology/master/latest/FND/Accounting/AccountingEquity.rdf");
    var baseIri = valueFactory.createIRI("https://spec.edmcouncil.org/fibo/ontology/FND/Accounting/AccountingEquity/");
    var outputFile =
        constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_inline_blank_nodes.rdf");
    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(
        BASE_IRI, baseIri,
        USE_DTD_SUBSET, true,
        INLINE_BLANK_NODES, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), baseIri.stringValue(), RDFFormat.RDFXML);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();
    assertTrue(
        outputFile.exists(),
        String.format("File '%s' should exists after writing.", outputFile.getAbsolutePath()));

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(
        BASE_IRI, baseIri,
        USE_DTD_SUBSET, true,
        INLINE_BLANK_NODES, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8.name()),
        baseIri.stringValue(),
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();
    assertTrue(
        outputFile2.exists(),
        String.format("File '%s' should exists after writing.", outputFile2.getAbsolutePath()));
  }

  @Test
  void shouldBeAbleSortTriplesConsistentlyWhenWritingInRdfXmlFormatWithInlineBlankNodes() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createTempDir(rootOutputDir2, RDFXML_PREFIX);

    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName()) &&
          !INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibn.rdf");
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd",
                "-ibn"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should be able to sort RDF triples consistently when writing in RDF/XML " +
            "format with inline blank nodes: {} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (sourceFile.getName().contains("_ibn")) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd",
                "-ibn"
            }
        );
      }
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      if (file1.getName().contains("_ibn")) {
        file1 = file1.getCanonicalFile();
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
  void shouldNotAddOrLoseRdfTriplesWhenWritingInRdfXmlFormatWithBlankNodesSingleFile() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    var inputFile = new File(rawRdfDirectory.getPath() + "/other/ControlParties.rdf");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_ibn2s.rdf");

    var outWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8.name());
    var factory = new SortedRdfWriterFactory(TargetFormats.RDF_XML);
    Map<String, Object> rdfXmlWriterOptions = Map.of(
        USE_DTD_SUBSET, true,
        INLINE_BLANK_NODES, true);
    var rdfXmlWriter = factory.getWriter(outWriter, rdfXmlWriterOptions);

    var inputModel = Rio.parse(new FileReader(inputFile), "", RDFFormat.RDFXML);
    Rio.write(inputModel, rdfXmlWriter);
    outWriter.flush();
    outWriter.close();

    var outputFile2 = constructTargetPath(outputFile, outputDir1, outputDir2);
    var outWriter2 = new OutputStreamWriter(new FileOutputStream(outputFile2), StandardCharsets.UTF_8.name());
    Map<String, Object> rdfXmlWriter2Options = Map.of(
        USE_DTD_SUBSET, true,
        INLINE_BLANK_NODES, true);
    var rdfXmlWriter2 = factory.getWriter(outWriter2, rdfXmlWriter2Options);

    var inputModel2 = Rio.parse(
        new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8.name()),
        "",
        RDFFormat.RDFXML);
    Rio.write(inputModel2, rdfXmlWriter2);
    outWriter2.flush();
    outWriter2.close();

    // Check that re-serialising the RDF/XML file has changed nothing.
    assertTrue(outputFile2.exists(), "file missing in outputDir2: " + outputFile2.getAbsolutePath());
    assertTrue(
        compareFiles(outputFile, outputFile2, StandardCharsets.UTF_8.name()),
        "file mismatch between outputDir1 and outputDir2: " + outputFile.getName());

    // Check that the re-serialised RDF/XML file has the same triple count as the matching raw file.
    var rdfFormat1 = Rio.getParserFormatForFileName(inputFile.getName()).orElseThrow();
    var rdfFormat2 = Rio.getParserFormatForFileName(outputFile2.getName()).orElseThrow();
    var inputModel1a = Rio.parse(prepareInputStream(inputFile), "", rdfFormat1);
    var inputModel2a = Rio.parse(prepareInputStream(outputFile2), "", rdfFormat2);

    assertTriplesMatch(inputModel1a, inputModel2a);
  }

  @Test
  void shouldNotAddOrLoseRdfTriplesWhenWritingInRdfXmlFormatWithBlankNodesMultipleFiles() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var outputDir2 = createDir(rootOutputDir2, outputDir1.getName());

    // Serialise sample files as sorted RDF/XML.
    var fileCount = 0;
    for (File sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibn2.rdf");
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd",
                "-ibn"
            }
        );
      }
    }
    LOGGER.info(
        "A SortedRdfXmlWriter should not add/lose RDF triples when writing in RDF/XML format with blank nodes " +
            "(multiple files): {} source files",
        fileCount);

    // Re-serialise the sorted files, again as sorted RDF/XML.
    fileCount = 0;
    for (File sourceFile : listDirTreeFiles(outputDir1)) {
      if (sourceFile.getName().contains("_ibn2")) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, outputDir1, outputDir2, RDFXML_STEM);
        RdfFormatter.run(
            new String[] {
                "-s", sourceFile.getAbsolutePath(),
                "-t", targetFile.getAbsolutePath(),
                "-tfmt", "rdf-xml",
                "-dtd",
                "-ibn"
            }
        );
      }
    }

    // Check that re-serialising the RDF/XML files has changed nothing.
    fileCount = 0;
    for (var file1 : listDirTreeFiles(outputDir1)) {
      if (file1.getName().contains("_ibn2")) {
        fileCount += 1;
        var file2 = constructTargetPath(file1, outputDir1, outputDir2);
        assertTrue(file2.exists(), "file missing in outputDir2: " + file2.getAbsolutePath());
        assertTrue(
            compareFiles(file1, file2, StandardCharsets.UTF_8.name()),
            "file mismatch between outputDir1 and outputDir2: " + file1.getName());
      }
    }

    // Check that the re-serialised RDF/XML files have the same triple count as the matching raw files.
    fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!INLINE_BLANK_NODES_EXCLUSION_SET.contains(sourceFile.getName())) {
        fileCount += 1;
        var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir2, "_ibn2.rdf");
        var rdfFormat1 = Rio.getParserFormatForFileName(sourceFile.getName()).orElseThrow();
        var rdfFormat2 = Rio.getParserFormatForFileName(targetFile.getName()).orElseThrow();
        var inputModel1 = Rio.parse(prepareInputStream(sourceFile), "", rdfFormat1);
        var inputModel2 = Rio.parse(prepareInputStream(targetFile), "", rdfFormat2);
        assertTriplesMatch(inputModel1, inputModel2);
      }
    }
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedRdfXmlFormatWithInferredBaseIri() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName()) &&
          !(INFERRED_BASE_IRI_EXCLUSION_SET.contains(sourceFile.getName())) &&
          !(IBI_EXCLUSION_SET.contains(sourceFile.getName()))) {
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

          var targetFile = constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibi.rdf");
          RdfFormatter.run(
              new String[] {
                  "-s", sourceFile.getAbsolutePath(),
                  "-t", targetFile.getAbsolutePath(),
                  "-tfmt", "rdf-xml",
                  "-dtd",
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

    LOGGER.info("A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML " +
            "format with an inferred base IRI: {} source files",
        fileCount);
  }

  @Test
  void shouldBeAbleToReadVariousRdfDocumentsAndWriteThemInSortedRdfXmlFormatWithInferredBaseIriAndInlineBlankNodes()
      throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);

    var fileCount = 0;
    for (var sourceFile : listDirTreeFiles(rawRdfDirectory)) {
      if (!RDFXML_EXCLUSION_SET.contains(sourceFile.getName()) &&
          !(INFERRED_BASE_IRI_EXCLUSION_SET.contains(sourceFile.getName())) &&
          !(IBI_EXCLUSION_SET.contains(sourceFile.getName()))) {
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
              constructTargetPath(sourceFile, rawRdfDirectory, outputDir1, "_ibi_ibn.rdf");
          RdfFormatter.run(
              new String[] {
                  "-s", sourceFile.getAbsolutePath(),
                  "-t", targetFile.getAbsolutePath(),
                  "-tfmt", "rdf-xml",
                  "-dtd",
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

    LOGGER.info("A SortedRdfXmlWriter should be able to read various RDF documents and write them in sorted RDF/XML " +
            "format with an inferred base IRI and inline blank nodes: {} source files",
        fileCount);
  }

  @Test
  void shouldBeAbleToDoPatternBasedIriReplacements() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_replaced.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-ip", "^http://topbraid.org/countries",
            "-ir", "http://replaced.example.org/countries"
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(
        content.contains("<!ENTITY countries \"http://replaced.example.org/countries#\">"),
        "IRI replacement seems to have failed");
  }

  @Test
  void shouldBeAbleToAddSingleLineLeadingAndTrailingComments() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile =
        constructTargetPath(inputFile, RESOURCE_DIR, outputDir1, "_single-comments.rdf");
    var linePrefix = "## ";
    var leadingComment = "Start of --> My New Ontology.";
    var trailingComment = "End of --> My New Ontology.";
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
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
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile =
        constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_multiple-comments.rdf");
    var linePrefix = "## ";
    var leadingComments = List.of("Start of: My New Ontology.", "Version 1.");
    var trailingComments = List.of("End of: My New Ontology.", "Version 1.");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-lc", leadingComments.get(0), "-lc", leadingComments.get(1),
            "-tc", trailingComments.get(0), "-tc", trailingComments.get(1)
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    for (var comment : leadingComments) {
      var escapedComment = SortedRdfXmlWriter.escapeCommentText(comment);
      assertTrue(
          content.contains(linePrefix + escapedComment),
          "leading comment insertion seems to have failed");
    }
    for (var comment : trailingComments) {
      var escapedComment = SortedRdfXmlWriter.escapeCommentText(comment);
      assertTrue(
          content.contains(linePrefix + escapedComment),
          "trailing comment insertion seems to have failed");
    }
  }

  @Test
  void shouldBeAbleToUseExplicitDataTypingForStrings() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile =
        constructTargetPath(inputFile, rawRdfDirectory , outputDir1, "_sdt_explicit.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-sdt", "explicit"
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    assertTrue(
        content.contains("rdf:datatype=\"&xsd;string\""),
        "explicit string data typing seems to have failed");
  }

  @Test
  void shouldBeAbleToOverrideTheLanguageForAllStrings() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");
    var outputFile =
        constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_override_language.rdf");
    var overrideLanguage = "en-us";
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-osl", overrideLanguage
        }
    );

    // Read in output file & test that all strings have the override language
    var baseIri = valueFactory.createIRI("http://topbraid.org/countries");
    var outputModel = Rio.parse(new FileReader(outputFile), baseIri.stringValue(), RDFFormat.RDFXML);
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
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/other/topbraid-countries-ontology.ttl");

    var outputFile =
        constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_indent_spaces.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-i", "  "
        }
    );
    var content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    var singleIndentLineCount = content.lines().filter(line -> line.matches("^  \\S.*$")).count();
    assertTrue(singleIndentLineCount >= 1, "double-space indent has failed");

    var outputFile2 =
        constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_indent_tabs.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile2.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-dtd",
            "-i", "\t\t"
        }
    );
    content = getFileContents(outputFile2, StandardCharsets.UTF_8.name());
    singleIndentLineCount = content.lines().filter(line -> line.matches("^\t\t\\S.*$")).count();
    assertTrue(singleIndentLineCount >= 1, "double-tab indent has failed");
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenAdditionalSettingsAreNotSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.rdf");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    String label1Line = getTrimmedLineContainingString(content, "label1");
    assertEquals("<rdfs:label>label1</rdfs:label>", label1Line);
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenOverrideLanguageSettingIsSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.rdf");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-osl", "fr",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    String label1Line = getTrimmedLineContainingString(content, "label1");
    assertEquals("<rdfs:label xml:lang=\"fr\">label1</rdfs:label>", label1Line);
    String label2Line = getTrimmedLineContainingString(content, "label2");
    assertEquals("<rdfs:label xml:lang=\"fr\">label2</rdfs:label>", label2Line);
    String label5Line = getTrimmedLineContainingString(content, "label5");
    assertEquals("<rdfs:label rdf:datatype=\"&xsd;token\">label5</rdfs:label>", label5Line);
  }

  @Test
  void shouldUseDefaultSettingsForSerializationOfLiteralsWhenUseDefaultLanguageIsSet() throws Exception {
    var rawRdfDirectory = getRawRdfDirectory();
    var outputDir1 = createTempDir(rootOutputDir1, RDFXML_PREFIX);
    var inputFile = new File(rawRdfDirectory.getPath() + "/literal/test1.rdf");

    var outputFile = constructTargetPath(inputFile, rawRdfDirectory, outputDir1, "_output.rdf");
    RdfFormatter.run(
        new String[] {
            "-s", inputFile.getAbsolutePath(),
            "-t", outputFile.getAbsolutePath(),
            "-tfmt", "rdf-xml",
            "-udl", "de",
        }
    );

    String content = getFileContents(outputFile, StandardCharsets.UTF_8.name());
    String label1Line = getTrimmedLineContainingString(content, "label1");
    assertEquals("<rdfs:label xml:lang=\"de\">label1</rdfs:label>", label1Line);
    String label2Line = getTrimmedLineContainingString(content, "label2");
    assertEquals("<rdfs:label xml:lang=\"en\">label2</rdfs:label>", label2Line);
    String label5Line = getTrimmedLineContainingString(content, "label5");
    assertEquals("<rdfs:label rdf:datatype=\"&xsd;token\">label5</rdfs:label>", label5Line);
  }
}