package com.bluejungle.destiny.tools.enrollment;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.nextlabs.shared.tools.DynamicConsoleTester;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.CliTestBase;
import com.nextlabs.shared.tools.impl.ErrorMessageGenerator;
import com.nextlabs.shared.tools.impl.OptionMod;

/**
 * Test EnrollmentMgr cli
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class TestEnrollmentMgrCli extends CliTestBase{
	private MockEnrollmentMgrCli cli;
	
	@Before
	public void cleanUp() throws InvalidOptionDescriptorException, ServiceException {
		OptionMod.reset();
		cli =  new MockEnrollmentMgrCli();
	}
	
	protected void assertNoError(String[] args) {
		assertError(args, (List<String>)null);

	}
	
	private String getOptionRequiredErr(String s){
		return ErrorMessageGenerator.getOptionRequired(OptionId.create(s, OptionValueType.ON_OFF));
	}

	@Test
	public void noArgs() {
		String args[] = {};
		assertNoError(args);
		assertTrue(cli.usagePrinted);
	}

	@Test
	public void helpOnly() {
		String args[] = {};
		assertNoError(args);
		assertTrue(cli.usagePrinted);
	}

	@Test
	public void enrollErrNameOnly() {
		String args[] = { "-enroll" };
		List<String> errorMessages = new ArrayList<String>();
		errorMessages.add(getOptionRequiredErr("t"));
		errorMessages.add(getOptionRequiredErr("d"));
		errorMessages.add(getOptionRequiredErr("n"));
		errorMessages.add(getOptionRequiredErr("u"));
		assertError(args, errorMessages);
	}

	@Test
	public void enrollErrNameWithT() {
		String args[] = { "-enroll", "-t", "DIR" };
		List<String> errorMessages = new ArrayList<String>();
		errorMessages.add(getOptionRequiredErr("a"));
		errorMessages.add(getOptionRequiredErr("d"));
		errorMessages.add(getOptionRequiredErr("n"));
		errorMessages.add(getOptionRequiredErr("u"));
		assertError(args, errorMessages);
	}

	@Test
	public void enrollDIR() {
		String args[] = { "-enroll", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void enrollDirMissingAD() {
		String args[] = { "-enroll", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertError(args, getOptionRequiredErr("a"));
	}

	@Test
	public void enrollPortal() {
		String args[] = { "-enroll", "-t", "PORTAL", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void enrollPortalMissingAD() {
		String args[] = { "-enroll", "-t", "PORTAL", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertError(args, getOptionRequiredErr("a"));
	}

	@Test
	public void enrollLdif() {
		String args[] = { "-enroll", "-t", "LDIF", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void enrollLdifMissingAD() {
		String args[] = { "-enroll", "-t", "LDIF", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertNoError(args);
	}

	@Test
	public void enrollText() {
		String args[] = { "-enroll", "-t", "TEXT", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void enrollTextMissingAD() {
		String args[] = { "-enroll", "-t", "TEXT", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertNoError(args);
	}

	@Test
	public void enrollErrMissingD() {
		String args[] = { "-enroll", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-a", "aa", };
		assertError(args, getOptionRequiredErr("d"));
	}

	@Test
	public void enrollOk() {
		String args[] = { "-enroll", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-a", "aa", "-d", "dd" };
		assertNoError(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}

	@Test
	public void enrollWithWrongType() {
		String args[] = { "-enroll", "-t", "NEWTYPE", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-a", "aa", "-d", "dd" };
		assertError(args, ErrorMessageGenerator.getValueNotInList(OptionId.create("t", OptionValueType.ON_OFF), "NEWTYPE", "LDIF/DIR/PORTAL/TEXT"));
	}

	@Test
	public void enrollWithHelp() {
		String args[] = { "-enroll", "-t", "DIR", "-h", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-a", "aa", "-d", "dd" };
		assertError(args, "-h,-enroll,-t,-f,-a,-d,-n,-s,-p,-u,-w cannot be used together. You can only select a maximum of 1 group(s)");
	}

	@Test
	public void updateOk() {
		String args[] = { "-update", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "ff", "-a", "aa", "-d", "dd" };
		assertNoError(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}

	@Test
	public void syncErrExtraFields() {
		String args[] = { "-sync", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "ff", "-a", "aa", "-d", "dd" };
		assertError(args, "-t,-f,-a,-d,-sync cannot be used together. You can only select a maximum of 1 group(s)");
	}

	@Test
	public void syncErrExtraFields2() {
		String args[] = { "-sync", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd" };
		assertError(args, "-t,-sync cannot be used together. You can only select a maximum of 1 group(s)");
	}

	@Test
	public void syncOk() {
		String args[] = { "-sync", "-n", "domain.com", "-s", "server", "-p", "123", "-u", "user",
				"-w", "pwd" };
		assertNoError(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}

	@Test
	public void syncWrongPort() {
		String args[] = { "-sync", "-n", "domain.com", "-s", "server", "-p", "onetwothree", "-u",
				"user", "-w", "pwd" };
		assertError(args, ErrorMessageGenerator.getInvalidValueType(OptionId.create("p", OptionValueType.ON_OFF), "onetwothree",
				OptionValueType.INTEGER));
	}

	@Test
	public void syncWithHelp() {
		String args[] = { "-h", "-sync", "-n", "domain.com", "-s", "server", "-p", "123", "-u",
				"user", "-w", "pwd" };
		assertError(args, "-h,-sync,-n,-s,-p,-u,-w cannot be used together. You can only select a maximum of 1 group(s)");
	}

	@Test
	public void deleteOk() {
		String args[] = { "-delete", "-n", "domain.com", "-s", "server", "-p", "123", "-u", "user",
				"-w", "pwd" };
		assertNoError(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}

	@Test
	public void listOk() {
		String args[] = { "-list", "-s", "server", "-p", "123", "-u", "user", "-w", "pwd" };
		assertNoError(args);
		assertTrue(cli.executed);
		assertFalse(cli.usagePrinted);
	}

	@Test
	public void listEnrollErr() {
		String args[] = { "-list", "-enroll", "-s", "server", "-p", "123", "-u", "user", "-w",
				"pwd" };
		assertError(args, "-enroll,-list cannot be used together. You can only select a maximum of 1 group(s)");
	}

	@Test
	public void updateDIR() {
		String args[] = { "-update", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void updateDirMissingAD() {
		String args[] = { "-update", "-t", "DIR", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertError(args, getOptionRequiredErr("a"));
	}

	@Test
	public void updatePortal() {
		String args[] = { "-update", "-t", "PORTAL", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void updatePortalMissingAD() {
		String args[] = { "-update", "-t", "PORTAL", "-n", "domain.com", "-s", "server", "-p",
				"123", "-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertError(args, getOptionRequiredErr("a"));
	}

	@Test
	public void updateLdif() {
		String args[] = { "-update", "-t", "LDIF", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void updateLdifMissingAD() {
		String args[] = { "-update", "-t", "LDIF", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertNoError(args);
	}

	@Test
	public void updateText() {
		String args[] = { "-update", "-t", "TEXT", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd", "-a", "aa" };
		assertNoError(args);
	}

	@Test
	public void updateTextMissingAD() {
		String args[] = { "-update", "-t", "TEXT", "-n", "domain.com", "-s", "server", "-p", "123",
				"-u", "user", "-w", "pwd", "-f", "T", "-d", "dd" };
		assertNoError(args);
	}
	
	
	@Test
	@Ignore
	//this test take a lot of memory, keep the list as small as possible
	public void dynamic() throws InvalidOptionDescriptorException, ServiceException{
		List<String> switches = new ArrayList<String>();
		switches.add("-list");
		switches.add("-enroll");
		switches.add("-delete");
//		switches.add("-sync");
//		switches.add("-update");
////		switches.add("-t");
//		switches.add("-t"+DynamicConsoleTester.SPLITTER+"LDIF");
////		switches.add("-n");
//		switches.add("-n"+DynamicConsoleTester.SPLITTER+"nextlabs.com");
////		switches.add("-s");
//		switches.add("-s"+DynamicConsoleTester.SPLITTER+"server");
////		switches.add("-p");
//		switches.add("-p"+DynamicConsoleTester.SPLITTER+"123");
////		switches.add("-u");
//		switches.add("-u"+DynamicConsoleTester.SPLITTER+"user");
////		switches.add("-w");
//		switches.add("-w"+DynamicConsoleTester.SPLITTER+"pwd");
////		switches.add("-f");
//		switches.add("-f"+DynamicConsoleTester.SPLITTER+"ff");
////		switches.add("-a");
//		switches.add("-a"+DynamicConsoleTester.SPLITTER+"aa");
////		switches.add("-d");
//		switches.add("-d"+DynamicConsoleTester.SPLITTER+"dd");
//		switches.add("-h");
		
		/**
		 *  -list,-s,server,-p,123,-u,user,
			-delete,-n,nextlabs.com,-s,server,-p,123,-u,user,
			-sync,-n,nextlabs.com,-s,server,-p,123,-u,user,
			-list,-s,server,-p,123,-u,user,-w,pwd,
			-delete,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,
			-sync,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-a,aa,-d,dd,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-a,aa,-d,dd,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-a,aa,-d,dd,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-a,aa,-d,dd,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-f,ff,-a,aa,-d,dd,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-f,ff,-a,aa,-d,dd,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-f,ff,-a,aa,-d,dd,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-f,ff,-a,aa,-d,dd,
			-list,-s,server,-p,123,-u,user,-h,
			-delete,-n,nextlabs.com,-s,server,-p,123,-u,user,-h,
			-sync,-n,nextlabs.com,-s,server,-p,123,-u,user,-h,
			-list,-s,server,-p,123,-u,user,-w,pwd,-h,
			-delete,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-h,
			-sync,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-h,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-a,aa,-d,dd,-h,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-a,aa,-d,dd,-h,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-a,aa,-d,dd,-h,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-a,aa,-d,dd,-h,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-f,ff,-a,aa,-d,dd,-h,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-f,ff,-a,aa,-d,dd,-h,
			-enroll,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-f,ff,-a,aa,-d,dd,-h,
			-update,-t,LDIF,-n,nextlabs.com,-s,server,-p,123,-u,user,-w,pwd,-f,ff,-a,aa,-d,dd,-h,
			-h,

		 */
		try {
			DynamicConsoleTester dct = new DynamicConsoleTester(switches,
					MockEnrollmentMgrCli.class.getClass());
			//			for(List<String> argList : dct.getAllPossibleComboList()){
			//				cleanUp();
//				List<String> newArgList = new LinkedList<String>();
//				for(String arg : argList){
//					if(arg.contains(DynamicConsoleTester.SPLITTER)){
//						for(String splitted : arg.split(DynamicConsoleTester.SPLITTER)){
//							newArgList.add(splitted);
//						}
//					}else{
//						newArgList.add(arg);
//					}
//					
//				}
//				dct.run(cli, CollectionUtils.toStringArray(newArgList));
//			}
			for(String[] ss: dct.getSuccessComboList()){
				for(String s: ss){
					System.out.print(s+",");
				}
				System.out.print("\n");
			}
		}catch(Throwable t){
			t.printStackTrace();
		}	
	}

	private class MockEnrollmentMgrCli extends EnrollmentMgr {
		boolean	usagePrinted	= false;
		boolean	executed		= false;
		
		/**
		 * @throws InvalidOptionDescriptorException
		 * @throws ServiceException
		 */
		public MockEnrollmentMgrCli() throws InvalidOptionDescriptorException, ServiceException {
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

	@Override
	protected void cliParseAndExecute(String[] args) throws ParseException {
		cli.parseAndExecute(args);
		
	}
}
