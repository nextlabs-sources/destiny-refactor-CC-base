/*
 * Created on Jul 24, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/Formatter.java#1 $
 */

public interface Formatter<T> {
    /**
     * convert <code>t</code> to a string 
     * this method must handle null object
     * @param t
     * @return
     */
    String toString(T t);
}
