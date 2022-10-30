package com.nextlabs.util.ref;

import com.nextlabs.util.Path;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/ref/IReference.java#1 $
 */

/**
 * This interface defines the contract for references to external items.
 * References may be by path or by a unique ID.
 *
 * @author Sergey Kalinichenko
 */
public interface IReference<T> {

    /**
     * Determines if the reference is by path or by id.
     *
     * @return true if the reference is by path; false otherwise.
     */
    boolean isByPath();

    /**
     * Gets the ID if this is a reference by ID.
     * @return ID if this is the reference by ID.
     * @throws IllegalStateException if this is a reference by path.
     */
    long getId();

    /**
     * Gets the path if this is a reference by path.
     * @return path if this is the reference by path.
     * @throws IllegalStateException if this is a reference by ID.
     */
    Path getPath();

    /**
     * Returns the referenced value.
     *
     * @return the value referred to by this reference.
     */
    T get();

    /**
     * Determines if the reference can resolve itself or not.
     *
     * @return true if the reference can be used to resolve itself;
     * false otherwise.
     */
    boolean canResolve();

    /**
     * Returns the class of the object referenced by this reference.
     *
     * @return the class of the object referenced by this reference.
     */
    Class<T> getRefClass();

}
