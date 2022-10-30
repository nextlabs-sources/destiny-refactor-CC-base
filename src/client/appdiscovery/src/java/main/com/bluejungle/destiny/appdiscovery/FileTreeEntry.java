/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/appdiscovery/src/java/main/com/bluejungle/destiny/appdiscovery/FileTreeEntry.java#1 $
 */

package com.bluejungle.destiny.appdiscovery;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;

/**
 * @author sergey
 *
 */
public class FileTreeEntry implements TreeEntry, Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean isDirectory;

    private final String path;
    private final String name;

    public FileTreeEntry( File f ) {
        isDirectory = f.isDirectory();
        String tmp = f.getAbsolutePath();
        if ( isDirectory && tmp.charAt(tmp.length()-1) != File.separatorChar ) {
            tmp += File.separatorChar;
        }
        path = tmp;
        name = f.getName();
    }

    public TreeEntry[] getChildren(FileFilter ff) {
        if (isDirectory) {
            File[] files = new File(path).listFiles(ff);
            if (files == null) {
                return new TreeEntry[0];
            }
            TreeEntry[] res = new TreeEntry[files.length];
            for ( int i = 0 ; i != files.length ; i++ ) {
                res[i] = new FileTreeEntry(files[i]);
            }
            return res;
        } else {
            return new TreeEntry[0];
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasChildren() {
        return isDirectory;
    }

    public String getPath() {
        return path;
    }

    public boolean isParentOf( TreeEntry entry ) {
        if ( entry instanceof FileTreeEntry ) {
            String otherPath = ((FileTreeEntry)entry).path;
            return otherPath.length() > path.length()
                && otherPath.startsWith(path);
        } else {
            return false;
        }
    }

    public TreeEntry getParent() {
        File parent = new File(path).getParentFile();
        if ( parent != null ) {
            return new FileTreeEntry(parent);
        } else {
            return null;
        }
    }

    public int hashCode() {
        return path.hashCode();
    }

    public boolean equals(Object other) {
        if ( other instanceof FileTreeEntry ) {
            return ((FileTreeEntry)other).path.equals(path);
        } else {
            return false;
        }
    }

    public String toString() {
        return "["+path+"]";
    }

}
