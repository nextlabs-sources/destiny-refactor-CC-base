/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.SimpleQueryImpl;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredQueryByIdResultDO;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredQuerySummaryResultDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.InvalidActivityLogIdException;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.BaseActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogDetailResult;

/**
 * This is the stateless implementation of the report execution manager. In this
 * implementation, the HQL queries to fetch the results are a little bit
 * different, since the result id is needed to fetch the results based on the
 * previous query state.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrStatelessImpl.java#1 $
 */

public class ReportExecutionMgrStatelessImpl extends BaseReportExecutionMgrStatefulImpl implements IStatelessReportExecutionMgr {

    /**
     * Names related to the data objects
     */
    protected static final String CUSTOM_TRACKING_ACTIVITY_ATTRIBUTE_DO = "TrackingActivityLogCustomAttributeDO";
    protected static final String CUSTOM_TRACKING_ACTIVITY_ATTRIBUTE_DO_CLASSNAME = TrackingActivityLogCustomAttributeDO.class.getName();
    protected static final String CUSTOM_POLICY_ACTIVITY_ATTRIBUTE_DO = "PolicyActivityLogCustomAttributeDO";
    protected static final String CUSTOM_POLICY_ACTIVITY_ATTRIBUTE_DO_CLASSNAME = PolicyActivityLogCustomAttributeDO.class.getName();
    protected static final String POLICY_DO = "PolicyDO";
    protected static final String POLICY_DO_CLASSNAME = PolicyDO.class.getName();
    
    /**
     * HQL to fetch policy detail from the result table
     */
    private static final String FETCH_POLICY_DETAIL_FROM_RESULT_TABLE = "select new ReportPolicyActivityDetailResultDO (log.action, log.applicationName, "
            + "log.fromResourceInfo.name, log.hostIPAddress, log.hostName, log.id, log.policyDecision, policy.fullName, result.id, log.timestamp, log.toResourceInfo.name, log.userName, log.level) from " + POLICY_ACTIVITY_LOG_DO
            + " log, PolicyDO policy, StoredQueryByIdResultDO result where log.policyId = policy.id AND log.id = result.resultId and result.query.id = :queryId";

    /**
     * HQL to fetch tracking details from the result table.
     */
    private static final String FETCH_TRACKING_DETAIL_FROM_RESULT_TABLE = "select new ReportTrackingActivityDetailResultDO (log.action, log.applicationName, log.fromResourceInfo.name, log.hostIPAddress, log.hostName, log.id, result.id, log.timestamp, log.toResourceInfo.name, "
            + "log.userName, log.level) from " + TRACKING_ACTIVITY_LOG_DO + " log, StoredQueryByIdResultDO result where log.id = result.resultId AND result.query.id = :queryId";

    /**
     * HQL to fetch custom detail for a single policy record
     */
    private static final String FETCH_POLICY_DETAIL_BY_ID = "select p from " + POLICY_ACTIVITY_LOG_DO + " p where p.id = :recordId";
    
    /**
     * HQL to fetch custom detail for a single tracking record
     */
    private static final String FETCH_TRACKING_DETAIL_BY_ID = "select p from " + TRACKING_ACTIVITY_LOG_DO + " p where p.id = :recordId";
    
    /**
     * HQL to fetch custom attributes for a single policy record
     */
    private static final String FETCH_POLICY_LOG_CUSTOM_ATTRIBUTES = "select p from " + CUSTOM_POLICY_ACTIVITY_ATTRIBUTE_DO_CLASSNAME + " p where p.record = :activityLog";
    
    /**
     * HQL to fetch custom attributes for a single tracking record
     */
    private static final String FETCH_TRACKING_LOG_CUSTOM_ATTRIBUTES = "select p from " + CUSTOM_TRACKING_ACTIVITY_ATTRIBUTE_DO_CLASSNAME + " p where p.record = :activityLog";
    
    /**
     * HQL to fetch summary results from the result table.
     */
    private static final String FETCH_SUMMARY_FROM_RESULT_TABLE = "select new ReportSummaryResultDO (result.id, result.value, result.count) from StoredQuerySummaryResultDO result where result.query.id = :queryId";
    
    /**
     * HQL to fetch the policy name by policy id
     */
    private static final String FETCH_POLICY_NAME_BY_POLICY_ID = "select p.fullName from " + POLICY_DO + " p where p.id = :policyId" ;
    
    private IReportMgr reportMgr;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr#executeReport(int,
     *      com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    public IReportResultReader executeReport(IReport report, int maxFetchRows) throws InvalidReportArgumentException, DataSourceException {
        return executeReport(report, maxFetchRows, -1);
    }

    /**
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr#executeReport(com.bluejungle.destiny.container.shared.inquirymgr.IReport,
     *      int)
     */
    public IStatelessReportExecutionResult executeReport(final IReport report, final int fetchRows, final int maxStoreRows) throws InvalidReportArgumentException, DataSourceException {
        IStatelessReportExecutionResult result = null;

        try {
            final SimpleQueryImpl partialQuery = new SimpleQueryImpl();
            buildQueryElements(report, partialQuery);

            //Performs the count only query first
            Integer totalCount = countResults(report, partialQuery);

            //Performs the main query and fetches only the maximum number rows
            // specified
            QueryImpl mainQuery = new QueryImpl(partialQuery);
            mainQuery.addQueryElement(getMainQueryElement(report));

            //Executes the HQL query against the database
            Long queryId = executeAndStoreQueryResults(mainQuery, report, maxStoreRows, totalCount.intValue());
            //Extract results from the result table
            //First time this report is executed, so create a fresh state.
            ReportResultStateImpl newState = new ReportResultStateImpl();
            newState.setQueryId(queryId);

            //Extract results from the result table
            result = fetchQueryResults(newState, fetchRows);
        } catch (HibernateException e) {
            getLog().error("An error occured when executing report", e);
            throw new DataSourceException(e);
        }
        return result;
    }

    /**
     * Returns the HQL expression to fetch policy details
     * 
     * @return the HQL expression to fetch policy details
     */
    protected String getFetchPolicyDetailsHQL() {
        return FETCH_POLICY_DETAIL_FROM_RESULT_TABLE;
    }

    /**
     * Returns the HQL expression to fetch tracking details
     * 
     * @return the HQL expression to fetch tracking details
     */
    protected String getFetchTrackingsDetailsHQL() {
        return FETCH_TRACKING_DETAIL_FROM_RESULT_TABLE;
    }
    
    /**
     * 
     * @return the HQL to fetch the detail for a single policy record.
     */
    public static String getFetchPolicyDetailHQL() {
        return FETCH_POLICY_DETAIL_BY_ID;
    }
  
    /**
     *
     * @return the HQL to fetch the detail for a single tracking record.
     */
    public static String getFetchTrackingDetailHQL() {
        return FETCH_TRACKING_DETAIL_BY_ID;
    }
    
    /**
     * 
     * @return the HQL to fetch the detail for a single policy record.
     */
    public static String getFetchPolicyCustomAttributeHQL() {
        return FETCH_POLICY_LOG_CUSTOM_ATTRIBUTES;
    }
         
    /**
     *
     * @return the HQL to fetch the detail for a single tracking record.
     */
    public static String getFetchTrackingCustomAttributeHQL() {
        return FETCH_TRACKING_LOG_CUSTOM_ATTRIBUTES;
    }
    
    /**
     * Returns the HQL expression to fetch summary results
     * 
     * @return the HQL expression to fetch summary results
     */
    protected String getFetchSummaryHQL() {
        return FETCH_SUMMARY_FROM_RESULT_TABLE;
    }
    
    /**
     * Returns the HQL expression to fetch the policy name
     * 
     * @return the HQL expression to fetch the policy name
     */
    protected String getFetchPolicyNameHQL() {
        return FETCH_POLICY_NAME_BY_POLICY_ID;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr#gotoNextSet(com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState,
     *      int)
     */
    public IStatelessReportExecutionResult gotoNextSet(IReportResultState state, int maxRows) {
        IStatelessReportExecutionResult result = null;
        try {
            result = fetchQueryResults(state, maxRows);
        } catch (HibernateException e) {
            getLog().error("Error when fetching next set for query", e);
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr#getLogDetail(long)
     */
    public ILogDetailResult getLogDetail(IReport report, long recordId) throws DataSourceException, InvalidActivityLogIdException {
        ILogDetailResult returnResult = null;
        LinkedHashMap<String, String> customAttributes = null;
        String policyName = null;
        BaseActivityLogDO activityLog = null;
        Session s = null;
        
        try {
            s = this.getDataSource().getSession();
            
            // First get the activity log itself
            String hqlExpression = buildSingleLogFetchQuery(report);
            Query q = s.createQuery(hqlExpression);
            q.setLong("recordId", recordId);            
            List result = q.list();

            if (result.size() > 0){
                activityLog = (BaseActivityLogDO)result.get(0);
                
                // Then Get the custom attributes
                hqlExpression = buildCustomAttributesFetchQuery(report);
                q = s.createQuery(hqlExpression);
                q.setEntity("activityLog", activityLog);  
                result = q.list();
               
                // HACK: we need to load the file system resource attributes
                //       into the custom attributes map because they are still
                //       stored inside the actual log tables
                IFromResourceInformation fromResource = activityLog.getFromResourceInfo();
                String fromResourceName = fromResource.getName();
                long fromResourceSize;
                if (fromResource.getSize() != null){
                    fromResourceSize = fromResource.getSize();
                } else {
                    fromResourceSize = -1;
                }
                String fromResourceOwner = fromResource.getOwnerId();
                Calendar fromResourceCreatedDate = fromResource.getCreatedDate();
                Calendar fromResourceModifiedDate = fromResource.getModifiedDate();
                customAttributes = new LinkedHashMap<String, String>();
                if (fromResourceName != null){
                    customAttributes.put("Name", fromResourceName);   
                }
                if (fromResourceSize >= 0){
                    String sizeString = null;
                    if (fromResourceSize > 1024){
                        long kbSize = fromResourceSize / 1024;
                        if (kbSize > 1000){
                            long mbSize = kbSize / 1000;
                            sizeString = mbSize + " MB";
                        } else {
                            sizeString = kbSize + " KB";
                        }
                    } else {
                        sizeString = fromResourceSize + " Bytes";
                    }
                    customAttributes.put("Size", sizeString);
                }
                if (fromResourceCreatedDate != null){
                    if (fromResourceCreatedDate.getTimeInMillis() > 0){
                        customAttributes.put("Created Date", DateFormat.getDateTimeInstance().format(fromResourceCreatedDate.getTime()));
                    }
                }
                if (fromResourceModifiedDate != null){
                    if (fromResourceModifiedDate.getTimeInMillis() > 0){
                        customAttributes.put("Modified Date", DateFormat.getDateTimeInstance().format(fromResourceModifiedDate.getTime()));
                    }
                }
                if (fromResourceOwner != null){
                    customAttributes.put("Owner ID", fromResourceOwner);
                }
                if (result.size() > 0){
                    for (int i = 0; i < result.size(); i++){
                        BaseActivityLogCustomAttributeDO attribute = (BaseActivityLogCustomAttributeDO)result.get(i);
                        customAttributes.put(attribute.getKey(), attribute.getValue());
                    }
                }
                if (InquiryTargetDataType.POLICY.equals(report.getInquiry().getTargetData())) {
                    // Get the policy name before returning
                    hqlExpression = getFetchPolicyNameHQL();
                    q = s.createQuery(hqlExpression);
                    q.setLong("policyId", ((PolicyActivityLogDO)activityLog).getPolicyId());  
                    result = q.list();   
                    policyName = (String)result.get(0);
                    returnResult = new PolicyActivityLogDetailResult(customAttributes, activityLog, policyName);
                } else if (InquiryTargetDataType.ACTIVITY.equals(report.getInquiry().getTargetData())) {
                    returnResult = new TrackingActivityLogDetailResult(customAttributes, activityLog);
                }
            } else { // Could not locate the activity log, then return null
                InvalidActivityLogIdException exception = new InvalidActivityLogIdException();
                throw exception;
            }
        } catch (HibernateException e){
            getLog().error("An error occurred when getting log detail", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return returnResult;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        ComponentInfo<IReportMgr> compInfo = new ComponentInfo<IReportMgr>(
                "reportMgr", 
                ReportMgrImpl.class, 
                IReportMgr.class, 
                LifestyleType.TRANSIENT_TYPE);
        this.reportMgr = getManager().getComponent(compInfo);
    }

    /**
     * This function fetches results from the result table and returns a state
     * associated with the results that have been fetched. This allows the
     * caller to disconnect from the execution manager and come back later to
     * fetch additional records.
     * 
     * @param state
     *            current state of the report execution
     * @param maxRows
     *            maximum number of rows to fetch
     * @param goNext
     *            true if the query has to fetch records after the current one,
     *            or false if it is before
     * @return a new result object encapsulating a new state and the fetched
     *         rows (if any)
     * @throws HibernateException
     */
    protected IStatelessReportExecutionResult fetchQueryResults(IReportResultState state, int maxRows) throws HibernateException {
        if (state == null) {
            throw new NullPointerException("State cannot be null");
        }

        //Get one of the table manager instance to retrieve the stored query.
        // Here, it does not matter which one is used since we simply fetch the
        // query definition.
        IResultTableManager resultMgr = getDetailResultTableManager();
        IStoredQuery storedQuery = resultMgr.getStoredQuery(state.getQueryId());

        IReport report = rebuildReportFromStoredQuery(storedQuery);
        String hqlExpression = buildFetchQuery(report);
        if (state.getLastRowSequenceId() != null) {
            hqlExpression += " AND result.id> :endRowId";
        }
        hqlExpression += HQLConstants.SPACE + getFetchOrderByHQL();
        Session s = this.getDataSource().getSession();
        Query q = s.createQuery(hqlExpression);
        if (maxRows > -1) {
            q.setMaxResults(maxRows);
        }
        q.setParameter("queryId", storedQuery.getId());
        if (state.getLastRowSequenceId() != null) {
            q.setParameter("endRowId", state.getLastRowSequenceId());
        }

        //Creates a reader object for the results
        IStatelessReportExecutionResult reader = new ReportResultReaderStatelessImpl(s, q, this.getDataSource(), storedQuery, state);
        return (new StatelessReportExecutionResultImpl(reader));
    }

    /**
     * Returns a report object constructed based on the storedQuery provided.
     * The report object is only partial and does not contain all the original
     * query specifications. However, it should contain enough to recreate the
     * HQL query to fetch data from the result table. Therefore, the report
     * needs to contain a least the summary type and the target data of the
     * query.
     * 
     * @param storedQuery
     *            stored query object
     * @return a report object
     */
    protected IReport rebuildReportFromStoredQuery(IStoredQuery storedQuery) {
        IReport report = this.reportMgr.createReport();
        String doName = storedQuery.getDataObjectName();
        String resultDOName = storedQuery.getResultObjectName();
        //I don't like this code. Maybe the storedquery object should store
        // more things and be more explicit. Reverse engineering the class names
        // like this is not good, and if classes change, this code has to be
        // changed also.
        if (PolicyActivityLogDO.class.getName().equals(doName)) {
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        } else if (TrackingActivityLogDO.class.getName().equals(doName)) {
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        }

        if (StoredQueryByIdResultDO.class.getName().equals(resultDOName)) {
            report.setSummaryType(ReportSummaryType.NONE);
        } else if (StoredQuerySummaryResultDO.class.getName().equals(resultDOName)) {
            //Not good either, here we just need to know that there is a
            // summary
            report.setSummaryType(ReportSummaryType.USER);
        }
        return report;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseReportExecutionMgr#fetchQueryResults(java.lang.Long,
     *      com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    protected IReportResultReader fetchQueryResults(Long queryId, IReport report) throws HibernateException {
        return null;
    }
    
    /**
     * This function builds the query to fetch a single activity log record.
     * The fetch query can changed based on the target object data object used 
     * (policy activity or tracking activity).
     * 
     * @param report
     *            report to execute
     * @return an HQL query string to retrieve results from the result table.
     */
    protected String buildSingleLogFetchQuery(IReport report) {
        String result = null;
        //Selects the relevant data object to query from
        if (InquiryTargetDataType.POLICY.equals(report.getInquiry().getTargetData())) {
            result = getFetchPolicyDetailHQL();
        } else if (InquiryTargetDataType.ACTIVITY.equals(report.getInquiry().getTargetData())) {
            result = getFetchTrackingDetailHQL();
        }
        return result;
    }
    
    /**
     * This function builds the query to fetch custom attributes for a single 
     * activity log record.  The fetch query can changed based on the target 
     * object data object used (policy activity or tracking activity).
     * 
     * @param report
     *            report to execute
     * @return an HQL query string to retrieve results from the result table.
     */
    protected String buildCustomAttributesFetchQuery(IReport report){
        String result = null;
        //Selects the relevant data object to query from
        if (InquiryTargetDataType.POLICY.equals(report.getInquiry().getTargetData())) {
            result = getFetchPolicyCustomAttributeHQL();
        } else if (InquiryTargetDataType.ACTIVITY.equals(report.getInquiry().getTargetData())) {
            result = getFetchTrackingCustomAttributeHQL();
        }
        return result;
    }
}
