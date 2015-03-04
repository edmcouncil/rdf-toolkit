package org.edmcouncil.rdf_serializer;

import org.apache.commons.cli.*;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

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
                "sfmt", "source-format", true, "source (input) RDF format; one of: " + SesameSortedTurtleWriter.SourceFormats.summarise()
        );
        options.addOption(
                "t", "target", true, "target (output) RDF file"
        );
        options.addOption(
                "tfmt", "target-format", true, "source (input) RDF format: one of: " + SesameSortedTurtleWriter.TargetFormats.summarise()
        );
        options.addOption(
                "h", "help", false, "print out details of the command-line arguments for the program"
        );
        options.addOption(
                "bu", "base-uri", true, "set URI to use as base URI"
        );
        options.addOption(
                "sup", "short-uri-priority", true, "set what takes priority when shortening URIs: " + SesameSortedTurtleWriter.ShortUriPreferences.summarise()
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
            logger.error(t.getStackTrace().toString());
            usage(options);
        }

    }

    /** Main method, but throws exceptions for use from inside other Java code. */
    public static void run(String[] args) throws Exception {
        final String indent = "\t\t";

        URI baseUri = null;
        String baseUriString = "";

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
                if (!(baseUriString.endsWith("#") || baseUriString.endsWith("/"))) {
                    logger.warn("base URI does not end in '#' nor '/': " + baseUriString);
                }
            }
        } catch (Throwable t) {
            baseUri = null;
            baseUriString = "";
        }

        // Load RDF file.
        SesameSortedTurtleWriter.SourceFormats sourceFormat = null;
        if (line.hasOption("sfmt")) {
            sourceFormat = SesameSortedTurtleWriter.SourceFormats.getByOptionValue(line.getOptionValue("sfmt"));
        } else {
            sourceFormat = SesameSortedTurtleWriter.SourceFormats.auto;
        }
        if (sourceFormat == null) {
            logger.error("Unsupported or unrecognised source format: " + line.getOptionValue("sfmt"));
            return;
        }
        RDFFormat sesameSourceFormat = null;
        if (SesameSortedTurtleWriter.SourceFormats.auto.equals(sourceFormat)) {
            sesameSourceFormat = Rio.getParserFormatForFileName(sourceFilePath, RDFFormat.TURTLE);
        } else {
            sesameSourceFormat = SesameSortedTurtleWriter.SourceFormats.getSesameFormat(sourceFormat);
        }
        if (sesameSourceFormat == null) {
            logger.error("Unsupported or unrecognised source format enum: " + sourceFormat);
        }
        Model sourceModel = Rio.parse(new FileInputStream(sourceFile), baseUriString, sesameSourceFormat);

        // Write sorted RDF file.
        SesameSortedTurtleWriter.TargetFormats targetFormat = null;
        if (line.hasOption("tfmt")) {
            targetFormat = SesameSortedTurtleWriter.TargetFormats.getByOptionValue(line.getOptionValue("tfmt"));
        } else {
            targetFormat = SesameSortedTurtleWriter.TargetFormats.turtle;
        }
        if (targetFormat == null) {
            logger.error("Unsupported or unrecognised target format: " + line.getOptionValue("tfmt"));
            return;
        }
        SesameSortedTurtleWriter.ShortUriPreferences shortUriPref = null;
        if (line.hasOption("sup")) {
            shortUriPref = SesameSortedTurtleWriter.ShortUriPreferences.getByOptionValue(line.getOptionValue("sup"));
        } else {
            shortUriPref = SesameSortedTurtleWriter.ShortUriPreferences.prefix;
        }
        if (shortUriPref == null) {
            logger.error("Unsupported or unrecognised short URI preference: " + line.getOptionValue("sup"));
            return;
        }
        // Note: only 'turtle' is supported as an output format, at present
        Writer targetWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
        SesameSortedTurtleWriterFactory factory = new SesameSortedTurtleWriterFactory();
        RDFWriter turtleWriter = factory.getWriter(targetWriter, baseUri, indent, shortUriPref);
        Rio.write(sourceModel, turtleWriter);
        targetWriter.flush();
        targetWriter.close();
    }

    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "SesameRdfFormatter", options );
    }

}
