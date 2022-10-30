package com.bluejungle.pf.domain.destiny.resource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.domain.destiny.obligation.CommandExecutorHelper;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;

//All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc,
// Redwood City CA,
//Ownership remains with Blue Jungle Inc, All rights reserved worldwide.

/**
 * TODO Write file summary here.
 * 
 * @author pkeni
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/AgentResourceManagerTest.java#3 $
 */

/**
 * @author pkeni
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AgentResourceManagerTest extends TestCase {
    private static final String BASE_DIR_PROPERTY_NAME = "build.root.dir";
    private static final String RELATIVE_CLIENT_KEYSTORE_FILE_NAME = "/run/server/certificates/agent-keystore.jks";
    private static final String RELATIVE_CLIENT_TRUSTSTORE_FILE_NAME = "/run/server/certificates/agent-truststore.jks";

    private AgentResourceManager rm;
 
    public static void main(String[] args) throws IOException, InvalidBundleException {
        junit.textui.TestRunner.run(AgentResourceManagerTest.class);
    }
 
    private static final ComponentInfo RM_INFO;
 
    static {
        HashMapConfiguration configurtion = new HashMapConfiguration();
        configurtion.setProperty(AgentResourceManager.AGENT_TYPE_CONFIG_PARAM,
                                 AgentTypeEnumType.DESKTOP);
  
        RM_INFO = new ComponentInfo(
            AgentResourceManager.class.getName(),
        AgentResourceManager.class.getName(), 
        AgentResourceManager.class.getName(),
        LifestyleType.SINGLETON_TYPE,
        configurtion);
    }
 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearCacheFolder();
    }

    @Override
    public void tearDown() throws Exception {
        clearCacheFolder();
    }


    protected void clearCacheFolder() {
        File cacheFolder = new File(System.getProperty("user.dir"));
        File[] files = cacheFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".data") || name.endsWith(".index");
            }
        });
        if (files != null) {
            for (File cacheFile : files) {
                if(cacheFile.isFile()){
                    assertTrue("I can't delete " + cacheFile.getAbsolutePath(), cacheFile.delete());
                }
            }
        }
    }
 
    public final void testCache() throws IOException, InvalidBundleException,
                                         IllegalStateException, CacheException {
        //  if(true) return;
        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        manager.getComponent(CommandExecutorHelper.class);
        rm = (AgentResourceManager) manager.getComponent(RM_INFO);

        final List<File> testFiles = new LinkedList<File>();
        final int numberOfSaveOjbects = 10;
        final long lastModifiedDate = 20000000000l;
        final String lastMOdifiedAttrStr = "modified_date";
        for (int i = 0; i < numberOfSaveOjbects; i++) {
            File f = new File("C:\\temp\\resource" + i + ".test");
            f.createNewFile();
            f.setLastModified(lastModifiedDate + i);
            testFiles.add(f);
        }

        IAgentResourceCacheManager cm = rm.getAgentResourceCacheManager();
        Cache cache = (cm instanceof AgentResourceCacheManager) ? ((AgentResourceCacheManager)cm).getCacheManager().getCache(AgentResourceManager.class.getName()) : null;

        final boolean isUsingDiskStoreWithMemoryStore = false;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(10000000000l);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            RelationOp op = RelationOp.getElement(">");
            ResourceAttribute ra = ResourceAttribute.MODIFIED_DATE;
            IRelation rel = ra.buildRelation(op, formatter.format(cal.getTime()));

            int cacheSize = 0;
            int cacheHitCount = 0;
            int cacheMissCountNotFound = 0;

            int cacheDiskStoreSize = 0;
            int cacheDiskStoreHitCount = 0;

            int cacheMemoryStoreSize = 0;
            int cacheMemoryStoreHitCount = 0;

            //before everything
            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            //put some file into the cache
            for (int i = 0; i < testFiles.size(); i++) {
                IResource res = rm.getResource(testFiles.get(i).getAbsolutePath(), null,
                                               AgentTypeEnumType.DESKTOP).getResource();
                assertEquals(lastModifiedDate + i, ((EvalValue) res
                                                    .getAttribute(lastMOdifiedAttrStr)).getValue());
            }

            cacheSize += numberOfSaveOjbects;
            cacheMemoryStoreSize += numberOfSaveOjbects;
            cacheMissCountNotFound += numberOfSaveOjbects;

            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            //close the cache
            long startTime = System.currentTimeMillis();
            rm.dispose();
            System.out.println("shutdown takes: " + (System.currentTimeMillis() - startTime));

            //reopen the cache
            rm.init();

            cm = rm.getAgentResourceCacheManager();
            cache = (cm instanceof AgentResourceCacheManager) ? ((AgentResourceCacheManager)cm).getCacheManager().getCache(AgentResourceManager.class.getName()) : null;

            cacheSize = numberOfSaveOjbects;
            //bug in echache, the counter didn't get reset since the cache object is still in memroy after shutdown
            cacheHitCount = 0;
            cacheMissCountNotFound = 0;
            cacheMemoryStoreHitCount = 0;


            cacheDiskStoreSize = isUsingDiskStoreWithMemoryStore ? numberOfSaveOjbects : 0;
            cacheDiskStoreHitCount = 0;

            cacheMemoryStoreSize = numberOfSaveOjbects;

            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            // get the same thing from the cache
            for (int i = 0; i < testFiles.size(); i++) {
                IResource res = rm.getResource(testFiles.get(i).getAbsolutePath(), null,
                                               AgentTypeEnumType.DESKTOP).getResource();
                assertEquals(lastModifiedDate + i, ((EvalValue) res
                                                    .getAttribute(lastMOdifiedAttrStr)).getValue());
            }
            cacheHitCount += numberOfSaveOjbects;
            cacheMemoryStoreHitCount += numberOfSaveOjbects;
            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            //get the same data again
            for (int i = 0; i < testFiles.size(); i++) {
                IResource res = rm.getResource(testFiles.get(i).getAbsolutePath(), null,
                                               AgentTypeEnumType.DESKTOP).getResource();
                assertEquals(lastModifiedDate + i, ((EvalValue) res
                                                    .getAttribute(lastMOdifiedAttrStr)).getValue());
            }
            cacheHitCount += numberOfSaveOjbects;
            cacheMemoryStoreHitCount += numberOfSaveOjbects;
            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            //change the first file
            File testFile = testFiles.get(0);

            final long magicModifiedDate = 12345679;
            testFile.setLastModified(magicModifiedDate);
            for (int i = 0; i < testFiles.size(); i++) {
                IResource res = rm.getResource(testFiles.get(i).getAbsolutePath(), null,
                                               AgentTypeEnumType.DESKTOP).getResource();
                long expected = i == 0 ? magicModifiedDate : lastModifiedDate + i;
                assertEquals(expected, ((EvalValue) res.getAttribute(lastMOdifiedAttrStr))
                             .getValue());
            }

            cacheHitCount += numberOfSaveOjbects;
            cacheMemoryStoreHitCount += numberOfSaveOjbects;
            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            for (int i = 0; i < testFiles.size(); i++) {
                IResource res = rm.getResource(testFiles.get(i).getAbsolutePath(), null,
                                               AgentTypeEnumType.DESKTOP).getResource();
                long expected = i == 0 ? magicModifiedDate : lastModifiedDate + i;
                assertEquals(expected, ((EvalValue) res.getAttribute(lastMOdifiedAttrStr))
                             .getValue());
            }

            cacheHitCount += numberOfSaveOjbects;
            cacheMemoryStoreHitCount += numberOfSaveOjbects;
            checkCacheSatistics(cache, cacheSize, cacheHitCount, cacheMissCountNotFound,
                                cacheMemoryStoreSize, cacheMemoryStoreHitCount, cacheDiskStoreSize,
                                cacheDiskStoreHitCount);

            if (rm != null) {
                rm.dispose();
            }
        } finally {
            if (rm != null && manager != null) {
                manager.releaseComponent(rm);
            }
            for (File testFile : testFiles) {
                testFile.delete();
            }
        }
    }
 
    //TODO test corrupted index file,
    //TODO test corrupted date file, 
    //TODO test corrupted index and data file, 
 
    public void testCacheLongRun() throws IOException, CacheException{
        final long indexFileEmptySize = 136;
        File cacheFolder = new File(System.getProperty("user.dir"));
        File dataFile = new File(cacheFolder, "evalcache.data");
        File indexFile = new File(cacheFolder, "evalcache.index");
        clearCacheFolder();
        assertFalse(dataFile.exists());
        assertFalse(indexFile.exists());

        IComponentManager manager = ComponentManagerFactory.getComponentManager();

  
        manager.getComponent(CommandExecutorHelper.class);
        rm = (AgentResourceManager) manager.getComponent(RM_INFO);

        final List<File> testFiles = new LinkedList<File>();
        final int numberOfSaveOjbects = 5000;
        final long lastModifiedDate = 20000000000l;
        for (int i = 0; i < numberOfSaveOjbects; i++) {
            File f = new File("C:\\temp\\resourceA" + i + ".test");
            f.createNewFile();
            f.setLastModified(lastModifiedDate + i);
            testFiles.add(f);
        }
  
        assertTrue(dataFile.exists());
        assertTrue(indexFile.exists());
        long dataFileSize = dataFile.length();
        assertEquals(0, dataFileSize);
        long indexFileSize = indexFile.length();
        assertEquals(indexFileEmptySize, indexFileSize);

  
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(10000000000l);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            RelationOp op = RelationOp.getElement(">");
            ResourceAttribute ra = ResourceAttribute.MODIFIED_DATE;
            IRelation rel = ra.buildRelation(op, formatter.format(cal.getTime()));

            // fill 100% capacity
            for (int i = 0; i < 1000; i++) {
                rm.getResource(testFiles.get(i).getAbsolutePath(), null, AgentTypeEnumType.DESKTOP)
                    .getResource();
            }
            rm.spoolAllToDisk();
   
            assertTrue(dataFile.length() > dataFileSize);
            assertTrue(indexFile.length() > indexFileSize);
   
            dataFileSize = dataFile.length();
            indexFileSize = indexFile.length();
   
            //fill 100% capacity multiple times
            for(int j = 1000; j < numberOfSaveOjbects; j += 1000 ){
                for (int i = 0; i < 1000; i++) {
                    rm.getResource(testFiles.get(i + j).getAbsolutePath(), null,
                                   AgentTypeEnumType.DESKTOP).getResource();
                }
                rm.spoolAllToDisk();
                double difference = (double)Math.abs(dataFile.length() - dataFileSize) / dataFileSize;
                assertTrue("The Data file differnece should be less than 5% but it is " + difference,
                           difference < 0.05);
    
                difference = (double)Math.abs(indexFile.length() - indexFileSize) / indexFileSize;
                assertTrue("The Index file differnece should be less than 5% but it is " +difference, 
                           difference < 0.05);
    
                dataFileSize = dataFile.length();
                indexFileSize = indexFile.length();
            }
   
            rm.clearCache();
            assertEquals(0, dataFile.length());
            assertEquals(indexFileEmptySize, indexFile.length());
        } finally {
            if (rm != null && manager != null) {
                manager.releaseComponent(rm);
            }
            if (rm != null) {
                rm.dispose();
            }
            for (File testFile : testFiles) {
                testFile.delete();
            }
        }
    }
 
    public void testCacheSlowlyIncreasing() throws IOException, CacheException{
        final long indexFileEmptySize = 136;
        File cacheFolder = new File(System.getProperty("user.dir"));
        File dataFile = new File(cacheFolder, "evalcache.data");
        File indexFile = new File(cacheFolder, "evalcache.index");
        clearCacheFolder();
        assertFalse(dataFile.exists());
        assertFalse(indexFile.exists());

        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        manager.getComponent(CommandExecutorHelper.class);
        rm = (AgentResourceManager) manager.getComponent(RM_INFO);

        final List<File> testFiles = new LinkedList<File>();
        final int numberOfSaveOjbects = 5000;
        final long lastModifiedDate = 20000000000l;
        for (int i = 0; i < numberOfSaveOjbects; i++) {
            File f = new File("C:\\temp\\resourceA" + i + ".test");
            f.createNewFile();
            f.setLastModified(lastModifiedDate + i);
            testFiles.add(f);
        }
  
        assertTrue(dataFile.exists());
        assertTrue(indexFile.exists());
        assertEquals(0, dataFile.length());
        assertEquals(indexFileEmptySize, indexFile.length());

  
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(10000000000l);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            RelationOp op = RelationOp.getElement(">");
            ResourceAttribute ra = ResourceAttribute.MODIFIED_DATE;
            IRelation rel = ra.buildRelation(op, formatter.format(cal.getTime()));

            //put 10% of the capacity
            for (int i = 0; i < 100; i++) {
                rm.getResource(testFiles.get(i).getAbsolutePath(), null, AgentTypeEnumType.DESKTOP)
                    .getResource();
            }
            int currentInserted = 100;
   
            rm.spoolAllToDisk();
            float averageDataEntrySize = dataFile.length() / currentInserted;
            float averageIndexEntrySize = indexFile.length() / currentInserted;
   
            for(int j = 100; j < 1000; j += 100 ){
                for (int i = 0; i < 100; i++) {
                    rm.getResource(testFiles.get(i + j).getAbsolutePath(), null,
                                   AgentTypeEnumType.DESKTOP).getResource();
                }
                currentInserted += 100;
                rm.spoolAllToDisk();
    
                float currentAverageDataEntrySize = dataFile.length() / currentInserted; 
                float currentAverageIndexEntrySize = indexFile.length() / currentInserted;
    
                float difference = (float) Math.abs(currentAverageDataEntrySize - averageDataEntrySize)
                                   / averageDataEntrySize;
                assertTrue("The Data file differnece should be less than 5% but it is " + difference,
                           difference < 0.05);
    
                difference =(float) Math.abs(currentAverageIndexEntrySize - averageIndexEntrySize)
                            / averageIndexEntrySize;
                assertTrue("The Index file differnece should be less than 5% but it is " +difference, 
                           difference < 0.05);
    
                //update
                averageDataEntrySize = currentAverageDataEntrySize;
                averageIndexEntrySize = currentAverageIndexEntrySize;
            }
   
            rm.clearCache();
            assertEquals(0, dataFile.length());
            assertEquals(indexFileEmptySize, indexFile.length());
        } finally {
            if (rm != null && manager != null) {
                manager.releaseComponent(rm);
            }
            if (rm != null) {
                rm.dispose();
            }
            for (File testFile : testFiles) {
                testFile.delete();
            }
        }
    }

    private void checkCacheSatistics(Cache cache, int cacheSize, int cacheHitCount,
                                     int cacheMissCountNotFound, int cacheMemoryStoreSize, int cacheMemoryStoreHitCount,
                                     int cacheDiskStoreSize, int cacheDiskStoreHitCount) throws CacheException {
        if (cache == null) {
            throw new NullPointerException("cache is null");
        }

        assertEquals(cacheDiskStoreHitCount, cache.getDiskStoreHitCount());
        assertEquals(cacheDiskStoreSize, cache.getDiskStoreSize());
        assertEquals(cacheSize, cache.getSize());
        assertEquals(cacheHitCount, cache.getHitCount());
        assertEquals(cacheMissCountNotFound, cache.getMissCountNotFound());
        assertEquals(cacheMemoryStoreHitCount, cache.getMemoryStoreHitCount());
        assertEquals(cacheMemoryStoreSize, cache.getMemoryStoreSize());
    }
}
