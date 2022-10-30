package com.nextlabs.shared.tools;


import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.shared.tools.impl.DetailUsagePrinterTest;
import com.nextlabs.shared.tools.impl.OptionTest;
import com.nextlabs.shared.tools.impl.OptionTriggerTest;
import com.nextlabs.shared.tools.impl.OptionValidatorPreCheckTest;
import com.nextlabs.shared.tools.impl.OptionValidatorTest;
import com.nextlabs.shared.tools.impl.SynopisUsagePrinterTest;


/**
 * TODO description
 *
 * @author hchan
 * @date Jun 28, 2007
 */
@RunWith(Suite.class)
@SuiteClasses( { 
	TestConsoleApplicationBase.class,
	OptionValueTypesTest.class, 
	StringFormaterTest.class, 
	DetailUsagePrinterTest.class, 
	OptionTest.class,
	OptionValidatorPreCheckTest.class, 
	OptionValidatorTest.class, 
	OptionTriggerTest.class, 
	SynopisUsagePrinterTest.class } )
	
public class TestCliSuite {
	public static Test suite() {
		return new JUnit4TestAdapter(TestCliSuite.class);
	}
}