package com.nextlabs.pf.destiny.importexport;

import java.util.Collection;
import com.bluejungle.framework.domain.IHasId;

public interface IImportState {
	enum ConflictAction {ASK_USER, ALWAYS_OLD, ALWAYS_NEW};
	enum Shallow {SHALLOW, FULL};
    enum RetainStatus { RETAIN, SET_TO_DRAFT };

	/**
	 * This method returns whether or not any entities have conflicts.
	 */
	boolean hasConflicts();

	/**
	 * This method returns a Collection of ImportConflict objects.
	 * @return conflicts, the Collection of conflicted policies.
	 */
	Collection<IImportConflict> getConflicts();

	void addConflict(IImportConflict conflict);

	//Leaf Error behavior
	Shallow getLeafErrors();

	void setLeafErrors(Shallow leafErrors);

	ConflictAction getKeepWhich();

	void setKeepWhich(ConflictAction keepWhich);

	Collection<ExportEntity> getUnconflicted();


	void addUnconflicted(ExportEntity unconflicted);
	
	Collection<ExportEntity> getErrors();
	
	void addError(ExportEntity error);
	
	Collection<IHasId> getShouldBeDeletedPolicies();
	
	void setShouldBeDeletedPolicies (Collection<IHasId> policies);
}
