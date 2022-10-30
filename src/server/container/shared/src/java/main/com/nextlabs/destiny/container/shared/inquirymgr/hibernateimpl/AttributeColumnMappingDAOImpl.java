/**
 * 
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author nnallagatla
 *
 */
public class AttributeColumnMappingDAOImpl implements AttributeColumnMappingDAO {

	/* (non-Javadoc)
	 * @see com.nextlabs.destiny.inquirycenter.mapping.dao.AttributeColumnMappingDAO#getAll()
	 */
	@Override
	public List<AttributeColumnMappingDO> getAll() throws HibernateException {
		IComponentManager compMgr = ComponentManagerFactory
				.getComponentManager();
		IHibernateRepository dataSource = (IHibernateRepository) compMgr
				.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());

		List<AttributeColumnMappingDO> mappingList = null;
		// The session should not be closed
		Session s = null;
		try {
			s = dataSource.getSession();
			Criteria criteria = s.createCriteria(AttributeColumnMappingDO.class);
			mappingList = criteria.list();
			
		} finally {
			HibernateUtils.closeSession(s, LOG);
		}
		return mappingList;
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.destiny.inquirycenter.mapping.dao.AttributeColumnMappingDAO#getAll()
	 */
	@Override
	public List<AttributeColumnMappingDO> getAll(String sType) throws HibernateException {
		IComponentManager compMgr = ComponentManagerFactory
				.getComponentManager();
		IHibernateRepository dataSource = (IHibernateRepository) compMgr
				.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());

		List<AttributeColumnMappingDO> mappingList = null;
		// The session should not be closed
		Session s = null;
		try {
			s = dataSource.getSession();
			Criteria criteria = s.createCriteria(AttributeColumnMappingDO.class);
			criteria.add(Expression.eq("attributeType", sType));
			mappingList = criteria.list();
			
		} finally {
			HibernateUtils.closeSession(s, LOG);
		}
		return mappingList;
	}
	
	public static List<AttributeColumnMappingDO> list() throws HibernateException
	{
		return new AttributeColumnMappingDAOImpl().getAll();
	}

	@Override
	public boolean insert(AttributeColumnMappingDO mapping)
			throws HibernateException {
		IComponentManager compMgr = ComponentManagerFactory
				.getComponentManager();
		IHibernateRepository dataSource = (IHibernateRepository) compMgr
				.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());

		// The session should not be closed
		Session s = null;
		Transaction t = null;
		
		boolean status = false;
		try {
			s = dataSource.getSession();
			t = s.beginTransaction();
			s.save(mapping);
			t.commit();
			status = true;
		} finally {
			HibernateUtils.closeSession(s, LOG);
		}
		return status;
	}
	
	public static boolean addMapping(AttributeColumnMappingDO mapping) throws HibernateException
	{
		return new AttributeColumnMappingDAOImpl().insert(mapping);
	}

}
