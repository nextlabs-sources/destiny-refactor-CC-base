/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.container.dabs;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
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

    public static SharedFolderData convertFromSharedFolderData(ISharedFolderData data) {
        if (data == null) {
            return null;
        }
        SharedFolderData result = new SharedFolderData();
        result.setCookie(new SharedFolderDataCookie(data.getCookie().getTimestamp()));
        SharedFolderAliasList wsAliasList = new SharedFolderAliasList();
        ISharedFolderAlias[] sharedFolderAliases = data.getAliases();
        if (sharedFolderAliases != null) {
            int size = sharedFolderAliases.length;
            SharedFolderAliases wsSharedFolderAliases[] = new SharedFolderAliases[size];
            for (int i = 0; i < size; i++) {
                SharedFolderAliases wsSharedFolderAlias = new SharedFolderAliases();
                ISharedFolderAlias folderAlias = sharedFolderAliases[i];
                ISharedFolderAliasesAlias[] sharedFolderAliasesAliases = folderAlias.getAliases();
                if (sharedFolderAliasesAliases != null) {
                    int aliasSize = sharedFolderAliasesAliases.length;
                    SharedFolderAliasesAlias[] wsSharedFolderAliasesAliases = new SharedFolderAliasesAlias[aliasSize];
                    for (int j = 0; j < aliasSize; j++) {
                        ISharedFolderAliasesAlias currentAlias = sharedFolderAliasesAliases[j];
                        SharedFolderAliasesAlias wsAlias = new SharedFolderAliasesAlias();
                        wsAlias.setName(currentAlias.getName());
                        wsSharedFolderAliasesAliases[j] = wsAlias;
                    }
                    wsSharedFolderAlias.setAlias(wsSharedFolderAliasesAliases);
                    wsSharedFolderAliases[i] = wsSharedFolderAlias;
                }
            }
            wsAliasList.setAliases(wsSharedFolderAliases);
        }
        result.setAliasList(wsAliasList);
        return result;
    }

}