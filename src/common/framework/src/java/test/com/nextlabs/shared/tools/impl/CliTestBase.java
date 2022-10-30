/*
 * Created on Nov 4, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Before;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/impl/CliTestBase.java#1 $
 */

public abstract class CliTestBase {
	protected ConsoleApplicationBase cli;
	
	@Before
	public void cleanUp() throws Exception {
		Option.reset();
	}
	
	protected void assertNoError(String[] args) {
		assertError(args, (List<String>)null);

	}
	
	protected void assertError(String[] args, String expectedErrorMessage) {
		assertError(args, Collections.singletonList(expectedErrorMessage));
	}
	
	protected void assertError(String[] args, List<String> expectedErrorMessages) {
		try{
			cliParseAndExecute(args);
			if(expectedErrorMessages != null){
				fail("I should fail but I didn't.");
			}
		}catch(ParseException e){
			if(expectedErrorMessages == null){
				fail(e.getMessage());
			}
			
			String expectged = CollectionUtils.asString(expectedErrorMessages, ConsoleDisplayHelper.NEWLINE);
			assertEquals(expectged, e.getMessage());
		}
	}
	
	protected abstract void cliParseAndExecute(String[] args) throws ParseException;
}
