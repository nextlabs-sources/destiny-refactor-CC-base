/*
 * Created on Jan 10, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.action.PolicyManagerActionFactory;
import com.bluejungle.destiny.policymanager.action.RedoAction;
import com.bluejungle.destiny.policymanager.action.UndoAction;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;

/**
 * @author bmeng
 */

public class PolicyAuthorActionBarAdvisor extends ActionBarAdvisor {

    private class WorkbenchPartEnabler extends Action implements GlobalState.IPartObserver {

        IWorkbenchWindow workbenchWindow;
        String partID;

        public WorkbenchPartEnabler(IWorkbenchWindow window, String title, String partID) {
            super(title, IAction.AS_CHECK_BOX);
            this.workbenchWindow = window;
            this.partID = partID;
            GlobalState.getInstance().addPartObserver(this);
            setChecked(false);
        }

        private void validateCheckedState() {
            /*
             * We need to perform the validation once the menu is stable
             */
            Display.getCurrent().asyncExec(new Runnable() {

                public void run() {
                    boolean v = false;
                    IWorkbenchPage page = workbenchWindow.getActivePage();
                    if (page != null) {
                        if (page.findView(partID) != null) {
                            v = true;
                        } else {
                            v = false;
                        }
                    } else {
                        LoggingUtil.logWarning(Activator.ID, "page is null on validateCheckedState", null);
                    }
                    setChecked(v);
                }
            });
        }

        public void workbenchInitialized() {
            validateCheckedState();
        }

        public void partOpened(IWorkbenchPart aPart) {
            if (aPart.getSite().getId().equals(partID)) {
                validateCheckedState();
            }
        }

        public void partClosed(IWorkbenchPart aPart) {
            if (aPart.getSite().getId().equals(partID)) {
                validateCheckedState();
            }
        }

        public void run() {
            IWorkbenchPage page = workbenchWindow.getActivePage();
            IViewPart part;

            if ((part = page.findView(partID)) != null) {
                page.hideView(part);
            } else {
                try {
                    page.showView(partID);
                } catch (PartInitException pie) {
                    System.err.println("" + pie);
                }
            }
        }
    }

    public static final String M_ACTION = IWorkbenchActionConstants.MENU_PREFIX + ApplicationMessages.MENU_ACTIONS;
    public static final String M_TOOLS = IWorkbenchActionConstants.MENU_PREFIX + ApplicationMessages.MENU_TOOLS;

    private IWorkbenchAction quitAction;
    private IAction previewAction;

    public PolicyAuthorActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createToolsMenu());
        menuBar.add(createActionMenu());
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
    }

    private MenuManager createToolsMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_TOOLS, M_TOOLS);
        menu.add(PolicyManagerActionFactory.getDeploymentHistoryAction());
        menu.add(PolicyManagerActionFactory.getDeploymentStatusAction());
        return menu;
    }

    private MenuManager createWindowMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_WINDOW, IWorkbenchActionConstants.M_WINDOW);
        menu.add(previewAction);
        return menu;
    }

    private MenuManager createHelpMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_HELP, IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
        // Welcome or intro page would go here
        menu.add(PolicyManagerActionFactory.getShowHelpAction());
        // Tips and tricks page would go here
        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        // About should always be at the bottom
        // To use the real RCP About dialog uncomment these lines
        // menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getShowAboutAction());
        return menu;
    }

    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_FILE, IWorkbenchActionConstants.M_FILE);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        menu.add(PolicyManagerActionFactory.getSaveAction());
        menu.add(PolicyManagerActionFactory.getChangePasswordAction());
        menu.add(PolicyManagerActionFactory.getObjectPropertiesAction());
        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(quitAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
        return menu;
    }

    private MenuManager createActionMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_ACTIONS, M_ACTION);
        menu.add(PolicyManagerActionFactory.getModifyAction());
        menu.add(PolicyManagerActionFactory.getSubmitForDeploymentAction());
        menu.add(PolicyManagerActionFactory.getScheduleDeploymentAction());
        menu.add(PolicyManagerActionFactory.getDeployAllAction());
        menu.add(PolicyManagerActionFactory.getDeactivateAction());
        menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getShowPolicyUsageAction());
        menu.add(PolicyManagerActionFactory.getShowDeployedVersionAction());
        menu.add(PolicyManagerActionFactory.getCheckDependenciesAction());
        menu.add(PolicyManagerActionFactory.getSetTargetsAction());
        menu.add(PolicyManagerActionFactory.getVersionHistoryAction());
        menu.add(new Separator());
        menu.add(PolicyManagerActionFactory.getUpdateComputersWithAgentsAction());
        return menu;
    }

    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager(ApplicationMessages.MENU_EDIT, IWorkbenchActionConstants.M_EDIT);
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

        menu.add(PolicyManagerActionFactory.getUndoAction());
        menu.add(PolicyManagerActionFactory.getRedoAction());
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));

        // menu.add(PolicyManagerActionFactory.getCopyAction());
        // menu.add(PolicyManagerActionFactory.getPasteAction());
        menu.add(PolicyManagerActionFactory.getDeleteAction());
        menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                UndoAction undoAction = (UndoAction) PolicyManagerActionFactory.getUndoAction();
                RedoAction redoAction = (RedoAction) PolicyManagerActionFactory.getRedoAction();
                IHasId current = GlobalState.getInstance().getCurrentObject();
                if (current != null) {
                    undoAction.refreshEnabledState(current);
                    redoAction.refreshEnabledState(current);
                } else {
                    undoAction.setEnabled(false);
                    redoAction.setEnabled(false);
                }
            }

        });
        return menu;
    }

    protected void makeActions(IWorkbenchWindow window) {
        quitAction = ActionFactory.QUIT.create(window);
        previewAction = new WorkbenchPartEnabler(window, ApplicationMessages.TITLE_PREVIEW, PreviewView.ID);
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        // ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        // toolBarManager.add(PolicyManagerActionFactory.getSaveAction());
        // toolBarManager.add(new Separator());
        // toolBarManager.add(undoAction);
        // toolBarManager.add(redoAction);
        // toolBarManager.add(new Separator());
        // coolBar.add(toolBarManager);
    }
}
