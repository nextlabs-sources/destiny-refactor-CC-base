/*
 * Created on Nov 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/importcoordinator/filter/AbstractSelection.java#1 $
 */

public abstract class AbstractSelection {

    private String extractedFilter;

    /**
     * Constructor
     *  
     */
    public AbstractSelection(String expression) {
        super();

        // Check if this is an expression or a direct reference:
        if (SelectionParser.isExpression(expression)) {
            // We assume that if it's an expression, it should be a direct
            // pass-through:
            this.extractedFilter = expression;
        } else if (!SelectionParser.isLegalAsValue(expression)) {
            throw new IllegalArgumentException("Expression is not valid: '" + expression + "'");
        } else {
            this.extractedFilter = "(" + getDirectReferenceAttribute() + "=" + LDAPFilterUtils.escape(expression) + ")";
        }
    }

    protected abstract String getDirectReferenceAttribute();

    public String getExtractedFilter() {
        return this.extractedFilter;
    }
}