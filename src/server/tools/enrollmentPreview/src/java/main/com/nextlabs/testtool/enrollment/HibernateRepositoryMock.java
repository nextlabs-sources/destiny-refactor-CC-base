/*
 * Created on Mar 3, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.Reference;

import net.sf.hibernate.Databinder;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.metadata.ClassMetadata;
import net.sf.hibernate.metadata.CollectionMetadata;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/HibernateRepositoryMock.java#1 $
 */
@SuppressWarnings("unchecked")
public class HibernateRepositoryMock implements IHibernateRepository {

	public void closeCurrentSession() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session getCountedSession() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session getCountedSession(Interceptor interceptor) throws HibernateException {
		throw new UnsupportedOperationException();
	}
	
	public int getCountedSessionCount() {
	    throw new UnsupportedOperationException();
	}

	public Session getCurrentSession() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session getCurrentSession(Interceptor interceptor) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session getSession() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session getSession(Connection connection) {
		throw new UnsupportedOperationException();
	}

	public void close() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evict(Class persistentClass) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evict(Class persistentClass, Serializable id) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evictCollection(String roleName) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evictCollection(String roleName, Serializable id) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evictQueries() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public void evictQueries(String cacheRegion) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Map getAllClassMetadata() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Map getAllCollectionMetadata() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Databinder openDatabinder() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session openSession() throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session openSession(Connection connection) {
		throw new UnsupportedOperationException();
	}

	public Session openSession(Interceptor interceptor) throws HibernateException {
		throw new UnsupportedOperationException();
	}

	public Session openSession(Connection connection, Interceptor interceptor) {
		throw new UnsupportedOperationException();
	}

	public Reference getReference() throws NamingException {
		throw new UnsupportedOperationException();
	}

	public IConfiguration getConfiguration() {
		throw new UnsupportedOperationException();
	}

	public void setConfiguration(IConfiguration arg0) {
		throw new UnsupportedOperationException();
	}

	public Log getLog() {
		throw new UnsupportedOperationException();
	}

	public void setLog(Log arg0) {
		throw new UnsupportedOperationException();
	}

	public void dispose() {
		throw new UnsupportedOperationException();
	}

	public void init() {
		throw new UnsupportedOperationException();
	}

	public IComponentManager getManager() {
		throw new UnsupportedOperationException();
	}

	public void setManager(IComponentManager arg0) {
		throw new UnsupportedOperationException();
	}

    @Override
    public DbType getDatabaseType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dialect getDialect() throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }
}
