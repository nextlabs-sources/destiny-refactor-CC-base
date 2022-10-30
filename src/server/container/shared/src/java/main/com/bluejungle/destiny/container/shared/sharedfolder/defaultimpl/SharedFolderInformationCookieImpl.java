/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder.defaultimpl;

import java.util.Calendar;
import java.util.Date;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/defaultimpl/SharedFolderInformationCookieImpl.java#1 $
 */

public class SharedFolderInformationCookieImpl implements ISharedFolderCookie {

    private Calendar timestamp;

    /**
     * Constructor
     *  
     */
    public SharedFolderInformationCookieImpl() {
        super();
        timestamp = Calendar.getInstance();
        timestamp.setTime( UnmodifiableDate.START_OF_TIME );
    }

    public SharedFolderInformationCookieImpl( Date when ) {
        super();
        timestamp = Calendar.getInstance();
        timestamp.setTime( when );
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationCookie#getTimestamp()
     */
    public Calendar getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the timestamp
     * 
     * @param timestamp
     */
    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }
}