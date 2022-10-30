/*
 * Created on Jan 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * A single User Profile Query Term. Currently, a combination of an User
 * ProfileQueryTermField and a value
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/UserProfileQueryTerm.java#1 $
 */

public class UserProfileQueryTerm {

    private UserProfileQueryField queryField;
    private Object value;

    /**
     * Create a UserProfileQueryTerm specifying a query field and value
     * 
     * @param queryField
     *            The field of the user profile to specify in the query term
     *            criterion
     * @param value
     *            The value that field should be to match the query term
     */
    public UserProfileQueryTerm(UserProfileQueryField queryField, Object value) {
        if (queryField == null) {
            throw new IllegalArgumentException("Query field cannot be null.");
        }

        this.queryField = queryField;
        this.value = value;
    }

    /**
     * Retrieve the query field associated with this query term
     * 
     * @return the queryField associated with this query term
     */
    public UserProfileQueryField getQueryField() {
        return this.queryField;
    }

    /**
     * Retrieve the value associated with this query term
     * 
     * @return the value associated with this query term. Note that this may be
     *         null is the query term is searching for null values
     */
    public Object getValue() {
        return this.value;
    }
}