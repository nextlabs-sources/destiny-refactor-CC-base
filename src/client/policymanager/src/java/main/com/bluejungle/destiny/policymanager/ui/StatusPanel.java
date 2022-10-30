/*
 * Created on Mar 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 */

public class StatusPanel extends Composite {

    private CLabel selectedLabel;
    private Label statusLabel;
    private Label statusValue;
    private Label deploymentMessage;
    private Composite deploymentMessagePanel;
    private Button modifyButton;
    private Button submitButton;
    private Button deployButton;
    private IHasId currentObject;

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    public StatusPanel(Composite parent, int style) {
        super(parent, style);
        setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        initialize();
        relayout();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.registerListener(new CurrentObjectChangedListener(), EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
        eventManager.registerListener(new CurrentObjectModifiedListener(), EventType.CURRENT_POLICY_OR_COMPONENT_MODIFIED_EVENT);
    }

    /**
     * @param currentObject
     * 
     */
    protected void refresh(IPolicyOrComponentData currentObject) {
        updateStatus(currentObject);
    }

    /**
     * 
     */
    private void initialize() {
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.spacing = 10;
        setLayout(layout);

        Composite buttonPanel = new Composite(this, SWT.NONE);
        buttonPanel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        FormData formData = new FormData();
        formData.top = new FormAttachment(null, 0, SWT.CENTER);
        formData.left = new FormAttachment(25);
        buttonPanel.setLayoutData(formData);
        buttonPanel.setLayout(new FormLayout());

        selectedLabel = new CLabel(this, SWT.LEFT);
        selectedLabel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        FormData fd = new FormData();
        fd.top = new FormAttachment(buttonPanel, 0, SWT.CENTER);
        fd.left = new FormAttachment(0, 0);
        fd.right = new FormAttachment(buttonPanel);
        selectedLabel.setLayoutData(fd);

        statusLabel = new Label(this, SWT.NONE);
        statusLabel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        statusLabel.setBackground(getBackground());
        statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        statusLabel.setText(ApplicationMessages.STATUSPANEL_STATUS);
        formData = new FormData();
        formData.top = new FormAttachment(buttonPanel, 0, SWT.CENTER);
        formData.left = new FormAttachment(buttonPanel);
        statusLabel.setLayoutData(formData);

        statusValue = new Label(this, SWT.NONE);
        // statusValue.setBackground(getBackground());
        statusValue.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));

        formData = new FormData();
        formData.top = new FormAttachment(buttonPanel, 0, SWT.CENTER);
        formData.left = new FormAttachment(statusLabel, 0, SWT.RIGHT);
        statusValue.setLayoutData(formData);

        modifyButton = new Button(buttonPanel, SWT.PUSH);
        modifyButton.setText(ApplicationMessages.STATUSPANEL_MODIFY);
        submitButton = new Button(buttonPanel, SWT.PUSH);
        submitButton.setText(ApplicationMessages.STATUSPANEL_SUBMIT);
        deployButton = new Button(buttonPanel, SWT.PUSH);
        deployButton.setText(ApplicationMessages.STATUSPANEL_DEPLOY);

        modifyButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IAction action = PolicyManagerActionFactory.getModifyAction();
                action.run();
            }
        });
        submitButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IAction action = PolicyManagerActionFactory.getSubmitForDeploymentAction();
                action.run();
            }
        });
        deployButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IAction action = PolicyManagerActionFactory.getScheduleDeploymentAction();
                action.run();
            }
        });

        // form attachment for deploy button never needs to change
        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(modifyButton, 5, SWT.RIGHT);
        deployButton.setLayoutData(formData);

        deploymentMessagePanel = new Composite(this, SWT.NONE);
        deploymentMessagePanel.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        formData = new FormData();
        formData.top = new FormAttachment(buttonPanel, 0, SWT.CENTER);
        formData.left = new FormAttachment(statusValue);
        deploymentMessagePanel.setLayoutData(formData);
        deploymentMessagePanel.setLayout(new FormLayout());

        deploymentMessage = new Label(deploymentMessagePanel, SWT.NONE);
        deploymentMessage.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        deploymentMessage.setLayoutData(formData);

        setNoObjectActive();
        layout();
    }

    private String getLastNameComponent(String aName) {
        int i = aName.lastIndexOf(PQLParser.SEPARATOR);
        if (i != -1) {
            return aName.substring(i + 1);
        } else {
            return aName;
        }
    }

    /**
     * Updates the state of the status panel based on the new object or the new
     * state of the current object
     * 
     * @param currentObjectEventData
     */
    private void updateStatus(IPolicyOrComponentData currentObjectEventData) {
        IHasId newObject = currentObjectEventData.getEntity();
        currentObject = newObject;

        if (currentObject != null) {
            selectedLabel.setText(getLastNameComponent(DomainObjectHelper.getDisplayName(currentObject)));
        } else {
            selectedLabel.setText("");
        }

        DomainObjectDescriptor descriptor = currentObjectEventData.getDescriptor();
        // FIX ME - Need case when there is no active editor!!!
        if (currentObject != null) {
            try {
                DomainObjectUsage entityUsage = currentObjectEventData.getEntityUsage();
                String statusKey = DomainObjectHelper.getDeploymentStatusKey(descriptor, entityUsage);
                String statusText = DomainObjectHelper.getStatusText(statusKey);
                String deploymentText = DomainObjectHelper.getDeploymentText(statusKey);
                if (deploymentText != null && deploymentText.length() != 0) {
                    statusValue.setText(statusText + " (" + deploymentText + ")");
                } else {
                    statusValue.setText(statusText);
                }

                DevelopmentStatus status = DomainObjectHelper.getStatus(currentObject);
                if ((status == DevelopmentStatus.APPROVED) || ((status == DevelopmentStatus.OBSOLETE) && (entityUsage.getCurrentlydeployedvcersion() != null))) {
                    modifyButton.setVisible(true);
                    submitButton.setVisible(false);
                    deployButton.setVisible(true);
                    FormData formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    modifyButton.setLayoutData(formData);
                    formData = new FormData(0, 0);
                    submitButton.setLayoutData(formData);
                } else if ((status == DevelopmentStatus.OBSOLETE) && (entityUsage.getCurrentlydeployedvcersion() == null)) {
                    modifyButton.setVisible(true);
                    submitButton.setVisible(false);
                    deployButton.setVisible(false);
                    FormData formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    modifyButton.setLayoutData(formData);
                } else {
                    modifyButton.setVisible(false);
                    submitButton.setVisible(true);
                    deployButton.setVisible(false);
                    FormData formData = new FormData(0, 0);
                    modifyButton.setLayoutData(formData);
                    formData = new FormData();
                    formData.top = new FormAttachment(0, 0);
                    formData.left = new FormAttachment(0, 0);
                    submitButton.setLayoutData(formData);
                }
            } catch (PolicyEditorException exception) {
                LoggingUtil.logWarning(Activator.ID, "Failed to load object usage for current entity.  Status panel text will not be accurate.", exception);

                statusValue.setText("");
                modifyButton.setVisible(false);
                submitButton.setVisible(false);
                deployButton.setVisible(false);
            }

            modifyButton.setEnabled(PolicyServerProxy.canPerformAction(currentObject, DAction.WRITE));
            submitButton.setEnabled(PolicyServerProxy.canPerformAction(currentObject, DAction.APPROVE));
            deployButton.setEnabled(PolicyServerProxy.canPerformAction(currentObject, DAction.DEPLOY));

        } else {
            setNoObjectActive();
        }

        layout();
    }

    /**
     * Sets the status panel to represent no active object
     */
    private void setNoObjectActive() {
        if (!selectedLabel.isDisposed()) {
            selectedLabel.setText("");
        }
        if (!statusValue.isDisposed()) {
            statusValue.setText("");
        }
        if (!deploymentMessage.isDisposed()) {
            deploymentMessage.setText("");
        }
        if (!modifyButton.isDisposed()) {
            modifyButton.setVisible(false);
        }
        if (!submitButton.isDisposed()) {
            submitButton.setVisible(false);
        }
        if (!deployButton.isDisposed()) {
            deployButton.setVisible(false);
        }
    }

    /**
     * 
     */
    private void relayout() {
    }

    private class CurrentObjectChangedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IEvent event) {
            CurrentPolicyOrComponentChangedEvent currentObjectChangedEvent = (CurrentPolicyOrComponentChangedEvent) event;
            if (currentObjectChangedEvent.currentObjectExists()) {
                IPolicyOrComponentData currentObject = currentObjectChangedEvent.getNewCurrentObject();
                StatusPanel.this.refresh(currentObject);
            } else {
                StatusPanel.this.currentObject = null;
                StatusPanel.this.setNoObjectActive();
            }
        }
    }

    private class CurrentObjectModifiedListener implements IEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IEvent event) {
            CurrentPolicyOrComponentModifiedEvent currentObjectModifiedEvent = (CurrentPolicyOrComponentModifiedEvent) event;
            IPolicyOrComponentData currentObject = currentObjectModifiedEvent.getCurrentObject();
            StatusPanel.this.refresh(currentObject);
        }
    }
}
