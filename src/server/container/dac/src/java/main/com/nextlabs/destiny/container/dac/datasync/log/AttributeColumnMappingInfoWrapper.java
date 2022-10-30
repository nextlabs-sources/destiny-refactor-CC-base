/*
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.util.HashMap;
import java.util.Map;

import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDO;

/**
 * We initialize all the data in this class and pass this object around.
 * 
 * Attribute Column Mapping can get new entries only during policy activity log sync task. Since 
 * that task runs as a single thread, we need not synchronize access to the methods in this class
 * 
 * @author nnallagatla
 *
 */
public class AttributeColumnMappingInfoWrapper {

    /**
	 * @return the numberOfAdditionalAttributes
	 */
	public int getNumberOfAdditionalAttributes() {
		return numberOfAdditionalAttributes;
	}

	/**
	 * @param numberOfAdditionalAttributes the numberOfAdditionalAttributes to set
	 */
	public void setNumberOfAdditionalAttributes(int numberOfAdditionalAttributes) {
		this.numberOfAdditionalAttributes = numberOfAdditionalAttributes;
	}

	/**
	 * @return the attributePrefix
	 */
	public String getAttributePrefix() {
		return attributePrefix;
	}

	/**
	 * @param attributePrefix the attributePrefix to set
	 */
	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}

	/**
	 * @return the attrColumnNameMapping
	 */
	public Map<String, Map<String, AttributeColumnMappingDO>> getAttrColumnNameMapping() {
		return attrColumnNameMapping;
	}

	/**
	 * @param attrColumnNameMapping the attrColumnNameMapping to set
	 */
	public void setAttrColumnNameMapping(
			Map<String, Map<String, AttributeColumnMappingDO>> attrColumnNameMapping) {
		this.attrColumnNameMapping = attrColumnNameMapping;
	}

	/**
	 * @return the columnsInUse
	 */
	public boolean[] getColumnsInUse() {
		return columnsInUse;
	}

	/**
	 * @param columnsInUse the columnsInUse to set
	 */
	public void setColumnsInUse(boolean[] columnsInUse) {
		this.columnsInUse = columnsInUse;
	}

	
	public AttributeColumnMappingInfoWrapper(int numberOfAttrColumns, String attrPrefix)
	{
		numberOfAdditionalAttributes = numberOfAttrColumns;
		attributePrefix = attrPrefix;
		
		//ignore 0th index
		columnsInUse = new boolean[numberOfAdditionalAttributes + 1];
	}
	
	private int numberOfAdditionalAttributes = 20;
    
    private String attributePrefix = "attr";
    
    private Map<String, Map<String, AttributeColumnMappingDO>> attrColumnNameMapping = new HashMap<String, Map<String, AttributeColumnMappingDO>>();
    
    private boolean[] columnsInUse;
}
