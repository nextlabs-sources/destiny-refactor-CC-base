/*
 * Created on Aug 08, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/configuration/DDACActiveDirectoryConfiguration.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.configuration;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.crypt.ReversibleEncryptor;

public class DDACActiveDirectoryConfiguration {
    private String hostName;
    private String login;
    private String password;
    private String adRuleIdentity;
    private String adCAPIdentity;

    private Map<String, String> acplToADMappings = new HashMap<String, String>();
    private Map<String, String> groupToSIDMappings = new HashMap<String, String>();

    public DDACActiveDirectoryConfiguration() {
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setLogin(String login) {
        // Assuming windows style domain\login. The \ is important
        this.login = login.replaceAll("/", "\\\\");
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        ReversibleEncryptor dec = new ReversibleEncryptor();

        this.password = dec.decrypt(password);
    }

    public String getPassword() {
        return password;
    }

    public void setActiveDirectoryRuleIdentity(String adRuleIdentity) {
        this.adRuleIdentity = adRuleIdentity;
    }

    public String getActiveDirectoryRuleIdentity() {
        return adRuleIdentity;
    }

    public void setActiveDirectoryCAPIdentity(String adCAPIdentity) {
        this.adCAPIdentity = adCAPIdentity;
    }

    public String getActiveDirectoryCAPIdentity() {
        return adCAPIdentity;
    }

    public void setAcplToADMapping(String acplProperty, String adProperty) {
        acplToADMappings.put(acplProperty, adProperty);
    }

    public Map<String, String> getAcplToADMappings() {
        return acplToADMappings;
    }

    public Map<String, String> getACPLToADMappings() {
        return acplToADMappings;
    }

    public void setGroupToSIDMapping(String groupName, String sid) {
        groupToSIDMappings.put(groupName, sid);
    }

    public Map<String, String> getGroupToSIDMappings() {
        return groupToSIDMappings;
    }
}
