package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.ResultSetKey;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.utils.StringUtils;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class ColumnM extends FieldAbstract{
	//this is default hibernate default column size
	private static final int DEFAULT_COLUMN_SIZE = 255;
	
	private static final String COLUMN_TEMP_SUFFIX = "_t";
	
	private final String sqlTypeName;
	private final short dataType;
	
	private final int columnSize;
	private final int decimalDigits;
	private final boolean isNullable;
	
	private List<ConstraintM> constraintRefs = new ArrayList<ConstraintM>();
	private List<ForeignKeyM> foreignKeyReferenced = new ArrayList<ForeignKeyM>();
	
	private boolean isPrimary = false;
	private boolean isIndex = false;
	private boolean isUnique = false;
	private boolean isForeignKeyFrom = false;
	private boolean isForeignKeyTo = false;
	
	private String columnName;
	private final TableM parent;
	
	public String getColumnName() {
		return columnName;
	}
	
	public TableM getTable() {
		return parent;
	}

	//TODO deciamlDigits
	public ColumnM(TableM parent,
			String colName, 
			String sqlTypeName, 
			short dataType,  
			int columnSize, 
			int decimalDigits, 
			boolean isNullable) {
		super(null, FieldType.COLUMN);
		this.parent = parent;
		setName( colName );
		this.sqlTypeName = sqlTypeName;
		this.dataType = dataType;
		this.columnSize = columnSize;
		this.decimalDigits = decimalDigits;
		this.isNullable = isNullable;
	}
	
	private void setName(String name) {
		if (name.charAt(0) == '`') {
			System.out.println("quoted " + name);
			quoted = true;
			this.columnName = name.substring(1, name.length() - 1);
		} else {
			this.columnName = name;
		}
	}
	
	public ColumnM(TableM parent, ResultSet rs) throws SQLException {
		super(FieldType.COLUMN);
		this.parent = parent;
		setName(rs.getString(ResultSetKey.COLUMN_NAME));
		name = null;
		sqlTypeName = rs.getString(ResultSetKey.TYPE_NAME);

		dataType = Short.parseShort(rs.getString(ResultSetKey.DATA_TYPE));

		columnSize = rs.getInt(ResultSetKey.COLUMN_SIZE);
		decimalDigits = rs.getInt(ResultSetKey.DECIMAL_DIGITS);

		Boolean isNullable = StringUtils.stringToBoolean(rs.getString(ResultSetKey.IS_NULLABLE));
		if (isNullable != null) {
			this.isNullable = isNullable;
		} else {
			throw new RuntimeException("unknown value on IS_NULLABLE, "
					+ rs.getString(ResultSetKey.IS_NULLABLE));
		}
	}
	
	protected ColumnM getObjByColName(Collection<ColumnM> items, String name) {
		for (ColumnM item : items) {
			String caName = item.getColumnName();
			if (caName.equals(name)) {
				return item;
			}
		}
		return null;
	}
	
	public int getColumnSize() {
		return columnSize;
	}

	public int getDecimalDigits() {
		return decimalDigits;
	}

	public boolean isNullable() {
		return isNullable;
	}
	
	public short getDataType() {
		return dataType;
	}

	public String getSqlTypeName() {
		return sqlTypeName;
	}
	
	void addConstraint(ConstraintM constraint) {
		constraintRefs.add(constraint);
		switch (constraint.getType()) {
		case PRIMARY_KEY:
			setPrimary();
			break;
		case FOREIGN_KEY:
			isForeignKeyFrom = true;
			break;
		case UNIQUE:
			setUnique();
			break;
		case INDEX:
			setIndex();
			break;
		}
	}
	
	void addForeignKeyReferenced(ForeignKeyM foreignKeyM) {
		foreignKeyReferenced.add(foreignKeyM);
		isForeignKeyTo = true;
	}
	
	public boolean isIndex() {
		return isIndex;
	}

	public void setIndex() {
		isIndex = true;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary() {
		isPrimary = true;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique() {
		isUnique = true;
	}

	@Override
	public String getName() {
		return getColumnName();
	}

	public String getDialectType(Dialect dialect) {
		return getDialectType(dialect, getColumnSize());
	}
	
	public String getDialectType(Dialect dialect, int columnSize) {
		try {
			//FIXME should fix this in the dialect
			if (getDataType() == Types.CHAR && columnSize != DEFAULT_COLUMN_SIZE) {
				return "char(" + columnSize + ")";
			} else {
				return dialect.getTypeName(getDataType(), columnSize);
			}
		} catch (Exception e) {
			/*
			 * happen in oracle
			 * input is 
			 * for example,
			 * policylogreportdetailresults.policydecision
			 * Hibernate create type = bytea, -3
			 * PostgreSQL 	data_type=-2 type_name=bytea length=-1
			 * Hibernate create type = raw(255), -3
			 * Oracle 		data_type=-3 type_name=raw length=255
			 */
			 return null;
		}
	}
	
	@Override
	public String toString() {
		return TAB + columnName + " ("+ sqlTypeName+ "," 
			+ getColumnSize()	
			//","+getDialectType(debugDialect)+
			+ "," + getDataType() + ")"		
			+ (isNullable ? "" : " ,NOT NULL")
			+ (isPrimary() ? " ,PRIMARY" : "")
			+ (isIndex() ? " ,INDEX" : "")
			+ (isUnique() ? " ,UNIQUE" : "")
			+ (isForeignKeyFrom ? " ,FK" : "")
			+ (isForeignKeyTo ? " ,FKed" : "")
			+ "\n";
	}	

	public List<ConstraintM> getConstraints(FieldType fieldType) {
		List<ConstraintM> list = new ArrayList<ConstraintM>();
		for (ConstraintM constraintRef : constraintRefs) {
			if (constraintRef.getType() == fieldType) {
				list.add(constraintRef);
			}
		}
		return list;
	}
	
	public ConstraintM getConstraint(String name, FieldType fieldType) {
		for (ConstraintM constraintRef : constraintRefs) {
			if (constraintRef.getName().equals(name) && constraintRef.getType() == fieldType) {
				return constraintRef;
			}
		}
		return null;
	}
	
	private String checkConstraint;
	/**
	 * @return Returns the checkConstraint.
	 */
	public String getCheckConstraint() {
		return checkConstraint;
	}

	/**
	 * @param checkConstraint The checkConstraint to set.
	 */
	public void setCheckConstraint(String checkConstraint) {
		this.checkConstraint = checkConstraint;
	}
	
	public boolean hasCheckConstraint() {
		return checkConstraint!=null;
	}
	
	//in postrgre, this doesn't work for char(n), even the length is different, it still return char(1)
	//for example, datatype is -1, length is 2
	public String sqlAlterColumnNewLength(int newLength, DialectExtended d) {
		setChanged();
		return d.sqlAlterColumnType(getTable().getQuotedName(d), getQuotedName(d),
				getDialectType(d, newLength));
	}

	public String sqlAlterColumnChangeDataType(String newDataType, DialectExtended d) {
		setChanged();
		return d.sqlAlterColumnType(getTable().getQuotedName(d), getQuotedName(d),
				newDataType);
	}
	
	
	
	private String tempName = null;

	public String getTempName() {
		return tempName;
	}

	public boolean isTemp() {
		return tempName != null;
	}
	
	String getQuotedTempName(Dialect d) {
		return quoted ? d.openQuote() + tempName + d.closeQuote() : tempName;
	}
	
	public List<String> sqlCloneTempColumn(DialectExtended dialect) {
		ArrayList<String> script = new ArrayList<String>();

		String columnName = getColumnName();

		//rename to dbColumnName + _temp
		tempName = columnName + DatabaseHelper.matchToDbStoreCase(COLUMN_TEMP_SUFFIX);

		Random r = new Random();
		//check if used, if so + number
		while (getTable().getColumn(tempName) != null) {
			tempName = columnName + DatabaseHelper.matchToDbStoreCase(COLUMN_TEMP_SUFFIX) + Math.abs(r.nextInt()) % 20;
		}
		
		//save the name and tempname to the mapping
		getTable().addTempColumn(getColumnName(), tempName);

		script.addAll(sqlRenameColumn(tempName, dialect));

		script.addAll(sqlAddColumn(dialect));

		return script;
	}
	
	public List<String> sqlRenameColumn(String newName, DialectExtended d) {
		//oracle
		//ALTER TABLE "ACTIVITY"."CACHED_APPLICATION" RENAME COLUMN "NEW_COLUMN" TO "NEW_COLUMN2"
		ArrayList<String> script = new ArrayList<String>();
		setChanged();
		//if column is related to foreign Key

		List<ConstraintM> foreignKeys = getConstraints(FieldType.FOREIGN_KEY);
		for (ConstraintM foreignKey : foreignKeys) {
			script.addAll(foreignKey.sqlDropConstraint(d));
		}
		script.add(d.sqlRenameColumn(
		        getTable().getName()   // tableName
		      , getName()              // columnName
		      , newName     // newColumnName
		));  
		        
		return script;
	}
	
	public List<String> sqlAddColumn(DialectExtended d) {
		ArrayList<String> script = new ArrayList<String>();
		if (isAdded()) {
			return script;
		}
		setAdded();
		script.add("ALTER TABLE " + getTable().getQuotedName(d) + " " + d.getAddColumnString()
				+ " " + getQuotedName(d) + " " + getDialectType(d));

		if (!isNullable()) {
			//update cached_user set id12312='1' where id12312 is null
			script.addAll(sqlAlterColumnUpdateDefault(d));
			script.addAll(sqlAlterColumnSetNullable(false, d));
		}
		
		
//		String defaultValue = column.getDefaultValue();
//		if ( defaultValue != null ) {
//			alter.append( " default " ).append( defaultValue );



//		//dialect.supportsNotNullUnique()
//		boolean useUniqueConstraint = isUnique() &&
//				dialect.supportsUnique() &&
//				( !isNullable() );
//		if ( useUniqueConstraint ) {
//			stat += " unique" ;
//		}
//
//		//dialect.supportsColumnCheck()
//		if ( hasCheckConstraint()  ) {
//			stat += " check(" + getCheckConstraint() + ")" ;
//		}

//		String columnComment = column.getComment();
//		if ( columnComment != null ) {
//			alter.append( dialect.getColumnComment( columnComment ) );
//		}
		
		setAdded();
		return script;
	}
	
	public List<String> sqlDropColumn(DialectExtended d) {
		ArrayList<String> script = new ArrayList<String>();
		if (isDropped()) {
			return script;
		}
		setDropped();
		
		//drop all the constraints before drop the table
		List<ConstraintM> constraints = getConstraints(FieldType.PRIMARY_KEY);
		constraints.addAll(getConstraints(FieldType.INDEX));
		constraints.addAll(getConstraints(FieldType.UNIQUE));
		constraints.addAll(getConstraints(FieldType.FOREIGN_KEY));
		for (ConstraintM constraint : constraints) {
			script.addAll(constraint.sqlDropConstraint(d));
		}
		
		script.add("ALTER TABLE " + getTable().getQuotedName(d) + " "
				+ d.getDropColumnString() + " " + d.openQuote() + getColumnName()
				+ d.closeQuote());
		return script;
	}
	
	public List<String> sqlAlterColumnSetNullable( boolean nullable, DialectExtended d) {
		setChanged();
		List<String> strs = new ArrayList<String>(1);
		strs.add("ALTER TABLE " + getTable().getQuotedName(d) + " "
				+ d.getAlterColumnString() + " " + getQuotedName(d)
				+ d.getSetNullableString(getDialectType(d, getColumnSize()), nullable));
		return strs;
	}
	
	public List<String> sqlAlterColumnUpdateDefault(DialectExtended d) {
		setChanged();
		List<String> strs = new ArrayList<String>(1);
		//update [table] set [field]=’value you want’ where [field] is nul
		strs.add("UPDATE " + getTable().getQuotedName(d) 
				+ " SET " + getQuotedName(d) + "="+ d.getDefaultValue(getDataType()) 
				+ " WHERE " + getQuotedName(d) +  " IS NULL");
		return strs;
	}

	public boolean isForeignKeyFrom() {
		return isForeignKeyFrom;
	}

	public boolean isForeignKeyTo() {
		return isForeignKeyTo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ColumnM other = (ColumnM) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}

}
