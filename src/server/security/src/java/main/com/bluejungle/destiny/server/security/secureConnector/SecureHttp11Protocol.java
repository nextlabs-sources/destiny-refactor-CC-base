/*
 * Created on Dec 28, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security.secureConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sgoldstein
 */

public class SecureHttp11Protocol extends SecurePasswordHttp11Protocol {

    private static final String KEYSTORE_SYSTEM_PROPERTY = "nextlabs.javax.net.ssl.keyStore";
    private static final String KEYSTORE_PASSWORD_SYSTEM_PROPERTY = "nextlabs.javax.net.ssl.keyStorePassword";
    private static final String TRUSTSTORE_SYSTEM_PROPERTY = "nextlabs.javax.net.ssl.trustStore";
    private static final String TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY = "nextlabs.javax.net.ssl.trustStorePassword";

    /**
     * @see com.bluejungle.destiny.server.secureConnector.SecurePasswordHttp11Protocol#setKeystorePass(java.lang.String)
     */
    @Override
    public void setKeystorePass(String cryptedPass) {
        super.setKeystorePass(cryptedPass);
        System.setProperty(KEYSTORE_PASSWORD_SYSTEM_PROPERTY, super.getKeystorePass());
    }
    

    /**
     * @see org.apache.coyote.http11.Http11BaseProtocol#setKeystoreFile(java.lang.String)
     */
    @Override
    public void setKeystoreFile(String file) {
        super.setKeystoreFile(file);
        System.setProperty(KEYSTORE_SYSTEM_PROPERTY, super.getKeystoreFile());
    }

    /**
     * @see com.bluejungle.destiny.server.secureConnector.SecurePasswordHttp11Protocol#setTruststorePass(java.lang.String)
     */
    @Override
    public void setTruststorePass(String cryptedPass) {
        super.setTruststorePass(cryptedPass);
        System.setProperty(TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, super.getTruststorePass());
    }

    /**
     * @see org.apache.coyote.http11.Http11BaseProtocol#setTruststoreFile(java.lang.String)
     */
    @Override
    public void setTruststoreFile(String file) {
        super.setTruststoreFile(file);
        System.setProperty(TRUSTSTORE_SYSTEM_PROPERTY, super.getTruststoreFile());
    }
    
    /**
     * @see com.bluejungle.destiny.server.secureConnector.SecurePasswordHttp11Protocol#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public boolean setProperty(String name, String value) {
        boolean ret = super.setProperty(name, value);

        return ret;
    }
}
