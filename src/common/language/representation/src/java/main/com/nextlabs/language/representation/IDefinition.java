package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IDefinition.java#1 $
 */

import com.nextlabs.util.Path;

/**
 * This interface defines the contract for definitions.
 * Definitions have names and descriptions, and they also support
 * a visitor interface for processing their content, which is
 * type-dependent.
 *
 * @author Sergey Kalinichenko
 */
public interface IDefinition<T extends IDefinition<T>> {

    /**
     * Returns the path to this definition.
     *
     * @return the path to this definition.
     */
    Path getPath();

    /**
     * Sets the Path of this definition.
     *
     * @param path the new Path of this definition.
     */
    void setPath(Path path);

    /**
     * Returns an optional description associated with this definition.
     *
     * @return an optional description associated with this definition.
     */
    String getDescription();


    /**
     * Sets the description of this definition.
     *
     * @param description the new description of this definition.
     */
    void setDescription(String description);

    /**
     * Accepts an IDefinitionVisitor and calls its type-dependent method.
     *
     * @param visitor the visitor on which to call a type-dependent method.
     */
    void accept(IDefinitionVisitor visitor);

}
