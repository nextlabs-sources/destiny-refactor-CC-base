package com.bluejungle.framework.datastore.hibernate.criteria;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/criteria/CaseInsensitiveLike.java#1 $
 */

import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.TypedValue;
import net.sf.hibernate.expression.AbstractCriterion;
import net.sf.hibernate.util.StringHelper;

public class CaseInsensitiveLike extends AbstractCriterion {
    /**
     * Name of the property to which this criterion is bound.
     */
    private final String propertyName;
    /**
     * Termplate for the SQL's like, converted to lower case.
     */
    private final String template;
    /**
     * The ab% template.
     */
    private final String llp;
    /**
     * The aB% template.
     */
    private final String lup;
    /**
     * The Ab% template.
     */
    private final String ulp;
    /**
     * The AB% template.
     */
    private final String uup;
    /**
     * The ab template.
     */
    private final String ll;
    /**
     * The aB template.
     */
    private final String lu;
    /**
     * The Ab template.
     */
    private final String ul;
    /**
     * The AB template.
     */
    private final String uu;
    /**
     * Position of the '%' in the preprocessed template, or -1
     */
    private final int wildcardPos;

    /** Arguments that toSqlString passes to getCondition(). */
    private static final String[] SQL_FORMAL_ARGS = new String[] { "?", "?", "?", "?", "?" };

    /**
     * Creates a case-insensitive condition on the specified field
     * with the specified template.
     * @param propertyName name of the field to which to bind the condition.
     * @param template value of the template with which to compare the field.
     */
    public CaseInsensitiveLike( String propertyName, String template ) {
        // Check arguments - they must not be empty
        if ( propertyName == null ) {
            throw new NullPointerException( "propertyName" );
        }
        if ( template == null ) {
            throw new NullPointerException( "template" );
        }
        
        this.propertyName = propertyName;

        // Preprocess the template - remove doubled %%s
        // except when prefixed by a backslash '\'
        int pos = 0;
        do {
            pos = template.indexOf( "%%", pos );
            if ( pos == -1 ) {
                break;
            } else if ( pos == 0 ) {
                template = template.substring( 1 );
            } else if ( template.charAt(pos-1) != '\\' ) {
                template = template.substring(0,pos)+template.substring(pos+1);
            } else {
                pos++;
            }
        } while ( true );

        this.template = template.toLowerCase();
                                          
        // Determine the position of the earliest wildcard
        int ptPos = template.indexOf('%');
        int scPos = template.indexOf('_');
        if ( ptPos == -1 ) {
            wildcardPos = scPos;
        } else if ( scPos == -1 ) {
            wildcardPos = ptPos;
        } else {
            wildcardPos = Math.min( ptPos, scPos );
        }

        // Prepare the upper-lower case combinations
        char[] buf = new char[] {
            template.length() > 0 ? Character.toLowerCase( template.charAt(0) ) : '%'
        ,   template.length() > 1 ? Character.toLowerCase( template.charAt(1) ) : '%'
        ,   '%'
        };
        llp = new String( buf ).substring( 0, Math.min( template.length(), 3 ) );
        ll = new String( buf ).substring( 0, Math.min( template.length(), 2 ) );
        buf[1] = Character.toUpperCase( buf[1] );
        lup = new String( buf ).substring( 0, Math.min( template.length(), 3 ) );
        lu = new String( buf ).substring( 0, Math.min( template.length(), 2 ) );
        buf[0] = Character.toUpperCase( buf[0] );
        uup = new String( buf ).substring( 0, Math.min( template.length(), 3 ) );
        uu = new String( buf ).substring( 0, Math.min( template.length(), 2 ) );
        buf[1] = Character.toLowerCase( buf[1] );
        ulp = new String( buf ).substring( 0, Math.min( template.length(), 3 ) );
        ul = new String( buf ).substring( 0, Math.min( template.length(), 2 ) );
    }

    /**
     * Gets the name of the property to which this condition is bound.
     * @return the name of the property to which this condition is bound.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the preprocessed template (i.e. with %%s removed).
     * @return the preprocessed template (i.e. with %%s removed).
     */
    public String getTemplate() {
        return template;
    }

    /**
     * String representation of this criterion.
     */
    public String toString() {
        return propertyName + "~=~ '" + template + "'";
    }

    /**
     * Produces a SQL string corresponding to this condition.
     */
    public String toSqlString(SessionFactoryImplementor sessionFactory,
            Class persistentClass, String alias, Map aliasClasses)
    throws HibernateException {
        String[] columns = getColumns(sessionFactory, persistentClass, propertyName, alias, aliasClasses);
        if ( columns.length!=1 ) {
            throw new HibernateException(
                "Case-insensitive LIKE expression may be applied only to single-column properties: " +
                propertyName
            );
        }

        return getCondition(
            columns[0],
            SQL_FORMAL_ARGS,
            sessionFactory.getDialect().getLowercaseFunction()
        );
    }

    /**
     * Produces an array of strings to bind to the statement. 
     */
    public TypedValue[] getTypedValues(
        SessionFactoryImplementor sessionFactory, Class persistentClass,
        Map aliasClasses) throws HibernateException {
        String[] vals = getBindStrings();
        TypedValue[] res = new TypedValue[vals.length];
        for ( int i = 0 ; i != res.length ; i++ ) {
            res[i] = getTypedValue( sessionFactory, persistentClass, propertyName, vals[i], aliasClasses); 
        }
        return res;
    }

    /**
     * Produces a SQL or an HQL string, based on the value of parameters.
     * @param col The name of a SQL or an HQL column.
     * @param fp an array of five names of formal arguments to be inserted
     * into the generated condition. 
     * @param lower the database-specific name of the toLower function,
     * or "lower" if generating the HQL. 
     * @return a SQL or an HQL string, based on the value of parameters.
     */
    public String getCondition( String col, String[] fp, String lower ) {
        // Check the arguments
        if ( col == null ) {
            throw new NullPointerException( "col" );
        }
        if ( col.length() == 0 ) {
            throw new IllegalArgumentException("col is empty");
        }
        if ( fp == null ) {
            throw new NullPointerException( "fp" );
        }
        if ( fp.length != 5 ) {
            throw new IllegalArgumentException( "fp must have exactly 5 elements" );
        }
        if ( lower == null ) {
            throw new NullPointerException( "lower" );
        }
        if ( lower.length() == 0 ) {
            throw new IllegalArgumentException("lower is empty");
        }
        // Build the search string
        switch ( template.length() ) {
        case 0:
            return col+"="+fp[0]+" and "+col+" is not null";
        case 1:
            if ( wildcardPos == -1 ) {
                return new StringBuffer()
                    .append( StringHelper.OPEN_PAREN )
                    .append( StringHelper.OPEN_PAREN )
                    .append( col ).append( "=" ).append( fp[0] )
                    .append( " OR " )
                    .append( col ).append( "=" ).append( fp[1] )
                    .append( StringHelper.CLOSE_PAREN )
                    .append( " AND ")
                    .append( col ).append(" is not null ")
                    .append( StringHelper.CLOSE_PAREN )
                    .toString();
            } else {
                return "1=1";
            }
        case 2:
            if ( wildcardPos == -1 ) {
                return new StringBuffer()
                    .append( StringHelper.OPEN_PAREN )
                    .append( StringHelper.OPEN_PAREN )
                    .append( col ).append( "=" ).append( fp[0] )
                    .append( " OR " )
                    .append( col ).append( "=" ).append( fp[1] )
                    .append( " OR ")
                    .append( col ).append( "=").append( fp[2] )
                    .append( " OR ")
                    .append( col ).append( "=" ).append( fp[3] )
                    .append( StringHelper.CLOSE_PAREN )
                    .append( " AND ")
                    .append( col ).append(" is not null ")
                    .append( StringHelper.CLOSE_PAREN )
                    .toString();
            } else {
                return new StringBuffer()
                    .append( StringHelper.OPEN_PAREN )
                    .append( StringHelper.OPEN_PAREN )
                    .append( col ).append( " LIKE " ).append( fp[0] )
                    .append( " OR ")
                    .append( col ).append( " LIKE " ).append( fp[1] )
                    .append( StringHelper.CLOSE_PAREN )
                    .append( " AND ")
                    .append( col ).append(" is not null ")
                    .append( StringHelper.CLOSE_PAREN )
                    .toString();
            }
        default:
            StringBuffer buff = new StringBuffer();
            buff.append( StringHelper.OPEN_PAREN )
                .append( StringHelper.OPEN_PAREN )
                .append( col ).append( " LIKE ").append( fp[0] )
                .append( " OR ")
                .append( col ).append( " LIKE " ).append( fp[1] );
            int lastArg;
            if ( wildcardPos != 0 && wildcardPos != 1 ) {
                buff.append( " OR " )
                    .append( col ).append( " LIKE ").append( fp[2] )
                    .append( " OR ")
                    .append( col ).append( " LIKE " ).append( fp[3] );
                lastArg = 4;
            } else {
                lastArg = 2;
            }
            buff.append( StringHelper.CLOSE_PAREN )
                .append( " AND " )
                .append( lower ) 
                .append( StringHelper.OPEN_PAREN )
                .append( col )
                .append( StringHelper.CLOSE_PAREN )
                .append( wildcardPos == -1 ? "=" : " LIKE " )
                .append( fp[lastArg] )
                .append( " AND ")
                .append( col ).append(" is not null ")
                .append( StringHelper.CLOSE_PAREN );
            return buff.toString();
        }
    }

    /**
     * Produces the string values to bind
     * to the SQL statement or the HQL query.
     * @return the string values to bind
     * to the SQL statement or the HQL query.
     */
    public String[] getBindStrings() {
        switch ( template.length() ) {
        case 0: // Template is an empty string.
            return new String[] { "" };
        case 1: // Template is a one-character string.
            if ( wildcardPos == -1 ) {
                return new String[] { uu, ll };
            } else {
                return new String[0];
            }
        case 2: // Template is a two-character string.
            if ( wildcardPos == -1 ) {
                return new String[] { ll, lu, ul, uu };
            } else {
                return new String[] { ll, uu };
            }
        default: // Template has three or more characters.
            if ( wildcardPos != 0 && wildcardPos != 1 ) {
                return new String[] { llp, lup, ulp, uup, template };
            } else {
                return new String[] { llp, uup, template };
            }
        }
    }
}
