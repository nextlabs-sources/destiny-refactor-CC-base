package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.hibernate.MappingException;
import net.sf.hibernate.dialect.Dialect;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class TableM extends FieldAbstract {
    private Map<String, String> columnTempNameToOrginalNameMapping = new TreeMap<String, String>();
    private Map<String, String> columnOrginalNameToTempNameMapping = new TreeMap<String, String>();
    
    private List<ColumnM> columns;
    private List<ConstraintM> constraints;
    private boolean isPrimaryKeyAssigned;
    
    private final DatabaseM parent;
    
    public TableM(DatabaseM parent, String name) {
        super(name, FieldType.TABLE);
        this.parent = parent;
        columns = new ArrayList<ColumnM>();
        constraints = new ArrayList<ConstraintM>();
        isPrimaryKeyAssigned = false;
    }
    
    public void addColumn(ColumnM column) {
        columns.add(column);
    }
    
    public ColumnM getColumn(String colName) {
        for (ColumnM c : columns) {
            if (c.getColumnName().equals(colName)) {
                return c;
            }
        }
        return null;
    }

    public Set<String> getColumnNames() {
        Set<String> list = new TreeSet<String>();
        for (ColumnM c : columns) {
            list.add(c.getColumnName());
        }
        return list;
    }
    
    public List<ColumnM> getColumns(){
        return columns;
    }
    
    public void addConstraint(String name, String colName, FieldType fieldType){
//      constraints.add(constraint);
        ConstraintM constraint = getConstraint(name, fieldType);
        if (constraint == null) {
            //new constraint
            constraint = new ConstraintM(this, name, fieldType);
            constraints.add(constraint);
        }
        if (colName.startsWith("\"") && colName.endsWith("\"")) {
            colName = colName.substring(1, colName.length() - 1);
        }

        ColumnM columnM = getColumn(colName);
        if (columnM == null) {
            throw new RuntimeException("Can't add a constraint " + name
                    + " referenced to not existed column " + colName);
        }
        constraint.addColumn(columnM);
        if (columnM != null) {
            columnM.addConstraint(constraint);
        }
    }
    
    //either referenceFrom or referenceTo must be null
    //the reference can't be pont on the same table
    public void addForeignKey(String name, String columnName, ColumnM referencedColumn) {
        //check if name already in table constraints
        boolean isFound = false;
        ForeignKeyM foreignKeyM = null;
        for (ConstraintM c : constraints) {
            if(c.getType() == FieldType.FOREIGN_KEY && c.getName().equals(name)){
//              if yes, update the list
                isFound = true;
                foreignKeyM = (ForeignKeyM) c;
                break;
            }
        }
        if (!isFound) {
            //else add to the list
            foreignKeyM = new ForeignKeyM(this, name);
            constraints.add(foreignKeyM);
        }

        ColumnM columnM = getColumn(columnName);
        if (columnM == null) {
            String debug = "Column not found " + columnName + " in table " + getName()
                    + "\navaliable column " + columns.size() + "=";
            for (ColumnM columnM2 : columns) {
                debug += columnM2.getColumnName() + ",";
            }
            throw new RuntimeException(debug);
        }
        foreignKeyM.addColumn(columnM, referencedColumn);

        //update local columns
        columnM.addConstraint(foreignKeyM);

        //update referenced columns
        referencedColumn.addForeignKeyReferenced(foreignKeyM);
    }
    
    public List<ConstraintM> getConstraints(FieldType fieldType) {
        List<ConstraintM> list = new ArrayList<ConstraintM>();
        for (ConstraintM item : constraints) {
            if (item.getType() == fieldType) {
                list.add(item);
            }
        }
        return list;
    }
    
    public ConstraintM getConstraint(String name, FieldType fieldType) {
        for (ConstraintM item : constraints) {
            if (item.getName().equals(name) && item.getType() == fieldType) {
                return item;
            }
        }
        return null;
    }
    
    List<ConstraintM> getConstraints(ColumnM columnM, FieldType fieldType) {
        List<ConstraintM> list = new ArrayList<ConstraintM>();
        for (ConstraintM item : constraints) {
            if (item.getType() == fieldType && item.contains(columnM)) {
                list.add(item);
            }
        }
        return list;
    }
    
    @Override
    public String toString() {
        String output = TAB + getName() + "\n";

        Set<String> columnNames = getColumnNames();
        for (String columnName : columnNames) {
            output += getColumn(columnName).toString().replaceAll(TAB, "\t" + TAB);
        }

        for (ConstraintM constraint : constraints) {
            output += constraint.toString().replaceAll(TAB, "\t" + TAB);
        }

        return output;
    }

    public DatabaseM getParent() {
        return parent;
    }

    void addTempColumn(String orginalName, String tempName) {
        columnTempNameToOrginalNameMapping.put(tempName, orginalName);
        columnOrginalNameToTempNameMapping.put(orginalName, tempName);
    }
    
    public void setPrimaryKeyAssigned(boolean isAssigned){
        isPrimaryKeyAssigned = isAssigned;
    }

    //Deprecated, the temp column will be dropped once the database mapping is updated. There is no need to drop them manually.
    @Deprecated
    public List<String> sqlDropAllTempColumn(Dialect d) {
        Set<String> columnNames = columnTempNameToOrginalNameMapping.keySet();

        ArrayList<String> script = new ArrayList<String>();

        for (String columnName : columnNames) {
            script.add("ALTER TABLE " + getQuotedName(d) + " " + "DROP " + d.openQuote()
                    + columnName + d.closeQuote());
        }

        return script;
    }
    
    public List<String> sqlCreateString(Dialect d) {
        List<String> script = new ArrayList<String>();
        if (!isAdded()) {
            setAdded();

            StringBuilder str = new StringBuilder("create table ").append(getQuotedName(d)).append(" (");

            Iterator<ColumnM> iter = columns.iterator();
            while (iter.hasNext()) {
                ColumnM col = iter.next();

                str.append(col.getQuotedName(d)).append(' ');

                if (isPrimaryKeyAssigned && col.isPrimary()) {
                    // to support dialects that have their own identity data type
                    if (d.hasDataTypeInIdentityColumn()) {
                        str.append(col.getDialectType(d));
                    }
                    try {
                        str.append(' ').append(d.getIdentityColumnString());
                    } catch (MappingException e) {
                        //ignore
                    }
                } else {
                    str.append(col.getDialectType(d));
                    str.append(col.isNullable() ? d.getNullColumnString() : " not null");
                }

                if (col.isUnique()) {
                    if (d.supportsUnique()) {
                        str.append(" unique");
                    }
                }
                if (col.hasCheckConstraint() && d.supportsCheck()) {
                    str.append(" check(").append(col.getCheckConstraint()).append(")");
                }
                
                col.setAdded();
                if (iter.hasNext()) {
                    str.append(", ");
                }

            }
            
            List<ConstraintM> primaryKeys = getConstraints(FieldType.PRIMARY_KEY);
            
            assert primaryKeys.size() <= 1;
            
            if (primaryKeys.size() != 0) {
                str.append(", primary key (");
                ConstraintM primaryKey = primaryKeys.iterator().next();
                List<ColumnM> columns = primaryKey.getColumns();
                
                Iterator<ColumnM> colmnsIterator = columns.iterator();
                while (colmnsIterator.hasNext()) {
                    ColumnM column = colmnsIterator.next();
                    str.append(column.getQuotedName(d));
                    if (colmnsIterator.hasNext()) {
                        str.append(", ");
                    }
                }
                str.append(")");
            }
            str.append(")");
            script.add(str.toString());
        }
        return script;
    }

    public String sqlDropString(Dialect dialect) {
        StringBuffer buf = new StringBuffer("drop table ");
        if (dialect.supportsIfExistsBeforeTableName()){
            buf.append("if exists ");
        }
        buf.append(getQuotedName(dialect)).append(dialect.getCascadeConstraintsString());
        if (dialect.supportsIfExistsAfterTableName()){
            buf.append(" if exists");
        }
        return buf.toString();
    }
}
