/*
 * Created on Oct 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;

import java.io.PrintStream;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/ConsoleDisplayHelper.java#1 $
 */

public class ConsoleDisplayHelper {
	// microsoft \r\n
	// *inx \n
	// mac \r
    // don't make this final
	public static String NEWLINE = System.getProperty("line.separator", "\n");
	
	//use the system "standard" output stream. 
	private static PrintStream defaultPrinter = System.out;
	
	public static void setDefaultPrinter(PrintStream defaultPrinter) {
		ConsoleDisplayHelper.defaultPrinter = defaultPrinter;
	}

	public static void redraw(PrintStream printStream, IDisplayable displayable) {
		//target to MS windows command prompt only
		if (displayable.isUpdateable()) {
			printStream.print('\r');
		}
		
		printStream.print(displayable.getOutput());
	}
	
	public static void redraw(IDisplayable displayable) {
		redraw(defaultPrinter, displayable);
	}
	
	public static String formatTime(long time) {
		//TODO use time method?
		
		StringBuilder sb = new StringBuilder();
		long seconds = time / 1000; //second

		long minute = seconds / 60;
		if (minute > 0) {
			long hour = minute / 60;
			if (hour > 0) {
				long day = hour / 24;
				if (day > 0) {
					//if the day is too crazy!
					if (day > 365) {
						return "NaN";
					}
					sb.append(day + "d ");
				}
				sb.append(hour % 24 + "h ");
			}
			sb.append(minute % 60 + "m ");
		}
		sb.append(seconds % 60 + "s");

		return sb.toString();
	}
	
	public static final int getScreenWidth(){
	    // 80 is windows default width, but we need to leave one for carriage return in case the
	    // object is redrawable.
	    // TODO get the width in runtime.
	    return 79;
	}
}
