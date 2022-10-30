package com.bluejungle.pf.domain.epicenter.resource;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/resource/IResourceManager.java#1 $
 */

import java.io.Serializable;
import java.util.Map;

import com.bluejungle.framework.expressions.IEvalValue;

/**
 * This interface defines the contract for resource manager classes.
 *
 * @author sergey
 * @param <RI> The Resource Information class.
 */
public interface IResourceManager<RI> {

    /**
     * This interface defines the contract for returning
     * resource information to the callers.
     *
     * @param <RI> The Resource Information class.
     */
    public interface ResourceInformation<RI> extends Serializable {
        IResource getResource();
        IMResource getMutableResource();
        RI getInformation();
    }

    /**
     * This interface defines the contract for making new instances of
     * resource information.
     *
     * @param <RI> The Resource Information class.
     */
    interface ResourceInformationMaker<RI> extends Serializable {
        /**
         * Given an id and a source of attribute data, creates a new instance of
         * resource information.
         * @return a new instance of <RI>.
         */
        RI makeResourceInformation();
    }

    /**
     * Given an id and a source of attribute data, gets an existing instance
     * of <code>IResource</code> or creates ana optionally caches a new one.
     *
     * @param id The id of the resource.
     * @param attr a <code>Map</code> of attribute names to attribute values.
     * Implementations that cache resources will not use methods of this argument
     * when the cached information is available. This enables the users to pass
     * lazy implementations of the <code>Map</code>.
     * @return an <code>ResourceInformation</code> for the specified resource.
     */
    ResourceInformation<RI> getResourceInfo(Serializable id, Map<String,IEvalValue> attr);

    /**
     * Given an id and a source of attribute data, gets an existing instance
     * of <code>IResource</code> or creates ana optionally caches a new one.
     *
     * @param id The id of the resource.
     * @param attr a <code>Map</code> of attribute names to attribute values.
     * Implementations that cache resources will not use methods of this argument
     * when the cached information is available. This enables the users to pass
     * lazy implementations of the <code>Map</code>.
     * @param loader optional "loader" to lazily load attributes that take some time to acquire
     * @return an <code>ResourceInformation</code> for the specified resource.
     */
    ResourceInformation<RI> getResourceInfo(Serializable id, Map<String,IEvalValue> attr, IResourceAttributeLoader loader);

    /**
     * Sets a new <code>ResourceInformationMaker<RI></code> for this manager.
     * @param maker the new <code>ResourceInformationMaker<RI></code> to set.
     */
    void setResourceInformationMaker(ResourceInformationMaker<RI> maker);
}
