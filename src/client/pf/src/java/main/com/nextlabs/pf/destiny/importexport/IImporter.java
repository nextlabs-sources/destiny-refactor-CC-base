package com.nextlabs.pf.destiny.importexport;

import java.util.Collection;

import com.nextlabs.shared.tools.ICommandLine;

public interface IImporter {

	/**
	 * This method processes the import file and returns the
	 * a collection of entities for the user to selectively import.
	 * @param importSource, the source of the import data
	 * @param failRecovery, the fail-recovery option selected at import
	 * @return entitesFromFile, containing the entities from the 
	 *  import file.
	 * @throws ImportException
	 */
	Collection<ExportEntity> getEntities() throws ImportException;

	/**
	 * This method takes in a Collection of entities to be imported,
	 * and processes them for conflicts.
	 * @param selected, the entities from the file selected for import.
	 * @return the IImportState for conflict resolution.
	 * @throws ImportException 
	 */
	IImportState doImport(Collection<ExportEntity> selected) throws ImportException;
	
	/**
	 * This method commits the import after all conflicts are handled.
	 * It will carry out the user's instructions for conflicts,
	 * parse the PQL with the rename mapping, and call saveEntities
	 * on the entities that will be committed.
	 * @throws ImportException 
	 */
	void commitImport() throws ImportException;

}
