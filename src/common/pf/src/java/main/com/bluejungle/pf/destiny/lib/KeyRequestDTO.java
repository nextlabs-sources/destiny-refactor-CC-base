/*
 * Created on Mar 01, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/KeyRequestDTO.java#1 $:
 */
package com.bluejungle.pf.destiny.lib;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyRequestDTO implements Externalizable, Serializable {
    private static final long serialVersionUID = -1017029538453337147L;

    private final List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();

    public List<KeyRingDTO> getKeyRings() {
        return Collections.unmodifiableList(keyRings);
    }

    // This name should be the same as the plugin name specified in the properties file
    public static final String SERVICE_NAME = "NL_KM_CLIENT";

    public static class KeyRingDTO {
        private final long timestamp;
        private final String keyRingName;

        public KeyRingDTO(String keyRingName, long timestamp) {
            this.keyRingName = keyRingName;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getName() {
            return keyRingName;
        }
    }

    public KeyRequestDTO() {
    }

    /**
     * Add a new key ring (with last modified date) into the request object
     */
    public void addKeyRing(String keyRingName, long timestamp) {
        keyRings.add(new KeyRingDTO(keyRingName, timestamp));
    }

    /**
     * Writes the object data to the specified OutputObject
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int size = keyRings.size();
        out.writeInt(size);
        for (KeyRingDTO keyRing : keyRings) {
            out.writeUTF(keyRing.getName());
            out.writeLong(keyRing.getTimestamp());
        } 
    }

    /**
     * Reads the object data from the specified input stream into self
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            long timestamp = in.readLong();

            addKeyRing(name, timestamp);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        
        for (KeyRingDTO keyRing : keyRings) {
            res.append(keyRing.getName());
            res.append(": ");
            res.append(keyRing.getTimestamp());
            res.append("\n");
        }

        return res.toString();
    }
}
