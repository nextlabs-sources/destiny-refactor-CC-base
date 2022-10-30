package com.bluejungle.framework.securestore;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/securestore/SecureFileStore.java#1 $
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class provides a template for storing serialized data in a file.
 *
 * @author Sergey Kalinichenko
 */
public class SecureFileStore<T extends Serializable> extends AbstractSecureStore<T>  {

    /**
     * The file identifying the place where the encrypted information is stored.
     */
    private final File storageFile;

    /**
     * Creates a new secure file store for the specified file and the private key name.
     *
     * @param storageFile the File where the information is to be stored.
     * @param privateKeyName the name of the private key to use for encryption.
     * @throws IOException if the operation fails.
     */
    public SecureFileStore(
        File storageFile
    ,   String privateKeyName
    ) throws IOException {
        super(privateKeyName);
        this.storageFile = storageFile;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!storageFile.canRead()) {
            return null;
        }
        return new FileInputStream(storageFile);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(storageFile);
    }

}
