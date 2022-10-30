/*
 * Created on Dec 6, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.profiling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.tools.dbinit.profiling.DatabaseProfiling.SizeSet;
import com.bluejungle.framework.utils.IPair;
import com.nextlabs.shared.tools.StringFormatter;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/profiling/ProfileResult.java#1 $
 */

//TODO
/*
 * save seperator
 * save what is small/large length value and decimal
 */
class ProfileResult extends Properties{
	private static final long serialVersionUID = -648426615568093305L;
	
	private static final String SEPERATOR = ",";

	
	Collection<IPair<String, Integer>> unmappedTypes;
	Collection<IPair<String, Integer>> mappedTypes;
	
	private static String asString(Collection<IPair<String,Integer>> strs) {
		StringBuilder output = new StringBuilder();
		for (IPair<String,Integer> str : strs) {
			output.append( str.second());
			output.append(SEPERATOR);
		}
		return output.toString();
	}
	
	private Map<Integer, List<TypeProfile>> typeProfileResults = new HashMap<Integer, List<TypeProfile>>();
	
	class TypeProfile{
		SizeSet sizeSet;
		int returnedTypeCode;
		int length;
		int decimal;
	}
	

	void put(int typeCode, ProfileKey key, int value, SizeSet sizeSet) {
		List<TypeProfile> typeProfiles = typeProfileResults.get(typeCode);
		if (typeProfiles == null) {
			typeProfiles = new ArrayList<TypeProfile>();
			typeProfileResults.put(typeCode, typeProfiles);
		}
		
		TypeProfile typeProfile = null;
		for(TypeProfile tp : typeProfiles){
			if(tp.sizeSet == sizeSet){
				typeProfile = tp;
			}
		}
		
		if(typeProfile == null){
			typeProfile = new TypeProfile();
			typeProfile.sizeSet = sizeSet;
			typeProfiles.add(typeProfile);
		}
		
		switch (key) {
		case LENGTH:
			typeProfile.length = value;
			break;
		case DECIMAL:
			typeProfile.decimal = value;
			break;
		case RETURNED_TYPE_CODE:
			typeProfile.returnedTypeCode = value;
			break;
		default:
			//ignore
			//throw new IllegalArgumentException(key.name());
			break;
		}
	}
	
	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MAPPED_TYPE").append(asString(mappedTypes)).append(ConsoleDisplayHelper.NEWLINE);
		sb.append("UNMAPPED_TYPE").append(asString(unmappedTypes)).append(ConsoleDisplayHelper.NEWLINE);

		sb.append("Types detail").append(ConsoleDisplayHelper.NEWLINE);
		for (int code : typeProfileResults.keySet()) {
			sb.append("  ").append(code);
			List<TypeProfile> typeProfiles = typeProfileResults.get(code);
			if (typeProfiles.size() > 0) {
				boolean allTypeHasSameReturnCode = true;
				int currentReturnCode = typeProfiles.get(0).returnedTypeCode;
//				check if all types has a same code
				for (TypeProfile typeProfile : typeProfiles) {
					if (typeProfile.returnedTypeCode != currentReturnCode) {
						allTypeHasSameReturnCode = false;
						break;
					}
				}
				
				if (allTypeHasSameReturnCode) {
					sb.append(", RETURNED CODE:")
						.append(typeProfiles.get(0).returnedTypeCode)
						.append(ConsoleDisplayHelper.NEWLINE);
				} else {
					for (TypeProfile typeProfile : typeProfiles) {
						sb.append(", " + typeProfile.sizeSet + " RETURNED CODE:")
							.append(typeProfile.returnedTypeCode)
							.append(ConsoleDisplayHelper.NEWLINE);
					}
				}
				
				for (TypeProfile typeProfile : typeProfiles) {
					sb.append("    " + typeProfile.sizeSet + " ").append(typeProfile.length);
					sb.append(", ").append(typeProfile.decimal).append(ConsoleDisplayHelper.NEWLINE);
				}
			}
		}
		
		
		for(Object o : this.keySet()){
			sb.append(o).append(" , ").append(this.get(o)).append(ConsoleDisplayHelper.NEWLINE);
		}
		return sb.toString();
	}
	
	public String generateReport(Dialect d) throws HibernateException {
		final String mappedTypesHeaderFormat 	= "  code \t %12s \t %12s%n";
		final String mappedTypesFormat 			= "  %d \t %12s \t %12s%n";
		
		final String unmappedTypesHeaderFormat 	= "  code \t %12s%n";
		final String unmappedTypesFormat 		= "  %d \t %12s%n";
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("-- SUMMARY -- ").append(ConsoleDisplayHelper.NEWLINE);
		
		sb.append("MAPPED TYPES").append(ConsoleDisplayHelper.NEWLINE);
		
		
		sb.append(String.format(mappedTypesHeaderFormat, "SQL Type", "DB Type"));
		for(IPair<String, Integer> mappedType : mappedTypes){
			int code = mappedType.second();
			sb.append(String.format(mappedTypesFormat, code, mappedType.first(),d.getTypeName(code)));
		}
		
		sb.append(ConsoleDisplayHelper.NEWLINE);
		sb.append("UNMAPPED TYPES").append(ConsoleDisplayHelper.NEWLINE);
		
		
		sb.append(String.format(unmappedTypesHeaderFormat, "SQL Type"));
		for(IPair<String, Integer> mappedType : unmappedTypes){
			sb.append(String.format(unmappedTypesFormat, mappedType.second(), mappedType.first()));
		}
		
		sb.append(ConsoleDisplayHelper.NEWLINE);
		sb.append("-- DETAIL -- ").append(ConsoleDisplayHelper.NEWLINE);
		
		Set<Integer> hasLengthTypes = new HashSet<Integer>();
		Set<Integer> hasDecimalTypes = new HashSet<Integer>();
		
		for (IPair<String, Integer> mappedType : mappedTypes) {
			int code = mappedType.second();
			
			sb.append(String.format(mappedTypesFormat, code, mappedType.first(), d.getTypeName(code)));
			
			List<TypeProfile> typeProfiles = typeProfileResults.get(code);
			if (typeProfiles != null && typeProfiles.size() > 0) {
				boolean allTypeHasSameReturnCode = true;
				int currentReturnCode = typeProfiles.get(0).returnedTypeCode;
//				check if all types has a same code
				for (TypeProfile typeProfile : typeProfiles) {
					if (typeProfile.returnedTypeCode != currentReturnCode) {
						allTypeHasSameReturnCode = false;
						break;
					}
				}
				
				if (allTypeHasSameReturnCode) {
					int returnCode = typeProfiles.get(0).returnedTypeCode;
					if(code != returnCode){
						sb.append(getReturnCodeLine("  RETURNED CODE", returnCode, d));
					}
				} else {
					for (TypeProfile typeProfile : typeProfiles) {
						sb.append(getReturnCodeLine(", " + typeProfile.sizeSet + " RETURNED CODE",
								typeProfile.returnedTypeCode, d));
					}
				}
				
				for (TypeProfile typeProfile : typeProfiles) {
					sb.append("    " + typeProfile.sizeSet + " ").append(typeProfile.length);
					sb.append(", ").append(typeProfile.decimal).append(ConsoleDisplayHelper.NEWLINE);
				}
				
				boolean allMatchColumnSize = true;
				boolean someMatchColumnSize = false;
				boolean allMatchDecimal = true;
				boolean someMatchDecimal = false;
				
				for (TypeProfile typeProfile : typeProfiles) {
					if(typeProfile.length != typeProfile.sizeSet.getColumnSize()){
						allMatchColumnSize = false;
					}else{
						someMatchColumnSize = true;
					}
					
					if(typeProfile.decimal != typeProfile.sizeSet.getDecimalDigits()){
						allMatchDecimal = false;
					}else{
						allMatchDecimal = true;
					}
				}
				
				
				if (allMatchColumnSize) {
					hasLengthTypes.add(code);
					sb.append("    hasLength").append(ConsoleDisplayHelper.NEWLINE);
				}else if(someMatchColumnSize){
					System.err.println("only some set matched the column size, why?");
					//TODO log warning here
				}
				
				if (allMatchDecimal) {
					hasDecimalTypes.add(code);
					sb.append("    hasDecimal").append(ConsoleDisplayHelper.NEWLINE);
				}else if(someMatchDecimal){
					System.err.println("only some set matched the decimal digital, why?");
					//TODO log warning here
				}
			}
			
			sb.append(StringFormatter.repeat('-', 60)).append(ConsoleDisplayHelper.NEWLINE);
		}
		
		TreeSet<Object> keys = new TreeSet<Object>();
		keys.addAll(this.keySet());
		for(Object o : keys){
			sb.append(o).append(" , ").append(this.get(o)).append(ConsoleDisplayHelper.NEWLINE);
		}
		return sb.toString();
	}
	
	private String getReturnCodeLine(String title, int returnedCode, Dialect dialect)
			throws HibernateException {
		return isMapped(returnedCode) 
			? String.format("%s: %d %s%n", title, returnedCode,
				getSQLName(returnedCode), dialect.getTypeName(returnedCode)) 
			: String.format("%s: %d %n" , title, returnedCode, getSQLName(returnedCode));
	}
	
	private boolean isMapped(int code){
		for (IPair<String, Integer> mappedType : mappedTypes) {
			if(mappedType.second().equals(code)){
				return true;
			}
		}
		return false;
	}
	
	private String getSQLName(int code){
		for (IPair<String, Integer> type : unmappedTypes) {
			if (type.second().equals(code)) {
				return type.first();
			}
		}

		for (IPair<String, Integer> type : mappedTypes) {
			if (type.second().equals(code)) {
				return type.first();
			}
		}
		
		throw new IllegalArgumentException("Unknown code :" + code);
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		return super.put(key.toString(), value.toString());
	}

	@Override
	public synchronized Object get(Object key) {
		return super.get(key.toString());
	}
}
