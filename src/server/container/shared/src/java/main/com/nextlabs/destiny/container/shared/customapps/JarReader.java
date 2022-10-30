/*
 * Created on Mar 4, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/JarReader.java#1 $
 */

class JarReader implements IJarReader, Closeable {
    private static final Log LOG = LogFactory.getLog(JarReader.class);
    
    protected final JarFile jarFile;
    
    public JarReader(JarFile jarFile) {
        this.jarFile = jarFile;
    }
    
    public byte[] read(String entryName) throws IOException, NullPointerException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            read(entryName, os);
        } finally {
            os.close();
        }

        return os.toByteArray();
    }

    public String getString(String entryName) throws IOException, NullPointerException {
        return new String(read(entryName), "UTF-8");
    }

    public void read(String entryName, OutputStream output)
            throws IOException, NullPointerException {
        ZipEntry entry = jarFile.getEntry(entryName);
        if (entry == null) {
            if(LOG.isDebugEnabled()){
                StringBuilder sb = new StringBuilder("Can't find " + entryName + "\n");
                Enumeration<JarEntry> allEntries = jarFile.entries();
                while(allEntries.hasMoreElements()){
                    sb.append(allEntries.nextElement().getName()).append("\n");
                }
                LOG.debug(sb.toString());
            }
            
            
            throw new NullPointerException(entryName);
        }

        InputStream is = jarFile.getInputStream(entry);
        try {
            byte[] buffer = new byte[512];
            int size;
            while ((size = is.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } finally {
            is.close();
        }
    }

    public void close() throws IOException {
        jarFile.close();
    }
}
