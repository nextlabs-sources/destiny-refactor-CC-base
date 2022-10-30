/*
 * Created on May 8, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2009 by NextLabs Inc.,
  * San Mateo CA, Ownership remains with Nextlabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;

/**
 * @author Nao
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/ActionConfigDO.java#1 $
 */

public class ActionConfigDO implements IActionConfigDO {

    private String name;
    private String displayName;
    private String shortName;
    private String category;
    
    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IActionConfigDO#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets Name
     * 
     * @param name
     *            Identifiable name, e.g., COPY, DELETE, FTP
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IActionConfigDO#getTypeDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Sets Display Name
     * 
     * @param displayName
     *            Name to be shown in GUI, e.g., "Copy Files" "FTP Upload or Download"
     */
    public void setDisplayName(String newDisplayName) {
        this.displayName = newDisplayName;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IActionConfigDO#getShortName()
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Sets Short Name
     * 
     * @param shortName
     *            Two character ID of action, e.g., "CO", "DE", "FT"
     */
    public void setShortName(String newShortName) {
        this.shortName = newShortName;
    }

    /**
    * @see com.bluejungle.destiny.container.dms.components.configmgr.IActionConfigDO#getCategory()
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Sets Category
     * 
     * @param category
     *            Category string used in Policy Studio, e.g., "Transform"
     */
    public void setCategory(String newCategory) {
        this.category = newCategory;
    }
}