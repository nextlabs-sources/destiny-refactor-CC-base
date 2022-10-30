package com.bluejungle.destiny.tools.dbinit.hibernate.mapping;

/**
 * @author Hor-kan Chan
 * @date Mar 7, 2007
 */
public class SequenceM extends FieldAbstract {
	public SequenceM(String name) {
		super(name, FieldType.SEQUENCE);
	}

	public String sqlCreate() {
		return "CREATE SEQUENCE " + name;
	}
	public String sqlDrop() {
		return "DROP SEQUENCE " + name;
	}
}
