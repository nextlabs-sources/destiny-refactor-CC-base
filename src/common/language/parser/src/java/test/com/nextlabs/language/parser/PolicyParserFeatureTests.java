package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/PolicyParserFeatureTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.language.representation.DefaultDefinitionVisitor;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinitionVisitor;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;

/**
 * This unit test verifies individual features of the parser.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyParserFeatureTests {

    private static final IPolicyParserFactory ppf = new PolicyParserFactory();

    private IPolicyLanguageParser p;

    private static final String COMPONENT_SRC = "component a is b = true";
    private static final String COMPONENT_TXT = "component a : #b = true";

    private static final String CONTEXT_SRC = "context a * string";
    private static final String CONTEXT_TXT = "context a {\n    * : string\n}";

    private static final String FUNCTION_SRC = "function x returns integer";
    private static final String FUNCTION_TXT = "function x returns integer";

    private static final String OBLIGATION_SRC = "obligation x(a double)";
    private static final String OBLIGATION_TXT = "obligation x(a:double)";

    private static final String POLICY_SRC = "policy a is b do deny";
    private static final String POLICY_TXT =
        "policy a is #b\nwhen true do DENY";

    private static final String POLICY_TYPE_SRC = "policy type a";
    private static final String POLICY_TYPE_TXT = "policy type a";

    private static final String POLICY_SET_SRC =
        "policy set a of t allow overrides";
    private static final String POLICY_SET_TXT =
        "policy set a of #t\nALLOW overrides";

    @Before
    public void prepare() throws PolicyLanguageException {
        p = ppf.getParser(1);
    }

    @Test
    public void keywords() {
        Set<String> keywords = p.getKeywords();
        assertNotNull(
            "Parser must return a non-null Set of keywords."
        ,   keywords
        );
        assertFalse(
            "Parser must return a non-empty Set of keywords."
        ,   keywords.isEmpty()
        );
    }

    @Test
    public void parseComponent() throws Exception {
        IPolicyComponent comp = p.parseComponentDeclaration(
            txt(COMPONENT_SRC)
        );
        assertNotNull(comp);
        assertEquals(COMPONENT_TXT, comp.toString());
    }

    @Test
    public void parseContextType() throws Exception {
        IContextType ctx = p.parseContextTypeDeclaration(txt(CONTEXT_SRC));
        assertNotNull(ctx);
        assertEquals(CONTEXT_TXT, ctx.toString());
    }

    @Test
    public void parseFunction() throws Exception {
        IFunctionType function = p.parseFunctionTypeDeclaration(
            txt(FUNCTION_SRC)
        );
        assertNotNull(function);
        assertEquals(FUNCTION_TXT, function.toString());
    }

    @Test
    public void parseObligation() throws Exception {
        IObligationType obligation = p.parseObligationTypeDeclaration(
            txt(OBLIGATION_SRC)
        );
        assertNotNull(obligation);
        assertEquals(OBLIGATION_TXT, obligation.toString());
    }

    @Test
    public void parsePolicy() throws Exception {
        IPolicy pl = p.parsePolicyDeclaration(
            txt(POLICY_SRC)
        );
        assertNotNull(pl);
        assertEquals(POLICY_TXT, pl.toString());
    }

    @Test
    public void parsePolicySet() throws Exception {
        IPolicySet ps = p.parsePolicySetDeclaration(
            txt(POLICY_SET_SRC)
        );
        assertNotNull(ps);
        assertEquals(POLICY_SET_TXT, ps.toString());
    }

    @Test
    public void parsePolicyType() throws Exception {
        IPolicyType pt = p.parsePolicyTypeDeclaration(
            txt(POLICY_TYPE_SRC)
        );
        assertNotNull(pt);
        assertEquals(POLICY_TYPE_TXT, pt.toString());
    }

    @Test
    public void parseExpression() throws Exception {
        IExpression expr = p.parseExpression(txt("a+1"));
        assertNotNull(expr);
        assertEquals("(#a + 1)", expr.toString());
    }

    @Test
    public void parseDeclarations() throws Exception {
        p.parseDeclarations(
            txt(
                COMPONENT_SRC + " "
            +   CONTEXT_SRC + " "
            +   FUNCTION_SRC + " "
            +   OBLIGATION_SRC + " "
            +   POLICY_SRC + " "
            +   POLICY_TYPE_SRC + " "
            +   POLICY_SET_SRC
            )
        ,   new IDefinitionVisitor() {
                private int counter = 0;
                public void visitContextType(IContextType contextType) {
                    assertEquals(1, counter++);
                    assertNotNull(contextType);
                    assertEquals(CONTEXT_TXT, contextType.toString());
                }
                public void visitFunctionType(IFunctionType functionType) {
                    assertEquals(2, counter++);
                    assertNotNull(functionType);
                    assertEquals(FUNCTION_TXT, functionType.toString());
                }
                public void visitObligationType(
                    IObligationType obligationType
                ) {
                    assertEquals(3, counter++);
                    assertNotNull(obligationType);
                    assertEquals(OBLIGATION_TXT, obligationType.toString());
                }
                public void visitPolicy(IPolicy policy) {
                    assertEquals(4, counter++);
                    assertNotNull(policy);
                    assertEquals(POLICY_TXT, policy.toString());
                }
                public void visitPolicyComponent(
                    IPolicyComponent policyComponent
                ) {
                    assertEquals(0, counter++);
                    assertNotNull(policyComponent);
                    assertEquals(COMPONENT_TXT, policyComponent.toString());
                }
                public void visitPolicyType(IPolicyType policyType) {
                    assertEquals(5, counter++);
                    assertNotNull(policyType);
                    assertEquals(POLICY_TYPE_TXT, policyType.toString());
                }
                public void visitPolicySet(IPolicySet policySet) {
                    assertEquals(6, counter++);
                    assertNotNull(policySet);
                    assertEquals(POLICY_SET_TXT, policySet.toString());
                }
            }
        );
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidDeclarations() throws Exception {
        p.parseDeclarations(
            txt("policy 1")
        ,   new DefaultDefinitionVisitor()
        );
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidComponentDeclaration() throws Exception {
        p.parseComponentDeclaration(txt("component ]"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidContextTypeDeclaration() throws Exception {
        p.parseContextTypeDeclaration(txt("context type abc"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidFunction() throws Exception {
        p.parseFunctionTypeDeclaration(txt("function 1"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidObligation() throws Exception {
        p.parseObligationTypeDeclaration(txt("obligation 1"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidPolicy() throws Exception {
        p.parsePolicyDeclaration(txt("policy abc+456"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidPolicySet() throws Exception {
        p.parsePolicySetDeclaration(txt("policy set abc+456"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidPolicyType() throws Exception {
        p.parsePolicyTypeDeclaration(txt("policy type #$$#"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidPredicate() throws Exception {
        p.parseExpression(txt("#+1"));
    }

    @Test(expected=PolicyLanguageException.class)
    public void parseInvalidExpression() throws Exception {
        p.parseExpression(txt("a 1"));
    }

    @Test(expected=NullPointerException.class)
    public void parseComponentNullSource() throws Exception {
        p.parseComponentDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parseContextTypeNullSource() throws Exception {
        p.parseContextTypeDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parseFunctionNullSource() throws Exception {
        p.parseFunctionTypeDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parseObligationNullSource() throws Exception {
        p.parseObligationTypeDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parsePolicyNullSource() throws Exception {
        p.parsePolicyDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parsePolicyTypeNullSource() throws Exception {
        p.parsePolicyTypeDeclaration(null);
    }

    @Test(expected=NullPointerException.class)
    public void parseExpressionNullSource() throws Exception {
        p.parseExpression(null);
    }

    private static Reader txt(String s) {
        return new StringReader(s);
    }

}
