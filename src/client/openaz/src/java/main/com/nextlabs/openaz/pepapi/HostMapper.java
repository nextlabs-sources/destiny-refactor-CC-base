/*
 * Created on May 10, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pepapi/HostMapper.java#1 $:
 */

package com.nextlabs.openaz.pepapi;

import org.apache.openaz.pepapi.MapperRegistry;
import org.apache.openaz.pepapi.ObjectMapper;
import org.apache.openaz.pepapi.PepConfig;
import org.apache.openaz.pepapi.PepRequest;
import org.apache.openaz.pepapi.PepRequestAttributes;

import com.nextlabs.openaz.utils.Constants;

public final class HostMapper implements ObjectMapper {
    private MapperRegistry mapperRegistry;
    private PepConfig pepConfig;
    
    @Override
    public Class<?> getMappedClass() {
        return Host.class;
    }

    @Override
    public void map(Object o, PepRequest pepRequest) {
        Host h = (Host)o;

        PepRequestAttributes hostAttributes = pepRequest.getPepRequestAttributes(Host.CATEGORY_ID);
        if (h.getIPAddress() != null) {
            hostAttributes.addAttribute(Constants.ID_NEXTLABS_HOST_IP_ADDR.stringValue(), h.getIPAddress());
        }

        if (h.getHostName() != null) {
            hostAttributes.addAttribute(Constants.ID_NEXTLABS_HOST_NAME.stringValue(), h.getHostName());
            hostAttributes.addAttribute(Constants.ID_NEXTLABS_HOST_IP_ADDR.stringValue(), Integer.toString(Host.LOCAL_HOST));
        }
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
