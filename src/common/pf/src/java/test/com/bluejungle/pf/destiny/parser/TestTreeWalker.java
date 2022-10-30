package com.bluejungle.pf.destiny.parser;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/parser/TestTreeWalker.java#1 $
 */

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import antlr.CommonAST;
import antlr.collections.AST;

import com.bluejungle.pf.destiny.parser.PQLLexer;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.parser.ParseTreeWalker;

/**
 * Tests the tree walker. 
 */
public class TestTreeWalker extends TestCase {
    public static Test suite() {
        return new TestSuite(TestTreeWalker.class);
    }
    /**
     * This test hard-codes a PQL policy definition
     * and a sequence of events that the tree walked
     * is expected to generate for it. The test checks
     * the actual sequence against the hard-coded one
     * line-by-line, and reports the result.
     * 
     * @throws Exception when an exception is thrown from the parser.
     */
    public void testTheWalker() throws Exception {
        PQLParser parser = new PQLParser(
            new PQLLexer( new StringReader( input ) )
        );
        assertNotNull( "Unable to obtain a parser", parser );
        parser.program();
        AST parseTree = (CommonAST) parser.getAST();
        assertNotNull( "Parser produced a null parse tree", parseTree );
        ParseTreeWalker.walk( parseTree, new DefaultHandler() {
            public void startElement( String ns, String name, String qName, Attributes attr ) {
                assertTrue( "Extra event detected", i < output.length );
                String elm = "start "+name+":"+attr.getValue(0);
                assertEquals( "Line "+i+", ", output[i++], elm );
            }
            public void endElement( String ns, String name, String qName ) {
                assertTrue( "Extra event detected", i < output.length );
                String elm = "end "+name;
                assertEquals(output[i++], elm);
            }
            public void startDocument() {
                assertEquals( i, 0 );
            }
            public void endDocument() {
                assertEquals( i, output.length );
            }
            int i = 0;
        });
    }
    /**
     * Hard-coded PQL.
     */
    private static String input =
        "policy complex\n"+
        "    RULE\n"+
        "      DESCRIPTION \"This description belongs to the first rule\"\n"+
        "      FOR * ON * BY *\n"+
        "      DESCRIPTION \"another description - syntax allows for it :-)\";\n"+
        "      WHERE CURRENT_TIME != \"01/02/03 00:00:00\";\n"+
        "      WHERE CURRENT_TIME >= \"01/02/03 00:00:00\"\n"+
        "      WHERE CURRENT_TIME <= \"01/02/03 00:00:00\";\n"+
        "      WHERE CURRENT_TIME > \"01/02/03 00:00:00\";\n"+
        "      WHERE CURRENT_TIME < \"01/02/03 00:00:00\"\n"+
        "      WHERE CURRENT_TIME == \"01/02/03 00:00:00\"\n"+
        "      WHERE CURRENT_TIME = \"01/02/03 00:00:00\"\n"+
        "      DO DENY\n"+
        "      DESCRIPTION \"yet another description\";\n"+
        "      DESCRIPTION \"I can have as many descriptions as I see fit (the compiler will reject most of them :-)\";\n"+
        "      ON LOCAL DENY DO something\n"+
        "      ON ALLOW DO LOG \"Yes!\" of_course;\n"+
        "      BY DEFAULT DO CONFIRM \"Are you sure?\"\n"+
        "      BY DEFAULT DO CONFIRM \"Are you really sure?\"\n"+
        "      DESCRIPTION \"It's OK to have a description at the end...\"\n"+
        "      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; // and lots of semicolons after it\n"+
        "    RULE\n"+
        "      DESCRIPTION \"This description belongs to the second rule\";\n"+
        "      FOR * ON * BY * // an all-inclusive target\n"+
        "      DO DENY\n"+
        "    RULE\n"+
        "      DESCRIPTION \"This description belongs to the third rule\";\n"+
        "      FOR * ON * BY * // an all-inclusive target\n"+
        "      DO DENY";
    /**
     * Hard-coded sequence of events.
     */
    private static String[] output = new String[] {
        "start POLICY:policy",
        "start IDENTIFIER:complex",
        "end IDENTIFIER",
        "start RULE:RULE",
        "start DESCRIPTION:This description belongs to the first rule",
        "end DESCRIPTION",
        "start TARGET:",
        "start FROM:",
        "start RESOURCE_EXPR:",
        "start STAR:*",
        "end STAR",
        "end RESOURCE_EXPR",
        "end FROM",
        "start ACTION_EXPR:",
        "start STAR:*",
        "end STAR",
        "end ACTION_EXPR",
        "start SUBJECT_EXPR:",
        "start STAR:*",
        "end STAR",
        "end SUBJECT_EXPR",
        "end TARGET",
        "start DESCRIPTION:another description - syntax allows for it :-)",
        "end DESCRIPTION",
        "start WHERE:WHERE",
        "start RELATION_OP:!=",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:>=",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:<=",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:>",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:<",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:=",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start WHERE:WHERE",
        "start RELATION_OP:=",
        "start IDENTIFIER:CURRENT_TIME",
        "end IDENTIFIER",
        "start QUOTED_STRING:01/02/03 00:00:00",
        "end QUOTED_STRING",
        "end RELATION_OP",
        "end WHERE",
        "start EFFECT_CLAUSE:DO",
        "start EFFECT_TYPE:DENY",
        "end EFFECT_TYPE",
        "end EFFECT_CLAUSE",
        "start DESCRIPTION:yet another description",
        "end DESCRIPTION",
        "start DESCRIPTION:I can have as many descriptions as I see fit (the compiler will reject most of them :-)",
        "end DESCRIPTION",
        "start OBLIGATION_CLAUSE:",
        "start ON:ON",
        "start LOCAL:LOCAL",
        "end LOCAL",
        "start EFFECT_TYPE:DENY",
        "end EFFECT_TYPE",
        "end ON",
        "start OBLIGATION:",
        "start CUSTOM_OBLIGATION:",
        "start IDENTIFIER:something",
        "end IDENTIFIER",
        "end CUSTOM_OBLIGATION",
        "end OBLIGATION",
        "end OBLIGATION_CLAUSE",
        "start OBLIGATION_CLAUSE:",
        "start ON:ON",
        "start EFFECT_TYPE:ALLOW",
        "end EFFECT_TYPE",
        "end ON",
        "start OBLIGATION:",
        "start LOG:LOG",
        "start QUOTED_STRING:Yes!",
        "end QUOTED_STRING",
        "start IDENTIFIER:of_course",
        "end IDENTIFIER",
        "end LOG",
        "end OBLIGATION",
        "end OBLIGATION_CLAUSE",
        "start DEFAULT:DEFAULT",
        "start EFFECT_CLAUSE:DO",
        "start EFFECT_TYPE:CONFIRM",
        "end EFFECT_TYPE",
        "start QUOTED_STRING:Are you sure?",
        "end QUOTED_STRING",
        "end EFFECT_CLAUSE",
        "end DEFAULT",
        "start DEFAULT:DEFAULT",
        "start EFFECT_CLAUSE:DO",
        "start EFFECT_TYPE:CONFIRM",
        "end EFFECT_TYPE",
        "start QUOTED_STRING:Are you really sure?",
        "end QUOTED_STRING",
        "end EFFECT_CLAUSE",
        "end DEFAULT",
        "start DESCRIPTION:It's OK to have a description at the end...",
        "end DESCRIPTION",
        "end RULE",
        "start RULE:RULE",
        "start DESCRIPTION:This description belongs to the second rule",
        "end DESCRIPTION",
        "start TARGET:",
        "start FROM:",
        "start RESOURCE_EXPR:",
        "start STAR:*",
        "end STAR",
        "end RESOURCE_EXPR",
        "end FROM",
        "start ACTION_EXPR:",
        "start STAR:*",
        "end STAR",
        "end ACTION_EXPR",
        "start SUBJECT_EXPR:",
        "start STAR:*",
        "end STAR",
        "end SUBJECT_EXPR",
        "end TARGET",
        "start EFFECT_CLAUSE:DO",
        "start EFFECT_TYPE:DENY",
        "end EFFECT_TYPE",
        "end EFFECT_CLAUSE",
        "end RULE",
        "start RULE:RULE",
        "start DESCRIPTION:This description belongs to the third rule",
        "end DESCRIPTION",
        "start TARGET:",
        "start FROM:",
        "start RESOURCE_EXPR:",
        "start STAR:*",
        "end STAR",
        "end RESOURCE_EXPR",
        "end FROM",
        "start ACTION_EXPR:",
        "start STAR:*",
        "end STAR",
        "end ACTION_EXPR",
        "start SUBJECT_EXPR:",
        "start STAR:*",
        "end STAR",
        "end SUBJECT_EXPR",
        "end TARGET",
        "start EFFECT_CLAUSE:DO",
        "start EFFECT_TYPE:DENY",
        "end EFFECT_TYPE",
        "end EFFECT_CLAUSE",
        "end RULE",
        "end POLICY"
    };
}
