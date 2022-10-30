package com.bluejungle.destiny.server.shared.configuration.impl;

/*
 * Created on Feb 9, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IFileSystemLogConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRegularExpressionConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ITrustedDomainsConfigurationDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/DABSComponentConfigurationDO.java#1 $
 */
public class DABSComponentConfigurationDO extends DCCComponentConfigurationDO implements IDABSComponentConfigurationDO {

    private TrustedDomainsConfigurationDO trustedDomainsConfiguration;
    private FileSystemLogConfigurationDO fileSystemLogConfiguration;
    private List<IRegularExpressionConfigurationDO> regularExpressions = new ArrayList<IRegularExpressionConfigurationDO>();

    /**
     * Constructor
     */
    public DABSComponentConfigurationDO() {
        super();
    }

    /**
     * Sets the trustedDomainsConfiguration
     *
     * @param trustedDomainsConfiguration The trustedDomainsConfiguration to set.
     */
    public void setTrustedDomainsConfiguration(TrustedDomainsConfigurationDO trustedDomainsConfiguration) {
        this.trustedDomainsConfiguration = trustedDomainsConfiguration;
    }

    public ITrustedDomainsConfigurationDO getTrustedDomainsConfiguration() {
        return trustedDomainsConfiguration;
    }

    public IFileSystemLogConfigurationDO getFileSystemLogConfiguration() {
        return fileSystemLogConfiguration;
    }
    
    public void setFileSystemLogConfiguration(FileSystemLogConfigurationDO fileSystemLogConfiguration) {
        this.fileSystemLogConfiguration = fileSystemLogConfiguration;
    }

    /**
     * @see IDABSComponentConfigurationDO#getRegularExpressions()
     */
    public IRegularExpressionConfigurationDO[] getRegularExpressions() {
        return (IRegularExpressionConfigurationDO[])
            regularExpressions.toArray(
                new IRegularExpressionConfigurationDO[regularExpressions.size()]
            );
    }

    /**
     * Adds a regular expression configuration.
     * @param toAdd the configuration to add.
     */
    public void addRegularExpression(IRegularExpressionConfigurationDO toAdd) {
        regularExpressions.add(toAdd);
    }

    public void setRegularExpressionsConfiguration(IRegularExpressionConfigurationDO[] regularExpressionConfiguration) {
        if (regularExpressionConfiguration == null) {
            regularExpressions = new ArrayList<IRegularExpressionConfigurationDO>();
        } else {
            regularExpressions = new ArrayList<IRegularExpressionConfigurationDO>(Arrays.asList(regularExpressionConfiguration));
        }
    }
}
