package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IPolicySet.java#1 $
 */

import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for policy sets. Policy sets
 * have a list of policy types, an optional inclusion and exclusion
 * targets, and a possibly empty list of policy references.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicySet extends IDefinition<IPolicySet> {

    /**
     * Accesses a collection of allowed types for this policy set.
     *
     * @return a collection of allowed types for this policy set.
     */
    Iterable<IReference<IPolicyType>> getAllowedTypes();

    /**
     * Accesses the number of allowed types for this policy set.
     *
     * @return the number of allowed types for this policy set.
     */
    int getAllowedTypeCount();

    /**
     * Accesses references to policies in this policy set.
     *
     * @return a collection of references to policies in this policy set.
     */
    Iterable<IReference<IPolicy>> getPolicies();

    /**
     * Accesses the number of policies in this policy set.
     *
     * @return the number of policies in this policy set.
     */
    int getPolicyCount();

    /**
     * Accesses the overriding outcome for this policy set.
     *
     * @return the overriding outcome for this policy set.
     */
    Outcome getOverridingOutcome();

    /**
     * Determines if this policy set has a target
     * that determines if the policy set is applicable or not.
     *
     * @return true if the policy set has an "apply when ..." target;
     * false otherwise.
     */
    ITarget getApplyTarget();

    /**
     * Accesses the target that determines if the policy set
     * is applicable or not.
     *
     * @return the "apply when" target of this policy set.
     * The returned target may be empty.
     */
    boolean hasApplyTarget();

    /**
     * Determines if this policy set has a target
     * that determines if the policy set needs to be ignored.
     *
     * @return true if the policy set has an "ignore when ..." target;
     * false otherwise.
     */
    ITarget getIgnoreTarget();

    /**
     * Accesses the target that determines if the policy set
     * needs to be ignored.
     *
     * @return the "ignore when" target of this policy set.
     * The returned target may be empty.
     */
    boolean hasIgnoreTarget();

}
