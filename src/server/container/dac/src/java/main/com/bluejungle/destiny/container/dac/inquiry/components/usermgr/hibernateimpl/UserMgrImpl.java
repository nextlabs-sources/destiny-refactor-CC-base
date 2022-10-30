/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr.hibernateimpl;

import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrSortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserGroupDO;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.SortDirectionType;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import org.apache.commons.logging.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the hibernate implementation of the user manager and the user class
 * manager component. This class implements both user and user class manager
 * interfaces.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/hibernateimpl/UserMgrImpl.java#1 $
 */

public class UserMgrImpl implements IUserMgr, IUserClassMgr, IInitializable, ILogEnabled, IConfigurable, IManagerEnabled {

    /**
     * Map of query field type to DO field type
     */
    private static final Map<UserMgrQueryFieldType, String> USER_FIELD_TYPE_2_DO_FIELD = 
            new HashMap<UserMgrQueryFieldType, String>();
    static {
        USER_FIELD_TYPE_2_DO_FIELD.put(UserMgrQueryFieldType.FIRST_NAME, "firstName");
        USER_FIELD_TYPE_2_DO_FIELD.put(UserMgrQueryFieldType.LAST_NAME, "lastName");
        USER_FIELD_TYPE_2_DO_FIELD.put(UserMgrQueryFieldType.USER_ID, "displayName");
    }

    private static final Map<UserClassMgrQueryFieldType, String> USERCLASS_FIELD_TYPE_2_DO_FIELD = 
            new HashMap<UserClassMgrQueryFieldType, String>();
    static {
        USERCLASS_FIELD_TYPE_2_DO_FIELD.put(UserClassMgrQueryFieldType.DISPLAY_NAME, "displayName");
        USERCLASS_FIELD_TYPE_2_DO_FIELD.put(UserClassMgrQueryFieldType.NAME, "name");
    }

    protected static final char SINGLECHAR_WILDCARD = '?';
    protected static final char WILDCARD = '*';
    protected static final char HQL_SINGLECHAR_WILDCARD = '_';
    protected static final char HQL_WILDCARD = '%';
    private IConfiguration config;
    private IHibernateRepository dataSource;
    private Log log;
    private IComponentManager compMgr;

    /**
     * Adds a query specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param querySpec
     *            query specification to add
     */
    protected void addQueryTerm(Criteria crit, IUserMgrQueryTerm queryTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (queryTerm == null) {
            throw new NullPointerException("QueryTerm cannot be null");
        }
        final String fieldName = USER_FIELD_TYPE_2_DO_FIELD.get(queryTerm.getFieldName());
        if (fieldName == null) {
            throw new NullPointerException("Unsupported query term field:" + queryTerm.getFieldName());
        }
        crit.add(new CaseInsensitiveLike(fieldName, 
                queryTerm.getExpression()
                    .replace(WILDCARD, HQL_WILDCARD)
                    .replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD)));
    }

    /**
     * Adds a query term to the HQL expression
     * 
     * @param hqlParams
     *            HQL parameters map
     * @param queryTerm
     *            query term to add to the HQL expression
     * @return the HQL fragment, along with the populated hqlParams
     */
    protected String addQueryTerm(Map<String, Object> hqlParams, IUserMgrQueryTerm queryTerm) {
        if (queryTerm == null) {
            throw new NullPointerException("QueryTerm cannot be null");
        }
        final String fieldName = USER_FIELD_TYPE_2_DO_FIELD.get(queryTerm.getFieldName());
        if (fieldName == null) {
            throw new NullPointerException("Unsupported query term field:" + queryTerm.getFieldName());
        }
        String hqlFieldName = "u." + fieldName;
        CaseInsensitiveLike likeSearch = new CaseInsensitiveLike(hqlFieldName, 
                queryTerm.getExpression()
                    .replace(WILDCARD, HQL_WILDCARD)
                    .replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD));
        final String[] resourceBindStrings = likeSearch.getBindStrings();
        final String[] argNames = new String[5];
        for (int i = 0; i != argNames.length; i++) {
            String argName = "term" + hqlParams.size();
            argNames[i] = ":" + argName;
            if (i < resourceBindStrings.length) {
                hqlParams.put(argName, resourceBindStrings[i]);
            }
        }
        return likeSearch.getCondition(hqlFieldName, argNames, "lower");
    }

    /**
     * Adds a query specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param querySpec
     *            query specification to add
     */
    protected void addQueryTerm(Criteria crit, IUserClassMgrQueryTerm queryTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (queryTerm == null) {
            throw new NullPointerException("QueryTerm cannot be null");
        }
        Criterion qs = null;
        final String fieldName = USERCLASS_FIELD_TYPE_2_DO_FIELD.get(queryTerm.getFieldName());
        if (fieldName == null) {
            throw new NullPointerException("Unsupported query term field:" + queryTerm.getFieldName());
        }
        crit.add(new CaseInsensitiveLike(fieldName, 
                queryTerm.getExpression()
                    .replace(WILDCARD, HQL_WILDCARD)
                    .replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD)));

        if (qs != null) {
            crit.add(qs);
        }
    }

    /**
     * Adds a sort specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param sortSpec
     *            sort specification to add
     */
    protected void addSortTerm(Criteria crit, IUserMgrSortTerm sortTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (sortTerm == null) {
            throw new NullPointerException("SortSpec cannot be null");
        }
        String fieldName = null;
        if (UserMgrSortFieldType.FIRST_NAME.equals(sortTerm.getFieldName())) {
            fieldName = "firstName";
        } else if (UserMgrSortFieldType.LAST_NAME.equals(sortTerm.getFieldName())) {
            fieldName = "lastName";
        }

        if (fieldName == null) {
            throw new NullPointerException("Unsupported sort term field:" + sortTerm.getFieldName());
        }
        if (SortDirectionType.DESCENDING.equals(sortTerm.getDirection())) {
            crit.addOrder(Order.desc(fieldName));
        } else {
            crit.addOrder(Order.asc(fieldName));
        }
    }

    /**
     * Returns the HQL fragment (along with the HQL parameters) for a given sort
     * term
     * 
     * @param hqlParams
     *            map of HQL parameters
     * @param sortTerm
     *            sort term to process
     * @param first
     *            true if this is the first sorting expression, false otherwise
     * @return the HQL fragment (along with the HQL parameters) for a given sort
     *         term
     */
    protected String addSortTerm(Map<String, Object> hqlParams, IUserMgrSortTerm sortTerm, boolean first) {
        if (sortTerm == null) {
            throw new NullPointerException("SortSpec cannot be null");
        }
        String fieldName = null;
        if (UserMgrSortFieldType.FIRST_NAME.equals(sortTerm.getFieldName())) {
            fieldName = "firstName";
        } else if (UserMgrSortFieldType.LAST_NAME.equals(sortTerm.getFieldName())) {
            fieldName = "lastName";
        }

        
        if (fieldName == null) {
            throw new NullPointerException("Unsupported sort term field:" + sortTerm.getFieldName());
        }
        String hqlFragment;
        hqlFragment = first ? " ORDER BY u." : ", u.";
        hqlFragment += fieldName;
        if (SortDirectionType.DESCENDING.equals(sortTerm.getDirection())) {
            hqlFragment += " DESC";
        } else {
            hqlFragment += " ASC";
        }
        return hqlFragment;
    }

    /**
     * Adds a sort specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param sortSpec
     *            sort specification to add
     */
    protected void addSortTerm(Criteria crit, IUserClassMgrSortTerm sortTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (sortTerm == null) {
            throw new NullPointerException("SortSpec cannot be null");
        }
        String fieldName = null;
        if (UserClassMgrSortFieldType.DISPLAY_NAME.equals(sortTerm.getFieldName())) {
            fieldName = "displayName";
        } else if (UserClassMgrSortFieldType.NAME.equals(sortTerm.getFieldName())) {
            fieldName = "name";
        }

        if (fieldName != null) {
            if (SortDirectionType.DESCENDING.equals(sortTerm.getDirection())) {
                crit.addOrder(Order.desc(fieldName));
            } else {
                crit.addOrder(Order.asc(fieldName));
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns the data source object
     * 
     * @return the data source set during the init phase
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgr#getUsers(com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec)
     */
    public List<IUser> getUsers(IUserMgrQuerySpec querySpec) throws DataSourceException {
        Session session = null;

        try {
            session = this.dataSource.getSession();
            StringBuffer hqlQuery = new StringBuffer("from UserDO u");
            hqlQuery.append(" WHERE ((u.originalId != '");
            hqlQuery.append(IHasId.UNKNOWN_ID);
            hqlQuery.append("')");
            hqlQuery.append(" AND (u.timeRelation.activeFrom <= :asOfTime) AND (u.timeRelation.activeTo > :asOfTime)");

            final Map<String, Object> hqlQueryParams = new HashMap<String, Object>();
            hqlQueryParams.put("asOfTime", new Long(Calendar.getInstance().getTimeInMillis()));

            if (querySpec != null) {
                IUserMgrQueryTerm[] searchTerms = querySpec.getSearchSpecTerms();
                if (searchTerms != null) {
                    int size = searchTerms.length;
                    for (int index = 0; index < size; index++) {
                        hqlQuery.append(" AND (");
                        IUserMgrQueryTerm queryTerm = querySpec.getSearchSpecTerms()[index];
                        hqlQuery.append(addQueryTerm(hqlQueryParams, queryTerm));
                        hqlQuery.append(")");
                    }
                }

                hqlQuery.append(")");

                IUserMgrSortTerm[] sortTerms = querySpec.getSortSpecTerms();
                if (sortTerms != null) {
                    int size = sortTerms.length;
                    for (int index = 0; index < size; index++) {
                        IUserMgrSortTerm sortTerm = querySpec.getSortSpecTerms()[index];
                        hqlQuery.append(addSortTerm(hqlQueryParams, sortTerm, index == 0));
                    }
                }
            } else {
                hqlQuery.append(")");
            }

            Query q = session.createQuery(hqlQuery.toString());
            for (Map.Entry<String, Object> e: hqlQueryParams.entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }
            if (querySpec != null && querySpec.getLimit() > 0) {
                q.setMaxResults(querySpec.getLimit());
            }
            final List<IUser> queryResults = q.list();
            return queryResults;
        } catch (HibernateException e) {
            getLog().error("Error when fetching users", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgr#getUserClasses(com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec)
     */
    public List<IUserGroup> getUserClasses(IUserClassMgrQuerySpec querySpec) throws DataSourceException {
        List<IUserGroup> results = null;
        Session session = null;

        try {
            session = this.dataSource.getSession();
            Criteria crit = session.createCriteria(UserGroupDO.class);

            if (querySpec != null) {
                int size = querySpec.getSearchSpecTerms().length;

                for (int index = 0; index < size; index++) {
                    IUserClassMgrQueryTerm queryTerm = querySpec.getSearchSpecTerms()[index];
                    addQueryTerm(crit, queryTerm);
                }
                
                size = querySpec.getSortSpecTerms().length;

                for (int index = 0; index < size; index++) {
                    IUserClassMgrSortTerm sortTerm = querySpec.getSortSpecTerms()[index];
                    addSortTerm(crit, sortTerm);
                }
            }

            Date now = new Date();
            crit.add(Expression.le("timeRelation.activeFrom", now));
            crit.add(Expression.gt("timeRelation.activeTo", now));

            results = crit.list();
        } catch (HibernateException e) {
            getLog().error("Error when fetching user classes", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
        return results;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.dataSource = this.config.get(IUserMgr.DATASOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source configuration must be provided for User Manager component");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.config = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newCompMgr) {
        this.compMgr = newCompMgr;
    }
}
