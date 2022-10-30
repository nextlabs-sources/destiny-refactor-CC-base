/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.engine.Mapping;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.AbstractType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Hibernate User Type for storing an instance of IAgentType in the database.
 * Note that this class extends AbstractType rather than implement UserType. The
 * reason for this, is that in Hibernate 2.1, custom disassamble/assemble
 * methods were not possible. Note that the UserType methods are still
 * implemented to allow easy transition for Hibernate 3.0
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentTypeUserType.java#1 $
 */

public class AgentTypeUserType extends AbstractType {

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    private IAgentManager agentManager;

    
    /**
     * @see net.sf.hibernate.type.AbstractType#assemble(java.io.Serializable, net.sf.hibernate.engine.SessionImplementor, java.lang.Object)
     */
    @Override
    public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
        return getAgentType((String)cached);
    }

    /**
     * @see net.sf.hibernate.type.AbstractType#disassemble(java.lang.Object, net.sf.hibernate.engine.SessionImplementor)
     */
    @Override
    public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
        return ((IAgentType)value).getId();
    }

    /**
     * @see net.sf.hibernate.type.Type#sqlTypes(net.sf.hibernate.engine.Mapping)
     */
    public int[] sqlTypes(Mapping mapping) throws MappingException {
        return this.sqlTypes();
    }

    /**
     * @see net.sf.hibernate.type.Type#getColumnSpan(net.sf.hibernate.engine.Mapping)
     */
    public int getColumnSpan(Mapping mapping) throws MappingException {
        return this.sqlTypes().length;
    }

    /**
     * @see net.sf.hibernate.type.Type#getReturnedClass()
     */
    public Class getReturnedClass() {
        return AgentTypeUserType.class;
    }

    /**
     * @see net.sf.hibernate.type.Type#equals(java.lang.Object,
     *      java.lang.Object)
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        return x.equals(y);
    }

    /**
     * @see net.sf.hibernate.type.Type#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], net.sf.hibernate.engine.SessionImplementor,
     *      java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return nullSafeGet(rs, names, owner);
    }

    /**
     * @see net.sf.hibernate.type.Type#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String, net.sf.hibernate.engine.SessionImplementor,
     *      java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String columnName, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return nullSafeGet(rs, new String[] { columnName }, session, owner);
    }

    /**
     * @see net.sf.hibernate.type.Type#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int, net.sf.hibernate.engine.SessionImplementor)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        nullSafeSet(st, value, index);
    }

    /**
     * @see net.sf.hibernate.type.Type#toString(java.lang.Object,
     *      net.sf.hibernate.engine.SessionFactoryImplementor)
     */
    public String toString(Object value, SessionFactoryImplementor factory) throws HibernateException {
        // Copied from CustomType
        return (value == null) ? "null" : value.toString();
    }

    /**
     * @see net.sf.hibernate.type.Type#fromString(java.lang.String)
     */
    public Object fromString(String xml) throws HibernateException {
        // Copied from CustomType
        throw new UnsupportedOperationException("not yet implemented!"); // TODO:
                                                                            // look
                                                                            // for
                                                                            // constructor
    }

    /**
     * @see net.sf.hibernate.type.Type#getName()
     */
    public String getName() {
        return AgentTypeUserType.class.getName();
    }

    /**
     * @see net.sf.hibernate.type.Type#hasNiceEquals()
     */
    public boolean hasNiceEquals() {
        // This method is deprecated. Not being used anymore.
        return false;
    }

    /**
     * 
     * @see net.sf.hibernate.type.Type#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }
    
    /**
     * 
     * @see net.sf.hibernate.type.Type#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Object objectToReturn = null;

        String agentId = rs.getString(names[0]);
        if (agentId != null) {
            objectToReturn = getAgentType(agentId);
        }

        return objectToReturn;
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        String valueToSet = null;
        if (value != null) {
            valueToSet = ((IAgentType) value).getId();
        }
        st.setString(index, valueToSet);
    }

    /**
     * @see net.sf.hibernate.UserType#returnedClass()
     */
    public Class returnedClass() {
        return IAgentType.class;
    }

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * @param agentId
     * @return
     */
    private Object getAgentType(String agentId) {
        return getAgentManager().getAgentType(agentId);
    }
    
    /**
     * Retrieve the component manager
     * 
     * @return the component manager
     */
    private IAgentManager getAgentManager() {
        if (this.agentManager == null) {
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            this.agentManager = (IAgentManager) componentManager.getComponent(IAgentManager.COMP_NAME);
        }

        return this.agentManager;
    }

}
