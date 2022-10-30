/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate.dialect;

import java.sql.Connection;

import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ConstraintM;

/**
 * Use this class for a dialect that hasn't been extended.
 * This class is useful for install a new database since they don't use any extended method.
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/UnsupportedDialectX.java#1 $
 */
@SuppressWarnings("unused")
public class UnsupportedDialectX extends DialectExtended{
	public UnsupportedDialectX(Dialect dialect) {
		super(dialect);
	}

	@Override
	public String getAlterColumnString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDefaultValue(short dataType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDropColumnString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSetNullableString(String sameType, boolean setNull) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSameLength(ColumnM dbColumnM, ColumnM hibColumnM) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSameType(short c1DataType, short c2DataType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSequenceExist(Connection connection, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String sqlAddUnique(String tableName, String name, String columns) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String sqlAlterColumnType(String tableName, String colname, String newType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTableBlackListed(String tableName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLengthString() {
		throw new UnsupportedOperationException();
	}

    @Override
    public String sqlRebuildIndex(String indexName, String tableName) {
        throw new UnsupportedOperationException();
    }
}
