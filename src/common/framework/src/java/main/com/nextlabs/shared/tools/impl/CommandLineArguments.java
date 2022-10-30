/*
 * Created on Apr 24, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ParseException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/impl/CommandLineArguments.java#1 $
 */

public class CommandLineArguments implements Cloneable{
	private static final String ARGUMENT_KEY_PREFIX = "-";
	
	private HashMap<String, List<String>> parsedMap;
	
	public CommandLineArguments(String[] args) throws ParseException{
		
		parsedMap = new HashMap<String, List<String>>();
		
		List<String> values = new LinkedList<String>();
		String previousIndicator = null;
		for(String arg : args){
			if (arg.startsWith(ARGUMENT_KEY_PREFIX)) {
				// i am a key
				if (previousIndicator != null) {
					saveOrUpdate(previousIndicator, values);
				} else {
					if (!values.isEmpty()) {
						throw new ParseException("unknown args: " + CollectionUtils.asString(values, " "));
					}
				}
				
				previousIndicator = arg.substring(ARGUMENT_KEY_PREFIX.length());
				values = new LinkedList<String>();
				
			}else{
				// i am a value
				values.add(arg);
			}
		}
		if (previousIndicator != null) {
			saveOrUpdate(previousIndicator, values);
		}
		
//		arguments = Arrays.asList(args);
	}
	
	private CommandLineArguments(HashMap<String, List<String>> parsedMap) {
		this.parsedMap = parsedMap;
	}
	
	private void saveOrUpdate(String indicator, List<String> values){
		List<String> savedValues = parsedMap.get(indicator);
		if (savedValues != null) {
			savedValues.addAll(values);
		} else {
			parsedMap.put(indicator, values);
		}
	}
	
	boolean containsIndicator(String key){
		return parsedMap.containsKey(key);
	}
	
	String[] getValues(String key){
		List<String> values = parsedMap.get(key);
		return values == null ? null : values.toArray(new String[values.size()]);
	}
	
	String[] popValues(String key){
		List<String> values = parsedMap.remove(key);
		return values == null ? null : values.toArray(new String[values.size()]);
	}
	
	Set<String> getAllIndicators(){
		return parsedMap.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object clone(){
		return new CommandLineArguments((HashMap<String, List<String>>)parsedMap.clone());
	}
}
