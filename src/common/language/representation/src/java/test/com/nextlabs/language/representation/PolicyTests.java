package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/PolicyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.FunctionCall;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the Policy class.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyTests {

    private static final Path TEST_PATH1 = new Path(
        "policy", "tests", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "policy", "tests", "two"
    );

    private static final IReference<IPolicyType> REF1T =
        IReferenceFactory.DEFAULT.create(TEST_PATH1, IPolicyType.class);

    private static final IReference<IPolicyType> REF2T =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IPolicyType.class);

    private static final IReference<IPolicy> REF1P =
        IReferenceFactory.DEFAULT.create(TEST_PATH1, IPolicy.class);

    private static final IReference<IPolicy> REF2P =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IPolicy.class);

    private static final String SECTION1 = "on";

    private static final String SECTION2 = "by";

    private IExpression ALL_TRUE =
        new UnaryExpression(UnaryOperator.ALL, IExpression.TRUE);

    private IExpression ANY_TRUE =
        new UnaryExpression(UnaryOperator.ANY, IExpression.TRUE);

    private IExpression ANY_FALSE =
        new UnaryExpression(UnaryOperator.ANY, IExpression.FALSE);

    private IExpression ALL_FALSE =
        new UnaryExpression(UnaryOperator.ALL, IExpression.FALSE);

    private Policy p1;

    private Policy p2;

    private IFunctionCall o1;

    private IFunctionCall o2;

    @Before
    public void prepare() {
        p1 = new Policy(TEST_PATH1, REF2P, null);
        p2 = new Policy(TEST_PATH2, null, REF1T);
        IReference<IFunction> R1 =
            IReferenceFactory.DEFAULT.create(TEST_PATH1, IFunction.class);
        IReference<IFunction> R2 =
            IReferenceFactory.DEFAULT.create(TEST_PATH2, IFunction.class);
        o1 = new FunctionCall(R1, IExpression.TRUE);
        o2 = new FunctionCall(R2, IExpression.FALSE, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void createNullPath() {
        new Policy(null, REF1P, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createNoBaseOrType() {
        new Policy(TEST_PATH1, null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createBothBaseAndType() {
        new Policy(TEST_PATH1, REF1P, REF2T);
    }

    @Test(expected=NullPointerException.class)
    public void setTypeNull() {
        p2.setType(null);
    }

    @Test(expected=NullPointerException.class)
    public void setBaseNull() {
        p1.setBase(null);
    }

    @Test
    public void setType() {
        assertTrue(p1.hasBase());
        assertFalse(p1.hasType());
        p1.setType(REF1T);
        assertTrue(p1.hasType());
        assertFalse(p1.hasBase());
        assertEquals(REF1T, p1.getType());
    }

    @Test
    public void setBase() {
        assertTrue(p2.hasType());
        assertFalse(p2.hasBase());
        p2.setBase(REF1P);
        assertTrue(p2.hasBase());
        assertFalse(p2.hasType());
        assertEquals(REF1T, p2.getBase());
    }

    @Test(expected=NullPointerException.class)
    public void addRuleNullCondition() {
        p1.addRule(null, Outcome.ALLOW);
    }

    @Test(expected=NullPointerException.class)
    public void addRuleNullOutcome() {
        p1.addRule(IExpression.TRUE, null);
    }

    @Test
    public void addRule() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.addRule(IExpression.FALSE, Outcome.DENY);
        Iterator<Policy.IRule> i = p1.getRules().iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());
        Policy.IRule r0 = i.next();
        assertNotNull(r0);
        assertEquals(IExpression.TRUE, r0.getCondition());
        assertTrue(r0.isDefault());
        assertEquals(Outcome.ALLOW, r0.getOutcome());
        assertTrue(i.hasNext());
        Policy.IRule r1 = i.next();
        assertNotNull(r1);
        assertEquals(IExpression.FALSE, r1.getCondition());
        assertFalse(r1.isDefault());
        assertEquals(Outcome.DENY, r1.getOutcome());
        assertFalse(i.hasNext());
        assertEquals(2, p1.getRuleCount());
    }

    @Test
    public void addRuleIndex() {
        Policy.IRule r0 =
            p1.addRule(0, IExpression.TRUE, Outcome.ALLOW);
        assertNotNull(r0);
        Policy.IRule r1 =
            p1.addRule(0, IExpression.FALSE, Outcome.DENY);
        assertNotNull(r1);
        assertEquals(2, p1.getRuleCount());
        assertEquals(IExpression.FALSE, p1.getRule(0).getCondition());
        assertEquals(Outcome.DENY, p1.getRule(0).getOutcome());
        assertEquals(IExpression.TRUE, p1.getRule(1).getCondition());
        assertEquals(Outcome.ALLOW, p1.getRule(1).getOutcome());
        assertEquals(r1, p1.getRule(0));
        assertEquals(r0, p1.getRule(1));
    }

    @Test(expected=NullPointerException.class)
    public void addRuleIndexNullCondition() {
        p1.addRule(0, null, Outcome.ALLOW);
    }

    @Test(expected=NullPointerException.class)
    public void addRuleIndexNullOutcome() {
        p1.addRule(0, IExpression.TRUE, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addRuleNegativeIndex() {
        p1.addRule(-1, IExpression.TRUE, Outcome.ALLOW);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addRuleLargeIndex() {
        p1.addRule(1, IExpression.TRUE, Outcome.ALLOW);
    }


    @Test
    public void removeRuleIndex() {
        Policy.IRule r0 =
            p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertNotNull(r0);
        Policy.IRule r1 =
            p1.addRule(IExpression.FALSE, Outcome.DENY);
        assertNotNull(r1);
        assertEquals(2, p1.getRuleCount());
        p1.removeRule(0);
        assertEquals(1, p1.getRuleCount());
        assertEquals(r1, p1.getRule(0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeRuleNegativeIndex() {
        p1.removeRule(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeRuleLargeIndex() {
        p1.removeRule(0);
    }

    @Test
    public void removeRule() {
        Policy.IRule r0 =
            p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertNotNull(r0);
        Policy.IRule r1 =
            p1.addRule(IExpression.FALSE, Outcome.DENY);
        assertNotNull(r1);
        Policy.IRule r2 =
            p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertNotNull(r2);
        Policy.IRule r3 =
            p1.addRule(IExpression.FALSE, Outcome.DENY);
        assertNotNull(r3);
        assertEquals(r0, r2);
        assertEquals(r1, r3);
        assertEquals(4, p1.getRuleCount());
        assertTrue(p1.removeRule(r0));
        assertEquals(2, p1.getRuleCount());
        assertEquals(r1, p1.getRule(0));
        assertEquals(r1, p1.getRule(1));
        assertTrue(p1.removeRule(r1));
        assertEquals(0, p1.getRuleCount());
    }

    @Test
    public void setRule() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertEquals(IExpression.TRUE, p1.getRule(0).getCondition());
        assertEquals(Outcome.ALLOW, p1.getRule(0).getOutcome());
        p1.setRule(0, IExpression.FALSE, Outcome.DENY);
        assertEquals(IExpression.FALSE, p1.getRule(0).getCondition());
        assertEquals(Outcome.DENY, p1.getRule(0).getOutcome());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setRuleNegativeIndex() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.setRule(-1, IExpression.FALSE, Outcome.DENY);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setRuleLargeIndex() {
        p1.setRule(1, IExpression.FALSE, Outcome.DENY);
    }

    @Test(expected=NullPointerException.class)
    public void setRuleNullPredicate() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.setRule(0, null, Outcome.DENY);
    }

    @Test(expected=NullPointerException.class)
    public void setRuleNullOutcome() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.setRule(0, IExpression.TRUE, null);
    }

    @Test
    public void ruleHashCode() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.addRule(IExpression.FALSE, Outcome.ALLOW);
        Iterator<Policy.IRule> i = p1.getRules().iterator();
        Policy.IRule r0 = i.next();
        Policy.IRule r1 = i.next();
        assertTrue(r0.hashCode() != r1.hashCode());
    }

    @Test
    public void ruleEquals() {
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        p1.addRule(IExpression.FALSE, Outcome.ALLOW);
        p1.addRule(IExpression.FALSE, Outcome.DENY);
        Iterator<Policy.IRule> i = p1.getRules().iterator();
        Policy.IRule r0 = i.next();
        Policy.IRule r1 = i.next();
        Policy.IRule r2 = i.next();
        assertEquals(r0, r0);
        assertEquals(r1, r1);
        assertEquals(r2, r2);
        assertFalse(r0.equals(r1));
        assertFalse(r0.equals(r2));
        assertFalse(r1.equals(r0));
        assertFalse(r1.equals(r2));
        assertFalse(r2.equals(r0));
        assertFalse(r2.equals(r1));
        assertFalse(r0.equals(null));
        assertFalse(r1.equals(false));
    }

    @Test(expected=NullPointerException.class)
    public void addObligationNullOutcome() {
        p1.addObligation(null, o1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addObligationInvalidOutcome() {
        p1.addObligation(Outcome.NOTHING, o1);
    }

    @Test(expected=NullPointerException.class)
    public void addObligationNullObligation() {
        p1.addObligation(Outcome.ALLOW, null);
    }

    @Test
    public void addObligation() {
        p1.addObligation(Outcome.ALLOW, o1);
        p1.addObligation(Outcome.ALLOW, o2);
        p1.addObligation(Outcome.DENY, o2);
        p1.addObligation(Outcome.DENY, o1);
        Iterator<IFunctionCall> ao =
            p1.getObligations(Outcome.ALLOW).iterator();
        assertNotNull(ao);
        assertTrue(ao.hasNext());
        assertEquals(o1, ao.next());
        assertTrue(ao.hasNext());
        assertEquals(o2, ao.next());
        assertFalse(ao.hasNext());
        Iterator<IFunctionCall> xo =
            p1.getObligations(Outcome.DENY).iterator();
        assertNotNull(xo);
        assertTrue(xo.hasNext());
        assertEquals(o2, xo.next());
        assertTrue(xo.hasNext());
        assertEquals(o1, xo.next());
        assertFalse(xo.hasNext());
    }

    @Test
    public void getObligations() {
        p1.addObligation(Outcome.ALLOW, o1);
        p1.addObligation(Outcome.ALLOW, o2);
        p1.addObligation(Outcome.DENY, o2);
        p1.addObligation(Outcome.DENY, o1);
        Iterator<IPolicy.IObligations> ao =
            p1.getObligations().iterator();
        assertNotNull(ao);
        assertTrue(ao.hasNext());
        IPolicy.IObligations oo1 = ao.next();
        assertNotNull(oo1);
        assertEquals(Outcome.ALLOW, oo1.getOutcome());
        Iterator<IFunctionCall> ao1 = oo1.iterator();
        assertTrue(ao1.hasNext());
        assertEquals(o1, ao1.next());
        assertEquals(o2, ao1.next());
        assertFalse(ao1.hasNext());
        assertTrue(ao.hasNext());
        IPolicy.IObligations oo2 = ao.next();
        assertNotNull(oo2);
        assertEquals(Outcome.DENY, oo2.getOutcome());
        assertFalse(ao.hasNext());
        Iterator<IFunctionCall> xo = oo2.iterator();
        assertNotNull(xo);
        assertTrue(xo.hasNext());
        assertEquals(o2, xo.next());
        assertTrue(xo.hasNext());
        assertEquals(o1, xo.next());
        assertFalse(xo.hasNext());
    }

    @Test
    public void setObligation() {
        p1.addObligation(Outcome.DENY, o1);
        assertEquals(o1, p1.getObligation(Outcome.DENY, 0));
        p1.setObligation(Outcome.DENY, 0, o2);
        assertEquals(o2, p1.getObligation(Outcome.DENY, 0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void setObligationInvalidOutcome() {
        p1.setObligation(Outcome.NOTHING, 0, o1);
    }

    @Test(expected=NullPointerException.class)
    public void setObligationNullOutcome() {
        p1.setObligation(null, 0, o1);
    }

    @Test(expected=NullPointerException.class)
    public void setObligationNullObligation() {
        p1.addObligation(Outcome.DENY, o1);
        p1.setObligation(Outcome.DENY, 0, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setObligationNegativeIndex() {
        p1.setObligation(Outcome.DENY, -1, o1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setObligationLargeIndex() {
        p1.setObligation(Outcome.DENY, 0, o1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addObligationNegativeIndex() {
        p1.addObligation(Outcome.ALLOW, -1, o1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addObligationLargeIndex() {
        p1.addObligation(Outcome.ALLOW, 1, o1);
    }

    @Test
    public void addObligationIndex() {
        p1.addObligation(Outcome.ALLOW, 0, o1);
        p1.addObligation(Outcome.ALLOW, 0, o2);
        assertEquals(2, p1.getObligationCount(Outcome.ALLOW));
        assertEquals(o2, p1.getObligation(Outcome.ALLOW, 0));
        assertEquals(o1, p1.getObligation(Outcome.ALLOW, 1));
    }

    @Test
    public void removeObligationIndex() {
        p1.addObligation(Outcome.ALLOW, 0, o1);
        p1.addObligation(Outcome.ALLOW, 0, o2);
        assertEquals(2, p1.getObligationCount(Outcome.ALLOW));
        assertEquals( o2, p1.removeObligation(Outcome.ALLOW, 0));
        assertEquals(1, p1.getObligationCount(Outcome.ALLOW));
        assertEquals(o1, p1.getObligation(Outcome.ALLOW, 0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeObligationNegativeIndex() {
        p1.removeObligation(Outcome.ALLOW, -1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeObligationLargeIndex() {
        p1.removeObligation(Outcome.ALLOW, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeObligationIndexInvalidOutcome() {
        p1.removeObligation(Outcome.NOTHING, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeObligationInvalidOutcome() {
        p1.removeObligation(Outcome.NOTHING, o1);
    }

    @Test(expected=NullPointerException.class)
    public void removeObligationIndexNullOutcome() {
        p1.removeObligation(null, 0);
    }

    @Test(expected=NullPointerException.class)
    public void removeObligationNullOutcome() {
        p1.removeObligation(null, o1);
    }

    @Test
    public void removeObligation() {
        p1.addObligation(Outcome.ALLOW, o1);
        p1.addObligation(Outcome.ALLOW, o2);
        p1.addObligation(Outcome.ALLOW, o1);
        p1.addObligation(Outcome.ALLOW, o2);
        assertEquals(4, p1.getObligationCount(Outcome.ALLOW));
        assertTrue(p1.removeObligation(Outcome.ALLOW, o1));
        assertEquals(2, p1.getObligationCount(Outcome.ALLOW));
        assertEquals(o2, p1.getObligation(Outcome.ALLOW, 0));
        assertEquals(o2, p1.getObligation(Outcome.ALLOW, 1));
        assertTrue(p1.removeObligation(Outcome.ALLOW, o2));
        assertEquals(0, p1.getObligationCount(Outcome.ALLOW));
        assertFalse(p1.removeObligation(Outcome.ALLOW, o1));
        assertFalse(p1.removeObligation(Outcome.ALLOW, o2));
    }


    @Test(expected=NullPointerException.class)
    public void getObligationsNullOutcome() {
        p1.getObligations(null);
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        p1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitPolicy(IPolicy p) {
                assertSame(p1, p);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void equalityToSelf() {
        assertEquals(p1, p1);
    }

    @Test
    public void inequalityToNull() {
        assertFalse(p1.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(p1.equals(123));
    }

    @Test
    public void inequalityToDifferentName() {
        IPolicy p = new Policy(TEST_PATH2, REF2P, null);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }
    @Test
    public void inequalityDifferentType() {
        IPolicy p = new Policy(TEST_PATH2, null, REF2T);
        assertFalse(p.equals(p2));
        assertFalse(p2.equals(p));
    }

    @Test
    public void inequalityDifferentBase() {
        IPolicy p = new Policy(TEST_PATH1, REF1P, null);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentObligationCount() {
        Policy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p.addObligation(Outcome.ALLOW, o1);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }

    @Test
    public void inequalityDifferentObligations() {
        Policy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p1.addObligation(Outcome.ALLOW, o1);
        p.addObligation(Outcome.ALLOW, o2);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }

    @Test
    public void inequalityDifferentRuleCount() {
        IPolicy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p1.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }


    @Test
    public void inequalityDifferentRules() {
        IPolicy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p1.addRule(IExpression.TRUE, Outcome.DENY);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }

    @Test
    public void inequalityDifferentContexts() {
        Policy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p.getTarget().addContext(SECTION1, ALL_FALSE);
        p1.getTarget().addContext(SECTION1, ANY_FALSE);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }

    @Test
    public void inequalityDifferentContextCounts() {
        Policy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        p.getTarget().addContext(SECTION1, ALL_FALSE);
        p1.getTarget().addContext(SECTION1, ALL_FALSE);
        assertEquals(p1, p);
        p.getTarget().addContext(SECTION2, ANY_TRUE);
        assertFalse(p1.equals(p));
        assertFalse(p.equals(p1));
    }

    @Test
    public void equality() {
        IPolicy p = new Policy(TEST_PATH1, REF2P, null);
        assertEquals(p1, p);
        assertEquals(p, p1);
        p = new Policy(TEST_PATH2, null, REF1T);
        assertEquals(p2, p);
        assertEquals(p, p2);
    }

    @Test
    public void hashCodeWorks() {
        assertTrue(p1.hashCode() != p2.hashCode());
    }

    @Test
    public void toStringWithBase() {
        assertEquals("policy "+TEST_PATH1+" extends "+REF2T, p1.toString());
    }

    @Test
    public void toStringWithType() {
        assertEquals("policy "+TEST_PATH2+" is "+REF1T, p2.toString());
    }

    @Test
    public void toStringWithRules() {
        p2.addRule(IExpression.TRUE, Outcome.ALLOW);
        assertEquals(
            "policy "+TEST_PATH2+" is "+REF1T+
            "\nwhen true do ALLOW"
        ,   p2.toString()
        );
    }

    @Test
    public void toStringWithObligations1() {
        p2.addObligation(Outcome.ALLOW, o1);
        assertEquals(
            "policy "+TEST_PATH2+" is "+REF1T+
            "\non ALLOW do "+o1
        ,   p2.toString()
        );
    }

    @Test
    public void toStringWithObligations2() {
        p2.addObligation(Outcome.ALLOW, o1);
        p2.addObligation(Outcome.ALLOW, o2);
        assertEquals(
            "policy "+TEST_PATH2+" is "+REF1T+
            "\non ALLOW do "+o1+", "+o2
        ,   p2.toString()
        );
    }

    @Test
    public void toStringWithContexts() {
        p2.getTarget().addContext(SECTION1, ALL_TRUE);
        assertEquals(
            "policy "+TEST_PATH2+" is "+REF1T+
            "\n    "+SECTION1+" ALL true"
        ,   p2.toString()
        );
    }

}
