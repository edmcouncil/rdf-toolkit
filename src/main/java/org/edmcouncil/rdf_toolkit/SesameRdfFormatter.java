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
package org.edmcouncil.rdf_toolkit;

import org.apache.commons.cli.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF formatter that formats in a consistent order, friendly for version control systems.
 * Should not be used with files that are too large to be fully loaded into memory for sorting.
 * Run with "--help" option for help.
 */
public class SesameRdfFormatter {

    private static final Logger logger = LoggerFactory.getLogger(SesameRdfFormatter.class);

    private static Options options = null;

    static {
        // Create list of program options, using Apache Commons CLI library.
        options = new Options();
        options.addOption(
                "s", "source", true, "source (input) RDF file to format"
        );
        options.addOption(
                "sfmt", "source-format", true, "source (input) RDF format; one of: " + SesameSortedRDFWriterFactory.SourceFormats.summarise()
        );
        options.addOption(
                "t", "target", true, "target (output) RDF file"
        );
        options.addOption(
                "tfmt", "target-format", true, "target (output) RDF format: one of: " + SesameSortedRDFWriterFactory.TargetFormats.summarise()
        );
        options.addOption(
                "h", "help", false, "print out details of the command-line arguments for the program"
        );
        options.addOption(
                "bu", "base-uri", true, "set URI to use as base URI"
        );
        options.addOption(
                "sup", "short-uri-priority", true, "set what takes priority when shortening URIs: " + SesameSortedRDFWriter.ShortUriPreferences.summarise()
        );
        options.addOption(
                "up", "uri-pattern", true, "set a pattern to replace in all URIs (used together with --uri-replacement)"
        );
        options.addOption(
                "ur", "uri-replacement", true, "set replacement text used to replace a matching pattern in all URIs (used together with --uri-pattern)"
        );
        options.addOption(
                "dtd", "use-dtd-subset", false, "for XML, use a DTD subset in order to allow prefix-based URI shortening"
        );
        options.addOption(
                "ibn", "inline-blank-nodes", false, "use inline representation for blank nodes.  NOTE: this will fail if there are any recursive relationships involving blank nodes.  Usually OWL has no such recursion involving blank nodes.  It also will fail if any blank nodes are a triple subject but not a triple object."
        );
        options.addOption(
                "ibu", "infer-base-uri", false, "use the OWL ontology URI as the base URI.  Ignored if an explicit base URI has been set"
        );
        options.addOption(
                "lc", "leading-comment", true, "sets the text of the leading comment in the ontology.  Can be repeated for a multi-line comment"
        );
        options.addOption(
                "tc", "trailing-comment", true, "sets the text of the trailing comment in the ontology.  Can be repeated for a multi-line comment"
        );
        options.addOption(
                "sdt", "string-data-typing", true, "sets whether string data values have explicit data types, or not; one of: " + SesameSortedRDFWriterFactory.StringDataTypeOptions.summarise()
        );
    }

    /** Main method for running the RDF formatter. Run with "--help" option for help. */
    public static void main(String[] args) {
        // Main program block.
        try {
            run(args);
            System.exit(0);
        } catch (Throwable t) {
            logger.error(SesameRdfFormatter.class.getSimpleName() + ": stopped by unexpected exception:");
            logger.error(t.getClass().getSimpleName() + ": " + t.getMessage());
            StringWriter stackTraceWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTraceWriter));
            logger.error(stackTraceWriter.toString());
            usage(options);
            System.exit(1);
        }

    }

    /** Main method, but throws exceptions for use from inside other Java code. */
    public static void run(String[] args) throws Exception {
        final String indent = "\t\t";

        URI baseUri = null;
        String baseUriString = "";
        String uriPattern = null;
        String uriReplacement = null;
        boolean useDtdSubset = false;
        boolean inlineBlankNodes = false;
        boolean inferBaseUri = false;
        URI inferredBaseUri = null;
        String[] leadingComments = null;
        String[] trailingComments = null;
        SesameSortedRDFWriterFactory.StringDataTypeOptions stringDataTypeOption = SesameSortedRDFWriterFactory.StringDataTypeOptions.implicit;

        // Parse the command line options.
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse( options, args );

        // Print out help, if requested.
        if (line.hasOption("h")) {
            usage(options);
            return;
        }

        // Check if required arguments provided.
        if (!line.hasOption("s")) {
            logger.error("No source (input) file specified, nothing to format.  Use --help for help.");
            return;
        }
        if (!line.hasOption("t")) {
            logger.error("No target (target) file specified, cannot format source.  Use --help for help.");
            return;
        }

        // Check if source files exists.
        String sourceFilePath = line.getOptionValue("s");
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            logger.error("Source file does not exist: " + sourceFilePath);
            return;
        }
        if (!sourceFile.isFile()) {
            logger.error("Source file is not a file: " + sourceFilePath);
            return;
        }
        if (!sourceFile.canRead()) {
            logger.error("Source file is not readable: " + sourceFilePath);
            return;
        }

        // Check if target file can be written.
        String targetFilePath = line.getOptionValue("t");
        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            if (!targetFile.isFile()) {
                logger.error("Target file is not a file: " + targetFilePath);
                return;
            }
            if (!targetFile.canWrite()) {
                logger.error("Target file is not writable: " + targetFilePath);
                return;
            }
        }

        // Create directory for target file, if required.
        File targetFileDir = targetFile.getParentFile();
        if (targetFileDir != null) {
            targetFileDir.mkdirs();
            if (!targetFileDir.exists()) {
                logger.error("Target file directory could not be created: " + targetFileDir.getAbsolutePath());
                return;
            }
        }

        // Check if a base URI was provided
        try {
            if (line.hasOption("bu")) {
                baseUriString = line.getOptionValue("bu");
                baseUri = new URIImpl(baseUriString);
                if (baseUriString.endsWith("#")) {
                    logger.warn("base URI ends in '#', which is unusual: " + baseUriString);
                }
            }
        } catch (Throwable t) {
            baseUri = null;
            baseUriString = "";
        }

        // Check if there is a valid URI pattern/replacement pair
        if (line.hasOption("up")) {
            if(line.hasOption("ur")) {
                if (line.getOptionValue("up").length() < 1) {
                    logger.error("A URI pattern cannot be an empty string.  Use --help for help.");
                    return;
                }
                uriPattern = line.getOptionValue("up");
                uriReplacement = line.getOptionValue("ur");
            } else {
                logger.error("If a URI pattern is specified, a URI replacement must also be specified.  Use --help for help.");
                return;
            }
        } else {
            if (line.hasOption("ur")) {
                logger.error("If a URI replacement is specified, a URI pattern must also be specified.  Use --help for help.");
                return;
            }
        }

        // Check if a DTD subset should be used for namespace shortening in XML
        if (line.hasOption("dtd")) {
            useDtdSubset = true;
        }

        // Check if blank nodes should be rendered inline
        if (line.hasOption("ibn")) {
            inlineBlankNodes = true;
        }

        // Check if the base URI should be set to be the same as the OWL ontology URI
        if (line.hasOption("ibu")) {
            inferBaseUri = true;
        }

        // Check if there are leading comments.
        if (line.hasOption("lc")) {
            leadingComments = line.getOptionValues("lc");
        }

        // Check if there are trailing comments.
        if (line.hasOption("tc")) {
            trailingComments = line.getOptionValues("tc");
        }

        // Check if there is a string data type option.
        if (line.hasOption("sdt")) {
            stringDataTypeOption = SesameSortedRDFWriterFactory.StringDataTypeOptions.getByOptionValue(line.getOptionValue("sdt"));
        }

        // Load RDF file.
        SesameSortedRDFWriterFactory.SourceFormats sourceFormat = null;
        if (line.hasOption("sfmt")) {
            sourceFormat = SesameSortedRDFWriterFactory.SourceFormats.getByOptionValue(line.getOptionValue("sfmt"));
        } else {
            sourceFormat = SesameSortedRDFWriterFactory.SourceFormats.auto;
        }
        if (sourceFormat == null) {
            logger.error("Unsupported or unrecognised source format: " + line.getOptionValue("sfmt"));
            return;
        }
        RDFFormat sesameSourceFormat = null;
        if (SesameSortedRDFWriterFactory.SourceFormats.auto.equals(sourceFormat)) {
            sesameSourceFormat = Rio.getParserFormatForFileName(sourceFilePath, RDFFormat.TURTLE);
        } else {
            sesameSourceFormat = sourceFormat.getRDFFormat();
        }
        if (sesameSourceFormat == null) {
            logger.error("Unsupported or unrecognised source format enum: " + sourceFormat);
        }
        Model sourceModel = Rio.parse(new FileInputStream(sourceFile), baseUriString, sesameSourceFormat);

        // Do any URI replacements
        if ((uriPattern != null) && (uriReplacement != null)) {
            Model replacedModel = new TreeModel();
            for (Statement st : sourceModel) {
                Resource replacedSubject = st.getSubject();
                if (replacedSubject instanceof URI) {
                    replacedSubject = new URIImpl(replacedSubject.stringValue().replaceFirst(uriPattern, uriReplacement));
                }

                URI replacedPredicate = st.getPredicate();
                replacedPredicate = new URIImpl(replacedPredicate.stringValue().replaceFirst(uriPattern, uriReplacement));

                Value replacedObject = st.getObject();
                if (replacedObject instanceof URI) {
                    replacedObject = new URIImpl(replacedObject.stringValue().replaceFirst(uriPattern, uriReplacement));
                }

                Statement replacedStatement = new StatementImpl(replacedSubject, replacedPredicate, replacedObject);
                replacedModel.add(replacedStatement);
            }
            // Do URI replacements in namespaces as well.
            Set<Namespace> namespaces = sourceModel.getNamespaces();
            for (Namespace nmsp : namespaces) {
                replacedModel.setNamespace(nmsp.getPrefix(), nmsp.getName().replaceFirst(uriPattern, uriReplacement));
            }
            sourceModel = replacedModel;
            // This is also the right time to do URI replacement in the base URI, if appropriate
            if (baseUri != null) {
                baseUriString = baseUriString.replaceFirst(uriPattern, uriReplacement);
                baseUri = new URIImpl(baseUriString);
            }
        }

        // Infer the base URI, if requested
        if (inferBaseUri) {
            LinkedList<URI> owlOntologyUris = new LinkedList<URI>();
            for (Statement st : sourceModel) {
                if ((SesameSortedRDFWriter.rdfType.equals(st.getPredicate())) && (SesameSortedRDFWriter.owlOntology.equals(st.getObject())) && (st.getSubject() instanceof URI)) {
                    owlOntologyUris.add((URI)st.getSubject());
                }
            }
            if (owlOntologyUris.size() >= 1) {
                Comparator<URI> uriComparator = new Comparator<URI>() {
                    @Override
                    public int compare(URI uri1, URI uri2) {
                        return uri1.toString().compareTo(uri2.toString());
                    }
                };
                owlOntologyUris.sort(uriComparator);
                inferredBaseUri = owlOntologyUris.getFirst();
            }
        }

        // Write sorted RDF file.
        SesameSortedRDFWriterFactory.TargetFormats targetFormat = null;
        if (line.hasOption("tfmt")) {
            targetFormat = SesameSortedRDFWriterFactory.TargetFormats.getByOptionValue(line.getOptionValue("tfmt"));
        } else {
            targetFormat = SesameSortedRDFWriterFactory.TargetFormats.turtle;
        }
        if (targetFormat == null) {
            logger.error("Unsupported or unrecognised target format: " + line.getOptionValue("tfmt"));
            return;
        }
        SesameSortedRDFWriter.ShortUriPreferences shortUriPref = null;
        if (line.hasOption("sup")) {
            shortUriPref = SesameSortedRDFWriter.ShortUriPreferences.getByOptionValue(line.getOptionValue("sup"));
        } else {
            shortUriPref = SesameSortedRDFWriter.ShortUriPreferences.prefix;
        }
        if (shortUriPref == null) {
            logger.error("Unsupported or unrecognised short URI preference: " + line.getOptionValue("sup"));
            return;
        }

        Writer targetWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
        SesameSortedRDFWriterFactory factory = new SesameSortedRDFWriterFactory(targetFormat);
        Map<String, Object> writerOptions = new HashMap<String, Object>();
        if (baseUri != null) {
            writerOptions.put("baseUri", baseUri);
        } else if (inferBaseUri && (inferredBaseUri != null)) {
            writerOptions.put("baseUri", inferredBaseUri);
        }
        if (indent != null) { writerOptions.put("indent", indent); }
        if (shortUriPref != null) { writerOptions.put("shortUriPref", shortUriPref); }
        writerOptions.put("useDtdSubset", useDtdSubset);
        writerOptions.put("inlineBlankNodes", inlineBlankNodes);
        writerOptions.put("leadingComments", leadingComments);
        writerOptions.put("trailingComments", trailingComments);
        writerOptions.put("stringDataTypeOption", stringDataTypeOption);
        RDFWriter rdfWriter = factory.getWriter(targetWriter, writerOptions);
        Rio.write(sourceModel, rdfWriter);
        targetWriter.flush();
        targetWriter.close();
    }

    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "SesameRdfFormatter", options );
    }

}
