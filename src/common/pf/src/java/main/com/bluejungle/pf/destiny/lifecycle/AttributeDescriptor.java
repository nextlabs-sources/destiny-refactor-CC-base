package com.bluejungle.pf.destiny.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.RelationOp;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/AttributeDescriptor.java#1 $
 */

/**
 * Instances of this class describe attributes of leaf entities.
 * Each attribute has two names - the display name for the UI,
 * and the PQL name for the policy framework.
 */
public class AttributeDescriptor {
    /** Represents the (optional) group name of the attribute.  Attibutes with the same group name
     *  can be grouped together (e.g. on a GUI).
     */
    private final String groupName;

    /** Represents the display name of the attribute. */
    private final String displayName;

    /** Represents the type of this attribute. */
    private final AttributeType type;

    /** Represents the isRequired flag. */
    private final boolean isRequired;

    /** The attribute represented by this descriptor. */
    private final IAttribute attribute;

    /** Represents the list of operators compatible with this attribute. */
    private final List<RelationOp> operators;

    /** Represents the list of attributes compatible with this attribute. */
    private final List<AttributeDescriptor> allowedAttributes;

    /** Represents the list of attributes compatible with this attribute. */
    private final List<String> enumeratedValues;

    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, and type.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param attribute the attribute
     */
    public AttributeDescriptor(
        String displayName
    ,   AttributeType type
    ,   IAttribute attribute
    ) {
        this(null, displayName, type, true, attribute, null, null, null);
    }


    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, type, and the required/optional flag.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param isRequired the flag indicating that the attribute is required.
     * @param attribute the attribute
     */
    public AttributeDescriptor(
        String displayName
    ,   AttributeType type
    ,   boolean isRequired
    ,   IAttribute attribute
    ) {
        this(null, displayName, type, isRequired, attribute, null, null, null);
    }

    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, type, and the required/optional flag.
     * @param groupName the group name of the attribute.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param isRequired the flag indicating that the attribute is required.
     * @param attribute the attribute
     */
    public AttributeDescriptor(
        String groupName
    ,   String displayName
    ,   AttributeType type
    ,   boolean isRequired
    ,   IAttribute attribute
    ) {
        this(groupName, displayName, type, isRequired, attribute, null, null, null);
    }

    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, and type.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param isRequired the flag indicating that the attribute is required.
     * @param attribute the attribute
     * @param values allowed values for this attributes
     */
    public AttributeDescriptor(
        String displayName
    ,   AttributeType type
    ,   boolean isRequired
    ,   IAttribute attribute
    ,   String[] enumeratedValues
    ) {
        this(null, displayName, type, isRequired, attribute, null, null, enumeratedValues);
    }

    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, and type.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param isRequired the flag indicating that the attribute is required.
     * @param attribute the attribute
     * @param values allowed values for this attributes
     */
    public AttributeDescriptor(
        String groupName
    ,   String displayName
    ,   AttributeType type
    ,   boolean isRequired
    ,   IAttribute attribute
    ,   String[] enumeratedValues
    ) {
        this(groupName, displayName, type, isRequired, attribute, null, null, enumeratedValues);
    }

    /**
     * Constructs the <code>AttributeDescriptor</code> with the specified
     * display name, PQL name, type, the required/optional flag,
     * a list of override operators, and a list of compatible attributes.
     * @param groupName the display name of the attribute.
     * @param displayName the display name of the attribute.
     * @param type the type of the attribute
     * @param isRequired the flag indicating that the attribute is required.
     * @param attribute the attribute
     * @param operators an array of operators to be used with this attribute.
     * @param allowedAttributes the list of attributes that may be placed
     * on the opposite side of this attribute in a relation.
     * @param enumeratedValues values (optional) the list of permissible values for
     * this attribute
     */
    public AttributeDescriptor(
        String groupName
    ,   String displayName
    ,   AttributeType type
    ,   boolean isRequired
    ,   IAttribute attribute
    ,   RelationOp[] operators
    ,   AttributeDescriptor[] allowedAttributes
    ,   String[] enumeratedValues
    ) {
        this.groupName = groupName;
        this.displayName = displayName;
        this.type = type;
        this.isRequired = isRequired;
        this.attribute = attribute;
        if (operators != null) {
            this.operators = new ArrayList<RelationOp>(
                Arrays.asList(operators)
            );
        } else {
            this.operators = Collections.emptyList();
        }
        if (allowedAttributes != null) {
            this.allowedAttributes = new ArrayList<AttributeDescriptor>(
                Arrays.asList(allowedAttributes)
                );
        } else {
            this.allowedAttributes = Collections.emptyList();
        }
        if (enumeratedValues != null) {
            this.enumeratedValues = new ArrayList<String>(
                Arrays.asList(enumeratedValues)
                );
        } else {
            this.enumeratedValues = Collections.emptyList();
        }
    }

    /**
     * Accesses the group name of the attribute.
     * @return the group name of the attribute.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Accesses the display name of the attribute.
     * @return the display name of the attribute.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Accesses the PQL name of this attribute.
     * @return the PQL name of this attribute.
     */
    public String getPqlName() {
        return attribute.getName();
    }

    /**
     * Access the context type.
     * @return the context type name.
     */
    public String getContextTypeName() {
        return attribute.getObjectTypeName();
    }

    /**
     * Access the subtype name.
     * @return the name of the context subtype.
     */
    public String getContextSubtypeName() {
        return attribute.getObjectSubTypeName();
    }

    /**
     * Accesses the type of this attribute.
     * @return the type of this attribute.
     */
    public AttributeType getType() {
        return type;
    }

    /**
     * Accesses the attribute described by this descriptor. 
     * @return the attribute described by this descriptor.
     */
    public IAttribute getAttribute() {
        return attribute;
    }

    /**
     * Determines if the attribute is required or not.
     * @return A flag indicating that the attribute is required.
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Determine if this descriptor has operator overrides.
     * @return true if the descriptor has operator overrides; false otherwise.
     */
    public boolean hasOperators() {
        return !operators.isEmpty();
    }

    /**
     * Determine if the descriptor has compatible attributes.
     * @return true if the descriptor has compatible attributes; false otherwise.
     */
    public boolean hasAttributes() {
        return !allowedAttributes.isEmpty();
    }

    /**
     * Returns a list of operator overrides for this attribute descriptor.
     * @return a list of operator overrides for this attribute descriptor.
     */
    public List<RelationOp> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    /**
     * Returns a list of compatible attributes for this attribute descriptor.
     * @return a list of compatible attributes for this attribute descriptor.
     */
    public List<AttributeDescriptor> getAllowedAttributes() {
        return Collections.unmodifiableList(allowedAttributes);
    }

    /**
     * Returns a list of enumerated values for this attribute descriptor.
     * @return a list of enumerated values for this attribute descriptor.
     */
    public List<String> getEnumeratedValues() {
        return Collections.unmodifiableList(enumeratedValues);
    }

}
