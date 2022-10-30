/*
 * Created on Mar 3, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.display;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/display/Spinner.java#1 $
 */

public class Spinner implements IDisplayable {
	private static final char[] CHARS = new char[] { '|', '/', '-', '\\' };
	private int index = 0;
	
	public int getLength() {
		return 1;
	}

	public String getOutput() {
		index++;
		if (index == CHARS.length) {
			index = 0;
		}
		return String.valueOf(CHARS[index]);
	}

	public boolean isUpdateable() {
		return true;
	}

}
