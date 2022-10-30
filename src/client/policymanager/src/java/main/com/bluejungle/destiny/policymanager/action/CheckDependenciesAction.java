/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.dialogs.DeployCheckDependenciesDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.SubmitCheckDependenciesDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.Policy;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/action/CheckDependenciesAction.java#15 $
 */

public class CheckDependenciesAction extends BaseDisableableAction {

    /**
     * Constructor
     * 
     */
    public CheckDependenciesAction() {
        super();
    }

    /**
     * Constructor
     * 
     * @param text
     */
    public CheckDependenciesAction(String text) {
        super(text);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param image
     */
    public CheckDependenciesAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public CheckDependenciesAction(String text, int style) {
        super(text, style);
    }

    public void refreshEnabledState(Set selectedItems) {
        boolean newState = !selectedItems.isEmpty();
        Iterator selectedItemsIterator = selectedItems.iterator();
        while ((selectedItemsIterator.hasNext()) && (newState)) {
            DomainObjectDescriptor nextSelectedItem = ((IPolicyOrComponentData) selectedItemsIterator.next()).getDescriptor();
            newState &= !nextSelectedItem.getType().equals(EntityType.FOLDER);
        }

        setEnabled(newState);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        List<DomainObjectDescriptor> objectList = new ArrayList<DomainObjectDescriptor>();
        IHasId domainObject = (IHasId) gs.getCurrentObject();
        DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);
        if (domainObject instanceof Policy) {
            objectList.add(EntityInfoProvider.getPolicyDescriptor(((Policy) domainObject).getName()));
        } else if (domainObject instanceof IDSpec) {
            objectList.add(EntityInfoProvider.getComponentDescriptor(((IDSpec) domainObject).getName()));
        }

        // TODO get states from the right APIs
        if (status == DevelopmentStatus.DRAFT || status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY) {
            // object state is draft
            SubmitCheckDependenciesDialog dlg = new SubmitCheckDependenciesDialog(Display.getCurrent().getActiveShell(), objectList, SubmitCheckDependenciesDialog.CHECK_DEPENDENCIES);
            dlg.open();
        } else if (status == DevelopmentStatus.APPROVED || status == DevelopmentStatus.OBSOLETE) {
            // if object is in submitted state
            DeployCheckDependenciesDialog dlg = new DeployCheckDependenciesDialog(Display.getCurrent().getActiveShell(), objectList);
            dlg.open();
        }
    }
}
