package com.bluejungle.pf.destiny.lib;

/*
 * All sources, binaries and HTML pages (C) Copyright 2004-2007 by NextLabs, Inc,
 * San Mateo, CA. Ownership remains with NextLabs, Inc. All rights reserved
 * worldwide.
 * 
 * @author sergey
 * 
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/LeafObjectType.java#1 $
 */

import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.patterns.EnumBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a type of a leaf object.
 */
public class LeafObjectType extends EnumBase {

    private static final long serialVersionUID = 1L;

    private static final Map<AgentTypeEnumType, LeafObjectType> forAgentType = new HashMap<AgentTypeEnumType, LeafObjectType>();

    public static LeafObjectType USER = new LeafObjectType("USER") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType USER_GROUP = new LeafObjectType("USER_GROUP") {
        private static final long serialVersionUID = 1L;
    };
    
    public static LeafObjectType CONTACT = new LeafObjectType("CONTACT") {
        private static final long serialVersionUID = 1L;
    };
    
    public static LeafObjectType HOST = new LeafObjectType("HOST") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType HOST_GROUP = new LeafObjectType("HOST_GROUP") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType APPLICATION = new LeafObjectType("APPLICATION") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType RESOURCE = new LeafObjectType("RESOURCE") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType ACTION = new LeafObjectType("ACTION") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType APPUSER = new LeafObjectType("APPUSER") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType ACCESSGROUP = new LeafObjectType("ACCESSGROUP") {
        private static final long serialVersionUID = 1L;
    };

    public static LeafObjectType FILE_SERVER_AGENT = new LeafObjectType("FILE_SERVER_AGENT") {
        private static final long serialVersionUID = 1L;
        @Override
        public AgentTypeEnumType getAgentType() {
            return AgentTypeEnumType.FILE_SERVER;
        }
    };

    public static LeafObjectType DESKTOP_AGENT = new LeafObjectType("DESKTOP_AGENT") {
        private static final long serialVersionUID = 1L;
        @Override
        public AgentTypeEnumType getAgentType() {
            return AgentTypeEnumType.DESKTOP;
        }
    };

    public static LeafObjectType PORTAL_AGENT = new LeafObjectType("PORTAL_AGENT") {
        private static final long serialVersionUID = 1L;
        @Override
        public AgentTypeEnumType getAgentType() {
            return AgentTypeEnumType.PORTAL;
        }
    };

    public static LeafObjectType ACTIVE_DIRECTORY_AGENT = new LeafObjectType("ACTIVE_DIRECTORY_AGENT") {
        private static final long serialVersionUID = 1L;
        @Override
        public AgentTypeEnumType getAgentType() {
            return AgentTypeEnumType.ACTIVE_DIRECTORY;
        }
    };
    /**
     * Creates a new leaf type.
     * 
     * @param name
     *            The name of this leaf type.
     */
    private LeafObjectType(String name) {
        super(name, LeafObjectType.class);
        if (getAgentType() != null) {
            forAgentType.put(getAgentType(), this);
        }
    }

    /**
     * Returns the number of distinct <code>LeafObjectType</code>s.
     * 
     * @return the number of distinct <code>LeafObjectType</code>s.
     */
    public static int getElementCount() {
        return numElements(LeafObjectType.class);
    }

    /**
     * Gets an LeafObjectType by its integer type.
     * 
     * @param enumType
     *            type of the enumeration element.
     * @return enumeration element.
     * @throws IllegalArgumentException
     *             if there is no enumeration element of type enumType, or there
     *             are no enumeration elements added for class clazz.
     */
    public static LeafObjectType forType(int leafType) {
        return getElement(leafType, LeafObjectType.class);
    }

    /**
     * Returns the leaf type corresponding to the specified agent type.
     *
     * @param agentType the agent type for which to retrieve the leaf type.
     * @return the leaf type corresponding to the specified agent type.
     */
    public static LeafObjectType forAgentType(AgentTypeEnumType agentType) {
        return forAgentType.get(agentType);
    }

    /**
     * Gets an <code>LeafObjectType</code> by its string name.
     * 
     * @param name
     *            the name of the desired type.
     * @return an <code>LeafObjectType</code> by its string name.
     * @throws IllegalArgumentException
     *             if there is no enumeration element with name enumName, or
     *             there are no enumeration elements added for class clazz.
     */
    public static LeafObjectType forName(String name) {
        return getElement(name, LeafObjectType.class);
    }

    /**
     * Retrieve all elements of the enum as a Set
     * 
     * @return all elements of the enum as a Set
     */
    public static Set<LeafObjectType> getElements() {
        return elements(LeafObjectType.class);
    }

    public AgentTypeEnumType getAgentType() {
        return null;
    }

}
