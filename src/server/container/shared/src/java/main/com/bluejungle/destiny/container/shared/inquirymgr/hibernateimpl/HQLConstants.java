/*
 * Created on Mar 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * This class defines various HQL constants that are used whenever building HQL
 * expressions.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/HQLConstants.java#1 $
 */

public final class HQLConstants {

    /**
     * HQL constants
     */
    public static final String AND = "AND";
    public static final String ASC = "ASC";
    public static final String CLOSE_PARENTHESE = ")";
    public static final String COMMA = ",";
    public static final String DESC = "DESC";
    public static final String DISTINCT = "distinct";
    public static final String DOT = ".";
    public static final String EQUAL = "=";
    public static final String ELEMENTS = "elements";
    public static final String FROM = "FROM";
    public static final String GREATER_THAN = ">";
    public static final String GREATER_THAN_OR_EQUAL = ">=";
    public static final String GROUPBY = "GROUP BY";
    public static final String IN = "IN";
    public static final String IS_NULL = "IS NULL";
    public static final String IS_NOT_NULL = "IS NOT NULL";
    public static final String LESS_THAN = "<";
    public static final String LESS_THAN_OR_EQUAL = "<=";
    public static final String LIKE = "LIKE";
    public static final String LOWER = "lower";
    public static final String NOT_EQUAL = "!=";
    public static final String NOT = "NOT";
    public static final String OPEN_PARENTHESE = "(";
    public static final String OR = "OR";
    public static final String ORDERBY = "ORDER BY";
    public static final String COLON = ":";
    public static final String SINGLE_WILCHARD = "?";
    public static final String SMALLER_THAN = "<";
    public static final String SMALLER_THAN_OR_EQUAL = "<=";
    public static final String SELECT = "select";
    public static final String SOME = "some";
    public static final String SOME_ELEMENTS = "some elements";
    public static final String SPACE = " ";
    public static final String SINGLE_CHAR_WILDCARD = "_";
    public static final String STAR_WILDCARD = "%";
    public static final String WHERE = "WHERE";

    /**
     * Combined HQL constants
     */
    public static final String AND_WITH_SPACES = SPACE + AND + SPACE;
    public static final String GREATER_THAN_WITH_SPACES = SPACE + GREATER_THAN + SPACE;
    public static final String OR_WITH_SPACES = SPACE + OR + SPACE;
    public static final String WHERE_WITH_SPACES = SPACE + WHERE + SPACE;
}
