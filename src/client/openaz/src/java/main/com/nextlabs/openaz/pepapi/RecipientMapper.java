/*
 * Created on May 09, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pepapi/RecipientMapper.java#1 $:
 */

package com.nextlabs.openaz.pepapi;

import java.util.Map;

import org.apache.openaz.pepapi.MapperRegistry;
import org.apache.openaz.pepapi.ObjectMapper;
import org.apache.openaz.pepapi.PepConfig;
import org.apache.openaz.pepapi.PepRequest;
import org.apache.openaz.pepapi.PepRequestAttributes;

import com.nextlabs.openaz.utils.Constants;

public class RecipientMapper implements ObjectMapper {
    private PepConfig pepConfig;
    private MapperRegistry mapperRegistry;

    @Override
    public Class<?> getMappedClass() {
        return Recipient.class;
    }

    @Override
    public void map(Object o, PepRequest pepRequest) {
        Recipient recipient = (Recipient)o;

        PepRequestAttributes recipientAttributes = pepRequest.getPepRequestAttributes(Recipient.CATEGORY_ID);
        
        if(recipient.getRecipientId() != null) {
        	recipientAttributes.addAttribute(Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_ID.stringValue(),
        			recipient.getRecipientId());
        }
        if(recipient.getRecipientEmails() != null) {
        	recipientAttributes.addAttribute(Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_EMAIL.stringValue(),
        			recipient.getRecipientEmails());
        }
        
        Map<String, Object[]> attributeMap = recipient.getAttributeMap();

        for (Map.Entry<String, Object[]> e : attributeMap.entrySet()) {
            recipientAttributes.addAttribute(e.getKey(), (String[])e.getValue());
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
