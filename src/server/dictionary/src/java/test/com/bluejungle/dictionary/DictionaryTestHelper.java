/*
 * Created on Mar 6, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.TimeRelation;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/DictionaryTestHelper.java#1 $
 */

public class DictionaryTestHelper {
	/**
	 * sorted by the dependence, doesn't change the order.
	 */
	private static final List<String> DICTIONARY_TABLES = new LinkedList<String>();
	static{
		DICTIONARY_TABLES.add("DICT_ENUM_REF_MEMBERS");
		DICTIONARY_TABLES.add("DICT_ENUM_MEMBERS");
		DICTIONARY_TABLES.add("DICT_ENUM_GROUP_MEMBERS");
		DICTIONARY_TABLES.add("DICT_UPDATES");
		DICTIONARY_TABLES.add("DICT_ENROLLMENT_PROPERTIES");
		DICTIONARY_TABLES.add("DICT_ENUM_GROUPS");
		DICTIONARY_TABLES.add("DICT_STRUCT_GROUPS");
		DICTIONARY_TABLES.add("DICT_LEAF_ELEMENTS");
		DICTIONARY_TABLES.add("DICT_ELEMENTS");
		DICTIONARY_TABLES.add("DICT_FIELD_MAPPINGS");
		DICTIONARY_TABLES.add("DICT_ENROLLMENTS");
	}

	public static void clearDictionary(IDictionary dictionary) throws SQLException,
			DictionaryException, HibernateException {
	    IConfigurationSession session = dictionary.createSession();
	    try {
	        Connection conn = ((DictionarySession)session).getSession().connection();
	        
	        final List<String> statements;
	       
	        if(conn instanceof oracle.jdbc.OracleConnection){
	        	String[][] foreignKeys = new String[][]{
	        			{ "DICT_UPDATES", 				"FKBF7601213A390036" },
	        			{ "DICT_ENROLLMENT_PROPERTIES", "FK143EAD853A390036" },
	        			{ "DICT_ENUM_GROUPS", 			"FKEDFF4BC93F3E855E" },
	        			{ "DICT_STRUCT_GROUPS", 		"FK391FE0B53F3E855E" },
	        			{ "DICT_LEAF_ELEMENTS", 		"FK3D23D3EFF7F2E440" },
	        			{ "DICT_LEAF_ELEMENTS", 		"FK3D23D3EF3F3E855E" },
	        			{ "DICT_ELEMENTS", 				"FKDFF5AB60F3F79036" },
	        			{ "DICT_FIELD_MAPPINGS", 		"FK578696D32262335F" },
	        			{ "DICT_FIELD_MAPPINGS", 		"FK578696D35CEA0FA" },
	        			{ "DICT_FIELD_MAPPINGS", 		"FK578696D33A390036" },
	        			{ "DICT_ELEMENTS",				"FKDFF5AB60F3F79036" },
	        	};
	        	
	        	statements = new LinkedList<String>();
	        	
				for (String[] foreignKey : foreignKeys) {
					statements.add(String.format("ALTER TABLE %s DISABLE CONSTRAINT %s", foreignKey[0],
							foreignKey[1]));
				}
				
				for (String table : DICTIONARY_TABLES) {
	        		statements.add(String.format("truncate table %s", table));
				}
				
				for (String[] foreignKey : foreignKeys) {
					statements.add(String.format("ALTER TABLE %s ENABLE CONSTRAINT %s", foreignKey[0],
							foreignKey[1]));
				}
	        }else{
	        	statements = new LinkedList<String>();
	        	for (String table : DICTIONARY_TABLES) {
	        		statements.add(String.format("delete from %s", table));
				}
	        }
	        
	        for (String s : statements) {
				conn.createStatement().executeUpdate(s);
				conn.commit();
			}
	    } finally {
	        session.close();
	    }
	}

	public enum DictioanryTable{
		DICT_ELEMENTS,
		DICT_ENUM_GROUP_MEMBERS,
		DICT_ENUM_GROUPS,
		DICT_ENUM_MEMBERS,
		DICT_ENUM_REF_MEMBERS,
		DICT_LEAF_ELEMENTS,
		DICT_STRUCT_GROUPS,
	}
	
	private static void quickTest(Session session) throws HibernateException{
	    List resuilt = session.createQuery(
                "from EnumerationProvisionalMember pm where "
            +   "    pm.groupId = :groupId "
            +   "and pm.path.pathHash in (:pathHash)")
            .setParameter("groupId", 234L)
            .setParameterList("pathHash", Collections.singleton(123L))
            .list();
	    
	    System.out.println(resuilt);
	}
	
	public static Map<DictioanryTable, Integer> queryDictionaryRowCounts(IDictionary dictionary)
			throws HibernateException, SQLException, DictionaryException {
		IConfigurationSession configSession = dictionary.createSession();
		try {
		    Session session = ((DictionarySession) configSession).getSession();
//		    quickTest(session);
		    
		    Connection conn = session.connection();
			
			List<String> countQueries = new ArrayList<String>();
			for( DictioanryTable dt : DictioanryTable.values()){
				countQueries.add(String.format("(select count(*) as %1$s from %1$s) %1$s", dt.name()));
			}
			
			String query = "select * from " + CollectionUtils.asString(countQueries, ", ");
			
			
			ResultSet resultSet = conn.createStatement().executeQuery( query);
					
			if (resultSet.next()) {
				Map<DictioanryTable, Integer> result = new LinkedHashMap<DictioanryTable,Integer>();
				
				int i = 1;
				for( DictioanryTable dt : DictioanryTable.values()){
					result.put(dt, resultSet.getInt(i++));
				}
				
				return result;
			}
			throw new SQLException("no result returned");
		} finally {
		    configSession.close();
		}
	}
	
	public static TimeRelation extractTimeRelation(Object e){
		if(e instanceof DictionaryElementBase){
			return ((DictionaryElementBase)e).getTimeRelation();
		}else{
			throw new IllegalArgumentException(e.getClass()
					+ " is not an instance of DictionaryElementBase");
		}
	}
}
