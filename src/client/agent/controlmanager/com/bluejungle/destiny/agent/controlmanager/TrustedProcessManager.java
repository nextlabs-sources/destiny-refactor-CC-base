/*
 * Created on Mar 22, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TrustedProcessManager implements ITrustedProcessManager, IInitializable {
    public static final ComponentInfo<ITrustedProcessManager> COMP_INFO = 
        new ComponentInfo<ITrustedProcessManager>(
            TrustedProcessManager.class.getName(), 
            TrustedProcessManager.class, 
            ITrustedProcessManager.class, 
            LifestyleType.SINGLETON_TYPE);
    
    private static final Log LOG = LogFactory.getLog(TrustedProcessManager.class);

    /*
     * trusted cache 
     */
    private Cache cache;
    
    /**
     * pid to path cache
     */
    private Cache pidToNameCache;

    /**
     * permanent trusted cache
     */
    private Set<Integer> permanentProcesses;
    
    private IControlManager controlManager;
    private IAgentPolicyContainer policyContainer = null;
    private IOSWrapper osWrapper;

    /**
     * @see ITrustedProcess#isTrustedProcess()
     */
    public boolean isTrustedProcess(int processID) {
        if (permanentProcesses.contains(processID)) {
            LOG.debug("Process " + processID + " is a permanent trusted process");
            return true;
        }
        
        // if the cache is disabled, we trust everything
        if (cache == null || pidToNameCache == null) {
            LOG.debug("cache is diabled, we trust everything.");
            return true;
        }
        
        try {
            if (cache.get(processID) != null) {
                return true;
            }
        } catch (CacheException ex) {
            LOG.error("Can't read cache.", ex);
        }
        
        String appName = null;
        
        // bug 12884, hardcode the process ID, 4, to the application name "[System]"
        if (processID == 4 ) {
            appName = "[System]";
        } else {
            try {
                Element element = pidToNameCache.get(processID);
                if (element != null ) {
                    appName = (String)element.getValue();
                }
            } catch (CacheException e) {
                LOG.error("Can't read cache.", e);
            }
            
            if (appName == null) {
                LOG.trace("Process " + processID + " is not cached.");
                appName = osWrapper.getPathFromPID(processID);
                if (appName == null) {
                    LOG.warn("Can't find the path of process " + processID);
                    appName = "";
                }
                
                pidToNameCache.put(new Element(processID, appName));
            }
        }
        
        synchronized (this) {
            // We get policyContainer here to avoid a nasty loop.  AgentPolicyContainer
            // depends on ServiceProviderManager, which depends on various services, one
            // of which (key sevice) depends on us.
            if (policyContainer == null) {
                IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
                policyContainer = (IAgentPolicyContainer) compMgr.getComponent(IAgentPolicyContainer.COMP_INFO);
            }
        }

        boolean isTrusted = policyContainer.isApplicationTrusted(appName);
        
        LOG.debug("Process " + processID + " (" + appName + ") is a "
                + (isTrusted ? "trusted" : "untrusted") + " process");

        return isTrusted;
    }

    /**
     * @see ITrustedProcess#addPermanentTrustedProcess()
     */
    public void addPermanentTrustedProcess(int processID) {
        LOG.debug("Adding process " + processID + " to permanent trusted process cache\n");
        permanentProcesses.add(processID);
    }

    /**
     * @see ITrustedProcess#addTrustedProcess()
     */
    public void addTrustedProcess(int processID) {
        LOG.debug("Adding process " + processID + " to trusted process cache\n");
        if (cache != null) {
            addElement(cache, processID);
        } else {
            LOG.info("No trusted process cache - ignoring addTrustedProcess()");
        }
    }
    
    private void addElement(Cache cache, int processID) {
        Element existing = null;
        try {
            existing = cache.get(processID);
        } catch (CacheException ex) {
            LOG.error("Can't read cache.", ex);
        }
        if (existing != null) {
            existing.setCreateTime();
        } else {
            cache.put(new Element(processID, null));
        }
    }


    /**
     * @see IInitializable#init()
     */
    @SuppressWarnings("deprecation")
    public void init() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        
        controlManager = (IControlManager) compMgr.getComponent(IControlManager.NAME);
        int ttl = controlManager.getTrustedProcessTTL();

        // A ttl of < 0 means that we never want any caches at all
        if (ttl >= 0) {
            CacheManager cacheManager = null;

            try {
                cacheManager = CacheManager.create();
                cache = createCache(cacheManager, "_trusted", ttl);
                pidToNameCache = createCache(cacheManager, "_pid2name", ttl);
            } catch (CacheException e) {
                LOG.error("Unable to create cacheManager");
            }
        }

        // Store the set of processes that are trusted forever
        permanentProcesses = Collections.synchronizedSet(new HashSet<Integer>());
        
        osWrapper = (OSWrapper) compMgr.getComponent(OSWrapper.NAME);
    }
    
    private Cache createCache(CacheManager cacheManager, String suffix, int ttl){
        if (cacheManager == null) {
            LOG.debug("cache Manager is null, I can't create cache, "
                    + TrustedProcessManager.class.getName() + suffix);
            return null;
        }
        
        Cache cache = cacheManager.getCache(TrustedProcessManager.class.getName() + suffix);
        if (cache != null) {
            LOG.trace("cache '" + TrustedProcessManager.class.getName() + suffix
                    + "' is already created.");
            return cache;
        }
        
        cache = new Cache(
                TrustedProcessManager.class.getName()  + suffix // name
              , 1000                                  // maximumSize
              , false                                 // overflowToDisk
              , false                                 // eternal
              , ttl                                   // timeToLiveSeconds
              , 0                                     // timeToIdleSeconds, 0 means disabled
        );
        
        try {
            cacheManager.addCache(cache);
            return cache;
        } catch (CacheException e) {
            LOG.error("Unable to create trusted process cache.  Proceeding uncached");
        }
        return null;
    }
}
