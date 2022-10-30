/*
 * Created on Mar 5, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.File;

import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/IExternalApplicationLoader.java#1 $
 */

public interface IExternalApplicationLoader {
    PropertyKey<File[]> APP_FOLDERS_KEY = new PropertyKey<File[]>("customAppFolders");
}
