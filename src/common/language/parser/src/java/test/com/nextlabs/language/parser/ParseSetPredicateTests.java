package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/ParseSetPredicateTests.java#1 $
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

public class ParseSetPredicateTests {

    private IPolicyLanguageParser parser;
        

    private final String setPredicate;

    private final String expected;

    @Before
    public void prepare() throws PolicyLanguageException {
        parser = new PolicyParserFactory().getParser(1);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            { "any id 1111.a > 1", "any id 1111.a > 1"}
        ,   { "all a.b>b or id 3.c<d or id 3<d or id 4.a.b.c.d > x.y.z.w"
            , "all (#a.b > #b or id 3.c < #d or id 3 < #d or id 4.a.b.c.d >"+
              " #x.y.z.w)" }
        ,   { "any (x.y).z < null or all (id -5.[a a]).bbb.ccc.ddd == 9879.567"
            , "(any #x.y.z < null or all id -5.a a.bbb.ccc.ddd == 9879.567)" }
        ,   { "all (((((a).b).c).d).e).f <= 0"
            , "all #a.b.c.d.e.f <= 0" }
        ,   { "any a+b-c*d+e/f%g+h-j+k*o < p-q+r-s+t*u*v*w-x/y%z"
            , "any (#a + #b - (#c * #d) + (#e / #f % #g) + #h - #j + (#k * #o)) <"+
              " (#p - #q + #r - #s + (#t * #u * #v * #w) - (#x / #y % #z))" }
        ,   { "any -5 > +3 and +1<+2 or -1<=-2"
            , "any ((-5 > 3 and 1 < 2) or -1 <= -2)" }
        ,   { "any a:b:c(a,b,c) < a/b/c"
            , "any #a:b:c(#a, #b, #c) < (#a / #b / #c)" }
        ,   { "all id 123(a=a, b=b, c=null) == null"
            , "all id 123(a=#a, b=#b, c=null) == null" }
        ,   { "all \"abc\" == null", "all \"abc\" == null" }
        ,   { "any abc == null", "any #abc == null" }
        ,   { "all f(true, false) == null", "all #f(true, false) == null" }
        ,   { "any true and all true and all not false"
            , "(any true and all true and all not false)"}
        ,   { "any true and all true or all not false"
            , "((any true and all true) or all not false)"}
        });
    }

    public ParseSetPredicateTests(String predicate, String expected) {
        this.setPredicate = predicate;
        this.expected = expected;
    }

    @Test
    public void checkParseSetPredicate()
        throws IOException, PolicyLanguageException {
        IExpression pred = parser.parseExpression(new StringReader(setPredicate));
        assertNotNull(pred);
        assertEquals(expected.toLowerCase(), pred.toString().toLowerCase());
    }

}
