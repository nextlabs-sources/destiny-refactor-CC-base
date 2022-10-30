/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class is a wrapper around a distinguished name. It performs trimming of
 * DNs, parsing of DN components (such as RDN, parent DN), replacement of
 * certain DN components etc.
 * 
 * @deprecated this file is same as com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.DistinguishedName
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/DistinguishedName.java#1 $
 */

@Deprecated
public class DistinguishedName {
    private static final char LDAP_ESCAPE_CHAR = '\\';
    private static final char LDAP_PATH_SEPERATOR = ',';

    RelativeDistinguishedName rdn;
    DistinguishedName parentDN;

    public DistinguishedName(String dn) throws InvalidDNException {
        this(splitPathToList(dn));
//        if (dn == null) {
//            throw new NullPointerException("dn is null");
//        }
//        String[] dnComponents = dn.split("[;,]", 2);
//        if (dnComponents == null || dnComponents.length == 0) {
//            throw new InvalidDNException("DN: '" + dn + "' is invalid");
//        }
//        String rdn = dnComponents[0];
//        this.rdn = new RelativeDistinguishedName(rdn);
//
//        if (dnComponents.length >= 2) {
//            String parentDN = dnComponents[1];
//            this.parentDN = new DistinguishedName(parentDN);
//        }
    }
    
    private DistinguishedName(Queue<String> dnComponents) throws InvalidDNException {
        String rdnStr = dnComponents.poll();
        assert rdn != null;
        this.rdn = new RelativeDistinguishedName(rdnStr);
        this.parentDN = !dnComponents.isEmpty() ? new DistinguishedName(dnComponents) : null;
    }
    
    public static LinkedList<String> splitPathToList(String dn) {
        LinkedList<String> strs = new LinkedList<String>();

        dn = dn.trim().toLowerCase();
        
        StringBuilder sb = new StringBuilder();
        boolean justEscaped = false;
        for (char c : dn.toCharArray()) {
            if (c == LDAP_PATH_SEPERATOR && !justEscaped) {
                strs.add(sb.toString());
                sb.setLength(0);
                justEscaped = false;
            } else {
                justEscaped = (c == LDAP_ESCAPE_CHAR && !justEscaped);
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            strs.add(sb.toString());
        }
        return strs;
    }
    
    public static String[] splitPath(String dn) {
        return splitPathToList(dn).toArray(new String[] {});
    }

    public static boolean isDNValid(String dnStr) {
        boolean isValid = true;
        try {
            new DistinguishedName(dnStr);
        } catch (InvalidDNException e) {
            isValid = false;
        }
        return isValid;
    }

    public DistinguishedName(RelativeDistinguishedName rdn, DistinguishedName parentDN) {
        this.rdn = rdn;
        this.parentDN = parentDN;
    }

    public RelativeDistinguishedName getRDN() {
        return this.rdn;
    }

    public DistinguishedName getParentDN() {
        return this.parentDN;
    }

    public void setParentDN(DistinguishedName parentDN) {
        this.parentDN = parentDN;
    }

    public void setRDN(RelativeDistinguishedName rdn) {
        this.rdn = rdn;
    }

    public String getAsString() {
        String dnStr = this.rdn.getAsString();
        if (this.parentDN != null) {
            dnStr += "," + this.parentDN.getAsString();
        }
        return dnStr;
    }

    public RelativeDistinguishedName[] getRDNArray() {
        List<RelativeDistinguishedName> rdnList = getRDNList();
        return rdnList.toArray(new RelativeDistinguishedName[rdnList.size()]);
    }

    protected List<RelativeDistinguishedName> getRDNList() {
        List<RelativeDistinguishedName> rdnList = new LinkedList<RelativeDistinguishedName>();
        rdnList.add(getRDN());
        if (getParentDN() != null) {
            rdnList.addAll(getParentDN().getRDNList());
        }
        return rdnList;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        boolean isEqual = false;
        if ((o != null) && (o instanceof DistinguishedName)) {
            DistinguishedName rhs = (DistinguishedName) o;
            boolean isRDNEqual = this.rdn.equals(rhs.rdn);
            if (isRDNEqual) {
                if (this.parentDN != null) {
                    isEqual = this.parentDN.equals(rhs.parentDN);
                } else if ((this.parentDN == null) && (rhs.parentDN == null)) {
                    isEqual = true;
                } else {
                    // This means that this.parent is null while rhs.parent is
                    // not null
                    isEqual = false;
                }
            }
        }
        return isEqual;
    }

    /**
     * Returns whether the current dn is a descendent of the parent.
     * 
     * @param potentialAncestorDN
     * @return
     */
    public boolean isDescendentOf(DistinguishedName potentialAncestorDN) {
        boolean isDescendent = false;
        if (this.equals(potentialAncestorDN)) {
            isDescendent = true;
        } else {
            DistinguishedName potentialDescendent = this;
            // Step through the current dn to see if the rest of it matches the
            // potential ancestor:
            while ((potentialDescendent.getParentDN() != null) 
                    && (!potentialDescendent.getParentDN().equals(potentialAncestorDN))) {
                potentialDescendent = potentialDescendent.getParentDN();
            }

            // If we're left-over with some of the dn that we were stepping
            // through, we have a match:
            if (potentialDescendent.getParentDN() != null) {
                isDescendent = true;
            }
        }
        return isDescendent;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (this.parentDN == null) {
            return this.rdn.hashCode();
        } else {
            return this.rdn.hashCode() ^ this.parentDN.hashCode();
        }
    }

    /**
     * This method removes a suffix DN from the given DN and returns the
     * truncated dn. The current dn is not affected.
     * 
     * @param suffix
     * @return
     */
    public DistinguishedName removeSuffix(DistinguishedName suffix) {
        DistinguishedName suffixDN = this;
        StringBuffer prefixDN = new StringBuffer();
        while ((suffixDN.getParentDN() != null) && (!suffixDN.getParentDN().equals(suffix))) {
            RelativeDistinguishedName nextRDN = suffixDN.getRDN();
            prefixDN.append(nextRDN.getAsString());
            prefixDN.append(",");
            suffixDN = suffixDN.getParentDN();
        }
        prefixDN.append(suffixDN.getRDN().getAsString());
        return new DistinguishedName(prefixDN.toString());
    }
}