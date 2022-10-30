package com.bluejungle.destiny.tools.enrollment;

import static org.junit.Assert.*;

import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * Test EnrollmentMgr cli
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class TestPropertyMgrCli {
	private MockPropertyMgrCli cli;
	
	@Before
	public void cleanUp() throws InvalidOptionDescriptorException, ServiceException {
		OptionMod.reset();
		cli = new MockPropertyMgrCli();
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

	@Test(expected = ParseException.class)
	public void addErrNameOnly() throws Exception {
		String args[] = { "-add" };
		cli.parseAndExecute(args);
	}

	@Test(expected = ParseException.class)
	public void addErrNameWithT() throws Exception {
		String args[] = { "-add", " -t", "STRING" };
		cli.parseAndExecute(args);
	}

	@Test
	public void addOk() throws Exception {
		String args[] = { "-add", "-s", "server", "-p", "123", "-u", "user", "-w", "pwd", "-t",
				"STRING", "-l", "logicalname", "-i", "displayname", "-e", "HOST" };
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}
	
	@Test(expected = ParseException.class)
	public void addWrongType() throws Exception {
		String args[] = { "-add", "-s", "server", "-p", "123", "-u", "user", "-w", "pwd", "-t",
				"XXX", "-l", "logicalname", "-i", "displayname", "-e", "HOST" };
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}
	
	@Test(expected = ParseException.class)
	public void addWrongEntityType() throws Exception {
		String args[] = { "-add", "-s", "server", "-p", "123", "-u", "user", "-w", "pwd", "-t",
				"STRING", "-l", "logicalname", "-i", "displayname", "-e", "hosT" };
		cli.parseAndExecute(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}
	

	@SuppressWarnings("unused")
	private class MockPropertyMgrCli extends PropertyMgr {
		boolean	usagePrinted	= false;
		boolean	executed		= false;
		
		/**
		 * @throws InvalidOptionDescriptorException
		 * @throws ServiceException
		 */
		public MockPropertyMgrCli() throws InvalidOptionDescriptorException, ServiceException {
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
