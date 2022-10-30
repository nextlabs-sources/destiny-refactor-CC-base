/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder;

/**
 * This is a convenience-interface that can be implemented by a class that needs
 * to be a source and a sink at the same time.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/ISharedFolderInformationRelay.java#1 $
 */

public interface ISharedFolderInformationRelay extends ISharedFolderInformationSource, ISharedFolderInformationSink {

    public static final String COMP_NAME = "SharedFolderInformationRelay";
}