/*
 * Created on Nov 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import javax.servlet.ServletContext;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/ApplicationInformationImpl.java#1 $
 */

public class ApplicationInformationImpl implements IApplicationInformation, IInitializable, IConfigurable {

    public static final String SERVLET_CONTEXT = "ServletContext";
    
    private IConfiguration configuration;
    private String appName;

    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }
    
    public IConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public void init() {
        ServletContext ctx = (ServletContext) this.configuration.get(SERVLET_CONTEXT);
        this.appName = ctx.getServletContextName();
    }
        
    /**
     * Constructor
     * 
     */
    public ApplicationInformationImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.IApplicationInformation#getApplicationName()
     */
    public String getApplicationName() {
        return this.appName;
    }
}
