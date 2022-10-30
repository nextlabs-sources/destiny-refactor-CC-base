/*
 * Created on Nov 5, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.epicenter.resource;

import java.io.Serializable;

import com.bluejungle.pf.domain.epicenter.resource.IResourceManager.ResourceInformationMaker;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/resource/AbstractResourceInformationMaker.java#1 $
 */

public class AbstractResourceInformationMaker<RI extends Serializable> implements
		ResourceInformationMaker<RI>, Serializable {
	private static final long serialVersionUID = 1L;
	private final Class<RI> clazz;

	public AbstractResourceInformationMaker(Class<RI> clazz) {
		this.clazz = clazz;
	}

	public RI makeResourceInformation() {
		try {
			return clazz.newInstance();
		} catch (IllegalAccessException iae) {
			return null;
		} catch (InstantiationException e) {
			return null;
		}
	}
}
