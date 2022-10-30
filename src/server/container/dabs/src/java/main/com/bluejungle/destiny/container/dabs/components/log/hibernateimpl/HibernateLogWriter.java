/*
 * Created on Jan 24, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.log.hibernateimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV1Wrapper;
import com.bluejungle.domain.log.PolicyActivityLogEntryWrapper;
import com.bluejungle.domain.log.PolicyAssistantLogEntryWrapper;
import com.bluejungle.domain.log.ResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV1Wrapper;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.domain.log.TrackingLogEntryWrapper;
import com.bluejungle.domain.policydecision.hibernateimpl.PolicyDecisionUserType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.datastore.hibernate.utils.MassDMLUtils;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;
import java.util.concurrent.ConcurrentHashMap;

import com.bluejungle.destiny.container.dabs.ServerKeyProvider;

/**
 * Hibernate specific implementation of the Log Writer
 *
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/components/log/hibernateimpl/HibernateLogWriter.java#11 $
 */

public class HibernateLogWriter implements ILogWriter, ILogEnabled, IConfigurable, IInitializable {
	
	private static final Log LOG = LogFactory.getLog(HibernateLogWriter.class);
    public static final PropertyKey<Integer> CUSTOM_ATTR_FIELD_LENGTH_KEY = new PropertyKey<Integer>("CUSTOM_ATTR_FIELD_LENGTH");
    private static final ConcurrentHashMap<String, String> actionCodeMap = new ConcurrentHashMap<String, String>(); 

    /**
     * Native database error codes
     */
    private static final String POSTGRESQL_DUPLICATE_ID_SQL_STATE = "23505";
    private static final String CONSTRAINT_VIOLATION_SQL_STATE = "23000";
    private static final int ORACLE_UNIQUE_ID_VIOLATION_ERR_CODE = 1;
    private static final int MS_SQL_UNIQUE_ID_VIOLATION_ERR_CODE = 2627;

    /**
     * The action code to use for actions that were used, but not configured
     */
    private static final String UNKNOWN_ACTION_CODE = "??";
    private static final String ATTRIBUTE_TYPE_FROM_RESOURCE = "RF";
    private static final String RESOURCE_TYPE = "ce::destinytype";

    /**
     * this is the hibernate default limit for string
     */
    private static final int DEFAULT_VARCHAR_LIMIT = 255;


    /**
     * Raw SQL query for insertion
     */
    private static final String PA_INSERT_QUERY = "insert into policy_activity_log " +
      "(id, time, month_nb, day_nb, host_id, user_id, application_id, application_name, " +
      "action, " +
      "fromresourcename, fromresourcesize, fromresourceownerid, fromresourcecreateddate, " +
      "fromresourcemodifieddate, toresourcename, " +
      "policy_id, policy_decision, decision_request_id, host_name, host_ip, user_name, log_level) values " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

    private static final String TR_INSERT_QUERY = "insert into tracking_activity_log " +
      "(id, time, month_nb, day_nb, host_id, user_id, application_id, application_name, " +
      "action, " +
      "fromresourcename, fromresourcesize, fromresourceownerid, fromresourcecreateddate, " +
      "fromresourcemodifieddate, toresourcename, " +
      "host_name, host_ip, user_name, log_level) values " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

    private static final String PASS_INSERT_QUERY = "insert into obligation_log " +
      "(id, ref_log_id, name, attr_one, attr_two, attr_three) values " +
      "(?, ?, ?, ?, ?, ?) ";

    /**
     * Raw SQL query for custom attributes insertion
     */
    private static final String PA_CUSTOM_ATTR_DESTINATION =
      "policy_custom_attr($id$policy_log_id, attribute_type, attribute_name, attribute_value)";

    private static final String PA_CUSTOM_ATTR_SOURCE_FIELDS = "$custom_attr_sequence$?, ?, ?, ?";

    private static final String TR_CUSTOM_ATTR_DESTINATION =
      "tracking_custom_attr($id$tracking_log_id, attribute_name, attribute_value)";

    private static final String TR_CUSTOM_ATTR_SOURCE_FIELDS = "$custom_attr_sequence$?, ?, ?";

    /**
     * Raw SQL query for policy tags
     */
    private static final String PA_POLICY_TAGS_DESTINATION =
      "policy_tags($id$policy_log_id, tag_name, tag_value)";

    private static final String PA_POLICY_TAGS_SOURCE_FIELDS = "$custom_tag_sequence$?, ?, ?";

    /**
     * <code>COMP_NAME</code> is the name of the ILogWriter component. This
     * name is used to retrieve an instance of a ILogWriter from the
     * ComponentManager
     */
    public static final String COMP_NAME = "hibernateLogWriter";
    private static final String ACTION_SHORT_CODE_SQL = "SELECT pa.short_code FROM pm_action_config pa "
    		+ "LEFT JOIN policy_model pm ON (pm.id = pa.plcy_model_id) WHERE pm.short_name= ? "
    		+ "AND pa.short_name = ? AND pa.plcy_model_id IS NOT NULL and pm.status = ? ";

    private Log log;
    private IConfiguration configuration;
    private int customAttrFieldLength;
    private static ActionEnumUserType actionEnumUserType = new ActionEnumUserType();
    private static PolicyDecisionUserType policyDecisionUserType = new PolicyDecisionUserType();

    public static final int DEFAULT_CUSTOM_ATTR_FIELD_LENGTH = 4000;


    @Override
    public void log(TrackingLogEntry[] logEntries) throws DataSourceException {
        int length = logEntries.length;
        TrackingLogEntryWrapper[] wrapped = new TrackingLogEntryWrapper[length];
        for (int i = 0; i < length; i++) {
            wrapped[i] = new TrackingLogEntryV1Wrapper(logEntries[i]);
        }
        log(wrapped);
    }

    @Override
    public void log(TrackingLogEntryV2[] logEntries) throws DataSourceException {
        log((TrackingLogEntryWrapper[]) logEntries);
    }

    @Override
    public void log(TrackingLogEntryV3[] logEntries) throws DataSourceException {
        log((TrackingLogEntryWrapper[]) logEntries);
    }

    /* Start of TrackingLogEntry methods */
    public void log(TrackingLogEntryWrapper[] entries) throws DataSourceException {
        Session session = null;
        Connection conn = null;
        try {
            session = getSession();
            session.beginTransaction();
            conn = session.connection();
            insertBatch(entries, conn, session);
            conn.commit();
        } catch (SQLException sqle) {
            Log log = getLog();
            if (log.isInfoEnabled()) {
                log.info("Error while attempting to persist logs in batch mode.  Will attempt to insert one at a time.", sqle);
            }
            try {
                conn.rollback();
                insertIndividually(entries, conn, session);
            } catch (SQLException sqle2) {
                getLog().warn("Error while attempt to persist logs one at a time.");
                throw new DataSourceException(sqle2);
            }
        } catch (HibernateException he) {
            throw new DataSourceException(he);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }

    private void insertBatch(TrackingLogEntryWrapper[] entries, Connection conn, Session s)
        throws DataSourceException, SQLException {
        PreparedStatement logStatement = null;
        PreparedStatement attrStatement = null;
        try {
            Calendar cal = Calendar.getInstance();
            logStatement = conn.prepareStatement(TR_INSERT_QUERY);

            IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);

            String insertAttrSql = formatter.formatInsert(
                TR_CUSTOM_ATTR_DESTINATION
            ,   TR_CUSTOM_ATTR_SOURCE_FIELDS
            ,   null
                );

            attrStatement = conn.prepareStatement(insertAttrSql);
            for (int i = 0; i < entries.length; i++) {
                setLogArgumentValues(logStatement, entries[i], cal);
                logStatement.addBatch();

                // prepare the statement for custom attributes
                DynamicAttributes customAttrs = entries[i].getCustomAttr();
                if (customAttrs != null){
                    for (Map.Entry<String,IEvalValue> entry : customAttrs.entrySet()) {
                        // HACK: Bug 5818
                        String fullFieldValue = entry.getValue().getValue().toString();
                        String persistenceFieldValue = 
                            massageCustomAttrValue(fullFieldValue, entries[i].getUid(), false);
                        setAttrArgumentValues(attrStatement, entries[i], entry.getKey(), persistenceFieldValue);
                        attrStatement.addBatch();
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
        // do not catch SQLException when executing batch, let the outer layer
        // handle it
        logStatement.executeBatch();
        logStatement.close();
        if (attrStatement != null){
            attrStatement.executeBatch();
            attrStatement.close();
        }
    }

    /**
     * Insert log entries one at a time, if inserting one entry fails, attempts
     * are made to insert remaining anyway. It won't propagate SQLExceptions in
     * case of individual insertion failures, only if it can't continue at all.
     *
     * @param entries
     * @param conn
     * @throws SQLException
     */
    private void insertIndividually(TrackingLogEntryWrapper[] entries, Connection conn, Session s)
        throws SQLException {
        PreparedStatement logStatement = null;
        PreparedStatement attrStatement = null;
        try {
            Log log = getLog();
            Calendar cal = Calendar.getInstance();
            logStatement = conn.prepareStatement(TR_INSERT_QUERY);

            IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);

            String insertAttrSql = formatter.formatInsert(
                TR_CUSTOM_ATTR_DESTINATION
            ,   TR_CUSTOM_ATTR_SOURCE_FIELDS
            ,   null
                );

            attrStatement = conn.prepareStatement(insertAttrSql);
            for (int i = 0; i < entries.length; i++) {
                setLogArgumentValues(logStatement, entries[i], cal);
                try {
                    logStatement.executeUpdate();

                    // prepare for custom attribute insertion
                    DynamicAttributes customAttrs = entries[i].getCustomAttr();
                    if (customAttrs != null){
                        for (Map.Entry<String,IEvalValue> entry : customAttrs.entrySet()) {
                            // HACK: Bug 5818
                            String fullFieldValue = entry.getValue().getValue().toString();
                            String persistenceFieldValue =
                                massageCustomAttrValue(fullFieldValue, entries[i].getUid(), false);                            
                            setAttrArgumentValues(attrStatement, entries[i], entry.getKey(), persistenceFieldValue);
                            attrStatement.executeUpdate();
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    if (isDuplicateIdException(e)) {
                        // go on happily
                        if (log.isDebugEnabled()) {
                            log.debug("Agent attempted to insert same tracking activity data again, may be ignored safely.", e);
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            if (logStatement != null) {
                logStatement.close();
            }
            if (attrStatement != null){
                attrStatement.close();
            }
        }
    }
    
    /**
     * refer to file TrackinActivityLogDO.hbm.xml
     *
     */
    private enum TrackingLogField{
        APP_NAME("Application Name") {
            String getValue(TrackingLogEntryWrapper entry) {
                return entry.getApplicationName();
            }
        },
        FROM_RESOURCE_NAME("From Resource Name", 512) {
            String getValue(TrackingLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getFromResourceInfo().getName();
            }
        },
        FROM_RESOURCE_OWNER_ID("From Resource Ower ID", 128) {
            String getValue(TrackingLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getFromResourceInfo().getOwnerId();
            }
        },
        TO_RESOURCE_NAME("To Resource Name", 512) {
            String getValue(TrackingLogEntryWrapper entry) {
                //already check to resource info is not null
                return entry.getToResourceInfo().getName();
            }
        },
        HOST_NAME("Host Name") {
            String getValue(TrackingLogEntryWrapper entry) {
                return entry.getHostName();
            }
        },
        HOST_IP("Host IP", 15) {
            String getValue(TrackingLogEntryWrapper entry) {
                return entry.getHostIP();
            }
        },
        USER_NAME("User Name") {
            String getValue(TrackingLogEntryWrapper entry) {
                return entry.getUserName();
            }
        },
        ;
        
        final String name;
        final int limit;

        private TrackingLogField(String name,int limit){
            this.name = name;
            this.limit = limit;
        }
        
        private TrackingLogField(String name){
            this(name, DEFAULT_VARCHAR_LIMIT);
        }
        
        abstract String getValue(TrackingLogEntryWrapper entry);
    }

    private void setLogArgumentValues(PreparedStatement statement,
                                      TrackingLogEntryWrapper entry, Calendar cal) throws SQLException {
        statement.setLong(1, entry.getUid());

        long ts = entry.getTimestamp();
        statement.setLong(2, ts);
        cal.setTimeInMillis(ts);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        //Sets the day number
        statement.setLong(4, cal.getTimeInMillis());

        //Sets the month number
        cal.set(Calendar.DAY_OF_MONTH, 1);
        statement.setLong(3, cal.getTimeInMillis());

        statement.setLong(5, entry.getHostId());
        statement.setLong(6, entry.getUserId());
        statement.setLong(7, entry.getApplicationId());
        setString(statement, 8, entry, TrackingLogField.APP_NAME);  	
        String actionCode = actionEnumUserType.getCodeByName(entry.getAction());
		
        if (actionCode == null) {
			String shortName = entry.getAction();
			actionCode = getActionCodeFromName("", shortName);
		}

        if (actionCode == null) {
            actionCode = UNKNOWN_ACTION_CODE;
        }
        statement.setString(9, actionCode);

        FromResourceInformation from = entry.getFromResourceInfo();
        setString(statement, 10, entry, TrackingLogField.FROM_RESOURCE_NAME);
        // Negative size means that the resource does not exist.
        // We put nulls into the attribute fields.
        if (from.getSize() < 0) {
            statement.setNull(11, Types.INTEGER);
            statement.setNull(12, Types.VARCHAR);
            statement.setNull(13, Types.INTEGER);
            statement.setNull(14, Types.INTEGER);
        } else {
            statement.setLong( 11, from.getSize() );
            setString(statement, 12, entry, TrackingLogField.FROM_RESOURCE_OWNER_ID);
            statement.setLong( 13, from.getCreatedDate() );
            statement.setLong( 14, from.getModifiedDate() );
        }

        ResourceInformation to = entry.getToResourceInfo();
        if (to == null) {
            statement.setNull(15, Types.VARCHAR);
        } else {
            setString(statement, 15, entry, TrackingLogField.TO_RESOURCE_NAME);
        }
        setString(statement, 16, entry, TrackingLogField.HOST_NAME);
        setString(statement, 17, entry, TrackingLogField.HOST_IP);
        String username = entry.getUserName();
        if(username == null || username.trim().length() == 0){
            // We can't have a null user name (same as empty string for Oracle) so use the user id
            statement.setString(18, Long.toString(entry.getUserId()));
        } else {
            setString(statement, 18, entry, TrackingLogField.USER_NAME);
        }
        statement.setInt(19, entry.getLevel());
    }
    
    private final void setAttrArgumentValues(PreparedStatement statement,
                                             TrackingLogEntryWrapper entry, String key, String value) throws SQLException {
        statement.setLong(1, entry.getUid());
        
        //refer to TrackingActivityLogCustomAttributeDO.hbm.xml
        setString(statement, 2, entry.getUid(), key, "Tracking Log Custom Attribute", "Key", DEFAULT_VARCHAR_LIMIT); 
        
        //the value already truncated by someone
        statement.setString(3, value);
    }
    /* End of TrackingLogEntry methods */



    /* Start of PolicyActivityLogEntry methods */
    
    
    @Override
    public void log(PolicyActivityLogEntry[] logEntries) throws DataSourceException {
        int length = logEntries.length;
        PolicyActivityLogEntryWrapper[] wrapped = new PolicyActivityLogEntryWrapper[length];
        for (int i = 0; i < length; i++) {
            wrapped[i] = new PolicyActivityLogEntryV1Wrapper(logEntries[i]);
        }
        log(wrapped);
    }

    @Override
    public void log(PolicyActivityLogEntryV2[] logEntries) throws DataSourceException {
        log((PolicyActivityLogEntryWrapper[]) logEntries);
    }

    @Override
    public void log(PolicyActivityLogEntryV3[] logEntries) throws DataSourceException {
        log((PolicyActivityLogEntryWrapper[]) logEntries);
    }

    @Override
    public void log(PolicyActivityLogEntryV4[] logEntries) throws DataSourceException {
        log((PolicyActivityLogEntryWrapper[]) logEntries);
    }
    
    @Override
    public void log(PolicyActivityLogEntryV5[] logEntries) throws DataSourceException {
        log((PolicyActivityLogEntryWrapper[]) logEntries);
    }
    /**
     * Persist the log entries to the activity data store. The first attempt
     * will be to insert the entries in batch mode. If that fails, the second
     * attempt is to insert them one by one.
     *
     * @param logEntriesToPersist
     *            the log entries to persist
     * @throws DataSourceException
     *             if persitence fails
     */
    public void log(PolicyActivityLogEntryWrapper[] entries) throws DataSourceException {
        Session session = null;
        Connection conn = null;
        try {
            session = getSession();
            session.beginTransaction();
            conn = session.connection();
            conn.setAutoCommit(false);
            insertBatch(entries, conn, session);
            conn.commit();
        } catch (SQLException sqle) {
            Log log = getLog();
            if (log.isInfoEnabled()) {
                log.info("Error while attempting to persist logs in batch mode.  Will attempt to insert one at a time.", sqle);
            }
            try {
                conn.rollback();
                insertIndividually(entries, conn, session);
            } catch (SQLException sqle2) {
                getLog().warn("Error while attempt to persist logs one at a time.");
                throw new DataSourceException(sqle2);
            }
        } catch (HibernateException he) {
            throw new DataSourceException(he);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }

    /**
     * Insert log entries one at a time, if inserting one entry fails, attempts
     * are made to insert remaining anyway. It won't propagate SQLExceptions in
     * case of individual insertion failures, only if it can't continue at all.
     *
     * @param entries
     * @param conn
     * @throws SQLException
     */
    private void insertIndividually(PolicyActivityLogEntryWrapper[] entries, Connection conn, Session s)
        throws SQLException {
        // Use the same Calendar instance for all Calendar-related calculations
        // for performance Reasons
        Calendar cal = Calendar.getInstance();
        PreparedStatement logStatement = null;
        PreparedStatement attrStatement = null;
        PreparedStatement tagStatement = null;
        
        try {
            Log log = getLog();
            logStatement = conn.prepareStatement(PA_INSERT_QUERY);

            IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);

            String insertAttrSql = formatter.formatInsert(
                PA_CUSTOM_ATTR_DESTINATION
            ,   PA_CUSTOM_ATTR_SOURCE_FIELDS
            ,   null
                );

            String insertTagsSql = formatter.formatInsert(
                PA_POLICY_TAGS_DESTINATION
            ,   PA_POLICY_TAGS_SOURCE_FIELDS
            ,   null
                );


            attrStatement = conn.prepareStatement(insertAttrSql);
            tagStatement = conn.prepareStatement(insertTagsSql);

            for (PolicyActivityLogEntryWrapper entry : entries) {
                setLogArgumentValues(logStatement, entry, cal);
                try {
                    logStatement.executeUpdate();

                    // prepare the statement for custom attributes
                    Map<String, DynamicAttributes> attributesMap = entry.getAttributesMap();
                    if(attributesMap!=null){
                    	 for (Map.Entry<String,DynamicAttributes> attributes : attributesMap.entrySet()) {
                    		String type = attributes.getKey();
                            for (Map.Entry<String,IEvalValue>  attr: attributes.getValue().entrySet()) {
                                // HACK: Bug 5818
                                // first check if the field is shorter than CUSTOM_ATTR_FIELD LENGTH,
                                // will only check multi-valued fields for now
                                String fullFieldValue = attr.getValue().getValue().toString();
                                String persistenceFieldValue = massageCustomAttrValue(fullFieldValue, entry.getUid(), true);  
                                log.warn("For type =  "+type+" attr key = "+attr.getKey()+" and value = "+persistenceFieldValue);
                                setAttrArgumentValues(attrStatement, entry, type, attr.getKey(), persistenceFieldValue);
                                attrStatement.addBatch();
                            }
                    	}
                    }

                    // prepare for policy tags attribute insertion
                    Collection<IPair<String, String>> tags = entry.getPolicyTags();

                    if (tags != null) {
                        for (IPair<String, String> tag : tags) {
                            tagStatement.setLong(1, entry.getUid());
                            setString(tagStatement, 2, entry.getUid(), tag.first(), "Policy Log Tag", "Key", DEFAULT_VARCHAR_LIMIT);
                                
                            tagStatement.setString(3, tag.second());
                            tagStatement.executeUpdate();
                        }
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    if (isDuplicateIdException(e)) {
                        // This might be a log that was sent twice from the endpoint (not a problem) or the endpoint might
                        // be repeating ids accidently (problem)
                        if (log.isWarnEnabled()) {
                            log.warn("HibernateLogWriter.insertIndividually: Failed to insert Policy Activity Log" 
                                     + getFormattedPolicyActivityLogEntry(entry)
                                     + "\nThere is already a record with the same ID in the database. This may be a repeated log."
                                     + "\nfrom the endpoint or the endpoint may be erroneously reusing log ids for different logs", e);
                            
                        }
                    } else {
                        if (log.isErrorEnabled()) {
                            log.error("HibernateLogWriter.insertIndividually: Failed to insert Policy Activity Log" 
                                      + getFormattedPolicyActivityLogEntry(entry) + " exception message: "
                                      + e.getMessage());
                        }
                        throw e;
                    }
                }
            }
        } finally {
            if (logStatement != null) {
                logStatement.close();
            }
            if (attrStatement != null){
                attrStatement.close();
            }
            if (tagStatement != null){
                tagStatement.close();
            }
        }
    }

    /**
     * Insert the log entires into the data store
     *
     * @param logEntriesToPersist
     *            the log entries to persist
     * @throws DataSourceException
     *             if persitence fails
     */
    private void insertBatch(PolicyActivityLogEntryWrapper[] entries, Connection conn, Session s)
        throws SQLException, DataSourceException {
        // Use the same Calendar instance for all Calendar-related calculations
        // for performance Reasons
        Calendar cal = Calendar.getInstance();
        PreparedStatement logStatement = null;
        PreparedStatement attrStatement = null;
        PreparedStatement tagStatement = null;

        try {
            logStatement = conn.prepareStatement(PA_INSERT_QUERY);

            IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);

            String insertAttrSql = formatter.formatInsert(
                PA_CUSTOM_ATTR_DESTINATION
            ,   PA_CUSTOM_ATTR_SOURCE_FIELDS
            ,   null
                );

            String insertTagsSql = formatter.formatInsert(
                PA_POLICY_TAGS_DESTINATION
            ,   PA_POLICY_TAGS_SOURCE_FIELDS
            ,   null
                );

            attrStatement = conn.prepareStatement(insertAttrSql);
            tagStatement = conn.prepareStatement(insertTagsSql);

            for (PolicyActivityLogEntryWrapper entry : entries) {
                setLogArgumentValues(logStatement, entry, cal);
                logStatement.addBatch();

             // prepare the statement for custom attributes
                Map<String, DynamicAttributes> attributesMap = entry.getAttributesMap();
                if(attributesMap!=null){
                	 for (Map.Entry<String,DynamicAttributes> attributes : attributesMap.entrySet()) {
                		String type = attributes.getKey();
                        for (Map.Entry<String,IEvalValue>  attr: attributes.getValue().entrySet()) {
                            // HACK: Bug 5818
                            // first check if the field is shorter than CUSTOM_ATTR_FIELD LENGTH,
                            // will only check multi-valued fields for now
                            String fullFieldValue = attr.getValue().getValue().toString();
                            String persistenceFieldValue = massageCustomAttrValue(fullFieldValue, entry.getUid(), true);                        
                            setAttrArgumentValues(attrStatement, entry, type, attr.getKey(), persistenceFieldValue);
                            attrStatement.addBatch();
                        }
                	}
                }


                // prepare for policy tags attribute insertion
                Collection<IPair<String, String>> tags = entry.getPolicyTags();
                
                if (tags != null) {
                    for (IPair<String, String> tag : tags) {
                        
                        tagStatement.setLong(1, entry.getUid());
                        setString(tagStatement, 2, entry.getUid(), tag.first(), "Policy Log Tag", "Key", DEFAULT_VARCHAR_LIMIT);
                        
                        tagStatement.setString(3, tag.second());
                        tagStatement.addBatch();
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
        // do not catch SQLException when executing batch, let the outer layer
        // handle it
        logStatement.executeBatch();
        logStatement.close();
        if (attrStatement != null){
            attrStatement.executeBatch();
            attrStatement.close();
        }

        if (tagStatement != null) {
            tagStatement.executeBatch();
            tagStatement.close();
        }
    }

    /**
     * refer to file PolicyActivityLogDO.hbm.xml
     *
     */
    private enum PolicyLogField{
        APP_NAME("Application Name") {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                return entry.getApplicationName();
            }
        },
        FROM_RESOURCE_NAME("From Resource Name", 512) {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getFromResourceInfo().getName();
            }
        },
        FROM_RESOURCE_OWNER_ID("From Resource Ower ID", 128) {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getFromResourceInfo().getOwnerId();
            }
        },
        TO_RESOURCE_NAME("To Resource Name", 512) {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                //already check to resource info is not null
                return entry.getToResourceInfo().getName();
            }
        },
        HOST_NAME("Host Name") {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                return entry.getHostName();
            }
        },
        HOST_IP("Host IP", 15) {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                return entry.getHostIP();
            }
        },
        USER_NAME("User Name") {
            String getValue(PolicyActivityLogEntryWrapper entry) {
                return entry.getUserName();
            }
        },
        ;
        
        final String name;
        final int limit;

        private PolicyLogField(String name,int limit){
            this.name = name;
            this.limit = limit;
        }
        
        private PolicyLogField(String name){
            this(name, DEFAULT_VARCHAR_LIMIT);
        }
        
        abstract String getValue(PolicyActivityLogEntryWrapper entry);
    }

    private void setLogArgumentValues(PreparedStatement statement, PolicyActivityLogEntryWrapper entry,
                                      Calendar cal) throws SQLException {
        statement.setLong(1, entry.getUid());

        long ts = entry.getTimestamp();
        statement.setLong(2, ts);
        cal.setTimeInMillis(ts);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        //Sets the day number
        statement.setLong(4, cal.getTimeInMillis());

        //Sets the month number
        cal.set(Calendar.DAY_OF_MONTH, 1);
        statement.setLong(3, cal.getTimeInMillis());

        statement.setLong(5, entry.getHostId());
        statement.setLong(6, entry.getUserId());
        statement.setLong(7, entry.getApplicationId());
        setString(statement, 8, entry, PolicyLogField.APP_NAME);
        
		// system code/ generated code (2 digit)	
		String shortName = entry.getAction();
		String resourceType = null;
		String actionCode = null;

		Map<String, DynamicAttributes> attributesMap = entry.getAttributesMap();
		if (attributesMap != null) {
			for (Map.Entry<String, DynamicAttributes> attributes : attributesMap.entrySet()) {
				String type = attributes.getKey();
				if (ATTRIBUTE_TYPE_FROM_RESOURCE.equalsIgnoreCase(type)) {
					DynamicAttributes dynResAttrs = attributesMap.get(type);
					resourceType = dynResAttrs.getString(RESOURCE_TYPE);
				}
			}
		}
		actionCode = getActionCodeFromName(resourceType, shortName);

		if (actionCode == null) {
			actionCode = actionEnumUserType.getCodeByName(entry.getAction());
		}
		if (actionCode == null) {
			actionCode = UNKNOWN_ACTION_CODE;
		}
		statement.setString(9, actionCode);

        FromResourceInformation from = entry.getFromResourceInfo();
        setString(statement, 10, entry, PolicyLogField.FROM_RESOURCE_NAME);
        // Negative size means that the resource does not exist.
        // We put nulls into the attribute fields.
        if (from.getSize() < 0) {
            statement.setNull(11, Types.INTEGER);
            statement.setNull(12, Types.VARCHAR);
            statement.setNull(13, Types.INTEGER);
            statement.setNull(14, Types.INTEGER);
        } else {
            statement.setLong(11, from.getSize());
            setString(statement, 12, entry, PolicyLogField.FROM_RESOURCE_OWNER_ID);
            statement.setLong(13, from.getCreatedDate());
            statement.setLong(14, from.getModifiedDate());
        }

        ResourceInformation to = entry.getToResourceInfo();
        if (to == null) {
            statement.setNull(15, Types.VARCHAR);
        } else {
            setString(statement, 15, entry, PolicyLogField.TO_RESOURCE_NAME);
        }
        statement.setLong(16, entry.getPolicyId());
        statement.setString(17, policyDecisionUserType.getCodeByType(entry.getPolicyDecision()));
        statement.setLong(18, entry.getDecisionRequestId());
        setString(statement, 19, entry, PolicyLogField.HOST_NAME);
        setString(statement, 20, entry, PolicyLogField.HOST_IP);
        String username = entry.getUserName();
        if(username == null || username.trim().length() == 0){
            // We can't have a null user name (same as empty string for Oracle) so use the user id
            statement.setString(21, Long.toString(entry.getUserId()));
        } else {
            setString(statement, 21, entry, PolicyLogField.USER_NAME);
        }
        statement.setInt(22, entry.getLevel());
    }

    private void setAttrArgumentValues(PreparedStatement statement,
                                       PolicyActivityLogEntryWrapper entry, String type, String key, String value) throws SQLException {
        statement.setLong(1, entry.getUid());
        
        statement.setString( 2, type);
        
        setString(statement, 3, entry.getUid(), key, "Policy Log Custom Attribute", "Key", DEFAULT_VARCHAR_LIMIT); 
        
        //already truncated by someone
        statement.setString(4, value);
    }
    
    
    
    @Override
    public void log(PolicyAssistantLogEntry[] logEntries) throws DataSourceException {
        log((PolicyAssistantLogEntryWrapper[])logEntries);
    }

    /**
     * Persist the log entries to the obligation data store. The first attempt
     * will be to insert the entries in batch mode. If that fails, the second
     * attempt is to insert them one by one.
     *
     * @param logEntriesToPersist
     *            the log entries to persist
     * @throws DataSourceException
     *             if persitence fails
     */
    public void log(PolicyAssistantLogEntryWrapper[] entries) throws DataSourceException {
        Session session = null;
        Connection conn = null;
        try {
            session = getSession();
            session.beginTransaction();
            conn = session.connection();
            insertBatch(entries, conn);
            conn.commit();
        } catch (SQLException sqle) {
            Log log = getLog();
            if (log.isInfoEnabled()) {
                log.info("Error while attempting to persist logs in batch mode.  Will attempt to insert one at a time.", sqle);
            }
            try {
                conn.rollback();
                insertIndividually(entries, conn);
            } catch (SQLException sqle2) {
                getLog().warn("Error while attempt to persist logs one at a time.");
                throw new DataSourceException(sqle2);
            }
        } catch (HibernateException he) {
            throw new DataSourceException(he);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }

    private void insertIndividually(PolicyAssistantLogEntryWrapper[] entries, Connection conn)
        throws SQLException {
        PreparedStatement logStatement = null;
        PolicyAssistantLogEntryWrapper thisEntry = null;
        
        try {
            logStatement = conn.prepareStatement(PASS_INSERT_QUERY);
            
            for (PolicyAssistantLogEntryWrapper entry : entries) {
                thisEntry = entry;
                setLogArgumentValues(logStatement, entry);
                logStatement.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            conn.rollback();
            if (isDuplicateIdException(e)) {
                // go on happily
                if (log.isDebugEnabled()) {
                    log.debug("Agent attempted to insert same obligation data again, may be ignored safely.", e);
                }
            } else {
                if (log.isErrorEnabled()) {
                    getLog().error("Exception: " + e.getMessage() + 
                                   " while trying to insert policy assistant entry with id: " + 
                                   thisEntry.getUid() + " and policy log id: " + thisEntry.getLogIdentifier());
                }
                throw e;
            }
        }

    }

    private void insertBatch(PolicyAssistantLogEntryWrapper[] entries, Connection conn)
        throws SQLException, DataSourceException {
        PreparedStatement logStatement = null;
        
        try {
            logStatement = conn.prepareStatement(PASS_INSERT_QUERY);
            
            for (PolicyAssistantLogEntryWrapper entry : entries) {
                setLogArgumentValues(logStatement, entry);
                logStatement.addBatch();
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
        // do not catch SQLException when executing batch, let the outer layer
        // handle it
        logStatement.executeBatch();
        logStatement.close();
    }

    /**
     * refer to file PolicyAssistantLogDO.hbm.xml
     *
     */
    private enum ObligationLogField {
        NAME("Name", 256) {
            String getValue(PolicyAssistantLogEntryWrapper entry) {
                return entry.getAssistantName();
            }
        },
        ATTR1("Attribute One", 1024) {
            String getValue(PolicyAssistantLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getAttrOne();
            }
        },
        ATTR2("Attribute Two", 1024) {
            String getValue(PolicyAssistantLogEntryWrapper entry) {
                //already check from resource info is not null
                return entry.getAttrTwo();
            }
        },
        ATTR3("Attribute Three", 1024) {
            String getValue(PolicyAssistantLogEntryWrapper entry) {
                //already check to resource info is not null
                return entry.getAttrThree();
            }
        },
        ;
        
        final String name;
        final int limit;

        private ObligationLogField(String name,int limit){
            this.name = name;
            this.limit = limit;
        }
        
        private ObligationLogField(String name){
            this(name, DEFAULT_VARCHAR_LIMIT);
        }
        
        abstract String getValue(PolicyAssistantLogEntryWrapper entry);
    }
    
    private void setLogArgumentValues(PreparedStatement statement, PolicyAssistantLogEntryWrapper entry) 
        throws SQLException {
        statement.setLong(1, entry.getUid());

        long logIdentifier = 0;
        
        try {
            logIdentifier = Long.parseLong(entry.getLogIdentifier());
        } catch (NumberFormatException e) {
        }
        statement.setLong(2, logIdentifier);
        setString(statement, 3, entry, ObligationLogField.NAME);
        setString(statement, 4, entry, ObligationLogField.ATTR1);
        setString(statement, 5, entry, ObligationLogField.ATTR2);
        setString(statement, 6, entry, ObligationLogField.ATTR3);
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

    /**!
     * Returns a data source object that can be used to create sessions.
     *
     * @return IHibernateDataSource Data source object
     */
    @SuppressWarnings("deprecation")
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(
            DestinyRepository.ACTIVITY_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for LogWriter.");
        }

        return dataSource;
    }
    
    private String getFormattedPolicyActivityLogEntry(PolicyActivityLogEntryWrapper thisEntry) {
        StringBuilder sb = new StringBuilder();
        if (thisEntry != null) {
            sb.append(" id: "       ).append( thisEntry.getUid()) 
                .append(" policyId: " ).append( thisEntry.getPolicyId()) 
                .append(" hostId: "   ).append( thisEntry.getHostId()) 
                .append(" hostIP: "   ).append( thisEntry.getHostIP()) 
                .append(" userName: " ).append( thisEntry.getUserName())
                .append(" action: "   ).append( thisEntry.getAction());
        }
        return sb.toString();
    }
    
    // HACK: Bug 5818
    // first check if the field is shorter than CUSTOM_ATTR_FIELD LENGTH,
    // will only check multi-valued fields for now
    private String massageCustomAttrValue(
        String customAttrVal, long uid, boolean isPolicyLog) {
        String persistedCustomAttr = customAttrVal;
        if (customAttrVal.length() > customAttrFieldLength){
            persistedCustomAttr = 
                customAttrVal.substring(0, customAttrFieldLength - 1);            
            persistedCustomAttr += "]";
            if (log.isTraceEnabled()) {
             
                log.trace("HibernateLogWriter.massageCustomAttrValue: " + 
                          "truncating custom attribute value of the entry with uid: " + uid +
                          " Log Type: " + (isPolicyLog ? "Policy" : "Tracking"));
            }
        } 
        return persistedCustomAttr;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    public void init() {
        customAttrFieldLength = configuration != null 
                                ? configuration.get(CUSTOM_ATTR_FIELD_LENGTH_KEY, DEFAULT_CUSTOM_ATTR_FIELD_LENGTH)
                                : DEFAULT_CUSTOM_ATTR_FIELD_LENGTH;
        if(customAttrFieldLength <= 0){
            throw new IllegalArgumentException(CUSTOM_ATTR_FIELD_LENGTH_KEY
                                               + " must be a positive number. The input value is " + customAttrFieldLength);
        }
    }
    
    /**
     * Returns true if the exception represents a duplicate id violation
     * exception. This function handles Postgres and Oracle and MS SQL error codes.
     * These error codes do not overlap accross databases, so it is safe to
     * compare them at the same time.
     *
     * Note that for each new database we support, we need to change this method to
     * add the appropriate error code, since we are interested in the unique id 
     * violation code and that differs across databases.
     * @param e  exception to process
     * @return true if the exception is a duplicate id violation, false
     *         otherwise
     */
    private boolean isDuplicateIdException(SQLException e) {
        
        if (CONSTRAINT_VIOLATION_SQL_STATE.equals(e.getSQLState()) && 
            e.getErrorCode() == ORACLE_UNIQUE_ID_VIOLATION_ERR_CODE) {
            return true;
        } else if (POSTGRESQL_DUPLICATE_ID_SQL_STATE.equals(e.getSQLState())) {
            return true;
        } else if (CONSTRAINT_VIOLATION_SQL_STATE.equals(e.getSQLState()) &&
                   e.getErrorCode() == MS_SQL_UNIQUE_ID_VIOLATION_ERR_CODE) {
            return true;
        }
        return false;
    }

    
    private static final String TRUNCATE_WARNING_TEMPLAYE = 
    "The length of the %1$s value '%2$s' (%3$d) is greater than the schema limit(%4$d)." 
    + " The value will be truncated. Log id is '%5$d'. The value before truncation is '%6$s'.";
    
    private void setString(PreparedStatement statement, int parameterIndex, TrackingLogEntryWrapper entry,
                           TrackingLogField field) throws SQLException {
        String x = field.getValue(entry);
        setString(statement, parameterIndex, entry.getUid(), x, "Tracking Log", field.name,
                  field.limit);
    }
    
    private void setString(PreparedStatement statement, int parameterIndex, PolicyActivityLogEntryWrapper entry,
                           PolicyLogField field) throws SQLException {
        String x = field.getValue(entry);
        setString(statement, parameterIndex, entry.getUid(), x, "Policy Log", field.name,
                  field.limit);
    }
    
    private void setString(PreparedStatement statement, int parameterIndex, PolicyAssistantLogEntryWrapper entry,
                           ObligationLogField field) throws SQLException {
        String x = field.getValue(entry);
        setString(statement, parameterIndex, entry.getUid(), x, "Obligation Log", field.name,
                  field.limit);
    }
    
    private void setString(PreparedStatement statement, int parameterIndex, long logId,
                           String x, String logType, String fieldName, int limit) throws SQLException {
        if (x != null && x.length() > limit) {
            log.warn(String.format(TRUNCATE_WARNING_TEMPLAYE, logType, fieldName, x.length(), limit, logId, x));
            StringBuilder sb = new StringBuilder(limit);
            sb.append(x, 0, limit - 3).append("...");
            x = sb.toString();
        }
        statement.setString(parameterIndex, x);
    }
    
	private String getActionCodeFromName(String resourceType, String shortName) throws SQLException {
		String key;
		if (resourceType == null || resourceType.isEmpty()) {
			key = shortName;
		} else {
			key = resourceType.concat("::").concat(shortName);
		}

		String shortCode = actionCodeMap.get(key);

		if (shortCode == null) {
			// load from DB
			Session session = null;
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				session = getSession();
				conn = session.connection();
				stmt = conn.prepareStatement(ACTION_SHORT_CODE_SQL);
				stmt.setString(1, resourceType);
				stmt.setString(2, shortName);
				stmt.setString(3, "ACTIVE");
				rs = stmt.executeQuery();
				if (rs.next()) {
					shortCode = rs.getString(1);
				}
				LOG.debug("Action short code found [ Resource Type : " + resourceType + ", Short name : " + shortName
						+ ", Code :" + shortCode + " ]");

				if (shortCode != null) {
					actionCodeMap.put(key, shortCode);
				}
			} catch (HibernateException he) {
				throw new SQLException(he);
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				HibernateUtils.closeSession(session, getLog());
			}
		}
		return shortCode;
	}
}
