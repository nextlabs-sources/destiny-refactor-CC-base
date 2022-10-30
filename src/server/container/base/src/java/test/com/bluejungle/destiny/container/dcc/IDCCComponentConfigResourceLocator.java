/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.io.InputStream;

/**
 * A Config File Locator.  This should eventually be moved into product
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/test/com/bluejungle/destiny/container/dcc/IDCCComponentConfigResourceLocator.java#1 $
 */
public interface IDCCComponentConfigResourceLocator {
    public InputStream getConfigResourceAsStream(String configResourceName);
}
