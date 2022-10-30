package com.bluejungle.pf.destiny.services;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author sergey
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/pf/src/java/test/com/bluejungle/pf/destiny/services/TestPolicyEditorClient.java#38 $
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
/*
import com.bluejungle.destiny.services.management.UserRoleServiceIF;
import com.bluejungle.destiny.services.management.UserRoleServiceLocator;
import com.bluejungle.destiny.services.management.types.DuplicateLoginNameException;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.Role;
*/
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.misc.IDTarget;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.misc.IAccessControlled;

/**
 * Tests for the client-side library for working with the Policy Editor.
 */

public class TestPolicyEditorClient extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(TestPolicyEditorClient.class);
    }
    
    private IPolicyEditorClient client;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        try {
        	IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
			HashMapConfiguration pfClientConfig = new HashMapConfiguration();
			pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, "https://localhost:8443");
			pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, "Administrator");
			pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, "123blue!");
			
			ComponentInfo compInfo = new ComponentInfo(PolicyEditorClient.COMP_INFO.getName(), PolicyEditorClient.COMP_INFO.getClassName(), PolicyEditorClient.COMP_INFO.getInterfaceName(), PolicyEditorClient.COMP_INFO.getLifestyleType(), pfClientConfig);
			this.client = (IPolicyEditorClient) compMgr.getComponent(compInfo);
			this.client.login();
        } catch (LoginException exception) {
            fail("Failed to create PolicyEditorClient: " + exception.getMessage());
        }
    }

    public void testInit() {
        assertNotNull(client);
        try {
            if (client.isLoggedIn()) {
                ((PolicyEditorClient) client).prepareForTests();
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testGetAllAgents() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<AgentStatusDescriptor> agents = client.getAgentList();
            assertNotNull(agents);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    /**
     * Update this constant when you add more resource definitions for testing
     * the resource preview feature.
     */
    static final int NUM_PREVIEW = 17;

    public void testResourcePreview() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
			assertNotNull(client.getLoggedInUser());
		} catch (PolicyEditorException e) {
			fail(e.toString());
		}
        String basePath = System.getProperty("src.root.dir");
        basePath += File.separator + "test_files" + File.separator;
        String subPath = getClass().getPackage().getName();
        subPath = subPath.replaceAll("[.]", "\\" + File.separator);
        basePath += subPath;
        // Clean the directory for the test output and the differences
        File outDir = new File(basePath, "test_output");
        deleteAll(outDir);

        String pql =
            "component resource/rp1 = name=\"\\\\\\\\pukapuka\\\\dosc\\\\**\" OR name=\"\\\\\\\\pukapuka\\\\secret\\\\**\" OR name=\"\\\\\\\\pukapuka\\\\secret\\\\topsecret\\\\**\"\n"
        +   "component resource/rp2 = name=\"\\\\\\\\pukapuka\\\\dosc\\\\**\" AND name=\"\\\\\\\\pukapuka\\\\secret\\\\**\" AND name=\"\\\\\\\\pukapuka\\\\secret\\\\topsecret\\\\**\"\n"
        +   "component resource/rp3 = resource/rp1 OR resource/rp2\n"
        +   "component resource/rp4 = not (not name=\"\\\\\\\\pukapuka\\\\doubleNOT1\\\\*\") OR name=\"\\\\\\\\pukapuka\\\\doubleNOT2\\\\*\"\n"
        +   "component resource/rp5 = name=\"?c:\\\\preview\\\\*\"\n"
        +   "component resource/rp6 = name=\"c:\\\\preview\\\\\"\n"
        +   "component resource/rp7 = resource/rp5 OR resource/rp6\n"
        +   "component resource/rp8 = name=\"\\\\\\\\pukapuka\\\\dosc\\\\**\" OR name=\"\\\\\\\\pukapuka\\\\secret\\\\**\" and size > 10\n"
        +   "component resource/rp9 = resource/rp1 AND resource/rp5 AND resource/rp6\n"
        +   "component resource/rpA = name=\"\\\\\\\\pukapuka\\\\dosc\\\\**\\\\**\" OR name=\"\\\\\\\\pukapuka\\\\dosc\\\\**\\\\*\"\n"
        +   "component resource/rpB = name=\"\\\\\\\\pukapuka\\\\dos.txt\" OR name=\"\\\\\\\\pukapuka\\\\blah.txt\"\n"
        +   "component resource/rpC = name=\"\\\\\\\\pukapuka\\\\d*\" OR name=\"\\\\\\\\pukapuka\\\\b*\"\n"
        +   "component resource/rpD = name=\"\\\\\\\\pukapuka\\\\d*\" OR name=\"\\\\\\\\puka**\"\n"
        +   "component resource/rpE = name=\"?c:\\\\**\\\\f*5.txt\" AND size > 50\n"
        +   "component resource/rpF = (((FALSE OR resource.name = \"c:\\\\**\") AND TRUE) AND (TRUE AND resource.name = \"destiny*\"))"
        +   "component resource/rpG = (((FALSE OR resource.name = \"c:\\\\**\") AND TRUE) AND (TRUE AND resource.name = \"\\\\destiny*\"))"
        +   "component resource/rpH = (((FALSE OR resource.name = \"[mydocuments]\\\\**\") AND TRUE) AND (TRUE AND resource.type = \"doc\"))";
        String myDocuments = ((IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class)).getMyDocumentsFolder();
        String[] testName = new String[] {
                "\\RPTEST$hello.world", "\\RPTEST$hello.doc", "\\RPTEST$hellodoc", "\\RPTEST$dochello.doc"
            };
        try {
            importBulkPql(pql);
            // Verify that the duplicate roots are eliminated
            Collection<DomainObjectDescriptor> rg = client.getDescriptorsForNameAndType("resource/rp3", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            DomainObjectDescriptor descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            ResourcePreview rp = client.getResourcePreview(descr);
            Collection<String> roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(2, roots.size());
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            rg = client.getDescriptorsForNameAndType("resource/rp1", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(2, roots.size());
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that double "NOT" works
            rg = client.getDescriptorsForNameAndType("resource/rp4", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(2, roots.size());
            assertTrue(roots.contains("\\\\pukapuka\\doublenot1\\"));
            assertTrue(roots.contains("\\\\pukapuka\\doublenot2\\"));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\doublenot1")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\doublenot2")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that resources referring to local roots work
            rg = client.getDescriptorsForNameAndType("resource/rp5", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertTrue(roots.isEmpty());
            assertFalse(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\pukapuka\\doublenot1")));
            assertFalse(rp.isProbableRoot(new File("\\\\pukapuka\\doublenot2")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertTrue(rp.isProbableRoot(new File("c:\\preview")));
            assertTrue(rp.isProbableRoot(new File("d:\\preview")));
            assertTrue(rp.isProbableRoot(new File("e:\\preview")));
            assertTrue(rp.isProbableRoot(new File("f:\\preview")));
            assertFalse(rp.isProbableRoot(new File("c:\\preview\\blah")));
            assertFalse(rp.tryAllNetworkRoots());
            assertTrue(rp.tryAllLocalRoots());

            // Verify that the size is ignored
            rg = client.getDescriptorsForNameAndType("resource/rp8", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(2, roots.size());
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that equivalent roots do not cancel each other
            rg = client.getDescriptorsForNameAndType("resource/rpA", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(1, roots.size());
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that individual files are included
            rg = client.getDescriptorsForNameAndType("resource/rpB", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(1, roots.size());
            assertFalse(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that roots are trimmed appropriately
            rg = client.getDescriptorsForNameAndType("resource/rpC", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(1, roots.size());
            assertFalse(rp.isProbableRoot(new File("\\\\pukapuka\\dosc\\hello\\world")));
            assertTrue(rp.isProbableRoot(new File("\\\\pukapuka")));
            assertFalse(rp.isProbableRoot(new File("\\\\notpukapuka")));
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Verify that entries with wildcards are not considered roots
            rg = client.getDescriptorsForNameAndType("resource/rpD", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertTrue(roots.isEmpty());
            assertTrue(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // Test a couple of real expressions from the policy author
            rg = client.getDescriptorsForNameAndType("resource/rpF", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertFalse(roots.isEmpty());
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // The expression is slightly different, but the result must be the same
            rg = client.getDescriptorsForNameAndType("resource/rpG", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertFalse(roots.isEmpty());
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());

            // The expression includes [mydocuments]
            rg = client.getDescriptorsForNameAndType("resource/rpH", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertEquals(1, roots.size());
            assertFalse(rp.tryAllNetworkRoots());
            assertFalse(rp.tryAllLocalRoots());
            for ( int i = 0 ; i != testName.length ; i++ ) {
                File testFile = new File( myDocuments + testName[i] );
                testFile.createNewFile();
                // Even-numbered elements of the list do not match; odd-numbered elements do match.
                assertEquals( i % 2 != 0, rp.accept( testFile ) );
            }
            // Create real files for testing
            outDir.mkdirs();
            for (int i = 0; i != 100; i++) {
                File f = new File(outDir, "f" + (i / 10) + (i % 10) + ".txt");
                FileOutputStream fos = new FileOutputStream(f);
                for (int j = 0; j != i; j++) {
                    fos.write(new byte[] { 65 });
                }
                fos.close();
            }

            // Verify that filtering works with real files
            rg = client.getDescriptorsForNameAndType("resource/rpE", EntityType.COMPONENT, false);
            assertNotNull(rg);
            assertEquals(1, rg.size());
            descr = (DomainObjectDescriptor) rg.iterator().next();
            assertNotNull(descr);
            rp = client.getResourcePreview(descr);
            roots = rp.getRoots();
            assertNotNull(roots);
            assertTrue(roots.isEmpty());
            assertFalse(rp.tryAllNetworkRoots());
            assertTrue(rp.tryAllLocalRoots());
            assertTrue(rp.isProbableRoot(new File(basePath)));
            assertTrue(rp.isProbableRoot(outDir));
            File[] filtered = outDir.listFiles((FileFilter) rp);
            assertNotNull(filtered);
            assertEquals(5, filtered.length);
            for (int i = 0; i != filtered.length; i++) {
                assertTrue(filtered[i].getAbsolutePath().endsWith("5.txt"));
            }
            String[] filteredNames = outDir.list(rp);
            assertNotNull(filteredNames);
            assertEquals(5, filteredNames.length);
            for (int i = 0; i != filteredNames.length; i++) {
                assertTrue(filteredNames[i].endsWith("5.txt"));
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        } catch (IOException pe) {
            fail(pe.getMessage());
        } finally {
            deleteAll(outDir);
            for ( int i = 0 ; i != testName.length ; i++ ) {
                File testFile = new File( myDocuments + testName[i] );
                testFile.delete();
            }
        }
    }

    public void testGetAttributes() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
			assertNotNull(client.getLoggedInUser());
		} catch (PolicyEditorException e) {
			fail(e.toString());
		}
        try {
            checkAttributes(client.getAttributesForType(EntityType.ACTION));
            checkAttributes(client.getAttributesForType(EntityType.RESOURCE));
            checkAttributes(client.getAttributesForType(EntityType.USER));
            checkAttributes(client.getAttributesForType(EntityType.HOST));
            checkAttributes(client.getAttributesForType(EntityType.APPLICATION));
        } catch (PolicyEditorException e) {
            fail(e.getMessage());
        }
    }

    private static void checkAttributes(Collection<AttributeDescriptor> attrs) {
        assertNotNull(attrs);
        assertFalse(attrs.isEmpty());
        for (AttributeDescriptor ad : attrs) {
            assertNotNull(ad.getDisplayName());
            assertNotNull(ad.getPqlName());
            assertNotNull(ad.getType());
        }
    }

    public void testGettingLeafObjects() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
			assertNotNull(client.getLoggedInUser());
		} catch (PolicyEditorException e) {
			fail(e.toString());
		}
        try {
            checkLeafObjects(
                client.runLeafObjectQuery(
                    new LeafObjectSearchSpec(
                        LeafObjectType.USER
                    ,   PredicateConstants.TRUE
                    ,   10
                    )
                )
            ,   LeafObjectType.USER);
            checkLeafObjects(
                    client.runLeafObjectQuery(
                        new LeafObjectSearchSpec(
                            LeafObjectType.USER
                        ,   PredicateConstants.TRUE
                        ,   "bluejungle.com"
                        ,   10
                        )
                    )
                ,   LeafObjectType.USER);
            checkLeafObjects(
                client.runLeafObjectQuery(
                    new LeafObjectSearchSpec(
                        LeafObjectType.USER_GROUP
                    ,   PredicateConstants.TRUE
                    ,   10
                    )
                )
            ,   LeafObjectType.USER_GROUP
            );
            checkLeafObjects(
                    client.runLeafObjectQuery(
                        new LeafObjectSearchSpec(
                            LeafObjectType.CONTACT
                        ,   PredicateConstants.TRUE
                        ,   "bluejungle.com"
                        ,   10
                        )
                    )
                ,   LeafObjectType.CONTACT);
            checkLeafObjects(
                client.runLeafObjectQuery(
                    new LeafObjectSearchSpec(
                        LeafObjectType.HOST
                    ,   PredicateConstants.TRUE
                    ,   10
                    )
                )
            ,   LeafObjectType.HOST
            );
            checkLeafObjects(
                client.runLeafObjectQuery(
                    new LeafObjectSearchSpec(
                        LeafObjectType.HOST_GROUP
                    ,   PredicateConstants.TRUE
                    ,   10
                    )
                )
            ,   LeafObjectType.HOST_GROUP
            );
        } catch (PolicyEditorException e) {
            fail(e.getMessage());
        }
    }

    private static void checkLeafObjects(Collection<LeafObject> objs, LeafObjectType type) {
        assertNotNull(objs);
        for (LeafObject leaf : objs) {
            assertEquals(type, leaf.getType());
        }
    }

    public void testGetAndSaveEntities() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
			assertNotNull(client.getLoggedInUser());
		} catch (PolicyEditorException e) {
			fail(e.toString());
		}
        try {
            String[] nameArray = new String[] { "resource/quick", "resource/brown", "resource/fox", "resource/jumps", "resource/over", "resource/lazy", "resource/god" };
            Arrays.sort(nameArray);
            Collection<String> names = Arrays.asList(nameArray);
            // Obtain a collection of entities
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.COMPONENT, false);
            assertNotNull(res);
            Iterator<String> iName = names.iterator();
            for (IHasId n : res) {
                assertTrue(n instanceof IDSpec);
                IDSpec s = (IDSpec) n;
                // The entities have to have the right names
                assertEquals(iName.next().toLowerCase(), s.getName());
                // The entities must be empty
                assertSame(DevelopmentStatus.NEW, s.getStatus());
                // Set the status and the description
                s.setStatus(DevelopmentStatus.APPROVED);
                s.setDescription("blah blah blah");
            }
            // Save the empty entities we've just obtained
            client.saveEntities(res);
            // IDs must change without re-reading
            assertNotNull(res);
            for (IHasId n : res) {
                assertTrue(n instanceof IDSpec);
                IDSpec s = (IDSpec) n;
                // The entities must not be empty
                assertNotSame(DevelopmentStatus.NEW, s.getStatus());
                // Check the status and the description
                assertEquals(DevelopmentStatus.APPROVED, s.getStatus());
                assertEquals("blah blah blah", s.getDescription());
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testGetAndSavePolicies() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            String[] nameArray = new String[] { "policy", "blah" };
            Arrays.sort(nameArray);
            Collection<String> names = Arrays.asList(nameArray);
            // Obtain a collection of entities
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.POLICY, false);
            assertNotNull(res);
            Iterator<String> iName = names.iterator();
            for (IHasId n : res) {
                assertTrue(n instanceof IDPolicy);
                IDPolicy p = (IDPolicy) n;
                // The entities have to have the right names
                assertEquals(iName.next().toLowerCase(), p.getName());
                // Set the status and the description
                p.setStatus(DevelopmentStatus.APPROVED);
                p.setDescription("blah blah blah");
            }
            // Save the empty entities we've just obtained
            client.saveEntities(res);
            // IDs must change without re-reading
            assertNotNull(res);
            for (IHasId n : res) {
                assertTrue(n instanceof IDPolicy);
                IDPolicy p = (IDPolicy) n;
                // Check the status and the description
                assertEquals(DevelopmentStatus.APPROVED, p.getStatus());
                assertEquals("blah blah blah", p.getDescription());
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testDeleteRestoreCycle() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            String[] nameArray = new String[] { "host/delete-restore" };
            Collection<String> names = Arrays.asList(nameArray);
            for (int i = 0; i != 10; i++) {
                // Obtain a collection of entities
                Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.COMPONENT, false);
                assertNotNull(res);
                assertEquals(1, res.size());
                SpecBase spec = (SpecBase) res.iterator().next();
                assertNotNull(spec);
                assertSame(i == 0 ? DevelopmentStatus.NEW : DevelopmentStatus.EMPTY, spec.getStatus());
                spec.setStatus(DevelopmentStatus.DELETED);
                client.saveEntities(res);
                res = client.getEntitiesForNamesAndType(names, EntityType.COMPONENT, false);
                assertNotNull(res);
                assertEquals(1, res.size());
                spec = (SpecBase) res.iterator().next();
                assertNotNull(spec);
                assertSame(DevelopmentStatus.NEW, spec.getStatus());
                client.saveEntities(res);
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testNamesWithSpaces() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            String[] nameArray = new String[] { "resource/quick brown fox jumps over lazy dog" };
            Collection<String> names = Arrays.asList(nameArray);
            // Obtain a collection of entities
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.COMPONENT, false);
            assertNotNull(res);
            assertTrue(res.iterator().hasNext());
            Iterator<String> iName = names.iterator();
            Object n = res.iterator().next();
            assertTrue(n instanceof IDSpec);
            IDSpec s = (IDSpec) n;
            // The entities have to have the right names
            assertEquals(iName.next().toString().toLowerCase(), s.getName());
            // Set the status
            s.setStatus(DevelopmentStatus.APPROVED);
            // Save the empty entity we've just obtained
            client.saveEntities(res);
            assertTrue(res.iterator().hasNext());
            n = res.iterator().next();
            assertTrue(n instanceof IDSpec);
            s = (IDSpec) n;
            // ID must change without re-reading
            assertNotNull(s.getId());
            // Delete the entity
            s.setStatus(DevelopmentStatus.DELETED);
            client.saveEntities(res);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testNamesForType() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> descriptors = client.getDescriptorsForNameAndType("resource/%", EntityType.COMPONENT, false);
            assertNotNull(descriptors);
            assertEquals(7 + NUM_PREVIEW, descriptors.size());
            int countBlah = 0;
            for (DomainObjectDescriptor descr : descriptors) {
                assertNotNull(descr.getId());
                assertNotNull(descr.getName());
                if ("blah blah blah".equals(descr.getDescription())) {
                    countBlah++;
                    assertEquals(DevelopmentStatus.APPROVED, descr.getStatus());
                }
            }
            assertEquals(7, countBlah);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testDescriptorsForNameAndType() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> descriptors = client.getDescriptorsForNameAndTypes("resource/%o%", Arrays.asList(new EntityType[] { EntityType.COMPONENT }), false);
            assertNotNull(descriptors);
            assertEquals(4, descriptors.size());
            for (DomainObjectDescriptor descr : descriptors) {
                assertTrue(descr.getName().toUpperCase().indexOf('O') != -1);
            }
            descriptors = client.getDescriptorsForNameAndType("resource/%o%", EntityType.COMPONENT, false);
            assertNotNull(descriptors);
            assertEquals(4, descriptors.size());
            for (DomainObjectDescriptor descr : descriptors) {
                assertTrue(descr.getName().toUpperCase().indexOf('O') != -1);
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testPrepareSaveWithReferences() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        String pql = "id null status draft component user/one = description \"FIRST\" true\n" + "id null status draft component user/two = description \"SECOND\" true\n" + "id null status draft component user/three = description \"THIRD\" true\n";

        try {
            importBulkPql(pql);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testSaveWithReferences() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<? extends IHasId> saved = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "user/one" }), EntityType.COMPONENT, false);
            assertEquals(1, saved.size());
            Object obj = saved.iterator().next();
            assertTrue(obj instanceof IDSpec);
            CompositePredicate cp = new CompositePredicate(BooleanOp.OR, Arrays.asList(new IPredicate[] { new SpecReference("user/two"), new SpecReference("user/three") }));
            ((IDSpec) obj).setPredicate(cp);
            client.saveEntities(saved);
            saved = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "user/one", "user/two", "user/three" }), EntityType.COMPONENT, false);
            assertEquals(3, saved.size());
            for (IHasId so : saved) {
                assertTrue(so instanceof IDSpec);
                IDSpec spec = (IDSpec) so;
                if (spec.getName().equals("user/one")) {
                    assertTrue(spec.getPredicate() instanceof ICompositePredicate);
                } else {
                    assertEquals(PredicateConstants.TRUE, spec.getPredicate());
                }
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testGetLatestDeploymentTime() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Calendar res = client.getLatestDeploymentTime();
            assertNotNull(res);
            assertEquals(0, res.getTimeInMillis());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testGetNumDeployedPolicies() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            assertEquals(0, client.getNumDeployedPolicies());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testDirectReferencesTo() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            // Get the referring objects for "two" and "three" (that's "t%").
            Collection<DomainObjectDescriptor> res = client.getReferringObjectsForGroup("user/t%", EntityType.COMPONENT, null, false, true);
            assertNotNull(res);
            assertEquals(1, res.size());
            Object obj = res.iterator().next();
            assertTrue(obj instanceof DomainObjectDescriptor);
            DomainObjectDescriptor descr = (DomainObjectDescriptor) obj;
            assertEquals("user/one", descr.getName());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testDirectReferencesFrom() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            // Get the referred objects for "one"(that's "two" and "three" ).
            Collection<DomainObjectDescriptor> one = client.getDescriptorsForNameAndType("user/one", EntityType.COMPONENT, false);
            assertNotNull(one);
            assertEquals(1, one.size());
            Collection<DomainObjectDescriptor> res = client.getDependencies(one);
            assertNotNull(res);
            assertEquals(2, res.size());
            DomainObjectDescriptor[] dods = new DomainObjectDescriptor[2];
            res.toArray(dods);
            Arrays.sort(dods, new Comparator<DomainObjectDescriptor>() {
                public int compare(DomainObjectDescriptor lhs, DomainObjectDescriptor rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            assertEquals("user/three", dods[0].getName());
            assertEquals("user/two", dods[1].getName());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        } catch (CircularReferenceException pe) {
            fail(pe.getMessage());
        }
    }

    public void testEntitiesForDescriptors() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> descriptors = client.getDescriptorsForNameAndType("resource/%o%", EntityType.COMPONENT, false);
            assertNotNull(descriptors);
            assertFalse(descriptors.isEmpty());
            Collection<? extends IHasId> entities = client.getEntitiesForDescriptors(descriptors);
            assertNotNull(entities);
            assertFalse(entities.isEmpty());
            assertEquals(descriptors.size(), entities.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testEightAndEighteen() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<? extends IHasId> entities = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "action/eight", "action/eighteen" }), EntityType.ACTION, false);
            assertNotNull(entities);
            assertEquals(2, entities.size());
            client.saveEntities(entities);
            Collection<DomainObjectDescriptor> reRead = client.getDescriptorsForNameAndType("action/%eight%", EntityType.COMPONENT, false);
            assertNotNull(reRead);
            assertEquals(2, reRead.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testPolicyFolderObjects() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            String[] nameArray = new String[] { "pf1", "pf2", "pf2/cpf1", "pf2/cpf2", "pf2/cpf3", "pf2/cpf1/a", "pf2/cpf1/b" };
            Arrays.sort(nameArray);
            Collection<String> names = Arrays.asList(nameArray);
            // Obtain a collection of entities
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            for (IHasId n : res) {
                assertTrue(n instanceof PolicyFolder);
                PolicyFolder pf = (PolicyFolder) n;
                // The entities have to have the right names
                assertTrue(names.contains(pf.getName().toLowerCase()));
                // The entities must be empty
                assertSame(DevelopmentStatus.NEW, pf.getStatus());
                // Set the status and the description
                pf.setStatus(DevelopmentStatus.APPROVED);
                pf.setDescription("blah blah blah");
            }
            // Save the empty entities we've just obtained
            client.saveEntities(res);
            // IDs must change without re-reading
            assertNotNull(res);
            for (IHasId n : res) {
                assertTrue(n instanceof PolicyFolder);
                PolicyFolder pf = (PolicyFolder) n;
                // The entities must not be empty
                assertNotSame(DevelopmentStatus.NEW, pf.getStatus());
                // Check the status and the description
                assertEquals(DevelopmentStatus.APPROVED, pf.getStatus());
                assertEquals("blah blah blah", pf.getDescription());
            }
            client.saveEntities(res);
            Collection<DomainObjectDescriptor> reRead = client.getDescriptorsForNameAndType("pf%", EntityType.FOLDER, false);
            assertNotNull(reRead);
            assertEquals(res.size(), reRead.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testObjectsForState() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> res = client.getDescriptorsForState(DevelopmentStatus.APPROVED, false);
            assertNotNull(res);
            assertEquals(16, res.size());
            res = client.getDescriptorsForState(DevelopmentStatus.DRAFT, false);
            assertNotNull(res);
            assertEquals(3, res.size());
            res = client.getDescriptorsForState(DevelopmentStatus.EMPTY, false);
            assertNotNull(res);
            assertEquals(3 + NUM_PREVIEW, res.size());
            res = client.getDescriptorsForState(DevelopmentStatus.OBSOLETE, false);
            assertNotNull(res);
            assertEquals(0, res.size());
            res = client.getDescriptorsForState(DevelopmentStatus.DELETED, false);
            assertNotNull(res);
            assertEquals(11, res.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testCircularRefInDevelopment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        String pql =
            "ID null STATUS APPROVED component user/testCR_UG1 = group=\"user/testCR_UG2\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG2 = group=\"user/testCR_UG3\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG3 = group=\"user/testCR_UG4\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG4 = group=\"user/testCR_UG5\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG5 = group=\"user/testCR_UG6\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG6 = group=\"user/testCR_UG7\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG7 = group=\"user/testCR_UG8\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG8 = group=\"user/testCR_UG9\"\n"
        +   "ID null STATUS APPROVED component user/testCR_UG9 = group=\"user/testCR_UG1\"\n";
        try {
            importBulkPql(pql);
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("user/testCR%", ALL_TYPES, false);
            assertNotNull(all);
            assertEquals(9, all.size());
            client.getDependencies(all);
            fail("The code did not detect a circular reference");
        } catch (CircularReferenceException ce) {
            Collection<DomainObjectDescriptor> refs = ce.getChainOfReferences();
            assertNotNull(refs);
            assertEquals(9, refs.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testSelfCircularRef() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        String pql =
            "ID null STATUS APPROVED component user/testSCR_UG1 = false OR group=\"user/testSCR_UG2\"\n"
        +   "ID null STATUS APPROVED component user/testSCR_UG2 = empty";
        try {
            importBulkPql(pql);
            Collection<? extends IHasId> all = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "user/testSCR_UG1", "user/testSCR_UG2" }), EntityType.USER, false);
            assertNotNull(all);
            assertEquals(2, all.size());
            // Modify the first object to be self-referencing
            SpecBase spec = (SpecBase) all.iterator().next();
            assertNotNull(spec);
            assertNotNull( spec.getId() );
            assertTrue(spec.getPredicate() instanceof CompositePredicate);
            CompositePredicate comp = (CompositePredicate) spec.getPredicate();
            // Add the self-reference
            comp.addPredicate(comp);
            client.saveEntities(all); // This will result in a circular
            // reference exception
            fail("The code did not detect a circular reference");
        } catch (IllegalStateException ise) {
            assertNotNull(ise.getMessage());
            assertTrue(ise.getMessage().toLowerCase().indexOf("circular") != -1);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testPrepareEntitiesToDeploy() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        String pql =
        // User groups
            "id null status approved component user/dt1u1 = description \"First user for testing\" user.name=\"fuad\"\n"
        +   "id null status approved component user/dt1u2 = description \"Second user for testing\" user.name=\"safdar@bluejungle.com\"\n"
        +   "id null status approved component user/dt1u3 = description \"Third user for testing\" user.uid=\"S-1-5-21-668023798-3031861066-1043980994-3468\"\n"
        +   "id null status approved component user/dt1u4 = description \"Forth user for testing\" user.name=\"keni\"\n"
        +   "id null status approved component user/dt1ug1 = description \"Group of u1 OR u2\" group=\"user/dt1u1\" OR group=\"user/dt1u2\"\n"
        +   "id null status approved component user/dt1ug2 = description \"Group of u3 AND u4\" group=\"user/dt1u3\" AND NOT group=\"user/dt1u4\"\n"
        +   "id null status approved component user/dt1ugALL = description \"Group of all user groups\" group=\"user/dt1ug1\" OR group=\"user/dt1ug2\"\n"
        // Host groups
        +   "id null status approved component host/dt1h1 = description \"First host for testing\" host.name=\"123\"\n" + "id null status approved component host/dt1h2 = description \"Second host for testing\" host.name=\"456\"\n"
        +   "id null status approved component host/dt1h3 = description \"Third host for testing\" host.name=\"789\"\n"
        +   "id null status approved component host/dt1h4 = description \"Forth host for testing\" host.name=\"xyz\"\n"
        +   "id null status approved component host/dt1hg1 = description \"Group of h1 OR NOT h2\" group=\"host/dt1h1\" OR NOT group=\"host/dt1h2\"\n"
        +   "id null status approved component host/dt1hg2 = description \"Group of u3 AND u4\" group=\"host/dt1h3\" AND group=\"host/dt1h4\"\n"
        +   "id null status approved component host/dt1hgALL = description \"Group of all host groups\" group=\"host/dt1hg1\" OR group=\"host/dt1hg2\"\n"
        // Application groups
        +   "id null status approved component application/dt1a1 = description \"First application for testing\" application.name=\"notepad\"\n"
        +   "id null status approved component application/dt1a2 = description \"Second application for testing\" application.name=\"doom\"\n"
        +   "id null status approved component application/dt1a3 = description \"Third application for testing\" application.name=\"javac\"\n"
        +   "id null status approved component application/dt1a4 = description \"Forth application for testing\" application.name=\"msdev\"\n"
        +   "id null status approved component application/dt1ag1 = description \"Group of NOT a1 OR a2\" group!=\"application/dt1a1\" OR group=\"application/dt1a2\"\n"
        +   "id null status approved component application/dt1ag2 = description \"Group of NOT a3 AND a4\" group!=\"application/dt1a3\" AND group=\"application/dt1a4\"\n"
        +   "id null status approved component application/dt1agALL = description \"Group of all application groups\" group=\"application/dt1ag1\" OR group=\"application/dt1ag2\"\n"
        // Resource groups
        +   "id null status approved component resource/dt1r1 = description \"First resource for testing\" name=\"*.java\"\n"
        +   "id null status approved component resource/dt1r2 = description \"Second resource for testing\" directory=\"**\\\\test\\\\**\"\n"
        +   "id null status approved component resource/dt1r3 = description \"Third resource for testing\" owner!=\"administrator\"\n"
        +   "id null status approved component resource/dt1r4 = description \"Forth resource for testing\" size>1000\n"
        +   "id null status approved component resource/dt1rg1 = description \"First combined resource for testing\" resource/dt1r1 OR resource/dt1r2 OR resource/dt1r3 OR resource/dt1r4\n"
        +   "id null status approved component resource/dt1rg2 = description \"Second combined resource for testing\" resource/dt1r1 AND resource/dt1r2 AND resource/dt1r3 AND resource/dt1r4\n"
        // Action groups
        +   "id null status approved component action/dt1ac1 = description \"First action for testing\" create_new OR open OR delete\n"
        +   "id null status approved component action/dt1ac2 = description \"Second action for testing\" change_attributes OR change_security OR edit\n"
        +   "id null status approved component action/dt1ac3 = description \"Third action for testing\" edit_copy OR sendto OR cut_paste\n"
        +   "id null status approved component action/dt1ac4 = description \"Forth action for testing\" cut_paste OR batch OR burn OR print OR copy OR rename\n"
        +   "id null status approved component action/dt1acg1 = description \"First combined action for testing\" action/dt1ac1 OR action/dt1ac2 OR action/dt1ac3 OR action/dt1ac4\n"
        +   "id null status approved component action/dt1acg2 = description \"Second combined action for testing\" action/dt1ac1 AND action/dt1ac2 AND action/dt1ac3 AND action/dt1ac4\n"
        // Policy 1
        +   "id null status approved policy dt1p1 description \"First policy for testing. This policy references 28 objects.\"\n"
        +   "  DO ALLOW \"Policy \\\"dt1p1\\\" worked\"\n"
        +   "  FOR resource/dt1r1\n"
        +   "  ON action/dt1ac2\n"
        +   "  TO resource/dt1r3 OR resource/dt1rg2\n"
        +   "  BY group=\"user/dt1ug1\" AND group=\"application/dt1ag2\" AND group=\"host/dt1hg2\"\n"
        +   "  ON DENY DO LOG \"Access denied by \\\"dt1p1\\\"\", DONOTLOG\n"
        +   "  ON ALLOW DO LOG \"Access granted by \\\"dt1p1\\\"\", DONOTLOG\n"
        // Policy 2
        +   "id null status approved policy dt1p2 description \"Second policy for testing\"\n"
        +   "  DO ALLOW \"Policy \\\"dt1p2\\\" worked\"\n"
        +   "  FOR resource/dt1r1\n"
        +   "  ON action/dt1acg2\n"
        +   "  TO resource/dt1r2\n"
        +   "  BY group=\"user/dt1ugALL\" AND group=\"application/dt1agALL\" AND group=\"host/dt1hgALL\"\n"
        +   "  ON DENY DO LOG \"Access denied by \\\"dt1p2\\\"\", DONOTLOG\n"
        +   "  ON ALLOW DO LOG \"Access granted by \\\"dt1p2\\\"\", DONOTLOG\n"
        // Policy 3
        +   "id null status approved policy dt1p3 description \"Third policy for testing. This policy refers to all dt1<...> objects.\"\n"
        +   "  DO ALLOW \"Policy \\\"dt1p3\\\" worked\"\n"
        +   "  FOR resource/dt1rg1\n"
        +   "  ON action/dt1acg1 OR action/dt1acg2\n"
        +   "  TO resource/dt1rg2"
        +   "  BY group=\"user/dt1ugALL\" AND group=\"application/dt1agALL\" AND group=\"host/dt1hgALL\"\n"
        +   "  ON DENY DO LOG \"Access denied by \\\"dt1p3\\\"\", DONOTLOG\n"
        +   "  ON ALLOW DO LOG \"Access granted by \\\"dt1p3\\\"\", DONOTLOG\n"
        // Policy 4
        +   "id null status approved policy dt1p4 description \"Forth policy for testing. This policy references three objects.\"\n"
        +   "  DO ALLOW \"Policy \\\"dt1p4\\\" worked\"\n"
        +   "  FOR resource/dt1r1\n"
        +   "  ON action/dt1ac2\n"
        +   "  BY group=\"user/dt1u1\"\n"
        +   "  ON DENY DO LOG \"Access denied by \\\"dt1p4\\\"\", DONOTLOG\n"
        +   "  ON ALLOW DO LOG \"Access granted by \\\"dt1p4\\\"\", DONOTLOG\n"
        // Policy 5
        +   "id null status approved policy dt1p5 description \"Fifth policy for testing. This policy does not refer to other objects.\"\n"
        +   "  DO ALLOW \"Policy \\\"dt1p5\\\" worked\"\n"
        +   "  FOR *\n"
        +   "  ON *\n"
        +   "  BY *\n"
        +   "  ON DENY DO LOG \"Access denied by \\\"dt1p5\\\"\", DONOTLOG\n"
        +   "  ON ALLOW DO LOG \"Access granted by \\\"dt1p5\\\"\", DONOTLOG\n"
        +   "\n";
        try {
            importBulkPql(pql);
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testLeafObjectsForDescriptor() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> ug = client.getDescriptorsForNameAndType("user/dt1ugALL", EntityType.COMPONENT, false);
            assertNotNull(ug);
            assertEquals(1, ug.size());
            checkLeafObjects(
                client.getMatchingSubjects(
                    (DomainObjectDescriptor) ug.iterator().next()
                )
            ,   LeafObjectType.USER
            );
            Collection<DomainObjectDescriptor> hg = client.getDescriptorsForNameAndType("host/dt1hgALL", EntityType.COMPONENT, false);
            assertNotNull(hg);
            assertEquals(1, hg.size());
            checkLeafObjects(
                client.getMatchingSubjects(
                    (DomainObjectDescriptor) hg.iterator().next()
                )
            ,   LeafObjectType.HOST
            );
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testPreDeploymentDependencies() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            // Check forward dependencies of policies
            Collection<DomainObjectDescriptor> policyCollection = client.getDescriptorsForNameAndType("dt1p1", EntityType.POLICY, false);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            Collection<DomainObjectDescriptor> deps = client.getDependencies(policyCollection);
            assertNotNull(deps);
            assertEquals(15, deps.size());
            String[] names = new String[deps.size()];
            int i = 0;
            for (DomainObjectDescriptor dep : deps) {
                names[i++] = dep.getName();
            }
            Arrays.sort(names);
            // These are dependencies of dt1p1
            String[] need = new String[] {
                "resource/dt1r1", "action/dt1ac2",      "resource/dt1r2",    "resource/dt1r3"
            ,   "resource/dt1r4", "resource/dt1rg2",    "user/dt1ug1",       "user/dt1u1"
            ,   "user/dt1u2",     "application/dt1ag2", "application/dt1a3", "application/dt1a4"
            ,   "host/dt1hg2",    "host/dt1h3",         "host/dt1h4"
            };
            Arrays.sort(need);
            for (i = 0; i != need.length; i++) {
                assertEquals(need[i], names[i]);
            }
            policyCollection = client.getDescriptorsForNameAndType("dt1p2", EntityType.POLICY, false);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependencies(policyCollection);
            assertNotNull(deps);
            assertEquals(28, deps.size());
            policyCollection = client.getDescriptorsForNameAndType("dt1p3", EntityType.POLICY, false);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependencies(policyCollection);
            assertNotNull(deps);
            assertEquals(33, deps.size());
            policyCollection = client.getDescriptorsForNameAndType("dt1p4", EntityType.POLICY, false);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependencies(policyCollection);
            assertNotNull(deps);
            assertEquals(3, deps.size());
            policyCollection = client.getDescriptorsForNameAndType("dt1p5", EntityType.POLICY, false);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependencies(policyCollection);
            assertNotNull(deps);
            assertTrue(deps.isEmpty());
            // Check forward dependencies of non-policy groups
            Collection<DomainObjectDescriptor> hosts = client.getDescriptorsForNameAndType("host/dt1hg%", EntityType.COMPONENT, false);
            assertNotNull(hosts);
            assertEquals(3, hosts.size());
            deps = client.getDependencies(hosts);
            assertNotNull(deps);
            assertEquals(4, deps.size());
            Collection<DomainObjectDescriptor> users = client.getDescriptorsForNameAndType("user/dt1ug1", EntityType.COMPONENT, false);
            assertNotNull(users);
            assertEquals(1, users.size());
            deps = client.getDependencies(users);
            assertNotNull(deps);
            assertEquals(2, deps.size());
            users = client.getDescriptorsForNameAndType("user/dt1u1", EntityType.COMPONENT, false);
            assertNotNull(users);
            assertEquals(1, users.size());
            deps = client.getDependencies(users);
            assertNotNull(deps);
            assertTrue(deps.isEmpty());
            Collection<DomainObjectDescriptor> apps = client.getDescriptorsForNameAndType("application/dt1ag_", EntityType.COMPONENT, false);
            assertNotNull(apps);
            assertEquals(2, apps.size());
            deps = client.getDependencies(apps);
            assertNotNull(deps);
            assertEquals(4, deps.size());
            // Check backward dependencies of various groups
            Collection<DomainObjectDescriptor> bwDep = client.getReferringObjectsForGroup("user/dt1u1", EntityType.COMPONENT, EntityType.COMPONENT, false, true);
            assertNotNull(bwDep);
            assertEquals(2, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("user/dt1u1", EntityType.COMPONENT, EntityType.COMPONENT, true, true);
            assertNotNull(bwDep);
            assertEquals(1, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("resource/dt1r1", EntityType.COMPONENT, EntityType.COMPONENT, false, true);
            assertNotNull(bwDep);
            assertEquals(2, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("resource/dt1r1", EntityType.COMPONENT, EntityType.COMPONENT, true, true);
            assertNotNull(bwDep);
            assertEquals(2, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("resource/dt1r1", EntityType.COMPONENT, null, true, true);
            assertNotNull(bwDep);
            assertEquals(5, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("user/dt1u1", EntityType.COMPONENT, null, false, true);
            assertNotNull(bwDep);
            assertEquals(6, bwDep.size());
            bwDep = client.getReferringObjectsForGroup("user/dt1u1", EntityType.COMPONENT, null, true, true);
            assertNotNull(bwDep);
            assertEquals(2, bwDep.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        } catch (CircularReferenceException pe) {
            fail(pe.getMessage());
        }
    }

    public void testScheduleDeployment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("%dt1%", ALL_TYPES, false);
            assertNotNull(all);
            assertEquals(38, all.size());
            client.scheduleDeployment(all, date("01/01/3000 00:00:00"));
            Collection<DomainObjectDescriptor> deployedDescriptors = client.getDeployedObjectDescriptors(all, date("01/01/3000 00:01:00"));
            assertNotNull(deployedDescriptors);
            assertEquals(38, deployedDescriptors.size());
            client.scheduleDeployment(all, date("01/01/3100 00:00:00"));
            Collection<? extends IHasId> deployedObjects = client.getDeployedObjects(all, date("01/01/3100 00:01:00"));
            assertNotNull(deployedObjects);
            assertEquals(38, deployedObjects.size());
            // Verify that the objects reference other objects by name, not by
            // ID
            IPredicateVisitor visitor = new DefaultPredicateVisitor() {

                public void visit(IPredicateReference pred) {
                    if (pred instanceof SpecReference) {
                        assertTrue(((SpecReference) pred).isReferenceByName());
                    }
                }
            };
            for (IHasId obj : deployedObjects) {
                if (obj instanceof IDPolicy) {
                    IDPolicy p = (IDPolicy) obj;
                    if (p.getTarget() != null) {
                        IDTarget t = (IDTarget) p.getTarget();
                        if (t.getSubjectPred() != null) {
                            t.getSubjectPred().accept(visitor, IPredicateVisitor.PREORDER);
                        }
                        if (t.getActionPred() != null) {
                            t.getActionPred().accept(visitor, IPredicateVisitor.PREORDER);
                        }
                        if (t.getFromResourcePred() != null) {
                            t.getFromResourcePred().accept(visitor, IPredicateVisitor.PREORDER);
                        }
                        if (t.getToResourcePred() != null) {
                            t.getToResourcePred().accept(visitor, IPredicateVisitor.PREORDER);
                        }
                    }
                    if (p.getConditions() != null) {
                        p.getConditions().accept(visitor, IPredicateVisitor.PREORDER);
                    }
                    if (p.getDeploymentTarget() != null) {
                        p.getDeploymentTarget().accept(visitor, IPredicateVisitor.PREORDER);
                    }
                } else if (obj instanceof IDSpec) {
                    ((IDSpec) obj).accept(visitor, IPredicateVisitor.PREORDER);
                } else {
                    fail("Unexpected object type.");
                }
            }
            DomainObjectDescriptor dod = (DomainObjectDescriptor) all.iterator().next();
            Collection<DeploymentHistory> dh = client.getDeploymentHistory(dod);
            assertNotNull(dh);
            assertEquals(2, dh.size());
            // Verify that the deployment records are correct
            Collection<DeploymentRecord> drh = client.getDeploymentRecords(null, null);
            assertNotNull(drh);
            assertEquals(2, drh.size());
            DeploymentRecord[] dr = drh.toArray(new DeploymentRecord[2]);
            Collection<DomainObjectDescriptor> deployed = client.getDescriptorsForDeploymentRecord(dr[0]);
            assertNotNull(deployed);
            assertEquals(all.size(), deployed.size());
            String[] allNames = new String[all.size()];
            String[] depNames = new String[deployed.size()];
            Long[] allIDs = new Long[all.size()];
            Long[] depIDs = new Long[all.size()];
            Iterator<DomainObjectDescriptor> a = all.iterator();
            Iterator<DomainObjectDescriptor> d = deployed.iterator();
            for (int i = 0; i != allNames.length; i++) {
                DomainObjectDescriptor ad = a.next();
                allNames[i] = ad.getName();
                allIDs[i] = ad.getId();
                DomainObjectDescriptor dd = d.next();
                depNames[i] = dd.getName();
                depIDs[i] = dd.getId();
            }
            Arrays.sort(allNames);
            Arrays.sort(allIDs);
            Arrays.sort(depNames);
            Arrays.sort(depIDs);
            for (int i = 0; i != allNames.length; i++) {
                assertEquals(allNames[i], depNames[i]);
                assertEquals(allIDs[i], depIDs[i]);
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testReferringObjectsAsOf() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            // Check forward dependencies of policies
            Collection<DomainObjectDescriptor> appCollection = client.getDescriptorsForNameAndType("application/dt1a1", EntityType.COMPONENT, false);
            assertNotNull(appCollection);
            assertEquals(1, appCollection.size());
            Date asOf = date("01/01/4100 00:00:00");
            Collection<DomainObjectDescriptor> directRef = client.getReferringObjectsAsOf(appCollection, asOf, true);
            assertNotNull(directRef);
            assertEquals(1, directRef.size());
            Collection<DomainObjectDescriptor> allRef = client.getReferringObjectsAsOf(appCollection, asOf, false);
            assertNotNull(allRef);
            assertEquals(4, allRef.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        } catch (CircularReferenceException pe) {
            fail(pe.getMessage());
        }
    }

    public void testUsageForDescriptors() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> descriptors = client.getDescriptorsForNameAndTypes("%dt1%", ALL_TYPES, false);
            assertNotNull(descriptors);
            assertFalse(descriptors.isEmpty());
            List<DomainObjectDescriptor> descrList = new ArrayList<DomainObjectDescriptor>( descriptors );
            Collection<DomainObjectUsage> usage = client.getUsageList(descrList);
            assertNotNull(usage);
            assertFalse(usage.isEmpty());
            assertEquals(descriptors.size(), usage.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testCancelScheduledDeployment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DeploymentRecord> dh = client.getDeploymentRecords(null, null);
            assertNotNull(dh);
            assertEquals(2, dh.size());
            DeploymentRecord dr = (DeploymentRecord) dh.iterator().next();
            client.cancelScheduledDeployment(dr);
            dh = client.getDeploymentRecords(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
            // There should be still only two deployment records,...
            assertNotNull(dh);
            assertEquals(2, dh.size());
            DeploymentRecord[] drs = dh.toArray(new DeploymentRecord[dh.size()]);
            // But the record that we've cancelled must now tell us when it was
            // cancelled.
            assertNotNull(drs[0].getWhenCancelled());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testScheduleUndeployment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("%dt1%", ALL_TYPES, false);
            assertNotNull(all);
            // Mark all objects obsolete
            assertEquals(38, all.size());
            Collection<? extends IHasId> allObjects = client.getEntitiesForDescriptors(all);
            assertEquals(38, allObjects.size());
            for (IHasId o : allObjects) {
                if (o instanceof IDPolicy) {
                    ((IDPolicy) o).setStatus(DevelopmentStatus.OBSOLETE);
                } else if (o instanceof IDSpec) {
                    ((IDSpec) o).setStatus(DevelopmentStatus.OBSOLETE);
                } else {
                    fail("Unexpected object found");
                }
            }
            // Save the obsoleted objects
            client.saveEntities(allObjects);
            // Schedule the undeployment
            client.scheduleUndeployment(all, date("01/01/4000 00:00:00"));
            Collection<DomainObjectDescriptor> deployedDescriptors = client.getDeployedObjectDescriptors(all, date("01/01/4000 00:01:00"));
            assertNotNull(deployedDescriptors);
            assertTrue(deployedDescriptors.isEmpty());
            // Our objects should have two deployment actions
            DomainObjectDescriptor dod = (DomainObjectDescriptor) all.iterator().next();
            Collection<DeploymentHistory> dh = client.getDeploymentHistory(dod);
            assertNotNull(dh);
            assertEquals(2, dh.size());
            // The undeployment should add a new record
            Collection<DeploymentRecord> dr = client.getDeploymentRecords(null, null);
            assertNotNull(dr);
            assertEquals(3, dr.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testCancelScheduledUndeployment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DeploymentRecord> dh = client.getDeploymentRecords(null, null);
            assertNotNull(dh);
            assertEquals(3, dh.size());
            DeploymentRecord[] dr = (DeploymentRecord[]) dh.toArray(new DeploymentRecord[dh.size()]);
            assertNotNull(dr[2]);
            assertEquals(38, dr[2].getNumberOfDeployedEntities());
            client.cancelScheduledDeployment(dr[2]);
            dh = client.getDeploymentRecords(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
            assertNotNull(dh);
            assertEquals(3, dh.size());
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("%dt1%", ALL_TYPES, false);
            assertNotNull(all);
            assertEquals(38, all.size());
            Collection<DomainObjectDescriptor> deployedDescriptors = client.getDeployedObjectDescriptors(all, date("01/01/4000 00:01:00"));
            assertNotNull(deployedDescriptors);
            assertEquals(38, deployedDescriptors.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testPostDeploymentDependencies() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            // Check forward dependencies of policies
            Collection<DomainObjectDescriptor> policyCollectionDev = client.getDescriptorsForNameAndType("dt1p1", EntityType.POLICY, false);
            assertNotNull(policyCollectionDev);
            assertEquals(1, policyCollectionDev.size());
            Date asOf = date("01/01/4100 00:00:00");
            Collection<DomainObjectDescriptor> policyCollection = client.getDeployedObjectDescriptors(policyCollectionDev, asOf);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            Collection<DomainObjectDescriptor> deps = client.getDependenciesAsOf(policyCollection, asOf);
            assertNotNull(deps);
            assertEquals(15, deps.size());
            String[] names = new String[deps.size()];
            int i = 0;
            for (DomainObjectDescriptor dep : deps) {
                names[i++] = dep.getName();
            }
            Arrays.sort(names);
            // These are dependencies of dt1p1
            String[] need = new String[] {
                "resource/dt1r1", "action/dt1ac2",      "resource/dt1r2",    "resource/dt1r3"
            ,   "resource/dt1r4", "resource/dt1rg2",    "user/dt1ug1",       "user/dt1u1"
            ,   "user/dt1u2",     "application/dt1ag2", "application/dt1a3", "application/dt1a4"
            ,   "host/dt1hg2",    "host/dt1h3",         "host/dt1h4" };
            Arrays.sort(need);
            for (i = 0; i != need.length; i++) {
                assertEquals(need[i], names[i]);
            }
            policyCollectionDev = client.getDescriptorsForNameAndType("dt1p2", EntityType.POLICY, false);
            assertNotNull(policyCollectionDev);
            assertEquals(1, policyCollectionDev.size());
            policyCollection = client.getDeployedObjectDescriptors(policyCollectionDev, asOf);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependenciesAsOf(policyCollection, asOf);
            assertNotNull(deps);
            assertEquals(28, deps.size());
            policyCollectionDev = client.getDescriptorsForNameAndType("dt1p3", EntityType.POLICY, false);
            assertNotNull(policyCollectionDev);
            assertEquals(1, policyCollectionDev.size());
            policyCollection = client.getDeployedObjectDescriptors(policyCollectionDev, asOf);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependenciesAsOf(policyCollection, asOf);
            assertNotNull(deps);
            assertEquals(33, deps.size());
            policyCollectionDev = client.getDescriptorsForNameAndType("dt1p4", EntityType.POLICY, false);
            assertNotNull(policyCollectionDev);
            assertEquals(1, policyCollectionDev.size());
            policyCollection = client.getDeployedObjectDescriptors(policyCollectionDev, asOf);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependenciesAsOf(policyCollection, asOf);
            assertNotNull(deps);
            assertEquals(3, deps.size());
            policyCollectionDev = client.getDescriptorsForNameAndType("dt1p5", EntityType.POLICY, false);
            assertNotNull(policyCollectionDev);
            assertEquals(1, policyCollectionDev.size());
            policyCollection = client.getDeployedObjectDescriptors(policyCollectionDev, asOf);
            assertNotNull(policyCollection);
            assertEquals(1, policyCollection.size());
            deps = client.getDependenciesAsOf(policyCollection, asOf);
            assertNotNull(deps);
            assertTrue(deps.isEmpty());
            // Check forward dependencies of non-policy groups
            Collection<DomainObjectDescriptor> hostsDev = client.getDescriptorsForNameAndType("host/dt1hg%", EntityType.COMPONENT, false);
            assertNotNull(hostsDev);
            assertEquals(3, hostsDev.size());
            Collection<DomainObjectDescriptor> hosts = client.getDeployedObjectDescriptors(hostsDev, asOf);
            assertNotNull(hosts);
            assertEquals(3, hosts.size());
            deps = client.getDependenciesAsOf(hosts, asOf);
            assertNotNull(deps);
            assertEquals(4, deps.size());
            Collection<DomainObjectDescriptor> usersDev = client.getDescriptorsForNameAndType("user/dt1ug1", EntityType.COMPONENT, false);
            assertNotNull(usersDev);
            assertEquals(1, usersDev.size());
            Collection<DomainObjectDescriptor> users = client.getDeployedObjectDescriptors(usersDev, asOf);
            assertNotNull(users);
            assertEquals(1, users.size());
            deps = client.getDependenciesAsOf(users, asOf);
            assertNotNull(deps);
            assertEquals(2, deps.size());
            usersDev = client.getDescriptorsForNameAndType("user/dt1u1", EntityType.COMPONENT, false);
            assertNotNull(usersDev);
            assertEquals(1, usersDev.size());
            users = client.getDeployedObjectDescriptors(usersDev, asOf);
            assertNotNull(users);
            assertEquals(1, users.size());
            deps = client.getDependenciesAsOf(users, asOf);
            assertNotNull(deps);
            assertTrue(deps.isEmpty());
            Collection<DomainObjectDescriptor> appsDev = client.getDescriptorsForNameAndType("application/dt1ag_", EntityType.COMPONENT, false);
            assertNotNull(appsDev);
            assertEquals(2, appsDev.size());
            Collection<DomainObjectDescriptor> apps = client.getDeployedObjectDescriptors(appsDev, asOf);
            assertNotNull(apps);
            assertEquals(2, apps.size());
            deps = client.getDependenciesAsOf(apps, asOf);
            assertNotNull(deps);
            assertEquals(4, deps.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        } catch (CircularReferenceException pe) {
            fail(pe.getMessage());
        }
    }

    public void testMatchingActions() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> ag = client.getDescriptorsForNameAndType("action/dt1acg2", EntityType.COMPONENT, false);
            assertNotNull(ag);
            assertEquals(1, ag.size());
            DomainObjectDescriptor descr = ((DomainObjectDescriptor) ag.iterator().next());
            Collection<? extends IAction> act = client.getMatchingActions(descr);
            assertNotNull(act);
            String[] expected = new String[] { "CREATE_NEW", "OPEN", "DELETE", "CHANGE_ATTRIBUTES", "CHANGE_SECURITY", "EDIT", "EDIT_COPY", "SENDTO", "CUT_PASTE", "BATCH", "BURN", "PRINT", "COPY", "RENAME" };
            assertEquals(expected.length, act.size());
            for (int i = 0; i != expected.length; i++) {
                assertTrue(expected[i] + " is missing", act.contains(DAction.getAction(expected[i])));
            }
        } catch (PolicyEditorException e) {
            fail(e.getMessage());
        }
    }

    public void testMultiplyingSlashes() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            importBulkPql("id null status approved component resource/btx01_01 = \n" + "resource.name=\"\\\\\\\\pukapuka\\\\data\\\\\"");
            String resourceName = "\"\\\\\\\\pukapuka\\\\data\\\\\"";
            Collection<? extends IHasId> resources = null;
            for (int i = 0; i != 10; i++) {
                resources = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "resource/btx01_01" }), EntityType.COMPONENT, false);
                assertNotNull(resources);
                assertEquals(1, resources.size());
                SpecBase spec = (SpecBase) resources.iterator().next();
                spec.setDescription("" + i);
                IPredicate pred = (IPredicate) resources.iterator().next();
                assertNotNull(pred);
                assertTrue(pred instanceof SpecBase);
                pred = spec.getPredicate();
                assertTrue(pred instanceof IRelation);
                IExpression lhs = ((IRelation) pred).getLHS();
                assertNotNull(lhs);
                IExpression rhs = ((IRelation) pred).getRHS();
                assertNotNull(rhs);
                assertTrue(rhs instanceof Constant);
                client.saveEntities(resources);
                assertEquals(resourceName, rhs.toString());
            }
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testNPEInResolveName() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        String pql =
            "ID null STATUS APPROVED POLICY testNPEInResolveName\n"
        +   "FOR (TRUE AND TRUE)\n"
        +   "ON (FALSE OR read)\n"
        +   "TO (TRUE AND TRUE)\n"
        +   "BY ((TRUE AND (FALSE OR group=\"user/testNPEInResolveName_UG\")) AND (TRUE AND TRUE) AND (TRUE AND TRUE))\n"
        +   "DO deny\n"
        +   "ID null STATUS APPROVED component user/testNPEInResolveName_UG = (((FALSE OR TRUE) AND TRUE) AND (TRUE AND TRUE))";
        try {
            importBulkPql(pql);
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("%testNPEInResolveName%", ALL_TYPES, false);
            assertNotNull(all);
            assertEquals(2, all.size());
            DomainObjectDescriptor descriptor = (DomainObjectDescriptor) client.getDescriptorsForNameAndType("testNPEInResolveName", EntityType.POLICY, false).iterator().next();
            client.scheduleDeployment(all, date("01/01/3000 19:00:00"));
            Collection<DeploymentHistory> ranges = client.getDeploymentHistory(descriptor);
            assertNotNull( ranges );
            Collection<DeploymentHistory> depRecs = client.getDeploymentHistory(descriptor);
            assertNotNull(depRecs);
            assertEquals(1, depRecs.size());
            TimeRelation tr = ((DeploymentHistory) depRecs.iterator().next()).getTimeRelation();
            assertNotNull(tr.getActiveFrom());
            assertNotNull(tr.getActiveTo());
            Collection<? extends IHasId> deployedObjects = client.getDeployedObjects(all, tr.getActiveFrom());
            assertNotNull(deployedObjects);
            assertEquals(2, deployedObjects.size());
            deployedObjects = client.getDeployedObjects(all, new Date());
            assertNotNull(deployedObjects);
            assertTrue(deployedObjects.isEmpty());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testHasObjectsToDeploy() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> candidates1 = client.getDescriptorsForState( DevelopmentStatus.APPROVED, false );
            assertNotNull( candidates1 );
            Collection<DomainObjectDescriptor> candidates2 = client.getDescriptorsForState( DevelopmentStatus.OBSOLETE, false );
            assertNotNull( candidates2 );
            Collection<DomainObjectDescriptor> toDeploy = new ArrayList<DomainObjectDescriptor>( candidates1.size() );
            for ( DomainObjectDescriptor dod : candidates1) {
                if ( dod.getType() != EntityType.FOLDER ) {
                    toDeploy.add( dod );
                }
            }
            assertFalse( toDeploy.isEmpty() );
            Collection<DomainObjectDescriptor> toUndeploy = new ArrayList<DomainObjectDescriptor>( candidates2.size() );
            for (DomainObjectDescriptor dod : candidates2) {
                if ( dod.getType() != EntityType.FOLDER ) {
                    toUndeploy.add( dod );
                }
            }
            assertFalse( toUndeploy.isEmpty() );
            // We should have some objects to deploy
            assertTrue( client.hasObjectsToDeploy() );
            // Now deploy the objects that need deployment
            client.scheduleDeployment( toDeploy, date("01/23/3456 12:34:56") );
            client.scheduleUndeployment( toUndeploy, date("01/23/3456 12:34:56") );
            // All the approved objects should be deployed now
            assertFalse( client.hasObjectsToDeploy() );
        } catch ( PolicyEditorException pe ) {
            fail( pe.getMessage() );
        }
    }

    public void testCircularRefInDeployment() {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            Collection<DomainObjectDescriptor> all = client.getDescriptorsForNameAndTypes("%/testCR%", ALL_TYPES, false);
            assertNotNull(all);
            assertEquals(9, all.size());
            Date when = date("01/23/4567 12:34:56");
            client.scheduleDeployment(all, when);
            client.getDependenciesAsOf(all, when);
            fail("The code did not detect a circular reference");
        } catch (CircularReferenceException ce) {
            Collection<DomainObjectDescriptor> refs = ce.getChainOfReferences();
            assertNotNull(refs);
            assertEquals(9, refs.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testNewFolder() {
        assertNotNull(client);
        assertTrue( client.isLoggedIn() );
        try {
            Collection<String> names = Arrays.asList(new String[] { "NewFolder" });
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
            client.saveEntities(res);
            Object[] tmp = res.toArray();
            assert tmp.length == 1; // No, this is not a test assertion :-)
            assertTrue(tmp[0] instanceof PolicyFolder);
            assertNotNull(((PolicyFolder) tmp[0]).getId());
            // Re-read the folder
            res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
            client.saveEntities(res);
            tmp = res.toArray();
            assert tmp.length == 1; // No, this is not a test assertion :-)
            assertTrue(tmp[0] instanceof PolicyFolder);
            assertNotNull(((PolicyFolder) tmp[0]).getId());
            // Re-read the folder again - there should be only one
            res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    public void testFolderNameWithSpaces() {
        assertNotNull(client);
        assertTrue( client.isLoggedIn() );
        try {
            Collection<String> names = Arrays.asList(new String[] { "New Folder" });
            Collection<? extends IHasId> res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
            client.saveEntities(res);
            Object[] tmp = res.toArray();
            assert tmp.length == 1; // No, this is not a test assertion :-)
            assertTrue(tmp[0] instanceof PolicyFolder);
            assertNotNull(((PolicyFolder) tmp[0]).getId());
            // Re-read the folder
            res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
            client.saveEntities(res);
            tmp = res.toArray();
            assert tmp.length == 1; // No, this is not a test assertion :-)
            assertTrue(tmp[0] instanceof PolicyFolder);
            assertNotNull(((PolicyFolder) tmp[0]).getId());
            // Re-read the folder again - there should be only one
            res = client.getEntitiesForNamesAndType(names, EntityType.FOLDER, false);
            assertNotNull(res);
            assertEquals(1, res.size());
        } catch (PolicyEditorException pe) {
            fail(pe.getMessage());
        }
    }

    /*
    public void testLocking() throws ServiceException, DuplicateLoginNameException, RemoteException, LoginException, PolicyEditorException {
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        
        // Create another client
        // Create User
        UserRoleServiceIF userRoleService = new UserRoleServiceLocator().getUserRoleServiceIFPort();
        UserDTO newUser = new UserDTO();
        newUser.setUid("loser");
        newUser.setPassword("loser");
        DMSUserData userData = new DMSUserData();
        Role[] roles = {Role.System_Administrator}; 
        userData.setRoles(roles);
        userRoleService.createUser(newUser, userData);
        IPolicyEditorClient loserClient = PolicyEditorClient.create("http://localhost:8444", "loser", "loser");
        

        // We'll start with two objects
        Collection saved = client.getEntitiesForNamesAndType(Arrays.asList(new String[] { "one", "two" }), EntityType.USER, false);
        assertNotNull(saved);
            assertEquals(2, saved.size());
        Iterator iter = saved.iterator();
        IHasId one = (IHasId) iter.next();
        IHasId two = (IHasId) iter.next();

        // Neither of the objects is locked
        assertFalse(client.hasLock(one));
        assertFalse(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));
        
        // Administrator successfully acquires the lock for object "one"
        // Now the object "one is locked by the Administrator
        // ...and the object "two" is not locked.
        client.acquireLock(one, false);
        assertTrue(client.hasLock(one));
        assertFalse(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));

        // Now looser tries to acquire a lock for object "one", but it's
        // locked by the Administrator
        // ...and the object "one" is still locked by the Administrator,
        // ...and the object "two" is not locked.
        try {
            loserClient.acquireLock(one, false);
            fail("Should throw PolicyEditorException for attempting to acquire locked object one");
        } catch (PolicyEditorException exception) {
        }
        assertTrue(client.hasLock(one));
        assertFalse(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));

        // Looser successfully acquires a lock for the object "two"
        // Now Administrator holds the lock for "one", while
        // Looser holds the lock for two.
        loserClient.acquireLock(two, false);
        assertTrue(client.hasLock(one));
        assertFalse(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertTrue(loserClient.hasLock(two));

        // Administrator successfully forces a lock override for "two"
        // Now Administrator holds the locks for both objects
        client.acquireLock(two, true);
        assertTrue(client.hasLock(one));
        assertTrue(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));

        // Looser tries to release the lock for "two", but the release is
        // ignored
        loserClient.releaseLock(two);
        assertTrue(client.hasLock(one));
        assertTrue(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));

        // Administrator successfully releases locks for both objects.
        // Both objects are unlocked again.
        client.releaseLock(one);
        client.releaseLock(two);
        assertFalse(client.hasLock(one));
        assertFalse(client.hasLock(two));
        assertFalse(loserClient.hasLock(one));
        assertFalse(loserClient.hasLock(two));
    }
    */
    
    private void importBulkPql(String pql) throws PolicyEditorException {
        assertNotNull(client);
        try {
            // Parse PQL into elements
            final Map<String,Object>[] newEntities = new Map[EntityType.getElementCount()];
            final ArrayList<String>[] names = new ArrayList[EntityType.getElementCount()];
            for (int i = 0; i != names.length; i++) {
                newEntities[i] = new HashMap<String,Object>();
                names[i] = new ArrayList<String>();
            }
            DomainObjectBuilder.processInternalPQL(pql, new IPQLVisitor() {

                public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                    names[descr.getType().getType()].add(descr.getName());
                    newEntities[descr.getType().getType()].put(descr.getName(), policy);
                }

                public void visitComponent(DomainObjectDescriptor descr, IPredicate spec) {
                    names[descr.getType().getType()].add(descr.getName());
                    newEntities[descr.getType().getType()].put(descr.getName(), new SpecBase(null, SpecType.ILLEGAL, descr.getId(), descr.getName(), descr.getDescription(), descr.getStatus(), spec, descr.isHidden()));
                }

                public void visitLocation(DomainObjectDescriptor descr, Location location) {
                    // Locations are ignored
                }

                public void visitFolder(DomainObjectDescriptor descriptor) {
                    // Policy Folders are ignored
                }

                public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                    // Free-standing access policies are ignored
                }
            });

            // Obtain entities for each type
            for (int i = 0; i != names.length; i++) {
                if (names[i].isEmpty()) {
                    continue;
                }
                final Map<String,Object> entitiesOfThisType = newEntities[i];
                EntityType type = EntityType.forType(i);
                String savedEntitiesPql = DTOUtils.makePql(client.getEntitiesForNamesAndType(names[i], type, false));
                final List<IHasId> toSave = new ArrayList<IHasId>(names[i].size());
                // Update entities with the given PQL
                DomainObjectBuilder.processInternalPQL(savedEntitiesPql, new IPQLVisitor() {

                    public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
                        processAccessControlled(descriptor);
                        Policy existing = (Policy) entitiesOfThisType.get(descriptor.getName());
                        existing.setId(descriptor.getId());
                    }

                    public void visitComponent(DomainObjectDescriptor descriptor, IPredicate spec) {
                        processAccessControlled(descriptor);
                        processSpecBase(descriptor);
                    }

                    public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                        // Locations are ignored
                    }

                    public void visitFolder(DomainObjectDescriptor descriptor) {
                        // Policy Folders are ignored
                    }

                    public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                        // Free-standing access policies are ignored
                    }

                    private void processAccessControlled(DomainObjectDescriptor descr) {
                        IAccessControlled toAdd = (IAccessControlled) entitiesOfThisType.get(descr.getName());
                        toAdd.setAccessPolicy((AccessPolicy) descr.getAccessPolicy());
                        if (descr.getOwner() != null) {
                            toAdd.setOwner(new Subject(descr.getOwner().toString(), descr.getOwner().toString(), descr.getOwner().toString(), descr.getOwner(), SubjectType.USER));
                        }
                        toSave.add((IHasId)toAdd);
                    }

                    private void processSpecBase(DomainObjectDescriptor descr) {
                        SpecBase existing = (SpecBase) entitiesOfThisType.get(descr.getName());
                        existing.setId(descr.getId());
                    }
                });
                // Save the results
                client.saveEntities(toSave);
            }
        } catch (PQLException pqlEx) {
            throw new PolicyEditorException(pqlEx);
        }
    }

    private static Date date(String s) {
        try {
            return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(s);
        } catch (ParseException dfe) {
            return null;
        }
    }

    private static Collection<EntityType> ALL_TYPES = Arrays.asList(new EntityType[] {
        EntityType.ACTION
    ,   EntityType.APPLICATION
    ,   EntityType.HOST
    ,   EntityType.RESOURCE
    ,   EntityType.USER
    ,   EntityType.POLICY
    } );

    /**
     * Deletes the file or the directory represented by the path. NOTE: This
     * method also deletes non-empty directories.
     * 
     * @param path
     *            the file or the directory to be deleted.
     */
    private static void deleteAll(File path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (path.isDirectory()) {
            // Directories must be empty before they are deleted
            File[] children = path.listFiles();
            for (int i = 0; i != children.length; i++) {
                deleteAll(children[i]);
            }
        }
        path.delete();
    }

    public void testExecutePush(){
        assertNotNull(client);
        assertTrue(client.isLoggedIn());
        try {
            client.executePush();
        } catch (PolicyEditorException pe) {
        	pe.printStackTrace();
            fail(pe.getMessage());
        }
    }
}
