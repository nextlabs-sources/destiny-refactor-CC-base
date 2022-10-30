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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/Bar.java#1 $
 */

public class Bar implements IDisplayable{
	private static final char START = '[';
	private static final char END = ']';
	private static final char DEFAULT_USED = '=';
	private static final char SPACE = ' ';
	
	public enum Direction {
		COUNT_UP, COUNT_DOWN
	};

	private final int length;
	private final Direction direction;
	private final char usedChar;
	private final Spinner spinner;
	
	private float percent;
	
	public Bar(int length, Direction direction) {
		this(length, direction, DEFAULT_USED);
	}
	
	public Bar(int length, Direction direction, char usedChar) {
		this(length, direction, usedChar, false);
	}
	
	public Bar(int length, Direction direction, boolean withSpinner) {
		this(length, direction, DEFAULT_USED, withSpinner);
	}

	public Bar(int length, Direction direction, char usedChar, boolean withSpinner) {
		if (length < 4) {
			throw new IllegalArgumentException("Minimum length of a bar is 4");
		}
		this.length = length;
		this.direction = direction;
		switch (direction) {
		case COUNT_UP:
			percent = 0;
			break;
		case COUNT_DOWN:
			percent = 100;
			break;
		}
		this.usedChar = usedChar; 
		spinner = withSpinner ? new Spinner() : null; 
	}
	
	public String getOutput() {
		StringBuilder sb = new StringBuilder();
		sb.append(START);

		int barLength = this.length - 2;

		switch (direction) {
		case COUNT_UP:
			int used = (int) (barLength * percent);
			sb.append(StringFormatter.repeat(usedChar, used));
			if (spinner != null) {
				sb.append(spinner.getOutput());
				used++;
			}
			
			sb.append(StringFormatter.repeat(SPACE, barLength - used));
			break;
		case COUNT_DOWN:
			int done = (int) (barLength * percent + 0.009);
			sb.append(StringFormatter.repeat(SPACE, barLength - done));
			if (spinner != null) {
				sb.append(spinner.getOutput());
				done++;
			}
			sb.append(StringFormatter.repeat(usedChar, done));
			break;
		}

		sb.append(END);
		return sb.toString();
	}

	/**
     * set the progress to specify percent
	 * @param percent range from 0 to 1
	 * @throws IllegalArgumentException if the percent is not in range.
	 */
	public void update(float percent) throws IllegalArgumentException {
		if (percent < 0 || percent > 1) {
			throw new IllegalArgumentException();
		}
		this.percent = percent;
	}

	/**
	 * increase the progress to specify percent, max cap at 1
	 * @param percent range from 0 to 1
	 */
	public void increment(float percent) {
		percent += this.percent;
		if (percent > 1) {
			percent = 1;
		}
		update(percent);
	}

	/**
	 * decrease the progress to specify percent, min cap at 0
	 * @param percent range from 0 to 1
	 */
	public void decrement(float percent) {
		percent = this.percent - percent;
		if (percent < 0) {
			percent = 0;
		}
		update(percent);
	}
	
	public boolean isUpdateable() {
		return true;
	}

	public int getLength() {
		return length;
	}

	public float getPercent() {
		return this.percent;
	}
	
	@Override
	public String toString(){
		return getOutput();
	}
}