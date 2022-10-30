/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ConditionElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IConditionElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.SimpleQueryImpl;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.destiny.container.shared.storedresults.ResultTableManagerException;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.ResultTableManagerQueryByIdImpl;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.ResultTableManagerSummaryImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.destiny.container.shared.pf.PolicyEditorServiceImpl;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This is the base class for stateful report execution manager. It contains
 * basic functionnality that can be reused by all the implementation for a
 * stateful query execution manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportExecutionMgrStatefulImpl.java#1 $
 */

public abstract class BaseReportExecutionMgrStatefulImpl extends BaseReportExecutionMgr {

    /**
     * Results ordering
     */
    private static final String FETCH_ORDER_BY = "ORDER BY result.id ASC";

    /**
     * HQL to fetch policy details
     */
    private static final String FETCH_POLICY_DETAIL_FROM_RESULT_TABLE = "select new ReportPolicyActivityDetailResultDO (log.action, log.applicationName, log.fromResourceInfo.name, log.hostIPAddress, log.hostName, log.id, "
            + "log.policyDecision, policy.fullName, log.timestamp, log.toResourceInfo.name, log.userName, log.level) from " + POLICY_ACTIVITY_LOG_DO + " log, PolicyDO policy, StoredQueryByIdResultDO result where " + "log.policyId = policy.id AND "
            + "log.id = result.resultId AND result.query.id = :queryId " + FETCH_ORDER_BY;

    /**
     * HQL to fetch tracking details
     */
    private static final String FETCH_TRACKING_DETAIL_FROM_RESULT_TABLE = "select new ReportTrackingActivityDetailResultDO (log.action, log.applicationName, log.fromResourceInfo.name, log.hostIPAddress, log.hostName, log.id, log.timestamp, log.toResourceInfo.name, log.userName, log.level) from "
            + TRACKING_ACTIVITY_LOG_DO + " log, StoredQueryByIdResultDO result where log.id = result.resultId AND result.query.id = :queryId " + FETCH_ORDER_BY;

    /**
     * HQL to fetch summary results
     */
    protected static final String FETCH_SUMMARY_FROM_RESULT_TABLE = "select new ReportSummaryResultDO (result.id, result.value, result.count) from StoredQuerySummaryResultDO result " + "where result.query.id = :queryId " + FETCH_ORDER_BY;

    private IResultTableManager detailResultMgr;
    private IResultTableManager summaryResultMgr;

    /**
     * This function builds the query to fetch results from the result table.
     * The fetch query can changed based on the report summary type (detail or
     * summary) or based on the target object data object used (policy activity
     * or tracking activity).
     * 
     * @param report
     *            report to execute
     * @return an HQL query string to retrieve results from the result table.
     */
    protected String buildFetchQuery(IReport report) {
        String result = null;
        if (ReportSummaryType.NONE.equals(report.getSummaryType())) {
            //Selects the relevant data object to query from
            if (InquiryTargetDataType.POLICY.equals(report.getInquiry().getTargetData())) {
                result = getFetchPolicyDetailsHQL();
            } else if (InquiryTargetDataType.ACTIVITY.equals(report.getInquiry().getTargetData())) {
                result = getFetchTrackingsDetailsHQL();
            }
        } else {
            result = getFetchSummaryHQL();
        }
        return result;
    }

    /**
     * Execute the HQL query and stores the HQL query results in the result
     * table.
     * 
     * @param report
     *            report to execute
     * @param query
     *            query to execute
     * @param maxStoreResults
     *            maximum numbers of results to store in the result table
     * @return the id of the query in the query table.
     * @throws HibernateException
     *             if query creation fails.
     */
    protected Long executeAndStoreQueryResults(IQuery query, IReport report, int maxStoreResults, int totalNbOfResults) throws HibernateException {

        //Store the result in the result table
        Long result = null;
        try {
            final Class targetDOClass = Class.forName(getTargetDOClassName(report));
            final IResultTableManager resultMgr = getResultTableMgr(report);
            result = resultMgr.storeResults(query, targetDOClass, maxStoreResults, totalNbOfResults);
        } catch (ResultTableManagerException e) {
            throw new HibernateException(e);
        } catch (ClassNotFoundException e) {
            getLog().error("Class not found for target data object class", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        //Creates the result table manager
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResultTableManager.DATA_SOURCE_CONFIG_PARAM, getDataSource());
        config.setProperty(IResultTableManager.RESULT_TABLE_NAME_CONFIG_PARAM, "STORED_QUERY_BY_ID_RESULTS");
        ComponentInfo compInfo = new ComponentInfo("queryByIdResultMgr", ResultTableManagerQueryByIdImpl.class.getName(), IResultTableManager.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        this.detailResultMgr = (IResultTableManager) getManager().getComponent(compInfo);

        //Creates the summary result table manager
        config = new HashMapConfiguration();
        config.setProperty(IResultTableManager.DATA_SOURCE_CONFIG_PARAM, getDataSource());
        config.setProperty(IResultTableManager.RESULT_TABLE_NAME_CONFIG_PARAM, "STORED_QUERY_SUMMARY_RESULTS");
        compInfo = new ComponentInfo("summaryResultMgr", ResultTableManagerSummaryImpl.class.getName(), IResultTableManager.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        this.summaryResultMgr = (IResultTableManager) getManager().getComponent(compInfo);
    }

    /**
     * Returns the HQL fragment to order the results
     * 
     * @return the HQL fragment to order the results
     */
    protected String getFetchOrderByHQL() {
        return FETCH_ORDER_BY;
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
     * Returns the HQL expression to fetch summary results
     * 
     * @return the HQL expression to fetch summary results
     */
    protected String getFetchSummaryHQL() {
        return FETCH_SUMMARY_FROM_RESULT_TABLE;
    }

    /**
     * Returns the detail result table manager.
     * 
     * @return the detail result table manager
     */
    protected IResultTableManager getDetailResultTableManager() {
        return this.detailResultMgr;
    }

    /**
     * REturns the appropriate result table manager that should handle the
     * execution and result storage of the report. If the report leads to a
     * detail result, then the "query by id" result table manager is used. If
     * the report contains a summary of some kind, then the "summary" result
     * table manager is used.
     * 
     * @param report
     *            report to execute.
     * @return the result table manager instance suitable for the report.
     */
    protected IResultTableManager getResultTableMgr(final IReport report) {
        if (report == null) {
            throw new NullPointerException("Report parameter cannot be null");
        }
        IResultTableManager result = null;
        if (ReportSummaryType.NONE.equals(report.getSummaryType())) {
            result = getDetailResultTableManager();
        } else {
            result = getSummaryResultTableMgr();
        }
        return result;
    }

    /**
     * Returns the summary result table manager
     * 
     * @return the summary result table manager
     */
    protected IResultTableManager getSummaryResultTableMgr() {
        return this.summaryResultMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr#executeReport(com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    public IReportResultReader executeReport(final IReport report) throws InvalidReportArgumentException, DataSourceException {
        return executeReport(report, -1);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr#executeReport(com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    public IReportResultReader executeReport(final IReport report, final int maxFetchRows) throws InvalidReportArgumentException, DataSourceException {
        IReportResultReader result = null;
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
            Long queryId = executeAndStoreQueryResults(mainQuery, report, maxFetchRows, totalCount.intValue());
            //Extract results from the result table
            result = fetchQueryResults(queryId, report);

        } catch (HibernateException e) {
            getLog().error("A hibernate error was returned when executing report", e);
            throw new DataSourceException(e);
        }
        return result;
    }

    /**
     * This function fetches the results from the results table.
     * 
     * @param queryId
     *            id of the query
     * @param report
     *            report to be executed
     * @return a reader object that fetched the objects from the results table.
     * @throws HibernateException
     */
    protected abstract IReportResultReader fetchQueryResults(Long queryId, IReport report) throws HibernateException;

    /**
     * Returns the query element associated with the resource class (if any)
     * 
     * @param report
     *            report to execute
     * @param asOf
     *            date to use in order to get the resource class definition. By
     *            default, this should be "now"
     * @return the query element associated with the resource class
     */
    protected IQueryElement getResourceClassElement(final IReport report, Date asOf) {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        IQueryElement result = null;
        Set resourceList = report.getInquiry().getResources();
        Iterator it = resourceList.iterator();
        Set groupNames = new HashSet();
        IEntityResolver resolver = getResourceAndGroupResolver();
        
        // TODO: This is a temp hack for Bug 4181, will need to 
        // address this when we bring back the resource group 
        // based queries in the future        
//        while (it.hasNext()) {
//            IInquiryResource entry = (IInquiryResource) it.next();
//            String name = entry.getName();
//            EntityExpressionType type = resolver.resolve(name);
//            if (EntityExpressionType.ENTITY_GROUP.equals(type) || EntityExpressionType.ENTITY_AND_ENTITY_GROUP.equals(type)) {
//                String groupName = resolver.extractValue(type, name);
//                if (groupName != null) {
//                    groupNames.add(groupName);
//                }
//            }
//        }

        final int groupNamesSize = groupNames.size();
        if (groupNamesSize > 0) {
            try {
                IPolicyEditorService policyEditorService = (IPolicyEditorService) getManager().getComponent(PolicyEditorServiceImpl.COMP_INFO);
                Collection resourceClassDescs = policyEditorService.getDescriptorsForNamesAndType(groupNames, EntityType.COMPONENT);
                String conditionExpr = "";
                //some resources class names queried for are not deployed, or
                // they
                // do not exist. Add a dummy condition to make sure that this
                // non-existant groups are part of the query.
                if (groupNamesSize != resourceClassDescs.size()) {
                    conditionExpr = HQLConstants.OPEN_PARENTHESE + "1=0" + HQLConstants.CLOSE_PARENTHESE;
                }

                it = resourceClassDescs.iterator();
                List IDList = new ArrayList();
                while (it.hasNext()) {
                    DomainObjectDescriptor descriptor = (DomainObjectDescriptor) it.next();
                    IDList.add(descriptor.getId());
                }

                Collection resourceClassAsOfDescs = policyEditorService.getDeployedObjectDescriptors(IDList, asOf);
                it = resourceClassAsOfDescs.iterator();
                List pqlList = new ArrayList();
                while (it.hasNext()) {
                    DomainObjectDescriptor desc = (DomainObjectDescriptor) it.next();
                    pqlList.add(policyEditorService.getFullyResolvedEntity(desc));
                }

                Map allArgs = new HashMap();
                it = pqlList.iterator();
                boolean first = conditionExpr.length() == 0 ? true : false;
                List selectList = new ArrayList();
                if (it.hasNext()) {
                    //Create the visitor object
                    ResourceClassVisitor visitor = new ResourceClassVisitor(allArgs);
                    while (it.hasNext()) {
                        final String currentPQL = (String) it.next();
                        visitor.setVariableName(RESULT_VAR_NAME + ".fromResourceInfo");
                        visitor.addNewEntityToVisit(first);
                        first = false;
                        DomainObjectBuilder.processInternalPQL(currentPQL, visitor);
                    }
                    conditionExpr += visitor.getHQLExpression();
                    if (visitor.getSelects().size() > 0) {
                        selectList.addAll(visitor.getSelects());
                    }
                }

                if (conditionExpr.length() > 0) {
                    conditionExpr = HQLConstants.OPEN_PARENTHESE + conditionExpr + HQLConstants.CLOSE_PARENTHESE;
                    result = new QueryElementImpl();
                    IConditionElement condition = new ConditionElementImpl(conditionExpr);
                    result.getConditions().add(condition);
                    result.getNamedParameters().putAll(allArgs);
                    if (selectList.size() > 0) {
                        result.getSelects().addAll(selectList);
                    }
                }
            } catch (PQLException e) {
                getLog().error("Error when collecting resource class names", e);
                result = new QueryElementImpl();
                IConditionElement condition = new ConditionElementImpl("1=0");
                result.getConditions().add(condition);
            } catch (PolicyServiceException e) {
                getLog().error("Error when collecting resource class names", e);
                result = new QueryElementImpl();
                IConditionElement condition = new ConditionElementImpl("1=0");
                result.getConditions().add(condition);
            } catch (CircularReferenceException e) {
                getLog().error("Error when collecting resource class names", e);
                result = new QueryElementImpl();
                IConditionElement condition = new ConditionElementImpl("1=0");
                result.getConditions().add(condition);
            }
        }
        return result;
    }
}
