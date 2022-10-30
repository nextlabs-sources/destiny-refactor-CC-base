/*
 * Created on Mar 10, 2015
 *
 * All sources, binaries and HTML pages (C) copyright 2015 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dpc/src/java/main/com/nextlabs/destiny/container/dpc/DPCContainerImpl.java#1 $:
 */

package com.nextlabs.destiny.container.dpc;

import com.bluejungle.destiny.container.dcc.DefaultContainerImpl;
import com.bluejungle.destiny.container.dcc.IDCCComponentStarter;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;

public class DPCContainerImpl extends DefaultContainerImpl implements IDCCContainer {
    private IDCCComponentStarter componentStarter;

    /**
     * Constructor
     */
    public DPCContainerImpl() {
        super();
    }

    protected void initComponentStarter(Object lock) {
        HashMapConfiguration compStarterConfig = new HashMapConfiguration();
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_CLASSNAME_CONFIG_PARAM, DPCComponentImpl.class);
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_NAME_CONFIG_PARAM, "DPC");
        // Not used, but DefaultContainerImpl requires it
        compStarterConfig.setProperty(IDCCComponentStarter.HEARTBEAT_MGR_CLASSNAME_CONFIG_PARAM, getHeartbeatMgrClassName());
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_TYPE_CONFIG_PARAM, getConfiguration().get(IDCCContainer.COMPONENT_TYPE_CONFIG_PARAM));
        compStarterConfig.setProperty(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME, getConfiguration().get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME));
        compStarterConfig.setProperty(IDCCComponentStarter.LOCK_OBJECT_CONFIG_PARAM, lock);

        ComponentInfo<IDCCComponentStarter> compStarterInfo =
            new ComponentInfo<IDCCComponentStarter>(DPCComponentStarter.COMP_NAME,
                                                    DPCComponentStarter.class,
                                                    IDCCComponentStarter.class,
                                                    LifestyleType.SINGLETON_TYPE,
                                                    compStarterConfig);

        componentStarter = getManager().getComponent(compStarterInfo);
    }

    @Override
    protected IDCCComponentStarter getComponentStarter() {
        return componentStarter;
    }

    @Override
    protected boolean requiresKeyManager() {
        return false;
    }
}


