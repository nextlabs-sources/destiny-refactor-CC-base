package com.bluejungle.framework.securestore;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/securestore/AbstractSecureStore.java#1 $
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.security.IKeyManager;

/**
 * This abstract class provides a base for implementing
 * the SecureStore<T> interface.
 *
 * @author Sergey Kalinichenko
 */
public abstract class AbstractSecureStore<T extends Serializable> implements ISecureStore<T>, ILogEnabled {

    private static final String AGENT_PRIVATE_KEY_STORE_ALIAS = "secretKeystore";
    private static final String SYMMETRIC_CIPHER_ALGORITHM = "AES";
    private static final int SYMMETRIC_KEY_LENGTH = 128;


    private final String privateKeyName;
    private Log log;

    public AbstractSecureStore(String privateKeyName) throws IOException {
        this.privateKeyName = privateKeyName;
    }

    public String getPrivateKeyName() {
        return privateKeyName;
    }

    /**
     * @see ISecureStore#read()
     */
    @SuppressWarnings("unchecked")
    public T read() throws IOException {
        InputStream bin = getInputStream();
        if (bin == null) {
            return null;
        }
        try {
            GZIPInputStream zin = new GZIPInputStream(bin);
            ObjectInputStream in = new ObjectInputStream(zin);
            SealedObject sealed = (SealedObject)in.readObject();
            SecretKey secretKey = getSecretKey();
            in.close();
            zin.close();
            return (T)sealed.getObject(secretKey);
        } catch (ClassNotFoundException e) {
            warn("Unable to decrypt", e);
        } catch (InvalidKeyException e) {
            warn("Unable to decrypt", e);
        } catch (NoSuchAlgorithmException e) {
            warn("Unable to decrypt", e);
        } finally {
            bin.close();
        }
        return null;
    }

    /**
     * @see ISecureStore#save(T)
     */
    public void save(T obj) throws IOException {
        OutputStream bout = getOutputStream();
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(SYMMETRIC_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            SealedObject sealed = new SealedObject(obj, cipher);
            GZIPOutputStream zout = new GZIPOutputStream(bout);
            ObjectOutputStream out = new ObjectOutputStream(zout);
            out.writeObject(sealed);
            out.flush();
            zout.flush();
            bout.flush();
            out.close();
            zout.close();
        } catch (IllegalBlockSizeException e) {
            warn("Unable to encrypt", e);
        } catch (NoSuchAlgorithmException e) {
            warn("Unable to encrypt", e);
        } catch (NoSuchPaddingException e) {
            warn("Unable to encrypt", e);
        } catch (InvalidKeyException e) {
            warn("Unable to encrypt", e);
        } finally {
            bout.close();
        }
    }

    public abstract InputStream getInputStream() throws IOException;

    public abstract OutputStream getOutputStream() throws IOException;

    private SecretKey getSecretKey() throws IOException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);
        SecretKey agentSecretKey = null;
        if (!keyManager.containsSecretKey(privateKeyName)) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_CIPHER_ALGORITHM);
                keyGenerator.init(SYMMETRIC_KEY_LENGTH);
                agentSecretKey = keyGenerator.generateKey();
                keyManager.addSecretKey(privateKeyName, agentSecretKey, AGENT_PRIVATE_KEY_STORE_ALIAS);
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("Encryption algorithm not provided");
            }
        } else {
            agentSecretKey = keyManager.getSecretKey(privateKeyName);
        }

        return agentSecretKey;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    private void warn(String message, Exception e) {
        if (log != null) {
            log.warn(message + e.getMessage());
        }
    }

}
