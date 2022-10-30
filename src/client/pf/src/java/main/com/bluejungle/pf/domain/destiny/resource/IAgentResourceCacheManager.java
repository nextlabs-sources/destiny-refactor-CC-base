/*
 * Created on Jul 08, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/resource/IAgentResourceCacheManager.java#1 $:
 */

package com.bluejungle.pf.domain.destiny.resource;

import java.io.Serializable;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public interface IAgentResourceCacheManager {
    public IAgentResourceCacheManager DUMMY_CACHE = new IAgentResourceCacheManager() {
        public Element get(Serializable id) {
            return null;
        }

        public void remove(Serializable id) {
            return;
        }

        public void put(Element element) {
            return;
        }

        public void clearCache() {
            return;
        }

        public void dispose() {
            return;
        }

        public void spoolAllToDisk() {
            return;
        }
    };

    /**
     * Get an element with this id from the cache
     *
     * @param id the id
     * @return the element, or null if not found
     */
    Element get(Serializable id);

    /**
     * Remove the element with this id from the cache
     *
     * @param id the id
     */
    void remove(Serializable id);

    /**
     * Add this element to the cache
     *
     * @param element the element
     */
    void put(Element element);

    /**
     * Remove all items from cache and remove stored cache on disk
     */
    void clearCache();

    /**
     * Prepare for shutdown. Save all information to disk and stop timer threads, etc
     */
    void dispose();

    /**
     * Save cache to disk
     */
    void spoolAllToDisk();
}
