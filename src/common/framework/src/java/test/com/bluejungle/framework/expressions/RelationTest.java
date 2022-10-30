/*
 * Created on April 24, 2007
 *
 * All sources, binaries, and HTML pages (C) copyright 2007 by Next Labs Inc.,
 * San Mateo CA.  Ownership remains with Next Labs Inc.  All rights reserved
 * worldwide.
 */

package com.bluejungle.framework.expressions;

import java.util.Date;

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;

/**
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/expressions/RelationTest.java#1 $:
 */

public class RelationTest extends TestCase {
    /**
     * Constructor for RelationTest.
     * @param arg0
     */
    private Constant beatles = null;
    private Constant beatlesRegexp = null;
    private Constant justPaul = null;
    private Constant justPaulRegexp = null;
    private Constant alphabeatles = null;
    private Constant livingBeatles = null;
    private Constant smallPrimes = null;
    private Constant emptyMultival = null;

    public RelationTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        beatles = Constant.build(Multivalue.create(new String[] { "John", "Paul", "George", "Ringo" }), "FABFOUR");
        beatlesRegexp = Constant.build(Multivalue.create(new String[] { "Jo?C", "Pa?C", "Geo?C", "R?C" }), "FABFOUR");
        justPaul = Constant.build(Multivalue.create(new String[] { "Paul"}), "The good looking one");
        justPaulRegexp = Constant.build(Multivalue.create(new String[] { "Pau?C"}), "The good looking one");
        alphabeatles = Constant.build(Multivalue.create(new String[] { "George", "John", "Paul", "Ringo" }), "ABFFORU");
        livingBeatles = Constant.build(Multivalue.create(new String[] { "Paul", "Ringo" }), "LIVING");
        smallPrimes = Constant.build(Multivalue.create (new Long[] { (long)2, (long)3, (long)5, (long)7, (long)11 }), "PRIMES");
        emptyMultival = Constant.build(Multivalue.create (new String[0]), "EMPTY");
    }

    private void check(RelationOp op, IExpression lhs, IExpression rhs, boolean expected) {
        Relation r = new Relation(op, lhs, rhs);
        assertEquals(expected, r.match(null));
    }

    private void checkIncompatible(RelationOp op, IExpression lhs, IExpression rhs) {
        Relation r = new Relation(op, lhs, rhs);
        try {
            r.match(null);
        } catch (RuntimeException e) {
            assertTrue(true);
            return;
        }

        assertTrue(false);
    }
            
    public final void testEQ() {
        RelationOp op = RelationOp.EQUALS;

        check(op, Constant.build(3), Constant.build(3), true);
        check(op, Constant.build("George"), beatles, true);
        check(op, justPaulRegexp, beatles, false);
        check(op, justPaul, beatlesRegexp, true);
        check(op, beatles, livingBeatles, true);
        check(op, alphabeatles, beatles, true);
        check(op, alphabeatles, beatlesRegexp, true);
        check(op, Constant.build("Ringo"), Constant.build("Ringo"), true);
        check(op, Constant.build("Ringo"), Constant.build("George"), false);
        check(op, beatles, Constant.build("Yoko"), false);
        check(op, Constant.NULL, Constant.build(3), false);
        check(op, Constant.build("4"), Constant.NULL, false);
        check(op, Constant.build("4"), Constant.build(4), true);
        check(op, Constant.build(3), Constant.build("4"), false);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), false);
        check(op, Constant.build("3"), Constant.build(new Date(3)), true);
        check(op, Constant.NULL, Constant.NULL, true);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
    }

    public final void testNEQ() {
        RelationOp op = RelationOp.NOT_EQUALS;

        check(op, Constant.build(3), Constant.build(3), false);
        check(op, Constant.build("George"), beatles, false);
        check(op, beatles, Constant.build("Ringo"), false);
        check(op, Constant.build("Ringo"), Constant.build("Ringo"), false);
        check(op, Constant.build("Ringo"), Constant.build("George"), true);
        check(op, beatles, Constant.build("Yoko"), true);
        check(op, Constant.NULL, Constant.build(3), true);
        check(op, Constant.build("4"), Constant.NULL, true);
        check(op, Constant.build("4"), Constant.build(4), false);
        check(op, Constant.build(3), Constant.build("4"), true);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), true);
        check(op, Constant.build("3"), Constant.build(new Date(3)), false);
        check(op, Constant.build(""), Constant.build(3), true);
        check(op, Constant.build(3), Constant.build(""), true);
        check(op, Constant.NULL, Constant.NULL, false);
    }

    public final void testLT() {
        RelationOp op = RelationOp.LESS_THAN;

        check(op, Constant.NULL, Constant.build(4), false);
        check(op, Constant.NULL, Constant.NULL, false);
        check(op, Constant.build(4), Constant.NULL, false);
        check(op, Constant.build(3), Constant.build(4), true);
        check(op, Constant.build("-3"), Constant.build("4"), true);
        check(op, Constant.build("4"), Constant.build(3), false);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), false);
        check(op, Constant.build(3), Constant.build("3"), false);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(55555)), true);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(44444)), false);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), true);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), false);
        check(op, Constant.NULL, Constant.NULL, false);

        checkIncompatible(op, Constant.build(new Date(44444)), Constant.build("3"));
        checkIncompatible(op, Constant.build(3), beatles);
        checkIncompatible(op, beatles, Constant.build("Paul"));
    }

    public final void testLTE() {
        RelationOp op = RelationOp.LESS_THAN_EQUALS;

        check(op, Constant.NULL, Constant.build(4), false);
        check(op, Constant.build(4), Constant.NULL, false);
        check(op, Constant.build(3), Constant.build(4), true);
        check(op, Constant.build("3"), Constant.build("4"), true);
        check(op, Constant.build("4"), Constant.build(3), false);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), true);
        check(op, Constant.build(3), Constant.build("3"), true);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(55555)), true);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(44444)), true);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), true);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), true);
        check(op, Constant.NULL, Constant.NULL, true);

        checkIncompatible(op, Constant.build(new Date(44444)), Constant.build("3"));
        checkIncompatible(op, Constant.build(3), beatles);
        checkIncompatible(op, beatles, Constant.build("John"));
    }

    public final void testGT() {
        RelationOp op = RelationOp.GREATER_THAN;

        check(op, Constant.NULL, Constant.build(4), false);
        check(op, Constant.build(4), Constant.NULL, false);
        check(op, Constant.build(3), Constant.build(4), false);
        check(op, Constant.build("3"), Constant.build("4"), false);
        check(op, Constant.build("4"), Constant.build(3), true);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), false);
        check(op, Constant.build(3), Constant.build("3"), false);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(55555)), false);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(44444)), false);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), false);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), false);
        check(op, Constant.NULL, Constant.NULL, false);

        checkIncompatible(op, Constant.build(new Date(44444)), Constant.build("3"));
        checkIncompatible(op, Constant.build(3), beatles);
        checkIncompatible(op, beatles, Constant.build("Ringo"));
    }

    public final void testGTE() {
        RelationOp op = RelationOp.GREATER_THAN_EQUALS;

        check(op, Constant.NULL, Constant.build(4), false);
        check(op, Constant.build(4), Constant.NULL, false);
        check(op, Constant.build(3), Constant.build(4), false);
        check(op, Constant.build("3"), Constant.build("4"), false);
        check(op, Constant.build("4"), Constant.build(3), true);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), true);
        check(op, Constant.build(3), Constant.build("3"), true);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(55555)), false);
        check(op, Constant.build(new Date(44444)), Constant.build(new Date(44444)), true);
        check(op, Constant.build("3"), Constant.build(new Date(44444)), false);
        check(op, Constant.build(""), Constant.build(3), false);
        check(op, Constant.build(3), Constant.build(""), false);
        check(op, Constant.build(""), Constant.build(""), true);
        check(op, Constant.NULL, Constant.NULL, true);

        checkIncompatible(op, Constant.build(new Date(44444)), Constant.build("3"));
        checkIncompatible(op, Constant.build(3), beatles);
        checkIncompatible(op, beatles, Constant.build("George"));
    }

    public final void testIncludes() {
        RelationOp op = RelationOp.INCLUDES;

        check(op, livingBeatles, beatles, false);
        check(op, livingBeatles, Constant.build("Paul"), true);
        check(op, livingBeatles, Constant.build("paul"), true);
        check(op, beatles, livingBeatles, true);
        check(op, beatles, beatles, true);
        check(op, livingBeatles, Constant.build("Paul"), true);
        check(op, smallPrimes, Constant.build(3), true);
        check(op, smallPrimes, Constant.build(4), false);
        check(op, beatles, emptyMultival, true);
        check(op, smallPrimes, emptyMultival, true);

        check(op, beatles, beatlesRegexp, true);
        check(op, alphabeatles, beatlesRegexp, true);
        check(op, justPaul, beatlesRegexp, false);
        check(op, justPaul, justPaulRegexp, true);
        check(op, beatles, justPaulRegexp, true);

        check(op, Constant.build("paul"), Constant.build("Paul"), true);
        check(op, Constant.build("paul"), justPaul, true);

        check(op, Constant.build(3), Constant.build(4), false);
        check(op, Constant.build(3), Constant.build(3), true);
        check(op, Constant.build(3), Constant.build(Multivalue.create(new Long[] {3l}), "Three"), true);
        
        check(op, Constant.NULL, Constant.NULL, true);
        check(op, Constant.NULL, beatles, false);
        check(op, beatles, Constant.NULL, true);
    }

    public final void testUnorderedEquals() {
        RelationOp op = RelationOp.EQUALS_UNORDERED;

        check(op, beatles, beatles, true);
        check(op, beatles, beatlesRegexp, false);
        check(op, beatles, alphabeatles, true);
        check(op, alphabeatles, beatles, true);
        check(op, livingBeatles, beatles, false);
        check(op, Constant.build("Paul"), beatles, false);
        check(op, Constant.build("Paul"), justPaul, true);
        check(op, justPaul, Constant.build("Paul"), true);
        check(op, Constant.build("paul"), Constant.build("Paul"), true);
        
        check(op, Constant.NULL, Constant.NULL, true);
        check(op, Constant.NULL, beatles, false);
        check(op, beatles, Constant.NULL, false);

        check(op, Constant.build(3), Constant.build(4), false);
        check(op, Constant.build(3), Constant.build(3), true);
    }
}



