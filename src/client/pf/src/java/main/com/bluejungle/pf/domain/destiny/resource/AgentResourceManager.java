/*
 * Created on Feb 25, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.domain.destiny.resource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSType;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.utils.LockByName;
import com.bluejungle.pf.destiny.services.UNCUtil;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.epicenter.resource.AbstractResourceManager;
import com.bluejungle.pf.domain.epicenter.resource.IResourceAttributeLoader;
import com.bluejungle.pf.engine.destiny.EngineResourceInformation;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/resource/AgentResourceManager.java#1 $:
 */

public class AgentResourceManager extends AbstractResourceManager<EngineResourceInformation>
    implements IInitializable, IManagerEnabled, ILogEnabled, IConfigurable, IDisposable {

    public static final ComponentInfo<AgentResourceManager> COMP_INFO = new ComponentInfo<AgentResourceManager>(AgentResourceManager.class.getName(), 
                                                                                                                AgentResourceManager.class, 
                                                                                                                AgentResourceManager.class, 
                                                                                                                LifestyleType.SINGLETON_TYPE);

    public static final PropertyKey<AgentTypeEnumType> AGENT_TYPE_CONFIG_PARAM = new PropertyKey<AgentTypeEnumType>("AgentType");
    public static final PropertyKey<Long> DISK_CACHE_FLUSH_FREQUENCY_CONFIG_PARAM = new PropertyKey<Long>("DiskCacheFlushFrequency");
    public static final PropertyKey<String> DISK_CACHE_LOCATION_CONFIG_PARAM = new PropertyKey<String>("DiskCacheLocation");
    public static final PropertyKey<Integer> DISK_CACHE_SIZE_CONFIG_PARAM= new PropertyKey<Integer>("DiskCacheSize");
    
    private static final int MAX_DESKTOP_CACHE_ELEMENTS = 1000;
    private static final int MAX_FILE_SERVER_CACHE_ELEMENTS = 20000;

    private static final long DISK_CACHE_FLUSH_DEFAULT_FREQUENCY = 30 * 60 * 1000; //30 minutes
    private static final long DISK_CACHE_FLUSH_MIN_FREQUENCY = 30 * 60 * 1000;
    private static final long DISK_CACHE_FLUSH_MAX_FREQUENCY = Long.MAX_VALUE; //24 * 60 * 60 * 1000; //1 days
    private static final String DEFAULT_DISK_CACHE_LOCATION = ".";

    private IComponentManager manager;
    private static IOSWrapper osWrapper;
    private Log log;
    private IConfiguration config;
    private AgentTypeEnumType agentType;
    private IAgentResourceCacheManager agentResourceCacheManager = IAgentResourceCacheManager.DUMMY_CACHE;

    private static final String ATTR_NAME = "name";
    private static final String ATTR_SIZE = "size";
    private static final String ATTR_ACC_DATE = "access_date";
    private static final String ATTR_CREATION_DATE = "created_date";
    private static final String ATTR_MOD_DATE = "modified_date";
    private static final String ATTR_OWNER = "owner";
    private static final String ATTR_LDAP_OWNER = "owner_ldap_group";
    static final String ATTR_IS_DIR = "isdirectory";

    private static final String UNKNOWN_SID = "-1";
    private static final IEvalValue UNKNOWN_SID_VALUE = EvalValue.build(UNKNOWN_SID);
    private static final IEvalValue UNKNOWN_DATE_VALUE = EvalValue.build(new Date(0));

    private static final Set<String> localDrives = new HashSet<String>();
    
    static {
        osWrapper = (IOSWrapper)ComponentManagerFactory.getComponentManager().getComponent( OSWrapper.class );
        File[] roots = File.listRoots();
        for ( int i = 0 ; i != roots.length ; i++ ) {
            // osWrapper is in static initializer block, so it should be here, no need to check
            if (osWrapper.isRemovableMedia(roots[i].getAbsolutePath()) != true) {
                if (roots[i].isDirectory()) {
                    localDrives.add(canonicalizeName(roots[i].getAbsolutePath()));
                }
            }
        }
    }

    private static String canonicalizeName(String nativeName) {
        nativeName = nativeName.replace ('\\', '/');
        if (nativeName.startsWith ("//")) {
            return "file:" + nativeName.toLowerCase ();
        } else {
            return "file:///" + nativeName.toLowerCase ();
        }
    }
    
    static IOSWrapper getOSWrapper() {
        return osWrapper;
    }

    /**
     * The default constructor.
     */
    public AgentResourceManager() {
        super(EngineResourceInformation.class);
    }

    public ResourceInformation<EngineResourceInformation> getResource(String resName, List<String> alternateNames, AgentTypeEnumType agentType) {
        return getResource(resName, alternateNames, null, null, agentType);
    }

    public ResourceInformation<EngineResourceInformation> getResource(
        final String resName
    ,   final List<String> alternateNames
    ,   final Map<String, IEvalValue> attr
    ,   final Long processToken
    ,   final AgentTypeEnumType agentType
    ) {
        ResourceInformation<EngineResourceInformation> ri = getResourceInfo(resName, new Map<String,IEvalValue>() {
            private Map<String,IEvalValue> map;
            private boolean initialized = false;
            private boolean locallyVisibleFile = true;
            private String nativeResName = null;
            private String processedName = null;
            private String ownerSid = UNKNOWN_SID;
            private long modDate = 0;
            private long accDate = 0;
            private long createdDate = 0;
            private long fileSize = -1;
            private boolean isDirectory = false;
            private boolean fileExists = true;
            
            public void clear() {
                throw new UnsupportedOperationException("clear");
            }
            public boolean containsKey(Object key) {
                return getMap(key).containsKey(key);
            }

            public boolean containsValue(Object value) {
                return getMap(null).containsValue(value);
            }
            public Set<Map.Entry<String, IEvalValue>> entrySet() {
                return getMap(null).entrySet();
            }
            public IEvalValue get(Object key) {
                return getMap(key).get(key);
            }
            public boolean isEmpty() {
                return getMap(null).isEmpty();
            }
            public Set<String> keySet() {
                return getMap(null).keySet();
            }
            public IEvalValue put(String key, IEvalValue value) {
                throw new UnsupportedOperationException("put");
            }
            public void putAll(Map<? extends String, ? extends IEvalValue> t) {
                throw new UnsupportedOperationException("putAll");
            }
            public IEvalValue remove(Object key) {
                throw new UnsupportedOperationException("remove");
            }
            public int size() {
                return getMap(null).size();
            }
            public Collection<IEvalValue> values() {
                return getMap(null).values();
            }
            private Map<String,IEvalValue> getMap(Object key) {
                // This key can only ever be in the enforcer supplied attributes,
                // so skip the regular code if we are being asked for it
                if (SpecAttribute.NOCACHE_NAME.equalsIgnoreCase((String)key)) {
                    if (attr == null) {
                        return new HashMap<String, IEvalValue>();
                    } else {
                        return attr;
                    }
                } 

                if (map == null) {
                    if (attr == null) {
                        map = new HashMap<String, IEvalValue>();
                    } else {
                        map = attr;
                    }

                    // Add name, owner SID, and modification time
                    processedName = resName;

                    if (attr != null) {
                        IEvalValue fsCheck = attr.get(SpecAttribute.FSCHECK_ATTR_NAME);

                        if (fsCheck != null && ((String)fsCheck.getValue()).equals("no")) {
                            locallyVisibleFile = false;
                        }
                    }

                    if (OSType.getSystemOS() == OSType.OS_LINUX && attr != null) {
                        processedName = (String)attr.get(SpecAttribute.ID_ATTR_NAME).getValue();
                    }

                    nativeResName = getNativeName(processedName);

                    // Make it available to all
                    if (locallyVisibleFile && map.get(SpecAttribute.NATIVE_RESOURCE_NAME) == null) {
                        map.put(SpecAttribute.NATIVE_RESOURCE_NAME, EvalValue.build(nativeResName));
                    }

                    if (map.get(ATTR_OWNER) == null) {
                        String sid = UNKNOWN_SID;
                        if (locallyVisibleFile) {
                            int agentTypeArg = (agentType == AgentTypeEnumType.DESKTOP) ? 0 : 1;

                            String ownerSid = osWrapper.getOwnerSID(nativeResName, agentTypeArg, processToken);
                            if (ownerSid != null && ownerSid.length() != 0) {
                                sid = ownerSid;
                            }
                        }

                        log.info("Resource " + nativeResName + " owner is " + sid);
                        IEvalValue sidVal = EvalValue.build(sid);
                        map.put(ATTR_OWNER, sidVal);
                        map.put(ATTR_LDAP_OWNER, sidVal);
                    }

                    if (locallyVisibleFile) {
                        long[] basicAttributes = osWrapper.getFileBasicAttributes(nativeResName, processToken);
                        if (basicAttributes != null && basicAttributes.length == 5) {
                            modDate = basicAttributes[0];
                            accDate = basicAttributes[1];
                            createdDate = basicAttributes[2];
                            fileSize = basicAttributes[3];
                            isDirectory = (basicAttributes[4] != 0);
                        } else {
                            fileExists = false;
                        }

                        IEvalValue mapModTime = map.get(ATTR_MOD_DATE);
                        if (mapModTime == null) {
                            map.put(ATTR_MOD_DATE, EvalValue.build(new Date(modDate)));
                        }
                    }

                    List<String> alternatives;
                    if (alternateNames != null) {
                        alternatives = new ArrayList<String>(alternateNames.size()+1);
                        for (String an : alternateNames) {
                            alternatives.add(escape(an));
                        }
                    } else {
                        alternatives = new ArrayList<String>(1);
                    }
                    alternatives.add(escape(canonicalizeName(makeNative(resName))));
                    IEvalValue nativeNames = EvalValue.build(Multivalue.create(alternatives));
                    map.put(ATTR_NAME, nativeNames);
                }

                if (!initialized 
                 && !ATTR_OWNER.equalsIgnoreCase((String)key)
                 && !ATTR_MOD_DATE.equalsIgnoreCase((String)key)
                 && !SpecAttribute.NOCACHE_NAME.equalsIgnoreCase((String)key)
                 && locallyVisibleFile) {
                    // Process standard attributes
                    IEvalValue mapIsDir = map.get(ATTR_IS_DIR);
                    boolean needIsDir = mapIsDir  == null;
                    IEvalValue mapFileSize = map.get(ATTR_SIZE);
                    boolean needFileSize = mapFileSize == null;

                    if (needIsDir || needFileSize) {
                        if (needFileSize) {
                            map.put(ATTR_SIZE, EvalValue.build(fileSize));
                        }

                        if (needIsDir) {
                            if (!fileExists) {
                                map.put(ATTR_IS_DIR, IEvalValue.NULL);
                            } else {
                                boolean isLocal = false;
                                for (String ld : localDrives) {
                                    isLocal = processedName.startsWith(ld);
                                    if (isLocal) {
                                        break;
                                    }
                                }
                                long resVal;
                                if (isLocal) {
                                    if (isDirectory) {
                                        if (osWrapper.isEmptyDirectory(nativeResName, processToken)) {
                                            resVal = -1;
                                        } else {
                                            resVal = 1;
                                        }
                                    } else {
                                        resVal = 0;
                                    }
                                } else {
                                    resVal = isDirectory ? 1 : 0;
                                }
                                map.put(ATTR_IS_DIR, EvalValue.build(resVal));
                            }
                        }
                    }

                    IEvalValue mapAccDate = map.get(ATTR_ACC_DATE);
                    if (mapAccDate == null) {
                        map.put(ATTR_ACC_DATE, EvalValue.build(new Date(accDate)));
                    }

                    IEvalValue mapCreationDate = map.get(ATTR_CREATION_DATE);
                    if (mapCreationDate == null) {
                        map.put(ATTR_CREATION_DATE, EvalValue.build(new Date(createdDate)));
                    }

                    IEvalValue eval = map.get(SpecAttribute.FSO_ATTR_INCLUDED_ATTR_NAME);
                    boolean needFSOAttrs = true;
                    if (eval != null && eval != IEvalValue.EMPTY && ((String)eval.getValue()).equals("yes")) {
                        needFSOAttrs = false;
                    }
                    initialized = true;
                }
                return map;
            }
            }, new AgentResourceAttributeLoader());

        ri.getMutableResource().setProcessToken(processToken);
        return ri;
    }

    private static long numCacheRequests = 0;
    private static long numCacheHits = 0;

    // Report the cache stats after ever REPORT_CACHE_STATS_EVERY lookup attempts
    private static final int REPORT_CACHE_STATS_EVERY = 100;

    private void printCacheHitStats() {
        if (numCacheRequests != 0 && numCacheRequests % REPORT_CACHE_STATS_EVERY == 0) {
            long hitPercentage = (numCacheHits * 100)/numCacheRequests;
            
            log.debug("Resource cache hit " + numCacheHits + " times in " + numCacheRequests + " requests (" + hitPercentage + "%)");
        }
    }

    /**
     * @see AbstractResourceManager#getCached(Serializable, Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ResourceInformation<EngineResourceInformation> getCached(Serializable id, Map<String, IEvalValue> attr) {
        printCacheHitStats();
        numCacheRequests++;

        Element element = agentResourceCacheManager.get(id);

        if (element != null) {
            ResourceInformation<EngineResourceInformation> res = (ResourceInformation<EngineResourceInformation>)element.getValue();
            if (res == null) {
                log.error("Unable to get resource " + id + " from cached entry\n");
                return null;
            }

            IEvalValue ownerNow = attr.get(ATTR_OWNER);
            IEvalValue ownerThen = res.getResource().getAttribute(ATTR_OWNER);
            if (ownerNow == null || (!ownerNow.equals(ownerThen) && !ownerNow.equals(UNKNOWN_SID_VALUE))) {
                if (log.isDebugEnabled()) {
                    String ownerNowName = (ownerNow == null || ownerNow.getValue() == IEvalValue.NULL) ? "<NONE>" : (String)ownerNow.getValue();
                    String ownerThenName = (ownerThen == null || ownerThen.getValue() == IEvalValue.NULL) ? "<NONE>" : (String)ownerThen.getValue();
                    log.debug("Resource " + id + " found in resource cache with different owner (" + ownerThenName + " vs " + ownerNowName + ")");
                }
                agentResourceCacheManager.remove(id);
                return null;
            }
            IEvalValue modNow = attr.get(ATTR_MOD_DATE);
            IEvalValue modThen = res.getResource().getAttribute(ATTR_MOD_DATE);
            if (modNow == null || (!modNow.equals(modThen) && !modNow.equals(UNKNOWN_DATE_VALUE))) {
                if (log.isDebugEnabled()) {
                    Object modNowTime = (modNow == null || modNow.getValue() == IEvalValue.NULL) ? "-1" : modNow.getValue();
                    Object modThenTime = (modThen == null || modThen.getValue() == IEvalValue.NULL) ? "-1" : modThen.getValue();
                    log.debug("Resource " + id + " found in resource cache with different modified date (" + modThenTime + " vs " + modNowTime + ")");
                }
                agentResourceCacheManager.remove(id);
                return null;
            }
            numCacheHits++;
            return res;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to find resource " + id + " in resource cache\n");
            }
            return null;
        }
    }

    /**
     * @see AbstractResourceManager#saveToCache(IPair)
     */
    @Override
    protected void saveToCache(ResourceInformation<EngineResourceInformation> data) {
        agentResourceCacheManager.put(new Element(data.getResource().getIdentifier(), data));
    }

    private static String getNativeName (String canonicalName) {
        return getNativeName(canonicalName, File.separatorChar);
    }

    private static String getNativeName(String canonicalName, char separator) {
        if ( canonicalName == null ) {
            return null;
        }
        
        return canonicalName.replaceAll("^file:(///)?", "").replace ('/', separator);
    }

    static String makeNative(String canonical) {
        if (canonical.startsWith ("file:///")) {
            return canonical.substring ("file:///".length()).replace ('/', '\\');
        } else if (canonical.startsWith ("file:")) {
            return canonical.substring ("file:".length()).replace ('/', '\\');
        } else {
            return canonical;
        }
    }
    

    private static String escape(String s) {
        assert(s != null);
        final StringBuffer res = new StringBuffer();

        char[] chars = s.toCharArray();

        int startSequence = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == ';' || c == '\\') {
                res.append(chars, startSequence, i - startSequence);
                res.append('\\');
                res.append(c);
                startSequence = i+1;
            }
        }

        if (startSequence < chars.length) {
            res.append(chars, startSequence, chars.length - startSequence);
        }
    
        return res.toString();
    }
    
   /**
     * @see IInitializable#init()
     */
    public void init() {
        long diskCacheFlushFrequency = (config == null) ? DISK_CACHE_FLUSH_DEFAULT_FREQUENCY : config.get(DISK_CACHE_FLUSH_FREQUENCY_CONFIG_PARAM, DISK_CACHE_FLUSH_DEFAULT_FREQUENCY);

        if (diskCacheFlushFrequency < DISK_CACHE_FLUSH_MIN_FREQUENCY) {
            diskCacheFlushFrequency = DISK_CACHE_FLUSH_MIN_FREQUENCY;
        } else if (diskCacheFlushFrequency > DISK_CACHE_FLUSH_MAX_FREQUENCY) {
            diskCacheFlushFrequency = DISK_CACHE_FLUSH_MAX_FREQUENCY;
        }
        
        final String diskStoreLocation = (config == null) ? DEFAULT_DISK_CACHE_LOCATION : config.get(DISK_CACHE_LOCATION_CONFIG_PARAM, DEFAULT_DISK_CACHE_LOCATION);;

        final int diskCacheDefaultSize = (agentType == AgentTypeEnumType.FILE_SERVER) ? MAX_FILE_SERVER_CACHE_ELEMENTS : MAX_DESKTOP_CACHE_ELEMENTS;

        final int diskCacheSize = (config == null) ? diskCacheDefaultSize : config.get(DISK_CACHE_SIZE_CONFIG_PARAM, diskCacheDefaultSize);

        if (diskCacheSize > 0) {
            agentResourceCacheManager = new AgentResourceCacheManager(agentType, diskCacheFlushFrequency, diskStoreLocation, diskCacheSize);
        }
    }

    /**
     * @see IManagerEnabled#setManager(IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see ILogEnabled#setLog(Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     *
     */
    public void clearCache() {
        agentResourceCacheManager.clearCache();
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
        if (config != null) {
            agentType = config.get(AGENT_TYPE_CONFIG_PARAM, AgentTypeEnumType.FILE_SERVER);
        } else {
            agentType = AgentTypeEnumType.FILE_SERVER;
        }
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public synchronized void dispose() {
        agentResourceCacheManager.dispose();
    }
    
    // Exposed for unit tests
    protected void spoolAllToDisk() {
        agentResourceCacheManager.spoolAllToDisk();
    }

    protected IAgentResourceCacheManager getAgentResourceCacheManager() {
        return agentResourceCacheManager;
    }
    
}

class AgentResourceAttributeLoader implements IResourceAttributeLoader {
    /**
     * The default serialization ID.
     */
    private static final long serialVersionUID = 1L;
    private static Log log;
    private static LockByName nameLock;
    
    private transient Long processToken;

    static
    {
        log = LogFactory.getLog(AgentResourceAttributeLoader.class.getName());
        nameLock = new LockByName();
    }

    public AgentResourceAttributeLoader() {
    }

    public void setProcessToken(Long processToken) {
        this.processToken = processToken;
    }

    public void getAttrs(Map<String, IEvalValue> attrs) {

        // Check to make sure that we need these attributes and that we can get them
        IEvalValue eval = attrs.get(SpecAttribute.FSO_ATTR_INCLUDED_ATTR_NAME);
        boolean needFSOAttrs = true;
        if (eval != null && eval != IEvalValue.EMPTY && ((String)eval.getValue()).equals("yes")) {
            needFSOAttrs = false;
        }
        
        IEvalValue fsCheck = attrs.get(SpecAttribute.FSCHECK_ATTR_NAME);
        boolean locallyVisibleFile = true;
        if (fsCheck != null && fsCheck != IEvalValue.EMPTY && ((String)fsCheck.getValue()).equals("no")) {
            locallyVisibleFile = false;
        }
        
        IEvalValue isDir = attrs.get(AgentResourceManager.ATTR_IS_DIR);
        boolean isDirectory = false;

        if (isDir != null && isDir != IEvalValue.NULL && ((Long)isDir.getValue() != 0)) {
            isDirectory = true;
        }

        if (needFSOAttrs && locallyVisibleFile && !isDirectory) {
            String resName = (String)attrs.get(SpecAttribute.NATIVE_RESOURCE_NAME).getValue();

            // Process custom attributes
            long enterTime = System.nanoTime();
            String[] custom = getCustomAttributes(resName, processToken);
            long elapsedTime = (System.nanoTime() - enterTime)/1000000;
            Map<String,List<Object>> attrVals = new HashMap<String,List<Object>>();
            Map<String,ValueType> attrTypes = new HashMap<String,ValueType>();
            StringBuilder attributeDetails = new StringBuilder("Attributes for " + resName + "\n");
            // Custom attributes support multivalues, so collect values into attr first
            if (custom != null && custom.length%3 == 0) {
                for ( int i = 0 ; i != custom.length ; i += 3) {
                    String attrName = custom[i];
                    String typeName = custom[i+1];
                    String valueStr = custom[i+2];
                    if (attrName == null || attrName.length() == 0) {
                        continue;
                    }
                    attrName = attrName.toLowerCase();
                    if (typeName == null || typeName.length() == 0) {
                        continue;
                    }
                    ValueType type = ValueType.forName(typeName.toLowerCase());
                    Object val;

                    if (log.isInfoEnabled()) {
                        attributeDetails.append("  ");
                        attributeDetails.append(attrName);
                        attributeDetails.append(": ");
                        if (valueStr == null) {
                            attributeDetails.append("<<NULL>>");
                        } else {
                            attributeDetails.append(valueStr);
                        }
                        attributeDetails.append("\n");
                    }
                    try {
                        if (type == ValueType.STRING) {
                            val = valueStr;
                        } else if (type == ValueType.LONG) {
                            val = Long.parseLong(valueStr);
                        } else if (type == ValueType.DATE) {
                            val = new Date(Long.parseLong(valueStr));
                        } else {
                            continue;
                        }
                    } catch (NumberFormatException ignore) {
                        continue;
                    }
                    List<Object> list = attrVals.get(attrName);
                    if (list == null) {
                        list = new ArrayList<Object>();
                        attrVals.put(attrName, list);
                    }
                    ValueType existingType = attrTypes.get(attrName);
                    if (existingType == null) {
                        attrTypes.put(attrName, type);
                    } else if (type != existingType){
                        continue;
                    }
                    list.add(val);
                }
                // Process collected attributes
                for (Map.Entry<String,List<Object>> e : attrVals.entrySet()) {
                    String attrName = e.getKey();
                    List<Object> vals = e.getValue();
                    ValueType type = attrTypes.get(attrName);
                    if (vals.size() == 1) {
                        attrs.put(attrName, new EvalValue(type, vals.get(0)));
                    } else {
                        attrs.put(attrName ,   EvalValue.build(Multivalue.create(vals, type)));
                    }
                }
                if (log.isInfoEnabled()) {
                    log.info(attributeDetails.toString() + "(" + elapsedTime + "ms)");
                }
            } else if (log.isInfoEnabled()) {
                log.info(attributeDetails.toString() + "NO ATTRIBUTES FOUND(" + elapsedTime + "ms)");
            }
        }
    }

    private static int CUSTOM_ATTRIBUTE_RETRY_LIMIT=5;
    
    private String[] getCustomAttributes(String resourceString, Object opaque) {
        IOSWrapper osWrapper = AgentResourceManager.getOSWrapper();

        if (osWrapper != null && resourceString != null && resourceString.length() != 0) {
            String nativeName = AgentResourceManager.makeNative(resourceString);

            for (int n = 0; n < CUSTOM_ATTRIBUTE_RETRY_LIMIT; n++) {
                try {
                    nameLock.lock(nativeName);
                    
                    String[] raw = osWrapper.getFileCustomAttributes(nativeName, opaque);
                    if (raw == null || raw.length%2 != 0) {
                        return null;
                    }
                    String[] res = new String[3*(raw.length/2)];
                    int j = 0;
                    for (int i = 0 ; i != raw.length ; i += 2) {
                        res[j++] = raw[i];   // name
                        res[j++] = "STRING"; // type
                        res[j++] = raw[i+1]; // value
                    }
                    return res;
                } catch (InterruptedException ie) {
                    log.warn("Interrupted exception taking lock for custom attributes on resource " + nativeName + "(attempt " + (n+1) + ")");
                } finally {
                    nameLock.unlock(nativeName);
                }
            }

            return null;
        } else {
            return null;
        }
    }
}
