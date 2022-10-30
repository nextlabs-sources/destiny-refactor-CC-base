/*
 * Created on Jul 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/SubjectCache.java#1 $:
 */

final class SubjectCache {

    private final Cache cache;
    private final int numPolicies;

    SubjectCache(int numPolicies) {

        this.numPolicies = numPolicies;

        CacheManager manager;

        try {
            manager = CacheManager.create();
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
        Cache thisCache = manager.getCache(SubjectCache.class.toString());
        if (thisCache == null) {
            // XXX fix the hard-coded values
            thisCache = new Cache(SubjectCache.class.toString(), 20000, false, false, 60, 60);
            try {
                manager.addCache(thisCache);
            } catch (CacheException e1) {
                throw new RuntimeException(e1);
            }
        } else {
            try {
                thisCache.removeAll();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }            
        }
        cache = thisCache;

    }

    void setMatches(Serializable key, int policyOrdinal, boolean match) {
        if (policyOrdinal >= numPolicies) {
            return;
        }

        synchronized (cache) {
            Element elem;
            try {
                elem = cache.get(key);
            } catch (CacheException e) {
                // no biggy
                return;
            }
            Entry entry;
            if (elem == null) {
                entry = new Entry(numPolicies);
                elem = new Element(key, entry);
                cache.put(elem);
            } else {
                entry = (Entry) elem.getValue();
            }
            if (match) {
                entry.matches.set(policyOrdinal);
            } else {
                entry.nonMatches.set(policyOrdinal);
            }
        }
    }

    Boolean match(Serializable key, int policyOrdinal) {
        if (policyOrdinal >= numPolicies) {
            return null;
        }

        synchronized (cache) {
            Element elem;
            try {
                elem = cache.get(key);
            } catch (CacheException e) {
                // no biggy -- no caching
                return null;
            }
            if (elem == null) {
                return null;
            }
            Entry entry = (Entry) elem.getValue();
            if (entry.matches.get(policyOrdinal)) {
                return Boolean.TRUE;
            }
            if (entry.nonMatches.get(policyOrdinal)) {
                return Boolean.FALSE;
            }
            return null;
        }
    }

    private static final class Entry implements Serializable {
        private final BitSet matches;
        private final BitSet nonMatches;

        Entry(int numPolicies) {
            matches = new BitSet(numPolicies);
            nonMatches = new BitSet(numPolicies);
        }
    }

}
