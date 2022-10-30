/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.util.Set;

import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import javax.servlet.ServletContext;

/**
 * This is the DCC container interface. The work of the DCC container is to:
 * 
 * 1)Register a DCC component with the DMS component service
 * 
 * 2) Upon successful registration with DMS, register the DCC component with the
 * local DCSF
 * 
 * 3) Initialize the DCC component with the correct configuration
 * 
 * 4) Provide DCC component heartbeat to the DMS component service
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/IDCCContainer.java#1 $:
 */

public interface IDCCContainer {

    /**
     * Name of the component class name config parameter
     */
    PropertyKey<Class<? extends IRegisteredDCCComponent>> COMPONENT_CLASS_CONFIG_PARAM = new PropertyKey<Class<? extends IRegisteredDCCComponent>>("ComponentClass");

    /**
     * Name of the component location config parameter
     */
    PropertyKey<String> COMPONENT_LOCATION_CONFIG_PARAM = new PropertyKey<String>("Location");

    /**
     * Name of the internal main component config
     */
    PropertyKey<IConfiguration> COMP_CONFIG_CONFIG_PARAM_NAME = new PropertyKey<IConfiguration>("InnerCompConfig");
    
    /**
     * Name of the component name config parameter
     */
    PropertyKey<String> COMPONENT_NAME_CONFIG_PARAM = new PropertyKey<String>("ComponentName");
    
    PropertyKey<String> COMPONENT_TYPE_DISPLAY_NAME_CONFIG_PARAM = new PropertyKey<String>("ComponentTypeDisplayName");

    /**
     * Name of the component type config parameter
     */
    PropertyKey<ServerComponentType> COMPONENT_TYPE_CONFIG_PARAM = new PropertyKey<ServerComponentType>("ComponentType");

    PropertyKey<Set<String>> COMPONENT_RESOUCES_CONFIG_PARAM = new PropertyKey<Set<String>>("ApplicationResources");
    
    /**
     * Name of the DMS location config parameter
     */
    PropertyKey<String> DMS_LOCATION_CONFIG_PARAM = new PropertyKey<String>("DMSLocation");

	/**
	 * Name of the main component config component name
	 */
	public static final String MAIN_COMPONENT_CONFIG_COMP_NAME = "MainComponentConfig";

	/**
     * Name of the tomcat home config parameter
     */
    public static final String INSTALL_HOME_PATH_CONFIG_PARAM = "InstallHome";

    /**
     * Name of the shared context locator class config parameter
     */
    PropertyKey<Class<? extends IDestinySharedContextLocator>> SHARED_CTX_LOCATOR_CLASSNAME_CONFIG_PARAM = new PropertyKey<Class<? extends IDestinySharedContextLocator>>("SharedCtxLocatorClassName");
    
    /**
     * Name of the servlet context for the container when the webapp initializes
     */
    PropertyKey<ServletContext> SERVLET_CONTEXT_CONFIG_PARAM = new PropertyKey<ServletContext>("ServletContext");
}