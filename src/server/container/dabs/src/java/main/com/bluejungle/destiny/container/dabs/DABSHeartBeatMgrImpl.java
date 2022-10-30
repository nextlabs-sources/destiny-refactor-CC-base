/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import com.bluejungle.destiny.container.dcc.HeartbeatMgrImpl;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSink;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/DABSHeartBeatMgrImpl.java#2 $
 */

public class DABSHeartBeatMgrImpl extends HeartbeatMgrImpl {

    /**
     * Constructor
     *  
     */
    public DABSHeartBeatMgrImpl() {
        super();
        super.setName("DABSHeartBeatMgr");
    }

    /**
     * Process updates provided during the heartbeat request. Updates will
     * consist of new event registrations. These need to be registered with the
     * remote listener manager.
     * 
     * @param response
     *            update provided by DMS
     */
    protected void processHeartBeatUpdate(IComponentHeartbeatResponse response) {
        super.processHeartBeatUpdate(response);
        // Handle the shared-folder updates that may have come with the update:
        ISharedFolderInformationSink sink = getSharedFolderInformationSink();
        sink.setUpdate(response.getSharedFolderData());
    }

    /**
     * This can be implemented by sub-classes to set additional information on
     * the heartbeate
     * 
     * @param heartbeat
     */
    protected void prepareNextHeartbeat(IComponentHeartbeatInfo heartbeat) {
        ISharedFolderInformationSink sink = getSharedFolderInformationSink();
        heartbeat.setSharedFolderCookie(sink.getLastUpdateCookie());
    }

    /**
     * Returns an instance of the ISharedFolderInformationSink implementation
     * class after obtaining it from the component manager.
     * 
     * @return ISharedFolderInformationSink
     */
    protected ISharedFolderInformationSink getSharedFolderInformationSink() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ISharedFolderInformationSink folderInformationSink = (ISharedFolderInformationSink) compMgr.getComponent(ISharedFolderInformationRelay.COMP_NAME);
        return folderInformationSink;
    }
}
