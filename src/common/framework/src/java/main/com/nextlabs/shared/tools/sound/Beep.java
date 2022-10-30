/*
 * Created on Oct 9, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.sound;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/sound/Beep.java#1 $
 */

public class Beep implements ISound{
	public static final char BEEP = '\u0007';

	/**
	 * @see com.nextlabs.shared.tools.sound.ISound#play()
	 */
	public void play() {
		System.out.print(BEEP);
	}
}
