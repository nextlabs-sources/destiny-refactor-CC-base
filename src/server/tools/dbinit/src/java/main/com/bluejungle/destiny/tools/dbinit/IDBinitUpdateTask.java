/*
 * Created on Dec 19, 2007
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;

/**
 * list all recognized version.
 *
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/IDBinitUpdateTask.java#1 $
 */

public interface IDBinitUpdateTask {
    IVersion VERSION_1_6     = new VersionDefaultImpl(1, 6, 0, 0, 0);
    IVersion VERSION_2_0     = new VersionDefaultImpl(2, 0, 0, 0, 0);
    IVersion VERSION_2_5     = new VersionDefaultImpl(2, 5, 0, 0, 0);
    IVersion VERSION_3_0     = new VersionDefaultImpl(3, 0, 0, 0, 0);
    IVersion VERSION_3_1     = new VersionDefaultImpl(3, 1, 0, 0, 0);
    IVersion VERSION_3_5     = new VersionDefaultImpl(3, 5, 0, 0, 0);
    IVersion VERSION_3_5_1   = new VersionDefaultImpl(3, 5, 1, 0, 0);
    IVersion VERSION_3_6     = new VersionDefaultImpl(3, 6, 0, 0, 0);
    IVersion VERSION_4_0     = new VersionDefaultImpl(4, 0, 0, 0, 0);
    IVersion VERSION_4_5     = new VersionDefaultImpl(4, 5, 0, 0, 0);
    IVersion VERSION_4_6     = new VersionDefaultImpl(4, 6, 0, 0, 0);
    IVersion VERSION_5_0     = new VersionDefaultImpl(5, 0, 0, 0, 0);
    IVersion VERSION_5_1     = new VersionDefaultImpl(5, 1, 0, 0, 0);
    IVersion VERSION_5_0_3   = new VersionDefaultImpl(5, 0, 3, 0, 0);
    IVersion VERSION_5_5_0   = new VersionDefaultImpl(5, 5, 0, 0, 0);
    IVersion VERSION_5_5_1   = new VersionDefaultImpl(5, 5, 1, 0, 0);
    IVersion VERSION_5_6_0   = new VersionDefaultImpl(5, 6, 0, 0, 0);
    IVersion VERSION_6_0_0   = new VersionDefaultImpl(6, 0, 0, 0, 0);
    IVersion VERSION_6_0_1   = new VersionDefaultImpl(6, 0, 1, 0, 0);
    IVersion VERSION_6_5_0   = new VersionDefaultImpl(6, 5, 0, 0, 0);
    IVersion VERSION_6_5_1   = new VersionDefaultImpl(6, 5, 1, 0, 0);
    IVersion VERSION_6_5_1_1 = new VersionDefaultImpl(6, 5, 1, 1, 0);
    IVersion VERSION_6_5_1_2 = new VersionDefaultImpl(6, 5, 1, 2, 0);
    IVersion VERSION_6_5_3   = new VersionDefaultImpl(6, 5, 3, 0, 0);
    IVersion VERSION_6_5_4   = new VersionDefaultImpl(6, 5, 4, 0, 0);
    IVersion VERSION_6_5_4_1 = new VersionDefaultImpl(6, 5, 4, 1, 0);
    IVersion VERSION_6_5_5   = new VersionDefaultImpl(6, 5, 5, 0, 0);
    IVersion VERSION_7_0_0   = new VersionDefaultImpl(7, 0, 0, 0, 0);
    IVersion VERSION_7_5_0   = new VersionDefaultImpl(7, 5, 0, 0, 0);
    IVersion VERSION_7_5_1   = new VersionDefaultImpl(7, 5, 1, 0, 0);
    IVersion VERSION_7_6_0   = new VersionDefaultImpl(7, 6, 0, 0, 0);
    
    IVersion[] ALL_KNOWN_VERSION = new IVersion[] {
        VERSION_1_6
        , VERSION_2_0
        , VERSION_2_5
        , VERSION_3_0
        , VERSION_3_1
        , VERSION_3_5
        , VERSION_3_5_1
        , VERSION_3_6
        , VERSION_4_0
        , VERSION_4_5
        , VERSION_4_6
        , VERSION_5_0
        , VERSION_5_1
        , VERSION_5_0_3
        , VERSION_5_5_0
        , VERSION_5_5_1
        , VERSION_5_6_0
        , VERSION_6_0_0
        , VERSION_6_0_1
        , VERSION_6_5_0
        , VERSION_6_5_1
        , VERSION_6_5_1_1
        , VERSION_6_5_1_2
        , VERSION_6_5_3
        , VERSION_6_5_4
        , VERSION_6_5_4_1
        , VERSION_6_5_5
        , VERSION_7_0_0
        , VERSION_7_5_0
		, VERSION_7_5_1
		, VERSION_7_6_0
    };
}
