package com.nextlabs.destiny.tools.enrollment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionStructurePrinter;
import com.nextlabs.shared.tools.impl.OptionTrigger;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.impl.OptionTrigger.Condition;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class ClientInfoMgrOptionDescriptorEnum extends EnrollmentMgrOptionDescriptorEnum{
	protected static final OptionId<Boolean> ENROLL_OPTIONS_ID 		= EnrollmentMgrOptionDescriptorEnum.ENROLL_OPTIONS_ID;
	protected static final OptionId<Boolean> SYNC_OPTIONS_ID 		= EnrollmentMgrOptionDescriptorEnum.SYNC_OPTIONS_ID;
	protected static final OptionId<Boolean> DELETE_OPTIONS_ID 		= EnrollmentMgrOptionDescriptorEnum.DELETE_OPTIONS_ID;
	protected static final OptionId<Boolean> LIST_OPTIONS_ID 		= EnrollmentMgrOptionDescriptorEnum.LIST_OPTIONS_ID;
	protected static final OptionId<String> DOMAIN_NAME_OPTIONS_ID 	= EnrollmentMgrOptionDescriptorEnum.DOMAIN_NAME_OPTIONS_ID;
	protected static final OptionId<File> XML_FILE_OPTIONS_ID 	    = OptionId.create("x", 		OptionValueType.EXIST_FILE);
	
	static final Collection<OptionId<?>> ACTIONS;
	static{
		ACTIONS = new TreeSet<OptionId<?>>();
		ACTIONS.add(ENROLL_OPTIONS_ID);
		ACTIONS.add(SYNC_OPTIONS_ID);
		ACTIONS.add(DELETE_OPTIONS_ID);
		ACTIONS.add(LIST_OPTIONS_ID);
	}
	
	/**
	 *  - Unique
     - [Simple] # [h]
     - Sequence
         - Unique
             - Sequence
                 - [Simple] # [enroll]
                 - Simple # x <clientInfo.file>
             - [Simple] # [sync]
             - [Simple] # [delete]
             - [Simple] # [list]
         - Simple # n <DomainName>
         - [Simple] # [s] <server>
         - [Simple] # [p] <port>
         - Simple # u <username>
         - [Simple] # [w] <password>
         - [Simple] # [v]
	 */
	
	ClientInfoMgrOptionDescriptorEnum() throws InvalidOptionDescriptorException {
	    super(new OptionDescriptorTreeImpl(new SequencedListOptionDescriptor()));
		String description;
		String valueLabel;
		Option<?> option;
		
		SequencedListOptionDescriptor root =
                (SequencedListOptionDescriptor) OptionDescriptorTreeImpl.getRealRootOption(
                        super.getOptions().getRootOption());

		SequencedListOptionDescriptor enrollSeq = new SequencedListOptionDescriptor();

		description = "enroll";
		Option<Boolean> enrollOption = Option.createOnOffOption(ENROLL_OPTIONS_ID, description);
		enrollOption.setLevel((byte)50);
		enrollSeq.add(enrollOption);

		description = "client information file";
		valueLabel = "clientInfo.file";
		enrollSeq.add(Option.createOption(XML_FILE_OPTIONS_ID, description, valueLabel));
		
		UniqueChoiceOptionDescriptor allActions = new UniqueChoiceOptionDescriptor();

		allActions.add(enrollSeq);
		
		description = "sync";
		option = Option.createOnOffOption(SYNC_OPTIONS_ID, description);
		option.setLevel((byte)50);
		allActions.add(option);

		description = "delete";
		option = Option.createOnOffOption(DELETE_OPTIONS_ID, description);
		option.setLevel((byte)50);
		allActions.add(option);
		
		description = "list";
        option = Option.createOnOffOption(LIST_OPTIONS_ID, description);
        option.setLevel((byte)50);
        allActions.add(option);

        root.add(allActions);
        
		description = "domain name";
		valueLabel = "DomainName";
		option = Option.createOption(DOMAIN_NAME_OPTIONS_ID, description, valueLabel);
		root.add(option);
		
        OptionTrigger.add(
                ConditionEnum.IF_FOUND, 
                LIST_OPTIONS_ID, 
                OptionTrigger.Action.MARK_AS_NOT_REQUIRED,
                DOMAIN_NAME_OPTIONS_ID);
        
		
		
		addSharedOptions(root);
	}
	
	public static void main(String[] args) {
        OptionStructurePrinter.print(new ClientInfoMgrOptionDescriptorEnum());
    }
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
	 */
	public String getLongDescription() {
		return getShortDescription();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
	 */
	public String getName() {
		return "clientInfoMgr";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
	 */
	public String getShortDescription() {
		return "Client Information Manager";
	}

}
