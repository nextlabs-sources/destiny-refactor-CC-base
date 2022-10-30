/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.basic.v1.SortDirection;
import com.bluejungle.destiny.types.basic.v1.StringList;
import com.bluejungle.destiny.types.effects.v1.EffectList;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortSpec;
import com.bluejungle.destiny.types.report.v1.ReportTargetType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is a utility class to covert report objects from their web service types
 * to their internal types. This class is given as a convenience for the 2 web
 * service interfaces that will both use this class to perform various
 * conversions.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/bindings/report/v1/ReportWSConverter.java#1 $
 */

class ReportWSConverter {

    /**
     * Mapping of internal report summary type to web service summary type
     */
    private static final Map REPORT_SUMMARY_2_SERVICE_TYPE = new HashMap();
    static {
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.NONE, com.bluejungle.destiny.types.report.v1.ReportSummaryType.None);
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.POLICY, com.bluejungle.destiny.types.report.v1.ReportSummaryType.Policy);
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.RESOURCE, com.bluejungle.destiny.types.report.v1.ReportSummaryType.Resource);
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.TIME_DAYS, com.bluejungle.destiny.types.report.v1.ReportSummaryType.TimeDays);
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.TIME_MONTHS, com.bluejungle.destiny.types.report.v1.ReportSummaryType.TimeMonths);
        REPORT_SUMMARY_2_SERVICE_TYPE.put(ReportSummaryType.USER, com.bluejungle.destiny.types.report.v1.ReportSummaryType.User);
    }

    /**
     * Mapping of internal target data to web service type
     */
    private static final Map TARGET_DATA_2_SERVICE_TYPE = new HashMap();
    static {
        TARGET_DATA_2_SERVICE_TYPE.put(InquiryTargetDataType.ACTIVITY, ReportTargetType.ActivityJournal);
        TARGET_DATA_2_SERVICE_TYPE.put(InquiryTargetDataType.POLICY, ReportTargetType.PolicyEvents);
    }

    /**
     * Mapping of internal target data to web service type
     */
    private static final Map TARGET_SERVICE_TYPE_2_TARGET_DATA = new HashMap();
    static {
        TARGET_SERVICE_TYPE_2_TARGET_DATA.put(ReportTargetType.ActivityJournal, InquiryTargetDataType.ACTIVITY);
        TARGET_SERVICE_TYPE_2_TARGET_DATA.put(ReportTargetType.PolicyEvents, InquiryTargetDataType.POLICY);
    }

    /**
     * Mapping of web service effect type to policy decision type
     */
    private static final Map EFFECT_SERVICE_TYPE_2_POLICY_DECISION = new HashMap();
    static {
        EFFECT_SERVICE_TYPE_2_POLICY_DECISION.put(EffectType.allow, PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        EFFECT_SERVICE_TYPE_2_POLICY_DECISION.put(EffectType.deny, PolicyDecisionEnumType.POLICY_DECISION_DENY);
    }

    /**
     * Mapping of policy decision to effect web service type
     */
    private static final Map POLICY_DECISION_2_EFFECT_TYPE = new HashMap();
    static {
        POLICY_DECISION_2_EFFECT_TYPE.put(PolicyDecisionEnumType.POLICY_DECISION_ALLOW, EffectType.allow);
        POLICY_DECISION_2_EFFECT_TYPE.put(PolicyDecisionEnumType.POLICY_DECISION_DENY, EffectType.deny);
    }

    /**
     * Converts from a web service type to domain action type
     * 
     * @param wsType
     *            web service type to convert
     * @return the corresponding domain action type
     */
    public static ActionEnumType convertActionToEnumType(String wsType) {
        ActionEnumType result = null;
        if (wsType == null) {
            result = null;
        } else {
            result = ActionEnumType.getActionEnum(wsType.toString());
        }
        return result;
    }

    /**
     * Converts from a policy decision to a web service effect type
     * 
     * @param policyDecision
     *            policy decision to convert
     * @return the corresponding web service type
     */
    public static EffectType convertPolicyDecisionTypeToEffectType(final PolicyDecisionEnumType policyDecision) {
        return (EffectType) POLICY_DECISION_2_EFFECT_TYPE.get(policyDecision);
    }

    /**
     * Converts a domain action type to a web service type
     * 
     * @param actionType
     *            domain action type to convert
     * @return the corresponding web service type.
     */
    public static String convertActionToServiceType(ActionEnumType actionType) {
        String result = null;
        if (actionType == null) {
            result = null;
        } else {
            result = actionType.getName();
        }
        return result;
    }

    /**
     * Converts a web service report to an internal report type
     * 
     * @param initialReport
     *            internal report instance
     * @param wsReport
     *            web service report type
     * @return an internal report instance (merged with web service report type)
     */
    public static IReport convertServiceTypeToReportType(IReport initialReport, Report wsReport) {

        if (initialReport == null) {
            throw new NullPointerException("Initial report cannot be null");
        }
        if (wsReport == null) {
            throw new NullPointerException("Web service report cannot be null");
        }
        IReport result = initialReport;

        //Copy the report properties
        result.setSummaryType(ReportWSConverter.convertServiceTypeToReportSummaryType(wsReport.getSummaryType()));
        if (wsReport.getBeginDate() != null) {
            result.getTimePeriod().setBeginDate(wsReport.getBeginDate());
        }
        if (wsReport.getEndDate() != null) {
            result.getTimePeriod().setEndDate(wsReport.getEndDate());
        }

        //Convert the sort specification
        ReportSortSpec wsSortSpec = wsReport.getSortSpec();
        if (wsSortSpec != null) {
            ReportSortFieldName wsSortField = wsSortSpec.getField();
            SortDirection wsSortDirection = wsSortSpec.getDirection();
            if (wsSortField != null && wsSortDirection != null) {
                SortFieldType sortField = SortFieldType.getSortFieldType(wsSortField.getValue());
                result.getSortSpec().setSortField(sortField);
                SortDirectionType sortDirection = SortDirectionType.getSortDirectionType(wsSortDirection.getValue());
                result.getSortSpec().setSortDirection(sortDirection);
            }
        }

        //Convert the inquiry properties
        IInquiry inquiry = initialReport.getInquiry();
        result.setSummaryType(ReportWSConverter.convertServiceTypeToReportSummaryType(wsReport.getSummaryType()));
        inquiry.setTargetData(ReportWSConverter.convertServiceTypeToTargetData(wsReport.getTarget()));

        //Converts the list of actions
        ActionList actionList = wsReport.getActions();
        inquiry.getActions().clear();
        if (actionList != null) {
            String[] actions = actionList.getActions();
            if (actions != null && actions.length > 0) {
                int size = actions.length;
                for (int index = 0; index < size; index++) {
                    inquiry.addAction(ReportWSConverter.convertActionToEnumType(actions[index]));
                }
            }
        }

        //Converts the list of policies
        StringList policyList = wsReport.getPolicies();
        inquiry.getPolicies().clear();
        if (policyList != null) {
            String[] policies = policyList.getValues();
            if (policies != null && policies.length > 0) {
                int size = policies.length;
                for (int index = 0; index < size; index++) {
                    final String currentPolicy = policies[index];
                    if (currentPolicy.length() > 0) {
                        inquiry.addPolicy(currentPolicy);
                    }
                }
            }
        }

        //Converts the list of effects (policy decisions)
        EffectList effectList = wsReport.getEffects();
        inquiry.getPolicyDecisions().clear();
        if (effectList != null) {
            EffectType[] effects = effectList.getValues();
            if (effects != null && effects.length > 0) {
                int size = effects.length;
                for (int index = 0; index < size; index++) {
                    final EffectType currentEffect = effects[index];
                    inquiry.addPolicyDecision(convertServiceEffectTypeToReportPolicyDecisionType(currentEffect));
                }
            }
        }

        //Converts the list of resource names
        StringList resourceList = wsReport.getResourceNames();
        inquiry.getResources().clear();
        if (resourceList != null) {
            String[] resources = resourceList.getValues();
            if (resources != null && resources.length > 0) {
                int size = resources.length;
                for (int index = 0; index < size; index++) {
                    final String currentResource = resources[index];
                    if (currentResource.length() > 0) {
                        inquiry.addResource(currentResource);
                    }
                }
            }
        }

        //Converts the list of users
        inquiry.getUsers().clear();
        StringList userList = wsReport.getUsers();
        if (userList != null) {
            String[] users = userList.getValues();
            if (users != null && users.length > 0) {
                int size = users.length;
                for (int index = 0; index < size; index++) {
                    final String currentUser = users[index];
                    if (currentUser.length() > 0) {
                        inquiry.addUser(users[index]);
                    }
                }
            }
        }
        
        //Converts the logging level
        inquiry.setLoggingLevel(wsReport.getLoggingLevel());
        return result;
    }

    /**
     * Converts a report summary type to a web service report summary type.
     * 
     * @param type
     *            report (internal) summary type.
     */
    public static com.bluejungle.destiny.types.report.v1.ReportSummaryType convertReportSummaryToServiceType(ReportSummaryType type) {
        com.bluejungle.destiny.types.report.v1.ReportSummaryType result = (com.bluejungle.destiny.types.report.v1.ReportSummaryType) REPORT_SUMMARY_2_SERVICE_TYPE.get(type);
        if (result == null) {
            throw new IllegalArgumentException("invalid type");
        }
        return result;
    }

    /**
     * This function converts an effect (web service type) to a policy decision
     * type (for the internal report).
     * 
     * @param effectToConvert
     *            effect web service object to convert
     * @return the corresponding internal policy decision type
     */
    public static PolicyDecisionEnumType convertServiceEffectTypeToReportPolicyDecisionType(final EffectType effectToConvert) {
        return (PolicyDecisionEnumType) EFFECT_SERVICE_TYPE_2_POLICY_DECISION.get(effectToConvert);
    }

    /**
     * This function convert a report summary type (web service type) to an
     * internal report summary type
     * 
     * @param wsType
     *            web service report summary type to convert
     * @return the corresponding internal summary type
     */
    public static ReportSummaryType convertServiceTypeToReportSummaryType(com.bluejungle.destiny.types.report.v1.ReportSummaryType wsType) {
        ReportSummaryType result = ReportSummaryType.getReportSummaryType(wsType.toString());
        return result;
    }

    /**
     * Converts a inquiry target data type to a web service target data type
     * 
     * @param type
     *            inquiry target data type to convert
     * @return the corresponding web service target data type
     */
    public static ReportTargetType convertTargetDataToServiceType(InquiryTargetDataType type) {
        ReportTargetType result = (ReportTargetType) TARGET_DATA_2_SERVICE_TYPE.get(type);
        if (result == null) {
            throw new IllegalArgumentException("invalid type");
        }
        return result;
    }

    /**
     * Converts a web service target data type into an internal inquiry target
     * data type.
     * 
     * @param type
     *            type to convert
     * @return the corresponding internal target data type
     */
    public static InquiryTargetDataType convertServiceTypeToTargetData(ReportTargetType type) {
        InquiryTargetDataType result = (InquiryTargetDataType) TARGET_SERVICE_TYPE_2_TARGET_DATA.get(type);
        if (result == null) {
            throw new IllegalArgumentException("invalid type");
        }
        return result;
    }
}