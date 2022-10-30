/*
 * Created on May 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import static org.junit.Assert.*;

import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.ErrorMessageGenerator;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionMod;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/TestConsoleApplicationBase.java#1 $
 */

public class TestConsoleApplicationBase {
	private static final OptionId<Boolean> ARG1_OPTION_ID = OptionId.create("arg1", OptionValueType.ON_OFF);
	private static final OptionId<String> ARG2_OPTION_ID = OptionId.create("arg2", OptionValueType.CASE_INSENSITIVE_STRING_LIST);
	private static final OptionId<Integer> ARG3_OPTION_ID = OptionId.create("arg3", OptionValueType.INTEGER);

	private static final boolean ARG1_DEFAULT_VALUE = true;
	private static final String ARG2_DEFAULT_VALUE = null;
	
	static ConsoleApplicationBaseMock consoleApp;
	
	static PrintStreamMonitor out;
	static PrintStreamMonitor err;
	
	static PrintStream outOriginal;
	static PrintStream errOriginal;
	
	@BeforeClass
	public static void init() throws InvalidOptionDescriptorException{
		OptionMod.reset();
		consoleApp = new ConsoleApplicationBaseMock();

		outOriginal = System.out;
		out = new PrintStreamMonitor(System.out);
		System.setOut(out);
		
		errOriginal = System.err;
		err = new PrintStreamMonitor(System.err);
		System.setErr(err);
	}
	
	@AfterClass
	public static void rollback() {
		if (out != null) {
			out.close();
		}
		if (err != null) {
			err.close();
		}

		if (outOriginal != null) {
			System.setOut(outOriginal);
		}
		if (errOriginal != null) {
			System.setErr(errOriginal);
		}
		
	}
	
	static PrintStream printStream ;
	
	@Before
	public void setUp(){
		consoleApp.arg1Input = ARG1_DEFAULT_VALUE;
		consoleApp.arg2Input = ARG2_DEFAULT_VALUE;
		consoleApp.executed = false;
	}
	
	@After
	public void cleanUp(){
		out.clear();
		err.clear();
	}
	
	@Test
	public void noArgs() throws ParseException {
		consoleApp.parseAndExecute(new String[] {});
		assertFalse(consoleApp.executed);
		
		checkUsageScreen();
		
		assertEquals(0, err.getString().length());
	}
	
	@Test
	public void helpArg() throws ParseException {
		consoleApp.parseAndExecute(new String[] {"-h"});
		assertFalse(consoleApp.executed);
		
		checkUsageScreen();
		
		assertEquals(0, err.getString().length());
	}

	private void checkUsageScreen() {
		final String N = ConsoleDisplayHelper.NEWLINE;
		assertEquals("NAME" + N 
				+ "shortName" + N 
				+ N 
				+ "SYNOPSIS" + N 
				+ "testName [-arg1] [-arg2 <value2>]" + N 
				+ N 
				+ "OPTIONS" + N 
				+ "[-h]   display this page" + N 
				+ N 
				+ "[-arg1]" + N
				+ "       description1" + N 
				+ N 
				+ "[-arg2 <value2>]" + N 
				+ "       description2" + N 
				+ N
				+ "DESCRIPTION" + N 
				+ "longDesc" + N 
				+ N, out.getString());
	}
	
	@Test
	public void oneArg() throws ParseException {
		consoleApp.parseAndExecute(new String[] {"-arg1"});
		assertTrue(consoleApp.executed);
		
		assertEquals(0, out.getString().length());
		assertEquals(0, err.getString().length());
	}
	
	@Test
	public void unknwonArg() {
		try {
			consoleApp.parseAndExecute(new String[] {"-arg4"});
			fail();
		} catch (ParseException e) {
			assertNotNull(e);
			String errorMessage = ErrorMessageGenerator.getUnknownArguments("arg4");
			assertEquals(errorMessage, e.getMessage());
			
			assertEquals(0, err.getString().length());
			cleanUp();
			
			ConsoleApplicationBase.printException(e);
			
			assertEquals(0, out.getString().length());
			assertEquals(ParseException.class.getName() + ": " + errorMessage
					+ ConsoleDisplayHelper.NEWLINE, err.getString());
		}
	}
	
	@Test
	public void unknwonOptionId() throws ParseException {
		consoleApp.parseAndExecute(new String[] {"-arg1"});
	}
	
	
	/**
	 * boolean	usagePrinted	= false;
		
	 */
	
	private static class ConsoleApplicationBaseMock extends ConsoleApplicationBase implements
			IConsoleApplicationDescriptor {
		boolean	executed		= false;
		boolean arg1Input		= ARG1_DEFAULT_VALUE;
		String arg2Input		= ARG2_DEFAULT_VALUE;
		
		final IOptionDescriptorTree tree;
		
		private ConsoleApplicationBaseMock() throws InvalidOptionDescriptorException{
			SequencedListOptionDescriptor options = new SequencedListOptionDescriptor();
			options.add(Option.createOnOffOption(ARG1_OPTION_ID, "description1"));
			options.add(Option.createOption(ARG2_OPTION_ID, "description2", "value2", ARG2_DEFAULT_VALUE));
			
			tree = new OptionDescriptorTreeImpl(options);
		}
		
		@Override
		protected void execute(ICommandLine commandLine) {
			executed = true;
			assertEquals(arg1Input, getValue(commandLine, ARG1_OPTION_ID));
			assertEquals(arg2Input, getValue(commandLine, ARG2_OPTION_ID));
			
			try {
				consoleApp.getValues(commandLine, ARG3_OPTION_ID);
				fail();
			} catch (OptionDescriptorNotFoundException  e) {
				assertNotNull(e);
			}
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
