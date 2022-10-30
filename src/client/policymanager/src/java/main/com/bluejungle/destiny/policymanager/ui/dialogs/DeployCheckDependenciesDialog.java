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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

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

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl;
import com.bluejungle.destiny.policymanager.ui.controls.DependencyControl.Dependency;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/ui/dialogs/DeployCheckDependenciesDialog.java#3 $
 */

public class DeployCheckDependenciesDialog extends Dialog {

    private static final Point SIZE = new Point(600, 500);
    private List<DomainObjectDescriptor> objectList = null;
    private List<Dependency> missingComponents = new ArrayList<Dependency>();
    private List<Dependency> requiredComponents = new ArrayList<Dependency>();
    private List<Dependency> modifiedComponents = new ArrayList<Dependency>();
    private List<Dependency> ongoingModificationsComponents = new ArrayList<Dependency>();

    /**
     * Constructor
     * 
     * @param parent
     *            parent shell
     * @param objectList
     *            list of objects to check dependencies for
     * 
     */
    public DeployCheckDependenciesDialog(Shell parent, List<DomainObjectDescriptor> objectList) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.objectList = objectList;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_TITLE);
        newShell.setImage(ImageBundle.POLICY_IMG);
        newShell.setSize(SIZE);
        checkDependencies();
    }

    /**
     * checks dependencies of the list objects and generate lists to show in the
     * different sections of the dialog
     */
    private void checkDependencies() {
        List<DomainObjectDescriptor> obsoleteObjects = new ArrayList<DomainObjectDescriptor>();
        List<DomainObjectDescriptor> approvedObjects = new ArrayList<DomainObjectDescriptor>();

        for (int i = 0, n = objectList.size(); i < n; i++) {
            DomainObjectDescriptor descriptor = objectList.get(i);
            if (descriptor.getStatus() == DevelopmentStatus.APPROVED) {
                approvedObjects.add(descriptor);
            } else if (descriptor.getStatus() == DevelopmentStatus.OBSOLETE) {
                obsoleteObjects.add(descriptor);
            }
        }

        List<DomainObjectDescriptor> obsoleteComponents = new ArrayList<DomainObjectDescriptor>(); // add
                                                                                                    // only
                                                                                                    // components
        // (not policies) to
        // this because policies
        // do not have referring
        // objects
        for (int i = 0, n = obsoleteObjects.size(); i < n; i++) {
            DomainObjectDescriptor descriptor = (DomainObjectDescriptor) obsoleteObjects.get(i);
            if (descriptor.getType() != EntityType.POLICY) {
                obsoleteComponents.add(descriptor);
            }
        }

        if (obsoleteComponents.size() > 0) {
            Collection<DomainObjectDescriptor> referringObjects = PolicyServerProxy.getAllReferringObjectsAsOf(obsoleteComponents, new GregorianCalendar().getTime(), true);
            approvedObjects.addAll(referringObjects);

            /*
             * When marking an object obsolete, all policies/components which
             * refer to it are modified to remove that object. Therefore, when
             * this obsolete object is "deployed" to undeploy it, the other,
             * modified objects, are then deployed. Therefore, these
             * policies/component must all be in the approved state and the
             * current user must have rights to deploy them
             */
            Collection<DomainObjectDescriptor> permissableReferringObjects = PolicyServerProxy.filterByAllowedAction(referringObjects, DAction.DEPLOY);
            Iterator<DomainObjectDescriptor> referringObjectIterator = referringObjects.iterator();
            while (referringObjectIterator.hasNext()) {
                DomainObjectDescriptor descriptor = (DomainObjectDescriptor) referringObjectIterator.next();
                if ((!permissableReferringObjects.contains(descriptor)) || (descriptor.getStatus() != DevelopmentStatus.APPROVED)) {
                    missingComponents.add(new Dependency(descriptor, false, false));
                } else {
                    requiredComponents.add(new Dependency(descriptor, false, false));
                }
            }
        }

        Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(approvedObjects);
        Collection<DomainObjectDescriptor> permissableDependencies = PolicyServerProxy.filterByAllowedAction(dependencies, DAction.DEPLOY);
        Collection<DomainObjectDescriptor> dependenciesDeployedDescriptors = PolicyServerProxy.getDeployedDescriptors(dependencies);
        Iterator<DomainObjectDescriptor> iterator = dependencies.iterator();
        while (iterator.hasNext()) {
            DomainObjectDescriptor descriptor = (DomainObjectDescriptor) iterator.next();
            Iterator<DomainObjectDescriptor> deployedIterator = dependenciesDeployedDescriptors.iterator();

            DomainObjectDescriptor deployedDescriptor = null;
            while (deployedIterator.hasNext()) {
                DomainObjectDescriptor tempDescriptor = (DomainObjectDescriptor) deployedIterator.next();
                if (tempDescriptor.getId().equals(descriptor.getId())) {
                    deployedDescriptor = tempDescriptor;
                    break;
                }
            }

            if (deployedDescriptor == null) {
                if ((descriptor.getStatus() != DevelopmentStatus.APPROVED) || (!permissableDependencies.contains(descriptor))) {
                    missingComponents.add(new Dependency(descriptor, false, false));
                } else {
                    requiredComponents.add(new Dependency(descriptor, false, false));
                }
            } else {
                if (descriptor.getStatus() == DevelopmentStatus.APPROVED && deployedDescriptor.getVersion() < descriptor.getVersion()) {
                    modifiedComponents.add(new Dependency(descriptor, false, false));
                } else if (descriptor.getStatus() != DevelopmentStatus.APPROVED) {
                    ongoingModificationsComponents.add(new Dependency(descriptor, false, false));
                }
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

        Label labelHeader = new Label(container, SWT.WRAP);
        labelHeader.setFont(FontBundle.TWELVE_POINT_ARIAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelHeader.setLayoutData(data);

        Label labelBody = new Label(container, SWT.WRAP);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelBody.setLayoutData(data);
        // Set the header labels.
        if (this.missingComponents.size() > 0) {
            iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
            labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_HEADER);
            labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_BODY);
        } else if (this.requiredComponents.size() > 0 || this.modifiedComponents.size() > 0) {
            iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_WARNING));
            labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_HEADER);
            labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_MODIFIED_BODY);
        } else {
            iconLabel.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
            labelHeader.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_HEADER);
            labelBody.setText(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_SAFE_BODY);
        }

        DependencyControl dc = new DependencyControl(container, SWT.NONE, false);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        dc.setLayoutData(data);
        dc.setLabel(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_CHECKED_FOR);
        List<String> nameList = new ArrayList<String>();
        for (int i = 0; i < objectList.size(); i++) {
            DomainObjectDescriptor descriptor = (DomainObjectDescriptor) this.objectList.get(i);
            nameList.add(DomainObjectHelper.getDisplayName(descriptor));
        }
        dc.setNames(nameList);

        dc.addSection(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENTS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK), DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MISSING_COMPONENT_INFO,
                missingComponents);
        dc.addSection(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_REQUIRED_COMPONENTS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK), DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_NEW_INFO, requiredComponents);
        dc.addSection(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_COMPONENTS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_WARN_TSK), DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_MODIFIED_INFO, modifiedComponents);
        dc.addSection(DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATIONS, PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_INFO_TSK), DialogMessages.DEPLOYCHECKDEPENDENCIESDIALOG_ONGOING_MODIFICATION_INFO,
                ongoingModificationsComponents);
        dc.initialize();

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
}