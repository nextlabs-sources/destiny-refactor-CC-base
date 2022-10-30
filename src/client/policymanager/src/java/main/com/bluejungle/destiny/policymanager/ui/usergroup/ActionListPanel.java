/*
 * Created on May 25, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.usergroup;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.destiny.policymanager.ui.dialogs.TitleAreaDialogEx;

/**
 * @author bmeng
 * 
 */
public class ActionListPanel extends ComponentListPanel {

    public ActionListPanel(Composite parent, int style) {
        super(parent, style);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getInputDialog()
     */
    protected TitleAreaDialogEx getInputDialog() {
        return new TitleAreaDialogEx(getShell(), Messages.ACTIONLISTPANEL_ACTION_TITLE, Messages.ACTIONLISTPANEL_ACTION_MSG, Messages.ACTIONLISTPANEL_ACTION_NAME, getNewComponentNameValidator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindString()
     */
    protected String getFindString() {
        return Messages.FIND_STRING;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bluejungle.destiny.policymanager.ui.usergroup.ComponentListPanel#getFindInstructions()
     */
    protected String getFindInstructions() {
        return Messages.FIND_INSTRUCTIONS;
    }

    @Override
    public ComponentEnum getComponentType() {
        return ComponentEnum.ACTION;
    }
}
