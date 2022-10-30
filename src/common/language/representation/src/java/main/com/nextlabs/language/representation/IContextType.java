package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IContextType.java#1 $
 */

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for context type definitions.
 * Context type definitions have an optional base, a collection of
 * fixed attributes, and a collection of attribute templates.
 *
 * @author Sergey Kalinichenko
 */
public interface IContextType extends IDefinition<IContextType> {

    /**
     * This interface defines the contract for accessing attributes
     * of this context type definition. Implementations of this interface
     * represent both actual and template attributes.
     */
    interface IAttribute {

        /**
         * The name of the attribute, or the prefix of the template attribute.
         *
         * @return the name of the attribute or the template attribute prefix.
         */
        String getName();

        /**
         * Returns the data type of this attribute.
         *
         * @return the data type of this attribute.
         */
        IDataType getType();

        /**
         * Determines if the attribute can serve as a unique identifier
         * of instances of its context.
         *
         * @return true if the attribute values identify context instances
         * uniquely; false otherwise.
         */
        boolean isUnique();

        /**
         * Determines if this is an actual or a template attribute.
         *
         * @return true if this is a template attribute; false otherwise.
         */
        boolean isTemplate();

    }

    /**
     * Returns true if this context type has a base context type;
     * false otherwise.
     *
     * @return true if this context type has a base context type;
     * false otherwise.
     */
    boolean hasBase();

    /**
     * Returns the base context type, or null if none is specified.
     *
     * @return the base context type, or null if none is specified.
     */
    IReference<IContextType> getBase();

    /**
     * Obtains the number of attributes in this context.
     *
     * @return the number of attributes in this context.
     */
    int getAttributeCount();

    /**
     * Obtains the number of attribute templates in this context.
     *
     * @return the number of attribute templates in this context.
     */
    int getAttributeTemplateCount();

    /**
     * Gets an Iterable with definitions of all actual attributes of this type.
     *
     * @return an Iterable with definitions of all actual attributes.
     */
    Iterable<IAttribute> getAttributes();

    /**
     * Gets an Iterable with definitions of all template attributes
     * of this type.
     *
     * @return an Iterable with definitions of all template attributes.
     */
    Iterable<IAttribute> getAttributeTemplates();

    /**
     * Determines if an attribute with the given name exists.
     * The preference is given to actual attributes over the
     * template ones.
     *
     * @param name the name of the attribute to check.
     * @return true if an actual attribute with the specified name exists,
     * or a template attribute shares a prefix with the name passed in.
     */
    boolean hasAttribute(String name);

    /**
     * Gets the attribute for name. The preference is given to actual
     * attributes over the template ones. However, if there is no actual
     * attribute, template attributes are searched for a match,
     *
     * @param name the name of the attribute to return.
     * @return The actual IAttribute or an attribute template.
     */
    IAttribute getAttribute(String name);

}
