/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.connection.ConnectionProviderFactory;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.type.BooleanType;
import net.sf.hibernate.type.ByteType;
import net.sf.hibernate.type.DoubleType;
import net.sf.hibernate.type.FloatType;
import net.sf.hibernate.type.IntegerType;
import net.sf.hibernate.type.LongType;
import net.sf.hibernate.type.NullableType;
import net.sf.hibernate.type.PrimitiveType;
import net.sf.hibernate.type.ShortType;
import net.sf.hibernate.type.StringType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseMetadataMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitTestBase.java#1 $
 */

public abstract class DBInitTestBase {
	protected static final Log DEBUG_LOG = LogFactory.getLog(DBInitTestBase.class.getSimpleName() + "_DEBUG" );
	protected static final Log LOG = LogFactory.getLog(DBInitTestBase.class);
	
	protected static final String COLUMN_NAME_PREFIX = "NX";
	protected static final String COLUMN_NAME_SUFFIX = "_p";
	
	
	protected static final PrimitiveType BOOLEAN_TYPE 	= new BooleanType();
	protected static final PrimitiveType BYTE_TYPE 		= new ByteType();
	protected static final PrimitiveType SHORT_TYPE 	= new ShortType();
	protected static final PrimitiveType INTEGER_TYPE 	= new IntegerType();
	protected static final PrimitiveType LONG_TYPE 		= new LongType();
	protected static final PrimitiveType DOUBLE_TYPE 	= new DoubleType();
	protected static final PrimitiveType FLOAT_TYPE 	= new FloatType();
	protected static final NullableType STRING_TYPE 	= new StringType();
	
	
	protected static final String getCallingMethodName(){
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
	
	public static void cleanTables(DialectExtended dialect, Connection connection) throws SQLException {
		connection.commit();
		List<String> allTableNames = new DatabaseMetadataMod(connection, dialect).getAllTableNames();
		for(String createdTempTable : allTableNames){
			try {
				String sql = "drop table " + createdTempTable;
				DatabaseHelper.processSqlStatement(connection, sql);
			} catch (SQLException e) {
				LOG.info(e);
			}
		}
	}
	
	protected static DatabaseM databaseM;
	protected static Connection connection;
	protected static DialectExtended dialect;
	protected static Properties props;
	
	@Before
	public void setup() throws HibernateException, SQLException{
		if(props == null){
			props = getProperties();
			connection = ConnectionProviderFactory.newConnectionProvider(props).getConnection();
			dialect =  DialectExtended.getDialectExtended(Dialect.getDialect(props));
			
			DatabaseMetaData meta = new DatabaseMetadataMod(connection, dialect).getSqlDatabaseMetaData();
			if(meta.storesUpperCaseIdentifiers()){
				DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_UPPER_CASE);
			}else if(meta.storesLowerCaseIdentifiers()){
				DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_LOWER_CASE);
			}else if(meta.storesMixedCaseIdentifiers()){
				DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_MIXED_CASE);
			}else{
				throw new RuntimeException("What type of case does the database store identifiers?");
			}
		}
		
		if(databaseM == null){
			databaseM = new DatabaseM(getDatabaseName(), null);
		}
		
		cleanTables(dialect, connection);
	}
	
	@AfterClass
	public static void cleanAll() throws SQLException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			LOG.fatal(e);
			throw e;
		}
		connection = null;
		dialect = null;
		databaseM = null;
		props = null;
	}
	
	protected static boolean processSqlStatements(List<String> statements){
		boolean allSucess = true;
		List<SQLException> sqlExceptions = DatabaseHelper.processSqlStatements(connection, statements);
		for(SQLException sqlException : sqlExceptions){
			if(sqlException != null){
				allSucess = false;
				DEBUG_LOG.info(sqlException);
			}
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			LOG.fatal(e);
			throw new RuntimeException(e);
		}
		return allSucess;
	}
	
	protected static boolean processSqlStatement(String statement){
		boolean allSucess = true;
		try{
			DatabaseHelper.processSqlStatement(connection, statement);
			allSucess = true;
		} catch (SQLException e) {
			allSucess = false;
			LOG.fatal(e);
			throw new RuntimeException(e);
		}finally{
			try {
				connection.commit();
			} catch (SQLException e) {
				LOG.fatal(e);
				throw new RuntimeException(e);
			}
		}
		return allSucess;
	}
	
	protected abstract Properties getProperties();
	
	protected abstract String getDatabaseName();
}
