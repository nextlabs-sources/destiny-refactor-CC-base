/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.util.Calendar;

import com.bluejungle.framework.domain.IDomainObject;

/**
 * The Base Profile Data Object contains methods shared between all profiles in Destiny 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IBaseProfileDO.java#1 $
 */

public interface IBaseProfileDO extends IDomainObject {
    /**
     * @return Returns the id.
     */
    public Long getId();

    /**
     * Returns the name.
     * 
     * @return the name.
     */
    public String getName();

    /**
     * Sets the name
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name);

    /**
     * Determine if this profile is the default
     * 
     * @return true if this is the default profile; false otherwise
     */
    public boolean isDefault();
    
    /**
     * Returns the createdDate.
     * 
     * @return the createdDate.
     */
    public Calendar getCreatedDate();

    /**
     * Returns the modifiedDate.
     * 
     * @return the modifiedDate.
     */
    public Calendar getModifiedDate();  
}