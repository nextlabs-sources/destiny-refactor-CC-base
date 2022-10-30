/*
 * Created on Aug 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;

import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;

/**
 * A Hibernate UserType which Serializes and Deserializes a com.bluejungle.version.IVersion 
 * to and from the DB as a String
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/VersionUserType.java#1 $
 */

public class VersionUserType implements UserType, Serializable {

    private static final int[] SQL_TYPES = { Types.VARCHAR };
    
    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * @see net.sf.hibernate.UserType#returnedClass()
     */
    public Class returnedClass() {
        return IVersion.class;
    }

    /**
     * @see net.sf.hibernate.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object arg0, Object arg1) throws HibernateException {
        boolean valueToReturn = false;
        if (arg0 == arg1) {
            valueToReturn = true;
        } else if ((arg0 != null) && (arg1 != null)) {
            valueToReturn = arg0.equals(arg1);
        }
        return valueToReturn;
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        IVersion valueToReturn = null;
        String versionString = (String) Hibernate.STRING.nullSafeGet(rs, names[0]);

        if (versionString != null) {             
            String majorString = versionString.substring(0, versionString.indexOf("."));
            versionString = versionString.substring(versionString.indexOf(".")+1);
            String minorString = versionString.substring(0, versionString.indexOf("."));
            versionString = versionString.substring(versionString.indexOf(".")+1);
            String maintenanceString = versionString.substring(0, versionString.indexOf("."));
            versionString = versionString.substring(versionString.indexOf(".")+1);
            String patchString = versionString.substring(0, versionString.indexOf("."));
            versionString = versionString.substring(versionString.indexOf(".")+1);
            String buildString = versionString;
            
            valueToReturn = new VersionDefaultImpl(Integer.parseInt(majorString), 
                    							   Integer.parseInt(minorString), 
                    							   Integer.parseInt(maintenanceString), 
                    							   Integer.parseInt(patchString), 
                    							   Integer.parseInt(buildString));
        }
        return valueToReturn;
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        IVersion versionObject = (VersionDefaultImpl) value;
        String valueToSet = "";
        valueToSet += versionObject.getMajor() + ".";
        valueToSet += versionObject.getMinor() + ".";
        valueToSet += versionObject.getMaintenance() + ".";
        valueToSet += versionObject.getPatch() + ".";
        valueToSet += versionObject.getBuild();
        Hibernate.STRING.nullSafeSet(st, valueToSet, index);
    }

    /**
     * @see net.sf.hibernate.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object arg0) throws HibernateException {
        return arg0; //It is immutable
    }

    /**
     * @see net.sf.hibernate.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }
}
