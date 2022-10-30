/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryApplication;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IUserResolver;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.ResourceAndGroupResolverImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.UserAndGroupResolverImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ConditionElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.GroupingElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.OrderByElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.SelectElementImpl;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.bluejungle.domain.policydecision.hibernateimpl.PolicyDecisionUserType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This the base class for the report execution manager implementations. It is
 * used as a base class for all report execution manager implementations.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportExecutionMgr.java#1 $
 */

abstract class BaseReportExecutionMgr implements IConfigurable, IInitializable, ILogEnabled, IManagerEnabled {

    // query data objects
    /**
     * Name of the result variable
     */
    protected static final String RESULT_VAR_NAME = "result";

    /**
     * Names related to the application object
     */
    protected static final String APPLICATION_DO_NAME = "ApplicationDO";
    protected static final String APPLICATION_VAR_NAME = "app";

    /**
     * Names related to the host object
     */
    protected static final String HOST_DO_NAME = "HostDO";
    protected static final String HOST_VAR_NAME = "host";

    /**
     * Names related to the policy object
     */
    protected static final String POLICY_DO_NAME = "PolicyDO";
    protected static final String POLICY_VAR_NAME = "policy";

    /**
     * Names related to the user object
     */
    protected static final String USER_DO_NAME = "UserDO";
    protected static final String USER_VAR_NAME = "user";
    protected static final String USERGROUP_DO_NAME = "UserGroupDO";
    protected static final String USERGROUP_VAR_NAME = "userGroup";

    /**
     * Names related to the data objects
     */
    protected static final String TRACKING_ACTIVITY_LOG_DO = "TrackingActivityLogDO";
    protected static final String TRACKING_ACTIVITY_LOG_DO_CLASSNAME = TrackingActivityLogDO.class.getName();
    protected static final String POLICY_ACTIVITY_LOG_DO = "PolicyActivityLogDO";
    protected static final String POLICY_ACTIVITY_LOG_DO_CLASSNAME = PolicyActivityLogDO.class.getName();

    /**
     * Pre-built HQL expressions
     */

    protected static final String OR = HQLConstants.SPACE + HQLConstants.OR + HQLConstants.SPACE;

    /**
     * Wildchars
     */
    protected static final char HQL_WILDCHAR = '%';
    protected static final char WILDCHAR = '*';

    /**
     * Other constants
     */
    protected static final Integer MINUS_ONE = new Integer(-1);

    /**
     * Map used to lookup the target data object based on the report summary
     * type
     */
    protected static final Map targetDOMap = new HashMap();

    static {
        targetDOMap.put(InquiryTargetDataType.ACTIVITY, TRACKING_ACTIVITY_LOG_DO);
        targetDOMap.put(InquiryTargetDataType.POLICY, POLICY_ACTIVITY_LOG_DO);
    }

    /**
     * Map used to lookup the field to sort on based on the grouping type.
     */
    protected static final Map timeSortMap = new HashMap();
    static {
        timeSortMap.put(ReportSummaryType.NONE, "timestamp.time");
        timeSortMap.put(ReportSummaryType.TIME_DAYS, "timestamp.dayNb");
        timeSortMap.put(ReportSummaryType.TIME_MONTHS, "timestamp.monthNb");
    }

    /**
     * This set contains the summary types that are related to time.
     */
    protected static final Set timeSummaryTypes = new HashSet();
    static {
        timeSummaryTypes.add(ReportSummaryType.TIME_DAYS);
        timeSummaryTypes.add(ReportSummaryType.TIME_MONTHS);
    }

    private IConfiguration configuration;
    private IHibernateRepository dataSource;
    private IComponentManager manager;
    private Log log;
    private IEntityResolver userAndGroupsResolver;
    private IEntityResolver resourceAndGroupsResolver;

    /**
     * Constructor
     */
    public BaseReportExecutionMgr() {
        super();
    }

    /**
     * Counts the number of results returned by the query, without limitation.
     * The count occurs only on queries that have no grouping defined.
     * 
     * @param report
     *            report to execute
     * @param partialQuery
     *            partial query object
     * @return the result of the count, or -1 if the count did not happen.
     */
    protected Integer countResults(final IReport report, final IQuery partialQuery) throws InvalidReportArgumentException {
        Integer totalCount = MINUS_ONE;
        Session s = null;
        try {
            if (ReportSummaryType.NONE.equals(report.getSummaryType())) {
                final IQuery countOnlyQuery = partialQuery.copy();
                countOnlyQuery.addQueryElement(getMainCountQueryElement(report));
                s = getDataSource().getSession();
                Query q = countOnlyQuery.getHQLQuery(s);
                totalCount = (Integer) q.uniqueResult();
            } else {
                ReportSummaryType tempType = report.getSummaryType();
                report.setSummaryType(ReportSummaryType.NONE);
                final IQuery countOnlyQuery = partialQuery.copy();
                countOnlyQuery.addQueryElement(getMainCountQueryElement(report));
                s = getDataSource().getSession();
                Query q = countOnlyQuery.getHQLQuery(s);
                totalCount = (Integer) q.uniqueResult();
                report.setSummaryType(tempType);
            }
        } catch (HibernateException e) {
            getLog().error("An error occured while counting query results", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return totalCount;
    }

    /**
     * Builds the query elements required for the report. The query elements
     * returned do not include the "main" query element that dictate what to do
     * with these results.
     * 
     * @param report
     *            report to execute
     * @return a query object containing the query elements only.
     */
    protected final void buildQueryElements(final IReport report, final IQuery query) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report cannot be null");
        }
        if (query == null) {
            throw new NullPointerException("Query cannot be null");
        }
        final String targetDOName = getTargetDOType(report);
        final IQueryElement actionElement = getActionElement(report);
        final IQueryElement applicationElement = getApplicationElement(report);
        final IQueryElement hostElement = getHostElement(report);
        final IQueryElement obligationElement = getObligationElement(report);
        final IQueryElement policyElement = getPolicyElement(report);
        final IQueryElement policyDecisionElement = getPolicyDecisionElement(report);
        final IQueryElement resourceNameElement = getResourceNameElement(report);
        final IQueryElement resourceClassElement = getResourceClassElement(report, new Date());
        final IQueryElement timePeriodElement = getTimePeriodElement(report);
        final IQueryElement userElement = getUserElement(report);
        final IQueryElement loggingLevelElement = getLoggingLevelElement(report);
        query.addQueryElement(actionElement);
        query.addQueryElement(applicationElement);
        query.addQueryElement(hostElement);
        query.addQueryElement(obligationElement);
        query.addQueryElement(policyElement);
        query.addQueryElement(policyDecisionElement);
        query.addQueryElement(resourceNameElement);
        query.addQueryElement(resourceClassElement);
        query.addQueryElement(timePeriodElement);
        query.addQueryElement(userElement);
        query.addQueryElement(loggingLevelElement);
    }

    /**
     * Returns the configuration.
     * 
     * @return the configuration.
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the data source object for the execution manager
     * 
     * @return the data source object for the execution manager
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * Returns the log.
     * 
     * @return the log.
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Returns the condition associated with action (if any)
     * 
     * @param query
     *            current query being assembled
     * @param report
     *            report to execute
     * @return the condition condition, or null if there are no action condition
     *         in the report.
     */
    protected IQueryElement getActionElement(final IReport report) throws InvalidReportArgumentException {

        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        QueryElementImpl result = null;
        Set actions = report.getInquiry().getActions();
        final String actionFieldName = RESULT_VAR_NAME + ".action";
        boolean hasActionNameCondition = actions.size() > 0;

        ISortSpec sortSpec = report.getSortSpec();
        if (sortSpec != null) {
            if (SortFieldType.ACTION.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                boolean ascending = true;
                if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = false;
                }
                result = new QueryElementImpl();
                result.getOrderBys().add(new OrderByElementImpl(ascending, actionFieldName));
            }
        }

        if (hasActionNameCondition) {
            if (result == null) {
                result = new QueryElementImpl();
            }
            String hqlCondition = actionFieldName + HQLConstants.SPACE + HQLConstants.IN + HQLConstants.SPACE + HQLConstants.OPEN_PARENTHESE;

            // Adds each action name in the list
            Iterator it = actions.iterator();
            int argCount = 0;
            while (it.hasNext()) {
                IInquiryAction currentAction = (IInquiryAction) it.next();
                String actionToQuery = UserTypeConverter.convertUserType(currentAction.getActionType(), new ActionEnumUserType());
                String argName = "action" + argCount++;
                result.getNamedParameters().put(argName, actionToQuery);
                hqlCondition += HQLConstants.COLON + argName;
                if (it.hasNext()) {
                    hqlCondition += HQLConstants.COMMA;
                }
            }
            hqlCondition += HQLConstants.CLOSE_PARENTHESE;
            ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
            result.getConditions().add(condition);
        }
        return result;
    }

    /**
     * Returns the application element associated with the query (if any)
     * 
     * @param report
     *            report to execute
     * @return the query element associated with applications, or null if there
     *         are no application specifications in the report.
     */
    protected IQueryElement getApplicationElement(final IReport report) throws InvalidReportArgumentException {

        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        QueryElementImpl result = null;
        Set applications = report.getInquiry().getApplications();
        if (applications.size() > 0) {
            result = new QueryElementImpl();
            SelectElementImpl select = new SelectElementImpl();
            select.setDOVarName(APPLICATION_VAR_NAME);
            select.setDOClassName(APPLICATION_DO_NAME);
            String hqlCondition = RESULT_VAR_NAME + ".applicationId = " + APPLICATION_VAR_NAME + ".originalId";
            hqlCondition += HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE + APPLICATION_VAR_NAME + ".name" + HQLConstants.SPACE + HQLConstants.IN + HQLConstants.SPACE + HQLConstants.OPEN_PARENTHESE;
            // Adds each policy name in the list
            Iterator it = applications.iterator();
            int argCount = 0;
            while (it.hasNext()) {
                IInquiryApplication currentApp = (IInquiryApplication) it.next();
                String argName = "application" + argCount++;
                result.getNamedParameters().put(argName, currentApp.getName());
                hqlCondition += HQLConstants.COLON + argName;
                if (it.hasNext()) {
                    hqlCondition += HQLConstants.COMMA;
                }
            }
            hqlCondition += HQLConstants.CLOSE_PARENTHESE;
            ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
            result.getConditions().add(condition);
            result.getSelects().add(select);
        }

        // Checks whether there is a sorting specified on the application name
        final ISortSpec sortSpec = report.getSortSpec();
        if (sortSpec != null) {
            if (SortFieldType.APPLICATION.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                boolean ascending = true;
                if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = false;
                }
                if (result == null) {
                    result = new QueryElementImpl();
                }
                result.getOrderBys().add(new OrderByElementImpl(ascending, RESULT_VAR_NAME + ".applicationName"));
            }
        }
        return result;
    }

    /**
     * Returns the element if there is no grouping in the report
     * 
     * @param report
     *            report to execute
     * @return the main query element if there is no grouping, or null if there
     *         is grouping on this report.
     */
    protected IQueryElement getGroupByNoneElement(final IReport report) throws InvalidReportArgumentException {
        QueryElementImpl result = null;
        if (ReportSummaryType.NONE.equals(report.getSummaryType())) {
            result = new QueryElementImpl();
            SelectElementImpl select = new SelectElementImpl();
            select.setDOClassName(getTargetDOType(report));
            select.setDOVarName(RESULT_VAR_NAME);
            select.setFieldName("id");
            result.getSelects().add(select);
        }
        return result;
    }

    /**
     * Returns the query element for the group by policy name (if applicabale
     * 
     * @param report
     *            report to execute
     * @return the query element for group by policy, if the report has a
     *         summary by policy. If not, the returned value is null.
     */
    protected QueryElementImpl getGroupByPolicyElement(final IReport report) throws InvalidReportArgumentException {
        QueryElementImpl result = null;
        if (ReportSummaryType.POLICY.equals(report.getSummaryType())) {
            result = new QueryElementImpl();
            SelectElementImpl countSelect = new SelectElementImpl();
            SelectElementImpl policyName = new SelectElementImpl();
            SelectElementImpl resultSelect = new SelectElementImpl();

            resultSelect.setDOClassName(getTargetDOType(report));
            resultSelect.setDOVarName(RESULT_VAR_NAME);
            result.getSelects().add(resultSelect);

            // Select in order! First the policy name, then the count
            policyName.setDOClassName(POLICY_DO_NAME);
            policyName.setDOVarName(POLICY_VAR_NAME);
            policyName.setFieldName("fullName");
            result.getSelects().add(policyName);

            countSelect.setDOClassName(POLICY_DO_NAME);
            countSelect.setDOVarName(POLICY_VAR_NAME);
            countSelect.setFieldName("fullName");
            countSelect.setFunction("count");
            result.getSelects().add(countSelect);

            String policyCondition = RESULT_VAR_NAME + ".policyId" + HQLConstants.EQUAL + POLICY_VAR_NAME + ".id";
            ConditionElementImpl condition = new ConditionElementImpl(policyCondition);
            result.getConditions().add(condition);

            GroupingElementImpl groupByPolicyElement = new GroupingElementImpl(POLICY_VAR_NAME + ".fullName");
            result.getGroupings().add(groupByPolicyElement);

            // Check if we have a grouping on the policy count
            ISortSpec sortSpec = report.getSortSpec();
            if (sortSpec != null && SortFieldType.COUNT.equals(sortSpec.getSortField())) {
                boolean ascending = false;
                if (SortDirectionType.ASCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = true;
                }
                OrderByElementImpl orderByPolicyCount = new OrderByElementImpl(ascending, "count(" + POLICY_VAR_NAME + ".fullName)");
                result.getOrderBys().add(orderByPolicyCount);
            }
        }
        return result;
    }

    /**
     * Returns the query element for the group by resource (if applicable).
     * 
     * @param report
     *            report to execute
     * @return the query element for group by user, if the report has a summary
     *         by resource. If not, the returned value is null.
     */
    protected IQueryElement getGroupByResourceElement(final IReport report) throws InvalidReportArgumentException {
        QueryElementImpl result = null;
        if (ReportSummaryType.RESOURCE.equals(report.getSummaryType())) {
            result = new QueryElementImpl();
            SelectElementImpl countSelect = new SelectElementImpl();
            SelectElementImpl resultSelect = new SelectElementImpl();

            resultSelect.setDOClassName(getTargetDOType(report));
            resultSelect.setDOVarName(RESULT_VAR_NAME);
            resultSelect.setFieldName("fromResourceInfo.name");
            result.getSelects().add(resultSelect);

            countSelect.setDOClassName(getTargetDOType(report));
            countSelect.setDOVarName(RESULT_VAR_NAME);
            countSelect.setFieldName("fromResourceInfo.name");
            countSelect.setFunction("count");
            result.getSelects().add(countSelect);

            GroupingElementImpl groupByResourceElement = new GroupingElementImpl(RESULT_VAR_NAME + ".fromResourceInfo.name");
            result.getGroupings().add(groupByResourceElement);

            // Check if we have a sorting on the resource count on top of
            // grouping
            ISortSpec sortSpec = report.getSortSpec();
            if (sortSpec != null && SortFieldType.COUNT.equals(sortSpec.getSortField())) {
                boolean ascending = false;
                if (SortDirectionType.ASCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = true;
                }
                OrderByElementImpl orderByResourceCount = new OrderByElementImpl(ascending, "count(" + RESULT_VAR_NAME + ".fromResourceInfo.name)");
                result.getOrderBys().add(orderByResourceCount);

                // If sorted on count, sort as well on resource name (ascending)
                OrderByElementImpl orderByResourceName = new OrderByElementImpl(true, RESULT_VAR_NAME + ".fromResourceInfo.name");
                result.getOrderBys().add(orderByResourceName);
            }
        }
        return result;
    }

    /**
     * This function returns the query element for the group by time (it
     * applicable). This function deals with all types of time grouping.
     * 
     * @param report
     *            report to execute
     * @return the query element associated with time grouping (if any time
     *         grouping is specified in the report), null otherwise.
     */
    protected QueryElementImpl getGroupByTimeElement(final IReport report) throws InvalidReportArgumentException {
        QueryElementImpl result = null;
        ReportSummaryType reportSummaryType = report.getSummaryType();
        if (timeSummaryTypes.contains(reportSummaryType)) {
            result = new QueryElementImpl();
            SelectElementImpl timeUnitSelect = new SelectElementImpl();

            // Select the time unit, then the count
            timeUnitSelect.setDOClassName(getTargetDOType(report));
            timeUnitSelect.setDOVarName(RESULT_VAR_NAME);
            final String fieldName = (String) timeSortMap.get(reportSummaryType);
            timeUnitSelect.setFieldName(fieldName);
            result.getSelects().add(timeUnitSelect);

            SelectElementImpl countSelect = new SelectElementImpl();
            countSelect.setDOClassName(getTargetDOType(report));
            countSelect.setDOVarName(RESULT_VAR_NAME);
            countSelect.setFieldName(fieldName);
            countSelect.setFunction("count");
            result.getSelects().add(countSelect);

            GroupingElementImpl groupByTimeUnit = new GroupingElementImpl(RESULT_VAR_NAME + "." + fieldName);
            result.getGroupings().add(groupByTimeUnit);
        }
        return result;
    }

    /**
     * Returns the query element for the group by user (if applicable).
     * 
     * @param report
     *            report to execute
     * @return the query element for group by user, if the report has a summary
     *         by user. If not, the returned value is null.
     */
    protected IQueryElement getGroupByUserElement(final IReport report) throws InvalidReportArgumentException {
        QueryElementImpl result = null;
        if (ReportSummaryType.USER.equals(report.getSummaryType())) {
            result = new QueryElementImpl();
            SelectElementImpl countSelect = new SelectElementImpl();
            SelectElementImpl userNameSelect = new SelectElementImpl();
            SelectElementImpl resultSelect = new SelectElementImpl();

            final String targetDOName = getTargetDOType(report);
            resultSelect.setDOClassName(targetDOName);
            resultSelect.setDOVarName(RESULT_VAR_NAME);
            result.getSelects().add(resultSelect);

            // Select in order! First the user name, then the count
            userNameSelect.setDOClassName(targetDOName);
            userNameSelect.setDOVarName(RESULT_VAR_NAME);
            userNameSelect.setFieldName("userName");
            result.getSelects().add(userNameSelect);

            countSelect.setDOClassName(targetDOName);
            countSelect.setDOVarName(RESULT_VAR_NAME);
            countSelect.setFieldName("userName");
            countSelect.setFunction("count");
            result.getSelects().add(countSelect);

            GroupingElementImpl groupByUserElement = new GroupingElementImpl(RESULT_VAR_NAME + ".userName");
            result.getGroupings().add(groupByUserElement);

            // Check if we have a grouping on the user count
            ISortSpec sortSpec = report.getSortSpec();
            if (sortSpec != null && SortFieldType.COUNT.equals(sortSpec.getSortField())) {
                boolean ascending = false;
                if (SortDirectionType.ASCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = true;
                }
                OrderByElementImpl orderByUserCount = new OrderByElementImpl(ascending, "count(" + RESULT_VAR_NAME + ".userName)");
                result.getOrderBys().add(orderByUserCount);
            }

        }
        return result;
    }

    /**
     * Returns the query element associated with host (if any). Query on
     * hostname is not supported at this point, however sorting is.
     * 
     * @param report
     *            report to execute
     * @return
     */
    protected IQueryElement getHostElement(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        IQueryElement result = null;
        ISortSpec sortSpec = report.getSortSpec();
        if (sortSpec != null) {
            if (SortFieldType.HOST.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                // Creates the "from" statement
                result = new QueryElementImpl();
                SelectElementImpl select = new SelectElementImpl();
                select.setDOVarName(HOST_VAR_NAME);
                select.setDOClassName(HOST_DO_NAME);
                result.getSelects().add(select);

                // Creates the host join condition
                String hqlCondition = RESULT_VAR_NAME + ".hostId = " + HOST_VAR_NAME + ".originalId";
                result.getConditions().add(new ConditionElementImpl(hqlCondition));

                // Creates the sort
                boolean ascending = true;
                if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = false;
                }
                OrderByElementImpl orderByHostName = new OrderByElementImpl(ascending, HOST_VAR_NAME + ".name");
                result.getOrderBys().add(orderByHostName);
            }
        }
        return result;
    }

    /**
     * Returns the query element associated with obligations (if any)
     * 
     * @param report
     *            report to execute
     * @return the query element containing information about obligations
     */
    protected IQueryElement getObligationElement(final IReport report) throws InvalidReportArgumentException {
        // No obligation support for now
        return null;
    }

    /**
     * Returns the query element associated with policy name (if any).
     * 
     * @param report
     *            report to execute
     * @return the policy element, or null if there are no condition on the
     *         policy in the report.
     */
    protected IQueryElement getPolicyElement(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        QueryElementImpl result = null;
        Set policies = report.getInquiry().getPolicies();
        boolean hasPolicyNameCondition = policies.size() > 0;
        boolean sortOnPolicy = false;

        // Let's see if we have a sorting condition on policy
        ISortSpec sortSpec = report.getSortSpec();
        if (sortSpec != null) {
            if (SortFieldType.POLICY.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                sortOnPolicy = true;
            }
        }
        if (InquiryTargetDataType.ACTIVITY.equals(report.getInquiry().getTargetData())) {
            // There cannot be a policy condition for tracking queries
            result = null;
        } else {
            // Creates the "from" statement
            result = new QueryElementImpl();
            SelectElementImpl select = new SelectElementImpl();
            select.setDOVarName(POLICY_VAR_NAME);
            select.setDOClassName(POLICY_DO_NAME);
            result.getSelects().add(select);

            // Creates the basic condition (for the join)
            String hqlCondition = HQLConstants.OPEN_PARENTHESE + RESULT_VAR_NAME + ".policyId = " + POLICY_VAR_NAME + ".id" + HQLConstants.CLOSE_PARENTHESE;
            if (hasPolicyNameCondition) {
                hqlCondition += HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE + HQLConstants.OPEN_PARENTHESE;
                // Adds each policy name in the list
                Iterator it = policies.iterator();
                int argCount = 0;
                while (it.hasNext()) {
                    hqlCondition += HQLConstants.OPEN_PARENTHESE;
                    IInquiryPolicy currentPolicy = (IInquiryPolicy) it.next();
                    String argName = "policy" + argCount++;
                    String searchExpr = currentPolicy.getName();

                    // The query field varies based on the search expression
                    // *ABCD => search on policy full names finishing with ABCD
                    // ABCD => Search on policy name exactly like ABCD
                    String fieldToSearch = "name";
                    if (searchExpr.indexOf(WILDCHAR) == 0 || searchExpr.indexOf("/") >= 0) {
                        fieldToSearch = "fullName";
                    }
                    hqlCondition += getHQLForPolicyNameQuery(fieldToSearch, searchExpr, result.getNamedParameters(), argName);
                    hqlCondition += HQLConstants.CLOSE_PARENTHESE;
                    if (it.hasNext()) {
                        hqlCondition += HQLConstants.OR;
                    }
                }
                hqlCondition += HQLConstants.CLOSE_PARENTHESE;
            }
            ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
            result.getConditions().add(condition);
            if (sortOnPolicy) {
                boolean ascending = true;
                if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = false;
                }
                OrderByElementImpl orderByPolicyName = new OrderByElementImpl(ascending, POLICY_VAR_NAME + ".fullName");
                result.getOrderBys().add(orderByPolicyName);
            }
        }
        return result;
    }

    /**
     * Returns the query element associated with an effect constraint
     * 
     * @param report
     *            report to execute
     * @return a query element associated with an effect constraint
     */
    protected IQueryElement getPolicyDecisionElement(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }

        QueryElementImpl result = null;
        // Policy decision query applies only to policy activity
        if (InquiryTargetDataType.POLICY.equals(report.getInquiry().getTargetData())) {
            final Set policyDecisions = report.getInquiry().getPolicyDecisions();
            final String policyDecisionFieldName = RESULT_VAR_NAME + ".policyDecision";
            boolean hasPolicyDecisionCondition = policyDecisions.size() > 0;

            ISortSpec sortSpec = report.getSortSpec();
            if (sortSpec != null) {
                if (SortFieldType.POLICY_DECISION.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                    boolean ascending = true;
                    if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                        ascending = false;
                    }
                    result = new QueryElementImpl();
                    result.getOrderBys().add(new OrderByElementImpl(ascending, policyDecisionFieldName));
                }
            }

            if (hasPolicyDecisionCondition) {
                if (result == null) {
                    result = new QueryElementImpl();
                }
                String hqlCondition = policyDecisionFieldName + HQLConstants.SPACE + HQLConstants.IN + HQLConstants.SPACE + HQLConstants.OPEN_PARENTHESE;

                // Adds each obligation name in the list
                Iterator it = policyDecisions.iterator();
                int argCount = 0;
                while (it.hasNext()) {
                    IInquiryPolicyDecision currentPolicyDecision = (IInquiryPolicyDecision) it.next();
                    final String policyDecisionToQuery = UserTypeConverter.convertUserType(currentPolicyDecision.getPolicyDecisionType(), new PolicyDecisionUserType());
                    String argName = "decision" + argCount++;
                    result.getNamedParameters().put(argName, policyDecisionToQuery);
                    hqlCondition += HQLConstants.COLON + argName;
                    if (it.hasNext()) {
                        hqlCondition += HQLConstants.COMMA;
                    }
                }
                hqlCondition += HQLConstants.CLOSE_PARENTHESE;
                ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
                result.getConditions().add(condition);
            }
        }
        return result;
    }

    /**
     * Returns the query element associated with the resource class (if any). In
     * the base class, by default we discard this parameter.
     * 
     * @param report
     *            report to execute
     * @param asOf
     *            date to use in order to get the resource class definition. By
     *            default, this should be "now"
     * @return the query element associated with the resource class
     */
    protected IQueryElement getResourceClassElement(final IReport report, Date asOf) throws InvalidReportArgumentException {
        return null;
    }

    /**
     * Returns the query element associated with the resource name (if any) and
     * the resource class(if any). Query for resource name and resource class
     * are slightly different. The resource name is directly stored in the log
     * record, and can filtered with a wilcard if necessary. The resource class
     * is more complex. With the resource class, the PF gives the definition of
     * the resource class, and another function transforms this definition into
     * a query element. Both elements are combined
     * 
     * @param report
     *            report to execute
     * @return the query element associated with the resource name
     */
    protected IQueryElement getResourceNameElement(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report cannot be null");
        }
        Set resources = report.getInquiry().getResources();
        Set resourceNames = new HashSet(); // Set of resource names
        Iterator it = resources.iterator();
        IEntityResolver resolver = getResourceAndGroupResolver();
        while (it.hasNext()) {
            IInquiryResource entry = (IInquiryResource) it.next();
            String name = entry.getName();
            EntityExpressionType type = resolver.resolve(name);
            if (EntityExpressionType.ENTITY.equals(type) || EntityExpressionType.ENTITY_AND_ENTITY_GROUP.equals(type)) {
                String resourceName = resolver.extractValue(type, name);
                if (resourceName != null) {
                    resourceNames.add(resourceName);
                }
            }
        }

        // See whether there is sorting on the "from resource" fiels
        ISortSpec sortSpec = report.getSortSpec();
        boolean hasFromResourceSort = false;
        boolean hasToResourceSort = false;
        boolean ascending = false;
        if (sortSpec != null) {
            if (SortFieldType.FROM_RESOURCE.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                hasFromResourceSort = true;
                if (SortDirectionType.ASCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = true;
                }
            } else if (SortFieldType.TO_RESOURCE.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                hasToResourceSort = true;
                if (SortDirectionType.ASCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = true;
                }
            }
        }

        // Build the resource name condition
        IQueryElement result = null;
        String hqlResourceCondition = null;
        boolean hasResourceNames = resourceNames.size() > 0;

        if (hasResourceNames || hasFromResourceSort || hasToResourceSort) {
            result = new QueryElementImpl();
            SelectElementImpl resultSelect = new SelectElementImpl();
            resultSelect.setDOVarName(RESULT_VAR_NAME);
            resultSelect.setDOClassName(getTargetDOType(report));
            if (hasResourceNames) {
                Map args = new HashMap();
                it = resourceNames.iterator();
                while (it.hasNext()) {
                    if (hqlResourceCondition == null) {
                        hqlResourceCondition = HQLConstants.OPEN_PARENTHESE;
                    } else {
                        hqlResourceCondition += OR;
                    }
                    final String resourceName = (String) it.next();
                    hqlResourceCondition += getHQLForResourceNameQuery(RESULT_VAR_NAME + ".fromResourceInfo.name", resourceName, args);
                    hqlResourceCondition += " " + HQLConstants.OR;
                    hqlResourceCondition += " " + getHQLForResourceNameQuery(RESULT_VAR_NAME + ".toResourceInfo.name", resourceName, args);
                }
                hqlResourceCondition += HQLConstants.CLOSE_PARENTHESE;
                result.getSelects().add(resultSelect);
                ConditionElementImpl condition = new ConditionElementImpl(hqlResourceCondition);
                result.getConditions().add(condition);
                result.getNamedParameters().putAll(args);
            }

            if (hasFromResourceSort) {
                result.getOrderBys().add(new OrderByElementImpl(ascending, RESULT_VAR_NAME + ".fromResourceInfo.name"));
            } else if (hasToResourceSort) {
                result.getOrderBys().add(new OrderByElementImpl(ascending, RESULT_VAR_NAME + ".toResourceInfo.name"));
            }
        }
        return result;
    }

    /**
     * Builds the HQL for the resource name
     * 
     * @param fieldName
     *            field name to use (to or from resource name)
     * @param resourceName
     *            name of the resource to process
     * @param args
     *            a <code>Map</code> representing arguments. This map is
     *            updated when new argsuments are added.
     * @return the HQL expression to search for the resource in a case
     *         insensitive fashion.
     */
    private String getHQLForResourceNameQuery(final String fieldName, final String resourceName, final Map args) {
        String resourceNameToUse = resourceName;
        if (resourceNameToUse.startsWith("\\\\")) {
            resourceNameToUse = "file:" + resourceNameToUse.replace('\\', '/');
        } else if (resourceNameToUse.startsWith(":\\", 1)) {
            resourceNameToUse = "file:///" + resourceNameToUse.replace('\\', '/');
        }
        resourceNameToUse = resourceNameToUse.replace(WILDCHAR, HQL_WILDCHAR);

        CaseInsensitiveLike resourceSearch = new CaseInsensitiveLike(RESULT_VAR_NAME + ".fromResourceInfo.name", resourceNameToUse);
        final String[] resourceBindStrings = resourceSearch.getBindStrings();
        final String[] argNames = new String[5];
        for (int i = 0; i != argNames.length; i++) {
            String argName = "csresource" + args.size();
            argNames[i] = HQLConstants.COLON + argName;
            if (i < resourceBindStrings.length) {
                args.put(argName, resourceBindStrings[i]);
            }
        }
        return resourceSearch.getCondition(fieldName, argNames, HQLConstants.LOWER);
    }

    /**
     * Returns the HQL to query on a policy name
     * 
     * @param fieldName
     *            name of the policy data object to query on
     * @param searchExpression
     *            search expression
     * @param args
     *            HQL named parameter map
     * @param argName
     *            argument name(to ensure unique named parameters)
     * @return the HQL expression to use in the query
     */
    private String getHQLForPolicyNameQuery(final String fieldName, final String searchExpression, final Map args, final String argName) {
        return HQLHelper.getHQLForObjectCaseInsensitiveQuery(POLICY_VAR_NAME, fieldName, searchExpression, args, argName);
    }

    /**
     * Returns the HQL to query on a user name
     * 
     * @param fieldName
     *            name of the user data object to query on
     * @param searchExpression
     *            search expression
     * @param args
     *            HQL named parameter map
     * @param argName
     *            argument name(to ensure unique named parameters)
     * @return the HQL expression to use in the query
     */
    private String getHQLForUserNameQuery(final String fieldName, final String searchExpression, final Map args, final String argName) {
        return HQLHelper.getHQLForObjectCaseInsensitiveQuery(RESULT_VAR_NAME, fieldName, searchExpression, args, argName);
    }

    /**
     * Prepare the main query part. If the report involve details or a summary,
     * the query is a little bit different. Also, this function takes care of
     * finding the target data object type for the query.
     * 
     * @param report
     *            report to execute
     * @return
     */
    protected IQueryElement getMainQueryElement(final IReport report) throws InvalidReportArgumentException {
        IQueryElement result = null;
        // Checks whether this is a detail or summary query
        ReportSummaryType reportSummaryType = report.getSummaryType();
        if (ReportSummaryType.NONE.equals(reportSummaryType)) {
            result = getGroupByNoneElement(report);
        } else if (ReportSummaryType.POLICY.equals(reportSummaryType)) {
            result = getGroupByPolicyElement(report);
        } else if (ReportSummaryType.RESOURCE.equals(reportSummaryType)) {
            result = getGroupByResourceElement(report);
        } else if (timeSummaryTypes.contains(reportSummaryType)) {
            result = getGroupByTimeElement(report);
        } else if (ReportSummaryType.USER.equals(reportSummaryType)) {
            result = getGroupByUserElement(report);
        }
        return result;
    }

    /**
     * Returns a query element to count only the results returned by the query
     * 
     * @param report
     *            report to execute. This should be a non grouped report
     * @return
     */
    protected IQueryElement getMainCountQueryElement(final IReport report) throws InvalidReportArgumentException {
        IQueryElement result = null;
        // Checks whether this is a detail or summary query
        ReportSummaryType reportSummaryType = report.getSummaryType();
        if (ReportSummaryType.NONE.equals(reportSummaryType)) {
            result = new QueryElementImpl();
            SelectElementImpl countSelect = new SelectElementImpl();
            countSelect.setDOClassName(getTargetDOType(report));
            countSelect.setDOVarName(RESULT_VAR_NAME);
            countSelect.setFunction("count");
            countSelect.setFieldName("id");
            result.getSelects().add(countSelect);
        }
        return result;
    }

    /**
     * Returns the resource and groups resolver
     * 
     * @return the resource and groups resolver
     */
    protected IEntityResolver getResourceAndGroupResolver() {
        return this.resourceAndGroupsResolver;
    }

    /**
     * Returns the class name of the target data object
     * 
     * @param report
     *            report to execute
     * @return the class name of the target data object specified in the query
     */
    protected final String getTargetDOClassName(final IReport report) {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }
        String result = null;
        InquiryTargetDataType target = report.getInquiry().getTargetData();
        if (InquiryTargetDataType.ACTIVITY.equals(target)) {
            result = TRACKING_ACTIVITY_LOG_DO_CLASSNAME;
        } else if (InquiryTargetDataType.POLICY.equals(target)) {
            result = POLICY_ACTIVITY_LOG_DO_CLASSNAME;
        }
        return result;
    }

    /**
     * Returns the name of the target data object
     * 
     * @param report
     *            report to execute
     * @return the name of the target data object specified in the query
     */
    protected final String getTargetDOType(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }
        return (String) targetDOMap.get(report.getInquiry().getTargetData());
    }

    /**
     * Returns the time period element (if any)
     * 
     * @param report
     *            report to execute
     * @return the time period element (if applicable), null if there are no
     *         time period condition in the report
     */
    protected IQueryElement getTimePeriodElement(final IReport report) throws InvalidReportArgumentException {
        if (report == null) {
            throw new NullPointerException("Report object cannot be null");
        }
        QueryElementImpl result = null;
        IReportTimePeriod timePeriod = report.getTimePeriod();
        Calendar begin = timePeriod.getBeginDate();
        Calendar end = timePeriod.getEndDate();

        if (begin != null || end != null) {
            String hqlCondition = "";
            result = new QueryElementImpl();
            if (begin != null) {
                hqlCondition = RESULT_VAR_NAME + ".timestamp.time >= :beginDate";
                result.getNamedParameters().put("beginDate", new Long(begin.getTimeInMillis()));
                if (end != null) {
                    hqlCondition += HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE;
                }
            }
            if (end != null) {
                hqlCondition += RESULT_VAR_NAME + ".timestamp.time < :endDate";
                result.getNamedParameters().put("endDate", new Long(end.getTimeInMillis()));
            }
            ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
            result.getConditions().add(condition);
        }

        // See if there is a sort specification associated with the time.
        ReportSummaryType reportSummary = report.getSummaryType();

        // If the report summary is by time but there is no sort spec specified,
        // create a sort specification here. This avoids the "random" result
        // order.
        ISortSpec sortSpec = report.getSortSpec();
        if (sortSpec == null && timeSummaryTypes.contains(reportSummary)) {
            sortSpec = new SortSpecImpl();
            sortSpec.setSortField(SortFieldType.DATE);
            sortSpec.setSortDirection(SortDirectionType.ASCENDING);
        }

        if (sortSpec != null) {
            // The sort specification is handled only if it is about date.
            if (SortFieldType.DATE.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                boolean ascending = true;
                if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                    ascending = false;
                }

                // If the report grouping is none or about time, add the correct
                // order by statement
                if (timeSortMap.get(reportSummary) != null) {
                    if (result == null) {
                        result = new QueryElementImpl();
                    }
                    result.getOrderBys().add(new OrderByElementImpl(ascending, RESULT_VAR_NAME + "." + (String) timeSortMap.get(reportSummary)));
                }
            }
        }
        return result;
    }

    /**
     * This function returns the condition associated with users (if any) in the
     * report. This function needs to looks at the expressions that the user
     * entered in the user collection and need to figure out which one are about
     * users and which ones are about user groups. Once the separation between
     * users and user groups is done, the function can build a query condition.
     * Most of the time, the user should take out the ambiguity by specifying
     * (User) or (group) before the name. If this is not specified, this
     * function has to figure it out.
     * 
     * @param report
     *            report to execute
     * @return the condition for users
     */
    protected IQueryElement getUserElement(final IReport report) throws InvalidReportArgumentException {
        Set users = report.getInquiry().getUsers();
        Set userDisplayNames = new HashSet(); // Set of user display names
        Set groupNames = new HashSet(); // Set of user group names

        ISortSpec sortSpec = report.getSortSpec();
        boolean hasUserSort = false;
        if (sortSpec != null) {
            if (SortFieldType.USER.equals(sortSpec.getSortField()) && sortSpec.getSortDirection() != null) {
                hasUserSort = true;
            }
        }
        Iterator it = users.iterator();
        int argCount = 0;
        Map namedArgs = new HashMap();
        while (it.hasNext()) {
            IInquiryUser entry = (IInquiryUser) it.next();
            String displayName = entry.getDisplayName();
            EntityExpressionType type = getUserAndGroupResolver().resolve(displayName);
            if (EntityExpressionType.ENTITY.equals(type) || EntityExpressionType.ENTITY_AND_ENTITY_GROUP.equals(type)) {
                String userDisplayName = getUserAndGroupResolver().extractValue(type, displayName);
                if (userDisplayName != null) {
                    userDisplayNames.add(userDisplayName);
                }
            }
            if (EntityExpressionType.ENTITY_GROUP.equals(type) || EntityExpressionType.ENTITY_AND_ENTITY_GROUP.equals(type)) {
                String groupName = getUserAndGroupResolver().extractValue(type, displayName);
                if (groupName != null) {
                    groupNames.add(groupName);
                }
            }
        }

        // Build the condition
        String hqlUserNameCondition = null;
        String hqlGroupNameCondition = null;
        IQueryElement result = null;

        if (hasUserSort == true) {
            boolean ascending = true;
            if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                ascending = false;
            }

            OrderByElementImpl userSort = new OrderByElementImpl(ascending, RESULT_VAR_NAME + ".userName");
            result = new QueryElementImpl();
            result.getOrderBys().add(userSort);
        }

        if (userDisplayNames.size() > 0) {
            hqlUserNameCondition = "";
            it = userDisplayNames.iterator();
            while (it.hasNext()) {
                hqlUserNameCondition += HQLConstants.OPEN_PARENTHESE;
                String userName = (String) it.next();
                
                // if the username contains "\", it will be rename and also the next char is removed,
                // such as QAWDE\jimmy.carter will be QAWDEimmy.carter
                // this is cause by CaseInsensitiveLike.unescape(String)
                // change the username before making the query
                userName = userName.replace("\\", "\\\\");
                
                String argName = "user" + argCount++;
                hqlUserNameCondition += getHQLForUserNameQuery("userName", userName, namedArgs, argName);
                hqlUserNameCondition += HQLConstants.CLOSE_PARENTHESE;
                if (it.hasNext()) {
                    hqlUserNameCondition += HQLConstants.OR;
                }
            }
        }

        if (groupNames.size() > 0) {
            hqlGroupNameCondition = HQLHelper.getGroupNameCondition(RESULT_VAR_NAME, ".userId", ".timestamp.time", USER_VAR_NAME, USER_DO_NAME, ".originalId", USERGROUP_VAR_NAME, ".originalId", USERGROUP_DO_NAME, "userGroupMember", "UserGroupMemberDO",
                    ".groupId", ".userId", groupNames, "groupName", namedArgs, report.getAsOf());
        }

        String hqlCondition = null;
        if (hqlUserNameCondition != null && hqlGroupNameCondition != null) {
            hqlCondition = HQLConstants.OPEN_PARENTHESE + HQLConstants.OPEN_PARENTHESE + hqlUserNameCondition + HQLConstants.CLOSE_PARENTHESE;
            hqlCondition += HQLConstants.OR_WITH_SPACES + HQLConstants.OPEN_PARENTHESE + hqlGroupNameCondition + HQLConstants.CLOSE_PARENTHESE + HQLConstants.CLOSE_PARENTHESE;
        } else if (hqlUserNameCondition == null) {
            hqlCondition = hqlGroupNameCondition;
        } else if (hqlGroupNameCondition == null) {
            hqlCondition = hqlUserNameCondition;
        }

        if (hqlCondition != null) {
            if (result == null) {
                result = new QueryElementImpl();
            }
            result.getConditions().add(new ConditionElementImpl(hqlCondition));
        }
        if (result != null) {
            result.getNamedParameters().putAll(namedArgs);
        }
        return result;
    }

    /**
     * Returns the logging level of the target data object
     * 
     * @param report
     *            report to execute
     * @return the logging level of the target data object specified in the
     *         query
     */
    protected IQueryElement getLoggingLevelElement(final IReport report) throws InvalidReportArgumentException {
        IQueryElement result = new QueryElementImpl();
        final String hqlCondition = RESULT_VAR_NAME + ".level >= :logLevel";
        result.getNamedParameters().put("logLevel", new Integer(report.getInquiry().getLoggingLevel()));
        ConditionElementImpl condition = new ConditionElementImpl(hqlCondition);
        result.getConditions().add(condition);

        ISortSpec sortSpec = report.getSortSpec();
        SortFieldType sortFieldType = sortSpec.getSortField();
        if (sortFieldType == SortFieldType.LOGGING_LEVEL) {
            boolean ascending = true;
            if (SortDirectionType.DESCENDING.equals(sortSpec.getSortDirection())) {
                ascending = false;
            }
            OrderByElementImpl orderByLoggingLevel = new OrderByElementImpl(ascending, RESULT_VAR_NAME + ".level");
            result.getOrderBys().add(orderByLoggingLevel);
        }

        return result;
    }

    /**
     * Returns the user and groups resolver
     * 
     * @return the user and groups resolver
     */
    protected IEntityResolver getUserAndGroupResolver() {
        return this.userAndGroupsResolver;
    }

    /**
     * Any report execution manager needs the data source to be setup properly
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        if (getConfiguration() == null) {
            throw new NullPointerException("Missing configuration for report execution manager");
        }
        this.dataSource = getConfiguration().get(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Configuration for report execution manager misses data source.");
        }

        // Creates the user and group entity resolver
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IUserResolver.DATA_SOURCE_CONFIG_PARAM, getDataSource());
        ComponentInfo<IEntityResolver> compInfo = 
            new ComponentInfo<IEntityResolver>(
                "userAndGroupsResolver", 
                UserAndGroupResolverImpl.class, 
                IEntityResolver.class, 
                LifestyleType.TRANSIENT_TYPE, 
                config);
        this.userAndGroupsResolver = getManager().getComponent(compInfo);

        // Creates the resource and group entity resolver
        config = new HashMapConfiguration();
        compInfo = new ComponentInfo<IEntityResolver>(
                "resourceAndGroupsResolver", 
                ResourceAndGroupResolverImpl.class, 
                IEntityResolver.class, 
                LifestyleType.TRANSIENT_TYPE, 
                config);
        this.resourceAndGroupsResolver = getManager().getComponent(compInfo);
    }

    /**
     * Sets the configuration
     * 
     * @param configuration
     *            The configuration to set.
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the log
     * 
     * @param log
     *            The log to set.
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }
}
