/*
 * Created on Nov 7, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import java.io.IOException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/ISecureConsolePrompt.java#1 $
 */

public interface ISecureConsolePrompt {
	char[] readConsoleSecure() throws IOException;
}
