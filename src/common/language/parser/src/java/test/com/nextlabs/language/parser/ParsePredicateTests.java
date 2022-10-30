package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/ParsePredicateTests.java#1 $
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

public class ParsePredicateTests {

    private IPolicyLanguageParser parser;
        

    private final String predicate;

    private final String expected;

    @Before
    public void prepare() throws PolicyLanguageException {
        parser = new PolicyParserFactory().getParser(1);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            { "id 1111.a > 1"
        ,     "id 1111.a > 1"}
        ,   { "a.b>b or id 3.c<d or id 3<d or id 4.a.b.c.d > x.y.z.w"
            , "(#a.b > #b or id 3.c < #d or id 3 < #d or id 4.a.b.c.d >"+
              " #x.y.z.w)" }
        ,   { "(x.y).z < null or (id -5.[a a]).bbb.ccc.ddd == 9879.567"
            , "(#x.y.z < null or id -5.a a.bbb.ccc.ddd == 9879.567)" }
        ,   { "(((((a).b).c).d).e).f <= 0"
            , "#a.b.c.d.e.f <= 0" }
        ,   { "a+b-c*d+e/f%g+h-j+k*o < p-q+r-s+t*u*v*w-x/y%z"
            , "(#a + #b - (#c * #d) + (#e / #f % #g) + #h - #j + (#k * #o)) <"+
              " (#p - #q + #r - #s + (#t * #u * #v * #w) - (#x / #y % #z))" }
        ,   { "-5 > +3 and +1<+2 or -1<=-2"
            , "((-5 > 3 and 1 < 2) or -1 <= -2)" }
        ,   { "a:b:c(a,b,c) < a/b/c"
            , "#a:b:c(#a, #b, #c) < (#a / #b / #c)" }
        ,   { "id 123(a=a, b=b, c=null) == null"
            , "id 123(a=#a, b=#b, c=null) == null" }
        ,   { "\"abc\" == null", "\"abc\" == null" }
        ,   { "abc == null", "#abc == null" }
        ,   { "f(true, false) == null", "#f(true, false) == null" }
        ,   { "(1,2,3) == null", "(1, 2, 3) == null" }
        ,   { "(1,2.5,3) == null", "(1.0, 2.5, 3.0) == null" }
        ,   { "(\"a\",\"b\",\"c\") == null", "(\"a\", \"b\", \"c\") == null" }
        ,   { "() == null", "() == null" }
        ,   { "((((())))) == null", "((((())))) == null" }
        ,   { "(\"ab\", \"cd\") == null", "(\"ab\", \"cd\") == null" }
        ,   { "(((null,1),(null,2),(null,3))) == null"
            , "(((null, 1), (null, 2), (null, 3))) == null" }
        ,   { "(null,2,3) == null", "(null, 2, 3) == null" }
        ,   { "(1,2.5,null) == null", "(1.0, 2.5, null) == null" }
        ,   { "(\"a\",null,\"c\") == null", "(\"a\", null, \"c\") == null" }
        ,   { "(a in () or b in ())", "(#a in () or #b in ())" }
        ,   { "(a in ((())) or b in ())", "(#a in ((())) or #b in ())" }
        ,   { "(a in ((()))+() or b in ())"
            , "(#a in (((())) + ()) or #b in ())" }
        ,   { "(a in (()+()) or b in ())", "(#a in (() + ()) or #b in ())" }
        ,   { "((((()+())+())+())+())==null"
            , "((((() + ()) + ()) + ()) + ()) == null" }
        ,   { "((1),(2),(3)) == null" , "((1), (2), (3)) == null" }
        ,   { "a in (null, 1, null)" , "#a in (null, 1)" }
        ,   { "a in (null)" , "#a in (null)" }
        ,   { "a in null" , "#a in null" }
        ,   { "not not not not a==0", "not not not not #a == 0"}
        ,   { "true and not false", "(true and not false)"}
        ,   { "true and a>b or false", "((true and #a > #b) or false)"}
        ,   { "true and a>b or not not false"
            , "((true and #a > #b) or not not false)"}
        });
    }

    public ParsePredicateTests(String predicate, String expected) {
        this.predicate = predicate;
        this.expected = expected;
    }

    @Test
    public void checkParsePredicate()
        throws IOException, PolicyLanguageException {
        IExpression pred = parser.parseExpression(new StringReader(predicate));
        assertNotNull(pred);
        assertEquals(expected.toLowerCase(), pred.toString().toLowerCase());
    }

}
