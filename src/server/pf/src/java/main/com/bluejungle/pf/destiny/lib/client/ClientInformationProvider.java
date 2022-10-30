package com.bluejungle.pf.destiny.lib.client;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lib/client/ClientInformationProvider.java#1 $
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldData;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ClientInfoReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.heartbeat.IHeartbeatProvider;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.destiny.lib.ClientInformationDTO;
import com.bluejungle.pf.destiny.lib.ClientInformationRequestDTO;

/**
 * This heartbeat information provider prepares client information
 * for sending to the agents.
 *
 * @author Sergey Kalinichenko, Alan Morgan
 */
public class ClientInformationProvider implements IHeartbeatProvider {

    /**
     * The log of the client information provider.
     */
    private static final Log log = LogFactory.getLog(ClientInformationProvider.class.getName());

    /**
     * The component manager for accessing the dictionary object.
     */
    private final IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    /**
     * The dictionary for reading the client information.
     */
    private final IDictionary dictionary;

    /**
     * The user-to-client map, matching dictionary IDs of users to
     * indexes of clients with whom they are associated.
     */
    private Map<Long,List<Integer>> userToClient = new HashMap<Long,List<Integer>>();  

    /**
     * The last time when the client information has been enrolled into the dictionary.
     */
    private Date lastBuildTime = null;

    /**
     * The cached DTO from which agent-specific DTOs are produced.
     */
    private ClientInformationDTO cachedDTO = null;

    private final IElementType clientType;
    private final IElementType userType;
    private final IElementField clientId;
    private final IElementField shortName;
    private final IElementField longName;
    private final IElementField domains;
    private final IElementField users;

    /**
     * Creates a client information provider.
     *
     * @throws DictionaryException if it is not possible to access the dictionary.
     */
    public ClientInformationProvider() throws DictionaryException {
        dictionary = componentManager.getComponent(Dictionary.COMP_INFO);
        clientType = dictionary.getType(ElementTypeEnumType.CLIENT_INFO.getName());
        clientId = clientType.getField(ClientInfoReservedFieldEnumType.IDENTIFIER.getName());
        shortName = clientType.getField(ClientInfoReservedFieldEnumType.SHORT_NAME.getName());
        longName = clientType.getField(ClientInfoReservedFieldEnumType.LONG_NAME.getName());
        domains = clientType.getField(ClientInfoReservedFieldEnumType.EMAIL_TEMPLATES.getName());
        users = clientType.getField(ClientInfoReservedFieldEnumType.USER_NAMES.getName());
        userType = dictionary.getType(ElementTypeEnumType.USER.getName());
    }

    public Serializable serviceHeartbeatRequest(String name, String data) {
        return serviceHeartbeatRequest(name, SerializationUtils.unwrapSerialized(data));
    }

    /**
     * Reads the data from the dictionary, or use cached data if available.
     * Based on the cached "generic" DTO, build an agent-specific one
     * by resolving UIDs to corresponding dictionary keys, and looking up
     * their values in the map of dictionary IDs to clients.
     */
    public Serializable serviceHeartbeatRequest(String name, Serializable requestData) {
        if (!(requestData instanceof ClientInformationRequestDTO)) {
            return null;
        }
        ClientInformationRequestDTO req = (ClientInformationRequestDTO)requestData;
        try {
            Date latestConsistentTime = dictionary.getLatestConsistentTime();
            if (req.getTimestamp().before(latestConsistentTime)) {
                Map<Long,List<Integer>> localUserToClient = null;
                synchronized(this) {
                    if (lastBuildTime == null || lastBuildTime.before(latestConsistentTime)) {
                        rebuild(latestConsistentTime);
                    }
                    localUserToClient = userToClient;
                }
                if (cachedDTO == null) {
                    return null;
                }
                ClientInformationDTO res = (ClientInformationDTO)cachedDTO.clone();
                IElementField keyField = userType.getField(req.getUidType());
                String[] uids = req.getUids();
                res.setPreparedForUids(uids);
                if (keyField != null && uids != null && uids.length != 0) {
                    List<IPredicate> uidPreds = new ArrayList<IPredicate>();
                    for (String uid : uids) {
                        uidPreds.add(new Relation(RelationOp.EQUALS, keyField, Constant.build(uid)));
                    }
                    Map<Integer,List<String>> clientIndexToUsers = new HashMap<Integer,List<String>>();
                    IDictionaryIterator<ElementFieldData> iter = dictionary.queryFields(
                        new IElementField[] {keyField}
                    ,   new CompositePredicate(BooleanOp.OR, uidPreds)
                    ,   latestConsistentTime
                    ,   null
                    ,   null
                    );
                    try {
                        while (iter.hasNext()) {
                            ElementFieldData data = iter.next();
                            List<Integer> index = localUserToClient.get(data.getInternalKey());
                            if (index == null) {
                                log.warn("Unresolved user reference, ID="+data.getInternalKey());
                                continue;
                            }
                            String uid = (String)data.getData()[0];
                            for (Integer ind : index) {
                                List<String> list = clientIndexToUsers.get(ind);
                                if (list == null) {
                                    clientIndexToUsers.put(ind, list = new ArrayList<String>());
                                }
                                list.add(uid);
                            }
                        }
                    } finally {
                        iter.close();
                    }
                    for (Map.Entry<Integer,List<String>> entry : clientIndexToUsers.entrySet()) {
                        List<String> list = entry.getValue();
                        res.setUids(entry.getKey(), list.toArray(new String[list.size()]));
                    }
                }
                return res;
            } else {
                return null;
            }
        } catch (CloneNotSupportedException ce) {
            return null;
        } catch (DictionaryException de) {
            return null;
        }
    }

    /**
     * Rebuilds the cached DTO and the Map of dictionary IDs to clients.
     *
     * @param latestConsistentTime The last consistent time as of which
     * to read the dictionary.
     *
     * @throws DictionaryException if an error is thrown from the dictionary code.
     */
    private void rebuild(Date latestConsistentTime) throws DictionaryException {
        log.info("Preparing client information as of "+latestConsistentTime);
        long start = System.currentTimeMillis();
        userToClient = new HashMap<Long,List<Integer>>();
        ClientInformationDTO res = new ClientInformationDTO(latestConsistentTime);

        IDictionaryIterator<IMElement> iter = dictionary.query(
            dictionary.condition(clientType)
        ,   latestConsistentTime
        ,   null
        ,   null
        );
        try {
            Integer i = 0;
            while (iter.hasNext()) {
                IElement client = iter.next();
                res.addClient(
                    (String)client.getValue(clientId)
                ,   (String)client.getValue(shortName)
                ,   (String)client.getValue(longName)
                ,   (String[])client.getValue(domains)
                ,   null
                );
                for (Long userId : (long[])client.getValue(users)) {
                    List<Integer> list = userToClient.get(userId);
                    if (list == null) {
                        userToClient.put(userId, list = new ArrayList<Integer>());
                    }
                    list.add(i);
                }
                i++;
            }
        } finally {
            iter.close();
        }
        cachedDTO = res;
        lastBuildTime = latestConsistentTime;
        long delta = System.currentTimeMillis() - start;
        log.info("Finished preparing client information in "+delta/1000+"."+delta%1000+" seconds");
    }

}
