/*
 * Created on March 9, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2009 by NextLabs Inc.,
 * San Mateo, CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

// list of actions

package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;

/**
 * @author Nao
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/ActionListConfigDO.java#1 $
 */
public class ActionListConfigDO implements IActionListConfigDO {

    private List<IActionConfigDO> actions = new ArrayList<IActionConfigDO>();

    /**
     * Constructor
     */
    public ActionListConfigDO() {
        super();
    }

    /**
     * @see IActionListConfigDO#getActions()
     */
    public IActionConfigDO[] getActions() {
        return (IActionConfigDO[])
            actions.toArray(new IActionConfigDO[actions.size()]);
    }

    /**
     * Adds a regular expression configuration.
     * @param toAdd action to be added
     */
    public void addAction(IActionConfigDO toAdd) {
        actions.add(toAdd);
    }

    public void setActions(IActionConfigDO[] newActions) {
        if (newActions == null) {
            actions = new ArrayList<IActionConfigDO>();
        } else {
            actions = new ArrayList<IActionConfigDO>(Arrays.asList(newActions));
        }
    }
}

