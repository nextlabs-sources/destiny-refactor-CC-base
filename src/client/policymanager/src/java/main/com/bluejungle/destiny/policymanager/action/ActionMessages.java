/*
 * Created on Jan 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import org.eclipse.osgi.util.NLS;

/**
 * @author bmeng
 */

public class ActionMessages extends NLS {

    private static final String BUNDLE_NAME = "com.bluejungle.destiny.policymanager.action.messages";//$NON-NLS-1$

    static {
        // load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
    }

    private ActionMessages() {
    }

    public static String ACTION_SAVE, ACTION_SET_DEPLOYMENT_TARGET, ACTION_CHECK_DEPENDENCIES, ACTION_SUBMIT, ACTION_DEPLOY, ACTION_DEPLOYMENT_HISTORY, ACTION_PROPERTIES, ACTION_MODIFY, ACTION_DEACTIVATE, ACTION_DELETE, ACTION_SHOW_DEPLOYED_VERSION,
            ACTION_SHOW_POLICY_USAGE, ACTION_DEPLOY_ALL, ACTION_DEPLOYMENT_STATUS, ACTION_VERSION_HISTORY, ACTION_ABOUT, ACTION_CHANGE_PASSWORD, ACTION_DISPLAY_HELP, ACTION_UPDATE_COMPUTERS, ACTION_COPY, ACTION_PASTE;

    public static String DEPLOYALLACTION_DEPLOY_ALL, DEPLOYALLACTION_NO_POLICIES;

    public static String SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED, SHOWDEPLOYEDVERSIONACTION_NO_DEPLOYED_MSG;

    public static String SHOWHELPACTION_HELP, SHOWHELPACTION_FAIL_LOCATE;
}
