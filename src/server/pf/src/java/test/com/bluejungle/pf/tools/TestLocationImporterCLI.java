/*
 * Created on Feb 26, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.tools;

import static org.junit.Assert.*;

import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/TestLocationImporterCLI.java#1 $
 */
public class TestLocationImporterCLI {
	private MockLocationImporterCLI cli;
	
	@Before
	public void cleanUp() throws InvalidOptionDescriptorException, ServiceException {
		OptionMod.reset();
		cli = new MockLocationImporterCLI();
	}
	
	@Test
	public void noArgs() throws Exception {
		String args[] = {};
		cli.parseAndExecute(args);
		assertTrue(cli.usagePrinted);
	}

	@Test
	public void helpOnly() throws Exception {
		String args[] = { "-h" };
		cli.parseAndExecute(args);
		assertTrue(cli.usagePrinted);
	}
	
	@Test
	public void allRequiredOptions() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
 
	@Test
	public void allRequiredOptionsWithInstanceOracle() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d oracle -i instance".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test (expected = ParseException.class)
	public void allRequiredOptionsWithOutInstanceOracle() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d oracle".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test
	public void allRequiredOptionsWithInstanceMSSQL() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d sqlserver -i instance".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test (expected = ParseException.class)
	public void allRequiredOptionsWithOutInstanceMSSQL() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d sqlserver".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test
	public void allRequiredOptionsWithInstanceDB2() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d db2 -i instance".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test (expected = ParseException.class)
	public void allRequiredOptionsWithOutInstanceDB2() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d db2".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test (expected = ParseException.class)
	public void allRequiredOptionsUnkownDB() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d noSuchDatabase".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test (expected = ParseException.class)
	public void allRequiredOptionsUnkownDBWithInstance () throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d noSuchDatabase - i instance".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test
	public void allRequiredOptionsWithOutInstance() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d postgres".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	@Test
	public void allRequiredOptionsWithNotRequiredInstance() throws Exception {
		String[] args = "-l filename -u username -w password -s host -p 1234 -d postgres -i instance".split("\\s");
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
	}
	
	
	@SuppressWarnings("unused")
	private class MockLocationImporterCLI extends LocationImporterCLI {
		boolean	usagePrinted	= false;
		boolean	executed		= false;
		
		/**
		 * @throws InvalidOptionDescriptorException
		 * @throws ServiceException
		 */
		public MockLocationImporterCLI() throws InvalidOptionDescriptorException, ServiceException {
			super();
		}

		@Override
		protected void execute( ICommandLine commandLine) {
			executed = true;
		}

		@Override
		protected void printUsage() {
			usagePrinted = true;
		}

		@Override
		public void parseAndExecute(String[] args) throws ParseException {
			super.parseAndExecute(args);
		}
	}
}
