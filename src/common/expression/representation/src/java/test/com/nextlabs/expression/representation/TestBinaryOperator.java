package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/TestBinaryOperator.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for the BinaryOperator.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={TestBinaryOperator.class})

public class TestBinaryOperator {

    private final BinaryOperator op;
    private final String name;
    private final String representation;
    private final int index;
    private final BinaryOperator[] compatibles;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            {   BinaryOperator.OR
            ,   "OR"
            ,   "OR"
            ,   0
            ,   new BinaryOperator[] {
                    BinaryOperator.OR
                }
            }
        ,   {   BinaryOperator.AND
            ,   "AND"
            ,   "AND"
            ,   1
            ,   new BinaryOperator[] {
                    BinaryOperator.AND
                }
            }
        ,   {   BinaryOperator.ADD
            ,   "ADD"
            ,   "+"
            ,   2
            ,   new BinaryOperator[] {
                    BinaryOperator.ADD
                ,   BinaryOperator.SUBTRACT
                }
            }
        ,   {   BinaryOperator.SUBTRACT
            ,   "SUBTRACT"
            ,   "-"
            ,   3
            ,   new BinaryOperator[] {
                    BinaryOperator.ADD
                ,   BinaryOperator.SUBTRACT
                }
            }
        ,   {   BinaryOperator.MULTIPLY
            ,   "MULTIPLY"
            ,   "*"
            ,   4
            ,   new BinaryOperator[] {
                    BinaryOperator.MULTIPLY
                ,   BinaryOperator.DIVIDE
                ,   BinaryOperator.REMAINDER
                }
            }
        ,   {   BinaryOperator.DIVIDE
            ,   "DIVIDE"
            ,   "/"
            ,   5
            ,   new BinaryOperator[] {
                    BinaryOperator.MULTIPLY
                ,   BinaryOperator.DIVIDE
                ,   BinaryOperator.REMAINDER
                }
            }
        ,   {   BinaryOperator.REMAINDER
            ,   "REMAINDER"
            ,   "%"
            ,   6
            ,   new BinaryOperator[] {
                    BinaryOperator.MULTIPLY
                ,   BinaryOperator.DIVIDE
                ,   BinaryOperator.REMAINDER
                }
            }
        });
    }

    public TestBinaryOperator(
        BinaryOperator op
    ,   String name
    ,   String representation
    ,   int index
    ,   BinaryOperator[] compatibles) {
        this.op = op;
        this.name = name;
        this.representation = representation;
        this.index = index;
        this.compatibles = compatibles;
    }

    @Test
    public void operatorExists() {
        assertEquals(op, BinaryOperator.valueOf(name));
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
        assertEquals(op, BinaryOperator.forRepresentation(representation));
    }

    @Test
    public void noOtherOperators() {
        assertEquals(op, BinaryOperator.values()[index]);
    }

    @Test
    public void compatibilityWorks() {
        Set<BinaryOperator> seen = new HashSet<BinaryOperator>();
        for (BinaryOperator compatible : compatibles) {
            seen.add(compatible);
            assertEquals(op.getPrecedence(), compatible.getPrecedence());
        }
        for (BinaryOperator incompatible : BinaryOperator.values()) {
            if (seen.contains(incompatible)) {
                continue;
            }
            assertTrue(op.getPrecedence() != incompatible.getPrecedence());
        }
    }

    public static class FeatureTests {

        @Test
        public void correctSize() {
            assertEquals(7,  BinaryOperator.size());
        }

        @Test
        public void operatorCount() {
            BinaryOperator[] ops = BinaryOperator.values();
            assertNotNull(ops);
            assertEquals(7, ops.length);
        }

        @Test(expected=NullPointerException.class)
        public void nullRepresentation() {
            BinaryOperator.forRepresentation(null);
        }

        @Test(expected=IllegalArgumentException.class)
        public void unknownRepresentation() {
            BinaryOperator.forRepresentation("~~~");
        }

        @Test(expected=IllegalArgumentException.class)
        public void duplicateRepresentation() {
            BinaryOperator.register(BinaryOperator.ADD);
        }

    }

}

