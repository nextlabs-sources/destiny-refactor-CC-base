/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.container.dms;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * This class converts shared folder information between web-service objects and
 * DO/Data objects.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/service/SharedFolderWebServiceHelper.java#1 $
 */

public class SharedFolderWebServiceHelper {

    /**
     * This method should extract the web-service object from the corresponding
     * shared folder cookie object
     * 
     * @param cookie
     */
    public static SharedFolderDataCookie convertFromSharedFolderCookieData(ISharedFolderCookie cookie) {
        Calendar cal;
        if (cookie != null) {
            cal = cookie.getTimestamp();
        } else {
            cal = Calendar.getInstance();
            cal.setTime(UnmodifiableDate.START_OF_TIME);
        }
        return new SharedFolderDataCookie(cal);
    }
}