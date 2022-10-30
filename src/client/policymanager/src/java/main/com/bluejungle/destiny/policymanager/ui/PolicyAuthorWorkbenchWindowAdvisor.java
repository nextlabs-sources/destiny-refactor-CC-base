/*
 * Created on Jan 10, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/ui/PolicyAuthorWorkbenchWindowAdvisor.java#1 $
 */

public class PolicyAuthorWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public PolicyAuthorWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

        Rectangle size = Display.getCurrent().getBounds();
        configurer.setInitialSize(new Point(size.width, size.height));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle(ApplicationMessages.POLICYAUTHORWORKBENCHWINDOWADVISOR_TITLE);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new PolicyAuthorActionBarAdvisor(configurer);
    }

    public void postWindowOpen() {
        getWindowConfigurer().getWindow().getShell().setImage(ImageBundle.POLICY_IMG);
    }
}
