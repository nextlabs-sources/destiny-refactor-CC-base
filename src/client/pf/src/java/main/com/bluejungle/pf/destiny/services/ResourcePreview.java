package com.bluejungle.pf.destiny.services;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/destiny/services/ResourcePreview.java#1 $
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collection;

/**
 * Provides information for previewing resource definitions.
 * Use instances of this class to filter files using the
 * directory listing APIs of the <code>File</code> class.
 * 
 * Callers obtain instances of <code>ResourcePreview</code> from
 * the <code>PolicyEditorClient.getResourcePreview</code> method.
 * The callers then call <code>tryAllNetworkRoots</code> and
 * <code>tryAllLocalRoots</code> to see if the <code>Collection</code>
 * returned from <code>getRoots</code> is complete. If both methods
 * return <code>false</code>, then only the roots returned from
 * <code>getRoots</code> need to be checked. If one or both methods
 * return <code>true</code>, then the corresponding root collections
 * need to be tried in addition to the result of <code>getRoots</code>.
 * 
 * When a directory is encountered during the search, the client need to
 * call <code>isProbableRoot</code> to determine whether or not the
 * directory and its subdirectories are to be explored.
 * 
 * @see java.io.FileFilter
 * @see java.io.FilenameFilter
 */
public interface ResourcePreview extends FileFilter, FilenameFilter {
    /**
     * Determines whether the resource definition is applicable
     * to all network roots.
     * @return true if the resource definition is applicable to all
     * network roots; returns false otherwise.
     */
    boolean tryAllNetworkRoots();

    /**
     * Determines whether the resource definition is applicable
     * to all local roots.
     * @return true if the resource definition is applicable to all
     * local roots; false otherwise.
     */
    boolean tryAllLocalRoots();

    /**
     * Determines whether the specified directory or any of its
     * subdirectories may contain possible matches for the resource preview.
     * @param dir the directory to be tested for potential matches.
     * @return true if the specified directory may contain resources
     * corresponding to the resource definition; false otehrwise.
     */
    boolean isProbableRoot( File dir );

    /**
     * Determines a <code>Collection</code> of network and local roots
     * to check for building the resource preview.
     * Each element of the return value must be checked to see if it is
     * a file or a directory. If it is a file, it should be checked for
     * inclusion by calling the accept(File f) method. If it is a directory,
     * the caller should perform a listing of that directory with the
     * <code>ResourcePreview</code> object as the filter.
     * @return a <code>Collection</code> of network and local roots
     * to check for building the resource preview.
     */
    Collection<String> getRoots();
}
