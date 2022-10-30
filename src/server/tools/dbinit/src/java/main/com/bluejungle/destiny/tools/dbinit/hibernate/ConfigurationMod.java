package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.engine.Mapping;
import net.sf.hibernate.id.PersistentIdentifierGenerator;
import net.sf.hibernate.mapping.Table;

import com.bluejungle.destiny.tools.dbinit.DBInitException;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;


/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class ConfigurationMod extends net.sf.hibernate.cfg.Configuration {
	public enum Action {
        CREATE_SCHEMA() {
            @Override
            public void generate(
                    ConfigurationMod configMod, 
                    MappingDbHibComparator dbHibComparator, 
                    MappingHibDbComparator hibDbComparator,
                    List<String> statements
            ) throws DBInitException, HibernateException {
                // hibernate can handle this correctly
                Collections.addAll(statements, configMod.generateSchemaCreationScript(configMod.getDialect()));
            }
        },
//        DROP_MAPPED_THEN_CREATE_SCHEMA() {
//            @Override
//            public void generate(ConfigurationMod configMod, 
//                    MappingDbHibComparator dbHibComparator, MappingHibDbComparator hibDbComparator,
//                    List<String> statements) throws DBInitException, HibernateException {
//                DROP_MAPPED_SCHEMA.generate(configMod, dialectX, dbHibComparator, hibDbComparator, statements);
//                CREATE_SCHEMA.generate(configMod, dialectX, dbHibComparator, hibDbComparator, statements);
//            }
//        },
        DROP_EXIST_MAPPED_THEN_CREATE_SCHEMA() {
            @Override
            public void generate(
                    ConfigurationMod configMod, 
                    MappingDbHibComparator dbHibComparator, 
                    MappingHibDbComparator hibDbComparator,
                    List<String> statements
            ) throws DBInitException, HibernateException {
                //dropped all foreign key ,table, sequence
                statements.addAll(dbHibComparator.dropAllExistMappedTable());
                
                CREATE_SCHEMA.generate(configMod, dbHibComparator, hibDbComparator, statements);
            }
        },
//        DROP_MAPPED_SCHEMA() {
//            @Override
//            public void generate(ConfigurationMod configMod, 
//                    MappingDbHibComparator dbHibComparator, MappingHibDbComparator hibDbComparator,
//                    List<String> statements) throws DBInitException, HibernateException {
//                //drop view
//                for(String viewName : configMod.dbViewNames){
//                    statements.add(dialectX.dropView(viewName));
//                }
//                
//                Collections.addAll(statements, configMod.generateDropSchemaScript(dialectX));
//            }
//        },
        UPDATE_SCHEMA() {
            @Override
            public void generate(
                    ConfigurationMod configMod, 
                    MappingDbHibComparator dbHibComparator, 
                    MappingHibDbComparator hibDbComparator,
                    List<String> statements
            ) throws DBInitException, HibernateException {
                statements.addAll(dbHibComparator.compare(
                        MappingDbHibComparator.DROP_COLUMN
                        | MappingDbHibComparator.ALTER_COLUMN
                        | MappingDbHibComparator.ALTER_CONSTRAINTS
                        | MappingDbHibComparator.CREATE_TEMP_COLUMN
                ));
                
                statements.addAll(hibDbComparator.compare(
                        MappingHibDbComparator.CREATE
                        | MappingHibDbComparator.ALTER_CONSTRAINTS
                ));
            }
        },
        PRE_SCHEMA() {
            @Override
            public void generate(
                    ConfigurationMod configMod, 
                    MappingDbHibComparator dbHibComparator, 
                    MappingHibDbComparator hibDbComparator,
                    List<String> statements
            ) throws DBInitException, HibernateException {
                statements.addAll(dbHibComparator.compare(
                        MappingDbHibComparator.ALTER_COLUMN
                        | MappingDbHibComparator.ALTER_CONSTRAINTS
                        | MappingDbHibComparator.CREATE_TEMP_COLUMN
                ));
                
                statements.addAll(hibDbComparator.compare(
                        MappingHibDbComparator.CREATE
                        | MappingHibDbComparator.ALTER_CONSTRAINTS
                ));
            }
        },
        POST_SCHEMA() {
            @Override
            public void generate(
                    ConfigurationMod configMod, 
                    MappingDbHibComparator dbHibComparator, 
                    MappingHibDbComparator hibDbComparator,
                    List<String> statements
            ) throws DBInitException, HibernateException {
                statements.addAll(dbHibComparator.compare(
                        MappingDbHibComparator.DROP_COLUMN
                        | MappingDbHibComparator.ALTER_COLUMN
                        | MappingDbHibComparator.ALTER_CONSTRAINTS
                ));
                
                statements.addAll(hibDbComparator.compare(
                        MappingHibDbComparator.CREATE
                ));
            }
        },
        ;
		
        List<String> generate(
                ConfigurationMod configMod, 
                MappingDbHibComparator dbHibComparator,
                MappingHibDbComparator hibDbComparator
        ) throws DBInitException, HibernateException {
            List<String> statements = new LinkedList<String>();
            generate(configMod, dbHibComparator, hibDbComparator, statements);
            return statements;
        }

        abstract void generate(
                ConfigurationMod configMod, 
                MappingDbHibComparator dbHibComparator, 
                MappingHibDbComparator hibDbComparator, List<String> statements
        ) throws DBInitException, HibernateException;
		
	};
	
	private DialectExtended dialectX;
	private DatabaseMetadataMod hibMeta;
	private DatabaseM databaseM ;
	private DatabaseM hibernateM;
	private Map<String, Table> tables;
	private Map<String, String> tableDbToHibStoreCase;
	
	//start construction
    public ConfigurationMod() {
        //nothing
    }
	
	public DatabaseM getHibernateM() {
		return hibernateM;
	}
	
	public DialectExtended getDialect() {
		return dialectX;
	}
	
    public void init(DatabaseMetadataMod hibernateDatabaseMetaData, DialectExtended dialectX)
            throws SQLException, DBInitException {
        this.dialectX = dialectX;
        hibMeta = hibernateDatabaseMetaData;
        DatabaseMetaData meta = hibMeta.getSqlDatabaseMetaData();
		if(meta.storesUpperCaseIdentifiers()){
			DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_UPPER_CASE);
		}else if(meta.storesLowerCaseIdentifiers()){
			DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_LOWER_CASE);
		}else if(meta.storesMixedCaseIdentifiers()){
			DatabaseHelper.setDbStoresIdentifiersType(DatabaseHelper.DB_IDENTIFIERS_STORE_MIXED_CASE);
		}else{
			throw new RuntimeException("What type of case does the database store identifers?");
		}
		
		//get all table name from hibernate
		tableDbToHibStoreCase = new TreeMap<String, String>();
        try {
            Field tablesField = Configuration.class.getDeclaredField("tables");
            tablesField.setAccessible(true);
            tables = (Map<String, Table>)tablesField.get(this);
        } catch (Exception e) {
            throw new DBInitException(e);
        }
        
        // match the case of the database
        Set<String> tableKeys = tables.keySet();
        for(String tableKey : tableKeys ){
            tableDbToHibStoreCase.put(DatabaseHelper.matchToDbStoreCase(tableKey), tableKey);
        }
        
        //call hibernate internal method
        try {
            Method spcMethod = Configuration.class.getDeclaredMethod("secondPassCompile");
            spcMethod.setAccessible(true);
            spcMethod.invoke(this);
        } catch (Exception e) {
            throw new DBInitException(e);
        }
	}
	
    public void constructMapping(String databaseName, Connection connection) throws SQLException,
            DBInitException {
		MappingConstructor c = new MappingConstructor(dialectX);
		databaseM = c.constructDatabaseMapping(databaseName, hibMeta);
		Mapping mapping;
        try {
            Field mappingField = Configuration.class.getDeclaredField("mapping");
            mappingField.setAccessible(true);
            mapping = (Mapping)mappingField.get(this);
        } catch (Exception e) {
            throw new DBInitException(e);
        }
		hibernateM = c.constructHibernateMapping(databaseName, tables, mapping);
		
		Iterator<PersistentIdentifierGenerator> seqIter;
		try {
            Method iterateMethod = Configuration.class.getDeclaredMethod("iterateGenerators", Dialect.class);
            iterateMethod.setAccessible(true);
            seqIter = (Iterator<PersistentIdentifierGenerator>) iterateMethod.invoke(this, dialectX);
        } catch (Exception e) {
            throw new DBInitException(e);
        }
		
		c.setSequences(seqIter, databaseM, hibernateM, connection);
	}
	
    public void updateDatabaseMapping(Connection connection) throws SQLException, DBInitException {
		MappingConstructor c = new MappingConstructor(dialectX);
		String databaseName = databaseM.getName();
		databaseM = c.constructDatabaseMapping(databaseName, hibMeta);
		Iterator<PersistentIdentifierGenerator> seqIter;
        try {
            Method iterateMethod = Configuration.class.getDeclaredMethod("iterateGenerators", Dialect.class);
            iterateMethod.setAccessible(true);
            seqIter = (Iterator<PersistentIdentifierGenerator>) iterateMethod.invoke(this, dialectX);
        } catch (Exception e) {
            throw new DBInitException(e);
        }
		c.setSequences(seqIter, databaseM, hibernateM, connection);
	}
	
	public List<String> generateSchema(Action action)
			throws DBInitException, HibernateException {
		final MappingDbHibComparator dbHibComparator =
                new MappingDbHibComparator(databaseM, hibernateM, dialectX);
        final MappingHibDbComparator hibDbComparator =
                new MappingHibDbComparator(databaseM, hibernateM, dialectX);
		
		List<String> statements = action.generate(this, dbHibComparator, hibDbComparator);
		return statements;
	}

	
	public Table getTable(String dbTableName){
		return tables.get(getHibernateName(dbTableName));
	}

	private String getHibernateName(String dbName) {
		return tableDbToHibStoreCase.get(dbName);
	}
}

