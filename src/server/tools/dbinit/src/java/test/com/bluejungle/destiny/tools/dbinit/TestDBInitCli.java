/*
 * Created on Aug 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/TestDBInitCli.java#1 $
 */

public class TestDBInitCli {
	
	private MockCli cli;
	
	
	@Before
	public void cleanUp() throws InvalidOptionDescriptorException, ServiceException{
		OptionMod.reset();
		cli = new MockCli();
	}
	
	@Test
	public void noParameter() throws Exception{
		String[] args = {};
		cli.parseAndExecute(args);
		assertTrue(cli.usagePrinted);
		assertFalse(cli.executed);
	}
	
	@Test
	public void help() throws Exception{
		String[] args = {"-h"};
		cli.parseAndExecute(args);
		assertTrue(cli.usagePrinted);
		assertFalse(cli.executed);
	}
	
	@Test
	public void goodInstall() throws ParseException {
		String[] args = { "-install", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath" };
		cli.parseAndExecute(args);
		assertFalse(cli.usagePrinted);
		assertTrue(cli.executed);
	}
	
	@Test
	public void goodInstallQuite() throws ParseException {
		String[] args = { "-install", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath", "-quiet" };
		cli.parseAndExecute(args);
		assertFalse(cli.usagePrinted);
		assertTrue(cli.executed);
	}
	
	@Test( expected=ParseException.class)
	public void installMissConfig() throws ParseException {
		String[] args = { "-install", "-connection", "configuration.xml", "-libraryPath",
				"libraryPath" };
		cli.parseAndExecute(args);
	}
	
	@Test( expected=ParseException.class)
	public void installMissConnection() throws ParseException {
		String[] args = { "-install", "-config", "configPath", "-libraryPath", "libraryPath" };
		cli.parseAndExecute(args);
	}
	
	@Test( expected=ParseException.class)
	public void installMissLibrary() throws ParseException {
		String[] args = { "-install", "-config", "configPath", "-connection", "configuration.xml", };
		cli.parseAndExecute(args);
	}
	
	@Test( expected=ParseException.class)
	public void installMissAll() throws ParseException {
		String[] args = { "-install", };
		cli.parseAndExecute(args);
	}
	
	@Test( expected=ParseException.class)
	public void goodExtraSchema() throws ParseException {
		String[] args = { "-install", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath", "-schema", "scheam" };
		cli.parseAndExecute(args);
	}
	
	@Test
	public void goodUpgrade() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath", "-fromV", "1.6.0", "-toV", "2.0.0" };
		cli.parseAndExecute(args);
		assertFalse(cli.usagePrinted);
		assertTrue(cli.executed);
	}
	
	@Test( expected=ParseException.class)
	public void upgradeBadVersion() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath", "-fromV", "1.7.0", "-toV", "2.0.0" };
		cli.parseAndExecute(args);
	}
	
	@Test( expected=ParseException.class)
	public void upgradeMissConfig() throws ParseException {
		String[] args = { "-upgrade", "-connection", "configuration.xml", "-libraryPath",
				"libraryPath", "-fromV", "1.6.0", "-toV", "2.0.0" };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void upgradeMissConnection() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-libraryPath", "libraryPath",
				"-fromV", "1.6.0", "-toV", "2.0.0" };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void upgradeMissLibrary() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-fromV", "1.6.0", "-toV", "2.0.0" };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void upgradeMissVersion() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml", };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void upgradeToMissVersion() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-fromV", "1.6.0", };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void upgradeFromMissVersion() throws ParseException {
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-toV", "2.0.0" };
		cli.parseAndExecute(args);
	}
	
	@Test
	public void downgrade() throws ParseException {
		//TODO we can't catch this problem in the CLI yet
		String[] args = { "-upgrade", "-config", "configPath", "-connection", "configuration.xml",
				"-libraryPath", "libraryPath", "-fromV", "2.0.0", "-toV", "1.6.0" };
		cli.parseAndExecute(args);
	}
	
	
	@Test( expected=ParseException.class)
	public void upgradeMissAll() throws ParseException {
		String[] args = { "-upgrade", };
		cli.parseAndExecute(args);
	}
	
	@SuppressWarnings("unused")
	private class MockCli extends DBInit {
		boolean	usagePrinted	= false;
		boolean	executed		= false;
		
		/**
		 * @throws InvalidOptionDescriptorException
		 * @throws ServiceException
		 */
		public MockCli() throws InvalidOptionDescriptorException, ServiceException {
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
