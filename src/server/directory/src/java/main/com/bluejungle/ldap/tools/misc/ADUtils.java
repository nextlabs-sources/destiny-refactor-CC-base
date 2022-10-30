/*
 * Created on Nov 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.misc;

import java.util.Stack;

import com.bluejungle.ldap.tools.ldifconverter.impl.DistinguishedName;
import com.bluejungle.ldap.tools.ldifconverter.impl.RelativeDistinguishedName;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/misc/ADUtils.java#1 $
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
        Stack domainComponents = new Stack();
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
            domainBuffer.append((String) domainComponents.pop());
        }
        return domainBuffer.toString().toLowerCase();
    }
}