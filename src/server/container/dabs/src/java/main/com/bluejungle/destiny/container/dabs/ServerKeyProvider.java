/*
 * Created on Mar 10, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/ServerKeyProvider.java#1 $:
 */

package com.bluejungle.destiny.container.dabs;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.plugin.IDCCHeartbeatServerPlugin;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.destiny.lib.KeyRequestDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO;
import com.nextlabs.destiny.services.keymanagement.KeyManagementDCCIF;
import com.nextlabs.destiny.services.keymanagement.KeyManagementDCCServiceLocator;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingsWithKeysDTO;

public class ServerKeyProvider implements IDCCHeartbeatServerPlugin {
    private static final Log LOG = LogFactory.getLog(ServerKeyProvider.class);
    
    private static final String KM_LOCATION_SERVLET_PATH = "/services/KeyManagementDCCIFPort";
    
    private static final long UNKNOWN_TIME = -2398;
    
    private KeyManagementDCCIF keyManagementService = null;

    // We keep a cache of all the key rings, because querying the service is expensive
    private long latestCacheUpdate = 0;
    private AtomicReference<KeyRingsWithKeysDTO> cachedKeyRingInfo = new AtomicReference<KeyRingsWithKeysDTO>();

    public ServerKeyProvider() {
        
    }
    
    KeyManagementDCCIF getKeyManagementIF() {
        if(keyManagementService != null){
            return keyManagementService;
        }
        
        // There is an issue in urlLocator. A parameter is not set. It doesn't know where the DMS is. 
        String[] urls = new String[]{"https://localhost:8443/dkms"};
        
        KeyManagementDCCServiceLocator keyManagementServiceLocator = new KeyManagementDCCServiceLocator();
        try {
            keyManagementService = keyManagementServiceLocator.getKeyManagementDCCIFPort(new URL(urls[0] + KM_LOCATION_SERVLET_PATH));
        } catch (MalformedURLException e) {
            LOG.warn("Unable to get keyManagementService", e);
        } catch (ServiceException e) {
            LOG.warn("Unable to get keyManagementService", e);
        }
        
        return keyManagementService;
    }

    public void init (IRegisteredDCCComponent notUsed) {
        // We aren't a "true plugin" yet, so there is nothing to do here
        return;
    }

    public Serializable serviceHeartbeatRequest(String providerName, String data) {
        return serviceHeartbeatRequest(providerName, SerializationUtils.unwrapSerialized(data));
    }

    public Serializable serviceHeartbeatRequest(String providerName, Serializable requestData) {
        KeyResponseDTO responseDTO = null;

        LOG.debug("serviceHeartbeatRequest " + providerName);
        if (KeyRequestDTO.SERVICE_NAME.equals(providerName) && requestData instanceof KeyRequestDTO) {
            KeyRequestDTO request = (KeyRequestDTO)requestData;

            List<KeyResponseDTO.KeyRingDTO> responseKeyRings = new ArrayList<KeyResponseDTO.KeyRingDTO>();
            responseDTO = new KeyResponseDTO(responseKeyRings);

            updateKeyRingCache();

            // Take a local reference - this value can change unpredictably
            KeyRingsWithKeysDTO keyRings = cachedKeyRingInfo.get();
            
            if (keyRings == null) {
                LOG.warn("Unable to get key rings from Key Management Service");
                return responseDTO;
            }
            
            // build keyrequest timestamp map
            Map<String, Long> keyRequestTsMap = new HashMap<String, Long>();
            if(request.getKeyRings() != null){
                for (KeyRequestDTO.KeyRingDTO keyRingDTO : request.getKeyRings()) {
                    LOG.debug("client requesting keyring " + keyRingDTO.getName() + ", ts=" + keyRingDTO.getTimestamp());
                    keyRequestTsMap.put(keyRingDTO.getName(), keyRingDTO.getTimestamp());
                }
            } else {
                LOG.debug("client requesting no keyrings");
            }

            /* Get the modified date for each key ring.  If the key
             * ring doesn't exist in the request or exists with an
             * earlier modified date then add it to the list send back to
             * the client
             */
            LOG.debug("Checking key rings");
            KeyRingWithKeysDTO[] keyRingWithKeysDTOs = keyRings.getKeyRings();
            if (keyRingWithKeysDTOs != null) {
                for (KeyRingWithKeysDTO keyRing : keyRingWithKeysDTOs) {
                    String name = keyRing.getName();
                    long modifiedDate = keyRing.getLastModifiedDate();

                    LOG.debug(name + " modified on " + modifiedDate);
                    
                    Long clientTime = keyRequestTsMap.get(name);
                    
                    if(clientTime == null || clientTime < modifiedDate) {
                        LOG.info("Adding key ring " + name + " to response. Client time = " + clientTime);
                        List<KeyResponseDTO.KeyDTO> updatedKeys = new ArrayList<KeyResponseDTO.KeyDTO>();
                        
                        KeyDTO[] keyDTOs = keyRing.getKeys();
                        if(keyDTOs != null){
                            for (KeyDTO key : keyRing.getKeys()) {
                                byte[] keyData = key.getKey();
                                int keyVersion = key.getVersion();
                                byte[] id = key.getKeyId().getHash();
                                long timestamp = key.getKeyId().getTimestamp();
    
                                KeyResponseDTO.KeyDTO updatedKey = new KeyResponseDTO.KeyDTO(keyVersion, timestamp, keyData, id);
                                updatedKeys.add(updatedKey);
                            }
    
                            LOG.debug("Added " + keyRing.getKeys().length + " keys");
                        }else{
                            LOG.debug("Added 0 keys, keyRing.getKeys() is null");
                        }
                        KeyResponseDTO.KeyRingDTO updatedKeyRing = new KeyResponseDTO.KeyRingDTO(name, modifiedDate, updatedKeys);

                        responseKeyRings.add(updatedKeyRing);
                    } else {
                        LOG.debug(name + " is up-to-date on client. Client time = " + clientTime);
                    }
                }
            }
        } else {
            LOG.debug("Name didn't match.  Ignoring");
        }

        return responseDTO;
    }

    /**
     * Update the local cache of the key ring data.
     */
    private void updateKeyRingCache() {
        // Do we need to update?
        long lastKeyChanges = latestKeyRingUpdate();
        LOG.debug("Latest change to key ring was " + lastKeyChanges);
        if (lastKeyChanges == 0 || lastKeyChanges  != latestCacheUpdate) {
            synchronized (cachedKeyRingInfo) {
                if (lastKeyChanges == 0 || lastKeyChanges  != latestCacheUpdate) {
                    try {
                        LOG.debug("Updating key rings...");
                        cachedKeyRingInfo.set(getKeyManagementIF().getAllKeyRingsWithKeys());
                        LOG.debug("Done updating key rings...");
                        latestCacheUpdate = lastKeyChanges;
                    } catch (RemoteException re) {
                        latestCacheUpdate = UNKNOWN_TIME;
                    }
                }
            }
        }
    }
    
    /**
     * Query the key service for the last update time
     */
    public long latestKeyRingUpdate() {
        try {
            return getKeyManagementIF().getAllLatestModifiedDate();
        } catch (RemoteException re) {
            LOG.warn("Unable to getAllLatestModifiedDate", re);
            return UNKNOWN_TIME;
        }
    }
}
