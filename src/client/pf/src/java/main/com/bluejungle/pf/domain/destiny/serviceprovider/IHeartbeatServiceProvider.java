package com.bluejungle.pf.domain.destiny.serviceprovider;

/*
 * Created on Dec 08, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/IHeartbeatServiceProvider.java#1 $:
 */

import com.bluejungle.framework.heartbeat.IHeartbeatListener;

/**
 * IHeartbeatServiceProvider is a marker interface for all objects that are
 * service providers that will be creating heartbeat plugins as well (they
 */

public interface IHeartbeatServiceProvider extends IHeartbeatListener, IServiceProvider {
}
