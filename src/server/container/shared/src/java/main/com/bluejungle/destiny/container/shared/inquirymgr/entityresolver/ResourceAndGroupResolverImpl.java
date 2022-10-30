/*
 * Created on Mar 29, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import java.util.regex.Pattern;

/**
 * This is the resource and resource group resolver class implementation. This
 * class can resolve an expression and decide whether an expression refers to a
 * resource or a resource class. The resource and resource classes are simply
 * detected based on the expression in the string. They are no specific
 * expression in front of the expression.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/UserAndGroupResolverImpl.java#1 $
 */

public class ResourceAndGroupResolverImpl extends BaseResolverImpl {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#create(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public String create(final EntityExpressionType type, final String value) {
        return value.trim();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#extractValue(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public String extractValue(EntityExpressionType type, String expression) {
        return expression.trim();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#isEntity(java.lang.String)
     */
    protected boolean isEntity(final String expression) {
        boolean result = false;
        if (expression != null) {
            String expressionToEval = expression.trim();
            // TODO: We now treat everything as resource entities because
            // we temporarily took out resource class queries.  This 
            // is commented out for Bug 4181.  
//            result = ((expressionToEval.startsWith("file://")) || (expressionToEval.startsWith("\\")) || (expressionToEval.indexOf("*") >= 0) || (expressionToEval.indexOf("?") >= 0) || (expressionToEval.matches("[a-zA-Z]:\\\\.*")));
            result = true;
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#isEntityGroup(java.lang.String)
     */
    protected boolean isEntityGroup(final String expression) {
        boolean result = false;
        if (expression != null && expression.length() > 0) {
            result = !isEntity(expression);
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#getEntityPattern()
     */
    protected Pattern getEntityPattern() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#getEntityGroupPattern()
     */
    protected Pattern getEntityGroupPattern() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.BaseResolverImpl#resolveAmbiguous(java.lang.String)
     */
    protected EntityExpressionType resolveAmbiguous(final String expression) {
        EntityExpressionType result;
        if (expression != null && expression.length() > 0) {
            //By default, if we don't know, this may be a resource name
            result = EntityExpressionType.ENTITY;
        } else {
            result = EntityExpressionType.UNKNOWN;
        }
        return result;
    }
}