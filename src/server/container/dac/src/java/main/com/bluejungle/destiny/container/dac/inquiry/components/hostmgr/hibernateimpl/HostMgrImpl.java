/*
 * Created on May 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Order;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.HostMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.HostMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
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
 * This is the hibernate implementation of the host manager and the host class
 * manager component. This class implements both host and host class manager
 * interfaces.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/hibernateimpl/HostMgrImpl.java#1 $
 */

public class HostMgrImpl implements IHostMgr, IInitializable, ILogEnabled, IConfigurable, IManagerEnabled {

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
    protected void addQueryTerm(Criteria crit, IHostMgrQueryTerm queryTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (queryTerm == null) {
            throw new NullPointerException("QueryTerm cannot be null");
        }
        Criterion qs = null;
        if (HostMgrQueryFieldType.NAME.equals(queryTerm.getFieldName())) {
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
     * @param sortSpec
     *            sort specification to add
     */
    protected void addSortTerm(Criteria crit, IHostMgrSortTerm sortTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (sortTerm == null) {
            throw new NullPointerException("SortSpec cannot be null");
        }
        String fieldName = null;
        if (HostMgrSortFieldType.NAME.equals(sortTerm.getFieldName())) {
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
     * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgr#getHosts(com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec)
     */
    public List<IHost> getHosts(IHostMgrQuerySpec querySpec) throws DataSourceException {
        List<IHost> results = null;
        Session session = null;
        try {
            session = this.dataSource.getSession();
            Criteria crit = session.createCriteria(HostDO.class);
            if (querySpec != null) {
                IHostMgrQueryTerm[] searchTerms = querySpec.getSearchSpecTerms();
                if (searchTerms != null) {
                    final int size = searchTerms.length;
                    for (int index = 0; index < size; index++) {
                        IHostMgrQueryTerm queryTerm = querySpec.getSearchSpecTerms()[index];
                        addQueryTerm(crit, queryTerm);
                    }
                }

                IHostMgrSortTerm[] sortTerms = querySpec.getSortSpecTerms();
                if (sortTerms != null) {
                    final int size = sortTerms.length;
                    for (int index = 0; index < size; index++) {
                        IHostMgrSortTerm sortTerm = querySpec.getSortSpecTerms()[index];
                        addSortTerm(crit, sortTerm);
                    }
                }
            }

            results = crit.list();
        } catch (HibernateException e) {
            getLog().error("Error when fetching hosts", e);
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
        this.dataSource = this.config.get(IHostMgr.DATASOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source configuration must be provided for Host Manager component");
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
