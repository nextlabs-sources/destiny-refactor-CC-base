/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.bluejungle.destiny.tools.dbinit.hibernate.MappingConstructor;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseMetadataMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ConstraintM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitTest.java#1 $
 */

public abstract class DBInitTest extends DBInitTestBase{
	private static final String DATABASE_NAME = "DBInitTest";
	
	@Override
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Test
	public void testSetBase() throws HibernateException, SQLException {
		String tableName = getCallingMethodName();
		createHibernateTableSet1(tableName);
		checkDatabaseSet1(tableName);
	}
	
	@Test
	public void testSetNewColumn() throws HibernateException, SQLException {
		String tableName = getCallingMethodName();
		createHibernateTableSet1(tableName);
		
		addNewColumnSet1A(tableName);
		checkDatabaseSet1A(tableName);
		
		deleteNewColumnSet1C(tableName);
		checkDatabaseSet1(tableName);
	}
	
	@Test
	public void testNewColumnChangeNull() throws HibernateException, SQLException {
		String tableName = getCallingMethodName();
		createHibernateTableSet1(tableName);
		
		addNewColumnSet1A(tableName);
		checkDatabaseSet1A(tableName);
		
		changeColumnNullSet1B(tableName);
		checkDatabaseSet1B(tableName);
		
		deleteNewColumnSet1C(tableName);
		checkDatabaseSet1(tableName);
	}
	
	@Test
	public void testChangeLength() throws HibernateException, SQLException {
		String tableName = getCallingMethodName();
		createHibernateTableSet1(tableName);
		
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				getDatabaseName(), meta);
		
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		TableM dbTable = realDatabase.getTable(tableName);
		ColumnM addressDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Address"));
		assertTrue(processSqlStatement(addressDbColumn.sqlAlterColumnNewLength(543, dialect)));
		
		
	}

	//database set 1
	private void createHibernateTableSet1(String tableName) throws HibernateException {
		TableM tableM = new TableM(databaseM, tableName);
		databaseM.addTable(tableM);
		tableM.addColumn(new ColumnM(tableM, "id", dialect.getTypeName(INTEGER_TYPE.sqlType()),
				(short) INTEGER_TYPE.sqlType(), 0, 0, false));
		tableM.addConstraint("constraintName", "id", FieldType.PRIMARY_KEY);

		tableM.addColumn(new ColumnM(tableM, "Name", dialect.getTypeName(STRING_TYPE.sqlType()),
				(short) STRING_TYPE.sqlType(), 50, 0, false));
		
		tableM.addColumn(new ColumnM(tableM, "Address", dialect.getTypeName(STRING_TYPE.sqlType()),
				(short) STRING_TYPE.sqlType(), 380, 0, false));
		
		tableM.addColumn(new ColumnM(tableM, "Salary", dialect.getTypeName(DOUBLE_TYPE.sqlType()),
				(short) DOUBLE_TYPE.sqlType(), 0, 0, false));
		
		tableM.addConstraint("salary_index", "Salary", FieldType.INDEX);
		
		assertTrue( processSqlStatements(tableM.sqlCreateString(dialect)));
		
		List<ConstraintM> constraints = tableM.getConstraints(FieldType.PRIMARY_KEY);
		for(ConstraintM constraint : constraints){
			assertTrue( processSqlStatements(constraint.sqlAddConstraint(dialect)));
		}
		
		constraints = tableM.getConstraints(FieldType.INDEX);
		for(ConstraintM constraint : constraints){
			assertTrue( processSqlStatements(constraint.sqlAddConstraint(dialect)));
		}
	}
	
	private void checkDatabaseSet1(String tableName) throws SQLException {
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				getDatabaseName(), meta);
		
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		TableM dbTable = realDatabase.getTable(tableName);
		
		Set<String> columnNames = dbTable.getColumnNames();
		assertEquals(4, columnNames.size());
		
		//check all column
		checkEachColumnSet1(dbTable);
		
		//check table misc
		assertEquals(0, dbTable.getConstraints(FieldType.FOREIGN_KEY).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.UNIQUE).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.RULE).size());
	}

	private void checkEachColumnSet1(TableM dbTable) {
		Set<String> columnNames = dbTable.getColumnNames();
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("id")));
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("Name")));
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("Address")));
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("Salary")));
		
		ColumnM idDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("id"));
		assertNotNull(idDbColumn);
		ColumnM nameDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Name"));
		assertNotNull(nameDbColumn);
		ColumnM addressDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Address"));
		assertNotNull(addressDbColumn);
		ColumnM salaryDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Salary"));
		assertNotNull(salaryDbColumn);
		
		
		//check each column
		
		//check column "id"
		List<ConstraintM> constraints;
		
		List<ConstraintM> constraintsFromTable =  dbTable.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(1, constraintsFromTable.size());
		
		List<ConstraintM> constraintsFromColumn = idDbColumn.getConstraints(FieldType.PRIMARY_KEY);
		assertEquals(1, constraintsFromColumn.size());
		
		assertArrayEquals(constraintsFromTable.toArray(), constraintsFromColumn.toArray());
		constraints = constraintsFromTable;
		
		//the name may not match because the primary key may not have name
		//assertEquals("constraintName", constraints.get(0).getName()); 
		
		assertTrue(constraints.get(0).contains(idDbColumn));
		assertFalse(constraints.get(0).contains(nameDbColumn));
		assertFalse(constraints.get(0).contains(addressDbColumn));
		assertFalse(constraints.get(0).contains(salaryDbColumn));
		
		assertEquals(0, idDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, idDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, idDbColumn.getConstraints(FieldType.RULE).size());
		
		assertTrue(dialect.isSameType(idDbColumn.getDataType(), (short)INTEGER_TYPE.sqlType()));
		assertFalse(idDbColumn.isNullable());
		
		//check column "salary"
		constraintsFromTable =  dbTable.getConstraints(FieldType.INDEX);
		assertEquals(1, constraintsFromTable.size());
		
		constraintsFromColumn = salaryDbColumn.getConstraints(FieldType.INDEX);
		assertEquals(1, constraintsFromColumn.size());
		assertArrayEquals(constraintsFromTable.toArray(), constraintsFromColumn.toArray());
		
		constraints = constraintsFromTable;
		
		//the name may not match because the primary key may not have name
		//assertEquals("constraintName", constraints.get(0).getName()); 
		
		assertFalse(constraints.get(0).contains(idDbColumn));
		assertFalse(constraints.get(0).contains(nameDbColumn));
		assertFalse(constraints.get(0).contains(addressDbColumn));
		assertTrue(constraints.get(0).contains(salaryDbColumn));
		
		assertEquals(0, salaryDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, salaryDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, salaryDbColumn.getConstraints(FieldType.RULE).size());
		
		assertTrue(dialect.isSameType(salaryDbColumn.getDataType(), (short)DOUBLE_TYPE.sqlType()));
		assertFalse(salaryDbColumn.isNullable());
		
		//check column "name"
		assertEquals(0, nameDbColumn.getColumnSize(), 50);
		assertEquals(0, nameDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, nameDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, nameDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, nameDbColumn.getConstraints(FieldType.RULE).size());
		
		assertTrue(dialect.isSameType(nameDbColumn.getDataType(), (short)STRING_TYPE.sqlType()));
		assertFalse(nameDbColumn.isNullable());
		
		//check column "address"
		assertEquals(0, addressDbColumn.getColumnSize(), 380);
		assertEquals(0, addressDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, addressDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, addressDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, addressDbColumn.getConstraints(FieldType.RULE).size());
		assertTrue(dialect.isSameType(addressDbColumn.getDataType(), (short)STRING_TYPE.sqlType()));
		assertFalse(addressDbColumn.isNullable());
	}
	
	//database set 1A
	private void addNewColumnSet1A(String tableName) throws HibernateException {
		TableM hibTableM =  databaseM.getTable(tableName);
		ColumnM newColumnM = new ColumnM(hibTableM, "IsMale", dialect.getTypeName(BOOLEAN_TYPE.sqlType()),
						(short) BOOLEAN_TYPE.sqlType(), 0, 0, false);
		hibTableM.addColumn(newColumnM);
		
		assertTrue(processSqlStatements(newColumnM.sqlAddColumn(dialect)));
		
		
		newColumnM = new ColumnM(hibTableM, "Company_name", dialect.getTypeName(STRING_TYPE.sqlType()),
						(short) STRING_TYPE.sqlType(), 27, 0, true);
		hibTableM.addColumn(newColumnM);
		
		assertTrue(processSqlStatements(newColumnM.sqlAddColumn(dialect)));
	}
	
	private void checkDatabaseSet1A(String tableName) throws SQLException {
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				getDatabaseName(), meta);
		
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		TableM dbTable = realDatabase.getTable(tableName);
		
		checkEachColumnSet1(dbTable);
		
		//check all column
		Set<String> columnNames = dbTable.getColumnNames();
		assertEquals(6, columnNames.size());
		
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("IsMale")));
		
		ColumnM isMaleDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("IsMale"));
		assertNotNull(isMaleDbColumn);
		//check column "isMale"
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.RULE).size());
		assertTrue(dialect.isSameType(isMaleDbColumn.getDataType(), (short)BOOLEAN_TYPE.sqlType()));
		assertFalse(isMaleDbColumn.isNullable());
		
		ColumnM companyDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Company_name"));
		assertNotNull(companyDbColumn);
		//check column "isMale"
		assertEquals(0, companyDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.RULE).size());
		assertTrue(dialect.isSameType(companyDbColumn.getDataType(), (short)STRING_TYPE.sqlType()));
		assertTrue(companyDbColumn.isNullable());
		
		//check table misc
		assertEquals(0, dbTable.getConstraints(FieldType.FOREIGN_KEY).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.UNIQUE).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.RULE).size());
	}

	//database set 1B
	private void changeColumnNullSet1B(String tableName) {
		TableM hibTableM =  databaseM.getTable(tableName);
		ColumnM hibColumnM = hibTableM.getColumn("IsMale");
		assertNotNull(hibColumnM);
		
		assertTrue(processSqlStatements(hibColumnM.sqlAlterColumnSetNullable(true, dialect)));
		
		hibColumnM = hibTableM.getColumn("Company_name");
		assertNotNull(hibColumnM);
		
		assertTrue(processSqlStatements(hibColumnM.sqlAlterColumnSetNullable(false, dialect)));
	}
	
	private void checkDatabaseSet1B(String tableName) throws SQLException {
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				getDatabaseName(), meta);
		
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		TableM dbTable = realDatabase.getTable(tableName);
		
		checkEachColumnSet1(dbTable);
		
		//check all column
		Set<String> columnNames = dbTable.getColumnNames();
		assertEquals(6, columnNames.size());
		
		assertTrue(columnNames.contains(DatabaseHelper.matchToDbStoreCase("IsMale")));
		
		ColumnM isMaleDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("IsMale"));
		assertNotNull(isMaleDbColumn);
		//check column "isMale"
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, isMaleDbColumn.getConstraints(FieldType.RULE).size());
		assertTrue(dialect.isSameType(isMaleDbColumn.getDataType(), (short)BOOLEAN_TYPE.sqlType()));
		assertTrue(isMaleDbColumn.isNullable());
		
		ColumnM companyDbColumn = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Company_name"));
		assertNotNull(companyDbColumn);
		//check column "isMale"
		assertEquals(0, companyDbColumn.getConstraints(FieldType.PRIMARY_KEY).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.INDEX).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.UNIQUE).size());
		assertEquals(0, companyDbColumn.getConstraints(FieldType.RULE).size());
		assertTrue(dialect.isSameType(companyDbColumn.getDataType(), (short)STRING_TYPE.sqlType()));
		assertFalse(companyDbColumn.isNullable());
		
		//check table misc
		assertEquals(0, dbTable.getConstraints(FieldType.FOREIGN_KEY).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.UNIQUE).size());
		
		assertEquals(0, dbTable.getConstraints(FieldType.RULE).size());
	}
	
	//	database set 1C
	private void deleteNewColumnSet1C(String tableName) throws SQLException {
		DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialect);
		DatabaseM realDatabase = new MappingConstructor(dialect).constructDatabaseMapping(
				getDatabaseName(), meta);
		
		tableName = DatabaseHelper.matchToDbStoreCase(tableName);
		TableM dbTable = realDatabase.getTable(tableName);
		
		ColumnM hibColumnM = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("IsMale"));
		assertNotNull(hibColumnM);

		assertTrue(processSqlStatements(hibColumnM.sqlDropColumn(dialect)));

		hibColumnM = dbTable.getColumn(DatabaseHelper.matchToDbStoreCase("Company_name"));
		assertNotNull(hibColumnM);

		assertTrue(processSqlStatements(hibColumnM.sqlDropColumn(dialect)));
	}
	
	@AfterClass
	public static void cleanUp() throws Exception{
		try {
			connection.commit();
			cleanTables(dialect, connection);
		} catch (Exception e) {
			LOG.fatal(e);
			throw e;
		}
		cleanAll();
	}
}
