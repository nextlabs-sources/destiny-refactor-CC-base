/*
 * Created on Dec 5, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.profiling;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.connection.ConnectionProviderFactory;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import com.bluejungle.destiny.tools.dbinit.DBInitTestBase;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.MappingConstructor;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.DatabaseMetadataMod;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.MappingTest;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.ConstraintM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.TableM;
import com.bluejungle.destiny.tools.dbinit.profiling.ProfileKey;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;
import com.bluejungle.framework.utils.StringUtils;

/**
 * TODO list
 *  - check is '' == null, it is true in oracle
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/profiling/DatabaseProfiling.java#1 $
 */
public class DatabaseProfiling extends DBInitTestBase{
	private static final int DEFAULT_MAX_TRY_OPEN_CONNECTION = 500;

	private static final String DATABASE_NAME = "profiling";

	enum SizeSet{
		SMALL,
		LARGE,;
		
		//return magic number
		int getColumnSize(){
			switch (this) {
			case SMALL:
				return 29;
			case LARGE:
				return 727;
			default:
				throw new IllegalArgumentException();
			}
		}
		
		//return magic number
		int getDecimalDigits(){
			switch (this) {
			case SMALL:
				return 3;
			case LARGE:
				return 13;
			default:
				throw new IllegalArgumentException();
			}
		}
	}
	
	private static Collection<IPair<String, Integer>> allTypes;
	private static ProfileResult profileResult;
	
	@Override
	protected Properties getProperties() {
		Properties props = new Properties();
		
		int dbType = 3;
		switch(dbType){
		case 1:
			props.setProperty(Environment.URL, "jdbc:postgresql://192.168.64.130:5432/" + DATABASE_NAME);
			props.setProperty(Environment.DIALECT, net.sf.hibernate.dialect.PostgreSQLDialect.class.getName());
			props.setProperty(Environment.DRIVER, org.postgresql.Driver.class.getName());
			props.setProperty(Environment.USER,"admin");
			props.setProperty(Environment.PASS, "123blue!");
			break;
		case 2:
			props.setProperty(Environment.URL, "jdbc:oracle:thin:@192.168.64.132:1521:xe");
			props.setProperty(Environment.DIALECT, net.sf.hibernate.dialect.Oracle9Dialect.class.getName());
			props.setProperty(Environment.DRIVER, oracle.jdbc.driver.OracleDriver.class.getName());
			props.setProperty(Environment.USER,"profiling");
			props.setProperty(Environment.PASS, "123blue!");
			break;
		case 3:
			props.setProperty(Environment.URL, "jdbc:sqlserver://192.168.64.134:1433;DatabaseName="+ DATABASE_NAME);
			props.setProperty(Environment.DIALECT, com.bluejungle.framework.datastore.hibernate.dialect.SqlServer2000Dialect.class.getName());
			props.setProperty(Environment.DRIVER, com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
			props.setProperty(Environment.USER,"admin");
			props.setProperty(Environment.PASS, "123blue!");
			break;
		case 4:
			props.setProperty(Environment.URL, "jdbc:postgresql://localhost:5432/" + DATABASE_NAME);
			props.setProperty(Environment.DIALECT, net.sf.hibernate.dialect.PostgreSQLDialect.class.getName());
			props.setProperty(Environment.DRIVER, org.postgresql.Driver.class.getName());
			props.setProperty(Environment.USER,"root");
			props.setProperty(Environment.PASS, "123blue!");
			break;
		}
		return props;
	}
	
	/**
	 * @see com.bluejungle.destiny.tools.dbinit.DBInitTestBase#getDatabaseName()
	 */
	@Override
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}

	
	@BeforeClass
	public static void initialize() throws Exception {
		try {
			profileResult = new ProfileResult();
			
			Field[] fields = java.sql.Types.class.getFields();
			allTypes = new ArrayList<IPair<String,Integer>>();
			for(Field field: fields){
				allTypes.add(new Pair<String, Integer>(field.getName(), field.getInt(field.getName())));
			}
			
			profileResult.put(ProfileKey.DEFAULT_TRY_CONNECTION, DEFAULT_MAX_TRY_OPEN_CONNECTION);
			
			for(SizeSet sizeSet : SizeSet.values()){
				profileResult.put(sizeSet.name() + ProfileKey.DEFAULT_TRY_LENGTH, sizeSet.getColumnSize());
				profileResult.put(sizeSet.name() + ProfileKey.DEFAULT_TRY_DECIMAL, sizeSet.getDecimalDigits());
			}
		} catch (Exception e) {
			LOG.fatal(e);
			throw e;
		}
	}
	
	@Before
	public void setup() throws SQLException, HibernateException{
		super.setup();
		if(profileResult.mappedTypes == null || profileResult.unmappedTypes == null){
			profileResult.mappedTypes = new ArrayList<IPair<String,Integer>>();
			profileResult.unmappedTypes = new ArrayList<IPair<String,Integer>>();
			for(IPair<String, Integer> eachType : allTypes){
				try {
					dialect.getTypeName(eachType.second());
					profileResult.mappedTypes.add(eachType);
				} catch (HibernateException e) {
					profileResult.unmappedTypes.add(eachType);
				}
			}
		}
	}
	
	@Test
	public void profileByQuery() throws SQLException{
		final DatabaseMetaData meta = connection.getMetaData();
		profileResult.put(ProfileKey.DATABASE_MAJOR_VERSION, meta.getDatabaseMajorVersion());
		profileResult.put(ProfileKey.DATABASE_MINOR_VERSION, meta.getDatabaseMinorVersion());
		profileResult.put(ProfileKey.DATABASE_PRODUCT_NAME, meta.getDatabaseProductName());
		profileResult.put(ProfileKey.DATABASE_PRODUCT_VERSION, meta.getDatabaseProductVersion());
		profileResult.put(ProfileKey.DRIVER_MAJOR_VERSION, meta.getDriverMajorVersion());
		profileResult.put(ProfileKey.DRIVER_MINOR_VERSION, meta.getDriverMinorVersion());
		profileResult.put(ProfileKey.DRIVER_NAME, meta.getDriverName());
		profileResult.put(ProfileKey.DRIVER_VERSION, meta.getDriverVersion());
		profileResult.put(ProfileKey.EXTRA_NAME_CHARACTERS, meta.getExtraNameCharacters());
		profileResult.put(ProfileKey.JDBC_MAJOR_VERSION, meta.getJDBCMajorVersion());
		profileResult.put(ProfileKey.JDBC_MINOR_VERSION, meta.getJDBCMinorVersion());
		profileResult.put(ProfileKey.MAX_CATALOG_NAME_LENGTH, meta.getMaxCatalogNameLength());
		profileResult.put(ProfileKey.MAX_CONNECTIONS, meta.getMaxConnections());
		profileResult.put(ProfileKey.MAX_TABLE_NAME_LENGTH, meta.getMaxTableNameLength());
		profileResult.put(ProfileKey.MAX_USER_NAME_LENGTH, meta.getMaxUserNameLength());
		profileResult.put(ProfileKey.SQL_KEYWORDS, meta.getSQLKeywords());
		
		profileResult.put(ProfileKey.STORES_LOWER_CASE_IDENTIFIERS, meta.storesLowerCaseIdentifiers());		
		profileResult.put(ProfileKey.STORES_LOWER_CASE_QUOTED_IDENTIFIERS, meta.storesLowerCaseQuotedIdentifiers());
		profileResult.put(ProfileKey.STORES_MIXED_CASE_IDENTIFIERS, meta.storesMixedCaseIdentifiers());
		profileResult.put(ProfileKey.STORES_MIXED_CASE_QUOTED_IDENTIFIERS, meta.storesMixedCaseQuotedIdentifiers());
		profileResult.put(ProfileKey.STORES_UPPER_CASE_IDENTIFIERS, meta.storesUpperCaseIdentifiers());
		profileResult.put(ProfileKey.STORES_UPPER_CASE_QUOTED_IDENTIFIERS, meta.storesUpperCaseQuotedIdentifiers());
		
		profileResult.put(ProfileKey.SUPPORTS_ALTER_TABLE_WITH_ADD_COLUMN, meta.supportsAlterTableWithAddColumn());
		profileResult.put(ProfileKey.SUPPORTS_ALTER_TABLE_WITH_DROP_COLUMN, meta.supportsAlterTableWithDropColumn());
		profileResult.put(ProfileKey.SUPPORTS_BATCH_UPDATES, meta.supportsBatchUpdates());
		profileResult.put(ProfileKey.SUPPORTS_NON_NULLABLE_COLUMNS, meta.supportsNonNullableColumns());
		
		Set<ProfileKey> STORES_IDENTIFIERS = new TreeSet<ProfileKey>();
		STORES_IDENTIFIERS.add(ProfileKey.STORES_LOWER_CASE_IDENTIFIERS);
		STORES_IDENTIFIERS.add(ProfileKey.STORES_MIXED_CASE_IDENTIFIERS);
		STORES_IDENTIFIERS.add(ProfileKey.STORES_UPPER_CASE_IDENTIFIERS);
		
		boolean onlyOneTrue = false;
		for (ProfileKey key : STORES_IDENTIFIERS) {
			String str = (String) profileResult.get(key);
			boolean b = StringUtils.stringToBoolean(str);
			if (onlyOneTrue && b) {
				onlyOneTrue = false;
				break;
			}
			if (b) {
				onlyOneTrue = true;
			}
		}
		assertTrue("should only one STORES_CASE_IDENTIFIERS is true", onlyOneTrue);
		
		Set<ProfileKey> STORES_QUOTED_IDENTIFIERS = new TreeSet<ProfileKey>();
		STORES_QUOTED_IDENTIFIERS.add(ProfileKey.STORES_LOWER_CASE_QUOTED_IDENTIFIERS);
		STORES_QUOTED_IDENTIFIERS.add(ProfileKey.STORES_MIXED_CASE_QUOTED_IDENTIFIERS);
		STORES_QUOTED_IDENTIFIERS.add(ProfileKey.STORES_UPPER_CASE_QUOTED_IDENTIFIERS);
		onlyOneTrue = false;
		for(ProfileKey key : STORES_QUOTED_IDENTIFIERS){
			String str = (String) profileResult.get(key);
			boolean b = StringUtils.stringToBoolean(str);
			if(onlyOneTrue && b){
				onlyOneTrue = false;
				break;
			}

			if(b){
				onlyOneTrue = true;
			}
		}
		assertTrue("should only one STORES_QUOTED_IDENTIFIERS is true", onlyOneTrue);
	}
	
	@Test
	public void smallSet() throws SQLException, HibernateException {
		SizeSet sizeSet = SizeSet.SMALL;
		profileSizeSet(sizeSet);
	}

	@Test
	public void largeSet() throws SQLException, HibernateException {
		SizeSet sizeSet = SizeSet.LARGE;
		profileSizeSet(sizeSet);
	}
	
	@Test
	public void createEmptyTable() throws SQLException, HibernateException {
		String tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		
		boolean success = processSqlStatements(tableM.sqlCreateString(dialect));
		connection.commit();
		profileResult.put(ProfileKey.CAN_CREATE_EMPTY_TABLE, success);
	}
	
	@Test
	public void dropDoesNotExist() throws HibernateException, SQLException {
		String tableName = DatabaseHelper.matchToDbStoreCase("NoSuchTable");
		boolean dropDNETableCauseException;
		try {
			DatabaseHelper.processSqlStatement(connection, "DROP TABLE " + tableName);
			dropDNETableCauseException = false;
		} catch (SQLException e) {
			DEBUG_LOG.info(e);
			dropDNETableCauseException = true;
		}
		connection.commit();
		profileResult.put(ProfileKey.DROP_DNE_TABLE_CAUSE_EXCEPTION, dropDNETableCauseException);
		
		//create a table and one column, some db doesn't allow an empty table
		tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		String columnName = COLUMN_NAME_PREFIX + "one" + COLUMN_NAME_SUFFIX;
		ColumnM columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.INTEGER),
				(short)Types.INTEGER,
				0, 
				0, 
				false //nullable
		);
		tableM.addColumn(columnM);
		boolean result = processSqlStatements(tableM.sqlCreateString(dialect));
		assertTrue(result);
		
		connection.commit();
		//create a new column but don't commit to the db
		columnName = COLUMN_NAME_PREFIX + "two" + COLUMN_NAME_SUFFIX;
		columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.INTEGER),
				(short)Types.INTEGER,
				0, 
				0, 
				false //nullable
		);
		tableM.addColumn(columnM);
		
		connection.commit();
		result = processSqlStatements(columnM.sqlDropColumn(dialect));
		profileResult.put(ProfileKey.DROP_DNE_COLUMN_CAUSE_EXCEPTION, !result);
		
		
//		create a new constraint but don't commit to the db
		tableM.addConstraint("constraintName", columnName, FieldType.PRIMARY_KEY);
		List<ConstraintM> constraints =  tableM.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(1, constraints.size());
		
		connection.commit();
		result = processSqlStatements(constraints.get(0).sqlDropConstraint(dialect));
		profileResult.put(ProfileKey.DROP_DNE_CONSTRAINT_CAUSE_EXCEPTION, !result);
	}
	
	@Test
	public void dataTruncation() throws HibernateException, SQLException{
		String tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		String columnName = COLUMN_NAME_PREFIX + "column" + COLUMN_NAME_SUFFIX;
		ColumnM columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.VARCHAR),
				(short)Types.VARCHAR,
				50, 
				0, 
				true //nullable
		);
		tableM.addColumn(columnM);
		
		boolean result = processSqlStatements(tableM.sqlCreateString(dialect));
		assertTrue(result);
		
		int size = DatabaseHelper.getColumnData(connection, tableName, columnName, columnName).size();
		assertEquals(0, size);

		final String data = "qwer1tyui2asdf3ghjk4zxcv5bnmn6opkl7z";
		String sqlStatement = String.format("INSERT INTO %s (%s) VALUES ('%s')", tableName,
				columnName, data);
		
		DatabaseHelper.processSqlStatement(connection, sqlStatement);
		
		size = DatabaseHelper.getColumnData(connection, tableName, columnName, columnName).size();
		assertEquals(1, size);
		
		connection.commit();
		
		boolean success = false;
		try {
			DatabaseHelper.processSqlStatement(connection,columnM.sqlAlterColumnNewLength(60, dialect));
			success= true;
		} catch (SQLException e) {
			DEBUG_LOG.info("no data truncation", e);
			success = false;
		}
		
		profileResult.put(ProfileKey.CAN_REDUCE_COLUMN_LENGTH, success);
		connection.commit();
		List<IPair<Object, Object>> results = DatabaseHelper.getColumnData(connection, tableName, columnName, columnName);
		size = results.size();
		assertEquals(1, size);
		results.get(0).second().equals(data);
		
		connection.commit();
		try {
			DatabaseHelper.processSqlStatement(connection,columnM.sqlAlterColumnNewLength(20, dialect));
			success= true;
		} catch (SQLException e) {
			DEBUG_LOG.info("data truncation", e);
			success = false;
		}
		profileResult.put(ProfileKey.CAN_REDUCE_COLUMN_LENGTH_WITH_DATA_TRUNCATION, success);
		
		connection.commit();
		results = DatabaseHelper.getColumnData(connection, tableName, columnName, columnName);
		size = results.size();
		assertEquals(1, size);
		results.get(0).second().equals(data.substring(0, 20));
	}
	
	private void profileSizeSet(SizeSet sizeSet) throws HibernateException, SQLException {
		for (IPair<String, Integer> eachType : profileResult.mappedTypes){
			String tableName = getCallingMethodName() + "_" + eachType.first();
			String columnName = COLUMN_NAME_PREFIX + eachType.first().toLowerCase() + COLUMN_NAME_SUFFIX;
			TableM tableM = new TableM(databaseM, tableName);
			
			//the name is mixed case			
			ColumnM columnM = new ColumnM(tableM, 
					columnName, 
					dialect.getTypeName(eachType.second()),
					eachType.second().shortValue(),
					sizeSet.getColumnSize(), 
					sizeSet.getDecimalDigits(), 
					false //nullable //don't define a nullable primary key, ms sql doesn't like it
			);
			tableM.addColumn(columnM);
			
			boolean allSucess = processSqlStatements(tableM.sqlCreateString(dialect));
			if(allSucess){
				tableName = DatabaseHelper.matchToDbStoreCase(tableName);
				columnName = DatabaseHelper.matchToDbStoreCase(columnName);
				
				DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
				DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
						DATABASE_NAME, meta);
				TableM dbTable = realDatabase.getTable(tableName);
			
				assertEquals(1, dbTable.getColumnNames().size());
				
				ColumnM dbColumn = dbTable.getColumn(columnName);
				profileResult.put(eachType.second(), ProfileKey.LENGTH, dbColumn.getColumnSize(), sizeSet);
				profileResult.put(eachType.second(), ProfileKey.DECIMAL, dbColumn.getDecimalDigits(), sizeSet);
				profileResult.put(eachType.second(), ProfileKey.RETURNED_TYPE_CODE, dbColumn.getDataType(), sizeSet);
			}
		}
	}
	
	@Test
	public void constrainsPrimaryKey() throws HibernateException, SQLException{
		String tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		String columnName = COLUMN_NAME_PREFIX + "column" + COLUMN_NAME_SUFFIX;
		ColumnM columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.INTEGER),
				(short)Types.INTEGER,
				0, 
				0, 
				false //nullable
		);
		tableM.addColumn(columnM);
		tableM.addConstraint("constraintName", columnName, FieldType.PRIMARY_KEY);
		
		boolean result = processSqlStatements(tableM.sqlCreateString(dialect));
		connection.commit();
		assertTrue(result);
		
		List<ConstraintM> constraints =  tableM.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(1, constraints.size());
		assertEquals(0, tableM.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, tableM.getConstraints(FieldType.INDEX).size());
		
		result = processSqlStatements(constraints.get(0).sqlAddConstraint(dialect));
		assertTrue(result);
		
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				DATABASE_NAME, meta);
		
		TableM dbTable = realDatabase.getTable(DatabaseHelper.matchToDbStoreCase(tableName));
		constraints =  dbTable.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(1, constraints.size());
		
		constraints =  dbTable.getConstraints(FieldType.UNIQUE);
		assertEquals(0, constraints.size());
		
		constraints =  dbTable.getConstraints(FieldType.INDEX);
		assertEquals(0, constraints.size());
		
		final String catalog = null;
		final String tableSchema = dialect.getTableSchema(connection.getMetaData());
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		int size;
		
		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.PRIMARY_KEY, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertEquals(1, size);
		
		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.UNIQUE, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertTrue(size <= 1);
		profileResult.put(ProfileKey.PRIMARY_IMPLY_UNIQUE, size != 0);

		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.INDEX, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertTrue(size <= 1);
		profileResult.put(ProfileKey.PRIMARY_IMPLY_INDEX, size != 0);

		
	}
	
	@Test
	public void constrainsUniqueKey() throws HibernateException, SQLException{
		String tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		String columnName = COLUMN_NAME_PREFIX + "column" + COLUMN_NAME_SUFFIX;
		ColumnM columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.INTEGER),
				(short)Types.INTEGER,
				0, 
				0, 
				true //nullable
		);
		tableM.addColumn(columnM);
		tableM.addConstraint("constraintName", columnName, FieldType.UNIQUE);
		
		boolean result = processSqlStatements(tableM.sqlCreateString(dialect));
		assertTrue(result);
		
		List<ConstraintM> constraints =  tableM.getConstraints(FieldType.UNIQUE);
		assertEquals(1, constraints.size());
		assertEquals(0, tableM.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, tableM.getConstraints(FieldType.INDEX).size());
		
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				DATABASE_NAME, meta);
		
		TableM dbTable = realDatabase.getTable(DatabaseHelper.matchToDbStoreCase(tableName));
		constraints =  dbTable.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(0, constraints.size());
		
		constraints =  dbTable.getConstraints(FieldType.UNIQUE);
		assertEquals(1, constraints.size());
		
		constraints =  dbTable.getConstraints(FieldType.INDEX);
		assertEquals(0, constraints.size());
		
		final String catalog = null;
		final String tableSchema = dialect.getTableSchema(connection.getMetaData());
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);

		int size;
		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.PRIMARY_KEY, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertEquals(0, size);
		
		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.UNIQUE, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertEquals(1, size);

		size = MappingTest.getAttribute(MappingConstructor.KeyAttribyte.INDEX, catalog,
				tableSchema, tableName, connection.getMetaData()).size();
		assertTrue(size <= 1);
		profileResult.put(ProfileKey.UNIQUE_IMPLY_INDEX, size != 0);
	}
	
	@Test @Ignore
	public void maxConnection(){
		List<Connection> connections = new LinkedList<Connection>();
		int i = 0;
		try {
			for (; i < DEFAULT_MAX_TRY_OPEN_CONNECTION; i++) {
				connections.add(ConnectionProviderFactory.newConnectionProvider(props).getConnection());
			}
		} catch (Throwable exceptedException) {

		} finally {
			profileResult.put(ProfileKey.MAX_TRIED_OPEN_CONNECTION, i);
			for (Connection connection : connections) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.fatal(e);
				}
			}
		}		
	}
	
	
	@Test(timeout=360000)	@Ignore //timeout six minutes
	public void openAndClose() throws InterruptedException{
		Sample sample = new Sample(100);
		Sample sleepTimeSample = new Sample(15);
		int sleepTime = 0;
//		  mid = low + ((high - low) / 2)
		int low = 0;
		int high = 10;
		  
		final int MAX_TRY = 100;
		final Random r = new Random();
		Connection connection = null;
		Thread.sleep(1000);

		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < 300000){
			try {
				for (int i = 0; i < MAX_TRY; i++) {
					if(sleepTime > 0){
						Thread.sleep(r.nextInt(sleepTime));
					}
					long openConnectionstartTime = System.currentTimeMillis();
					connection = ConnectionProviderFactory.newConnectionProvider(props).getConnection();
					connection.close();
					sample.add((int) (System.currentTimeMillis() - openConnectionstartTime));
				}
				high = sleepTime;
				sleepTimeSample.add(sleepTime);
			} catch (Throwable exceptedException) {
//				System.out.println(exceptedException);
				high *= 2;
				low = sleepTime;
			} finally {
				sleepTime = (low + high) >>> 1;
				DEBUG_LOG.info("total=" + sample.getNumOfSamples() + ", sleepTime=" + sleepTime);
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						LOG.fatal(e);
					}
				}
			}
			Thread.sleep(2000);
		}
		System.out.println(sample);
		System.out.println(sleepTimeSample);
	}
	
	
	@Test
	public void checkBooleanAcceptValue() throws HibernateException, SQLException{
		//create table first
		String tableName = getCallingMethodName();
		TableM tableM = new TableM(databaseM, tableName);
		String columnName = COLUMN_NAME_PREFIX + "one" + COLUMN_NAME_SUFFIX;
		ColumnM columnM = new ColumnM(tableM, 
				columnName, 
				dialect.getTypeName(Types.BIT),
				(short)Types.BIT,
				0, 
				0, 
				false //nullable
		);
		tableM.addColumn(columnM);
		boolean result = processSqlStatements(tableM.sqlCreateString(dialect));
		assertTrue(result);
		
		//try different value
		final String insertFormat = "INSERT INTO "+ tableName +" VALUES (%s)";
		
		final String[] tryValues = { "0", "'0'", "false", "'false'", "F", "'F'" };
		
		for(String tryValue : tryValues){
			boolean success;
			try {
				DatabaseHelper.processSqlStatement(connection, String.format(insertFormat, tryValue));
				success= true;
			} catch (SQLException e) {
				DEBUG_LOG.info("no data truncation", e);
				success = false;
			}finally{
				connection.commit();
			}
			System.out.println(tryValue + " is " + (success ? "ok" : "bad"));
		}
	}
	
	@AfterClass
	public static void showResult() throws Exception{
		try {
			System.out.print(profileResult.generateReport(dialect));
			FileOutputStream fos= new FileOutputStream(new File("c:\\temp\\result.dbpr"));
			profileResult.store(fos, "comments go here");
			fos.close();
		} catch (Exception e) {
			LOG.fatal(e);
			throw e;
		}
	}
}
