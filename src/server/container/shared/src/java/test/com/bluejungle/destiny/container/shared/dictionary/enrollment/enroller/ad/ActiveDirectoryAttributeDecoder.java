/*
 * Created on Apr 2, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.bluejungle.framework.utils.ArrayUtils;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/ActiveDirectoryAttributeDecoder.java#1 $
 */

public class ActiveDirectoryAttributeDecoder {
	
	private static final Map<String, ActiveDirectoryAttributeDecoder> ALL_DENCODER = 
		new HashMap<String, ActiveDirectoryAttributeDecoder>();
	
	static final ActiveDirectoryAttributeDecoder USER_ACCOUNT_CONTROL;
	
	static{
		// http://support.microsoft.com/kb/305144
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(1 << 0,  "SCRIPT");
		map.put(1 << 1,  "ACCOUNTDISABLE");
		map.put(1 << 3,  "HOMEDIR_REQUIRED");
		map.put(1 << 4,  "LOCKOUT");
		map.put(1 << 5,  "PASSWD_NOTREQD");
		map.put(1 << 6,  "PASSWD_CANT_CHANGE");
		map.put(1 << 7,  "ENCRYPTED_TEXT_PWD_ALLOWED");
		map.put(1 << 8,  "TEMP_DUPLICATE_ACCOUNT");
		map.put(1 << 9,  "NORMAL_ACCOUNT");
		map.put(1 << 11, "INTERDOMAIN_TRUST_ACCOUNT");
		map.put(1 << 12, "WORKSTATION_TRUST_ACCOUNT");
		map.put(1 << 13, "SERVER_TRUST_ACCOUNT");
		map.put(1 << 16, "DONT_EXPIRE_PASSWORD");
		map.put(1 << 17, "MNS_LOGON_ACCOUNT");
		map.put(1 << 18, "SMARTCARD_REQUIRED");
		map.put(1 << 19, "TRUSTED_FOR_DELEGATION");
		map.put(1 << 20, "NOT_DELEGATED");
		map.put(1 << 21, "USE_DES_KEY_ONLY");
		map.put(1 << 22, "DONT_REQ_PREAUTH");
		map.put(1 << 23, "PASSWORD_EXPIRED");
		map.put(1 << 24, "TRUSTED_TO_AUTH_FOR_DELEGATION");
		USER_ACCOUNT_CONTROL = new ActiveDirectoryAttributeDecoder("UserAccountControl", true, map);
	}
	
	static{
		// http://msdn.microsoft.com/en-us/library/ms677840.aspx
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(1 << 4,  "UF_LOCKOUT");
		map.put(1 << 23, "UF_PASSWORD_EXPIRED");
		map.put(1 << 26, "UF_PARTIAL_SECRETS_ACCOUNT");
		map.put(1 << 27, "UF_USE_AES_KEYS");
		new ActiveDirectoryAttributeDecoder("ms-DS-User-Account-Control-Computed", true, map);
	}
	
	static{
		// http://msdn.microsoft.com/en-us/library/ms676204(VS.85).aspx
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(1 << 0, "The head of naming context.");
		map.put(1 << 1, "This replica is not instantiated.");
		map.put(1 << 2, "The object is writable on this directory.");
		map.put(1 << 3, "The naming context above this one on this directory is held.");
		map.put(1 << 4, "The naming context is being constructed for the first time via replication.");
		map.put(1 << 5, "The naming context is being removed from the local directory system agent (DSA).");
		new ActiveDirectoryAttributeDecoder("instanceType", true, map);
	}
	
	static{
		// http://msdn.microsoft.com/en-us/library/cc220839(PROT.10).aspx
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(0x00000000, "SAM_DOMAIN_OBJECT");
		map.put(0x10000000, "SAM_GROUP_OBJECT");
		map.put(0x10000001, "SAM_NON_SECURITY_GROUP_OBJECT");
		map.put(0x20000000, "SAM_ALIAS_OBJECT");
		map.put(0x20000001, "SAM_NON_SECURITY_ALIAS_OBJECT");
		map.put(0x30000000, "SAM_USER_OBJECT");
		map.put(0x30000001, "SAM_MACHINE_ACCOUNT");
		map.put(0x30000002, "SAM_TRUST_ACCOUNT");
		map.put(0x40000000, "SAM_APP_BASIC_GROUP");
		map.put(0x40000001, "SAM_APP_QUERY_GROUP");
		map.put(0x7FFFFFFF, "SAM_ACCOUNT_TYPE_MAX");
		new ActiveDirectoryAttributeDecoder("sAMAccountType", false, map);
	}

	private final String name;
	private final boolean isBitArray;
	private final Map<Integer, String> map;	
	private ActiveDirectoryAttributeDecoder(String name, boolean isBitArray, Map<Integer, String> map){
		this.name = name;
		this.isBitArray = isBitArray;
		this.map = map;
		ALL_DENCODER.put(name, this);
	}
	
	public String[] decode(int userControl){
		List<String> output = new LinkedList<String>();
		if (isBitArray) {
			for (Map.Entry<Integer, String> entry : map.entrySet()) {
				if ((entry.getKey() & userControl) != 0) {
					output.add(entry.getValue());
				}
			}
		} else {
			output.add(map.get(userControl));
		}
		return output.toArray(new String[output.size()]);
	}
	
	public String toString(int userAccountControl){
		String[] codes = decode(userAccountControl);
		return ArrayUtils.asString(codes, ", ");
	}
	
	public ActiveDirectoryAttributeDecoder getEnDencoder(String name){
		return ALL_DENCODER.get(name.toLowerCase());
	}
	
}
