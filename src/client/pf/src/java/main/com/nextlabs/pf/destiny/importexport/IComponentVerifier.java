package com.nextlabs.pf.destiny.importexport;

import java.util.Collection;
import java.util.TreeMap;

public interface IComponentVerifier {
	/**
	 * This method will attempt to match imported components to existing
	 * components via the webserver, and place them in the proper Collection,
	 * depending on if it is a verified components, or a conflicted component.
	 * Renamed components will have an entry in the state's replacement mapping.
	 * Each conflicted components will have an IImportConflict object created to
	 * store the user's conflict resolution for that entity.
	 */
	IImportState verifyComponents(
			IImportState importState, 
			Collection<ExportEntity> components,
			ExportFile importData, 
			TreeMap<Long, Long> userIdMapping) throws ImportException;

/*
	**
	 * This method returns the verified components.
	 * 
	 * @return the Collection of verified components
	 *
	public Collection returnVerified();

	**
	 * This method returns the conflicted components for the user to resolve.
	 * 
	 * @return the Collection of IImportConflict objects
	 *
	public Collection returnConflicted();
*/
}
