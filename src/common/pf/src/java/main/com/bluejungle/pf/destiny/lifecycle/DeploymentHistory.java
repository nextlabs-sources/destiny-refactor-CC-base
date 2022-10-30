package com.bluejungle.pf.destiny.lifecycle;

import java.util.Date;

import com.bluejungle.framework.utils.TimeRelation;

public class DeploymentHistory {
    
    private final TimeRelation timeRelation;
    
    private final Long modifier;
    
    private final Date lastModified;
    
    private final Long submitter;
    
    private final Date submittedTime; 
    
    private final Date deployTime;
    
    private final Long deployer;

    public DeploymentHistory(
            TimeRelation timeRelation
          , Date lastModified
          , Long modifier
          , Date submittedTime
          , Long submitter
          , Date deployTime
          , Long deployer
    ) {
        this.timeRelation = timeRelation;
        this.lastModified = lastModified;
        this.modifier = modifier;
        this.submittedTime = submittedTime;
        this.submitter = submitter;
        this.deployTime = deployTime;
        this.deployer = deployer;
    }

    public TimeRelation getTimeRelation() {
        return timeRelation;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public Long getModifier() {
        return modifier;
    }

    public Date getSubmittedTime() {
        return submittedTime;
    }
    
    public Long getSubmitter() {
        return submitter;
    }
    
    public Date getDeployTime() {
        return deployTime;
    }

    public Long getDeployer() {
        return deployer;
    }
    
}
