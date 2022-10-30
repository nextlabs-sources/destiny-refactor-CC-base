package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IPolicyComponent.java#1 $
 */

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for policy components.
 *
 * @author Sergey Kalinichenko
 */
public interface IPolicyComponent extends IDefinition<IPolicyComponent> {

    /**
     * Obtain a reference to the context type for which this policy component
     * is defined.
     *
     * @return a reference to the context type for which this policy component
     * is defined.
     */
    IReference<IContextType> getType();

    /**
     * Obtains the predicate of this policy component.
     *
     * @return the predicate of this policy component.
     */
    IExpression getPredicate();

}