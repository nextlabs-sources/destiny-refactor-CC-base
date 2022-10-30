/*
 * Created on Feb 14, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;

/**
 * This is the HQL helper class. The HQL helper class provides function that
 * generate HQL fragments based on a set of parameters
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/HQLHelper.java#1 $
 */

public class HQLHelper {

    /**
     * Wildcard
     */
    private static final char HQL_WILDCHAR = '%';
    private static final char WILDCHAR = '*';

    /**
     * Time relation constants
     */
    private static final String TIME_RELATION_FROM = ".timeRelation.activeFrom";
    private static final String TIME_RELATION_TO = ".timeRelation.activeTo";
    private static final String LOG_TIME_FIELD = ".timestamp.time";
    private static final String ASOF_TIME_FIELD = "asOfTime";
    private static final String ASOF_TIME = ":" + ASOF_TIME_FIELD;

    /**
     * Returns the HQL expression to search on a group by name. If the report is
     * run without an asOf condition, the timestamp of the activity record is
     * used directly. If an asOf condition is used, then the time condition is
     * strictly based on that date, regardless of the record timestamp.
     * 
     * t.userId IN ( select u.originalId from UserDO u, UserGroupDO g,
     * UserGroupMemberDO m where t.userId = u.originalId AND t.timestamp.time >=
     * u.timeRelation.activeFrom AND t.timestamp.time < u.timeRelation.activeTo
     * AND u.originalId = m.userId AND t.timestamp.time >=
     * m.timeRelation.activeFrom AND t.timestamp.time < m.timeRelation.activeTo
     * AND g.originalId = m.groupId AND t.timestamp.time >=
     * g.timeRelation.activeFrom AND t.timestamp.time < g.timeRelation.activeTo
     * AND g.name LIKE 'P%'";
     * 
     * @param logVarName
     *            name of the variable representing the activity log.
     * @param logEntityIdFieldName
     *            name of the id field for the entity.
     * @param logTimeFieldName
     *            name of the time field for the activity log data object
     * @param entityVarName
     *            name of the variable name for the activity log data object
     * @param entityDOName
     *            name of the entity DO class name
     * @param entityFieldIdName
     *            name of the entity DO class name id field
     * @param groupVarName
     *            name of the group DO variable name
     * @param groupIdFieldName
     *            name of the group DO id field name
     * @param groupDOName
     *            name of the group DO class name
     * @param groupMemberVarName
     *            name of the group DO variable name
     * @param groupMemberDOName
     *            name of the entity group member DO class name
     * @param groupMemberIdFieldName
     *            name of the entity group member DO id field
     * @param groupMemberEntityIdFieldName
     *            name of the entity group member DO group id name
     * @param groupNames
     *            list of <code>string</code> instances containing the
     *            group(s) expression(s) to search on.
     * @param argName
     *            argument name to use for the group parameters
     * @param args
     *            a <code>Map</code> of query arguments
     * @param asOf
     *            date asOf which the condition should be built. If this value
     *            is NULL, the HQL statement is based on the timestamp of the
     *            activity record. If not, the query is based on the value of
     *            this parameter.
     * @return an HQL expression representing the group membership query
     *         condition
     */
    public static String getGroupNameCondition(String logVarName, String logEntityIdFieldName, String logTimeFieldName, String entityVarName, String entityDOName, String entityFieldIdName, String groupVarName, String groupIdFieldName, String groupDOName,
            String groupMemberVarName, String groupMemberDOName, String groupMemberIdFieldName, String groupMemberEntityIdFieldName, Collection groupNames, String argName, Map args, Date asOf) {

        String result = logVarName + logEntityIdFieldName + HQLConstants.SPACE + HQLConstants.IN + HQLConstants.SPACE;
        result += HQLConstants.OPEN_PARENTHESE + HQLConstants.SPACE + HQLConstants.SELECT + HQLConstants.SPACE;
        result += entityVarName + entityFieldIdName + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE;
        result += entityDOName + HQLConstants.SPACE + entityVarName + HQLConstants.COMMA + HQLConstants.SPACE;
        result += groupDOName + HQLConstants.SPACE + groupVarName + HQLConstants.COMMA;
        result += groupMemberDOName + HQLConstants.SPACE + groupMemberVarName;
        result += HQLConstants.WHERE_WITH_SPACES;
        result += logVarName + logEntityIdFieldName + HQLConstants.SPACE + HQLConstants.EQUAL + HQLConstants.SPACE;
        result += entityVarName + entityFieldIdName + HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE;
        result += getTimeRelationCondition(logVarName, entityVarName, asOf, args);
        result += HQLConstants.AND_WITH_SPACES;
        result += entityVarName + entityFieldIdName + HQLConstants.SPACE + HQLConstants.EQUAL + groupMemberVarName + groupMemberEntityIdFieldName;
        result += HQLConstants.AND_WITH_SPACES;
        result += getTimeRelationCondition(logVarName, groupMemberVarName, asOf, args);
        result += HQLConstants.AND_WITH_SPACES;
        result += groupVarName + groupIdFieldName + HQLConstants.SPACE;
        result += HQLConstants.EQUAL + HQLConstants.SPACE + groupMemberVarName + groupMemberIdFieldName;
        result += HQLConstants.AND_WITH_SPACES;
        result += getTimeRelationCondition(logVarName, groupVarName, asOf, args);
        result += HQLConstants.AND_WITH_SPACES;

        final Iterator it = groupNames.iterator();
        int argCount = 0;
        result += HQLConstants.OPEN_PARENTHESE;
        while (it.hasNext()) {
            result += HQLConstants.OPEN_PARENTHESE;
            final String groupName = (String) it.next();
            String currentArgName = argName + argCount++;
            result += getHQLForObjectCaseInsensitiveQuery(groupVarName, "name", groupName, args, currentArgName);
            result += HQLConstants.CLOSE_PARENTHESE;
            if (it.hasNext()) {
                result += HQLConstants.OR;
            }
        }
        result += HQLConstants.CLOSE_PARENTHESE + HQLConstants.CLOSE_PARENTHESE;
        return result;
    }

    /**
     * 
     * @param logVarName
     *            name of the activity log HQL variable
     * @param entityVarName
     *            name of the entity HQL variable
     * @param asOf
     *            date as of which to build the time relation condition. If this
     *            date is NULL, then the condition is built based on the
     * @param args
     *            HQL arguments
     * @return the HQL expression to for the time relation condition
     */
    private static String getTimeRelationCondition(String logVarName, String entityVarName, Date asOf, Map args) {
        if (asOf == null) {
            return getTimeRelationConditionWithLog(logVarName, entityVarName);
        } else {
            return getTimeRelationConditionWithTimestamp(logVarName, entityVarName, asOf, args);
        }
    }

    /**
     * This function returns a time relation condition between an entity and an
     * absolute timestamp. For example, if the UserDO called "u" has a time
     * relation with the log called "t", the time relation will be something
     * like this
     * 
     * u.timeRelation.activeFrom <= :asOfTime AND u.timeRelation.activeTo >
     * :asOfTime
     * 
     * 
     * @param logVarName
     *            name of the log variable in the HQL expression (in the
     *            example, "t")
     * @param entityVarName
     *            name of the entity variable in the HQL expression (in the
     *            example, "u")
     * @return the HQL expression to join the log timestamp with the entity time
     *         relation field
     */
    private static String getTimeRelationConditionWithTimestamp(String logVarName, String entityVarName, Date asOf, Map args) {
        String result = entityVarName + TIME_RELATION_FROM + HQLConstants.SPACE + HQLConstants.SMALLER_THAN_OR_EQUAL + HQLConstants.SPACE;
        result += ASOF_TIME + HQLConstants.AND_WITH_SPACES;
        result += entityVarName + TIME_RELATION_TO + HQLConstants.GREATER_THAN_WITH_SPACES;
        result += ASOF_TIME;
        args.put(ASOF_TIME_FIELD, new Long(asOf.getTime()));
        return result;
    }

    /**
     * This function returns a time relation condition between an entity and a
     * log object. For example, if the UserDO called "u" has a time relation
     * with the log called "t", the time relation will be something like this
     * 
     * u.timeRelation.activeFrom <= t.timestamp.time AND u.timeRelation.activeTo >
     * t.timestamp.time
     * 
     * 
     * @param logVarName
     *            name of the log variable in the HQL expression (in the
     *            example, "t")
     * @param entityVarName
     *            name of the entity variable in the HQL expression (in the
     *            example, "u")
     * @return the HQL expression to join the log timestamp with the entity time
     *         relation field
     */
    private static String getTimeRelationConditionWithLog(String logVarName, String entityVarName) {
        String result = entityVarName + TIME_RELATION_FROM + HQLConstants.SPACE + HQLConstants.SMALLER_THAN_OR_EQUAL + HQLConstants.SPACE;
        result += logVarName + LOG_TIME_FIELD + HQLConstants.AND_WITH_SPACES;
        result += entityVarName + TIME_RELATION_TO + HQLConstants.GREATER_THAN_WITH_SPACES;
        result += logVarName + LOG_TIME_FIELD;
        return result;
    }

    /**
     * Returns the HQL for querying on a particular entity in a case insensitive
     * way
     * 
     * @param varName
     *            variable name to query on
     * @param fieldName
     *            field name to query on
     * @param searchExpression
     *            HQL expression that fieldName needs to match
     * @param args
     *            HQL named parameter map
     * @param argName
     *            argument name(to ensure unique named parameters)
     * @return the HQL expression to use in the query
     */
    public static String getHQLForObjectCaseInsensitiveQuery(final String varName, final String fieldName, final String searchExpression, final Map args, final String argName) {
        String searchExpressionToUse = searchExpression.replace(WILDCHAR, HQL_WILDCHAR);
        final String fieldNameToSearch = varName + "." + fieldName;
        CaseInsensitiveLike caseInsensitiveSearch = new CaseInsensitiveLike(fieldNameToSearch, searchExpressionToUse);
        final String[] resourceBindStrings = caseInsensitiveSearch.getBindStrings();
        final String[] argNames = new String[5];
        for (int i = 0; i != argNames.length; i++) {
            String arg = argName + "_" + args.size();
            argNames[i] = HQLConstants.COLON + arg;
            if (i < resourceBindStrings.length) {
                args.put(arg, resourceBindStrings[i]);
            }
        }
        return caseInsensitiveSearch.getCondition(fieldNameToSearch, argNames, HQLConstants.LOWER);
    }
}
