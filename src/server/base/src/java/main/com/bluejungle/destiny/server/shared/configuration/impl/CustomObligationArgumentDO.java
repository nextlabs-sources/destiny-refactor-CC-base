/*
 * Created on Aug 17, 2007
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.ICustomObligationArgumentDO;

/**
 * @author amorgan
 * @version $Id: //depot/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/CustomObligationArgumentDO.java#1
 */

public class CustomObligationArgumentDO implements ICustomObligationArgumentDO {
    private String name;
    private List<String> values = new ArrayList<String>();
    private String defaultValue;
    private boolean userEditable;
    private boolean hidden;

    public CustomObligationArgumentDO() {
    }

    /**
     * The constructor taking parameters for each field
     * @param name the name for this argument
     * @param values allowable values
     * @param defaultValue the default value
     * @param userEditable can the user edit this option
     * @param hidden is the item hidden from view
     */
    public CustomObligationArgumentDO(String name, List<String> values, String defaultValue, boolean userEditable, boolean hidden) {
        this.name = name;
        this.values = values;

        if (valueExists(defaultValue)) {
            this.defaultValue = defaultValue;
        } else {
            this.defaultValue = null;
        }

        this.userEditable = userEditable;
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getValues() {
        return (String[])values.toArray(new String[values.size()]);
    }

    public void addValue(String value, String makeDefault) {
        boolean toDefault = Boolean.parseBoolean(makeDefault);

        if (toDefault && defaultValue == null) {
            defaultValue = value;
        }
        values.add(value);
    }

    public void setValues(List<String> values) {
        this.values = values;

        if (!valueExists(defaultValue)) {
            defaultValue = null;
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        if (valueExists(defaultValue)) {
            this.defaultValue = defaultValue;
        } else {
            this.defaultValue = null;
        }
    }

    public boolean isArgumentUserEditable() {
        return userEditable;
    }

    // Called by digester, hence non-standard capitalization
    public void setUsereditable(String userEditable) {
        this.userEditable = Boolean.parseBoolean(userEditable);
    }

    public boolean isArgumentHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = Boolean.parseBoolean(hidden);
    }

    private boolean valueExists(String value) {
        for (String v : values) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
