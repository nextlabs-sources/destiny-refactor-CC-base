package com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.bluejungle.destiny.tools.dbinit.hibernateMod.dialect.DialectExtended;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.dialect.Dialect;

import junit.framework.TestCase;

public class ColumnMTest extends TestCase{
	private ColumnM column;
	private DialectExtended dialect;
	
	public ColumnMTest() throws HibernateException {
		super();
		Properties properties = new Properties();
		properties.put(Environment.DIALECT, "net.sf.hibernate.dialect.PostgreSQLDialect");
		dialect = DialectExtended.getDialectExtended(Dialect.getDialect(properties));
	}

	@Override
	protected void setUp() throws Exception{
		super.setUp();
		DatabaseM d = new DatabaseM("database1"); 
		TableM t = new TableM(d,"table1");
		column = new ColumnM(t, "Invalid-Name", "typeDNE", (short)999, -9, -8, true);
		t.addColumn(column);
	}

	public void testGetterSetter(){
		TableM t2 = column.getTable();
		assertEquals("table1", t2.getName());
		
		Set columnNames = t2.getColumnNames();
		assertEquals(1,columnNames.size() );
		assertEquals("Invalid-Name", columnNames.iterator().next() );
		assertEquals("Invalid-Name", column.getColumnName());
		assertEquals("Invalid-Name", column.getName());
		assertEquals(-9, column.getColumnSize());
		assertEquals("typeDNE", column.getSqlTypeName());
		assertEquals(999, column.getDataType());
		assertEquals(true, column.isNullable());
		assertEquals(false, column.isAdded());
		assertEquals(false, column.isDropped());
		assertEquals(false, column.isChanged());
		
		assertNull(column.getTempName());
		assertFalse(column.isTemp());
	}
	
	public void testDialectPostrgreSQL() throws Exception{
		String str = column.getDialectType(dialect);
		assertNull(str);
		
	}
	
	public void testSql(){
		DatabaseM database = new DatabaseM("database1"); 
		TableM table = new TableM(database,"table1");
		ColumnM column = new ColumnM(table, "column1", "int8", (short)Types.BIGINT , 0, 0, true);
		
		List<String> sqlOutputs = column.sqlAddColumn(dialect);
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table1 add column column1 int8");
		assertArrayEquals(expected, sqlOutputs);
		
		sqlOutputs = column.sqlAddColumn(dialect);
		expected = new ArrayList<String>();
		assertArrayEquals(expected, sqlOutputs);
		
		String sqlOutput = column.sqlAlterColumnChangeDataType("dneType", dialect);
		assertEquals("ALTER TABLE table1 ALTER COLUMN column1 TYPE dneType", sqlOutput);
		
		sqlOutput = column.sqlAlterColumnNewLength(123, dialect);
		assertEquals("ALTER TABLE table1 ALTER COLUMN column1 TYPE int8", sqlOutput);
		
		sqlOutputs = column.sqlAlterColumnSetNullable(true, dialect);
		expected = new ArrayList<String>();
		expected.add("ALTER TABLE table1 ALTER COLUMN column1 DROP NOT NULL");
		assertArrayEquals(expected, sqlOutputs);
		
		sqlOutputs = column.sqlAlterColumnSetNullable(false, dialect);
		expected = new ArrayList<String>();
		expected.add("ALTER TABLE table1 ALTER COLUMN column1 SET NOT NULL");
		assertArrayEquals(expected, sqlOutputs);
		
	}
	
	//	we are not in junit 4 yet
	private void assertArrayEquals(List expected, List sqlOutput){
		assertEquals(expected.size(), sqlOutput.size());
		for(int i =0; i<expected.size(); i++){
			assertEquals(expected.get(i), sqlOutput.get(i));
		}
	}
}

