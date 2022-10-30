/*
 * Created on May 09, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pepapi/Recipient.java#1 $:
 */

package com.nextlabs.openaz.pepapi;

import java.util.HashMap;
import java.util.Map;

import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.XACML3;

import com.nextlabs.openaz.utils.Constants;

/**
 * Container class defining a Recipient (typically an email recipient) and associated
 * attributes.
 *
 * The associated mapper is {@link com.nextlabs.openaz.pepapi.RecipientMapper}
 */
public final class Recipient {
    public static final Identifier CATEGORY_ID = XACML3.ID_SUBJECT_CATEGORY_RECIPIENT_SUBJECT;
        
    private String recipientId;
    private String[] recipientEmails;

    private final Map<String, Object[]> attributeMap;
    
    private Recipient() {
        this.attributeMap = new HashMap<String, Object[]>();
    }

    /**
     * Get the id of this recipient
     *
     * @return
     */
    public String getRecipientId() {
        return recipientId;
    }
    
    /**
     * Get recipient emails
     * @return
     */
    public String[] getRecipientEmails() {
    	return recipientEmails;
    }

    /**
     * Creates a new Recipient instance with the given id 
     *
     * @param recipientId the identifier of the recipient
     * @return
     */
    public static Recipient newInstance(String recipientId) {
        Recipient recipient = new Recipient();
        recipient.recipientId = recipientId;
        return recipient;
    }
    
    /**
     * Create a new Recipient instance with emails
     * 
     * @param emails
     * @return
     */
    public static Recipient newInstance(String... emails) {
    	Recipient recipient = new Recipient();
    	recipient.recipientEmails = emails;
    	return recipient;
    }
    
    private final void addToMap(String id, Object[] values) {
        if (values != null && values.length > 0) {
            attributeMap.put(id, values);
        } else {
            throw new IllegalArgumentException("Values can not be null or empty");
        }
    }

    /**
     * Add a new attribute with the given id and one or more String values. If this
     * id has already been assigned a value it will be overwritten.
     *
     * @param id the attribute name
     * @param values one or more values to be associated with this id
     */
    public void addAttribute(String id, String... values) {
        addToMap(id, values);
    }

    Map<String, Object[]> getAttributeMap() {
        return attributeMap;
    }

    String resolveAttributeId(String attributeId) {
        return attributeId;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object[]> e : attributeMap.entrySet()) {
            builder.append("Attribute Id: " + e.getKey());
            builder.append(", Attribute Values: ");
            for (Object o : e.getValue()) {
                builder.append(o.toString() + ", ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
