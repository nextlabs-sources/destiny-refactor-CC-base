/*
 * Created on Dec 16, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security;

import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.security.IKeyManager;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import javax.servlet.http.HttpServletRequest;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This is the certification checker implementation class. The security handler
 * checks that the caller is authorized to call into the given web service. It
 * loads one or more trusted certificates from a truststore and makes sure that
 * the caller is using one of these certificates to talk to the server.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/security/com/bluejungle/destiny/server/security/CertificateChecker.java#1 $:
 */

public class CertificateChecker extends BaseHandlerImpl {

    private static final String TRUSTED_CERTS_OPTION_NAME = "trustedCerts";
    private Set trustedKeys;

    /**
     * This is the initialization function. The list of trusted certificates is
     * loaded for later use.
     * 
     * @see org.apache.axis.Handler#init()
     */
    public void init() {
        super.init();
        this.trustedKeys = new HashSet();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);

        // Extracts the aliases specified in the trusted certificate list
        String[] aliases = getTrustedCerts();
        for (int i = 0; i < aliases.length; i++) {
            if (keyManager.containsPublicKey(aliases[i])) {
                PublicKey pubKey = keyManager.getPublicKey(aliases[i]);
                // Store the public key separately for faster lookup
                this.trustedKeys.add(pubKey);
                getLog().debug("Added certificate " + aliases[i] + " in the list of trusted callers");
            }
        }
    }

    /**
     * This function parses the certificate list given in the handler options.
     * The names represent aliases inside the keystore.
     * 
     * @return an array of trusted aliases names
     */
    private String[] getTrustedCerts() {
        String[] names = null;

        String certs = (String) getOptions().get(TRUSTED_CERTS_OPTION_NAME);
        if (certs == null) {
            getLog().warn("No trusted certificates found in the handler options");
            names = new String[0];
        } else {
            // Parse the list of aliases. For now, we assume there are separated
            // by
            // either a space or a comma.

            StringTokenizer parser = new StringTokenizer(certs, SPACE);
            int count = parser.countTokens();
            names = new String[count];
            for (int i = 0; i < count; i++) {
                names[i] = parser.nextToken();
            }
        }

        return (names);
    }

    /**
     * This method is called when the handler is destroyed.
     * 
     * @see org.apache.axis.Handler#cleanup()
     */
    public void cleanup() {
        super.cleanup();
        this.trustedKeys.clear();
    }

    /**
     * This is the main handler method. This methods extracts the certificate
     * from the caller. If the certificate matches one of the trusted ones, then
     * the message will proceed, otherwise an exception is thrown back to the
     * caller.
     * 
     * @param context
     *            message context
     * @throws AxisFault
     *             if the certificate used for the request is not in the trusted
     *             list of issuers for this service.
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext context) throws AxisFault {
        HttpServletRequest request = (HttpServletRequest) context.getProperty(HTTP_REQUEST_ATTR);

        if (request == null) {
            throw new AxisFault("Illegal message - no HTTP request provided");
        }

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(HTTP_REQUEST_CERT_ATTR);

        // if there is no certificate provided at all, the security may not
        // be enabled at all
        // If the code reached here, it means that the basic SSL handshake
        // already succeeded, which means that the certificate was in the
        // trustore. So, it is safe to assume that if the certificate chain
        // is empty, the security is not enabled.
        if (certs != null) {
            // Now, walk the certificate chain until one match is found
            int count = certs.length;
            for (int i = 0; i < count; i++) {
                X509Certificate cert = certs[i];
                if (this.trustedKeys.contains(cert.getPublicKey())) {
                    // All set!
                    return;
                }
            }

            // Did not find the trusted certificate in the list, reject this
            // request
            throw new UnauthorizedCallerFault();
        } else {
            getLog().warn("No X509 certificate found in the request from the caller. Your system may be running without secure connectors. Please check your configuration settings.");
        }
    }
}
