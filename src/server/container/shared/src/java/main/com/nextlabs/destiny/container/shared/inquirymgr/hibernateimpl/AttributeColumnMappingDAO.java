/**
 * 
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.HibernateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nnallagatla
 *
 */
public interface AttributeColumnMappingDAO {
	
	public static final Log LOG = LogFactory.getLog(AttributeColumnMappingDAO.class.getName());
	
	public List<AttributeColumnMappingDO> getAll() throws HibernateException;
	
	public List<AttributeColumnMappingDO> getAll(String sType) throws HibernateException;
	
	public boolean insert(AttributeColumnMappingDO mapping) throws HibernateException;
}
