/*
 * Created on Sep 7, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.OptionTrigger.Action;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/impl/OptionTriggerTest.java#1 $
 */

public class OptionTriggerTest extends OptionValidatorTestBase implements ErrorMessage{
	@Before
	public void cleanup(){
		OptionTrigger.reset();
	}
	
	public void createOptionSet1(){
		Option optionA = createOption("a", false, false, 0);
		Option optionB = createOption("b", false, true, 1);
		
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		root.setRequired(true);
		root.add(optionA);
		root.add(optionB);
		
		OptionTrigger.add(ConditionEnum.IF_FOUND, optionA,
				Action.MARK_AS_REQUIRED, optionB.getParent());
		this.root = root;
	}
	
	@Test
	public void hNoOption() {
		createOptionSet1();
		assertNoError(new String[] {});
	}
	
	@Test
	public void aTriggerBFailed() {
		createOptionSet1();
		assertError(new String[] { "-a" }, 
				Arrays.asList(new String[] { OPTION_REQUIRED.replace(KEY_OPTION_ID, "b") }));
	}
	
	@Test
	public void justBMissValue() {
		createOptionSet1();
		assertError(new String[] { "-b" }, Arrays.asList(new String[] { VALUE_REQUIRED.replace(
				KEY_OPTION_ID, "b") }));
	}
	
	@Test
	public void justB() {
		createOptionSet1();
		assertNoError(new String[] { "-b", "123"});
	}
	
	@Test
	public void aTriggerB() {
		createOptionSet1();
		assertNoError(new String[] { "-b", "123", "-a"});
	}
	
	
	public void createOptionSet2() throws InvalidOptionDescriptorException{
		Option optionApple = createOption("a", false, false, 0);
		Option optionOrange = createOption("o", false, false, 0);
			
		Object defaultValue = null;
		String optionId = "b";
		String valueLabel = optionId;

		Option optionB = new Option(
				OptionId.create(optionId, OptionValueType.CUSTOM_LIST),	// OptionId<T> optionId,
				Collections.singleton(optionId),	// Collection<String> commandLineIndicators,
				"",				// String description,
				false,			// boolean required,
				valueLabel,		// String valueLabel,
				defaultValue,	// T defaultValue, 
				true, 			// boolean valueRequired,
				2);				// int numPossibleValues
		
		OptionValueType.CUSTOM_LIST.addCustomValue(optionB.getOptionId(), "apple");
		OptionValueType.CUSTOM_LIST.addCustomValue(optionB.getOptionId(), "orange");
		OptionValueType.CUSTOM_LIST.addCustomValue(optionB.getOptionId(), "lemon");
		
		
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		root.setRequired(true);
		root.add(optionApple);
		root.add(optionOrange);
		root.add(optionB);
		
		OptionTrigger.add(ConditionEnum.IF_FOUND, optionB, "apple",
				Action.MARK_AS_REQUIRED, optionApple.getParent());
		OptionTrigger.add(ConditionEnum.IF_FOUND, optionB, "orange",
				Action.MARK_AS_REQUIRED, optionOrange.getParent());
		this.root = root;
	}
	
	@Test
	public void set2Nothing() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] { });
	}
	
	@Test
	public void set2justApple() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-a" });
	}
	
	@Test
	public void triggerMissingApple() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertError(new String[] {"-b", "apple" }, 
				Arrays.asList(new String[] { OPTION_REQUIRED.replace(KEY_OPTION_ID, "a") }));
	}
	
	@Test
	public void triggerApple() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-b", "apple", "-a" });
	}
	
	@Test
	public void triggerMissingOrange() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertError(new String[] {"-b", "orange" }, 
				Arrays.asList(new String[] { OPTION_REQUIRED.replace(KEY_OPTION_ID, "o") }));
	}
	
	@Test
	public void triggerOrange() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-b", "orange", "-o" });
	}
	
	@Test
	public void triggerOrangeExtraApple() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-b", "orange", "-o", "-a" });
	}
	
	@Test
	public void triggerLemon() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-b", "lemon", });
	}
	
	@Test
	public void triggerTwoThingsButMissApple() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertError(new String[] { "-o", "-b", "orange", "apple" }, 
				Arrays.asList(new String[] { OPTION_REQUIRED.replace(KEY_OPTION_ID, "a") }));
	}
	
	@Test
	public void triggerTwoThings() throws InvalidOptionDescriptorException {
		createOptionSet2();
		assertNoError(new String[] {"-o", "-b", "orange", "apple", "-a" });
	}
}
