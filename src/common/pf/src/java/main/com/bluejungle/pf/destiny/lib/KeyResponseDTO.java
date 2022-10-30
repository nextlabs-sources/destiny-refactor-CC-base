/*
 * Created on Mar 01, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/KeyResponseDTO.java#1 $:
 */

package com.bluejungle.pf.destiny.lib;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeyResponseDTO implements Externalizable, Serializable {
    
    private List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();

    public KeyResponseDTO() {
    }

    public KeyResponseDTO(List<KeyRingDTO> keyRings) {
        this.keyRings = keyRings;
    }

    public List<KeyRingDTO> getKeyRings() {
        return Collections.unmodifiableList(keyRings);
    }

    public void setKeyRings(List<KeyRingDTO> keyRings) {
        this.keyRings = keyRings;
    }

    public static class KeyRingDTO implements Externalizable, Serializable {
        
        private long timestamp;
        private String keyRingName;
        private List<KeyDTO> keys;

        public KeyRingDTO() {
        }

        public KeyRingDTO(String keyRingName, long timestamp, List<KeyDTO> keys) {
            this.keyRingName = keyRingName;
            this.timestamp = timestamp;
            this.keys = keys;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getName() {
            return keyRingName;
        }

        public List<KeyDTO> getKeys() {
            return Collections.unmodifiableList(keys);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(timestamp);
            out.writeUTF(keyRingName);
            out.writeInt(keys.size());
            for (KeyDTO key : keys) {
                key.writeExternal(out);
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            timestamp = in.readLong();
            keyRingName = in.readUTF();
            int len = in.readInt();

            keys = new ArrayList<KeyDTO>();
            for (int i = 0 ; i < len ; i++) {
                KeyDTO key = new KeyDTO();
                key.readExternal(in);
                keys.add(key);
            }
        }
    }

    public static class KeyDTO implements Externalizable, Serializable {
        
        private int structureVersion;
        private long timestamp;
        private byte[] key;
        private byte[] id;

        public KeyDTO() {
        }

        public KeyDTO(int structureVersion, long timestamp, byte[] key, byte[] id) {
            this.structureVersion = structureVersion;
            this.timestamp = timestamp;
            this.key = key;
            this.id = id;
        }

        public int getVersion() {
            return structureVersion;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getId() {
            return id;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(structureVersion);
            out.writeLong(timestamp);

            out.writeInt(key.length);
            out.write(key);
            out.flush();
            
            out.writeInt(id.length);
            out.write(id);
            out.flush();
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            structureVersion = in.readInt();
            timestamp = in.readLong();
            
            int len = in.readInt();
            key = new byte[len];
            in.read(key, 0, len);

            len = in.readInt();
            id = new byte[len];
            in.read(id, 0, len);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(keyRings.size());
        for (KeyRingDTO keyRing : keyRings) {
            keyRing.writeExternal(out);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            KeyRingDTO keyRing = new KeyRingDTO();
            keyRing.readExternal(in);
            keyRings.add(keyRing);
        }
    }
}
