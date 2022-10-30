/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrQueryFieldType;

/**
 * Implementation of the persistent report query term.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentReportQueryTermImpl.java#1 $
 */

public class PersistentReportQueryTermImpl implements IPersistentReportMgrQueryTerm {

    private PersistentReportMgrQueryFieldType fieldName;
    private String expression;

    /**
     * 
     * Constructor
     * 
     * @param newField
     *            field to query on
     * @param newExpression
     *            expression to apply to the field
     */
    public PersistentReportQueryTermImpl(PersistentReportMgrQueryFieldType newField, String newExpression) {

        if (newExpression == null) {
            throw new NullPointerException("expression cannot be null");
        }
        if (newField == null) {
            throw new NullPointerException("field name cannot be null");
        }

        this.expression = newExpression;
        this.fieldName = newField;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm#getFieldName()
     */
    public PersistentReportMgrQueryFieldType getFieldName() {
        return this.fieldName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm#getExpression()
     */
    public String getExpression() {
        return this.expression;
    }

}