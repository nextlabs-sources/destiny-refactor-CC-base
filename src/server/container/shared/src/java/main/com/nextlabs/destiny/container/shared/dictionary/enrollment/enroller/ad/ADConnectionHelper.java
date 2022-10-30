/*
 * Created on May 27, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/ad/ADConnectionHelper.java#1 $:
 */

package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.io.IOException;
import javax.net.ssl.SSLSocketFactory;

import com.nextlabs.framework.ssl.ConfigurableSSLSocketFactory;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPJSSEStartTLSFactory;

public class ADConnectionHelper {
    public enum ConnectionType { SSL, TLS, UNENCRYPTED };

    public static LDAPConnection createConnection(ConnectionType type, boolean alwaysTrustAD) throws IOException {
        SSLSocketFactory sslSocketFactory = getSocketFactory(alwaysTrustAD);
        
        if (type == ConnectionType.SSL) {
            return new LDAPConnection(new LDAPJSSESecureSocketFactory(sslSocketFactory));
        } else if (type == ConnectionType.TLS) {
            return new LDAPConnection(new LDAPJSSEStartTLSFactory(sslSocketFactory));
        } else {
            return new LDAPConnection();
        }
    }

    private static SSLSocketFactory getSocketFactory(boolean alwaysTrustServer) throws IOException {
        ConfigurableSSLSocketFactory sslSocketFactory = new ConfigurableSSLSocketFactory();
        
        if (alwaysTrustServer) {
            sslSocketFactory.removeTrustStore();
        }

        sslSocketFactory.init();
        
        return sslSocketFactory;
    }
}
