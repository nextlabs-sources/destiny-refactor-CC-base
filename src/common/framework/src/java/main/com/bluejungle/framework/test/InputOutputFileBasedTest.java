package com.bluejungle.framework.test;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/test/InputOutputFileBasedTest.java#1 $
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Provides a base for building a dynamic unit tester
 * based on files describing the input and the expected outcome.
 * 
 * This class expects the following directory structure:
 * <base_dir>/test_files/<pkg>/test_input/ - Inputs,
 * <base_dir>/test_files/<pkg>/test_approval/ - Approvals.
 * where <base_dir> is the path to the location of your test_files
 * root (typically, it is in the same place as the src directory),
 * and <pkg> is the the name of the Java package being tested
 * with '.' characters replaced with '/' characters.
 * 
 * When the test directory has subdirectories, a test suite is
 * created for each such subdirectory.
 * 
 * Input files must have a .in extension. Approved outputs must
 * have an .approved extension for "positive" tests (i.e. when
 * the expected outcome is an output file), and .exception
 * extension for "negative" tests, (i.e. when the expected outcome
 * is an Exception throwed from the testing method).
 * 
 * When the test suite is run, the following directories are created: 
 * <base_dir>/test_files/<pkg>/test_output - Outputs,
 * <base_dir>/test_files/<pkg>/test_differences - Diffs.
 *
 * To use this class you have to:
 * - Define a class that extends TestCase and implements FileBasedTest.Tester,
 * - Provide a static suite() method that calls FileBasedTest.suite(Tester)
 *   passing an instance of your class as the argument, and
 * - Implement the test(in,out) method that, given an input stream,
 *   produces the output stream for your test. 
 *  
 * @author sergey
 */
public class InputOutputFileBasedTest {
    private static FileBasedTestDirTree testFileDirTree;
    
    /**
     * Users of the FileBasedTest implement this interface,
     * and pass its instance to the suite(Tester) method. 
     */
    public interface Tester {
        /**
         * Provides the definition of the test by converting the
         * input to the corresponding output or throwing an exception.
         * @param input the input stream. Clients are not responsible
         * for closing this stream.
         * @param out the output stream. Clients are not responsible
         * for closing this stream. 
         * @throws Exception when the class being tested throws an exception.
         */
        public void test( InputStream input, OutputStream out ) throws Exception;
    }
    /**
     * Sets up the base path, and creates a suite of tests
     * based on the content of the test_files directory.
     * @return A suite of tests based on the content
     * of the test_files directory.
     */
    public static TestSuite buildSuite( Tester tester ) {
        if ( tester == null ) {
            throw new NullPointerException("tester");
        }
        // The name of the suite is the name of the tester's package
        String suiteName = tester.getClass().getPackage().getName();
        // Obtain the base path
        String basePath = buildBaseFilePath(suiteName);
        
        testFileDirTree = new FileBasedTestDirTree(basePath);
        testFileDirTree.cleanAndPrepareForTest();
        
        // Finally, prepare the suite by reading the directory
        // of the input files and adding one test per input:
        File topDir = testFileDirTree.getInputDirectory();
        if ( !topDir.isDirectory() ) {
            // The top directory does not exist --
            // the suite is empty:
            return new TestSuite( suiteName+" [Empty]" );
        }
        return buildSuites( topDir, testFileDirTree.getOutputDirectory(), testFileDirTree.getDiffDirectory(), testFileDirTree.getApprovedDirectory(), suiteName, tester );
    }

    /**
     * Recursively builds the test suite by traversing
     * the directory structure looking for the test input files.   
     * @param inDir The directory where to start looking.
     * @param outDir The directory where to put output files.
     * @param diffDir The directory where to put diffs.
     * @param tester The tester to which to delegate the testing.
     * @return The suite that corresponds to the given directory structure.
     */
    private static TestSuite buildSuites(
        final File inDir,
        final File outDir,
        final File diffDir,
        final File approvedDir,
        final String suiteName,
        final Tester tester
    ) {
        assert inDir.isDirectory(); // We check this in all code paths
        // Build the suite 
        TestSuite res = new TestSuite( suiteName );
        final File[] inFiles = inDir.listFiles();
        for ( int i = 0 ; i != inFiles.length ; i++ ) {
            final File inFile = inFiles[i];
            final String testFileName = inFile.getName();
            if ( inFile.isFile() && testFileName.endsWith(TEST_EXT)) {
                // This is a test file - add a test for it:
                int nameLen = testFileName.length()-TEST_EXT.length();
                final String testName = testFileName.substring( 0, nameLen );
                res.addTest( new TestCase( testName ) {
                    public void runTest() throws Exception {
                        // Prepare the arguments and call the runTest method
                        testAndCompare(
                            inFile,
                            new File( outDir, testName + OUTPUT_EXT ),
                            new File( diffDir, testName + DIFF_EXT ),
                            new File( approvedDir, testName + APPROVED_EXT ),
                            new File( outDir, testName + EXCEPTION_EXT ),
                            tester
                        );
                    }
                });
            } else if ( inFiles[i].isDirectory() ) {
                // This is a directory - traverse it to build a suite:
                TestSuite toAdd = buildSuites(
                    inFiles[i],
                    new File( outDir, inFiles[i].getName() ),
                    new File( diffDir, inFiles[i].getName() ),
                    new File( approvedDir, inFiles[i].getName() ),
                    inFiles[i].getName(),
                    tester
                );
                if ( toAdd.testCount() != 0 ) {
                    res.addTest( toAdd );
                }
            }
        }
        return res;
    }
    /**
     * This is the main method for testing the parser.
     * It uses the name of the test available through the getName()
     * method to construct its input, output, approval, and the diff
     * file namess. Then the method performs these steps:
     * - Opens the input file and parses its content,
     * - Dumps the output AST of the parser to the output file,
     * - Obtains the approval output for the test,
     * - Compares the approved output and the current output. 
     */
    private static void testAndCompare(
        final File inFile,
        final File outFile,
        final File diffFile,
        final File approvedFile,
        final File exceptionFile,
        final Tester tester
    ) throws IOException {
        if ( approvedFile.exists() && exceptionFile.exists() ) {
            TestCase.fail("Both an approval and an exception file exist for the test.");
        }
        InputStream in = new FileInputStream( inFile );
        outFile.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream( outFile );
        try {
            tester.test( in, out );
        } catch ( Exception ex ) {
            PrintStream ps = new PrintStream( out );
            ps.println("Test run resulted in an exception:");
            ps.println("Class:");
            ps.println( ex.getClass().getName() );
            ps.println("Message:");
            ps.println( ex.getMessage() );
            ps.println("Stack:");
            ex.printStackTrace( ps );
            ps.close();
            if ( !exceptionFile.exists() ) {
                TestCase.fail( "Unexpected exception (see test output)" );
            }
            String[] exData = readStream( new FileInputStream( exceptionFile ) );
            assert exData != null; // ...it may be empty, but it must not be null 
            if ( exData.length == 0 ) {
                TestCase.fail("Exception file is empty.");
            } else if ( !ex.getClass().getName().equals( exData[0] ) ) {
                TestCase.fail("Exception "+ ex.getClass() +
                    " does not match the expected exception "+ exData[0]
                );
            }
            // We are done with the exception processing branch
            return;
        } finally {
            in.close();
            out.close();
        }
        // If we are here, the run has completed successfully
        // Check if we have an approval file
        if ( !approvedFile.exists() ) {
            TestCase.fail("Approved file does not exist.");
        }
        String[] lhs = readStream( new FileInputStream( approvedFile ) );
        String[] rhs = readStream( new FileInputStream( outFile ) );
        DiffFinder finder = new DiffFinder( lhs, rhs );
        // Run the diffs, and write the results into a file.
        DiffFinder.ChangeRecord diff = finder.diffForward();

        if ( diff != null ) {
            // We have diffs - write them out to the diff file,
            // and report a test failure.
            // First, try creating a directory for the diff file
            diffFile.getParentFile().mkdirs();
            // Then, open a file and make a print stream out of it: 
            PrintStream ps = new PrintStream( 
                new FileOutputStream( diffFile )
            );

            try {
                // Go through the diffs and report each one
                while ( diff != null ) {
                    // Each diff record says how many lines are deleted
                    // on the LHS, how many lines are inserted from the RHS,
                    // what are the positions of the insertion and the deletion.
                    if ( diff.deleted != 0 ) {
                        ps.println( "LINES DELETED FROM THE APPROVAL: "+diff.deleted );
                        for ( int i = 0 ; i != diff.deleted ; i++ ) {
                            ps.println( lhs[diff.lineLHS+i] );
                        }
                    }
                    if ( diff.inserted != 0 ) {
                        ps.println( "LINES INSERTED INTO THE OUTPUT: "+diff.inserted );
                        for ( int i = 0 ; i != diff.inserted ; i++ ) {
                            ps.println( rhs[diff.lineRHS+i] );
                        }
                    }
                    // Diffs are a linked list - go to the next element
                    diff = diff.next;
                    if ( diff != null ) {
                        // We are not done - print a separator line
                        ps.println("##############################");
                    }
                }
            } finally {
                ps.close();
                StringBuffer errorMessage = new StringBuffer("File based test failed.  Differences written to, ");
                errorMessage.append(diffFile.getCanonicalPath());
                errorMessage.append(".  The first 100 lines are shown below:\n");
                BufferedReader fileReader = new BufferedReader(new FileReader(diffFile));
                String lineRead = null;
                for (int i = 0; ((i < 100) && ((lineRead = fileReader.readLine()) != null)); i++) {
                    errorMessage.append(lineRead);
                }
                TestCase.fail(errorMessage.toString());
            }
        }
    }

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

   /**
     * Reads the input stream and parses it into an array of Strings
     * (one string per line of the input stream). 
     * @param in The input stream
     * @return an array of Strings (one per line in the input stream).
     * @throws IOException when an I/O exception is thrown from an
     * underlying stream.
     */
    private static String[] readStream( InputStream in ) throws IOException {
        StreamTokenizer st = new StreamTokenizer(
            new BufferedReader(
                new InputStreamReader( in )
            )
        );
        st.resetSyntax();
        st.wordChars( '\0', '\255' );
        st.whitespaceChars( '\n', '\n' );
        st.whitespaceChars( '\r', '\r' );
        st.eolIsSignificant(false);
        List out = new LinkedList();
        while ( st.nextToken() != StreamTokenizer.TT_EOF ) {
            out.add( st.sval );
        }
        return (String[])out.toArray( new String[out.size()] );
    }
    
    /**
     *  Builds the base file path for all test file directories.   
     * @param testSuiteName TODO
     * @return The built base file path
     */
    private static String buildBaseFilePath(String testSuiteName) {
        String pathToReturn = System.getProperty("src.root.dir");
        pathToReturn += File.separator + "test_files" + File.separator;
        // A bit of a hack.  Doesn't really belong here
        String subPath = testSuiteName.replaceAll( "[.]", "\\"+File.separator );
        pathToReturn += subPath;
        
        return pathToReturn;
    }
}
