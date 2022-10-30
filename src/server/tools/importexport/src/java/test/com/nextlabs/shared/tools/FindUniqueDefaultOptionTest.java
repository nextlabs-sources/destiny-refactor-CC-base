package com.nextlabs.shared.tools;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.SimpleCompoundOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;


/**
 * TODO description
 *
 * @author hchan
 * @date Apr 9, 2007
 */
public class FindUniqueDefaultOptionTest {
	@Test
	public void noSelected(){
		try {
			UniqueChoiceOptionDescriptor unique = new UniqueChoiceOptionDescriptor();
			Option option =  new Option("1a", "", false, false);
			unique.add(option);
			
			option =  new Option("1b", "", false,true);
			unique.add(option);
			
			option =  new Option("1c", "", false,false);
			unique.add(option);
			
			option =  new Option("1d", "", false,false);
			unique.add(option);
			
			FindUniqueDefaultOption finder = new FindUniqueDefaultOption();
			finder.visit(unique);
			
			IOptionDescriptor defaultOption = finder.getOption();
			assertEquals("1b", defaultOption.getOptionIdName());
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void haveSelected(){
		try {
			UniqueChoiceOptionDescriptor unique = new UniqueChoiceOptionDescriptor();
			Option option = new Option("2a", "", false, false);
			unique.add(option);

			option = new Option("2b", "", false, true);
			unique.add(option);

			option = new Option("2c", "", false, false);
			unique.add(option);

			option = new Option("2d", "", false, false);
			ICompoundOptionDescriptor selected = new SimpleCompoundOptionDescriptor(option);
			unique.add(selected);

			unique.setSelected(selected);

			FindUniqueDefaultOption finder = new FindUniqueDefaultOption();
			finder.visit(unique);

			IOptionDescriptor defaultOption = finder.getOption();
			assertEquals("2d", defaultOption.getOptionIdName());
		} catch (InvalidOptionDescriptorException e) {
			fail(e.toString());
		} catch (ParseException e) {
			fail(e.toString());
		}
	}
	
}
