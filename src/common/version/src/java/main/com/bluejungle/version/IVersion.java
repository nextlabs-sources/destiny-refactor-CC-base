/*
 * Created on Aug 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.version;

import java.io.Serializable;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/main/com/bluejungle/version/IVersion.java#1 $
 */

public interface IVersion extends Comparable<IVersion>, Serializable{
        
    /**
     * Returns the major version
     * @return the major version
     */
    public int getMajor();
    
    /**
     * Returns the minor version
     * @return the minor version
     */
    public int getMinor();
    
    /**
     * Returns the maintenance version
     * @return the maintenance version
     */
    public int getMaintenance();
    
    /**
     * Returns the patch version
     * @return the patch version
     */
    public int getPatch();
    
    /**
     * Returns the build version
     * @return the build version
     */
    public int getBuild();
    
    /**
     * sets the major version
     */
    public void setMajor(int major);
    
    /**
     * sets the minor version
     */
    public void setMinor(int minor);
    
    /**
     * sets the maintenance version
     */
    public void setMaintenance(int maintenance);
    
    /**
     * sets the patch version
     */
    public void setPatch(int patch);
    
    /**
     * sets the build version
     */
    public void setBuild(int build);
}
