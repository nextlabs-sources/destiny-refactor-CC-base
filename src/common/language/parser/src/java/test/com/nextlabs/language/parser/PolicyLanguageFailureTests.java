package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/PolicyLanguageFailureTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;
import org.junit.Before;
import org.junit.Test;

import com.nextlabs.language.parser.antlr.v1.PolicyLanguageLexer;
import com.nextlabs.language.parser.antlr.v1.PolicyLanguageParser;

/**
 * Failure tests for the policy language.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyLanguageFailureTests {

    private PolicyLanguageLexer lexer;

    @Before
    public void prepareLexer() {
        lexer = new PolicyLanguageLexer();
    }

    @Test
    public void lexerReady() {
        String grammarName = lexer.getGrammarFileName();
        assertNotNull(grammarName);
        assertTrue(grammarName.indexOf("Policy") != -1);
    }

    @Test(expected=RecognitionException.class)
    public void badDouble() throws Exception {
        lexer.setCharStream(new ANTLRStringStream("z"));
        lexer.mFPLiteral();
    }

    @Test(expected=RecognitionException.class)
    public void badInteger() throws Exception {
        lexer.setCharStream(new ANTLRStringStream("z"));
        lexer.mIntegerLiteral();
    }

    @Test(expected=RecognitionException.class)
    public void badExponent() throws Exception {
        lexer.setCharStream(new ANTLRStringStream("z"));
        lexer.mExponent();
    }

    @Test(expected=RecognitionException.class)
    public void badLetter() throws Exception {
        lexer.setCharStream(new ANTLRStringStream("1"));
        lexer.mIdentifierStart();
    }

    @Test
    public void keywordsAndIdentifiers() throws RecognitionException {
        Set<String> tokens = PolicyLanguageParser.getTokens();
        for (String token : tokens) {
            for ( int i = 1 ; i < token.length() ; i++ ) {
                String prefix = token.substring(0, i);
                if (!PolicyLanguageParser.isToken(prefix)) {
                    checkIdentifier(prefix);
                }
                checkIdentifier(prefix+"x");
            }
            checkIdentifier(token+"1");
            checkIdentifier(token+"$");
            checkIdentifier(token+"_");
            checkIdentifier(token+"Z");
            checkIdentifier(token+"a");
            assertFalse(
                "'"+token+"_' must not be a keyword."
            ,   PolicyLanguageParser.isKeyword(token+"_")
            );
        }
    }

    @Test
    public void checkCommonMethods() {
        PolicyLanguageParser p =
            new PolicyLanguageParser(new CommonTokenStream(lexer));
        assertNotNull(p);
        TreeAdaptor ta = new CommonTreeAdaptor();
        p.setTreeAdaptor(ta);
        assertSame(ta, p.getTreeAdaptor());
        assertNotNull(p.getGrammarFileName());
        assertNotNull(p.getTokenNames());
    }

    @Test
    public void checkDFAs() throws Exception {
        PolicyLanguageParser p =
            new PolicyLanguageParser(new CommonTokenStream(lexer));
        checkDFAs(PolicyLanguageLexer.class, lexer);
        checkDFAs(PolicyLanguageParser.class, p);
    }

    private void checkIdentifier(String s) {
        // Certain keywords are substrings of other keywords
        // (such as 'in' and 'insensitive'). We must skip such prefixes:
        if (PolicyLanguageParser.isKeyword(s)) {
            return;
        }
        lexer.setCharStream(new ANTLRStringStream(s));
        Token t = lexer.nextToken();
        assertEquals(
            "Not an identifier: '"+s+"'"
        ,   PolicyLanguageLexer.Identifier
        ,   t.getType()
        );
        assertEquals(s, t.getText());
    }

    public <T> void checkDFAs(Class<T> base, T baseObj) throws Exception {
        for (Class<?> inner : base.getDeclaredClasses()) {
            if (inner.getName().indexOf("DFA") != -1) {
                Constructor<?> c = inner.getDeclaredConstructor(
                    base, BaseRecognizer.class
                );
                c.setAccessible(true);
                Object o = c.newInstance(baseObj, baseObj);
                Method m = inner.getDeclaredMethod("getDescription");
                m.setAccessible(true);
                m.invoke(o, new Object[0]);
            }
        }
    }

}
