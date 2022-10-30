/*
 * Created on Dec 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.bluejungle.framework.utils.Pair;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.domain.log.PolicyActivityInfoV5;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataConfigBase.java#1 $
 */

class ReporterDataConfigBase {
	public enum PolicyType{
		POLICY,
		TRACKING,
	}
	
	protected static final Pair<Integer, Integer> NON_NEGATIVE_INTEGER_RANGE =
		new Pair<Integer, Integer>(0, Integer.MAX_VALUE);
	
	protected static final String DEFAULT_VALUE_SEPERATOR = ",";
	
	protected static final String NO_DEFAULT_VALUE = null;
	
	protected static String getString(Properties props, String key, String defaultValue)
			throws IllegalArgumentException {
		String value = props.getProperty(key, defaultValue);
		if (value == null) {
			throw new IllegalArgumentException("\"" + key + "\" is null.");
		}
		return value.trim();
	}
	
	protected static String getString(Properties props, String key) throws IllegalArgumentException {
		return getString(props, key, NO_DEFAULT_VALUE);
	}
	
	protected static String[] getStringArray(Properties props, String key, String seperator) {
		String value = getString(props, key);
		String[] values = value.split(seperator);
		return values;
	}
	
	protected static Map<String, String> getCustomAttributeMap(Properties props, String key, String typeSeperator, String lineSeperator) {
		Map<String, String> orderedMap = new LinkedHashMap<String, String>();
		String value = getString(props, key);
		String[] values = value.split(lineSeperator);
		List<String> attributeKeys = new ArrayList<String>();
		
		for(String val : values) {
			String[] vals = val.split(typeSeperator, -1);
			
			if (vals.length >= 2) 
			  orderedMap.put(vals[0], vals[1]);
			else
			  orderedMap.put(vals[0], PolicyActivityInfoV5.FROM_RESOURCE_ATTRIBUTES_TAG);
		}
		return orderedMap;
	}
	
	protected static String[] getStringArray(Properties props, String key) {
		return getStringArray(props, key, DEFAULT_VALUE_SEPERATOR);
	}
	
	protected static int getNonNegativeInteger(Properties props, String key)
			throws IllegalArgumentException {
		return getInteger(props, key, NON_NEGATIVE_INTEGER_RANGE);
	}

	protected static int getInteger(Properties props, String key, Pair<Integer, Integer> range)
			throws IllegalArgumentException {
		String value = getString(props, key);
		return getIntegerInternal(value, key, range);
	}
	
	private static int getIntegerInternal(String str, String key, Pair<Integer, Integer> range){
		int valueInt;
		
		try {
			valueInt = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("\"" + key + "\" is not an integer.", e);
		}

		if (valueInt < range.first() || valueInt > range.second()) {
			throw new IllegalArgumentException("\"" + key + "\" should be between " + range.first()
					+ " and " + range.second());
		}
		return valueInt;
	}

	protected static boolean getBoolean(Properties props, String key) throws IllegalArgumentException {
		String value = getString(props, key);
		Boolean valueBoolean = StringUtils.stringToBoolean(value);
		if (valueBoolean == null) {
			throw new IllegalArgumentException("\"" + key + "\" is not an boolean.");
		}
		return valueBoolean;
	}


	protected static float getFloat(Properties props, String key, Pair<Float, Float> range)
			throws IllegalArgumentException {
		String value = getString(props, key);
		float valueInt;
		try {
			valueInt = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("\"" + key + "\" is not a float.", e);
		}

		if (valueInt < range.first() || valueInt > range.second()) {
			throw new IllegalArgumentException("\"" + key + "\" should be between " + range.first()
					+ " and " + range.second());
		}
		return valueInt;
	}
	
	protected static int[] getIntegerArray(Properties props, String key, String seperator,
			Pair<Integer, Integer> range) {
		String[] values = getStringArray(props, key, seperator);
		int[] result = new int[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = getIntegerInternal(values[i], key, range);
		}
		return result;
	}
	
	private final boolean isInteractive;
	private final boolean isHacker;
	
	ReporterDataConfigBase(boolean isInteractive, boolean isHacker){
		this.isInteractive = isInteractive;
		this.isHacker = isHacker;
	}
	
	ReporterDataConfigBase(){
		this(true, false);
	}

	public boolean isInteractive() {
		return isInteractive;
	}

	public boolean isHacker() {
		return isHacker;
	}
}
