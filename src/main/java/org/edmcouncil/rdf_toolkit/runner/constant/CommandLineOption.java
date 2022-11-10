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
package org.edmcouncil.rdf_toolkit.runner.constant;

import org.apache.commons.cli.Options;
import org.edmcouncil.rdf_toolkit.io.format.SourceFormats;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.edmcouncil.rdf_toolkit.util.StringDataTypeOptions;

public enum CommandLineOption {

  SOURCE("s", "source", true, "source (input) RDF file to format"),
  SOURCE_DIRECTORY("sd", "source-directory", true, "source (input) directory of RDF files to format.  This is a directory processing option"),
  SOURCE_DIRECTORY_PATTERN("sdp", "source-directory-pattern", true, "relative file path pattern (regular expression) used to select files to format in the source directory.  This is a directory processing option"),
  SOURCE_FORMAT("sfmt", "source-format", true, "source (input) RDF format; one of: " + SourceFormats.summarise()),
  TARGET("t", "target", true, "target (output) RDF file"),
  TARGET_DIRECTORY("td", "target-directory", true, "target (output) directory for formatted RDF files.  This is a directory processing option"),
  TARGET_DIRECTORY_PATTERN("tdp", "target-directory-pattern", true, "relative file path pattern (regular expression) used to construct file paths within the target directory.  This is a directory processing option"),
  TARGET_FORMAT("tfmt", "target-format", true, "target (output) RDF format: one of: " + TargetFormats.summarise()),
  VERSION("v", "version", false, "print out version details"),
  HELP("h", "help", false, "print out details of the command-line arguments for the program"),
  BASE_IRI("bi", "base-iri", true, "set IRI to use as base URI"),
  SHORT_IRI_PRIORITY("sip", "short-iri-priority", true, "set what takes priority when shortening IRIs: " + ShortIriPreferences.summarise()),
  IRI_PATTERN("ip", "iri-pattern", true, "set a pattern to replace in all IRIs (used together with --iri-replacement)"),
  IRI_REPLACEMENT("ir", "iri-replacement", true, "set replacement text used to replace a matching pattern in all IRIs (used together with --iri-pattern)"),
  USE_DTD_SUBSET("dtd", "use-dtd-subset", false, "for XML, use a DTD subset in order to allow prefix-based IRI shortening"),
  INLINE_BLANK_NODES("ibn", "inline-blank-nodes", false, "use inline representation for blank nodes.  NOTE: this will fail if there are any recursive relationships involving blank nodes.  Usually OWL has no such recursion involving blank nodes.  It also will fail if any blank nodes are a triple subject but not a triple object."),
  INFER_BASE_IRI("ibi", "infer-base-iri", false, "use the OWL ontology IRI as the base URI.  Ignored if an explicit base IRI has been set"),
  LEADING_COMMENT("lc", "leading-comment", true, "sets the text of the leading comment in the ontology.  Can be repeated for a multi-line comment"),
  TRAILING_COMMENT("tc", "trailing-comment", true, "sets the text of the trailing comment in the ontology.  Can be repeated for a multi-line comment"),
  STRING_DATA_TYPING("sdt", "string-data-typing", true, "sets whether string data values have explicit data types, or not; one of: " + StringDataTypeOptions.summarise()),
  OVERRIDE_STRING_LANGUAGE("osl", "override-string-language", true, "sets an override language that is applied to all strings"),
  INDENT("i", "indent", true, "sets the indent string.  Default is a single tab character"),
  LINE_END("ln", "line-end", true, "sets the end-line character(s); supported characters: \\n (LF), \\r (CR). Default is the LF character"),
  OMIT_XMLNS_NAMESPACE("oxn", "omit-xmlns-namespace", false, "omits xmlns namespace"),
  SUPPRESS_NAMED_INDIVIDUALS("sni", "suppress-named-individuals", false, "suppresses all instances of owl:NamedIndividual");

  private final String shortOpt;
  private final String longOpt;
  private final boolean hasArgument;
  private final String description;

  CommandLineOption(String shortOpt, String longOpt, boolean hasArgument, String description) {
    this.shortOpt = shortOpt;
    this.longOpt = longOpt;
    this.hasArgument = hasArgument;
    this.description = description;
  }

  public String getShortOpt() {
    return shortOpt;
  }

  public String getLongOpt() {
    return longOpt;
  }

  public boolean getHasArgument() {
    return hasArgument;
  }

  public String getDescription() {
    return description;
  }

  public static Options prepareOptions() {
    var options = new Options();
    for (CommandLineOption option : values()) {
      options.addOption(
          option.getShortOpt(),
          option.getLongOpt(),
          option.getHasArgument(),
          option.getDescription());
    }
    return options;
  }
}
