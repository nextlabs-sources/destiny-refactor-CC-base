 package com.bluejungle.destiny.tools.dbinit.hibernateMod;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernateMod.MappingConstructor.KeyAttribyte;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.MappingConstructor.Pair;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.TableM;

public abstract class MappingTest extends TestCase{
	protected DatabaseM databaseM;
	protected DatabaseM hibernateM;
	private MappingComparator mappingComparator;
	protected DialectExtended dialect;
	private ConfigurationMod cm;
	
	//make it available for profiling
	public static List<Pair> getAttribute(KeyAttribyte attribute, 
			String catalog, 
			String tableSchema,
			String tableName, 
			DatabaseMetaData meta) throws SQLException {
		return MappingConstructor.getAttribute(attribute, catalog, tableSchema, tableName, meta);
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();
			Properties properties = new Properties();
			properties.put(Environment.DIALECT, getDialectClassString());
			dialect = DialectExtended.getDialectExtended(Dialect.getDialect(properties));
			
			updateDatabaseM();
			updateHibernateM();
			cm = new ConfigurationMod();
			mappingComparator = new MappingComparator(databaseM, hibernateM,dialect);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract String getDialectClassString();


	protected List<String> getPreSchema() {
		List<String> list = generateAlterSchemaScript(false, true, true, false);
		return list;
	}
	
	protected List<String> getPostSchema(){
		List<String> list = generateAlterSchemaScript(true, false, false,true );
		list.addAll(hibernateM.sqlDropAllTempColumn(dialect));
		return list;
	}

	
	protected List<String> generateAlterSchemaScript(
			boolean doDrop,
			boolean doCreate,
			boolean doAlterColumn,
			boolean doAlterConstraint){
		return cm.generateAlterSchemaScript(null, mappingComparator, doDrop, doDrop, doCreate, doAlterColumn, doAlterConstraint);
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
		try {
			final String intTypeName = dialect.getTypeName(Types.INTEGER);
			ColumnM c;
			dbM = new DatabaseM(name);
			TableM t = new TableM(dbM, "table1");
			dbM.addTable(t);
			t = new TableM(dbM, "table2");
			t.addColumn(new ColumnM(t, "c21", intTypeName, (short)Types.INTEGER, 0, 0, true));
			t.addColumn(new ColumnM(t, "c22", intTypeName, (short)Types.INTEGER, 0, 0, true));
			dbM.addTable(t);
			
			t = new TableM(dbM, "table2_clone");
			t.addColumn(new ColumnM(t, "c21", intTypeName, (short)Types.INTEGER, 0, 0, true));
			t.addColumn(new ColumnM(t, "c22", intTypeName, (short)Types.INTEGER, 0, 0, true));
			dbM.addTable(t);
			
			t = new TableM(dbM, "table3");
			t.addColumn(new ColumnM(t, "c31", intTypeName, (short)Types.INTEGER, 0, 0, true));
			t.addColumn(new ColumnM(t, "c32",  dialect.getTypeName(Types.VARCHAR, 100), (short)Types.VARCHAR, 100, 0, true));
			c = new ColumnM(t, "u33", dialect.getTypeName(Types.VARCHAR, 7), (short)Types.VARCHAR, 7, 0, false);
			t.addColumn(c);
			t.addConstraint(DatabaseHelper.matchToDbStoreCase("table3_u33_key"), "u33", FieldType.UNIQUE);
			dbM.addTable(t);
			
			t = new TableM(dbM, "table4");
			c = new ColumnM(t, "u41", intTypeName, (short)Types.INTEGER, 0, 0, false);
			t.addColumn(c);
			t.addConstraint(DatabaseHelper.matchToDbStoreCase("table3_u41_key"), "u41", FieldType.UNIQUE);
			
			dbM.addTable(t);
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
		
		return dbM;
	}
	
	protected void updateHibernateM(){
		hibernateM = setup("hibernate");
	}
	
	protected void updateDatabaseM(){
		databaseM = setup("database");
	}
	
	protected void printList(List<String> strs){
		for(String str: strs){
			System.out.println(str);
		}
	}

	//table
	public abstract void testAddTable();	
	public abstract void testAddTableWithIndex();	
	
	//column
	public abstract void testAddColumn();
	public abstract void testRemoveColumn();
	public abstract void testRemoveColumnWithPK();
	public abstract void testAddRemoveColumnWithPK();
	public abstract void testAddRemoveColumn();

	//column nul
	public abstract void testSetNullable();
	public abstract void testDropNullable();
	
	//column length
	public abstract void testReduceLength();
	public abstract void testIncreaseLength();
	public abstract void testNoChangeLength();
	public abstract void testDifferentTypeWithSameLength();
	public abstract void testIncreaseLengthWithDifferentTypeCodeButSameTypeInDatabase();
	public abstract void testChangeDataType();
	
	public void testAddPK(){
		setUp();
		TableM t = hibernateM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ADD PRIMARY KEY (c21)");
		
		assertEquals(expected, script);
	}
	
	public void testRemovePK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		
		//pre-schema
		List<String> script = getPreSchema();
		
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey\"");
		
		assertEquals(expected, script);
	}
	
	public void testChangeReducePK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		t.addConstraint("table2_pkey", "c22", FieldType.PRIMARY_KEY);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey\"");
		expected.add("ALTER TABLE table2 ADD PRIMARY KEY (c21)");
		
		assertEquals(expected, script);
	}
	
	public void testChangeIncreasePK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		t.addConstraint("table2_pkey", "c22", FieldType.PRIMARY_KEY);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();

		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey\"");
		expected.add("ALTER TABLE table2 ADD PRIMARY KEY (c21,c22)");
		
		assertEquals(expected, script);
	}
	
	public void testChangePK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_pkey2", "c21", FieldType.PRIMARY_KEY);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_pkey", "c22", FieldType.PRIMARY_KEY);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey2\"");
		expected.add("ALTER TABLE table2 ADD PRIMARY KEY (c22)");
		
		assertEquals(expected, script);
	}
	
	public void testAddIndexWithSameConstraint(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_index", "c21", FieldType.INDEX);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_index", "c21", FieldType.INDEX);
		t.addConstraint("table2_index", "c22", FieldType.INDEX);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("DROP INDEX \"table2_index\"");
		expected.add("CREATE INDEX table2_index ON table2 (c21,c22)");
		
		assertEquals(expected, script);
	}
	
	public void testAddIndexWithDifferenteConstraint(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_index", "c21", FieldType.INDEX);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_ind1", "c21", FieldType.INDEX);
		t.addConstraint("table2_index", "c22", FieldType.INDEX);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("DROP INDEX \"table2_index\"");
		expected.add("CREATE INDEX table2_ind1 ON table2 (c21)");
		expected.add("CREATE INDEX table2_index ON table2 (c22)");
		
		assertEquals(expected, script);
	}
	
	public void testRemoveIndex(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addConstraint("table2_index", "c21", FieldType.INDEX);
		t.addConstraint("table2_index", "c22", FieldType.INDEX);
		
		t = hibernateM.getTable("table2");
		t.addConstraint("table2_index", "c21", FieldType.INDEX);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("DROP INDEX \"table2_index\"");
		expected.add("CREATE INDEX table2_index ON table2 (c21)");
		
		assertEquals(expected, script);
	}
	
	public void testAddUnique(){
		setUp();
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique", "u23", FieldType.UNIQUE);
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique", "u23", FieldType.UNIQUE);
		
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u24_unique", "u24", FieldType.UNIQUE);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u24_unique UNIQUE (u24)");
		
		assertEquals(expected, script);
	}
	
	public void testAddUniqueReuseSameName(){
		/**
		 * database
		 * table2 	- u23	unique(table2_u2_unique)
		 * 			- u24	
		 * 
		 * hiberante
		 * table2	- u23	unique(table2_u2_unique2)
		 *			- u24	unique(table2_u2_unique3)
		 */
		setUp();
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique", "u23", FieldType.UNIQUE);
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique2", "u23", FieldType.UNIQUE);
		
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u24_unique3", "u24", FieldType.UNIQUE);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u24_unique3 UNIQUE (u24)");
		
		assertEquals(expected, script);
	}
	
	public void testChangeIncreaseUnique(){
		/**
		 * database
		 * table2 	- u23	unique(table2_u2_unique)
		 * 			- u24	
		 * 
		 * hiberante
		 * table2	- u23	unique(table2_u2_unique)
		 *			- u24	unique(table2_u2_unique)
		 */
		
		setUp();
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u2_unique", "u23", FieldType.UNIQUE);
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u2_unique", "u23", FieldType.UNIQUE);
		
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u2_unique", "u24", FieldType.UNIQUE);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_u2_unique\"");
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u2_unique UNIQUE (u23,u24)");
		assertEquals(expected, script);
	}
	
	public void testChangeReduceUnique(){
		setUp();
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		
		t.addConstraint("table2_u2_unique", "u23", FieldType.UNIQUE);
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		t.addConstraint("table2_u2_unique", "u24", FieldType.UNIQUE);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		
		
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u2_unique", "u24", FieldType.UNIQUE);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_u2_unique\"");
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u2_unique UNIQUE (u24)");
		assertEquals(expected, script);
	}
	
	public void testRemoveUnique(){
		setUp();
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique", "u23", FieldType.UNIQUE);
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u24_unique", "u24", FieldType.UNIQUE);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		c.setUnique();
		t.addColumn(c);
		t.addConstraint("table2_u23_unique", "u23", FieldType.UNIQUE);
		
		c = new ColumnM(t, "u24", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_u24_unique\"");
		
		assertEquals(expected, script);
	}
	
	public void testRemoveFk(){
		setUp();
		TableM t2 = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t3 = databaseM.getTable("table3");
		ColumnM referencedCol = t3.getColumn("u33");
		
		t2.addForeignKey("table2_u23_fk", "u23", referencedCol );
		
		
		t2 = hibernateM.getTable("table2");
		c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
				
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT table2_u23_fk");
		
		assertEquals(expected, script);
	}
	
	public void testAddFk(){
		setUp();
		TableM t2 = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		t2 = hibernateM.getTable("table2");
		c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t3 = hibernateM.getTable("table3");
		ColumnM referencedCol = t3.getColumn("u33");
		
		t2.addForeignKey("table2_u23_fk", "u23", referencedCol );
				
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u23_fk FOREIGN KEY (u23) REFERENCES table3(u33)");
		assertEquals(expected, script);
	}
	
	public void testChangeFk(){
		setUp();
		//datbase
		TableM t2 = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t3 = hibernateM.getTable("table3");
		
		t2.addForeignKey("table2_u23_fk", "u23",  t3.getColumn("u33") );
		
		//hibernate
		t2 = hibernateM.getTable("table2");
		c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t4 = hibernateM.getTable("table4");
		
		t2.addForeignKey("table2_u23_fk", "u23", t4.getColumn("u41") );
				
		//pre-schema
		List<String> script = getPreSchema();
		assertEquals(0, script.size());

		//post-schema
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT table2_u23_fk");
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u23_fk FOREIGN KEY (u23) REFERENCES table4(u41)");
		assertEquals(expected, script);
	}
	
	public abstract void testRemoveColumnWithFk();
}