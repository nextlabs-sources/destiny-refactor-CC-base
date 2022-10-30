/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.policydecision.hibernateimpl;

import java.sql.Types;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * This is the user hibernate class for the agent activity type. It allows the
 * agent activity type to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/domain/src/java/main/com/bluejungle/domain/policydecision/hibernateimpl/PolicyDecisionUserType.java#1 $
 */

public class PolicyDecisionUserType extends EnumUserType<PolicyDecisionEnumType> {

    /**
     * Policy Decisions are stored as char
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Constructor
     */
    public PolicyDecisionUserType() {
        super(new PolicyDecisionEnumType[] { 
        			PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
        			PolicyDecisionEnumType.POLICY_DECISION_DENY }, 
        		new String[] { "A", "D" }, 
        		PolicyDecisionEnumType.class);
    }
}