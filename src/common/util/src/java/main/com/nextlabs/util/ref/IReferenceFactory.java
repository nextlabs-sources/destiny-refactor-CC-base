package com.nextlabs.util.ref;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/ref/IReferenceFactory.java#1 $
 */

import com.nextlabs.util.Path;

/**
 * This interface defines the contract for reference factories.
 *
 * @author Sergey Kalinichenko
 */
public interface IReferenceFactory {

    /**
     * This is the default reference factory. The references it produces
     * cannot be resolved.
     */
    IReferenceFactory DEFAULT = new IReferenceFactory() {

        /**
         * @see IReferenceFactory#create(Path, Class)
         */
        public <T> IReference<T> create(Path path, Class<T> refClass) {
            return new Reference<T>(path, refClass);
        }

        /**
         * @see IReferenceFactory#create(long, Class)
         */
        public <T> IReference<T> create(long id, Class<T> refClass) {
            return new Reference<T>(id, refClass);
        }

    };

    /**
     * Creates a reference by path to an object of the specified type.
     *
     * @param <T> the type of the referenced object.
     * @param path the path to the referenced object.
     * @param type the class matching the type of the referenced object.
     * @return a reference by path to an object of the specified type.
     */
    <T> IReference<T> create(Path path, Class<T> type);

    /**
     * Creates a reference by id to an object of the specified type.
     *
     * @param <T> the type of the referenced object.
     * @param id the id of the referenced object.
     * @param type the class matching the type of the referenced object.
     * @return a reference by id to an object of the specified type.
     */
    <T> IReference<T> create(long id, Class<T> type);

}
