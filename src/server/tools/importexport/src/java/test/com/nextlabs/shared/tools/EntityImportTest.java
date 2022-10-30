package com.nextlabs.shared.tools;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * Test EntityImport
 *
 * @author hchan
 * @date Apr 17, 2007
 */
public class EntityImportTest {
	@Before
	public void cleanUp(){
		OptionMod.reset();
	}
	
	@Test
	public void missingParameter1() throws Exception{
		String[] args = {};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
	}
	
	@Test
	public void help() throws Exception{
		String[] args = {"-h"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertFalse(mockEntityImport.isExecuteCalled());
		assertTrue(mockEntityImport.isHelpPrinted());
		assertEquals(mockEntityImport.getReturnCode(), MockEntityImport.STATUS_NO_ACTION );
	}
	
	@Test (expected=ParseException.class)
	public void helpAndSomething() throws Exception{
		String[] args = {"-h", "-s", "localhost", "-u", "admin", "-w", "password"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
	}
	
	@Test (expected=ParseException.class) //help can't combine with anything anymore
	public void helpAndSomething2() throws Exception{
		String[] args = {"-h", "-s", "localhost", "-u", "admin", "-w", "password", "-F", "abc"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertTrue(mockEntityImport.isExecuteCalled());
		assertTrue(mockEntityImport.isHelpPrinted());
		assertEquals(mockEntityImport.getReturnCode(), MockEntityImport.STATUS_NO_ACTION );
	}
	
	@Test
	public void unknown(){
		String[] args = {"-H"};
		try {
			MockEntityImport mockEntityImport = new MockEntityImport();
			mockEntityImport.parseAndExecute(args);
			assertTrue(mockEntityImport.isExecuteCalled());
			fail();
		} catch (ParseException e) {
			assertEquals("Unrecognized option: -H", e.getMessage());
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		}
	}
	
	@Test(expected=ParseException.class)
	public void basicWithOutFilename() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password"};
		
		MockEntityImport mockEntityImport;
		mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
	}
	
	@Test
	public void basicFile() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-F", "doesNotExist.xml"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertTrue(mockEntityImport.isExecuteCalled());
		assertFalse(mockEntityImport.isHelpPrinted());
		assertEquals(MockEntityImport.STATUS_ERR_FILE, mockEntityImport.getReturnCode());
	}
	
	@Test
	public void basicFileWithCancelOnConflict() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-F", "doesNotExist.xml", "-x"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertTrue(mockEntityImport.isExecuteCalled());
		assertFalse(mockEntityImport.isHelpPrinted());
		assertEquals(mockEntityImport.getReturnCode(), MockEntityImport.STATUS_ERR_FILE );
	}
	
	// secure prompt causes infinite loop
	@Test @Ignore
	public void basicFileMIssed1() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-F", "c:\\temp\\export1.xml"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertTrue(mockEntityImport.isExecuteCalled());
		assertFalse(mockEntityImport.isHelpPrinted());
		
	}
	
	@Test (expected=ParseException.class)
	public void basicFileMIssed2() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "-F", "doesNotExist.xml"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		
	}
	
	@Test (expected=ParseException.class)
	public void basicListMissedFile() throws Exception{
		String[] args = {"-l"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
	}
	
	@Test  
	public void basicList() throws Exception{
		String[] args = {"-l", "-F", "doesNotExist.xml"};
		MockEntityImport mockEntityImport = new MockEntityImport();
		mockEntityImport.parseAndExecute(args);
		assertTrue(mockEntityImport.isExecuteCalled());
		assertFalse(mockEntityImport.isHelpPrinted());
		assertEquals(mockEntityImport.getReturnCode(), MockEntityImport.STATUS_ERR_FILE );
	}
	
	
	private class MockEntityImport extends EntityImport{
		private static final int STATUS_NO_ACTION = 104017;
		private boolean executeCalled;
		private boolean helpPrinted;
		private int returnCode = STATUS_NO_ACTION;
		
		
		public MockEntityImport() throws InvalidOptionDescriptorException {
			super();
		}

		@Override
		protected void execute(ICommandLine commandLine) {
			executeCalled = true;
			try {
				returnCode = doAction(commandLine);
			} catch (Exception e) {
				returnCode = STATUS_ERR_UNKNOWN;
			}
		}
		
		public void printUsage() {
			helpPrinted = true;
		}

		public final boolean isExecuteCalled() {
			return executeCalled;
		}
		
		public final boolean isHelpPrinted() {
			return helpPrinted;
		}

		public final int getReturnCode() {
			return returnCode;
		}


		@Override
		protected IPolicyEditorClient createPolicyEditorClient(String hostUrl, String username,
				String password) {
			return new MockPolicyEditorClient(hostUrl, username, password);
		}

	}
}
