package com.bluejungle.pf.domain.epicenter.resource;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/resource/Resource.java#1 $
 */

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.ValueType;

/**
 * This is the implementation of the <code>IMResource</code> interface.
 * @author sergey
 */
public class Resource implements IMResource, Serializable {

    /**
     * The default serialization ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The <code>Map</code> containing the attributes arranged by name.
     */
    private final Map<String,IEvalValue> attributes;

    private final Serializable id;

    /**
     * The <code>attributeLoader</code> can be used to lazily load any
     * attributes that might be slow to use and not always used (custom
     * attributes are an example).
     */
    private IResourceAttributeLoader attributeLoader;

    /**
     *  Indicates if we have invoked the attribute loader
     */
    private boolean attributeLoaderInvoked = false;

    public Resource(Serializable id) {
        this.id = id;
        attributes = new HashMap<String,IEvalValue>();
        attributeLoader = IResourceAttributeLoader.EMPTY;
    }

    public Resource(Serializable id, Map<String,IEvalValue> attributes) {
        this.id = id;
        this.attributes = new HashMap<String,IEvalValue>(attributes);
        attributeLoader = IResourceAttributeLoader.EMPTY;
    }

    public Resource(Serializable id, Map<String,IEvalValue> attributes, IResourceAttributeLoader attributeLoader) {
        this.id = id;
        this.attributes = new HashMap<String,IEvalValue>(attributes);
        this.attributeLoader = attributeLoader;
    }
    
    public IMResource clone() {
        return new Resource(id, attributes, attributeLoader);
    }

    /**
     * @see IMResource#clearAttribute(String)
     */
    public synchronized void clearAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * @see IMResource#setAttribute(String, IEvalValue)
     */
    public synchronized void setAttribute(String name, IEvalValue val) {
        attributes.put(name, val);
    }

    /**
     * @see IMResource#setAttribute(String, String)
     */
    public synchronized void setAttribute(String name, String val) {
        attributes.put(name, EvalValue.build(val));
    }

    /**
     * @see IMResource#setAttribute(String, long)
     */
    public synchronized void setAttribute(String name, long val) {
        attributes.put(name, EvalValue.build(val));
    }

    /**
     * @see IMResource#setAttribute(String, Date)
     */
    public synchronized void setAttribute(String name, Date val) {
        attributes.put(name, EvalValue.build(val));
    }

    /**
     * @see IMResource#setAttribute(String, Collection)
     */
    public synchronized <T> void setAttribute(String name, Collection<T> vals) {
        attributes.put(name, EvalValue.build(Multivalue.create(vals)));
    }

    /**
     * @see IMResource#setAttribute(String, Collection, ValueType)
     */
    public synchronized <T> void setAttribute(String name, Collection<T> vals, ValueType type) {
        attributes.put(name, EvalValue.build(Multivalue.create(vals, type)));
    }

    /**
     * @see IMResource#setProcessToken()
     */
    public void setProcessToken (Long processToken) {
        attributeLoader.setProcessToken(processToken);
    }

    /**
     * @see IResource#getAttribute(String)
     */
    public synchronized IEvalValue getAttribute(String name) {
        IEvalValue res = attributes.get(name);

        if (res == null && !attributeLoaderInvoked) {
            invokeLoader();
            res = attributes.get(name);
        }

        return (res!=null) ? res : IEvalValue.NULL;
    }

    /**
     * @see IResource#getEntrySet()
     *
     * Because this is defined in the immutable interface, we should make
     * sure that modifications can't be made to this set
     */
    public synchronized Set<Map.Entry<String, IEvalValue>> getEntrySet() {
        return Collections.unmodifiableSet(attributes.entrySet());
    }

    private void invokeLoader() {
        attributeLoaderInvoked = true;
        attributeLoader.getAttrs(attributes);
    }

    /**
     * @see IResource#hasAttribute(String)
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * @see IResource#getIdentifier()
     */
    public Serializable getIdentifier() {
        return id;
    }
}
