package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/RelationOperatorTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for the RelationOperator.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={RelationOperatorTests.class})

public class RelationOperatorTests {

    private final RelationOperator op;
    private final String name;
    private final String representation;
    private final int index;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            {   RelationOperator.EQUAL
            ,   "EQUAL"
            ,   "=="
            ,   0 }
        ,   {   RelationOperator.NOT_EQUAL
            ,   "NOT_EQUAL"
            ,   "!="
            ,   1 }
        ,   {   RelationOperator.GREATER_THAN
            ,   "GREATER_THAN"
            ,   ">"
            ,   2 }
        ,   {   RelationOperator.GREATER_THAN_OR_EQUAL_TO
            ,   "GREATER_THAN_OR_EQUAL_TO"
            ,   ">="
            ,   3 }
        ,   {   RelationOperator.LESS_THAN
            ,   "LESS_THAN"
            ,   "<"
            ,   4 }
        ,   {   RelationOperator.LESS_THAN_OR_EQUAL_TO
            ,   "LESS_THAN_OR_EQUAL_TO"
            ,   "<="
            ,   5 }
        });
    }

    public RelationOperatorTests(
        RelationOperator op
    ,   String name
    ,   String representation
    ,   int index) {
        this.op = op;
        this.name = name;
        this.representation = representation;
        this.index = index;
    }

    @Test
    public void operatorExists() {
        assertEquals(op, RelationOperator.valueOf(name));
    }

    @Test
    public void representationIsCorrect() {
        assertEquals(representation, op.getRepresentation());
    }

    @Test
    public void toStringWorks() {
        assertEquals(representation, op.toString());
    }

    @Test
    public void forRepresentationWorks() {
        assertEquals(op, RelationOperator.forRepresentation(representation));
    }

    @Test
    public void noOtherOperators() {
        assertEquals(op, RelationOperator.values()[index]);
    }

    @Test
    public void correctSize() {
        assertEquals(10,  RelationOperator.size());
    }

    public static class FeatureTests {

        @Test
        public void operatorCount() {
            RelationOperator[] ops = RelationOperator.values();
            assertNotNull(ops);
            assertEquals(10, ops.length);
        }

        @Test(expected=NullPointerException.class)
        public void nullRepresentation() {
            RelationOperator.forRepresentation(null);
        }

        @Test(expected=IllegalArgumentException.class)
        public void unknownRepresentation() {
            RelationOperator.forRepresentation("~~~");
        }

        @Test(expected=IllegalArgumentException.class)
        public void duplicateRepresentation() {
            RelationOperator.register(RelationOperator.EQUAL);
        }

    }

}

