/*
 * Created on Sep 10, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.ParseException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/impl/OptionValidatorTestBase.java#1 $
 */

public abstract class OptionValidatorTestBase {
	protected ICompoundOptionDescriptor root;

	@BeforeClass
	public static void initialize() {
	}
	
	@Before
	public void reset() {
		Option.reset();
	}
	
	protected Option createOption(
			String commandLineIndicator, 
			boolean required,
			boolean valueRequired, 
			int numPossibleValues
			) {
		return createOption(Collections.singleton(commandLineIndicator), required, valueRequired,
				numPossibleValues);
	}

	protected Option createOption(
			Set<String> commandLineIndicators, 
			boolean required,
			boolean valueRequired, 
			int numPossibleValues
			) {
		Object defaultValue = null;
		String optionId = commandLineIndicators.iterator().next();
		String valueLabel = optionId;

		try {
			return new Option(
					OptionId.create(optionId, OptionValueType.INTEGER),	// OptionId<T> optionId,
					commandLineIndicators,	// Collection<String> commandLineIndicators,
					"",						// String description,
					required,				// boolean required,
					valueLabel,				// String valueLabel,
					defaultValue,			// T defaultValue, 
					valueRequired, 			// boolean valueRequired,
					numPossibleValues);		// int numPossibleValues
		} catch (InvalidOptionDescriptorException e) {
			throw new RuntimeException(e);
		}
	}

	protected void assertNoError(String[] args) {
		assertError(args, null);

	}
	
	protected void assertError(String[] args, List<String> expectedErrorMessages) {
		IConsoleApplicationDescriptor consoleAppDescriptor = new MockConsoleAppDescriptor(root);
		CommandLineArguments commandLineArguments = null;
		try {
			commandLineArguments = new CommandLineArguments(args);
		} catch (ParseException e) {
			fail(e.toString());
		}
		
		OptionValidator optionValidator = new OptionValidator(commandLineArguments, true);
		optionValidator.renderUsage(consoleAppDescriptor);
		if (expectedErrorMessages == null) {
			assertFalse("should not have errors, "
					+ CollectionUtils.toString(optionValidator.getErrorLog()), optionValidator
					.hasError());
			assertEquals(0, optionValidator.getErrorLog().size());
		} else {
			assertTrue("should have errors", optionValidator.hasError());
			assertEquals(expectedErrorMessages, optionValidator.getErrorLog());
		}
	}
	

	protected class MockConsoleAppDescriptor implements IConsoleApplicationDescriptor {
		private NoOptioDescriptorTree	noOptioDescriptorTree;

		public MockConsoleAppDescriptor(ICompoundOptionDescriptor root) {
			noOptioDescriptorTree = new NoOptioDescriptorTree(root);
		}

		public String getLongDescription() {
			return null;
		}

		public String getName() {
			return null;
		}

		public String getShortDescription() {
			return null;
		}

		public IOptionDescriptorTree getOptions() {
			return noOptioDescriptorTree;
		}

		private class NoOptioDescriptorTree implements IOptionDescriptorTree {
			private ICompoundOptionDescriptor	root;

			public NoOptioDescriptorTree(ICompoundOptionDescriptor root) {
				this.root = root;
			}

			public ICompoundOptionDescriptor getRootOption() {
				return root;
			}

			@SuppressWarnings("unused")
			public IOptionDescriptor getOption(	String optionId) {
				return null;
			}

		}
	}
}
