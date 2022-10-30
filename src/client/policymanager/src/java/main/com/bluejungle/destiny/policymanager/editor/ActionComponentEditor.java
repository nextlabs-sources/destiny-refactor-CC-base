/*
 * Created on May 31, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.UserProfileEnum;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.ui.ActionComponentUndoElement;
import com.bluejungle.destiny.policymanager.ui.ActionComponentUndoElementOp;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author dstarke
 */
public class ActionComponentEditor extends ComponentEditor {

    private Map<IDAction, Button> checkboxActionMap = new HashMap<IDAction, Button>();
    private List<Button> checkboxList = new ArrayList<Button>();

    private Button buttonRun, buttonOpen, buttonDelete, buttonMove, buttonRename, buttonChangeAttributes, buttonFilePermissions;
    private Button buttonCreateEdit, buttonCopy, buttonPaste, buttonPrint, buttonExport, buttonAttachToItem;
    private Button buttonEmail, buttonIM, buttonMeet, buttonCall;
    private Label labelCommunicate;

    /**
     * @param parent
     * @param style
     * @param domainObject
     * @param showPropertyExpressions
     */
    public ActionComponentEditor(Composite parent, int style, IDSpec domainObject) {
        super(parent, style, domainObject, false);

        final ObjectModifiedListner objectModifiedListner = new ObjectModifiedListner();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        final IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.registerListener(objectModifiedListner, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, this.domainObject);

        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                eventManager.unregisterListener(objectModifiedListner, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, ActionComponentEditor.this.domainObject);
            }
        });
    }

    protected void updateFromDomainObject() {
        readActionsFromDomainObject();
    }

    protected void initializeMembers() {
        FormLayout layout = new FormLayout();
        membersComposite.setLayout(layout);

        Composite membersLabel = initializeSectionHeading(membersComposite, EditorMessages.ACTIONCOMPONENTEDITOR_ACTIONS);
        FormData data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(0);
        data.right = new FormAttachment(100);
        membersLabel.setLayoutData(data);

        Composite actionSection = new Composite(membersComposite, SWT.NONE);
        data = new FormData();
        data.left = new FormAttachment(0);
        data.top = new FormAttachment(membersLabel);
        actionSection.setLayoutData(data);

        initializeActions(actionSection);
        readActionsFromDomainObject();
    }

    protected void relayoutMembers() {
        readActionsFromDomainObject();
    }

    private void initializeActions(Composite actionSection) {
        CompositePredicate actionDomainObj = getControlDomainObject(CONTROL_ID_COMPOSITION, getDomainObject());
        Set<IDAction> initialState = PredicateHelpers.getActionSet(actionDomainObj);

        Color background = getBackground();
        actionSection.setBackground(background);
        FormLayout sectionLayout = new FormLayout();
        actionSection.setLayout(sectionLayout);

        Composite access = new Composite(actionSection, SWT.NONE);
        access.setBackground(background);
        FormData aData = new FormData();
        aData.left = new FormAttachment(0, 45);
        access.setLayoutData(aData);
        GridLayout layout = new GridLayout();
        access.setLayout(layout);

        Label accessLabel = new Label(access, SWT.NONE);
        accessLabel.setBackground(background);
        accessLabel.setText(EditorMessages.ACTIONCOMPONENTEDITOR_ACCESS);

        buttonOpen = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_OPEN, actionDomainObj, initialState, DAction.OPEN);
        buttonOpen.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonDelete = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_DELETE, actionDomainObj, initialState, DAction.DELETE);
        buttonDelete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonMove = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_MOVE, actionDomainObj, initialState, DAction.MOVE);
        buttonMove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonRename = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_RENAME, actionDomainObj, initialState, DAction.RENAME);
        buttonRename.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonRun = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_RUN, actionDomainObj, initialState, DAction.RUN);
        buttonRun.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonChangeAttributes = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_CHANGE_ATTRIBUTES, actionDomainObj, initialState, DAction.CHANGE_PROPERTIES);
        buttonChangeAttributes.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonFilePermissions = createActionButton(access, EditorMessages.ACTIONCOMPONENTEDITOR_CHANGE_FILE_PERMISSIONS, actionDomainObj, initialState, DAction.CHANGE_SECURITY);
        buttonFilePermissions.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });

        Composite transform = new Composite(actionSection, SWT.NONE);
        transform.setBackground(background);
        FormData tData = new FormData();
        tData.left = new FormAttachment(access);
        transform.setLayoutData(tData);
        layout = new GridLayout();
        transform.setLayout(layout);

        Label transformLabel = new Label(transform, SWT.NONE);
        transformLabel.setBackground(background);
        transformLabel.setText(EditorMessages.ACTIONCOMPONENTEDITOR_TRANSFORM);

        Set<IDAction> createEditSet = new HashSet<IDAction>();
        createEditSet.add(DAction.CREATE_NEW);
        createEditSet.add(DAction.EDIT);
        buttonCreateEdit = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_CREATE_EDIT, actionDomainObj, initialState, createEditSet);
        buttonCreateEdit.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonCopy = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_COPY, actionDomainObj, initialState, DAction.COPY);
        buttonCopy.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonPaste = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_PASTE, actionDomainObj, initialState, DAction.COPY_PASTE);
        buttonPaste.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonPrint = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_PRINT, actionDomainObj, initialState, DAction.PRINT);
        buttonPrint.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonExport = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_EXPORT, actionDomainObj, initialState, DAction.EXPORT);
        buttonExport.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonAttachToItem = createActionButton(transform, EditorMessages.ACTIONCOMPONENTEDITOR_ATTACH_TO_ITEM, actionDomainObj, initialState, DAction.ATTACH);
        buttonAttachToItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });

        Composite communicate = new Composite(actionSection, SWT.NONE);
        communicate.setBackground(background);
        FormData dData = new FormData();
        dData.left = new FormAttachment(transform);
        communicate.setLayoutData(dData);
        layout = new GridLayout();
        communicate.setLayout(layout);

        labelCommunicate = new Label(communicate, SWT.NONE);
        labelCommunicate.setBackground(background);
        labelCommunicate.setText(EditorMessages.ACTIONCOMPONENTEDITOR_COMMUNICATE);

        buttonEmail = createActionButton(communicate, EditorMessages.ACTIONCOMPONENTEDITOR_EMAIL, actionDomainObj, initialState, DAction.EMAIL);
        buttonEmail.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonIM = createActionButton(communicate, EditorMessages.ACTIONCOMPONENTEDITOR_INSTANT_MESSAGE, actionDomainObj, initialState, DAction.IM);
        buttonIM.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonMeet = createActionButton(communicate, EditorMessages.ACTIONCOMPONENTEDITOR_MEET, actionDomainObj, initialState, DAction.MEETING);
        buttonMeet.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });
        buttonCall = createActionButton(communicate, EditorMessages.ACTIONCOMPONENTEDITOR_CALL, actionDomainObj, initialState, DAction.AVD);
        buttonCall.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsStatus();
            }
        });

        // set layout based on user profile
        if (PlatformUtils.getProfile() == UserProfileEnum.PORTAL) {
            buttonChangeAttributes.setVisible(false);
            buttonFilePermissions.setVisible(false);
            buttonPaste.setVisible(false);
            buttonEmail.setVisible(false);
            buttonIM.setVisible(false);
            buttonMeet.setVisible(false);
            buttonCall.setVisible(false);
        } else if (PlatformUtils.getProfile() == UserProfileEnum.FILESYSTEM) {
            buttonAttachToItem.setVisible(false);
            buttonExport.setVisible(false);
        }
    }

    private class ActionCheckboxListener extends SelectionAdapter {

        Set<IDAction> myActions = new HashSet<IDAction>();
        CompositePredicate domainObj;

        public ActionCheckboxListener(CompositePredicate domainObj, IDAction myAction) {
            myActions.add(myAction);
            this.domainObj = domainObj;
        }

        public ActionCheckboxListener(CompositePredicate domainObj, Set<IDAction> actionSet) {
            myActions = actionSet;
            this.domainObj = domainObj;
        }

        public void widgetSelected(SelectionEvent e) {
            Button b = (Button) e.getSource();
            Set<IDAction> actionSet = PredicateHelpers.getActionSet(domainObj);
            ActionComponentUndoElement undo = new ActionComponentUndoElement();
            if (b.getSelection()) {
                actionSet.addAll(myActions);
                PredicateHelpers.updateActionSet(domainObj, actionSet);
                undo.setOp(ActionComponentUndoElementOp.ADD_ACTION);
                undo.setNewValue(myActions.toArray(new IPredicate[myActions.size()]));
            } else {
                actionSet.removeAll(myActions);
                PredicateHelpers.updateActionSet(domainObj, actionSet);
                undo.setOp(ActionComponentUndoElementOp.REMOVE_ACTION);
                undo.setOldValue(myActions.toArray(new IPredicate[myActions.size()]));
            }
            GlobalState.getInstance().addUndoElement(undo);
        }
    }

    private Button createActionButton(Composite parent, String name, CompositePredicate domainObj, Set initialState, IDAction myAction) {
        Set<IDAction> actionSet = new HashSet<IDAction>();
        actionSet.add(myAction);
        return createActionButton(parent, name, domainObj, initialState, actionSet);
    }

    private Button createActionButton(Composite parent, String name, CompositePredicate domainObj, Set initialState, Set<IDAction> myActions) {
        Button button = new Button(parent, SWT.CHECK);
        button.setText(name);
        SelectionListener listener = new ActionCheckboxListener(domainObj, myActions);
        button.addSelectionListener(listener);
        button.setBackground(getBackground());
        for (IDAction action : myActions) {
            checkboxActionMap.put(action, button);
        }
        checkboxList.add(button);
        return button;
    }

    private void readActionsFromDomainObject() {
        Set<IDAction> actionSet = PredicateHelpers.getActionSet(getControlDomainObject(CONTROL_ID_COMPOSITION, getDomainObject()));
        Set<Button> visited = new HashSet<Button>();
        for (Button checkbox : checkboxList) {
            if (checkbox != null && !checkbox.isDisposed()) {
                checkbox.setSelection(false);
            }
        }

        for (IDAction action : actionSet) {
            Button checkbox = (Button) checkboxActionMap.get(action);
            if (checkbox != null && !checkbox.isDisposed() && !visited.contains(checkbox)) {
                checkbox.setSelection(true);
                visited.add(checkbox);
            }
        }

        updateButtonsStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#getSpecType()
     */
    protected SpecType getSpecType() {
        return SpecType.ACTION;
    }

    public CompositePredicate getControlDomainObject(int controlId, IHasId domainObject) {
        return (CompositePredicate) ((IDSpec) domainObject).getPredicate();
    }

    protected String getMemberLabel() {
        return EditorMessages.ACTIONCOMPONENTEDITOR_ACTIONS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#getObjectTypeLabelText()
     */
    public String getObjectTypeLabelText() {
        return EditorMessages.ACTIONCOMPONENTEDITOR_ACTION_COMPONENT;
    }

    // -------------------------
    // Irrelevant Methods
    // -------------------------

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.EditorPanel#getEntityType()
     */
    protected EntityType getEntityType() {
        return EntityType.ACTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyOperatorList()
     */
    protected List<String> getPropertyOperatorList() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#getPropertyList()
     */
    protected List<String> getPropertyList() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.editor.ComponentEditor#setupPreviewTable(org.eclipse.swt.widgets.Table)
     */
    protected void setupPreviewTable(Table table) {
    }

    /**
     * @author sgoldstein
     */
    public class ObjectModifiedListner implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            updateFromDomainObject();
        }
    }

    @Override
    protected ComponentEnum getComponentType() {
        return ComponentEnum.ACTION;
    }

    private void updateButtonsStatus() {
        CompositePredicate actionDomainObj = getControlDomainObject(CONTROL_ID_COMPOSITION, getDomainObject());
        Set<IDAction> actions = PredicateHelpers.getActionSet(actionDomainObj);
        if (isDisposed()) {
            return;
        }

        if (actions.contains(DAction.RUN)) {
            buttonOpen.setEnabled(false);
            buttonDelete.setEnabled(false);
            buttonMove.setEnabled(false);
            buttonRename.setEnabled(false);

            buttonCreateEdit.setEnabled(false);
            buttonCopy.setEnabled(false);

            buttonPrint.setEnabled(false);
        } else {
            buttonOpen.setEnabled(true);
            buttonDelete.setEnabled(true);
            buttonMove.setEnabled(true);
            buttonRename.setEnabled(true);

            buttonCreateEdit.setEnabled(true);
            buttonCopy.setEnabled(true);

            buttonPrint.setEnabled(true);
        }

        if (actions.contains(DAction.RUN) || actions.contains(DAction.EXPORT) || actions.contains(DAction.ATTACH)) {
            buttonChangeAttributes.setEnabled(false);
            buttonFilePermissions.setEnabled(false);

            buttonPaste.setEnabled(false);

            buttonEmail.setEnabled(false);
            buttonIM.setEnabled(false);
            buttonMeet.setEnabled(false);
            buttonCall.setEnabled(false);
        } else {
            buttonChangeAttributes.setEnabled(true);
            buttonFilePermissions.setEnabled(true);

            buttonPaste.setEnabled(true);

            buttonEmail.setEnabled(true);
            buttonIM.setEnabled(true);
            buttonMeet.setEnabled(true);
            buttonCall.setEnabled(true);
        }

        if (actions.contains(DAction.RUN) || actions.contains(DAction.CHANGE_PROPERTIES) || actions.contains(DAction.CHANGE_SECURITY) || actions.contains(DAction.COPY_PASTE) || actions.contains(DAction.EMAIL) || actions.contains(DAction.IM)
                || actions.contains(DAction.MEETING) || actions.contains(DAction.AVD)) {
            buttonExport.setEnabled(false);
            buttonAttachToItem.setEnabled(false);
        } else {
            buttonExport.setEnabled(true);
            buttonAttachToItem.setEnabled(true);
        }

        if (!actions.contains(DAction.RUN) && actions.size() > 0) {
            buttonRun.setEnabled(false);
        } else {
            buttonRun.setEnabled(true);
        }

        for (Button button : checkboxList) {
            button.setEnabled(button.getEnabled() && isEditable());
        }
    }
}
