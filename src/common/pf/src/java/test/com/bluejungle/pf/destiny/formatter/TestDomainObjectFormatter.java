package com.bluejungle.pf.destiny.formatter;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/formatter/TestDomainObjectFormatter.java#1 $
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import antlr.CommonAST;
import antlr.collections.AST;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLLexer;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.framework.test.InputOutputFileBasedTest;

/**
 * Tests the domain object formatter.
 */
public class TestDomainObjectFormatter extends TestCase implements InputOutputFileBasedTest.Tester {

    private DomainObjectFormatter f;
    
    public static Test suite() {
        return InputOutputFileBasedTest.buildSuite( new TestDomainObjectFormatter() );
    }

    public void test( InputStream in, OutputStream out ) throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(in);
        f = new DomainObjectFormatter();
        TestVisitor v = new TestVisitor();
        dob.processInternalPQL(v);
        
        PrintStream ps = new PrintStream( out );
        try {
            String pql = f.getPQL();
            ps.print( pql );
            ps.println();
            ps.println();
            ps.println( "================ Re-parsed PQL =================" );
            ps.println();
            PQLParser parser = new PQLParser(
                new PQLLexer(
                    new StringReader(pql)
                )
            );
            assertNotNull( "Unable to obtain a parser", parser );
            parser.program();
            AST parseTree = (CommonAST) parser.getAST();
            assertNotNull( "Parser produced a null parse tree", parseTree );
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
    
    private class TestVisitor implements IPQLVisitor {

        /**
         * @see IPQLVisitor#visitPolicy(DomainObjectDescriptor, IDPolicy)
         */
        public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
            assertNotNull("unable to parse policy", policy);
            f.formatPolicyDef(descr, policy);
        }

        /**
         * @see IPQLVisitor#visitResource(DomainObjectDescriptor, IDResourceSpec)
         */
        public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
            assertNotNull("unable to parse component spec", pred);
            f.formatDef(descr, pred);
        }

        /**
         * @see IPQLVisitor#visitLocation(DomainObjectDescriptor, Location)
         */
        public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
            // TODO: location formatting
        }

        /**
         * @see IPQLVisitor#visitFolder(DomainObjectDescriptor)
         */
        public void visitFolder(DomainObjectDescriptor descriptor) {
            f.formatFolder( descriptor );
        }
        /**
         * @see IPQLVisitor#visitAccessPolicy(DomainObjectDescriptor, IAccessPolicy)
         */
        public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
            f.formatAccessPolicy(accessPolicy);
        }
    }
}
