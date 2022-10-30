/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * Test for SecureSessionImpl
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionImplTest.java#1 $
 */

public class SecureSessionImplTest extends BaseDCCComponentTestCase {

    private static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
    }

    private static final String COMPONENT_NAME = "dac";

    private HibernateSecureSessionManager sessionManager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionImplTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        ComponentInfo secureSessionMgrCompInfo = new ComponentInfo(ISecureSessionManager.COMPONENT_NAME, HibernateSecureSessionManager.class.getName(), ISecureSessionManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.sessionManager = (HibernateSecureSessionManager) componentManager.getComponent(secureSessionMgrCompInfo);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for SecureSessionImplTest.
     * 
     * @param arg0
     */
    public SecureSessionImplTest(String arg0) {
        super(arg0);
    }

    public void testGetId() throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        Long id = new Long(547547);

        SecureSessionImpl secureSession = new SecureSessionImpl(sessionManager, id, new Long(1), new Long(2));
        assertEquals("testGetId - Ensure id set is that retrieved", id, secureSession.getId());

        IPersistedSecureSession persistentSession = createPersistentSession(new Properties());
        secureSession = new SecureSessionImpl(persistentSession, new Long(0));
        assertEquals("testGetId - Ensure id of persisted session is that retrieved", persistentSession.getId(), secureSession.getId());
    }

    public void testGenerateKey() {
        SecureSessionImpl secureSession = new SecureSessionImpl(sessionManager, new Long(3), new Long(1), new Long(2));
        assertNotNull("testGenerateKey - Ensure key generated is not null.", secureSession.generateKey());
    }

    public void testGetProperties() throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        Properties properties = new Properties();
        String propName = "propName";
        String propValue = "propValue";
        properties.put(propName, propValue);

        IPersistedSecureSession persistentSession = createPersistentSession(properties);

        SecureSessionImpl secureSession = new SecureSessionImpl(sessionManager, persistentSession.getId(), new Long(1), new Long(2));
        Properties secureSessionProperties = secureSession.getProperties();
        assertEquals("testGetProperties- Ensure properties are as execpted", properties, secureSessionProperties);

        // Now try with other constructor
        secureSession = new SecureSessionImpl(persistentSession, new Long(0));
        secureSessionProperties = secureSession.getProperties();
        assertEquals("testGetProperties- Ensure properties are as execpted (2)", properties, secureSessionProperties);
    }

    public void testGetProperty() throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        Properties properties = new Properties();
        String propName = "propName";
        String propValue = "propValue";
        properties.put(propName, propValue);

        IPersistedSecureSession persistentSession = createPersistentSession(properties);

        SecureSessionImpl secureSession = new SecureSessionImpl(sessionManager, persistentSession.getId(), new Long(1), new Long(2));
        assertNull("testGetProperty- Ensure random property does not exist", secureSession.getProperty("foo"));
        assertEquals("testGetProperty- Ensure real property is does exist", propValue, secureSession.getProperty(propName));

        // Now try with other constructor
        secureSession = new SecureSessionImpl(persistentSession, new Long(0));
        assertNull("testGetProperty- Ensure random property does not exist (2)", secureSession.getProperty("foo"));
        assertEquals("testGetProperty- Ensure real property is does exist (2)", propValue, secureSession.getProperty(propName));
    }

    private IPersistedSecureSession createPersistentSession(Properties properties) throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        ISecureSession sessionCreated = sessionManager.createSession(properties);
        return sessionManager.getPersistedSession(sessionCreated.getId());
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getComponentName()
     */
    protected String getComponentName() {
        return COMPONENT_NAME;
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getDataRepositories()
     */
    protected Set getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
}
