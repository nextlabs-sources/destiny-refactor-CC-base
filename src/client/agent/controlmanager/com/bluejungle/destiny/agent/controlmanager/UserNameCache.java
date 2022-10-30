/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * This class caches user name for for faster retrieval
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/UserNameCache.java#1 $
 */

public class UserNameCache {

    private static final String USERNAME_CACHE_SUFFIX = "UserNameCache";
    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
    private static Log LOG = LogFactory.getLog(UserNameCache.class);

    //userName cache
    private static Cache userNameCache;

    static {
        try {
            final CacheManager cacheManager = CacheManager.create();
            userNameCache = cacheManager.getCache(CMRequestHandler.class.getName() + USERNAME_CACHE_SUFFIX);
            if (userNameCache == null) {
                userNameCache = new Cache(CMRequestHandler.class.getName() + USERNAME_CACHE_SUFFIX, 5000, false, false, 0, 0);
                try {
                    cacheManager.addCache(userNameCache);
                } catch (CacheException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Unable to create username cache", e);
                    }
                }
            }
        } catch (CacheException e) {
            if (LOG.isWarnEnabled()) {
                //no big deal, no caching
                LOG.warn("Unable to create reverse DNS cache, proceeding in uncached mode.", e);
            }
        }
    }

    /**
     * Gets username from cache if it is available, otherwise computes it
     * 
     * @param userId
     *            Sid of user
     * @return username@domain
     */
    public static String getUserName(String userId) {
        if (userId == null) {
            // Uh-oh
            return "";
        }
        Element elem = null;
        try {
            elem = userNameCache.get(userId);
        } catch (IllegalStateException e) {
        } catch (CacheException e) {
        }

        if (elem == null) {
            String userName = osWrapper.getUserName(userId);
            if (userName == null || userName.equals("")) {
                userName = userId;
            }
            LOG.trace("Add Username to cache: " + userName);
            elem = new Element(userId, userName);
            userNameCache.put(elem);
            return userName;
        }
        return (String) elem.getValue();
    }

}
