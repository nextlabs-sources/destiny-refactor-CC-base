package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class ForeignKeyM extends ConstraintM{
	private List<TwoColumn> columnsRefs =new ArrayList<TwoColumn>();
	
	private class TwoColumn {
		ColumnM local;
		ColumnM referenced;

		public TwoColumn(ColumnM column, ColumnM references) {
			this.local = column;
			this.referenced = references;
		}

		public boolean equals(TwoColumn twoColumn) {
			if (this == twoColumn)
				return true;
			if (twoColumn == null)
				return false;

			if (local == null) {
				if (twoColumn.local != null)
					return false;
			} else if (!local.getColumnName().equals(twoColumn.local.getColumnName()))
				return false;
			if (referenced == null) {
				if (twoColumn.referenced != null)
					return false;
			} else if (!referenced.getColumnName().equals(twoColumn.referenced.getColumnName()))
				return false;
			return true;
		}

	}
	
	public ForeignKeyM(TableM parent, String name) {
		super(parent, name, FieldType.FOREIGN_KEY);
	}
	

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(TAB + "fk - " + getName() + " - ");
		for (TwoColumn s : columnsRefs) {
			str.append(s.local.getColumnName() 
					+ "-" + s.referenced.getTable().getName() 
					+ "."+ s.referenced.getColumnName() + " ,");
		}
		str.append("\n");
		return str.toString();
	}
	
	@Override
	@SuppressWarnings("unused")
	void addColumn(	ColumnM columnM ){
		throw new RuntimeException("YOu need to add two columns");
	}
	
	void addColumn(ColumnM column, ColumnM reference ){
//		for(TwoColumn tc: columnsRefs){
//			if(tc.local.equals(local)){
//				
//			}
//		}
		columnsRefs.add(new TwoColumn(column, reference));
	}
//	
//	public void setReference(ColumnM column, ColumnM reference ){
//		for(TwoColumn tc: columnsRefs){
//			if(tc.local.equals(column)){
//				tc.referenced = reference;
//				return; 
//			}
//		}
//		throw new IndexOutOfBoundsException();
//	}
//	
//	public void addColumn(ColumnM columnMFrom, ColumnM columnMTo){
//		columnsRefs.add(new TwoColumn(columnMFrom, columnMTo));
//	}
	
	@Override
	//either it is in the referenceFrom or referenceTo
	public boolean contains(ColumnM columnM) {
		for (TwoColumn tc : columnsRefs) {
			if (tc.local.equals(columnM)) {
				return true;
			} else if (tc.referenced.equals(columnM)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isReferenceFrom(ColumnM columnM) {
		for (TwoColumn tc : columnsRefs) {
			if (tc.local.equals(columnM)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isReferenceTo(ColumnM columnM) {
		for (TwoColumn tc : columnsRefs) {
			if (tc.referenced.equals(columnM)) {
				return true;
			}
		}
		return false;
	}

	public boolean equalColumns(ForeignKeyM obj) {
		if (obj == null) {
			return false;
		}

		if (columnsRefs.size() != obj.columnsRefs.size()) {
			return false;
		}
		for (TwoColumn tc : columnsRefs) {
			boolean isFound = false;
			for (TwoColumn otherTc : obj.columnsRefs) {
				if (tc.equals(otherTc)) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public List<String> sqlDropConstraint(DialectExtended d) {
		List<String> strs = new ArrayList<String>(1);
		if (!isDropped()) {
			setDropped();
			strs.add("ALTER TABLE " + getTable().getQuotedName(d) + " " + "DROP CONSTRAINT" + " "
					+ getQuotedName(d));
		}
		return strs;
	}
	
	@Override
	public List<String>  sqlAddConstraint(DialectExtended d){
		List<String> strs = new ArrayList<String>(1);
		if (!isAdded()) {
			setAdded();

			StringBuilder list1 = new StringBuilder();
			StringBuilder list2 = new StringBuilder();

			String refercenedTableName = columnsRefs.get(0).referenced.getTable().getQuotedName(d);

			for (int i = 0; i < columnsRefs.size(); i++) {
				TwoColumn tc = columnsRefs.get(i);
				list1.append( tc.local.getQuotedName(d));
				list2.append( tc.referenced.getQuotedName(d));
				if (i < columnsRefs.size() - 1) {
					list1.append( ",");
					list2.append( ",");
				}
			}

			//ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses(address) MATCH FULL;
			
			strs.add("ALTER TABLE " +getTable().getQuotedName(d) 
					+ " ADD CONSTRAINT " + getName() 
					+ " FOREIGN KEY (" + list1 + ") REFERENCES " 
					+ refercenedTableName + "("	+ list2 + ")");
		}
		return strs;
	}
}
