package com.bluejungle.destiny.policymanager.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;
import com.bluejungle.destiny.policymanager.ui.controls.WindowShade;
import com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.PolicyListPanel;
import com.bluejungle.destiny.policymanager.ui.usergroup.TabFolderPanel;
import com.bluejungle.pf.destiny.lifecycle.EntityType;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart {

    public static final String ID_VIEW = "com.bluejungle.destiny.policymanager.ui.SampleView"; //$NON-NLS-1$

    private SashForm vSash = null;

    private WindowShade windowShade = null;

    public SampleView() {
    }

    public void createPartControl(Composite parent) {
        showUI(parent);

        GlobalState.getInstance().setView(this);
    }

    /**
     * 
     */
    private void showUI(Composite parentControl) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;

        parentControl.setLayout(gridLayout);

        initialize(parentControl);

        setupActions();

        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                EntityInfoProvider.updatePolicyTreeAsync();
                for (ComponentEnum ev : ComponentEnum.values()) {
                    EntityInfoProvider.updateComponentListAsync(ev);
                }
                GlobalState.getInstance().willBecomeVisible();
            }
        });

        // showLoginWindow(parentControl.getShell());
    }

    /**
     * sets up actions, key bindings for the view
     */
    private void setupActions() {
        IActionBars actionBars = getViewSite().getActionBars();
        PolicyManagerActionFactory.getUndoAction().setEnabled(false);
        PolicyManagerActionFactory.getUndoAction().setActionDefinitionId("org.eclipse.ui.edit.undo");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getUndoAction());

        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), PolicyManagerActionFactory.getUndoAction());

        PolicyManagerActionFactory.getRedoAction().setEnabled(false);
        PolicyManagerActionFactory.getRedoAction().setActionDefinitionId("org.eclipse.ui.edit.redo");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getRedoAction());

        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), PolicyManagerActionFactory.getRedoAction());

        PolicyManagerActionFactory.getCopyAction().setEnabled(true);
        PolicyManagerActionFactory.getCopyAction().setActionDefinitionId("org.eclipse.ui.edit.copy");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getCopyAction());

        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), PolicyManagerActionFactory.getCopyAction());

        PolicyManagerActionFactory.getPasteAction().setEnabled(true);
        PolicyManagerActionFactory.getPasteAction().setActionDefinitionId("org.eclipse.ui.edit.paste");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getPasteAction());

        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), PolicyManagerActionFactory.getPasteAction());

        PolicyManagerActionFactory.getCutAction().setEnabled(true);
        PolicyManagerActionFactory.getCutAction().setActionDefinitionId("org.eclipse.ui.edit.cut");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getCutAction());

        actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), PolicyManagerActionFactory.getCutAction());

        PolicyManagerActionFactory.getSaveAction().setActionDefinitionId("org.eclipse.ui.file.save");
        getSite().getKeyBindingService().registerAction(PolicyManagerActionFactory.getSaveAction());

        actionBars.setGlobalActionHandler(ActionFactory.SAVE.getId(), PolicyManagerActionFactory.getSaveAction());
    }

    private void initialize(Composite parent) {
        vSash = new SashForm(parent, SWT.VERTICAL);
        GridData data = new GridData(GridData.FILL_BOTH);
        vSash.setLayoutData(data);

        PolicyListPanel policyPanel = new PolicyListPanel(vSash, SWT.BORDER);
        GlobalState.getInstance().setPolicyListPanel(policyPanel);

        windowShade = new WindowShade(vSash, SWT.BORDER);

        IConfigurationElement[] decls = Platform.getExtensionRegistry().getConfigurationElementsFor("com.bluejungle.destiny.policymanager.layout");
        for (int i = 0; i < decls.length; i++) {
            IConfigurationElement element = decls[i];
            IConfigurationElement[] children = element.getChildren();
            TabFolderPanel tabfolder = null;
            if (children.length > 1) {
                tabfolder = new TabFolderPanel(windowShade, SWT.NONE);
                data = new GridData(GridData.FILL_BOTH);
                tabfolder.setLayoutData(data);
            }
            CTabItem defaultTab = null;
            ComponentListPanel panel = null;
            for (int j = 0, n = children.length; j < n; j++) {
                IConfigurationElement child = children[j];
                String classname = child.getAttribute("class");
                String name = child.getAttribute("name");
                try {
                    Constructor constructor[] = Class.forName(classname).getConstructors();
                    if (n > 1) {
                        panel = (ComponentListPanel) constructor[0].newInstance(new Object[] { tabfolder.getFolder(), SWT.NONE });
                        CTabItem tab = tabfolder.add(name);
                        tab.setControl(panel);
                        if (j == 0) {
                            defaultTab = tab;
                        }
                    } else
                        panel = (ComponentListPanel) constructor[0].newInstance(new Object[] { windowShade, SWT.NONE });
                    data = new GridData(GridData.FILL_BOTH);
                    panel.setLayoutData(data);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (InvalidRegistryObjectException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (children.length > 1) {
                tabfolder.getFolder().setSelection(defaultTab);
                windowShade.addPanel(element.getAttribute("name"), tabfolder);
            } else
                windowShade.addPanel(element.getAttribute("name"), panel);
        }

        windowShade.relayout();
    }

    public void setListView(EntityType type) {
        if (type != EntityType.POLICY) {
            if (type == EntityType.USER) {
                windowShade.setOpenShade(0);
            } else if (type == EntityType.HOST) {
                windowShade.setOpenShade(1);
            } else if (type == EntityType.APPLICATION) {
                windowShade.setOpenShade(2);
            } else if (type == EntityType.RESOURCE) {
                windowShade.setOpenShade(3);
            }
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus() {
    }

}
