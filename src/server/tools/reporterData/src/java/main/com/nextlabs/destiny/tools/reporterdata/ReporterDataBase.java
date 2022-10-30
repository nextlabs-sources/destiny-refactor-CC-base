/*
 * Created on Dec 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.nextlabs.random.Dictionary;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataBase.java#1 $
 */

public abstract class ReporterDataBase {
	protected static final String DICTIOINARY_FILENAME = "/com/nextlabs/destiny/tools/reporterdata/dictionary.dic";
	
	protected final Dictionary dictionary;
	protected final boolean isInteractive;
	
	protected static final Random r = new Random();
	
	public ReporterDataBase(boolean loadEnglishDictionary, boolean isInteractive) throws IOException{
	    dictionary = loadEnglishDictionary ? new Dictionary(new File("dictionary.dic")) : null;
		this.isInteractive = isInteractive;
	}
	
	public ReporterDataBase(boolean loadEnglishDictionary) throws IOException{
		this(loadEnglishDictionary, true);
	}
	
	/**
     * the result set may be less than request number, be careful
     * @param s
     * @param numberOfUsers
     * @param clazz only HostDO and UserDO now
     * @param isInteractive
     * @param isHacker	hacker will just create the data without even ask a single question
     * @return
     * @throws HibernateException
     * @throws IOException 
     * @throws UserAbortException 
     */
	protected <T> List<T> getSpecificSizeOfObjects(Session s, int numberOfObject,
			Class<T> clazz, boolean isInteractive, boolean isHacker) throws HibernateException,
			IOException, UserAbortException {
		List<T> objs;
		objs = getObjects(s, numberOfObject, clazz);
        if(objs.size() < numberOfObject ){
        	if(isHacker){
        		createObject(s, numberOfObject - objs.size(), clazz);
        		objs = getObjects(s, numberOfObject, clazz);
        	} else { 
        		final String className = clazz.getSimpleName();
	        	String message = String.format("There is only %d %s avaliable, but you request %d.", 
	        			objs.size(), className, numberOfObject);
	        	if (!isInteractive) {
					throw new UserAbortException(message);
				}
	        	String response = InteractiveQuestion
						.prompt(message + " Do you want to continue? (y/n) ");
	        	if ( ! StringUtils.stringToBoolean(response, false) ){
	        		throw new UserAbortException(message);
	        	}
	        	
	        	final int maxTry = 5;
	        	message = String.format(
	        			"Do you want to create %d new %s (C) or lower the number (L) to continue? (C/L) ",
	        			numberOfObject - objs.size(), className); 
	        	int i;
	        	for (i = 0; i < maxTry; i++) {
					response = InteractiveQuestion.prompt(message);
					response = response.trim();
					
					if (response.equalsIgnoreCase("C")) {
						//create extra object
						message = String.format( "WARNING, you should do enrollment to create " +
								"a new %s! What I am going to do is a hack. No warranty, UAYOR. " +
								"Do you agree? (y/n) ",	className);
						response = InteractiveQuestion.prompt(message);
						if ( ! StringUtils.stringToBoolean(response, false) ){
			        		throw new UserAbortException(message);
			        	}
						
						//user agree my term,
						createObject(s, numberOfObject - objs.size(), clazz);
		        		objs = getObjects(s, numberOfObject, clazz);
						break;
					} else if (response.equalsIgnoreCase("L")) {
						//do nothing, the number is already lowered
						break;
					} else if (i >= maxTry) {
						message = "Wrong input! (C/L) ";
					}
				}
	        	if (i >= maxTry) {
					throw new UserAbortException("You have tried too many times. Abort!");
				}
        	}
        }
        
        return objs;
    }

	private static <T> List<T> getObjects(Session s, int maxResults, Class<T> clazz)
			throws HibernateException {
		List<T> objs;
		Criteria crit = s.createCriteria(clazz);
        crit.setMaxResults(maxResults);
        objs = crit.list();
		return objs;
	}
	
	protected <T> void createObject(Session s, int number, Class<T> clazz)
			throws HibernateException, UnsupportedOperationException {
		if(clazz == HostDO.class){
			createHosts(number, s);
		} else if (clazz == UserDO.class) {
			createUsers(number, s);
		} else if (clazz == PolicyDO.class){
			createPolicies(number ,s);
		}else{
			throw new UnsupportedOperationException("I don't know how to create " +clazz);
		}
	}
	
   
    
    protected void createHosts(int number, Session s) throws HibernateException{
    	Long maxId = (Long) s.createQuery("select max(h.originalId) from HostDO h").uniqueResult();
		if (maxId == null) {
			maxId = 1L;
		}
    	
    	for (int i = 0; i < number; i++) {
    		HostDO host = new HostDO();
    		host.setOriginalId(++maxId);
    		host.setName(getRandomHostname(maxId));
    		host.setTimeRelation(new TimeRelation(UnmodifiableDate.START_OF_TIME,
					UnmodifiableDate.END_OF_TIME));
    		s.save(host);
    	}
    	s.flush();
    }
    
    protected String getRandomHostname(long id){
    	return String.format("host%05d.demo.nextlabs.com", id);
    }
    
    protected void createUsers(int number, Session s) throws HibernateException{
    	Long maxId = (Long) s.createQuery("select max(u.originalId) from UserDO u").uniqueResult();
		if (maxId == null) {
			maxId = 1L;
		}
    	
    	for (int i = 0; i < number; i++) {
    		UserDO user = new UserDO();
			user.setOriginalId(++maxId);
			user.setDisplayName(getRandomUserDisplayname(maxId));
			user.setFirstName(getRandomUserFirstname(maxId));
			user.setLastName(getRandomUserLastname(maxId));
			user.setSID(getRandomUserSidname(maxId));
			user.setTimeRelation(new TimeRelation(UnmodifiableDate.START_OF_TIME,
                    UnmodifiableDate.END_OF_TIME));
			s.save(user);
    	}
    	s.flush();
    }
    
    protected String getRandomUserDisplayname(long id){
    	return String.format("Display%05d", id);
    }
    
    protected String getRandomUserFirstname(long id){
    	return String.format("First%05d", id);
    }
    
    protected String getRandomUserLastname(long id){
    	return String.format("Last%05d", id);
    }
    
    protected String getRandomUserSidname(long id){
    	return String.format("S-1123-%05d", id);
    }
    
    protected void createPolicies(int number, Session s) throws HibernateException{
    	Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
		if (maxId == null) {
			maxId = 1L;
		}
    	
    	for (int i = 0; i < number; i++) {
    		createOrGetPolicies(getRandomPolicyFullName(i), s, false);
    	}
    }
    
    protected String getRandomPolicyFullName(long id){
    	return String.format("/Folder %s/Policy %s %d", dictionary.getRandomWord(),
    	        dictionary.getRandomWord(), id);
    }
    
    protected static String ipAddressTransform(int ipAddressInIntegerForm){
		StringBuilder sb = new StringBuilder();
        sb.append((ipAddressInIntegerForm & 0xFF000000) >>> 24).append(".");
        sb.append((ipAddressInIntegerForm & 0x00FF0000) >>> 16).append(".");
        sb.append((ipAddressInIntegerForm & 0x0000FF00) >>> 8).append(".");
        sb.append((ipAddressInIntegerForm & 0x000000FF));
        
        return sb.toString();
	}
    
    
    protected Map<String, UserDO> userDisplayNameToDOMap = new HashMap<String, UserDO>();
	protected Map<Long, UserDO> userOriginalIdToDOMap = new HashMap<Long, UserDO>();
	private UserDO getUser(String userDisplayName, long userId, Session s, boolean useCache)
			throws HibernateException, NotFoundException {
		if (useCache) {
			UserDO exsitingId = userDisplayName != null 
					? userDisplayNameToDOMap.get(userDisplayName) 
					: userOriginalIdToDOMap.get(userId);
			
			if(exsitingId != null){
				return exsitingId;
			}
		}
		
		UserDO user;
		if(userDisplayName != null){
			user = (UserDO) s.createCriteria(UserDO.class).add(
				Expression.ilike("displayName", userDisplayName)).uniqueResult();
			if( user == null){
				throw new NotFoundException("User", "displayName", userDisplayName);
			}
		}else{
			user = (UserDO) s.createCriteria(UserDO.class).add(
					Expression.eq("originalId", userId)).uniqueResult();
			if( user == null){
				throw new NotFoundException("User", "originalId", userId);
			}
		}
		
		if (useCache) {
			userDisplayNameToDOMap.put(user.getDisplayName(), user);
			userOriginalIdToDOMap.put(user.getOriginalId(), user);
		}
		return user;
	}
	
	protected IUser getUser(String userDisplayName, Session s, boolean useCache)
			throws HibernateException, NotFoundException {
		return getUser(userDisplayName, 0, s, useCache);
	}

	protected IUser getUser(String userDisplayName, Session s) throws HibernateException,
			NotFoundException {
		return getUser(userDisplayName, s, true);
	}
	
	protected IUser getUser(long userid, Session s, boolean useCache) throws HibernateException,
			NotFoundException {
		return getUser(null, userid, s, useCache);
	}
	
	protected IUser getUser(long userid, Session s) throws HibernateException, NotFoundException {
		return getUser(userid, s, true);
	}
	
	
	
	protected Map<String, HostDO> hostNameToDOMap = new HashMap<String, HostDO>();
	protected Map<Long, HostDO> hostOriginalIdToDOMap = new HashMap<Long, HostDO>();
	private HostDO getHost(String hostname, long hostId, Session s, boolean useCache)
			throws HibernateException, NotFoundException {
		if (useCache) {
			HostDO exsitingId = hostname != null 
					? hostNameToDOMap.get(hostname) 
					: hostOriginalIdToDOMap.get(hostId);
			
			if(exsitingId != null){
				return exsitingId;
			}
		}
		
		HostDO host;
		if(hostname != null){
			host = (HostDO) s.createCriteria(HostDO.class).add(
					Expression.ilike("name", hostname)).uniqueResult();
			if( host == null){
				throw new NotFoundException("Host", "name", hostname);
			}
		}else{
			host = (HostDO) s.createCriteria(HostDO.class).add(
					Expression.eq("originalId", hostId)).uniqueResult();
			if( host == null){
				throw new NotFoundException("User", "originalId", hostId);
			}
		}
		
		if (useCache) {
			hostNameToDOMap.put(host.getName(), host);
			hostOriginalIdToDOMap.put(host.getOriginalId(), host);
		}
		return host;
	}
	
	protected IHost getHost(String hostname, Session s, boolean useCache)
			throws HibernateException, NotFoundException {
		return getHost(hostname, 0, s, useCache);
	}

	protected IHost getHost(String hostname, Session s) throws HibernateException,
			NotFoundException {
		return getHost(hostname, s, true);
	}

	protected IHost getHost(long hostId, Session s, boolean useCache) throws HibernateException,
			NotFoundException {
		return getHost(null, hostId, s, useCache);
	}

	protected IHost getHost(long hostId, Session s) throws HibernateException, NotFoundException {
		return getHost(hostId, s, true);
	}
	
	
	protected Map<String, PolicyDO> policyNameToDOMap = new HashMap<String, PolicyDO>();
	protected Map<Long, PolicyDO> policyIdToDOMap = new HashMap<Long, PolicyDO>();
	
	protected IPolicy createOrGetPolicies(String policyFullname, Session s, boolean useCache) throws HibernateException {
		if (useCache) {
			PolicyDO exsitingId = policyNameToDOMap.get(policyFullname);
			if(exsitingId != null){
				return exsitingId;
			}
		}
		
		PolicyDO policy = (PolicyDO) s.createCriteria(PolicyDO.class).add(
				Expression.eq("fullName", policyFullname)).uniqueResult();
		//if policy does not exist, create one
		if (policy == null) {
			Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
			if (maxId == null) {
				maxId = 1L;
			}
			
			policy = new PolicyDO();

			policy.setFullName(policyFullname);
			policy.setId(++maxId);
			s.save(policy);
			s.flush();
		}
		if (useCache) {
			policyNameToDOMap.put(policy.getFullName(), policy);
			policyIdToDOMap.put(policy.getId(), policy);
		}
		return policy;
	}
	
	protected IPolicy createOrGetPolicies(String policyFullname, Session s) throws HibernateException {
		return createOrGetPolicies(policyFullname, s, true);
	}
	
	protected IPolicy getPolicy(long policyId, Session s, boolean useCache) throws HibernateException,
			NotFoundException {
		if (useCache) {
			PolicyDO exsitingId = policyIdToDOMap.get(policyId);
			if(exsitingId != null){
				return exsitingId;
			}
		}

		PolicyDO policy = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("id", policyId))
						.uniqueResult();
		if (policy == null) {
			throw new NotFoundException("policy", "id", policyId);
		}

		if (useCache) {
			policyNameToDOMap.put(policy.getFullName(), policy);
			policyIdToDOMap.put(policy.getId(), policy);
		}
		return policy;
	}
	
	protected IPolicy getPolicy(long policyId, Session s) throws HibernateException, NotFoundException {
		return getPolicy(policyId, s, true);
	}
	
	protected void clearCache(){
		userDisplayNameToDOMap.clear();
		userOriginalIdToDOMap.clear();
		hostNameToDOMap.clear();
		hostOriginalIdToDOMap.clear();
	}
}
