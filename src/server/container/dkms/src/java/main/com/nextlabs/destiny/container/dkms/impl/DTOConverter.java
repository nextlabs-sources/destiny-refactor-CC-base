package com.nextlabs.destiny.container.dkms.impl;

import java.util.Collection;
import java.util.Set;

import com.nextlabs.destiny.container.dkms.IKey;
import com.nextlabs.destiny.container.dkms.IKeyId;
import com.nextlabs.destiny.container.dkms.IKeyRing;
import com.nextlabs.destiny.container.dkms.KeyManagementException;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingNamesDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;

public final class DTOConverter {

    public static IKeyId convertToKeyId(KeyIdDTO keyIdDTO){
        return new KeyId(keyIdDTO.getHash(), keyIdDTO.getTimestamp());
    }
    
    public static KeyIdDTO convertToKeyIdDTO(IKeyId keyId){
        return new KeyIdDTO(keyId.getId(), keyId.getCreationTimeStamp());
    }
    
    public static KeyRingDTO convertToKeyRingDTO(IKeyRing keyRing) throws KeyManagementException{
        return new KeyRingDTO(
                keyRing.getName() // java.lang.String name,
              , toKeyIdDTOs(keyRing.getKeys()) // java.lang.String[] keyNames,
              , keyRing.getKeyRingDO().getCreated().getTime()  // createDate,
              , keyRing.getKeyRingDO().getLastUpdated().getTime()  // latestModifiedDate
        );
    }
    
    public static KeyRingWithKeysDTO convertToKeyRingWithKeysDTO(IKeyRing keyRing) throws KeyManagementException {
        return new KeyRingWithKeysDTO(
                keyRing.getName() // java.lang.String name,
              , toKeyDTOs(keyRing.getKeys()) // java.lang.String[] keyNames,
              , keyRing.getKeyRingDO().getCreated().getTime()  // createDate,
              , keyRing.getKeyRingDO().getLastUpdated().getTime()  // latestModifiedDate
        );
    }
    
    public static KeyIdDTO[] toKeyIdDTOs(Collection<IKey> keys){
        KeyIdDTO[] result = new KeyIdDTO[keys.size()];
        int i =0;
        for(IKey key : keys){
            result[i++] = new KeyIdDTO(key.getId(), key.getCreationTimeStamp());
        }
        return result;
    }
    
    public static KeyDTO[] toKeyDTOs(Collection<IKey> keys){
        KeyDTO[] result = new KeyDTO[keys.size()];
        int i =0;
        for(IKey key : keys){
            result[i++] = convertToKeyDTO(key);
        }
        return result;
    }
    
    public static KeyRingNamesDTO toKeyRingNamesDTO(Set<String> keyRingNames) {
        String[] names = keyRingNames.toArray(new String[keyRingNames.size()]);
        return new KeyRingNamesDTO(names);
    }
    
    public static IKey convertToKey(KeyDTO keyDTO) {
        return new Key(
                new KeyId(
                        keyDTO.getKeyId().getHash()
                      , keyDTO.getKeyId().getTimestamp())
              , keyDTO.getVersion()
              , keyDTO.getKey()
        );
    }
    
    public static KeyDTO convertToKeyDTO(IKey key) {
        return new KeyDTO(
                key.getStructureVersion()
                , new KeyIdDTO(key.getId(), key.getCreationTimeStamp())
                , key.getEncoded()
        );
    }
    
    private DTOConverter() {
    }
}
