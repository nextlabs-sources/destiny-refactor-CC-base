package com.nextlabs.shared.tools;

import java.util.List;

/**
 * Compound Option Descriptor interface.
 * A compound is a node in the option tree. It never be a end node in the tree. End node must be IOptionDescriptor
 * 
 * @author hchan
 * @date Mar 27, 2007
 */
public interface ICompoundOptionDescriptor {
	enum OptionDescriptorType{ 
		UNIQUE, 
		SEQUENCE, 
		SIMPLE, 
		MULTI,;
	}
	
	/**
	 * @return all compound under this compound
	 */
	List<ICompoundOptionDescriptor> getCompoundOptions();
	
	/**
	 * return a string describing  this compound, the name is meaningless in the program. 
	 * It is only useful when a compound is missing, the user can tell easily
	 * @return name of this compound.
	 */
	String getName();
	
	/**
	 * @return the type of this compound
	 */
	OptionDescriptorType getType();

	void accept(IOptionDescriptorVisitor visitor);

	/**
	 * Tells whether or not this compound is required 
	 * @return true if required
	 */
	boolean isRequired();
}
