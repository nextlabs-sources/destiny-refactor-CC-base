/*
 * Created on Apr 5, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate;

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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/MappingHibDbComparator.java#1 $
 */

public class MappingHibDbComparator extends MappingComparator {
    public static final int CREATE;
    public static final int ALTER_CONSTRAINTS;
    static{
        int i =0;
        CREATE              = 1 << i++;
        ALTER_CONSTRAINTS   = 1 << i++;
    }

    public MappingHibDbComparator(DatabaseM databaseM, DatabaseM hibDatabaseM,
            DialectExtended dialect) {
        super(hibDatabaseM, databaseM, dialect);
    }
    
    @Override
    protected void compareDb(DatabaseM hibDatabase, DatabaseM dbDatabase) {
        super.compareDb(hibDatabase, dbDatabase);
        
        //the constraint must be created after the tables are created
        if (has(ALTER_CONSTRAINTS)) {
            List<TableM> hibTables = hibDatabase.getTables();
            for(TableM hibTable : hibTables){
                TableM dbTable = dbDatabase.getTable(hibTable.getName());
                
                if (dbTable != null) {
                    compareConstraints(hibTable, dbTable);
                }
            }
        }
        
        //the sequence must be created after the tables are created
        if (has(CREATE)) {
            Set<String> hibSequenceNames = hibDatabase.getSequenceNames();
            for (String hibSequenceName : hibSequenceNames) {
                SequenceM hibSequence = hibDatabase.getSequence(hibSequenceName);
                SequenceM dbSequence = dbDatabase.getSequence(hibSequenceName);
                if (dbSequence == null) {
                    scripts.add(hibSequence.sqlCreate());
                }
            }
        }
    }

    @Override
    protected void findMisMatchTable(TableM hibTable) {
        if (has(CREATE)) {
            scripts.addAll(hibTable.sqlCreateString(d));
        }
    }

    @Override
    protected void findMisMatchColumn(ColumnM hibColumn) {
        if (has(CREATE)) {
            scripts.addAll(hibColumn.sqlAddColumn(d));
        }
    }
    
    
    @Override
    protected void compareColumn(ColumnM hibColumnM, ColumnM dbColumnM) {
        if (!has(ALTER_CONSTRAINTS)) {
            //only run if ALTER_CONSTRAINTS
            return;
        }
        
        if (hibColumnM.isPrimary()) {
            ConstraintM hibPrimaryKey = hibColumnM.getConstraints(FieldType.PRIMARY_KEY).get(0);
            List<ConstraintM> dbConstraints = dbColumnM.getConstraints(FieldType.PRIMARY_KEY);
            boolean isAdd = false;
            if (dbConstraints.size() == 0) {
                isAdd = true;
            } else {
                ConstraintM dbPrimaryKey = dbConstraints.get(0);
                if (dbPrimaryKey.isDropped()) {
                    isAdd = true;
                } else {
                    if (!dbColumnM.isPrimary()) {
                        isAdd = true;
                    } else {
                        //nothing, db and hibernate both is primary key
                    }
                }
            }
            
            if(isAdd){
                scripts.addAll(hibPrimaryKey.sqlAddConstraint(d));
            }
        }
        
        if(hibColumnM.isIndex() ){
            List<ConstraintM> hibConstraints = hibColumnM.getConstraints(FieldType.INDEX);
            for (ConstraintM hibConstraint : hibConstraints) {
                ConstraintM dbConstraint = dbColumnM.getConstraint(hibConstraint.getName(), FieldType.INDEX);
                boolean isAdd = false;
                if (dbConstraint == null) {
                    isAdd = true;
                } else {
                    if (dbConstraint.isDropped()) {
                        isAdd = true;
                    } else {
                        if (!dbColumnM.isIndex()) {
                            isAdd = true;
                        } else {
                            //nothing, db and hibernate both is index key
                        }
                    }
                }
                
                if(isAdd){
                    scripts.addAll(hibConstraint.sqlAddConstraint(d));
                }
            }
        }
        
        if(hibColumnM.isUnique() ){
            List<ConstraintM>  hibConstraints = hibColumnM.getConstraints(FieldType.UNIQUE);
            
            for (ConstraintM hibConstraint : hibConstraints) {
                ConstraintM dbConstraint = dbColumnM.getConstraint(hibConstraint.getName(), FieldType.UNIQUE);
                if(dbConstraint == null){
                    //is it different name but have same mapping?
                    boolean isFound = false;
                    List<ConstraintM> dbUniqueConstraints = dbColumnM.getConstraints(FieldType.UNIQUE);
                    for (ConstraintM dbUniqueConstraint : dbUniqueConstraints) {
                        if (dbUniqueConstraint.equalColumns(hibConstraint)) {
                            isFound = true;
                            break;
                        }
                    }
                    
                    if(!isFound){
                        scripts.addAll(hibConstraint.sqlAddConstraint(d));
                    }
                }else{
                    if (dbConstraint.isDropped()) {
                        scripts.addAll(hibConstraint.sqlAddConstraint(d));
                    } else {
                        if (!dbColumnM.isUnique()) {
                            scripts.addAll(hibConstraint.sqlAddConstraint(d));
                        }else{
                            //nothing, db and hibernate both is index key
                        }
                    }
                }
            }
        }
    }
    
    
    
    @Override
    protected void compareConstraints(TableM hibTable, TableM dbTable) {
        List<ConstraintM> hibConstraints = hibTable.getConstraints(FieldType.FOREIGN_KEY);
        for(ConstraintM hibConstraint : hibConstraints){
            ForeignKeyM hibFk = (ForeignKeyM)hibConstraint;
            ForeignKeyM dbFk = (ForeignKeyM)dbTable.getConstraint(hibFk.getName(),FieldType.FOREIGN_KEY);
            if(dbFk == null){
                //fk only in hibernate but not in db
                scripts.addAll(hibFk.sqlAddConstraint(d));
            }else{
                if(dbFk.isDropped() || hibFk.isDropped()){
                    //need to add it back since db fk is dropped
                    scripts.addAll(hibFk.sqlAddConstraint(d));
                }else{
                    if(!dbFk.equalColumns(hibFk)){
                        scripts.addAll(hibConstraint.sqlAddConstraint(d));
                    }else{
                        //nothing, db and hibernate both is index key
                    }
                }
            }
        }
    }
}
