/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ElementField.java#1 $
 */

package com.bluejungle.dictionary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;
import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.IExpressionVisitor.Order;
import com.bluejungle.framework.utils.ObjectHelper;

/**
 * Instances of this class represent definitions of field elements.
 */
class ElementField implements IMappedElementField, Comparable<ElementField> {
    private Long id;
    private int version;
    private ElementType parentType;
    private String name;
    private String nameUpper;
    private Method getter;
    private Method setter;
    private String mapping;
    private ElementFieldType type;
    private String label;
    private boolean deleted;

    /** This package-private no-argument constructor is for Hibernate. */
    ElementField() {
    }

    /**
     * Package-private constructor for adding new instances.
     */
    ElementField( String name, ElementType parentType, ElementFieldType type ) {
        if ( name == null ) {
            throw new NullPointerException("name");
        }
        if ( parentType == null ) {
            throw new NullPointerException("parentType");
        }
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        this.name = name;
        nameUpper = name.toUpperCase();
        this.parentType = parentType;
        this.type = type;
        int baseTypeCount = 0;
        String baseType = type.getMappingBase();
        Collection<IElementField> otherFields = parentType.fields;
        for (IElementField f : otherFields) {
            if ( baseType.equals(f.getType().getMappingBase())) {
                baseTypeCount++;
            }
        }
        // The following check needs to match the number of elements of each type defined in
        // LeafElement.hbm.xml 
        int MAX_TYPES_COUNT = 20;
        if (baseTypeCount > (MAX_TYPES_COUNT - 1)) {
            throw new IllegalArgumentException("Unable to map element of type "+ type +
            		". The maximum number of " + MAX_TYPES_COUNT + " " + type + 
            		" types are already defined.");
        }
        setMapping(type.getMappingBase()+baseTypeCount/10+baseTypeCount%10);
    }

    /**
     * Returns the name of this field. 
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name of this field.
     * @param name the new name of this field.
     */
    void setName( String name ) {
        this.name = name;
        nameUpper = name.toUpperCase();
    }

    /**
     * @see IElementField#getLabel()
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this field.
     * @param label the new label of this field.
     */
    void setLabel( String label ) {
        this.label = label;
    }

    /**
     * Returns the type of this element.
     * @return the type of this element.
     */
    public ElementFieldType getType() {
        return type;
    }

    /**
     * Returns the parent type of this element.
     * @return the parent type of this element.
     */
    ElementType getParentType() {
        return parentType;
    }

    /**
     * Returns true if this is a deleted field; returns false otherwise.
     * @return true if this is a deleted field; returns false otherwise.
     */
    boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the deleted flag on this field.
     * @param deleted the new value of the deleted flag.
     */
    void setDeleted( boolean deleted) {
        this.deleted = deleted;
    }

    public Object getValue( IElement element ) {
        if ( element == null ) {
            throw new NullPointerException("element");
        }
        if ( !element.getType().equals( parentType ) ) {
            throw new IllegalArgumentException("Element [" + element.getDisplayName() + "] has type " + element.getType().getName() + " which does not match type of parent, " + parentType.getName());
        }
        try {
            return type.convertFromBase(getter.invoke(element));
        } catch ( IllegalAccessException e ) {
            throw new IllegalArgumentException("element");
        } catch ( InvocationTargetException cause ) {
            throw new IllegalArgumentException("element");
        }
    }

    public void setValue( IElement element, Object value ) {
        if ( element == null ) {
            throw new NullPointerException("element");
        }
        if ( !element.getType().equals( parentType ) ) {
            throw new IllegalArgumentException("Element [" + element.getDisplayName() + "] has type " + element.getType().getName() + " which does not match type of parent, " + parentType.getName());
        }
        try {
            setter.invoke(element, type.convertToBase(value));
        } catch ( IllegalAccessException e ) {
            throw new IllegalArgumentException("element");
        } catch ( InvocationTargetException cause ) {
            throw new IllegalArgumentException("element");
        }
    }

    /**
     * Formatted PQL string should include the object type.
     */
    public String getObjectTypeName() {
        return parentType.getName();
    }

    /**
     * Dictionary fields do not have a subtype.
     */
    public String getObjectSubTypeName() {
        return null;
    }

    /**
     * Element fields are not designed for evaluation.
     */
    public IEvalValue evaluate( IArguments arg ) {
        throw new UnsupportedOperationException("evaluate");
    }

    /**
     * Implementation of the IExpression's method.
     */
    public IRelation buildRelation( RelationOp op, IExpression rhs ) {
        return new Relation( op, this, rhs );
    }

    /**
     * Implementation of the IExpression's visitor interface.
     */
    public void acceptVisitor( IExpressionVisitor visitor, Order order ) {
        visitor.visit( this );
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
     * This is the mapping getter for use by Hibernate and the HQL formatter.
     * @return the name of the property to which the field is mapped.
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * Package-private mapping setter for use by Hibernate.
     * @param mapping the name of the property to which the field is mapped.
     */
    void setMapping( String mapping ) {
        this.mapping = mapping;
        try {
            getter = LeafElement.class.getDeclaredMethod(
                "get"+mapping
            );
            getter.setAccessible(true);
            setter = LeafElement.class.getDeclaredMethod(
                "set"+mapping
            ,   getter.getReturnType()
            );
            setter.setAccessible(true);
        } catch ( SecurityException e ) {
            throw new IllegalArgumentException("Unable to map field "+name);
        } catch ( NoSuchMethodException e ) {
            throw new IllegalArgumentException("Unable to map field "+name);
        }
    }

    /**
     * Checks if this instance equals another instance.
     * @param the other object.
     * @return true if this object equals the other object,
     * false otherwise.
     */
    public boolean equals( Object other ) {
        if ( ! (other instanceof ElementField ) || this.id == null ) {
            return other == this;
        }
        return id.equals(((ElementField)other).id);
    }

    /**
     * Obtains the hash code for this class.
     * @return the hash code for this class.
     */
    public int hashCode() {
        return ObjectHelper.nullSafeHashCode(id);
    }

    /**
     * Obtains a string representation of this field.
     * @return String representation of this field.
     */
    public String toString() {
        return "["+name+":"+type+"]";
    }

    /**
     * Compares this field to another one.
     * The ordering is consistent with equals as long as
     * there are no distinct fields with duplicate IDs.
     * Hibernate should take care of that requirement.
     * @param other the other field.
     * @return a negative number if the name of the other field is
     * alphabetically earlier than the name of this field or
     * the names are identical but the ID of the other field is smaller,
     * zero if the names are identical and the fields have the same id,
     * and a positive number otherwise.
     */
    public int compareTo(ElementField rhs) {
        if ( rhs == null ) {
            throw new NullPointerException("rhs");
        }
        int res = nameUpper.compareTo( rhs.nameUpper );
        if ( res == 0 ) {
            if ( id != null ) {
                return rhs.id == null ? 1 : id.compareTo(rhs.id);
            } else {
                return rhs.id == null ? 0 : -1;
            }
        } else {
            return res;
        }
    }

    /**
     * This package-private method is for getting the uppercased
     * name of the field. It is used in <code>ElementType</code>
     * for searching.
     * @return the name of the field in upper case.
     */
    String getCaseInsensitiveName() {
        return nameUpper;
    }

    /**
     * Hibernate user type for the type of the field
     */
    public static final Type FIELD_TYPE = makeFieldType();

    /**
     * This private method is necessary because of the need to catch
     * the <code>HibernateException</code> thrown from the Hibernate.cistom.
     * @return A custom Hibernate type associated with the anonymous class
     * for storing ElementFieldType in a two-character string field.
     */
    static private Type makeFieldType() {
        try {
            return Hibernate.custom( FieldTypeUserType.class );
        } catch ( HibernateException he ) {
            // This should never happen because everything is hard-coded.
            return null;
        }
    }

    /**
     * User type for mapping field types with Hibernate.
     */
    public static class FieldTypeUserType extends EnumUserType<ElementFieldType> {
        public FieldTypeUserType() {
            super(
                new ElementFieldType[] {
                        ElementFieldType.STRING
                    ,   ElementFieldType.CS_STRING
                    ,   ElementFieldType.STRING_ARRAY
                    ,   ElementFieldType.NUMBER
                    ,   ElementFieldType.DATE
                    ,   ElementFieldType.NUM_ARRAY
                    ,   ElementFieldType.LONG_STRING
                    },  new String[] {
                        "ST"
                    ,   "CS"
                    ,   "SA"
                    ,   "NM"
                    ,   "DT"
                    ,   "NA"
                    ,	"LS"
                    }
                ,  ElementFieldType.class
            );
        }
    }

}
