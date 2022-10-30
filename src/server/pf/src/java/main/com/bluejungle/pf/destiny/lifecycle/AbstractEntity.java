package com.bluejungle.pf.destiny.lifecycle;

import java.util.Date;

import com.bluejungle.pf.destiny.parser.IHasPQL;

abstract class AbstractEntity implements IHasPQL {

    /** The database identifier of this entity. */
    Long id;
    
    /** The name of this entity. */
    String name;

    /** Entity description. */
    String description;
    
    /** The PQL content of this entity. */
    String pql;
    
    /** Internal version of this deployment entity (for concurrency control). */
    int version;
    
    /** A flag that indicates whether or not the entity is hidden. */
    boolean hidden;
    
    /** The date this entity was last updated. */
    Date lastModified;
    
    /** The unique id of the modifier */
    Long modifier;
    
    Date submittedTime;
    
    /** The unique id of the submitter */
    Long submitter;
    
    AbstractEntity() {
    }

    AbstractEntity(
            String name
          , String description
          , String pql
          , boolean hidden
          , Date lastModified
          , Long modifier
          , Date submittedTime
          , Long submitter
    ) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
        this.description = description;
        this.pql = pql;
        this.hidden = hidden;
        this.lastModified = lastModified;
        this.modifier = modifier;
        this.submittedTime = submittedTime;
        this.submitter = submitter;
    }
    
    /**
     * Obtains the ID of this entity.
     * This is an internal method for LifecycleManager's and hibernate's use.
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
     * Returns the name of this entity.
     * @return the name of this entity.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Changes the name of the entity.
     * @param name the new name of the entity.
     */
    void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
    }
    
    /**
     * Gets the description of this entity.
     * @return the description of this entity.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this entity.
     * @param description the new description of this entity.
     */
    void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Returns the PQL content of this entity.
     * @return the PQL content of this entity.
     */
    @Override
    public String getPql() {
        return pql;
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
    void setVersion(int version) {
        this.version = version;
    }

    /**
     * Determines if the entity is hidden.
     * @return Returns true if the entity is hidden; returns false otherwise.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets the hidden status of the entity.
     * @param hidden The hidden flag to set.
     */
    void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    /**
     * Gets the date this entity was last updated.
     * @return the date this entity was last updated.
     */
    public Date getLastModified() {
        return lastModified;
    }
    
    void setLastModified(Date lastModified){
        this.lastModified = lastModified;
    }
    
    public Long getModifier() {
        return modifier;
    }
    
    void setModifier(Long modifier) {
        this.modifier = modifier;
    }
    
    public Date getSubmittedTime() {
        return submittedTime;
    }
    
    void setSubmittedTime(Date submittedTime) {
        this.submittedTime = submittedTime;
    }
    
    public Long getSubmitter() {
        return submitter;
    }

    void setSubmitter(Long submitter) {
        this.submitter = submitter;
    }
    
}
