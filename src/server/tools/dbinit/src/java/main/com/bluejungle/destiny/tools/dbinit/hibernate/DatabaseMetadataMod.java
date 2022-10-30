package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class DatabaseMetadataMod extends net.sf.hibernate.tool.hbm2ddl.DatabaseMetadata {
//	private static final String[] ALL_KNOWN_TYPES = { "TABLE", "VIEW", "SYSTEM TABLE",
//			"GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM" };
//	private static final String[] ALL_TYPES = null;

	// java.sql.DatabaseMetaData
	private DatabaseMetaData meta;
	private DialectExtended dialectX;
	
	public DatabaseMetadataMod(Connection connection, DialectExtended dialectX) throws SQLException {
		super(connection, dialectX);
		meta = connection.getMetaData();
		this.dialectX = dialectX;
	}
	
	public DatabaseMetaData getSqlDatabaseMetaData(){
		return meta;
	}
	
	public List<String> getAllTableNames() throws SQLException {
        return getAllTables(new String[] { "TABLE" });
	}
	
	public List<String> getAllViewNames() throws SQLException {
        return getAllTables(new String[] { "VIEW" });
	}
	
	public List<String> getAllTables(String[] type) throws SQLException {
        String schema = dialectX.getTableSchema(meta);
        
        //copy from Hibernate 2.1.8
        List<String> tables = new ArrayList<String>();
        ResultSet rs = null;
        try {
            String name = null;
            String catalog = null;
            //all field is null, so doesn't matter if they are upper/lower/mixed case
            rs = meta.getTables(catalog, schema, name, type);
            while (rs.next()) {
                String tableName = rs.getString(ResultSetKey.TABLE_NAME);
                
                //if oracle and start wit "BIN$", don't add. it is a recycled item
                if (!dialectX.isTableBlackListed(tableName)) {
                    tables.add(tableName);
                }
            }
            return tables;
        } finally {
            if (rs != null)
                rs.close();
        }
    }
        
        
}
