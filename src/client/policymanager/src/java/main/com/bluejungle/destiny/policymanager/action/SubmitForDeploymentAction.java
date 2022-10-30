/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.SubmitCheckDependenciesDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/action/CheckDependenciesAction.java#2 $
 */

public class SubmitForDeploymentAction extends BaseDisableableAction {

    /**
     * Constructor
     * 
     */
    public SubmitForDeploymentAction() {
        super();
    }

    /**
     * Constructor
     * 
     * @param text
     */
    public SubmitForDeploymentAction(String text) {
        super(text);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param image
     */
    public SubmitForDeploymentAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public SubmitForDeploymentAction(String text, int style) {
        super(text, style);
    }

    public void run() {
        GlobalState gs = GlobalState.getInstance();
        List<DomainObjectDescriptor> objectList = new ArrayList<DomainObjectDescriptor>();
        IHasId domainObject = (IHasId) gs.getCurrentObject();

        DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);

        // Check consistency
        for (Iterator iter = EffectType.elements().iterator(); iter.hasNext();) {
            EffectType effectType = (EffectType) iter.next();
            if (domainObject instanceof IDPolicy) {
                Collection obligations = ((IDPolicy) domainObject).getObligations(effectType);
                for (Iterator iter2 = obligations.iterator(); iter2.hasNext();) {
                    IDObligation obligation = (IDObligation) iter2.next();
                    if (obligation.getType().equals("notify")) {
                        if (((NotifyObligation) obligation).getEmailAddresses().length() == 0) {
                            MessageDialog.openError(Display.getCurrent().getActiveShell(), "No recipients specified", "There are no recipients specified for one or more Send Email obligations");
                            return;
                        }
                    }
                }
            }
        }

        if (status == DevelopmentStatus.NEW || status == DevelopmentStatus.EMPTY) {
            // TODO: make sure that empty status is removed when appropriate.
            DomainObjectHelper.setStatus(domainObject, DevelopmentStatus.DRAFT);
        }

        if (PolicyServerProxy.saveEntity(domainObject) == null) {
            displayErrorDialog("Error Saving Entity", "The object could not be saved. There may be an error in the object. Please correct the problem and try again. If the problem persists, please contact your System Administrator.");
            return;
        }

        if (domainObject instanceof Policy) {
            Policy policy = (Policy) domainObject;
            objectList.add(EntityInfoProvider.getPolicyDescriptor(policy.getName()));
            // Add validation for policy start and end times. In future, create
            // full validation framework
            IPredicate conditions = policy.getConditions();
            Long startTimeAsLong = extractPolicyStartTime(conditions);
            Long endTimeAsLong = extractPolicyEndtime(conditions);

            long currentTime = System.currentTimeMillis();
            if ((endTimeAsLong != null) && (endTimeAsLong.longValue() < currentTime)) {
                displayErrorDialog("Invalid Policy", "Please verify that the end time condition of the policy falls after the current time.");
                return;
            }

            if ((endTimeAsLong != null) && (startTimeAsLong != null) && (startTimeAsLong.longValue() > endTimeAsLong.longValue())) {
                displayErrorDialog("Invalid Policy", "Please verify that the policy end time condition falls after the start time condition.");
                return;
            }
        } else if (domainObject instanceof IDSpec) {
            // Validation check for action component
            String name = ((IDSpec) domainObject).getName();
            objectList.add(EntityInfoProvider.getComponentDescriptor(name));
        }

        if (status == DevelopmentStatus.NEW || status == DevelopmentStatus.DRAFT || status == DevelopmentStatus.EMPTY) {
            SubmitCheckDependenciesDialog dlg = new SubmitCheckDependenciesDialog(Display.getCurrent().getActiveShell(), objectList, SubmitCheckDependenciesDialog.SUBMIT);
            dlg.open();
        }
    }

    /**
     * Extract the policy end time from the specified conditions
     * 
     * @param conditions
     * @return the policy end time if it exists; null otherwise
     */
    private Long extractPolicyEndtime(IPredicate conditions) {
        Long endTimeAsLong = null;
        Relation endTime = (Relation) PredicateHelpers.getEndTime(conditions);
        if (endTime != null) {
            endTimeAsLong = (Long) endTime.getRHS().evaluate(null).getValue();
        }
        return endTimeAsLong;
    }

    /**
     * Extract the policy start time from the specified conditions
     * 
     * @param conditions
     * @return the policy start time if it exists; null otherwise
     */
    private Long extractPolicyStartTime(IPredicate conditions) {
        Long startTimeAsLong = null;
        Relation startTime = (Relation) PredicateHelpers.getStartTime(conditions);
        if (startTime != null) {
            startTimeAsLong = (Long) startTime.getRHS().evaluate(null).getValue();
        }
        return startTimeAsLong;
    }

    /**
     * Display an error dialog
     * 
     * @param errorMessageSummary
     * @param errorMessageDetail
     */
    private void displayErrorDialog(String errorMessageSummary, String errorMessageDetail) {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), errorMessageSummary, errorMessageDetail);
    }

    public void refreshEnabledState(Set selectedItems) {
        boolean newState = false;
        if (selectedItems.size() == 1) {
            IPolicyOrComponentData selectedItem = (IPolicyOrComponentData) selectedItems.iterator().next();
            IHasId selectedEntity = selectedItem.getEntity();
            if (selectedEntity instanceof PolicyFolder) {
                newState = false;
            } else {
                DevelopmentStatus status = DomainObjectHelper.getStatus(selectedEntity);
                newState = (status == DevelopmentStatus.NEW || status == DevelopmentStatus.DRAFT || status == DevelopmentStatus.EMPTY);
                newState &= PolicyServerProxy.canPerformAction(selectedEntity, DAction.APPROVE);
            }
        }

        setEnabled(newState);
    }
}
