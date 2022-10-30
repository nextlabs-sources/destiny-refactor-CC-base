/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/RelativeDistinguishedName.java#1 $
 */

public class RelativeDistinguishedName {

    /*
     * Private variables:
     */
    private String rdnAttribute;
    private String rdnValue;

    public RelativeDistinguishedName(String fullRDN) throws InvalidDNException {
        String[] rdnComponents = fullRDN.split("=");
        if ((rdnComponents == null) || (rdnComponents.length != 2)) {
            throw new InvalidDNException("RDN :'" + fullRDN + "' is invalid");
        } else {
            this.rdnAttribute = rdnComponents[0].trim();
            this.rdnValue = rdnComponents[1].trim();
        }
    }

    public RelativeDistinguishedName(String rdnAttribute, String rdnValue) {
        this.rdnAttribute = rdnAttribute.trim();
        this.rdnValue = rdnValue.trim();
    }

    public String getRDNAttribute() {
        return this.rdnAttribute;
    }

    public String getRDNValue() {
        return this.rdnValue;
    }

    public String getAsString() {
        String fullRDN = this.rdnAttribute + "=" + this.rdnValue;
        return fullRDN;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        boolean isEqual = false;
        if ((o != null) && (o instanceof RelativeDistinguishedName)) {
            RelativeDistinguishedName rhs = (RelativeDistinguishedName) o;
            isEqual = this.rdnAttribute.equalsIgnoreCase(rhs.rdnAttribute);
            isEqual = isEqual && this.rdnValue.equalsIgnoreCase(rhs.rdnValue);
        }
        return isEqual;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.rdnAttribute.toLowerCase().hashCode() ^ this.rdnValue.toLowerCase().hashCode();
    }
}