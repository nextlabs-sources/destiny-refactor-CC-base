package com.nextlabs.destiny.container.dkms.impl;

import java.security.KeyStore.PasswordProtection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.nextlabs.destiny.container.dkms.IKeyRing;
import com.nextlabs.destiny.container.dkms.IKeyRingManager;
import com.nextlabs.destiny.container.dkms.KeyManagementException;
import com.nextlabs.destiny.container.dkms.KeyManagementRepository;
import com.nextlabs.destiny.container.dkms.hibernateimpl.IKeyRingDO;
import com.nextlabs.destiny.container.dkms.hibernateimpl.KeyRingDO;
import com.nextlabs.destiny.container.dkms.hibernateimpl.KeyRingInterceptor;
import com.nextlabs.pf.destiny.lib.KeyManagerConstants;

public class KeyRingManager implements IKeyRingManager,
        IHasComponentInfo<IKeyRingManager>, ILogEnabled, IManagerEnabled,
        IInitializable {
    
    private static final ComponentInfo<IKeyRingManager> COMP_INFO =
            new ComponentInfo<IKeyRingManager>(
                    KeyRingManager.class
                  , LifestyleType.THREADED_TYPE
            );
    
    private static final Interceptor ENTITY_SAVE_INTERCEPTOR = new KeyRingInterceptor();
    
    private IComponentManager compManager;
    private Log log;
    private IHibernateRepository repository;
    
    @Override
    public ComponentInfo<IKeyRingManager> getComponentInfo() {
        return COMP_INFO;
    }
    
    @Override
    public void setManager(IComponentManager manager) {
        this.compManager = manager;
    }

    @Override
    public IComponentManager getManager() {
        return compManager;
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public Log getLog() {
        return log;
    }
    
    @Override
    public void init() {
        repository = compManager.getComponent(KeyManagementRepository.COMP_INFO);
    }
    
    private abstract class DbAction<T> {
        
        final boolean isUpdate;
        
        DbAction(boolean isUpdate){
            this.isUpdate = isUpdate; 
        }
        
        T execute() throws KeyManagementException {
            Session s = null;
            Transaction t = null;
            boolean success = false;
            try {
                s = isUpdate ? repository.openSession(ENTITY_SAVE_INTERCEPTOR) : repository.openSession();
                
                t = s.beginTransaction();
                
                T object = run(s);
                
                t.commit();
                success = true;
                
                return object;
            } catch (Exception e) {
                throw new KeyManagementException(e);
            } finally {
                if(!success){
                    HibernateUtils.rollbackTransation(t, log);
                }
                HibernateUtils.closeSession(s, log);
            }
        }
        
        abstract T run(Session session) throws Exception;
    }

    
    @Override
    public IKeyRing createKeyRing(final String name, PasswordProtection password)
            throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("createKeyRing (" + name + ")");
        }

        if (name.length() > KeyManagerConstants.MAX_KEY_RING_NAME_LIMIT) {
            throw new KeyManagementException("Key name " + name + " exceeds length limit of " + KeyManagerConstants.MAX_KEY_RING_NAME_LIMIT);
        }

        final KeyRingDO keyRingDO = new KeyRingDO(name, null, KeyRingJCEKSImpl.KEY_STRORE_TYPE);
        
        new DbAction(false){
            @Override
            Object run(Session session) throws Exception {
                session.delete("from KeyRingDO k where k.name = ?" 
                        + " and k.format = '" + IKeyRingDO.FORMAT_DELETED + "'"
                    , name
                    , Hibernate.STRING);
                return null;
            }
        }.execute();
        
        new DbAction(true){
            @Override
            Object run(Session session) throws Exception {
                session.save(keyRingDO);
                return null;
            }
        }.execute();
        
        KeyRingJCEKSImpl keyRingImpl =  new KeyRingJCEKSImpl(keyRingDO, password, this);
        keyRingImpl.flush();
        return keyRingImpl;
    }

    @Override
    public IKeyRing getKeyRing(final String name, PasswordProtection password)
            throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("getKeyRing (" + name + ")");
        }
        KeyRingDO keyRingDO = new DbAction<KeyRingDO>(false){
            @Override
            KeyRingDO run(Session session) throws Exception {
                List<KeyRingDO> keyRingDOs = 
                    session.find("from KeyRingDO k where k.name = ?" 
                            + " and k.format != '" + IKeyRingDO.FORMAT_DELETED + "'"
                        , name
                        , Hibernate.STRING);
                assert keyRingDOs.size() <= 1;
                return keyRingDOs.isEmpty() ? null : keyRingDOs.get(0);
            }
        }.execute();
        
        if(keyRingDO == null) {
            return null;
        }
        
        return new KeyRingJCEKSImpl(keyRingDO, password, this);
    }

    @Override
    public void updateKeyRing(final IKeyRing keyRing)
            throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("updateKeyRing (" + toShortName(keyRing) + ")");
        }
        new DbAction(true){
            @Override
            Object run(Session session) throws Exception {
                session.update(keyRing.getKeyRingDO());
                return null;
            }
        }.execute();
    }

    @Override
    public boolean deleteKeyRing(final String name) throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("deleteKeyRing keyRing(" + name + ")");
        }
        
        return new DbAction<Boolean>(true){
            @Override
            Boolean run(Session session) throws Exception {
                List<KeyRingDO> keyRingDOs = 
                    session.find("from KeyRingDO k where k.name = ?"
                            + " and k.format != '" + IKeyRingDO.FORMAT_DELETED + "'"
                        , name
                        , Hibernate.STRING);
                
                assert keyRingDOs.size() <= 1;

                if (keyRingDOs.size() == 0) {
                    return false;
                }

                KeyRingDO keyRingDO = keyRingDOs.get(0);

                keyRingDO.setKeyStoreData(null);
                keyRingDO.setFormat(IKeyRingDO.FORMAT_DELETED);
                session.update(keyRingDO);
                return true;
            }
        }.execute();
    }

    @Override
    public boolean deleteKeyRing(final IKeyRing keyRing) throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("deleteKeyRing keyRing(" + toShortName(keyRing) + ")");
        }
        
        if(IKeyRingDO.FORMAT_DELETED.equals(keyRing.getKeyRingDO().getFormat())){
            return false;
        }
        
        new DbAction(true){
            @Override
            Object run(Session session) throws Exception {
                keyRing.getKeyRingDO().setKeyStoreData(null);
                keyRing.getKeyRingDO().setFormat(IKeyRingDO.FORMAT_DELETED);
                session.update(keyRing.getKeyRingDO());
                return null;
            }
        }.execute();
        
        return true;
    }
    

    @Override
    public Set<String> getKeyRings() throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("getKeyRings");
        }
        
        List<String> names = new DbAction<List<String>>(false){
            @Override
            List<String> run(Session session) throws Exception {
                List<String> names = session.find("select k.name from KeyRingDO k where k.format != '" + IKeyRingDO.FORMAT_DELETED + "'");
                return names;
            }
        }.execute();
        
        return new HashSet<String>(names);
    }

    private String toShortName(IKeyRing keyRing) {
        return keyRing != null ? keyRing.getName() : "null";
    }
    
    @Override
    public long getLatestModifiedDate() throws KeyManagementException {
        if (log.isTraceEnabled()) {
            log.trace("getLastModifiedDate");
        }
        
        long latestModifiedDate = new DbAction<Long>(false){
            @Override
            Long run(Session session) throws Exception {
                Query query = session.createQuery("select max(k.lastUpdated) from KeyRingDO k");
                List<UnmodifiableDate> result = (List<UnmodifiableDate>)query.list();
                
                if (!result.isEmpty()) {
                    UnmodifiableDate date = result.get(0);
                    if (date != null) {
                        return date.getTime();
                    }
                }
                return UnmodifiableDate.START_OF_TIME.getTime();
            }
        }.execute();
        
        return latestModifiedDate;
    }
   
}
