package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IDefinitionVisitor.java#1 $
 */

/**
 * This interface defines the contract for visitors of definitions
 * of the policy language. Definitions accept this visitor, and call back
 * a type-dependent method for processing based on the type of the definition.
 *
 * @author Sergey Kalinichenko
 */
public interface IDefinitionVisitor {

    /**
     * This method is called for policy type definitions.
     *
     * @param policyType the policy type definition.
     */
    void visitPolicyType(IPolicyType policyType);

    /**
     * This method is called for context type definitions.
     *
     * @param contextType the context type definition.
     */
    void visitContextType(IContextType contextType);

    /**
     * This method is called for function type definitions.
     *
     * @param functionType the function type definition.
     */
    void visitFunctionType(IFunctionType functionType);

    /**
     * This method is called for obligation type definitions.
     *
     * @param obligationType the obligation type definition.
     */
    void visitObligationType(IObligationType obligationType);

    /**
     * This method is called for policy definitions.
     *
     * @param policy the policy definition.
     */
    void visitPolicy(IPolicy policy);

    /**
     * This method is called for policy set definitions.
     *
     * @param policySet the policy set definition.
     */
    void visitPolicySet(IPolicySet policySet);

    /**
     * This method is called for policy component definitions.
     *
     * @param policyComponent the policy component definition.
     */
    void visitPolicyComponent(IPolicyComponent policyComponent);

    /**
     * This method is called for folder definitions
     *
     * @param folder the folder definition
     */
    void visitFolder(IFolder folder);
}
