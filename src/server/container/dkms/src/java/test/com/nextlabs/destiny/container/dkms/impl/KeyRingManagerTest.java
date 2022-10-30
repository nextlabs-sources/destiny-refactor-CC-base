package com.nextlabs.destiny.container.dkms.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.StaleObjectStateException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.dkms.DKMSComponentTestBase;
import com.nextlabs.destiny.container.dkms.IKey;
import com.nextlabs.destiny.container.dkms.IKeyRing;
import com.nextlabs.destiny.container.dkms.IKeyRingManager;
import com.nextlabs.destiny.container.dkms.KeyManagementException;

public class KeyRingManagerTest extends DKMSComponentTestBase {

    private IKeyRingManager keyRingManager;
    
    protected void deleteOldDatabase() throws HibernateException, SQLException {
        IHibernateRepository repository = initDataSource();
        Session s = repository.openSession();
        try {
            Connection c = s.connection();
            Statement statement = c.createStatement();
            statement.execute("DROP TABLE km_keyring");
            statement.close();
        } finally {
            s.close();
        }
    }
    
    @Before
    public void init() throws HibernateException {
        super.initAll();
        initDataSource();
        keyRingManager = ComponentManagerFactory.getComponentManager().getComponent(KeyRingManager.class);
    }
    
    protected void deleteAll() throws KeyManagementException {
        for(String name : keyRingManager.getKeyRings()){
            assertTrue(keyRingManager.deleteKeyRing(name));
        }
        assertEquals(0, keyRingManager.getKeyRings().size());
    }
    
    @Test 
    public void freshmeat() throws KeyManagementException, HibernateException, SQLException {
        deleteOldDatabase();
        
        // reinit the repository
        ComponentManagerFactory.getComponentManager().shutdown();
        DKMSComponentTestBase.compMgr = ComponentManagerFactory.getComponentManager();
        init();
        
        assertEquals(0, keyRingManager.getKeyRings().size());
        
        PasswordProtection password = new PasswordProtection("password".toCharArray());
        
        final String keyRingName = "kr1";
        
        IKeyRing keyRing;
        keyRing = keyRingManager.getKeyRing(keyRingName, password);
        assertNull(keyRing);
        
        keyRing = keyRingManager.createKeyRing(keyRingName, password);
        assertNotNull(keyRing);
        
        assertEquals(1, keyRingManager.getKeyRings().size());
        
        keyRing = keyRingManager.getKeyRing(keyRingName, password);
        assertNotNull(keyRing);
        
        assertTrue(keyRingManager.deleteKeyRing(keyRing));
        
        assertEquals(0, keyRingManager.getKeyRings().size());
        
        //re delete
        assertFalse(keyRingManager.deleteKeyRing(keyRing));
    }

    @Test 
    public void closeCorrectly() throws KeyManagementException, HibernateException, SQLException {
        deleteAll();
        
        PasswordProtection password = new PasswordProtection("password".toCharArray());
        final String keyRingName = "kr1";
        IKeyRing keyRing = keyRingManager.createKeyRing(keyRingName, password);
        assertNotNull(keyRing);
        
        assertFalse(password.isDestroyed());
        keyRing.close();
        assertTrue(password.isDestroyed());
    }
    
    @Test
    public void manyKeys() throws KeyManagementException, HibernateException, SQLException {
        deleteAll();
        
        int size = 10;
        for (int i = 0; i < size; i++) {
            PasswordProtection password = new PasswordProtection("password".toCharArray());
            keyRingManager.createKeyRing("kr" + i, password);
        }
        
        assertEquals(size, keyRingManager.getKeyRings().size());
    }
    
    @Test
    public void checkDate() throws KeyManagementException, InterruptedException {
        deleteAll();
        
        PasswordProtection password = new PasswordProtection("password".toCharArray());
        final String keyRingName = "kr1";
        
        Date beforeCreate = new Date();
        IKeyRing keyRing1 = keyRingManager.createKeyRing(keyRingName, password);
        Date afterCreate = new Date();
        
        Date createTime1 = keyRing1.getKeyRingDO().getCreated();
        Date updateTime1 = keyRing1.getKeyRingDO().getLastUpdated();
        
        assertTrue(!beforeCreate.after(createTime1));
        assertTrue(!afterCreate.before(createTime1));
        assertTrue(!beforeCreate.after(updateTime1));
        assertTrue(!afterCreate.before(updateTime1));
        
        Thread.sleep(300);
        
        IKeyRing keyRing2 = keyRingManager.getKeyRing(keyRingName, password);
        
        Date createTime2 = keyRing2.getKeyRingDO().getCreated();
        Date updateTime2 = keyRing2.getKeyRingDO().getLastUpdated();
        
        assertEquals(createTime1, createTime2);
        assertEquals(updateTime1, updateTime2);
    }
    
    @Test(expected = KeyManagementException.class)
    public void createDuplicate() throws KeyManagementException {
        deleteAll();
        
        PasswordProtection password = new PasswordProtection("password".toCharArray());
        final String keyRingName = "kr1";
        keyRingManager.createKeyRing(keyRingName, password);
        keyRingManager.createKeyRing(keyRingName, password);
    }
    
    @Test
    public void getByWrongPassword() throws KeyManagementException, InterruptedException, ExecutionException {
        deleteAll();
        
        final char[] password = "p".toCharArray();
        final String keyRingName = "kr";
        
        IKeyRing keyRing = keyRingManager.createKeyRing(keyRingName, new PasswordProtection(password));
        assertNotNull(keyRing);
        
        try {
            keyRingManager.getKeyRing(keyRingName, new PasswordProtection("w".toCharArray()));
            fail();
        } catch (KeyManagementException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        
    }
    
    @Test
    public void getNonExist() throws KeyManagementException, InterruptedException, ExecutionException {
        deleteAll();
        
        final char[] password = "p".toCharArray();
        final String keyRingName = "kr";
        
        IKeyRing keyRing = keyRingManager.getKeyRing(keyRingName, new PasswordProtection(password));
        assertNull(keyRing);
    }
    
    @Test
    @Ignore
    public void concurrentCRUD() throws KeyManagementException, InterruptedException, ExecutionException {
        deleteAll();
        
        int numberOfThread = 10;
        final int numberOfRun = 200;
        
        ExecutorService pool = Executors.newFixedThreadPool(numberOfThread);
        Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>(numberOfThread);
        
        for (int i = 0; i < numberOfThread; i++) {
            final int threadId = i;
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Random r = new Random();
                    for (int j = 0; j < numberOfRun; j++) {
                        String debug = threadId + "_" + j;
                        
                        final char[] password = ("p" + threadId).toCharArray();
                        final String keyRingName = "kr" + threadId;
                        
                        IKeyRing keyRing = keyRingManager.createKeyRing(keyRingName, new PasswordProtection(password));
                        assertNotNull(debug, keyRing);
                        
                        byte[] key = ("k" + debug + "_" + r.nextInt()).getBytes();
                        byte[] value = ("v" + debug + "_" + r.nextInt()).getBytes();
                        
                        long ts = (threadId + 1) * 100000 + j;
                        keyRing.setKey(new Key(new KeyId(key, ts), value));
                        
                        assertEquals(debug, 1, keyRing.getKeys().size());
                        
                        if (r.nextBoolean()) {
                            keyRing.close();
                        }
                        
                        keyRing = keyRingManager.getKeyRing(keyRingName, new PasswordProtection(password));
                        assertEquals(debug, 1, keyRing.getKeys().size());
                        
                        IKey aKey = keyRing.getKeys().iterator().next();
                        assertArrayEquals(debug, key, aKey.getId());
                        assertArrayEquals(debug, value, aKey.getEncoded());
                        
                        boolean deleted;
                        if (r.nextBoolean()) {
                            deleted = keyRingManager.deleteKeyRing(keyRing);
                        }else{
                            deleted = keyRingManager.deleteKeyRing(keyRingName);
                        }
                        
                        assertTrue(debug, deleted);
                        
                        assertNull(keyRingManager.getKeyRing(keyRingName, new PasswordProtection(password)));
                        
                    }
                    return null;
                }
            });
        }
        
        List<Future<Object>> results = pool.invokeAll(tasks);
        for(Future<Object> result : results){
            result.get();
        }
    }
    
    @Test
    public void updateConflict() throws KeyManagementException, InterruptedException, ExecutionException {
        deleteAll();
        
                        
        final char[] password = "p".toCharArray();
        final String keyRingName = "kr";
        
        IKeyRing keyRing = keyRingManager.createKeyRing(keyRingName, new PasswordProtection(password));
        assertNotNull(keyRing);
        
        IKeyRing keyRing1 = keyRingManager.getKeyRing(keyRingName, new PasswordProtection(password));
        assertNotNull(keyRing1);
        
        IKeyRing keyRing2 = keyRingManager.getKeyRing(keyRingName, new PasswordProtection(password));
        assertNotNull(keyRing2);
        
        byte[] key2 = "k2".getBytes();
        byte[] value2 = "v2".getBytes();
        long timestamp  = System.currentTimeMillis();
        keyRing2.setKey(new Key(new KeyId(key2, timestamp), value2));
        
        byte[] key1 = "k1".getBytes();
        byte[] value1 = "v1".getBytes();
        try {
            keyRing1.setKey(new Key(new KeyId(key1, timestamp), value1));
            fail();
        } catch (KeyManagementException e) {
            assertTrue(e.getCause() instanceof StaleObjectStateException);
        }
        
        assertEquals(1, keyRing2.getKeys().size());
        IKey key = keyRing2.getKey(new KeyId(key2, timestamp));
        assertNotNull(key);
        assertArrayEquals(key2, key.getId());
        assertArrayEquals(value2, key.getEncoded());
        
        assertEquals(1, keyRing1.getKeys().size());
    }
    
    @Test
    public void getAllLatestModifedDate() throws KeyManagementException, InterruptedException {
        deleteAll();
        
        final char[] password = "p".toCharArray();
        
        IKeyRing keyRing = keyRingManager.createKeyRing("kr1", new PasswordProtection(password));
        assertNotNull(keyRing);
        
        long lastModifiedTime = keyRing.getKeyRingDO().getLastUpdated().getTime();
        assertEquals(lastModifiedTime, keyRingManager.getLatestModifiedDate());
        

        
        final long sleepTime = 1000;
        Thread.sleep(sleepTime);
        
        IKeyRing keyRing2 = keyRingManager.createKeyRing("kr2", new PasswordProtection(password));
        assertNotNull(keyRing2);
        long lastModifiedTime2 = keyRing2.getKeyRingDO().getLastUpdated().getTime();
        
        assertTrue(lastModifiedTime + sleepTime <= lastModifiedTime2);
        assertEquals(lastModifiedTime2, keyRingManager.getLatestModifiedDate());
        
        keyRing = keyRingManager.getKeyRing(keyRing.getName(), new PasswordProtection(password));
        
        assertEquals(lastModifiedTime, keyRing.getKeyRingDO().getLastUpdated().getTime());

        
        
        Thread.sleep(sleepTime);
        
        keyRingManager.deleteKeyRing(keyRing);
        
        assertTrue(lastModifiedTime2 < keyRingManager.getLatestModifiedDate());
        assertTrue(System.currentTimeMillis() >= keyRingManager.getLatestModifiedDate());
    }
    
    
}
