package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DeploymentRecord.java#1 $
 */

import java.util.Date;

import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * Represents a record of deploying one or more development entities.
 */
public class DeploymentRecord {

    private Long id;

    private DeploymentActionType actionType;

    private Date asOf;

    private Date whenRequested;

    private Date whenCancelled = null;

    /** The deployment type of this record. */
    private DeploymentType deploymentType;

    /** The number of entities deployed with this record. */
    private int numberOfDeployedEntities = 0;

    /** A flag that indicates whether or not the record is hidden. */
    private boolean hidden;
    
    private Long deployer;

    /**
     * Makes a new DeploymentRecord.
     *
     * This constructor is package-private. The only place where it is used
     * is the LifecycleManager - there is no outside access to it.
     * This is why all argument checks are replaced with assertions.
     *
     * @param actionType the type of deployment action.
     * @param asOf the asOf date for the action.
     * @param now the date the change was requested.
     */
    DeploymentRecord(
        DeploymentActionType actionType
    ,   DeploymentType deploymentType
    ,   Date asOf
    ,   Date now
    ,   boolean hidden
    ,   Long deployer) {
        assert actionType != null;
        assert asOf != null;
        assert now != null;
        assert deploymentType != null;
        this.actionType = actionType;
        this.deploymentType = deploymentType;
        this.asOf = UnmodifiableDate.forDate( asOf );
        this.whenRequested = UnmodifiableDate.forDate( now );
        this.hidden = hidden;
        this.deployer = deployer;
    }

    /**
     * Makes a new DeploymentRecord for a complete set of fields.
     *
     * @param id the ID of this record.
     * @param actionType the type of deployment action.
     * @param deploymentType the deployment type of this record.
     * @param asOf the asOf date for the action.
     * @param now the date the change was requested.
     */
    public DeploymentRecord(
        Long id
    ,   DeploymentActionType actionType
    ,   DeploymentType deploymentType
    ,   Date asOf
    ,   Date now
    ,   Date whenCancelled
    ,   int numberOfDeployedEntities
    ,   boolean hidden
    ,   Long deployer) {
        // Outside applications are allowed to handle existing records,
        // but are prohibited from creating new ones. Therefore, ID must be non-null.
        if ( id == null ) {
            throw new NullPointerException("id");
        }
        if ( actionType == null ) {
            throw new NullPointerException("actionType");
        }
        if ( deploymentType == null ) {
            throw new NullPointerException("deploymentType");
        }
        if ( asOf == null ) {
            throw new NullPointerException("asOf");
        }
        if ( now == null ) {
            throw new NullPointerException("now");
        }

        this.id = id;
        this.actionType = actionType;
        this.deploymentType = deploymentType;
        this.asOf = UnmodifiableDate.forDate( asOf );
        this.whenRequested = UnmodifiableDate.forDate( now );
        this.whenCancelled = whenCancelled;
        this.numberOfDeployedEntities = numberOfDeployedEntities;
        this.hidden = hidden;
        this.deployer = deployer;
    }

    /**
     * Constructs an empty deployment record.
     * This is a private constructor for hibernate.
     */
    DeploymentRecord() {
    }

    /**
     * Returns the deployment action type.
     * @return the deployment action type.
     */
    public DeploymentActionType getDeploymentActionType() {
        return actionType;
    }

    /**
     * Sets the deployment action type.
     * This package-private method is for Hibernate's use.
     *
     * @param actionType the deployment action type.
     */
    void setDeploymentActionType( DeploymentActionType actionType ) {
        this.actionType = actionType;
    }

    /**
     * Returns the asOf date for the change.
     * @return the asOf date for the change.
     */
    public Date getAsOf() {
        return asOf;
    }

    /**
     * Sets the asOf date for the change.
     * This package-private method is for Hibernate's use.
     *
     * @param asOf the asOf date for the change.
     */
    void setAsOf( Date asOf ) {
        this.asOf = UnmodifiableDate.forDate( asOf );
    }

    /**
     * Mark the record cancelled as of the specified date.
     * @param when the cancellation <code>Date</code> for this record.
     */
    public void cancel( Date when ) {
        whenCancelled = when;
    }

    /**
     * Returns true if this record represents an action that has been cancelled;
     * returns false otherwise.
     * @return true if this record represents an action that has been cancelled;
     * false otherwise.
     */
    public boolean isCancelled() {
        return whenCancelled != null;
    }

    /**
     * Returns the date when the change was requested.
     * @return the date when the change was requested.
     */
    public Date getWhenRequested() {
        return whenRequested;
    }

    /**
     * Sets the date when the change was requested.
     * This package-private method is for Hibernate's use.
     *
     * @param whenRequested the date when the change was requested.
     */
    void setWhenRequested( Date whenRequested ) {
        this.whenRequested = UnmodifiableDate.forDate( whenRequested );
    }

    /**
     * Obtains the ID of this entity.
     * @return the ID of this entity.
     */
    public Long getId() {
        return id;
    }

    /**
     * Changes the ID of this entity.
     * This package-private method is for Hibernate's use.
     *
     * @param id the new ID for this entity.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the deployment type of this record.
     * @return the deployment type of this record.
     */
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    /**
     * Sets the deployment type of this record.
     * @param deploymentType the deployment type of this record.
     */
    public void setDeploymentType(DeploymentType deploymentType) {
        this.deploymentType = deploymentType;
    }

    /**
     * Get the number of entities deployed on this record. 
     * @return the number of entities deployed on this record.
     */
    public int getNumberOfDeployedEntities() {
        return numberOfDeployedEntities;
    }

    public Date getWhenCancelled() {
        return whenCancelled;
    }

    /**
     * Sets the optional date when the deployment has been cancelled.
     * This package-private method is for Hibernate's use.
     *
     * @param whenCancelled the optional date when the deployment
     * has been cancelled.
     */
    void setWhenCancelled(Date whenCancelled) {
        this.whenCancelled = whenCancelled;
    }

    void setNumberOfDeployedEntities(int numberOfDeployedEntities) {
        this.numberOfDeployedEntities = numberOfDeployedEntities;
    }

    /**
     * Determines if the record is hidden.
     * @return Returns true if the record is hidden; returns false otherwise.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets the hidden status of the record.
     * @param hidden The hidden flag to set.
     */
    void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Long getDeployer() {
        return deployer;
    }

    void setDeployer(Long deployer) {
        this.deployer = deployer;
    }

}
