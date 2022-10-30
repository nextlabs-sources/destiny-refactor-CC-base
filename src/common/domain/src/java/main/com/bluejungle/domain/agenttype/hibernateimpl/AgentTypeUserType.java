/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.agenttype.hibernateimpl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.type.Type;

/**
 * This is the user hibernate class for the agent type enum. It allows an agent
 * type enum instance to be stored in the database.
 * 
 * @author sgoldstein
 */
public class AgentTypeUserType extends EnumUserType<AgentTypeEnumType> {

    private static final AgentTypeEnumType[] ENUM_ARRAY = new AgentTypeEnumType[2];
    private static final String[] STRING_ARRAY = new String[2];

    static {
        ENUM_ARRAY[0] = AgentTypeEnumType.DESKTOP;
        ENUM_ARRAY[1] = AgentTypeEnumType.FILE_SERVER;

        STRING_ARRAY[0] = "D";
        STRING_ARRAY[1] = "F";
    }

    
    /**
     * Agent types are stored as one character in the database
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    private static final Log LOG = LogFactory.getLog(AgentTypeUserType.class.getName());

    /**
     * Type to be used during Hibernate queries
     */
    public static final Type TYPE;
    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(AgentTypeUserType.class);
        } catch (HibernateException exception) {
            LOG.error("Failed to create AgentTypeUserType Type", exception);
            typeCreated = null;
        }
        TYPE = typeCreated;
    }

    /**
     * Constructor
     */
    public AgentTypeUserType() {
        super(ENUM_ARRAY, STRING_ARRAY, AgentTypeEnumType.class);
    }

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

}