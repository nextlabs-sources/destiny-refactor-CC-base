/*
* Created on Sep 25, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filereader/EnrollableSubdomain.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.tools.enrollment.filereader;

import java.io.File;

/**
 * @author dwashburn
 *
 */
public class EnrollableSubdomain {
	private String name;
	private String domainType;
	private String connectionFile;
	private String definitionFile;
	private String filterFile;
	
	protected EnrollableSubdomain(String name, String domainType, String connectionFile, String definitionFile, String filterFile)
	{
		this.name = name;
		this.domainType = domainType;
		this.connectionFile = connectionFile;
		this.definitionFile = definitionFile;
		this.filterFile = filterFile;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the domainType
	 */
	public String getDomainType() {
		return domainType;
	}
	/**
	 * @param domainType the domainType to set
	 */
	public void setDomainType(String domainType) {
		this.domainType = domainType;
	}
	/**
	 * @return the connectionFile
	 */
	public File getConnectionFile() {
		if (connectionFile == null)
			return null;
		return new File(connectionFile);
	}
	/**
	 * @param connectionFile the connectionFile to set
	 */
	public void setConnectionFile(String connectionFile) {
		this.connectionFile = connectionFile;
	}
	/**
	 * @return the definitionFile
	 */
	public File getDefinitionFile() {
		if (definitionFile == null)
			return null;
		return new File(definitionFile);
	}
	/**
	 * @param definitionFile the definitionFile to set
	 */
	public void setDefinitionFile(String definitionFile) {
		this.definitionFile = definitionFile;
	}
	/**
	 * @return the filterFile
	 */
	public File getFilterFile() {
		if (filterFile == null)
			return null;
		return new File(filterFile);
	}
	/**
	 * @param filterFile the filterFile to set
	 */
	public void setFilterFile(String filterFile) {
		this.filterFile = filterFile;
	}

}
