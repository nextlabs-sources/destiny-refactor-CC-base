/*
 * Created on May 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.search;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * RelationalOp is an enumeration of relational operators which can be used to
 * define a search.
 * 
 * Developer Note - Similar functionality can be found within the Predicate API.
 * At the time this class was created, the Predicate API existed, but provided
 * more functionality than was needed. To save time for the implementation of
 * the search, the impementation of the clients using the search, and
 * maintenance of both, using this alternative enumeration was thought to be a
 * better approach. As more advanced search becomes necessary, it's likely that
 * this class will be removed and all search APIs will utilize Predicate
 * Expressions. For example: <br />
 * <br />
 * AgentManager.getAgents(IPredicate agentQuery, ...)
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/search/RelationalOp.java#1 $
 */
public class RelationalOp extends EnumBase {

    private static final Map<String, RelationalOp> VERBOSE_NAME_TO_OP_MAP = new HashMap<String, RelationalOp>();
    private String verboseName;

    public static final RelationalOp EQUALS = new RelationalOp("equals", "=");
    public static final RelationalOp LIKE = new RelationalOp("like", "like");
    public static final RelationalOp NOT_EQUALS = new RelationalOp("not_equals", "!=");
    public static final RelationalOp GREATER_THAN = new RelationalOp("greater_than", ">");
    public static final RelationalOp LESS_THAN = new RelationalOp("less_than", "<");
    public static final RelationalOp GREATER_THAN_EQUALS = new RelationalOp("greater_than_equals", ">=");
    public static final RelationalOp LESS_THAN_EQUALS = new RelationalOp("less_than_equals", "<=");
    public static final RelationalOp HAS = new RelationalOp("has", "has");
    public static final RelationalOp STARTS_WITH = new RelationalOp("starts_with", "stw");

    /**
     * Create an instance of RelationalOp
     * 
     * @param verboseName
     * @param shortName
     */
    private RelationalOp(String verboseName, String shortName) {
        super(shortName);

        if (verboseName == null) {
            throw new NullPointerException("verboseName cannot be null.");
        }

        this.verboseName = verboseName;
        VERBOSE_NAME_TO_OP_MAP.put(verboseName, this);
    }

    /**
     * Retrieve a RelationalOp by either short or verbose name
     * 
     * @param name
     *            the verbose or short name of the relational operator to
     *            retrieve
     * @return the RelationalOp with the specified name
     * @throws IllegalArgumentException
     *             if the name is not recognized as a name of a RelationalOp
     */
    public static RelationalOp getRelationalOp(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        RelationalOp operatorToReturn = null;
        if (existsElement(name, RelationalOp.class)) {
            operatorToReturn = getElement(name, null);
        } else if (VERBOSE_NAME_TO_OP_MAP.containsKey(name)) {
            operatorToReturn = VERBOSE_NAME_TO_OP_MAP.get(name);
        } else {
            throw new IllegalArgumentException("Unknown name, " + name);
        }

        return operatorToReturn;
    }

    /**
     * Retrieve the verbose name of this relational operator
     * 
     * @return the verbose name of this relational operator
     */
    public String getVerboseName() {
        return this.verboseName;
    }
}