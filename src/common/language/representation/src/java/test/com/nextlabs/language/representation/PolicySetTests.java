package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/PolicySetTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for Policy Sets.
 *
 * @author Sergey Kalinichenko
 */
public class PolicySetTests {

    private static final Path TEST_PATH1 = new Path(
        "policy", "set", "tests", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "policy", "set", "tests", "two"
    );

    private static final IReference<IPolicyType> REF1T =
        IReferenceFactory.DEFAULT.create(TEST_PATH1, IPolicyType.class);

    private static final IReference<IPolicyType> REF2T =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IPolicyType.class);

    private static final IReference<IPolicy> REF1P =
        IReferenceFactory.DEFAULT.create(TEST_PATH1, IPolicy.class);

    private static final IReference<IPolicy> REF2P =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IPolicy.class);

    private PolicySet p1;

    private IExpression ALL_TRUE =
        new UnaryExpression(UnaryOperator.ALL, IExpression.TRUE);

    private IExpression ALL_FALSE =
        new UnaryExpression(UnaryOperator.ALL, IExpression.FALSE);

    @Before
    public void prepare() {
        p1 = new PolicySet(TEST_PATH1);
    }

    @Test
    public void overridingOutcome() {
        assertEquals(Outcome.DENY, p1.getOverridingOutcome());
        p1.setOverridingOutcome(Outcome.ALLOW);
        assertEquals(Outcome.ALLOW, p1.getOverridingOutcome());
    }

    @Test(expected=NullPointerException.class)
    public void nullOverridingOutcome() {
        p1.setOverridingOutcome(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidOverridingOutcome() {
        p1.setOverridingOutcome(Outcome.NOTHING);
    }

    @Test
    public void applyTarget() {
        assertFalse(p1.hasApplyTarget());
        p1.getApplyTarget().addContext("a", ALL_FALSE);
        assertTrue(p1.hasApplyTarget());
    }

    @Test
    public void ignoreTarget() {
        assertFalse(p1.hasIgnoreTarget());
        p1.getIgnoreTarget().addContext("a", ALL_FALSE);
        assertTrue(p1.hasIgnoreTarget());
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        p1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitPolicySet(IPolicySet p) {
                assertSame(p1, p);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void addAllowedType() {
        assertEquals(0, p1.getAllowedTypeCount());
        p1.addAllowedType(REF1T);
        assertEquals(1, p1.getAllowedTypeCount());
        p1.addAllowedType(REF1T);
        assertEquals(1, p1.getAllowedTypeCount());
        p1.addAllowedType(REF2T);
        assertEquals(2, p1.getAllowedTypeCount());
        Iterable<IReference<IPolicyType>> at = p1.getAllowedTypes();
        assertNotNull(at);
        Iterator<IReference<IPolicyType>> ai = at.iterator();
        assertNotNull(ai);
        assertTrue(ai.hasNext());
        assertEquals(REF1T, ai.next());
        assertTrue(ai.hasNext());
        assertEquals(REF2T, ai.next());
        assertFalse(ai.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addAllowedTypeNull() {
        p1.addAllowedType(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addAllowedTypes() {
        p1.addAllowedTypes(
            Arrays.asList(REF1T, REF2T, REF2T, REF1T)
        );
        Iterable<IReference<IPolicyType>> at = p1.getAllowedTypes();
        assertNotNull(at);
        Iterator<IReference<IPolicyType>> ai = at.iterator();
        assertNotNull(ai);
        assertTrue(ai.hasNext());
        assertEquals(REF1T, ai.next());
        assertTrue(ai.hasNext());
        assertEquals(REF2T, ai.next());
        assertFalse(ai.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removeAllowedTypes() {
        p1.addAllowedTypes(
            Arrays.asList(REF1T, REF2T)
        );
        assertEquals(2, p1.getAllowedTypeCount());
        p1.removeAllowedType(REF1T);
        assertEquals(1, p1.getAllowedTypeCount());
        Iterable<IReference<IPolicyType>> at = p1.getAllowedTypes();
        assertNotNull(at);
        Iterator<IReference<IPolicyType>> ai = at.iterator();
        assertNotNull(ai);
        assertTrue(ai.hasNext());
        assertEquals(REF2T, ai.next());
        assertFalse(ai.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addAllowedTypesNull() {
        p1.addAllowedTypes(null);
    }

    @Test
    public void addPolicy() {
        assertEquals(0, p1.getPolicyCount());
        p1.addPolicy(REF1P);
        assertEquals(1, p1.getPolicyCount());
        p1.addPolicy(REF1P);
        assertEquals(1, p1.getPolicyCount());
        p1.addPolicy(REF2P);
        assertEquals(2, p1.getPolicyCount());
        Iterable<IReference<IPolicy>> p = p1.getPolicies();
        assertNotNull(p);
        Iterator<IReference<IPolicy>> pi = p.iterator();
        assertNotNull(pi);
        assertTrue(pi.hasNext());
        assertEquals(REF1T, pi.next());
        assertTrue(pi.hasNext());
        assertEquals(REF2T, pi.next());
        assertFalse(pi.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addPolicyNull() {
        p1.addPolicy(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addPolicies() {
        p1.addPolicies(
            Arrays.asList(REF1P, REF2P, REF2P, REF1P)
        );
        p1.addPolicy(REF1P);
        p1.addPolicy(REF2P);
        Iterable<IReference<IPolicy>> p = p1.getPolicies();
        assertNotNull(p);
        Iterator<IReference<IPolicy>> pi = p.iterator();
        assertNotNull(pi);
        assertTrue(pi.hasNext());
        assertEquals(REF1T, pi.next());
        assertTrue(pi.hasNext());
        assertEquals(REF2T, pi.next());
        assertFalse(pi.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removePolicies() {
        p1.addPolicies(
            Arrays.asList(REF1P, REF2P)
        );
        assertEquals(2, p1.getPolicyCount());
        p1.removePolicy(REF2P);
        assertEquals(1, p1.getPolicyCount());
        Iterable<IReference<IPolicy>> p = p1.getPolicies();
        assertNotNull(p);
        Iterator<IReference<IPolicy>> pi = p.iterator();
        assertNotNull(pi);
        assertTrue(pi.hasNext());
        assertEquals(REF1T, pi.next());
        assertFalse(pi.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addPoliciesNull() {
        p1.addPolicies(null);
    }

    @Test
    public void hashCodeWorks() {
        PolicySet pA = new PolicySet(TEST_PATH1);
        assertTrue(pA.hashCode() == p1.hashCode());
        PolicySet pB = new PolicySet(TEST_PATH2);
        assertTrue(pB.hashCode() != p1.hashCode());
    }


    @Test
    public void inequalityToNull() {
        assertFalse(p1.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(p1.equals("p1"));
    }

    @Test
    public void inequalityToDifferentPath() {
        PolicySet p = new PolicySet(TEST_PATH2);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentOverridingOutcome() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.setOverridingOutcome(Outcome.ALLOW);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentAllowTarget() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.getApplyTarget().addContext("a", ALL_TRUE);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentIgnoreTarget() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.getIgnoreTarget().addContext("a", ALL_TRUE);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentAllowType() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.addAllowedType(REF1T);
        p1.addAllowedType(REF2T);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentAllowTypeCount() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.addAllowedType(REF1T);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentPolicies() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.addPolicy(REF1P);
        p1.addPolicy(REF2P);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void inequalityDifferentPolicyCounts() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p.addPolicy(REF1P);
        assertFalse(p.equals(p1));
        assertFalse(p1.equals(p));
    }

    @Test
    public void equality() {
        PolicySet p = new PolicySet(TEST_PATH1);
        p1.addAllowedType(REF1T);
        p1.addAllowedType(REF2T);
        p.addAllowedType(REF2T);
        p.addAllowedType(REF1T);
        p1.addPolicy(REF1P);
        p1.addPolicy(REF2P);
        p.addPolicy(REF2P);
        p.addPolicy(REF1P);
        p1.getIgnoreTarget().addContext("a", ALL_TRUE);
        p.getIgnoreTarget().addContext("a", ALL_TRUE);
        p1.getApplyTarget().addContext("a", ALL_FALSE);
        p.getApplyTarget().addContext("a", ALL_FALSE);
        assertEquals(p, p1);
        assertEquals(p1, p);
    }

    @Test
    public void toStringWorks() {
        p1.addAllowedType(REF1T);
        p1.addAllowedType(REF2T);
        p1.addPolicy(REF1P);
        p1.addPolicy(REF2P);
        p1.getIgnoreTarget().addContext("a", ALL_TRUE);
        p1.getApplyTarget().addContext("a", ALL_FALSE);
        assertEquals(
            p1.toString()
        ,   "policy set "+TEST_PATH1+" of "+REF1T+", "+REF2T
        +   "\napply when\n    a ALL false"
        +   "\nignore when\n    a ALL true\nDENY overrides ( "
        +   REF1T+", "+REF2T+" )"
        );
    }

}
