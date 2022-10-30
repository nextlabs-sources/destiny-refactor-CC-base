package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Calendar;
import com.bluejungle.framework.domain.IDomainObject;

/*
 * Created on Oct 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentRegistrationDO.java#1 $
 */

public interface IAgentRegistrationDO extends IDomainObject {

    public Calendar getRegistrationTime();
}