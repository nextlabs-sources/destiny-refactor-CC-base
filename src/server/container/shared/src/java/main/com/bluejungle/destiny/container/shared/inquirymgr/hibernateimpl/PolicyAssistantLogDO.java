/*
 * Created on Sep 19, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Next Labs Inc.,
 * San Mateo CA, Ownership remains with Next Labs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;


/**
 * This is the data object for the policy activity log.
 * 
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PolicyAssistantLogDO.java#1 $
 */

public class PolicyAssistantLogDO {
    private Long id;
    private Long logUid;
    private String policyAssistantName;
    private String attrOne;
    private String attrTwo;
    private String attrThree;
    private boolean syncDone;
    
    /**
     * Constructor
     */
    public PolicyAssistantLogDO() {
    }

    public Long getId() {
        return id;
    }

    public Long getLogUid() {
        return logUid;
    }

    public String getPolicyAssistantName() {
        return policyAssistantName;
    }

    public String getAttrOne() {
        return attrOne;
    }

    public String getAttrTwo() {
        return attrTwo;
    }

    public String getAttrThree() {
        return attrThree;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLogUid(Long uid) {
        this.logUid = uid;
    }

    public void setPolicyAssistantName(String policyAssistantName) {
        this.policyAssistantName = policyAssistantName;
    }

    public void setAttrOne(String attrOne) {
        this.attrOne = attrOne;
    }

    public void setAttrTwo(String attrTwo) {
        this.attrTwo = attrTwo;
    }

    public void setAttrThree(String attrThree) {
        this.attrThree = attrThree;
    }
    
    public boolean getsyncDone() {
        return syncDone;
    }

    public void setsyncDone(boolean syncDone) {
        this.syncDone = syncDone;
    }
}
