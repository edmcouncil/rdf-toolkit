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

import static org.edmcouncil.rdf_toolkit.runner.constant.CommandLineOption.HELP;
import static org.edmcouncil.rdf_toolkit.runner.constant.CommandLineOption.VERSION;
import static org.edmcouncil.rdf_toolkit.runner.constant.RunningMode.EXIT;
import static org.edmcouncil.rdf_toolkit.runner.constant.RunningMode.PRINT_AND_EXIT;
import static org.edmcouncil.rdf_toolkit.runner.constant.RunningMode.PRINT_USAGE_AND_EXIT;
import static org.edmcouncil.rdf_toolkit.runner.constant.RunningMode.RUN_ON_DIRECTORY;
import static org.edmcouncil.rdf_toolkit.runner.constant.RunningMode.RUN_ON_FILE;

import com.jcabi.manifests.Manifests;
import java.io.FileNotFoundException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.memory.model.MemValueFactory;
import org.edmcouncil.rdf_toolkit.RdfFormatter;
import org.edmcouncil.rdf_toolkit.runner.constant.CommandLineOption;
import org.edmcouncil.rdf_toolkit.runner.exception.RdfToolkitOptionHandlingException;

public class CommandLineArgumentsHandler {

  private final Options options;
  private final ValueFactory valueFactory;

  public CommandLineArgumentsHandler() {
    this.options = CommandLineOption.prepareOptions();
    this.valueFactory = new MemValueFactory();
  }

  public RdfToolkitOptions handleArguments(String[] args)
      throws RdfToolkitOptionHandlingException, FileNotFoundException, ParseException {
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
    optionHandler.handleOmitXmlnsNamespace();
    optionHandler.handleSuppressNamedIndividuals();

    rdfToolkitOptions.setRunningMode(RUN_ON_FILE);

    return rdfToolkitOptions;
  }

  private void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(100);
    formatter.printHelp(getVersion(), options);
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
}
