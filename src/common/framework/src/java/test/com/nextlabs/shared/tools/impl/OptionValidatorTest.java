package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


/**
 * @author hchan
 * @date Mar 28, 2007
 */
public class OptionValidatorTest extends OptionValidatorTestShared implements ErrorMessage{
	@Test @Override
	public void noOptionsOneArgWithoutValue() {
		root = new SimpleCompoundOptionDescriptor(null);
		List<String> expected = new ArrayList<String>();
		expected.add(UNKNOWN_ARGUMENTS.replace(KEY_CMD_INDICATOR, "a"));
		assertError(new String[] { "-a" }, expected);
	}
	
	@Test @Override
	public void noOptionsOneArgWithValue() {
		root = new SimpleCompoundOptionDescriptor(null);
		List<String> expected = new ArrayList<String>();
		expected.add(UNKNOWN_ARGUMENTS.replace(KEY_CMD_INDICATOR, "a"));
		assertError(new String[] { "-a", "123" }, expected);
	}
	
	@Test @Override
	public void oneRequiredOptionWithoutValueNoArgs() {
		Option option = createOption("a", true, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] {}, expected);
	}

	@Test @Override
	public void oneRequiredOptionWithValueNoArgs() {
		Option option = createOption("a", true, true, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] {}, expected);
	}

	@Test @Override
	public void oneRequiredOptionWithoutValueOneArgWithValue1() {
		Option option = createOption("a", true, false, 0);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(NO_VALUES.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123abc" }, expected);
	}

	@Test
	public void multiOptionDidNotMeetMin() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(1, 2);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		List<String> expected = new ArrayList<String>();
		expected.add(REQUIRE_SELECT_MIN.replace(KEY_MIN, "1").replace(
				KEY_NAME, "a,b,c,"));
		assertError(new String[] {}, expected);
	}
	
	@Test
	public void multiOptionMeetMin() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(1, 2);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		assertNoError(new String[] {"-a"});
	}
	
	@Test
	public void multiOptionDidNotMeetMin2() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(2, 3);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		List<String> expected = new ArrayList<String>();
		expected.add(REQUIRE_SELECT_MIN.replace(KEY_MIN, "2").replace(
				KEY_NAME, "a,b,c,"));
		assertError(new String[] {}, expected);
	}
	
	@Test
	public void multiOptionDidNotMeetMin2b() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(2, 3);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		List<String> expected = new ArrayList<String>();
		expected.add(REQUIRE_SELECT_MIN.replace(KEY_MIN, "2").replace(
				KEY_NAME, "[a],b,c,"));
		assertError(new String[] {"-a"}, expected);
	}
	
	@Test
	public void multiOptionMeetMin2() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(2, 3);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		assertNoError(new String[] {"-b", "-c"});
	}
	
	@Test
	public void multiOptionMeetMin2b() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(2, 3);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		super.root = root;
		
		assertNoError(new String[] {"-a", "-b", "-c"});
	}
	
	@Test
	public void multiOptionReachMax() {
		MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(1, 2);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		root.add(createOption("d", true, false, 0));
		root.add(createOption("e", true, false, 0));
		super.root = root;
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_SELECTED.replace(KEY_MAX, "2").replace(
				KEY_NAME, "-b,-c,-d"));
		assertError(new String[] {"-d", "-b", "-c"}, expected);
	}
	
	@Test
	public void oneOptionalMultiRequiredValuesExtra() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "456", "789", "962"}, expected);
	}
	
	@Test
	public void oneOptionalMultiRequiredValuesSameExtra() {
		Option option = createOption("a", true, true, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "123", "123", "123"}, expected);
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesExtra() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "456", "789", "962"}, expected);
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesExtra2() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);

		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "456", "789", "-a", "962"}, expected);
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesSameExtra() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "123", "123", "123"}, expected);
	}
	
	@Test
	public void oneOptionalMultiOptionalValuesSameExtra2() {
		Option option = createOption("a", true, false, 3);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_MAX, ""+3)
				.replace(KEY_VALUE, ""+4)
				.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] { "-a", "123", "123", "123", "-a", "123"}, expected);
	}
	
	
	@Test
	public void nonRequiredSequenceWithRequiredOption() {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		root.setRequired(false);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		root.add(createOption("d", true, false, 0));
		root.add(createOption("e", false, false, 0));
		super.root = root;
		
		assertNoError(new String[]{});
		
		List<String> expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] {"-d", "-b", "-c"}, expected);
		
		assertNoError(new String[]{"-d", "-b", "-c", "-a"});
		
		assertNoError(new String[]{"-d", "-b", "-c", "-a", "-e"});
		
		expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "b"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "c"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "d"));
		assertError(new String[] {"-e"}, expected);
	}
	
	@Test
	public void requiredSequenceWithRequiredOption() {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		//default require is true
		//root.setRequired(true);
		root.add(createOption("a", true, false, 0));
		root.add(createOption("b", true, false, 0));
		root.add(createOption("c", true, false, 0));
		root.add(createOption("d", true, false, 0));
		root.add(createOption("e", false, false, 0));
		super.root = root;
		
		List<String> expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "b"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "c"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "d"));
		assertError(new String[]{},expected);
		
		expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		assertError(new String[] {"-d", "-b", "-c"}, expected);
		
		assertNoError(new String[]{"-d", "-b", "-c", "-a"});
		
		assertNoError(new String[]{"-d", "-b", "-c", "-a", "-e"});
		
		expected = new ArrayList<String>();
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "a"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "b"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "c"));
		expected.add(OPTION_REQUIRED.replace(KEY_OPTION_ID, "d"));
		assertError(new String[] {"-e"}, expected);
	}
	
	@Test
	public void unknownOption() {
		Option option = createOption("a", false, false, 1);
		root = new SimpleCompoundOptionDescriptor(option);
		
		List<String> expected = new ArrayList<String>();
		expected.add(UNKNOWN_ARGUMENTS.replace(KEY_CMD_INDICATOR, "b"));
		assertError(new String[] {"-b"}, expected);
	}
}
