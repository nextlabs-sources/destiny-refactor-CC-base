/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ShowDeployedVersionDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author dstarke
 * 
 */
public class ShowDeployedVersionAction extends BaseDisableableAction {

    /**
     * Constructor
     * 
     */
    public ShowDeployedVersionAction() {
        super();
    }

    /**
     * Constructor
     * 
     * @param text
     */
    public ShowDeployedVersionAction(String text) {
        super(text);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param image
     */
    public ShowDeployedVersionAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public ShowDeployedVersionAction(String text, int style) {
        super(text, style);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        IHasId currentObject = (IHasId) gs.getCurrentObject();

        String name;
        DomainObjectDescriptor desc;
        if (currentObject instanceof IDPolicy) {
            name = ((IDPolicy) currentObject).getName();
            desc = EntityInfoProvider.getPolicyDescriptor(name);
        } else {
            name = ((IDSpec) currentObject).getName();
            desc = EntityInfoProvider.getComponentDescriptor(name);
        }

        Object tmp = PolicyServerProxy.getDeployedVersion(desc);
        if (tmp == null) {
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(), ActionMessages.SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED, ActionMessages.SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED_MSG);
            return;
        }
        ShowDeployedVersionDialog window = new ShowDeployedVersionDialog(Display.getCurrent().getActiveShell(), desc);
        window.open();
    }

    /**
     * @see com.bluejungle.destiny.policymanager.action.BaseDisableableAction#refreshEnabledState()
     */
    protected void refreshEnabledState(Set selectedItems) {
        IHasId currentObject = (IHasId) GlobalState.getInstance().getCurrentObject();
        if (currentObject == null) {
            setEnabled(false);
            return;
        }
        DomainObjectDescriptor descriptor;
        if (currentObject instanceof IDPolicy) {
            descriptor = EntityInfoProvider.getPolicyDescriptor(((IDPolicy) currentObject).getName());
        } else {
            descriptor = EntityInfoProvider.getComponentDescriptor(((IDSpec) currentObject).getName());
        }

        boolean enabled = descriptor != null && PolicyServerProxy.getDeployedVersion(descriptor) != null;
        setEnabled(enabled);
    }
}
