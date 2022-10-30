/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/DictionaryElementBase.java#1 $
 */

package com.bluejungle.dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the base class for data elements the Dictionary stores,
 * both the leaf elements and the groups. Instances of this class
 * keep the ID, the version, and the time relation of the element.
 * Leaf elements add the attributes, while group elements add names
 * to the list of common attributes defined by this class.
 * This class is protected - its purpose is mainly to reuse
 * some implementation details between groups and leaf elements.
 */

abstract class DictionaryElementBase implements IMElementBase {
	private static final Log LOG = LogFactory.getLog(DictionaryElementBase.class);

    /** The primary key of this element. */
    private Long id;

    /** The primary key of the first */
    private Long originalId;

    /** The version of this item for the optimistic locking purposes. */
    private int version;

    /** The Enrollment that was responsible for producing this entity. */
    private Enrollment enrollment;

    /** The active-from and active-to fields for this entity. */
    private TimeRelation timeRelation;

    /** The key of this element. */
    private DictionaryKey key;

    /** This is the path to this item in the dictionary. */
    private DictionaryPath path;

    /** This is the display name of this dictionary item. */
    private String displayName;

    /** This is the unique name of this dictionary item. */
    private String uniqueName;

    /**
     * This field is used in the copy-on-write scheme
     * to hold a copy of the object being modified.
     */
    private DictionaryElementBase original;

    // TODO (sergey) remove this attribute when we remove
    // the group change APIs relying on storing this flag.
    private boolean isReparented;

    /**
     * This constructor is necessary for derived classes
     * to be able to provide the corresponding constructor
     * for Hibernate.
     */
    protected DictionaryElementBase() {
    }

    /**
     * Redived classes use this constructor to set the enrollment.
     */
    protected DictionaryElementBase(DictionaryPath path, Enrollment enrollment, DictionaryKey key) {
        this.path = path;
        this.enrollment = enrollment;
        this.key = key;
        isReparented = true;
    }

    /**
     * Returns the enrollment from which this element was created.
     * @return the enrollment from which this element was created.
     */
    public IEnrollment getEnrollment() {
        return enrollment;
    }

    /**
     * @see IElementBase#getExternalKey()
     */
    public DictionaryKey getExternalKey() {
        return key;
    }

    /**
     * @see IElementBase#getInternalKey()
     */
    public Long getInternalKey() {
        if ( originalId == null ) {
            throw new IllegalStateException("Getting an internal key of an unsaved element.");
        }
        return originalId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DictionaryPath getPath() {
        return path;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public boolean setDisplayName( String displayName ) {
        boolean changed = (displayName!=null) ? !displayName.equals(this.displayName) : this.displayName != null;
        if ( changed ) {
            onUpdate();
            this.displayName = displayName;
        }
        return changed;
    }

    public boolean setExternalKey( DictionaryKey key ) {
        boolean changed = (key!=null) ? !key.equals(this.key) : this.key != null;
        if ( changed ) {
            onUpdate();
            this.key = key;
        }
        return changed;
    }

    public boolean setPath(DictionaryPath path) {
        return setPath(path, false);
    }

    boolean setPath(DictionaryPath path, boolean replacement) {
        if ( path == null ) {
            throw new NullPointerException("path");
        }
        if (path.equals(this.path)) {
            return false;
        }
        onUpdate();
        this.path = path;
        isReparented = !replacement;
        return true;
    }

    public boolean setUniqueName( String uniqueName ) {
        boolean changed = (uniqueName!=null) ? !uniqueName.equals(this.uniqueName) : this.uniqueName != null;
        if ( changed ) {
            onUpdate();
            this.uniqueName = uniqueName;
        }
        return changed;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        Long eId = effectiveId();
        return eId==null ? super.hashCode() : eId.hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object other) {
        if (other instanceof DictionaryElementBase) {
            Long eId = effectiveId();
            return eId == null ? super.equals(other) : eId.equals(((DictionaryElementBase)other).effectiveId());
        } else {
            return false;
        }
    }

    private Long effectiveId() {
        return id==null ? originalId==null? null : originalId : id;
    }

    /**
     * Subclasses call this method on each update operation.
     */
    protected final void onUpdate() {
        if (isNew() || isUpdated()) {
            return;
        }
        if (isClosed()) {
            throw new IllegalStateException("An attempt is made to update an inactive record.");
        }
        if(LOG.isDebugEnabled()){
        	StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        	if(stackTrace != null && stackTrace.length > 1){
				LOG.debug(stackTrace[1].getClassName() + "." + stackTrace[1].getMethodName()
						+ " triggered update on "
						+ "id = " + id + ", " + "path = "+ path);
        	}
        }
        original = deepCopy();
        original.id = id;
        original.version = version;
        original.enrollment = enrollment;
        original.originalId = originalId;
        original.key = key;
        original.timeRelation = timeRelation;
        original.displayName = displayName;
        original.uniqueName = uniqueName;
        original.path = path;
        original.isReparented = isReparented;
        isReparented = false;
        this.id = null;
        this.version = 1;
    }

    /**
     * This method indicates that the element is a historical
     * record rather than the active element.
     * @return true if the record is closed; false otherwise.
     */
    private boolean isClosed() {
        return timeRelation.isClosed();
    }

    /**
     * Once the changes have been saved, this method is called 
     * to mark the save completed. Subsequent updates to this object
     * will result in creating a new instance on save.
     */
    void saveComplete() {
        original = null;
    }

    /**
     * Determines if the object has been updated.
     * @return true if the object has been updated, false otherwise.
     */
    protected final boolean isUpdated() {
        return original != null;
    }

    /**
     * Determines if the object is new.
     * @return true if the object is new, false otherwise.
     */
    protected final boolean isNew() {
        return id == null;
    }

    /**
     * Subclasses provide an implementation of this method
     * to construct deep copies of themselves.
     *
     * @return a deep copy of this object.
     */
    protected abstract DictionaryElementBase deepCopy();
    /**
     * Obtains the ID of this entity.
     * This is an internal method for hibernate's use.
     * @return the ID of this entity.
     */
    Long getId() {
        return id;
    }

    /**
     * Changes the ID of this entity.
     * This is an internal method for hibernate's use.
     * @param id the new ID for this entity.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtains the stored original ID of this entity.
     * This is an internal method for hibernate's use.
     * @return the ID of this entity.
     */
    Long getOriginalId() {
        return originalId;
    }

    /**
     * Changes the stored origianl ID of the entity.
     * This is an internal method for hibernate's use.
     * @param id the new ID for this entity.
     */
    void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    /**
     * Gets the version of this entity.
     * This is an internal method for hibernate's use.
     * @return the version of this entity.
     */
    int getVersion() {
        return version;
    }

    /**
     * Changes the version of this entity.
     * This is an internal method for hibernate's use.
     * @param version the new version for this entity.
     */
    void setVersion( int version ) {
        this.version = version;
    }

    /**
     * Returns the time relation (from-to).
     * This is an internal method for hibernate's use.
     * @return the time relation (from-to).
     */
    TimeRelation getTimeRelation() {
        return timeRelation;
    }

    /**
     * Sets the new time relation (from-to).
     * This is an internal method for hibernate's use.
     * @param timeRelation the new time relation (from-to).
     */
    void setTimeRelation( TimeRelation timeRelation ) {
        this.timeRelation = timeRelation;
    }

    /**
     * This method lets the enrollment session get the original
     * element, if any, for the given element.
     * @return the original element, if any, for the given element.
     */
    DictionaryElementBase getOriginal() {
        return original;
    }

    /**
     * This is a package-private getter for Hibernate.
     * @return the byte array from the dictionary key.
     */
    byte[] getKeyData() {
        return key != null ? key.getKey() : null;
    }

    /**
     * This is a package-private setter for Hibernate.
     * @param data the key data.
     */
    void setKeyData(byte[] data) {
        key = data != null ? new DictionaryKey(data) : null;
    }

    /**
     * Obtains an indirect update object to perform changes
     * resulting from the change to the current object.
     * @return an indirect update object to perform changes
     * resulting from the change to the current object.
     */
    IIndirectUpdate getIndirectUpdate() {
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName() + "/" + getId();
    }
}
