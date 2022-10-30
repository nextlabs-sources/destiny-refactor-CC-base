package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/PathUserType.java#1 $
 */

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.utils.ObjectHelper;
import com.bluejungle.framework.utils.TimeRelation;

/**
 * This class is used to map the <code>DictionaryPath</code> objects
 * for use with Hibernate as an atomic immutable data type.
 */
public class PathUserType implements CompositeUserType, Serializable {
    
    /**
     * This character is used to escape the special char in the path.
     */
    public static char ESCAPE_CHAR = '\\';
	
    /**
     * The Hibernate type for use when setting parameters of
     * queries and other prepared statements.
     */
    public static final Type TYPE;

    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(PathUserType.class);
        } catch (HibernateException exception) {
            typeCreated = null;
        }
        TYPE = typeCreated;
    }

    private static final long serialVersionUID = 1L;

    private static final int[] SQL_TYPES = { Types.VARCHAR, Types.BIGINT};

    private static final String[] PROPERTY_NAMES = { "path", "pathHash" };

    private static final Type[] PROPERTY_TYPES = { Hibernate.STRING, Hibernate.LONG};

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    public Type[] getPropertyTypes() {
        return PROPERTY_TYPES;
    }

    public Class returnedClass() {
        return DictionaryPath.class;
    }

    public boolean equals(Object x, Object y) {
        String[] lhs = null, rhs = null;
        if ( x instanceof String[] ) {
            lhs = (String[])x;
        }
        if ( y instanceof String[] ) {
            rhs = (String[])y;
        }
        return ObjectHelper.nullSafeEquals(lhs, rhs);
    }

    public Object deepCopy(Object obj) {
        return obj; // DictionaryPath is immutable
    }

    public boolean isMutable() {
        return false;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor se, Object owner) throws SQLException {
        String value = rs.getString(names[0]);
        if (rs.wasNull() || value == null) {
            return null;
        }
        String [] path = parseEscapeOnSpecialChar(value);        
        return new DictionaryPath(path);
    }

    public void nullSafeSet(PreparedStatement ps, Object val, int ind, SessionImplementor se) throws SQLException {
        if ( ( val instanceof DictionaryPath) && ( val != null ) ) {
            ps.setString(ind, escapeDictionaryPath((DictionaryPath)val));
            ps.setLong(ind+1, val.hashCode());
        } else {
            ps.setNull(ind, SQL_TYPES[0]);
            ps.setNull(ind+1, SQL_TYPES[1]);
        }
    }

    public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
        return cached;
    }

    public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
        return (Serializable)value;
    }

    public Object getPropertyValue(Object obj, int ind) {
        if (obj == null || !(obj instanceof TimeRelation)) {
            return null;
        }
        TimeRelation tr = (TimeRelation) obj;
        return (ind == 0) ? tr.getActiveFrom() : tr.getActiveTo();
    }

    public void setPropertyValue(Object obj, int ind, Object val) {
        throw new UnsupportedOperationException("DictionaryPath is immutable.");
    }
    

    static String escapeDictionaryPath(DictionaryPath path) {
        String [] pathElements = path.getPath();
        StringBuffer res = new StringBuffer();
        for ( int i = 0 ; i != pathElements.length ; i++ ) {
            res.append(DictionaryPath.SEPARATOR);
            if (i == pathElements.length-1) {
                res.append(DictionaryPath.NAME_MARKER);
            }
            res.append( escapePathOnSpecialChar( pathElements[i]) );
        }
        return res.toString();
    }
    
    /**
     * Escape special char [ '\', ',', '>' ] in Dictionary path element
     * ',' and '>' are sepcial char in LDAP DN, we are using that for our seperator
     * Those chars have to be escaped for distingish with DN data value
     * 
     * @return String with sepcial char 
     */

    private static String escapePathOnSpecialChar (String input ) {              
        if ( ( input != null ) && ( input.length() > 0 ) ) {
            StringBuffer output = new StringBuffer(input.length());           
            for( int i = 0; i<input.length(); i++ ) {
                char c = input.charAt(i); 
                if ( ( c == ESCAPE_CHAR ) || 
                     ( c == DictionaryPath.NAME_MARKER ) || 
                     ( c == DictionaryPath.SEPARATOR ) ) {
                     output.append(ESCAPE_CHAR);
                } 
                output.append(c);
            }
            return output.toString();
        }
        // only when input is null or empty
        return input;
    }
   
    /**
     * Parse path with special char escaped in method escapePathOnSpecialChar()
     * Input is a string with sepcail char escaped
     * 
     * @return parsed path element in String[]
     */
    private static String[] parseEscapeOnSpecialChar( String path) {
        if( path == null ) {
            return null;
        }
        List<String> res = new ArrayList<String>();
        StringBuffer tmp = null;
        if (path.length() == 0 || path.charAt(0) != DictionaryPath.SEPARATOR) {
            throw new IllegalArgumentException("path must start in '"+ DictionaryPath.SEPARATOR+"'");
        }
        for ( int i = 1 ; i != path.length() ; i++ ) {
            if ( tmp == null ) {
                tmp = new StringBuffer();
            }
            char ch = path.charAt(i);
            if ( ch == ESCAPE_CHAR ) {
                i++;
                ch = path.charAt(i);
                tmp.append(ch);
                continue;
            }
            if ( ch == DictionaryPath.SEPARATOR ) {
                res.add(tmp.toString());
                tmp = null;
            } else if (ch != DictionaryPath.NAME_MARKER) {    
                tmp.append(ch);
            }
        }
        if ( tmp != null ) {
            res.add(tmp.toString());
        }
        return res.toArray(new String[res.size()]);
    }
}
