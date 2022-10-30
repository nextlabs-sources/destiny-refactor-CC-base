package com.nextlabs.destiny.tools.keymanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import com.bluejungle.framework.utils.CodecHelper;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;

public class KeyRingFileHelper {
    
    //have to pick the one that is not the encode table
    private static final char TOKEN = ' ';
    
    private static final String INTERNAL_PROPERTY_PREFIX = "X";
    private static final String NAME_PROPERTY = INTERNAL_PROPERTY_PREFIX + "name";
    
    private static final String KEY_STRORE_TYPE = "JCEKS";
    
    private static final int DEFAULT_KEY_VERSION = 1;
    
    static void save(
            KeyRingWithKeysDTO keyRingDTO
          , PasswordProtection password
          , File file
    ) throws KeyManagementMgrException {
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new KeyManagementMgrException(e);
        }
        try {
            save(keyRingDTO, password, outputStream);
        } catch (Exception e) {
            throw new KeyManagementMgrException("unable to save the keyring to file " + file.getAbsolutePath(), e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new KeyManagementMgrException("unable to close outputstream", e); 
            }
        }
        
    }
    
    static void save(
            KeyRingWithKeysDTO keyRingDTO
          , PasswordProtection password
          , OutputStream outputStream
    ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, 
             IOException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STRORE_TYPE);
        keyStore.load(null);
        
        KeyStore.Entry entry = new KeyStore.SecretKeyEntry(
                new SecretKeySpec(CodecHelper.toBytes(keyRingDTO.getName()), "RAW"));
        keyStore.setEntry(NAME_PROPERTY, entry, password);
        
        KeyDTO[] keys = keyRingDTO.getKeys();
        if(keys != null){
            
            for(KeyDTO key : keys){
                String alias = CodecHelper.base16Encode(key.getKeyId().getHash()) 
                        + TOKEN 
                        + Long.toHexString(key.getKeyId().getTimestamp());
                javax.crypto.SecretKey mySecretKey = new SecretKeySpec(key.getKey(), "NX" + key.getVersion());
                keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(mySecretKey), password);
            }
        }
        keyStore.store(outputStream, password.getPassword());
        System.out.println("saved " + (keys != null ? keys.length : 0) + " keys");
    }
    
    static KeyRingWithKeysDTO load(File file, PasswordProtection password)
            throws KeyManagementMgrException {
        InputStream inputstream;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new KeyManagementMgrException(e);
        }
        
        try {
            return load(inputstream, password);
        } catch (Exception e) {
            throw new KeyManagementMgrException("unable to load the keyring from file " + file.getAbsolutePath(), e);
        } finally {
            try {
                inputstream.close();
            } catch (IOException e) {
                throw new KeyManagementMgrException("unable to close inputstream", e); 
            }
        }
    }
    
    static KeyRingWithKeysDTO load(
            InputStream inputstream
          , PasswordProtection password
    ) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException,
            KeyManagementMgrException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STRORE_TYPE);
        
        keyStore.load(inputstream, password.getPassword());

        KeyRingWithKeysDTO keyRingDTO = new KeyRingWithKeysDTO();
        
        byte[] encodedName = getKey(keyStore, password, NAME_PROPERTY);
        String storedName = CodecHelper.toString(encodedName);
        keyRingDTO.setName(storedName);
        
        List<KeyDTO> keys = new LinkedList<KeyDTO>();
        
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (alias.toUpperCase().startsWith(INTERNAL_PROPERTY_PREFIX)) {
                continue;
            }
            
            int sepIndex = alias.lastIndexOf(TOKEN);
            if(sepIndex == -1){
                throw new KeyManagementMgrException("invalid alias format, missing token. ");
            }
            String idEncoded = alias.substring(0, sepIndex);
            byte[] original = CodecHelper.base16Decode(idEncoded.toUpperCase());
            long date = Long.parseLong(alias.substring(sepIndex + 1), 16);
            
            byte[] keyData = getKey(keyStore, password, alias);
            
            keys.add(new KeyDTO(DEFAULT_KEY_VERSION
                    , new KeyIdDTO(original, date)
                    , keyData));
        }
        
        keyRingDTO.setKeys(keys.toArray(new KeyDTO[keys.size()]));
        return keyRingDTO;
    }
    
    static byte[] getKey(KeyStore keyStore, PasswordProtection password,
            String alias) throws
            NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException {
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore
                .getEntry(alias, password);
        if (entry != null) {
            return entry.getSecretKey().getEncoded();
        }
        return null;
    }
}
