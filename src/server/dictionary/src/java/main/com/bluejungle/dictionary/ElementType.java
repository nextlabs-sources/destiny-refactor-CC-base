/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ElementType.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.bluejungle.framework.utils.ObjectHelper;

/**
 * This class represents the element type.
 */
class ElementType implements IMElementType {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of this element for Hibernate.
     */
    private Long id;

    /**
     * The version of this element type for optimistic locking.
     */
    private int version;

    /**
     * The name of this element type.
     */
    private String name;

    /**
     * The fields belonging to this element type.
     */
    SortedSet<IElementField> fields = new TreeSet<IElementField>();

    /** A cache for getting fields by name. */
    private transient Map<String,IElementField> fieldByName = new HashMap<String,IElementField>();

    /**
     * Package-private constructor for Hibernate.
     */
    ElementType() {
    }

    /**
     * Constructor for making new element types.
     * @param name the name of the new element type.
     */
    ElementType(String name) {
        this.name = name;
    }

    /**
     * Returns a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     * @return a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     */
    public synchronized Collection<IElementField> getFields() {
        return getFields(false);
    }
    
    /**
     * Returns a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. Includes deleted fields
     * @return a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     */
    public synchronized Collection<IElementField> getAllFields() {
        return getFields(true);
    }
    
    /**
     * Returns a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     * @param includeDeleted should deleted fields be included
     * @return a <code>Collection</code> of <code>IElementField</code>
     * objects representing fields of this element. 
     */
    private synchronized Collection<IElementField> getFields(boolean includeDeleted) {
        SortedSet<IElementField> res = new TreeSet<IElementField>();
        for (IElementField f : fields) {
            if ( !((ElementField)f).isDeleted() || includeDeleted) {
                res.add( f );
            }
        }
        return res;
    }

    /**
     * @see IElementType#getFieldNames()
     */
    public String[] getFieldNames() {
        List<String> res = new ArrayList<String>(fields.size());
        for (IElementField f : fields) {
            if ( !((ElementField)f).isDeleted() ) {
                res.add( f.getName() );
            }
        }
        return (String[])res.toArray(new String[res.size()]);
    }

    /**
     * Obtains a field specified by the given name.
     * @param name the name of the field.
     * @return the field specified by the given name.
     */
    public synchronized IElementField getField(String name) {
        if ( name == null ) {
            throw new NullPointerException("name");
        }
        name = name.toUpperCase();
        IElementField res = (IElementField)fieldByName.get(name);
        if ( res != null ) {
            return res;
        }
        for (IElementField field : fields) {
            ElementField f = (ElementField)field;
            if (name.equals(f.getCaseInsensitiveName()) && !f.isDeleted()) {
                fieldByName.put(name, f);
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown field: "+name);
    }

    /**
     * Adds a new field to this type.
     * @param name the name of the field to be added.
     * @param type the type of the field to be added.
     * @throws IllegalArgumentException if the type already has
     * the specified field.
     */
    public synchronized IElementField addField(String name, ElementFieldType type) {
        for (IElementField f : fields) {
            if (f.getName().equalsIgnoreCase(name)) {
                if (((ElementField)f).isDeleted()) {
                    if (f.getType() == type) {
                        // Restoring a deleted field
                        ((ElementField)f).setDeleted(false);
                        return f;
                    }
                }
            }
        }
        IElementField res = new ElementField(name, this, type);
        fields.add(res);
        return res;
    }

    /**
     * Removes a field from a type. If the field is not in the type,
     * the operation is ignored.
     * @param field the field to remove.
     */
    public synchronized void deleteField(IElementField field) {
        validateField((ElementField)field);
        fieldByName.remove(field.getName());
        ((ElementField)field).setDeleted(true);
    }

    /**
     * @see IMElementType#renameField(IElementField, String)
     */
    public synchronized void renameField(IElementField field, String name) {
        validateField((ElementField)field);
        for (IElementField otherField : fields) {
            ElementField other = (ElementField)otherField;
            if (other.getName().equalsIgnoreCase(name) ) {
                if (!other.isDeleted() || other.getType() == field.getType()) {
                    throw new IllegalArgumentException(
                        other.isDeleted() ?
                            "Renaming conflicts with a deleted field of the same type"
                        :   "Duplicate field names are not allowed"
                    );
                }
            }
        }
        ((ElementField)field).setName(name);
    }

    /**
     * @see IMElementType#setFieldLabel(IElementField, String)
     */
    public synchronized void setFieldLabel(IElementField field, String label) {
        validateField((ElementField)field);
        ((ElementField)field).setLabel(label);
    }

    /**
     * Returns the name of this element type.
     * @return the name of this element type.
     */
    public String getName() {
        return name;
    }

    /**
     * Package-private ID getter for use by Hibernate.
     */
    Long getId() {
        return id;
    }

    /**
     * Package-private ID setter for use by Hibernate.
     */
    void setId( Long id ) {
        this.id = id;
    }

    /**
     * Package-private version getter for use by Hibernate.
     */
    int getVersion() {
        return version;
    }

    /**
     * Package-private version setter for use by Hibernate.
     */
    void setVersion( int version ) {
        this.version = version;
    }

    /**
     * Obtains a string representation of this type.
     * @return String representation of this type.
     */
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append(name);
        res.append(" { ");
        boolean first = true;
        for (IElementField f : fields) {
            if (!first) {
                res.append(", ");
            } else {
                first = false;
            }
            res.append(f);
        }
        res.append(" }");
        return res.toString();
    }

    /**
     * Obtains the hash code for this class.
     * @return the hash code for this class.
     */
    public int hashCode() {
        return ObjectHelper.nullSafeHashCode(id);
    }

    /**
     * Checks if this instance equals another instance.
     * @param the other object.
     * @return true if this object equals the other object,
     * false otherwise.
     */
    public boolean equals(Object other) {
        if (other instanceof ElementType) {
            ElementType rhs = (ElementType)other;
            if ( id != null && rhs.id != null ) {
                return id.equals(rhs.id);
            } else {
                return this == rhs;
            }
        } else {
            return false;
        }
    }

    /**
     * validates that the specified field belongs to this type.
     * @param field the field to be validated.
     * @throws IllegalArgumentException if the field does not belong
     * to this type.
     */
    private void validateField(ElementField field) {
        if (!this.equals(field.getParentType())) {
            throw new IllegalArgumentException("Unable to modify a field that belongs to a differen type");
        }
    }

}
