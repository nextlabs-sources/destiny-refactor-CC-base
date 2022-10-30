/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.container.dcsf;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderAliasImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderAliasesAliasImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderCookieImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderDataImpl;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasList;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliases;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasesAlias;
import com.bluejungle.destiny.types.shared_folder.SharedFolderData;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;

/**
 * This class converts shared folder information between web-service objects and
 * DO/Data objects.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/service/SharedFolderWebServiceHelper.java#1 $
 */

public class SharedFolderWebServiceHelper {

    /*
     * Private variable:
     */
    private static final SharedFolderWebServiceHelper SINGLETON_INSTANCE = new SharedFolderWebServiceHelper();

    /**
     * Constructor
     *  
     */
    private SharedFolderWebServiceHelper() {
    }

    /**
     * This method should extract the shared folder data object from the
     * corresponding web-service object.
     * 
     * @return
     */
    public ISharedFolderData convertToSharedFolderData(SharedFolderData wsData) {
        if (wsData == null) {
            return null;
        }
        SharedFolderDataCookie cookie = wsData.getCookie();
        Calendar cal;
        if (cookie != null) {
            cal = cookie.getTimestamp();
        } else {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
        }
        SharedFolderDataImpl result = new SharedFolderDataImpl();
        result.setCookie(new SharedFolderCookieImpl(cal));
        SharedFolderAliasList wsList = wsData.getAliasList();
        SharedFolderAliases[] wsFolderAliases = wsList.getAliases();
        if (wsFolderAliases != null) {
            int size = wsFolderAliases.length;
            for (int i = 0; i < size; i++) {
                SharedFolderAliases wsAliases = wsFolderAliases[i];
                SharedFolderAliasesAlias[] wsAliasesAlias = wsAliases.getAlias();
                if (wsAliasesAlias != null) {
                    SharedFolderAliasImpl aliasesAlias = new SharedFolderAliasImpl();
                    int aliasSize = wsAliasesAlias.length;
                    for (int j = 0; j < aliasSize; j++) {
                        SharedFolderAliasesAlias wsAlias = wsAliasesAlias[j];
                        aliasesAlias.addAlias(new SharedFolderAliasesAliasImpl(wsAlias.getName()));
                    }
                    SharedFolderDataImpl data = new SharedFolderDataImpl();
                    result.addSharedFolderAlias(aliasesAlias);
                }
            }
        }
        return result;
    }

    /**
     * Returns the singleton instance of this helper class
     * 
     * @return helper class instance
     */
    public static SharedFolderWebServiceHelper getInstance() {
        return SINGLETON_INSTANCE;
    }

    /**
     * This method should extract the web-service object from the corresponding
     * data object
     * 
     * @param data
     */
    /*
     * public SharedFolderData
     * convertFromSharedFolderData(ISharedFolderInformation data) { if (data ==
     * null) { return null; } return new SharedFolderData(data.getData(),
     * convertFromSharedFolderCookieData(data.extractCookie())); }
     */

    /**
     * This method should extract the shared folder cookie data object from the
     * corresponding web-service object.
     * 
     * @return
     */
    public ISharedFolderCookie convertToSharedFolderCookieData(SharedFolderDataCookie wsCookie) {
        Calendar when;
        if (wsCookie != null) {
            when = wsCookie.getTimestamp();
        } else {
            when = Calendar.getInstance();
            when.setTimeInMillis(0);
        }
        return new SharedFolderCookieImpl(when);
    }

    /**
     * This method should extract the web-service object from the corresponding
     * shared folder cookie object
     * 
     * @param cookie
     */
    /*
     * public SharedFolderDataCookie
     * convertFromSharedFolderCookieData(ISharedFolderInformationCookie cookie) {
     * Calendar cal; if (cookie != null) { cal = cookie.getTimestamp(); } else {
     * cal = Calendar.getInstance();
     * cal.setTime(UnmodifiableDate.START_OF_TIME); } return new
     * SharedFolderDataCookie(cal); }
     */
}
