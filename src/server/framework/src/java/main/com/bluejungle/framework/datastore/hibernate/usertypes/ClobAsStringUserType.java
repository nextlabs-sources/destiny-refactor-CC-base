/*
 * Created on Feb 20, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OraclePreparedStatement;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.lob.ClobImpl;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/ClobAsStringUserType.java#1 $
 */

public class ClobAsStringUserType implements UserType {

	private static final long serialVersionUID = 1L;

    /** Represents the SQL type of the field to which we map (String). */
    private static final int[] SQL_TYPES = { Types.CLOB };

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (x != null && y != null) {
			return x.equals(y);
		} else {
			return false;
		}
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		Clob clob = rs.getClob(names[0]);
		if (clob != null && !rs.wasNull()) {
			return clob.getSubString(1, (int) clob.length());
		} else {
			return null;
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value != null) {
			if (st instanceof OraclePreparedStatement) {
				((OraclePreparedStatement) st).setStringForClob(index, (String) value);
			} else {
				st.setClob(index, new ClobImpl2((String) value));
			}
		} else {
			st.setNull(index, SQL_TYPES[0]);
		}
	}

	public Class returnedClass() {
		return String.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}
	
	/**
	 * From hibernate3 implementation
	 * this class needs to be upgraded when we upgrade to hibernate 3
	 * 
	 * @author hchan
	 *
	 */
	private class ClobImpl2 extends ClobImpl {
		private boolean needsReset = false;

		public ClobImpl2(String string) {
			super(string);
		}

		public ClobImpl2(Reader reader, int length) {
			super(reader, length);
		}

		private Reader getReader() throws SQLException {
			return super.getCharacterStream();
		}

		public Reader getCharacterStream() throws SQLException {
			final Reader reader = getReader();
			try {
				if (needsReset)
					reader.reset();
			} catch (IOException ioe) {
				throw new SQLException("could not reset reader");
			}
			needsReset = true;
			return reader;

		}

		public InputStream getAsciiStream() throws SQLException {
			final Reader reader = getReader();
			try {
				if (needsReset)
					reader.reset();
			} catch (IOException ioe) {
				throw new SQLException("could not reset reader");
			}
			needsReset = true;
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return reader.read();
				}
			};
		}
	}
}