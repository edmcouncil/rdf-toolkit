package org.edmcouncil.rdf_serializer;

import org.apache.commons.cli.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    }

    /** Main method for running the RDF formatter. Run with "--help" option for help. */
    public static void main(String[] args) {
        // Main program block.
        try {
            run(args);
        } catch (Throwable t) {
            logger.error(SesameRdfFormatter.class.getSimpleName() + ": stopped by unexpected exception:");
            logger.error(t.getClass().getSimpleName() + ": " + t.getMessage());
            StringWriter stackTraceWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTraceWriter));
            logger.error(stackTraceWriter.toString());
            usage(options);
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
        targetFileDir.mkdirs();
        if (!targetFileDir.exists()) {
            logger.error("Target file directory could not be created: " + targetFileDir.getAbsolutePath());
            return;
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
        if (baseUri != null) { writerOptions.put("baseUri", baseUri); }
        if (indent != null) { writerOptions.put("indent", indent); }
        if (shortUriPref != null) { writerOptions.put("shortUriPref", shortUriPref); }
        writerOptions.put("useDtdSubset", useDtdSubset);
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
