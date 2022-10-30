/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IActionType;
import com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.ICustomizableActivityJournalingSettings;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UnknownEntryException;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/HibernateActivityJournalingSettingsManager.java#8 $
 */

public class HibernateActivityJournalingSettingsManager implements IActivityJournalingSettingsManager, ILogEnabled, IInitializable {

    private static final String NAME_PROPERTY = "name";

    private Log log;

    private final Map<IAgentType, List<IActivityJournalingSettings>> agentTypeToPredefinedSettingsListMap = new HashMap<IAgentType, List<IActivityJournalingSettings>>();
    private final Map<IAgentType, Map<String, IActivityJournalingSettings>> agentTypeToPredefinedSettingsMap = new HashMap<IAgentType, Map<String, IActivityJournalingSettings>>();

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IAgentManager agentManager = (IAgentManager) componentManager.getComponent(IAgentManager.COMP_NAME);
        List<IAgentType> agentTypes = agentManager.getAgentTypes();
        for (IAgentType nextAgentType : agentTypes) {
            Set<IActionType> actions = nextAgentType.getActionTypes();
            Map<IActivityJournalingAuditLevel, Set<ActionEnumType>> predefinedAuditingLevels = new TreeMap<IActivityJournalingAuditLevel, Set<ActionEnumType>>(new Comparator<IActivityJournalingAuditLevel>() {
                /**
                 * @see java.util.Comparator#compare(java.lang.Object,
                 *      java.lang.Object)
                 */
                public int compare(IActivityJournalingAuditLevel levelOne, IActivityJournalingAuditLevel levelTwo) {
                    return levelTwo.getOrdinal() - levelOne.getOrdinal();
                }
            });

            for (IActionType nextActionType : actions) {
                IActivityJournalingAuditLevel auditLevel = nextActionType.getActivityJournalingAuditLevel();
                Set<ActionEnumType> currentActionsForLevel = predefinedAuditingLevels.get(auditLevel);
                if (currentActionsForLevel == null) {
                    currentActionsForLevel = new HashSet<ActionEnumType>();
                    predefinedAuditingLevels.put(auditLevel, currentActionsForLevel);
                }
                currentActionsForLevel.add(ActionEnumType.getActionEnum(nextActionType.getId()));
            }

            List<IActivityJournalingSettings> predefinedSettingsList = new ArrayList<IActivityJournalingSettings>(predefinedAuditingLevels.size());
            Map<String, IActivityJournalingSettings> predefinedSettingsMap = new HashMap<String, IActivityJournalingSettings>();
            for (Map.Entry<IActivityJournalingAuditLevel, Set<ActionEnumType>> nextPredefinedAuditLevel : predefinedAuditingLevels.entrySet()) {
                String predefinedAuditLevel = nextPredefinedAuditLevel.getKey().getId();
                ActivityJournalingSettingsDO activityJournalingSettings = new ActivityJournalingSettingsDO(predefinedAuditLevel, nextPredefinedAuditLevel.getValue());
                activityJournalingSettings.setCustomizable(false);
                predefinedSettingsList.add(activityJournalingSettings);
                predefinedSettingsMap.put(predefinedAuditLevel, activityJournalingSettings);
            }

            this.agentTypeToPredefinedSettingsListMap.put(nextAgentType, predefinedSettingsList);
            this.agentTypeToPredefinedSettingsMap.put(nextAgentType, predefinedSettingsMap);
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        if (log == null) {
            throw new NullPointerException("log cannot be null.");
        }

        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager#getActivityJournalingSettings(java.lang.String,
     *      IAgentType)
     */
    public IActivityJournalingSettings getActivityJournalingSettings(String name, IAgentType agentType) throws DataSourceException, UnknownEntryException {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        if (agentType == null) {
            throw new NullPointerException("agentType cannot be null.");
        }

        IActivityJournalingSettings journalSettingsToReturn = null;

        Map<String, IActivityJournalingSettings> predefinedJournalingSettings = this.agentTypeToPredefinedSettingsMap.get(agentType);
        if (predefinedJournalingSettings.containsKey(name)) {
            journalSettingsToReturn = predefinedJournalingSettings.get(name);
        } else {
            Session hSession = null;
            try {
                hSession = getSession();
                Criteria selectByNameCriteria = hSession.createCriteria(ActivityJournalingSettingsDO.class);
                selectByNameCriteria.add(Expression.eq(NAME_PROPERTY, name));
                journalSettingsToReturn = (IActivityJournalingSettings) selectByNameCriteria.uniqueResult();
            } catch (HibernateException exception) {
                throw new DataSourceException(exception);
            } finally {
                closeSession();
            }
        }

        if (journalSettingsToReturn == null) {
            Map fieldValues = new HashMap();
            fieldValues.put(NAME_PROPERTY, name);
            throw new UnknownEntryException(ActivityJournalingSettingsDO.class.getName(), fieldValues);
        }

        return journalSettingsToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager#getActivityJournalSettings(IAgentType)
     */
    public List<IActivityJournalingSettings> getActivityJournalSettings(IAgentType agentType) throws DataSourceException {
        return this.agentTypeToPredefinedSettingsListMap.get(agentType);
    }

    /**
     * @param name
     * @param loggedActivities
     * @param hSession
     * @return
     * @throws DataSourceException
     * @throws HibernateException
     */
    ICustomizableActivityJournalingSettings createActivityJournalingSettings(String name, Set loggedActivities, Session hSession) throws HibernateException {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        if (loggedActivities == null) {
            throw new NullPointerException("loggedActivities cannot be null.");
        }

        ActivityJournalingSettingsDO createdSettings = new ActivityJournalingSettingsDO(name, loggedActivities);
        hSession.save(createdSettings);

        return createdSettings;
    }

    /**
     * Stores updates to an ActivityJournalingSettingsDO instance
     * 
     * @param settingsToUpdate
     * @throws DataSourceException
     */
    void update(ActivityJournalingSettingsDO settingsToUpdate) throws DataSourceException {
        if (settingsToUpdate == null) {
            throw new NullPointerException("settingsToUpdate cannot be null.");
        }

        if (!settingsToUpdate.isCustomizable()) {
            throw new IllegalArgumentException("Cannot update non-modifibale settings instance");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.update(settingsToUpdate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());
            throw new DataSourceException(exception);
        } finally {
            closeSession();
        }
    }

    /**
     * Delete an ActivityJournalingSettingsDO. Package protected to prevent
     * deletion by clients. Deletion will automatically occur for custom
     * settings when the associated comm profile is deleted
     * 
     * @param settingsToDelete
     *            the settings object to delete
     * @throws DataSourceException
     */
    void delete(ActivityJournalingSettingsDO settingsToDelete) throws DataSourceException {
        if (settingsToDelete == null) {
            throw new NullPointerException("settingsToDeletecannot be null.");
        }

        if (!settingsToDelete.isCustomizable()) {
            throw new IllegalArgumentException("Cannot update non-modifibale settings instance");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.delete(settingsToDelete);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());
            throw new DataSourceException(exception);
        } finally {
            closeSession();
        }
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getCountedSession();
    }
    
    private void closeSession(){
        HibernateUtils.closeSession(getDataSource(), getLog());
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
            throw new IllegalStateException("Required datasource not initialized for ActivityJournalingSettingsManager.");
        }

        return dataSource;
    }
}
