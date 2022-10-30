/*
 * Created on Jan 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import java.io.IOException;

import com.novell.ldap.LDAPControl;
import com.novell.ldap.asn1.ASN1Integer;
import com.novell.ldap.asn1.ASN1Object;
import com.novell.ldap.asn1.ASN1OctetString;
import com.novell.ldap.asn1.ASN1Sequence;
import com.novell.ldap.asn1.LBERDecoder;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/dirsync/DirSyncResponseControl.java#1 $
 */

public class DirSyncResponseControl extends LDAPControl {

    private static final int MORE_DATA_INDEX = 0;
    private static final int COOKIE_INDEX = 2;

    private boolean moreDataAvailable;
    private byte[] cookie;

    /**
     * Constructor
     * 
     * @param oid
     * @param isCritical
     * @param value
     */
    public DirSyncResponseControl(String oid, boolean isCritical, byte[] value) throws IOException {
        super(oid, isCritical, value);

        // Parse the data:
        // Create a decoder objet
        LBERDecoder decoder = new LBERDecoder();
        ASN1Object asnObj = decoder.decode(value);
        if ((asnObj == null) || (!(asnObj instanceof ASN1Sequence)))
            throw new IOException("Decoding error. Control value in unrecognized format - '" + value + "'");

        ASN1Sequence sequence = (ASN1Sequence) asnObj;

        // First element must be an integer specifying if there is more data
        // available:
        ASN1Object asn1MoreData = sequence.get(MORE_DATA_INDEX);
        if ((asn1MoreData == null) || (!(asn1MoreData instanceof ASN1Integer)))
            throw new IOException("Decoding error. Could not decipher control sub-sequence value '0' - '" + asn1MoreData + "'");
        this.moreDataAvailable = ((ASN1Integer) asn1MoreData).intValue() != 0;

        // Third element must be an opaque octet string "cookie":
        ASN1Object asn1Cookie = sequence.get(COOKIE_INDEX);
        if ((asn1Cookie == null) || (!(asn1Cookie instanceof ASN1OctetString)))
            throw new IOException("Decoding error. Could not decipher control sub-sequence value '1' - '" + asn1Cookie + "'");
        this.cookie = ((ASN1OctetString) asn1Cookie).byteValue();
    }

    /**
     * Returns whether there is more data available for retrieval
     * 
     * @return true if there is more data available for retrieval
     */
    public boolean isMoreDataAvailable() {
        return this.moreDataAvailable;
    }

    /**
     * Returns the cookie to be used on the subsequent DirSync call
     * 
     * @return cookie to be used on the subsequent DirSync call
     */
    public byte[] getCookie() {
        return this.cookie;
    }
}