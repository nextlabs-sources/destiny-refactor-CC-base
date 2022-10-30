package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author pkeni, sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/engine/destiny/AgentPolicyAssemblyTest.java#1 $
 */

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.obligation.CommandExecutorHelper;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.TestUser;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.subject.AgentSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.resource.IResourceManager;

/**
 * These tests verify that the policy assembly behaves correctly.
 */
public class AgentPolicyAssemblyTest extends TestCase {
    private static final String BASE_DIR_PROPERTY_NAME = "build.root.dir";

    private IComponentManager cm;
    private AgentPolicyContainer container;
    private AgentPolicyAssembly assembly;
    private IDSubjectManager sm;
    private AgentResourceManager rm;

    public static final int comment = 1;
    private boolean validBundle = false;
    private Log log;

    public static void main(String[] args) {
        TestRunner.run(AgentPolicyAssemblyTest.class);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        cm = ComponentManagerFactory.getComponentManager();
        log = LogFactory.getLog(AgentPolicyAssemblyTest.class.getName());

        String baseDir = System.getProperty(BASE_DIR_PROPERTY_NAME);
        if (baseDir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY_NAME + " system property must be specified for unit test to function");
        }

        ComponentInfo info = new ComponentInfo(ICommandExecutor.NAME, CommandExecutorHelper.class.getName(), ICommandExecutor.class.getName(), LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(info, true);

        cm.registerComponent(AgentResourceManager.COMP_INFO, true);
        rm = (AgentResourceManager) cm.getComponent(AgentResourceManager.COMP_INFO);

        sm = (IDSubjectManager)cm.getComponent(AgentSubjectManager.COMP_INFO);
        ComponentInfo bundleVaultInfo = new ComponentInfo(IBundleVault.COMPONENT_NAME, UnsecuredBundleVaultImpl.class.getName(), IBundleVault.class.getName(), LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(bundleVaultInfo, true);

        container = (AgentPolicyContainer) cm.getComponent(IAgentPolicyContainer.COMP_INFO);

        this.container.loadBundle();
        assembly = container.getAssembly();
        validBundle = true;
    }

    public void testGetApplicablePolicies() {
        assertTrue(validBundle);
        long ts = System.currentTimeMillis();
        log.debug("Start testGetApplicablePolicies");

        IResourceManager.ResourceInformation<EngineResourceInformation> fromInfo = rm.getResource(
            "C:\\from.java"
        ,   null
        ,   AgentTypeEnumType.DESKTOP
        );
        assertNotNull(fromInfo);
        assertNotNull(fromInfo.getResource());
        assertNotNull(fromInfo.getInformation());

        IDSubject user = sm.getSubject(TestUser.BMENG.getSID(), SubjectType.USER);
        assertNotNull(user);
        String userName = "pkeni@bluejungle.com";

        IDSubject host = sm.getSubject("savaii.bluejungle.com", SubjectType.HOST);
        assertNotNull(host);

        IDSubject application = sm.getSubject("", SubjectType.APP, attr("name", "c:\\small.exe"));
        assertNotNull(application);

        IDActionManager actionManager;
        actionManager = (IDActionManager) cm.getComponent(IDActionManager.COMP_INFO);
        IDAction action = actionManager.getAction(IDAction.CUT_PASTE_NAME);
        assertNotNull(action);

        final String hostIPAddress = "10.17.11.130";
        EvaluationRequest er = new EvaluationRequest(
            (long)1234
        ,   action
        ,   fromInfo.getResource()
        ,   fromInfo.getInformation()
        ,   null
        ,   new EngineResourceInformation()
        ,   user
        ,   userName
        ,   null
        ,   application
        ,   null
        ,   host
        ,   hostIPAddress
        ,   null
        ,   null
        ,   (long)0
        ,   false
        ,   ts
        ,   0
        ,   null
        ,   false
        ,   IContentAnalysisManager.DEFAULT
        ,   assembly
        ,   IClientInformationManager.DEFAULT
        ,   null
        );

        log.debug("Get applicable policies: pkeni, *.java, ");
        IDPolicy[] policies = assembly.getPolicies();
        BitSet applicables = assembly.getApplicablePolicies(er);
        if (applicables != null) {
            log.debug("Subject : " + user);
            for (int i = applicables.nextSetBit(0); i >= 0; i = applicables.nextSetBit(i + 1)) {
                log.debug(policies[i]);
            }
        }
        DynamicAttributes fromDA = new DynamicAttributes();
        fromDA.put(SpecAttribute.ID_ATTR_NAME, new String[] {"file:///C:/from.java"});
        fromDA.put(SpecAttribute.NAME_ATTR_NAME, new String[] {"file:///C:/from.java"});
        fromDA.put(SpecAttribute.DESTINYTYPE_ATTR_NAME, "fso");
        Map<String,DynamicAttributes> context = new HashMap<String,DynamicAttributes>();
        context.put("user", attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com"));
        context.put("host", attr("name", "savaii.bluejungle.com", "inet_address", hostIPAddress));
        context.put("application", attr("name", "c:\\small.exe", "fingerprint", TestUser.SMALL_APP_FP));
        context.put("from", fromDA);
        context.put("action", attr("name", IDAction.COPY_PASTE_NAME));
        EvaluationResult result = container.evaluate(
            12345
        ,   AgentTypeEnumType.DESKTOP
        ,   null
        ,   context
        ,   ts
        ,   0
        ,   true);

        log.debug("Result : ");
        if (result.getEffectName() == EvaluationResult.ALLOW) {
            log.debug("ALLOW");
        } else if (result.getEffectName() == EvaluationResult.DENY) {
            log.debug("DENY");
        } else {
            log.debug("NON-ALLOW/DENY");
        }

        user = new Subject(
            TestUser.BMENG.getSID()
        ,   "fuad@bluejungle.com"
        ,   TestUser.BMENG.getSID()
        ,   new Long(2624)
        ,   SubjectType.USER
        );
        er.setUser(user);

        log.debug("Get applicable policies: fuad, *.java, ");
        applicables = assembly.getApplicablePolicies(er);
        if (applicables != null) {
            log.debug("Subject : " + user);
            for (int i = applicables.nextSetBit(0); i >= 0; i = applicables.nextSetBit(i + 1)) {
                log.debug(policies[i]);
            }
        }
        context.put("user", attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com"));
        result = container.evaluate(
            12346
        ,   AgentTypeEnumType.DESKTOP
        ,   null
        ,   context
        ,   ts
        ,   0
        ,   true);
        log.debug("Result : ");
        if (result.getEffectName() == EvaluationResult.ALLOW) {
            log.debug("ALLOW");
        } else if (result.getEffectName() == EvaluationResult.DENY) {
            log.debug("DENY");
        } else {
            log.debug("NON-ALLOW/DENY");
        }
        log.debug("End testGetApplicablePolicies");
    }

    /*
     * Class under test for IDSubject getSubject(String)
     */
    public void testGetSubjectString() {
        if (!validBundle) {
            return;
        }
        log.debug("Start testGetSubjectString");
        log.debug(sm.getSubject(TestUser.BMENG.getSID(), SubjectType.USER));
        log.debug(sm.getSubject("S-1-5-21-668023798-3031861066-1043980994-2625", SubjectType.USER));
        log.debug("End testGetSubjectString");
    }

    public void testGetTimestamp() {
        assertNotNull(assembly.getTimestamp());
    }

    private static DynamicAttributes attr(String ... args) {
        if (args == null) {
            throw new NullPointerException("args");
        }
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("args");
        }
        DynamicAttributes res = new DynamicAttributes();
        for ( int i = 0 ; i < args.length ; i += 2 ) {
            res.add(args[i], args[i+1]);
        }
        return res;
    }

}
