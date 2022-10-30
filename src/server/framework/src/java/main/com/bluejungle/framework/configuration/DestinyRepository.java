/*
 * Created on Jan 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a set of constants representing the component names of the logical
 * databases in Destiny
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/configuration/DestinyRepository.java#1 $
 */
public enum DestinyRepository {
    MANAGEMENT_REPOSITORY("management.repository"),
    ACTIVITY_REPOSITORY("activity.repository"),
    POLICY_FRAMEWORK_REPOSITORY("policyframework.repository"),
    DICTIONARY_REPOSITORY("dictionary.repository"),
    
    COMMON_REPOSITORY("common.repository.properties", "common.repoistory");
    ;
    
    private final String configFileName;
    private final String name;
    
    private DestinyRepository(String configFileName, String name) {
        this.configFileName = configFileName;
        this.name = name;
    }
    
    private DestinyRepository(String name) {
        this(name + ".xml", name);
    }
    
    /**
     * Returns the path of the configuration file for this repository relative
     * to the web-application root.
     * 
     * @return string representing configuration file name
     */
    public String getConfigFileName() {
        return this.configFileName;
    }

    /**
     * This converts the DestinyRepository into a string that is the same as its
     * name
     * 
     * @return name of repository
     * @deprecated don't get the HiberanteRepoitory by name, call <code>getHibernateRepository</code>
     */
    @Deprecated
    public String getName() {
        return this.name;
    }
    
    private static final Map<String, DestinyRepository> NAME_TO_DESTINY_REPOSITORY;
    static {
        DestinyRepository[] destinyRepositories = DestinyRepository.values();
        NAME_TO_DESTINY_REPOSITORY = new HashMap<String, DestinyRepository>(
                destinyRepositories.length);
        for (DestinyRepository destinyRepository : destinyRepositories) {
            NAME_TO_DESTINY_REPOSITORY.put(destinyRepository.getName(),
                    destinyRepository);
        }
    }
    
    public static DestinyRepository getByName(String name){
        return NAME_TO_DESTINY_REPOSITORY.get(name);
    }
    
}
