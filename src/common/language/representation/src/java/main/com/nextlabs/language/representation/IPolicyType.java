package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IPolicyType.java#1 $
 */

import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for policy types.
 * Policy type is a named collection of contexts and obligation references,
 * with additional data defining which contexts and obligations are required.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyType extends IDefinition<IPolicyType> {


    /**
     * This interface defines the contract for policy type context sections.
     * A context section has a name, and a collection of context type
     * references, and a flag specifying if the section is required or not.
     * Required sections must be present in each policy of this policy type;
     * optional sections may be left blank, which is identical to specifying
     * a predicate constant "true".
     */
    public interface IContextSection
           extends Iterable<IReference<IContextType>> {

        /**
         * Obtains the section to which this context is attached.
         *
         * @return the section to which the context is attached.
         */
        String getSection();

        /**
         * Determines if this section is required or not.
         *
         * @return true if this section is required; otherwise false.
         */
        boolean isRequired();

        /**
         * Obtains the policy type to which this section belongs.
         *
         * @return the policy type to which this section belongs.
         */
        IPolicyType getPolicyType();

    }

    /**
     * Determines if the policy type has a base policy type or not.
     *
     * @return true if the policy type has a base policy type; false otherwise.
     */
    boolean hasBase();

    /**
     * Obtains a reference to the base type of this policy type.
     *
     * @return a reference to the base type of this policy type, or null
     * if the base policy type is not set.
     */
    IReference<IPolicyType> getBase();

    /**
     * @see IPolicyType#getContextSections()
     */
    Iterable<IContextSection> getContextSections();

    /**
     * Gets a context reference in the section identified by name
     * at the specified index.
     *
     * @param sectionName the name of the section
     * from which to get the reference.
     * @param index the index from which to get the reference.
     * @return the reference at the specified index of the specified section.
     */
    IReference<IContextType> getContext(String sectionName, int index);

    /**
     * Gets all references for the specific section of the policy type.
     *
     * @param sectionName the name of the section from which to get
     * the references.
     * @return an Iterable with references for the specific section
     * of the policy type.
     */
    Iterable<IReference<IContextType>> getContexts(String sectionName);

    /**
     * Determines if the specified section has a context identified by
     * a specific reference.
     *
     * @param sectionName the name of the section in which to look for the
     * specified reference.
     * @param reference the reference to find in the specified section
     * of this policy type.
     * @return true if the specified section has the context; false otherwise.
     */
    boolean hasContext(String sectionName, IReference<IContextType> reference);

    /**
     * Determines the number of context references in the specified section
     * of this policy type.
     *
     * @param sectionName the name of the section the number of context
     * references in which we need to determine.
     * @return the number of references in the specified section of this
     * policy type.
     */
    int getContextCount(String sectionName);

    /**
     * Gets the names of all required sections.
     *
     * @return an Iterable with the names of all required sections
     * defined in this policy type.
     */
    Iterable<String> getRequiredSections();

    /**
     * Determines if the specified section is required.
     *
     * @param sectionName the name of the section to check.
     * @return true if the specified section is required; false otherwise.
     */
    boolean isSectionRequired(String sectionName);

    /**
     * Gets the reference associated with this policy type
     * at the specified index.
     *
     * @param index the index of the context to get.
     * @return the context associated with this policy type
     * at the specified index.
     */
    IReference<IContextType> getContext(int index);

    /**
     * Gets the contexts associated with this policy type.
     *
     * @return an Iterable of context references associated
     * with this policy type.
     */
    Iterable<IReference<IContextType>> getContexts();

    /**
     * Determines if the policy type has a context identified by
     * a specific reference.
     *
     * @param reference the reference to find among the contexts
     * of this policy type.
     * @return true if the policy type has the context; false otherwise.
     */
    boolean hasContext(IReference<IContextType> reference);

    /**
     * Gets the number of contexts associated with this policy.
     *
     * @return the number of contexts associated with this policy.
     */
    int getContextCount();

    /**
     * Obtains the name of the sections of this policy type.
     *
     * @return an Iterable of section names of this policy type.
     */
    Iterable<String> getSectionNames();

    /**
     * Determines if this policy type has the specified section.
     *
     * @param sectionName the name of the section to check.
     * @return true if this policy type has the specified section;
     * false otherwise.
     */
    boolean hasSection(String sectionName);

    /**
     * Determines if the policy type has a specific obligation.
     *
     * @param reference the reference to check for being on the list
     * of obligations of this policy type.
     * @return true if this obligation is on the list of obligations of
     * this policy type; false otherwise.
     */
    boolean hasObligation(IReference<IObligationType> reference);

    /**
     * Gets the obligation at the specified index.
     *
     * @param index the index of the obligation to get.
     * @return the obligation at the specified index.
     */
    IReference<IObligationType> getObligation(int index);

    /**
     * Obtains the number of obligations associated with this policy type.
     *
     * @return the number of obligations associated with this policy type.
     */
    int getObligationCount();

    /**
     * Obtains the list of obligations associated with this policy type.
     *
     * @return the list of obligations associated with this policy type.
     */
    Iterable<IReference<IObligationType>> getObligations();

}