/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.framework.environment.IResourceLocator;

/**
 * This interface extends the IResourceLocator interface and requires the
 * implementor to associate a fully-qualified name with the relative path/name.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/INamedResourceLocator.java#2 $
 */

public interface INamedResourceLocator extends IResourceLocator {

    /**
     * Returns a fully-qualified name of the initialized resource. If the
     * 'relativePath' param is null, it should return the fully qualified name
     * of the root.
     * 
     * @return
     */
    public String getFullyQualifiedName(String relativePath);
}