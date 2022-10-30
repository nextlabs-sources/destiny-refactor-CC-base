/*
 * Created on Jan 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bluejungle.framework.utils.Pair;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/test/com/nextlabs/destiny/tools/reporterdata/TestReporterDataFromCSVConfig.java#1 $
 */

public class TestReporterDataFromCSVConfig{
	private static final String SRC_ROOT_DIR = "src.root.dir";
	private static final String RELATIVE_PATH_FROM_SRC = "/server/tools/reporterData/";

	private static String insertPerLogKey;
	private static File reporterRoot = null;

	private ReporterDataFromCSVConfig r;
	
	
	@BeforeClass
	public static void setup() throws FileNotFoundException{
		String s;
		if( (s = System.getProperty(SRC_ROOT_DIR)) == null){
			if( (s = System.getenv(SRC_ROOT_DIR)) == null){
				throw new FileNotFoundException(SRC_ROOT_DIR + " is not set.");
			}
		}
		reporterRoot = new File(s, RELATIVE_PATH_FROM_SRC);
		
		
	}
	
	private void initOrReuse() throws Exception{
		if( r == null){
			r =	new ReporterDataFromCSVConfig(new File(reporterRoot, "etc/csvConfig.properties"));
			Field field = ReporterDataFromCSVConfig.class.getDeclaredField("INSERTION_PER_LOG");
			field.setAccessible(true);
			insertPerLogKey = field.get(r).toString();
		}
	}
	
	@Test
	public void createFromGoodFile() throws IOException {
		r =	new ReporterDataFromCSVConfig(new File(reporterRoot, "etc/csvConfig.properties"));
	}
	
	
	@Test
	public void parseInsertionTimeSimple() throws Exception {
		initOrReuse();
		Properties p = new Properties();
		p.put(insertPerLogKey, "1=10");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(1, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
	}
	
	@Test
	public void parseInsertionTimeMultiValue() throws Exception {
		initOrReuse();
		
		Properties p = new Properties();
		p.put(insertPerLogKey, "1=10;2=4;3=9");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(3, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(4, l.get(1).second().intValue());
		assertEquals(3, l.get(2).first().intValue());
		assertEquals(9, l.get(2).second().intValue());
		
		p = new Properties();
		p.put(insertPerLogKey, "1=10;2=4;3=9;");
		l = r.parseInsertionTimeString(p);
		assertEquals(3, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(4, l.get(1).second().intValue());
		assertEquals(3, l.get(2).first().intValue());
		assertEquals(9, l.get(2).second().intValue());
		
		p = new Properties();
		p.put(insertPerLogKey, ";1=10;2=4;3=9;");
		l = r.parseInsertionTimeString(p);
		assertEquals(3, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(4, l.get(1).second().intValue());
		assertEquals(3, l.get(2).first().intValue());
		assertEquals(9, l.get(2).second().intValue());
	}
	
	@Test
	public void parseInsertionTimeRepeatValue() throws Exception {
		initOrReuse();
		Properties p = new Properties();
		p.put(insertPerLogKey, "1=10;2=4;1=10");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(3, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(4, l.get(1).second().intValue());
		assertEquals(1, l.get(2).first().intValue());
		assertEquals(10, l.get(2).second().intValue());
	}
	
	
	@Test
	public void parseInsertionTimeOrValue() throws Exception {
		initOrReuse();
		Properties p = new Properties();
		p.put(insertPerLogKey, "1,2=10");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(2, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(10, l.get(1).second().intValue());
		
		p = new Properties();
		p.put(insertPerLogKey, "1,2,5,9=10");
		l = r.parseInsertionTimeString(p);
		assertEquals(4, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(10, l.get(1).second().intValue());
		assertEquals(5, l.get(2).first().intValue());
		assertEquals(10, l.get(2).second().intValue());
		assertEquals(9, l.get(3).first().intValue());
		assertEquals(10, l.get(3).second().intValue());
	}
	
	@Test
	public void parseInsertionTimeOrValueMulti() throws Exception {
		initOrReuse();
		
		Properties p = new Properties();
		p.put(insertPerLogKey, "1,2=10;3,9=11");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(4, l.size());
		assertEquals(1, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(2, l.get(1).first().intValue());
		assertEquals(10, l.get(1).second().intValue());
		assertEquals(3, l.get(2).first().intValue());
		assertEquals(11, l.get(2).second().intValue());
		assertEquals(9, l.get(3).first().intValue());
		assertEquals(11, l.get(3).second().intValue());
	}
	
	@Test
	public void parseInsertionTimeRangeValue() throws Exception {
		initOrReuse();
		
		Properties p = new Properties();
		p.put(insertPerLogKey, "2-4=10");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(3, l.size());
		assertEquals(2, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(3, l.get(1).first().intValue());
		assertEquals(10, l.get(1).second().intValue());
		assertEquals(4, l.get(2).first().intValue());
		assertEquals(10, l.get(2).second().intValue());
	}
	
	@Test (expected = IllegalArgumentException.class )
	public void parseInsertionTimeRangeBad() throws Exception {
		initOrReuse();
		Properties p = new Properties();
		p.put(insertPerLogKey, "2-4-8=10");
		r.parseInsertionTimeString(p);
	}
	
	@Test
	public void parseInsertionTimeRangeValueMulti() throws Exception {
		initOrReuse();
		
		Properties p = new Properties();
		p.put(insertPerLogKey, "2-4=10;5-6=11");
		List<Pair<Integer, Integer>> l = r.parseInsertionTimeString(p);
		assertEquals(5, l.size());
		assertEquals(2, l.get(0).first().intValue());
		assertEquals(10, l.get(0).second().intValue());
		assertEquals(3, l.get(1).first().intValue());
		assertEquals(10, l.get(1).second().intValue());
		assertEquals(4, l.get(2).first().intValue());
		assertEquals(10, l.get(2).second().intValue());
		assertEquals(5, l.get(3).first().intValue());
		assertEquals(11, l.get(3).second().intValue());
		assertEquals(6, l.get(4).first().intValue());
		assertEquals(11, l.get(4).second().intValue());
	}
	
	@Test (expected = IllegalArgumentException.class )
	public void parseInsertionTimeRangeMixedValue() throws Exception {
		initOrReuse();
		
		Properties p = new Properties();
		p.put(insertPerLogKey, "1,3-4=10");
		r.parseInsertionTimeString(p);
	}
	
}
