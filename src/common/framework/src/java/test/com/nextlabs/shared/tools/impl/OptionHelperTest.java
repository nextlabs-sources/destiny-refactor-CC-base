/*
 * Created on May 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.ParseException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/impl/OptionHelperTest.java#1 $
 */

public class OptionHelperTest {

	@Before
	public void setUp(){
		OptionMod.reset();
	}
	
	@Test
	public void mixedOptionValueType() throws ParseException {
		final OptionId<Boolean> ARG1_OPTION_ID = OptionId.create("arg1", OptionValueType.BOOLEAN);
		final OptionId<String> ARG2_OPTION_ID = OptionId.create("arg2", OptionValueType.STRING);
		final OptionId<Integer> ARG3_OPTION_ID = OptionId.create("arg3", OptionValueType.INTEGER);
		final OptionId<Integer> ARG4_OPTION_ID = OptionId.create("arg4", OptionValueType.INTEGER);
		
		ConsoleApplicationBaseMock consoleApp = new ConsoleApplicationBaseMock(new CallBack(){
			public ICompoundOptionDescriptor createRoot() {
				SequencedListOptionDescriptor options = new SequencedListOptionDescriptor();
				options.add(Option.createOption(ARG1_OPTION_ID, "description1", "value1"));
				options.add(Option.createOption(ARG2_OPTION_ID, "description2", "value2"));
				options.add(Option.createOption(ARG3_OPTION_ID, "description3", "value3"));
				
				return options;
			}
			
			public void check(ICommandLine commandLine) {
				//all id with regular order
				Collection<OptionId<?>> optionIds = new ArrayList<OptionId<?>>();
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				OptionId<?> selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG1_OPTION_ID, selected);
				
				//all id with different order
				optionIds.clear();
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG2_OPTION_ID, selected);
				
				
				//only one id
				optionIds.clear();
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG3_OPTION_ID, selected);
				
				//duplicated optionid
				optionIds.clear();
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG1_OPTION_ID, selected);
				
				// not registered id
				optionIds.clear();
				optionIds.add(ARG4_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertNull(selected);
				
				optionIds.clear();
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertNull(selected);
				
				try {
					selected = OptionHelper.findSelectedOption(null, commandLine);
					fail();
				} catch (NullPointerException e) {
					assertNotNull(e);
				}
				
				
				Collection<OptionId<Boolean>> trueOptions =
						OptionHelper.findTrueOption(Collections.singleton(ARG1_OPTION_ID), commandLine);
				
				assertNotNull(trueOptions);
				assertEquals(1, trueOptions.size());
				assertEquals(ARG1_OPTION_ID, trueOptions.iterator().next());
			}
		});
		consoleApp.parseAndExecute(new String[] { "-arg1", "true", "-arg2", "something", "-arg3", "123" });
		assertTrue(consoleApp.executed);
	}
	
	@Test
	public void booleanOnlyOptionValueType() throws ParseException {
		final OptionId<Boolean> ARG1_OPTION_ID = OptionId.create("arg1", OptionValueType.BOOLEAN);
		final OptionId<Boolean> ARG2_OPTION_ID = OptionId.create("arg2", OptionValueType.BOOLEAN);
		final OptionId<Boolean> ARG3_OPTION_ID = OptionId.create("arg3", OptionValueType.BOOLEAN);
		final OptionId<Boolean> ARG4_OPTION_ID = OptionId.create("arg4", OptionValueType.ON_OFF);
		final OptionId<Boolean> ARG5_OPTION_ID = OptionId.create("arg5", OptionValueType.ON_OFF);
		
		ConsoleApplicationBaseMock consoleApp = new ConsoleApplicationBaseMock(new CallBack(){
			public ICompoundOptionDescriptor createRoot() {
				SequencedListOptionDescriptor options = new SequencedListOptionDescriptor();
				options.add(Option.createOption(ARG1_OPTION_ID, "description1", "value1"));
				options.add(Option.createOption(ARG2_OPTION_ID, "description2", "value2", true));
				options.add(Option.createOption(ARG3_OPTION_ID, "description3", "value3", false));
				options.add(Option.createOnOffOption(ARG4_OPTION_ID, "description4"));
				options.add(Option.createOnOffOption(ARG5_OPTION_ID, "description5"));
				
				return options;
			}
			
			public void check(ICommandLine commandLine) {
				//all id with regular order
				Collection<OptionId<?>> optionIds = new ArrayList<OptionId<?>>();
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				optionIds.add(ARG4_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				optionIds.add(ARG5_OPTION_ID);
				OptionId<?> selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG1_OPTION_ID, selected);
				
				//all id with different order
				optionIds.clear();
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG2_OPTION_ID, selected);
				
				
				//only one id
				optionIds.clear();
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG3_OPTION_ID, selected);
				
				//duplicated optionid
				optionIds.clear();
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG2_OPTION_ID);
				optionIds.add(ARG1_OPTION_ID);
				optionIds.add(ARG3_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertEquals(ARG1_OPTION_ID, selected);
				
				// not registered id
				optionIds.clear();
				optionIds.add(ARG4_OPTION_ID);
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertNull(selected);
				
				optionIds.clear();
				selected = OptionHelper.findSelectedOption(optionIds, commandLine);
				assertNull(selected);
				
				try {
					selected = OptionHelper.findSelectedOption(null, commandLine);
					fail();
				} catch (NullPointerException e) {
					assertNotNull(e);
				}
				
				
				Collection<OptionId<Boolean>> trueOptions =
						OptionHelper.findTrueOption(Collections.singleton(ARG1_OPTION_ID), commandLine);
				
				assertNotNull(trueOptions);
				assertEquals(1, trueOptions.size());
				assertEquals(ARG1_OPTION_ID, trueOptions.iterator().next());
			}
		});
		consoleApp.parseAndExecute(new String[] { "-arg1", "false", "-arg4" });
		assertTrue(consoleApp.executed);
	}
	
	
	
	
	private interface CallBack{
		ICompoundOptionDescriptor createRoot();
		void check(ICommandLine commandLine);
	}
	
	private static class ConsoleApplicationBaseMock extends ConsoleApplicationBase implements
			IConsoleApplicationDescriptor {
		boolean	executed		= false;
		final IOptionDescriptorTree tree;
		final CallBack callBack;
		
		private ConsoleApplicationBaseMock(CallBack callBack) throws InvalidOptionDescriptorException{
			this.callBack = callBack;
			tree = new OptionDescriptorTreeImpl(callBack.createRoot());
		}
		
		@Override
		protected void parseAndExecute(String[] args) throws ParseException {
			super.parseAndExecute(args);
		}

		@Override
		protected void execute(ICommandLine commandLine) {
			executed = true;
			callBack.check(commandLine);
		}
		
		@Override
		protected IConsoleApplicationDescriptor getDescriptor() {
			return this;
		}

		public String getLongDescription() {
			return "longDesc";
		}

		public String getName() {
			return "testName";
		}

		public IOptionDescriptorTree getOptions() {
			return tree;
		}

		public String getShortDescription() {
			return "shortName";
		}
	}
	

}
