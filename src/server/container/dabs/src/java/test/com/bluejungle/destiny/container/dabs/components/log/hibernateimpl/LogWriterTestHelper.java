/*
 * Created on Feb 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.log.hibernateimpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.domain.policydecision.hibernateimpl.PolicyDecisionUserType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/log/hibernateimpl/LogWriterTestHelper.java#3 $
 */

public class LogWriterTestHelper {

    private static final LogWriterTestHelper SINGLETON_INSTANCE = new LogWriterTestHelper();
    private static final ActionEnumUserType ACTION_USER_TYPE = new ActionEnumUserType();
    private static final PolicyDecisionUserType POLICY_DECISION_USER_TYPE = new PolicyDecisionUserType();

    /**
     * Constructor
     *  
     */
    private LogWriterTestHelper() {
        super();
    }

    /**
     * @return
     */
    public static LogWriterTestHelper getInstance() {
        return SINGLETON_INSTANCE;
    }

    /**
     * Deletes all records from the various logs. This is a utility method to
     * start fresh from clean database tables.
     * 
     * @throws HibernateException
     */
    public void deleteAllLogs() throws HibernateException, SQLException {
        deletePolicyActivityLogs();
        deleteTrackingActivityLogs();
    } 

    public List<PolicyAssistantLogEntry> retrievePolicyAssistantLogs(
    		String policyLogID) throws HibernateException, SQLException {
    	List<PolicyAssistantLogEntry> retList = new ArrayList<PolicyAssistantLogEntry>();
    	Session session = getSession();
    	Statement statement = null;
    	ResultSet qryResult = null;
    	try {
    		Connection conn = session.connection();
    		statement = conn.createStatement();
    		String selectStmt = 
    			"select * from obligation_log where ref_log_id = '" + 
    			policyLogID + "' order by id";
            if (!statement.execute(selectStmt)) {
                return Collections.EMPTY_LIST;
            }
            qryResult = statement.getResultSet();
            while (qryResult.next()) {
            	long uid = qryResult.getLong(1);
            	long logId = qryResult.getLong(2);
            	String obligationName = qryResult.getString(3);
            	String option = qryResult.getString(4);
            	String description = qryResult.getString(5);
            	String userAction = qryResult.getString(6);
            	PolicyAssistantLogEntry entry = new PolicyAssistantLogEntry(
            	   String.valueOf(logId), 
            	   obligationName, option, description, userAction, uid, 0);	
            	retList.add(entry);
            }
    	} finally {
    		if (qryResult != null) qryResult.close();
    		if (statement != null) statement.close();
    		HibernateUtils.closeSession(session, null);
    	}
    	return retList;
    }

    public List<PolicyActivityLogEntry> retrievePolicyActivityLogs() throws HibernateException, SQLException {
        Session session = getSession();
        try {
            Connection conn = session.connection();
            Statement statement = conn.createStatement();
            if (!statement.execute("select id, time, month_nb, day_nb, policy_id, host_id," 
                    + " host_ip, host_name, user_id, user_name, " + "application_id,"
            		+ " application_name, action, policy_decision, decision_request_id,"
                    + " fromresourcename, fromresourcesize, fromresourceownerid,"
                    + " fromresourcecreateddate, " + "fromresourcemodifieddate, toresourcename,"
                    + " log_level from policy_activity_log order by id")) {
                return Collections.EMPTY_LIST;
            }

            List<PolicyActivityLogEntry> rv = new ArrayList<PolicyActivityLogEntry>();
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                long ts = resultSet.getLong(2);
                long policyId = resultSet.getLong(5);
                long hostId = resultSet.getLong(6);
                String hostIP = resultSet.getString(7);
                String hostName = resultSet.getString(8);
                long userId = resultSet.getLong(9);
                String userName = resultSet.getString(10);
                long appId = resultSet.getLong(11);
                String appName = resultSet.getString(12);
                String actionName = resultSet.getString(13);
                ActionEnumType action = ACTION_USER_TYPE.getTypeByCode(actionName);
                String pdName = resultSet.getString(14);
                PolicyDecisionEnumType pd = POLICY_DECISION_USER_TYPE.getTypeByCode(pdName);
                long decisionRequestId = resultSet.getLong(15);
                FromResourceInformation from = getFromResourceInfo(resultSet, 16);
                ToResourceInformation to = getToResourceInfo(resultSet, 21);
                int level = resultSet.getInt(22);
                
                // get the custom attributes
                Statement customAttrStatement = conn.createStatement();
                DynamicAttributes customAttrs = null;
                if (customAttrStatement.execute("SELECT attribute_type, attribute_name, attribute_value FROM policy_custom_attr " +
                                                "WHERE policy_log_id = " + id)) {
                    customAttrs = new DynamicAttributes();
                    ResultSet customAttrResultSet = customAttrStatement.getResultSet();
                    while (customAttrResultSet.next()){
                        String key = customAttrResultSet.getString(1);
                        String value = customAttrResultSet.getString(2);
                        customAttrs.put(key, value);
                    }

                    if (customAttrs.size() == 0) {
                        customAttrs = null;
                    }
                }
                               
                PolicyActivityInfo info = new PolicyActivityInfo(from, to, userName, userId, 
                        hostName, hostIP, hostId, appName, appId, action, pd, decisionRequestId, 
                        ts, level, customAttrs);
                PolicyActivityLogEntry entry = new PolicyActivityLogEntry(info, policyId, id);
                rv.add(entry);
            }
            statement.close();
            return rv;
        } finally {
            HibernateUtils.closeSession(session, null);
        }

    }

    /**
     * @param resultSet
     * @param offset
     * @return
     * @throws SQLException
     */
    public FromResourceInformation getFromResourceInfo(ResultSet resultSet, int offset) throws SQLException {
        String name = resultSet.getString(offset);
        if (name == null) {
            return null;
        }
        long size = resultSet.getLong(offset + 1);
        boolean sizeWasNull = resultSet.wasNull();
        String ownerId = resultSet.getString(offset + 2);
        long created = resultSet.getLong(offset + 3);
        long modified = resultSet.getLong(offset + 4);
        if (sizeWasNull) {
            size = -1;
            created = -1;
            modified = -1;
            ownerId = null;
        }
        return new FromResourceInformation(name, size, created, modified, ownerId);
    }

    /**
     * 
     * @param resultSet
     * @param offset
     * @return
     * @throws SQLException
     */
    public ToResourceInformation getToResourceInfo(ResultSet resultSet, int offset) throws SQLException {
        String name = resultSet.getString(offset);
        if (name == null) {
            return null;
        }
        return new ToResourceInformation(name);
    }

    public List<TrackingLogEntry> retrieveTrackingActivityLogs() throws HibernateException, SQLException {
        Session session = getSession();
        try {
            Connection conn = session.connection();
            Statement statement = conn.createStatement();
            if (!statement.execute("select id, time, month_nb, day_nb, host_id, host_ip, host_name,"
                    + " user_id, user_name, " + "application_id, application_name, action, "
                    + "fromresourcename, fromresourcesize, fromresourceownerid," 
                    + " fromresourcecreateddate, fromresourcemodifieddate, toresourcename,"
                    + "log_level from tracking_activity_log order by id")) {
                return Collections.EMPTY_LIST;
            }

            List<TrackingLogEntry> rv = new ArrayList<TrackingLogEntry>();
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                long ts = resultSet.getLong(2);
                long hostId = resultSet.getLong(5);
                String hostIP = resultSet.getString(6);
                String hostName = resultSet.getString(7);
                long userId = resultSet.getLong(8);
                String userName = resultSet.getString(9);
                long appId = resultSet.getLong(10);
                String appName = resultSet.getString(11);
                String actionName = resultSet.getString(12);
                ActionEnumType action = ACTION_USER_TYPE.getTypeByCode(actionName);
                FromResourceInformation from = getFromResourceInfo(resultSet, 13);
                ToResourceInformation to = getToResourceInfo(resultSet, 18);
                int level = resultSet.getInt(19);
                
                // get the custom attributes
                Statement customAttrStatement = conn.createStatement();
                DynamicAttributes  customAttrs = null;
                if (customAttrStatement.execute("SELECT attribute_type, attribute_name, attribute_value FROM tracking_custom_attr " +
                                                "WHERE tracking_log_id = " + id)) {
                    customAttrs = new DynamicAttributes();
                    ResultSet customAttrResultSet = customAttrStatement.getResultSet();
                    while (customAttrResultSet.next()){
                        String key = customAttrResultSet.getString(1);
                        String value = customAttrResultSet.getString(2);
                        customAttrs.put(key, value);
                    }

                    if (customAttrs.size() == 0) {
                        customAttrs = null;
                    }
                }

                TrackingLogEntry entry = new TrackingLogEntry(from, to, userName, userId, hostName, 
                        hostIP, hostId, appName, appId, action, id, ts, level, customAttrs);
                rv.add(entry);
            }
            statement.close();
            return rv;
        } finally {
            HibernateUtils.closeSession(session, null);
        }

    }

    public void deletePolicyActivityLogs() throws HibernateException, SQLException {
        Session session = getSession();

        try {

            session.beginTransaction();
            Connection conn = session.connection();
            Statement statement = conn.createStatement();
            statement.execute("delete from obligation_log");
            statement.execute("delete from policy_custom_attr");
            statement.execute("delete from policy_activity_log");
            conn.commit();
        } finally {
            HibernateUtils.closeSession(session, null);
        }

    }

    public void deleteTrackingActivityLogs() throws HibernateException, SQLException {
        Session session = getSession();

        try {

            session.beginTransaction();
            Connection conn = session.connection();
            Statement statement = conn.createStatement();
            statement.execute("delete from tracking_custom_attr");
            statement.execute("delete from tracking_activity_log");
            conn.commit();
        } finally {
            HibernateUtils.closeSession(session, null);
        }
    }

    /**
     * Retrieve a HibernateSession for persisting data
     * 
     * @return a HibernateSession for persisting data
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
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(
                DestinyRepository.ACTIVITY_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for LogWriter.");
        }

        return dataSource;
    }

}
