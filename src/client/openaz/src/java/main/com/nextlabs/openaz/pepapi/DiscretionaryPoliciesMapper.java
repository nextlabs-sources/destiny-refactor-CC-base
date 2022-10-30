/*
 * Created on May 10, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pepapi/DiscretionaryPoliciesMapper.java#1 $:
 */

package com.nextlabs.openaz.pepapi;

import org.apache.openaz.pepapi.MapperRegistry;
import org.apache.openaz.pepapi.ObjectMapper;
import org.apache.openaz.pepapi.PepConfig;
import org.apache.openaz.pepapi.PepRequest;
import org.apache.openaz.pepapi.PepRequestAttributes;

import com.nextlabs.openaz.utils.Constants;

public class DiscretionaryPoliciesMapper implements ObjectMapper {
    private MapperRegistry mapperRegistry;
    private PepConfig pepConfig;

    @Override
    public Class<?> getMappedClass() {
        return DiscretionaryPolicies.class;
    }

    @Override
    public void map(Object o, PepRequest pepRequest) {
        DiscretionaryPolicies policies = (DiscretionaryPolicies)o;

        PepRequestAttributes policiesAttributes = pepRequest.getPepRequestAttributes(DiscretionaryPolicies.CATEGORY_ID);
        policiesAttributes.addAttribute(Constants.ID_NEXTLABS_POD_POD_ID.stringValue(),
        		policies.getPql());
        policiesAttributes.addAttribute(Constants.ID_NEXTLABS_POD_IGNORE_BUILT_IN.stringValue(),
        		Boolean.toString(policies.getIgnorePDPPoliciesFlag()));
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
