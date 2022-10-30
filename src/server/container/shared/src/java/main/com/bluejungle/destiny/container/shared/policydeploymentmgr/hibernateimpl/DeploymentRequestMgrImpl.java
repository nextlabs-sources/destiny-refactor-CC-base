/*
 * Created on Feb 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import java.util.Calendar;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the deployment request manager implementation class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/DeploymentRequestMgrImpl.java#1 $
 */

public class DeploymentRequestMgrImpl implements IDeploymentRequestMgr {

    private IConfiguration configuration;
    private IHibernateRepository mgmtDateSource;
    private Log log;

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr#createDeploymentRequest()
     */
    public IDeploymentRequest createDeploymentRequest() {
        return createDeploymentRequest(Calendar.getInstance());
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr#createDeploymentRequest()
     */
    public IDeploymentRequest createDeploymentRequest(Calendar scheduleTime) {
        IDeploymentRequest result = null;
        Session s = null;
        Transaction t = null;
        try {
            s = this.mgmtDateSource.getSession();
            t = s.beginTransaction();
            DeploymentRequestDO request = new DeploymentRequestDO();
            request.setTime(Calendar.getInstance());
            request.setScheduleTime(scheduleTime);
            s.save(request);
            t.commit();
            result = request;
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Unable to create a new deployment request, " +e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.log = null;
        this.configuration = null;
        try {
            this.mgmtDateSource.closeCurrentSession();
        } catch (HibernateException ignore) {
        }
        this.mgmtDateSource = null;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr#getDeploymentRequest(java.lang.Long)
     */
    public IDeploymentRequest getDeploymentRequest(Long id) {
        IDeploymentRequest result = null;
        Session session = null;
        try {
            session = this.mgmtDateSource.getSession();
            Criteria criteria = session.createCriteria(DeploymentRequestDO.class);
            criteria.add(Expression.eq("id", id));
            List list = criteria.list();
            result = (IDeploymentRequest) list.get(0);
        } catch (HibernateException e) {
            getLog().error("Unable to retrieve deployment request:" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        this.mgmtDateSource = (IHibernateRepository) this.configuration.get(MGMT_DATA_SOURCE_CONFIG_PARAM);
        if (this.mgmtDateSource == null) {
            throw new NullPointerException("The management data source needs to be set in the configuration for the deployment request Mgr.");
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr#saveDeploymentRequest(com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest)
     */
    public void saveDeploymentRequest(IDeploymentRequest updated) {
        if (updated.getId() == null) {
            throw new NullPointerException("The argument cannot be a new record (id cannot be null)");
        }

        Session s = null;
        Transaction t = null;
        try {
            s = this.mgmtDateSource.getSession();
            t = s.beginTransaction();
            s.saveOrUpdate(updated);
            List list = updated.getTargetHosts();
            for(Object obj : list){
            	s.save(obj);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Error when saving deployment request: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }
}