/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/MassDML.java#1 $
 */

package com.bluejungle.dictionary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.datastore.hibernate.utils.MassDMLUtils;
import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * This utility class is used to supplement Hibernate 2.x
 * with mass update functionality, part of which is available
 * in version 3.x.
 *
 * In addition, this utility class provides methods for
 * formatting SQL statements (adding IN lists, replacing
 * sequence variables, etc.)
 *
 * TODO: Refactor the uses of this class when we switch to Hibernate 3.x.
 */

class MassDML {
	private static final Log LOG = LogFactory.getLog(MassDML.class);
	
    private static final Object[] EMPTY_ARGS = new Object[0];

    private static final Type[] EMPTY_TYPES = new Type[0];

    /**
     * This helper method expects a SQL string formatted as follows:
     * DELETE FROM <table> WHERE ID IN #
     * or
     * UPDATE <table> SET <field>=? WHERE ID IN #
     * The method adds a JDBC argument list, binds the IDs to it,
     * and executes the resulting update or delete statement.
     * 
     * @param session A Hibernate session to use.
     * @param sqlSeed the SQL formatted as above.
     * @param params the parameter values for the SQL.
     * @param types Hibernate types of the parameters.
     * @param ids a <code>Collection</code> of ID objects.
     * @param idType the type of the ID objects.
     * @return the number of rows changed as the result of executing
     * the statement.
     * @throws HibernateException when the operation cannot complete
     * because of a SQLException.
     */
    public static int updateOrDelete(Session session, String sqlSeed, Object[] params,
			Type[] types, Collection<Long> ids, Type idType) throws HibernateException {
    	//TODO check <code>params</code> length, make sure it doesn't over the database parameter limit
    	
        nullCheck(ids, "ids");
        nullCheck(session, "session");
        if (!session.isOpen()) {
            throw new IllegalArgumentException("session is closed");
        }
        
        // We are about to access DB through the backdoor (as far as Hibernate is concerned),
        // so we need to flush the session to make previous updates visible to SQL.
        session.flush();

        Connection conn = session.connection();
        nullCheck(conn, "session.connection");
        nullCheck(sqlSeed, "sql");
        if ( params == null ) {
            params = EMPTY_ARGS;
        }
        if (types == null) {
            types = EMPTY_TYPES;
        }
        if (params.length != types.length) {
            throw new IllegalArgumentException("params.length != types.length");
        }
        nullCheck(idType, "id type");
        
        if(LOG.isDebugEnabled()){
        	LOG.debug(sqlSeed 											+ ConsoleDisplayHelper.NEWLINE + 
        			"size of ids = "+ ids.size() 						+ ConsoleDisplayHelper.NEWLINE +
        			"params = " 	+ ArrayUtils.asString(params, ",") 	+ ConsoleDisplayHelper.NEWLINE +
        			"types = " 		+ ArrayUtils.asString(types, ",") 	+ ConsoleDisplayHelper.NEWLINE +
        			"ids.size() = " + ids.size() 						+ ConsoleDisplayHelper.NEWLINE +
        			"idType = " 	+ idType 							+ ConsoleDisplayHelper.NEWLINE +
        			"called by = " 	+ Thread.currentThread().getStackTrace()[3]
        	);
        }
        
        int listUses = 0;
        for (int i = 0 ; i != sqlSeed.length(); i++) {
            if (sqlSeed.charAt(i)=='#') {
                listUses++;
            }
        }
        
        int rowChanged = 0;
		try {
			final Long[] idsArray = ids.toArray(new Long[ids.size()]);
			
			//ids could be empty!!!
			for (int idsIndex = 0; idsIndex < ids.size() || ids.size() == 0; idsIndex += HibernateUtils.DATABASE_PARAMETER_LIMIT) {
				int size = Math.min(ids.size() - idsIndex, HibernateUtils.DATABASE_PARAMETER_LIMIT);

				String sql = sqlSeed.replaceAll("#", makeInList(size));

				PreparedStatement ps = conn.prepareStatement(sql);
				try {
					// Set the non-ID parameters, if any
					int i = 1;
					for (int j = 0; j != params.length; i++, j++) {
						types[j].nullSafeSet(ps, params[j], i, null);
					}
					for (int listCount = 0; listCount != listUses; listCount++) {
						// Set the ID parameters
						for (int subIdsIndex = idsIndex; subIdsIndex < idsIndex + size; subIdsIndex++) {
							idType.nullSafeSet(ps, idsArray[subIdsIndex], i++, null);
						}
					}
					rowChanged += ps.executeUpdate();
				} finally {
					ps.close();
				}
				// stop if the ids is empty
				if (ids.size() == 0) {
					break;
				}
			}
		} catch (SQLException cause) {
			throw new HibernateException("Unable to mass-update or delete", cause);
		}
		return rowChanged; 
        
    }

    /**
     * Execute a self-contained insert statement, possibly with references to
     * sequence-generated id columns.
     * This method expects SQL of the form
     * "INSERT INTO ($field$ f1,f2,...) (SELECT #sequence# v1, v2,...)".
     * The $field$ represents the field name into which the sequence
     * is to be inserted.
     * The #sequence# represents the name of the corresponding sequence.
     * If present, these constructs should not be followed by ',' commas.
     *
     * The method prepares the statement, binds the parameters to it,
     * and then executes and closes the resulting prepared statement.
     *
     * @param session A Hibernate session to use.
     * @param sqlSeed the SQL formatted as above.
     * @param params the parameter values for the SQL.
     * @param types Hibernate types of the parameters.
     * @throws HibernateException when the operation cannot complete
     * because of a SQLException.
     */
    public static int insert(Session session, String target, String sourceFields, String source,
			Object[] params, Type[] types) throws HibernateException {
        nullCheck(session, "session");
        if (!session.isOpen()) {
            throw new IllegalArgumentException("session is closed");
        }

        // We are about to access DB through the backdoor (as far as Hibernate is concerned),
        // so we need to flush the session to make previous updates visible to SQL.
        session.flush();

        Connection conn = session.connection();
        nullCheck(conn, "session.connection");
        nullCheck(source, "source");
        nullCheck(sourceFields, "sourceFields");
        nullCheck(target, "target");
        if ( params == null ) {
            params = EMPTY_ARGS;
        }
        if ( types == null ) {
            types = EMPTY_TYPES;
        }
        if (params.length != types.length) {
            throw new IllegalArgumentException("params.length != types.length");
        }
        for ( int i = 0 ; i != types.length ; i++ ) {
            nullCheck(types[i], "types[]");
        }
        IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(session);
        try {
            PreparedStatement ps = conn.prepareStatement(
                formatter.formatInsert(target, sourceFields, source)
            );
            try {
                for ( int i = 0 ; i != params.length ; i++ ) {
                    types[i].nullSafeSet(ps, params[i], i+1, null);
                }
                return ps.executeUpdate();
            } finally {
                ps.close();
            }
        } catch ( SQLException cause ) {
            throw new HibernateException("Unable to mass-update", cause);
        }
    }

    /**
     * Makes a string of the form "(?,?,...,?)" with <code>size</code>
     * question marks.
     * @param size The number of question marks to insert.
     * The value of this argument may not be negative.
     * When the value is zero, the string "(null)" is returned.
     * @return A string of <code>size</code> comma-separated
     * question marks suitable for use as an in-list in a query.
     */
    public static String makeInList(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size may not be negative: "+size);
        }
        if (size == 0) {
            return "(null)";
        }
        StringBuffer res = new StringBuffer("(");
        for ( int i = 0 ; i != size ; i++) {
            if (i!=0) {
                res.append(',');
            }
            res.append('?');
        }
        res.append(')');
        return res.toString();
    }

    /**
     * Null-checks the object and throws <code>NullPointerException</code>
     * with the specified message if necessary.
     * @param obj the object to check for <code>null</code>.
     * @param message the message to pass to <code>NullPointerException</code>
     * if the object is <code>null</code>.
     * @throws NullPointerException with the specified <code>message</code>
     * if the <code>obj</code> is <code>null</code>.
     */
    private static void nullCheck(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

}
