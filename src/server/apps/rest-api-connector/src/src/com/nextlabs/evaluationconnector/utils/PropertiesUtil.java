/*
 * Created on Jan 22, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.parsers.XmlXACMLParser;

/**
 * <p>
 * PropertiesUtil
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public final class PropertiesUtil {

	private static final Log log = LogFactory.getLog(XmlXACMLParser.class);
	private static final Properties props = new Properties();

	/**
	 * <p>
	 * Load the properties from the given file
	 * </p>
	 *
	 * @param inStream
	 *            {@link InputStream}
	 * @throws Exception
	 */
	public static void init(String filePath) throws Exception {
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(filePath);
			props.load(inStream);

		} catch (Exception e) {
			throw e;
		} finally {
			if (inStream != null)
				inStream.close();
		}
		log.info("Properties loaded successfully. [ Total No of Properties : "
				+ props.size() + "]");
	}

	/**
	 * <p>
	 * Load the properties from the given input stream
	 * </p>
	 *
	 * @param inStream
	 *            {@link InputStream}
	 * @throws Exception
	 */
	public static void init(InputStream inStream) throws Exception {

		try {
			props.load(inStream);

		} catch (Exception e) {
			throw e;
		} finally {
			if (inStream != null)
				inStream.close();
		}
		log.info("Properties loaded successfully. [ Total No of Properties : "
				+ props.size() + "]");
	}

	/**
	 * <p>
	 * Get String Value for the given key from Properties file
	 * </p>
	 *
	 * @param key
	 * @return value
	 */
	public static String getString(String key) {
		return props.getProperty(key, "");
	}

	/**
	 * <p>
	 * Get integer Value for the given key from Properties file
	 * </p>
	 *
	 * @param key
	 * @return value
	 */
	public static int getInt(String key) {
		String strVal = props.getProperty(key, "-1");
		return Integer.valueOf(strVal);
	}

	/**
	 * <p>
	 * Get long Value for the given key from Properties file
	 * </p>
	 *
	 * @param key
	 * @return value
	 */
	public static long getLong(String key) {
		String strVal = props.getProperty(key, "-1");
		return Long.valueOf(strVal);
	}

	/**
	 * <p>
	 * Get double Value for the given key from Properties file
	 * </p>
	 *
	 * @param key
	 * @return value
	 */
	public static double getDouble(String key) {
		String strVal = props.getProperty(key, "-1");
		return Double.valueOf(strVal);
	}

	/**
	 * <p>
	 * Check values either not null or empty
	 * </p>
	 *
	 * @param value
	 * @return false is null or empty otherwise true
	 */
	public static boolean isNotNullOrEmpty(String value) {
		return (value == null || value.isEmpty()) ? false : true;
	}

	// public static FileWriter writer1 = null;
	// public static FileWriter writer2 = null;
	// public static FileWriter writer3 = null;
	// public static FileWriter writer4 = null;
	// public static FileWriter writer5 = null;
	// public static FileWriter writer6 = null;
	//
	// public static void createFile(String fileName) {
	// try {
	// writer1 = new FileWriter(fileName + "_1.csv");
	// writer2 = new FileWriter(fileName+ "_2.csv");
	// writer3 = new FileWriter(fileName+ "_3.csv");
	// writer4 = new FileWriter(fileName+ "_4.csv");
	// writer5 = new FileWriter(fileName+ "_5.csv");
	// writer6 = new FileWriter(fileName+ "_6.csv");
	// } catch (IOException e) {
	// log.error("Error in creating a file. ", e);
	// }
	// return;
	// }

	// public static final StringBuffer buffer1 = new StringBuffer();
	// public static final StringBuffer buffer2 = new StringBuffer();
	// public static final StringBuffer buffer3 = new StringBuffer();
	// public static final StringBuffer buffer4 = new StringBuffer();
	// public static final StringBuffer buffer5 = new StringBuffer();
	// public static final StringBuffer buffer6 = new StringBuffer();

	// public static void write(int id, String ar) {
	// switch (id) {
	// case 1:
	// buffer1.append(ar).append(",\n");
	// break;
	// case 2:
	// buffer2.append(ar).append(",\n");
	// break;
	// case 3:
	// buffer3.append(ar).append(",\n");
	// break;
	// case 4:
	// buffer4.append(ar).append(",\n");
	// break;
	// case 5:
	// buffer5.append(ar).append(",\n");
	// break;
	// case 6:
	// buffer6.append(ar).append(",\n");
	// break;
	//
	// default:
	// break;
	// }
	// }

	// public static void flushToFile() throws IOException {
	// writer1.write(buffer1.toString());
	// writer1.write(",");
	// writer1.flush();
	//
	// writer2.write(buffer2.toString());
	// writer2.write(",");
	// writer2.flush();
	//
	// writer3.write(buffer3.toString());
	// writer3.write(",");
	// writer3.flush();
	//
	// writer4.write(buffer4.toString());
	// writer4.write(",");
	// writer4.flush();
	//
	// writer5.write(buffer5.toString());
	// writer5.write(",");
	// writer5.flush();
	//
	// writer6.write(buffer6.toString());
	// writer6.write(",\n");
	// writer6.flush();
	//
	// writer1.close();
	// writer2.close();
	// writer3.close();
	// writer4.close();
	// writer5.close();
	// writer6.close();
	// }
}
