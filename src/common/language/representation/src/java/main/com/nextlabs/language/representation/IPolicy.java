package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IPolicy.java#1 $
 */

import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for policies. Policies have an optional
 * base or an optional type (one of these must be set), and aggregate
 * policy rules and policy contexts.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicy extends IDefinition<IPolicy> {

    /**
     * This interface defines the contract for Rules,
     * pairing up conditions and outcomes.
     * A rule defines the outcome when its condition is true.
     * Policies have at least one IRule.
     */
    public interface IRule {

        /**
         * Retrieves the condition of this rule.
         *
         * @return the condition of this rule.
         */
        IExpression getCondition();

        /**
         * Retrieves the outcome of this rule.
         * @return the outcome of this rule.
         */
        Outcome getOutcome();

        /**
         * Returns true if the rule has no condition (i.e. when
         * getCondition returns IExpression.TRUE).
         *
         * @return true when the rule has no condition; false otherwise.
         */
        boolean isDefault();

    }

    /**
     * This interface defines the contract for collections of
     * obligations defined for a specific outcome.
     */
    public interface IObligations extends Iterable<IFunctionCall> {

        /**
         * Accesses the outcome for which these obligations are defined.
         *
         * @return the outcome for which these obligations are defined.
         */
        Outcome getOutcome();

    }

    /**
     * Gets the rule at the specified index.
     *
     * @param index the index for which to get the rule.
     * @return the rule at the specified index.
     */
    IRule getRule(int index);

    /**
     * Accesses the rules associated with this policy.
     *
     * @return an Iterable<IRule> containing the rules associated
     * with this policy.
     */
    Iterable<IRule> getRules();

    /**
     * Obtains the number of rules defined for this policy.
     *
     * @return the number of rules defined for this policy.
     */
    int getRuleCount();

    /**
     * Gets all obligations defined for this policy.
     *
     * @return an Iterable<IObligations> defined for this policy.
     */
    Iterable<IObligations> getObligations();

    /**
     * Gets the obligation at the specific index for the given outcome.
     *
     * @param outcome the outcome for which to get an obligation. Note that
     * Outcome.NOTHING is not a valid argument, because obligations of
     * policies that do not apply are ignored.
     * @param index the index of the obligation to retrieve.
     * @return the obligation at the specific index for the given outcome.
     */
    IFunctionCall getObligation(Outcome outcome, int index);

    /**
     * Gets the number of obligations specified for the given outcome.
     * Note that Outcome.NOTHING is not a valid argument, because obligations
     * of policies that do not apply are ignored.
     *
     * @param outcome the outcome for which to get the number of obligations.
     * @return the number of obligations specified for the given outcome.
     */
    int getObligationCount(Outcome outcome);

    /**
     * Gets the obligations associated with the specified outcome.
     *
     * @param outcome the outcome the obligations for which to get.
     * @return an Iterable<IFunctionCall> with the obligations associated
     * with this policy for the specified outcome.
     */
    Iterable<IFunctionCall> getObligations(Outcome outcome);

    /**
     * Gets the target of this Policy.
     *
     * @return the target of this policy.
     */
    ITarget getTarget();

    /**
     * Gets a reference to the type of this policy, if one is present.
     *
     * @return a reference to the type of this policy, if set; null otherwise.
     */
    IReference<IPolicyType> getType();

    /**
     * Determines if this policy has a type.
     *
     * @return true if the policy type is set; false otherwise.
     */
    boolean hasType();

    /**
     * Gets a reference to the base policy of this policy, if one is set.
     *
     * @return a reference to the base policy of this policy if one is set;
     * null otherwise.
     */
    IReference<IPolicy> getBase();

    /**
     * Determines if this policy has a base policy.
     *
     * @return true if this policy has a base policy; false otherwise.
     */
    boolean hasBase();

}