/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatusManager;
import com.bluejungle.framework.comp.IInitializable;

/**
 * This is the implementation class for the target status manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/TargetStatusManagerImpl.java#1 $
 */

public class TargetStatusManagerImpl implements ITargetStatusManager, IInitializable {

    //Definition of the various target deployment status
    private static final String DEPLOYMENT_NOT_STARTED_NAME = "NotStarted";
    private static final int DEPLOYMENT_NOT_STARTED_TYPE = 1;

    private static final String DEPLOYMENT_IN_PROGRESS_NAME = "InProgress";
    private static final int DEPLOYMENT_IN_PROGRESS_TYPE = 2;

    private static final String DEPLOYMENT_SUCCEEDED_NAME = "Succeeded";
    private static final int DEPLOYMENT_SUCCEEDED_TYPE = 3;

    private static final String DEPLOYMENT_FAILED_NAME = "Failed";
    private static final int DEPLOYMENT_FAILED_TYPE = 4;

    private static Map targetStatus = new HashMap();

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatusManager#getTargetStatus(java.lang.String)
     */
    public ITargetStatus getTargetStatus(String name) {
        if (!targetStatusExists(name)) {
            throw new IllegalArgumentException("Deployment target status with name, " + name + ", does not exist.");
        }

        return (ITargetStatus) targetStatus.get(name);
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        ITargetStatus notStarted = new TargetDeploymentStatusImpl(DEPLOYMENT_NOT_STARTED_NAME, DEPLOYMENT_NOT_STARTED_TYPE);
        targetStatus.put(DEPLOYMENT_NOT_STARTED_NAME, notStarted);

        ITargetStatus inProgress = new TargetDeploymentStatusImpl(DEPLOYMENT_IN_PROGRESS_NAME, DEPLOYMENT_IN_PROGRESS_TYPE);
        targetStatus.put(DEPLOYMENT_IN_PROGRESS_NAME, inProgress);

        ITargetStatus succeeded = new TargetDeploymentStatusImpl(DEPLOYMENT_SUCCEEDED_NAME, DEPLOYMENT_SUCCEEDED_TYPE);
        targetStatus.put(DEPLOYMENT_SUCCEEDED_NAME, succeeded);

        ITargetStatus failed = new TargetDeploymentStatusImpl(DEPLOYMENT_FAILED_NAME, DEPLOYMENT_FAILED_TYPE);
        targetStatus.put(DEPLOYMENT_FAILED_NAME, failed);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatusManager#targetStatusExists(java.lang.String)
     */
    public boolean targetStatusExists(String name) {
        return targetStatus.containsKey(name);
    }

    private class TargetDeploymentStatusImpl implements ITargetStatus {

        private String name;
        private int type;
        private Long id;

        /**
         * Constructor
         * 
         * @param name
         *            name of the deployment status
         * @param type
         *            type of the deployment status
         */
        public TargetDeploymentStatusImpl(String name, int type) {
            super();
            this.name = name;
            this.type = type;
            this.id = new Long(type);
        }

        /**
         * @see com.bluejungle.framework.patterns.IEnum#getName()
         */
        public String getName() {
            return this.name;
        }

        /**
         * @see com.bluejungle.framework.patterns.IEnum#getType()
         */
        public int getType() {
            return this.type;
        }

        /**
         * @see com.bluejungle.framework.domain.IHasId#getId()
         */
        public Long getId() {
            return this.id;
        }

        /**
         * @see com.bluejungle.framework.domain.IHasId#setId(java.lang.Long)
         */
        public void setId(Long newId) {
            this.id = newId;
        }
    }
}