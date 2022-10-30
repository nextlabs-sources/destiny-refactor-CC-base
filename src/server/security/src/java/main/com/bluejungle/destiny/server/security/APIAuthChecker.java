/*
 * Created on Jan 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security;

import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.security.IKeyManager;

/**
 * This handler is a generic handler verifying that a caller with a given
 * certificate can access a given API on a web service. Typically, callers can
 * access all APIs of a web service as long as their certificate is valid, but
 * in some cases, we want to make a distinction and give access only to some
 * APIs based on the caller. The handler uses configuration parameters to figure
 * out which certificate allows access to which API on the web service. By
 * default, if nothing is specified in the configuration, this handler denies
 * access to all callers regardless of their certificate. There are two ways to
 * specify the access list for a given certificate:
 * 
 * 1) Alias=API1 API2 API3 gives access only to API1 API2 and API3
 * 
 * 2) Alias= *- API1 gives access to all APIs but API1
 * 
 * The handler configuration specifies what API are allowed/denied for a given
 * certificate.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/security/com/bluejungle/destiny/server/security/APIAuthChecker.java#1 $
 */

public class APIAuthChecker extends BaseHandlerImpl {

    private static final String ALL_METHODS = "*";
    private static final String MINUS = "-";
    protected Map includedMethods = new HashMap();
    protected Map excludedMethods = new HashMap();

    /**
     * Cleans up the handler before its destruction.
     * 
     * @see org.apache.axis.Handler#cleanup()
     */
    public void cleanup() {
        super.cleanup();
        this.includedMethods.clear();
        this.excludedMethods.clear();
    }

    /**
     * Initialization method. This method walks through the option map and
     * populates the list of allowed APIs for different certificate aliases.
     * 
     * @see org.apache.axis.Handler#init()
     */
    public void init() {
        super.init();
        if (getOptions() == null) {
            // Everything will be denied!
            return;
        }

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);

        // Loads the certificate based on the configuration
        Set optionKeys = getOptions().keySet();
        Iterator it = optionKeys.iterator();
        while (it.hasNext()) {
            String aliasName = (String) it.next();
            PublicKey pubKey = keyManager.getPublicKey(aliasName);
            String accessList = (String) getOptions().get(aliasName);
            parseAccessList(pubKey, accessList);
        }
    }

    /**
     * This is the main handler invocation method.
     * 
     * @param context
     *            message context
     * @throws AxisFault
     *             if the invokation fails (e.g no request object)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext context) throws AxisFault {
        HttpServletRequest request = (HttpServletRequest) context.getProperty(HTTP_REQUEST_ATTR);

        if (request == null) {
            throw new AxisFault("Illegal message - no HTTP request provided");
        }

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(HTTP_REQUEST_CERT_ATTR);
        // If no certificate is given, security is not enabled and, the handler
        // should be simply bypassed.
        if (certs != null) {
            // Search the operation that is targeted
            String opName = context.getOperation().getName();

            // See if this operation is mapped to an alias that we know of
            int count = certs.length;
            for (int i = 0; i < count; i++) {
                X509Certificate cert = certs[i];
                PublicKey pubKey = cert.getPublicKey();
                if (isAccessAllowed(pubKey, opName)) {
                    return;
                }
            }
            // No certificate in the chain can be used to access this method
            throw new UnauthorizedCallerFault();
        }
    }

    /**
     * This method returns whether a certificate with a given public key can
     * access a given API or not. The method inspects the list of included or
     * excluded methods to figure out what answer to give.
     * 
     * @param key
     *            public key of the certificate
     * @param methodName
     *            API to be accessed
     * @return true if the certificate can access this API, false otherwise.
     */
    private boolean isAccessAllowed(PublicKey key, String methodName) {
        if (this.includedMethods.containsKey(key)) {
            // to allow access, the method has to be present in the set
            Set allowedMethods = (Set) this.includedMethods.get(key);
            return (allowedMethods.contains(methodName));
        } else if (this.excludedMethods.containsKey(key)) {
            // to allow access, the method should not be present in the set
            Set disallowedMethods = (Set) this.excludedMethods.get(key);
            return (!disallowedMethods.contains(methodName));
        } else {
            // the certificate is not found anywhere, by default, deny access
            return (false);
        }
    }

    /**
     * This method parses the access list given for a particular alias. Based on
     * the access list specification, it places the key in the relevant groups
     * (included or excluded)
     * 
     * @param key
     *            public key for the certificate alias
     * @param accessList
     *            String to parse, containing the access list specification
     */
    private void parseAccessList(PublicKey key, String accessList) {

        if (key == null || accessList == null) {
            return;
        }

        // Check whether the access list is based on inclusion or exclusion
        String exclusionPattern = ALL_METHODS + MINUS;
        int index = accessList.indexOf(exclusionPattern);
        if (index >= 0) {
            // This is an exclusion. Process excluded methods
            String excludedMethods = accessList.substring(index + exclusionPattern.length());
            StringTokenizer parser = new StringTokenizer(excludedMethods, SPACE);
            Set exclusions = new HashSet();
            int methodCount = parser.countTokens();
            for (int i = 0; i < methodCount; i++) {
                exclusions.add(parser.nextToken());
            }
            this.excludedMethods.put(key, exclusions);
        } else {
            // This is an inclusion. Process included methods
            StringTokenizer parser = new StringTokenizer(accessList, SPACE);
            Set inclusions = new HashSet();
            int methodCount = parser.countTokens();
            for (int i = 0; i < methodCount; i++) {
                inclusions.add(parser.nextToken());
            }
            this.includedMethods.put(key, inclusions);
        }
    }
}
