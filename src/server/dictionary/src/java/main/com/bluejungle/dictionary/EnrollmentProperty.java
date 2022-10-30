/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnrollmentProperty.java#1 $
 */

package com.bluejungle.dictionary;

import java.io.IOException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.type.Type;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * Instances of this class represent individual enrollment properties.
 */
class EnrollmentProperty implements IEnrollmentProperty {
    Long id;
    int version;
    Enrollment enrollment;
    private String name;
    EnrollmentPropertyType type;
    String value;

    EnrollmentProperty() {
    }

    EnrollmentProperty( String name, Enrollment enrollment, EnrollmentPropertyType type ) {
        if ( name == null ) {
            throw new NullPointerException("name");
        }
        if ( enrollment == null ) {
            throw new NullPointerException("enrollment");
        }
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        this.name = name;
        this.enrollment = enrollment;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public EnrollmentPropertyType getType() {
        return type;
    }

    /**
     * @see IEnrollmentProperty#getString()
     */
    public String getString() {
        if ( type != EnrollmentPropertyType.STRING ) {
            throw new UnsupportedOperationException("getString");
        }
        return value;
    }

    /**
     * @see IEnrollmentProperty#setString(String)
     */
    public void setString( String strValue ) {
        if ( type != EnrollmentPropertyType.STRING ) {
            throw new UnsupportedOperationException("setString");
        }
        value = strValue;
    }

    /**
     * @see IEnrollmentProperty#getStrArray()
     */
    public String[] getStrArray() {
        if ( type != EnrollmentPropertyType.STRING_ARRAY ) {
            throw new UnsupportedOperationException("getStrArray");
        }
        return MultivalueHelper.splitElements(value);
    }

    /**
     * @see IEnrollmentProperty#setStrArray(String[])
     */
    public void setStrArray( String[] strValue ) {
        if ( type != EnrollmentPropertyType.STRING_ARRAY ) {
            throw new UnsupportedOperationException("setStrArray");
        }
        value = MultivalueHelper.joinElements(strValue);
    }

    /**
     * @see IEnrollmentProperty#getNumber()
     */
    public long getNumber() {
        if ( type != EnrollmentPropertyType.NUMBER ) {
            throw new UnsupportedOperationException("getNumber");
        }
        if ( value == null ) {
            return 0;
        }
        try {
            return Long.parseLong( value );
        } catch (NumberFormatException cause ) {
            throw new IllegalStateException("Unable to get a numeric value.");
        }
    }

    /**
     * @see IEnrollmentProperty#setNumber(long)
     */
    public void setNumber( long numValue ) {
        if ( type != EnrollmentPropertyType.NUMBER ) {
            throw new UnsupportedOperationException("setNumber");
        }
        value = Long.toString(numValue);
    }

    /**
     * @see IEnrollmentProperty#getBinary()
     */
    public byte[] getBinary() {
        if ( type != EnrollmentPropertyType.BINARY ) {
            throw new UnsupportedOperationException("getBinary");
        }
        if ( value == null ) {
            return null;
        }
        try {
            return new BASE64Decoder().decodeBuffer( value );
        } catch (IOException cause ) {
            throw new IllegalStateException("Unable to get a binary value.");
        }
    }

    /**
     * @see com.bluejungle.dictionary.IEnrollmentProperty#setBinary(byte[])
     */
    public void setBinary( byte[] binValue ) {
        if ( type != EnrollmentPropertyType.BINARY ) {
            throw new UnsupportedOperationException("setBinary");
        }
        if ( binValue != null )  {
            value = new BASE64Encoder().encode(binValue);
        } else {
            value = null;
        }
    }

    /**
     * Hibernate user type for the type of the field
     */
    public static final Type FIELD_TYPE = makeFieldType();

    /**
     * This private method is necessary because of the need to catch
     * the <code>HibernateException</code> thrown from the Hibernate.cistom.
     * @return A custom Hibernate type associated with the anonymous class
     * for storing enrollment property type in a two-character string field.
     */
    static private Type makeFieldType() {
        try {
            return Hibernate.custom( PropertyTypeUserType.class );
        } catch ( HibernateException he ) {
            // This should never happen because everything is hard-coded.
            return null;
        }
    }

    /**
     * User type for mapping field types with Hibernate.
     */
    public static class PropertyTypeUserType extends EnumUserType<EnrollmentPropertyType> {
        public PropertyTypeUserType() {
            super(
                new EnrollmentPropertyType[] {
                    EnrollmentPropertyType.STRING
                ,   EnrollmentPropertyType.NUMBER
                ,   EnrollmentPropertyType.BINARY
                ,   EnrollmentPropertyType.STRING_ARRAY
                },  new String[] {
                    "ST"
                ,   "NM"
                ,   "BI"
                ,   "SA"
                }
                ,  EnrollmentPropertyType.class
            );
        }
    }

	@Override
	public String toString() {
		return name + " - " + value;
	}
    
    
}
