package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.MappingException;
import net.sf.hibernate.engine.Mapping;
import net.sf.hibernate.id.IdentityGenerator;
import net.sf.hibernate.id.PersistentIdentifierGenerator;
import net.sf.hibernate.mapping.Column;
import net.sf.hibernate.mapping.ForeignKey;
import net.sf.hibernate.mapping.Index;
import net.sf.hibernate.mapping.PrimaryKey;
import net.sf.hibernate.mapping.SimpleValue;
import net.sf.hibernate.mapping.Table;
import net.sf.hibernate.mapping.UniqueKey;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ConstraintM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;

/**
 * build a database and hibernate mapping
 * the hibernate mapping name needs to match 
 * 
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class MappingConstructor{
	private static final Log LOG = LogFactory.getLog(MappingConstructor.class);
	public enum KeyAttribyte {FOREIGN_KEY, PRIMARY_KEY, INDEX, UNIQUE }
	private final DialectExtended dialect;
	
	public MappingConstructor(DialectExtended dialect){
		this.dialect = dialect;
	}

	public DatabaseM constructDatabaseMapping(String databaseName, DatabaseMetadataMod databaseMetadata)
			throws SQLException {
		LOG.info("START building database mapping");
		
		final String tableSchema = dialect.getTableSchema(databaseMetadata.getSqlDatabaseMetaData());
		final String catalog = null;
		
		DatabaseM databaseM = new DatabaseM(DatabaseHelper.matchToDbStoreCase(databaseName), tableSchema);
		
		//get all table names
		List<String> tableNames = databaseMetadata.getAllTableNames();
		
		//this is sql object
		DatabaseMetaData meta = databaseMetadata.getSqlDatabaseMetaData();
		
		//for each table
		for(String tableName : tableNames){
			databaseM.addTable(tableName, meta);
			TableM tableM = databaseM.getTable(tableName);
			
			//add primary
			List<Pair> primaryKeyAtts = getAttribute(KeyAttribyte.PRIMARY_KEY, catalog, tableSchema, tableName,
					meta);
			for (Pair att : primaryKeyAtts) {
				tableM.addConstraint(att.name, att.colName, FieldType.PRIMARY_KEY);
			}
						
			List<Pair> uniqueKeyAtts = getAttribute(KeyAttribyte.UNIQUE, catalog, tableSchema, tableName, meta);
			for (Pair att : uniqueKeyAtts) {
				if(dialect.isPrimaryImplyUnique()){
					//check if index is primary key
					//if not found, add
					ConstraintM constraintM = tableM.getConstraint(att.name, FieldType.PRIMARY_KEY);
					if(constraintM== null){
						tableM.addConstraint(att.name, att.colName, FieldType.UNIQUE);
					}
				}else{
					tableM.addConstraint(att.name, att.colName, FieldType.UNIQUE);
				}
			}
			
			//add index
			List<Pair> indexAtts = getAttribute(KeyAttribyte.INDEX, catalog, tableSchema, tableName, meta);
			for(Pair att : indexAtts ){
				boolean isImplyFromPrimaryKey;
				if(dialect.isPrimaryImplyIndex()){
					//check if index is primary key
					//if not found, add
					ConstraintM constraintM = tableM.getConstraint(att.name, FieldType.PRIMARY_KEY);
					isImplyFromPrimaryKey = (constraintM != null);
				}else{
					isImplyFromPrimaryKey = false;
				}
				
				if(!isImplyFromPrimaryKey){
					if(dialect.isUniqueImplyIndex()){
						//check if index is primary key
						//if not found, add
						ConstraintM constraintM = tableM.getConstraint(att.name, FieldType.UNIQUE);
						if(constraintM == null){
							tableM.addConstraint(att.name, att.colName, FieldType.INDEX);
						}
					}else{
						tableM.addConstraint(att.name, att.colName, FieldType.INDEX);
					}
				}
				
			}
			
		}
		
		//foreign keys need to do after the tables and columns are constructed
		for(String tablename: databaseM.getTableNames()){
			List<Pair> atts = getAttribute(KeyAttribyte.FOREIGN_KEY, catalog, tableSchema, tablename, meta);
			TableM tableM = databaseM.getTable(tablename);
			for(Pair att: atts){
				ColumnM referencedColumn = databaseM.getTable( att.tableName).getColumn(
						att.referencedColumn);
				tableM.addForeignKey( att.name, att.colName, referencedColumn);
			}
		}
		
		LOG.info("DONE building database mapping");
		return databaseM;
	}
	
	private Field tableIdValueField = null;
	
	private boolean isPrimaryKeyAssigned(Table table){
	    try {
            if(tableIdValueField == null){
                tableIdValueField = Table.class.getDeclaredField("idValue");
                tableIdValueField.setAccessible(true);
            }
            
            SimpleValue idValue = (SimpleValue)tableIdValueField.get(table);
            if(idValue == null){
                return false;
            }
            
            return idValue.createIdentifierGenerator(dialect) instanceof IdentityGenerator;
        } catch (Exception e) {
            LOG.fatal("fail to get idValue from hibernate table", e);
            //take a wild guess is false
            return false;
        }
    }
	
	
	
	public DatabaseM constructHibernateMapping(String databaseName, Map<String, Table> tables, Mapping mapping) {
		LOG.info("START building hibernate mapping");
		DatabaseM hibernateM = new DatabaseM(DatabaseHelper.matchToDbStoreCase(databaseName), null);
		
		//for each table
		Set<String> tableNames = tables.keySet();		
		for(String tableName : tableNames){
			TableM tableM = new TableM(hibernateM, DatabaseHelper.matchToDbStoreCase(tableName));
			
			Table table = tables.get(tableName);
			tableM.setPrimaryKeyAssigned(isPrimaryKeyAssigned(table));
			
			//for each column
			Iterator<Column> columnIterator = table.getColumnIterator();
			while(columnIterator.hasNext()){
				Column column = columnIterator.next();
				
				String columnName = DatabaseHelper.matchToDbStoreCase(column.getName());
				
				short dataType;
				try {
					dataType = (short)column.getType().sqlTypes(mapping)[ column.getTypeIndex() ];
				} catch (MappingException e) {
					throw new RuntimeException(e);
				}
				
				int length = column.getLength();
				length = dialect.getColumnLength(length, dataType);
				
				String sqlType = column.getSqlType();
//				if(sqlType == null){
//					sqlType = dialect.getTypeName(dataType, length);
//				}
				
				ColumnM columnM =new ColumnM(
						tableM,
						columnName,
						sqlType,
						dataType,
						length,
						0,
						column.isNullable()
						);
				columnM.setCheckConstraint(column.getCheckConstraint());
				tableM.addColumn(columnM);
				
				if(column.isUnique()){
					tableM.addConstraint(DatabaseHelper.matchToDbStoreCase(tableName + "_" + columnName + "_key"),
							columnName, FieldType.UNIQUE);
				}
			}
			
			//add primary key
			PrimaryKey pk = tables.get(tableName).getPrimaryKey();
			if(pk != null){
				String name;
				if(pk.getName() != null){
					name = DatabaseHelper.matchToDbStoreCase(pk.getName());
				}else{
					name = DatabaseHelper.matchToDbStoreCase(tableName + "_pkey");
				}
				Iterator<Column> subPkIterator = pk.getColumnIterator();
				while(subPkIterator.hasNext()){
					Column column = subPkIterator.next();
					String colName = DatabaseHelper.matchToDbStoreCase(column.getName());
					tableM.addConstraint(name, colName, FieldType.PRIMARY_KEY);
				}
			}
			
			//add indexes
			Iterator<Index> indexIterator = tables.get(tableName).getIndexIterator();
			while(indexIterator.hasNext()){
				Index index = indexIterator.next();
				String name = DatabaseHelper.matchToDbStoreCase(index.getName());
				Iterator<Column> subColumnIterator = index.getColumnIterator();
				while(subColumnIterator.hasNext()){
					Column column = subColumnIterator.next();
					String colName = DatabaseHelper.matchToDbStoreCase(column.getName());
					tableM.addConstraint(name, colName,FieldType.INDEX);
				}
			}
			
			Iterator<UniqueKey> uniqueIterator = tables.get(tableName).getUniqueKeyIterator();
			while(uniqueIterator.hasNext()){
				UniqueKey uniqueKey = uniqueIterator.next();
				String name = DatabaseHelper.matchToDbStoreCase(uniqueKey.getName());
				Iterator<Column> subIterator = uniqueKey.getColumnIterator();
				while(subIterator.hasNext()){
					Column column = subIterator.next();
					String colName = DatabaseHelper.matchToDbStoreCase(column.getName());
					tableM.addConstraint(name, colName, FieldType.UNIQUE);
				}
				
			}
			hibernateM.addTable(tableM);
		}
		//for each foreign Key
		for(String tableName : tableNames){
			TableM tableM = hibernateM.getTable(DatabaseHelper.matchToDbStoreCase(tableName));
			
			Iterator<ForeignKey> foreignIterator = tables.get(tableName).getForeignKeyIterator();
			while(foreignIterator.hasNext()){
				ForeignKey foreignKey = foreignIterator.next();
				
				String name = DatabaseHelper.matchToDbStoreCase(foreignKey.getName());
				String localColname = ((Column)foreignKey.getColumnIterator().next()).getName();
				localColname = DatabaseHelper.matchToDbStoreCase(localColname);
				
				Table refTable = foreignKey.getReferencedTable();
				String refTableName = DatabaseHelper.matchToDbStoreCase(refTable.getName());
				TableM refTableM = hibernateM.getTable(refTableName);
				Iterator<Column> iterator = refTable.getPrimaryKey().getColumnIterator();
				while(iterator.hasNext()){
					Column refColumn = iterator.next();
					String refColName = DatabaseHelper.matchToDbStoreCase(refColumn.getName());
					tableM.addForeignKey(name, localColname, refTableM.getColumn(refColName));
				}
				
			}
		}
		
		LOG.info("DONE building hibernate mapping");
		return hibernateM;
	}
	//end construction
	
	public void setSequences(Iterator<PersistentIdentifierGenerator> seqIter, 
			DatabaseM databaseM, 
			DatabaseM hibernateM,
			Connection connection) {
		while ( seqIter.hasNext() ) {
            String sequenceKey = (String) (seqIter.next()).generatorKey();
			sequenceKey = DatabaseHelper.matchToDbStoreCase(sequenceKey);
			
			hibernateM.addSequences(sequenceKey);
			
			if( dialect.isSequenceExist(connection, sequenceKey) ){
				databaseM.addSequences(sequenceKey);
			}
		}
	}
	
	static List<Pair> getAttribute(KeyAttribyte attribute, 
			String catalog, 
			String tableSchema,
			String tableName, 
			DatabaseMetaData meta) throws SQLException {
		ResultSet rs = null;
		List<Pair> list = new ArrayList<Pair>();
		try {
			switch (attribute) {
			case FOREIGN_KEY:
				rs = meta.getImportedKeys(catalog, tableSchema, tableName);
				while ( rs.next() ){
					String name = rs.getString(ResultSetKey.FK_NAME);
					String colName = rs.getString(ResultSetKey.FKCOLUMN_NAME);
					
					String pkColName = rs.getString(ResultSetKey.PKCOLUMN_NAME);
					String pkTableName = rs.getString(ResultSetKey.PKTABLE_NAME);
					if(colName != null){
						list.add(new Pair(name, colName, pkTableName, pkColName));
					}
				}
				break;
			case PRIMARY_KEY:
				rs = meta.getPrimaryKeys(catalog, tableSchema, tableName);
				while ( rs.next() ){
					String name = rs.getString(ResultSetKey.PK_NAME);
					String colName = rs.getString(ResultSetKey.COLUMN_NAME);
					if(colName != null){
						list.add(new Pair(name, colName));
					}
				}
				break;
			case INDEX:
				rs = meta.getIndexInfo(catalog, tableSchema, tableName, false, true);
				while ( rs.next() ){
					String name = rs.getString(ResultSetKey.INDEX_NAME);
					String colName = rs.getString(ResultSetKey.COLUMN_NAME);
					if(colName != null){
						list.add(new Pair(name, colName));
					}
				}
				break;
			case UNIQUE:
				rs = meta.getIndexInfo(catalog, tableSchema, tableName, true, true);
				while (rs.next()) {
					String name = rs.getString(ResultSetKey.INDEX_NAME);
					String colName = rs.getString(ResultSetKey.COLUMN_NAME);
					if (colName != null) {
						list.add(new Pair(name, colName));
					}
				}
				break;
			default:
				throw new IllegalArgumentException(" attributename \"" + attribute + "\" is unknown.");
			}
		} catch (SQLException sqlException){
		    if(tableName.startsWith(meta.getIdentifierQuoteString()) && tableName.endsWith(meta.getIdentifierQuoteString())){
		        //already put the quote
		        throw sqlException;
		    }
			tableName = meta.getIdentifierQuoteString() + tableName	+ meta.getIdentifierQuoteString();
			return getAttribute(attribute, catalog, tableSchema, tableName, meta);
		} finally  {
			if (rs != null){
				rs.close();
			}
		}
		return list;
	}
	
	
	static class Pair{
		String name;
		String colName;
		String tableName;
		String referencedColumn;
		
		Pair(String name, String colName) {
			this.name = name;
			this.colName = colName;
		}
		
		Pair(String name, String colName, String tableName, String referencedColumn) {
			this(name, colName);
			this.tableName = tableName;
			this.referencedColumn = referencedColumn;
		}
	}
	
}
