package com.bluejungle.pf.domain.epicenter.deployment;
// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/deployment/IActivationInfo.java#1 $
 * 
 * IActivationInfo represents time intervals for when a policy should
 * be active.
 * 
 */

import java.io.Serializable;
import java.util.Calendar;

import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

public interface IActivationInfo extends Serializable {
    IPolicy getPolicy ();
    void setPolicy (IPolicy policy);
    Calendar[] getStartStopDates ();
    void   setStartStopDates (Calendar[] ss);
}
