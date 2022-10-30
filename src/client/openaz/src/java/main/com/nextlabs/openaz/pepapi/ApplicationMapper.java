/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pepapi;

import org.apache.openaz.pepapi.MapperRegistry;
import org.apache.openaz.pepapi.ObjectMapper;
import org.apache.openaz.pepapi.PepConfig;
import org.apache.openaz.pepapi.PepRequest;
import org.apache.openaz.pepapi.PepRequestAttributes;

import com.nextlabs.openaz.utils.Constants;

public class ApplicationMapper implements ObjectMapper {
	
	private MapperRegistry mapperRegistry;
    private PepConfig pepConfig;
    
	@Override
	public Class<?> getMappedClass() {
		return Application.class;
	}

	@Override
	public void map(Object o, PepRequest pepRequest) {
		Application application = (Application)o;
		PepRequestAttributes applicationAttributes = pepRequest.getPepRequestAttributes(Application.CATEGORY_ID);
		applicationAttributes.addAttribute(
				Constants.ID_NEXTLABS_APPLICATION_APPLICATION_ID.stringValue(),
				application.getApplicationID());
	}

	@Override
	public void setMapperRegistry(MapperRegistry mapperRegistry) {
		this.mapperRegistry = mapperRegistry;
	}

	@Override
	public void setPepConfig(PepConfig pepConfig) {
		this.pepConfig = pepConfig;
	}
	
}
