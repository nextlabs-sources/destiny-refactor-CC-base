package com.bluejungle.framework.crypt;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * Test Encryptor CLI
 *
 * @author hchan
 * @date Jul 19, 2007
 */
public class TestEncryptor {
	@Before
	public void cleanUp(){
		OptionMod.reset();
	}
	
	@Test
	public void nothing() throws Exception{
		String[] args = { };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
		assertTrue(console.helpPrinted);
		assertFalse(console.executeCalled);
	}
	
	@Test
	public void help() throws Exception{
		String[] args = {"-h" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
		assertTrue(console.helpPrinted);
		assertFalse(console.executeCalled);
	}
	
	@Test (expected=ParseException.class)
	public void passwordShortIndicatorMissedValue() throws Exception{
		String[] args = {"-w" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
	}
	
	@Test
	public void passwordShortIndicator() throws Exception{
		String[] args = {"-w", "12345" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
		assertFalse(console.helpPrinted);
		assertTrue(console.executeCalled);
	}
	
	@Test (expected=ParseException.class)
	public void passwordLongIndicatorMissedValue() throws Exception{
		String[] args = {"-password" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
	}
	
	@Test
	public void passwordLongIndicator() throws Exception{
		String[] args = {"-password", "abcd123" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
		assertFalse(console.helpPrinted);
		assertTrue(console.executeCalled);
	}
	
	@Test (expected=ParseException.class)
	public void helpWithSomething() throws Exception{
		String[] args = {"-h", "-w", "12345" };
		MockEncryptor console = new MockEncryptor();
		console.parseAndExecute(args);
	}
	
	private class MockEncryptor extends Encryptor{
		private boolean executeCalled;
		private boolean helpPrinted;
		
		public MockEncryptor() throws InvalidOptionDescriptorException {
			super();
		}
		
		@Override
		protected void execute(ICommandLine commandLine) {
			if( commandLine.getParsedValues(CryptOptionDescriptorEnum.HELP_OPTION_ID) != null ){
				printUsage();
			}else{
				executeCalled = true;
			}
		}
		

		@Override
		protected void parseAndExecute(String[] args) throws ParseException {
			super.parseAndExecute(args);
		}

		@Override
		protected void printUsage() {
			helpPrinted = true;
		}
	}

}
