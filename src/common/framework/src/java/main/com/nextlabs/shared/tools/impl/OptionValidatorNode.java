package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.ParseException;

/**
 *
 * @author hchan
 * @date Apr 11, 2007
 */
class OptionValidatorNode {
	
	private ICompoundOptionDescriptor currentNode;
	private OptionValidatorNode parentNode;
	private List<OptionValidatorNode> children;
	private boolean required;
	private boolean exist;
	private Set<OptionId<?>> selectedOptionIds;
	
	public OptionValidatorNode(ICompoundOptionDescriptor currentNode) {
		this(currentNode, null);
	}
	
	private OptionValidatorNode(ICompoundOptionDescriptor currentNode, OptionValidatorNode parentNode) {
//		this.values = new ArrayList<IParsedOption>();
		this.currentNode = currentNode;
		this.required = currentNode.isRequired();
		this.parentNode = parentNode;
		List<ICompoundOptionDescriptor> compoundChildren = currentNode.getCompoundOptions();
		children = new ArrayList<OptionValidatorNode>(compoundChildren.size());
		for(ICompoundOptionDescriptor compoundChild : compoundChildren){
			children.add(new OptionValidatorNode(compoundChild, this));
		}
		exist = false;
		selectedOptionIds = new HashSet<OptionId<?>>();
	}
	
	public ICompoundOptionDescriptor getCurrentNode() {
		return currentNode;
	}
	public void setCurrentNode(ICompoundOptionDescriptor currentNode) {
		this.currentNode = currentNode;
	}
	public OptionValidatorNode getParentNode() {
		return parentNode;
	}
	public void setParentNode(OptionValidatorNode parentNode) {
		this.parentNode = parentNode;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean requried) {
		this.required = requried;
	}
	
	public void setAllChildrenRequired(boolean requried){
		this.required = requried;
		for(OptionValidatorNode child: children){
			child.setAllChildrenRequired(requried);
		}
	}
	
	public void setAllParentRequired(boolean requried){
		this.required = requried;
		if(parentNode != null){
			parentNode.setAllParentRequired(requried);
		}
	}
	
	public OptionValidatorNode getNode(ICompoundOptionDescriptor compoundOptionDescriptor){
		if(currentNode == compoundOptionDescriptor){
			return this;
		}
		for(OptionValidatorNode child : children){
			OptionValidatorNode matchedNode = child.getNode(compoundOptionDescriptor);
			if(matchedNode != null){
				return matchedNode;
			}
		}
		return null;
	}
	
	public OptionValidatorNode getNode(IOptionDescriptor<?> optionDescriptor){
		if(currentNode instanceof SimpleCompoundOptionDescriptor){
			if (((SimpleCompoundOptionDescriptor) currentNode).getOption() == optionDescriptor) {
				return this;
			}
		}
		for(OptionValidatorNode child : children){
			OptionValidatorNode matchedNode = child.getNode(optionDescriptor);
			if(matchedNode != null){
				return matchedNode;
			}
		}
		return null;
	}
	
	/**
	 * Tells whether or not this compound is required 
	 * @return true if exist
	 */
	public boolean isExist(){
		return exist;
	}
	
	/**
	 * Set this compound is exist in the args, once it has been set. You can't change it. 
	 */
	public void setExist(){
		exist = true;
	}
	
	public void setExist(OptionId<?> selectedOptionId){
		this.setExist();
		this.selectedOptionIds.add(selectedOptionId);
	}
	
//	public void setExist(String selectedOptionId){
//		this.setExist();
//		this.selectedOptionIds.add(selectedOptionId);
//	}
	
	private void putSelectedOptionNames(Collection<String> names){
		for(OptionId<?> optionId : this.selectedOptionIds){
			names.add(optionId.getName());
		}
		
		for(OptionValidatorNode child : children){
			if(child.isExist()){
				child.putSelectedOptionNames(names);
			}
		}
	}
	
	public Collection<String> getSelectedOptionNames(){
		Collection<String> set = new ArrayList<String>();
		putSelectedOptionNames(set);
		return set;
	}
	
	
	
	private Collection<ICompoundOptionDescriptor> selected = new ArrayList<ICompoundOptionDescriptor>();
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#addSelected(com.nextlabs.shared.tools.ICompoundOptionDescriptor)
	 */
	public void addSelected(ICompoundOptionDescriptor option) throws ParseException {
		for(ICompoundOptionDescriptor componentOption : currentNode.getCompoundOptions() ){
			if(componentOption == option){
				selected.add(option);
				return;
			}
		}
		throw new ParseException(ErrorMessageGenerator.getSelectUnknown(option.getName(), currentNode.getName()));
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#getNumSelected()
	 */
	public int getNumSelected() {
		return getSelectedOptions().size();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#getSelected()
	 */
	public Collection<ICompoundOptionDescriptor> getSelectedOptions() {
		return selected;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#isSelected()
	 */
	public boolean isSelected(ICompoundOptionDescriptor option) {
		for(ICompoundOptionDescriptor componentOption : getSelectedOptions() ){
			if(componentOption == option){
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#isSelected()
	 */
	public boolean isSelected() {
		return getNumSelected() != 0;
	}
	
	public String getName() {
		StringBuilder output = new StringBuilder();
		for (ICompoundOptionDescriptor componentOption : currentNode.getCompoundOptions()) {
			if (isSelected(componentOption)) {
				output.append("[").append(componentOption.getName()).append("],");
			} else {
				output.append(componentOption.getName()).append(",");
			}
		}
		return output.toString();
	}
	
	
}
