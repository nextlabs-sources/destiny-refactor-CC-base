package com.bluejungle.pf.engine.destiny;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.security.KeyNotFoundException;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleFactory;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BundleVaultImpl implements IBundleVault, IConfigurable, IInitializable {

    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("baseDir");
    public static final PropertyKey<String> BUNDLE_FILE_PROPERTY_NAME = new PropertyKey<String>("bundleFileProperty");
    private static final String DEFAULT_BUNDLE_FILE_NAME = "bundle.bin";
    private static final String DEFAULT_BASE_DIR = ".";

    private static final String SERVER_PUBLIC_KEY_ALIAS = "dcc";
    private static final String SECRET_KEY_ALIAS = "bundleKey";
    private static final String AGENT_PRIVATE_KEY_STORE_ALIAS = "secretKeystore";
    private static final String SYMMETRIC_CIPHER_ALGORITHM = "AES";
    private static final int SYMMETRIC_KEY_LENGTH = 128;

    private IConfiguration configuration;
    private String bundleFileName = DEFAULT_BUNDLE_FILE_NAME;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration configuration = this.getConfiguration();
        if (configuration != null) {
            /*
             * If a bundle file name is given we will use it. If it isn't then we will look for "bundle.bin" under the
             * specified base directory. If the base directory is not given we will look in the pwd.
             */
            String baseDir = configuration.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
            bundleFileName = configuration.get(BUNDLE_FILE_PROPERTY_NAME, baseDir + File.separator + DEFAULT_BUNDLE_FILE_NAME);
        } 
        
        bundleFileName = new File(bundleFileName).getAbsolutePath();
    }

    /**
     * @throws BundleVaultException
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#validateAndStore(com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope)
     */
    public BundleInfo validateAndStore(DeploymentBundleSignatureEnvelope signedDeploymentBundle) throws InvalidBundleException, BundleVaultException {
        IDeploymentBundle bundle = null;
        String[] subjects = null;
        try {
            PublicKey serverPublicKey = getServerPublicKey();

            bundle = signedDeploymentBundle.getDeploymentBundle(serverPublicKey);
            subjects = signedDeploymentBundle.getSubjects();

            SealedObject encryptedDeploymentBundle = encryptDeploymentBundle(signedDeploymentBundle);

            FileOutputStream bundleFileOutputStream = new FileOutputStream(this.bundleFileName);
            GZIPOutputStream bundleZipFileOutputStream = new GZIPOutputStream(bundleFileOutputStream);
            ObjectOutputStream bundleObjectOutputStream = new ObjectOutputStream(bundleZipFileOutputStream);
            bundleObjectOutputStream.writeObject(encryptedDeploymentBundle);
            bundleObjectOutputStream.flush();
            bundleObjectOutputStream.close();
        } catch (IOException exception) {
            throw new BundleVaultException("Failed to store bundle", exception);
        } catch (InvalidKeyException exception) {
            throw new BundleVaultException("Failed to validate bundle", exception);
        } catch (KeyNotFoundException exception) {
            throw new BundleVaultException("Couldn't find key", exception);
        } catch (SignatureException exception) {
            throw new BundleVaultException("Failed to validate bundle", exception);
        }
        return new BundleInfoImpl(bundle, subjects);
    }

    /**
     * 
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#getBundleInfo()
     */
    public IBundleVault.BundleInfo getBundleInfo() throws BundleVaultException, InvalidBundleException {

        IDeploymentBundle bundle = null;
        String[] subjects = null;
        try {
            PublicKey serverPublicKey = getServerPublicKey();

            File bundleFile = new File(this.bundleFileName);
            if (bundleFile.exists()) {
                FileInputStream bundleFileInputStream = new FileInputStream(bundleFile);
                GZIPInputStream bundleZipFileInputStream = new GZIPInputStream(bundleFileInputStream);
                ObjectInputStream bundleObjectInputStream = new ObjectInputStream(bundleZipFileInputStream);

                SealedObject encryptedDeploymentBundle = (SealedObject) bundleObjectInputStream.readObject();
                DeploymentBundleSignatureEnvelope signedDeploymentBundle = decryptBundle(encryptedDeploymentBundle);
                subjects = signedDeploymentBundle.getSubjects();

                bundle = signedDeploymentBundle.getDeploymentBundle(serverPublicKey);

                bundleObjectInputStream.close();
            }
        } catch (IOException exception) {
            // In this case, there could be an error reading the file because
            // it's not a valid bundle. Be conservative and assume so
            throw new InvalidBundleException("Failed to read bundle", exception);
        } catch (ClassNotFoundException exception) {
            throw new BundleVaultException("Failed to read bundle", exception);
        } catch (InvalidKeyException exception) {
            throw new BundleVaultException("Failed to validate bundle", exception);
        } catch (KeyNotFoundException exception) {
            throw new BundleVaultException("Couldn't find key", exception);
        } catch (SignatureException exception) {
            // In this case, there could be an error reading the signature
            // because it's not a valid bundle. Be conservative and assume so
            throw new InvalidBundleException("Failed to validate bundle", exception);
        } catch (IllegalStateException exception) {
            // Likewise, probably not a valid bundle
            throw new InvalidBundleException("Failed to decrypt bundle/validate bundle", exception);
        }

        if (bundle == null) {
            Calendar timestamp = Calendar.getInstance();
            timestamp.setTimeInMillis(0);
            bundle = DeploymentBundleFactory.makeBundle(timestamp);
        }
        return new BundleInfoImpl(bundle, subjects);
    }

    
    /**
     * @see com.bluejungle.pf.engine.destiny.IBundleVault#getDeploymentBundleFilename()
     */
    public String getDeploymentBundleFilename() {
        return this.bundleFileName;
    }

    /**
     * Encrypt the deployment bundle
     * 
     * @param signedDeploymentBundle
     *            the bundle to encrypt
     * @return the encrypted bundle as a sealed object
     * @throws IOException
     *             if there was a problem serializing the deployment bundle
     */
    private SealedObject encryptDeploymentBundle(DeploymentBundleSignatureEnvelope signedDeploymentBundle) throws IOException {
        SealedObject sealedObjectToReturn = null;
        try {
            SecretKey secretKey = getAgentSecretKey();

            Cipher bundleCipher = Cipher.getInstance(SYMMETRIC_CIPHER_ALGORITHM);
            bundleCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            sealedObjectToReturn = new SealedObject(signedDeploymentBundle, bundleCipher);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Encryption algorithm not provided");
        } catch (NoSuchPaddingException exception) {
            throw new IllegalStateException("Padding algorithm not provided");
        } catch (InvalidKeyException exception) {
            throw new IllegalStateException("Encryption key is invalid: " + exception.getMessage());
        } catch (IllegalBlockSizeException exception) {
            throw new IllegalStateException("Padding algorithm not appropriate: " + exception.getMessage());
        }

        return sealedObjectToReturn;
    }

    /**
     * Decrypt the deployment bundle
     * 
     * @param encryptedDeploymentBundle
     *            the deployment bundle to decrypt
     * @return the decrypted bundle
     * @throws IOException
     *             if there was a problem deserializing the bundle
     */
    private DeploymentBundleSignatureEnvelope decryptBundle(SealedObject encryptedDeploymentBundle) throws IOException {
        DeploymentBundleSignatureEnvelope deploymentBundleToReturn = null;

        try {
            SecretKey secretKey = getAgentSecretKey();

            deploymentBundleToReturn = (DeploymentBundleSignatureEnvelope) encryptedDeploymentBundle.getObject(secretKey);
        } catch (InvalidKeyException exception) {
            throw new IllegalStateException("Invalid Key Utilized: " + exception.getMessage());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Encryption algorithm not provided");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("DeploymentBundleSignatureEnvelope class not found.");
        }

        return deploymentBundleToReturn;
    }

    /**
     * Retrieve the Agent's secret key
     * 
     * @return the Agent's secret key
     */
    private SecretKey getAgentSecretKey() throws IOException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);
        SecretKey agentSecretKey = null;
        if (!keyManager.containsSecretKey(SECRET_KEY_ALIAS)) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_CIPHER_ALGORITHM);
                keyGenerator.init(SYMMETRIC_KEY_LENGTH);
                agentSecretKey = keyGenerator.generateKey();
                keyManager.addSecretKey(SECRET_KEY_ALIAS, agentSecretKey, AGENT_PRIVATE_KEY_STORE_ALIAS);
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("Encryption algorithm not provided");
            }
        } else {
            agentSecretKey = keyManager.getSecretKey(SECRET_KEY_ALIAS);
        }

        return agentSecretKey;
    }

    /**
     * Retrieve the Server's Public Key
     * 
     * @return the Server's Public Key
     */
    private PublicKey getServerPublicKey() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);
        PublicKey serverPublicKey = keyManager.getPublicKey(SERVER_PUBLIC_KEY_ALIAS);
        return serverPublicKey;
    }

    private static class BundleInfoImpl implements BundleInfo {
        private final IDeploymentBundle bundle;
        private final String[] subjects;
        public BundleInfoImpl(IDeploymentBundle bundle, String[] subjects) {
            this.bundle = bundle;
            this.subjects = subjects;
        }
        public IDeploymentBundleV2 getBundle() {
            /* This function is only ever called by new, V2, agents,
             * who are expecting a V2 bundle.  The constructor,
             * however, is called on the server, which might have to
             * build bundles for either old or new systems.  This
             * explains why we take IDeploymentBundle, but only give
             * out IDeploymentBundleV2
             */
            if (bundle instanceof IDeploymentBundleV2) {
                return (IDeploymentBundleV2)bundle;
            } else {
                return null;
            }
        }
        public String[] getSubjects() {
            return subjects;
        }
    }

}
