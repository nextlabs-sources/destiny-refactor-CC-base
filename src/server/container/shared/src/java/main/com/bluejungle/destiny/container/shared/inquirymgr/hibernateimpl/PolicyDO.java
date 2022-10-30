/*
 * Created on Mar 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.StringTokenizer;

/**
 * This is the policy data object. It represents one policy defined in the
 * policy framework.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PolicyDO.java#2 $
 */

public class PolicyDO implements IPolicy {

    private static final String FOLDER_SEPARATOR = "/";
    private Long id;
    private String fullName;
    private String name;

    /**
     * Constructor
     *  
     */
    public PolicyDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy#getFolderName()
     */
    public String getFolderName() {
        StringTokenizer st = new StringTokenizer(getFullName(), FOLDER_SEPARATOR);
        int nbToken = st.countTokens();
        String folderName = "";
        for (int currentToken = 0; currentToken < nbToken - 1; currentToken++) {
            String tokenString = st.nextToken();
            if (tokenString.length() > 0) {
                folderName += FOLDER_SEPARATOR + tokenString;
            }
        }
        return folderName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy#getFullName()
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the policy id
     * 
     * @param newId
     *            new id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the policy canonical name
     * 
     * @param newFullName
     *            new canonical name to set
     */
    public void setFullName(String newFullName) {
        this.fullName = newFullName.trim();
        StringTokenizer st = new StringTokenizer(fullName, FOLDER_SEPARATOR);
        int nbToken = st.countTokens();
        for (int currentToken = 0; currentToken < nbToken - 1; currentToken++) {
            st.nextToken();
        }
        setName(st.nextToken());
    }

    /**
     * Sets the policy name
     * 
     * @param newName
     *            new name to set
     */
    protected void setName(String newName) {
        this.name = newName;
    }
}