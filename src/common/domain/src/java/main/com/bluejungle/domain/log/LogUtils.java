/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.axis.encoding.Base64;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/LogUtils.java#1 $
 *
 */
public final class LogUtils {

    /**
     * Serializes, compresses, and encodes (Base64) collection of log entries.
     * The enclosing collection is not serialized, instead the number of entries
     * is the first entry (int) in the serialized stream.
     * 
     * @param logEntries entries to serialize, must be Externalizable
     * @return String encoding of serialized and compressed log entries
     * @throws IOException
     */
    public static final String encodeLogEntries(Collection logEntries) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        GZIPOutputStream zipStream = new GZIPOutputStream(outStream);
        ObjectOutputStream oos = new ObjectOutputStream(zipStream);
        
        oos.writeInt(logEntries.size());
        for (Iterator iter = logEntries.iterator(); iter.hasNext();) {
            Externalizable entry = (Externalizable) iter.next();
            entry.writeExternal(oos);
        }
        oos.close();

        byte[] bytes = outStream.toByteArray();
        return Base64.encode(bytes);        
    }
    
    /**
     * Decodes passed in encoded (Base64) and compressed data and provides
     * an ObjectInputStream
     * 
     * @param data data to decode
     * @return ObjectInputStream decoded from the data
     * @throws IOException
     */
    public static final ObjectInputStream decodeData(String data) throws IOException {
        byte[] bytes = Base64.decode(data);
        
        return (new ObjectInputStream (new GZIPInputStream (new ByteArrayInputStream(bytes))));
    }
}
