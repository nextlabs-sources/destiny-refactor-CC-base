package com.nextlabs.shared.tools;

import static org.junit.Assert.*;

import java.io.File;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * test Entity Export
 *
 * @author hchan
 * @date Apr 10, 2007
 */
public class EntityExportTest {
	/**
	 * Comment for <code>TEMP_FILE_STR</code>
	 */
	private static final String TEMP_FILE_STR = "c:\\temp\\export1.xml";

	@Before
	public void prepare(){
		OptionMod.reset();
	}
	
	@After
	public void cleanup(){
		File tempFile = new File(TEMP_FILE_STR);
		tempFile.delete();
	}
	
	@Test
	public void missingParameter1() throws Exception{
		String[] args = {};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test
	public void help() throws Exception{
		String[] args = {"-h"};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertFalse(mockEntityExport.isExecuteCalled());
		assertTrue(mockEntityExport.isHelpPrinted());
		assertEquals(mockEntityExport.getReturnCode(), MockEntityExport.STATUS_NO_ACTION );
	}
	
	@Test (expected=ParseException.class) //help can't combine with anything anymore
	public void helpAndSomething() throws Exception{
		String[] args = {"-h", "-s", "localhost", "-u", "admin", "-w", "password", "-l"};
	
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertTrue(mockEntityExport.isHelpPrinted());
		assertEquals(mockEntityExport.getReturnCode(), MockEntityExport.STATUS_NO_ACTION );
	}
	
	@Test (expected=ParseException.class)
	public void helpAndSomething2() throws Exception{
		String[] args = {"-h", "-s", "localhost", "-u", "admin", "-w", "password"};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test
	public void unknown(){
		String[] args = {"-H"};
		try {
			MockEntityExport mockEntityExport = new MockEntityExport();
			mockEntityExport.parseAndExecute(args);
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
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test
	public void basicFile() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-F", TEMP_FILE_STR};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertFalse(mockEntityExport.isHelpPrinted());
		assertEquals(MockEntityExport.STATUS_SUCCESS, mockEntityExport.getReturnCode());
		
	}
	
	//removed becuase SecureConsole cause dead lock
	@Test 
	@Ignore
	public void basicFileMIssed1() throws Exception{
		try {
			String[] args = {"-s", "localhost", "-u", "admin", "-F", TEMP_FILE_STR};
			new MockEntityExport().parseAndExecute(args);
			fail("not here");
		} catch (RuntimeException e) {
			assertNotNull(e);
		}
		
	}
	
	@Test (expected=ParseException.class)
	public void basicFileMIssed2() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "-F", TEMP_FILE_STR};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test
	public void basicList() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-l"};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertFalse(mockEntityExport.isHelpPrinted());
		assertEquals(mockEntityExport.getReturnCode(), MockEntityExport.STATUS_SUCCESS );
	}
	
	@Test  (expected=ParseException.class)
	public void fileAndList() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-l", "-F", TEMP_FILE_STR};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test  (expected=ParseException.class)
	public void fileAndListMissedPath() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-l", "-F"};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test  (expected=ParseException.class)
	public void fileMissedPath() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-F"};
		new MockEntityExport().parseAndExecute(args);
	}
	
	@Test
	public void list() throws Exception{
		String[] args = {"-s", "localhost", "-u", "admin", "-w", "password", "-l", "-d", "dd2d", "-d", "d3d", "-r", "oneFf", "-d", "DeeAgain"};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertFalse(mockEntityExport.isHelpPrinted());
		assertEquals(MockEntityExport.STATUS_SUCCESS, mockEntityExport.getReturnCode() );
	}
	
	@Test
	public void port() throws Exception{
		String[] args = {"-s", "localhost", "-p", "1234", "-u", "admin", "-w", "password", "-F", TEMP_FILE_STR};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertFalse(mockEntityExport.isHelpPrinted());
		assertEquals(MockEntityExport.STATUS_SUCCESS, mockEntityExport.getReturnCode() );
	}
	
	@Test (expected=ParseException.class)
	public void port2() throws Exception{
		String[] args = {"-s", "localhost", "-p", "abc", "-u", "admin", "-w", "password", "-F", TEMP_FILE_STR};
		MockEntityExport mockEntityExport = new MockEntityExport();
		mockEntityExport.parseAndExecute(args);
		assertTrue(mockEntityExport.isExecuteCalled());
		assertFalse(mockEntityExport.isHelpPrinted());
		assertEquals(MockEntityExport.STATUS_SUCCESS, mockEntityExport.getReturnCode() );
	}
	
	private class MockEntityExport extends EntityExport{
		private static final int STATUS_NO_ACTION = 104017;
		private boolean executeCalled;
		private boolean helpPrinted;
		private int returnCode = STATUS_NO_ACTION;
		
		public MockEntityExport() throws InvalidOptionDescriptorException {
			super();
		}
		
		@Override
		protected void execute(ICommandLine commandLine) {
			executeCalled = true;
			try {
				returnCode = doAction(commandLine);
			} catch (Exception e) {
				e.printStackTrace();
				returnCode = STATUS_ERR_UNKNOWN;
			}
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
		
		protected void printUsage() {
			helpPrinted = true;
		}

		
		@Override
		protected IPolicyEditorClient createPolicyEditorClient(String hostUrl, String username, String password) {
			IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
			HashMapConfiguration pfClientConfig = new HashMapConfiguration();
			pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, hostUrl);
			pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, username);
			pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, password);

			ComponentInfo compInfo = new ComponentInfo(
					PolicyEditorClient.COMP_INFO.getName(),
					MockPolicyEditorClient.class.getName(), 
					PolicyEditorClient.COMP_INFO.getInterfaceName(), 
					PolicyEditorClient.COMP_INFO.getLifestyleType(),
					pfClientConfig);
			IPolicyEditorClient client = (IPolicyEditorClient) compMgr.getComponent(compInfo);
			return client;
		}
	}
}
