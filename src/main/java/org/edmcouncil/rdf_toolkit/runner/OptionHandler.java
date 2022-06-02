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
package org.edmcouncil.rdf_toolkit.runner;

import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.INDENT;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.INFER_BASE_IRI;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.INLINE_BLANK_NODES;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.LEADING_COMMENT;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.LINE_END;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.OVERRIDE_STRING_LANGUAGE;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SHORT_IRI_PRIORITY;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE_DIRECTORY_PATTERN;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE_FORMAT;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.STRING_DATA_TYPING;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET_DIRECTORY_PATTERN;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET_FORMAT;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TRAILING_COMMENT;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.USE_DTD_SUBSET;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.PRINT_USAGE_AND_EXIT;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.RUN_ON_DIRECTORY;
import org.apache.commons.cli.CommandLine;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.edmcouncil.rdf_toolkit.io.format.SourceFormats;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.runner.exception.RdfToolkitOptionHandlingException;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.edmcouncil.rdf_toolkit.util.StringDataTypeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionHandler.class);

  private final RdfToolkitOptions rdfToolkitOptions;
  private final CommandLine commandLine;

  public OptionHandler(RdfToolkitOptions rdfToolkitOptions) {
    this.rdfToolkitOptions = rdfToolkitOptions;
    this.commandLine =  rdfToolkitOptions.getCommandLine();
  }

  /**
   * Check if a base IRI was provided
   */
  public void handleBaseIri(ValueFactory valueFactory) {
    IRI baseIri = null;
    var baseIriString = "";
    try {
      if (commandLine.hasOption(BASE_IRI.getShortOpt())) {
        baseIriString = commandLine.getOptionValue(BASE_IRI.getShortOpt());
        baseIri = valueFactory.createIRI(baseIriString);
        if (baseIriString.endsWith("#")) {
          LOGGER.warn("base IRI ends in '#', which is unusual: {}", baseIriString);
        }
      }
    } catch (Exception t) {
      baseIriString = "";
      baseIri = null;
    }
    rdfToolkitOptions.setBaseIriString(baseIriString);
    rdfToolkitOptions.setBaseIri(baseIri);
  }

  public void handleIriReplacementOptions() throws RdfToolkitOptionHandlingException {
    String iriPattern;
    String iriReplacement;
    // Check if there is a valid URI pattern/replacement pair
    if (commandLine.hasOption("ip")) {
      if (commandLine.hasOption("ir")) {
        if (commandLine.getOptionValue("ip").length() < 1) {
          throw new RdfToolkitOptionHandlingException(
              "An IRI pattern cannot be an empty string.  Use --help for help.");
        }

        iriPattern = commandLine.getOptionValue("ip");
        rdfToolkitOptions.setIriPattern(iriPattern);

        iriReplacement = commandLine.getOptionValue("ir");
        rdfToolkitOptions.setIriReplacement(iriReplacement);
      } else {
        throw new RdfToolkitOptionHandlingException(
            "If an IRI pattern is specified, an IRI replacement must also be specified.  Use --help for help.");
      }
    } else {
      if (commandLine.hasOption("ir")) {
        throw new RdfToolkitOptionHandlingException(
            "If an IRI replacement is specified, an IRI pattern must also be specified.  Use --help for help.");
      }
    }
  }

  /**
   * Check if source files exists.
   */
  public File handleSourceFile() throws RdfToolkitOptionHandlingException, FileNotFoundException {
    File sourceFile = null;
    InputStream sourceInputStream;
    if (commandLine.hasOption(SOURCE.getShortOpt())) {
      String sourceFilePath = commandLine.getOptionValue(SOURCE.getShortOpt());
      sourceFile = new File(sourceFilePath);
      if (!sourceFile.exists()) {
        throw new RdfToolkitOptionHandlingException(String.format("Source file does not exist: %s", sourceFilePath));
      }
      if (!sourceFile.isFile()) {
        throw new RdfToolkitOptionHandlingException(String.format("Source file is not a file: %s", sourceFilePath));
      }
      if (!sourceFile.canRead()) {
        throw new RdfToolkitOptionHandlingException(String.format("Source file is not readable: %s", sourceFilePath));
      }
      sourceInputStream = new FileInputStream(sourceFile);
    } else {
      if (!commandLine.hasOption(SOURCE_FORMAT.getShortOpt())) {
        throw new RdfToolkitOptionHandlingException(
            "The source format must be specified using --source-format when reading from the standard input.");
      }
      sourceInputStream = System.in; // default to reading the standard input
    }
    rdfToolkitOptions.setSourceFile(sourceFile);
    rdfToolkitOptions.setSourceInputStream(sourceInputStream);
    return sourceFile;
  }

  public void handleRunningOnDirectory(CommandLine commandLine, RdfToolkitOptions rdfToolkitOptions) {
    // Check if the command-line options suggest that a directory of files is to be formatted
    if (commandLine.hasOption(SOURCE_DIRECTORY.getShortOpt()) ||
        commandLine.hasOption(SOURCE_DIRECTORY_PATTERN.getShortOpt()) ||
        commandLine.hasOption(TARGET_DIRECTORY.getShortOpt()) ||
        commandLine.hasOption(TARGET_DIRECTORY_PATTERN.getShortOpt())) {
      // Assume user wants to process a directory of files.
      if (!commandLine.hasOption(SOURCE_DIRECTORY.getShortOpt()) ||
          !commandLine.hasOption(SOURCE_DIRECTORY_PATTERN.getShortOpt()) ||
          !commandLine.hasOption(TARGET_DIRECTORY.getShortOpt()) ||
          !commandLine.hasOption(TARGET_DIRECTORY_PATTERN.getShortOpt())) {
        LOGGER.error("Directory processing options must all be used together: -sd (--source-directory), " +
            "-sdp (--source-directory-pattern), -td (--target-directory), -tdp (--target-directory-pattern)");
        rdfToolkitOptions.setRunningMode(PRINT_USAGE_AND_EXIT);
      }
      if (commandLine.hasOption(SOURCE.getShortOpt()) ||
          commandLine.hasOption(TARGET.getShortOpt())) {
        LOGGER.error("Source (-s or --source) and target (-t or --target) options cannot be used together with " +
            "directory processing options.");
        rdfToolkitOptions.setRunningMode(PRINT_USAGE_AND_EXIT);
      }
      if (!commandLine.hasOption(SOURCE_FORMAT.getShortOpt()) ||
          !commandLine.hasOption(TARGET_FORMAT.getShortOpt())) {
        LOGGER.error("Source format (-sfmt or --source-format) and target format (-tfmt or --target-format) options " +
            "must be provided when using directory processing options.");
        rdfToolkitOptions.setRunningMode(PRINT_USAGE_AND_EXIT);
      }

      if (rdfToolkitOptions.getRunningMode() == null) {
        rdfToolkitOptions.setRunningMode(RUN_ON_DIRECTORY);
      }
    }
  }

  public void handleSourceFormat(File sourceFile) throws RdfToolkitOptionHandlingException {
    SourceFormats sourceFormat;
    if (commandLine.hasOption(SOURCE_FORMAT.getShortOpt())) {
      sourceFormat = SourceFormats.getByOptionValue(commandLine.getOptionValue(SOURCE_FORMAT.getShortOpt()));
    } else {
      sourceFormat = SourceFormats.AUTO;
    }
    if (sourceFormat == null) {
      throw new RdfToolkitOptionHandlingException(
          String.format(
              "Unsupported or unrecognised source format: %s",
              commandLine.getOptionValue(SOURCE_FORMAT.getShortOpt())));
    }
    if ((sourceFile == null) && (sourceFormat == SourceFormats.AUTO)) {
      throw new RdfToolkitOptionHandlingException(
          "The source format (--source-format or -sfmt) cannot be 'auto' when reading from the standard input.");
    }

    RDFFormat sesameSourceFormat;
    if (sourceFile != null && SourceFormats.AUTO == sourceFormat) {
      sesameSourceFormat = Rio.getParserFormatForFileName(sourceFile.getName()).orElse(sourceFormat.getRDFFormat());
    } else {
      sesameSourceFormat = sourceFormat.getRDFFormat();
    }
    if (sesameSourceFormat == null) {
      throw new RdfToolkitOptionHandlingException(
          String.format("Unsupported or unrecognised source format enum: %s", sourceFormat));
    }
    rdfToolkitOptions.setRdf4jSourceFormat(sesameSourceFormat);
  }

  /**
   * Check if target file can be written.
   */
  public void handleTargetFile() throws RdfToolkitOptionHandlingException, FileNotFoundException {
    File targetFile;
    if (commandLine.hasOption(TARGET.getShortOpt())) {
      String targetFilePath = commandLine.getOptionValue(TARGET.getShortOpt());
      targetFile = new File(targetFilePath);
      if (targetFile.exists()) {
        if (!targetFile.isFile()) {
          throw new RdfToolkitOptionHandlingException(String.format("Target file is not a file: %s", targetFilePath));
        }
        if (!targetFile.canWrite()) {
          throw new RdfToolkitOptionHandlingException(String.format("Target file is not writable: %s", targetFilePath));
        }
      }

      // Create directory for target file, if required.
      var targetFileDir = targetFile.getParentFile();
      if (targetFileDir != null) {
        targetFileDir.mkdirs();
        if (!targetFileDir.exists()) {
          throw new RdfToolkitOptionHandlingException(
              String.format("Target file directory could not be created: %s", targetFileDir.getAbsolutePath()));
        }
      }

      rdfToolkitOptions.setTargetFile(targetFile);
      rdfToolkitOptions.setShouldUseStandardOutputStream(false);
    } else {
      rdfToolkitOptions.setShouldUseStandardOutputStream(true);
    }
  }

  /**
   * Check if a DTD subset should be used for namespace shortening in XML.
   */
  public void handleUseDtdSubset() {
    boolean useDtdSubset = commandLine.hasOption(USE_DTD_SUBSET.getShortOpt());
    rdfToolkitOptions.setUseDtdSubset(useDtdSubset);
  }

  /**
   * Check if blank nodes should be rendered inline.
    */
  public void handleInlineBlankNodes() {
    boolean inlineBlankNodes = commandLine.hasOption(INLINE_BLANK_NODES.getShortOpt());
    rdfToolkitOptions.setInlineBlankNodes(inlineBlankNodes);
  }

  /**
   * Check if the base URI should be set to be the same as the OWL ontology URI.
   */
  public void handleInferBaseIri() {
    boolean inferBaseIri = commandLine.hasOption(INFER_BASE_IRI.getShortOpt());
    rdfToolkitOptions.setInferBaseIri(inferBaseIri);
  }

  /**
   * Check if there are leading comments.
   */
  public void handleLeadingComments() {
    String[] leadingComments = null;
    if (commandLine.hasOption(LEADING_COMMENT.getShortOpt())) {
      leadingComments = commandLine.getOptionValues(LEADING_COMMENT.getShortOpt());
    }
    rdfToolkitOptions.setLeadingComments(leadingComments);
  }

  /**
   * Check if there are trailing comments.
   */
  public void handleTrailingComments() {
    String[] trailingComments = null;
    if (commandLine.hasOption(TRAILING_COMMENT.getShortOpt())) {
      trailingComments = commandLine.getOptionValues(TRAILING_COMMENT.getShortOpt());
    }
    rdfToolkitOptions.setTrailingComments(trailingComments);
  }

  /**
   * Check if there is a string data type option.
   */
  public void handleStringDataTyping() {
    StringDataTypeOptions stringDataTypeOption = StringDataTypeOptions.IMPLICIT;
    if (commandLine.hasOption(STRING_DATA_TYPING.getShortOpt())) {
      stringDataTypeOption = StringDataTypeOptions.getByOptionValue(
          commandLine.getOptionValue(STRING_DATA_TYPING.getShortOpt()));
    }
    rdfToolkitOptions.setStringDataTypeOption(stringDataTypeOption);
  }

  /**
   * Check if there is an override language setting for all strings.
   */
  public void handleOverrideStringLanguage() {
    String overrideStringLanguage = null;
    if (commandLine.hasOption(OVERRIDE_STRING_LANGUAGE.getShortOpt())) {
      overrideStringLanguage = commandLine.getOptionValue(OVERRIDE_STRING_LANGUAGE.getShortOpt());
    }
    rdfToolkitOptions.setOverrideStringLanguage(overrideStringLanguage);
  }

  /**
   * Check if an explicit indent string has been provided.
   */
  public void handleIndent() {
    var indent = "\t";
    if (commandLine.hasOption(INDENT.getShortOpt())) {
      // use 'replaceFirst' to get cheap support for escaped characters like tabs
      indent = "ABC".replaceFirst("ABC", commandLine.getOptionValue(INDENT.getShortOpt()));
    }
    rdfToolkitOptions.setIndent(indent);
  }

  /**
   * Write sorted RDF file.
   */
  public void handleTargetFormat() throws RdfToolkitOptionHandlingException {
    TargetFormats targetFormat;
    if (commandLine.hasOption(TARGET_FORMAT.getShortOpt())) {
      targetFormat = TargetFormats.getByOptionValue(commandLine.getOptionValue(TARGET_FORMAT.getShortOpt()));
    } else {
      targetFormat = TargetFormats.TURTLE; // default to Turtle
    }
    if (targetFormat == null) {
      throw new RdfToolkitOptionHandlingException(
          String.format(
              "Unsupported or unrecognised target format: %s",
              commandLine.getOptionValue(TARGET_FORMAT.getShortOpt())));
    }
    rdfToolkitOptions.setFormat(targetFormat);
  }

  public void handleShortUriPref() throws RdfToolkitOptionHandlingException {
    ShortIriPreferences shortUriPref;
    if (commandLine.hasOption(SHORT_IRI_PRIORITY.getShortOpt())) {
      shortUriPref = ShortIriPreferences.getByOptionValue(commandLine.getOptionValue(SHORT_IRI_PRIORITY.getShortOpt()));
    } else {
      shortUriPref = ShortIriPreferences.PREFIX;
    }
    if (shortUriPref == null) {
      throw new RdfToolkitOptionHandlingException(
          String.format(
              "Unsupported or unrecognised short IRI preference: %s",
              commandLine.getOptionValue(SHORT_IRI_PRIORITY.getShortOpt())));
    }
    rdfToolkitOptions.setShortUriPref(shortUriPref);
  }

  public void handleLineEnd() {
    String lineEnd = "\n";
    if (commandLine.hasOption(LINE_END.getShortOpt())) {
      lineEnd = commandLine.getOptionValue(LINE_END.getShortOpt())
          .replace("\\r", "\r")
          .replace("\\n", "\n");
    }
    rdfToolkitOptions.setLineEnd(lineEnd);
  }
}
