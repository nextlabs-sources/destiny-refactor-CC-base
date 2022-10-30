package com.nextlabs.destiny.container.dkms;

import static org.junit.Assume.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.junit.Before;
import org.junit.Test;

import com.bluejungle.destiny.server.shared.context.DestinySharedContextFactory;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.repository.IConnectionPool;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

public class KeyManagementRepositoryTest extends DKMSComponentTestBase{
    
    @Before
    public void init() {
        super.initAll();
    }
    
    private static IHibernateRepository previousRepository;

    @Test
    public void initOK() throws HibernateException {
        previousRepository = initDataSource();
        
        Session s = previousRepository.openSession();
        s.connection();
        s.close();
    }
    
    @Test
    public void recreate() throws HibernateException {
        assumeNotNull(previousRepository);
        IHibernateRepository newRepository = initDataSource();
        assertSame(previousRepository, newRepository);
        
        Session s = newRepository.openSession();
        s.connection();
        s.close();
    }
    
    @Test
    public void shutDownAndCreate() throws Exception {
        
        assumeNotNull(previousRepository);
        
        Session s = previousRepository.openSession();
        s.connection();
        s.close();
        
        IDestinySharedContext sharedContext = DestinySharedContextFactory.getInstance().getSharedContext();
        assertNotNull(sharedContext);

        IConnectionPoolFactory connectionPoolFactory = sharedContext.getConnectionPoolFactory();
        assertNotNull(connectionPoolFactory);
        
        IConnectionPool connectionPool = connectionPoolFactory.getConnectionPoolByName(
                KeyManagementRepository.CONNECTION_POOL_NAME);
        assertNotNull(connectionPool);
        
        previousRepository.close();
        compMgr.releaseComponent(previousRepository);
        compMgr.unregisterComponent(KeyManagementRepository.COMP_INFO);
        assertFalse(compMgr.isComponentRegistered(KeyManagementRepository.COMP_INFO.getName()));
        
        removeConnectionPool(connectionPoolFactory, KeyManagementRepository.CONNECTION_POOL_NAME);
        
        // there will be an error about the connection pool is closed but that's not what this test about
        IHibernateRepository newRepository = initDataSource();
        assertNotSame(previousRepository, newRepository);
        
        s = newRepository.openSession();
        s.connection();
        s.close();
        
        previousRepository = newRepository;
    }

    /**
     * a hack
     */
    public static void removeConnectionPool(IConnectionPoolFactory connectionPoolFactory, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        assertTrue(connectionPoolFactory instanceof C3P0ConnectionPoolWrapperFactory);
        
        Field field = C3P0ConnectionPoolWrapperFactory.class.getDeclaredField("connectionPoolsByName");
        field.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Map<String, IConnectionPool> map = (Map<String, IConnectionPool>)field.get(connectionPoolFactory);

        map.remove(name);
        
        IConnectionPool connectionPool = connectionPoolFactory.getConnectionPoolByName(name);
        assertNull(connectionPool);
        
        
    }
    
}
