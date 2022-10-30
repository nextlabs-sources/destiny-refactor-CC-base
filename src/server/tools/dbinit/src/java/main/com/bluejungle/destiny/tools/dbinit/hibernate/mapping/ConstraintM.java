package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class ConstraintM extends FieldAbstract{
	private List<ColumnM> columnRefs;
	private final TableM parent;
	
	public ConstraintM(TableM parent,String name, FieldType fieldType) {
		super(name, fieldType);
		this.parent = parent;
		columnRefs = new ArrayList<ColumnM>();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(TAB);
		switch(getType()){
		case PRIMARY_KEY:
			sb.append("pk - ");
			break;
		case FOREIGN_KEY:
			sb.append("fk - ");
			break;
		case UNIQUE:
			sb.append("unique - ");
			break;
		case INDEX:
			sb.append("index - ");
			break;
		default:
			// do nothing
		}
		
	    sb.append(getName()).append(" - ");
		for(ColumnM s : columnRefs){
		    sb.append(s.getColumnName()).append(",");
		}
		
		sb.append("\n");
		return sb.toString();
	}
	
	void addColumn(ColumnM columnM){
		columnRefs.add(columnM);
	}
	
	public List<ColumnM> getColumns(){
	    return columnRefs;
	}
	
	public boolean contains(ColumnM columnM){
		return columnRefs.contains(columnM);
	}

	public TableM getTable() {
		return parent;
	}

	public boolean equals(ConstraintM obj) {
		if (this == obj)
			return true;
		if (obj == null) {
			return false;
		}
		if (getType() != obj.getType()) {
			return false;
		}

		if (!getName().equals(obj.getName())) {
			return false;
		}

		return equalColumns(obj);
	}

	public boolean equalColumns(ConstraintM obj) {
		if (obj == null) {
			return false;
		}

		if (columnRefs.size() != obj.columnRefs.size()) {
			return false;
		}
		for (ColumnM columnRef : columnRefs) {
			boolean isFound = false;
			for (ColumnM otherColumnRef : obj.columnRefs) {
				String thisColumnName = columnRef.getColumnName();
				String otherColumnName = otherColumnRef.getColumnName();
				if (thisColumnName.equals(otherColumnName)) {
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
	
	public List<String> sqlDropConstraint(DialectExtended d) {
		List<String> strs = new ArrayList<String>(1);
		if (!isDropped()) {
			setDropped();
			if (getType() == FieldType.INDEX) {
				strs.add(d.sqlDropIndex(getName(), getTable().getName()));
			} else {
				//	ALTER TABLE cached_user DROP CONSTRAINT cached_user_pkey
				strs.add("ALTER TABLE " + getTable().getQuotedName(d) + " " 
						+ "DROP CONSTRAINT"	+ " \"" + getName() + "\"");
			}
		}
		return strs;
	}
	
	public List<String> sqlAddConstraint(DialectExtended d) {
		List<String> strs = new ArrayList<String>(1);
		if (!isAdded()) {
			setAdded();
			StringBuilder sb = new StringBuilder();

			String columns = "";
			for (int i = 0; i < columnRefs.size(); i++) {
				columns += columnRefs.get(i).getQuotedName(d);

				if (i < columnRefs.size() - 1) {
					columns += ",";
				}
			}

			switch (getType()) {
			case PRIMARY_KEY:
				//ALTER TABLE distributors ADD PRIMARY KEY (dist_id);
			    sb.append("ALTER TABLE " + getTable().getQuotedName(d) + " " + "ADD PRIMARY KEY");
				break;
			case FOREIGN_KEY:
				//ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses(address) MATCH FULL;
				throw new RuntimeException("ask ForeignKey Object  to add the constraint");
			case UNIQUE:
				//ALTER TABLE distributors ADD CONSTRAINT dist_id_zipcode_key UNIQUE (dist_id, zipcode);
				strs.add(d.sqlAddUnique(getTable().getQuotedName(d), getName(),
						columns));
				return strs;
//				break;
			case INDEX:
				//oracle
				//CREATE INDEX "ACTIVITY"."NEWINDEXNAME" ON "ACTIVITY"."CACHED_APPLICATION" ("ID")

			    sb.append("CREATE INDEX " + getQuotedName(d) + " ON " + getTable().getQuotedName(d));
				break;
			}
			sb.append(" (").append(columns).append(")");
			strs.add(sb.toString());
		}
		return strs;
	}
}
