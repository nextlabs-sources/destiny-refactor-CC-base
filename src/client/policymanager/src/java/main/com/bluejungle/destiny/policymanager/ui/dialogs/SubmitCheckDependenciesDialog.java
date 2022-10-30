/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/ui/dialogs/SubmitCheckDependenciesDialog.java#1 $
 */

public class SubmitCheckDependenciesDialog extends Dialog {

    public static final String CHECK_DEPENDENCIES = "CHECK_DEPENDENCIES";
    public static final String SUBMIT = "SUBMIT";

    private static final IEventManager EVENT_MANAGER;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        EVENT_MANAGER = (IEventManager) componentManager.getComponent(IEventManager.COMPONENT_NAME);
    }

    private static final Point SIZE = new Point(600, 500);
    private List<DomainObjectDescriptor> objectList = null;
    private List<DomainObjectDescriptor> requiredComponents = new ArrayList<DomainObjectDescriptor>();
    private List<DomainObjectDescriptor> modifiedComponents = new ArrayList<DomainObjectDescriptor>();
    private String dialogType = null;
    private DependencyControl dependencyControl;

    /**
     * Constructor
     * 
     * @param parent
     *            parent shell
     * @param objectList
     *            list of objects to check dependencies for
     * 
     */
    public SubmitCheckDependenciesDialog(Shell parent, List<DomainObjectDescriptor> objectList, String dialogType) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.objectList = objectList;
        this.dialogType = dialogType;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (dialogType.equals(CHECK_DEPENDENCIES))
            newShell.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_TITLE_CHECK);
        else
            newShell.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_TITLE_SUBMIT);
        newShell.setSize(SIZE);
        newShell.setImage(ImageBundle.POLICY_IMG);
        checkDependencies();
    }

    /**
     * checks dependencies of the list objects
     */
    private void checkDependencies() {
        Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(objectList);
        Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy.getDeployedDescriptors(dependencies);
        for (DomainObjectDescriptor descriptor : dependencies) {
            DomainObjectDescriptor deployedDescriptor = null;
            for (DomainObjectDescriptor tempDescriptor : dependenciesDeployedDescriptors) {
                if (tempDescriptor.getId().equals(descriptor.getId())) {
                    deployedDescriptor = tempDescriptor;
                    break;
                }
            }

            if (descriptor.getStatus() != DevelopmentStatus.APPROVED && deployedDescriptor == null) {
                requiredComponents.add(descriptor);
            } else if (descriptor.getStatus() == DevelopmentStatus.DRAFT) {
                modifiedComponents.add(descriptor);
            }
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite root = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(root, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(data);

        CLabel iconLabel = new CLabel(container, SWT.NONE);
        iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));

        Label labelHeader = new Label(container, SWT.WRAP);
        labelHeader.setFont(FontBundle.TWELVE_POINT_ARIAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelHeader.setLayoutData(data);

        Label labelBody = new Label(container, SWT.WRAP);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelBody.setLayoutData(data);

        if (objectList.size() == 1) {
            if (dialogType.equals(CHECK_DEPENDENCIES)) {
                if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_BODY);
                } else {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_MODIFIED_BODY);
                }
            } else {
                if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SINGLE_UNMODIFIED_BODY);
                } else {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_SINGLE_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_BODY);
                }
            }
        } else {
            if (dialogType.equals(CHECK_DEPENDENCIES)) {
                if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_BODY);
                } else {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_MODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_MODIFIED_BODY);
                }
            } else {
                if (modifiedComponents.size() == 0 && requiredComponents.size() == 0) {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MULTIPLE_UNMODIFIED_BODY);
                } else {
                    labelHeader.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_MULTIPLE_HEADER);
                    labelBody.setText(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_SUBMIT_BODY);
                }
            }
        }

        boolean isCheckDependenciesDialog = dialogType.equals(CHECK_DEPENDENCIES);
        dependencyControl = new DependencyControl(container, SWT.NONE, !isCheckDependenciesDialog);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        dependencyControl.setLayoutData(data);
        if (isCheckDependenciesDialog) {
            dependencyControl.setLabel(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_CHECKED_FOR);
        } else {
            dependencyControl.setLabel(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_OBJECTS_TO_SUBMIT);
        }

        List<String> nameList = new ArrayList<String>();
        for (DomainObjectDescriptor descriptor : objectList) {
            nameList.add(DomainObjectHelper.getDisplayName(descriptor));
        }
        dependencyControl.setNames(nameList);

        List<DependencyControl.Dependency> requiredDependencies = buildDependenciesList(requiredComponents);
        dependencyControl.addSection(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
                DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS_DESC, requiredDependencies);

        List<DependencyControl.Dependency> modifiedDependencies = buildDependenciesList(modifiedComponents);
        dependencyControl.addSection(DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK),
                DialogMessages.SUBMITCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS_DESC, modifiedDependencies);

        dependencyControl.initialize();

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        boolean isCheckDependenciesDialog = dialogType.equals(CHECK_DEPENDENCIES);

        if (isCheckDependenciesDialog) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.OK_LABEL, true);
        } else {
            createButton(parent, IDialogConstants.OK_ID, DialogMessages.LABEL_SUBMIT, true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        }
    }

    @Override
    protected void okPressed() {
        submit();
        super.okPressed();
    }

    /**
     * submits objects along with the selected objects.
     */
    protected void submit() {
        List<DomainObjectDescriptor> submitList = new ArrayList<DomainObjectDescriptor>(objectList);
        submitList.addAll(dependencyControl.getSelection());
        Collection<? extends IHasId> objectsToSubmit = PolicyServerProxy.getEntitiesForDescriptor(submitList);

        objectsToSubmit = PolicyServerProxy.getEditedEntitiesMatching(objectsToSubmit);

        for (IHasId item : objectsToSubmit) {
            DomainObjectHelper.setStatus(item, DevelopmentStatus.APPROVED);
        }

        PolicyServerProxy.saveEntities(objectsToSubmit);
        EntityInfoProvider.refreshDescriptors(submitList);

        Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
        for (IHasId item : objectsToSubmit) {
            PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent(item);
            eventsToFire.add(objectModifiedEvent);
        }

        EVENT_MANAGER.fireEvent(eventsToFire);
    }

    /**
     * Build the dependency list to be passed to the Dependency Control
     * 
     * @return the dependency list to be passed to the Dependency Control
     */
    private List<DependencyControl.Dependency> buildDependenciesList(List<DomainObjectDescriptor> dependentComponents) {
        if (dependentComponents == null) {
            throw new NullPointerException("dependentComponents cannot be null.");
        }

        Collection selectableDependentComponents;
        if (SUBMIT.equals(dialogType)) {
            selectableDependentComponents = PolicyServerProxy.filterByAllowedAction(dependentComponents, DAction.APPROVE);
        } else {
            selectableDependentComponents = Collections.EMPTY_SET;
        }

        List<DependencyControl.Dependency> dependenciesList = new ArrayList<DependencyControl.Dependency>();
        for (DomainObjectDescriptor nextDependentComponent : dependentComponents) {
            boolean isSelectable = selectableDependentComponents.contains(nextDependentComponent);
            DependencyControl.Dependency nextDependency = new DependencyControl.Dependency(nextDependentComponent, isSelectable, isSelectable);
            dependenciesList.add(nextDependency);
        }
        return dependenciesList;
    }
}
