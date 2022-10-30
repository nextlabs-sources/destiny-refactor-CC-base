package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;
import java.util.Collection;

import net.sf.hibernate.dialect.Dialect;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public abstract class FieldAbstract {	
	protected static final String TAB = "<t>";
	
	protected String name;
	private FieldType fieldType;
	
	private boolean dropped = false;
	private boolean added 	= false;
	private boolean changed = false;
	
//	copy from hibernate 2.1.8
	protected boolean quoted=false;
	
	public String getQuotedName(Dialect d) {
		return getQuotedName(getName(), d);
	}
	
	protected String getQuotedName(String name , Dialect d) {
		return quoted ? d.openQuote() + name + d.closeQuote() :	name;
	}
	
	private void setName(String name) {
		if (name != null && name.charAt(0) == '`') {
			quoted = true;
			this.name = name.substring(1, name.length() - 1);
		} else {
			this.name = name;
		}
	}
	
	protected FieldAbstract(String name, FieldType fieldType) {
		this(fieldType);
		setName(name);
	}

	protected FieldAbstract(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getName() {
		return name;
	}

	public final FieldType getType() {
		return fieldType;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	//equal if the name and type are same
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FieldAbstract other = (FieldAbstract) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (fieldType != other.fieldType)
			return false;
		return true;
	}

	protected FieldAbstract getObjByName(Collection items, String name) {
		for (Object item : items) {
			FieldAbstract fa = (FieldAbstract) item;
			if (fa.getName().equals(name)) {
				return fa;
			}
		}
		return null;
	}

	public boolean isAdded() {
		return added;
	}

	protected void setAdded() {
		this.added = true;
	}

	public boolean isChanged() {
		return changed;
	}

	protected void setChanged() {
		this.changed = true;
	}

	public boolean isDropped() {
		return dropped;
	}

	protected void setDropped() {
		this.dropped = true;
	}
}
