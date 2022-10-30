package com.nextlabs.language.repository;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/repository/src/java/main/com/nextlabs/language/repository/IDefinitionRepository.java#1 $
 */

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the contract for repositories of definitions.
 *
 * @author Sergey Kalinichenko
 */
public interface IDefinitionRepository {

    /**
     * Given a reference of a specific type and the corresponding class,
     * returns the object to which the reference points.
     *
     * @param <T> The type referenced by the reference.
     * @param path the path to be resolved for a definition.
     * @param refClass the class of the reference's type.
     * @return the definition referenced by the reference.
     * @throws UnresolvedReferenceException when the reference
     * cannot be resolved.
     */
    <T> T getDefinition(
        Path path
    ,   Class<T> refClass
    );

    /**
     * Given a reference of a specific type and the corresponding class,
     * returns the object to which the reference points.
     *
     * @param <T> The type referenced by the reference.
     * @param id the id to be resolved for a definition.
     * @param refClass the class of the reference's type.
     * @return the definition referenced by the reference.
     * @throws UnresolvedReferenceException when the reference
     * cannot be resolved.
     */
    <T> T getDefinition(
        long id
    ,   Class<T> refClass
    );

    /**
     * Given a reference of a specific type and the corresponding class,
     * returns the object to which the reference points.
     *
     * @param <T> The type referenced by the reference.
     * @param ref the reference to be resolved for a definition.
     * @param refClass the class of the reference's type.
     * @return the definition referenced by the reference.
     * @throws UnresolvedReferenceException when the reference
     * cannot be resolved.
     */
    <T> T getDefinition(
        IReference<T> ref
    ,   Class<T> refClass
    );

    /**
     * Determine if the reference passed in exists in the repository.
     *
     * @param <T> the type of the referenced object.
     * @param path the path to be looked up in the repository.
     * @param refClass the class of the reference's type.
     * @return true if the reference can be resolved; false otherwise.
     */
    <T> boolean hasDefinition(
        Path path
    ,   Class<T> refClass
    );

    /**
     * Determine if the reference passed in exists in the repository.
     *
     * @param <T> the type of the referenced object.
     * @param id the id to be looked up in the repository.
     * @param refClass the class of the reference's type.
     * @return true if the reference can be resolved; false otherwise.
     */
    <T> boolean hasDefinition(
        long id
    ,   Class<T> refClass
    );

    /**
     * Determine if the reference passed in exists in the repository.
     *
     * @param <T> the type of the referenced object.
     * @param ref the reference to be looked up in the repository.
     * @param refClass the class of the reference's type.
     * @return true if the reference can be resolved; false otherwise.
     */
    <T> boolean hasDefinition(
        IReference<T> ref
    ,   Class<T> refClass
    );

    <T> Iterable<T> getDefinitions(Class<T> refClass);

}
