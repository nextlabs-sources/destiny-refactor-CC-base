/*
 * Created on Apr 5, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ConstraintM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.FieldType;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ForeignKeyM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.SequenceM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/MappingDbHibComparator.java#1 $
 */

public class MappingDbHibComparator extends MappingComparator {
    public static final int DROP_TABLE ;
    public static final int DROP_COLUMN;
    public static final int CREATE_TEMP_COLUMN ;
    public static final int ALTER_COLUMN ;
    public static final int ALTER_CONSTRAINTS;
    public static final int ALTER_INDEX;
    static{
        int i =0;
        DROP_TABLE          = 1 << i++;
        DROP_COLUMN         = 1 << i++;
        CREATE_TEMP_COLUMN  = 1 << i++;
        ALTER_COLUMN        = 1 << i++;
        ALTER_CONSTRAINTS   = 1 << i++;
        ALTER_INDEX         = 1 << i++;
    }

    public MappingDbHibComparator(DatabaseM dbDatabase, DatabaseM hibDatabase,
            DialectExtended dialect) {
        //database1 is databaseM;
        //database2 is hibernateM;
        super(dbDatabase, hibDatabase, dialect);
    }
    
    /**
     * dropped all foreign key ,table, sequence in this order
     * @return a List of SQL statements
     */
    public List<String> dropAllExistMappedTable(){
        ArrayList<String> script = new ArrayList<String>();
        ArrayList<TableM> willDropTables = new ArrayList<TableM>();
        
        //drop all foreign keys first
        Set<String> hibTableNames = database2.getTableNames();
        for(String hibTableName : hibTableNames){
            
            TableM hibTableM = database2.getTable(hibTableName);
            TableM dbTableM = database1.getTable(hibTableName);
            if(hibTableM != null && dbTableM != null){
                //drop all constraints first
                List<ConstraintM> constraints = dbTableM.getConstraints(FieldType.FOREIGN_KEY);
                for(ConstraintM constraint : constraints){
                    script.addAll(constraint.sqlDropConstraint(d));
                }
                willDropTables.add(dbTableM);
            }
        }
        
        //then drop all tables
        for(TableM willDropTable : willDropTables){
            script.add(willDropTable.sqlDropString(d));
        }
        
        //drop all sequence
        Set<String> hibSequenceNames = database2.getSequenceNames();
        for(String hibSequenceName : hibSequenceNames){
            SequenceM hibSequenceM = database2.getSequence(hibSequenceName);
            
            SequenceM dbSequenceM = database1.getSequence(hibSequenceName);
            if(hibSequenceM != null && dbSequenceM != null){
                //drop all constraints first
                script.add(dbSequenceM.sqlDrop());
            }
        }

        return script;
    }
    
    @Override
    protected void findMisMatchTable(TableM dbTable) {
        if (has(DROP_TABLE)) {
            scripts.add(dbTable.sqlDropString(d));
        }
    }

    @Override
    protected void findMisMatchColumn(ColumnM dbColumn) {
        if (has(DROP_COLUMN)) {
            //find in database but not in hibernate
            //scripts.addAll(dbColumn.sqlDropColumn(d));
        }
    }

    protected void compareColumn(
            ColumnM dbColumnM, 
            ColumnM hibColumnM) {
        if (has(CREATE_TEMP_COLUMN)) {
            //create a clone of the column if datatype is changed
            short dbColumnDataType = dbColumnM.getDataType();
            short hibColumnDataType = hibColumnM.getDataType();
            if (!d.isSameType(dbColumnDataType, hibColumnDataType)) {
                //this item could be a customed type
                String hibCustomType = hibColumnM.getSqlTypeName();
                String hibDialectTypeName = hibCustomType == null
                        ? hibColumnM.getDialectType(d)
                        : hibCustomType;

                String dbDialectTypeName = dbColumnM.getDialectType(d);

                if (!dbDialectTypeName.equals(hibDialectTypeName)) {
                    //if they are differenet type
                    scripts.addAll(hibColumnM.sqlCloneTempColumn(d));
                }
            }
        }

        if (has(ALTER_COLUMN)) {
            //if type is same AND the type can have length AND differnet length
            //then change the length

            short dbColumnDataType = dbColumnM.getDataType();
            short hibColumnDataType = hibColumnM.getDataType();
            if (d.isSameType(dbColumnDataType, hibColumnDataType)) {
                //if they have same data type, then compare length
                if (d.doesTypeHaveLength(dbColumnDataType)) {
                    if (!d.isSameLength(dbColumnM, hibColumnM)) {
                        String hibCustomType = hibColumnM.getSqlTypeName();
                        if (hibCustomType != null) {
                            scripts.add(dbColumnM.sqlAlterColumnChangeDataType(hibCustomType, d));
                        } else {
                            scripts.add(dbColumnM.sqlAlterColumnNewLength(hibColumnM.getColumnSize(), d));
                        }
                    }
                }
            }//end check same type
            
            //compare null
            if (dbColumnM.isNullable() != hibColumnM.isNullable()) {
                scripts.addAll(dbColumnM.sqlAlterColumnSetNullable(hibColumnM.isNullable(), d));
            }

            if (dbColumnM.isUnique()) {
                if (hibColumnM.isUnique()) {
                    List<ConstraintM> hibUniques = hibColumnM.getConstraints(FieldType.UNIQUE);
                    List<ConstraintM> dbUniques = dbColumnM.getConstraints(FieldType.UNIQUE);
                    for (ConstraintM dbUnique : dbUniques) {
                        boolean isFound = false;
                        for (ConstraintM hibUnique : hibUniques) {
                            if (dbUnique.equalColumns(hibUnique)) {
                                isFound = true;
                                break;
                            }
                        }
                        
                        if (!isFound) {
                            scripts.addAll(dbUnique.sqlDropConstraint(d));
                        }
                    }
                } else {
                    //drop unique
                    List<ConstraintM> dbUniques = dbColumnM.getConstraints(FieldType.UNIQUE);
                    for (ConstraintM dbUnique : dbUniques) {
                        scripts.addAll(dbUnique.sqlDropConstraint(d));
                    }
                }
            }//end check unique
        }//end doAlterAttribute
    }

    protected void compareConstraints(TableM dbTableM, TableM hibTableM) {
        
        if (has(ALTER_CONSTRAINTS)) {
            List<ConstraintM> dbPkeys = dbTableM.getConstraints(FieldType.PRIMARY_KEY);
            for (ConstraintM dbPKey : dbPkeys) {
                List<ConstraintM> hibConstraints = hibTableM.getConstraints(FieldType.PRIMARY_KEY);
                ConstraintM hibPkey = hibConstraints.size() > 0 ? hibConstraints.get(0) : null;
    
                if (!dbPKey.equalColumns(hibPkey)) {
                    //not find the constraint in hibernate
                    scripts.addAll(dbPKey.sqlDropConstraint(d));
                }
            }
        }
        
        if (has(ALTER_CONSTRAINTS)) {
            List<ConstraintM> dbFkeys = dbTableM.getConstraints(FieldType.FOREIGN_KEY);
            for (ConstraintM dbFkey : dbFkeys) {
                ForeignKeyM dbFk = (ForeignKeyM) dbFkey;
                ForeignKeyM hibFk = (ForeignKeyM)hibTableM.getConstraint(dbFk.getName(),FieldType.FOREIGN_KEY);
                
                if (hibFk == null || !hibFk.equalColumns(dbFk)) {
                    scripts.addAll(dbFkey.sqlDropConstraint(d));
                }
            }
        }
        
        if (has(ALTER_CONSTRAINTS) && has(ALTER_INDEX)) {
            List<ConstraintM> dbIndexes = dbTableM.getConstraints(FieldType.INDEX);
            for (ConstraintM dbIndex : dbIndexes) {
                ConstraintM hibIndex = hibTableM.getConstraint(dbIndex.getName(), FieldType.INDEX);
                if (!dbIndex.equals(hibIndex)) {
                    //not find the constraint in hibernate
                    scripts.addAll(dbIndex.sqlDropConstraint(d));
                }
            }
        }
    }

}
