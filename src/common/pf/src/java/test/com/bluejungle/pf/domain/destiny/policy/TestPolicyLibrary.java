/*
 * Created on Jan 27, 205
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/policy/TestPolicyLibrary.java#1 $:
 */
package com.bluejungle.pf.domain.destiny.policy;

import java.util.ArrayList;
import java.util.BitSet;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.action.IDActionSpec;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.Target;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * TestPolicyLibrary is a collection of policies that is useful for various tests across the board. Add policies to it as needed, but make sure all the unit tests succeed after you add a policy. Never remove policies.
 */

public class TestPolicyLibrary {
    private final String INAPPLICABLE_POLICY = "NOT_APPLICABLE";

    private final IComponentManager manager = ComponentManagerFactory.getComponentManager();
    private final IDPolicyManager policyManager = (IDPolicyManager) manager.getComponent(IDPolicyManager.COMP_INFO);
    private final IDObligationManager obManager = (IDObligationManager) manager.getComponent(DObligationManager.COMP_INFO);
    private final IDActionManager actionManager = (IDActionManager) manager.getComponent(IDActionManager.COMP_INFO);

    private final ArrayList<IDPolicy> policies;

    public TestPolicyLibrary() {
        policies = new ArrayList<IDPolicy>();

        try {
            addFuadDenyPolicy();
            addTrackingPolicy();
            addFuadAllowPolicy();
            addWherePolicies();
            addInetAddressPolicy();
            addPolicyWithExceptions();
            addEvalPathPolicies();
        } catch (PQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void addInetAddressPolicy() throws PQLException {
        final DomainObjectBuilder dob = new DomainObjectBuilder("policy localhost_policy for resource.name = \"c:\\\\inet_address.pql\" on * by host.inet_address = \"127.0.0.1/32\" do deny");
        policies.add(dob.processPolicy());
    }

    private void addFuadAllowPolicy() throws PQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("policy AllowPolicyOnFuad\n");
        sb.append("description \"Policy that allows Alan or HorKan access to *.java via notepad and denies it to everyone else.\"\n");
        sb.append("for resource.fso.name = \"\\\\**.java\" OR resource.fso.name = \"**.java\"\n");
        sb.append("on *\n");
        sb.append("by ((user.name = \"" + TestUser.HCHAN.getSID() + "\" OR ");
        sb.append("      user.name = \"" + TestUser.AMORGAN.getSID() + "\") AND ");
        sb.append("      application.name = \"notepad.exe\")\n");
        sb.append("do allow\n");
        sb.append("by default do deny\n");
        sb.append("on allow do LOG\n");
        
        DomainObjectBuilder dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
    }

    private void addFuadDenyPolicy() throws PQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("policy DenyPolicyOnFuad\n");
        sb.append("description \"Policy that does not allow Fuad to do anything to financial spreadsheets.\"\n");
        sb.append("for resource.fso.name = \"C:\\\\**finance**\\\\*.txt\"\n");
        sb.append("on (DELETE OR EDIT OR CREATE_NEW)\n");
        sb.append("by user.name = \"" + TestUser.HCHAN.getSID() + "\"\n");
        sb.append("do deny\n");

        DomainObjectBuilder dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
    }

    private void addWherePolicies() throws PQLException {
        DomainObjectBuilder dob = new DomainObjectBuilder("policy where_policy for resource.name=\"c:\\\\where.pql\" on * by * where current_time.year > 1945 do allow");
        policies.add(dob.processPolicy());
        
        dob = new DomainObjectBuilder("policy where_policy for resource.name=\"c:\\\\nowhere.pql\" on * by * where current_time.year >= 3000 do deny");        
        policies.add(dob.processPolicy());        
    }
    
    private void addTrackingPolicy() {
        final IDPolicy policy = policyManager.newPolicy( new Long( 103 ), "TrackingPolicy");
        policy.setDescription("Applies to everyone, doing anything, on any document -- tracking policy.");
        policies.add(policy);
        final IPredicate resource = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "**" );
        final Target target = new Target();
        target.setFromResourcePred(resource);
        target.setActionPred(IDActionSpec.ALL_ACTIONS);
        target.setSubjectPred(IDSubjectSpec.ALL_SUBJECTS);
        policy.setTarget(target);
        policy.setMainEffect(EffectType.DONT_CARE);

        final IDObligation logObligation = obManager.createLogObligation();
        policy.addObligation(logObligation, EffectType.DONT_CARE);
    }

    private void addPolicyWithExceptions() throws PQLException {
        StringBuilder sb = new StringBuilder();
        
        sb.append("policy itar_with_exceptions\n");
        sb.append("for resource.fso.itar=\"yes\"\n");
        sb.append("on *\n");
        sb.append("by *\n");
        sb.append("subpolicy allow_overrides \"exception_1_amorgan\", \"exception_2_not_applicable\", \"exception_3_delete_doc\"\n");
        sb.append("do deny\n");
        sb.append("by default do allow");
        
        DomainObjectBuilder dob = new DomainObjectBuilder(sb.toString());
        
        policies.add(dob.processPolicy());
        
        sb = new StringBuilder();
        sb.append("policy exception_1_amorgan\n");
        sb.append("description \"amorgan can read ITAR documents\"\n");
        sb.append("attribute " + IDPolicy.EXCEPTION_ATTRIBUTE + "\n");
        sb.append("for *\n"); 
        sb.append("on *\n");
        sb.append("by user.name = \"" + TestUser.AMORGAN.getSID() + "\"\n");
        sb.append("do allow");
        
        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
        
        
        sb = new StringBuilder();
        sb.append("policy exception_2_not_applicable\n");
        sb.append("description \"this policy is not applicable\"\n");
        sb.append("attribute " + IDPolicy.EXCEPTION_ATTRIBUTE + "\n");
        sb.append("attribute " + INAPPLICABLE_POLICY + "\n");
        sb.append("for resource.fso.name=\"**.doc\"\n"); 
        sb.append("on *\n");
        sb.append("by FALSE\n");
        sb.append("do allow");
        
        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
        
        
        sb = new StringBuilder();
        sb.append("policy exception_3_delete_doc\n");
        sb.append("description \"anyone can delete doc files\"\n");
        sb.append("attribute " + IDPolicy.EXCEPTION_ATTRIBUTE + "\n");
        sb.append("for resource.fso.name=\"**.doc\"\n"); 
        sb.append("on DELETE\n");
        sb.append("by *\n");
        sb.append("do allow");
        
        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
    }

    // Policies specifically designed for testing evaluation path (resource matches,
    // and subject doesn't. That sort of thing)
    private void addEvalPathPolicies() throws PQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("policy allow_only\n");
        sb.append("description \"allow only policy\"\n");
        sb.append("for resource.fso.for_allow_only = \"yes\"\n");
        sb.append("on BLOOP\n");
        sb.append("by user.name = \"" + TestUser.HCHAN.getSID() + "\"\n");
        sb.append("where resource.fso.cond_attr = \"yes\"\n");
        sb.append("do allow\n");
        sb.append("by default do deny\n");

        DomainObjectBuilder dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());

        sb = new StringBuilder();
        sb.append("policy true_allow\n");
        sb.append("attribute " + IDPolicy.TRUE_ALLOW_ATTRIBUTE + "\n");
        sb.append("description \"true allow policy\"\n");
        sb.append("for resource.fso.for_true_allow = \"yes\"\n");
        sb.append("on BLOOP\n");
        sb.append("by user.name = \"" + TestUser.HCHAN.getSID() + "\"\n");
        sb.append("where resource.fso.cond_attr = \"yes\"\n");
        sb.append("do allow\n");
        sb.append("by default do deny\n");

        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
        
        sb = new StringBuilder();
        sb.append("policy regular_allow\n");
        sb.append("description \"regular allow policy\"\n");
        sb.append("for resource.fso.for_allow = \"yes\"\n");
        sb.append("on BLOOP\n");
        sb.append("by user.name = \"" + TestUser.HCHAN.getSID() + "\"\n");
        sb.append("where resource.fso.cond_attr = \"yes\"\n");
        sb.append("do allow\n");

        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
        
        sb = new StringBuilder();
        sb.append("policy regular_deny\n");
        sb.append("description \"regular deny policy\"\n");
        sb.append("for resource.fso.for_deny = \"yes\"\n");
        sb.append("on BLOOP\n");
        sb.append("by user.name = \"" + TestUser.HCHAN.getSID() + "\"\n");
        sb.append("where resource.fso.cond_attr = \"yes\"\n");
        sb.append("do deny\n");
        sb.append("by default do allow\n");

        dob = new DomainObjectBuilder(sb.toString());
        policies.add(dob.processPolicy());
    }
    
    public IDPolicy[] getPolicies() {
        IDPolicy[] rv = new IDPolicy[policies.size()];
        policies.toArray(rv);
        return rv;
    }

    public static String getPQLPolicies() {
        StringBuilder sb = new StringBuilder();

        sb.append("policy pod_policy_txt for resource.name = \"c:\\\\pod\\\\*.txt\" on * by * do deny\n");
        sb.append("policy pod_policy_pdf for resource.name = \"c:\\\\pod\\\\*.pdf\" on * by * do deny\n");

        return sb.toString();
    }

    public BitSet getApplicables() {
        BitSet applicables = new BitSet(policies.size());

        int i = 0;
        for (IDPolicy policy : policies) {
            if (!policy.hasAttribute(INAPPLICABLE_POLICY)) {
                applicables.set(i, true);
            }
            i++;
        }

        return applicables;
    }
}
