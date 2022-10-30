package com.nextlabs.shared.tools.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.ParseException;

/**
 * TODO description
 *
 * @author hchan
 * @date Apr 13, 2007
 */
public class OptionValidatorPreCheckTest extends OptionValidatorTestShared{
	@Override
	protected void assertError(String[] args, List<String> expectedErrorMessages) {
		OptionValidatorPreCheck preCheck = createPreCheck(args, root);
		root.accept(preCheck);
		if (expectedErrorMessages == null) {
			if(preCheck.hasError()){
				System.err.println(preCheck.getErrorLog());
			}
			assertFalse("should not have errors", preCheck.hasError());
			assertEquals(0, preCheck.getErrorLog().size());
		} else {
			assertTrue("should have errors", preCheck.hasError());
			assertEquals(expectedErrorMessages, preCheck.getErrorLog());
		}
	}

	private OptionValidatorPreCheck createPreCheck(String[] args, ICompoundOptionDescriptor root) {
		IConsoleApplicationDescriptor consoleAppDescriptor = new MockConsoleAppDescriptor(root);
		CommandLineArguments commandLineArguments = null;
		try {
			commandLineArguments = new CommandLineArguments(args);
		} catch (ParseException e) {
			fail(e.toString());
		}
		return new OptionValidatorPreCheck(new OptionValidatorNode(root), commandLineArguments);
	}
	
	@Test @Override
	public void noOptionsOneArgWithoutValue() {
		root = new SimpleCompoundOptionDescriptor(null);
		assertNoError(new String[] { "-a" });
	}

	@Test @Override
	public void noOptionsOneArgWithValue() {
		root = new SimpleCompoundOptionDescriptor(null);
		assertNoError(new String[] { "-a", "123" });
	}
	
	@Test @Override
	public void oneRequiredOptionWithoutValueNoArgs() {
		Option option = createOption("a", true, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		//should not check the option required
		assertNoError(new String[] {});
	}
	
	@Test @Override
	public void oneRequiredOptionWithValueNoArgs() {
		Option option = createOption("a", true, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
//		should not check the option required
		assertNoError(new String[] {"-a", "123"});
	}
	
	@Test @Override
	public void oneRequiredOptionWithoutValueOneArgWithValue1() {
		Option option = createOption("a", true, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		//should not check unknown 
		List<String> expected = new ArrayList<String>();
		expected.add(NO_VALUES.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123abc" }, expected);
	}
}
