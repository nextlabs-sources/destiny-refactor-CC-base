/*
 * Created on Nov 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

/**
 * Parses a user-friendly expression into an LDAP filter
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/importcoordinator/filter/SelectionParser.java#1 $
 */

public class SelectionParser {

    private static final String EQ = "=";
    private static final String NEQ = "!=";

    /**
     * Constructor
     *  
     */
    public SelectionParser() {
        super();
    }

    public static boolean isLegalAsValue(String value) {
        // No recognized operator should exist within the value:
        boolean isLegal = (value.indexOf(EQ) == -1) && (value.indexOf(NEQ) == -1);
        return isLegal;
    }

    public static boolean isExpression(String expression) {
        boolean isExpression = expression.startsWith("(");
        return isExpression;
    }
}