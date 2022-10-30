package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DeploymentEntity.java#1 $
 */

import java.util.Date;

import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

public class DeploymentEntity extends AbstractEntity implements IHasChangeablePQL, Cloneable {

    /** The development entity for which this is a deployment entity. */
    private DevelopmentEntity devEntity;

    /** The deployment record associated with making this deployment entity. */
    private DeploymentRecord deploymentRecord;
    
    /** The PQL content once server target resolver gets done with it **/
    private String processedPQL;

    /** The number of times this deployment record has been overriden. */
    private int overriddeCount = 0;

    /** The fromDate-toDate active range of this entity. */
    private TimeRelation timeRelation;

    /** The original version of the development entity used to create this deployment entity. */
    private int originalVersion;

    /**
     * Internal default constructor for hibernate's use.
     */
    DeploymentEntity() {
    }
    
    private DeploymentEntity(
            String name
            , String description
            , String pql
            , DeploymentRecord deploymentRecord
            , DevelopmentEntity devEntity
            , int originalVersion
            , Date asOf
            , boolean hidden
            , Date lastModified
            , Long lastModifier
            , Date submittedTime
            , Long submitter
            , boolean checkPqlNull
     ) {
        super(  name
              , description
              , null
              , hidden
              , lastModified
              , lastModifier
              , submittedTime
              , submitter
        );
          
        if (devEntity == null) {
            throw new NullPointerException("deventity");
        }
        this.devEntity = devEntity;
        this.originalVersion = originalVersion;
        this.timeRelation = new TimeRelation(asOf, UnmodifiableDate.END_OF_TIME);
        this.deploymentRecord = deploymentRecord;
        if (checkPqlNull && pql == null) {
            throw new NullPointerException("pql");
        }
        this.pql = pql;
    }
    
    /**
     * Creates a new deployment entity with the specified set of parameters.
     * This method has package visibility because it is intended only for the
     * use of <code>DevelopmentEntity.makeDeploymentEntity()</code> method.
     * @param name the name of the deployment entity.
     * @param pql the pql content of the deployment entity.
     * @param devEntity the development entity for which this is a deployment entity.
     * @param deploymentType the deployment type for this entity.
     * @param originalVersion the version of the development entity at the time of deployment request.
     * @param asOf the time at which the deployment entity is to become active.
     */
    DeploymentEntity(
        String name
      , String description
      , String pql
      , DeploymentRecord deploymentRecord
      , DevelopmentEntity devEntity
      , int originalVersion
      , Date asOf
      , boolean hidden
      , Date lastModified
      , Long lastModifier
      , Date submittedTime
      , Long submitter
     ) {
        this(  name
              , description
              , pql
              , deploymentRecord
              , devEntity
              , originalVersion
              , asOf
              , hidden
              , lastModified
              , lastModifier
              , submittedTime
              , submitter
              , true
        );
    }

    /**
     * Creates a new deployment entity with the specified set of parameters and null PQL.
     * This method has package visibility because it is intended only for the
     * use of <code>DevelopmentEntity.makeDeploymentEntity()</code> method.
     * @param name the name of the deployment entity.
     * @param devEntity the development entity for which this is a deployment entity.
     * @param originalVersion the version of the development entity at the time of deployment request.
     * @param asOf the time at which the deployment entity is to become active.
     */
    DeploymentEntity(
        String name
      , String description
      , DeploymentRecord deploymentRecord
      , DevelopmentEntity devEntity
      , int originalVersion
      , Date asOf
      , boolean hidden
      , Date lastModified
      , Long lastModifier
      , Date submittedTime
      , Long submitter
    ) {
        this(  name
             , description
             , null
             , deploymentRecord
             , devEntity
             , originalVersion
             , asOf
             , hidden
             , lastModified
             , lastModifier
             , submittedTime
             , submitter
             , false
        );
    }

    /**
     * Returns a copy of this object. A copy is safe to send to
     * RDBMS for persisting as new.
     * @return a copy of this object.
     */
    DeploymentEntity copy() {
        try {
            DeploymentEntity res = (DeploymentEntity) super.clone();
            res.setId(null);
            return res;
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError("DeploymentEntity is declared to support cloneable");
        }
    }

    

    /**
     * Returns the type of this entity (a policy, a resource group, etc.)
     * @return the type of this entity (a policy, a resource group, etc.)
     */
    public EntityType getType() {
        return devEntity.getType();
    }

    /**
     * Returns true if this entity has not been overriden, false otherwise.
     * @return true if this entity has not been overriden, false otherwise.
     */
    public boolean isActive() {
        return overriddeCount < 1;
    }

    /**
     * Increments the override count of this entity.
     */
    public void addOverride() {
        overriddeCount++;
    }

    /**
     * Decrements the override count of this entity.
     */
    public void removeOverride() {
        if (overriddeCount != 0) {
            overriddeCount--;
        }
    }

    /**
     * Returns the date on which this deployment object becomes active.
     * @return the date on which this deployment object becomes active.
     */
    public Date getActiveFrom() {
        // This object either (1) came from hibernate, or (2) was created
        // as a result of a call to the deploy method of the lifecycle manager.
        // In both cases the timeRelation must be set to a non-null value.
        assert timeRelation != null;
        return timeRelation.getActiveFrom();
    }

    /**
     * Returns the date on which this deployment object becomes inactive.
     * @return the date on which this deployment object becomes inactive.
     */
    public Date getActiveTo() {
        // This object either (1) came from hibernate, or (2) was created
        // as a result of a call to the deploy method of the lifecycle manager.
        // In both cases the timeRelation must be set to a non-null value.
        assert timeRelation != null;
        return timeRelation.getActiveTo();
    }

    /**
     * Returns the development entity of this deployment entity.
     * This is an internal method for use by hibernate and the LifecycleManager.
     * @return the development entity of this deployment entity.
     */
    public DevelopmentEntity getDevelopmentEntity() {
        return devEntity;
    }

    /**
     * Returns the processed PQL content of the entity, which can
     * be null if this entity has not been processed or does not
     * need processing.
     * 
     * @return processed PQL
     */
    public String getProcessedPQL() {
        return this.processedPQL;
    }
    /**
     * 
     * Sets the processed PQL content of this entity
     * 
     * @param pql processed PQL
     */
    public void setProcessedPQL(String pql) {
        this.processedPQL = pql;
    }
    
    /**
     * Changes the PQL content of the entity.
     * @param pql the new PQL content of the entity.
     */
    public void setPql(String pql) {
        this.pql = pql;
    }

    /**
     * Sets the new development entity that owns this deployment entity.
     * This is an internal method for hibernate's and lifecycle manager's use.
     * @param devEntity the new development entity that owns this deployment entity.
     */
    void setDevelopmentEntity(DevelopmentEntity devEntity) {
        this.devEntity = devEntity;
    }

    /**
     * Returns the number of times this deployment record has been overriden.
     * This is an internal method for hibernate's use.
     * @return the number of times this deployment record has been overriden.
     */
    int getOverrideCount() {
        return overriddeCount;
    }

    /**
     * Sets the new value for the override count.
     * This is an internal method for hibernate's use.
     * @param count the new value for the override count.
     */
    void setOverrideCount(int count) {
        overriddeCount = count;
    }

    /**
     * Returns the time relation (from-to).
     * This is an internal method for hibernate's and LifecycleManager's use.
     * @return the time relation (from-to).
     */
    TimeRelation getTimeRelation() {
        return timeRelation;
    }

    /**
     * Sets the new time relation (from-to).
     * This is an internal method for hibernate's and LifecycleManager's use.
     * @param timeRelation the new time relation (from-to).
     */
    void setTimeRelation(TimeRelation timeRelation) {
        this.timeRelation = timeRelation;
    }

    /**
     * Returns the version of the development entity used to create this deployment entity.
     * @return the version of the development entity used to create this deployment entity.
     */
    public int getOriginalVersion() {
        return originalVersion;
    }

    /**
     * Sets the version of the development entity used to create this deployment entity.
     * This is an internal method for hibernate's use.
     * @param originalVersion the version of the development entity used to create this deployment entity.
     */
    void setOriginalVersion( int originalVersion ) {
        this.originalVersion = originalVersion;
    }

    
    /**
     * Returns the deploymentRecord.
     * @return the deploymentRecord.
     */
    public DeploymentRecord getDeploymentRecord() {
        return this.deploymentRecord;
    }
    
    
    /**
     * Sets the deploymentRecord
     * @param deploymentRecord The deploymentRecord to set.
     */
    void setDeploymentRecord(DeploymentRecord deploymentRecord) {
        this.deploymentRecord = deploymentRecord;
    }

}
