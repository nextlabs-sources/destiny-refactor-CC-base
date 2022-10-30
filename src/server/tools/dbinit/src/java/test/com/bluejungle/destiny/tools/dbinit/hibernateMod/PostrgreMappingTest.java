package com.bluejungle.destiny.tools.dbinit.hibernateMod;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernateMod.mapping.TableM;

public class PostrgreMappingTest extends MappingTest {
	@Override
	public String getDialectClassString(){
		return net.sf.hibernate.dialect.PostgreSQLDialect.class.getName();
	}
	
	public void testAddTable(){
		TableM t = hibernateM.getTable("table99");
		assertNull(t);
		t = new TableM(hibernateM, "table99");
		hibernateM.addTable(t);
		t.addColumn(new ColumnM(t, "n991", "varchar(23)", (short)Types.VARCHAR, 23, 0, false));
		t.addColumn(new ColumnM(t, "n992", "int2", (short)Types.SMALLINT, 0, 0, false));
		
		List<String> script = generateAlterSchemaScript( true, false, true, true);
		
		//becuase doAdd is false
		assertEquals(0, script.size());
		
		//pre-schema
		script = getPreSchema();
		
		List<String> expected = new ArrayList<String>();
		expected.add("create table table99 (n991 varchar(23) not null,n992 int2 not null)");
		
		assertEquals(expected, script);
		
		//post-schema
		script = getPostSchema();
		assertEquals(0, script.size());
		
		//do it again
		script = generateAlterSchemaScript( true, true, true, true);
		//same thing won't add twice
		assertEquals(0, script.size());
	}

	public void testAddTableWithIndex(){
		setUp();
		TableM t = new TableM(hibernateM, "table5");
		hibernateM.addTable(t);
		t.addColumn(new ColumnM(t, "n51", null, (short)Types.VARCHAR, 23, 0, false));
		t.addColumn(new ColumnM(t, "n52", null, (short)Types.SMALLINT, 0, 0, false));
		t.addConstraint("table5_index", "n52", FieldType.INDEX);
		
		//pre-schema
		List<String> script = getPreSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("create table table5 (n51 varchar(23) not null,n52 int2 not null)");
		
		assertEquals(expected, script);
		
		//post-schema should not have anything since the table is not created
		script = getPostSchema();
		assertEquals(0, script.size());
		
		//add the table
		t = new TableM(databaseM, "table5");
		databaseM.addTable(t);
		t.addColumn(new ColumnM(t, "n51", null, (short)Types.VARCHAR, 23, 0, false));
		t.addColumn(new ColumnM(t, "n52", null, (short)Types.SMALLINT, 0, 0, false));

		//pre-schema has nothing
		script = getPreSchema();
		assertEquals(0, script.size());
		
		//post-schema should not have anything since the table is not created
		script = getPostSchema();
		expected.clear();
		expected.add("CREATE INDEX table5_index ON table5 (n52)");
		assertEquals(expected, script);
	}
	
	public void testAddColumn(){
		TableM t = hibernateM.getTable("table2");
		assertNotNull(t);
		t.addColumn(new ColumnM(t, "n23", null, (short)Types.VARCHAR, 23, 0, false));
		
		List<String> script = generateAlterSchemaScript( true, false, true, true);

		//because doAdd is false
		assertEquals(0, script.size());
		
		//pre-schema
		script = getPreSchema();
		
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 add column n23 varchar(23)");
		expected.add("UPDATE table2 SET n23='' WHERE n23 IS NULL");
		expected.add("ALTER TABLE table2 ALTER COLUMN n23 SET NOT NULL");
		assertEquals(expected, script);

		//post schema
		script = getPostSchema();
		assertEquals(0, script.size());
		
		script = generateAlterSchemaScript( true, true, true, true);
		assertEquals(0, script.size());
	}
	
	public void testRemoveColumn(){
		TableM t = databaseM.getTable("table2");
		assertNotNull(t);
		ColumnM c = new ColumnM(t, "n24", null, (short)Types.BIT, 0, 0, false);
		t.addColumn(c);
		
		//pre-schema
		List<String> script = getPreSchema();//becuase doAdd is false
		assertEquals(0, script.size());
		
		script = getPostSchema();
		
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP \"n24\"");
		
		assertEquals(expected, script);
		
		script = generateAlterSchemaScript( true, true, true, true);
		assertEquals(0, script.size());
	}
	
	public void testSetNullable(){
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "n25", null, (short)Types.BIT, 0, 0, false);
		t.addColumn(c);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "n25", null, (short)Types.BIT, 0, 0, true);
		t.addColumn(c);
		
		//pre-schema
		List<String> script = getPreSchema();//because doAdd is false
		assertEquals(0, script.size());
		
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ALTER COLUMN n25 DROP NOT NULL");
		
		assertEquals(expected, script);
	}
	
	public void testDropNullable(){
		TableM t = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t, "n26", null, (short)Types.BIT, 0, 0, true);
		t.addColumn(c);
		
		t = hibernateM.getTable("table2");
		c = new ColumnM(t, "n26", null, (short)Types.BIT, 0, 0, false);
		t.addColumn(c);
		
		//pre-schema
		List<String> script = getPreSchema();//becuase doAdd is false
		assertEquals(0, script.size());
		
		script = getPostSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 ALTER COLUMN n26 SET NOT NULL");
		
		assertEquals(expected, script);
	}
	
	public void testRemoveColumnWithPK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addColumn(new ColumnM(t, "cp23", null, (short)Types.CHAR, 0, 0, false));
		t.addConstraint("table2_pkey", "cp23", FieldType.PRIMARY_KEY);
		
		
		//pre-schema
		List<String> script = getPreSchema();//becuase doAdd is false
		assertEquals(0, script.size());
		
		script = getPostSchema();
		
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey\"");
		expected.add("ALTER TABLE table2 DROP \"cp23\"");
		
		
		assertEquals(expected, script);
	}
	
	public void testAddRemoveColumnWithPK(){
		setUp();
		TableM t = databaseM.getTable("table2");
		t.addColumn(new ColumnM(t, "cp23", null, (short)Types.CHAR, 0, 0, false));
		t.addConstraint("table2_pkey", "cp23", FieldType.PRIMARY_KEY);
		t = hibernateM.getTable("table2");
		t.addColumn(new ColumnM(t, "cp24", null, (short)Types.TIMESTAMP, 0, 0, false));
		t.addConstraint("table2_pkey", "cp24", FieldType.PRIMARY_KEY);
		t.addConstraint("table2_pkey", "c21", FieldType.PRIMARY_KEY);
		
		
		//pre-schema
		List<String> script = getPreSchema();//becuase doAdd is false
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 add column cp24 timestamp");
		expected.add("UPDATE table2 SET cp24='0000-1-1' WHERE cp24 IS NULL");
		expected.add("ALTER TABLE table2 ALTER COLUMN cp24 SET NOT NULL");
		assertEquals(expected, script);
		
		script = getPostSchema();
		expected.clear();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT \"table2_pkey\"");
		expected.add("ALTER TABLE table2 DROP \"cp23\"");
		expected.add("ALTER TABLE table2 ADD PRIMARY KEY (cp24,c21)");
		
		assertEquals(expected, script);
	}
	
	public void testAddRemoveColumn(){
		setUp();
		TableM t = hibernateM.getTable("table2");
		assertNotNull(t);
		ColumnM c = new ColumnM(t, "n23", null, (short)Types.VARCHAR, 23, 0, false);
		t.addColumn(c);
		
		t = databaseM.getTable("table2");
		assertNotNull(t);
		c = new ColumnM(t, "n24", null, (short)Types.BIT, 0, 0, false);
		t.addColumn(c);
		
		List<String> script = generateAlterSchemaScript( false, false, false, false);
		//nothing should happen
		assertEquals(0, script.size());
		
		//pre-schema
		script = getPreSchema();
		
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 add column n23 varchar(23)");
		expected.add("UPDATE table2 SET n23='' WHERE n23 IS NULL");
		expected.add("ALTER TABLE table2 ALTER COLUMN n23 SET NOT NULL");
		
		assertEquals(script, expected);
		
		//post-schema
		script = getPostSchema();
		
		expected.clear();
		expected.add("ALTER TABLE table2 DROP \"n24\"");
		
		assertEquals(expected, script);
		
		//do it again
		script = generateAlterSchemaScript( true, true, true, true);
		assertEquals(0, script.size());
	}

	public void testReduceLength(){
		TableM t = hibernateM.getTable("table3");
		t.addColumn(new ColumnM(t, "c33", null, (short)Types.VARCHAR, 10, 0, false));
		
		t = databaseM.getTable("table3");
		t.addColumn(new ColumnM(t, "c33", "varchar", (short)Types.VARCHAR, 30, 0, false));
		
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		script = getPostSchema();

		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table3 ALTER COLUMN c33 TYPE varchar(10)");
		
		assertEquals(expected, script);
	}
	
	public void testIncreaseLength(){
		TableM t = hibernateM.getTable("table3");
		t.addColumn(new ColumnM(t, "c34", null, (short)Types.VARCHAR, 31, 0, false));
		
		t = databaseM.getTable("table3");
		t.addColumn(new ColumnM(t, "c34", "varchar", (short)Types.VARCHAR, 30, 0, false));
		
		List<String> script = generateAlterSchemaScript( false, false, false, false);
		assertEquals(0, script.size());
		
		script = generateAlterSchemaScript( true, true, true, true);

		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table3 ALTER COLUMN c34 TYPE varchar(31)");
		
		assertEquals(expected, script);
	}
	
	public void testIncreaseLengthWithDifferentTypeCodeButSameTypeInDatabase(){
		TableM t = hibernateM.getTable("table3");
		t.addColumn(new ColumnM(t, "c35", null, (short)Types.VARCHAR, 31, 0, true));
		
		t = databaseM.getTable("table3");
		t.addColumn(new ColumnM(t, "c35", "varchar", (short)Types.CHAR, 1, 0, true));
		
		List<String> script = getPreSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table3 RENAME COLUMN c35 TO c35_t");
		expected.add("ALTER TABLE table3 add column c35 varchar(31)");
		
		assertEquals(expected, script);
		
		script = getPostSchema();
		expected.clear();
		expected.add("ALTER TABLE table3 DROP \"c35_t\"");
		assertEquals(expected, script);
	}
	
	public void testNoChangeLength(){
		TableM t = hibernateM.getTable("table3");
		t.addColumn(new ColumnM(t, "c36", null, (short)Types.VARCHAR, 30, 0, false));
		
		t = databaseM.getTable("table3");
		t.addColumn(new ColumnM(t, "c36", "varchar", (short)Types.VARCHAR, 30, 0, false));
		
		List<String> script = getPreSchema();
		assertEquals(0, script.size());
		
		script = getPostSchema();
		assertEquals(0, script.size());;
	}
	
	public void testChangeDataType(){
		setUp();
		TableM t = hibernateM.getTable("table3");
		t.addColumn(new ColumnM(t, "c35", null, (short)Types.INTEGER, 0, 0, false));
		
		t = databaseM.getTable("table3");
		t.addColumn(new ColumnM(t, "c35", null, (short)Types.VARCHAR, 30, 0, false));
		
//		pre-schema
		List<String> script = getPreSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table3 RENAME COLUMN c35 TO c35_t");
		expected.add("ALTER TABLE table3 add column c35 int4");
		expected.add("UPDATE table3 SET c35=0 WHERE c35 IS NULL");
		expected.add("ALTER TABLE table3 ALTER COLUMN c35 SET NOT NULL");
		
		assertEquals(expected, script);
		
		//post-schema
		script = getPostSchema();
				
		expected.clear();
		expected.add("ALTER TABLE table3 DROP \"c35_t\"");
		assertEquals(expected, script);
		//do it again
		script = generateAlterSchemaScript( true, true, true, true);
	}
	
	public void testRemoveColumnWithFk(){
		setUp();
		//datbase
		TableM t2 = databaseM.getTable("table2");
		ColumnM c = new ColumnM(t2, "u23", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t3 = hibernateM.getTable("table3");
		
		t2.addForeignKey("table2_u23_fk", "u23",  t3.getColumn("u33") );
		
		//hibernate
		t2 = hibernateM.getTable("table2");
		c = new ColumnM(t2, "c25", null, (short)Types.VARCHAR, 23, 0, false);
		t2.addColumn(c);
		
		TableM t4 = hibernateM.getTable("table4");
		
		t2.addForeignKey("table2_u23_fk", "c25", t4.getColumn("u41") );
				
		//pre-schema
		List<String> script = getPreSchema();
		List<String> expected = new ArrayList<String>();
		expected.add("ALTER TABLE table2 add column c25 varchar(23)");
		expected.add("UPDATE table2 SET c25='' WHERE c25 IS NULL");
		expected.add("ALTER TABLE table2 ALTER COLUMN c25 SET NOT NULL");
		
		assertEquals(expected, script);

		//post-schema
		script = getPostSchema();
		expected.clear();
		expected.add("ALTER TABLE table2 DROP CONSTRAINT table2_u23_fk");
		expected.add("ALTER TABLE table2 DROP \"u23\"");
		expected.add("ALTER TABLE table2 ADD CONSTRAINT table2_u23_fk FOREIGN KEY (c25) REFERENCES table4(u41)");
		assertEquals(expected, script);
	}
	
	public void testDifferentTypeWithSameLength(){
		ColumnM dbColumn = new ColumnM(null, "c23", "varchar", (short)Types.VARCHAR, -1, 0, false);
		ColumnM hibColumn = new ColumnM(null, "c23", null, (short)Types.CLOB, 255, 0, false);
		assertTrue(dialect.isSameLength(dbColumn, hibColumn));
		
		dbColumn = new ColumnM(null, "c23", "text", (short)Types.CLOB, 255, 0, false);
		hibColumn = new ColumnM(null, "c23", null, (short)Types.VARCHAR, -1, 0, false);
		assertFalse(dialect.isSameLength(dbColumn, hibColumn));
	}
	
}
