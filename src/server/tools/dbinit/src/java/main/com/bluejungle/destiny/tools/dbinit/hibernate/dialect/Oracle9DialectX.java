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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/Oracle9DialectX.java#1 $
 */

class Oracle9DialectX extends DialectExtended{
	/**
	 * Constructor
	 * @param dialect
	 */
	Oracle9DialectX(Dialect dialect) {
		super(dialect);
	}

	private static final List<Set<Short>> oracle9DialectTypesMap =
        new ArrayList<Set<Short>>();

    static{
        Set<Short> list = new TreeSet<Short>();
        list.add((short)Types.BIT );
        list.add((short)Types.BIGINT );
        list.add((short)Types.SMALLINT );
        list.add((short)Types.TINYINT );
        list.add((short)Types.INTEGER );
        list.add((short)Types.NUMERIC );
        list.add((short)Types.DECIMAL );
        oracle9DialectTypesMap.add(list);
        
        list = new TreeSet<Short>();
        list.add((short)Types.FLOAT );
        list.add((short)Types.DOUBLE );
        oracle9DialectTypesMap.add(list);

        list = new TreeSet<Short>();
        list.add((short)Types.DATE );
        list.add((short)Types.TIME );
        list.add((short)Types.TIMESTAMP );
        oracle9DialectTypesMap.add(list);
    }
	
    @Override
	public boolean doesTypeHaveLength(short type) {
		if(super.doesTypeHaveLength(type)){
			return true;
		}else{
			return (type == Types.BIT 
					|| type == Types.BIGINT 
					|| type == Types.SMALLINT
					|| type == Types.TINYINT 
					|| type == Types.INTEGER 
					|| type == Types.NUMERIC 
					|| type == Types.DECIMAL);
		}
	}

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
			//default format for DATE is "DD-MON-YY".
			return "'01-JAN-00'";
		default:
			throw new IllegalArgumentException("Type is invalid " + dataType + " in " + this.toString());
		}
	}

	@Override
	public boolean isSameLength(ColumnM dbColumnM, ColumnM hibColumnM) {
		if (dbColumnM.getColumnSize() != hibColumnM.getColumnSize()) {
            //length is different

            //this item could be a custom type
            String hibTypeName = hibColumnM.getSqlTypeName();

            //if custom type is not found, get type from dialect
            if(hibTypeName == null) {
            	hibTypeName = hibColumnM.getDialectType(this);
            }

            String dbTypeName = dbColumnM.getDialectType(this);
            
            if(dbTypeName != null ) {
            	return dbTypeName.equalsIgnoreCase(hibTypeName);
            } else 
            	return false;
            
        }else{
            return true;
        }
	}

	@Override
	public boolean isSameType(short c1DataType, short c2DataType) {
		if (c1DataType != c2DataType) {
            for(Set<Short> typeSet :oracle9DialectTypesMap){
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
    public int getColumnLength(int reportedLength, short dataType){
        int length = reportedLength;
        if(length == HIBERNATE_DEFAULT_LENGTH ){
            switch(dataType){
            case Types.BIT:			length = 1;		break;
            case Types.BIGINT:		length = 19;	break;
            case Types.SMALLINT:	length = 5;		break;
            case Types.TINYINT :	length = 3;		break;
            case Types.INTEGER:		length = 10;	break;
            }
        }
        return length;
    }
    
    @Override
    public String getTableSchema(DatabaseMetaData meta) throws SQLException {
		return DatabaseHelper.matchToDbStoreCase(meta.getUserName());
	}

	@Override
	public String getAlterColumnString() {
		return "MODIFY";
	}

	@Override
	public String getDropColumnString() {
		return "DROP COLUMN";
	}
	
	@Override
	public String sqlAlterColumnType(String tableName, String colname, String newType) {
		return ALTER_TABLE + tableName + " " + this.getAlterColumnString() + " " + colname + " "
				+ newType;
	}

	@Override
	public String getSetNullableString(String sameType, boolean setNull) {
        //ALTER TABLE "ACTIVITY"."CACHED_APPLICATION" MODIFY ( "NAME" NULL )
        //ALTER TABLE "ACTIVITY"."CACHED_APPLICATION" MODIFY ( "NEW_COLUMN" NOT NULL )
        return (setNull ? " " : " NOT ") + "NULL";
	}
	
	@Override
	public boolean isSequenceExist(Connection connection, String name){
        Statement statement = null;
        try {
            statement = connection.createStatement();
            try {
                ResultSet rs = statement.executeQuery("SELECT " +name +".currval FROM dual");
                return rs.next();
            } catch (SQLException e) {
                ResultSet rs = statement.executeQuery("SELECT " +name +".nextval FROM dual");
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        } finally{
            if(statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

	@Override
	public boolean isTableBlackListed(String tableName) {
		return super.isTableBlackListed(tableName) || tableName.startsWith("BIN$");
	}
	
	@Override
	public String getLengthString() {
		return "LENGTH";
	}

    @Override
    public String sqlRebuildIndex(String indexName, String tableName) {
        // http://stanford.edu/dept/itss/docs/oracle/10g/server.101/b10759/statements_1008.htm
        // ALTER INDEX name REBUILD { PARALLEL | ONLINE };
        // Parallel DML is not supported during online index building. 
        // If you specify ONLINE and then issue parallel DML statements, 
        //   Oracle Database returns an error.
        // You cannot specify ONLINE for a bitmap index or a cluster index.
        
        return "ALTER INDEX " + indexName + " REBUILD";
    }
}
