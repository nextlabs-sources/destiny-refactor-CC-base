/**
 * 
 */
package com.nextlabs.shared.tools.impl;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;

/**
 * TODO description
 *
 * @author hchan
 * @date Mar 30, 2007
 */
public class SynopisUsagePrinterTest {

	@Before
	public void reset(){
		Option.reset();
	}
	
	@Test
	public void oneOption(){
		MockConsoleApplicationDescriptor mockConsoleApplicationDescriptor = new MockConsoleApplicationDescriptor();
		mockConsoleApplicationDescriptor.setRoot(new SimpleCompoundOptionDescriptor(createOption("t", false)));
		SynopisUsagePrinter printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock [-t]", printer.getCache().get(0));
	}
	
	@Test
	public void oneRequiredOption(){
		MockConsoleApplicationDescriptor mockConsoleApplicationDescriptor = new MockConsoleApplicationDescriptor();
		mockConsoleApplicationDescriptor.setRoot(new SimpleCompoundOptionDescriptor(createOption("t", true)));
		SynopisUsagePrinter printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock -t", printer.getCache().get(0));
	}
	
	@Test
	public void oneSequence(){
		MockConsoleApplicationDescriptor mockConsoleApplicationDescriptor = new MockConsoleApplicationDescriptor();
		SequencedListOptionDescriptor sequenceOptions = new SequencedListOptionDescriptor();
		reset();
		sequenceOptions.add(createOption("a", false));
		sequenceOptions.add(createOption("b", false));
		sequenceOptions.add(createOption("c", true));
		sequenceOptions.setRequired(true);
		mockConsoleApplicationDescriptor.setRoot(sequenceOptions);
		SynopisUsagePrinter printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock [-a] [-b] -c", printer.getCache().get(0));
		
		reset();
		sequenceOptions = new SequencedListOptionDescriptor();
		sequenceOptions.add(createOption("a", false));
		sequenceOptions.add(createOption("b", false));
		sequenceOptions.setRequired(true);
		mockConsoleApplicationDescriptor.setRoot(sequenceOptions);
		printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock [-a] [-b]", printer.getCache().get(0));
		
		reset();
		sequenceOptions = new SequencedListOptionDescriptor();
		sequenceOptions.add(createOption("a", true));
		sequenceOptions.add(createOption("b", true));
		sequenceOptions.setRequired(true);
		mockConsoleApplicationDescriptor.setRoot(sequenceOptions);
		printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock -a -b", printer.getCache().get(0));
		
		reset();
		sequenceOptions = new SequencedListOptionDescriptor();
		sequenceOptions.add(createOption("a", true));
		sequenceOptions.add(createOption("b", true));
		sequenceOptions.setRequired(false);
		mockConsoleApplicationDescriptor.setRoot(sequenceOptions);
		printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock [-a -b]", printer.getCache().get(0));
		
		reset();
		sequenceOptions = new SequencedListOptionDescriptor();
		sequenceOptions.add(createOption("a", false));
		sequenceOptions.add(createOption("b", false));
		sequenceOptions.setRequired(false);
		mockConsoleApplicationDescriptor.setRoot(sequenceOptions);
		printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock [[-a] [-b]]", printer.getCache().get(0));
	}
	
	@Test
	public void helpIsAddedAuto() throws InvalidOptionDescriptorException{
		MockConsoleApplicationDescriptor mockConsoleApplicationDescriptor = new MockConsoleApplicationDescriptor();
		ICompoundOptionDescriptor root = new SimpleCompoundOptionDescriptor(createOption("t", true));
		root = OptionDescriptorTreeImpl.addHelpToRoot(root);
		mockConsoleApplicationDescriptor.setRoot(root);
		SynopisUsagePrinter printer = new SynopisUsagePrinter();
		printer.renderUsage(mockConsoleApplicationDescriptor);
		assertEquals("mock -t", printer.getCache().get(0));
	}
	
	public Option<Boolean> createOption(String cmdIndicator, boolean required){
		try {
			if(required){
				return Option.createOption(OptionId.create(cmdIndicator,
						OptionValueType.BOOLEAN), "", null);
			}else{
				return Option.createOnOffOption(OptionId.create(cmdIndicator,
						OptionValueType.ON_OFF), "");
			}
		} catch (InvalidOptionDescriptorException e) {
			throw new RuntimeException(e);
		}
	}
	
	private class MockConsoleApplicationDescriptor implements IConsoleApplicationDescriptor {
		private MockOptionDescriptorTree tree = new MockOptionDescriptorTree();
		public String getName() {
			return "mock";
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
		
		@SuppressWarnings("unused")
		public IOptionDescriptor getOption(String optionId){
			return null;
		}
		
	}
}
