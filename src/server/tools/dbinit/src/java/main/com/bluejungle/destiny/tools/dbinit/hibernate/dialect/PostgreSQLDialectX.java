/*
 * Created on Dec 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/PostgreSQLDialectX.java#1 $
 */

class PostgreSQLDialectX extends DialectExtended {
	/**
	 * Constructor
	 * @param dialect
	 */
	PostgreSQLDialectX(Dialect dialect) {
		super(dialect);
	}

	private static final List<Set<Short>> postgresDialectTypesMap = new ArrayList<Set<Short>>();

	static {
		Set<Short> list = new TreeSet<Short>();
		list.add((short) Types.SMALLINT);
		list.add((short) Types.TINYINT);
		postgresDialectTypesMap.add(list);

		list = new TreeSet<Short>();
		list.add((short) Types.FLOAT);
		list.add((short) Types.REAL);
		postgresDialectTypesMap.add(list);

		list = new TreeSet<Short>();
		list.add((short) Types.VARBINARY);
		list.add((short) Types.BINARY);
		list.add((short) Types.BLOB);
		postgresDialectTypesMap.add(list);

		list = new TreeSet<Short>();
		list.add((short) Types.VARCHAR);
		list.add((short) Types.CLOB);
		postgresDialectTypesMap.add(list);
	}

	@Override
	public String getDefaultValue(short dataType) {
		switch (dataType) {
		case Types.BIT:
		case Types.BOOLEAN:	//unmapped
			return "false";
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
		case Types.INTEGER:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.NUMERIC:
			return "0";
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.VARBINARY:
		case Types.BLOB:
		case Types.CLOB:
			return "''";
		case Types.DATE:
		case Types.TIMESTAMP:
			return "'0000-1-1'";
		case Types.TIME:
			return "'00:00'";
		default:
			throw new RuntimeException("Type is invalid " + dataType + " in " + this.toString());
		}
	}

	@Override
	public boolean isSameLength(ColumnM dbColumnM, ColumnM hibColumnM) {
		 if (dbColumnM.getColumnSize() != hibColumnM.getColumnSize()) {
            //length is different

            short dbColumnDataType = dbColumnM.getDataType();

            // in postgre, hibernate.clob == database.varchar(-1 or 2147483647)
            // in postgre 8.3, the value is 2147483647
            // probably in 8.0, it is -1
            if(      hibColumnM.getDataType() == Types.CLOB 
                  && dbColumnDataType == Types.VARCHAR 
                  && (   dbColumnM.getColumnSize() == -1 
                      || dbColumnM.getColumnSize() == 2147483647
                     )
            ){
                return true;
            }

            //this item could be a custom type
            String hibCustomType = hibColumnM.getSqlTypeName();

            //if custom type is not found, get type from dialect
            String hibTypeName = hibCustomType == null
                    ? hibColumnM.getDialectType(this)
                    : hibCustomType;

            //dirty fix of char type in dialect
            String dbTypeName;
            if( dbColumnM.getDataType() == Types.CHAR){
                //this Types.CHAR has length.
                //hibernate(specially on dialect) thinks Types.CHAR can only have length one char(1),
                //how ever, it could be a case that you set a custom type in hibernate which is char(2).
                //when I retrieve the type, it is char(2). But if I use the dialect, it can only return char(1)
                dbTypeName = "char("+dbColumnM.getColumnSize()+")";
            }else{
//	                dbTypeName = dbColumnM.getSqlTypeName();
                dbTypeName = dbColumnM.getDialectType(this);
            }

            
//	            if(dbTypeName == null){
//	                //log this, but this should not happen.
//	                //probably only on test case, the sql type is null
//	                dbTypeName = dbColumnM.getDialectType(d);
//	            }
            return dbTypeName.equalsIgnoreCase(hibTypeName);
        }else{
            return true;
        }
	}

	@Override
	public boolean isSameType(short c1DataType, short c2DataType) {
		if (c1DataType != c2DataType) {
			for (Set<Short> typeSet : postgresDialectTypesMap) {
				if (typeSet.contains(c1DataType) && typeSet.contains(c2DataType)) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public String getAlterColumnString() {
		return "ALTER COLUMN";
	}

	@Override
	public String getDropColumnString() {
		return "DROP";
	}
	
	@Override
	public String sqlAlterColumnType(String tableName, String colname, String newType) {
		return ALTER_TABLE + tableName + " " + this.getAlterColumnString() + " " + colname
				+ " TYPE " + newType;
	}
	
	@Override
	public String getSetNullableString(String sameType, boolean setNull) {
		return (setNull ? " DROP" : " SET") + " NOT NULL";
	}
	
	@Override
	public boolean isSequenceExist(Connection connection, String name) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + name);
			return rs.next();
		} catch (SQLException e) {
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					//ignore
				}
			}
		}
	}
	
	@Override
	public String getLengthString() {
		return "LENGTH";
	}

    @Override
    public String sqlRebuildIndex(String indexName, String tableName) {
        // http://www.postgresql.org/docs/8.0/interactive/sql-reindex.html
        // REINDEX { DATABASE | TABLE | INDEX } name [ FORCE ]
        //TODO beware quoted name
        return "REINDEX INDEX " + indexName;
    }
}
