/*
 * Created on Sep 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2005 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;
import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.epicenter.common.ISpec;

/**
 * @author aweber
 */
public class DomainObjectEditor extends EditorPart implements IReusableEditor {

    private Composite container;
    private EditorPanel editorPanel;
    private DomainObjectInput input;
    private boolean doNotSave = false;
    private IEditorSite site;
    private IContextualEventListener objectModifiedListener;
    private boolean isDisposed = false;

    public DomainObjectEditor() {
        objectModifiedListener = new ObjectModifiedListener();
    }

    public void init(IEditorSite aSite, IEditorInput input) {
        this.site = aSite;
        setInput(input);

        IActionBars actionBars = aSite.getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), PolicyManagerActionFactory.getUndoAction());
        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), PolicyManagerActionFactory.getRedoAction());
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), PolicyManagerActionFactory.getCopyAction());
        actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), PolicyManagerActionFactory.getCutAction());
        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), PolicyManagerActionFactory.getPasteAction());
        actionBars.setGlobalActionHandler(ActionFactory.SAVE.getId(), PolicyManagerActionFactory.getSaveAction());

        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getUndoAction());
        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getRedoAction());
        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getCopyAction());
        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getCutAction());
        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getPasteAction());
        aSite.getKeyBindingService().registerAction(PolicyManagerActionFactory.getSaveAction());
    }

    public void dispose() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
        eventManager.unregisterListener(this.objectModifiedListener, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, getDomainObject());
        // Scott's fix for locking - start
        saveEditor();
        isDisposed = true;
        super.dispose();
        releaseLockOnDomainObject();
        // Scott's fix for locking - end
    }

    private void updateTitle() {
        setPartName(input.getName());
        setTitleImage(ObjectLabelImageProvider.getImage(getDomainObject()));
    }

    public IWorkbenchPartSite getSite() {
        return (IWorkbenchPartSite) site;
    }

    public IEditorSite getEditorSite() {
        return site;
    }

    public IHasId getDomainObject() {
        // Scott's fix for locking - start
        IHasId objectToReturn = null;
        if (input != null) {
            objectToReturn = input.getDomainObject();
        }
        return objectToReturn;
        // return input.getDomainObject();
        // Scott's fix for locking - end
    }

    public IEditorPanel getEditorPanel() {
        return editorPanel;
    }

    public void createPartControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        createEditorPanel();
    }

    // Scott's fix for locking - start
    // public void autoSave() {
    // if (!doNotSave) {
    // editorPanel.saveContents();
    // }
    // }
    public void saveEditor() {
        try {
            if ((!doNotSave) && (PolicyServerProxy.hasLock(getDomainObject()))) {
                editorPanel.saveContents();
            }
        } catch (PolicyEditorException exception) {
            LoggingUtil.logError(Activator.ID, "Failed to check lock status. Changes may be lost.", exception);
        }
    }

    // Scott's fix for locking - end

    public void doSave(IProgressMonitor monitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

    public Class getPreviewClass() {
        return editorPanel.getPreviewClass();
    }

    public Display getDisplay() {
        return editorPanel.getDisplay();
    }

    public void setInput(IEditorInput input) {
        if (input == null) {
            throw new NullPointerException("input cannot be null.");
        }

        if (!(input instanceof DomainObjectInput)) {
            throw new IllegalArgumentException("Unrecognized input: " + input.getClass().toString());
        }

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IEventManager eventManager = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);

        // Scott's fix for locking - start
        // GlobalState globalState = GlobalState.getInstance();
        // if (this.input != null) {
        // IHasId currentDomainObject = getDomainObject();
        IHasId currentDomainObject = getDomainObject();
        if (currentDomainObject != null) {
            // Scott's fix for locking - end
            eventManager.unregisterListener(this.objectModifiedListener, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, currentDomainObject);
        }

        if (editorPanel != null) {
            editorPanel.dispose();
            editorPanel = null;
        }

        releaseLockOnDomainObject();

        this.input = (DomainObjectInput) input;
        IHasId domainObject = this.input.getDomainObject();
        if (domainObject instanceof ISpec) {
            EntityInfoProvider.loadLeafObjectsForComponent((ISpec) domainObject);
        }
        super.setInput(this.input);

        eventManager.registerListener(this.objectModifiedListener, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, getDomainObject());
        GlobalState globalState = GlobalState.getInstance();
        globalState.setOpenedEntity(getDomainObject());

        if (container != null) {
            createEditorPanel();
        }
    }

    /**
     * Create the Editor Panel to hold the domain object
     */
    private void createEditorPanel() {
        IHasId domainObject = getDomainObject();
        if ((editorPanel = EditorPanelFactory.getEditorPanel(container, SWT.NONE, domainObject)) != null) {
            // Scott's fix for locking - start
            // editorPanel.setEditable(DomainObjectHelper.isEditable(domainObject));
            boolean editable = DomainObjectHelper.isEditable(domainObject);
            if (editable) {
                try {
                    PolicyServerProxy.acquireLock(domainObject);
                } catch (PolicyEditorException exception) {
                    editable = false;
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(), EditorMessages.DOMAINOBJECTEDITOR_OBJECT_LOCKED, EditorMessages.DOMAINOBJECTEDITOR_OBJECT_LOCKED_MSG);
                }
            }
            editorPanel.setEditable(editable);
            // Scott's fix for locking - end
            editorPanel.initialize();
            container.layout();
        }

        updateTitle();
    }

    public void stopAutoSaves() {
        doNotSave = true;
    }

    /**
     * Release the current lock on the domain editor object
     */
    private void releaseLockOnDomainObject() {
        try {
            IHasId currentDomainObject = getDomainObject();
            if (currentDomainObject != null) {
                PolicyServerProxy.releaseLock(getDomainObject());
            }
        } catch (PolicyEditorException exception) {
            // Not much we can do here by log, unforunately
            LoggingUtil.logError(Activator.ID, "Failed to release lock on object with id, " + getDomainObject().getId(), exception);
        }
    }

    /**
     * @author sgoldstein
     */
    public class ObjectModifiedListener implements IContextualEventListener {

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IContextualEvent event) {
            // Events are executed asynchronously. Therefore, it's possibly that
            // an event is queued, this part gets disposed, and then the event
            // is fired. Therefore, we have this check
            if (!DomainObjectEditor.this.isDisposed) {
                GlobalState globalState = GlobalState.getInstance();

                PolicyOrComponentModifiedEvent objectModifiedEvent = (PolicyOrComponentModifiedEvent) event;
                IPolicyOrComponentData eventContext = objectModifiedEvent.getEventContextAsPolicyOrComponentData();
                IHasId newDomainObject = eventContext.getEntity();
                if ((!this.equals(globalState.getActiveEditor())) || (DomainObjectHelper.isEditable(newDomainObject) != DomainObjectEditor.this.editorPanel.isEditable())) {
                    /*
                     * Either a change to another object has changed this one or
                     * the user has changed the state of object which changes
                     * whether the editor should be editable or not (e.g. from
                     * Approved to Draft)
                     */
                    DomainObjectInput newDomainObjectInput = new DomainObjectInput(newDomainObject);
                    DomainObjectEditor.this.setInput(newDomainObjectInput);
                }
            }
        }
    }
}
