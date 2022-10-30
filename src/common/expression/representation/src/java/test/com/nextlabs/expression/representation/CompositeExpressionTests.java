package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/CompositeExpressionTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CompositeExpression class.
 *
 * @author Sergey Kalinichenko
 */
public class CompositeExpressionTests {

    private static final Constant ZERO = Constant.makeInteger(0);

    private static final List<IExpression> EMPTY_EXPRESSIONS =
        Collections.emptyList();
    private static final List<IExpression> ONE_EXPRESSION =
        new ArrayList<IExpression>();
    private static final List<IExpression> TWO_EXPRESSIONS =
        new ArrayList<IExpression>();
    private static final List<IExpression> THREE_EXPRESSIONS =
        new ArrayList<IExpression>();
    private static final List<IExpression> FOUR_EXPRESSIONS =
        new ArrayList<IExpression>();

    static {
        ONE_EXPRESSION.add(Constant.makeDouble(1));
        TWO_EXPRESSIONS.add(Constant.makeInteger(1));
        THREE_EXPRESSIONS.add(Constant.makeDouble(1));
        FOUR_EXPRESSIONS.add(Constant.makeInteger(1));
        TWO_EXPRESSIONS.add(Constant.makeDouble(2));
        THREE_EXPRESSIONS.add(Constant.makeInteger(2));
        FOUR_EXPRESSIONS.add(Constant.makeDouble(2));
        THREE_EXPRESSIONS.add(Constant.makeInteger(3));
        FOUR_EXPRESSIONS.add(Constant.makeDouble(3));
        FOUR_EXPRESSIONS.add(Constant.makeInteger(4));
    }

    private static final List<BinaryOperator> EMPTY_OPERATORS =
        Collections.emptyList();
    private static final List<BinaryOperator> ONE_OPERATOR =
        new ArrayList<BinaryOperator>();
    private static final List<BinaryOperator> TWO_OPERATORS =
        new ArrayList<BinaryOperator>();
    private static final List<BinaryOperator> THREE_OPERATORS =
        new ArrayList<BinaryOperator>();
    private static final List<BinaryOperator> FOUR_OPERATORS =
        new ArrayList<BinaryOperator>();

    static {
        ONE_OPERATOR.add(BinaryOperator.MULTIPLY);
        TWO_OPERATORS.add(BinaryOperator.MULTIPLY);
        THREE_OPERATORS.add(BinaryOperator.MULTIPLY);
        FOUR_OPERATORS.add(BinaryOperator.MULTIPLY);
        TWO_OPERATORS.add(BinaryOperator.DIVIDE);
        THREE_OPERATORS.add(BinaryOperator.DIVIDE);
        FOUR_OPERATORS.add(BinaryOperator.DIVIDE);
        THREE_OPERATORS.add(BinaryOperator.REMAINDER);
        FOUR_OPERATORS.add(BinaryOperator.REMAINDER);
        FOUR_OPERATORS.add(BinaryOperator.MULTIPLY);
    }

    private static final Collection<IExpression> FIRST_NULL_EXPRESSIONS =
        new ArrayList<IExpression>();
    private static final Collection<IExpression> SECOND_NULL_EXPRESSIONS =
        new ArrayList<IExpression>();
    private static final Collection<BinaryOperator> INCOMPATIBLE_OPS =
        new ArrayList<BinaryOperator>();
    private static final Collection<BinaryOperator> OPERATORS_WITH_NULLS =
        new ArrayList<BinaryOperator>();

    static {
        FIRST_NULL_EXPRESSIONS.add(null);
        FIRST_NULL_EXPRESSIONS.add(IExpression.NULL);
        SECOND_NULL_EXPRESSIONS.add(IExpression.NULL);
        SECOND_NULL_EXPRESSIONS.add(null);
        INCOMPATIBLE_OPS.add(BinaryOperator.ADD);
        INCOMPATIBLE_OPS.add(BinaryOperator.MULTIPLY);
        OPERATORS_WITH_NULLS.add(BinaryOperator.ADD);
        OPERATORS_WITH_NULLS.add(null);
    }

    private CompositeExpression ce2;

    private CompositeExpression ce4;

    @Before
    public void prepare() {
        ce2 = new CompositeExpression(ONE_OPERATOR, TWO_EXPRESSIONS);
        ce4 = new CompositeExpression(THREE_OPERATORS, FOUR_EXPRESSIONS);

    }

    @Test(expected=NullPointerException.class)
    public void nullIterableOperators() {
        new CompositeExpression(null, TWO_EXPRESSIONS);
    }

    @Test(expected=NullPointerException.class)
    public void nullArrayOperators() {
        new CompositeExpression(null, new IExpression[] {
            IExpression.NULL, IExpression.NULL
        });
    }

    @Test(expected=NullPointerException.class)
    public void nullIterableExpressions() {
        new CompositeExpression(TWO_OPERATORS, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullArrayExpressions() {
        new CompositeExpression(new BinaryOperator[] {
            BinaryOperator.ADD, BinaryOperator.ADD
        }, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyIterableOperators() {
        new CompositeExpression(EMPTY_OPERATORS, TWO_EXPRESSIONS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyIterableExpressions() {
        new CompositeExpression(TWO_OPERATORS, EMPTY_EXPRESSIONS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyArrayOperators() {
        new CompositeExpression(new BinaryOperator[0], new IExpression[] {
            IExpression.NULL, IExpression.NULL
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyArrayExpressions() {
        new CompositeExpression(new BinaryOperator[] {
            BinaryOperator.ADD, BinaryOperator.ADD
        }, new IExpression[0]);
    }

    @Test(expected=NullPointerException.class)
    public void expressionsFirstNullIterable() {
        new CompositeExpression(
            ONE_OPERATOR, FIRST_NULL_EXPRESSIONS
        );
    }

    @Test(expected=NullPointerException.class)
    public void expressionsHasNullIterable() {
        new CompositeExpression(
            ONE_OPERATOR, SECOND_NULL_EXPRESSIONS
        );
    }

    @Test(expected=NullPointerException.class)
    public void expressionsFirstNullArray() {
        new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {null, IExpression.NULL}
        );
    }

    @Test(expected=NullPointerException.class)
    public void expressionsHasNullArray() {
        new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, null}
        );
    }

    @Test(expected=NullPointerException.class)
    public void operatorsHasNullsIterable() {
        new CompositeExpression(
            OPERATORS_WITH_NULLS
        ,   THREE_EXPRESSIONS
        );
    }

    @Test(expected=NullPointerException.class)
    public void operatorsHasNullsArray() {
        new CompositeExpression(
            new BinaryOperator[] {null}
        ,   new IExpression[] {
            IExpression.NULL, IExpression.NULL
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void incompatibleOperatorsIterable() {
        new CompositeExpression(INCOMPATIBLE_OPS, THREE_EXPRESSIONS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void incompatibleOperatorsArray() {
        new CompositeExpression(
            new BinaryOperator[] {
                BinaryOperator.ADD, BinaryOperator.MULTIPLY
            }, new IExpression[] {
                IExpression.NULL, IExpression.NULL, IExpression.NULL
            }
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void shortExpressionsIterable() {
        new CompositeExpression(THREE_OPERATORS, THREE_EXPRESSIONS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void shortExpressionsArray() {
        new CompositeExpression(
            new BinaryOperator[] {
                BinaryOperator.ADD, BinaryOperator.ADD
            }, new IExpression[] {
                IExpression.NULL, IExpression.NULL
            }
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void shortOperatorsIterable() {
        new CompositeExpression(TWO_OPERATORS, FOUR_EXPRESSIONS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void shortOperatorsArray() {
        new CompositeExpression(
            new BinaryOperator[] {
                BinaryOperator.ADD
            }, new IExpression[] {
                IExpression.NULL, IExpression.NULL, IExpression.NULL
            }
        );
    }

    @Test
    public void constructFromIterables1() {
        new CompositeExpression(ONE_OPERATOR, TWO_EXPRESSIONS);
    }

    @Test
    public void constructFromIterables2() {
        new CompositeExpression(TWO_OPERATORS, THREE_EXPRESSIONS);
    }

    @Test
    public void constructFromIterables3() {
        new CompositeExpression(THREE_OPERATORS, FOUR_EXPRESSIONS);
    }

    @Test
    public void constructFromArrays1() {
        new CompositeExpression(new BinaryOperator[] {
            BinaryOperator.ADD
        }, new IExpression[] {
            IExpression.NULL, IExpression.NULL
        });
    }

    @Test
    public void constructFromArrays2() {
        new CompositeExpression(new BinaryOperator[] {
            BinaryOperator.ADD, BinaryOperator.SUBTRACT
        }, new IExpression[] {
            IExpression.NULL, IExpression.NULL, IExpression.NULL
        });
    }

    @Test
    public void constructFromArrays3() {
        new CompositeExpression(new BinaryOperator[] {
            BinaryOperator.ADD
        ,   BinaryOperator.ADD
        ,   BinaryOperator.ADD
        }, new IExpression[] {
            IExpression.NULL
        ,   IExpression.NULL
        ,   IExpression.NULL
        ,   IExpression.NULL
        });
    }

    @Test
    public void getSize() {
        assertEquals(4, ce4.size());
    }

    @Test
    public void iterableIsImplemented() {
        Iterator<IExpression> ei = FOUR_EXPRESSIONS.iterator();
        Iterator<BinaryOperator> oi = THREE_OPERATORS.iterator();
        boolean first = true;
        for (ICompositeExpression.Element e : ce4) {
            if (!first) {
                assertEquals(ei.next(), e.getExpression());
                assertEquals(oi.next(), e.getOperatorBefore());
                assertFalse(e.isFirst());
            } else {
                assertEquals(ei.next(), e.getExpression());
                assertTrue(e.isFirst());
                first = false;
            }
        }
    }

    @Test
    public void validIndexing() {
        Iterator<IExpression> ei = FOUR_EXPRESSIONS.iterator();
        Iterator<BinaryOperator> oi = THREE_OPERATORS.iterator();
        BinaryOperator last = null;
        for ( int i = 0 ; i != ce4.size() ; i++) {
            assertEquals(ei.next(), ce4.getExpression(i));
            if (i != 0) {
                assertEquals(last, ce4.getOperatorBefore(i));
            } else {
                assertNull(ce4.getOperatorBefore(i));
            }
            if (i != 3) {
                assertEquals(last=oi.next(), ce4.getOperatorAfter(i));
            } else {
                assertNull(ce4.getOperatorAfter(i));
            }
        }
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void negativeIndexingForExpressions() {
        ce4.getExpression(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void negativeIndexingForOperators() {
        ce4.getOperatorBefore(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void offByOneIndexingForExpressions() {
        ce4.getExpression(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void offByOneIndexingForOperators() {
        ce4.getOperatorBefore(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void offByManyIndexingForExpressions() {
        ce4.getExpression(400);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void offByManyIndexingForOperators() {
        ce4.getOperatorBefore(400);
    }

    @Test
    public void elementIteration() {
        Iterator<ICompositeExpression.Element> ei = ce4.iterator();
        assertNotNull(ei);
        assertTrue(ei.hasNext());
        ICompositeExpression.Element e;
        // Expression 0
        e = ei.next();
        assertNotNull(e);
        assertTrue(e.isFirst());
        assertFalse(e.isLast());
        assertEquals(FOUR_EXPRESSIONS.get(0), e.getExpression());
        assertEquals(THREE_OPERATORS.get(0), e.getOperatorAfter());
        assertNull(e.getOperatorBefore());
        // Expression 1
        e = ei.next();
        assertNotNull(e);
        assertFalse(e.isFirst());
        assertFalse(e.isLast());
        assertEquals(FOUR_EXPRESSIONS.get(1), e.getExpression());
        assertEquals(THREE_OPERATORS.get(0), e.getOperatorBefore());
        assertEquals(THREE_OPERATORS.get(1), e.getOperatorAfter());
        // Expression 2
        e = ei.next();
        assertNotNull(e);
        assertFalse(e.isFirst());
        assertFalse(e.isLast());
        assertEquals(FOUR_EXPRESSIONS.get(2), e.getExpression());
        assertEquals(THREE_OPERATORS.get(1), e.getOperatorBefore());
        assertEquals(THREE_OPERATORS.get(2), e.getOperatorAfter());
        // Expression 3
        e = ei.next();
        assertNotNull(e);
        assertFalse(e.isFirst());
        assertTrue(e.isLast());
        assertEquals(FOUR_EXPRESSIONS.get(3), e.getExpression());
        assertEquals(THREE_OPERATORS.get(2), e.getOperatorBefore());
        assertNull(e.getOperatorAfter());
    }

    @Test
    public void insertionBeforeWorks0() {
        int h = ce4.hashCode();
        ce4.insertBefore(0, ZERO, BinaryOperator.REMAINDER);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(0 % 1 * 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void insertionBeforeWorks1() {
        int h = ce4.hashCode();
        ce4.insertBefore(1, ZERO, BinaryOperator.REMAINDER);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 0 % 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void insertionBeforeWorks2() {
        int h = ce4.hashCode();
        ce4.insertBefore(2, ZERO, BinaryOperator.REMAINDER);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 0 % 3.0 % 4)", ce4.toString());
    }

    @Test
    public void insertionBeforeWorks3() {
        int h = ce4.hashCode();
        ce4.insertBefore(3, ZERO, BinaryOperator.REMAINDER);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 3.0 % 0 % 4)", ce4.toString());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void insertionBeforeExceptionNeg() {
        ce4.insertBefore(-1, ZERO, BinaryOperator.REMAINDER);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void insertionBeforeExceptionLarge() {
        ce4.insertBefore(4, ZERO, BinaryOperator.REMAINDER);
    }

    @Test
    public void insertionAfterWorks0() {
        int h = ce4.hashCode();
        ce4.insertAfter(0, BinaryOperator.REMAINDER, ZERO);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 % 0 * 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void insertionAfterWorks1() {
        int h = ce4.hashCode();
        ce4.insertAfter(1, BinaryOperator.REMAINDER, ZERO);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 % 0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void insertionAfterWorks2() {
        int h = ce4.hashCode();
        ce4.insertAfter(2, BinaryOperator.REMAINDER, ZERO);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 3.0 % 0 % 4)", ce4.toString());
    }

    @Test
    public void insertionAfterWorks3() {
        int h = ce4.hashCode();
        ce4.insertAfter(3, BinaryOperator.REMAINDER, ZERO);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 3.0 % 4 % 0)", ce4.toString());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void insertionAfterExceptionNeg() {
        ce4.insertAfter(-1, BinaryOperator.REMAINDER, ZERO);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void insertionAfterExceptionLarge() {
        ce4.insertAfter(4, BinaryOperator.REMAINDER, ZERO);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removalBeforeWorks0() {
        ce4.removeBefore(0);
    }

    @Test
    public void addWorks1() {
        int h = ce2.hashCode();
        ce2.add(BinaryOperator.MULTIPLY, Constant.makeDouble(3));
        assertFalse(ce2.hashCode() == h);
        assertEquals("(1 * 2.0 * 3.0)", ce2.toString());
    }

    @Test
    public void addWorks2() {
        int h = ce2.hashCode();
        ce2.add(BinaryOperator.DIVIDE, Constant.makeDouble(3));
        assertFalse(ce2.hashCode() == h);
        ce2.add(BinaryOperator.REMAINDER, Constant.makeDouble(4));
        assertEquals("(1 * 2.0 / 3.0 % 4.0)", ce2.toString());
    }

    @Test
    public void removalBeforeWorks1() {
        int h = ce4.hashCode();
        ce4.removeBefore(1);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void removalBeforeWorks2() {
        ce4.removeBefore(2);
        assertEquals("(1 * 2.0 % 4)", ce4.toString());
    }

    @Test
    public void removalBeforeWorks3() {
        int h = ce4.hashCode();
        ce4.removeBefore(3);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 3.0)", ce4.toString());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removalBeforeExceptionNeg() {
        ce4.removeBefore(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removalBeforeExceptionLarge() {
        ce4.removeBefore(4);
    }

    @Test(expected=IllegalStateException.class)
    public void removalAfterExceptionLastElement0() {
        ce2.removeAfter(0);
    }

    @Test(expected=IllegalStateException.class)
    public void removalAfterExceptionLastElement1() {
        ce2.removeAfter(1);
    }

    @Test
    public void removalAfterWorks0() {
        int h = ce4.hashCode();
        ce4.removeAfter(0);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void removalAfterWorks1() {
        int h = ce4.hashCode();
        ce4.removeAfter(1);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 3.0 % 4)", ce4.toString());
    }

    @Test
    public void removalAfterWorks2() {
        int h = ce4.hashCode();
        ce4.removeAfter(2);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 * 2.0 / 4)", ce4.toString());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removalAfterExceptionNeg() {
        ce4.removeAfter(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removalAfterExceptionLarge() {
        ce4.removeAfter(3);
    }

    @Test(expected=IllegalStateException.class)
    public void removalBeforeExceptionLastElement0() {
        ce2.removeBefore(0);
    }

    @Test(expected=IllegalStateException.class)
    public void removalBeforeExceptionLastElement1() {
        ce2.removeBefore(1);
    }

    @Test
    public void setExpression() {
        int h = ce2.hashCode();
        ce2.setExpression(0, IExpression.NULL);
        assertFalse(ce4.hashCode() == h);
    }

    @Test
    public void setOpBeforen() {
        int h = ce4.hashCode();
        ce4.setOperatorBefore(1, BinaryOperator.DIVIDE);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 / 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test
    public void setOpAfter() {
        int h = ce4.hashCode();
        ce4.setOperatorAfter(0, BinaryOperator.DIVIDE);
        assertFalse(ce4.hashCode() == h);
        assertEquals("(1 / 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test(expected=NullPointerException.class)
    public void setExpressionChecksNull() {
        ce2.setExpression(0, null);
    }

    @Test(expected=NullPointerException.class)
    public void setOpBeforenChecksNull() {
        ce2.setOperatorBefore(1, null);
    }

    @Test(expected=NullPointerException.class)
    public void setOpAfterChecksNull() {
        ce2.setOperatorAfter(0, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setOpBeforenChecksCompatibility() {
        ce2.setOperatorBefore(1, BinaryOperator.ADD);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setOpAfterChecksCompatibility() {
        ce2.setOperatorAfter(0, BinaryOperator.ADD);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setOpBeforenChecksIndex() {
        ce2.setOperatorBefore(0, BinaryOperator.MULTIPLY);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setOpAfterChecksIndex() {
        ce2.setOperatorAfter(1, BinaryOperator.MULTIPLY);
    }

    @Test(expected=NoSuchElementException.class)
    public void invalidIteration() {
        Iterator<ICompositeExpression.Element> ei = ce4.iterator();
        for ( int i = 0 ; i != ce4.size() + 1 ; i++) {
            ei.next();
        }
    }

    @Test(expected=UnsupportedOperationException.class)
    public void removingExpressionThroughIterator() {
        ce4.iterator().remove();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void removingElementThroughIterator() {
        ce4.iterator().remove();
    }

    @Test
    public void accept() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        ce4.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitComposite", ev.getMethods()[0]);
        assertSame(ce4, ev.getArguments()[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals("(1 * 2.0 / 3.0 % 4)", ce4.toString());
    }

    @Test(expected=NullPointerException.class)
    public void addNullOperator() {
        ce2.add(null, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void insertBeforeNullOperator() {
        ce2.insertBefore(1, IExpression.NULL, null);
    }

    @Test(expected=NullPointerException.class)
    public void insertAfterNullOperator() {
        ce2.insertAfter(0, null, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addIncompatibleOperator() {
        ce2.add(BinaryOperator.ADD, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertBeforeIncompatibleOperator() {
        ce2.insertBefore(1, IExpression.NULL, BinaryOperator.ADD);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertAfterIncompatibleOperator() {
        ce2.insertAfter(0, BinaryOperator.ADD, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addImmediateReference() {
        ce2.add(BinaryOperator.MULTIPLY, ce2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setImmediateReference() {
        ce2.setExpression(0, ce2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertBeforeImmediateReference() {
        ce2.insertBefore(1, ce2, BinaryOperator.MULTIPLY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertAfterImmediateReference() {
        ce2.insertAfter(0, BinaryOperator.MULTIPLY, ce2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addCircularReference() {
        CompositeExpression ce = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {ce2, IExpression.FALSE}
        );
        ce2.add(BinaryOperator.MULTIPLY, ce);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setCircularReference() {
        CompositeExpression ce = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {ce2, IExpression.FALSE}
        );
        ce2.setExpression(0, ce);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertBeforeCircularReference() {
        CompositeExpression ce = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {ce2, IExpression.NULL}
        );
        ce2.insertBefore(1, ce, BinaryOperator.MULTIPLY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void insertAfterCircularReference() {
        CompositeExpression ce = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, ce2}
        );
        ce2.insertAfter(0, BinaryOperator.MULTIPLY, ce);
    }

    @Test(expected=NullPointerException.class)
    public void addNull() {
        ce2.add(BinaryOperator.MULTIPLY, null);
    }

    @Test(expected=NullPointerException.class)
    public void insertBeforeNullExpression() {
        ce2.insertBefore(1, null, BinaryOperator.MULTIPLY);
    }

    @Test(expected=NullPointerException.class)
    public void insertAfterNullExpression() {
        ce2.insertAfter(0, BinaryOperator.MULTIPLY, null);
    }

    @Test
    public void equalityToSelf() {
        assertTrue(ce2.equals(ce2));
        assertTrue(ce4.equals(ce4));
    }

    @Test
    public void equality() {
        assertTrue(ce2.equals(
            new CompositeExpression(ONE_OPERATOR, TWO_EXPRESSIONS))
        );
    }

    @Test
    public void inequalityWrongOperator() {
        IExpression lhs = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, IExpression.NULL}
        );
        IExpression rhs = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.SUBTRACT}
        ,   new IExpression[] {IExpression.NULL, IExpression.NULL}
        );
        assertFalse(lhs.equals(rhs));
        assertFalse(rhs.equals(lhs));
    }

    @Test
    public void inequalityWrongOperatorCount() {
        IExpression lhs = new CompositeExpression(
            new BinaryOperator[] {
                BinaryOperator.ADD,BinaryOperator.ADD
            }
        ,   new IExpression[] {
                IExpression.NULL, IExpression.NULL, IExpression.NULL
            }
        );
        IExpression rhs = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, IExpression.NULL}
        );
        assertFalse(lhs.equals(rhs));
        assertFalse(rhs.equals(lhs));
    }


    @Test
    public void inequalityWrongExpression() {
        IExpression lhs = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, IExpression.NULL}
        );
        IExpression rhs = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.ADD}
        ,   new IExpression[] {IExpression.NULL, IExpression.TRUE}
        );
        assertFalse(lhs.equals(rhs));
        assertFalse(rhs.equals(lhs));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(ce2.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(ce2.equals("ce2"));
    }

    @Test
    public void hashCodeWorks() {
        assertFalse(ce2.hashCode() == ce4.hashCode());
        assertEquals(
            ce2.hashCode()
        ,   new CompositeExpression(ONE_OPERATOR, TWO_EXPRESSIONS).hashCode()
        );
    }

}
