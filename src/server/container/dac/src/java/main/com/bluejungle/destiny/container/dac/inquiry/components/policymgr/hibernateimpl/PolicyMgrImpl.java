/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.policymgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Order;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrSortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
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
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the hibernate implementation of the policy manager component
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/hibernateimpl/PolicyMgrImpl.java#1 $
 */

public class PolicyMgrImpl implements IPolicyMgr, IInitializable, ILogEnabled, IConfigurable, IManagerEnabled {

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
     * @param queryTerm
     *            query specification to add
     */
    protected void addSearchTerm(Criteria crit, IPolicyMgrQueryTerm queryTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (queryTerm == null) {
            throw new NullPointerException("QuerySpec cannot be null");
        }
        Criterion qs = null;
        if (PolicyMgrQueryFieldType.NAME.equals(queryTerm.getFieldName())) {
            qs = new CaseInsensitiveLike("name", queryTerm.getExpression().replace(WILDCARD, HQL_WILDCARD).replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD));
        }

        if (qs != null) {
            crit.add(qs);
        }
    }

    /**
     * Adds a sort specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param sortTerm
     *            sort specification to add
     */
    protected void addSortTerm(Criteria crit, IPolicyMgrSortTerm sortTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (sortTerm == null) {
            throw new NullPointerException("sortTerm cannot be null");
        }
        String fieldName = null;
        if (PolicyMgrSortFieldType.NAME.equals(sortTerm.getFieldName())) {
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
     * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgr#getPolicies(com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrQuerySpec,
     *      com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrSortSpec)
     */
    public List<IPolicy> getPolicies(IPolicyMgrQuerySpec querySpec) throws DataSourceException {
        List<IPolicy> results = null;
        Session session = null;
        try {
            session = this.dataSource.getSession();
            Criteria crit = session.createCriteria(PolicyDO.class);
            if (querySpec != null) {
                IPolicyMgrQueryTerm[] searchTerms = querySpec.getSearchSpecTerms();
                if (searchTerms != null) {
                    int size = querySpec.getSearchSpecTerms().length;
                    for (int index = 0; index < size; index++) {
                        IPolicyMgrQueryTerm queryTerm = querySpec.getSearchSpecTerms()[index];
                        addSearchTerm(crit, queryTerm);
                    }
                }

                IPolicyMgrSortTerm[] sortTerms = querySpec.getSortSpecTerms();
                if (sortTerms != null) {
                    int size = querySpec.getSortSpecTerms().length;
                    for (int index = 0; index < size; index++) {
                        IPolicyMgrSortTerm sortTerm = querySpec.getSortSpecTerms()[index];
                        addSortTerm(crit, sortTerm);
                    }
                }
            }
            results = crit.list();
        } catch (HibernateException e) {
            getLog().error("Error when fetching policies", e);
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
        getLog().debug("Initializing policy manager...");
        this.dataSource = this.config.get(DATASOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source configuration must be provided for Policy Manager component");
        }
        getLog().debug("Policy manager initialized.");
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
