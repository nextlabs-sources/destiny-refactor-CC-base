/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IConcreteAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.search.RelationalOp;

import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Disjunction;
import net.sf.hibernate.expression.Expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HibernateConcreteAgentMgrQueryTerm.java#1 $
 */

class HibernateConcreteAgentMgrQueryTerm implements IConcreteAgentMgrQueryTerm, IHibernateAgentMgrQueryTerm {

    private static final char WILDCHAR = '*';
    private static final char HIBERNATE_WILDCHAR = '%';

    private static final String LAST_HEARTBEAT_PROPERTY_NAME = "lastHeartbeat";

    /**
     * This map maps the agent data objet fields to their HQL expression
     */
    private static final Map QUERY_FIELD_TYPE_TO_PROPERTY_MAP = new HashMap();
    static {
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.ID, "id");
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.COMM_PROFILE_ID, "commProfile.id");
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.HOST, "host");
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.LAST_POLICY_UPDATE, "policyAssemblyStatus.lastAcknowledgedDeploymentBundleTimestamp");
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.REGISTERED, "registered");
        QUERY_FIELD_TYPE_TO_PROPERTY_MAP.put(AgentMgrQueryFieldType.TYPE, "type");
    }

    private static final Map OPERATOR_TO_EXPRESSION_MAP = new HashMap();
    static {
        Class[] parameterTypes = { String.class, Object.class };
        try {
            Method equalsMethod = Expression.class.getMethod("eq", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.EQUALS, equalsMethod);
            
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.NOT_EQUALS, equalsMethod);
            
            Method greaterThanMethod = Expression.class.getMethod("gt", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.GREATER_THAN, greaterThanMethod);
            
            Method greaterThanOrEqualsToQueryMethod = Expression.class.getMethod("ge", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.GREATER_THAN_EQUALS, greaterThanOrEqualsToQueryMethod);

            Method lessThanMethod = Expression.class.getMethod("lt", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.LESS_THAN, lessThanMethod);
            
            Method lessThanOrEqualsToQueryMethod = Expression.class.getMethod("le", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.LESS_THAN_EQUALS, lessThanOrEqualsToQueryMethod);
            
            Method caseInsensitiveLikeMethod = Expression.class.getMethod("ilike", parameterTypes);
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.STARTS_WITH, caseInsensitiveLikeMethod);
            
            OPERATOR_TO_EXPRESSION_MAP.put(RelationalOp.LIKE, caseInsensitiveLikeMethod);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Receieved NoSuchMethodException in static initializer: " + exception.toString());
        }
    }

    private AgentMgrQueryFieldType agentFieldType;
    private RelationalOp operator;
    private Object dataValue;

    /**
     * Create an instance of HibernateConcreteAgentMgrQueryTerm
     * 
     * @param queryField
     * @param operator2
     * @param value
     */
    public HibernateConcreteAgentMgrQueryTerm(AgentMgrQueryFieldType queryField, RelationalOp operator, Object value) {
        if (queryField == null) {
            throw new NullPointerException("queryField cannot be null.");
        }
        
        if (operator == null) {
            throw new NullPointerException("operator cannot be null.");
        }
        
        if (value == null) {
            throw new NullPointerException("value cannot be null.");
        }
        
        this.agentFieldType = queryField;
        this.operator = operator;
        this.dataValue = value;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IConcreteAgentMgrQueryTerm#getExpression()
     */
    public Object getExpression() {
        return this.dataValue;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IConcreteAgentMgrQueryTerm#getFieldName()
     */
    public AgentMgrQueryFieldType getFieldName() {
        return this.agentFieldType;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IConcreteAgentMgrQueryTerm#getOperator()
     */
    public RelationalOp getOperator() {
        return this.operator;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.IHibernateAgentMgrQueryTerm#getCriterion()
     */
    public Criterion getCriterion() {
        validateQueryTerm();

        Criterion valueToReturn = null;
        AgentMgrQueryFieldType fieldType = getFieldName();
        if (fieldType == AgentMgrQueryFieldType.ONLINE) {
            valueToReturn = getOnlineFieldTypeCriterion();
        } else {

            String propertyName = (String) QUERY_FIELD_TYPE_TO_PROPERTY_MAP.get(fieldType);
            if (propertyName == null) {
                throw new IllegalStateException("Unexpected field type, " + fieldType + ".  No mapping from field type to property.");
            }

            Method expressionMethodForOperator = (Method) OPERATOR_TO_EXPRESSION_MAP.get(getOperator());
            if (expressionMethodForOperator == null) {
                throw new IllegalStateException("Unexpected operator, " + getOperator() + ".  No mapping from operator to excpression factory method.");
            }

            Object[] expressionMethodArguments = { propertyName, getQueryValue() };
            try {
                valueToReturn = (Criterion) expressionMethodForOperator.invoke(null, expressionMethodArguments);
                if (getOperator().equals(RelationalOp.NOT_EQUALS)) {
                    valueToReturn = Expression.not(valueToReturn);
                }
            } catch (IllegalArgumentException exception) {
                // Shouldn't happen, since we're invoking instance methods
                throw new IllegalStateException("Unexpected IllegalArgumentException when creating Criterion: " + exception.getMessage());
            } catch (IllegalAccessException exception) {
                // Shouldn't happen, since we're invoking public methods
                throw new IllegalStateException("Unexpected IllegalAccessException when creating Criterion: " + exception.getMessage());
            } catch (InvocationTargetException exception) {
                // FIX ME
                throw new IllegalStateException("FIX ME " + exception.toString());
            }
        }

        return valueToReturn;
    }

    /**
     * This function tests that the query term is valid. For now, we don't cover
     * all the possibilities, since the web service is used internally only. The
     * callers should know what they are doing. If this service becomes public,
     * we can make this validation more robust.
     * 
     * @throws InvalidQuerySpecException
     *             if the query term is incorrect
     */
    private void validateQueryTerm() throws InvalidQuerySpecException {
        RelationalOp queryOperator = getOperator();
        AgentMgrQueryFieldType fieldType = getFieldName();
        Object value = getExpression();
        if (AgentMgrQueryFieldType.TYPE.equals(fieldType)) {
            if (RelationalOp.EQUALS != queryOperator && RelationalOp.NOT_EQUALS != queryOperator) {
                throw new InvalidQuerySpecException("Type query supports only equal and not equal");
            }
            if (!(value instanceof IAgentType)) {
                throw new InvalidQuerySpecException("Type query supports only IAgentType for the value");
            }
        } else if (AgentMgrQueryFieldType.REGISTERED.equals(fieldType)) {
            if (RelationalOp.EQUALS != queryOperator && RelationalOp.NOT_EQUALS != queryOperator) {
                throw new InvalidQuerySpecException("Registered query supports only equal and not equal");
            }
            if (!(value instanceof Boolean)) {
                throw new InvalidQuerySpecException("Registered query supports only Boolean for the value");
            }
        } else if (AgentMgrQueryFieldType.ONLINE.equals(fieldType)) {
            if (RelationalOp.EQUALS != queryOperator) {
                throw new InvalidQuerySpecException("Online query only supports equals");
            }
            if (!Boolean.FALSE.equals(value)) {
                throw new InvalidQuerySpecException("Online query only supports Boolean with FALSE for the value");
            }
        }
    }

    /**
     * Retrieve the query value based on the query term expression
     * 
     * @return
     */
    private Object getQueryValue() {
        Object valueToReturn = getExpression();
        if (valueToReturn instanceof String) {
            String processedValue = (String) valueToReturn;

            processedValue = processedValue.replace(WILDCHAR, HIBERNATE_WILDCHAR);
            if ((this.getOperator().equals(RelationalOp.STARTS_WITH)) && (!processedValue.endsWith(String.valueOf(HIBERNATE_WILDCHAR)))) {
                processedValue += HIBERNATE_WILDCHAR;
            }
            
            valueToReturn = processedValue;
        }

        return valueToReturn;
    }

    /**
     * Build a Criterion for online query field
     * 
     * @return a Criterion for online query field
     */
    private Criterion getOnlineFieldTypeCriterion() {
        // Note - We only support false for the online value at the moment
        Disjunction onlineFieldTypeDisjunction = Expression.disjunction();

        Criterion lastHeartBeatNullCriterion = Expression.isNull(LAST_HEARTBEAT_PROPERTY_NAME);
        onlineFieldTypeDisjunction.add(lastHeartBeatNullCriterion);

        Calendar previousDay = Calendar.getInstance();
        previousDay.add(Calendar.DATE, -1);
        Criterion lastHeartBeatInLastDayCriterion = Expression.lt(LAST_HEARTBEAT_PROPERTY_NAME, previousDay);
        onlineFieldTypeDisjunction.add(lastHeartBeatInLastDayCriterion);

        return onlineFieldTypeDisjunction;
    }
}
