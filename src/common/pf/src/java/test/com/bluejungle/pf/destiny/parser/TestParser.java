package com.bluejungle.pf.destiny.parser;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/parser/TestParser.java#1 $
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import antlr.CommonAST;
import antlr.collections.AST;

import com.bluejungle.pf.destiny.parser.PQLLexer;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.framework.test.InputOutputFileBasedTest;

/**
 * Tests the ANTLR-generated parser.
 * This tester is based on the FileBasedTest.
 * See javadocs for more details.
 * 
 * @author sergey
 */
public class TestParser extends TestCase implements InputOutputFileBasedTest.Tester {
    /**
     * Uses the FileBasedTest to build the test suite for this component.
     * @return A dynamically generated test suite for this component. 
     */
    public static TestSuite suite() {
        return InputOutputFileBasedTest.buildSuite( new TestParser() );        
    }
    /**
     * This is the main method for testing the parser.
     * It opens the input file and parses its content, and then
     * dumps the output AST of the parser to the output stream.
     */
    public void test( InputStream in, OutputStream out ) throws Exception {
        PQLParser parser = new PQLParser( new PQLLexer( in ) );
        assertNotNull( "Unable to obtain a parser", parser );
        parser.program();
        AST parseTree = (CommonAST) parser.getAST();
        
        assertNotNull( "Parser produced a null parse tree", parseTree );
        PrintStream ps = new PrintStream( out );
        try {
            printNode( ps, parseTree, "", true );
        } finally {
            ps.close();
        }
    }
    /**
     * Dumps the AST to the specified output stream. 
     * @param out the output to which the tree needs to be written.
     * @param tree the node to be shown.
     * @param indent the indent for the current level of the AST.
     * @param showSiblings indicates whether the node needs to be shown with
     * or without all of its siblings.
     */
    private static void printNode( PrintStream out, AST tree, String indent, boolean showSiblings ) {
        if ( tree == null ) {
            // null trees are acceptable - they are considered empty
            return;
        }
        // Show the current node
        String tokTypeStr = PQLParser._tokenNames[tree.getType()];
        out.println( indent+tokTypeStr+": '"+tree.getText()+"'" );
        // Show all of its children (note the showSiblings flag set to true)
        printNode( out, tree.getFirstChild(), indent+"    ", true );
        while ( showSiblings && tree != null ) {
            tree = tree.getNextSibling();
            printNode( out, tree, indent, false );
        }
    }
}
