/*
 * Created on May 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;

import java.util.Properties;

/**
 * This class is used to insert comm profile related seed data into the
 * database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/CommProfileSeedDataMgr.java#1 $
 */

public class CommProfileSeedDataMgr extends SeedDataTaskBase {

    private HibernateProfileManager profileMgr;

    /**
     * @see com.bluejungle.destiny.tools.dbinit.ISeedDataTask#execute()
     */
    public void execute() throws SeedDataTaskException {
        try {
            Properties props = getConfiguration().get(ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM);
            this.profileMgr.createDefaultProfiles(props);
        } catch (DataSourceException e) {
            throw new SeedDataTaskException(e);
        }
        getLog().info("ok");
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
                
        ComponentInfo<IAgentManager> agentMgrCompInfo = 
            new ComponentInfo<IAgentManager>(
                IAgentManager.COMP_NAME, 
                AgentManager.class, 
                IAgentManager.class, 
                LifestyleType.SINGLETON_TYPE
        );
        IAgentManager agentMgr = getManager().getComponent(agentMgrCompInfo);

        //Instantiates the profile manager
        HashMapConfiguration compConfig = new HashMapConfiguration();
        ComponentInfo<HibernateProfileManager> compInfo = 
            new ComponentInfo<HibernateProfileManager>(
                IProfileManager.COMP_NAME, 
                HibernateProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.TRANSIENT_TYPE, 
                compConfig
            );
        this.profileMgr = getManager().getComponent(compInfo);        
    }
}