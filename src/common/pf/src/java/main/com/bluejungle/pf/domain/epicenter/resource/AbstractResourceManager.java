package com.bluejungle.pf.domain.epicenter.resource;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/resource/AbstractResourceManager.java#1 $
 */

import java.io.Serializable;
import java.util.Map;

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;

/**
 * @author sergey
 * @param <RI> The resource information type.
 */
public abstract class AbstractResourceManager<RI extends Serializable> implements IResourceManager<RI> {

    private ResourceInformationMaker<RI> maker;

    protected AbstractResourceManager(final Class<RI> clazz) {
        maker = new AbstractResourceInformationMaker<RI>(clazz);
    }

    /**
     * Does the resource contain an attribute that indicates it should neither be read from nor saved
     * to the cache?
     */
    private boolean resourceCanBeCached(Map<String, IEvalValue> attr) {
        if (attr == null) {
            // Should be impossible, but safety first
            return true;
        }
        IEvalValue ignoreCacheValue = attr.get(SpecAttribute.NOCACHE_NAME);
        if (ignoreCacheValue != null && ((String)ignoreCacheValue.getValue()).equals("yes")) {
            return false;
        }

        return true;
    }

    /**
     * @see IResourceManager#getResourceInfo(Serializable, Map)
     */
    public ResourceInformation<RI> getResourceInfo(Serializable id, Map<String,IEvalValue> attr) {
        return getResourceInfo(id, attr, IResourceAttributeLoader.EMPTY);
    }

    /**
     * @see IResourceManager#getResourceInfo(Serializable, Map, IResourceAttributeLoader)
     */
    public ResourceInformation<RI> getResourceInfo(Serializable id, Map<String, IEvalValue> attr, IResourceAttributeLoader loader) {
        boolean useCache = resourceCanBeCached(attr);

        ResourceInformation<RI> res = useCache ? getCached(id, attr) : null;

        if (res == null) {
            res = new ResourceInformationImpl<RI>(new Resource(id, attr, loader), maker);

            if (useCache ) {
                saveToCache(res);
            }
        }
        return res;
    }

    /**
     * @see IResourceManager#setResourceInformationMaker(IResourceManager.ResourceInformationMaker)
     */
    public void setResourceInformationMaker(ResourceInformationMaker<RI> maker) {
        if (maker == null) {
            throw new NullPointerException("maker");
        }
        this.maker = maker;
    }

    /**
     * Given an id, returns a cached <code>IPair<IResource,RI></code>.
     * If there is no cached resource, returns <code>null</code>.
     * @param id the id of the resource to retrieve from cache.
     * @param attr a <code>Map</code> of attribute names to attribute values.
     * @return a cached <code>IPair<IResource,RI></code> or <code>null</code>
     * if the specified ID does not have a corresponding cached entry.
     */
    protected ResourceInformation<RI> getCached(Serializable id, Map<String,IEvalValue> attr) {
        return null;
    }

    /**
     * Saves the <code><code>IPair<IResource,RI></code></code> to cache.
     * @param data the <code><code>IPair<IResource,RI></code></code> to save
     * to cache.
     */
    protected void saveToCache(ResourceInformation<RI> data) {
    }

    /**
     * This is an implementation of the <code>ResourceInformation<RI></code>
     * used in the <code>AbstractResourceManager<RI></code>.
     *
     * @param <RI> The resource information type.
     */
    private static class ResourceInformationImpl<RI extends Serializable> implements ResourceInformation<RI>, Serializable{
        private static final long serialVersionUID = 1L;

        private final IMResource resource;

        private RI info;

        private ResourceInformationMaker<RI> maker;
        
        public ResourceInformationImpl() {
            resource=null;
        }

        public ResourceInformationImpl(IMResource resource, ResourceInformationMaker<RI> maker) {
            if (resource == null) {
                throw new NullPointerException("resource");
            }
            if (maker == null) {
                throw new NullPointerException("maker");
            }
            this.resource = resource;
            this.maker = maker;
        }

        public synchronized RI getInformation() {
            if (info == null && maker != null) {
                info = maker.makeResourceInformation();
                maker = null;
            }
            return info;
        }

        public IResource getResource() {
            return resource;
        }

        public IMResource getMutableResource() {
            return resource;
        }
    }

}
