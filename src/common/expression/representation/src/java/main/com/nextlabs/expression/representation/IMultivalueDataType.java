package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IMultivalueDataType.java#1 $
 */

/**
 * This interface defines the contract for multi value data
 * types (i.e. collections of other data types)
 *
 * @author Alan Morgan
 */

public interface IMultivalueDataType extends IDataType {
    /**
     * Returns the type of the individual entities in this multi value
     */
    IDataType getInnerType();
}
