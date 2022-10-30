/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HQLConstants;

/**
 * This is the implementation class for the HQL query object.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/QueryImpl.java#1 $
 */

public class QueryImpl implements IQuery {

    private static final Log LOG = LogFactory.getLog(QueryImpl.class);
    private static final String DOT = ".";
    private Set conditionElements = new LinkedHashSet();
    private Map doClassNames = new HashMap();
    private Set groupingElements = new LinkedHashSet();
    private Map namedParameters = new HashMap();
    private Set orderByElements = new LinkedHashSet();
    private Set selectElements = new LinkedHashSet();

    /**
     * Constructor
     */
    public QueryImpl() {
        super();
    }

    /**
     * Copy constructor
     * 
     * @param original
     *            object to clone
     */
    public QueryImpl(QueryImpl original) {
        getConditionElements().addAll(original.getConditionElements());
        getDoClassNames().putAll(original.getDoClassNames());
        getGroupingElements().addAll(original.getGroupingElements());
        getNamedParameters().putAll(original.getNamedParameters());
        getOrderByElements().addAll(original.getOrderByElements());
        getSelectElements().addAll(original.getSelectElements());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#addQueryElement(com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement)
     */
    public void addQueryElement(IQueryElement newElement) {
        if (newElement != null) {

            //Check that the variable names used in the select are compatible
            Map selectVars = new HashMap();
            Iterator it = newElement.getSelects().iterator();
            while (it.hasNext()) {
                ISelectElement select = (ISelectElement) it.next();
                if (!isValidVarName(select)) {
                    throw new IllegalArgumentException("Illegal variable name for data object " + select.getDOClassName());
                }
                selectVars.put(select.getDOClassName(), select.getDOVarName());
            }

            //The new query element is valid, add it to the list
            this.conditionElements.addAll(newElement.getConditions());
            this.doClassNames.putAll(selectVars);
            this.groupingElements.addAll(newElement.getGroupings());
            this.namedParameters.putAll(newElement.getNamedParameters());
            this.orderByElements.addAll(newElement.getOrderBys());
            this.selectElements.addAll(newElement.getSelects());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#copy()
     */
    public IQuery copy() {
        QueryImpl result = null;
        try {
            Class forName = Class.forName(getClass().getName());
            result = (QueryImpl) forName.newInstance();
            result.conditionElements.addAll(this.conditionElements);
            result.doClassNames.putAll(this.doClassNames);
            result.groupingElements.addAll(this.groupingElements);
            result.namedParameters.putAll(this.namedParameters);
            result.orderByElements.addAll(this.orderByElements);
            result.selectElements.addAll(this.selectElements);
        } catch (ClassNotFoundException e) {
            result = null;
        } catch (InstantiationException e) {
            result = null;
        } catch (IllegalAccessException e) {
            result = null;
        }
        return result;
    }

    /**
     * Returns if the variable name associated with some data objects in the
     * select statement are not conflicting with other variable names already
     * defined. The caller is supposed to get correct variable names by calling
     * the findVarName API.
     * 
     * @param select
     *            select element to analyze
     * @return true if the select element contains valid variable names, false
     *         otherwise.
     */
    protected boolean isValidVarName(ISelectElement select) {
        boolean result = true;
        String dataObjectName = select.getDOClassName();

        if (doClassNames.containsKey(dataObjectName) && !doClassNames.get(dataObjectName).equals(select.getDOVarName())) {
            result = false;
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#findVarName(java.lang.String)
     */
    public String findVarName(String dataObjectName) {
        return (String) this.doClassNames.get(dataObjectName);
    }

    /**
     * Returns the conditionElements.
     * 
     * @return the conditionElements.
     */
    protected Set getConditionElements() {
        return this.conditionElements;
    }

    /**
     * Returns the doClassNames.
     * 
     * @return the doClassNames.
     */
    protected Map getDoClassNames() {
        return this.doClassNames;
    }

    /**
     * Returns the groupingElements.
     * 
     * @return the groupingElements.
     */
    protected Set getGroupingElements() {
        return this.groupingElements;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Generates a from statement based on the query definition
     * 
     * @param currentBuffer
     *            current result buffer to append into
     */
    protected void generateFromStatement(final StringBuffer currentBuffer) {
        if (currentBuffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        currentBuffer.append(HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE);
        Iterator fromIt = this.doClassNames.keySet().iterator();
        if (fromIt.hasNext()) {
            String doClassName = (String) fromIt.next();
            currentBuffer.append(doClassName + HQLConstants.SPACE + (String) getDoClassNames().get(doClassName));
        }
        while (fromIt.hasNext()) {
            final String doClassName = (String) fromIt.next();
            currentBuffer.append(HQLConstants.COMMA + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + (String) getDoClassNames().get(doClassName));
        }
    }

    /**
     * Generates a group by statement based on the query definition
     * 
     * @param currentBuffer
     *            current result buffer to append into
     */
    protected void generateGroupByStatement(final StringBuffer currentBuffer) {
        if (currentBuffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        Set groupings = getGroupingElements();
        if (groupings.size() > 0) {
            currentBuffer.append(HQLConstants.SPACE + HQLConstants.GROUPBY + HQLConstants.SPACE);
            Iterator groupIt = groupings.iterator();
            if (groupIt.hasNext()) {
                IGroupingElement group = (IGroupingElement) groupIt.next();
                currentBuffer.append(group.getExpression());
            }
            while (groupIt.hasNext()) {
                IGroupingElement group = (IGroupingElement) groupIt.next();
                currentBuffer.append(HQLConstants.COMMA + HQLConstants.SPACE + group.getExpression());
            }
        }

    }

    /**
     * Generates an order by statement based on the query definition
     * 
     * @param currentBuffer
     *            current result buffer to append into
     */
    protected void generateOrderByStatement(final StringBuffer currentBuffer) {
        if (currentBuffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        if (this.orderByElements.size() > 0) {
            currentBuffer.append(HQLConstants.SPACE + HQLConstants.ORDERBY + HQLConstants.SPACE);
            Iterator orderIt = this.orderByElements.iterator();
            if (orderIt.hasNext()) {
                IOrderByElement order = (IOrderByElement) orderIt.next();
                currentBuffer.append(order.getExpression() + HQLConstants.SPACE + (order.isAcending() ? HQLConstants.ASC : HQLConstants.DESC));
            }
            while (orderIt.hasNext()) {
                IOrderByElement order = (IOrderByElement) orderIt.next();
                currentBuffer.append(HQLConstants.COMMA + HQLConstants.SPACE + order.getExpression() + HQLConstants.SPACE + (order.isAcending() ? HQLConstants.ASC : HQLConstants.DESC));
            }
        }
    }

    /**
     * Generates a where statement based on the query definition
     * 
     * @param currentBuffer
     *            current result buffer to append into
     */
    protected void generateWhereStatement(final StringBuffer currentBuffer) {
        if (currentBuffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        Set conditions = getConditionElements();
        if (conditions.size() > 0) {
            currentBuffer.append(HQLConstants.SPACE + HQLConstants.WHERE + HQLConstants.SPACE);
            Iterator conditionIt = conditions.iterator();
            if (conditionIt.hasNext()) {
                IConditionElement condition = (IConditionElement) conditionIt.next();
                currentBuffer.append(condition.getExpression());
            }
            while (conditionIt.hasNext()) {
                IConditionElement condition = (IConditionElement) conditionIt.next();
                currentBuffer.append(HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE + condition.getExpression());
            }
        }
    }

    /**
     * Generates a select statement based on the query definition
     * 
     * @return the expression for the select statement
     */
    protected StringBuffer generateSelectStatement() {
        final StringBuffer result = new StringBuffer();
        //Assembles the select statement
        Iterator selectIt = getSelectElements().iterator();
        result.append(HQLConstants.SELECT + HQLConstants.SPACE);
        boolean useComma = false;
        while (selectIt.hasNext()) {
            ISelectElement select = (ISelectElement) selectIt.next();
            String selectExpr = "";
            String function = select.getFunction();
            if (useComma) {
                selectExpr = HQLConstants.COMMA + HQLConstants.SPACE;
            }
            if (function != null) {
                selectExpr += function + HQLConstants.OPEN_PARENTHESE;
            }
            if (select.getFieldName() != null) {
                String varName = select.getDOVarName();
                if (varName != null) {
                    selectExpr += varName + DOT;
                }
                selectExpr += select.getFieldName();
                useComma = true;
            }
            if (function != null) {
                selectExpr += HQLConstants.CLOSE_PARENTHESE;
            }
            if (select.getFieldName() != null) {
                result.append(selectExpr);
            }
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#getHQLQuery(net.sf.hibernate.Session)
     */
    public Query getHQLQuery(final Session session) throws HibernateException {
        final String hqlExpression = getHQLQueryString();
        final Map parameterMap = getHQLQueryParameters();
        final Query result = session.createQuery(hqlExpression);

        getLog().trace("HQL query is : " + hqlExpression);

        //Apply the query parameters (if any)
        final Iterator it = parameterMap.keySet().iterator();
        while (it.hasNext()) {
            final String key = (String) it.next();
            final Object value = parameterMap.get(key);
            result.setParameter(key, parameterMap.get(key));
            getLog().trace("Added HQL query parameter " + key + ":" + value);
        }
        return result;
    }

    /**
     * This function builds the final HQL query to be executed. It assembles all
     * the pieces from the various query elements.
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#getHQLQueryString()
     */
    public String getHQLQueryString() {
        String result = "";
        if (getSelectElements().size() > 0) {
            StringBuffer buffer = generateSelectStatement();
            generateFromStatement(buffer);
            generateWhereStatement(buffer);
            generateGroupByStatement(buffer);
            generateOrderByStatement(buffer);
            result = buffer.toString();
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery#getHQLQueryParameters()
     */
    public Map getHQLQueryParameters() {
        return this.namedParameters;
    }

    /**
     * Returns the namedParameters.
     * 
     * @return the namedParameters.
     */
    protected Map getNamedParameters() {
        return this.namedParameters;
    }

    /**
     * Returns the orderByElements.
     * 
     * @return the orderByElements.
     */
    protected Set getOrderByElements() {
        return this.orderByElements;
    }

    /**
     * Returns the selectElements.
     * 
     * @return the selectElements.
     */
    protected Set getSelectElements() {
        return this.selectElements;
    }
}