package com.nextlabs.destiny.container.dpc;

import com.bluejungle.destiny.container.dcc.DCCComponentStarterImpl;
import com.bluejungle.destiny.container.dcc.IDCCComponentStarter;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This class starts a component without registering it with DMS (for
 * cases, like DPC, where that just isn't necessary) Perhaps this
 * should be a generic "silent component starter" or perhaps
 * DCCComponentStarterImpl can be configurable. For now, we'll have
 * this class.
 */

public class DPCComponentStarter extends DCCComponentStarterImpl
{
    public final static String COMP_NAME = "DPCComponentStarter";

    public DPCComponentStarter() {
        super("DPCComponentStarter");
    }

    @Override
    protected void prepareToStartDCCComponent() {
        // Technically the DPC shouldn't be an IRegisteredDCCComponent, but
        // that interface isn't immediately removable, so we are going to
        // pretend...
        giveLockObject();

        ComponentInfo<IRegisteredDCCComponent> dpcComponentInfo =
            new ComponentInfo<IRegisteredDCCComponent>(
                getConfiguration().get(IDCCComponentStarter.DCC_COMPONENT_NAME_CONFIG_PARAM),
                getConfiguration().get(IDCCComponentStarter.DCC_COMPONENT_CLASSNAME_CONFIG_PARAM),
                IRegisteredDCCComponent.class,
                LifestyleType.SINGLETON_TYPE,
                getConfiguration().get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME));

        getManager().getComponent(dpcComponentInfo);
    }
}
