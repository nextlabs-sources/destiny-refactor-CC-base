/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.net.URL;
import java.util.Set;

import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.version.IVersion;

/**
 * This is the DCC registration info implementation class. This class holds all
 * the information related to a server component registration.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/DCCRegistrationInfoImpl.java#1 $
 */

/**
 * @author hchan
 *
 */
public class DCCRegistrationInfoImpl implements IDCCRegistrationInfo {

    private String name;
    private ServerComponentType type;
    private String typeDisplayName;
    private URL url;
    private URL listenerURL;
    private IVersion version;
    private Set<String> applicationResouces;

    /**
     * Constructor
     *  
     */
    public DCCRegistrationInfoImpl() {
        super();
    }
    
    public DCCRegistrationInfoImpl(
            String name
          , ServerComponentType type
          , String typeDisplayName
          , URL url
          , URL listenerURL
          , IVersion version
          , Set<String> applicationResouces
    ) {
        this.name = name;
        this.type = type;
        this.typeDisplayName = typeDisplayName;
        this.url = url;
        this.listenerURL = listenerURL;
        this.version = version;
        this.applicationResouces = applicationResouces;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#getComponentName()
     */
    public String getComponentName() {
        return name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return type;
    }
    
    @Override
    public String getComponentTypeDisplayName() {
        return typeDisplayName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#getComponentURL()
     */
    public URL getComponentURL() {
        return url;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#getEventListenerURL()
     */
    public URL getEventListenerURL() {
        return listenerURL;
    }
    
    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#getComponentVersion()
     */
    public IVersion getComponentVersion() {
        return version;
    }
    
    @Override
    public Set<String> getApplicationResources() {
        return applicationResouces;
    }
    
    

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#setComponentName(java.lang.String)
     */
    public void setComponentName(String name) {
        this.name = name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#setComponentType(com.bluejungle.destiny.server.shared.registration.ServerComponentType)
     */
    public void setComponentType(ServerComponentType type) {
        this.type = type;
    }
    
    @Override
    public void setComponentTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#setComponentURL(java.net.URL)
     */
    public void setComponentURL(URL url) {
        this.url = url;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#setEventListenerURL(java.net.URL)
     */
    public void setEventListenerURL(URL url) {
        this.listenerURL = url;
    }
    
    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo#setComponentVersion(com.bluejungle.version.IVersion)
     */
    public void setComponentVersion(IVersion version) {
        this.version = version;
    }

    @Override
    public void setApplicationResources(Set<String> appResources) {
        this.applicationResouces = appResources;
    }

}
