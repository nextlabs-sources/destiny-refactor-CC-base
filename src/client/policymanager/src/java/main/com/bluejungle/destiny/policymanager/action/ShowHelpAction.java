/*
 * Created on Nov 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.util.PlatformUtils;
import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * Action for displaying Help in policy Author
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/action/ShowHelpAction.java#3 $
 */

public class ShowHelpAction extends Action {

    private static final String HELP_BUNDLE_NAME = "help.HelpLocations";
    private static final String TOC_HELP_URL_BUNDLE_KEY = "policyauthortoc_help_url";
    private static final String DEFINING_COMPONENTS_HELP_URL_BUNDLE_KEY = "definepolicycomponent_help_url";
    private static final String BASE_HELP_DIRECTORY = "/resources";

    /**
     * 
     * Create an instance of ShowHelpAction
     * 
     * @param actionName
     */
    public ShowHelpAction(String actionName) {
        super(actionName);
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        Display currentDisplay = Display.getCurrent();
        Shell browserShell = new Shell(currentDisplay);
        browserShell.setText(ActionMessages.SHOWHELPACTION_HELP);
        browserShell.setImage(ImageBundle.POLICY_IMG);
        browserShell.setLayout(new FillLayout());

        try {
            String helpURL = getHelpURL();
            Browser helpBrowser = new Browser(browserShell, SWT.NONE);
            helpBrowser.setUrl(helpURL);
        } catch (IOException exception) {
            Label errorLabel = new Label(browserShell, SWT.CENTER);
            errorLabel.setText(ActionMessages.SHOWHELPACTION_FAIL_LOCATE);
            errorLabel.setForeground(ResourceManager.getColor(SWT.COLOR_RED));
        }

        browserShell.open();
    }

    /**
     * Retrive the help url for the current context
     * 
     * @return the help url for the current context
     * @throws IOException
     *             if an error occurs while retrieving the Help URL
     */
    private String getHelpURL() throws IOException {
        String urlBundleKey;
        if (GlobalState.getInstance().getActiveEditor() != null) {
            urlBundleKey = DEFINING_COMPONENTS_HELP_URL_BUNDLE_KEY;
        } else {
            urlBundleKey = TOC_HELP_URL_BUNDLE_KEY;
        }

        String helpFileLocationSuffix = BASE_HELP_DIRECTORY + retrieveURLFromBundle(urlBundleKey);
        URL resourceURL = PlatformUtils.getResource(helpFileLocationSuffix);
        return resourceURL.toString();
    }

    /**
     * Retrieve the url associated with the specified key from the help resource
     * bundle
     * 
     * @param urlBundleKey
     *            the key associated with the URL to retreive
     * @return the request url
     */
    private String retrieveURLFromBundle(String urlBundleKey) {
        if (urlBundleKey == null) {
            throw new NullPointerException("urlBundleKey cannot be null.");
        }

        return getBundle().getString(urlBundleKey);
    }

    /**
     * Retrieve the Help Location Resource Bundle
     * 
     * @return the Help Location Resource Bundle
     */
    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(HELP_BUNDLE_NAME);
    }
}
