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

import static org.edmcouncil.rdf_toolkit.util.Constants.BASE_IRI;
import static org.edmcouncil.rdf_toolkit.util.Constants.USE_DEFAULT_LANGUAGE;
import static org.edmcouncil.rdf_toolkit.util.Constants.INDENT;
import static org.edmcouncil.rdf_toolkit.util.Constants.INLINE_BLANK_NODES;
import static org.edmcouncil.rdf_toolkit.util.Constants.LEADING_COMMENTS;
import static org.edmcouncil.rdf_toolkit.util.Constants.LINE_END;
import static org.edmcouncil.rdf_toolkit.util.Constants.OMIT_XMLNS_NAMESPACE;
import static org.edmcouncil.rdf_toolkit.util.Constants.OVERRIDE_STRING_LANGUAGE;
import static org.edmcouncil.rdf_toolkit.util.Constants.SHORT_URI_PREF;
import static org.edmcouncil.rdf_toolkit.util.Constants.STRING_DATA_TYPE_OPTION;
import static org.edmcouncil.rdf_toolkit.util.Constants.SUPPRESS_NAMED_INDIVIDUALS;
import static org.edmcouncil.rdf_toolkit.util.Constants.TRAILING_COMMENTS;
import static org.edmcouncil.rdf_toolkit.util.Constants.USE_DTD_SUBSET;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.edmcouncil.rdf_toolkit.io.format.TargetFormats;
import org.edmcouncil.rdf_toolkit.runner.constant.RunningMode;
import org.edmcouncil.rdf_toolkit.util.ShortIriPreferences;
import org.edmcouncil.rdf_toolkit.util.StringDataTypeOptions;

public class RdfToolkitOptions {

  private final String[] args;
  private String output;
  private RunningMode runningMode;
  private CommandLine commandLine;
  private String iriPattern;
  private String iriReplacement;
  private TargetFormats targetFormat;
  private File targetFile;
  private boolean shouldUseStandardOutputStream;
  private String baseIriString;
  private IRI baseIri;
  private File sourceFile;
  private InputStream sourceInputStream;
  private boolean inferBaseIri;
  private boolean inlineBlankNodes;
  private boolean useDtdSubset;
  private StringDataTypeOptions stringDataTypeOption;
  private String indent;
  private ShortIriPreferences shortUriPref;
  private RDFFormat rdf4jSourceFormat;
  private String[] leadingComments;
  private String[] trailingComments;
  private String overrideStringLanguage;
  private String lineEnd;
  private boolean omitXmlnsNamespace;
  private boolean suppressNamedIndividuals;
  private String defaultLanguage;

  public RdfToolkitOptions(String[] args) {
    this.args = args;
  }

  public String[] getArgs() {
    return args;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public Map<String, Object> getOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put(BASE_IRI, getBaseIri());
    options.put(INDENT, getIndent());
    options.put(SHORT_URI_PREF, getShortUriPref());
    options.put(USE_DTD_SUBSET, getUseDtdSubset());
    options.put(INLINE_BLANK_NODES, getInlineBlankNodes());
    options.put(LEADING_COMMENTS, getLeadingComments());
    options.put(TRAILING_COMMENTS, getTrailingComments());
    options.put(STRING_DATA_TYPE_OPTION, getStringDataTypeOption());
    options.put(OVERRIDE_STRING_LANGUAGE, getOverrideStringLanguage());
    options.put(LINE_END, getLineEnd());
    options.put(OMIT_XMLNS_NAMESPACE, getOmitXmlnsNamespace());
    options.put(SUPPRESS_NAMED_INDIVIDUALS, getSuppressNamedIndividuals());
    options.put(USE_DEFAULT_LANGUAGE, getDefaultLanguage());
    return options;
  }

  public RunningMode getRunningMode() {
    return runningMode;
  }

  public void setRunningMode(RunningMode runningMode) {
    this.runningMode = runningMode;
  }

  public CommandLine getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  public File getTargetFile() {
    return targetFile;
  }

  public void setTargetFile(File targetFile) {
    this.targetFile = targetFile;
  }

  public boolean isShouldUseStandardOutputStream() {
    return shouldUseStandardOutputStream;
  }

  public void setShouldUseStandardOutputStream(boolean shouldUseStandardOutputStream) {
    this.shouldUseStandardOutputStream = shouldUseStandardOutputStream;
  }

  public TargetFormats getTargetFormat() {
    return targetFormat;
  }

  public void setFormat(TargetFormats targetFormat) {
    this.targetFormat = targetFormat;
  }

  public InputStream getSourceInputStream() {
    return sourceInputStream;
  }

  public void setSourceInputStream(InputStream sourceInputStream) {
    this.sourceInputStream = sourceInputStream;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public String getBaseIriString() {
    return baseIriString;
  }

  public void setBaseIriString(String baseIriString) {
    this.baseIriString = baseIriString;
  }

  public IRI getBaseIri() {
    return baseIri;
  }

  public void setBaseIri(IRI baseIri) {
    this.baseIri = baseIri;
  }

  public String getIriPattern() {
    return iriPattern;
  }

  public void setIriPattern(String iriPattern) {
    this.iriPattern = iriPattern;
  }

  public String getIriReplacement() {
    return iriReplacement;
  }

  public void setIriReplacement(String iriReplacement) {
    this.iriReplacement = iriReplacement;
  }

  public boolean getInferBaseIri() {
    return inferBaseIri;
  }

  public void setInferBaseIri(boolean inferBaseIri) {
    this.inferBaseIri = inferBaseIri;
  }

  public boolean getUseDtdSubset() {
    return this.useDtdSubset;
  }

  public void setUseDtdSubset(boolean useDtdSubset) {
    this.useDtdSubset = useDtdSubset;
  }

  public boolean getInlineBlankNodes() {
    return inlineBlankNodes;
  }

  public void setInlineBlankNodes(boolean inlineBlankNodes) {
    this.inlineBlankNodes = inlineBlankNodes;
  }

  public StringDataTypeOptions getStringDataTypeOption() {
    return stringDataTypeOption;
  }

  public void setStringDataTypeOption(StringDataTypeOptions stringDataTypeOption) {
    this.stringDataTypeOption = stringDataTypeOption;
  }

  public String getIndent() {
    return indent;
  }

  public void setIndent(String indent) {
    this.indent = indent;
  }

  public ShortIriPreferences getShortUriPref() {
    return shortUriPref;
  }

  public void setShortUriPref(ShortIriPreferences shortUriPref) {
    this.shortUriPref = shortUriPref;
  }

  public RDFFormat getRdf4jSourceFormat() {
    return rdf4jSourceFormat;
  }

  public void setRdf4jSourceFormat(RDFFormat rdf4jSourceFormat) {
    this.rdf4jSourceFormat = rdf4jSourceFormat;
  }

  public String[] getLeadingComments() {
    return leadingComments;
  }

  public void setLeadingComments(String[] leadingComments) {
    this.leadingComments = leadingComments;
  }

  public String[] getTrailingComments() {
    return trailingComments;
  }

  public void setTrailingComments(String[] trailingComments) {
    this.trailingComments = trailingComments;
  }

  public String getOverrideStringLanguage() {
    return overrideStringLanguage;
  }

  public void setOverrideStringLanguage(String overrideStringLanguage) {
    this.overrideStringLanguage = overrideStringLanguage;
  }

  public String getLineEnd() {
    return lineEnd;
  }

  public void setLineEnd(String lineEnd) {
    this.lineEnd = lineEnd;
  }

  public boolean getOmitXmlnsNamespace() {
    return this.omitXmlnsNamespace;
  }

  public void setOmitXmlnsNamespace(boolean omitXmlnsNamespace) {
    this.omitXmlnsNamespace = omitXmlnsNamespace;
  }

  public boolean getSuppressNamedIndividuals() {
    return suppressNamedIndividuals;
  }

  public void setSuppressNamedIndividuals(boolean suppressNamedIndividuals) {
    this.suppressNamedIndividuals = suppressNamedIndividuals;
  }

  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
  }
}