/*
 * Created on Nov 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import java.util.Stack;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/impl/ADUtils.java#1 $
 */

public class ADUtils {
	
	private static final String DOMAIN_COMPONENT_ATTRIBUTE = "dc";

    /**
     * Constructor
     *  
     */
    private ADUtils() {
        super();
    }

    /**
     * This code is specific to Active Directory. Given a distinguished name, it
     * returns the domain that it belongs to
     * 
     * @param dnStr
     * @throws IllegalArgumentException
     *             if the dn is not in the correct format
     * @return
     */
    public static String extractADDomainFromRootDN(String dnStr) throws IllegalArgumentException {
        if (dnStr == null) {
            throw new NullPointerException("dn is null");
        }
        DistinguishedName dn = new DistinguishedName(dnStr);

        RelativeDistinguishedName[] rdnArr = dn.getRDNArray();
        Stack<String> domainComponents = new Stack<String>();
        boolean bFirst = true;
        boolean domainSequenceEnded = false;

        // Continue until the domain sequence ends or the dn is completely
        // parsed:
        for (int i = rdnArr.length - 1; ((i >= 0) && (!domainSequenceEnded)); i--, bFirst = false) {
            RelativeDistinguishedName rdn = rdnArr[i];
            if (bFirst) {
                if (rdn.getRDNAttribute().equalsIgnoreCase(DOMAIN_COMPONENT_ATTRIBUTE)) {
                    domainComponents.push(rdn.getRDNValue());
                } else {
                    throw new IllegalArgumentException("active directory dn: '" + dn.getAsString() + "' does not have '" + DOMAIN_COMPONENT_ATTRIBUTE + "' as its most significant attribute");
                }
            } else {
                if (rdn.getRDNAttribute().equalsIgnoreCase(DOMAIN_COMPONENT_ATTRIBUTE)) {
                    domainComponents.push(".");
                    domainComponents.push(rdn.getRDNValue());
                } else {
                    domainSequenceEnded = true;
                }
            }
        }

        // Unwind the stack and create the domain name:
        StringBuffer domainBuffer = new StringBuffer();
        while (!domainComponents.empty()) {
            domainBuffer.append(domainComponents.pop());
        }
        return domainBuffer.toString().toLowerCase();
    }
    
    /**
     * Constructs the root DN for a given Active Directory domain name. This is
     * possible because a domain name "x.y.z" will have a DC of
     * "dc=x,dc=y,dc=z".
     * 
     * @param domainName
     * @return
     */
    public static String constructDomainRootDN(String domainName) {
        StringBuffer rootDNBuff = new StringBuffer();
        String[] dnComps = domainName.split("[.]");
        for (int i = 0; i < dnComps.length; i++) {
            String comp = dnComps[i].trim();
            if (i > 0) {
                rootDNBuff.append(",dc=" + comp);
            } else {
                rootDNBuff.append("dc=" + comp);
            }
        }
        return rootDNBuff.toString();
    }
}