package com.nextlabs.pf.destiny.importexport.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.nextlabs.pf.destiny.importexport.ConflictResolution;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.ConflictResolution.ConflictType;

public class ImportConflict implements IImportConflict {
	private static IPolicyEditorClient client;
	
	private final String name;
	private final String type;
	private String fromFile;
	private String fromServer;
	private ConflictResolution conflictResolution = ConflictResolution.UNKNOWN;
	
	ImportConflict(String name, String type, String fromFile, String fromServer) {
		this.name = name;
		this.type = type;
		this.fromFile = fromFile;
		this.fromServer = fromServer;
	}

	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}

	public void setFromServer(String fromServer) {
		this.fromServer = fromServer;
	}

	/**
	 * This method provides the import file's version of a conflicted
	 * entity
	 * @return the version of the entity from the export file
	 */
	public String versionFromFile() {
		return fromFile;
	}

	/**
	 * This method provides the server's existing version of a conflicted
	 * entity
	 * @return the version of the entity from the database
	 */
	public String versionFromServer() {
		return fromServer;
	}

	/**
	 * This method tells whether the conflict has been resolved or not.
	 * @return whether the conflict has been resolved or not.
	 */
	public boolean isResolved() {
		return conflictResolution != null && conflictResolution != ConflictResolution.UNKNOWN;
	}

	/**
	 * This method sets the user's resolution to that conflict.
	 */
	public void setResolution(ConflictResolution conflictResolution) throws ImportException {
		if(conflictResolution.getType() == ConflictType.RENAME_NEW){
			//need to check the new name make sure it is not duplicated
			if(client== null){
				//init PolicyEditorClient
				IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
				client = (IPolicyEditorClient) compMgr.getComponent(PolicyEditorClient.COMP_INFO.getName());
			}
			
			Collection<String> nameExist = new ArrayList<String>();
			nameExist.add(conflictResolution.getNewName());
			Collection currEntityCollec;
			try {
				EntityType type = EntityType.forName(this.getType());
				currEntityCollec = client.getEntitiesForNamesAndType(nameExist, type, true);
			} catch (PolicyEditorException e) {
				throw new ImportException(e);
			}
			
			IHasId currEntity = (IHasId) (currEntityCollec.iterator().next());
			if (currEntity.getId() != null) { 
				//ID in policy is null when getEntitiesForNamesAndType creates a new policy 
				throw new ImportException(conflictResolution.getNewName() + " already exists");
			} 
		}
		this.conflictResolution = conflictResolution;
	}

	public ConflictResolution getResolution() {
		return conflictResolution;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

}
