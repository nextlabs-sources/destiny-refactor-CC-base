package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/PolicyLanguageLexerAccuracyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.language.parser.antlr.v1.PolicyLanguageLexer;

/**
 * Accuracy tests for the Policy Language Lexer.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={PolicyLanguageLexerAccuracyTests.class})

public class PolicyLanguageLexerAccuracyTests {

    private final String input;
    private final int token;
    private PolicyLanguageLexer lexer;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            {"/*xyz*/", PolicyLanguageLexer.Comment}
        ,   {"/**/", PolicyLanguageLexer.Comment}
        ,   {"/**.*a*/", PolicyLanguageLexer.Comment}
        ,   {"//xyz\n", PolicyLanguageLexer.Comment}
        ,   {"//xyz\r", PolicyLanguageLexer.Comment}
        ,   {" \t\n\r\u000C", PolicyLanguageLexer.Whitespace}
        ,   {"abc", PolicyLanguageLexer.Identifier}
        ,   {"ab1", PolicyLanguageLexer.Identifier}
        ,   {"ab1", PolicyLanguageLexer.Identifier}
        ,   {"$", PolicyLanguageLexer.Identifier}
        ,   {"_", PolicyLanguageLexer.Identifier}
        ,   {"a", PolicyLanguageLexer.Identifier}
        ,   {"$$__", PolicyLanguageLexer.Identifier}
        ,   {"123", PolicyLanguageLexer.IntegerLiteral}
        ,   {"1e2", PolicyLanguageLexer.FPLiteral}
        ,   {"1e+2", PolicyLanguageLexer.FPLiteral}
        ,   {"1e-2", PolicyLanguageLexer.FPLiteral}
        ,   {".1", PolicyLanguageLexer.FPLiteral}
        ,   {".1e2", PolicyLanguageLexer.FPLiteral}
        ,   {".1e+2", PolicyLanguageLexer.FPLiteral}
        ,   {".1e-2", PolicyLanguageLexer.FPLiteral}
        ,   {"1.0", PolicyLanguageLexer.FPLiteral}
        ,   {"1.2", PolicyLanguageLexer.FPLiteral}
        ,   {"12.23", PolicyLanguageLexer.FPLiteral}
        ,   {"\"\"", PolicyLanguageLexer.StringLiteral}
        ,   {"\"a\"", PolicyLanguageLexer.StringLiteral}
        ,   {"\"\\t\"", PolicyLanguageLexer.StringLiteral}
        ,   {"\"\\z\"", -1}
        ,   {"/", PolicyLanguageLexer.DIV}
        ,   {"*", PolicyLanguageLexer.ASTERISK}
        ,   {"%",PolicyLanguageLexer.REM}
        ,   {"+",PolicyLanguageLexer.PLUS}
        ,   {"-",PolicyLanguageLexer.MINUS}
        ,   {"[this is an identifier.]",PolicyLanguageLexer.Identifier}
        ,   {"[this#is@an_identifier!]",PolicyLanguageLexer.Identifier}
        ,   {"[ ]",PolicyLanguageLexer.Identifier}
        ,   {"[\\]]",PolicyLanguageLexer.Identifier}
        ,   {"[abc[\\]]",PolicyLanguageLexer.Identifier}
        ,   {"`", -1}
        ,   {"^", -1}
        ,   {"7.3ex", -1}
        ,   {"7.3e", -1}
        ,   {"\"a", -1}
        });
    }

    public PolicyLanguageLexerAccuracyTests(String input, int token) {
        this.input = input;
        this.token = token;
    }

    @Before
    public void prepareLexer() {
        lexer = new PolicyLanguageLexer(
            new ANTLRStringStream(input)
        );
    }

    @Test
    public void test() {
        Token next = lexer.nextToken();
        assertNotNull(next);
        assertEquals(token, next.getType());
        int n = input.length()-1;
        if (next.getType() == PolicyLanguageLexer.StringLiteral) {
            assertEquals(unescape(input.substring(1,n)), next.getText());
        } else if (next.getType() == PolicyLanguageLexer.Comment) {
            assertTrue(input.indexOf(next.getText()) != -1);
        } else if (next.getType() == PolicyLanguageLexer.Identifier) {
            if (input.charAt(0)=='[') {
                assertEquals(unescape(input.substring(1,n)), next.getText());
            } else {
                assertEquals(input, next.getText());
            }
        } else if (next.getType() != -1) {
            assertEquals(input, next.getText());
        } else {
            assertNull(next.getText());
        }
    }

    private static String unescape(String s) {
        StringBuffer res = new StringBuffer();
        for (int i = 0 ; i != s.length() ; i++) {
            char c = s.charAt(i);
            if (c=='\\') {
                i++;
                if (i==s.length()) {
                    throw new IllegalStateException();
                }
                c = s.charAt(i);
                switch(c) {
                case 'b': c = '\b'; break;
                case 'f': c = '\f'; break;
                case 'r': c = '\r'; break;
                case 't': c = '\t'; break;
                case 'n': c = '\n'; break;
                }
                res.append(c);
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }

}
