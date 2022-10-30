package com.nextlabs.destiny.container.dkms.impl;

import java.util.Arrays;

import com.nextlabs.destiny.container.dkms.IKeyId;

public class KeyId implements IKeyId{

    private static final long serialVersionUID = 1L;

    private final byte[] id;
    
    private final long creationTimeStamp;
    
    public KeyId(byte[] hash, long createiontTimeStamp) {
        this.id = hash;
        this.creationTimeStamp = createiontTimeStamp;
    }
    
    public byte[] getId() {
        return id;
    }
    
    public long getCreationTimeStamp(){
        return creationTimeStamp;
    }
    
    @Override
    public String toString() {
        return "KeyId" + "\n" 
            + "  id: " + Arrays.toString(id) + "\n" 
            + "  time: " + creationTimeStamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (creationTimeStamp ^ (creationTimeStamp >>> 32));
        result = prime * result + Arrays.hashCode(id);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof IKeyId)) {
            return false;
        }
        IKeyId other = (IKeyId) obj;
        if (creationTimeStamp != other.getCreationTimeStamp())
            return false;
        if (!Arrays.equals(id, other.getId()))
            return false;
        return true;
    }
}
