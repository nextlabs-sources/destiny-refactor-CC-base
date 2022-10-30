package com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernateMod.DatabaseHelper;

import junit.framework.TestCase;

public class TableMTest extends TestCase{
	private DatabaseM databaseM;
	private Dialect dialect;
	
	public TableMTest() throws HibernateException{
		Properties properties = new Properties();
		properties.put(Environment.DIALECT, "net.sf.hibernate.dialect.PostgreSQLDialect");
		dialect = Dialect.getDialect(properties);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseM = setup("a");
	}
	
	protected DatabaseM setup(String name){
		/*
		 * table1 	-
		 * table2 	- c21 (int8)
		 * 			- c22 (int8)
		 * table3 	- c31 (int8)
		 * 			- c32 (VARCHAR 100)
		 * 			- u33 (VARCHAR 7)
		 * table4	- u41 (int8)
		 */
		
		DatabaseM dbM;
		ColumnM c;
		dbM = new DatabaseM(name);
		TableM t = new TableM(dbM, "table1");
		dbM.addTable(t);
		t = new TableM(dbM, "table2");
		t.addColumn(new ColumnM(t, "c21", null, (short)Types.INTEGER, 0, 0, true));
		t.addColumn(new ColumnM(t, "c22", null, (short)Types.INTEGER, 0, 0, true));
		dbM.addTable(t);
		
		t = new TableM(dbM, "table2_clone");
		t.addColumn(new ColumnM(t, "c21", null, (short)Types.INTEGER, 0, 0, true));
		t.addColumn(new ColumnM(t, "c22", null, (short)Types.INTEGER, 0, 0, true));
		dbM.addTable(t);
		
		t = new TableM(dbM, "table3");
		t.addColumn(new ColumnM(t, "c31", null, (short)Types.INTEGER, 0, 0, true));
		t.addColumn(new ColumnM(t, "c32",  null, (short)Types.VARCHAR, 100, 0, true));
		c = new ColumnM(t, "u33", null, (short)Types.VARCHAR, 7, 0, false);
		t.addColumn(c);
		t.addConstraint(DatabaseHelper.matchToDbStoreCase("table3_u33_key"), "u33", FieldType.UNIQUE);
		dbM.addTable(t);
		
		t = new TableM(dbM, "table4");
		c = new ColumnM(t, "u41", null, (short)Types.INTEGER, 0, 0, false);
		t.addColumn(c);
		t.addConstraint(DatabaseHelper.matchToDbStoreCase("table3_u41_key"), "u41", FieldType.UNIQUE);
		dbM.addTable(t);
		return dbM;
	}
	
	public void testTable1Correctess(){
		//test empty table
		TableM t = databaseM.getTable("tableDNE");
		assertNull(t);
		t = databaseM.getTable("table1");
		assertNotNull(t);
		assertEquals("table1", t.getName());
		
		assertEquals(0, t.getColumnNames().size() );
		
		assertNull(t.getColumn("c21"));
		assertNull(t.getColumn("c32"));
		assertNull(t.getColumn("c11"));
	}
	
	public void testTable2Correctess(){
		TableM t = databaseM.getTable("tableDNE");
		assertNull(t);
		t = databaseM.getTable("table2");
		
		assertNotNull(t);
		assertEquals( "table2", t.getName());
		
		assertEquals(2, t.getColumnNames().size());
		
		assertNull(t.getColumn("c32"));
		assertNull(t.getColumn("c11"));
		
		ColumnM column = t.getColumn("c21");
		assertNotNull(column);
		assertEquals(column.getColumnName(), "c21");
		assertFalse(column.isAdded());
		assertFalse(column.isAdded());
		assertFalse(column.isDropped());
		assertTrue(column.isNullable());
		assertEquals((short)Types.INTEGER, column.getDataType());
		assertNotNull(column.getTable());
		assertEquals("table2", column.getTable().getName());
		
		assertNotNull(t.getColumn("c22"));
	}
	
	public void testCreate(){
		List<String> sqlOutput = databaseM.getTable("table1").sqlCreateString(dialect);
		List<String> expected = new ArrayList<String>();
		expected.add("create table table1 ()");
		assertArrayEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table2").sqlCreateString(dialect);
		expected = new ArrayList<String>();
		expected.add("create table table2 (c21 int4,c22 int4)");
		assertArrayEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table3").sqlCreateString(dialect);
		expected = new ArrayList<String>();
		expected.add("create table table3 (c31 int4,c32 varchar(100),u33 varchar(7) not null unique)");
		assertArrayEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table4").sqlCreateString(dialect);
		expected = new ArrayList<String>();
		expected.add("create table table4 (u41 int4 not null unique)");
		assertArrayEquals(expected, sqlOutput);
	}
	
	
	public void testDrop(){
		String sqlOutput = databaseM.getTable("table1").sqlDropString(dialect);
		String expected = "drop table table1";
		assertEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table2").sqlDropString(dialect);
		expected = "drop table table2";
		assertEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table3").sqlDropString(dialect);
		expected = "drop table table3";
		assertEquals(expected, sqlOutput);
		
		sqlOutput = databaseM.getTable("table4").sqlDropString(dialect);
		expected = "drop table table4";
		assertEquals(expected, sqlOutput);
	}
	
	//	we are not in junit 4 yet
	private void assertArrayEquals(List expected, List sqlOutput){
		assertEquals(expected.size(), sqlOutput.size());
		for(int i =0; i<expected.size(); i++){
			assertEquals(expected.get(i), sqlOutput.get(i));
		}
	}
}
