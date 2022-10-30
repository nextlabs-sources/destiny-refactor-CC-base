package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/Policy.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * This is a policy definition class.
 * Policies have path, a type or a base policy, and consist of target
 * contexts and rules. Policies may optionally define obligations.
 * The Policy class provides methods for accessing and changing
 * all parts of policy definitions.
 *
 * @author Sergey Kalinichenko
 */
public class Policy extends AbstractDefinition<IPolicy> implements IPolicy {

    /**
     * This class provides an immutable implementation
     * of the IRule interface.
     */
    private static class Rule implements IRule {

        /**
         * This is the condition of this rule.
         */
        private final IExpression condition;

        /**
         * This is the outcome of this rule.
         */
        private final Outcome outcome;

        /**
         * Creates the rule with the specified condition and the outcome.
         *
         * @param condition the condition of this rule.
         * @param outcome the outcome when the condition evaluates to true.
         */
        public Rule(IExpression condition, Outcome outcome) {
            if (condition == null) {
                throw new NullPointerException("condition");
            }
            if (outcome == null) {
                throw new NullPointerException("outcome");
            }
            this.condition = condition;
            this.outcome = outcome;
        }

        /**
         * @see IRule#getCondition()
         */
        public IExpression getCondition() {
            return condition;
        }
        /**
         * @see IRule#getOutcome()
         */
        public Outcome getOutcome() {
            return outcome;
        }

        /**
         * @see IPolicy.IRule#isDefault()
         */
        public boolean isDefault() {
            return condition == IExpression.TRUE;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Rule)) {
                return false;
            }
            Rule other = (Rule)obj;
            return condition.equals(other.getCondition())
                && outcome == other.getOutcome();
        }
        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return condition.hashCode() ^ outcome.hashCode();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "when "+condition+" do "+outcome;
        }
    }

    private final Target target = new Target();

    /**
     * A list of rules defined for this Policy.
     */
    private final List<IRule> rules = new ArrayList<IRule>();

    /**
     * This array holds obligations for each outcome type.
     */
    @SuppressWarnings("unchecked")
    private final List<IFunctionCall> obligations[] = new List[Outcome.SIZE];

    {
        // Only the allow and deny outcomes may obligations -
        // the "nothing" outcome is identical to "not applicable."
        obligations[Outcome.ALLOW.getOrdinal()] =
            new ArrayList<IFunctionCall>();
        obligations[Outcome.DENY.getOrdinal()] =
            new ArrayList<IFunctionCall>();
    }

    /**
     * This field holds a reference to the base of this Policy.
     * This field will be set to null when the type is not null.
     */
    private IReference<IPolicy> base;

    /**
     * This field holds a reference to the type of this Policy.
     * This field will be set to null when the base is not null.
     */
    private IReference<IPolicyType> type;

    /**
     * Constructs a policy definition with the specified path, base, or type.
     * Either a type or a base must be specified, but not both.
     *
     * @param path the path of this policy definition.
     * @param base the base that this policy extends.
     * @param type the policy type of this policy.
     */
    public Policy(
        Path path
    ,   IReference<IPolicy> base
    ,   IReference<IPolicyType> type
    ) {
        super(path);
        if ((base == null) == (type==null)) {
            throw new IllegalArgumentException(
                "either base or type must be specified, but not both."
            );
        }
        this.base = base;
        this.type = type;
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitPolicy(this);
    }

    /**
     * @see IPolicy#getTarget()
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Adds a rule with the specified condition and the outcome
     * to the end of the list of rules for this policy.
     *
     * @param condition the condition of this rule.
     * @param outcome the outcome of this rule.
     * @return the rule added by this call.
     */
    public IRule addRule(IExpression condition, Outcome outcome) {
        return addRule(rules.size(), condition, outcome);
    }

    /**
     * Adds a rule with the specified condition and the outcome
     * to the list of rules at the specified index.
     *
     * @param index the index at which to add the rule.
     * @param condition the condition of the rule.
     * @param outcome the outcome of the rule.
     * @return the rule added by this call.
     */
    public IRule addRule(int index, IExpression condition, Outcome outcome) {
        IRule res = new Rule(condition, outcome);
        rules.add(index, res);
        return res;
    }

    /**
     * Removes the rule at the specified index.
     *
     * @param index the index at which to remove the rule.
     * @return the removed rule.
     */
    public IRule removeRule(int index) {
        return rules.remove(index);
    }

    /**
     * @see IPolicy#getRule(int)
     */
    public IRule getRule(int index) {
        return rules.get(index);
    }

    /**
     * Puts a rule with the specified condition and the outcome
     * into the list of rules at the specified index.
     *
     * @param index the index at which to put the rule.
     * @param condition the condition of the rule.
     * @param outcome the outcome of the rule.
     * @return the rule created by this call.
     */
    public IRule setRule(int index, IExpression condition, Outcome outcome) {
        IRule res = new Rule(condition, outcome);
        rules.set(index, res);
        return res;
    }

    /**
     * Removes the specified rule from the list. If more than one instance
     * of the same rule is present, all instances are removed.
     *
     * @param rule the rule to remove.
     * @return true if one or more instances of the rule were removed;
     * false otherwise.
     */
    public boolean removeRule(IRule rule) {
        return rules.removeAll(Collections.singleton(rule));
    }

    /**
     * @see IPolicy#getRules()
     */
    public Iterable<IRule> getRules() {
        return Collections.unmodifiableCollection(rules);
    }

    /**
     * @see IPolicy#getRuleCount()
     */
    public int getRuleCount() {
        return rules.size();
    }

    /**
     * Adds an obligation for the specific outcome to the end of the list of
     * obligations defined for this outcome.
     *
     * @param outcome the outcome for which to add an obligation. Note that
     * Outcome.NOTHING is not a valid argument, because obligations of
     * policies that do not apply are ignored.
     * @param obligation the obligation to add to the list.
     */
    public void addObligation(Outcome outcome, IFunctionCall obligation) {
        checkOutcomeForObligation(outcome);
        addObligation(
            outcome
        ,   obligations[outcome.getOrdinal()].size()
        ,   obligation
        );
    }

    /**
     * Adds an obligation for the specific outcome at the specified index
     * of the list of obligations defined for this outcome.
     *
     * @param outcome the outcome for which to add an obligation. Note that
     * Outcome.NOTHING is not a valid argument, because obligations of
     * policies that do not apply are ignored.
     * @param index the index at which to add the obligation.
     * @param obligation the obligation to add to the list.
     */
    public void addObligation(
        Outcome outcome
    ,   int index
    ,   IFunctionCall obligation) {
        checkOutcomeForObligation(outcome);
        if (obligation == null) {
            throw new NullPointerException("obligation");
        }
        obligations[outcome.getOrdinal()].add(index, obligation);
    }

    /**
     * Removes an obligation at the specified index from the list
     * of obligations associated with this policy for the given outcome.
     *
     * @param outcome the outcome an obligation for which to remove.
     * @param index the index of the obligation to remove.
     * @return the removed obligation.
     */
    public IFunctionCall removeObligation(Outcome outcome, int index) {
        checkOutcomeForObligation(outcome);
        return obligations[outcome.getOrdinal()].remove(index);
    }

    /**
     * Removes the specified obligation from the list of obligations
     * associated with this policy for the given outcome. If an obligation
     * is added several times, all instances of it will be removed.
     *
     * @param outcome the outcome an obligation for which to remove.
     * @param obligation the obligation to remove.
     * @return true if one or more instance of the obligation has been
     * removed from the list.
     */
    public boolean removeObligation(
        Outcome outcome
    ,   IFunctionCall obligation) {
        checkOutcomeForObligation(outcome);
        return obligations[outcome.getOrdinal()].removeAll(
            Collections.singleton(obligation)
        );
    }

    /**
     * @see IPolicy#getObligations()
     */
    public Iterable<IObligations> getObligations() {
        return new Iterable<IObligations>() {
            /**
             * @see Iterable#iterator()
             */
            public Iterator<IObligations> iterator() {
                return new Iterator<IObligations>() {
                    private Outcome currentOutcome = Outcome.NOTHING;
                    /**
                     * @see Iterator#hasNext()
                     */
                    public boolean hasNext() {
                        return nextOutcome() != null;
                    }
                    /**
                     * @see Iterator#next()
                     */
                    public IObligations next() {
                        currentOutcome = nextOutcome();
                        if (currentOutcome == null) {
                            throw new NoSuchElementException();
                        }
                        return new IObligations() {
                            private final Outcome outcome = currentOutcome;
                            final Collection<IFunctionCall> obl =
                                Collections.unmodifiableCollection(
                                    obligations[outcome.getOrdinal()]
                                );
                            public Outcome getOutcome() {
                                return outcome;
                            }
                            public Iterator<IFunctionCall> iterator() {
                                return obl.iterator();
                            }
                        };
                    }
                    /**
                     * @see Iterator#remove()
                     */
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                    /**
                     * Attempt to advance to the next outcome for which
                     * at least one obligation is defined.
                     *
                     * @return the next outcome for which an obligation
                     * is defined, or null if there is no such outcome.
                     */
                    private Outcome nextOutcome() {
                        Outcome res = currentOutcome;
                        if ( res == Outcome.NOTHING ) {
                            res = Outcome.ALLOW;
                            if (obligations[res.getOrdinal()] != null
                            && !obligations[res.getOrdinal()].isEmpty()) {
                                return res;
                            }
                        }
                        if ( res == Outcome.ALLOW ) {
                            res = Outcome.DENY;
                            if (obligations[res.getOrdinal()] != null
                            && !obligations[res.getOrdinal()].isEmpty()) {
                                return res;
                            }
                        }
                        return null;
                    }
                };
            }
        };
    }

    /**
     * @see IPolicy#getObligation(Outcome, int)
     */
    public IFunctionCall getObligation(Outcome outcome, int index) {
        checkOutcomeForObligation(outcome);
        return obligations[outcome.getOrdinal()].get(index);
    }

    /**
     * Sets an obligation for the specific outcome at the specified index
     * of the list of obligations defined for this outcome.
     *
     * @param outcome the outcome for which to add an obligation. Note that
     * Outcome.NOTHING is not a valid argument, because obligations of
     * policies that do not apply are ignored.
     * @param index the index at which to set the obligation.
     * @param obligation the obligation to set into the list.
     */
    public void setObligation(
        Outcome outcome
    ,   int index
    ,   IFunctionCall obligation) {
        checkOutcomeForObligation(outcome);
        if (obligation == null) {
            throw new NullPointerException("obligation");
        }
        obligations[outcome.getOrdinal()].set(index, obligation);
    }

    /**
     * @see IPolicy#getObligationCount(Outcome)
     */
    public int getObligationCount(Outcome outcome) {
        checkOutcomeForObligation(outcome);
        return obligations[outcome.getOrdinal()].size();
    }

    /**
     * @see IPolicy#getObligations(Outcome)
     */
    public Iterable<IFunctionCall> getObligations(Outcome outcome) {
        if (outcome == null) {
            throw new NullPointerException("outcome");
        }
        return Collections.unmodifiableList(obligations[outcome.getOrdinal()]);
    }

    /**
     * @see IPolicy#getType()
     */
    public IReference<IPolicyType> getType() {
        return type;
    }

    /**
     * @see IPolicy#hasType()
     */
    public boolean hasType() {
        return type != null;
    }

    /**
     * Sets the type of this policy, and removes the base policy
     * if one has been provided previously.
     *
     * @param type a reference to the new type of this policy.
     */
    public void setType(IReference<IPolicyType> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
        base = null;
    }

    /**
     * @see IPolicy#getBase()
     */
    public IReference<IPolicy> getBase() {
        return base;
    }

    /**
     * @see IPolicy#hasBase()
     */
    public boolean hasBase() {
        return base != null;
    }

    /**
     * Sets the base policy of this policy, and removes the policy type
     * if one has been previously set for this policy.
     *
     * @param base the new reference to the base policy of this policy.
     */
    public void setBase(IReference<IPolicy> base) {
        if (base == null) {
            throw new NullPointerException("base");
        }
        this.base = base;
        type = null;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Policy)) {
            return false;
        }
        IPolicy other = (IPolicy)obj;
        return super.equals(obj)
            && (base == null ?
                   !other.hasBase()
               :   base.equals(other.getBase()))
            && (type == null ?
                   !other.hasType()
               :   type.equals(other.getType()))
            && getTarget().equals(other.getTarget())
            && compareIterables(getRules(), other.getRules())
            && compareIterables(
                   getObligations(Outcome.ALLOW)
               ,   other.getObligations(Outcome.ALLOW))
            && compareIterables(
                   getObligations(Outcome.DENY)
               ,   other.getObligations(Outcome.DENY));
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("policy ");
        res.append(getPath());
        if (type != null) {
            res.append(" is ");
            res.append(type);
        }
        if (base != null) {
            res.append(" extends ");
            res.append(base);
        }
        res.append(target.toString());
        for (IRule rule : rules) {
            res.append('\n');
            res.append(rule);
        }
        toStringObligations(res, Outcome.ALLOW);
        toStringObligations(res, Outcome.DENY);
        return res.toString();
    }

    /**
     * Converts the list of obligations for the outcome
     * to a string representation.
     *
     * @param out the StringBuffer into which to do the conversion.
     * @param outcome the outcome for which to output the obligations.
     */
    private void toStringObligations(StringBuffer out, Outcome outcome) {
        if (!obligations[outcome.getOrdinal()].isEmpty()) {
            out.append("\non ");
            out.append(outcome);
            out.append(" do ");
            boolean first = true;
            for (IFunctionCall obligation : getObligations(outcome)) {
                if (!first) {
                    out.append(", ");
                } else {
                    first = false;
                }
                out.append(obligation);
            }
        }
    }

    /**
     * Determines if the given outcome is one that supports obligations
     * (i.e. that it is not the Outcome.NOTHING outcome).
     *
     * @param outcome the outcome to check.
     */
    private static void checkOutcomeForObligation(Outcome outcome) {
        if (outcome == null) {
            throw new NullPointerException("outcome");
        }
        if (outcome == Outcome.NOTHING) {
            throw new IllegalArgumentException("outcome");
        }
    }

}

