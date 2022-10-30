package com.bluejungle.destiny.container.shared.sharedfolder.defaultimpl;

/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/defaultimpl/SharedFolderInformationImpl.java#3 $
 */

public class SharedFolderInformationImpl implements ISharedFolderData {

    private final ISharedFolderAlias[] list;
    private final SharedFolderInformationCookieImpl cookie;

    /**
     * Constructor
     *  
     */
    public SharedFolderInformationImpl(ISharedFolderAlias[] list) {
        this(list, Calendar.getInstance());
    }

    /**
     * Constructor
     *  
     */
    public SharedFolderInformationImpl(ISharedFolderAlias[] list, Calendar when) {
        super();
        this.list = list;
        this.cookie = new SharedFolderInformationCookieImpl();
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(when.getTime());
        cookie.setTimestamp(tmp);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getCookie()
     */
    public ISharedFolderCookie getCookie() {
        return cookie;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getAliases()
     */
    public ISharedFolderAlias[] getAliases() {
        return list;
    }

}
