/*
 * Created on Aug 07, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/configuration/DDACConfiguration.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDACConfiguration {
    private int checkUpdatesFrequency;
    private Map<String, String> actionMappings = new HashMap<String, String>();
    private List<DDACActiveDirectoryConfiguration> adConfig = new ArrayList<DDACActiveDirectoryConfiguration>();

    public int getCheckUpdatesFrequency() {
        return checkUpdatesFrequency;
    }

    public void setCheckUpdatesFrequency(int checkUpdatesFrequency) {
        this.checkUpdatesFrequency = checkUpdatesFrequency;
    }

    public void setActionMapping(String nextlabsActions, String sddlPermission) {
        actionMappings.put(nextlabsActions, sddlPermission);
    }

    public Map<String, String> getActionMappings() {
        return actionMappings;
    }

    public void setADConfig(List<DDACActiveDirectoryConfiguration> adConfig) {
        this.adConfig = adConfig;
    }

    public List<DDACActiveDirectoryConfiguration> getADConfig() {
        return adConfig;
    }
}
