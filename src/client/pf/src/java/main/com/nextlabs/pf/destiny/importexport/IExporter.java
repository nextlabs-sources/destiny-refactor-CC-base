package com.nextlabs.pf.destiny.importexport;

import java.io.File;
import java.util.Collection;

import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

public interface IExporter {
	/**
	 * This method retrieves takes in a Collection of policies to be exported,
	 * finds all dependencies, and parses all necessary components that are
	 * available.
	 * 
	 * @param selected
	 * @return requiredComponents, the Collection of components plus their
	 *         dependencies
	 * @throws ExportException 
	 */
	Collection<DomainObjectDescriptor> prepareForExport(Collection<DomainObjectDescriptor> selected)
			throws ExportException;
	
	/**
	 * This method is called after prepareForExport has
	 * prepared selected files for export.  It takes in a File,
	 * and exports the prepared data to that File. 
	 * @param xmlFile
	 * @throws ExportException 
	 */
	void executeExport(File xmlFile) throws ExportException;
}
