/*
 * Created on Oct 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;

import com.nextlabs.shared.tools.StringFormatter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/ProgressBar.java#1 $
 */

public class ProgressBar extends Bar{
	//per message won't update
	//bar will change everytime(assume the the percent is changed)
	//the post message will be changed (even the bar didn't change), think about this is a estimated time;
	
	private final int perMessageLength;
	private final int postMessageLength;
	private final int totalLength;

	private String perMessage;
	private String postMessage;

	private long startTime;
	private long lastUpdate;

	private float overallSpeed;
	private float currentSpeed;
	
	public ProgressBar(final int perMessageLength, final int barLength, final int postMessageLength) {
		super(barLength, Bar.Direction.COUNT_UP, true);
		if (perMessageLength < 0 || postMessageLength < 0) {
			throw new IllegalArgumentException();
		}
		this.perMessageLength = perMessageLength;
		this.postMessageLength = postMessageLength;
		totalLength = perMessageLength + super.getLength() + postMessageLength;
		setPerMessage("");
		setPostMessage("");
		start();
	}
	
	@Override
	public String getOutput() {
		return perMessage + super.getOutput() + postMessage;
	}
		
	//set the progress to specify percent
	public void setPostMessage(String postMessage) {
		this.postMessage = StringFormatter.fitLength(postMessage, postMessageLength);
	}

	public void setPerMessage(String perMessage) {
		this.perMessage = StringFormatter.fitLength(perMessage, perMessageLength);
	}

	@Override
	public void update(float percent) {
		float different = Math.abs(getPercent() - percent);
		long diffTime = System.currentTimeMillis() - lastUpdate;
		super.update(percent);
		currentSpeed = diffTime / different;
		overallSpeed = (System.currentTimeMillis() - startTime) / percent;
		lastUpdate = System.currentTimeMillis();
	}

	@Override
	public int getLength() {
		return totalLength;
	}
	
	public void start(){
		startTime = System.currentTimeMillis();
		lastUpdate = System.currentTimeMillis();
	}
	
	//return in milli seconds
	public long getEstimateTimeLeft() {
		return (long) (currentSpeed * (1 - getPercent()));
	}
	
	//return in milli seconds
	public long getOverallEstiimateTimeLeft() {
		return (long) (overallSpeed * (1 - getPercent()));
	}
	
	public long getElapsedTime(){
	    return System.currentTimeMillis() - startTime;
	}
	
	@Override
	public String toString() {
		return getOutput();
	}
}
