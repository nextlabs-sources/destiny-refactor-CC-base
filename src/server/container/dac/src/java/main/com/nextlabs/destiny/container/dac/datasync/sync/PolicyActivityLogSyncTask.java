/*
 * Created on Jun 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.sync;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.datastore.hibernate.BatchWriter;
import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.destiny.container.dac.datasync.Constants;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLog;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLog.CustomAttribute;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLogWriter;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/sync/PolicyActivityLogSyncTask.java#1 $
 */

public class PolicyActivityLogSyncTask extends SyncTaskBase<ReportPolicyActivityLog> {
    private static final Log LOG = LogFactory.getLog(PolicyActivityLogSyncTask.class);
    private static final Log PROFILE_LOG = LogFactory.getLog(PolicyActivityLogSyncTask.class.getName() + ".profiling");
    
    /* policy activity log    */
    private static final int ID_COLUMN;
    
    private static final int TIME_COLUMN;
    
    private static final int HOST_ID_COLUMN;
    private static final int HOST_IP_COLUMN;
    private static final int HOST_NAME_COLUMN;
    
    private static final int USER_ID_COLUMN;
    private static final int USER_NAME_COLUMN;

    private static final int APP_ID_COLUMN;
    private static final int APP_NAME_COLUMN;
    
    private static final int ACTION_COLUMN;
    private static final int POLICY_ID_COLUMN;
    private static final int POLICY_DECISION_COLUMN;
    private static final int DECISION_REQUEST_ID_COLUMN;
    private static final int LEVEL_COLUMN;
    
    private static final int FROM_RESOURCE_NAME_COLUMN;
    private static final int FROM_RESOURCE_SIZE_COLUMN;
    private static final int FROM_RESOURCE_OWNER_ID_COLUMN;
    private static final int FROM_RESOURCE_CREATED_DATE_COLUMN;
    private static final int FROM_RESOURCE_MODIFIED_DATE_COLUMN;
    
    private static final int TO_RESOURCE_NAME_COLUMN;
    
    private static final String SELECT_LOG_QUERY;
    
	protected ReportPolicyActivityLog prevRecord = null;
	protected long callsToDictAPI = 0;
	protected long callsAvoided = 0;
	
    static {
        int i = 1;
        ID_COLUMN = i++;
        
        TIME_COLUMN = i++;
        
        HOST_ID_COLUMN = i++;
        HOST_IP_COLUMN = i++;
        HOST_NAME_COLUMN = i++;
        
        USER_ID_COLUMN = i++;
        USER_NAME_COLUMN = i++;

        APP_ID_COLUMN = i++;
        APP_NAME_COLUMN = i++;
        
        ACTION_COLUMN = i++;
        POLICY_ID_COLUMN = i++;
        POLICY_DECISION_COLUMN = i++;
        DECISION_REQUEST_ID_COLUMN = i++;
        LEVEL_COLUMN = i++;
        
        FROM_RESOURCE_NAME_COLUMN = i++;
        FROM_RESOURCE_SIZE_COLUMN = i++;
        FROM_RESOURCE_OWNER_ID_COLUMN = i++;
        FROM_RESOURCE_CREATED_DATE_COLUMN = i++;
        FROM_RESOURCE_MODIFIED_DATE_COLUMN = i++;
        
        TO_RESOURCE_NAME_COLUMN = i++;
        
        SELECT_LOG_QUERY = "select id,time"
                + ",host_id,host_ip,host_name"
                + ",user_id,user_name"
                + ",application_id,application_name"
                + ",action,policy_id,policy_decision,decision_request_id,log_level"
                + ",fromresourcename,fromresourcesize,fromresourceownerid"
                + ",fromresourcecreateddate,fromresourcemodifieddate,toresourcename"
                + " from " + SharedLib.PA_TABLE 
                + " where sync_done is null ORDER BY user_id ASC, time ASC";
    }
    
    /* policy custom attribute */
    private static final int CUSTOM_ID_COLUMN;
    private static final int CUSTOM_POLICY_ID_COLUMN;
	private static final int CUSTOM_ATTR_TYPE_COLUMN;
    private static final int CUSTOM_ATTR_NAME_COLUMN;
    private static final int CUSTOM_ATTR_VALUE_COLUMN;
    
    /* Policy Tag attribute */
    private static final int POLCY_TAG_ID_COLUMN;
    private static final int POLICY_TAG_POLICY_ID_COLUMN;
    private static final int POLICY_TAG_NAME_COLUMN;
    private static final int POLICY_TAG_VALUE_COLUMN;
    
    private static final String SELECT_CUSTOM_ATTR_QUERY_TEMPLATE;
    
    private static final String SELECT_POLICY_TAGS_QUERY_TEMPLATE;
    
    static{
        int i = 1;
        CUSTOM_ID_COLUMN = i++;
        CUSTOM_POLICY_ID_COLUMN = i++;
		CUSTOM_ATTR_TYPE_COLUMN = i++;
        CUSTOM_ATTR_NAME_COLUMN = i++;
        CUSTOM_ATTR_VALUE_COLUMN = i++;
        
        SELECT_CUSTOM_ATTR_QUERY_TEMPLATE = 
            "select id,policy_log_id,attribute_type,attribute_name,attribute_value" 
            + " from " + SharedLib.PA_CUST_ATTR_TABLE
            + " where policy_log_id in (%s)";
        
        i =1;
        POLCY_TAG_ID_COLUMN = i++;
        POLICY_TAG_POLICY_ID_COLUMN = i++;
        POLICY_TAG_NAME_COLUMN = i++;
        POLICY_TAG_VALUE_COLUMN = i++; 
        
        SELECT_POLICY_TAGS_QUERY_TEMPLATE = 
    		"select id,policy_log_id,tag_name,tag_value" 
            + " from " + Constants.POLICY_TAGS
            + " where policy_log_id in (%s)";
        
    }
    /* end policy custom attribute */    
    
    
    
    /* cached policy */
    private static final int CACHED_POLICY_ID_COLUMN;
    private static final int CACHED_POLICY_NAME_COLUMN;
    private static final int CACHED_POLICY_FULLNAME_COLUMN;
    
    private static final String SELECT_CACHED_POLICY_QUERY_TEMPLATE;
    
    static{
        int i = 1;
        CACHED_POLICY_ID_COLUMN = i++;
        CACHED_POLICY_NAME_COLUMN = i++;
        CACHED_POLICY_FULLNAME_COLUMN = i++;
        
        SELECT_CACHED_POLICY_QUERY_TEMPLATE =
                "select id,name,fullname"
                + " from " + SharedLib.CACHED_POLICY 
                + " where id in";
        
    }
    /* end cached policy */

    
    private PreparedStatement selectLogStatement = null;
    
    
    public PolicyActivityLogSyncTask() {
        super(ReportPolicyActivityLog.class, String.format(COUNT_QUERY_TEMPLATE, SharedLib.PA_TABLE));
    }

    @Override
    protected void start() throws Exception {
        super.start();
        update.setPrefix("Policy Activity Log Synchronization");
        String sql = getDialect().getLimitString(SELECT_LOG_QUERY, false, getBatchSize());
        LOG.trace(sql);
        selectLogStatement = connection.prepareStatement(sql);
        
        //initialize Attribute column mapping here
        populateAttrColumnMap();
    }
    
    
    @Override
    protected int parse() throws Exception {
    	
		boolean lctBeforeUserRecord = false;    	
		
		/*
		 * last consistent time of dictionary
		 */
		Date lct = dict.getLatestConsistentTime();
		
    	transform = new HashMap<Number, ReportPolicyActivityLog>(getBatchSize());
        
        List<ReportPolicyActivityLog> unknownPolicyEntries =
                new ArrayList<ReportPolicyActivityLog>(getBatchSize());
        List<ReportPolicyActivityLog> unknownUserEntries =
                new ArrayList<ReportPolicyActivityLog>(getBatchSize());
        
        
        Set<Long> unknownPolicyIds = new HashSet<Long>(getBatchSize());
        Set<Long> unknownUserIds = new HashSet<Long>(getBatchSize());
        Set<Long> userIds = new HashSet<Long>(getBatchSize());
        
        LOG.trace("getRemainingTime() = " + getRemainingTime());
        selectLogStatement.setQueryTimeout(getRemainingTime());
        if (getDialect().supportsVariableLimit()) {
            selectLogStatement.setInt(1, getBatchSize());
        }
        
        ResultSet r = null; 
            
        try{
            long startTime = 0;

            if (PROFILE_LOG.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            
            r = selectLogStatement.executeQuery();

            if (PROFILE_LOG.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                
                if (time > 2000 || PROFILE_LOG.isTraceEnabled()) {
                    PROFILE_LOG.debug("Time taken for selectLogStatement query: "
                            + (System.currentTimeMillis() - startTime) + " ms");
                }
            }
            
            Calendar cal = Calendar.getInstance();
            
            while (r.next()) {
                ReportPolicyActivityLog t = new ReportPolicyActivityLog(attrColumnMappingConfig);
                t.id    = r.getLong(ID_COLUMN);
                long time = r.getLong(TIME_COLUMN);
                
                t.time  = new Timestamp(time);
                t.dateTime = time;
                
                cal.setTimeInMillis(time);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                t.minute   = cal.getTimeInMillis();
                
                cal.set(Calendar.MINUTE, 0);
                t.hour   = cal.getTimeInMillis();
                
                cal.set(Calendar.HOUR_OF_DAY, 0);
                t.day   = cal.getTimeInMillis();
                
                cal.set(Calendar.DAY_OF_WEEK, 1);
                t.week  = cal.getTimeInMillis(); 
                
                cal.setTimeInMillis(time);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                t.month = cal.getTimeInMillis();
                
                t.hostId    = r.getLong(HOST_ID_COLUMN);
                t.hostIp    = r.getString(HOST_IP_COLUMN);
                t.hostName  = r.getString(HOST_NAME_COLUMN);
                
                // set the username from the original log.
                // the username will be overridden if the user is in cached_user table
                t.userName  = r.getString(USER_NAME_COLUMN);
                if (setUser(t, r.getLong(USER_ID_COLUMN))) {
                    unknownUserEntries.add(t);
                    unknownUserIds.add(t.userId);
                }
                
                userIds.add(t.userId);
                
                t.applicationId     = r.getLong(APP_ID_COLUMN);
                t.applicationName   = r.getString(APP_NAME_COLUMN);
                
                t.action            = r.getString(ACTION_COLUMN);
                
                if (setPolicy(t, r.getLong(POLICY_ID_COLUMN))) {
                    unknownPolicyEntries.add(t);
                    unknownPolicyIds.add(t.policyId);
                }
               
                t.policyDecision    = r.getString(POLICY_DECISION_COLUMN);
                t.decisionRequestId = r.getLong(DECISION_REQUEST_ID_COLUMN);
                t.logLevel          = r.getInt(LEVEL_COLUMN);
                
                t.fromResourceName          = r.getString(FROM_RESOURCE_NAME_COLUMN);
                t.fromResourceSize          = r.getLong(FROM_RESOURCE_SIZE_COLUMN);
                t.fromResourceOwnerId       = r.getString(FROM_RESOURCE_OWNER_ID_COLUMN);
                t.fromResourceCreatedDate   = r.getLong(FROM_RESOURCE_CREATED_DATE_COLUMN);
                t.fromResourceModifiedDate  = r.getLong(FROM_RESOURCE_MODIFIED_DATE_COLUMN);
    
                setFromResourceSplitedName(t);
                
                t.fromResourceName = lower(t.fromResourceName);
                
                t.toResourceName            = r.getString(TO_RESOURCE_NAME_COLUMN);
                
				/*
				 * we are trying to optimize calls to dictionary API
				 * 
				 * We have changed select query to sort by user, time.
				 * If the sort order is changed, this optimization will not work
				 * 
				 */
				
				
				if (prevRecord != null) {
					
					if (prevRecord.userId != t.userId) {
						
						/*
						 * Did the first record for this new user happened
						 * after last consistent time
						 */
						if (t.time.getTime() >= lct.getTime()) {
							lctBeforeUserRecord = true;
						} else {
							lctBeforeUserRecord = false;
						}
						callsToDictAPI++;
						solveUserAttributes(t);
					}
					else
					{
						if (lctBeforeUserRecord)
						{
							/*
							 * if the user's first record has occurred after
							 * dictionary's last consistent time, then we can
							 * reuse the already lookup values
							 */
							t.userAttrs = prevRecord.userAttrs;
							callsAvoided++;
						}
						else if(t.time.getTime() == prevRecord.time.getTime())
						{
							t.userAttrs = prevRecord.userAttrs;
							callsAvoided++;
						}
						else
						{
							/*
							 * if the current log has occurred after dictionary's
							 * last consistent time, we can avoid dictionary API call for 
							 * next few records of the same 
							 * 
							 */
							if (t.time.getTime() >= lct.getTime()) {
								lctBeforeUserRecord = true;
							} else {
								lctBeforeUserRecord = false;
							}
							solveUserAttributes(t);
							callsToDictAPI++;
						}
					}
				}
				else
				{
					/*
					 * Did the first record for this first user happened
					 * after last consistent time
					 */
					if (t.time.getTime() >= lct.getTime()) {
						lctBeforeUserRecord = true;
					} else {
						lctBeforeUserRecord = false;
					}
					solveUserAttributes(t);
					callsToDictAPI++;
				}
				
				prevRecord = t;
                
                transform.put(t.id, t);
            }
        } finally {
            close(r);
        }
        
        if(LOG.isTraceEnabled()){
            StringBuilder sb = new StringBuilder();
            sb.append("get ").append(transform.size()).append(" from " + SharedLib.PA_TABLE)
                .append("\n  # of unknownPolicyEntries = ").append(unknownPolicyEntries.size())
                .append("\n  # of unknownPolicyIds = ").append(unknownPolicyIds.size())
                .append("\n  # of unknownUserEntries = ").append(unknownUserEntries.size())
                .append("\n  # of unknownUserIds = ").append(unknownUserIds.size())
                .append("\n  # of callsToDictAPI = ").append(callsToDictAPI)
                .append("\n  # of callsAvoidedToDictAPI = ").append(callsAvoided);

            LOG.trace(sb.toString());
        }
        
        solveUnknownPolicies(unknownPolicyEntries, unknownPolicyIds);
        
        solveUnknownUsers(unknownUserIds, unknownUserEntries);

        solvePolicyTags();
        
        solveCustomAttribute();
        
        return transform.size();
    }
    
    @Override
	protected void solvePolicyTags(Set<Long> ids) throws SQLException {
        if (ids == null || ids.size() == 0) {
            return;
        }
        String sql = String.format(SELECT_POLICY_TAGS_QUERY_TEMPLATE, 
                CollectionUtils.asString(ids, ","));
        
        if (!policyAttributesBlackList.isEmpty())
        {
        	sql = sql + " AND tag_name " + getNOTINSQLQuery(policyAttributesBlackList);
        }
                
        Statement statement = connection.createStatement();
        ResultSet rs = null;
        try{
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                long policyId = rs.getLong(POLICY_TAG_POLICY_ID_COLUMN);
                
                String tagName = rs.getString(POLICY_TAG_NAME_COLUMN);
                String tagValue = rs.getString(POLICY_TAG_VALUE_COLUMN);
                
                ReportPolicyActivityLog t = transform.get(policyId);
                assert t != null;
                if(t.policyAttrs == null){
                    t.policyAttrs = new HashMap<String, String>();
                }
                t.policyAttrs.put(tagName, tagValue);
            }
        }finally{
            close(rs);
            statement.close();
        }    	
	}
    
    
    @Override
    protected void solveCustomAttribute(Set<Long> ids) throws SQLException{
        if (ids == null || ids.size() == 0) {
            return;
        }
        String sql = String.format(SELECT_CUSTOM_ATTR_QUERY_TEMPLATE, 
                CollectionUtils.asString(ids, ","));
        
        if (!resourceAttributesBlackList.isEmpty())
        {
        	sql = sql + " AND attribute_name " + getNOTINSQLQuery(resourceAttributesBlackList);
        }
                
        Statement statement = connection.createStatement();
        ResultSet rs = null;
        try{
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                long policyId = rs.getLong(CUSTOM_POLICY_ID_COLUMN);
                
                ReportPolicyActivityLog t = transform.get(policyId);
                assert t != null;
                
                String rawAttrType = rs.getString(CUSTOM_ATTR_TYPE_COLUMN);
                /*
                 * special treatment for dynamic user attributes to resolve against the
                 * enrolled user attributes
                 */
        		if (CustomAttribute.USER_RAW_TYPE.equals(rawAttrType)) {
                    if(t.dynamicUserAttrs == null){
                        t.dynamicUserAttrs = new HashMap<String,String>();
                    }
                    t.dynamicUserAttrs.put(rs.getString(CUSTOM_ATTR_NAME_COLUMN).toLowerCase(), rs.getString(CUSTOM_ATTR_VALUE_COLUMN));
        		}
        		else {
                    CustomAttribute tca = new CustomAttribute();
                    tca.id = rs.getLong(CUSTOM_ID_COLUMN);
    				tca.attributeType = rawAttrType;
                    tca.attributeName = rs.getString(CUSTOM_ATTR_NAME_COLUMN);
                    tca.attributeValue = rs.getString(CUSTOM_ATTR_VALUE_COLUMN);
                    
                    if(t.attrs == null){
                        t.attrs = new LinkedList<CustomAttribute>();
                    }
                    t.attrs.add(tca);        			
        		}
            }
        }finally{
            close(rs);
            statement.close();
        }
    }
    
    // return true is the policy is not in cache
    protected boolean setPolicy(ReportPolicyActivityLog log, long policyId) throws CacheException {
        log.policyId = policyId;
        Element policyElement = policyCache.get(policyId);
        if (policyElement != null) {
            String[] policy = (String[])policyElement.getValue();
            if (policy != null) {
                log.policyName = policy[0];
                log.policyFullname = policy[1];
            } 
            //else{ I have seen this policy before but I can't find it.}
            
            return false;
        }else{
            return true;
        }
    }

    private void solveUnknownPolicies(List<ReportPolicyActivityLog> unknownPolicyEntries,
            Set<Long> unknownPolicyIds)
            throws SQLException, CacheException {
        //solve all unknown policies
        if(!unknownPolicyIds.isEmpty()){
            int size = unknownPolicyIds.size();
            PreparedStatement s = connection.prepareStatement(SELECT_CACHED_POLICY_QUERY_TEMPLATE
                    + SQLHelper.makeInList(size));
            ResultSet policiesResult = null;
            try {

                int i = 1;
                for (long policyId : unknownPolicyIds) {
                    s.setLong(i++, policyId);
                }
                
                policiesResult = s.executeQuery();

                while(policiesResult.next()){
                    long id = policiesResult.getLong(CACHED_POLICY_ID_COLUMN);
                    String name = policiesResult.getString(CACHED_POLICY_NAME_COLUMN);
                    String fullname = policiesResult.getString(CACHED_POLICY_FULLNAME_COLUMN);
                    
                    policyCache.put(new Element(id, new String[] { name, lower(fullname) }));
                    
                    //now I know the id
                    unknownPolicyIds.remove(id);
                }
            } finally {
                close(policiesResult);
                s.close();
            }
            
            //the rest in the database
            if (!unknownPolicyIds.isEmpty()) {
                for (long policyId : unknownPolicyIds) {
                    policyCache.put(new Element(policyId, null));
                }
            }
            
            for (ReportPolicyActivityLog unknownPolicyEntry : unknownPolicyEntries) {
                Element e = policyCache.get(unknownPolicyEntry.policyId);
                if (e != null) {
                    String[] policy = (String[]) e.getValue();
                    if (policy != null) {
                        unknownPolicyEntry.policyName = policy[0];
                        unknownPolicyEntry.policyFullname = policy[1];
                    }
                } 
            }
            
            if(LOG.isDebugEnabled()){
                StringBuilder sb = new StringBuilder();
                sb.append("unresolved ").append(unknownPolicyIds.size()).append(" policies.");
                if(!unknownPolicyIds.isEmpty()){
                    sb.append(" They are ").append(CollectionUtils.asString(unknownPolicyIds, ","));
                }
                LOG.debug(sb.toString());
            }
        }
    }

    @Override
    protected void done() throws Exception {
        if (selectLogStatement != null) {
            selectLogStatement.close();
        }

        super.done();
    }

    @Override
    protected int getResultCheckIndex() {
        return ReportPolicyActivityLogWriter.INSERT_LOG_QUERY_INDEX;
    }

	@Override
	protected BatchWriter<ReportPolicyActivityLog> getWriter() {
		ReportPolicyActivityLog record = new ReportPolicyActivityLog(attrColumnMappingConfig);
		BatchWriter<ReportPolicyActivityLog> writer = new ReportPolicyActivityLogWriter(record.getInsertQueryString());
		writer.setLog(LogFactory.getLog(ReportPolicyActivityLogWriter.class));
		return writer;
	}

	@Override
	public void generateInsertQuery(int numberOfExtendedAttrColumns,
			String attrPrefix) {
		ReportPolicyActivityLog.generateInsertQuery(numberOfExtendedAttrColumns, attrPrefix);
	}
    
}
