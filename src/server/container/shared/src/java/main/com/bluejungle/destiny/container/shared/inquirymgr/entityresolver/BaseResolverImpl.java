/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;

/**
 * This is the base resolver class. It is extended by the various
 * implementations of the entity resolver.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/BaseResolverImpl.java#1 $
 */

abstract class BaseResolverImpl implements IManagerEnabled, ILogEnabled, IConfigurable, IDisposable, IInitializable, IEntityResolver {

    private IConfiguration configuration;
    private Log log;
    private IComponentManager compMgr;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#create(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public abstract String create(final EntityExpressionType type, final String value);

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.configuration = null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#extractValue(com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.EntityExpressionType,
     *      java.lang.String)
     */
    public abstract String extractValue(EntityExpressionType type, String expression);

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the pattern used to recognize an entity expression
     * 
     * @return the regex pattern used to recognize an entity expression
     */
    protected abstract Pattern getEntityPattern();

    /**
     * Returns the pattern used to recognize an entity group expression
     * 
     * @return the regex pattern used to recognize an entity group expression
     */
    protected abstract Pattern getEntityGroupPattern();

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    /**
     * This function returns whether a given expression corresponds to the
     * description of an entity
     * 
     * @return true if this expression expresses an entity, false otherwise
     */
    protected boolean isEntity(final String expression) {
        boolean result = false;
        if (expression != null) {
            Matcher m = getEntityPattern().matcher(expression);
            result = m.matches();
        }
        return result;
    }

    /**
     * This function returns whether a given expression corresponds to the
     * description of an entity group
     * 
     * @return true if this expression expresses an entity group, false
     *         otherwise
     */
    protected boolean isEntityGroup(final String expression) {
        boolean result = false;
        if (expression != null) {
            Matcher m = getEntityGroupPattern().matcher(expression);
            result = m.matches();
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.IEntityResolver#resolve(java.lang.String)
     */
    public final EntityExpressionType resolve(final String expression) {
        EntityExpressionType result = EntityExpressionType.UNKNOWN;
        boolean isEntity = isEntity(expression);
        boolean isGroup = isEntityGroup(expression);

        if (isEntity && isGroup) {
            result = EntityExpressionType.ENTITY_AND_ENTITY_GROUP;
        } else if (isEntity) {
            result = EntityExpressionType.ENTITY;
        } else if (isGroup) {
            result = EntityExpressionType.ENTITY_GROUP;
        } else {
            result = resolveAmbiguous(expression);
        }
        return result;
    }

    /**
     * Resolves an ambiguous expression where the user did not specify whether
     * the expression is for an entity or a group.
     * 
     * @param expression
     *            expression to inspect
     * @return the corresponding entity expression type for this expression
     */
    protected abstract EntityExpressionType resolveAmbiguous(final String expression);

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.compMgr = newMgr;
    }
}