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

import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.HELP;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.SOURCE_DIRECTORY_PATTERN;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.TARGET_DIRECTORY_PATTERN;
import static org.edmcouncil.rdf_toolkit.runner.CommandLineOption.VERSION;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.EXIT;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.PRINT_AND_EXIT;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.PRINT_USAGE_AND_EXIT;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.RUN_ON_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.RunningMode.RUN_ON_FILE;
import com.jcabi.manifests.Manifests;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.model.MemValueFactory;
import org.edmcouncil.rdf_toolkit.RdfFormatter;
import org.edmcouncil.rdf_toolkit.io.DirectoryWalker;
import org.edmcouncil.rdf_toolkit.runner.exception.RdfToolkitOptionHandlingException;
import org.edmcouncil.rdf_toolkit.util.Constants;
import org.edmcouncil.rdf_toolkit.writer.SortedRdfWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RdfToolkitRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(RdfToolkitRunner.class);

  private final Options options;
  private final ValueFactory valueFactory;

  public RdfToolkitRunner() {
    this.options = CommandLineOption.prepareOptions();
    this.valueFactory = new MemValueFactory();
  }

  public void run(String[] args) throws Exception {
    var rdfToolkitOptions = handleArguments(args);

    switch (rdfToolkitOptions.getRunningMode()) {
      case EXIT:
        return;
      case PRINT_AND_EXIT:
        System.out.println(rdfToolkitOptions.getOutput());
        break;
      case RUN_ON_DIRECTORY:
        // Run the serializer over a directory of files
        runOnDirectory(rdfToolkitOptions.getCommandLine());
        break;
      case RUN_ON_FILE:
        runOnFile(rdfToolkitOptions);
        break;
      default:
        throw new RdfToolkitOptionHandlingException("Unknown running mode: " + rdfToolkitOptions.getRunningMode());
    }
  }

  private RdfToolkitOptions handleArguments(String[] args)
      throws ParseException, FileNotFoundException, RdfToolkitOptionHandlingException {
    var rdfToolkitOptions = new RdfToolkitOptions(args);

    // Parse the command line options.
    CommandLineParser parser = new DefaultParser();
    CommandLine line = parser.parse(options, args);
    rdfToolkitOptions.setCommandLine(line);

    var optionHandler = new OptionHandler(rdfToolkitOptions);

    // Print out version, if requested.
    if (line.hasOption(VERSION.getShortOpt())) {
      rdfToolkitOptions.setOutput(getVersion());
      rdfToolkitOptions.setRunningMode(PRINT_AND_EXIT);
      return rdfToolkitOptions;
    }

    // Print out help, if requested.
    if (line.hasOption(HELP.getShortOpt())) {
      usage(options);
      rdfToolkitOptions.setRunningMode(EXIT);
      return rdfToolkitOptions;
    }

    optionHandler.handleRunningOnDirectory(line, rdfToolkitOptions);
    if (rdfToolkitOptions.getRunningMode() == PRINT_USAGE_AND_EXIT) {
      usage(options);
      return rdfToolkitOptions;
    }
    if (rdfToolkitOptions.getRunningMode() == RUN_ON_DIRECTORY) {
      return rdfToolkitOptions;
    }

    var sourceFile = optionHandler.handleSourceFile();
    optionHandler.handleTargetFile();
    optionHandler.handleBaseIri(valueFactory);
    optionHandler.handleIriReplacementOptions();
    optionHandler.handleUseDtdSubset();
    optionHandler.handleInlineBlankNodes();
    optionHandler.handleInferBaseIri();
    optionHandler.handleLeadingComments();
    optionHandler.handleTrailingComments();
    optionHandler.handleStringDataTyping();
    optionHandler.handleOverrideStringLanguage();
    optionHandler.handleIndent();
    optionHandler.handleSourceFormat(sourceFile);
    optionHandler.handleTargetFormat();
    optionHandler.handleShortUriPref();
    optionHandler.handleLineEnd();

    rdfToolkitOptions.setRunningMode(RUN_ON_FILE);

    return rdfToolkitOptions;
  }

  private String getVersion() {
    String implementationTitle = Manifests.read("Implementation-Title");
    String implementationVersion = Manifests.read("Implementation-Version");
    return String.format(
        "%s (%s version %s)",
        RdfFormatter.class.getSimpleName(),
        implementationTitle,
        implementationVersion);
  }

  private void runOnFile(RdfToolkitOptions rdfToolkitOptions) throws Exception {
    var sourceModel = readModel(rdfToolkitOptions);

    // Do any URI replacements
    if ((rdfToolkitOptions.getIriPattern() != null) && (rdfToolkitOptions.getIriReplacement() != null)) {
      Model replacedModel = new TreeModel();
      for (Statement st : sourceModel) {
        Resource replacedSubject = st.getSubject();
        if (replacedSubject instanceof IRI) {
          replacedSubject = valueFactory.createIRI(
              replacedSubject.stringValue().replaceFirst(
                  rdfToolkitOptions.getIriPattern(),
                  rdfToolkitOptions.getIriReplacement()));
        }

        IRI replacedPredicate = st.getPredicate();
        replacedPredicate = valueFactory.createIRI(
            replacedPredicate.stringValue().replaceFirst(
                rdfToolkitOptions.getIriPattern(),
                rdfToolkitOptions.getIriReplacement()));

        Value replacedObject = st.getObject();
        if (replacedObject instanceof IRI) {
          replacedObject = valueFactory.createIRI(
              replacedObject.stringValue().replaceFirst(
                  rdfToolkitOptions.getIriPattern(),
                  rdfToolkitOptions.getIriReplacement()));
        }

        Statement replacedStatement = valueFactory.createStatement(replacedSubject, replacedPredicate, replacedObject);
        replacedModel.add(replacedStatement);
      }
      // Do IRI replacements in namespaces as well.
      Set<Namespace> namespaces = sourceModel.getNamespaces();
      for (Namespace namespace : namespaces) {
        replacedModel.setNamespace(
            namespace.getPrefix(),
            namespace.getName().replaceFirst(
                rdfToolkitOptions.getIriPattern(),
                rdfToolkitOptions.getIriReplacement()));
      }
      sourceModel = replacedModel;
      // This is also the right time to do IRI replacement in the base URI, if appropriate
      if (rdfToolkitOptions.getBaseIri() != null) {
        String newBaseIriString = rdfToolkitOptions.getBaseIriString().replaceFirst(
            rdfToolkitOptions.getIriPattern(),
            rdfToolkitOptions.getIriReplacement());
        rdfToolkitOptions.setBaseIriString(newBaseIriString);
        rdfToolkitOptions.setBaseIri(valueFactory.createIRI(newBaseIriString));
      }
    }

    // Infer the base URI, if requested
    IRI inferredBaseIri = null;
    if (rdfToolkitOptions.getInferBaseIri()) {
      LinkedList<IRI> owlOntologyIris = new LinkedList<>();
      for (Statement st : sourceModel) {
        if ((Constants.RDF_TYPE.equals(st.getPredicate())) &&
            (Constants.owlOntology.equals(st.getObject())) &&
            (st.getSubject() instanceof IRI)) {
          owlOntologyIris.add((IRI)st.getSubject());
        }
      }
      if (!owlOntologyIris.isEmpty()) {
        Comparator<IRI> iriComparator = Comparator.comparing(IRI::toString);
        owlOntologyIris.sort(iriComparator);
        inferredBaseIri = owlOntologyIris.getFirst();
      }
    }
    if (rdfToolkitOptions.getInferBaseIri() && (inferredBaseIri != null)) {
      rdfToolkitOptions.setBaseIri(inferredBaseIri);
    }

    Writer targetWriter = new OutputStreamWriter(
        rdfToolkitOptions.getTargetOutputStream(),
        StandardCharsets.UTF_8.name());
    SortedRdfWriterFactory factory = new SortedRdfWriterFactory(rdfToolkitOptions.getTargetFormat());
    RDFWriter rdfWriter = factory.getWriter(targetWriter, rdfToolkitOptions.getOptions());
    Rio.write(sourceModel, rdfWriter);
    targetWriter.flush();
    targetWriter.close();
  }

  private Model readModel(RdfToolkitOptions rdfToolkitOptions) {
    Model sourceModel = null;
    try {
      sourceModel = Rio.parse(
          rdfToolkitOptions.getSourceInputStream(),
          rdfToolkitOptions.getBaseIriString(),
          rdfToolkitOptions.getRdf4jSourceFormat());
    } catch (Exception t) {
      LOGGER.error("{}: stopped by unexpected exception:", RdfFormatter.class.getSimpleName());
      LOGGER.error("Unable to parse input file: {}", rdfToolkitOptions.getSourceFile().getAbsolutePath());
      LOGGER.error("Command line arguments: {}", Arrays.toString(rdfToolkitOptions.getArgs()));
      LOGGER.error("{}: {}", t.getClass().getSimpleName(), t.getMessage());
      StringWriter stackTraceWriter = new StringWriter();
      t.printStackTrace(new PrintWriter(stackTraceWriter));
      LOGGER.error(stackTraceWriter.toString());
      usage(options);
      System.exit(1);
    }
    return sourceModel;
  }

  private void runOnDirectory(CommandLine line) throws Exception {
    // Construct list of common arguments passed on to every invocation of 'run'
    ArrayList<String> commonArgsList = new ArrayList<>();
    List<String> noPassArgs = Arrays.asList(
        SOURCE_DIRECTORY.getShortOpt(),
        SOURCE_DIRECTORY_PATTERN.getShortOpt(),
        TARGET_DIRECTORY.getShortOpt(),
        TARGET_DIRECTORY_PATTERN.getShortOpt());

    for (Option option : line.getOptions()) {
      if (noPassArgs.contains(option.getOpt())) { continue; }
      commonArgsList.add(String.format("-%s", option.getOpt()));
      if (option.hasArg()) {
        commonArgsList.add(option.getValue());
      }
    }

    // Check the input & output directories
    var sourceDir = new File(line.getOptionValue(SOURCE_DIRECTORY.getShortOpt()));
    if (!sourceDir.exists()) {
      LOGGER.error("Source directory does not exist: {}", sourceDir.getAbsolutePath());
      return;
    }
    if (!sourceDir.canRead()) {
      LOGGER.error("Source directory is not readable: {}", sourceDir.getAbsolutePath());
      return;
    }
    var sourceDirPattern = Pattern.compile(line.getOptionValue("sdp"));

    final File targetDir = new File(line.getOptionValue("td"));
    if (!targetDir.exists()) {
      targetDir.mkdirs();
    }
    if (!targetDir.exists()) {
      LOGGER.error("Target directory could not be created: {}", targetDir.getAbsolutePath());
      return;
    }
    if (!targetDir.canWrite()) {
      LOGGER.error("Target directory is not writable: {}", targetDir.getAbsolutePath());
      return;
    }
    final String targetDirPatternString = line.getOptionValue("tdp");

    // Iterate through matching files.
    final DirectoryWalker dw = new DirectoryWalker(sourceDir, sourceDirPattern);
    final String[] stringArray = new String[]{};
    for (DirectoryWalker.DirectoryWalkerResult sourceResult : dw.pathMatches()) {
      // Construct output path.
      final Matcher sourceMatcher = sourceDirPattern.matcher(sourceResult.getRelativePath());
      final String targetRelativePath = sourceMatcher.replaceFirst(targetDirPatternString);
      final File targetFile = new File(targetDir, targetRelativePath);

      // Run serializer
      List<String> runArgs = new ArrayList<>();
      runArgs.addAll(commonArgsList);
      runArgs.add("-s");
      runArgs.add(sourceResult.getFile().getAbsolutePath());
      runArgs.add("-t");
      runArgs.add(targetFile.getAbsolutePath());
      LOGGER.info("... formatting '{}' to '{}' ...", sourceResult.getRelativePath(), targetRelativePath);

      run(runArgs.toArray(stringArray));
    }
  }

  private void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(100);
    formatter.printHelp(getVersion(), options);
  }
}