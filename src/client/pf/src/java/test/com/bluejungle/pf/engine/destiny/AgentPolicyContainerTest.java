package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author pkeni, sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/engine/destiny/AgentPolicyContainerTest.java#1 $
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.domain.destiny.obligation.CommandExecutorHelper;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.policy.TestUser;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.nextlabs.domain.log.PolicyActivityInfoV5;

/**
 * !!! IMPORTANT !!! IMPORTANT !!! IMPORTANT !!! IMPORTANT !!! IMPORTANT !!! 
 *
 * These tests expect a bundle created as part of running the deployment tests.
 * See PolicyDeploymentImplTest.PolicyDeploymentImplTest for details.
 * The policies and components deployed for the bundle are located in
 * <main>/test_files/com/bluejungle/pf/engine/destiny/policies.
 * Bundle printout on which these tests are based is available at
 * <main>/test_files/com/bluejungle/pf/destiny/policymap/test_approval,
 * in the file called testAgentBundle.approved.
 */
public class AgentPolicyContainerTest extends TestCase {
    private static final String BASE_DIR_PROPERTY_NAME = "build.root.dir";

    private AgentPolicyContainer container;
    private IOSWrapper osWrapper;
    private CommandExecutorHelper ce;

    private static final String SMALL_EXE = "c:\\small.exe";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AgentPolicyContainerTest.class);
    }

    private static final String[] dirNames = new String[] {
        "C:\\financepa\\"
    ,   "C:\\finance\\"
    ,   "C:\\e"
    ,   "C:\\e\\f"
    ,   "C:\\e\\f\\g\\"
    };

    private static final String[] fileNames = new String[] {
        "c:\\foo.ldap"
    ,   "C:\\financepa\\foo.txt"
    ,   "C:\\fubar.java"
    ,   "C:\\foo.java"
    ,   "C:\\finance\\foo.txt"
    ,   "c:\\e\\f\\g\\me.you"
    ,   "c:\\foofoo.ldap"
    ,   "c:\\foofoo.doc"
    ,   "c:\\subref.test"
    ,   "c:\\owner.test"
    ,   "c:\\owner_destiny.test"
    ,   "c:\\owner_ldap.test"
    ,   "c:\\negated_ldap_group.test"
    ,   "c:\\verify.obligations"
    };

    public AgentPolicyContainerTest() throws InvalidBundleException {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        String baseDir = System.getProperty(BASE_DIR_PROPERTY_NAME);
        if (baseDir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY_NAME + " system property must be specified for unit test to function");
        }
        
        ce = (CommandExecutorHelper)manager.getComponent(CommandExecutorHelper.class);
        osWrapper = (IOSWrapper) manager.getComponent(OSWrapper.class);
        
        ComponentInfo bundleVaultInfo = new ComponentInfo(IBundleVault.COMPONENT_NAME, UnsecuredBundleVaultImpl.class.getName(), IBundleVault.class.getName(), LifestyleType.SINGLETON_TYPE);
        manager.registerComponent(bundleVaultInfo, true);
        
        container = (AgentPolicyContainer) manager.getComponent(IAgentPolicyContainer.COMP_INFO);
        container.loadBundle();
        container.setClientInformationManager(new ClientInformationManager(
            new Date()
        ,   new String[0]
        ,   new String[] {
            "goldman"
        ,   "merrill"
        ,   "morgan"
        ,   "broker"
        }
        ,   new String[] {
            "Goldman Sachs"
        ,   "Merrill Lynch"
        ,   "Morgan Stanley"
        ,   "Broker"
        }
        ,   new String[] {
            "Goldman Sachs, Inc."
        ,   "Merrill Lynch Company"
        ,   "Morgan Stanley Financial Services"
        ,   "Multi-client Broker"
        }
        ,   new String[][] {
                new String[] {"*@gs.com", "agent@gmail.com"}
            ,   new String[] {"*@ml.com", "agent@gmail.com"}
            ,   new String[] {"*@ms.com", "agent@gmail.com"}
            ,   new String[] {"broker@ms.com", "broker@ml.com", "broker@gs.com", "broker@gmail.com"}
        }
        ,   new String[][] {
                new String[] {}
            ,   new String[] {}
            ,   new String[] {}
            ,   new String[] {}
        }
        ));
    }

    public void testUpdate() throws FileNotFoundException, PQLException, IOException, ClassNotFoundException, InvalidBundleException {
        InputStream inStream = null;
        File bundleFile = new File("bogus");

        if (bundleFile.exists()) {
            DeploymentBundleSignatureEnvelope bundleDTO;
            inStream = new BufferedInputStream(new FileInputStream(bundleFile));
            ObjectInputStream inObjectStream = new ObjectInputStream(inStream);
            bundleDTO = (DeploymentBundleSignatureEnvelope) inObjectStream.readObject();
            assertNotNull(bundleDTO);
            container.update(bundleDTO);
            assertNotNull(((AgentPolicyContainer)container).getAssembly());
            //System.err.println(bundle);
        }
    }

    public void testPrepareFiles() throws IOException {
        for (String dirName : dirNames) {
            new File(dirName).mkdir();
        }
        for (String fileName : fileNames) {
            new File(fileName).createNewFile();
        }
    }

    public void testLdapGroupMembership1() {
        // Hor-Kan should be allowed the action based on his membership in LDAP group
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\foo.ldap")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testLdapGroupMembership2() {
        // Bernard should be denied the action because he's not a member of the LDAP group
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\foo.ldap")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.ISUNDIUS.getSID(), "name", "bernard@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName());
    }

    public void testPolicyAuthorPolicy1() {
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\financepa\\foo.txt")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName());
    }

    public void testPolicyAuthorPolicy2() {
        EvaluationResult result = callEvaluate(
                canonicalize("C:\\financepa\\foo.txt")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testAllowWithDefaultDeny1() {
        // Testing "allow hchan with default deny" for **.java policy
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\fubar.java")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());        
    }

    public void testAllowWithDefaultDeny2() {
        // Testing "allow hchan with default deny" for **.java policy
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\fubar.java", "C:\foo.java")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.AHAN.getSID(), "name", "andy@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName());
    }

    public void testFinanceDeny() {
        // Testing "finance deny policy"
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\finance\\foo.txt")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName());        
    }

    public void testTrackingPolicy() {
        // Tracking policy
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\e\\f\\g\\me.you")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());        
    }

    public void testLocalhostdeny() {
        // Localhost deny
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch( Exception ex ) {
            fail(ex.getMessage());
        }
        EvaluationResult result = callEvaluate(
            canonicalize("C:\\foofoo.doc")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", localHost.getCanonicalHostName(), "inet_address", localHost.getHostAddress())
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName()); 
    }

    public void testCreatedDate() {
        // Creation date
        String policyDirName = System.getProperty("src.root.dir")
        +   File.separator + "test_files" + File.separator + "com" 
        +   File.separator + "bluejungle" + File.separator + "pf"
        +   File.separator + "engine" + File.separator + "destiny"
        + File.separator + "policies";
        String filename = policyDirName + File.separator + "resource_date.pql";
        EvaluationResult result = callEvaluate(
            canonicalize(filename)
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.AMORGAN.getSID(), "name", "amorgan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("resource_date.pql should be denied", EvaluationResult.DENY, result.getEffectName());
    }

    public void testUnknownEverything() {
        // Unknown user, host, and app
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\foofoo.ldap")
        ,   null
        ,   attr("name", IDAction.CHANGE_PROPERTIES_NAME)
        ,   attr("id", "bad-SID-value", "name", "badguy@bluejungle.com")
        ,   attr("name", "badhost.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "mindreader.exe", "fingerprint", "unknown fp")
        );
        assertEquals("evaluation of unknown subjects should be applicable to policies with * or default clause", EvaluationResult.DENY, result.getEffectName());
    }

    public void testMultiResource() {
        // multi-resource case, 4 from resources, with only one matching
        EvaluationResult result = callEvaluate(
            canonicalize(
                 "c:\\no_policy.match"
            ,   "c:\\nothing_ever_matches"
            ,   "c:\\finance\\foo.txt"
            ,   "c:\\another_bogus\\file"
            )
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("evaluation of multiple names for resources should be applicable if at least one resource matches", EvaluationResult.DENY, result.getEffectName());
    }

    public void testResourceSubref1() {
        // resource subref test
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\subref.test")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testResourceSubref2() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\subref.test")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.AMORGAN.getSID(), "name", "amorgan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals(EvaluationResult.DENY, result.getEffectName());
    }

    // This test, although valid, uses a policy with an unsupported feature that
    // recent changes broke.  The policy has a 'context sensitive' resource predicate
    // (owner matches current user) which breaks our caching code.  This sort of
    // policy can't be written in PA (yet) so we'll ignore the problem for the moment.
    /*
    public void testResourceOwner() {
        // resource owner test
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\owner.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", TestUser.AMORGAN.getSID(), "name", "amorgan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("unknown user should not match the owner", EvaluationResult.ALLOW, result.getEffectName());
        String ownerSid = osWrapper.getOwnerSID( "c:\\owner.test", 0, null  );
        result = callEvaluate(
            canonicalize("c:\\owner.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", ownerSid, "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("The actual resource owner sould match the fso resource owner", EvaluationResult.DENY, result.getEffectName());
    }
    */

    public void testResourceLdapOwner1() throws IOException {
        // resource ldap owner test
        new File("c:\\owner_ldap.test").delete();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\subref.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("unknown user should not match engineering", EvaluationResult.ALLOW, result.getEffectName());
        new File("c:\\owner_ldap.test").createNewFile();
        result = callEvaluate(
            canonicalize("c:\\owner_ldap.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        String ownerSid = osWrapper.getOwnerSID("c:\\owner_ldap.test", 0, null);
        assertEquals("The user running this test ("+ownerSid+") is not a member of destiny-eng LDAP group", EvaluationResult.DENY, result.getEffectName());
    }

    public void testResourceDestinyOwner1() throws IOException {
        // resource destiny owner test
        new File("c:\\owner_destiny.test").delete();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\owner_destiny.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("unknown user should not match destiny_group_level2", EvaluationResult.ALLOW, result.getEffectName());
        new File("c:\\owner_destiny.test").createNewFile();
        String ownerSid = osWrapper.getOwnerSID("c:\\owner_destiny.test", 0, null);
        result = callEvaluate(
            canonicalize("c:\\owner_destiny.test")
        ,   null
        ,   attr("name", IDAction.DELETE_NAME)
        ,   attr("id", TestUser.SERGEY.getSID(), "name", "sergey@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("The user running this test ("+ownerSid+") is not a member of destiny_group_level2", EvaluationResult.DENY, result.getEffectName());
    }

    public void testNegatedLdapGroup() {
        // negated ldap group test
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\negated_ldap_group.test")
        ,   null
        ,   attr("name", IDAction.COPY_NAME)
        ,   attr("id", TestUser.ISUNDIUS.getSID(), "name", "isundius@test.bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Ioanna should match (!engineering)", EvaluationResult.DENY, result.getEffectName());
        result = callEvaluate(
            canonicalize("c:\\negated_ldap_group.test")
        ,   null
        ,   attr("name", IDAction.COPY_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Hor-Kan should not match (!engineering)", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testOpeningDirectoriesIsOK() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\windows")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.KENG.getSID(), "name", "keng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Opening directories should always be allowed.", EvaluationResult.ALLOW, result.getEffectName());
        result = callEvaluate(
            canonicalize("c:\\windows")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", TestUser.KENG.getSID(), "name", "keng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("There is a policy denying the edit.", EvaluationResult.DENY, result.getEffectName());
    }

    public void testObligations01() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\verify.obligations")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        // We aren't executing the obligations, but we can check that they should be applied
        // Obligations01, Obligations02, and TrackingPolicy should apply.
        // Obligations03 should not apply because it is for a different user.
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(3, obl.size());
    }

    public void testObligations02() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\verify.obligations")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(3, obl.size());
    }

    public void testApplicationByNameDeny() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\does_not_matter")
        ,   null
        ,   attr("name", IDAction.RUN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "autorun.exe")
        );
        assertEquals("DenyAutorun should block this.", EvaluationResult.DENY, result.getEffectName());
    }

    public void testApplicationByNameAllow() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\does_not_matter")
        ,   null
        ,   attr("name", IDAction.RUN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "manualrun.exe")
        );
        assertEquals("DenyAutorun should not apply.", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testApplicationURLDeny() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\deny_url.txt")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP, "url", "http://mail.google.com/123459857398457.jsp?a=123,b=456")
        );
        assertEquals("DenyBasedOnURL should block this.", EvaluationResult.DENY, result.getEffectName());
    }

    public void testApplicationURLAllow() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\deny_url.txt")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.BMENG.getSID(), "name", "bmeng@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP, "url", "http://mail.yahoo.com")
        );
        assertEquals("DenyBasedOnURL should not apply.", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testRemoteRun() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\does_not_matter")
        ,   null
        ,   attr("name", IDAction.RUN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("RemoteRun should not apply.", EvaluationResult.ALLOW, result.getEffectName());

        // This policy doesn't apply.  The only way to distinguish this from an ALLOW is to check
        // obligations.  An inapplicable policy doesn't set obligations (however, tracking.pql will
        // set an obligation, hence the count of 1)
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
    }

    public void testCommunicationSingleUserDirectEmail() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\communication_policy")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "keng.lim@bluejungle.com")
        );
        assertEquals("No e-mails to Keng.", EvaluationResult.DENY, result.getEffectName());
        // Policies 01 and 03 should apply
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(2, obl.size());
        Object obl1 = obl.get(0);
        assertTrue(obl1 instanceof CustomObligation);
        Object obl2 = obl.get(1);
        assertTrue(obl2 instanceof CustomObligation);
        
        assertEquals("policy01", ((CustomObligation)obl1).getCustomObligationName());
        assertEquals("policy03", ((CustomObligation)obl2).getCustomObligationName());
    }

    public void testEmailToUsersOutsideOfBundleRequest() {
        // This test case is nearly identical to the one above
        // (i.e. testCommunicationSingleUserDirectEmail), except
        // the target of the e-mail was not among the users
        // for whom the bundle has been requested.
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\communication_policy")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "krista.kendall@bluejungle.com")
        );
        assertEquals("No e-mails to Krista.", EvaluationResult.DENY, result.getEffectName());
        // Policy 03 should apply (user.name='k*')
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        Object obl1 = obl.get(0);
        assertTrue(obl1 instanceof CustomObligation);
        assertEquals("policy03", ((CustomObligation)obl1).getCustomObligationName());
    }

    public void testCommunicationSingleUserEnrolledEmail() {
        ce.resetLog();
        // The policies talk about keng.lim@bluejungle.com, but
        // klim@nextlabs.com is also enrolled, so the policy should apply.
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\communication_policy")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "klim@nextlabs.com")
        );
        assertEquals("No e-mails to Keng.", EvaluationResult.DENY, result.getEffectName());
        // Policies 01 and 03 should apply
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(2, obl.size());
        Object obl1 = obl.get(0);
        assertTrue(obl1 instanceof CustomObligation);
        Object obl2 = obl.get(1);
        assertTrue(obl2 instanceof CustomObligation);
        
        assertEquals("policy01", ((CustomObligation)obl1).getCustomObligationName());
        assertEquals("policy03", ((CustomObligation)obl2).getCustomObligationName());
    }

    public void testCommunicationMultipleUsers() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\communication_policy")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "savaii.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "keng.lim@bluejungle.com", "email", "someone@gs.com")
        );
        assertEquals("No e-mails to Keng or GS people.", EvaluationResult.DENY, result.getEffectName());
        // Policies 01 and 03 apply to Keng; Policy02 applies to GS person
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(3, obl.size());
        Object obl1 = obl.get(0);
        assertTrue(obl1 instanceof CustomObligation);
        Object obl2 = obl.get(1);
        assertTrue(obl2 instanceof CustomObligation);
        Object obl3 = obl.get(2);
        assertTrue(obl3 instanceof CustomObligation);
        
        assertEquals("policy01", ((CustomObligation)obl1).getCustomObligationName());
        assertEquals("policy02", ((CustomObligation)obl2).getCustomObligationName());
        assertEquals("policy03", ((CustomObligation)obl3).getCustomObligationName());
    }

    public void testCommunicationUnknownUser() {
        ce.resetLog();
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\communication_policy_not")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "someone@unknown.com")
        );
        assertEquals("No e-mails to unknown.", EvaluationResult.DENY, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        Object obl1 = obl.get(0);
        assertTrue(obl1 instanceof CustomObligation);
        
        assertEquals("policyNOT", ((CustomObligation)obl1).getCustomObligationName());
    }

    public void testEmailToContact() {
        ce.resetLog();
        // Must not work for an unknown e-mail
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\emailcontact")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.SERGEY.getSID(), "name", "sergey@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "someone@unknown.com")
        );
        assertEquals("Should not apply", EvaluationResult.ALLOW, result.getEffectName());
        // Must work for all e-mail alternatives
        for ( String jedsEmail : new String[] {
                "jboulton2@bluejungle.com"
            ,   "jboulton2@nextlabs.com"
            ,   "jed.boulton2@bluejungle.com"
            ,   "jed.boulton2@nextlabs.com"
            ,   "jed@dubious.com"
            ,   "jeddubious.com@bluejungle.com"
            ,   "jeddubious.com@nextlabs.com"} ) {
            result = callEvaluate(
                canonicalize("c:\\emailcontact")
            ,   null
            ,   attr("name", IDAction.EMAIL_NAME)
            ,   attr("id", TestUser.SERGEY.getSID(), "name", "sergey@bluejungle.com")
            ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
            ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
            ,   attr("email", jedsEmail)
            );
            assertEquals(
                "Should work for jed's e-mail "+jedsEmail
            ,   EvaluationResult.DENY
            ,   result.getEffectName()
            );
            List<IDObligation> obligations = result.getObligations();
            assertNotNull(obligations);
            boolean found = false;
            for (IObligation obl : obligations) {
                found |= (obl instanceof CustomObligation)
                      && "emc_deny".equals(((CustomObligation)obl).getCustomObligationName());
            }
            assertTrue("emc_deny must be triggered", found);
        }
    }

    public void testEmailToNotContact() {
        ce.resetLog();
        // Must work for an unknown e-mail
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\emailcontact")
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.SGOLDSTEIN.getSID(), "name", "sgoldstein@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        ,   attr("email", "someone@unknown.com")
        );
        assertEquals("Should apply", EvaluationResult.DENY, result.getEffectName());
        List<IDObligation> obligations = result.getObligations();
        assertNotNull(obligations);
        boolean found = false;
        for (IObligation obl : obligations) {
            found |= (obl instanceof CustomObligation)
                  && "emNc_deny".equals(((CustomObligation)obl).getCustomObligationName());
        }
        assertTrue("emNc_deny must be triggered", found);
        // Must not work for all e-mail alternatives of the contact
        for ( String jedsEmail : new String[] {
                "jboulton2@bluejungle.com"
            ,   "jboulton2@nextlabs.com"
            ,   "jed.boulton2@bluejungle.com"
            ,   "jed.boulton2@nextlabs.com"
            ,    "jed@dubious.com"
            ,   "jeddubious.com@bluejungle.com"
            ,   "jeddubious.com@nextlabs.com"} ) {
            result = callEvaluate(
                canonicalize("c:\\emailcontact")
            ,   null
            ,   attr("name", IDAction.EMAIL_NAME)
            ,   attr("id", TestUser.SGOLDSTEIN.getSID(), "name", "sgoldstein@bluejungle.com")
            ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
            ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
            ,   attr("email", jedsEmail)
            );
            assertEquals(
                "Should not apply to jed's e-mail "+jedsEmail
            ,   EvaluationResult.ALLOW
            ,   result.getEffectName()
            );
        }
    }

    public void testSubjectIdentifiedByEmail() {
        ce.resetLog();
        for (String scottEmail : new String[] {
                "scott.goldstein@bluejungle.com"
            ,   "scott.goldstein@nextlabs.com"
            ,   "sgoldstein@bluejungle.com"
            ,   "sgoldstein@nextlabs.com"} ) {
            EvaluationResult result = callEvaluate(
                canonicalize("c:\\userbyemail")
            ,   null
            ,   attr("name", IDAction.EDIT_NAME)
            ,   attr("id", TestUser.SGOLDSTEIN.getSID(), "name", scottEmail)
            ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
            ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
            );
            assertEquals("Should apply", EvaluationResult.DENY, result.getEffectName());
            List<IDObligation> obligations = result.getObligations();
            assertNotNull(obligations);
            boolean found = false;
            for (IObligation obl : obligations) {
                found |= (obl instanceof CustomObligation)
                      && "byemail".equals(((CustomObligation)obl).getCustomObligationName());
            }
            assertTrue("byemail must be triggered", found);
        }
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\userbyemail")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", TestUser.SERGEY.getSID(), "name", "sergey@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Should not apply", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testSubjectIdentifiedByEmailDomain() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\userbyemaildomain")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@nextlabs.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Should apply", EvaluationResult.DENY, result.getEffectName());
        List<IDObligation> obligations = result.getObligations();
        assertNotNull(obligations);
        boolean found = false;
        for (IObligation obl : obligations) {
            found |= (obl instanceof CustomObligation)
                  && "byemaildomain".equals(((CustomObligation)obl).getCustomObligationName());
        }
        assertTrue("byemaildomain must be triggered", found);
        result = callEvaluate(
            canonicalize("c:\\userbyemaildomain")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", TestUser.BCLINTON.getSID(), "name", "bclinton@test.bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Should not apply", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testUnknownSubjectIdentifiedByEmailDomain() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\userbyemaildomain")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", "### UNKNOWN ###", "name", "unknown@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Should not apply", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testIdOfEnrolledNotApplicableUsers() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\tracking")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "unknown@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Tracking policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        PolicyActivityInfoV5 paInfo = result.getPAInfo();
        assertNotNull(paInfo);
        Long userId = paInfo.getUserId();
        assertNotNull(userId);
        assertFalse(
            "Iannis is part of the bundle - he should have a known ID"
        ,   userId.equals(IDeploymentBundle.KEY_OF_UNKNOWN_USER)
        );
    }

    public void testIdOfUnenrolledNotApplicableUsers() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\tracking")
        ,   null
        ,   attr("name", IDAction.EDIT_NAME)
        ,   attr("id", "### UNKNOWN ###", "name", "unknown@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Tracking policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        PolicyActivityInfoV5 paInfo = result.getPAInfo();
        assertNotNull(paInfo);
        Long userId = paInfo.getUserId();
        assertNotNull(userId);
        assertEquals(
            "Unknown unenrolled users should have a predefined ID"
        ,   userId.longValue()
        ,   IDeploymentBundle.KEY_OF_UNKNOWN_USER
        );
    }

    public void testNegatedApplicationUnknown() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\negatedapp")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        );
        assertEquals("Policy for !SMALL should apply", EvaluationResult.DENY, result.getEffectName());
        result = callEvaluate(
            canonicalize("c:\\negatedapp")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\write.exe", "fingerprint", "unknown-write")
        );
        assertEquals("Policy for !SMALL should apply", EvaluationResult.DENY, result.getEffectName());
    }

    public void testNegatedApplicationKnown() {
        EvaluationResult result = callEvaluate(
            canonicalize("c:\\negatedapp")
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", SMALL_EXE, "fingerprint", TestUser.SMALL_APP_FP)
        );
        assertEquals("Policy for !SMALL should not apply", EvaluationResult.ALLOW, result.getEffectName());
    }

    public void testMisdirectedEmailResourceIdIsNotSet() {
        DynamicAttributes from = canonicalize("misdirected1.txt");
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@unknown.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testMisdirectedEmailResourceIdIsSet() {
        DynamicAttributes from = canonicalize("misdirected2.txt");
        from.add("client_id", "goldman");
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("misdirected", obl.get(0).toPQL());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@gs.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@gs.com", "email", "joe@gs.com", "email", "james@ms.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("misdirected", obl.get(0).toPQL());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@unknown.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@unknown.com", "email", "joe@gs.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testMisdirectedEmailMultipleClientIdPerFile() {
        DynamicAttributes from = canonicalize("misdirected3.txt");
        // The document is prepared for morgan and for goldman
        from.add("client_id", "morgan");
        from.add("client_id", "goldman");
        // Sending to morgan - should pass
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "joe@ms.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Sending to goldman - should pass
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@gs.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Sending to merryll - should fail
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "james@ml.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("misdirected", obl.get(0).toPQL());
    }

    public void testMisdirectedEmailMultiplClientIdsPerRecipient() {
        DynamicAttributes from = canonicalize("misdirected4.txt");
        from.add("client_id", "morgan");
        // Sending to morgan - should pass
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "joe@ms.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Agent is associated with all three firms, so he should be allowed as a recipient:
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "agent@gmail.com")
        );
        assertEquals("Nothing should apply - agent is OK for all firms", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Sending to goldman - should fail
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@gs.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("misdirected", obl.get(0).toPQL());
    }

    public void testMisdirectedEmailMultiplClientIdsPerRecipientOfMultiClientFiles() {
        DynamicAttributes from = canonicalize("misdirected5.txt");
        from.add("client_id", "morgan");
        from.add("client_id", "goldman");
        // Sending to morgan - should pass
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "joe@ms.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Sending to goldman - should pass
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@gs.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        // Agent is associated with all three firms, so he should be allowed as a recipient:
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "agent@gmail.com")
        );
        assertEquals("Nothing should apply - agent is OK for all firms", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testMultiClientEmail() {
        DynamicAttributes from = canonicalize("multiclient.txt");
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com", "email", "bob@gs.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("multiclient", obl.get(0).toPQL());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com", "email", "joe@ml.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "sergey@bluejungle.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testMultiClientEmailSinglePerson() {
        DynamicAttributes from = canonicalize("multiclient.txt");
        from.add("modified_date", "2");
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "broker@ml.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("multiclient", obl.get(0).toPQL());
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "broker@gmail.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testUnsetAttribute() {
        DynamicAttributes from = canonicalize("unset_attribute1.txt");
        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com")
        );
        assertEquals("Only a monitor policy should apply", EvaluationResult.ALLOW, result.getEffectName());
        List<IDObligation> obl = result.getObligations();
        assertNotNull(obl);
        assertEquals(1, obl.size());
        assertEquals("unset_attribute", obl.get(0).toPQL());
        from = canonicalize("unset_attribute2.txt");
        from.add("client_id", "goldman");
        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.EMAIL_NAME)
        ,   attr("id", TestUser.IHANEN.getSID(), "name", "ihanen@anonymous.net")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        ,   attr("email", "bob@ml.com")
        );
        assertEquals("Nothing should apply", EvaluationResult.ALLOW, result.getEffectName());
        obl = result.getObligations();
        assertNotNull(obl);
        assertTrue(obl.isEmpty());
    }

    public void testCachingResource() {
        DynamicAttributes from = canonicalize("c:\\smallfiles\\a.txt");
        from.add("modified_date", "2");
        from.add("size", "1000");

        EvaluationResult result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        );
        assertEquals("Policy should apply for files < 1024", EvaluationResult.DENY, result.getEffectName());

        // Modified date hasn't changed, so the cached version will be used (with size 1000)
        from = canonicalize("c:\\smallfiles\\a.txt");
        from.add("modified_date", "2");
        from.add("size", "2000");

        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        );
        assertEquals("Policy should apply due to cached data", EvaluationResult.DENY, result.getEffectName());

        // Modified date has now changed so cache is invalidated
        from = canonicalize("c:\\smallfiles\\a.txt");
        from.add("modified_date", "3");
        from.add("size", "2000");

        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        );
        assertEquals("Policy should not apply to file >= 1024", EvaluationResult.ALLOW, result.getEffectName());

        // Modified date has not changed, but request that the cache be ignored
        from = canonicalize("c:\\smallfiles\\a.txt");
        from.add("modified_date", "3");
        from.add(SpecAttribute.NOCACHE_NAME, "yes");
        from.add("size", "1000");

        result = callEvaluate(
            from
        ,   null
        ,   attr("name", IDAction.OPEN_NAME)
        ,   attr("id", TestUser.HCHAN.getSID(), "name", "hchan@bluejungle.com")
        ,   attr("name", "cicia.bluejungle.com", "inet_address", "10.17.11.130")
        ,   attr("name", "c:\\windows\\notepad.exe", "fingerprint", "unknown-notepad")
        );
        assertEquals("Policy should apply for files < 1024", EvaluationResult.DENY, result.getEffectName());
    }

    public void testCleanupFiles() {
        for (String fileName : fileNames) {
            new File(fileName).delete();
        }
        for (int i = dirNames.length-1 ; i != -1 ; i--) {
            new File(dirNames[i]).delete();
        }
    }

    private DynamicAttributes canonicalize(String ... names) {
        if (names == null) {
            return null;
        }
        List<String> canonicalNames = new ArrayList<String>();
        for (String  name : names) {
            String nativeResource = name.replace(File.separatorChar, '/');
            if (nativeResource.startsWith("//")) {
                canonicalNames.add("file:" + nativeResource.toLowerCase());
            } else {
                canonicalNames.add("file:///" + nativeResource.toLowerCase());
            }
        }
        DynamicAttributes res = new DynamicAttributes(canonicalNames.size());
        res.put(SpecAttribute.NAME_ATTR_NAME, canonicalNames);
        res.put(SpecAttribute.ID_ATTR_NAME, canonicalNames);
        res.put(SpecAttribute.DESTINYTYPE_ATTR_NAME, "fso");
        return res;
    }

    private EvaluationResult callEvaluate(
        DynamicAttributes from
    ,   DynamicAttributes to
    ,   DynamicAttributes action
    ,   DynamicAttributes user
    ,   DynamicAttributes host
    ,   DynamicAttributes application
    ) {
        return callEvaluate(
            from
        ,   to
        ,   action
        ,   user
        ,   host
        ,   application
        ,   DynamicAttributes.EMPTY
        );
    }

    private EvaluationResult callEvaluate(
        DynamicAttributes from
    ,   DynamicAttributes to
    ,   DynamicAttributes action
    ,   DynamicAttributes user
    ,   DynamicAttributes host
    ,   DynamicAttributes application
    ,   DynamicAttributes sendto
    ) {
        Map<String,DynamicAttributes> ctx = new HashMap<String,DynamicAttributes>();
        ctx.put("action", action);
        ctx.put("user", user);
        ctx.put("host", host);
        ctx.put("application", application);
        ctx.put("from", from);
        ctx.put("to", to);
        ctx.put("sendto", sendto);
        return container.evaluate(
            12345L                    // request ID - fixed for all tests
        ,   AgentTypeEnumType.DESKTOP // Agent type - fixed for all tests
        ,   null                      // Process token - fixed for all tests
        ,   ctx
        ,   System.currentTimeMillis()
        ,   0                         // Log level - fixed for all tests
        ,   true                      // Execute obligations - fixed for all tests
        );
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

