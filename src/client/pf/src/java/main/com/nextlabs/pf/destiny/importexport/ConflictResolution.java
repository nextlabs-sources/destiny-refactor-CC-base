/*
 * Created on Jan 21, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.pf.destiny.importexport;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/nextlabs/pf/destiny/importexport/ConflictResolution.java#1 $
 */

public class ConflictResolution {
	public enum ConflictType {
		UNKNOWN, 
		KEEP_OLD, 
		KEEP_NEW, 
		RENAME_NEW
		};
	
	public static final ConflictResolution UNKNOWN = new ConflictResolution(ConflictType.UNKNOWN);
	public static final ConflictResolution KEEP_OLD = new ConflictResolution(ConflictType.KEEP_OLD);
	public static final ConflictResolution KEEP_NEW = new ConflictResolution(ConflictType.KEEP_NEW);
	public static final ConflictResolution RENAME_NEW = new ConflictResolution(ConflictType.RENAME_NEW, null);
	
	
	
	private ConflictType type;
	
	//only used if the Type is RENAME
	private String newName;

	
	private ConflictResolution(ConflictType type) {
		if(type == ConflictType.RENAME_NEW){
			throw new IllegalArgumentException("rename requires renameSuffix");
		}
		this.type = type;
		this.newName = null;
	}
	
	
	public ConflictResolution(ConflictType type, String newName) {
		if(type != ConflictType.RENAME_NEW){
			throw new IllegalArgumentException("only rename requires renameSuffix");
		}
		this.type = type;
		this.newName = newName;
	}

	/**
	 * 
	 * @return the rename suffix or null if it doesn't have/need one
	 */
	public String getNewName() {
		return this.newName;
	}

	public ConflictType getType() {
		return this.type;
	}
}
