package com.nextlabs.shared.tools.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;

/**
 * Test Option
 *
 * @author hchan
 * @date Apr 6, 2007
 */
public class OptionTest {
	//all good default value
	private Set<String> commandLineIndicators;
	private Boolean defaultValue; 
	private String description; 
	private OptionId<Boolean> optionId; 
	private String valueLabel;
	private boolean required;
	private boolean valueRequired;
	private int numPossibleValues;
	
	@Before
	public void init(){
		Option.reset();
		commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("a");
		defaultValue = null;
		description = "description";
		optionId = OptionId.create("a", OptionValueType.ON_OFF);
		valueLabel = "value";
		required = false;
		valueRequired = false;
		numPossibleValues = 0;
	}
	
	@Test public void goodOption(){
		try {
			Option<Boolean> option = new Option<Boolean>(
					optionId,
					commandLineIndicators,
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			assertNotNull(option);
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		}
	}
	
	@Test public void duplicatedOptionId(){
		goodOption();
		try {
			Set<String> commandLineIndicators2 = new TreeSet<String>();
			commandLineIndicators2.add("b");
			new Option<Boolean>(
					optionId,
					commandLineIndicators2,
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with duplicated OptionId(");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
	}
	
	@Test public void duplicatedCommandLineIndicators(){
		goodOption();
		try {
			Set<String> commandLineIndicators2 = new TreeSet<String>();
			commandLineIndicators2.add("a");
			
			OptionId<Boolean> optionId2 = OptionId.create("b", OptionValueType.ON_OFF);
			new Option<Boolean>(
					optionId2,
					commandLineIndicators2,
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with duplicated CommandLineIndicators2(");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
	}
	
	
	@Test public void emptyCommandLineIndicators(){
		try {
			new Option<Boolean>(
					optionId,
					null,
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with null commandLineIndicators");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
		
		try {
			new Option<Boolean>(
					optionId,
					new TreeSet<String>(),
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with null commandLineIndicators");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
		
		try {
			//TODO should I fix this?
			new Option<Boolean>(
					optionId,
					Collections.singleton(""),
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		}
	}
	
	@Test public void nullDescription(){
		try {
			new Option<Boolean>(
					optionId,
					commandLineIndicators,
					null,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with null Description");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
		
	}
	
	@Test public void nullOptionId(){
		try {
			new Option<Object>(
					null,
					commandLineIndicators,
					description,
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues );
			fail("should not be able to create Option with null OptionId");
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
	}
	
	@Test public void requriedValue(){
		try {
			new Option<Integer>(
					OptionId.create("a", OptionValueType.INTEGER),
					commandLineIndicators,
					description,
					required,
					valueLabel,
					123,
					true,
					0 );
			fail();
		} catch (InvalidOptionDescriptorException e) {
			assertNotNull(e.toString());
		}
		
		try {
			Option<Integer> option = new Option<Integer>(
					OptionId.create("a", OptionValueType.INTEGER),
					commandLineIndicators,
					description,
					required,
					valueLabel,
					123,
					true,
					1 );
			assertNotNull(option);
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		}
	}
	
	/**
	 * doesn't fit after using generic
	 */
//	@Test public void requriedValueTypeOk(){
//		try {
//			Option option = new Option(commandLineIndicators, "234", description, optionId, valueLabel, OptionValueType.INTEGER,
//					required, true, 1 );
//
//			new Option<Integer>(
//					OptionId.create("a", OptionValueType.INTEGER);,
//					commandLineIndicators,
//					description,
//					required,
//					valueLabel,
//					"234",
//					true,
//					1 );
//			
//			assertNotNull(option);
//		} catch (InvalidOptionDescriptorException e) {
//			fail(e.toString());
//		}
//	}
	
	/**
	 * doesn't fit after using generic
	 */
//	@Test public void requriedValueTypeErr(){
//		try {
//			new Option(commandLineIndicators, "foo", description, optionId, valueLabel, OptionValueType.INTEGER,
//					required, true, 1 );
//			
//			new Option<Integer>(
//					OptionId.create("a", OptionValueType.INTEGER),
//					commandLineIndicators,
//					description,
//					required,
//					valueLabel,
//					"foo",
//					true,
//					1 );
//			fail();
//		} catch (InvalidOptionDescriptorException e) {
//			assertNotNull(e.toString());
//		}
//	}

}
