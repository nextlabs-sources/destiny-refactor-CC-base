package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/ITarget.java#1 $
 */

import com.nextlabs.expression.representation.IExpression;

/**
 * This interface defines the contract for targets, parts of policies
 * and policy sets.
 *
 * @author Sergey Kalinichenko
 */
public interface ITarget {

    /**
     * This interface defines the contract for contexts, elements of targets.
     * Contexts define the section with which they are associated,
     * and a set predicate. The section corresponds to the context attachment
     * of a policy type. The condition is a set predicate describing when
     * this context is applicable.
     */
    public interface IContext {

        /**
         * Retrieves the condition of this context.
         *
         * @return the condition of this context.
         */
        IExpression getCondition();

        /**
         * Retrieves the section for which this context is defined.
         *
         * @return the section for which this context is defined.
         */
        String getSection();

        /**
         * Retrieves the target in which this IContext is defined.
         *
         * @return the target in which this context is defined.
         */
        ITarget getTarget();

    }

    /**
     * Gets the contexts defined for this target.
     *
     * @return an Iterable of contexts defined for this target.
     */
    Iterable<IContext> getContexts();

    /**
     * Gets the context defined for the given section.
     *
     * @param section the section for which to retrieve the context.
     * @return the context defined for the given section.
     */
    IContext getContext(String section);

    /**
     * Determines if a context is defined for the given section.
     *
     * @param section the section for which to check if the context is defined.
     * @return true if a context is defined for the given section;
     * false otherwise.
     */
    boolean hasContext(String section);

    /**
     * Gets the sections for which contexts are defined in this target.
     *
     * @return An Iterable of sections of this target for which
     * contexts were defined.
     */
    Iterable<String> getSections();

    /**
     * Returns true if this target is empty; false otherwise.
     *
     * @return true if this target is empty; false otherwise.
     */
    boolean isEmpty();

}
