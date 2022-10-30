/*
 * Created on Mar 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.DistinguishedName;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.sun.jndi.ldap.LdapCtxFactory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/ActiveDirectoryTestHelper.java#1 $
 */

public class ActiveDirectoryTestHelper {
    private static final Log LOG = LogFactory.getLog(ActiveDirectoryTestHelper.class);
    
	//the following are default values
	private static final String AD_HOSTNAME = "linuxad01.linuxtest.bluejungle.com";
	private static final int AD_PORT 		= 389;
	protected static final String BASE_DN 	= "dc=linuxtest,dc=bluejungle,dc=com";

	public static final String USERNAME 	= "Administrator@linuxtest.bluejungle.com";
	public static final String PASSWORD 	= "123blue!";

	protected static final String OU_NAME;
    static{
        String name;
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            name = System.getProperty("user.name", "default");
        }
        OU_NAME = name;
    }
    
    protected static String generateRootDn(String ouName){
        return "OU=" + ouName + ",OU=" + ActiveDirectoryTestHelper.OU_NAME + ","
                + ActiveDirectoryTestHelper.BASE_DN;
    }
	
	
	private final String host;
	private final int port;
	private final String root;
	
	//need some workaround for sun jndi too, because it uses space as delimiter (see LdapURL), 
	// when the root has space, it will be a trouble
	private final String rootWithoutSpace;
	private final String username;
	private final String password;
	
	public ActiveDirectoryTestHelper(String root){
		this(AD_HOSTNAME, AD_PORT, root, BASE_DN, USERNAME, PASSWORD);
	}
	
	public ActiveDirectoryTestHelper(String host, int port, String root, String bassDNwithoutSpace,
			String username, String password) {
		this.host = host;
		this.port = port;
		this.root = root.toLowerCase();
		if (bassDNwithoutSpace.contains(" ")) {
			throw new IllegalArgumentException("doesn't allow a space in \"bassDNwithoutSpace\"");
		}
		rootWithoutSpace = bassDNwithoutSpace.toLowerCase();
		this.username = username;
		this.password = password;
	}
	
	public String getRootDn(){
		return root;
	}
	
	public String getUsersRootDn(){
		return "OU=Users," + root;
	}
	
	public String getGroupsRootDn(){
		return "OU=Groups," + root;
	}
	
	public String getHostsRootDn(){
		return "OU=Hosts," + root;
	}
	
	public String getOthersRootDn(){
		return "OU=Others," + root;
	}
	
	public String[] getAllOus(){

		//the order is important, create parent before children
		return new String[]{
				getRootDn(), 
				getUsersRootDn(),
				getGroupsRootDn(), 
				getHostsRootDn(),
				getOthersRootDn()
		};
	}
	
	public void add(LDAPEntry entry) throws LDAPException{
		LDAPConnection connection = getConnection();
		try {
            connection.add(entry);
        } catch (LDAPException e) {
            LOG.error("fail to add entry " + entry);
            throw e;
        }
	}
	
	public void delete(String dn) throws LDAPException{
		LDAPConnection connection = getConnection();
		try {
            connection.delete(dn);
        } catch (LDAPException e) {
            LOG.error("fail to delete " + dn);
            throw e;
        }
	}
	
	public void modify(String dn, LDAPModification... mods) throws LDAPException{
		LDAPConnection connection = getConnection();
		try {
		    connection.modify(dn, mods);
		} catch (LDAPException e) {
            LOG.error("fail to modify " + dn);
            throw e;
        }
	}
	
	public void move(String dn, String newdn) throws NamingException{
		// workaround for bug in novell ldap library
		// the bug is when move the entry with different parent, it still in the same parent
		DirContext dirContext = getDirContext();
		String oldName;
		String newName;
		if( !dn.toLowerCase().endsWith(rootWithoutSpace)){
			throw new NamingException(dn);
		}
		oldName = dn.substring(0, dn.length() - rootWithoutSpace.length() - 1);
		
		if( !newdn.toLowerCase().endsWith(rootWithoutSpace)){
			throw new NamingException(newdn);
		}
		newName = newdn.substring(0, newdn.length() - rootWithoutSpace.length() - 1);
		
		try {
            dirContext.rename(oldName, newName);
        } catch (NamingException e) {
            LOG.error("fail to move " + dn + " to " + newdn);
            throw e;
        }
	}
	
	public boolean isExist(String dn) throws LDAPException {
		LDAPConnection connection = getConnection();
		
		LDAPSearchResults rs = connection.search(
				dn, 
				LDAPConnection.SCOPE_BASE, 
				"",
				new String[] { "dn" }, 
				false);
		
		if(rs.hasMore()){
			try {
				rs.next();
				return true;
			} catch (LDAPException e) {
				if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
					return false;
				}else{
					throw e;
				}
			}
		}else{
			return false;
		}
	}
	
	public void removeAllUnitTestData() throws LDAPException {
		int c = removeAllUnitTestData(getUsersRootDn());
		System.out.println("removed " + c + " users");
		c = removeAllUnitTestData(getGroupsRootDn());
		System.out.println("removed " + c + " groups");
		c = removeAllUnitTestData(getHostsRootDn());
		System.out.println("removed " + c + " hosts");
		c = removeAllUnitTestData(getOthersRootDn());
		System.out.println("removed " + c + " others");
		
		c = removeAllUnitTestData(getRootDn());
		System.out.println("removed " + c + " under root");
		
		try {
			delete(getRootDn());
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
				//no such object
				//ignore
			} else {
				throw e;
			}
		}
	}
	
	public int removeAllUnitTestData(String root) throws LDAPException {
		int count = 0;
		LDAPConnection connection = getConnection();
		LDAPSearchResults rs;
		boolean hasMore = true;
		while (hasMore) {
			rs = connection.search(
					root, 
					LDAPConnection.SCOPE_ONE, 
					"",
					new String[] { "dn" }, 
					false);

			try {
				while (rs.hasMore()) {
				    count += removeAllUnitTestData(rs.next().getDN());
				}
				count++;
				connection.delete(root);
				hasMore = false;
			} catch (LDAPException e) {
				if (e.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED) {
					// more than I can get at each call
					hasMore = true;
				} else if (e.getResultCode() == LDAPException.NO_SUCH_OBJECT) {
					//no such object
					hasMore = false;
					break;
				} else {
					throw e;
				}
			}
		}
		
		return count;
	}
	
	public void createOu(String dn) throws LDAPException{
		connection = getConnection();
		LDAPAttributeSet attrs= new LDAPAttributeSet();
		attrs.add(new LDAPAttribute("objectClass", "organizationalUnit"));
		attrs.add(new LDAPAttribute("ou", LDAPEnrollmentHelper.getNameFromDN(dn, "")));
		LDAPEntry ou = new LDAPEntry(dn, attrs);
        connection.add(ou);
	}
	
	public void createOuRecursively(String dn) throws LDAPException{
	    LinkedList<String> rootDn = DistinguishedName.splitPathToList(dn);
	    StringBuilder sb = new StringBuilder();

        while (rootDn.size() > 0) {
            if(sb.length() != 0){
                sb.insert(0, ',');
            }
            sb.insert(0, rootDn.removeLast());

            String tempDn = sb.toString();
            if (tempDn.toLowerCase().startsWith("ou")) {
                if (!isExist(tempDn)) {
                    createOu(tempDn);
                }
            }
        }
    }
	
	public void createOus() throws LDAPException {
	    createOuRecursively(root);
		
		for (String ou : getAllOus()) {
		    createOuRecursively(ou);
		}
	}

	protected LDAPConnection connection = null;
	protected DirContext dirContext = null;

	protected DirContext getDirContext() throws NamingException {
		if (dirContext == null) {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, LdapCtxFactory.class.getName());
			env.put(Context.PROVIDER_URL, "ldap://" + host + ":"
					+ port + "/" + rootWithoutSpace);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, username);
			env.put(Context.SECURITY_CREDENTIALS, password);

			dirContext = new InitialDirContext(env);
		}
		return dirContext;
	}
	
	protected LDAPConnection getConnection() throws LDAPException {
		if(connection == null){
			connection = new LDAPConnection();
		}
		
		if (!connection.isConnected()) {
			// Create a connection:
			byte[] passwd = null;
			passwd = password.getBytes();

			connection.connect(host, port);
			connection.bind(LDAPConnection.LDAP_V3, username, passwd);
			
			
			// Enable referral handling:
			LDAPSearchConstraints searchConstraints = connection.getSearchConstraints();
			searchConstraints.setReferralFollowing(true);

//			searchConstraints.setReferralHandler(new LDAPBindHandlerImpl(username, password));
			searchConstraints.setMaxResults(0);
			searchConstraints.setTimeLimit(300000);
			searchConstraints.setServerTimeLimit(30000000);
			connection.setConstraints(searchConstraints);
		}
		return connection;
	}
	
	public void disconnect() throws LDAPException, NamingException {
		if (connection != null && connection.isConnected()) {
			connection.disconnect();
		}
		
		if(dirContext != null ){
			dirContext.close();
			dirContext = null;
		}
	}
}
