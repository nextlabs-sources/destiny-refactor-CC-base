/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IResourceAttributeConfigurationDO;

/**
 * This is the implementation of the definition of a custom resource attribute.
 * @author sergey
 */
public class ResourceAttributeConfigurationDO implements IResourceAttributeConfigurationDO {

    /** The group to which the attribute belongs. */
    private String groupName;

    /** The display name of the attribute. */
    private String displayName;

    /** The PQL name of the attribute. */
    private String pqlName;

    /** The name of the attribute type. */
    private String typeName;

    /** An optional list of compatible attributes. */
    private final List<String> attributes = new ArrayList<String>();

    /** An optional list of enumerated values. */
    private final List<String> values = new ArrayList<String>();

    /**
     * The default constructor for the digester.
     */
    public ResourceAttributeConfigurationDO() {
    }

    /**
     * The constructor taking parameters for each data field.
     * @param groupName the group name.
     * @param displayName the display name.
     * @param pqlName the PQL name.
     * @param typeName the type name.
     * @param attributes the related attributes
     * @param values the enumerated values
     */
    public ResourceAttributeConfigurationDO(
        String groupName
    ,   String displayName
    ,   String pqlName
    ,   String typeName
    ,   String[] attributes
    ,   String[] values
    ) {
        this.groupName = groupName;
        this.displayName = displayName;
        this.pqlName = pqlName;
        this.typeName = typeName;
        if (attributes != null) {
            this.attributes.addAll(Arrays.asList(attributes));
        }
        if (values != null) {
            this.values.addAll(Arrays.asList(values));
        }
    }

    /**
     * @see IResourceAttributeConfigurationDO#getGroup()
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @see IResourceAttributeConfigurationDO#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @see IResourceAttributeConfigurationDO#getPqlName()
     */
    public String getPqlName() {
        return pqlName;
    }

    /**
     * @param pqlName the pqlName to set
     */
    public void setPqlName(String pqlName) {
        this.pqlName = pqlName;
    }

    /**
     * @see IResourceAttributeConfigurationDO#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the configured attributes as an unmodifiable list.
     */
    public List<String> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Adds an attribute with the specified name.
     *
     * @param attributeName the name of the attribute to add.
     */
    public void addAttribute(String attributeName) {
        if (attributeName != null && attributeName.length() != 0) {
            attributes.add(attributeName);
        }
    }

    /**
     * Gets the configured values as an unmodifiable list.
     */
    public List<String> getEnumeratedValues() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Adds an value with the specified name.
     *
     * @param valueName the name of the attribute to add.
     */
    public void addValue(String valueName) {
        if (valueName != null && valueName.length() != 0) {
            values.add(valueName);
        }
    }

}
