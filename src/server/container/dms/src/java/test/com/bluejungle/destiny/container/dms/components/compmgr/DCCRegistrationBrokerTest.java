/*
 * Created on Jan 12, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.components.BaseDMSComponentTest;
import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatInfoImpl;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationInfoImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;
import com.nextlabs.random.RandomString;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class DCCRegistrationBrokerTest extends BaseDMSComponentTest {

    protected MockEventManager eventMgr;
    protected IDCCRegistrationBroker registrationBroker;
    protected IDCCComponentMgr compMgr;
    protected IDCCEventRegistrationMgr eventRegMgr;
    protected LifecycleManager lifecycleManager;
    private static final int major = 2;
    private static final int minor = 12;
    private static final int maintenance = 1;
    private static final int patch = 3;
    private static final int build = 329;
    private static final IVersion version = new VersionDefaultImpl(major, minor, maintenance, patch, build);

    /**
     * Constructor.
     * 
     * @param testName
     *            the name of the test
     */
    public DCCRegistrationBrokerTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the test
     * 
     * @throws Exception
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IDestinySharedContextLocator> locatorInfo = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = compMgr.getComponent(locatorInfo);

        // Get the event manager:
        IDestinySharedContext sharedCtx = locator.getSharedContext();
        this.eventMgr = (MockEventManager) sharedCtx.getEventManager();

        // Initialize the Configuration Manager so that the registration broker
        // can use it:
        ComponentInfo<IDestinyConfigurationManager> configMgrCompInfo = 
            new ComponentInfo<IDestinyConfigurationManager>(
                IDestinyConfigurationManager.COMP_NAME, 
                MockConfigurationManagerImpl.class, 
                IDestinyConfigurationManager.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinyConfigurationManager configMgr = compMgr.getComponent(configMgrCompInfo);

        // Initialize the Registration broker:
        ComponentInfo<IDCCRegistrationBroker> compFacadeInfo = 
            new ComponentInfo<IDCCRegistrationBroker>(
                IDCCRegistrationBroker.COMP_NAME, 
                DCCRegistrationBrokerImpl.class, 
                IDCCRegistrationBroker.class, 
                LifestyleType.SINGLETON_TYPE);
        this.registrationBroker = compMgr.getComponent(compFacadeInfo);

        // Obtain a handle to the event reg mgr and comp mgr objects that were
        // initialized by the registration broker:
        this.eventRegMgr = (IDCCEventRegistrationMgr) compMgr.getComponent(IDCCEventRegistrationMgr.COMP_NAME);
        this.compMgr = (IDCCComponentMgr) compMgr.getComponent(IDCCComponentMgr.COMP_NAME);
        
        this.lifecycleManager = compMgr.getComponent(LifecycleManager.COMP_INFO);

        // Clear all the database tables - we call this method by casting to the
        // right type since the facade interface doesn't expose this method:
        ((DCCRegistrationBrokerImpl) this.registrationBroker).clearAll();
        deleteAllAppResourceComponents(compMgr);
    }
    
    private void deleteAllAppResourceComponents(IComponentManager compMgr)
            throws HibernateException, EntityManagementException {
        IHibernateRepository repository = (IHibernateRepository) compMgr.getComponent(
                DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        Collection<DevelopmentEntity> existing = lifecycleManager.getAllApplicatinResourceComponents();
        if(existing.isEmpty()){
            return;
        }
        
        boolean isSuccess = false;
        Session s = null;
        Transaction t = null;
        try {
            
            s = repository.getCurrentSession();
            t = s.beginTransaction();
            for(DevelopmentEntity e : existing){
                s.delete(e);
            }
            isSuccess = true;
        } finally {
            try {
                if(t!= null){
                    if (isSuccess) {
                        t.commit();
                    } else {
                        t.rollback();
                    }
                }
            } finally {
                repository.closeCurrentSession();
            }
        }
    }

    /**
     * Tests component registration
     *  
     */
    public void testRegisterComponent() {
        // Try registering for a DCSF component that has a config file:
        DCCRegistrationInfoImpl regInfo = new DCCRegistrationInfoImpl(
                "dcsf-1" // String name
              , ServerComponentType.DCSF // ServerComponentType type
              , "Communication Server" // String displayName
              , createURL("http://www1.mockURI.for.dcsf-1") // URL url
              , createURL("http://www1.mockURI.for.dcsf-1") // URL listenerURL
              , version //IVersion version
              , null //Set<String> applicationResouces        
        
        );

        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            assertEquals("DCC component registration was supposed to succeed but failed", 
                    DMSRegistrationResult.SUCCESS, status.getRegistrationResult());
            assertEquals("DCC Component registration did not cause the right number of update events to be fired", 
                    0, this.eventMgr.getNumEventsFired());
            verifyComponentRegistration(regInfo, true, true);

            //Reset the num of events fired:
            this.eventMgr.reset();
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Now, re-register as the same component as before ("dcsf-1") after
        // changing the callback URL. We should not get an error. However, we
        // should get 2 fired events (one for unregistration of the old
        // component, and one for re-registration since its callback URL
        // changed):
        regInfo.setEventListenerURL(this.createURL("http://www2.mockURI.for.dcsf-1"));
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            assertEquals("DCC component registration should succeed", 
                    DMSRegistrationResult.SUCCESS, status.getRegistrationResult());
            assertEquals("DCC Component registration should cause the right number of update events to be fired", 
                    1, this.eventMgr.getNumEventsFired());
            this.verifyComponentRegistration(regInfo, true, true);

            //Reset the num of events fired:
            this.eventMgr.reset();
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Now re-register the same component without changing anything. We
        // should get no error, but no event should be fired either:
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            assertEquals("DCC component registration should succeed ",
                    DMSRegistrationResult.SUCCESS, status.getRegistrationResult());
            assertEquals("DCC Component registration should cause the right number of update events to be fired", 
                    0, this.eventMgr.getNumEventsFired());
            this.verifyComponentRegistration(regInfo, true, true);

            //Reset the num of events fired:
            this.eventMgr.reset();
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Ensure component was registered:
        try {
            ComponentDO component = this.compMgr.getComponentByName(regInfo.getComponentName());
            this.verifyComponentRegistration(regInfo, true, true);
        } catch (DataSourceException e) {
            assertTrue(e.toString(), false);
        }

        // Now register a new DCSF componnent (with a config file) that has the
        // same callback as the previous dcsf:
        // FIX ME - This functionality is currently removed from the test suite.  See Bug #2416
        /*
        regInfo.setComponentName("dcsf2-1");
        regInfo.setEventListenerURL(this.createURL("http://www2.mockURI.for.dcsf-1"));
        regInfo.setComponentURL(this.createURL("http://www2.mockURI.for.dcsf-1"));
        regInfo.setComponentType(ServerComponentType.DCSF);
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);

            // We should never hit this code, otherwise it's an error:
            assertTrue("DCC component registration was supposed to fail but it succeeded", false);
        } catch (ComponentRegistrationException e) {
            assertEquals("A failed DCC component registration should not fire an update event", 
                    0, this.eventMgr.getNumEventsFired());
            this.verifyComponentRegistration(regInfo, false, false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }
        */
        
        // Try registering for a non-DCSF component that has a config file. We
        // use the same URI as the DCSF since we assume this DMS is co-located.
        // This should NOT throw an error.
        regInfo.setEventListenerURL(this.createURL("http://www2.mockURI.for.dcsf-1"));
        regInfo.setComponentURL(this.createURL("http://www2.mockURI.for.dms"));
        regInfo.setComponentName("dms-1");
        regInfo.setComponentType(ServerComponentType.DMS);
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            assertEquals("DCC component registration should succeed", 
                    DMSRegistrationResult.SUCCESS, status.getRegistrationResult());
            assertEquals("DCC Component registration should cause the right number of update events to be fired", 
                    0, this.eventMgr.getNumEventsFired());
            this.verifyComponentRegistration(regInfo, true, true);

            //Reset the num of events fired:
            this.eventMgr.reset();
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Try registering a valid component with insufficeint registration
        // information:
        regInfo.setComponentName("dcsf-1");
        regInfo.setComponentType(ServerComponentType.DMS);
        regInfo.setEventListenerURL(null); // This should cause an error
        regInfo.setComponentURL(this.createURL("http://www2.mockURI.for.dcsf-1"));
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);

            // We should never hit this code, otherwise it's an error:
            assertTrue("DCC component registration was supposed to fail but it succeeded", false);
        } catch (ComponentRegistrationException e) {
            assertEquals("A failed DCC component registration should not fire an update event", 
                    0, this.eventMgr.getNumEventsFired());
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }
    }

    /**
     * Tests component unregistration
     *  
     */
    public void testUnregisterComponent() {
        DCCRegistrationInfoImpl regInfo = new DCCRegistrationInfoImpl();

        // Register a valid DCSF component and then unregister it:
        regInfo.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        regInfo.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        regInfo.setComponentName("dcsf-1");
        regInfo.setComponentTypeDisplayName("Communication Server");
        regInfo.setComponentType(ServerComponentType.DCSF);
        regInfo.setComponentVersion(version);
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            this.verifyComponentRegistration(regInfo, true, true);
            this.registrationBroker.unregisterComponent(regInfo);
            this.verifyComponentRegistration(regInfo, true, false);
            assertEquals("DCC Component registration should cause the right number of update events to be fired", 
                    1, this.eventMgr.getNumEventsFired());

            //Reset the num of events fired:
            this.eventMgr.reset();
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Register and then un-register a non-dcsf component:
        regInfo.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        regInfo.setComponentURL(this.createURL("http://www1.mockURI.for.dms"));
        regInfo.setComponentName("dms-1");
        regInfo.setComponentType(ServerComponentType.DMS);
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            this.verifyComponentRegistration(regInfo, true, true);
            this.registrationBroker.unregisterComponent(regInfo);
            this.verifyComponentRegistration(regInfo, true, false);
            assertEquals("DCC Component registration did not cause the right number of update events to be fired", 
                    0, this.eventMgr.getNumEventsFired());
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }

        // Unregister a component that never existed:
        regInfo.setComponentName("dcsf-43434");
        regInfo.setComponentType(ServerComponentType.DMS);
        try {
            this.registrationBroker.unregisterComponent(regInfo);
            this.verifyComponentRegistration(regInfo, false, false);
            assertEquals("DCC Component registration did not cause the right number of update events to be fired", 
                    0, this.eventMgr.getNumEventsFired());
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        }
    }

    /**
     * Tests event registration
     *  
     */
    public void testEventRegistration() {
        DCCRegistrationInfoImpl regInfo = new DCCRegistrationInfoImpl();

        // Register any component:
        regInfo.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        regInfo.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        regInfo.setComponentName("dcsf-1");
        regInfo.setComponentTypeDisplayName("Communication Server");
        regInfo.setComponentType(ServerComponentType.DCSF);
        regInfo.setComponentVersion(version);
        try {
            IDCCRegistrationStatus status = this.registrationBroker.registerComponent(regInfo);
            this.registrationBroker.registerEvent("XYZ", regInfo.getEventListenerURL());
            this.registrationBroker.registerEvent("ABC", regInfo.getEventListenerURL());
            this.registrationBroker.registerEvent("XYZ", regInfo.getEventListenerURL());

            assertEquals("DCC event registration did not cause the right number of update events to be fired", 
                    3, this.eventMgr.getNumEventsFired());

            //Reset the num of events fired:
            this.eventMgr.reset();
            this.registrationBroker.unregisterEvent("XYZ", regInfo.getEventListenerURL());
            this.registrationBroker.unregisterEvent("XYZ", regInfo.getEventListenerURL());
            this.registrationBroker.unregisterEvent("XYZ", regInfo.getEventListenerURL());
            assertEquals("DCC event unregistration did not cause the right number of update events to be fired", 
                    3, this.eventMgr.getNumEventsFired());

        } catch (EventRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ComponentRegistrationException e) {
            assertTrue(e.toString(), false);
        } catch (ConfigNotFoundException e) {
            assertTrue(e.toString(), false);
        }
    }
    
    private DCCRegistrationInfoImpl createGoodRegInfo() {
        DCCRegistrationInfoImpl regInfo = new DCCRegistrationInfoImpl();
        regInfo.setEventListenerURL(this.createURL("http://www.nextlabs.com:1234/xxx"));
        regInfo.setComponentURL(this.createURL("http://www.nextlabs.com"));
        regInfo.setComponentName("xxx");
        regInfo.setComponentType(ServerComponentType.fromString("XXX"));
        regInfo.setComponentTypeDisplayName("displayName");
        regInfo.setComponentVersion(version);
        regInfo.setApplicationResources(null );//Collections.singleton("resource1"));
        return regInfo;
    }
    
    private ComponentRegistrationException failComponentRegister(
            IDCCRegistrationInfo regInfo) throws ConfigNotFoundException {
        try {
            registrationBroker.registerComponent(regInfo);
            fail();
            
            //the fail() will throw exception first. This RuntimeException just make the compiler happy.
            throw new RuntimeException();
        } catch (ComponentRegistrationException e) {
            assertNotNull(e.getErrorMessages());
            return e;
        }
    }
    
    public void testInvalidComponentRegistrationValues()
            throws ConfigNotFoundException, ComponentRegistrationException {
        DCCRegistrationInfoImpl regInfo;
        ComponentRegistrationException e;
        
        
        regInfo = createGoodRegInfo();
        
        registrationBroker.registerComponent(regInfo);
        registrationBroker.unregisterComponent(regInfo);
        
        
        // null
        regInfo = createGoodRegInfo();
        regInfo.setComponentName(null);
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        //too short
        regInfo = createGoodRegInfo();
        regInfo.setComponentName("");
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        //too long
        regInfo = createGoodRegInfo();
        regInfo.setComponentName(RandomString.getRandomString(129, 129, RandomString.GRAPH));
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // null
        regInfo = createGoodRegInfo();
        regInfo.setComponentType(null);
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // too short
        regInfo = createGoodRegInfo();
        regInfo.setComponentType(ServerComponentType.fromString("M"));
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // invalid character
        regInfo = createGoodRegInfo();
        regInfo.setComponentType(ServerComponentType.fromString("ABC#"));
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());

        // too long
        regInfo = createGoodRegInfo();
        regInfo.setComponentType(ServerComponentType.fromString(
                RandomString.getRandomString(33, 33, RandomString.ALNUM)));
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // null
        regInfo = createGoodRegInfo();
        regInfo.setComponentTypeDisplayName(null);
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // too short
        regInfo = createGoodRegInfo();
        regInfo.setComponentTypeDisplayName("");
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        // invalid character
        regInfo = createGoodRegInfo();
        regInfo.setComponentTypeDisplayName("display\nname");
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());

        // too long
        regInfo = createGoodRegInfo();
        regInfo.setComponentTypeDisplayName(
                RandomString.getRandomString(129, 129, RandomString.GRAPH + " "));
        e = failComponentRegister(regInfo);
        assertEquals(1, e.getErrorMessages().size());
        
        //multi errors
        regInfo = createGoodRegInfo();
        regInfo.setComponentName("");
        regInfo.setComponentType(null);
        regInfo.setComponentTypeDisplayName("");
        e = failComponentRegister(regInfo);
        assertEquals(3, e.getErrorMessages().size());
        
        //TODO
        //setEventListenerURL
        
        //TODO
        //setComponentURL
        
        //setApplicationResources
        //setVersion
    }
    
    public void testRegisterApplicationResourcesNull()
            throws ComponentRegistrationException, ConfigNotFoundException,
            DataSourceException, EntityManagementException {
        Collection<DevelopmentEntity> existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());
        
        DCCRegistrationInfoImpl regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(null);
        registrationBroker.registerComponent(regInfo);
        
        existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());
    }
    
    public void testRegisterApplicationResourcesEmpty()
            throws ComponentRegistrationException, ConfigNotFoundException,
            EntityManagementException {
        Collection<DevelopmentEntity> existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());
        
        DCCRegistrationInfoImpl regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(Collections.EMPTY_SET);
        registrationBroker.registerComponent(regInfo);
        
        existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());
    }
    
    public void testRegisterApplicationResourcesOne()
            throws ComponentRegistrationException, ConfigNotFoundException,
            DataSourceException, EntityManagementException{
        Collection<DevelopmentEntity> existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());
        
        String appResourceName = "app1";
        
        DCCRegistrationInfoImpl regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(Collections.singleton(appResourceName));

        registrationBroker.registerComponent(regInfo);
        
        existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertEquals(1, existing.size());
        DevelopmentEntity entity = existing.iterator().next();
        assertEquals(entity.getName(), appResourceName);
    }
    
    public void testRegisterApplicationResourcesMulti()
            throws ComponentRegistrationException, ConfigNotFoundException,
            DataSourceException, EntityManagementException {
        Collection<DevelopmentEntity> existing = lifecycleManager
                .getAllApplicatinResourceComponents();
        assertTrue(existing.isEmpty());

        Set<String> appResourceNames = new HashSet<String>();
        appResourceNames.add("name 1");
        appResourceNames.add("name 2_2");
        appResourceNames.add("nameThree");

        DCCRegistrationInfoImpl regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(appResourceNames);

        registrationBroker.registerComponent(regInfo);

        existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertEquals(appResourceNames.size(), existing.size());
        
        for(DevelopmentEntity e : existing) {
            String name = e.getName();
            assertTrue(name, appResourceNames.remove(name));
        }
        assertTrue(appResourceNames.isEmpty());
    }
    
    public void testRegisterApplicationResourcesDuplicated()
            throws ComponentRegistrationException, ConfigNotFoundException,
            DataSourceException, EntityManagementException {
        String appResourceName = "app1";
        DCCRegistrationInfoImpl regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(Collections.singleton(appResourceName));

        registrationBroker.registerComponent(regInfo);
        
        Collection<DevelopmentEntity> existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertEquals(1, existing.size());
        DevelopmentEntity entity = existing.iterator().next();
        assertEquals(entity.getName(), appResourceName);
        Date firstUpdateDate = entity.getLastModified();
        assertNotNull(firstUpdateDate);
        
        //re-regiser
        regInfo = createGoodRegInfo();
        regInfo.setApplicationResources(Collections.singleton(appResourceName));
        registrationBroker.registerComponent(regInfo);
        
        existing = lifecycleManager.getAllApplicatinResourceComponents();
        assertEquals(1, existing.size());
        entity = existing.iterator().next();
        assertEquals(entity.getName(), appResourceName);
        //the update date should not changed.
        assertEquals(firstUpdateDate, entity.getLastModified());
    }

    /**
     * Tests heartbeat updates
     *  
     */
    public void testCheckUpdates() {
        // DCSF-1
        DCCRegistrationInfoImpl dcsfInfo1 = new DCCRegistrationInfoImpl();
        dcsfInfo1.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentName("dcsf2-1");
        dcsfInfo1.setComponentType(ServerComponentType.DCSF);
        dcsfInfo1.setComponentTypeDisplayName("Communication Server");
        dcsfInfo1.setComponentVersion(version);
        IComponentHeartbeatInfo dcsfHB1 = new ComponentHeartbeatInfoImpl();
        dcsfHB1.setComponentName(dcsfInfo1.getComponentName());
        ComponentHeartbeatResponseImpl dcsfUpdate1;

        // DCSF-2
        DCCRegistrationInfoImpl dcsfInfo2 = new DCCRegistrationInfoImpl();
        dcsfInfo2.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf2-2"));
        dcsfInfo2.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf2-2"));
        dcsfInfo2.setComponentName("dcsf2-2");
        dcsfInfo2.setComponentTypeDisplayName("Communication Server");
        dcsfInfo2.setComponentType(ServerComponentType.DCSF);
        dcsfInfo2.setComponentVersion(version);
        IComponentHeartbeatInfo dcsfHB2 = new ComponentHeartbeatInfoImpl();
        dcsfHB2.setComponentName(dcsfInfo2.getComponentName());
        ComponentHeartbeatResponseImpl dcsfUpdate2;

        // DMS (Co-located with DCSF-1)
        DCCRegistrationInfoImpl dmsInfo = new DCCRegistrationInfoImpl();
        dmsInfo.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        dmsInfo.setComponentURL(this.createURL("http://www1.mockURI.for.dms"));
        dmsInfo.setComponentName("dms-1");
        dmsInfo.setComponentTypeDisplayName("Management Server");
        dmsInfo.setComponentType(ServerComponentType.DMS);
        dmsInfo.setComponentVersion(version);
        IComponentHeartbeatInfo dmsHB = new ComponentHeartbeatInfoImpl();
        dmsHB.setComponentName(dmsInfo.getComponentName());
        ComponentHeartbeatResponseImpl dmsUpdate;

        try {
            // Register components:
            this.registrationBroker.registerComponent(dcsfInfo1);
            this.registrationBroker.registerComponent(dcsfInfo2);
            this.registrationBroker.registerComponent(dmsInfo);

            // Send first heartbeats from both components:
            dcsfUpdate1 = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dcsfHB1);
            assertNotNull("DCSF heartbeat response should include a cookie", dcsfUpdate1.getCookie());
            dcsfHB1.setHeartbeatCookie(dcsfUpdate1.getCookie());
            assertEquals("DCSF heartbeat response should not include any registrations yet", 
                    0, dcsfUpdate1.getEventRegistrationInfo().length);

            dcsfUpdate2 = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dcsfHB2);
            assertNotNull("DCSF heartbeat response should include a cookie", dcsfUpdate2.getCookie());
            dcsfHB2.setHeartbeatCookie(dcsfUpdate2.getCookie());
            assertEquals("DCSF heartbeat response should not include any registrations yet", 
                    0, dcsfUpdate2.getEventRegistrationInfo().length);

            dmsUpdate = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dmsHB);
            assertNull("DMS Heartbeat response should not include a cookie", dmsUpdate.getCookie());
            assertNotNull("DMS heartbeat response should not include any registrations", 
                    dmsUpdate.getEventRegistrationInfo());
            assertEquals("DMS heartbeat response should not include any registrations", 
                    0, dmsUpdate.getEventRegistrationInfo().length);

            // Now register for some events:
            this.registrationBroker.registerEvent("XYZ", dcsfInfo1.getEventListenerURL());
            this.registrationBroker.registerEvent("ABC", dcsfInfo1.getEventListenerURL());

            // Heartbeat from DCSF2-1 should receive no updates since it is the
            // listener to these events.
            dcsfUpdate1 = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dcsfHB1);
            assertNotNull("DCSF heartbeat response should include a cookie", dcsfUpdate1.getCookie());
            dcsfUpdate1.setCookie(dcsfUpdate1.getCookie());
            assertEquals("DCSF heartbeat response should include 0 registrations since this is the listener for those events", 
                    0, dcsfUpdate1.getEventRegistrationInfo().length);

            // Heartbeat from DCSF2-2 should receive those updates.
            dcsfUpdate2 = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dcsfHB2);
            assertNotNull("DCSF heartbeat response should include a cookie", dcsfUpdate2.getCookie());
            dcsfUpdate2.setCookie(dcsfUpdate2.getCookie());
            assertEquals("DCSF heartbeat response should include 2 registrations since this is the listener for those events", 
                    2, dcsfUpdate2.getEventRegistrationInfo().length);
            IEventRegistrationInfo[] regArray2 = dcsfUpdate2.getEventRegistrationInfo();
            for (int i = 0; i < regArray2.length; i++) {
                IEventRegistrationInfo reg = regArray2[i];
                assertTrue("Registration update's event name was inaccurate", 
                        (reg.getName().compareTo("XYZ") == 0) || (reg.getName().compareTo("ABC") == 0));
                assertTrue("Registration update's callback URL was inaccurate", 
                        (reg.getCallbackURL().equals(dcsfInfo1.getEventListenerURL())));
                assertTrue("Registration update's isActive flag was inaccurate", reg.isActive() == true);
            }

            // Heartbeat from DMS should receive no updates:
            dmsUpdate = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dmsHB);
            assertTrue("DMS Heartbeat response should not include a cookie", (dmsUpdate.getCookie() == null));
            assertNotNull("DMS heartbeat response should not include any registrations", 
                    dmsUpdate.getEventRegistrationInfo());
            assertEquals("DMS heartbeat response should not include any registrations", 
                    0, dmsUpdate.getEventRegistrationInfo().length);

            // We unregister dcsf2-1 and ensure that the updates received by
            // dcsf2 are correct:
            this.registrationBroker.unregisterComponent(dcsfInfo1);
            dcsfUpdate2 = (ComponentHeartbeatResponseImpl) this.registrationBroker.checkUpdates(dcsfHB2);
            assertTrue("DCSF heartbeat response should include a cookie", (dcsfUpdate2.getCookie() != null));
            dcsfUpdate2.setCookie(dcsfUpdate2.getCookie());
            assertEquals("DCSF heartbeat response should include 0 registrations since this is the listener for those events", 
                    2, dcsfUpdate2.getEventRegistrationInfo().length);
            regArray2 = dcsfUpdate2.getEventRegistrationInfo();
            for (int i = 0; i < regArray2.length; i++) {
                IEventRegistrationInfo reg = regArray2[i];
                assertTrue("Registration update's event name was inaccurate", 
                        (reg.getName().compareTo("XYZ") == 0) || (reg.getName().compareTo("ABC") == 0));
                assertTrue("Registration update's callback URL was inaccurate", 
                        (reg.getCallbackURL().equals(dcsfInfo1.getEventListenerURL())));
                assertTrue("Registration update's isActive flag was inaccurate", reg.isActive() == false);
            }
        } catch (EventRegistrationException e) {
            fail("No EventRegistrationException should be thrown");
        } catch (ComponentRegistrationException e) {
            fail("No ComponentRegistrationException exception should be thrown");
        } catch (ConfigNotFoundException e) {
            fail("No ConfigNotFoundException exception should be thrown");
        }
    }

    /**
     * Checks if the registered component information matches its registration
     * information.
     * 
     * @param componentToCompare
     * @param regToCompareAgainst
     * @return boolean
     */
    protected void verifyComponentRegistration(
            IDCCRegistrationInfo regToCompareAgainst,
            boolean shouldBePersisted, boolean shouldBeActive) {
        ComponentDO componentToCompare = null;
        try {
            componentToCompare = this.compMgr.getComponentByName(regToCompareAgainst.getComponentName());

            if (shouldBePersisted) {
                assertTrue("Component should have been persisted but it wasn't.", (componentToCompare != null));
                URL persistedURI = createURL(componentToCompare.getCallbackURL());

                if (!persistedURI.equals(regToCompareAgainst.getEventListenerURL())) {
                    assertTrue("URI of persisted component did not match expected URI from registration", false);
                }

                if (componentToCompare.getName().compareTo(regToCompareAgainst.getComponentName()) != 0) {
                    assertTrue("Name of persisted component did not match expected name from registration", false);
                }

                assertEquals("Component type should match", 
                        regToCompareAgainst.getComponentType().getName(), componentToCompare.getType().getName());
                assertEquals("Active state of component (true/false) was not as expected", 
                        shouldBeActive, componentToCompare.isActive());
            } else {
                assertTrue("Component should not have been persisted but it was.", (componentToCompare == null));
            }
        } catch (DataSourceException e) {
            assertTrue(e.toString(), false);
        }
    }

    /**
     * Test getRegisteredComponents() method
     * 
     * @throws DataSourceException
     * @throws ConfigNotFoundException
     * @throws ComponentRegistrationException
     */
    public void testGetRegisteredComponents() throws DataSourceException,
            ComponentRegistrationException, ConfigNotFoundException {
        List registeredComponents = this.registrationBroker.getRegisteredComponents();
        assertTrue("testGetRegisteredComponents - Ensure list of components is initially empty", registeredComponents.isEmpty());

        // Now, register some components
        // DCSF-1
        DCCRegistrationInfoImpl dcsfInfo1 = new DCCRegistrationInfoImpl();
        dcsfInfo1.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentName("dcsf2-1");
        dcsfInfo1.setComponentType(ServerComponentType.DCSF);
        dcsfInfo1.setComponentTypeDisplayName("Communication Server");
        dcsfInfo1.setComponentVersion(version);

        // DMS (Co-located with DCSF-1)
        DCCRegistrationInfoImpl dmsInfo = new DCCRegistrationInfoImpl();
        dmsInfo.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf-1"));
        dmsInfo.setComponentURL(this.createURL("http://www1.mockURI.for.dms1"));
        dmsInfo.setComponentName("dms-1");
        dmsInfo.setComponentType(ServerComponentType.DMS);
        dmsInfo.setComponentTypeDisplayName("Management Server");
        dmsInfo.setComponentVersion(version);

        // Register components:
        this.registrationBroker.registerComponent(dcsfInfo1);
        this.registrationBroker.registerComponent(dmsInfo);

        // Now, retieve list again
        registeredComponents = this.registrationBroker.getRegisteredComponents();
        assertEquals("testGetRegisteredComponents - Ensure list of components is size of 2", 2, registeredComponents.size());
        Iterator componentIterator = registeredComponents.iterator();
        while (componentIterator.hasNext()) {
            IDCCComponentDO element = (IDCCComponentDO) componentIterator.next();
            if (element.getName().equals(dcsfInfo1.getComponentName())) {
                assertEquals("Ensure component 1 type is correct", 
                        dcsfInfo1.getComponentType().getName(), element.getType().getName());
                assertEquals("Ensure component 1 url is correct", 
                        dcsfInfo1.getEventListenerURL().toString(), element.getCallbackURL());
            } else if (element.getName().equals(dmsInfo.getComponentName())) {
                assertEquals("Ensure component 2 type is correct", 
                        dmsInfo.getComponentType().getName(), element.getType().getName());
                assertEquals("Ensure component 2 url is correct", 
                        dmsInfo.getEventListenerURL().toString(), element.getCallbackURL());
            } else {
                fail("Uknown component returned: " + element.getName());
            }
        }

        // Now, try removing them
        ((DCCRegistrationBrokerImpl) this.registrationBroker).clearAll();

        // Ensure unregistered components are not returned
        registeredComponents = this.registrationBroker.getRegisteredComponents();
        assertTrue("testGetRegisteredComponents - Ensure list of components is empty after unregistration", 
                registeredComponents.isEmpty());
    }

    /**
     * Test getRegisteredComponentsByType() method
     * 
     * @throws DataSourceException
     * @throws ConfigNotFoundException
     * @throws ComponentRegistrationException
     */
    public void testGetRegisteredComponentsByType() throws DataSourceException,
            ComponentRegistrationException, ConfigNotFoundException {
        List registeredComponents = this.registrationBroker.getRegisteredComponentsByType(DCCComponentEnumType.DCSF);
        assertTrue("testGetRegisteredComponents - Ensure list of components is initially empty", registeredComponents.isEmpty());

        // Now, register a components
        // DCSF-1
        DCCRegistrationInfoImpl dcsfInfo1 = new DCCRegistrationInfoImpl();
        dcsfInfo1.setEventListenerURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentURL(this.createURL("http://www1.mockURI.for.dcsf2-1"));
        dcsfInfo1.setComponentName("dcsf2-1");
        dcsfInfo1.setComponentType(ServerComponentType.DCSF);
        dcsfInfo1.setComponentTypeDisplayName("Communication Server");
        dcsfInfo1.setComponentVersion(version);

        // Register components:
        this.registrationBroker.registerComponent(dcsfInfo1);

        // Now, retieve list again
        registeredComponents = this.registrationBroker.getRegisteredComponents();
        assertEquals("testGetRegisteredComponents - Ensure list of components is size of 1", 1, registeredComponents.size());
        Iterator componentIterator = registeredComponents.iterator();
        while (componentIterator.hasNext()) {
            IDCCComponentDO element = (IDCCComponentDO) componentIterator.next();
            if (element.getName().equals(dcsfInfo1.getComponentName())) {
                assertEquals("Ensure component 1 type is correct", 
                        dcsfInfo1.getComponentType().getName(), element.getType().getName());
                assertEquals("Ensure component 1 url is correct", 
                        dcsfInfo1.getEventListenerURL().toString(), element.getCallbackURL());
            } else {
                fail("Uknown component returned: " + element.getName());
            }
        }

        // Now, try removing them
        ((DCCRegistrationBrokerImpl) this.registrationBroker).clearAll();

        // Ensure unregistered components are not returned
        registeredComponents = this.registrationBroker.getRegisteredComponents();
        assertTrue("testGetRegisteredComponents - Ensure list of components is empty after unregistration", 
                registeredComponents.isEmpty());
    }

    /**
     * Since creation of a URL requires catching a malformed URL exception, this
     * is a utilty method that prevents us from cluttering the code.
     * 
     * @param strURL
     * @return URL object reprsenting strURL
     */
    protected URL createURL(String strURL) {
        URL uri = null;
        try {
            uri = new URL(strURL);
        } catch (MalformedURLException e) {
            fail(e.toString());
        }
        return uri;
    }

    /**
     * Performs cleanup before the next test. All test data is erased.
     *  
     */
    protected void tearDown() throws Exception {
        // Clear all the database tables:
        ((DCCRegistrationBrokerImpl) this.registrationBroker).clearAll();

        super.tearDown();
    }
}