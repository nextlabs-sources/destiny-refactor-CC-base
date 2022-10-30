/*
 * Created on Oct 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/display/ProgressBarTest.java#1 $
 */

public class ProgressBarTest {

	public static void main(String[] args) throws InterruptedException {
		ProgressBar pb = new ProgressBar(10,50,50);
		pb.setPerMessage("per-mes");
		
//		long startTime = System.currentTimeMillis();
		for(int i=0; i <= 100; i+=10){
			
			update(pb, i);
			if( i==20){
				Thread.sleep(1000);
			}
			if ( i == 50){
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
				Thread.sleep(1000);
				update(pb, i);
			}
			Thread.sleep(10);
		}
		
//		System.out.println(System.currentTimeMillis()- startTime);
		
	}

	private static void update(ProgressBar pb, int i) {
		pb.update(i);
		String timeLeave = ConsoleDisplayHelper.formatTime(pb.getEstimateTimeLeft());
		timeLeave += " - "+ ConsoleDisplayHelper.formatTime(pb.getOverallEstiimateTimeLeft());
		pb.setPostMessage(timeLeave);
		ConsoleDisplayHelper.redraw(pb);
	}
}
