package com.nextlabs.destiny.container.dkms;

public interface IKey extends IKeyId{
    public IKeyId getKeyId();
    
    public int getStructureVersion();
    
    public byte[] getEncoded();
}
