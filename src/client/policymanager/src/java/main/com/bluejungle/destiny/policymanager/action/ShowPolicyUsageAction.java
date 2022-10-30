/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.SampleView;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;

/**
 * @author dstarke
 * 
 */
public class ShowPolicyUsageAction extends BaseDisableableAction {

    /**
     * Constructor
     * 
     */
    public ShowPolicyUsageAction() {
        super();
    }

    /**
     * Constructor
     * 
     * @param text
     */
    public ShowPolicyUsageAction(String text) {
        super(text);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param image
     */
    public ShowPolicyUsageAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public ShowPolicyUsageAction(String text, int style) {
        super(text, style);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        IHasId currentObject = gs.getCurrentObject();
        String name = ((IDSpec) currentObject).getName();
        EntityType entityType = EntityType.COMPONENT;

        ((SampleView) gs.getView()).setListView(EntityType.POLICY);
        gs.getPolicyListPanel().setupPolicyUsageFilterControl(name, entityType);
    }

    public void refreshEnabledState(Set selectedItems) {
        boolean newState = false;
        if (selectedItems.size() == 1) {
            IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems.iterator().next();
            IHasId selectedEntity = selectedItem.getEntity();
            newState = (!(selectedEntity instanceof IDPolicy) && !(selectedEntity instanceof PolicyFolder));
        }

        setEnabled(newState);
    }

}
