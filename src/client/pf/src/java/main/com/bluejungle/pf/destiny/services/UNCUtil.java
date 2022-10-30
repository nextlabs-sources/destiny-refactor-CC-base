/*
 * Created on Oct 24, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.destiny.services;

import java.io.File;
import java.net.InetAddress;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;

//for ehcache
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

// for logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// for reverse DNS lookup using DNSJava 
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/UNCUtil.java#1 $:
 */

public class UNCUtil {

    private static String localHostAddress = null;
    private static List<String> localHostUNC = new ArrayList<String>();
    private static IOSWrapper osWrapper = null;
    public static Cache aliasCache = initializeAliasCache();
    private static Pattern ipAddressPattern = Pattern.compile("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$");
    // Doesn't cover all the uri schemes, but enough for our purposes
    private static Pattern schemePattern = Pattern.compile("^[A-Za-z]*\\:[\\\\/]{2}.*$");

    private static final long networkSharesLiveTime = 60 * 1000;  // 60 seconds
    private static long networkSharesTimeStamp = 0;
    private static List<String> networkShares = new ArrayList<String>();
    private static final Log log = LogFactory.getLog(UNCUtil.class.getName());

    /**
     * If this is a UNC path to the local machine, return the physical path
     * to the resource (e.g. \\mare\shared\foo.txt => c:\data\shared\foo.txt)
     * If it is anything else, just return null
     *
     * @param path
     * @return resolved path if the path is local. otherwise null
     */
    public static String resolveLocalPath(String path) {
        String ret = null;

        if (!path.startsWith("\\\\")) {
            // References a local file by drive letter, so we can skip it
            return null;
        }

        if (path.startsWith("\\\\.\\")) {
            // Device 
            return null;
        }

        if (path.startsWith("\\\\?\\")) {
            if (path.startsWith("\\\\?\\unc\\")) {
                path = "\\\\" + path.substring(8);
            } else {
                // Long UNC that references a local file by drive letter, so we can skip it
                return null;
            }
        }
        int i = path.indexOf(File.separatorChar, 2);
        if (i != -1) {
            String host = path.substring(2, i);
            int j = path.indexOf(File.separatorChar, i + 1);
            if (j == -1) {
                j = path.length();
            }
            String share = path.substring(i + 1, j);

            if (UNCUtil.isLocalHost(host)) {
                return osWrapper.getSharePhysicalPath(share) + path.substring(j);
            }
        }

        return ret;
    }

    /**
     * @param host
     *            hostname or ip
     * @return true if host points to local machine.
     */
    public static boolean isLocalHost(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || UNCUtil.localHostAddress.equals(addr.getHostAddress())) {
                return true;
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return false;
    }


    /**
     * Sets the localHostAddress and initializes oswrapper
     * 
     * @param localHostAddress
     *            The localHostAddress to set.
     */
    public static synchronized void setLocalHost(String localHost) {
        InetAddress addr;

        if (UNCUtil.localHostAddress == null) {
            try {
                addr = InetAddress.getByName(localHost);
                UNCUtil.localHostAddress = addr.getHostAddress();
                
                List<String> tmpLocalHostUNC = getEquivalentHostNames("\\\\" + localHost + "\\");
                UNCUtil.localHostUNC = new ArrayList<String>();
                for (String name : tmpLocalHostUNC) {
                    UNCUtil.localHostUNC.add(name.toLowerCase());
                }
            } catch (UnknownHostException e) {
                UNCUtil.localHostAddress = "";
            }
        }

        if (osWrapper == null) {
            osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
        }
    }

    /**
     * Splits a resource name into the following components:
     *   scheme           :  (e.g. http://)
     *     or
     *   Windows UNC      :  \\
     *   server name      :  (e.g. mare.bluejungle.com)
     *   everything else  :  (e.g. :8080/foo/bar/baz.pdf)
     *
     * If the resource name can't be broken down this way (i.e. c:/foo.txt) then null is returned
     * The components are returned in an array of length 3.  If there is nothing after the server
     * name then that array entry will be an empty string
     */
    private static String[] splitResourceName(String resourceName) {
        if (resourceName == null) {
            return null;
        }

        int resourceNameLength = resourceName.length();

        String prefix = null;
        if (resourceName.startsWith("\\\\")) {
            prefix = "\\\\";
        } else if (schemePattern.matcher(resourceName).matches()) {
            int firstColon = resourceName.indexOf(':');
            if (firstColon < 2 || resourceName.startsWith("file:") || resourceName.startsWith("device:")) {
                // Either a plain file name, drive letter, or some URI without a server name
                return null;
            }

            if (firstColon + 3 >= resourceNameLength) {
                return null;
            }

            prefix = resourceName.substring(0, firstColon+3);
        } else {
            return null;
        }

        String[] result = new String[3];
        
        result[0] = prefix;

        // server name will be terminated by one of : \ / or end of string
        int index = prefix.length();
        for (;index < resourceNameLength; index++) {
            if (resourceName.charAt(index) == ':' ||
                resourceName.charAt(index) == '\\' ||
                resourceName.charAt(index) == '/') {
                break;
            }
        }

        result[1] = resourceName.substring(prefix.length(), index);

        if (index < resourceNameLength) {
            result[2] = resourceName.substring(index, resourceNameLength);
        } else {
            result[2] = "";
        }
        
        return result;
    }

    // Obtain FQDN from IP address, using reverse DNS lookup
    // This code uses third-party code, DNSJava, for high performance
    public static String reverseDNSLookup(String hostIp) throws IOException {
    	Record opt = null;
    	SimpleResolver res = new SimpleResolver();
    	Name name = ReverseMap.fromAddress(hostIp);
    	int type = Type.PTR;
    	int dclass = DClass.IN;
    	Record rec = Record.newRecord(name, type, dclass);
    	Message query = Message.newQuery(rec);
    	res.setTimeout(2);
    	Message response = null;
    	try {
    	    response = res.send(query);
    	} catch (SocketTimeoutException e) {
    		log.warn("Timed out on reverse DNS lookup on IP address: " + hostIp);
    	    return hostIp;
    	}
    	
    	Record[] answers = response.getSectionArray(Section.ANSWER);

    	if (answers.length == 0) {
    		log.warn("Failed to reverse DNS lookup IP address: " + hostIp);
    		return hostIp;
    	}     	else {
    	    // cut trailing "."
    	    String ret = answers[0].rdataToString();
    	    if (ret.charAt(ret.length() - 1) == '.') {
    		ret = ret.substring(0, ret.length() - 1);
    	    }
    		
    	    return ret;
    	}
    }

    /**
     * returns a set of equivalent file names e.g., if the fileName is
     * \\hostname\share\foo.txt, this method will add
     * \\hostname.domain.com\share\foo.txt to the set. if the filename is 
     * \\{ip address}\share\foo.txt, the method adds both \\hostname\share\foo.txt and
     * \\hostname.domain.com\share\foo.txt to the returned set
     * 
     * Later parts of the code (see AgentResourceManager) will attempt to identify the
     * actual resource by trying each of these names in turn.  We try to optimize this
     * by ensuring that resourceName is the first, on the assumption that that's most
     * likely to be the best one.
     *
     * @param resourceName the name of the resource
     * @return a list of alternate formulations of the resource name
     */
    public static List<String> getEquivalentHostNames(String resourceName) {
        // We could do this in splitResourceName (perhaps we should), but we
        // also want to add the resource name into the return set and we don't
        // want the UNC junk
        if (resourceName.startsWith("\\\\?\\")) {
            if (resourceName.startsWith("\\\\?\\unc\\")) {
                // This is followed by the host name
                resourceName = "\\\\" + resourceName.substring(8);
            } else {
                // Just a simple file name
                resourceName = resourceName.substring(4);
            }
        }

        List<String> ret = new ArrayList<String>();
        ret.add (resourceName);

        String[] splitEntries = splitResourceName(resourceName);

        if (splitEntries != null)
        {
            String scheme = splitEntries[0];
            String host = splitEntries[1];
            String remainder = splitEntries[2];

            if (host != null) {
                if (host.equals(".")) {
                    // Device path.  Not very useful
                    return ret;
                }

                Set<String> aliases = null;
                if (aliasCache != null) {
                	try {
                		Element e = aliasCache.get(host);
                		SerializableStringSet serialSet = null;

                		if (e != null) {
                			serialSet = (SerializableStringSet)e.getValue();
                		
                			if (serialSet != null) {
                				aliases = serialSet.getSet();	    
                			}
                		}
                	} catch (CacheException ex) {
                		log.warn("CacheException on host: " + host + ".  Will treat this as a cache miss.", ex);
                		aliases = null;
                	}
                }

                if (aliases == null) {
                    try {
                        InetAddress addr = InetAddress.getByName(host);
                        aliases = new HashSet<String>();
                        String canonicalName = reverseDNSLookup(addr.getHostAddress());

                        if (!isIpAddress(canonicalName)) {
                            if (isIpAddress(host)) {
                                aliases.add (canonicalName);
                                int dotIndex = canonicalName.indexOf('.');
                                if (dotIndex > 0) {
                                    aliases.add (canonicalName.substring(0, dotIndex));
                                }
                            } else {
                                if (!canonicalName.toLowerCase().startsWith(host.toLowerCase())){
                                    // this may happen if the host name is really a
                                    // domain name and the canonical name points to
                                    // the domain server
                                    int dotIndex = canonicalName.indexOf('.');
                                    if (dotIndex > 0) {
                                        String secondaryName = canonicalName.substring(dotIndex + 1);
                                        dotIndex = secondaryName.indexOf('.');
                                        if (dotIndex > 0) {
                                            aliases.add(secondaryName.substring(0, dotIndex));
                                        }
                                        aliases.add(secondaryName);
                                    }
                                }
                                int dotIndex = canonicalName.indexOf('.');
                                if (dotIndex > 0) {
                                    aliases.add(canonicalName.substring(0, dotIndex));
                                }
                                aliases.add(canonicalName);
                            }
                        }
                        if (aliasCache != null) {
                            Element elem = new Element(host, new SerializableStringSet(aliases));
                            aliasCache.put(elem);
                        }
                    } catch (UnknownHostException e) {
                    	//Unknown host. Just ignore and quit without any alternate filenames
                    	log.info("UnknownHostException for host: " + host, e);
                    } catch (IOException e) {
                    	// Something in IO went wrong.  Ignore and quit without any alternate filenames
                    	log.warn("IOException when looking up host: " + host, e);
                    }
                }
                if (aliases != null) {
                    for (String alias : aliases) {
                        String name = scheme + alias + remainder;

                        if (!ret.contains(name)) {
                            ret.add(name);
                        }
                    }
                }
            }               
        }
        
        return ret;
    }
    
    /**
     * @param host
     * @return true if host has an IP address format. 
     */
    public static boolean isIpAddress (final String host)
    {
        return ipAddressPattern.matcher(host).matches();
    }

    /**
     * @return the current, active list of mounted hosts
     */
    private static List<String> reloadNetworkShareInformation(Object processHandle) {
        if (osWrapper == null) {
            osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
        }

        String[] resources = osWrapper.getNetworkDiskResources(processHandle);
        
        Set<String> uniqueResources = new HashSet<String>();

        StringBuilder sb = new StringBuilder("Read network resources:\n");

        for (String res : resources) {
            res = res.toLowerCase();

            sb.append(res+"\n");

            if (!res.startsWith("\\\\")) {
                // really??
                continue;
            }

            // First \ after the initial ones
            int i = res.indexOf("\\", 2);

            if (i != -1) {
                uniqueResources.add(res.substring(0, i+1).toLowerCase());
            } else {
                uniqueResources.add(res.toLowerCase() + "\\");
            }
        }

        log.info(sb.toString());

        return new ArrayList<String>(uniqueResources);
    }

    /**
     * Set the cache up to date
     */
    private static void refreshNetworkShareCache(Object processHandle) {
        networkSharesTimeStamp = System.currentTimeMillis();
        networkShares = reloadNetworkShareInformation(processHandle);
    }

    /**
     * Get the cached information about network shares.  Will update
     * if the information isn't recent enough
     */
    private static List<String> getNetworkShareInformation(Object processHandle) {
        if (System.currentTimeMillis() - networkSharesLiveTime > networkSharesTimeStamp) {
            refreshNetworkShareCache(processHandle);
        }

        return networkShares;
    }

    private static List<String> filterNames(List<String> resNames, Object processHandle) {
        List<String> currentNetworkShares = getNetworkShareInformation(processHandle);

        List<String> results = new ArrayList<String>();
        for (String res: resNames) {
            if (res.startsWith("\\\\")) {
                // We have a UNC name.  Does it start with any of the known mounted name?
                for (String networkShare : currentNetworkShares) {
                    if (res.startsWith(networkShare)) {
                        results.add(res);
                        continue;
                    }
                }

                // Is the current host?
                for (String hostName : localHostUNC) {
                    if (res.startsWith(hostName)) {
                        results.add(res);
                        continue;
                    }
                }
            } else {
                results.add(res);
            }
        }

        return results;
    }

    /**
     *
     */
    public static synchronized List<String> pruneResourceNames(List<String> resNames, Object processToken) {

        List<String> filteredNames = filterNames(resNames, processToken);

        if (filteredNames.size() == 0) {
            // Try again
            refreshNetworkShareCache(processToken);
            filteredNames = filterNames(resNames, processToken);
        }

        if (filteredNames.size() == 0) {
            filteredNames = resNames;
        }

        return filteredNames;
    }
    
    // time to live for entries in Alias Cache
    private static final long TTL_ALIAS_CACHE = 60 * 60 * 12; // 12 hours
    
    // Initialize Alias Cache, invoked by static initializer
    private static Cache initializeAliasCache()
    {
    	// setup EHCache
        CacheManager cacheManager;
        try {
        	cacheManager = CacheManager.create();
        } catch (CacheException e) {
        	log.warn("Unable to create Aliases Cache, proceeding in uncached mode.", e);
        	// no caching
        	return null;
        }
        Cache cache = cacheManager.getCache(UNCUtil.class.getName());
        if (cache == null) {
        	// create EHCache to store aliases
        	// cache max size 2,000 entries
        	// cache will stay in memory; will not be stored on disk
        	// elements aren't eternal
        	// entry lives for 12 hours after being created
        	// accessing the entry does not change the lifetime of the entry
        	cache = new Cache(UNCUtil.class.getName(), 2000, false, false, TTL_ALIAS_CACHE, 0); 
            try {
            	cacheManager.addCache(cache);
            } catch (CacheException e) {
            	log.warn("unable to add Alias Cache to EHCache, proceeding in uncached mode.", e);
            	// no caching
            	return null;
            }
        }
        
        return cache;
    }

    // private class to make Set<String> serializable
    static private class SerializableStringSet implements Serializable {
    	private Set<String> set = null;

    	SerializableStringSet(Set<String> inSet)
    	{
    	    set = inSet;
    	}

    	public Set<String> getSet()
    	{
    	    return set;
    	}
    	
    	private void writeObject(java.io.ObjectOutputStream out)
    	    throws IOException
    	{
    	    out.writeInt(set.size());
    	    for (String s : set) {
    		out.writeObject(s);
    	    }
    	}
    	
    	private void readObject(java.io.ObjectInputStream in)
    	    throws IOException, ClassNotFoundException
    	{
    	    int size = in.readInt();
    	    set = new HashSet<String>();
    	    for (int i=0; i<size; i++) {
    		String str = (String)in.readObject();
    		set.add(str);
    	    }
    	}
    }

    
}
