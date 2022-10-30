/*
 * Created on Sep 7, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IParsedOptions;
import com.nextlabs.shared.tools.OptionId;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/impl/OptionTrigger.java#1 $
 */

public class OptionTrigger {
	public enum ConditionEnum {
		IF_FOUND,
		//IF_NOT_FOUND	//not ready yet
		;
	};
	

	public enum Action {
		MARK_AS_REQUIRED,
		MARK_AS_NOT_REQUIRED,
		
		//the follow Actions need to be implement before use
		//MARK_AS_VALUE_REQUIRED
		//MARK_AS_VALUE_NOT_REQUIRED
		;
	};
	
	public static class Condition{
		final IOptionDescriptor<?> source;
		final ConditionEnum conditionEnum;
		final Object value;
		
		public Condition(IOptionDescriptor<?> source, ConditionEnum conditionEnum) {
			this(source, conditionEnum, null);
		}
		
		public Condition(OptionId<?> optionId, ConditionEnum conditionEnum) {
            this(optionId.getOption(), conditionEnum);
        }
        
        public <T> Condition(IOptionDescriptor<T> source, ConditionEnum conditionEnum, T value) {
            this.source = source;
            this.conditionEnum = conditionEnum;
            this.value = value;
        }
		
		public <T> Condition(OptionId<T> optionId, ConditionEnum conditionEnum, T value) {
			this(optionId.getOption(), conditionEnum, value);
		}
		
		boolean doesConditionMeet(OptionValidatorNode root, IParsedOptions existedOption){
			switch (conditionEnum) {
            case IF_FOUND:
                OptionValidatorNode sourceNode = root.getNode(source);
                if (!sourceNode.isExist()) {
                    return false;
                } else if (value == null) {
                    //option exist and value doesn't need to check
                    return true;
                }
				
				SimpleCompoundOptionDescriptor simple = (SimpleCompoundOptionDescriptor) sourceNode.getCurrentNode();
				IOptionDescriptor<?> option = simple.getOption();
				List<?> values = existedOption.get(option);
				if(values == null){
					//no value found but value is required.
					return false;
				}
				
				for (Object value : values) {
					if (option.getValueType().contains(value, this.value)) {
						//find the match value , return true immediately.
						return true;
					}
				}		
				return false;
			default:
				return false;
			}
		}
	}

	private static class Trigger {		
		boolean executed = false;
		final List<Condition> conditions;
		final Action action;
		final ICompoundOptionDescriptor target;

		public Trigger(final List<Condition> conditions, final Action action, ICompoundOptionDescriptor target) {
			super();
			this.conditions = conditions;
			this.action = action;
			this.target = target;
		}

		void visit(OptionValidatorNode root, IParsedOptions existedOption) {
			if( doesConditionsMeet(root,existedOption) ){
				doAction(root);
			}
		}
		
		private void doAction(OptionValidatorNode root){
			switch (action) {
			case MARK_AS_REQUIRED: {
                OptionValidatorNode targeNode = root.getNode(target);
                targeNode.setRequired(true);
                executed = true;
            }
                break;
            case MARK_AS_NOT_REQUIRED: {
                OptionValidatorNode targeNode = root.getNode(target);
                targeNode.setRequired(false);
                executed = true;
            }
                break;
            default:
                break;
            }
		}
		
		private boolean doesConditionsMeet(OptionValidatorNode root, IParsedOptions existedOption){
			for (Condition condition : conditions) {
				if (!condition.doesConditionMeet(root, existedOption)) {
					return false;
				}
			}
			return true;
		}
	}
	
	private static List<Trigger> triggers = new ArrayList<Trigger>();
	
	static void reset(){
		OptionTrigger.triggers = new ArrayList<Trigger>();
	}

	/**
	 * add a Trigger that if the option is existed.
	 * @param conditionEnum
	 * @param source
	 * @param action
	 * @param target
	 */
	public static void add(
	        ConditionEnum conditionEnum, 
	        IOptionDescriptor<?> source, 
			Action action,
			ICompoundOptionDescriptor target) {
		List<Condition> conditions = Collections.singletonList(
		        new Condition(source, conditionEnum));
		OptionTrigger.triggers.add(new Trigger(conditions, action, target));
	}
	
	/**
     * add a Trigger that if the option is existed.
     * simplified version of <code>add(ConditionEnum, IOptionDescriptor, Action, ICompoundOptionDescriptor)</code>
     * @param conditionEnum
     * @param source
     * @param action
     * @param target
     */
    public static void add(
            ConditionEnum conditionEnum, 
            OptionId<?> sourceId, 
            Action action,
            OptionId<?> targetId) {
        add(conditionEnum, sourceId.getOption(), action, targetId.getOption().getParent());
    }
	
	/**
     * add a Trigger that the option with specify value;
     * @param conditionEnum
     * @param source
     * @param value
     * @param action
     * @param target
     */
    public static <T> void add(
            ConditionEnum conditionEnum, 
            IOptionDescriptor<T> source, 
            T value,
            Action action, 
            ICompoundOptionDescriptor target){
        List<Condition> conditions = Collections.singletonList(
                new Condition(source, conditionEnum, value));
        OptionTrigger.triggers.add(new Trigger(conditions, action, target));
    }
    
    /**
     * add a Trigger that the option with specify value;
     * @param conditionEnum
     * @param source
     * @param value
     * @param action
     * @param target
     */
    public static <T> void add(
            ConditionEnum conditionEnum, 
            OptionId<T> sourceId, 
            T value,
            Action action, 
            OptionId<?> targetId){
        add(conditionEnum, sourceId.getOption(), value, action, targetId.getOption().getParent());
    }
    
	

//	/**
//	 * 
//	 * @param conditionEnums
//	 * @param sources
//	 * @param action
//	 * @param target
//	 */
//	public static void add(
//			List<ConditionEnum> conditionEnums, 
//			List<IOptionDescriptor<?>> sources,
//			Action action, 
//			ICompoundOptionDescriptor target) {
//		if(sources.size() != conditionEnums.size()){
//			//TODO throw some more meaningful exception
//			throw new RuntimeException("sources and conditions size are different");
//		}
//		
//		List<Condition> conditions = new ArrayList<Condition>();
//		for (int i = 0; i < sources.size(); i++) {
//			conditions.add(new Condition(sources.get(i), conditionEnums.get(i)));
//		}
//
//		OptionTrigger.triggers.add(new Trigger(conditions, action, target));
//	}

	/**
	 * add a trigger that will only run when all conditions meet.
	 * @param conditions
	 * @param action
	 * @param target
	 */
	public static void add(
			List<Condition> conditions,  
			Action action, 
			ICompoundOptionDescriptor target){
		OptionTrigger.triggers.add(new Trigger(conditions, action, target));
	}
	
	public static void add(
            List<Condition> conditions,  
            Action action, 
            OptionId<?> targetId){
        OptionTrigger.triggers.add(new Trigger(conditions, action, targetId.getOption().getParent()));
    }
	
	static void markAll(OptionValidatorNode root, IParsedOptions existedOption) {
		eachTrigger: for (Trigger trigger : triggers) {
			if(trigger.executed){
				continue eachTrigger;
			}
			trigger.visit(root, existedOption);
		}
	}
}
