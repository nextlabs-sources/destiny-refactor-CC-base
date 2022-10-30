package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/PolicySet.java#1 $
 */

import static com.nextlabs.language.representation.Utils.containsAll;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * This is a policy set class.
 *
 * @author Sergey Kalinichenko
 */
public class PolicySet extends AbstractDefinition<IPolicySet>
                       implements IPolicySet {

    /**
     * A Set of references to all the allowed types.
     */
    private final Set<IReference<IPolicyType>> allowedTypes =
        new LinkedHashSet<IReference<IPolicyType>>();

    /**
     * A Set of references to all policies.
     */
    private final Set<IReference<IPolicy>> policies =
        new LinkedHashSet<IReference<IPolicy>>();

    /**
     * The overriding outcome for this policy set.
     */
    private Outcome overridingOutcome = Outcome.DENY;

    /**
     * The apply target of this policy set.
     */
    private final Target applyTarget = new Target();

    /**
     * The ignore target of this policy set.
     */
    private final Target ignoreTarget = new Target();

    /**
     * Creates a policy set with the specified path.
     *
     * @param path the path of this policy set.
     */
    public PolicySet(Path path) {
        super(path);
    }

    /**
     * @see IPolicySet#getOverridingOutcome()
     */
    public Outcome getOverridingOutcome() {
        return overridingOutcome;
    }

    /**
     * Sets the override outcome for this policy set.
     * The override outcome must be either an allow or a deny - it may not
     * be null or NOTHING.
     *
     * @param overridingOutcome the new override outcome for this policy set.
     */
    public void setOverridingOutcome(Outcome overridingOutcome) {
        if (overridingOutcome == null) {
            throw new NullPointerException("overridingOutcome");
        }
        if (overridingOutcome == Outcome.NOTHING) {
            throw new IllegalArgumentException(
                "Overriding outcome of a policy set must be ALLOW or DENY"
            );
        }
        this.overridingOutcome = overridingOutcome;
    }

    /**
     * @see IDefinition#accept(IDefinitionVisitor)
     */
    public void accept(IDefinitionVisitor visitor) {
        visitor.visitPolicySet(this);
    }

    /**
     * @see IPolicySet#hasApplyTarget()
     */
    public boolean hasApplyTarget() {
        return !applyTarget.isEmpty();
    }

    /**
     * @see IPolicySet#getApplyTarget()
     */
    public Target getApplyTarget() {
        return applyTarget;
    }

    /**
     * @see IPolicySet#hasIgnoreTarget()
     */
    public boolean hasIgnoreTarget() {
        return !ignoreTarget.isEmpty();
    }

    /**
     * @see IPolicySet#getIgnoreTarget()
     */
    public Target getIgnoreTarget() {
        return ignoreTarget;
    }

    /**
     * @see IPolicySet#getAllowedTypes()
     */
    public Iterable<IReference<IPolicyType>> getAllowedTypes() {
        return Collections.unmodifiableCollection(allowedTypes);
    }

    /**
     * @see IPolicySet#getAllowedTypeCount()
     */
    public int getAllowedTypeCount() {
        return allowedTypes.size();
    }

    /**
     * Adds an allowed type to this policy set.
     *
     * @param allowedType the allowed type to be added to this policy set.
     */
    public void addAllowedType(IReference<IPolicyType> allowedType) {
        if (allowedType == null) {
            throw new NullPointerException("allowedType");
        }
        allowedTypes.add(allowedType);
    }

    /**
     * Adds multiple allowed types to the policy set.
     *
     * @param allowedTypes the allowed types to be added to this policy set.
     */
    public void addAllowedTypes(
        Iterable<IReference<IPolicyType>> allowedTypes
    ) {
        if (allowedTypes == null) {
            throw new NullPointerException("allowedTypes");
        }
        for (IReference<IPolicyType> ref : allowedTypes) {
            addAllowedType(ref);
        }
    }

    /**
     * Removes the specified allowed type from this policy set.
     *
     * @param allowedType the allowed type to be removed from
     * this policy set.
     * @return true if the allowed type has been removed; false if the
     * allowed type was not in the set.
     */
    public boolean removeAllowedType(IReference<IPolicyType> allowedType) {
        return allowedTypes.remove(allowedType);
    }

    /**
     * @see IPolicySet#getPolicies()
     */
    public Iterable<IReference<IPolicy>> getPolicies() {
        return Collections.unmodifiableCollection(policies);
    }

    /**
     * @see IPolicySet#getPolicyCount()
     */
    public int getPolicyCount() {
        return policies.size();
    }

    /**
     * Adds a single policy to this policy set.
     *
     * @param policy the policy to be added to this policy set.
     */
    public void addPolicy(IReference<IPolicy> policy) {
        if (policy == null) {
            throw new NullPointerException("policy");
        }
        policies.add(policy);
    }

    /**
     * Add multiple policies to this policy set.
     *
     * @param policies the policies to add to this policy set.
     */
    public void addPolicies(Iterable<IReference<IPolicy>> policies) {
        if (policies == null) {
            throw new NullPointerException("policies");
        }
        for (IReference<IPolicy> ref : policies) {
            addPolicy(ref);
        }
    }

    /**
     * Removes the specified policy from this policy set.
     *
     * @param policy the policy to be removed from this policy set.
     * @return true if the policy has been removed; false if the
     * policy was not in the set.
     */
    public boolean removePolicy(IReference<IPolicy> policy) {
        return policies.remove(policy);
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof PolicySet)) {
            return false;
        }
        PolicySet other = (PolicySet)obj;
        return overridingOutcome.equals(other.getOverridingOutcome())
            && applyTarget.equals(other.getApplyTarget())
            && ignoreTarget.equals(other.getIgnoreTarget())
            && getAllowedTypeCount() == other.getAllowedTypeCount()
            && getPolicyCount() == other.getPolicyCount()
            && containsAll(allowedTypes, other.getAllowedTypes())
            && containsAll(policies, other.getPolicies());
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
        StringBuffer res = new StringBuffer("policy set ");
        res.append(getPath());
        res.append(" of ");
        boolean isFirst = true;
        for (IReference<IPolicyType> ref : allowedTypes) {
            if (!isFirst) {
                res.append(", ");
            } else {
                isFirst = false;
            }
            res.append(ref);
        }
        if (hasApplyTarget()) {
            res.append("\napply when");
            res.append(applyTarget);
        }

        if (hasIgnoreTarget()) {
            res.append("\nignore when");
            res.append(ignoreTarget);
        }

        res.append("\n");
        res.append(overridingOutcome);
        res.append(" overrides");
        if (!policies.isEmpty()) {
            res.append(" ( ");
            isFirst = true;
            for (IReference<IPolicy> ref : policies) {
                if (!isFirst) {
                    res.append(", ");
                } else {
                    isFirst = false;
                }
                res.append(ref);
            }
            res.append(" )");
        }
        return res.toString();
    }

}
