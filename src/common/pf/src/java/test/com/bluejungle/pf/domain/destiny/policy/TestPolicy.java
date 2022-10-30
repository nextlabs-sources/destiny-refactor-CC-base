/*
 * Created on Dec 23, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.policy;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.domain.destiny.action.ActionManager;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.Target;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.evaluation.IEvaluatableNode;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/policy/TestPolicy.java#1 $:
 */

public class TestPolicy extends TestCase {

    private final static String POLICY1 = "Policy 1";

    private IComponentManager manager;
    private PolicyManager policyMgr;
    private ActionManager actionMgr;
    private IDSpecManager sm;
    private IPolicy policy;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestPolicy.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        manager = ComponentManagerFactory.getComponentManager();
        policyMgr = (PolicyManager) manager.getComponent(PolicyManager.class);
        policy = policyMgr.newPolicy( new Long( 100 ), POLICY1);
        actionMgr = (ActionManager) manager.getComponent(ActionManager.class);
        sm = (IDSpecManager) manager.getComponent(IDSpecManager.COMP_INFO);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestPolicy.
     * @param arg0
     */
    public TestPolicy(String arg0) {
        super(arg0);
    }

    public final void testPolicy() {
        assertNotNull(policy);
        assertEquals(POLICY1, policy.getName());
    }

    public final void testGetChildren() {
        IEvaluatableNode[] children = policy.getChildren();
        assertNotNull(children);
        assertEquals(0, children.length);
    }

    /*
     * Class under test for String toString()
     */
    public final void testToString() {
        String pql = policy.toString();
        assertNotNull(pql);
        assertFalse(pql.length() == 0);
    }

    public final void testIsLeafNode() {
        assertTrue(policy.isLeafNode());
    }

    public final void testTarget() {
        IPredicate resource = sm.getSpecReference("foo");
        IAction action = actionMgr.getAction(IDAction.CREATE_NEW_NAME);
        IDSubjectSpec subject = IDSubjectSpec.ALL_USERS;
        ITarget target = Target.forFileAction(resource, action, subject);
        policy.setTarget(target);
        assertSame(target, policy.getTarget());

    }

    public final void testObligations() {
        IDObligationManager obManager =
            (IDObligationManager) manager.getComponent(DObligationManager.COMP_INFO);

        IObligation ob1 = obManager.createLogObligation();
        policy.addObligation(ob1, EffectType.ALLOW);
        IObligation obs[] = policy.getObligationArray(EffectType.ALLOW);
        assertNotNull(obs);
        assertEquals(1, obs.length);
        assertSame(ob1, obs[0]);

        IObligation ob2 = obManager.createLogObligation();
        policy.addObligation(ob2, EffectType.DENY);
        obs = policy.getObligationArray(EffectType.DENY);
        assertNotNull(obs);
        assertEquals(1, obs.length);
        assertSame(ob2, obs[0]);

        policy.deleteObligation(ob1, EffectType.ALLOW);
        obs = policy.getObligationArray(EffectType.ALLOW);
        assertNotNull(obs);
        assertEquals(0, obs.length);

        obs = policy.getObligationArray(EffectType.ALLOW);
        assertNotNull(obs);
        assertEquals(0, obs.length);

    }

    public final void testAttributes() {
        assertNotNull(policy.getAttributes());
        assertEquals(0, policy.getAttributes().size());
        assertFalse(policy.hasAttribute("tracking"));
        policy.setAttribute("tracking", true);
        assertTrue(policy.hasAttribute("tracking"));
        assertTrue(policy.hasAttribute("TrAcking"));
        assertFalse(policy.hasAttribute("Tracking123"));
        assertEquals(1, policy.getAttributes().size());
        policy.setAttribute("tracking", false);
        assertFalse(policy.hasAttribute("tracking"));
        assertNotNull(policy.getAttributes());
        assertEquals(0, policy.getAttributes().size());
        policy.setAttribute("one", true);
        policy.setAttribute("two", true);
        policy.setAttribute("three", true);
        policy.setAttribute("one", true);
        policy.setAttribute("two", true);
        policy.setAttribute("three", true);
        assertEquals(3, policy.getAttributes().size());
        String prev = "";
        for (String s : policy.getAttributes()) {
            assertTrue(prev.compareTo(s) < 0);
            prev = s;
        }
    }

    public final void testMainEffect() {
        policy.setMainEffect(EffectType.ALLOW);
        assertSame(EffectType.ALLOW, policy.getMainEffect());
    }

    public final void testOtherwiseEffect() {
        assertFalse(policy.hasOtherwise());

        policy.setOtherwiseEffect(EffectType.ALLOW);
        assertSame(EffectType.ALLOW, policy.getOtherwiseEffect());

        policy.setOtherwiseEffect(EffectType.DENY);
        assertSame(EffectType.DENY, policy.getOtherwiseEffect());

        assertTrue(policy.hasOtherwise());

        policy.removeOtherwise();
        assertFalse(policy.hasOtherwise());
    }

}
