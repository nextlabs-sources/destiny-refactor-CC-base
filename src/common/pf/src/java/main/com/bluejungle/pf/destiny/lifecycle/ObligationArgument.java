package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2007 by Next Labs Inc,
 * San Mateo, CA.
 * Ownership remains with Next Labs Inc. All rights reserved worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/ObligationArgument.java#1 $
 */

public class ObligationArgument {
    /** Represents the display name of the obligation argument */
    private final String displayName;
    private final String[] values;
    private final String defaultValue;
    private final boolean userEditable;
    private final boolean hidden;

    /**
     * Constructs the <code>ObligationArgument</code> with the specified <code>displayName</code> and <code>type</code>
     * @param displayName the displayName of the argument
     * @param type the type of the argument
     */

    public ObligationArgument (String displayName, String[] values, String defaultValue, boolean userEditable, boolean hidden) {
        this.displayName = displayName == null ? "" : displayName;
        this.values = values;
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        this.hidden = hidden;
    }

    /**
     * Accesses the display name of the argument.
     * @return the display name of the argument.
     */
    public String getDisplayName(){
        return displayName;
    }

    /**
     * Is the argument user editable
     * @return userEditable
     */
    public boolean isUserEditable() {
        return userEditable;
    }

    /**
     * Is the argument hidden
     * @return hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Access the (possible null) default value.  The caller may assume that this value
     * is one of the ones returned by getValues()
     * @return defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Access the (possibly empty) array of allowed values for this argument
     * @return values
     */
    public String[] getValues() {
        return values;
    }
}
