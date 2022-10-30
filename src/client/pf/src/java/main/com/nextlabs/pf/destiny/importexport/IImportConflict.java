package com.nextlabs.pf.destiny.importexport;

/**
 * @author clee
 *
 */
public interface IImportConflict {
	//getter
	String getName();
	
	//getter
	String getType();
	
	/**
	 * This method provides the import file's version of a conflicted
	 * entity
	 * @return the version of the entity from the export file
	 */
	String versionFromFile();

	/**
	 * This method provides the server's existing version of a conflicted
	 * entity
	 * @return the version of the entity from the database
	 */
	String versionFromServer();

	/**
	 * This method tells whether the conflict has been resolved or not.
	 * @return whether the conflict has been resolved or not.
	 */
	boolean isResolved();

	/**
	 * This method sets the user's resolution to that conflict.
	 */
	void setResolution(ConflictResolution conflictResolution) throws ImportException;

	/**
	 * This method provides the user's resolution for this conflict.
	 * @return the user's resolution for this conflict.
	 */
	ConflictResolution getResolution();

}
