/*
 * Created on May 8, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2009 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with Nextlabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * @author Nao
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IActionConfigDO.java#1 $
 */

public interface IActionConfigDO {

    /**
     * Returns Name
     * 
     * @return Identifiable name, e.g., COPY, DELETE, FTP
     */
    public String getName();

    /**
     * Returns Display Name
     * 
     * @return Name to be shown in GUI, e.g., "Copy Files" "FTP Upload or Download"
     */
    public String getDisplayName();

    /**
     * Returns Short Name
     * 
     * @return Two character ID of action, e.g., "CO", "DE", "FT"
     */
    public String getShortName();

    /**
     * Returns Category
     * 
     * @return Category string used in Policy Studio, e.g., "Transform"
     */
    public String getCategory();

}