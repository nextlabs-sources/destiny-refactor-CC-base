package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/main/com/nextlabs/language/formatter/IPolicyLanguageFormatter.java#1 $
 */

import java.io.IOException;
import java.io.Writer;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinition;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;

/**
 * This interface defines the contract for policy language formatters.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyLanguageFormatter {

    /**
     * Implementations of this method format policy type definitions.
     *
     * @param out the Writer to which to write the output.
     * @param policyType the policy type definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatPolicyType(Writer out, IPolicyType policyType)
        throws IOException;

    /**
     * Implementations of this method format context type definitions.
     *
     * @param out the Writer to which to write the output.
     * @param contextType the context type definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatContextType(Writer out, IContextType contextType)
        throws IOException;

    /**
     * Implementations of this method format function type definitions.
     *
     * @param out the Writer to which to write the output.
     * @param functionType the function type definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatFunctionType(Writer out, IFunctionType functionType)
        throws IOException;

    /**
     * Implementations of this method format obligation type definitions.
     *
     * @param out the Writer to which to write the output.
     * @param obligationType the obligation type definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatObligationType(Writer out, IObligationType obligationType)
        throws IOException;

    /**
     * Implementations of this method format policy definitions.
     *
     * @param out the Writer to which to write the output.
     * @param policy the policy definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatPolicy(Writer out, IPolicy policy)
        throws IOException;

    /**
     * Implementations of this method format policy set definitions.
     *
     * @param out the Writer to which to write the output.
     * @param policySet the policy component definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatPolicySet(Writer out, IPolicySet policySet)
        throws IOException;

    /**
     * Implementations of this method format policy component definitions.
     *
     * @param out the Writer to which to write the output.
     * @param policyComponent the policy component definition.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatPolicyComponent(Writer out, IPolicyComponent policyComponent)
        throws IOException;

    /**
     * Implementations of this method format an Iterable of IDefinition
     * objects into the specified output Writer.
     *
     * @param out the Writer to which to write the output.
     * @param separator the separator to place between definitions.
     * @param defs the definitions to format.
     * @throws IOException when an output operation to the writer fails.
     */
    void format(Writer out, String separator, Iterable<IDefinition<?>> defs)
        throws IOException;

    /**
     * Implementations of this method format an array of IDefinition objects
     * into the specified output Writer.
     *
     * @param out the Writer to which to write the output.
     * @param separator the separator to place between definitions.
     * @param defs the definitions to format.
     * @throws IOException when an output operation to the writer fails.
     */
    void format(Writer out, String separator, IDefinition<?> ... defs)
        throws IOException;

    /**
     * This method formats an expression.
     *
     * @param out the Writer to which to write the output.
     * @param expression the expression to format.
     * @throws IOException when an output operation to the writer fails.
     */
    void formatExpression(Writer out, IExpression expression)
        throws IOException;

}
