/**
 * 
 */
package com.nextlabs.shared.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nextlabs.shared.tools.OptionValueType.OptionValueTypeList;
import com.nextlabs.shared.tools.impl.Option;

import static org.junit.Assert.*;
/**
 * TODO description
 *
 * @author hchan
 * @date Apr 5, 2007
 */
public class OptionValueTypesTest {
	private static final File TEST_ROOT = new File("C:\\temp\\");
	
	@BeforeClass
	public static void perpare(){
		if(!TEST_ROOT.exists()){
			if(! TEST_ROOT.mkdirs() ){
				throw new ExceptionInInitializerError("Can't create test root folder "
						+ TEST_ROOT.getAbsolutePath());
			}
		}
	}
	
	@Test
	public void booleanType(){
		checkBooleanType(OptionValueType.BOOLEAN);
	}
	
	@Test
	public void onOffType(){
		checkBooleanType(OptionValueType.ON_OFF);
	}

	private void checkBooleanType(final OptionValueType<Boolean> optionValueType) {
		assertFalse(optionValueType.isValid("String"));
		assertFalse(optionValueType.isValid(123));
		assertTrue(optionValueType.isValid(false));
		assertTrue(optionValueType.isValid(true));
		assertTrue(optionValueType.isValid("false"));
		assertTrue(optionValueType.isValid("FalSE"));
		assertTrue(optionValueType.isValid("yes"));
		assertTrue(optionValueType.isValid("no"));
		assertTrue(optionValueType.isValid("y"));
		assertTrue(optionValueType.isValid("n"));
		assertTrue(optionValueType.isValid('N'));
		assertTrue(optionValueType.isValid("nO"));
		assertTrue(optionValueType.isValid('T'));
		assertTrue(optionValueType.isValid('F'));
		assertTrue(optionValueType.isValid("F"));
		assertTrue(optionValueType.isValid('f'));
		assertTrue(optionValueType.isValid("f"));
		
		assertFalse(optionValueType.isValid(1));
		assertFalse(optionValueType.isValid(0));
		assertFalse(optionValueType.isValid(new Object()));
		assertTrue(optionValueType.isValid(new Boolean(true)));
	}
	
	@Test
	public void stringType(){
		final OptionValueType<String> optionValueType = OptionValueType.STRING;
		assertTrue(optionValueType.isValid("String"));
		assertTrue(optionValueType.isValid(123));
		assertTrue(optionValueType.isValid(false));
		assertTrue(optionValueType.isValid(true));
		assertTrue(optionValueType.isValid("false"));
		assertTrue(optionValueType.isValid("FalSE"));
		assertTrue(optionValueType.isValid("yes"));
		assertTrue(optionValueType.isValid("no"));
		assertTrue(optionValueType.isValid("y"));
		assertTrue(optionValueType.isValid("n"));
		assertTrue(optionValueType.isValid('N'));
		assertTrue(optionValueType.isValid(0));
		assertTrue(optionValueType.isValid(new Boolean(true)));
	}
	
	@Test
	public void integerType(){
		final OptionValueType<Integer> optionValueType = OptionValueType.INTEGER;
		assertFalse(optionValueType.isValid("String"));
		assertTrue(optionValueType.isValid(123));
		assertFalse(optionValueType.isValid(false));
		assertFalse(optionValueType.isValid(true));
		assertTrue(optionValueType.isValid("123"));
		assertFalse(optionValueType.isValid("123.031"));
		assertFalse(optionValueType.isValid(123.2));
		assertTrue(optionValueType.isValid(-12312));
		assertFalse(optionValueType.isValid("y"));
		assertFalse(optionValueType.isValid("n"));
		assertFalse(optionValueType.isValid('n'));
		assertFalse(optionValueType.isValid(10.0));
		assertFalse(optionValueType.isValid("9999999999999999999999999999999999999999999"));
		assertFalse(optionValueType.isValid(new Boolean(true)));
	}
	
	@Test
	public void floatType(){
		final OptionValueType<Float> optionValueType = OptionValueType.FLOAT;
		assertFalse(optionValueType.isValid("String"));
		assertTrue(optionValueType.isValid(123));
		assertFalse(optionValueType.isValid(false));
		assertFalse(optionValueType.isValid(true));
		assertTrue(optionValueType.isValid("123"));
		assertTrue(optionValueType.isValid("123.031"));
		assertTrue(optionValueType.isValid(123.2));
		assertTrue(optionValueType.isValid(-12312));
		assertFalse(optionValueType.isValid("y"));
		assertFalse(optionValueType.isValid("n"));
		assertFalse(optionValueType.isValid('n'));
		assertTrue(optionValueType.isValid(10.0));
		assertTrue(optionValueType.isValid("9999999999999999999999999999999999999999999"));
		assertFalse(optionValueType.isValid(new Boolean(true)));
	}
	
	@Test
	public void doubleType(){
		final OptionValueType<Double> optionValueType = OptionValueType.DOUBLE;
		assertFalse(optionValueType.isValid("String"));
		assertTrue(optionValueType.isValid(123));
		assertFalse(optionValueType.isValid(false));
		assertFalse(optionValueType.isValid(true));
		assertTrue(optionValueType.isValid("123"));
		assertTrue(optionValueType.isValid("123.031"));
		assertTrue(optionValueType.isValid(123.2));
		assertTrue(optionValueType.isValid(-12312));
		assertFalse(optionValueType.isValid("y"));
		assertFalse(optionValueType.isValid("n"));
		assertFalse(optionValueType.isValid('n'));
		assertTrue(optionValueType.isValid(10.0));
		assertTrue(optionValueType.isValid("9999999999999999999999999999999999999999999"));
		assertFalse(optionValueType.isValid(new Boolean(true)));
	}
	
	/**
	 * the test doesn't fit after using generic 
	 */
//	@Test(expected = InvalidOptionDescriptorException.class)
//	public void invalidAddCustomType() throws InvalidOptionDescriptorException{
//		final OptionValueType optionValueType = OptionValueType.INTEGER;
//		IOptionDescriptor option = createNoneOption("invalidAddCustomType");
//		optionValueType.addCustomValue(option, "abc");
//	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = InvalidOptionDescriptorException.class)
	public void invalidAddStringListType() throws InvalidOptionDescriptorException{
		final OptionValueTypeList optionValueType = OptionValueType.CASE_INSENSITIVE_STRING_LIST;
		IOptionDescriptor<Boolean> option =	Option.createOnOffOption(
				OptionId.create("invalidAddCustomType2", OptionValueType.ON_OFF), "");
		optionValueType.addCustomValue(option.getOptionId(), 123);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void customType() throws InvalidOptionDescriptorException{
		final OptionValueTypeList optionValueType = OptionValueType.CUSTOM_LIST;
		OptionId<Boolean> option = Option.createOnOffOption(
						OptionId.create("customType", OptionValueType.ON_OFF), "")
						.getOptionId();
		optionValueType.addCustomValue(option, "abc");
		optionValueType.addCustomValue(option, "123");
		optionValueType.addCustomValue(option, "e4t");
		optionValueType.addCustomValue(option, "8.9");
		optionValueType.addCustomValue(option, 61.3);
		optionValueType.addCustomValue(option, "apple");
		
		assertTrue(optionValueType.isValid(option, "abc"));
		assertTrue(optionValueType.isValid(option, "123"));
		assertFalse(optionValueType.isValid(option, 123));
		assertTrue(optionValueType.isValid(option, "e4t"));
		assertTrue(optionValueType.isValid(option, "apple"));
		assertFalse(optionValueType.isValid(option, "Apple"));
		assertFalse(optionValueType.isValid(option, "orange"));
		assertTrue(optionValueType.isValid(option, "8.9"));
		assertFalse(optionValueType.isValid(option, 8.9));
		assertTrue(optionValueType.isValid(option, 61.3));
		assertFalse(optionValueType.isValid(option, "61.3"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void ciStringList() throws InvalidOptionDescriptorException{
		final OptionValueTypeList optionValueType = OptionValueType.CASE_INSENSITIVE_STRING_LIST;
		OptionId<Boolean> option = Option.createOnOffOption(
				OptionId.create("customType2", OptionValueType.ON_OFF), "").getOptionId();
		optionValueType.addCustomValue(option, "abc");
		optionValueType.addCustomValue(option, "123");
		optionValueType.addCustomValue(option, "e4t");
		optionValueType.addCustomValue(option, "8.9");
		optionValueType.addCustomValue(option, "apple");
		
		assertTrue(optionValueType.isValid(option, "abc"));
		assertTrue(optionValueType.isValid(option, "123"));
		assertTrue(optionValueType.isValid(option, 123));
		assertTrue(optionValueType.isValid(option, "e4t"));
		assertTrue(optionValueType.isValid(option, "apple"));
		assertTrue(optionValueType.isValid(option, "Apple"));
		assertFalse(optionValueType.isValid(option, "orange"));
		assertTrue(optionValueType.isValid(option, "8.9"));
		assertTrue(optionValueType.isValid(option, 8.9));
		assertTrue(optionValueType.isValid(option, 8.9f));
		assertTrue(optionValueType.isValid(option, 8.9d));
		assertFalse(optionValueType.isValid(option, 61.3));
		assertFalse(optionValueType.isValid(option, "61.3"));
	}
	
	@Test
	public void fileType() throws IOException{
		File testExistFolder = getRandomTestFolder();
		assertTrue(testExistFolder.mkdir());
		testExistFolder.deleteOnExit();
		File testNonExistFolder = getRandomTestFolder();
		
		File testExistFile = getRandomTestFile(testExistFolder);
		FileWriter fileWriter = new FileWriter(testExistFile);
		fileWriter.write("this is a test file from " + this);
		fileWriter.close();
		testExistFile.deleteOnExit();
		
		File testNonExistFile = getRandomTestFile(testExistFolder);
		
		OptionValueType<?> optionValueType;
		
		optionValueType = OptionValueType.FOLDER;
		assertTrue(optionValueType.isValid(testExistFolder));
		assertTrue(optionValueType.isValid(testNonExistFolder));
		assertFalse(optionValueType.isValid(testExistFile));
		assertTrue(optionValueType.isValid(testNonExistFile));
		
		optionValueType = OptionValueType.EXIST_FOLDER;
		assertTrue(optionValueType.isValid(testExistFolder));
		assertFalse(optionValueType.isValid(testNonExistFolder));
		assertFalse(optionValueType.isValid(testExistFile));
		assertFalse(optionValueType.isValid(testNonExistFile));
		
		optionValueType = OptionValueType.NON_EXIST_FOLDER;
		assertFalse(optionValueType.isValid(testExistFolder));
		assertTrue(optionValueType.isValid(testNonExistFolder));
		assertFalse(optionValueType.isValid(testExistFile));
		assertTrue(optionValueType.isValid(testNonExistFile));
		
		optionValueType = OptionValueType.FILE;
		assertFalse(optionValueType.isValid(testExistFolder));
		assertTrue(optionValueType.isValid(testNonExistFolder));
		assertTrue(optionValueType.isValid(testExistFile));
		assertTrue(optionValueType.isValid(testNonExistFile));
		
		optionValueType = OptionValueType.EXIST_FILE;
		assertFalse(optionValueType.isValid(testExistFolder));
		assertFalse(optionValueType.isValid(testNonExistFolder));
		assertTrue(optionValueType.isValid(testExistFile));
		assertFalse(optionValueType.isValid(testNonExistFile));
		
		optionValueType = OptionValueType.NON_EXIST_FILE;
		assertFalse(optionValueType.isValid(testExistFolder));
		assertTrue(optionValueType.isValid(testNonExistFolder));
		assertFalse(optionValueType.isValid(testExistFile));
		assertTrue(optionValueType.isValid(testNonExistFile));
	}

	private File getRandomTestFolder() throws IOException {
		Random r = new Random();
		File testFolder;
		
		//only try 50 times
		for (int i = 0; i < 50; i++) {
			testFolder = new File(TEST_ROOT, "Folder" + r.nextInt());
			if (!testFolder.exists()) {
				return testFolder;
			}
		}
		throw new IOException("can't create random test folder");
	}
	
	private File getRandomTestFile(File folder) throws IOException {
		Random r = new Random();
		File testFolder;
		
		//only try 50 times
		for (int i = 0; i < 50; i++) {
			testFolder = new File(folder, "File" + r.nextInt());
			if (!testFolder.exists()) {
				return testFolder;
			}
		}
		throw new IOException("can't create random test file under folder "
				+ folder.getAbsolutePath());
	}
}
