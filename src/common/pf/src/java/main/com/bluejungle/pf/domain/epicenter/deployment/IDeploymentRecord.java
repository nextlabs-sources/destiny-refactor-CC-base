package com.bluejungle.pf.domain.epicenter.deployment;

import java.sql.Timestamp;

// Copyright Blue Jungle, Inc.

/*
 * IDeploymentRecord captures all the informatio about deployment of
 * a particular deployment spec
 * 
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/deployment/IDeploymentRecord.java#1 $
 */

public interface IDeploymentRecord {

    Long getDeploymentSpecId();
    int getDeploymentSpecVersion();
    Timestamp getDeploymentTime();
    
}
