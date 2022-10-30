/**
 * Created on Jan 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import com.novell.ldap.LDAPControl;
import com.novell.ldap.asn1.ASN1Integer;
import com.novell.ldap.asn1.ASN1OctetString;
import com.novell.ldap.asn1.ASN1Sequence;
import com.novell.ldap.asn1.LBEREncoder;

/**
 * DirSync control for Active Directory
 * 
 * Note that if we access a Windows 2000 Active Directory server, we will need
 * special rights to be able to use this control. On Win2K 2003, we don't need
 * that thanks to a flag setting below.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/dirsync/DirSyncControl.java#1 $
 */

public class DirSyncControl extends LDAPControl {

    /*
     * In Windows 2003 Server, if this flag is not present, the caller must have
     * the replicate changes right. If this flag is present, the caller requires
     * no rights, but can only view objects and attributes accessible to the
     * caller. In Windows 2000 Server, this is not supported.
     */
    private static final int LDAP_DIRSYNC_OBJECT_SECURITY = 1;

    /*
     * Return parent objects before child objects, when parent objects would
     * otherwise appear later in the replication stream.
     */
    private static final int LDAP_DIRSYNC_ANCESTORS_FIRST_ORDER = 2048;

    /*
     * Do not return private data in the search results.
     */
    private static final int LDAP_DIRSYNC_PUBLIC_DATA_ONLY = 8192;

    /*
     * In Windows 2003 Server, if this flag is not present, all of the values,
     * up to a server-specified limit, in a multi-valued attribute are returned
     * when any value changes. If this flag is present, only the changed values
     * are returned. In Windows 2000 Server, this is not supported.
     */
    private static final long LDAP_DIRSYNC_INCREMENTAL_VALUES = 2147483648L;

    /*
     * Default control parameters:
     */
    private static final String OID = "1.2.840.113556.1.4.841";
    private static final boolean IS_CRITICAL = true; // Always set the criticality to TRUE
    private static final ASN1Integer FLAGS = new ASN1Integer( LDAP_DIRSYNC_OBJECT_SECURITY | LDAP_DIRSYNC_ANCESTORS_FIRST_ORDER | LDAP_DIRSYNC_PUBLIC_DATA_ONLY );
    private static final ASN1Integer MAX_ATTRIBUTE_COUNT = new ASN1Integer(0x7FFFFFFF);

    /*
     * Control sequence indices:
     */
    private static final int SEQUENCE_SIZE = 3;
    private static final int FLAGS_INDEX = 0;
    private static final int MAX_ATTRIBUTE_COUNT_INDEX = 1;
    private static final int COOKIE_INDEX = 2;

    /*
     * Encoder:
     */
    private static LBEREncoder ENCODER = new LBEREncoder();

    /*
     * We register the control response
     */
    static {
        /*
         * Register the DirSync Response control class which is returned by the
         * server in response to a DirSync request
         */
        try {
            LDAPControl.register(OID, Class.forName(DirSyncResponseControl.class.getName()));
        } catch (ClassNotFoundException e) {
        }
    }

    /*
     * Private members:
     */
    private ASN1Sequence sequence = new ASN1Sequence();

    /**
     * Constructor
     * 
     * @param cookie
     */
    public DirSyncControl() {
        super(OID, IS_CRITICAL, null);

        // Create the control sequence:
        this.sequence.add(FLAGS);
        this.sequence.add(MAX_ATTRIBUTE_COUNT);
        this.sequence.add(new ASN1OctetString(new byte[] {}));
        setValue();
    }

    public void updateCookie(byte[] cookie) {
        this.sequence.set(COOKIE_INDEX, new ASN1OctetString(cookie == null ? (new byte[] {}) : cookie));
        setValue();
    }
    
    public void setControlValue(byte[] value) {
    	super.setValue(value);
    }
    
    public String toString() {
        byte[] data = this.sequence.getEncoding(ENCODER);
        StringBuffer buf = new StringBuffer(data.length);
        for (int i = 0; i < data.length; i++) {
            buf.append(Byte.toString(data[i]));
            if (i < data.length - 1)
                buf.append(",");
        }
        return buf.toString();
    }

    /**
     * Sets the encoded value of the LDAPControlClass
     */
    private void setValue() {
        super.setValue(this.sequence.getEncoding(ENCODER));
    }
}
