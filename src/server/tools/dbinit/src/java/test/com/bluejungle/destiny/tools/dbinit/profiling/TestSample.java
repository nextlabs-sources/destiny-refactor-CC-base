/*
 * Created on Dec 11, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.profiling;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/profiling/TestSample.java#1 $
 */
public class TestSample {
	public TestSample(){
		
	}
	
	@Test
	public void sampleAdd() {
		Sample sample = new Sample(100);
		for (int i = 0; i < 30; i++) {
			sample.add(i * 3);
		}
		for (int i = 0; i < 30; i++) {
			assertEquals(i * 3, sample.getResult()[i]);
		}
		
		assertEquals(1, sample.getCompressionRatio());
		assertEquals(30, sample.getNumOfSamples());
		assertEquals(43.5, sample.getAverage(), 0.0);
	}
	
	@Test
	public void addMore1() {
		Sample sample = new Sample(10);
		for (int i = 0; i < 20; i++) {
			sample.add(i * 3);
		}
		assertArrayEquals(new int[] { 2, 8, 14, 20, 26, 31, 37, 43, 49, 55 }, sample.getResult());
		
		assertEquals(2, sample.getCompressionRatio());
		assertEquals(20, sample.getNumOfSamples());
		assertEquals(28.5, sample.getAverage(), 0.0);
		
		sample.add(999);
		
		assertEquals(4, sample.getCompressionRatio());
		assertEquals(21, sample.getNumOfSamples());
		assertEquals("addMore1 incorrect average", 228.4, sample.getAverage(), 0.0);
	}
	
	@Test
	public void addMore2() {
		Sample sample = new Sample(10);
		for (int i = 0; i < 30; i++) {
			sample.add(i * 3);
		}
		assertArrayEquals(new int[] { 5, 17, 29, 40, 52, 63, 75, 85  }, sample.getResult());
		
		assertEquals(4, sample.getCompressionRatio());
		assertEquals(30, sample.getNumOfSamples());
		assertEquals(52.285713, sample.getAverage(), 0.0);
	}
	
}
