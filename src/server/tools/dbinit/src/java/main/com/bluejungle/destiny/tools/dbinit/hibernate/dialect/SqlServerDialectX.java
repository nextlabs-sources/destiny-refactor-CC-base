/*
 * Created on Dec 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/SqlServerDialectX.java#1 $
 */

class SqlServerDialectX extends DialectExtended {
	/**
	 * Constructor
	 * @param dialect
	 */
	SqlServerDialectX(Dialect dialect) {
		super(dialect);
	}

	private static final List<Set<Short>> msSqlDialectTypesMap =
        new ArrayList<Set<Short>>();
    
    static{
        Set<Short> list = new TreeSet<Short>();
        list.add((short)Types.BIT );
        list.add((short)Types.TINYINT );
        msSqlDialectTypesMap.add(list);

        list = new TreeSet<Short>();
        list.add((short)Types.BIGINT  );
        list.add((short)Types.NUMERIC  );
        msSqlDialectTypesMap.add(list);

        list = new TreeSet<Short>();
        list.add((short)Types.FLOAT  );
        list.add((short)Types.DOUBLE );
        msSqlDialectTypesMap.add(list);
        
        list = new TreeSet<Short>();
        list.add((short)Types.DATE  );
        list.add((short)Types.TIMESTAMP );
        list.add((short)Types.TIME );
        msSqlDialectTypesMap.add(list);
        
        list = new TreeSet<Short>();
        list.add((short)Types.BLOB  );
        list.add((short)Types.LONGVARBINARY );
        list.add((short)Types.TIME );
        msSqlDialectTypesMap.add(list);
        
        list = new TreeSet<Short>();
        list.add((short)Types.CLOB  );
        list.add((short)Types.LONGVARCHAR );
        list.add((short)Types.NVARCHAR );
        msSqlDialectTypesMap.add(list);
        
        list = new TreeSet<Short>();
        list.add((short)Types.VARCHAR);
        list.add((short)Types.NVARCHAR);
        msSqlDialectTypesMap.add(list);
    }
	
    @Override
	public String getAlterColumnString() {
		return "ALTER COLUMN";
	}

	//TODO test
	@Override
	public String getDefaultValue(short dataType) {
		switch (dataType) {
		case Types.BIT:
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
		case Types.INTEGER:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.NUMERIC:
		case Types.DECIMAL:
			return "0";
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.VARBINARY:
		case Types.BLOB:
		case Types.CLOB:
			return "' '";
		case Types.DATE:
		case Types.TIMESTAMP:
		case Types.TIME:
			//default format for DATE is "YYMMDD".
			return "'000101'";
		default:
			throw new IllegalArgumentException("Type is invalid " + dataType + " in " + this.toString());
		}
	}

	//TODO test
	@Override
	public String getDropColumnString() {
		return "DROP COLUMN";
	}

	//TODO test
	@Override
	public boolean isSameLength(ColumnM dbColumnM, ColumnM hibColumnM) {
		if (dbColumnM.getColumnSize() != hibColumnM.getColumnSize()) {
            //length is different

            //this item could be a custom type
            String hibTypeName = hibColumnM.getSqlTypeName();

            //if custom type is not found, get type from dialect
            if(hibTypeName == null){
            	hibTypeName = hibColumnM.getDialectType(this);
            }
            
            if(dbColumnM.getDataType() == Types.NVARCHAR ||
            	hibColumnM.getDataType() == Types.NVARCHAR) {
            	if(dbColumnM.getColumnSize() == 2147483647 &&
            		hibTypeName.equalsIgnoreCase("nvarchar(max)")) {
            		return true;
            	}
            }
            String dbTypeName = dbColumnM.getDialectType(this);
            
            return (dbTypeName.equalsIgnoreCase(hibTypeName));            
        }else{
            return true;
        }
	}

	 /**
     * compare two column if they have same length
     * @param dbColumnM
     * @param hibColumnM
     * @return true if they are/will be same length in database
     */
	@Override
	public boolean doesTypeHaveLength(short type) {
		return (type == Types.ARRAY 
				|| type == Types.CHAR 				
				|| type == Types.VARBINARY 
				|| type == Types.VARCHAR
				|| type == Types.NVARCHAR);
	}
	
	@Override
	public boolean isSameType(short c1DataType, short c2DataType) {
		if (c1DataType != c2DataType) {
            for(Set<Short> typeSet :msSqlDialectTypesMap){
                if(typeSet.contains(c1DataType) && typeSet.contains(c2DataType)){
                    return true;
                }
            }
            return false;
        }else{
            return true;
        }
	}

	@Override
	public String getSetNullableString(String sameType, boolean setNull) {
		return " " + sameType + (setNull ? " " : " NOT ") + "NULL";
	}

	@Override
	public boolean isSequenceExist(Connection connection, String name) {
		//do I use sequence?
		return true;
	}

	@Override
	public String sqlAlterColumnType(String tableName, String colname, String newType) {
		return ALTER_TABLE + tableName + " " + this.getAlterColumnString() + " " + colname + " "
			+ newType;
	}

	@Override
	public String getLengthString() {
		return "DATALENGTH";
	}

	@Override
	public String getSubStringString() {
		return "substring";
	}
	
	@Override
	public String sqlDropIndex(String indexName, String tableName){
		return "DROP INDEX " + openQuote() + indexName + closeQuote() 
			+ " ON " + openQuote() + tableName + closeQuote();
	}

    @Override
    public String sqlRebuildIndex(String indexName, String tableName) {
        // http://technet.microsoft.com/en-us/library/ms188388.aspx
        // ALTER INDEX indexName ON TableName REBUILD;
        return "ALTER INDEX " + indexName + " ON " + tableName + " REBUILD";
    }

    @Override
    public String getTableSchema(DatabaseMetaData meta) throws SQLException {
        return "dbo";
    }

    @Override
    public String sqlRenameColumn(String tableName, String columnName,
            String newColumnName) {
        // sp_rename 'COMPONENT.type', 'type_c', 'COLUMN';
        return "sp_rename " + openQuote() + tableName + "." + columnName + closeQuote() 
            + ", " + openQuote() + newColumnName + closeQuote() + ", 'COLUMN'";
    }
    
    
}
