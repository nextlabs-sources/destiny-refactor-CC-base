/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * JUnit test for the HibernateSecureSessionManager
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/HibernateSecureSessionManagerTest.java#1 $
 */
public class HibernateSecureSessionManagerTest extends BaseDCCComponentTestCase {

    private static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
    }

    private static final String COMPONENT_NAME = "dac";

    private static final Long TEST_SESSION_TIMEOUT = new Long(4000);
    private static final Long TEST_SESSION_CLEANUP_TIME_INTERVAL = new Long(10000);
    
    private HibernateSecureSessionManager sessionManagerToTest;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateSecureSessionManagerTest.class);
    }

    /*
     * @see BaseDACComponentTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration secureSessionConfig = new HashMapConfiguration();
        secureSessionConfig.setProperty(HibernateSecureSessionManager.SESSION_TIMEOUT_CONFIG_PROPERTY_NAME, TEST_SESSION_TIMEOUT);
        secureSessionConfig.setProperty(HibernateSecureSessionManager.SESSION_CLEANUP_TIME_INTERVAL_PROPERTY_NAME, TEST_SESSION_CLEANUP_TIME_INTERVAL);

        ComponentInfo secureSessionMgrCompInfo = new ComponentInfo(ISecureSessionManager.COMPONENT_NAME, HibernateSecureSessionManager.class.getName(), ISecureSessionManager.class.getName(), LifestyleType.SINGLETON_TYPE, secureSessionConfig);
        this.sessionManagerToTest = (HibernateSecureSessionManager) componentManager.getComponent(secureSessionMgrCompInfo);
    }

    /*
     * @see BaseDACComponentTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for HibernateSecureSessionManagerTest.
     * 
     * @param testName
     */
    public HibernateSecureSessionManagerTest(String testName) {
        super(testName);
    }

    public void testCreateSession() throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        // First, try with empty set of properties
        Properties properties = new Properties();
        ISecureSession sessionCreated = sessionManagerToTest.createSession(properties);
        assertNotNull("testCreateSession - Ensure session created with empty properties is not null", sessionCreated);

        // Now try with username property
        String username = "foobar";
        properties.setProperty("name", username);
        sessionCreated = sessionManagerToTest.createSession(properties);
        assertNotNull("testCreateSession - Ensure session created with non empty properties is not null", sessionCreated);
        assertEquals("testCreateSession - Ensure property set is accessible on session", username, sessionCreated.getProperty("name"));

        // Try pulling from database and making sure it is the same
        String sessionKey = sessionCreated.generateKey();
        ISecureSession sessionRetrieved = sessionManagerToTest.getSessionByKey(sessionKey);
        assertNotNull("testCreateSession - Ensure session retrieved with non empty properties is not null", sessionRetrieved);
        assertEquals("testCreateSession - Ensure property set is accessible on session retrieved", username, sessionRetrieved.getProperty("name"));

        // Test null pointer
        NullPointerException expectionException = null;
        try {
            sessionManagerToTest.createSession(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException exception) {
            expectionException = exception;
        }
        assertNotNull("testCreateSession - Ensure nullpointer exception was thrown when providing null Propeties", expectionException);
    }

    public void testGetSessionByKey() throws SecureSessionPersistenceException, InterruptedException, HibernateException {
        Properties properties = new Properties();
        String username = "foobar";
        properties.setProperty("name", username);
        ISecureSession sessionCreated = sessionManagerToTest.createSession(properties);

        // Try pulling from database and making sure it is the same
        String sessionKey = sessionCreated.generateKey();
        ISecureSession sessionRetrieved = sessionManagerToTest.getSessionByKey(sessionKey);
        assertNotNull("testGetSessionKey - Ensure session retrieved with non empty properties is not null", sessionRetrieved);
        assertEquals("testGetSessionKey - Ensure property set is accessible on session retrieved", username, sessionRetrieved.getProperty("name"));

        // Wait for the timeout and make sure that we can't retrieve it
        Thread.sleep(TEST_SESSION_TIMEOUT.longValue());
        ISecureSession expiredSession = sessionManagerToTest.getSessionByKey(sessionKey);
        assertNull("testGetSessionByKey - Ensure timeout session is null", expiredSession);

        // Ensure that it's still in the DB
        assertTrue("testGetSessionByKey - Ensure persisted session exists before cleanup time", isSessionPersisted(sessionRetrieved));

        // Now, wait for cleanup interval
        Thread.sleep(TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue() * 2);
        assertTrue("testGetSessionByKey - Ensure persisted session does not exist after cleanup time", !isSessionPersisted(sessionRetrieved));

        // Test whether or not the end of life is updated properly when session
        // is repeatedly accessed
        sessionCreated = sessionManagerToTest.createSession(properties);
        sessionKey = sessionCreated.generateKey();
        // Iterate twice the cleanup interval and even 2 addional.  Ensures that the cleanup process will run
        long iterationSleepPeriod = TEST_SESSION_TIMEOUT.longValue() / 4l;  // Sleep time is a 4th of the timeout to ensure it doesn't timeout when sleeping
        long numIterations = 2*TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue()/(iterationSleepPeriod) + 2;
        for (int i = 0; i < numIterations; i++) {
            Thread.sleep(iterationSleepPeriod);
            ISecureSession secureSession = sessionManagerToTest.getSessionByKey(sessionKey);
            if (secureSession == null) {
                fail("Session timed out prematurely");
            }
            
            sessionKey = secureSession.generateKey();
        }
        
        // Ensure that it is now still persisted
        assertTrue("testGetSessionByKey - Ensure persisted session exists after end of life update", isSessionPersisted(sessionCreated));

        // Now, wait for cleanup interval
        Thread.sleep(TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue() * 2);
        assertTrue("testGetSessionByKey - Ensure persisted session does not exist after cleanup time has passed again", !isSessionPersisted(sessionCreated));

        // Test null pointer
        NullPointerException expectionException = null;
        try {
            sessionManagerToTest.getSessionByKey(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException exception) {
            expectionException = exception;
        }
        assertNotNull("testGetSessionKey - Ensure nullpointer exception was thrown when providing null Propeties", expectionException);
    }

    /**
     * @param sessionRetrieved
     * @return
     * @throws HibernateException
     */
    private boolean isSessionPersisted(ISecureSession secureSession) throws HibernateException {
        boolean valueToReturn = false;

        Session hSession = getSession();

        try {
            Criteria queryCriteria = hSession.createCriteria(SecureSessionDO.class);
            queryCriteria.add(Expression.eq("id", secureSession.getId()));
            SecureSessionDO secureSessionRetrieved = (SecureSessionDO) queryCriteria.uniqueResult();
            if (secureSessionRetrieved != null) {
                valueToReturn = true;
            }
        } finally {
            if (hSession != null) {
                hSession.close();
            }
        }

        return valueToReturn;
    }

    public void testGetPersistedSession() throws InterruptedException, SecureSessionPersistenceException, SecureSessionNotFoundException {
        Properties properties = new Properties();
        String username = "foobar";
        properties.setProperty("name", username);
        ISecureSession sessionCreated = sessionManagerToTest.createSession(properties);

        // Try pulling from database and making sure it is the same        
        IPersistedSecureSession sessionRetrieved = sessionManagerToTest.getPersistedSession(sessionCreated.getId());
        assertNotNull("testGetPersistedSession - Ensure session retrieved with non empty properties is not null", sessionRetrieved);
        assertEquals("testGetPersistedSession - Ensure property set is accessible on session retrieved", username, sessionRetrieved.getProperty("name"));
        
        // Wait for cleanup interval
        Thread.sleep(TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue() * 2);
        SecureSessionNotFoundException expectedException = null;
        try {
            sessionRetrieved = sessionManagerToTest.getPersistedSession(sessionCreated.getId());
            fail("Should throw SecureSessionNotFoundException");
        } catch (SecureSessionNotFoundException exception) {
            expectedException = exception;
        }
        assertNotNull("testGetSessionByKey - Ensure persisted session does not exist after cleanup time", expectedException);

        
        // Test whether or not the end of life is updated properly when session
        // is repeatedly accessed
        sessionCreated = sessionManagerToTest.createSession(properties);
        String sessionKey = sessionCreated.generateKey();
        // Iterate twice the cleanup interval and even 2 addional.  Ensures that the cleanup process will run
        long iterationSleepPeriod = TEST_SESSION_TIMEOUT.longValue() / 4l;  // Sleep time is a 4th of the timeout to ensure it doesn't timeout when sleeping
        long numIterations = 2*TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue()/(iterationSleepPeriod) + 2;
        for (int i = 0; i < numIterations; i++) {
            Thread.sleep(iterationSleepPeriod);
            ISecureSession secureSession = sessionManagerToTest.getSessionByKey(sessionKey);
            if (secureSession == null) {
                fail("Session timed out prematurely");
            }
            
            sessionKey = secureSession.generateKey();
        }
        
        // Ensure that it is now still persisted
        sessionRetrieved = sessionManagerToTest.getPersistedSession(sessionCreated.getId());
        assertNotNull("testGetPersistedSession - Ensure session retrieved with after end of life update", sessionRetrieved);

        // Now, wait for cleanup interval
        Thread.sleep(TEST_SESSION_CLEANUP_TIME_INTERVAL.longValue() * 2);
        expectedException = null;
        try {
            sessionRetrieved = sessionManagerToTest.getPersistedSession(sessionCreated.getId());
            fail("Should throw SecureSessionNotFoundException");
        } catch (SecureSessionNotFoundException exception) {
            expectedException = exception;
        }
        assertNotNull("testGetSessionByKey - Ensure persisted session does not exist after cleanup time has passed again", expectedException);

        // Test null pointer
        NullPointerException expectionException = null;
        try {
            sessionManagerToTest.getPersistedSession(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException exception) {
            expectionException = exception;
        }
        assertNotNull("testGetPersistedSession - Ensure nullpointer exception was thrown when providing null Propeties", expectionException);
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create Hibernate
     * sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
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