/*
 * Created on Dec 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.security.IKeyManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Agent Key Manager Component Builder. Encapsulates all knowledge required
 * to setup the agent keymanager when the agent is in either a registered or
 * non-registered
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/IAgentKeyManagerComponentBuilder.java#1 $
 */

public interface IAgentKeyManagerComponentBuilder {

    public static final String COMPONENT_NAME = IAgentKeyManagerComponentBuilder.class.getName();
    public static final String KEYSTORE_NAME = "keystore";
    public static final String SECRET_KEYSTORE_NAME = "secretKeystore";
    public static final String TRUSTSTORE_NAME = "truststore";

    /**
     * Build the non-registered key manager component with the name
     * {@link IKeyManager#COMPONENT_NAME}
     */
    public abstract void buildNonregisteredKeyManager();

    /**
     * Build the non-registered key manager component with the name
     * {@link IKeyManager#COMPONENT_NAME}
     */
    public abstract void buildRegisteredKeyManager();

    /**
     * Update the registered keystore file
     * 
     * @param keystoreStream
     *            the keystore contents
     * @throws IOException
     */
    public void updateRegisteredKeystore(InputStream keystoreStream) throws IOException;

    /**
     * Update the registered truststore file
     * 
     * @param truststoreStream
     *            the truststore content
     * @throws IOException
     */
    public void updateRegisteredTruststore(InputStream truststoreStream) throws IOException;

    /**
     * Update the registered keystore password
     * 
     * @param password
     * @throws IOException 
     */
    public void updateRegisteredKeystorePassword(String password) throws IOException;

}