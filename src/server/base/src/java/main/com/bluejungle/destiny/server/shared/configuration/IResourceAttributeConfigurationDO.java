/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.server.shared.configuration;

import java.util.List;

/**
 * This is the interface for defining custom resource attributes.
 * @author sergey
 */
public interface IResourceAttributeConfigurationDO {

    /**
     * Accesses the group of the attribute.
     * @return the name of the group to which the attribute belongs
     */
    String getGroupName();

    /**
     * Accesses the display name of the attribute.
     * @return the display name of the attribute.
     */
    String getDisplayName();

    /**
     * Accesses the PQL name of this attribute.
     * @return the PQL name of this attribute.
     */
    String getPqlName();

    /**
     * Accesses the name of the type of this attribute.
     * @return the name of the type of this attribute.
     */
    String getTypeName();

    /**
     * Accesses an optional list of compatible attributes.
     * @return an optional list of compatible attributes.
     */
    List<String> getAttributes();

    /**
     * Accesses an optional list of enumerated values
     * @return an optional list of enumerated values
     */
    List<String> getEnumeratedValues();

}
