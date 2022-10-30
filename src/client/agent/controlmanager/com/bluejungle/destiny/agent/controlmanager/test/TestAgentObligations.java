/*
 * Created on Dec 29, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager.test;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.IPolicyEvaluator;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.obligation.AgentDisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.AgentNotifyObligation;
import com.bluejungle.pf.domain.destiny.obligation.AgentObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.policy.PolicyManager;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.engine.destiny.EngineResourceInformation;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;
import com.bluejungle.pf.engine.destiny.IClientInformationManager;
import com.bluejungle.pf.engine.destiny.IContentAnalysisManager;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;
import com.bluejungle.pf.domain.epicenter.resource.IResourceManager;

/**
 * @author sasha
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/etc/eclipse/destiny-code-templates.xml#3 $:
 */

public class TestAgentObligations extends TestCase {

    private static final IDSubject USER = new Subject( "Joe", "joe@example.org", "Joe", new Long(1), SubjectType.USER);
    private static final IDSubject HOST = new Subject( "host", "host@example.domain", "host", new Long(2), SubjectType.HOST);
    private static final IDSubject APP = new Subject( "destiny", null, "destiny", new Long(3), SubjectType.APP);

    private IComponentManager manager;
    private IDObligationManager obligationManager;
    private AgentResourceManager rm;
    private IDActionManager actionManager;
    private PolicyManager policyManager;
    IPolicy po;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        manager = ComponentManagerFactory.getComponentManager();
        // force loading of command executor by comp_info
        obligationManager = (IDObligationManager) manager.getComponent(AgentObligationManager.COMP_INFO);

        policyManager = (PolicyManager) manager.getComponent(PolicyManager.class);
        actionManager = (IDActionManager) manager.getComponent(IDActionManager.COMP_INFO);

        manager.registerComponent(AgentResourceManager.COMP_INFO, true);
        rm = (AgentResourceManager)manager.getComponent(AgentResourceManager.COMP_INFO);
        po = policyManager.createPolicy( new Long(301), "Policy 1");

        // Initialize the policy evaluator
        HashMapConfiguration policyEvalConfig = new HashMapConfiguration();
        policyEvalConfig.setProperty(IPolicyEvaluator.AGENT_POLICY_CONTAINER_CONFIG_PARAM, manager.getComponent(IAgentPolicyContainer.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.CONTROL_MANAGER_CONFIG_PARAM, (IControlManager)manager.getComponent(IControlManager.NAME));
        policyEvalConfig.setProperty(IPolicyEvaluator.RESOURCE_MANAGER_CONFIG_PARAM, manager.getComponent(AgentResourceManager.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.SUBJECT_MANAGER_CONFIG_PARAM, manager.getComponent(IDSubjectManager.COMP_INFO));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestAgentObligations.
     *
     * @param arg0
     */
    public TestAgentObligations(String arg0) {
        super(arg0);
    }

    public void testLogObligation() {
        long ts = System.currentTimeMillis();
        String key = "key1";
        String value = "value1";
        IDObligation logObligation = obligationManager.createLogObligation();
        assertNotNull(logObligation);

        String fromName = "foo.txt";
        String toName = "c:\bar";

        logObligation.setPolicy(po);

        IResourceManager.ResourceInformation<EngineResourceInformation> fromInfo = rm.getResource(
            fromName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        IResourceManager.ResourceInformation<EngineResourceInformation> toInfo = rm.getResource(
            toName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        FromResourceInformation from = new FromResourceInformation(fromName, 23, 0, 0, "bob");
        ToResourceInformation to = new ToResourceInformation(toName);
        DynamicAttributes fromResAttrs = new DynamicAttributes();
        fromResAttrs.put(key, value);

        String hostIPAddr = "1.2.3.4";
        String action = "EDIT";
        PolicyActivityInfo req = new PolicyActivityInfo(from, to, USER.getName(), USER.getId().longValue(), HOST.getName(), hostIPAddr, HOST.getId().longValue(), APP.getName(), APP.getId().longValue(), ActionEnumType.getActionEnum(action), PolicyDecisionEnumType.getPolicyDecisionEnum(IDEffectType.ALLOW_NAME), 1234, ts, 0, fromResAttrs);
        EvaluationRequest engineReq = new EvaluationRequest(
            (long)1234
        ,   actionManager.getAction(action)
        ,   fromInfo.getResource()
        ,   fromInfo.getInformation()
        ,   toInfo.getResource()
        ,   toInfo.getInformation()
        ,   USER
        ,   USER.getName()
        ,   null
        ,   APP
        ,   null
        ,   HOST
        ,   hostIPAddr
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   System.currentTimeMillis()
        ,   0
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   null
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        EvaluationResult evalResult = new EvaluationResult(engineReq, "ALLOW");

        // It doesn't seem like we have a good way to test these...
    }

    public void testNotifyObligation() {
        String toAddress = "me@you.com, you@me.com";
        String body = "you've violated the policy of truth";
        String key = "key1";
        String value = "value1";

        IDObligation notifyObligation = obligationManager.createNotifyObligation(toAddress, body);
        notifyObligation.setPolicy(po);
        assertTrue("obligation must be of type AgentNotifyObligation", notifyObligation instanceof AgentNotifyObligation);
        String fromName = "foo.txt";
        String toName = "c:\bar";

        IResourceManager.ResourceInformation<EngineResourceInformation> fromInfo = rm.getResource(
            fromName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        IResourceManager.ResourceInformation<EngineResourceInformation> toInfo = rm.getResource(
            toName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        FromResourceInformation from = new FromResourceInformation(fromName, 23, 0, 0, "bob");
        ToResourceInformation to = new ToResourceInformation(toName);

        DynamicAttributes fromResAttrs = new DynamicAttributes();
        fromResAttrs.put(key, value);

        String hostIPAddr = "1.2.3.4";
        String action = "EDIT";

        PolicyActivityInfo req = new PolicyActivityInfo(from, to, USER.getName(), USER.getId().longValue(), HOST.getName(), hostIPAddr, HOST.getId().longValue(), APP.getName(), APP.getId().longValue(), ActionEnumType.getActionEnum(action), PolicyDecisionEnumType.getPolicyDecisionEnum(IDEffectType.ALLOW_NAME), 1234, System.currentTimeMillis(), 0, fromResAttrs);

        EvaluationRequest engineReq = new EvaluationRequest(
            (long)1234
        ,   actionManager.getAction(action)
        ,   fromInfo.getResource()
        ,   fromInfo.getInformation()
        ,   toInfo.getResource()
        ,   toInfo.getInformation()
        ,   USER
        ,   USER.getName()
        ,   null
        ,   APP
        ,   null
        ,   HOST
        ,   hostIPAddr
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   System.currentTimeMillis()
        ,   0
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   null
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        EvaluationResult evalResult = new EvaluationResult(engineReq, "ALLOW");

    }

    public void testDisplayObligation() {
        String key = "key1";
        String value = "value1";
        IDObligation displayObligation = obligationManager.createDisplayObligation("hello");
        assertNotNull(displayObligation);
        assertTrue(displayObligation instanceof AgentDisplayObligation);
        assertEquals("hello", ((AgentDisplayObligation)displayObligation).getMessage());

        String fromName = "foo.txt";
        String toName = "c:\bar";

       IResourceManager.ResourceInformation<EngineResourceInformation> fromInfo = rm.getResource(
            fromName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        IResourceManager.ResourceInformation<EngineResourceInformation> toInfo = rm.getResource(
            toName
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        FromResourceInformation from = new FromResourceInformation(fromName, 23, 0, 0, "bob");
        ToResourceInformation to = new ToResourceInformation(toName);

        DynamicAttributes fromResAttrs = new DynamicAttributes();
        fromResAttrs.put(key, value);

        String hostIPAddr = "1.2.3.4";
        String action = "EDIT";

        PolicyActivityInfo req = new PolicyActivityInfo(from, to, USER.getName(), USER.getId().longValue(), HOST.getName(), hostIPAddr, HOST.getId().longValue(), APP.getName(), APP.getId().longValue(), ActionEnumType.getActionEnum(action), PolicyDecisionEnumType.getPolicyDecisionEnum(IDEffectType.ALLOW_NAME), 1234, System.currentTimeMillis(), 0, fromResAttrs);
        EvaluationRequest engineReq = new EvaluationRequest(
            (long)1234
        ,   actionManager.getAction(action)
        ,   fromInfo.getResource()
        ,   fromInfo.getInformation()
        ,   toInfo.getResource()
        ,   toInfo.getInformation()
        ,   USER
        ,   USER.getName()
        ,   null
        ,   APP
        ,   null
        ,   HOST
        ,   hostIPAddr
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   System.currentTimeMillis()
        ,   0
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   null
        ,   IClientInformationManager.DEFAULT
        ,   IServiceProviderManager.DEFAULT
        );
        EvaluationResult evalResult = new EvaluationResult(engineReq, "ALLOW");

        // TODO
    }
}
