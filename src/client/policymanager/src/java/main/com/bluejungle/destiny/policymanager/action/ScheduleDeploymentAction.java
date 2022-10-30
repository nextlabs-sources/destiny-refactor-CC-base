/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.ScheduleDeploymentDialog;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/action/CheckDependenciesAction.java#2 $
 */

public class ScheduleDeploymentAction extends BaseDisableableAction {

    /**
     * Constructor
     * 
     */
    public ScheduleDeploymentAction() {
        super();
    }

    /**
     * Constructor
     * 
     * @param text
     */
    public ScheduleDeploymentAction(String text) {
        super(text);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param image
     */
    public ScheduleDeploymentAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public ScheduleDeploymentAction(String text, int style) {
        super(text, style);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        List<DomainObjectDescriptor> objectList = new ArrayList<DomainObjectDescriptor>();
        IHasId domainObject = (IHasId) gs.getCurrentObject();
        if (domainObject instanceof IDPolicy) {
            String name = ((IDPolicy) domainObject).getName();
            DomainObjectDescriptor descriptor = EntityInfoProvider.getPolicyDescriptor(name);
            objectList.add(descriptor);
        } else if (domainObject instanceof IDSpec) {
            String name = ((IDSpec) domainObject).getName();
            DomainObjectDescriptor descriptor = EntityInfoProvider.getComponentDescriptor(name);
            objectList.add(descriptor);
        }

        ScheduleDeploymentDialog dlg = new ScheduleDeploymentDialog(Display.getCurrent().getActiveShell(), objectList, PolicyServerProxy.getNextDeploymentTime());
        dlg.open();
    }

    public void refreshEnabledState(Set selectedItems) {
        boolean newState = false;
        if (selectedItems.size() == 1) {
            IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems.iterator().next();
            IHasId selectedEntity = selectedItem.getEntity();

            if (PolicyServerProxy.canPerformAction(selectedEntity, DAction.DEPLOY)) {

                DevelopmentStatus status = DomainObjectHelper.getStatus(selectedEntity);
                if (status == DevelopmentStatus.APPROVED) {
                    newState = true;
                } else if (status == DevelopmentStatus.OBSOLETE) {
                    try {
                        DomainObjectUsage entityUsage = selectedItem.getEntityUsage();
                        Long currentlyDeployedVersion = entityUsage.getCurrentlydeployedvcersion();
                        if (currentlyDeployedVersion != null) {
                            newState = true;
                        }
                    } catch (PolicyEditorException exception) {
                        LoggingUtil.logWarning(Activator.ID, "Failed to get the currently deployed version.  ScheduleDeploymentAciton menu will be disabled.", exception);
                    }
                }
            }
        }
        setEnabled(newState);
    }
}