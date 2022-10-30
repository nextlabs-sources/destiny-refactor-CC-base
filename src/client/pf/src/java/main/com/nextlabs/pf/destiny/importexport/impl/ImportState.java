package com.nextlabs.pf.destiny.importexport.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.bluejungle.framework.domain.IHasId;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;

public class ImportState implements IImportState {
	//opportunity to log or do some special notification for leaf errors
//	public static final int SHALLOW = 0;
//	public static final int FULL = 1;
	
	boolean hasConflicts = false;
	private Collection<IImportConflict> conflicts = new ArrayList<IImportConflict>(); //collection of ImportConflicts
	private Collection<ExportEntity> unconflicted = new ArrayList<ExportEntity>(); //collection of ExportEntities
	private Collection<ExportEntity> errors = new ArrayList<ExportEntity>(); //collection of ExportEntities
	private Collection<IHasId> shouldBeDeletedPolicies = new ArrayList<IHasId>(); // collection of shouldBeDeletedPolicies
	private ConflictAction keepWhich = ConflictAction.ASK_USER;
	private Shallow leafErrors;  //for import preference dialog's fail recovery: "skip on error" or "abort" 
	
	//Constructor, requires knowing whether to abort on errors or not.
	//this is anthony's fail recovery dropdown
	public ImportState(Shallow leafErrors) {
		this.leafErrors = leafErrors;
	}
	
	public boolean hasConflicts() {
		return !conflicts.isEmpty();
	}

	public Collection<IImportConflict> getConflicts() {
		return conflicts;
	}

	public void addConflict(IImportConflict conflict) {
		conflicts.add(conflict);
	}
	
	//Leaf Error behavior
	public Shallow getLeafErrors() {
		return leafErrors;
	}

	public void setLeafErrors(Shallow leafErrors) {
		this.leafErrors = leafErrors;
	}

	public ConflictAction getKeepWhich() {
		return keepWhich;
	}

	public void setKeepWhich(ConflictAction keepWhich) {
		this.keepWhich = keepWhich;
	}

	public Collection<ExportEntity> getUnconflicted() {
		return unconflicted;
	}

	public void addUnconflicted(ExportEntity unconflicted) {
		this.unconflicted.add(unconflicted);
	}
	
	public Collection<ExportEntity> getErrors() {
		return errors;
	}
	
	public void addError(ExportEntity error) {
		errors.add(error);
	}
	
	public Collection<IHasId> getShouldBeDeletedPolicies(){
		return shouldBeDeletedPolicies;
	}

	public void setShouldBeDeletedPolicies(Collection<IHasId> policies) {
		this.shouldBeDeletedPolicies = policies;
	}
}
