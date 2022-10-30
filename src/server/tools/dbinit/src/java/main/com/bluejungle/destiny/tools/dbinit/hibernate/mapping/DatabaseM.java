package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.dialect.Dialect;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class DatabaseM extends FieldAbstract{
	private List<TableM> tables;
	private List<SequenceM> sequences;
	private final String schema;

	public DatabaseM(String name, String schema) {
		super(name, FieldType.DATABASE);
		tables = new ArrayList<TableM>();
		sequences = new ArrayList<SequenceM>();
		this.schema = schema;
	}

	public void addSequences(String name) {
		sequences.add(new SequenceM(name));
	}
	
	public Set<String> getSequenceNames() {
		Set<String> set = new TreeSet<String>();
		for (SequenceM sequence : sequences) {
			set.add(sequence.getName());
		}
		return set;
	}

	public SequenceM getSequence(String name) {
		return (SequenceM) getObjByName(sequences, name);
	}

	public void addTable(TableM table) {
		tables.add(table);
	}
	
	public void addTable(String tableName, DatabaseMetaData meta) throws SQLException {
		TableM table = new TableM(this, tableName);

		//for each column
		ResultSet rs = null;
		try {
			//catalog is null
			//schema is null
			rs = meta.getColumns(null, schema, tableName, null);
			while (rs.next()) {
				table.addColumn(new ColumnM(table, rs));
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		addTable(table);
	}
	
	public TableM getTable(String tableName) {
		return (TableM) getObjByName(tables, tableName);
	}

	public Set<String> getTableNames() {
		Set<String> set = new TreeSet<String>();
		for (TableM table : tables) {
			set.add(table.getName());
		}
		return set;
	}
	
	public List<TableM> getTables(){
	    return tables;
	}
	
//	public void addSequence(SequenceM sequence){
//		sequences.add(sequence);
//	}
//	
//	public SequenceM getSequence(String seqName){
//		return (SequenceM) getObjByName(sequences, seqName);
//	}

	@Override
	public String toString() {
		String output = "DATABASE :" + getName() + "\n";

		//print ordered by name
		Set<String> tableNames = getTableNames();
		for (String tableName : tableNames) {
			output += getTable(tableName).toString().replaceAll(TAB, "\t");
			;
		}
		return output;
	}
	
	@Deprecated
	public List<String> sqlDropAllTempColumn(Dialect d) {
		ArrayList<String> script = new ArrayList<String>();

		for (TableM table : tables) {
			script.addAll(table.sqlDropAllTempColumn(d));
		}

		return script;
	}
	
}
