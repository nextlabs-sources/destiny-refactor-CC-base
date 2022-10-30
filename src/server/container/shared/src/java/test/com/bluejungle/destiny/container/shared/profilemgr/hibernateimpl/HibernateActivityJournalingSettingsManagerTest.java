/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.ICustomizableActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UnknownEntryException;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/HibernateActivityJournalingSettingsManagerTest.java#5 $
 */

public class HibernateActivityJournalingSettingsManagerTest extends BaseContainerSharedTestCase {

    private HibernateActivityJournalingSettingsManager managerToTest;
    private IAgentManager agentManager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateActivityJournalingSettingsManagerTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentManager = (IAgentManager) componentManager.getComponent(agentMgrCompInfo);

        ComponentInfo settingsManagerCompInfo = new ComponentInfo(IActivityJournalingSettingsManager.COMP_NAME, HibernateActivityJournalingSettingsManager.class.getName(), IActivityJournalingSettingsManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.managerToTest = (HibernateActivityJournalingSettingsManager) componentManager.getComponent(settingsManagerCompInfo);
    }

    public void testGetActivityJournalingSettings() throws DataSourceException, HibernateException {
        // Ensure that we can get the three predefined levels
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        assertNotNull("testGetActivityJournalingSettings - Ensure Extended predefined setting it not null", this.managerToTest.getActivityJournalingSettings("Extended", desktopAgentType));
        assertNotNull("testGetActivityJournalingSettings - Ensure Default predefined setting it not null", this.managerToTest.getActivityJournalingSettings("Default", desktopAgentType));
        assertNotNull("testGetActivityJournalingSettings - Ensure Minimal predefined setting it not null", this.managerToTest.getActivityJournalingSettings("Minimal", desktopAgentType));

        // Now, create one and make that we can get it back
        Set loggedActions = new HashSet();
        loggedActions.add(ActionEnumType.ACTION_COPY);
        String mySettingsName = "mySettings";

        createTestActivityJournalingSettings(loggedActions, mySettingsName);
        IActivityJournalingSettings settingsRetrieved = this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        assertEquals("testGetActivityJournalingSettings - Ensure retrieve settings has correct name", mySettingsName, settingsRetrieved.getName());
        assertEquals("testGetActivityJournalingSettings - Ensure retrieve settings has correct logged actions", loggedActions, settingsRetrieved.getLoggedActions());

        ((ActivityJournalingSettingsDO) settingsRetrieved).delete();

        // Test UnknownEntryException
        UnknownEntryException expectedException = null;
        try {
            this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        } catch (UnknownEntryException exception) {
            expectedException = exception;
        }
        assertNotNull("testGetActivityJournalingSettings - Ensure UnknownEntryException was thrown.", expectedException);

        // Test null pointer
        NullPointerException expectedNullpointerException = null;
        try {
            this.managerToTest.getActivityJournalingSettings(null, desktopAgentType);
        } catch (NullPointerException exception) {
            expectedNullpointerException = exception;
        }
        assertNotNull("testGetActivityJournalingSettings - Ensure NullPointerException was thrown.", expectedNullpointerException);

        try {
            this.managerToTest.getActivityJournalingSettings(mySettingsName, null);
            fail("Should throw NullPpointerException");
        } catch (NullPointerException excetion) {
        }
    }

    public void testGetActivityJournalSettingsList() throws DataSourceException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        List activityJournalSettings = this.managerToTest.getActivityJournalSettings(desktopAgentType);
        assertNotNull("testGetActivityJournalSettingsList - Ensure settings list is not null", activityJournalSettings);
        assertEquals("testGetActivityJournalSettingsList - Ensure settings list is right size", 3, activityJournalSettings.size());
        assertEquals("testGetActivityJournalingSettings - Ensure Extended predefined setting is first in list", "Extended", ((IActivityJournalingSettings) activityJournalSettings.get(0)).getName());
        assertEquals("testGetActivityJournalingSettings - Ensure Defalut predefined setting is second in list", "Default", ((IActivityJournalingSettings) activityJournalSettings.get(1)).getName());
        assertEquals("testGetActivityJournalingSettings - Ensure Minimal predefined setting is third in list", "Minimal", ((IActivityJournalingSettings) activityJournalSettings.get(2)).getName());
    }

    public void testCreateActivityJournalingSettings() throws DataSourceException, HibernateException {
        // Create a journaling settings
        Set loggedActions = new HashSet();
        loggedActions.add(ActionEnumType.ACTION_COPY);
        String mySettingsName = "mySettings";

        // Create an activity journal setting. Don't use convenience method to
        // ensure robust test
        Session hSession = getSession();
        Transaction transaction = null;
        try {
            transaction = hSession.beginTransaction();
            this.managerToTest.createActivityJournalingSettings(mySettingsName, loggedActions, hSession);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, null);
            throw exception;
        } finally {
            HibernateUtils.closeSession(hSession, null);
        }

        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IActivityJournalingSettings settingsRetrieved = this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        assertEquals("testGetActivityJournalingSettings - Ensure retrieve settings has correct name", mySettingsName, settingsRetrieved.getName());
        assertEquals("testGetActivityJournalingSettings - Ensure retrieve settings has correct logged actions", loggedActions, settingsRetrieved.getLoggedActions());

        ((ActivityJournalingSettingsDO) settingsRetrieved).delete();

        hSession = getSession();
        try {
            // Test null pointer
            NullPointerException expectedNullpointerException = null;
            try {
                this.managerToTest.createActivityJournalingSettings(null, loggedActions, hSession);
            } catch (NullPointerException exception) {
                expectedNullpointerException = exception;
            }
            assertNotNull("testCreateActivityJournalingSettings - Ensure NullPointerException was thrown for null name.", expectedNullpointerException);

            // Test null pointer
            expectedNullpointerException = null;
            try {
                this.managerToTest.createActivityJournalingSettings(mySettingsName, null, hSession);
            } catch (NullPointerException exception) {
                expectedNullpointerException = exception;
            }
            assertNotNull("testCreateActivityJournalingSettings - Ensure NullPointerException was thrown for null loggedActions Set.", expectedNullpointerException);

            // Test null pointer
            expectedNullpointerException = null;
            try {
                this.managerToTest.createActivityJournalingSettings(mySettingsName, null, hSession);
            } catch (NullPointerException exception) {
                expectedNullpointerException = exception;
            }
            assertNotNull("testCreateActivityJournalingSettings - Ensure NullPointerException was thrown for null loggedActions Set.", expectedNullpointerException);
        } finally {
            HibernateUtils.closeSession(hSession, null);
        }
    }

    public void testUpdate() throws DataSourceException, HibernateException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());

        // Create Settings
        Set loggedActions = new HashSet();
        loggedActions.add(ActionEnumType.ACTION_COPY);
        String mySettingsName = "mySettings";

        createTestActivityJournalingSettings(loggedActions, mySettingsName);
        ICustomizableActivityJournalingSettings settingsRetrieved = (ICustomizableActivityJournalingSettings) this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        assertEquals("testUpdate - Ensure retrieve settings has correct name", mySettingsName, settingsRetrieved.getName());
        assertEquals("testUpdate - Ensure retrieve settings has correct logged actions", loggedActions, settingsRetrieved.getLoggedActions());

        // Now, add a logged activity
        loggedActions.add(ActionEnumType.ACTION_EDIT);
        settingsRetrieved.setLoggedActions(loggedActions);
        this.managerToTest.update((ActivityJournalingSettingsDO) settingsRetrieved);

        // Retrieve it and check updated actions
        settingsRetrieved = (ICustomizableActivityJournalingSettings) this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        assertEquals("testUpdate - Ensure retrieve settings after update has correct logged actions", loggedActions, settingsRetrieved.getLoggedActions());

        ((ActivityJournalingSettingsDO) settingsRetrieved).delete();

        // Test null pointer
        NullPointerException expectedNullpointerException = null;
        try {
            this.managerToTest.update(null);
        } catch (NullPointerException exception) {
            expectedNullpointerException = exception;
        }
        assertNotNull("testUpdate - Ensure NullPointerException was thrown for null settings object.", expectedNullpointerException);

    }

    public void testDelete() throws DataSourceException, HibernateException {
        // Create a journaling settings
        Set loggedActions = new HashSet();
        loggedActions.add(ActionEnumType.ACTION_COPY);
        String mySettingsName = "mySettings";

        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        createTestActivityJournalingSettings(loggedActions, mySettingsName);
        IActivityJournalingSettings settingsRetrieved = this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        assertNotNull("testDelete - Ensure settings was stored correctly", settingsRetrieved);

        ((ActivityJournalingSettingsDO) settingsRetrieved).delete();

        UnknownEntryException expectedException = null;
        try {
            this.managerToTest.getActivityJournalingSettings(mySettingsName, desktopAgentType);
        } catch (UnknownEntryException exception) {
            expectedException = exception;
        }
        assertNotNull("testDelete - Ensure object could not be retrieved.", expectedException);

        // Test null pointer
        NullPointerException expectedNullpointerException = null;
        try {
            this.managerToTest.delete(null);
        } catch (NullPointerException exception) {
            expectedNullpointerException = exception;
        }
        assertNotNull("testDelete - Ensure NullPointerException was thrown for null settings object.", expectedNullpointerException);
    }

    /**
     * @param loggedActions
     * @param mySettingsName
     * @throws HibernateException
     */
    private void createTestActivityJournalingSettings(Set loggedActions, String mySettingsName) throws HibernateException {
        // Ensure that it can be retrieved
        Session hSession = getSession();
        Transaction transaction = null;
        try {
            transaction = hSession.beginTransaction();
            this.managerToTest.createActivityJournalingSettings(mySettingsName, loggedActions, hSession);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, null);
            throw exception;
        } finally {
            HibernateUtils.closeSession(hSession, null);
        }
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
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
    }
}