package com.bluejungle.destiny.container.dps;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 * 
 * @author sergey
 */

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.shared.pf.PolicyEditorServiceImpl;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.nextlabs.destiny.container.shared.utils.DCCComponentHelper;

/**
 * This is the DPS component implementation class
 * 
 * @author sergey
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dps/src/java/main/com/bluejungle/destiny/container/dps/DPSComponent.java#11 $
 */
public class DPSComponent extends BaseDCCComponentImpl {

    /**
     * DPS container initialization
     */
    public void init() {
        setComponentType(ServerComponentType.DPS);
        super.init();

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Install the server subject manager for the Server Manager
        compMgr.registerComponent(ServerSpecManager.COMP_INFO, true);

        // Ensure that the required data source(s) exist(s). These should have
        // been initialized by the BaseDCCComponentImpl based on the
        // configuration file for this component.
        compMgr.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());


        DCCComponentHelper.initSecurityComponents(getManager(), getLog());

        // Policy Editor initialization:
        compMgr.getComponent(PolicyEditorServiceImpl.COMP_INFO);
    }

}
