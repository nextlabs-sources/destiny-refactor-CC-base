/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.net.URL;
import java.util.Set;

import com.bluejungle.version.IVersion;

/**
 * This interface is used to represent a given server component registration
 * information
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IDCCRegistrationInfo.java#1 $
 */

public interface IDCCRegistrationInfo {

    /**
     * Returns the component name
     * 
     * @return the component name
     */
    public String getComponentName();

    /**
     * Returns the component type
     * 
     * @return the component type
     */
    public ServerComponentType getComponentType();
    
    /**
     * Returns the component display name
     * 
     * @return the component display name
     */
    public String getComponentTypeDisplayName();

    /**
     * Returns the URL where the component is accessible
     * 
     * @return the URL where the component is accessible
     */
    public URL getComponentURL();

    /**
     * Returns the URL where the event listener for the component (i.e. DCSF) is
     * accessible
     * 
     * @return the URL where the event listener for the component is accessible
     */
    public URL getEventListenerURL();

    /**
     * Returns the component version
     * 
     * @return the component version
     */
    public IVersion getComponentVersion();
    
    /**
     * Returns the application resources
     * 
     * @return the application resources
     */
    public Set<String> getApplicationResources();
    
    
    /**
     * Sets the component name
     * 
     * @param name
     *            component name to set
     */
    public void setComponentName(String name);

    /**
     * Sets the component type
     * 
     * @param type
     *            type to set
     */
    public void setComponentType(ServerComponentType type);
    
    /**
     * Sets the component display name
     * 
     * @param type
     *            display name to set
     */
    public void setComponentTypeDisplayName(String typeDisplayName);

    /**
     * Sets the URL where the component is accessible
     * 
     * @param url
     *            url to set
     */
    public void setComponentURL(URL url);

    /**
     * Sets the event listener URL
     * 
     * @param url
     *            url to set
     */
    public void setEventListenerURL(URL url);
    
    /**
     * Sets the component version
     * 
     * @param version
     *            component version to set
     */
    public void setComponentVersion(IVersion version);
    
    /**
     * Sets the application resources
     * 
     * @param appResources
     *            the application resources
     */
    public void setApplicationResources(Set<String> appResources);
}
