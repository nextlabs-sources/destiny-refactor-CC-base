package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/main/com/nextlabs/language/parser/IPolicyLanguageParser.java#1 $
 */

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinitionVisitor;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicySet;
import com.nextlabs.language.representation.IPolicyType;

/**
 * This interface defines the contract for policy parsers.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyLanguageParser {

    /**
     * Obtain the version of the policy language supported by this parser.
     *
     * @return the version of the policy language supported by this parser.
     */
    int getVersion();

    /**
     * Obtains a set of keywords of the policy language supported by
     * this version of the parser.
     *
     * @return a set of keywords of the policy language supported by
     * this version of the parser.
     */
    Set<String> getKeywords();

    /**
     * Implementations of this method parse one or more declaration,
     * calling back the definition visitor as the declarations
     * are being parsed.
     *
     * @param source the source from which to read the policy language.
     * @param visitor the visitor for passing the parsed definitions.
     */
    void parseDeclarations(Reader source, IDefinitionVisitor visitor)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a policy type.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed policy type definition.
     */
    IPolicyType parsePolicyTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a context type.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed context type definition.
     */
    IContextType parseContextTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a function type.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed function type definition.
     */
    IFunctionType parseFunctionTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of an obligation type.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed obligation type definition.
     */
    IObligationType parseObligationTypeDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a policy.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed policy definition.
     */
    IPolicy parsePolicyDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a policy component.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed policy component definition.
     */
    IPolicyComponent parseComponentDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single definition of a policy set.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed policy set definition.
     */
    IPolicySet parsePolicySetDeclaration(Reader source)
        throws IOException, PolicyLanguageException;

    /**
     * Parses a single expression.
     *
     * @param source the source from which to read the policy language.
     * @return the parsed expression.
     */
    IExpression parseExpression(Reader source)
        throws IOException, PolicyLanguageException;

}
