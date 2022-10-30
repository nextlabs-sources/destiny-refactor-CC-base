/*
 * Created on Feb 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.IDisplayable;
//import com.nextlabs.shared.tools.display.Spinner;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/ConsoleStatus.java#1 $
 */

public class ConsoleStatus implements IDisplayable {
	private static final String FORMAT = "count: %d, speed: %.2f (entries/s), overall speed: %.2f";
	
//	private final Spinner spinner;
	
	private int currentCount = 0;
	
	private long startTime;
	private long lastUpdate;

	private float overallSpeed;
	private float currentSpeed;
	
	public ConsoleStatus(){
//		spinner = new Spinner();
	}

	public int getLength() {
		return ConsoleDisplayHelper.getScreenWidth();
	}
	
	public void start(){
		startTime = System.currentTimeMillis();
		lastUpdate = System.currentTimeMillis();
	}
	
	public void set(int number) {
		float different = number - currentCount;
		long diffTime = System.currentTimeMillis() - lastUpdate;
		if(different == 0){
			currentSpeed = 0;
		}else{
			currentSpeed = different * 1000f / diffTime ;
		}
		
		currentCount = number;
		overallSpeed = (float)currentCount / (System.currentTimeMillis() - startTime) * 1000;
		lastUpdate = System.currentTimeMillis();
	}
	
//	public void increase(int number) {
//		long diffTime = System.currentTimeMillis() - lastUpdate;
//		currentSpeed = (float)diffTime / number;
//		
//		currentCount += number;
//		overallSpeed = (System.currentTimeMillis() - startTime) / currentCount;
//		lastUpdate = System.currentTimeMillis();
//	}

	public String getOutput() {
		return String.format(FORMAT, currentCount, currentSpeed, overallSpeed);
	}

	public boolean isUpdateable() {
		return true;
	}

}
