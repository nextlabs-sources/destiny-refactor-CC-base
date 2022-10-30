/*
 * Created on Mar 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.pf.PolicyEditorServiceImpl;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.utils.PQLTestUtils;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogCustomAttributeDO;

/**
 * This is the sample data manager class. This class takes care of creating /
 * deleting sample data records in the database. It is invoked by the various
 * tests to prepare and cleanup the appropriate data.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/SampleDataMgr.java#1 $
 */

public class SampleDataMgr extends BaseDataMgr {

    public static final String APPLICATION_NAME_PATTERN = "Application";
    public static final String POLICY_NAME_PATTERN = "/folder/Policy";
    private int nbPolicies = 20;

    /**
     * Creates a set of applicatios in the application cache table
     * 
     * @param s
     *            Hibernate session to use to create the applications
     * @throws HibernateException
     *             if database operation fails
     */
    public void createApplicationsAndGroups(Session s) throws HibernateException {
        ApplicationAndGroupDataMgr.createAppsAndGroups(s);
    }

    /**
     * Creates a date object based on the day, month and year
     * 
     * @param year
     *            year number
     * @param month
     *            month (in CALENDAR format)
     * @param day
     *            day number
     * @return the corresponding date
     */
    public static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * Creates a calendar object based on the day, month and year
     * 
     * @param year
     *            year number
     * @param month
     *            month (in CALENDAR format)
     * @param day
     *            day number
     * @return the corresponding calendar object
     */
    public static Calendar createCalDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

    /**
     * Creates a set of hosts and groups in the host and host group cache tables
     * 
     * @param s
     *            Hibernate session to use to create the data
     * @throws HibernateException
     */
    public void createHosts(Session s) throws HibernateException {
        HostAndGroupDataMgr.createHostsAndGroups(s);
    }

    /**
     * Creates a set of policies in the policy cache table
     * 
     * @param s
     *            Hibernate session to use to create the policies
     * @throws HibernateException
     */
    public void createPolicies(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (long i = 0; i < this.nbPolicies; i++) {
                PolicyDO newPolicy = new PolicyDO();
                newPolicy.setId(i);
                newPolicy.setFullName(POLICY_NAME_PATTERN + i);
                s.save(newPolicy);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }

    }

    /**
     * Create resource classes with the specified pql
     * 
     * @param pql
     *            the pql entites to save
     * @throws PolicyServiceException
     * @throws PQLException
     * @throws EntityManagementException
     */
    public void createResourceClasses(String pql) throws EntityManagementException, PQLException, PolicyServiceException {
        List<DomainObjectDescriptor> savedEntityDescriptors = this.savePql(pql);
        this.deployEntities(savedEntityDescriptors);
    }

    /**
     * Delete all resource classes
     * 
     * @param s
     * @throws HibernateException
     */
    public void deletePFEntities(Session s) throws HibernateException {
        Transaction tx = s.beginTransaction();
        try {
            s.delete("from DeploymentEntity");
            s.delete("from DevelopmentEntity");
            s.delete("from DeploymentRecord");
            tx.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(tx, null);
            throw exception;
        }
    }

    /**
     * Creates a set of users and groups in the user and user group cache tables
     * 
     * @param s
     *            Hibernate session to use to create the data
     * @throws HibernateException
     */
    public void createUsersAndGroups(Session s) throws HibernateException {
        UserAndGroupsDataMgr.createUsersAndGroups(s);
    }

    /**
     * Creates a set of users and groups in the user and user group cache tables
     * 
     * @param s
     *            Hibernate session to use to create the data
     * @throws HibernateException
     */
    public void createHistoricalUsersAndGroups(Session s) throws HibernateException {
        UserAndGroupsDataMgr.createHistoricalUsersAndGroups(s);
    }

    /**
     * Deletes all the applications in the application cache table
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operations fails
     */
    public void deleteApplications(Session s) throws HibernateException {
        ApplicationAndGroupDataMgr.deleteHostsAndGroups(s);
    }

    /**
     * Deletes all the hosts and hosts groups in the hosts cache tables
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operations fails
     */
    public void deleteHosts(Session s) throws HibernateException {
        HostAndGroupDataMgr.deleteHostsAndGroups(s);
    }

    /**
     * Deletes all the policies in the policy cache table
     * 
     * @param s
     *            Hibernate session to use to delete the policies
     * @throws HibernateException
     *             if a database operations fails
     */
    public void deletePolicies(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from PolicyDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all the policy logs in the database
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void deletePolicyLogs(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from TestPolicyActivityLogEntryDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all the tracking logs in the database
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void deleteTrackingLogs(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from TestTrackingActivityLogEntryDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all the custom attributes for policy logs
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void deletePolicyLogCustomAttributes(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from TestPolicyActivityLogCustomAttributeDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all the custom attributes for tracking logs
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void deleteTrackingLogCustomAttributes(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from TestTrackingActivityLogCustomAttributeDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
    
    /**
     * Deletes all the users and user groups information in the database
     * 
     * @param s
     *            Hibernate session to use for the deletion
     * @throws HibernateException
     *             if the database operation fails
     */
    public void deleteUsersAndGroups(Session s) throws HibernateException {
        UserAndGroupsDataMgr.deleteUsersAndGroups(s);
    }

    /**
     * Returns a basic policy log record.
     * 
     * @return a basic policy log record.
     */
    public TestPolicyActivityLogEntryDO getBasicPolicyLogRecord() {
        TestPolicyActivityLogEntryDO logEntry = new TestPolicyActivityLogEntryDO();
        logEntry.setAction(ActionEnumType.ACTION_OPEN);
        logEntry.setApplicationId(2L);
        logEntry.setApplicationName("MSWord");
        logEntry.setDecisionRequestId(-1L);
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(10000L);
        logEntry.setFromResourceInfo(fromResourceInfo);
        logEntry.setHostId(1L);
        logEntry.setHostName("stbarts.bluejungle.com");
        logEntry.setHostIPAddress("10.17.11.130");
        ToResourceInformationDO toResourceInfo = new ToResourceInformationDO();
        toResourceInfo.setName("file:///c/docs/toResource");
        logEntry.setToResourceInfo(toResourceInfo);
        logEntry.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        logEntry.setPolicyId(18L); //Make sure this one is within the
        // range of policies
        logEntry.setTimestamp(Calendar.getInstance());
        logEntry.setUserId(8L);
        logEntry.setUserName("ihanen@bluejungle.com");
        logEntry.setUserResponse("my user reponse");
        logEntry.setLevel(0);
        return (logEntry);
    }

    /**
     * Returns a basic tracking log record.
     * 
     * @return a basic tracking log record.
     */
    public TestTrackingActivityLogEntryDO getBasicTrackingLogRecord() {
        TestTrackingActivityLogEntryDO logEntry = new TestTrackingActivityLogEntryDO();
        logEntry.setAction(ActionEnumType.ACTION_OPEN);
        logEntry.setApplicationId(2L);
        logEntry.setApplicationName("MSWord");
        logEntry.setHostId(1L);
        logEntry.setHostIPAddress("10.17.11.130");
        logEntry.setHostName("stbarts.bluejungle.com");
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(1000L);
        ToResourceInformationDO toResourceInfo = new ToResourceInformationDO();
        toResourceInfo.setName("file:///c/docs/toResource");
        logEntry.setFromResourceInfo(fromResourceInfo);
        logEntry.setToResourceInfo(toResourceInfo);
        logEntry.setTimestamp(Calendar.getInstance());
        logEntry.setUserId(8L);
        logEntry.setUserName("ihanen@bluejungle.com");
        logEntry.setLevel(0);
        return (logEntry);
    }

    /**
     * Returns the nbPolicies.
     * 
     * @return the nbPolicies.
     */
    public int getNbPolicies() {
        return this.nbPolicies;
    }

    /**
     * Insert identical policy activity log records
     * 
     * @param s
     *            Hibernate session to use
     * @param startId
     *            starting row id (subsequent ones will be incremented)
     * @param nbRecords
     *            total number of records to insert
     * @param modelDO
     *            template of the data object to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void insertIdenticalPolicyLogRecords(Session s, Long startId, int nbRecords, TestPolicyActivityLogEntryDO modelDO) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbRecords; i++) {
                TestPolicyActivityLogEntryDO newRecord = new TestPolicyActivityLogEntryDO(modelDO);
                newRecord.setId(startId + i);
                s.save(newRecord);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Insert identical tracking activity log records
     * 
     * @param s
     *            Hibernate session to use
     * @param startId
     *            starting row id (subsequent ones will be incremented)
     * @param nbRecords
     *            total number of records to insert
     * @param modelDO
     *            template of the data object to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void insertIdenticalTrackingLogRecords(Session s, Long startId, int nbRecords, TestTrackingActivityLogEntryDO modelDO) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbRecords; i++) {
                TestTrackingActivityLogEntryDO newRecord = new TestTrackingActivityLogEntryDO(modelDO);
                newRecord.setId(startId + i);
                s.save(newRecord);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Inserts policy activity logs, including custom attributes
     * 
     * @param s
     *          Hibernate session to use
     * @param startId
     *          starting row id
     * @param nbRecords
     *          total number of records to insert
     * @param modelDO
     *          template of the policy activity log data object to use
     * @param attrStartId
     *          starting id for the custom attribute entries
     * @param nbAttributes
     *          total number of custom attributes to insert
     * @throws HibernateException
     */
    public void insertIdenticalPolicyCustomRecords(Session s, Long startId, int nbRecords, TestPolicyActivityLogEntryDO modelDO, Long attrStartId, int nbAttributes) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbRecords; i++) {
                TestPolicyActivityLogEntryDO newRecord = new TestPolicyActivityLogEntryDO(modelDO);
                newRecord.setId(startId + i);
                s.save(newRecord);
                insertPolicyLogCustomAttributes(s, newRecord, nbAttributes, attrStartId + (i * nbAttributes));
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
    
    /** 
     * Inserts custom attributes for a particular policy activity log 
     * 
     * @param s
     * @param recordId
     * @param nbAttributes
     * @param attrStartId
     * @throws HibernateException
     */
    public void insertPolicyLogCustomAttributes(Session s, TestPolicyActivityLogEntryDO newRecord, int nbAttributes, long attrStartId) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbAttributes; i++) {
                TestPolicyActivityLogCustomAttributeDO newAttribute = new TestPolicyActivityLogCustomAttributeDO();
                newAttribute.setValue("Value" + (attrStartId + i));
                s.update(newRecord); 
                newAttribute.setRecord(newRecord);
                newAttribute.setKey("Key" + (attrStartId + i));
                s.save(newAttribute);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
    
    /**
     * Insert identical tracking activity log records
     * 
     * @param s
     *            Hibernate session to use
     * @param startId
     *            starting row id (subsequent ones will be incremented)
     * @param nbRecords
     *            total number of records to insert
     * @param modelDO
     *            template of the data object to use
     * @throws HibernateException
     *             if a database operation fails.
     */
    public void insertIdenticalTrackingCustomRecords(Session s, Long startId, int nbRecords, TestTrackingActivityLogEntryDO modelDO, Long attrStartId, int nbAttributes) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbRecords; i++) {
                TestTrackingActivityLogEntryDO newRecord = new TestTrackingActivityLogEntryDO(modelDO);
                newRecord.setId(startId + i);
                s.save(newRecord);
                insertTrackingLogCustomAttributes(s, newRecord, nbAttributes, attrStartId + (i * nbAttributes));
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
    
    /**
     * Inserts custom attributes for a specific document activity log
     * 
     * @param s
     * @param recordId
     * @param nbAttributes
     * @param attrStartId
     * @throws HibernateException
     */
    public void insertTrackingLogCustomAttributes(Session s, TestTrackingActivityLogEntryDO newRecord, int nbAttributes, long attrStartId) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbAttributes; i++) {
                TestTrackingActivityLogCustomAttributeDO newAttribute = new TestTrackingActivityLogCustomAttributeDO();
                s.update(newRecord);
                newAttribute.setRecord(newRecord);
                newAttribute.setKey("Key" + (attrStartId + i));
                newAttribute.setValue("Value" + (attrStartId + i));
                s.save(newAttribute);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
    
    /**
     * Sets the number of applications
     * 
     * @param nbApplications
     *            number of applications to set
     */
    public void setNbApplications(int nbApplications) {
    }

    /**
     * Sets the nbPolicies
     * 
     * @param nbPolicies
     *            The nbPolicies to set.
     */
    public void setNbPolicies(int nbPolicies) {
        this.nbPolicies = nbPolicies;
    }

    /**
     * Save PQL entities
     * 
     * @param pql
     *            the pql entities to save
     * @throws PolicyServiceException
     *             if the persistence fails
     */
    private List<DomainObjectDescriptor> savePql(String pql) throws PolicyServiceException {
        IPolicyEditorService policyEditorService = getPolicyEditorService();
        return policyEditorService.saveEntities(pql);
    }

    /**
     * Deploy entities with specified ids
     * 
     * @param entityDescriptors
     * @throws EntityManagementException
     * @throws PQLException
     * @throws PolicyServiceException
     */
    private void deployEntities(List<DomainObjectDescriptor> entityDescriptors) throws EntityManagementException, PQLException, PolicyServiceException {
        LifecycleManager lm = getLifecycleManager();
        Collection<DevelopmentEntity> entities = lm.getEntitiesForDescriptors(entityDescriptors);
        for (DevelopmentEntity nextEntity : entities) {
            PQLTestUtils.setStatus(nextEntity, DevelopmentStatus.APPROVED);
        }

        Date asOfDate = UnmodifiableDate.current();
        lm.deployEntities(entities, asOfDate, DeploymentType.PRODUCTION, false, null);
    }

    /**
     * Retrieve the policy editor service
     * 
     * @return the policy editor service
     */
    private IPolicyEditorService getPolicyEditorService() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        return (IPolicyEditorService) componentManager.getComponent(PolicyEditorServiceImpl.COMP_INFO);
    }

    private LifecycleManager getLifecycleManager() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        return (LifecycleManager) componentManager.getComponent(LifecycleManager.COMP_INFO);
    }
}
