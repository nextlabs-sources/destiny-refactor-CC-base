package com.bluejungle.destiny.policymanager.ui.usergroup;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;

/**
 * @author bmeng
 */
public class PortalListPanel extends ComponentListPanel {

    public PortalListPanel(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
     */
    protected TitleAreaDialogEx getInputDialog() {
        return new TitleAreaDialogEx(getShell(), Messages.PORTALLISTPANEL_PORTAL_TITLE, Messages.PORTALLISTPANEL_PORTAL_MSG, Messages.PORTALLISTPANEL_PORTAL_NAME, getNewComponentNameValidator());
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindInstructions()
     */
    protected String getFindInstructions() {
        return Messages.FIND_INSTRUCTIONS;
    }

    /**
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindString()
     */
    protected String getFindString() {
        return Messages.FIND_STRING;
    }

    @Override
    public ComponentEnum getComponentType() {
        return ComponentEnum.PORTAL;
    }
}
