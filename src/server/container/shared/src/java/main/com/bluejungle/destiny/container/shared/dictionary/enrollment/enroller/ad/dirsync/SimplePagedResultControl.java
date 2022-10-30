package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import com.novell.ldap.LDAPControl;

import com.novell.ldap.asn1.ASN1Integer;
import com.novell.ldap.asn1.ASN1OctetString;
import com.novell.ldap.asn1.ASN1Sequence;
import com.novell.ldap.asn1.LBEREncoder;

/**
 * http://www.ietf.org/rfc/rfc2696.txt
 * 
 */
public class SimplePagedResultControl extends LDAPControl {

    /*
     * Default control parameters:
     */
    public static final String OID = "1.2.840.113556.1.4.319";
    private static final boolean IS_CRITICAL = true;
    private final ASN1Integer pageSize;
    private final ASN1OctetString cookie;

    /*
     * Control sequence indices:
     */
    static final int SEQUENCE_SIZE = 2;
    static final int PAGE_SIZE_INDEX = 0;
    static final int COOKIE_INDEX = 1;

    /*
     * Encoder:
     */
    private static LBEREncoder ENCODER = new LBEREncoder();

    /*
     * We register the control response
     */
    static {
        /*
         * Register the Paged Result Response control class which is returned by
         * the server in response to a Paged Result request
         */
        LDAPControl.register(OID, SimplePagedResultResponseControl.class);
    }

    /*
     * Private members:
     */
    private ASN1Sequence sequence = new ASN1Sequence(SEQUENCE_SIZE);

    /**
     * Constructor
     * 
     * @param cookie
     */
    public SimplePagedResultControl(int pageSize, byte[] cookie) {
        super(OID, IS_CRITICAL, null);
        // Create the control sequence:
        this.pageSize = new ASN1Integer(pageSize);
		this.cookie = new ASN1OctetString(cookie != null ? cookie : new byte[] {});
		
        this.sequence.add(this.pageSize);
        this.sequence.add(this.cookie);
        setValue();
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

    public void updateCookie(byte[] cookie) {
        this.sequence.set(COOKIE_INDEX,	new ASN1OctetString(cookie != null ? cookie : new byte[] {}));
        setValue();
    }

    public void updatePageSize(int pageSize) {
        this.sequence.set(PAGE_SIZE_INDEX, new ASN1Integer(pageSize));
        setValue();
    }
}