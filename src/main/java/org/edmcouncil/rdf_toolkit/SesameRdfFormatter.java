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
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.manifests.Manifests;

import javax.annotation.Nonnull;
import javax.io.DirectoryWalker;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * RDF formatter that formats in a consistent order, friendly for version control systems.
 * Should not be used with files that are too large to be fully loaded into memory for sorting.
 * Run with "--help" option for help.
 */
public class SesameRdfFormatter {

    private static final Logger logger = LoggerFactory.getLogger(SesameRdfFormatter.class);

    private static Options options = null;

    private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

    static {
        // Create list of program options, using Apache Commons CLI library.
        options = new Options();
        options.addOption(
                "s", "source", true, "source (input) RDF file to format"
        );
        options.addOption(
                "sd", "source-directory", true, "source (input) directory of RDF files to format.  This is a directory processing option"
        );
        options.addOption(
                "sdp", "source-directory-pattern", true, "relative file path pattern (regular expression) used to select files to format in the source directory.  This is a directory processing option"
        );
        options.addOption(
                "sfmt", "source-format", true, "source (input) RDF format; one of: " + SesameSortedRDFWriterFactory.SourceFormats.summarise()
        );
        options.addOption(
                "t", "target", true, "target (output) RDF file"
        );
        options.addOption(
                "td", "target-directory", true, "target (output) directory for formatted RDF files.  This is a directory processing option"
        );
        options.addOption(
                "tdp", "target-directory-pattern", true, "relative file path pattern (regular expression) used to construct file paths within the target directory.  This is a directory processing option"
        );
        options.addOption(
                "tfmt", "target-format", true, "target (output) RDF format: one of: " + SesameSortedRDFWriterFactory.TargetFormats.summarise()
        );
        options.addOption(
                "v", "version", false, "print out version details"
        );
        options.addOption(
                "h", "help", false, "print out details of the command-line arguments for the program"
        );
        options.addOption(
                "bi", "base-iri", true, "set IRI to use as base URI"
        );
        options.addOption(
                "sip", "short-iri-priority", true, "set what takes priority when shortening IRIs: " + SesameSortedRDFWriter.ShortIriPreferences.summarise()
        );
        options.addOption(
                "ip", "iri-pattern", true, "set a pattern to replace in all IRIs (used together with --iri-replacement)"
        );
        options.addOption(
                "ir", "iri-replacement", true, "set replacement text used to replace a matching pattern in all IRIs (used together with --iri-pattern)"
        );
        options.addOption(
                "dtd", "use-dtd-subset", false, "for XML, use a DTD subset in order to allow prefix-based IRI shortening"
        );
        options.addOption(
                "ibn", "inline-blank-nodes", false, "use inline representation for blank nodes.  NOTE: this will fail if there are any recursive relationships involving blank nodes.  Usually OWL has no such recursion involving blank nodes.  It also will fail if any blank nodes are a triple subject but not a triple object."
        );
        options.addOption(
                "ibi", "infer-base-iri", false, "use the OWL ontology IRI as the base URI.  Ignored if an explicit base IRI has been set"
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
        options.addOption(
                "i", "indent", true, "sets the indent string.  Default is a single tab character"
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
        IRI baseIri = null;
        String baseIriString = "";
        String iriPattern = null;
        String iriReplacement = null;
        boolean useDtdSubset = false;
        boolean inlineBlankNodes = false;
        boolean inferBaseIri = false;
        IRI inferredBaseIri = null;
        String[] leadingComments = null;
        String[] trailingComments = null;
        String indent = "\t";
        SesameSortedRDFWriterFactory.StringDataTypeOptions stringDataTypeOption = SesameSortedRDFWriterFactory.StringDataTypeOptions.implicit;

        // Parse the command line options.
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse( options, args );

        // Print out version, if requested.
        if (line.hasOption("v")) {
            System.out.println(getVersion());
            System.out.flush();
            return;
        }

        // Print out help, if requested.
        if (line.hasOption("h")) {
            usage(options);
            return;
        }

        // Check if the command-line options suggest that a directory of files is to be formatted
        if (line.hasOption("sd") || line.hasOption("sdp") || line.hasOption("td") || line.hasOption("tdp")) {
            // Assume user wants to process a directory of files.
            if (!line.hasOption("sd") || !line.hasOption("sdp") || !line.hasOption("td") || !line.hasOption("tdp")) {
                logger.error("Directory processing options must all be used together: -sd (--source-directory), -sdp (--source-directory-pattern), -td (--target-directory), -tdp (--target-directory-pattern)");
                usage(options);
                return;
            }
            if (line.hasOption("s") || line.hasOption("t")) {
                logger.error("Source (-s or --source) and target (-t or --target) options cannot be used together with directory processing options.");
                usage(options);
                return;
            }
            if (!line.hasOption("sfmt") || !line.hasOption("tfmt")) {
                logger.error("Source format (-sfmt or --source-format) and target format (-tfmt or --target-format) options must be provided when using directory processing options.");
                usage(options);
                return;
            }

            runOnDirectory(line); // run the serializer over a directory of files
            return;
        }

        // Check if source files exists.
        File sourceFile = null;
        InputStream sourceInputStream = null;
        if (line.hasOption("s")) {
            String sourceFilePath = line.getOptionValue("s");
            sourceFile = new File(sourceFilePath);
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
            sourceInputStream = new FileInputStream(sourceFile);
        } else {
            if (!line.hasOption("sfmt")) {
                logger.error("The source format must be specified using --source-format when reading from the standard input.");
                return;
            }
            sourceInputStream = System.in; // default to reading the standard input
        }

        // Check if target file can be written.
        OutputStream targetOutputStream = null;
        File targetFile = null;
        if (line.hasOption("t")) {
            String targetFilePath = line.getOptionValue("t");
            targetFile = new File(targetFilePath);
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

            targetOutputStream = new FileOutputStream(targetFile);
        } else {
            targetOutputStream = System.out; // default to the standard output
        }

        // Check if a base URI was provided
        try {
            if (line.hasOption("bi")) {
                baseIriString = line.getOptionValue("bi");
                baseIri = valueFactory.createIRI(baseIriString);
                if (baseIriString.endsWith("#")) {
                    logger.warn("base IRI ends in '#', which is unusual: " + baseIriString);
                }
            }
        } catch (Throwable t) {
            baseIri = null;
            baseIriString = "";
        }

        // Check if there is a valid URI pattern/replacement pair
        if (line.hasOption("ip")) {
            if(line.hasOption("ir")) {
                if (line.getOptionValue("ip").length() < 1) {
                    logger.error("An IRI pattern cannot be an empty string.  Use --help for help.");
                    return;
                }
                iriPattern = line.getOptionValue("ip");
                iriReplacement = line.getOptionValue("ir");
            } else {
                logger.error("If an IRI pattern is specified, an IRI replacement must also be specified.  Use --help for help.");
                return;
            }
        } else {
            if (line.hasOption("ir")) {
                logger.error("If an IRI replacement is specified, an IRI pattern must also be specified.  Use --help for help.");
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
        if (line.hasOption("ibi")) {
            inferBaseIri = true;
        }

        // Check if there are leading comments
        if (line.hasOption("lc")) {
            leadingComments = line.getOptionValues("lc");
        }

        // Check if there are trailing comments
        if (line.hasOption("tc")) {
            trailingComments = line.getOptionValues("tc");
        }

        // Check if there is a string data type option
        if (line.hasOption("sdt")) {
            stringDataTypeOption = SesameSortedRDFWriterFactory.StringDataTypeOptions.getByOptionValue(line.getOptionValue("sdt"));
        }

        // Check if an explicit indent string has been provided
        if (line.hasOption("i")) {
            indent = "ABC".replaceFirst("ABC", line.getOptionValue("i")); // use 'replaceFirst' to get cheap support for escaped characters like tabs
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
        if ((sourceFile == null) && (sourceFormat == SesameSortedRDFWriterFactory.SourceFormats.auto)) {
            logger.error("The source format (--source-format or -sfmt) cannot be 'auto' when reading fromn the standard input.");
            return;
        }
        RDFFormat sesameSourceFormat = null;
        if (SesameSortedRDFWriterFactory.SourceFormats.auto.equals(sourceFormat)) {
            sesameSourceFormat = Rio.getParserFormatForFileName(sourceFile.getName()).orElse(sourceFormat.getRDFFormat());
        } else {
            sesameSourceFormat = sourceFormat.getRDFFormat();
        }
        if (sesameSourceFormat == null) {
            logger.error("Unsupported or unrecognised source format enum: " + sourceFormat);
        }

        Model sourceModel = null;
        try {
            sourceModel = Rio.parse(sourceInputStream, baseIriString, sesameSourceFormat);
        } catch (Throwable t) {
            logger.error(SesameRdfFormatter.class.getSimpleName() + ": stopped by unexpected exception:");
            logger.error("Unable to parse input file: " + sourceFile.getAbsolutePath());
            logger.error(t.getClass().getSimpleName() + ": " + t.getMessage());
            StringWriter stackTraceWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTraceWriter));
            logger.error(stackTraceWriter.toString());
            usage(options);
            System.exit(1);
        }

        // Do any URI replacements
        if ((iriPattern != null) && (iriReplacement != null)) {
            Model replacedModel = new TreeModel();
            for (Statement st : sourceModel) {
                Resource replacedSubject = st.getSubject();
                if (replacedSubject instanceof IRI) {
                    replacedSubject = valueFactory.createIRI(replacedSubject.stringValue().replaceFirst(iriPattern, iriReplacement));
                }

                IRI replacedPredicate = st.getPredicate();
                replacedPredicate = valueFactory.createIRI(replacedPredicate.stringValue().replaceFirst(iriPattern, iriReplacement));

                Value replacedObject = st.getObject();
                if (replacedObject instanceof IRI) {
                    replacedObject = valueFactory.createIRI(replacedObject.stringValue().replaceFirst(iriPattern, iriReplacement));
                }

                Statement replacedStatement = valueFactory.createStatement(replacedSubject, replacedPredicate, replacedObject);
                replacedModel.add(replacedStatement);
            }
            // Do IRI replacements in namespaces as well.
            Set<Namespace> namespaces = sourceModel.getNamespaces();
            for (Namespace nmsp : namespaces) {
                replacedModel.setNamespace(nmsp.getPrefix(), nmsp.getName().replaceFirst(iriPattern, iriReplacement));
            }
            sourceModel = replacedModel;
            // This is also the right time to do IRI replacement in the base URI, if appropriate
            if (baseIri != null) {
                baseIriString = baseIriString.replaceFirst(iriPattern, iriReplacement);
                baseIri = valueFactory.createIRI(baseIriString);
            }
        }

        // Infer the base URI, if requested
        if (inferBaseIri) {
            LinkedList<IRI> owlOntologyIris = new LinkedList<IRI>();
            for (Statement st : sourceModel) {
                if ((SesameSortedRDFWriter.rdfType.equals(st.getPredicate())) && (SesameSortedRDFWriter.owlOntology.equals(st.getObject())) && (st.getSubject() instanceof IRI)) {
                    owlOntologyIris.add((IRI)st.getSubject());
                }
            }
            if (owlOntologyIris.size() >= 1) {
                Comparator<IRI> iriComparator = new Comparator<IRI>() {
                    @Override
                    public int compare(IRI iri1, IRI iri2) {
                        return iri1.toString().compareTo(iri2.toString());
                    }
                };
                owlOntologyIris.sort(iriComparator);
                inferredBaseIri = owlOntologyIris.getFirst();
            }
        }

        // Write sorted RDF file.
        SesameSortedRDFWriterFactory.TargetFormats targetFormat = null;
        if (line.hasOption("tfmt")) {
            targetFormat = SesameSortedRDFWriterFactory.TargetFormats.getByOptionValue(line.getOptionValue("tfmt"));
        } else {
            targetFormat = SesameSortedRDFWriterFactory.TargetFormats.turtle; // default to Turtle
        }
        if (targetFormat == null) {
            logger.error("Unsupported or unrecognised target format: " + line.getOptionValue("tfmt"));
            return;
        }
        SesameSortedRDFWriter.ShortIriPreferences shortUriPref = null;
        if (line.hasOption("sip")) {
            shortUriPref = SesameSortedRDFWriter.ShortIriPreferences.getByOptionValue(line.getOptionValue("sip"));
        } else {
            shortUriPref = SesameSortedRDFWriter.ShortIriPreferences.prefix;
        }
        if (shortUriPref == null) {
            logger.error("Unsupported or unrecognised short IRI preference: " + line.getOptionValue("sup"));
            return;
        }

        Writer targetWriter = new OutputStreamWriter(targetOutputStream, "UTF-8");
        SesameSortedRDFWriterFactory factory = new SesameSortedRDFWriterFactory(targetFormat);
        Map<String, Object> writerOptions = new HashMap<String, Object>();
        if (baseIri != null) {
            writerOptions.put("baseIri", baseIri);
        } else if (inferBaseIri && (inferredBaseIri != null)) {
            writerOptions.put("baseIri", inferredBaseIri);
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

    public static void runOnDirectory(@Nonnull CommandLine line) throws Exception {
        // Construct list of common arguments passed on to every invocation of 'run'
        ArrayList<String> commonArgsList = new ArrayList<>();
        final List<String> noPassArgs = Arrays.asList(new String[] {
                "sd", "sdp", "td", "tdp"
        });
        for (Option opt : line.getOptions()) {
            if (noPassArgs.contains(opt.getOpt())) { continue; }
            commonArgsList.add(String.format("-%s", opt.getOpt()));
            if (opt.hasArg()) {
                commonArgsList.add(opt.getValue());
            }
        }

        // Check the input & output directories
        final File sourceDir = new File(line.getOptionValue("sd"));
        if (!sourceDir.exists()) {
            logger.error("Source directory does not exist: " + sourceDir.getAbsolutePath());
            return;
        }
        if (!sourceDir.canRead()) {
            logger.error("Source directory is not readable: " + sourceDir.getAbsolutePath());
            return;
        }
        final Pattern sourceDirPattern = Pattern.compile(line.getOptionValue("sdp"));

        final File targetDir = new File(line.getOptionValue("td"));
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (!targetDir.exists()) {
            logger.error("Target directory could not be created: " + targetDir.getAbsolutePath());
            return;
        }
        if (!targetDir.canWrite()) {
            logger.error("Target directory is not writable: " + targetDir.getAbsolutePath());
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
            ArrayList<String> runArgs = new ArrayList<>();
            runArgs.addAll(commonArgsList);
            runArgs.add("-s");
            runArgs.add(sourceResult.getFile().getAbsolutePath());
            runArgs.add("-t");
            runArgs.add(targetFile.getAbsolutePath());
            logger.info(String.format("... formatting '%s' to '%s' ...", sourceResult.getRelativePath(), targetRelativePath));

            run(runArgs.toArray(stringArray));
        }
    }

    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( getVersion(), options );
    }

    private static String getVersion() {
        String implementationTitle = Manifests.read("Implementation-Title");
        String implementationVersion = Manifests.read("Implementation-Version");
        return String.format("%s (%s version %s)", SesameRdfFormatter.class.getSimpleName(), implementationTitle, implementationVersion);
    }

}
