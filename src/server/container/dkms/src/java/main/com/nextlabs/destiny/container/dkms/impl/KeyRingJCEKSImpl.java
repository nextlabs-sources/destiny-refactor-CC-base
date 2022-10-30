package com.nextlabs.destiny.container.dkms.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.CodecHelper;
import com.nextlabs.destiny.container.dkms.IKey;
import com.nextlabs.destiny.container.dkms.IKeyId;
import com.nextlabs.destiny.container.dkms.IKeyRing;
import com.nextlabs.destiny.container.dkms.IKeyRingManager;
import com.nextlabs.destiny.container.dkms.KeyManagementException;
import com.nextlabs.destiny.container.dkms.hibernateimpl.KeyRingDO;

public class KeyRingJCEKSImpl implements IKeyRing {
    private static final Log LOG = LogFactory.getLog(KeyRingJCEKSImpl.class);
    
    //have to pick the one that is not the encode table
    private static final char TOKEN = ' ';  
    
    static final String KEY_STRORE_TYPE = "JCEKS";
    
    
    private final KeyStore keyStore;
    private final PasswordProtection password;
    private final IKeyRingManager keyRingManager;
    private final KeyRingDO keyRingDO;
    
    private boolean isClosed = false;
    
    KeyRingJCEKSImpl(
            KeyRingDO keyRingDO
          , PasswordProtection password
          , IKeyRingManager keyRingManager
    ) throws KeyManagementException {
        this.keyRingDO = keyRingDO;
        this.password = password;
        this.keyRingManager = keyRingManager;
        
        try {
            this.keyStore = KeyStore.getInstance(keyRingDO.getFormat());
            
            InputStream is = keyRingDO.getKeyStoreInputStream();
            if (is != null) {
                keyStore.load(is, password.getPassword());
                // no need to close stream since it is already closed
                // and it may throw exception in some cases
            } else {
                keyStore.load(null);
            }
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (CertificateException e) {
            throw new KeyManagementException(e);
        } catch (IOException e) {
            throw new KeyManagementException(e);
        } catch (SQLException e) {
            throw new KeyManagementException(e);
        }
    }
    
    @Override
    public String getName() {
        return keyRingDO.getName();
    }
    
    @Override
    public KeyRingDO getKeyRingDO() {
        return keyRingDO;
    }
    
    protected String convertToAlias(byte[] id, long timestamp){
        return CodecHelper.base16Encode(id) + TOKEN + Long.toHexString(timestamp);
    }
    
    protected String convertToAlias(IKeyId keyId){
        return convertToAlias(keyId.getId(), keyId.getCreationTimeStamp());
    }
    
    protected KeyId convertToKeyId(String alias) throws KeyManagementException {
        alias = alias.toUpperCase();
        int sepIndex = alias.lastIndexOf(TOKEN);
        if(sepIndex == -1){
            throw new KeyManagementException("invalid alias format, missing token. ");
        }
        
        
        String idEncoded = alias.substring(0, sepIndex);
        
        byte[] original;
        try {
            original = CodecHelper.base16Decode(idEncoded);
        } catch (IllegalArgumentException e) {
            throw new KeyManagementException("invalid alias format, '" + alias + "'. " + e.getMessage());
        }
        
        long date = Long.parseLong(alias.substring(sepIndex + 1), 16);
        return new KeyId(original, date);
    }
    
    private void checkClosed() throws KeyManagementException {
        if (isClosed) {
            throw new KeyManagementException("The keyRing is already closed.");
        }
    }

    public void deleteKey(IKeyId keyId) throws KeyManagementException {
        checkClosed();
        
        String alias = convertToAlias(keyId);
        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        }
        
        flush();
    }
    
    protected byte[] getKey(String alias) throws KeyManagementException{
        checkClosed();
        LOG.debug(">getting key: " + alias);
        KeyStore.SecretKeyEntry entry;
        try {
            entry = (KeyStore.SecretKeyEntry)keyStore.getEntry(alias,  password);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (UnrecoverableEntryException e) {
            throw new KeyManagementException(e);
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        }
        if(entry != null){
            return entry.getSecretKey().getEncoded();
        }
        
        return null;
    }
    
    protected IKey getKey(String alias, IKeyId keyId) throws KeyManagementException {
        byte[] value = getKey(alias);
        if(value != null){
            return new Key(keyId, value);
        }
        return null;
    }

    public IKey getKey(IKeyId keyId) throws KeyManagementException {
        String alias = convertToAlias(keyId);
        return getKey(alias, keyId);
    }

    
    public Collection<IKey> getKeys() throws KeyManagementException {
        checkClosed();
        
        Collection<IKey> keys = new LinkedList<IKey>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                keys.add(getKey(alias, convertToKeyId(alias)));
            }
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        }
        return keys;
    }

    @Override
    public void setKey(final IKey key) throws KeyManagementException {
        checkClosed();
        
        String alias = convertToAlias(key);
        
        LOG.debug("<setting key: " + alias);
        
        javax.crypto.SecretKey mySecretKey = convertToSecretKey(key);

        try {
            keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(mySecretKey), password);
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        }
        
        flush();
    }
    
    protected javax.crypto.SecretKey convertToSecretKey(IKey key){
        return new SecretKeySpec(key.getEncoded(), "NX" + key.getStructureVersion());
    }
    
    protected synchronized void flush() throws KeyManagementException{
        checkClosed();
        
        LOG.debug("flush");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            keyStore.store(baos, password.getPassword());
        } catch (GeneralSecurityException e) {
            throw new KeyManagementException(e);
        } catch (IOException e) {
            throw new KeyManagementException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                LOG.warn("unable to close the in memory outputstrem");
            }
        }

        byte[] oldData;
        try {
            InputStream oldIs = keyRingDO.getKeyStoreInputStream();
            if(oldIs != null){
                try {
                    ByteArrayOutputStream oldBaos = new ByteArrayOutputStream();
                    int i;
                    while( (i = oldIs.read()) != -1 ){
                        oldBaos.write(i);
                    }
                    oldBaos.close();
                    oldData = oldBaos.toByteArray();
                } finally {
                    oldIs.close();
                }
            } else {
                oldData = null;
            }
        } catch (SQLException e) {
            throw new KeyManagementException(e);
        } catch (IOException e) {
            throw new KeyManagementException(e);
        }
        
        keyRingDO.setKeyStoreData(baos.toByteArray());

        boolean isFailed = true;
        try {
            keyRingManager.updateKeyRing(this);
            isFailed = false;
        } finally {
            if(isFailed){
                // rollback
                keyRingDO.setKeyStoreData(oldData);
            }
        }
    }
    
    @Override
    public void close() {
        isClosed = true;
        try {
            password.destroy();
        } catch (DestroyFailedException e) {
            LOG.warn("Fail to destory password", e);
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    

}
