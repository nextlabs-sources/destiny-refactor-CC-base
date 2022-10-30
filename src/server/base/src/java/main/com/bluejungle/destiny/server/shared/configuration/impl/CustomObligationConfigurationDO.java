/*
 * Created on Mar 5, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.ICustomObligationArgumentDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationConfigurationDO;

/**
 * @author amorgan
 * @version $Id:
 */

public class CustomObligationConfigurationDO implements ICustomObligationConfigurationDO {

    private String displayName;
    private String runAt;
    private String runBy;
    private String invocationString;
    private List<ICustomObligationArgumentDO> arguments = new ArrayList<ICustomObligationArgumentDO>();
    private static final String defaultRunBy = "User";

    /**
     * Constructor
     */
    public CustomObligationConfigurationDO() {
        runBy = defaultRunBy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRunAt() {
        return runAt;
    }

    public void setRunAt(String runAt) {
        this.runAt = runAt;
    }

    public String getRunBy() {
        return runBy;
    }

    public void setRunBy(String runBy) {
        this.runBy = runBy;
    }

    public String getInvocationString() {
        return invocationString;
    }

    public void setInvocationString(String invocationString) {
        this.invocationString = invocationString;
    }
    
    public ICustomObligationArgumentDO[] getArguments() {
        return (ICustomObligationArgumentDO[])arguments.toArray(new ICustomObligationArgumentDO[arguments.size()]);
    }
    
    public void setArguments(List<ICustomObligationArgumentDO> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(ICustomObligationArgumentDO argument) {
        this.arguments.add(argument);
    }
}


