/*
 * Created on Sep 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/test/FileBasedTestDirTree.java#1 $
 */

public class FileBasedTestDirTree {

    /** The extension of test input files. */
    private static final String TEST_EXT = ".in";

    /** The extension of approved test output files. */
    private static final String APPROVED_EXT = ".approved";

    /** The extension of approved test output files. */
    private static final String EXCEPTION_EXT = ".exception";

    /** The extension of approved test output files. */
    private static final String OUTPUT_EXT = ".out";

    /** The extension of the files with DIFFs. */
    private static final String DIFF_EXT = ".diff";

    private String basePath;
    private File outputDirectory;
    private File inputDirectory;
    private File approvedDirectory;
    private File diffDirectory;

    /**
     * Create an instance of FileBasedTestDirTree
     * 
     * @param basePath
     */
    FileBasedTestDirTree(String basePath) {

        if (basePath == null) {
            throw new NullPointerException("basePath cannot be null.");
        }

        this.basePath = basePath;
        this.inputDirectory = appendToBase("test_input");
        this.outputDirectory = appendToBase("test_output");
        this.approvedDirectory = appendToBase("test_approval");
        this.diffDirectory = appendToBase("test_differences");
    }

    void cleanAndPrepareForTest() {
        boolean directoryCreationSuccess = true;

        File outputDirectory = getOutputDirectory();
        deleteAll(outputDirectory);
        directoryCreationSuccess = outputDirectory.mkdirs();
        if (!directoryCreationSuccess) {
            throw new IllegalStateException("Failed to create output directory, " + outputDirectory);
        }

        File diffDirectory = getDiffDirectory();
        deleteAll(diffDirectory);
        directoryCreationSuccess = diffDirectory.mkdirs();
        if (!directoryCreationSuccess) {
            throw new IllegalStateException("Failed to create diff directory, " + diffDirectory);
        }

        File approvedDirectory = getApprovedDirectory();
        if (!approvedDirectory.exists()) {
            directoryCreationSuccess = approvedDirectory.mkdirs();
            if (!directoryCreationSuccess) {
                throw new IllegalStateException("Failed to create approved directory, " + approvedDirectory);
            }
        }

        File inputDirectory = getInputDirectory();
        if (!inputDirectory.exists()) {
            directoryCreationSuccess = inputDirectory.mkdirs();
            if (!directoryCreationSuccess) {
                throw new IllegalStateException("Failed to create input directory, " + inputDirectory);
            }
        }
    }

    /**
     * Retrieve the approvedDirectory.
     * 
     * @return the approvedDirectory.
     */
    File getApprovedDirectory() {
        return this.approvedDirectory;
    }

    /**
     * Retrieve the basePath.
     * 
     * @return the basePath.
     */
    String getBasePath() {
        return this.basePath;
    }

    /**
     * Retrieve the diffDirectory.
     * 
     * @return the diffDirectory.
     */
    File getDiffDirectory() {
        return this.diffDirectory;
    }

    /**
     * Retrieve the inputDirectory.
     * 
     * @return the inputDirectory.
     */
    File getInputDirectory() {
        return this.inputDirectory;
    }

    /**
     * Retrieve the outputDirectory.
     * 
     * @return the outputDirectory.
     */
    File getOutputDirectory() {
        return this.outputDirectory;
    }

    OutputStream createOutputFile(String fileName) throws FileNotFoundException {
        if (fileName == null) {
            throw new NullPointerException("fileName cannot be null.");
        }

        File outputFile = new File(getOutputDirectory(), fileName + OUTPUT_EXT);

        return new FileOutputStream(outputFile);
    }

    /**
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    OutputStream createApprovedFile(String fileName) throws FileNotFoundException {
        if (fileName == null) {
            throw new NullPointerException("fileName cannot be null.");
        }

        File approvedFile = new File(getApprovedDirectory(), fileName + APPROVED_EXT);

        return new FileOutputStream(approvedFile);
    }

    /**
     * @param name
     * @throws IOException
     * @throws FileNotFoundException
     */
    File runComparison(String name) throws FileNotFoundException, IOException {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        File outputFile = new File(getOutputDirectory(), name + OUTPUT_EXT);
        File approvedFile = new File(getApprovedDirectory(), name + APPROVED_EXT);
        File diffFile = new File(getDiffDirectory(), name + DIFF_EXT);

        String[] lhs = readStream(new FileInputStream(approvedFile));
        String[] rhs = readStream(new FileInputStream(outputFile));

        DiffFinder finder = new DiffFinder(lhs, rhs);
        // Run the diffs, and write the results into a file.
        DiffFinder.ChangeRecord diff = finder.diffForward();

        if (diff != null) {
            // We have diffs - write them out to the diff file,
            // and report a test failure.
            // First, try creating a directory for the diff file
            diffFile.getParentFile().mkdirs();
            // Then, open a file and make a print stream out of it:
            PrintStream ps = new PrintStream(new FileOutputStream(diffFile));

            try {
                // Go through the diffs and report each one
                while (diff != null) {
                    // Each diff record says how many lines are deleted
                    // on the LHS, how many lines are inserted from the RHS,
                    // what are the positions of the insertion and the deletion.
                    if (diff.deleted != 0) {
                        ps.println("LINES DELETED FROM THE APPROVAL: " + diff.deleted);
                        for (int i = 0; i != diff.deleted; i++) {
                            ps.println(lhs[diff.lineLHS + i]);
                        }
                    }
                    if (diff.inserted != 0) {
                        ps.println("LINES INSERTED INTO THE OUTPUT: " + diff.inserted);
                        for (int i = 0; i != diff.inserted; i++) {
                            ps.println(rhs[diff.lineRHS + i]);
                        }
                    }
                    // Diffs are a linked list - go to the next element
                    diff = diff.next;
                    if (diff != null) {
                        // We are not done - print a separator line
                        ps.println("##############################");
                    }
                }
            } finally {
                ps.close();
            }
        }

        return diffFile;
    }

    /**
     * Makes a File for the base path and a name.
     * 
     * @param name -
     *            the name of the file (appended to the basePath).
     * @return A File for the base path and a name.
     */
    private File appendToBase(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        return new File(getBasePath(), name);
    }

    /**
     * Deletes the file or the directory represented by the path. NOTE: This
     * method also deletes non-empty directories.
     * 
     * @param path
     *            the file or the directory to be deleted.
     */
    private static void deleteAll(File path) {
        if (path == null) {
            throw new NullPointerException("path cannot be null.");
        }

        if (path.isDirectory()) {
            // Directories must be empty before they are deleted
            File[] children = path.listFiles();
            for (int i = 0; i != children.length; i++) {
                deleteAll(children[i]);
            }
        }
        path.delete();
    }

    /**
     * Reads the input stream and parses it into an array of Strings (one string
     * per line of the input stream).
     * 
     * @param in
     *            The input stream
     * @return an array of Strings (one per line in the input stream).
     * @throws IOException
     *             when an I/O exception is thrown from an underlying stream.
     */
    private static String[] readStream(InputStream in) throws IOException {
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(in)));
        st.resetSyntax();
        st.wordChars('\0', '\255');
        st.whitespaceChars('\n', '\n');
        st.whitespaceChars('\r', '\r');
        st.eolIsSignificant(false);
        List out = new LinkedList();
        try {
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                out.add(st.sval);
            }
            return (String[]) out.toArray(new String[out.size()]);
        } finally {
            in.close();
        }
    }
}
