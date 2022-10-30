/*
 * Created on Oct 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/display/BarTest.java#1 $
 */

public class BarTest {
	@Test(expected=IllegalArgumentException.class)
	public void lengthTooShort(){
		new Bar(3, Bar.Direction.COUNT_UP);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void lengthTInvalid(){
		new Bar(-1, Bar.Direction.COUNT_UP);
	}
	
	@Test
	public void lengthOk(){
		new Bar(4, Bar.Direction.COUNT_UP);
	}
	
	@Test
	public void countUp(){
		Bar bar = new Bar(12, Bar.Direction.COUNT_UP);
		assertEquals( "[          ]", bar.getOutput() );
		bar.update(0.09F);
		assertEquals( "[          ]", bar.getOutput() );
		bar.update(0.10F);
		assertEquals( "[=         ]", bar.getOutput() );
		bar.update(0.19F);
		assertEquals( "[=         ]", bar.getOutput() );
		bar.update(0.20F);
		assertEquals( "[==        ]", bar.getOutput() );
		bar.update(0.99F);
		assertEquals( "[========= ]", bar.getOutput() );
		bar.update(1.00F);
		assertEquals( "[==========]", bar.getOutput() );
	}
	
	@Test
	public void countDown(){
		Bar bar = new Bar(12, Bar.Direction.COUNT_DOWN);
		assertEquals( "[==========]", bar.getOutput() );
		bar.update(0.99F);
		assertEquals( "[==========]", bar.getOutput() );
		bar.update(0.94F);
		assertEquals( "[==========]", bar.getOutput() );
		bar.update(0.20F);
		assertEquals( "[        ==]", bar.getOutput() );
		bar.update(0.19F);
		assertEquals( "[        ==]", bar.getOutput() );
		bar.update(0.10F);
		assertEquals( "[         =]", bar.getOutput() );
		bar.update(0.09F);
		assertEquals( "[         =]", bar.getOutput() );
		bar.update(0.01F);
		assertEquals( "[         =]", bar.getOutput() );
		bar.update(0.00F);
		assertEquals( "[          ]", bar.getOutput() );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void update1(){
		Bar bar = new Bar(12, Bar.Direction.COUNT_UP);
		bar.update((float)-0.1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void update2(){
		Bar bar = new Bar(12, Bar.Direction.COUNT_UP);
		bar.update((float)1.001);
	}
}
