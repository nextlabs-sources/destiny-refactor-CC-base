/**
 * 
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * @author nnallagatla
 * 
 */
public class AttributeColumnMappingDO {
	private Long id;
	private String attributeName;
	private String columnName;
	private String dataType;
	private String attributeType;
	private boolean dynamic;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @param attributeName
	 *            the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the attributeType
	 */
	public String getAttributeType() {
		return attributeType;
	}

	/**
	 * @param attributeType
	 *            the attributeType to set
	 */
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	/**
	 * <p>
	 * Getter method for dynamic
	 * </p>
	 * 
	 * @return the dynamic
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * <p>
	 * Setter method for dynamic
	 * </p>
	 * 
	 * @param dynamic
	 *            the dynamic to set
	 */
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

}
