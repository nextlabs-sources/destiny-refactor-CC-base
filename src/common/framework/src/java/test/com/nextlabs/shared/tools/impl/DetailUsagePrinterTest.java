package com.nextlabs.shared.tools.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;

import static org.junit.Assert.*;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public class DetailUsagePrinterTest {
	private MockConsoleApplicationDescriptor mockConsoleApplicationDescriptor;
	
	@Before
	public void setup(){
		Option.reset();
		mockConsoleApplicationDescriptor = new MockConsoleApplicationDescriptor();
		Set<String> commonLineIndicators = new TreeSet<String>();
		commonLineIndicators.add("t");
		commonLineIndicators.add("test");
		commonLineIndicators.add("foo");
		Boolean defaultValue = false; 
		String description = "this is the MockOptionDescriptor.getDescription\n"
			+ "Dump : As drafted, the legislation requires a troop withdrawal to begin within 120 "
			+ "days, with a nonbinding goal that calls for the combat troops to be gone within a "
			+ "year. The measure also includes a series of suggested goals for the Iraqi government "
			+ "to meet to provide for its own security, enhance democracy and distribute its oil "
			+ "wealth fairly — provisions designed to attract support from Nelson and Sen. Mark Pryor "
			+ "(news, bio, voting record) of Arkansas. "; 
		OptionId<Boolean> optionId = OptionId.create("m", OptionValueType.ON_OFF);
		String valueLabel = null;
		boolean required = false;
		boolean valueRequired = false;
		int numPossibleValues = 0;
		Option<Boolean> option;
		try {
			option = new Option<Boolean>(
					optionId, 
					commonLineIndicators, 
					description, 
					required,
					valueLabel,
					defaultValue,
					valueRequired,
					numPossibleValues);
			mockConsoleApplicationDescriptor.setRoot(new SimpleCompoundOptionDescriptor(option));
		} catch (InvalidOptionDescriptorException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void renderUsage1() {
		DetailUsagePrinter printer = new DetailUsagePrinter(80, "\n", "    ", true);
		printer.renderUsage(mockConsoleApplicationDescriptor);
		List<String> expected = new LinkedList<String>();

		expected.add("[-foo, -t, -test]\n" + "    this is the MockOptionDescriptor.getDescription\n"
				+ "    Dump : As drafted, the legislation requires a troop withdrawal to begin\n"
				+ "    within 120 days, with a nonbinding goal that calls for the combat troops to\n"
				+ "    be gone within a year. The measure also includes a series of suggested goals\n"
				+ "    for the Iraqi government to meet to provide for its own security, enhance\n"
				+ "    democracy and distribute its oil wealth fairly — provisions designed to\n"
				+ "    attract support from Nelson and Sen. Mark Pryor (news, bio, voting record)\n"
				+ "    of Arkansas. \n");
		assertEquals(expected, printer.getCache());
	}

	@Test
	public void renderUsage2() {
		DetailUsagePrinter printer = new DetailUsagePrinter(64, "\n", "         ", true);
		printer.renderUsage(mockConsoleApplicationDescriptor);

		List<String> expected = new LinkedList<String>();

		expected.add("[-foo, -t, -test]\n" + "         this is the MockOptionDescriptor.getDescription\n"
				+ "         Dump : As drafted, the legislation requires a troop\n"
				+ "         withdrawal to begin within 120 days, with a nonbinding\n"
				+ "         goal that calls for the combat troops to be gone within\n"
				+ "         a year. The measure also includes a series of suggested\n"
				+ "         goals for the Iraqi government to meet to provide for\n"
				+ "         its own security, enhance democracy and distribute its\n"
				+ "         oil wealth fairly — provisions designed to attract\n"
				+ "         support from Nelson and Sen. Mark Pryor (news, bio,\n"
				+ "         voting record) of Arkansas. \n");

		assertEquals(expected, printer.getCache());
	}

	private class MockConsoleApplicationDescriptor implements IConsoleApplicationDescriptor {
		private MockOptionDescriptorTree tree = new MockOptionDescriptorTree();
		public String getName() {
			return null;
		}
		public String getShortDescription() {
			return null;
		}
		public String getLongDescription() {
			return null;
		}
		public IOptionDescriptorTree getOptions() {
			return tree;
		}
		public void setRoot(ICompoundOptionDescriptor root){
			tree.setRoot(root);
		}
	}

	private class MockOptionDescriptorTree implements IOptionDescriptorTree {
		private ICompoundOptionDescriptor root;
		public void setRoot(ICompoundOptionDescriptor root){
			this.root = root;
		}
		public ICompoundOptionDescriptor getRootOption() {
			return root;
		}
		public IOptionDescriptor<?> getOption(String optionId) {
			return null;
		}
	}
}
