package com.bluejungle.destiny.tools.dbinit.hibernate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.DatabaseM;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.TableM;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public abstract class MappingComparator {
	protected final DatabaseM database1;
	protected final DatabaseM database2;
	protected final DialectExtended d;
	protected final List<String> scripts;
	protected int config;
	
	protected MappingComparator(DatabaseM database1, DatabaseM database2, DialectExtended dialect) {
		this.database1 = database1;
		this.database2 = database2;
		this.d = dialect;
		scripts = new LinkedList<String>();
	}
	
    public List<String> compare(int config) {
        this.config = config;
        scripts.clear();
        compareDb(database1, database2);
        return Collections.unmodifiableList(scripts);
    }

    protected void compareDb(DatabaseM database1, DatabaseM database2){
        List<TableM> tables1 = database1.getTables();
        for(TableM table1 : tables1){
            TableM table2 = database2.getTable(table1.getName());
            
            if (table2 != null) {
                compareTable(table1, table2);
            } else {
                //find a table in database1 but in databaase2
                findMisMatchTable(table1);
            }
        }
    }
    
    protected abstract void findMisMatchTable(TableM table1);

    protected void compareTable(TableM table1, TableM table2) {
        List<ColumnM> columns1 = table1.getColumns();
        for (ColumnM column1 : columns1) {
            ColumnM column2 = table2.getColumn(column1.getColumnName());
            
            if (column2 != null) {
                compareColumn(column1, column2);
            } else {
                findMisMatchColumn(column1);
            }
        }

        compareConstraints(table1, table2);
    }
    
    protected abstract void findMisMatchColumn(ColumnM column1);

    protected abstract void compareColumn(ColumnM column1, ColumnM column2);

    protected abstract void compareConstraints(TableM table1, TableM table2);
    
    protected boolean has(int flag) {
        return (config & flag) != 0;
    }
}
