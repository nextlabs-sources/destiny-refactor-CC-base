/*
 * Created on Mar 29, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This is the user and group resolver class implementation. This class allows
 * figuring out whether an expression represents a user or a user group. By
 * default, an expression is supposed to represent a user and a group. However,
 * advanced user may separate the users from the user group by putting brackets
 * in front.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/UserAndGroupResolverImpl.java#3 $
 */

public class UserAndGroupResolverImpl extends BaseResolverImpl implements IUserResolver {

    /**
     * Regular expression patterns used to match a user or a group
     */
    private static final Pattern USER_PATTERN = Pattern.compile("\\s*[(]\\s*[u[U]][s[S]][e[E]][r[R]]\\s*[)][\\p{Graph}\\s]*");
    private static final Pattern USERGROUP_PATTERN = Pattern.compile("\\s*[(]\\s*[g[G]][r[R]][o[O]][u[U]][p[P]]\\s*[)][\\p{Graph}\\s]*");

    /**
     * Expressions use to take off ambiguity between user and group names
     */
    protected static final String OPEN_CHAR = "(";
    protected static final String CLOSE_CHAR = ")";
    protected static final String USER_KEY = OPEN_CHAR + "User" + CLOSE_CHAR;
    protected static final String USERGROUP_KEY = OPEN_CHAR + "Group" + CLOSE_CHAR;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#create(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public String create(final EntityExpressionType type, final String value) {
        String result = value.trim();
        if (EntityExpressionType.ENTITY.equals(type)) {
            result = USER_KEY + value;
        } else if (EntityExpressionType.ENTITY_GROUP.equals(type)) {
            result = USERGROUP_KEY + value;
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#extractValue(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public String extractValue(EntityExpressionType type, String expression) {
        String result = null;
        if (expression != null) {
            if (EntityExpressionType.ENTITY.equals(type) || EntityExpressionType.ENTITY_GROUP.equals(type)) {
                StringTokenizer tokenizer = new StringTokenizer(expression, CLOSE_CHAR);
                //Below two tokens, the expression is not properly formatted
                if (tokenizer.countTokens() >= 2) {
                    tokenizer.nextToken();
                    result = tokenizer.nextToken().trim();
                }
            } else {
                result = expression.trim();
            }
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#getEntityPattern()
     */
    protected Pattern getEntityPattern() {
        return USER_PATTERN;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#getEntityGroupPattern()
     */
    protected Pattern getEntityGroupPattern() {
        return USERGROUP_PATTERN;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#resolveAmbiguous(java.lang.String)
     */
    protected EntityExpressionType resolveAmbiguous(final String expression) {
        EntityExpressionType result;
        if (expression != null && expression.length() > 0) {
            result = EntityExpressionType.ENTITY_AND_ENTITY_GROUP;
        } else {
            result = EntityExpressionType.UNKNOWN;
        }
        return result;
    }
}