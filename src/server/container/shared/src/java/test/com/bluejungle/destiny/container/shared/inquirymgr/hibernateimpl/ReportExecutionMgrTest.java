/* Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IConditionElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ISelectElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.SortDirectionType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This is the test class for the report execution manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrTest.java#1 $
 */

public class ReportExecutionMgrTest extends DACContainerSharedTestCase {

    /**
     * Returns an instance of the report manager
     * 
     * @return an instance of the report manager
     */
    private IReportMgr getReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo compInfo = new ComponentInfo("reportMgr", ReportMgrImpl.class.getName(), IReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IReportMgr reportMgr = (IReportMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * Returns an instance of the report execution manager
     * 
     * @return an instance of the report execution manager
     */
    private ReportExecutionMgrStatefulImpl getReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgr", ReportExecutionMgrStatefulImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        ReportExecutionMgrStatefulImpl reportMgr = (ReportExecutionMgrStatefulImpl) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * This test verifies that the report execution manager gets properly
     * created
     */
    public void testReportExecutionMgrClassBasics() {
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        assertTrue("Report Execution manager class should implement the right interfaces", reportExMgr instanceof IReportExecutionMgr);
        assertTrue("Report Execution manager class should implement the right interfaces", reportExMgr instanceof ILogEnabled);
        assertTrue("Report Execution manager class should implement the right interfaces", reportExMgr instanceof IConfigurable);
        assertTrue("Report Execution manager class should implement the right interfaces", reportExMgr instanceof IInitializable);
        assertTrue("Report Execution manager class should implement the right interfaces", reportExMgr instanceof IManagerEnabled);
        assertNotNull("Report execution manager should create its own result table manager instance", reportExMgr.getDetailResultTableManager());
        assertNotNull("Report execution manager should create its own result table manager instance", reportExMgr.getSummaryResultTableMgr());
        assertNotNull("Report execution manager should create its own entity resolver for users and groups", reportExMgr.getUserAndGroupResolver());
    }

    /**
     * This test verifies that the report execution manager can handle reports
     * that have no particular specification for data, besides the basic
     * constraints.
     */
    public void testReportExecutionMgrWithoutConstraint() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getActionElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getGroupByPolicyElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getGroupByUserElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getObligationElement(report));
        assertNull("No policy element should be created if the report targets tracking", reportExMgr.getPolicyElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getResourceNameElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getResourceClassElement(report, new Date()));
        assertNotNull("A report with minimal constraint should generate a default query element by date descending", reportExMgr.getTimePeriodElement(report));
        assertNull("A report with minimal constraint should generate a query element", reportExMgr.getUserElement(report));
        //the main select is not null
        assertNotNull(reportExMgr.getGroupByNoneElement(report));
        IQuery query = new QueryImpl();
        reportExMgr.buildQueryElements(report, query);
        assertNotNull(query);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        assertNotNull("A policy element should be created if the report targets policy", reportExMgr.getPolicyElement(report));
    }

    /**
     * This test verifies that the action element is properly created from a
     * report object
     */
    public void testReportExecutionMgrActionElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.getInquiry().addAction(ActionEnumType.ACTION_COPY);
        report.getInquiry().addAction(ActionEnumType.ACTION_EMBED);
        IQueryElement result = reportExMgr.getActionElement(report);
        assertNotNull("An action query element should be created if the report contains specifition about action", result);
        assertEquals(1, result.getConditions().size());

        report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        Set actions = inquiry.getActions();
        assertEquals("By default, inquiries have no action conditions.", 0, actions.size());

        ReportExecutionMgrStatefulImpl reportExecutionMgr = (ReportExecutionMgrStatefulImpl) getReportExecutionMgr();

        //Try with null report
        boolean exThrown = false;
        try {
            reportExecutionMgr.getActionElement(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Null report is not a valid argument for action condition", exThrown);

        //Try with no condition. Still, the join condition should be there
        IQueryElement queryElement = reportExecutionMgr.getActionElement(report);
        assertNull("No condition should be created if no actions are specified in report", queryElement);

        //Put 3 action names
        report.getInquiry().addAction(ActionEnumType.ACTION_COPY);
        report.getInquiry().addAction(ActionEnumType.ACTION_OPEN);
        report.getInquiry().addAction(ActionEnumType.ACTION_MOVE);
        queryElement = reportExecutionMgr.getActionElement(report);
        assertNotNull("The action condition should not be null when actions are specified", queryElement);
        Set conditions = queryElement.getConditions();
        assertEquals("The query element should contain one condition", 1, conditions.size());
        assertEquals("The query element should contain one condition and three named parameters", 0, queryElement.getGroupings().size());
        assertEquals("The query element should contain one condition and three named parameters", 3, queryElement.getNamedParameters().size());
        assertEquals("The query element should contain one condition, three named parameters, and nothing else", 0, queryElement.getOrderBys().size());
        assertEquals("The query element should contain one condition, three named parameters, and nothing else", 0, queryElement.getSelects().size());
        Iterator it = conditions.iterator();
        while (it.hasNext()) {
            IConditionElement condition = (IConditionElement) it.next();
            assertEquals("The three actions should be in the expression", 3, countExpressionOccurence(condition.getExpression(), HQLConstants.COLON));
            assertEquals("The three actions should be in the expression", 1, countExpressionOccurence(condition.getExpression(), HQLConstants.IN));
        }
    }

    /**
     * This test verifies if the 'group by' element for 'none' is created
     * properly.
     */
    public void testReportExecutionMgrGroupByNoneElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.POLICY);
        IQueryElement result = reportExMgr.getGroupByNoneElement(report);
        assertNull("No group by none element should be created unless the report has no grouping specified", result);
        report.setSummaryType(ReportSummaryType.RESOURCE);
        result = reportExMgr.getGroupByNoneElement(report);
        assertNull("No group by none element should be created unless the report has no grouping specified", result);
        report.setSummaryType(ReportSummaryType.TIME_DAYS);
        result = reportExMgr.getGroupByNoneElement(report);
        assertNull("No group by none element should be created unless the report has no grouping specified", result);
        report.setSummaryType(ReportSummaryType.TIME_MONTHS);
        result = reportExMgr.getGroupByNoneElement(report);
        assertNull("No group by none element should be created unless the report has no grouping specified", result);
        report.setSummaryType(ReportSummaryType.USER);
        result = reportExMgr.getGroupByNoneElement(report);
        assertNull("No group by none element should be created unless the report has no grouping specified", result);

        //Sets no summary for the report
        report.setSummaryType(ReportSummaryType.NONE);
        result = reportExMgr.getGroupByNoneElement(report);
        assertNotNull("A group by none element should be created if the report has no grouping specified", result);
        //The selection should be based on the id field
        assertEquals("GroupBy none query element should be properly created", 0, result.getConditions().size());
        assertEquals("GroupBy none query element should be properly created", 0, result.getGroupings().size());
        assertEquals("GroupBy none query element should be properly created", 0, result.getNamedParameters().size());
        assertEquals("GroupBy none query element should be properly created", 0, result.getOrderBys().size());
        assertEquals("GroupBy none query element should be properly created", 1, result.getSelects().size());
        Iterator it = result.getSelects().iterator();
        ISelectElement select = (ISelectElement) it.next();
        assertEquals("Select for groupBy none should be properly constructed", reportExMgr.getTargetDOClassName(report), select.getDOClassName());
        assertEquals("Select for groupBy none should be properly constructed", ReportExecutionMgrStatefulImpl.RESULT_VAR_NAME, select.getDOVarName());
        assertEquals("Select for groupBy none should be properly constructed", "id", select.getFieldName());
        assertNull("Select for groupBy none should be properly constructed", select.getFunction());
    }

    /**
     * This test verifies that the grouping element grouping by policy name is
     * properly created.
     */
    public void testReportExecutionMgrGroupByPolicyElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.NONE);
        IQueryElement result = reportExMgr.getGroupByPolicyElement(report);
        assertNull("No group by policy element should be created unless the report specifies a grouping by policy", result);
        report.setSummaryType(ReportSummaryType.RESOURCE);
        result = reportExMgr.getGroupByPolicyElement(report);
        assertNull("No group by policy element should be created unless the report specifies a grouping by policy", result);
        report.setSummaryType(ReportSummaryType.TIME_DAYS);
        result = reportExMgr.getGroupByPolicyElement(report);
        assertNull("No group by policy element should be created unless the report specifies a grouping by policy", result);
        report.setSummaryType(ReportSummaryType.TIME_MONTHS);
        result = reportExMgr.getGroupByPolicyElement(report);
        assertNull("No group by policy element should be created unless the report specifies a grouping by policy", result);
        report.setSummaryType(ReportSummaryType.USER);
        result = reportExMgr.getGroupByPolicyElement(report);
        assertNull("No group by policy element should be created unless the report specifies a grouping by policy", result);

        //Sets a report grouped by policy name
        report.setSummaryType(ReportSummaryType.POLICY);
        result = reportExMgr.getGroupByPolicyElement(report);
        assertNotNull("A group by policy element should be created if the report specifies a grouping by policy", result);
        //The group by policy should result in several query elements.
        //We could drill into every single element and look at them here, but
        // that may be a bit too much -- Maybe I will add this if I run into
        // trouble
        assertEquals("GroupBy policy query element should be properly created", 3, result.getSelects().size());
        assertEquals("GroupBy policy query element should be properly created", 1, result.getConditions().size());
        assertEquals("GroupBy policy query element should be properly created", 1, result.getGroupings().size());
    }

    /**
     * This test verifies that the obligation element is properly created
     */
    public void testReportExecutionMgrObligationElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        assertNull("No obligation element should be returned for now", reportExMgr.getObligationElement(report));
    }

    /**
     * This test verifies that a query element is created properly when the
     * report has a query specification for the policy name.
     */
    public void testReportExecutionMgrPolicyElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.NONE);
        IQueryElement result = reportExMgr.getObligationElement(report);
        assertNull("No policy query element should be created unless the report has a policy query specification", result);
        final String policy1 = "/Test1";
        final String policy2 = "/Test2";
        report.getInquiry().addPolicy(policy1);
        report.getInquiry().addPolicy(policy2);
        result = reportExMgr.getPolicyElement(report);
        assertNotNull("A policy element should be created if a policy query specification is specified", result);
        assertEquals("Policy query element should be properly created", 1, result.getConditions().size());
        assertEquals("Policy period query element should be properly created", 0, result.getGroupings().size());
        assertEquals("Policy period query element should be properly created", 1, result.getSelects().size());
        assertEquals("Policy period query element should be properly created", 0, result.getOrderBys().size());
        assertEquals("Policy period query element should have the right number of arguments", 10, result.getNamedParameters().size());

        report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        Set policies = inquiry.getPolicies();
        assertEquals("By default, inquiries have no policy conditions.", 0, policies.size());

        ReportExecutionMgrStatefulImpl reportExecutionMgr = (ReportExecutionMgrStatefulImpl) getReportExecutionMgr();

        //Try with null report
        boolean exThrown = false;
        try {
            reportExecutionMgr.getPolicyElement(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Null report is not a valid argument", exThrown);

        //Try with no condition.
        IQueryElement policyNameElement = reportExecutionMgr.getPolicyElement(report);
        assertNotNull("If no policies names are specified in the report object, there should still be a policy element (for join)", policyNameElement);
        assertNotNull("If no policies names are specified in the report object, there should still be a policy element (for join)", policyNameElement.getConditions());
        assertNotNull("If no policies names are specified in the report object, there should still be a policy element (for join)", policyNameElement.getSelects());
        assertEquals("If no policies names are specified in the report object, there should still be a policy element (for join)", 1, policyNameElement.getConditions().size());
        assertEquals("If no policies names are specified in the report object, there should still be a policy element (for join)", 1, policyNameElement.getSelects().size());
        assertNotNull("If no policies names are specified in the report object, there should still be a policy element (for join)", policyNameElement.getOrderBys());
        assertEquals("If no policies names are specified in the report object, there should still be a policy element (for join)", 0, policyNameElement.getOrderBys().size());

        //Put 3 policy names
        report.getInquiry().addPolicy("policy1");
        report.getInquiry().addPolicy("policy2");
        report.getInquiry().addPolicy("policy3");
        policyNameElement = reportExecutionMgr.getPolicyElement(report);
        assertNotNull("The policy condition should not be null when policies are specified", policyNameElement);
        Iterator it = policyNameElement.getConditions().iterator();
        IConditionElement condition = (IConditionElement) it.next();
        assertEquals("The three policies should be in the expression", 15, countExpressionOccurence(condition.getExpression(), HQLConstants.COLON));
        assertEquals("The three policies should be in the expression", 4, countExpressionOccurence(condition.getExpression(), HQLConstants.AND));

        //Make sure the combination of query for traking and group by policy is
        // not accepted
        report.getInquiry().getPolicies().clear();
        report.setSummaryType(ReportSummaryType.POLICY);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        assertNull("No policy element should be created for tracking / group by policy", reportExecutionMgr.getPolicyElement(report));

        //Make sure if sorting on policy is done, some policy condition exist
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        report.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        report.getSortSpec().setSortField(SortFieldType.POLICY);
        policyNameElement = reportExecutionMgr.getPolicyElement(report);
        assertNotNull("A policy element should be created upon sorting", policyNameElement);
        assertNotNull("If sorting on policy is done, there should be a policy element", policyNameElement.getConditions());
        assertEquals("If sorting on policy is done, there should be a policy element", 1, policyNameElement.getConditions().size());
        assertNotNull("If sorting on policy is done, there should be a policy element", policyNameElement.getSelects());
        assertEquals("If sorting on policy is done, there should be a policy element", 1, policyNameElement.getSelects().size());
        assertNotNull("If sorting on policy is done, there should be a policy element", policyNameElement.getOrderBys());
        assertEquals("If sorting on policy is done, there should be a policy element", 1, policyNameElement.getOrderBys().size());
    }

    /**
     * This test verifies that the obligation element is properly created
     */
    public void testReportExecutionMgrPolicyDecisionElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();

        //Test the empty case
        assertNull("No policy decision element should be created if the report does not have a policy decision constraint", reportExMgr.getPolicyDecisionElement(report));

        //Test with the wrong target -- should be ignored
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        inquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        IQueryElement elem = reportExMgr.getPolicyDecisionElement(report);
        assertNull("A query element should never be returned for tracking activity", elem);

        //Test with one policy decision
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        elem = reportExMgr.getPolicyDecisionElement(report);
        assertNotNull("A query element should be returned for policy decision when targeted to policy activity", elem);
        assertNotNull("The query element should be filled up properly", elem.getConditions());
        assertEquals("The query element should be filled up properly", 1, elem.getConditions().size());
        assertNotNull("The query element should be filled up properly", elem.getGroupings());
        assertEquals("The query element should be filled up properly", 0, elem.getGroupings().size());
        assertNotNull("The query element should be filled up properly", elem.getNamedParameters());
        assertEquals("The query element should have two arguments", 2, elem.getNamedParameters().size());
        assertNotNull("The query element should be filled up properly", elem.getOrderBys());
        assertEquals("The query element should be filled up properly", 0, elem.getOrderBys().size());
    }

    /**
     * This test verifies that the time period query element is created
     * according to the report query specifications.
     */
    public void testReportExecutionMgrTimePeriodElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.NONE);
        IQueryElement result = reportExMgr.getTimePeriodElement(report);
        assertNotNull("A time period query element should be created for default sorting even if the report does not specify a query specification for a time period", result);
        assertEquals("A time period query element should be created for default sorting even if the report does not specify a query specification for a time period", 1, result.getOrderBys().size());

        //Adds a begin date condition
        report.getTimePeriod().setBeginDate(Calendar.getInstance());
        result = reportExMgr.getTimePeriodElement(report);
        assertNotNull("A time period query element should be created if the report specifies a query specification for a time period", result);
        assertEquals("Time period query element should be properly created", 1, result.getConditions().size());
        assertEquals("Time period query element should be properly created", 0, result.getGroupings().size());
        assertEquals("Time period query element should be properly created", 0, result.getSelects().size());
        assertEquals("Time period query element should be properly created", 1, result.getOrderBys().size());
        assertEquals("Time period query element should be properly created", 1, result.getNamedParameters().size());

        //Adds an end date condition
        report.getTimePeriod().setEndDate(Calendar.getInstance());
        result = reportExMgr.getTimePeriodElement(report);
        assertEquals("Time period query element should be properly created", 1, result.getConditions().size());
        assertEquals("Time period query element should be properly created", 0, result.getGroupings().size());
        assertEquals("Time period query element should be properly created", 0, result.getSelects().size());
        assertEquals("A default order by time", 1, result.getOrderBys().size());
        assertEquals("Time period query element should be properly created", 2, result.getNamedParameters().size());
    }

    /**
     * This test verifies that the time period condition is properly created.
     */
    public void testReportExecutionMgrTimePeriodElement() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        IReportTimePeriod timePeriod = report.getTimePeriod();
        assertNull("By default, new report begin date for time period is null", timePeriod.getBeginDate());
        assertNull("By default, new report end date for time period is null", timePeriod.getEndDate());
        ReportExecutionMgrStatefulImpl reportExecutionMgr = (ReportExecutionMgrStatefulImpl) getReportExecutionMgr();

        //Try with null report
        boolean exThrown = false;
        try {
            reportExecutionMgr.getTimePeriodElement(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Null report is not a valid argument", exThrown);

        //Try with no condition
        IQueryElement timePeriodElement = reportExecutionMgr.getTimePeriodElement(report);
        assertNotNull("A default order by time condition should be generated even if no time period is specified", timePeriodElement);
        assertNotNull("A default order by time condition should be generated even if no time period is specified", timePeriodElement.getOrderBys());
        assertEquals("A default order by time condition should be generated even if no time period is specified", 1, timePeriodElement.getOrderBys().size());

        //Try with begin condition
        Calendar date = Calendar.getInstance();
        report.getTimePeriod().setBeginDate(date);
        timePeriodElement = reportExecutionMgr.getTimePeriodElement(report);
        //Can't test much here
        assertNotNull("Condition should be created if begin date is specified", timePeriodElement);
        assertEquals("One parameter should be specified for begin date", 1, timePeriodElement.getNamedParameters().size());

        //Try with begin condition
        date = Calendar.getInstance();
        report.getTimePeriod().setBeginDate(date);
        report.getTimePeriod().setEndDate(date);
        timePeriodElement = reportExecutionMgr.getTimePeriodElement(report);
        assertNotNull("Condition should be created if begin and end date is specified", timePeriodElement);
        assertEquals("One parameter should be specified for begin and end date", 2, timePeriodElement.getNamedParameters().size());
        Iterator it = timePeriodElement.getNamedParameters().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Long timeMillis = (Long) timePeriodElement.getNamedParameters().get(key);
            assertEquals("Condition should contain time in miliseconds", new Long(date.getTimeInMillis()), timeMillis);
        }
    }

    /**
     * This test verifies that the resource class query element is properly
     * created based on the report definition.
     */
    public void testReportExecutionMgrResourceNameElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.NONE);
        //For now - later on there is going to be one
        assertNull(reportExMgr.getResourceNameElement(report));
    }

    /**
     * This test verifies that the correct result table manager is picked based
     * on the right report type.
     */
    public void testReportExecutionMgrResultTableManagerSelection() {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        IResultTableManager detailResultMgr = reportExMgr.getDetailResultTableManager();
        IResultTableManager summaryResultMgr = reportExMgr.getSummaryResultTableMgr();
        assertNotNull("Report execution manager should have a detail result table manager", detailResultMgr);
        assertNotNull("Report execution manager should have a summary result table manager", summaryResultMgr);
        assertNotSame("Summary result and detail result table manager should be different", detailResultMgr, summaryResultMgr);
        report.setSummaryType(ReportSummaryType.NONE);
        assertEquals("The detail result table manager should be used when no report summary type is specified", detailResultMgr, reportExMgr.getResultTableMgr(report));
        report.setSummaryType(ReportSummaryType.POLICY);
        assertEquals("The summary result table manager should be used when a report summary type is specified", summaryResultMgr, reportExMgr.getResultTableMgr(report));
        report.setSummaryType(ReportSummaryType.RESOURCE);
        assertEquals("The summary result table manager should be used when a report summary type is specified", summaryResultMgr, reportExMgr.getResultTableMgr(report));
        report.setSummaryType(ReportSummaryType.TIME_DAYS);
        assertEquals("The summary result table manager should be used when a report summary type is specified", summaryResultMgr, reportExMgr.getResultTableMgr(report));
        report.setSummaryType(ReportSummaryType.TIME_MONTHS);
        assertEquals("The summary result table manager should be used when a report summary type is specified", summaryResultMgr, reportExMgr.getResultTableMgr(report));
        report.setSummaryType(ReportSummaryType.USER);
        assertEquals("The summary result table manager should be used when a report summary type is specified", summaryResultMgr, reportExMgr.getResultTableMgr(report));
    }

    /**
     * This test verifies that the user query element is created according to
     * the report query specification.
     */
    public void testReportExecutionMgrUserElementCreation() throws InvalidReportArgumentException {
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
        report.setSummaryType(ReportSummaryType.NONE);
        IQueryElement result = reportExMgr.getUserElement(report);
        assertNull("A user related query element should not be created when a report does not specify a user query specification", result);
        report.getInquiry().addUser("(User) ihanen@bluejungle.com");
        report.getInquiry().addUser("(User) sasha@bluejungle.com");
        result = reportExMgr.getUserElement(report);
        assertEquals("User query element should be properly created", 1, result.getConditions().size());
        assertEquals("User query element should be properly created", 0, result.getGroupings().size());
        assertEquals("User query element should be properly created", 0, result.getSelects().size());
        assertEquals("User query element should be properly created", 0, result.getOrderBys().size());
        assertNotNull("User query element should have named parameters", result.getNamedParameters());
        assertTrue("User query element should have named parameters", result.getNamedParameters().size() > 0);

        //Adds a group to the query
        report.getInquiry().addUser("(Group) group1");
        result = reportExMgr.getUserElement(report);
        assertEquals("User query element should be properly created", 1, result.getConditions().size());
        assertEquals("User query element should be properly created", 0, result.getGroupings().size());
        assertEquals("User query element should be properly created", 0, result.getSelects().size());
        assertEquals("User query element should be properly created", 0, result.getOrderBys().size());
        assertNotNull("User query element should have named parameters", result.getNamedParameters());
        assertTrue("User query element should have named parameters", result.getNamedParameters().size() > 0);

        // test username contains slash \
        // this is target bug 5634
        report = reportMgr.createReport();
        report.getInquiry().addUser("(User) Test\\hchan");
        result = reportExMgr.getUserElement(report);
        assertTrue("User query element should have the added User", result.getNamedParameters().containsValue("test\\hchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("testchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("testhchan"));
        
        report = reportMgr.createReport();
        report.getInquiry().addUser("(User) Test\\\\hchan");
        result = reportExMgr.getUserElement(report);
        assertTrue("User query element should have the added User", result.getNamedParameters().containsValue("test\\\\hchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("test\\hchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("test\\chan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("test\\han"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("testhchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("testchan"));
        assertFalse("User query element should have the unknown User", result.getNamedParameters().containsValue("testhan"));
        
        report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        Set users = inquiry.getUsers();
        assertEquals("By default, inquiries have no users conditions.", 0, users.size());

        ReportExecutionMgrStatefulImpl reportExecutionMgr = (ReportExecutionMgrStatefulImpl) getReportExecutionMgr();

        //Try with null report
        boolean exThrown = false;
        try {
            reportExecutionMgr.getUserElement(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Null report is not a valid argument", exThrown);

        //Try with no condition.
        IQueryElement userElement = reportExecutionMgr.getUserElement(report);
        assertNull("If no user names are specified in the report object, there should be no user element", userElement);

        //Make sure if sorting on policy is done, some user condition exist
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        report.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        report.getSortSpec().setSortField(SortFieldType.USER);
        userElement = reportExecutionMgr.getUserElement(report);
        assertNotNull("A user element should be created upon sorting", userElement);
        assertNotNull("If sorting on user is done, there should be a user element", userElement.getConditions());
        assertEquals("If sorting on user is done, there should be a user element", 0, userElement.getConditions().size());
        assertNotNull("If sorting on user is done, there should be a user element", userElement.getSelects());
        assertEquals("If sorting on user is done, there should be a user element", 0, userElement.getSelects().size());
        assertNotNull("If sorting on user is done, there should be a user element", userElement.getOrderBys());
        assertEquals("If sorting on user is done, there should be a user element", 1, userElement.getOrderBys().size());
    }
    

    /**
     * This test verifies that a resource class definition can be transformed
     * properly into HQL TODO: add a bunch of extra tests here and maybe place
     * them in a separate test class.
     */
    //TODO: this is commented out due to the fact that we removed resource 
    //      class based queries, we will need to add this back once it is added back.
//    public void testResourceClassTransformationInHQL() {
//        Calendar now = Calendar.getInstance();
//        now.add(Calendar.MINUTE, 10);
//        Date in10 = new Date(now.getTimeInMillis());
//        try {
//            LifecycleManager lm = (LifecycleManager) ComponentManagerFactory.getComponentManager().getComponent(LifecycleManager.COMP_INFO);
//            DevelopmentEntity de1 = lm.getEntityForName(EntityType.COMPONENT, "name1", LifecycleManager.MAKE_EMPTY);
//            de1.setPql("ID " + de1.getId() + " STATUS APPROVED resource name1 = NAME != \"foo.doc\" and NAME != \"foo2.doc\"");
//            DevelopmentEntity de2 = lm.getEntityForName(EntityType.COMPONENT, "name2", LifecycleManager.MAKE_EMPTY);
//            de2.setPql("ID " + de2.getId() + " STATUS APPROVED resource name2 = TYPE != \"PERL\" and TYPE == \"JAVA\" OR  SIZE > 100 and SIZE < 1000");
//            lm.deployEntities(Arrays.asList(new DevelopmentEntity[] { de1, de2 }), in10, DeploymentType.PRODUCTION, false);
//        } catch (EntityManagementException e) {
//            fail("No error should occur during entity deployment");
//        } catch (PQLException e) {
//            fail("No error should occur during entity deployment");
//        }
//
//        IReportMgr reportMgr = getReportMgr();
//        IReport report = reportMgr.createReport();
//        ReportExecutionMgrStatefulImpl reportExMgr = getReportExecutionMgr();
//        report.setSummaryType(ReportSummaryType.NONE);
//        report.getInquiry().addResource("name1");
//        IQueryElement queryElement = reportExMgr.getResourceClassElement(report, in10);
//        Set conditions = queryElement.getConditions();
//        assertEquals("There should be only one condition generated for all resource classes", 1, conditions.size());
//        final String condition1 = "(((lower(result.fromResourceInfo.name) NOT LIKE :resource0 AND lower(result.fromResourceInfo.name) NOT LIKE :resource1)))";
//        IConditionElement condition = (IConditionElement) conditions.iterator().next();
//        assertEquals("The resource class HQL for one resource class should be correct", condition1, condition.getExpression());
//        Map args = queryElement.getNamedParameters();
//        assertEquals("file://%/foo.doc", args.get("resource0"));
//        assertEquals("file://%/foo2.doc", args.get("resource1"));
//        //Adds a second resource now
//        report.getInquiry().addResource("name2");
//        queryElement = reportExMgr.getResourceClassElement(report, in10);
//        assertEquals("There should be only one condition generated for all resource classes", 1, conditions.size());
//        final String condition2 = "(((lower(result.fromResourceInfo.name) NOT LIKE :resource0 AND lower(result.fromResourceInfo.name) NOT LIKE :resource1)) OR (((lower(result.fromResourceInfo.name) NOT LIKE :resource2 AND lower(result.fromResourceInfo.name) LIKE :resource3) OR (result.fromResourceInfo.size > 100 AND result.fromResourceInfo.size < 1000))))";
//        conditions = queryElement.getConditions();
//        args = queryElement.getNamedParameters();
//        assertEquals("file://%/foo.doc", args.get("resource0"));
//        assertEquals("file://%/foo2.doc", args.get("resource1"));
//        condition = (IConditionElement) conditions.iterator().next();
//        assertEquals("The resource class HQL for 2 resource classes should be correct", condition2, condition.getExpression());
//    }

    /**
     * Counts the number of occurence of an expression in a string
     * 
     * @param stringToSearch
     *            string to look into
     * @param expression
     *            expression to find
     * @return the number of distinct occurence of this expression
     */
    private int countExpressionOccurence(String stringToSearch, String expression) {
        String[] nbOccurences = stringToSearch.split(expression);
        int result = nbOccurences.length - 1;
        return result;
    }
}
