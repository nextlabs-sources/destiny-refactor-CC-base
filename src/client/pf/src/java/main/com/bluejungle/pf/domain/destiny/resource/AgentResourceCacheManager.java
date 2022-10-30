/*
 * Created on Nov 05, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/resource/AgentResourceCacheManager.java#1 $:
 */

package com.bluejungle.pf.domain.destiny.resource;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.DiskStore;
import net.sf.ehcache.store.Store;

import com.bluejungle.domain.agenttype.AgentTypeEnumType;

class AgentResourceCacheManager implements IAgentResourceCacheManager {
    private CacheManager cacheManager;
    private Cache cache;
    private DiskStore diskStore;
    private String diskStoreLocation;
    private long diskCacheFlushFrequency;
    private Timer spoolTimer;
    private Log log;

    private static final String DISK_CACHE_FILE_PREFIX = "evalcache";

    public AgentResourceCacheManager(AgentTypeEnumType agentType, long diskCacheFlushFrequency, String diskStoreLocation, int diskCacheSize) {
        log = LogFactory.getLog(AgentResourceCacheManager.class.getName());

        this.diskCacheFlushFrequency = diskCacheFlushFrequency;
        this.diskStoreLocation = diskStoreLocation;

        try {
            cacheManager = CacheManager.create();
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
        cache = cacheManager.getCache(AgentResourceManager.class.getName());
        if (cache == null) {
            cache = new Cache(AgentResourceCacheManager.class.getName(),
                              diskCacheSize,  
                              false,     // boolean overflowToDisk required since we set the diskPersistent to true
                              true,      // boolean eternal,
                              0,         // long timeToLiveSeconds, 0 means never expired
                              0);        // long timeToIdleSeconds, 0 means never expired
            try {
                cacheManager.addCache(cache);
            } catch (CacheException e1) {
                throw new RuntimeException(e1);
            }
        }
        // init diskStore
        if (diskStore == null) {
            initDiskStore(true);
            
            if (diskStore != null) {
                spoolTimer = new Timer(true);
                spoolTimer.schedule(new TimerTask(){
                    @Override
                        public void run() {
                        long startTime = System.currentTimeMillis();
                        spoolAllToDisk();
                        log.trace("spoolAllToDisk takes " + (System.currentTimeMillis() - startTime)
                                  + " ms");
                    }
      
                }, diskCacheFlushFrequency, diskCacheFlushFrequency);
            }
        }
    }

    // Exposed for unit tests
    CacheManager getCacheManager(){
        return cacheManager;
    }

    private void initDiskStore(boolean loadDataFromDiskToMemory) {
        try {
            diskStore = initDiskStore(diskStoreLocation, false);
            if (loadDataFromDiskToMemory) {
                loadFromDiskToCacheAndCloseDisk(diskStore, cache);
            }
        } catch (IOException e) {
            log.warn("Fail to load diskstore cache. Create new data and index file");
            //the diskstore is corrupted, delete and create a new one
            try {
                diskStore = initDiskStore(diskStoreLocation, true);
                if (loadDataFromDiskToMemory) {
                    loadFromDiskToCacheAndCloseDisk(diskStore, cache);
                }
            } catch (IOException e1) {
                log.error("Fail to create diskstore cache, diskStore is disabled.");
                diskStore =  null;
            }
        }
    }

    private DiskStore initDiskStore(String diskStoreLocation, boolean initNewDataFiles) throws IOException{
  
        log.trace("Create disk store at " + diskStoreLocation);
  
        //create a dummy cache object for diskstore
        Cache dummyCache = new Cache(DISK_CACHE_FILE_PREFIX, //String name, will be the prefix of the filename
                                     cache.getMaxElementsInMemory(),  // int maximumSize, doesn't matter in this dummy cache
                                     false,    // boolean overflowToDisk, //required since we set the diskPersistent to true
                                     true,    // boolean eternal,
                                     0,     // long timeToLiveSeconds, // 0 means never expired
                                     0,     // long timeToIdleSeconds, // 0 means never expired
                                     true,   // we are not using disk in this cache, but we need this to enable the diskcache. 
                                     3600);   // diskExpiryThreadIntervalSeconds
        
        if (initNewDataFiles) {
            File file = new File(diskStoreLocation, cache.getName() + ".data");
            if (!file.delete()) {
                log.error("can't delete" + file.getAbsolutePath());
            }
            file = new File(diskStoreLocation, cache.getName() + ".index");
            if (!file.delete()) {
                log.error("can't delete" + file.getAbsolutePath());
            }
        }
  
        return new DiskStore(dummyCache, diskStoreLocation);
    }

    private void loadFromDiskToCacheAndCloseDisk(DiskStore diskStore, Cache cache) throws IOException {
        //load everything to Memory
        log.trace("load " + diskStore.getSize() + " items from diskStore cache to memory");
        try{
            if (diskStore.getSize() > 0) {
                boolean success = true;
                try {
                    //the index file store all the keys
                    //if the index is corrupted, it will create a new one
                    // the data will not be cleared. The data is going to overwrite on it.
                    for (Object key : diskStore.getKeyArray()) {
      
                        Element e = diskStore.get((Serializable) key);
                        if (e == null) {
                            success = false;
                            break;
                        }else{
                            cache.putQuiet(e);
                        }
                    }
                    //nobody is really throwing IOException
                    //the diskStore.get have a full try/catch block and log the exception instead of throwing
                } catch (IOException e) {
                    log.error(e);
                }
                if (!success) {
                    // it is the .data file corrupted not the index file,
                    throw new IOException("the diskStore is not readable, possibly corrupted.");
                }
            }
        } finally {
            diskStore.dispose();
        }
    }

    public void clearCache() {
        try {
            cache.removeAll();
            spoolAllToDisk();
        } catch (IOException e) {
            throw new RuntimeException(e);
            // TODO: recover by creating new cache
        }
    }

    public synchronized void dispose() {
        log.trace("dispose()");
        spoolTimer.cancel();
        spoolAllToDisk();
        try {
            cacheManager.shutdown();
        } catch (NullPointerException expected) {
            //it throws NullPointer because cache doesn't have diskstore;
        }
        if(diskStore != null){
            if (diskStore.getStatus() == Store.STATUS_ALIVE) {
                diskStore.dispose();
            }
        }
        diskStore = null;
        cache = null;
            
    }
    

    public void spoolAllToDisk() {
        if(diskStore != null){
            try {
                // synchronized the diskStore, make sure two things touch the diskStore at the same time
                synchronized (diskStore) {
                    initDiskStore(false);
                    diskStore.removeAll();
                    if (cache.getSize() > 0) {
                        List<? extends Serializable> keys = cache.getKeys();
                        log.trace("spooling " + keys.size() + " items to the disk");
                        for (Serializable key : keys) {
                            Element e = cache.getQuiet(key);
                            //the value could be null when the cache is removed after we get the key
                            if (e != null) {
                                diskStore.put(e);
                            }
                        }
                    }
                    diskStore.dispose();
                }
            } catch (IOException e) {
                log.error(e);
            } catch (CacheException e) {
                log.error(e);
            }
        }
    }


    public void put(Element e) {
        cache.put(e);
    }

    public Element get(Serializable id) {
        Element element = null;
        try {
            element = cache.get(id);
        } catch (CacheException e) {
            if (log.isWarnEnabled()) {
                log.warn("Error accessing resource cache", e);
            }
            // no biggy, just no caching
        }
        
        return element;
    }

    public void remove(Serializable id) {
        cache.remove(id);
    }
}
