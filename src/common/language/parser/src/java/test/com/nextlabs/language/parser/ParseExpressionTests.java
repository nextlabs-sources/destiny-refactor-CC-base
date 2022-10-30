package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/ParseExpressionTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.expression.representation.IExpression;

/**
 * Tests for parsing predicates.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={ParsePredicateTests.class})

public class ParseExpressionTests {

    private IPolicyLanguageParser parser;
        

    private final String expression;

    private final String expected;

    @Before
    public void prepare() throws PolicyLanguageException {
        parser = new PolicyParserFactory().getParser(1);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            { "null", "null" }
        ,   { "12345", "12345" }
        ,   { "1.2345", "1.2345" }
        ,   { "id 1111.a", "id 1111.a"}
        ,   { "a.b", "#a.b"}
        ,   { "(((((a).b).c).d).e).f", "#a.b.c.d.e.f" }
        ,   { "a+b-c*d+e/f%g+h-j+k*o"
            , "(#a + #b - (#c * #d) + (#e / #f % #g) + #h - #j + (#k * #o))"}
        ,   { "((((()))))", "((((()))))" }
        ,   { "(\"ab\", \"cd\")", "(\"ab\", \"cd\")" }
        ,   { "(((null,1),(null,2),(null,3)))"
            , "(((null, 1), (null, 2), (null, 3)))" }
        ,   { "(null,2,3)", "(null, 2, 3)" }
        ,   { "(1,2.5,null)", "(1.0, 2.5, null)" }
        ,   { "(\"a\",null,\"c\")", "(\"a\", null, \"c\")" }
        });
    }

    public ParseExpressionTests(String expression, String expected) {
        this.expression = expression;
        this.expected = expected;
    }

    @Test
    public void checkParseExpression()
        throws IOException, PolicyLanguageException {
        IExpression expr = parser.parseExpression(new StringReader(expression));
        assertNotNull(expr);
        assertEquals(expected, expr.toString());
    }

}
