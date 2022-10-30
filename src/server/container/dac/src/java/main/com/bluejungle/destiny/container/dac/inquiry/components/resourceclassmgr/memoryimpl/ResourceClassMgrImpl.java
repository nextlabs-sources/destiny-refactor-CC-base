/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.memoryimpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;

/**
 * This is the hibernate implementation of the resource class manager component
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/memoryimpl/ResourceClassMgrImpl.java#1 $
 */

public class ResourceClassMgrImpl implements IResourceClassMgr, IInitializable, ILogEnabled, IConfigurable, IManagerEnabled {

    private IConfiguration config;
    private Log log;
    private IComponentManager compMgr;
    private LifecycleManager lifecycleMgr;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns the policy framework lifecycle manager
     * 
     * @return the policy framework lifecycle manager
     */
    protected LifecycleManager getLifecycleMgr() {
        return this.lifecycleMgr;
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
     * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgr#getResourceClasses(com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec)
     */
    public Set getResourceClasses(final IResourceClassMgrQuerySpec querySpec) throws DataSourceException {
        return getResourceClasses(querySpec, new Date());
    }

    /**
     * Returns the matching resource classes
     * 
     * @param querySpec
     *            query specification
     * @param asOf
     *            date as of which the resource class should be pulled
     * @return the list of matching resource classes
     */
    protected Set getResourceClasses(final IResourceClassMgrQuerySpec querySpec, final Date asOf) {
        Set results = new TreeSet();
        //For now, support only "starts with" style query. We expect the first
        // letter to be passed in, and we expect only one for now
        String startWith = null;
        if (querySpec != null) {
            IResourceClassMgrQueryTerm[] terms = querySpec.getSearchSpecTerms();
            if (terms != null && terms.length > 0) {
                IResourceClassMgrQueryTerm term = terms[0];
                startWith = term.getExpression();
                int starIndex = startWith.indexOf('*');
                if (starIndex > 0) {
                    startWith = startWith.substring(0, starIndex).toLowerCase();
                }
            }
        }

        try {
            LifecycleManager lm = getLifecycleMgr();
             // FIXME: (sergey) This should be more specific at targeting resource components through the use of namespace.
            Collection entities = lm.getAllEntitiesOfType(Arrays.asList(new EntityType[] { EntityType.COMPONENT }));
            Collection deployedEntities = lm.getDeployedEntitiesForEntities(entities, asOf, DeploymentType.PRODUCTION);
            Iterator it = deployedEntities.iterator();
            while (it.hasNext()) {
                DeploymentEntity entity = (DeploymentEntity) it.next();
                final String name = entity.getName();
                if (startWith == null || (startWith != null && name.toLowerCase().startsWith(startWith)) || startWith.equals("*")) {
                    results.add(name);
                }
            }
        } catch (EntityManagementException e) {
            results.clear();
            getLog().error("Error occured when retrieving the resource class entities", e);
        }
        return results;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        //Initializes the PF lifecycle manager
        getLog().debug("Initializing the report execution manager in DAC component...");
        this.lifecycleMgr = (LifecycleManager) getManager().getComponent(LifecycleManager.COMP_INFO);
        getLog().debug("Initialized the report execution manager in DAC component...");
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