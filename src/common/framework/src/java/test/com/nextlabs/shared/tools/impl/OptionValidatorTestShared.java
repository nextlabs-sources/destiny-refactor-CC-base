package com.nextlabs.shared.tools.impl;


import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import com.nextlabs.shared.tools.OptionValueType;

/**
 *
 * @author hchan
 * @date Apr 13, 2007
 */
public abstract class OptionValidatorTestShared extends OptionValidatorTestBase implements ErrorMessage{
	@Test
	public void noOptionsNoArgs() {
		root = new SimpleCompoundOptionDescriptor(null);
		
		assertNoError(new String[] {});
	}
	
	@Test
	public void oneRequiredOptionWithRequiredValueOneArgWithValue() {
		Option option = createOption("a", true, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		List<String> expected = Arrays.asList(INVALID_VALUE_TYPE
				.replace(KEY_OPTION_ID, "a")
				.replace(KEY_VALUE, "abc")
				.replace(KEY_VALUE_TYPE, "INTEGER"));
		
		assertError(new String[] { "-a", "abc" }, expected);
	}
	
	@Test
	public abstract void oneRequiredOptionWithoutValueNoArgs();

	@Test
	public abstract void oneRequiredOptionWithValueNoArgs();
	
	@Test
	public void oneOptionalOptionNoArgs() {
		Option option = createOption("a", false, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] {});
	}

	@Test
	public abstract void noOptionsOneArgWithoutValue();

	@Test
	public abstract void noOptionsOneArgWithValue();

	@Test
	public void oneRequiredOptionWithoutValueOneArgWithoutValue() {
		Option option = createOption("a", true, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a" });
	}
	
	@Test
	public abstract void oneRequiredOptionWithoutValueOneArgWithValue1();
	
	@Test
	public void oneRequiredOptionWithoutValueOneArgWithValue2() {
		Option option = createOption("a", true, false, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123" });
	}

	@Test
	public void oneRequiredOptionWithRequiredValueOneArgWithoutValue() {
		Option option = createOption("a", true, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = Arrays.asList(VALUE_REQUIRED
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a" }, expected);
	}

	@Test
	public void oneRequiredOptionWithRequiredValueOneArgWithValue2() {
		Option option = createOption("a", true, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123" });
	}

	@Test
	public void oneRequiredOptionWithOptionalValueOneArgWithoutValue() {
		Option option = createOption("a", true, false, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a" });
	}

	@Test
	public void oneRequiredOptionWithOptionalValueOneArgWithValue() {
		Option option = createOption("a", true, false, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123" });
	}

	@Test
	public void oneOptionalOptionOneArg() {
		Option option = createOption("a", false, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a" });
	}

	@Test
	public void oneOptionalOptionWithoutValueOneArgWithValue() {
		Option option = createOption("a", false, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123" });
	}

	@Test
	public void oneOptionalOptionWithoutValueTwoSameArg() {
		Option option = createOption("a", false, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		//TODO how to catch this error?
		assertNoError(new String[] { "-a", "-a" });
	}
	
	@Test
	public void oneOptionalMultiRequiredValues() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123", "456", "678" });
	}
	
	
	@Test
	public void oneOptionalMultiRequiredValues2() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123", "456", "-a", "678" });
	}
	
	@Test
	public void oneOptionalMultiRequiredValuesLess() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123", "456", });
	}
	
	@Test
	public void oneOptionalMultiRequiredValuesWrongType() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = Arrays.asList(INVALID_VALUE_TYPE
				.replace(KEY_OPTION_ID, "a")
				.replace(KEY_VALUE, "abf")
				.replace(KEY_VALUE_TYPE, OptionValueType.INTEGER.toString()));
		assertError(new String[] { "-a", "abf", "456", }, expected);
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesNone() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", });
	}
	
	@Test
	public void oneOptionalMultiOptionalValues() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123", "456", "678" });
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesLess() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "123", "456", });
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesWrongType() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = Arrays.asList(INVALID_VALUE_TYPE
				.replace(KEY_OPTION_ID, "a")
				.replace(KEY_VALUE, "abf")
				.replace(KEY_VALUE_TYPE, OptionValueType.INTEGER.toString()));
		assertError(new String[] { "-a", "abf", "456", }, expected);
	}
	
	@Test @Ignore
	public void deltaOption() {
		Option option1 = createOption("a", false, false, 0);
		Option option2 = createOption("b", false, false, 0);
		Option option3 = createOption("c", false, false, 0);
		UniqueChoiceOptionDescriptor root = new UniqueChoiceOptionDescriptor();
		SequencedListOptionDescriptor left = new SequencedListOptionDescriptor();
		left.add(option1);
		left.add(option2);
		SequencedListOptionDescriptor right = new SequencedListOptionDescriptor();
		right.add(option1);
		right.add(option3);
		root.add(left);
		root.add(right);
		super.root = root;
		
		List<String> expected = Arrays.asList("123");
		expected.add("123");
		assertError(new String[] { "-a", "-b" }, expected);
		assertNoError(new String[] { "-a", "-b" });
		
		//TODO how to solve this issue?
		assertNoError(new String[] { "-a", "-c" });
		
		assertError(new String[] { "-a", "-b", "-c" }, expected);
		assertError(new String[] { "-a", "-b", "-b" }, expected);
		
	}

	@Test
	public void oneOptionalOptionWithoutValueTwoSameArg2() {
		Set<String> commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("a");
		commandLineIndicators.add("b");
		Option option = createOption(commandLineIndicators, false, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a" });
	}

	@Test
	public void oneOptionalOptionWithoutValueTwoSameArg3() {
		Set<String> commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("a");
		commandLineIndicators.add("b");
		Option option = createOption(commandLineIndicators, false, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		assertNoError(new String[] { "-a", "-b" });
	}
}
