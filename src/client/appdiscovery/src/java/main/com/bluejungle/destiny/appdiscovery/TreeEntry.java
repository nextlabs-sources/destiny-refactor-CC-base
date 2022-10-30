/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/appdiscovery/src/java/main/com/bluejungle/destiny/appdiscovery/TreeEntry.java#1 $
 */

package com.bluejungle.destiny.appdiscovery;

import java.io.FileFilter;

/**
 * This interface defines elements that go into trees of files.
 * Each implementation encapsulates a file or a file system root.
 */

public interface TreeEntry {
    TreeEntry[] getChildren(FileFilter ff);
    boolean hasChildren();
    String getPath();
    String getName();
    TreeEntry getParent();
    boolean isParentOf( TreeEntry entry );
}
