package com.nextlabs.pf.destiny.importexport;

import java.io.File;

import com.nextlabs.pf.destiny.importexport.mapping.User;

import junit.framework.TestCase;

public class ImportFileParserTest extends TestCase {

	File testFile = new File("C:\\testImport.xml");

	//define importFile
/*	FileWriter testWrite = new FileWriter(testFile);
	testWrite.write(
			new String(
			"<ExportFile>\n" +
			"	<version>\"-1\"</version>\n" +
			"\n" +
			"	<ExportEntity name=\"testPolicy\">\n" +
			"		<type>POLICY</type>\n" +
			"		<pql>testPQL</pql>\n" +
			"	</ExportEntity>\n" +
			"\n" +
			"	<User>\n" +
			"		<name>john</name>\n" +
			"		<login>doe</login>\n" +
			"		<sid>123abc</sid>\n" +
			"		<id>42</id>\n" +
			"	</User>\n" +
			"</ExportFile>"));
	testWrite.close();
*/	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ImportFileParserTest.class);
		
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testParseFile() throws ImportException {
		ExportFile parsed = ImportFileParser.parseFile(testFile);
		assertNotNull(parsed);
		assertTrue(!parsed.getExportEntities().isEmpty());
		assertTrue(!parsed.getUsers().isEmpty());
//		System.out.println(parsed.getVersion());
		ExportEntity testEnt = (ExportEntity)parsed.getExportEntities().iterator().next();
		assertTrue(testEnt.getName().compareTo("testPolicy") == 0);
		assertTrue(testEnt.getPql().compareTo("testPQL") == 0);
		assertTrue(testEnt.getType().compareTo("POLICY") == 0);
		User testUser = (User)parsed.getUsers().iterator().next();
		assertTrue(testUser.getName().compareTo("john") == 0);
		assertTrue(testUser.getLogin().compareTo("doe") == 0);
		assertTrue(testUser.getSid().compareTo("123abc") == 0);
		assertTrue(testUser.getId() == 42);
	}
}
