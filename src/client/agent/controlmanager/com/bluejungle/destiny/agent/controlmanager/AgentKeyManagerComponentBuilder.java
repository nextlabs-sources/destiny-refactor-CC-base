/*
 * Created on Dec 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.security.KeyManagerImpl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates creation of Key Managers for the Agent
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/AgentKeyManagerComponentBuilder.java#1 $
 */

public class AgentKeyManagerComponentBuilder implements IAgentKeyManagerComponentBuilder, IConfigurable {

    public static final PropertyKey<String> AGENT_HOME_DIRECTORY_PROPERTY_NAME = new PropertyKey<String>("agentHome");

    private static final String RELATIVE_TEMP_KEYSTORE_FILE = "/config/security/temp_agent-keystore.jks";
    private static final String TEMP_KEYSTORE_FILE_PASSWORD = "password";
    private static final String RELATIVE_KEYSTORE_FILE = "/config/security/agent-keystore.jks";
    private static final String RELATIVE_TRUSTSTORE_FILE = "/config/security/agent-truststore.jks";
    private static final String RELATIVE_PASSWORD_FILE = "/config/config.dat";
    private static final String RELATIVE_SECRET_KEYSTORE_FILE = "/config/security/agent-secret-keystore.jceks";

    private IConfiguration configuration;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        if (config == null) {
            throw new NullPointerException("config cannot be null.");
        }

        this.configuration = config;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder#buildNonregisteredKeyManager()
     */
    public void buildNonregisteredKeyManager() {
        String agentHome = getAgentHome();

        String keystoreLocation = agentHome + RELATIVE_TEMP_KEYSTORE_FILE;
        String keystorePassword = TEMP_KEYSTORE_FILE_PASSWORD;
        Set<KeyManagerImpl.KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeyManagerImpl.KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo(IAgentKeyManagerComponentBuilder.KEYSTORE_NAME, keystoreLocation, "jks", keystorePassword));

        HashMapConfiguration keyManagerConfiguration = new HashMapConfiguration();
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);

        ComponentInfo<KeyManagerImpl> keyManagerComponentInfo = new ComponentInfo<KeyManagerImpl>(
        		IKeyManager.COMPONENT_NAME, 
        		KeyManagerImpl.class, 
        		IKeyManager.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		keyManagerConfiguration);
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        manager.registerComponent(keyManagerComponentInfo, true);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder#buildRegisteredKeyManager()
     */
    public void buildRegisteredKeyManager() {
        String agentHome = getAgentHome();

        String truststoreLocation = agentHome + RELATIVE_TRUSTSTORE_FILE;
        String keystoreLocation = agentHome + RELATIVE_KEYSTORE_FILE;
        String secretKeystoreLocation = agentHome + RELATIVE_SECRET_KEYSTORE_FILE;
        
        String storePassword = getKeystorePassword();
        
        Set<KeyManagerImpl.KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeyManagerImpl.KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo(IAgentKeyManagerComponentBuilder.KEYSTORE_NAME, keystoreLocation, "jks", storePassword));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo(IAgentKeyManagerComponentBuilder.TRUSTSTORE_NAME, truststoreLocation, "jks", storePassword));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo(IAgentKeyManagerComponentBuilder.SECRET_KEYSTORE_NAME, secretKeystoreLocation, "jceks", storePassword));

        HashMapConfiguration keyManagerConfiguration = new HashMapConfiguration();
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        ComponentInfo<KeyManagerImpl> keyManagerComponentInfo = new ComponentInfo<KeyManagerImpl>(
        		IKeyManager.COMPONENT_NAME, 
        		KeyManagerImpl.class, 
        		IKeyManager.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		keyManagerConfiguration);
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        if (manager.isComponentRegistered(IKeyManager.COMPONENT_NAME)) {
            IKeyManager currentKeyManager = (IKeyManager) manager.getComponent(IKeyManager.COMPONENT_NAME);
            manager.releaseComponent(currentKeyManager);
        }
        manager.registerComponent(keyManagerComponentInfo, true);
    }

    /**
     * @throws IOException 
     * @see com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder#updateRegisteredKeystore(java.io.InputStream)
     */
    public void updateRegisteredKeystore(InputStream keystoreStream) throws IOException {
        if (keystoreStream == null) {
            throw new NullPointerException("keystoreStream cannot be null.");
        }
        
        String agentHome = getAgentHome();
        String keystoreLocation = agentHome + RELATIVE_KEYSTORE_FILE;
        
        updateStore(keystoreStream, keystoreLocation);
    }


    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder#updateRegisteredTruststore(java.io.InputStream)
     */
    public void updateRegisteredTruststore(InputStream truststoreStream) throws IOException {
        if (truststoreStream == null) {
            throw new NullPointerException("truststoreStream cannot be null.");
        }

        String agentHome = getAgentHome();
        String truststoreLocation = agentHome + RELATIVE_TRUSTSTORE_FILE;
        
        updateStore(truststoreStream, truststoreLocation);        
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder#updateRegisteredKeystorePassword(byte[])
     */
    public void updateRegisteredKeystorePassword(String password) throws IOException {
        String agentHome = getAgentHome();
        String passwordFileName = agentHome + RELATIVE_PASSWORD_FILE;
        
        ReversibleEncryptor encryptor = new ReversibleEncryptor();
        String encryptedPassword = encryptor.encrypt(password);
        
        // A bit of a hack, but I want to reuse the file data write code
        ByteArrayInputStream passwordByteStream = new ByteArrayInputStream(encryptedPassword.getBytes());
        updateStore(passwordByteStream, passwordFileName);
    }
    
    /**
     * Update the store at the specified path location with the specified input stream data
     * @param dataStream
     * @param fileLocation
     * @throws IOException
     */
    private void updateStore(InputStream dataStream, String fileLocation) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(fileLocation));
        while ((bytesRead = dataStream.read(buffer)) != -1) {
            fileStream.write(buffer, 0, bytesRead);
        }
        fileStream.close();
    }
    
    /**
     * Reads password from file.
     * 
     * @return password
     */
    private String getKeystorePassword() {
        String agentHome = getAgentHome();

        String passwordFileName = agentHome + RELATIVE_PASSWORD_FILE;

        String password = null;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(passwordFileName));
            password = fileReader.readLine();

            ReversibleEncryptor decryptor = new ReversibleEncryptor();
            try {
                password = decryptor.decrypt(password);
            } catch (Exception e) {
                // FIX ME - What if someone accidentally deletes the
                // password file? Find a
                // better way to deal with this
                throw new IllegalStateException("Password not readable");
            }
        } catch (FileNotFoundException e) {
            // FIX ME - What if someone accidentally deletes the password file?
            // Find a better way to deal with this
            throw new IllegalStateException("Could not read password file");
        } catch (IOException e) {
            // FIX ME - What if someone accidentally deletes the password file?
            // Find a better way to deal with this
            throw new IllegalStateException("Could not read password file");
        }
        return password;
    }

    /**
     * @return
     */
    private String getAgentHome() {
        IConfiguration config = getConfiguration();
        String agentHome = config.get(AGENT_HOME_DIRECTORY_PROPERTY_NAME);
        if (agentHome == null) {
            throw new IllegalStateException("Agent home not specified");
        }

        return agentHome;
    }
}
