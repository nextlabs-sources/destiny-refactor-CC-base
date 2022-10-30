/*
 * Created on Dec 14, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.security.KeyManagerImpl;
import com.bluejungle.framework.security.KeyManagerImpl.KeystoreFileInfo;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleFactory;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/client/pf/src/java/test/com/bluejungle/pf/engine/destiny/BundleVaultImplTest.java#1 $
 */
public class BundleVaultImplTest extends TestCase {

    private static final String BUNDLE_NAME = "testbundle.bin";
    private static final String BASE_DIR_PROPERTY_NAME = "build.root.dir";
    private static final String RELATIVE_CLIENT_KEYSTORE_FILE_NAME = "/run/server/certificates/agent-keystore.jks";
    private static final String RELATIVE_CLIENT_TRUSTSTORE_FILE_NAME = "/run/server/certificates/agent-truststore.jks";
    private static final String RELATIVE_CLIENT_SECRET_KEYSTORE_FILE_NAME = "/run/server/certificates/agent-secret-keystore.jceks";

    private static final String RELATIVE_SERVER_KEYSTORE_FILE_NAME = "/run/server/certificates/dcc-keystore.jks";
    private static final String RELATIVE_SERVER_TRUSTSTORE_FILE_NAME = "/run/server/certificates/dcc-truststore.jks";
    private static final String RELATIVE_SERVER_SECRET_KEYSTORE_FILE_NAME = "/run/server/certificates/dcc-secret-keystore.jceks";

    private BundleVaultImpl vaultToTest;
    private IKeyManager agentKeyManager;
    private IKeyManager serverKeyManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        File bundleFile = new File(BUNDLE_NAME);
        bundleFile.delete();

        String baseDir = System.getProperty(BASE_DIR_PROPERTY_NAME);
        if (baseDir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY_NAME + " system property must be specified for unit test to function");
        }

        // Set up agent key manager
        HashMapConfiguration keyManagerConfiguration = new HashMapConfiguration();
        Set<KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("keystore", baseDir + RELATIVE_CLIENT_KEYSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("truststore", baseDir + RELATIVE_CLIENT_TRUSTSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("secretKeystore", baseDir + RELATIVE_CLIENT_SECRET_KEYSTORE_FILE_NAME, "jceks", "password"));
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        ComponentInfo agentKeyManagerInfo = new ComponentInfo(IKeyManager.COMPONENT_NAME, KeyManagerImpl.class.getName(), IKeyManager.class.getName(), LifestyleType.SINGLETON_TYPE, keyManagerConfiguration);

        // Set up server key manager
        keyManagerConfiguration = new HashMapConfiguration();
        keyStoreFileInfo = new HashSet<KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("privateKeyStore", baseDir + RELATIVE_SERVER_KEYSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("publicTrustStore", baseDir + RELATIVE_SERVER_TRUSTSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("secretKeystore", baseDir + RELATIVE_SERVER_SECRET_KEYSTORE_FILE_NAME, "jceks", "password"));
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        ComponentInfo serverKeyManagerInfo = new ComponentInfo("server" + IKeyManager.COMPONENT_NAME, KeyManagerImpl.class.getName(), IKeyManager.class.getName(), LifestyleType.SINGLETON_TYPE, keyManagerConfiguration);
        HashMapConfiguration vaultConfiguration = new HashMapConfiguration();
        vaultConfiguration.setProperty(
            BundleVaultImpl.BUNDLE_FILE_PROPERTY_NAME
        ,   BUNDLE_NAME
        );
        ComponentInfo bundleVaultInfo = new ComponentInfo(IBundleVault.COMPONENT_NAME, BundleVaultImpl.class.getName(), IBundleVault.class.getName(), LifestyleType.SINGLETON_TYPE, vaultConfiguration);

        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        manager.registerComponent(serverKeyManagerInfo, true);
        this.serverKeyManager = (KeyManagerImpl) componentManager.getComponent(serverKeyManagerInfo);
        manager.registerComponent(agentKeyManagerInfo, true);
        this.agentKeyManager = (IKeyManager) manager.getComponent(agentKeyManagerInfo);
        manager.registerComponent(bundleVaultInfo, true);
        this.vaultToTest = (BundleVaultImpl) manager.getComponent(bundleVaultInfo);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    /**
     * Test method for
     * {@link com.bluejungle.pf.engine.destiny.BundleVaultImpl#validateAndStore(com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope)}
     * and
     * {@link com.bluejungle.pf.engine.destiny.BundleVaultImpl#getBundleInfo()}..
     */
    public void testStoreAndGetDeploymentBundle() throws BundleVaultException, InvalidBundleException, InvalidKeyException, SignatureException, IOException {
        // First, make sure we get set an empty bundle
        IBundleVault.BundleInfo bundleInfo = this.vaultToTest.getBundleInfo();
        assertNotNull(bundleInfo);
        assertNotNull(bundleInfo.getBundle());
        assertNull(bundleInfo.getSubjects());
        assertTrue("testStoreAndGetDeploymentBundle - Ensure bundle is initially empty", bundleInfo.getBundle().isEmpty());

        // Now, create a new bundle and store it
        IDeploymentBundle deploymentBundle = DeploymentBundleFactory.makeBundle("bogus", Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_SET, new BitSet(), new BitSet(), new BitSet(), Calendar.getInstance());
        PrivateKey serverPrivateKey = this.serverKeyManager.getPrivateKey("DCC");
        String[] uids = new String[] {"zzz", "aaa"};
        DeploymentBundleSignatureEnvelope signedBundle = new DeploymentBundleSignatureEnvelope(deploymentBundle, uids, serverPrivateKey);
        String[] uidsBack = signedBundle.getSubjects();
        assertNotNull(uidsBack);
        assertEquals(2, uidsBack.length);
        assertEquals("AAA", uidsBack[0]);
        assertEquals("ZZZ", uidsBack[1]);
        IBundleVault.BundleInfo bundleInfoReturned = this.vaultToTest.validateAndStore(signedBundle);
        assertEquals(
            "testStoreAndGetDeploymentBundle - Ensure bundle is as expected"
        ,   deploymentBundle.getDeploymentEntities()
        ,   bundleInfoReturned.getBundle().getDeploymentEntities()
        );

        // Make sure a call to getDeploymentBundle returns was we expect
        assertEquals(
            "testStoreAndGetDeploymentBundle - Ensure bundle is as expected through get"
        ,   deploymentBundle.getDeploymentEntities()
        ,   this.vaultToTest.getBundleInfo().getBundle().getDeploymentEntities()
        );

        // Now try bad bundle
        PrivateKey agentPrivateKey = this.agentKeyManager.getPrivateKey("Agent");
        signedBundle = new DeploymentBundleSignatureEnvelope(deploymentBundle, uids, agentPrivateKey);
        try {
            this.vaultToTest.validateAndStore(signedBundle);
            fail("Should throw InvalidBundleException for invalid bundle");
        } catch (InvalidBundleException exception) {
        }

        // Now try bad bundle on file system
        File bundleFile = new File(BUNDLE_NAME);
        FileOutputStream bundleFileOutputStream = new FileOutputStream(bundleFile);
        bundleFileOutputStream.write(1);
        bundleFileOutputStream.close();

        try {
            this.vaultToTest.getBundleInfo();
            fail("Should throw BundleVaultException for invalid bundle on file system");
        } catch (BundleVaultException exception) {
        } catch (InvalidBundleException exception) {
        }
    }

    /**
     * Test for {@link BundleVaultImpl#getDeploymentBundleFilename()}
     * 
     */
    public void testGetDeploymentBundleFilename() {
        String deploymentBundleFilename = this.vaultToTest.getDeploymentBundleFilename();
        assertEquals("testGetDeploymentBundleFilename - Ensure bundle file is as expected", new File(BUNDLE_NAME).getAbsolutePath(), deploymentBundleFilename);
    }
}
