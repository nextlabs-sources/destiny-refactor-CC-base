/*
 * Created on Apr 1, 2005 (this is serious code though)
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

/**
 * This is the implementation class for the select element.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/SelectElementImpl.java#1 $
 */

public class SelectElementImpl implements ISelectElement {

    private String doClassName;
    private String doVarName;
    private String fieldName;
    private String function;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ISelectElement#getDOVarName()
     */
    public String getDOVarName() {
        return this.doVarName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ISelectElement#getDOClassName()
     */
    public String getDOClassName() {
        return this.doClassName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ISelectElement#getFieldName()
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.ISelectElement#getFunction()
     */
    public String getFunction() {
        return this.function;
    }

    /**
     * Sets the data object variable name
     * 
     * @param newName
     *            name to set
     */
    public void setDOVarName(String newName) {
        this.doVarName = newName;
    }

    /**
     * Sets the data object class name
     * 
     * @param newName
     *            name to set
     */
    public void setDOClassName(String newName) {
        this.doClassName = newName;
    }

    /**
     * Sets the field name
     * 
     * @param newName
     *            new name to set
     */
    public void setFieldName(String newName) {
        this.fieldName = newName;
    }

    /**
     * Sets the function name
     * 
     * @param function
     *            name to set
     */
    public void setFunction(String function) {
        this.function = function;
    }
}