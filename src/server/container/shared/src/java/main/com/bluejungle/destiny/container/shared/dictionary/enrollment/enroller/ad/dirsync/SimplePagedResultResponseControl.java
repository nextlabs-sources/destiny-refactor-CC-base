package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import java.io.IOException;

import com.novell.ldap.LDAPControl;
import com.novell.ldap.asn1.ASN1Integer;
import com.novell.ldap.asn1.ASN1Object;
import com.novell.ldap.asn1.ASN1OctetString;
import com.novell.ldap.asn1.ASN1Sequence;
import com.novell.ldap.asn1.LBERDecoder;

public class SimplePagedResultResponseControl extends LDAPControl {

    private int estimatedResultSetSize;
    private byte[] cookie;

    /**
     * Constructor
     * 
     * @param rawControl
     * @throws IOException
     */
    public SimplePagedResultResponseControl(LDAPControl rawControl) throws IOException {
        super(rawControl.getID(), rawControl.isCritical(), rawControl.getValue());

        parseValue(rawControl.getValue());
    }

    /**
     * Constructor
     * 
     * @param oid
     * @param isCritical
     * @param value
     */
    public SimplePagedResultResponseControl(String oid, boolean isCritical, byte[] value) throws IOException {
        super(oid, isCritical, value);

        parseValue(value);
    }

    /**
     * @param value
     * @throws IOException
     */
    protected void parseValue(byte[] value) throws IOException {
        // Parse the data:
        // Create a decoder objet
        LBERDecoder decoder = new LBERDecoder();
        ASN1Object asnObj = decoder.decode(value);
        if ((asnObj == null) || (!(asnObj instanceof ASN1Sequence)))
            throw new IOException("Decoding error. Control value in unrecognized format - '" + value + "'");

        ASN1Sequence sequence = (ASN1Sequence) asnObj;

        // First element must be an integer specifying the estimated page result
        // size:
        ASN1Object asn1EstimatedResultSetSize = sequence.get(SimplePagedResultControl.PAGE_SIZE_INDEX);
        if ((asn1EstimatedResultSetSize == null) || (!(asn1EstimatedResultSetSize instanceof ASN1Integer)))
            throw new IOException("Decoding error. Could not decipher control sub-sequence value '0' - '" + asn1EstimatedResultSetSize + "'");
        this.estimatedResultSetSize = ((ASN1Integer) asn1EstimatedResultSetSize).intValue();

        // Third element must be an opaque octet string "cookie":
        ASN1Object asn1Cookie = sequence.get(SimplePagedResultControl.COOKIE_INDEX);
        if ((asn1Cookie == null) || (!(asn1Cookie instanceof ASN1OctetString)))
            throw new IOException("Decoding error. Could not decipher control sub-sequence value '1' - '" + asn1Cookie + "'");
        this.cookie = ((ASN1OctetString) asn1Cookie).byteValue();
    }

    /**
     * Returns the estimated page-result size
     * 
     * @return the estimated page-result size
     */
    public int getEstimatedPageResultSize() {
        return this.estimatedResultSetSize;
    }

    /**
     * Returns the cookie to be used on the subsequent paged-result call
     * 
     * @return cookie to be used on the subsequent paged-result call
     */
    public byte[] getCookie() {
        return this.cookie;
    }

    /**
     * Returns whether there are more pages remaining
     * 
     * @return whether these are more pages remaining
     */
    public boolean hasMorePages() {
        if ((cookie != null) && (cookie.length > 0)) {
            return true;
        } else {
            return false;
        }
    }
}
