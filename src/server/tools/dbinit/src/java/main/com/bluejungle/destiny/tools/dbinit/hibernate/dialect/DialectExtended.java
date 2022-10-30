/*
 * Created on Dec 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.dialect.Oracle9Dialect;
import net.sf.hibernate.dialect.PostgreSQLDialect;
import net.sf.hibernate.dialect.SQLFunction;
import net.sf.hibernate.dialect.SQLServerDialect;
import net.sf.hibernate.sql.CaseFragment;
import net.sf.hibernate.sql.JoinFragment;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.mapping.ColumnM;

/**
 * Upgrading database need extra information from the Dialect.
 * This class  
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/DialectExtended.java#1 $
 */

public abstract class DialectExtended extends Dialect{
	static final String ALTER_TABLE = "ALTER TABLE ";
	
	protected final Dialect d;
	
	public DialectExtended(Dialect dialect){
		d = dialect;
	}
	
	public Dialect getUnwrappedDialect(){
		return d;
	}
	
    public static final DialectExtended getDialectExtended(Dialect dialect) {
        if (dialect instanceof DialectExtended) {
            return (DialectExtended)dialect;
        } else if (dialect instanceof Oracle9Dialect) {
            return new Oracle9DialectX(dialect);
        } else if (dialect instanceof PostgreSQLDialect) {
            return new PostgreSQLDialectX(dialect);
        } else if (dialect instanceof SQLServerDialect) {
            return new SqlServerDialectX(dialect);
        } else {
            return new UnsupportedDialectX(dialect);
        }
    }
	
	 /**
     * compare two column if they have same length
     * @param dbColumnM
     * @param hibColumnM
     * @return true if they are/will be same length in database
     */
	public boolean doesTypeHaveLength(short type) {
		return (type == Types.ARRAY 
				|| type == Types.BLOB 
				|| type == Types.CLOB
				|| type == Types.CHAR 
				|| type == Types.LONGVARBINARY 
				|| type == Types.LONGVARCHAR
				|| type == Types.VARBINARY 
				|| type == Types.VARCHAR
				|| type == Types.NVARCHAR);
	}
	
	/**
	 * compare two column if they have same length
	 * @param dbColumnM
	 * @param hibColumnM
	 * @param d
	 * @return true if they are/will be same length in database
	 */
	public abstract boolean isSameLength(ColumnM dbColumnM, ColumnM hibColumnM);
	
	public abstract boolean isSameType(short c1DataType, short c2DataType);
	
	public abstract String getDefaultValue(short dataType);
	
	public abstract String getAlterColumnString();
    
	public abstract String getDropColumnString();
	
	public String getSubStringString(){
		return "substr";
	}
	
	public abstract String getLengthString();
	
	public boolean isTableBlackListed(String tableName){
		//if(tableName.equals(DatabaseHelper.matchToDbStoreCase(
		//  C3P0ConnectionPoolWrapper.DEFAULT_AUTOMATIC_TEST_TABLE))){
		if(tableName.equals(DatabaseHelper.matchToDbStoreCase("connection_test_table"))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
     * a sql to change a column type
     * @param tableName
     * @param colname
     * @param newType
     * @param d
     * @return
     */
	public abstract String sqlAlterColumnType(String tableName, String colname, String newType);
	
	public String sqlAddUnique(String tableName, String name, String columns){
		return ALTER_TABLE + tableName + " ADD"
			+ ((name != null) ? (" CONSTRAINT " + name) : "") 
			+ " UNIQUE (" + columns + ")";
	}
	
	public String sqlDropIndex(String indexName, String tableName){
		return "DROP INDEX " + openQuote() + indexName + closeQuote();
	}
	
	public abstract String getSetNullableString(String sameType, boolean setNull);
	
	public String getCountString(){
		return "count";
	}
	public abstract boolean isSequenceExist(Connection connection, String name);
      
	public boolean isPrimaryImplyIndex(){
		return true;
	}

	public boolean isPrimaryImplyUnique(){
		return true;
	}

	public boolean isUniqueImplyIndex(){
		return true;
	}
    
	static final int HIBERNATE_DEFAULT_LENGTH = 255;

	public int getColumnLength(int reportedLength, short dataType){
		return reportedLength;
	}
    
	public String getTableSchema(DatabaseMetaData meta) throws SQLException{
		return null;
	}
	
	public abstract String sqlRebuildIndex(String indexName, String tableName);
    
	public String dropView(String viewName){
	    return "DROP VIEW " + openQuote() + viewName + closeQuote();
	}
	
	public String sqlRenameColumn(String tableName, String columnName, String newColumnName) {
	    return 
	            "ALTER TABLE " 
	          + openQuote() + tableName + closeQuote() 
	          + " RENAME COLUMN "
              + openQuote() + columnName + closeQuote() 
              + " TO " 
              + openQuote() + newColumnName + closeQuote();
	}
	
	
	
	
	
	
	
	//Don't implement any Dialect methods 
	
	@Override
	public final String appendIdentitySelectToInsert(String insertSQL) {
		return d.appendIdentitySelectToInsert(insertSQL);
	}

	@Override
	public final boolean bindLimitParametersFirst() {
		return d.bindLimitParametersFirst();
	}

	@Override
	public final boolean bindLimitParametersInReverseOrder() {
		return d.bindLimitParametersInReverseOrder();
	}

	@Override
	public final char closeQuote() {
		return d.closeQuote();
	}

	@Override
	public final CaseFragment createCaseFragment() {
		return d.createCaseFragment();
	}

	@Override
	public final JoinFragment createOuterJoinFragment() {
		return d.createOuterJoinFragment();
	}

	@Override
	public final boolean dropConstraints() {
		return d.dropConstraints();
	}

	@Override
	public final String getAddColumnString() {
		return d.getAddColumnString();
	}

	@Override
    public final String getAddForeignKeyConstraintString(String constraintName,
            String[] foreignKey, String referencedTable, String[] primaryKey) {
        return d.getAddForeignKeyConstraintString(constraintName, foreignKey, referencedTable,
                primaryKey);
    }

	@Override
	public final String getAddPrimaryKeyConstraintString(String constraintName) {
		return d.getAddPrimaryKeyConstraintString(constraintName);
	}

	@Override
	public final String getCascadeConstraintsString() {
		return d.getCascadeConstraintsString();
	}

	@Override
	public final String getCreateSequenceString(String sequenceName) throws MappingException {
		return d.getCreateSequenceString(sequenceName);
	}

	@Override
	public final String getDropSequenceString(String sequenceName) throws MappingException {
		return d.getDropSequenceString(sequenceName);
	}

	@Override
	public final String getIdentityColumnString() throws MappingException {
		return d.getIdentityColumnString();
	}

	@Override
	public final String getIdentityInsertString() {
		return d.getIdentityInsertString();
	}

	@Override
	public final String getIdentitySelectString() throws MappingException {
		return d.getIdentitySelectString();
	}

	@Override
	public final String getLimitString(String querySelect, boolean hasOffset, int limit) {
		return d.getLimitString(querySelect, hasOffset, limit);
	}

	@Override
	public final String getLimitString(String querySelect, boolean hasOffset) {
		return d.getLimitString(querySelect, hasOffset);
	}

	@Override
	public final String getLowercaseFunction() {
		return d.getLowercaseFunction();
	}

	@Override
	public final String getNoColumnsInsertString() {
		return d.getNoColumnsInsertString();
	}

	@Override
	public final String getNullColumnString() {
		return d.getNullColumnString();
	}

	@Override
	public final String getQuerySequencesString() {
		return d.getQuerySequencesString();
	}

	@Override
	public final char getSchemaSeparator() {
		return d.getSchemaSeparator();
	}

	@Override
	public final String getSequenceNextValString(String sequenceName) throws MappingException {
		return d.getSequenceNextValString(sequenceName);
	}

	@Override
	public final String getTypeName(int code, int length) throws HibernateException {
		return d.getTypeName(code, length);
	}

	@Override
	public final String getTypeName(int code) throws HibernateException {
		return d.getTypeName(code);
	}

	@Override
	public final boolean hasAlterTable() {
		return d.hasAlterTable();
	}

	@Override
	public final boolean hasDataTypeInIdentityColumn() {
		return d.hasDataTypeInIdentityColumn();
	}

	@Override
	public final  char openQuote() {
		return d.openQuote();
	}

	@Override
	public final boolean qualifyIndexName() {
		return d.qualifyIndexName();
	}

	@Override
	protected final void registerColumnType(int code, int capacity, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void registerColumnType(int code, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void registerFunction(String name, SQLFunction function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean supportsCheck() {
		return d.supportsCheck();
	}

	@Override
	public final boolean supportsForUpdate() {
		return d.supportsForUpdate();
	}

	@Override
	public final boolean supportsForUpdateNowait() {
		return d.supportsForUpdateNowait();
	}

	@Override
	public final boolean supportsForUpdateOf() {
		return d.supportsForUpdateOf();
	}

	@Override
	public final boolean supportsIdentityColumns() {
		return d.supportsIdentityColumns();
	}

	@Override
	public final boolean supportsIfExistsAfterTableName() {
		return d.supportsIfExistsAfterTableName();
	}

	@Override
	public final boolean supportsIfExistsBeforeTableName() {
		return d.supportsIfExistsBeforeTableName();
	}

	@Override
	public final boolean supportsLimit() {
		return d.supportsLimit();
	}

	@Override
	public final boolean supportsLimitOffset() {
		return d.supportsLimitOffset();
	}

	@Override
	public final boolean supportsSequences() {
		return d.supportsSequences();
	}

	@Override
	public final boolean supportsUnique() {
		return d.supportsUnique();
	}

	@Override
	public final boolean supportsVariableLimit() {
		return d.supportsVariableLimit();
	}

	@Override
	public final String toString() {
		//d is null in the construction time
		// the super constructer calls toString() will cause NullPointer
		return d != null ? d.toString() : super.toString();
	}

	@Override
	public final boolean useMaxForLimit() {
		return d.useMaxForLimit();
	}

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public final boolean equals(Object obj) {
		return d.equals(obj);
	}

	@Override
	protected final void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	public final int hashCode() {
		return d.hashCode();
	}
	
	
	
	
	
}
