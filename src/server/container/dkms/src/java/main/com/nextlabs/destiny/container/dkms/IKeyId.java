package com.nextlabs.destiny.container.dkms;

import java.io.Serializable;

public interface IKeyId extends Serializable{

    byte[] getId();
    
    long getCreationTimeStamp();
    
}
