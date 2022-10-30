package com.bluejungle.pf.destiny.lib.axis;

/*
 * Created on Feb 25, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc,
 * All rights reserved worldwide.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldData;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.IPolicyQuery;
import com.bluejungle.pf.destiny.policymap.PolicyQueryImpl;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.version.IVersion;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lib/axis/PolicyDeploymentImpl.java#1 $:
 */

public class PolicyDeploymentImpl implements IPolicyDeployment, IManagerEnabled, IInitializable, ILogEnabled {

    public static final ComponentInfo<IPolicyDeployment> COMP_INFO =
        new ComponentInfo<IPolicyDeployment>(
                IPolicyDeployment.class.getName()
              , PolicyDeploymentImpl.class
              , IPolicyDeployment.class
              , LifestyleType.SINGLETON_TYPE
    );

    private IPolicyQuery deployer;
    private IComponentManager manager;
    private IDictionary dictionary;
    private final Map<String,IElementField> userFields = new HashMap<String,IElementField>();
    private IElementField hostName;
    private Log log;
    private long lastKnownDictionaryUpdate = -1;
    private final Set<String> reportedMissingUids =
        Collections.synchronizedSet(new HashSet<String>());
    private final Map<Object,Long> keysOfSubjects =
        Collections.synchronizedMap(new HashMap<Object,Long>());

    /**
     * @see IPolicyDeployment#getDeploymentBundle(DeploymentRequest, Long, String, IVersion)
     */
    public IDeploymentBundle getDeploymentBundle(
        DeploymentRequest request
    ,   Long agentId
    ,   String agentDomain
    ,   IVersion agentVersion
    ) {
        Long[] userIds;
        SystemUser[] systemUsers = request.getPolicyUsers();

        if (systemUsers != null) {
            Set<String> uids = new HashSet<String>();
            List<IElementField> fields = new ArrayList<IElementField>();
            for (SystemUser systemUser : systemUsers) {
                String uid = systemUser.getSystemId();
                if (uid == null) {
                    log.warn("Found a null SID in a bundle request.");
                    continue;
                }
                String idType = systemUser.getUserSubjectType();
                IElementField field = userFields.get(idType);
                if (field == null) {
                    log.warn("Found an unknown UID type in a bundle request: "+idType);
                    continue;
                }
                uids.add(uid);
                fields.add(field);
            }

            Set<String> unsatisfied = new HashSet<String>();
            userIds = getKeysForUniqueField(
                fields.toArray(new IElementField[fields.size()])
            ,   uids.toArray(new String[uids.size()])
            ,   unsatisfied
            );
            if (userIds == null || userIds.length != uids.size()) {
                if (log.isWarnEnabled()) {
                    StringBuffer msg = new StringBuffer();
                    msg.append(
                        "An agent has requested a bundle on behalf of one or more unknown users. "
                    +   "Users with these IDs are not enrolled: {");
                    boolean isFirst = true;
                    for (String s : unsatisfied) {
                        if (reportedMissingUids.contains(s)) {
                            continue;
                        }
                        reportedMissingUids.add(s);
                        if (!isFirst) {
                            msg.append(", ");
                        } else {
                            isFirst = false;
                        }
                        msg.append(s);
                    }
                    msg.append("}");
                    if (!isFirst) {
                        // This means that we've included at least one item
                        log.warn(msg);
                    }
                }
                if (userIds == null) {
                    userIds = new Long[0];
                }
            }
        } else {
            userIds = new Long[0];
        }

        String hostUID = request.getAgentHost();
        Long[] hostIds = getKeysForUniqueField(
            new IElementField[] {hostName}
        ,   new String[] {hostUID}
        ,   new HashSet<String>()
        );
        Long hostId;
        if (hostIds != null && hostIds.length == 1 && hostIds[0] != null) {
            hostId = hostIds[0];
        } else {
            if (!reportedMissingUids.contains(hostUID)) {
                log.warn("Unknown host: " + hostUID);
                reportedMissingUids.add(hostUID);
            }
            hostId = IHasId.UNKNOWN_ID;
        }

        Calendar ts = request.getTimestamp();

        try {
            AgentTypeEnumType agentType = AgentTypeEnumType.getAgentType(request.getAgentType().getValue());

            IDeploymentBundle bundle = deployer.getDeploymentBundle(
                userIds
            ,   hostId
            ,   agentId
            ,   agentDomain
            ,   agentType
            ,   agentVersion
            ,   ts
            );

            return bundle;
        } catch ( Exception e ) {
            log.error("Error in building deployment bundle: ", e);
        }
        
        return null;
    }

    /**
     * get a subject element from dictionary by given Sid field  and Sid string value
     * @param field
     * @param value
     * @return subject in IElement type
     */
    private Long[] getKeysForUniqueField(IElementField[] fields, String[] keys, Set<String> unsatisfied) {
        if (keys == null || keys.length == 0 || fields == null || fields.length != keys.length) {
            return new Long[0];
        }
        Date consistentTime;
        try {
            /* This block is synchronized because it updates a field
             * that is shared among multiple threads.
             * We cannot synchronize the entire method to avoid creating
             * a bottleneck. There is a possibility of the cache
             * getting out of sync with the dictionary when a UID is changed,
             * but it does not create significant problems because the correct
             * data will be cached righ after clearing out the cache.
             */ 
            synchronized(this) {
                consistentTime = dictionary.getLatestConsistentTime();
                if (consistentTime.getTime() != lastKnownDictionaryUpdate) {
                    lastKnownDictionaryUpdate = consistentTime.getTime();
                    reportedMissingUids.clear();
                    keysOfSubjects.clear();
                }
            }
        } catch (DictionaryException e) {
            log.error("Unable to get a subject for UIDs", e);
            return new Long[0];
        }
        List<Long> res = new ArrayList<Long>();
        for ( int i = 0 ; i != keys.length ; i++ ) {
            Long id = keysOfSubjects.get(keys[i]);
            if (id != null) {
                res.add(id);
                keys[i] = null;
            }
        }
        IPredicate condition;
        Set<IElementField> uidFieldSet = new HashSet<IElementField>();
        List<IPredicate> preds = new ArrayList<IPredicate>();
        for (int i = 0 ; i != keys.length ; i++) {
            if (keys[i] == null) {
                continue;
            }
            preds.add(
                fields[i].buildRelation(
                    RelationOp.EQUALS
                ,   Constant.build(keys[i])
                )
            );
            uidFieldSet.add(fields[i]);
            unsatisfied.add(keys[i]);
        }
        if (preds.size() > 1) {
            condition = new CompositePredicate(
                BooleanOp.OR
            ,   preds
            );
        } else if (preds.size() == 1) {
            condition = preds.get(0);
        } else {
            // We have resolved everything though the cache
            return res.toArray(new Long[res.size()]);
        }
        IElementField uidFields[] = uidFieldSet.toArray(new IElementField[uidFieldSet.size()]);
        IDictionaryIterator<ElementFieldData> iter = null;
        try {
            iter = dictionary.queryFields(
                uidFields
            ,   condition
            ,   consistentTime
            ,   null
            ,   null
            );
            while( iter.hasNext() ) {
                ElementFieldData data = iter.next();
                Object[] uids = data.getData();
                Long key = data.getInternalKey();
                for ( Object uid : uids ) {
                    if (uid != null) {
                        unsatisfied.remove(uid);
                        keysOfSubjects.put(uid, key);
                    }
                }
                res.add(key);
            }
            return res.toArray(new Long[res.size()]);
        } catch (DictionaryException e ) {
            log.error("Unable to get a subject for UIDs", e);
            return new Long[0];
        } finally {
            if ( iter != null ) {
                try {
                    iter.close();
                } catch ( DictionaryException e ) {
                    log.error("Can not close search result:", e);
                }
            }
        }
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        deployer = (IPolicyQuery) manager.getComponent(PolicyQueryImpl.COMP_INFO);
        dictionary = (IDictionary) manager.getComponent(Dictionary.COMP_INFO);
        try {
            IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
            userFields.put(
                UserReservedFieldEnumType.WINDOWS_SID.getName()
            ,   userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName())
            );
            userFields.put(
                UserReservedFieldEnumType.UNIX_ID.getName()
            ,   userType.getField(UserReservedFieldEnumType.UNIX_ID.getName())
            );
            IElementType hostTYpe = this.dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            hostName = hostTYpe.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        } catch ( DictionaryException e ) {
            throw new IllegalArgumentException("failed to get subject types from dictionary:" + e.getMessage());
        }
    }

    /**
     * Returns the manager.
     * @return the manager.
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Sets the manager
     * @param manager The manager to set.
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the log.
     * @return the log.
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Sets the log
     * @param log The log to set.
     */
    public void setLog(Log log) {
        this.log = log;
    }

}
