package com.nextlabs.language.repository;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/repository/src/java/main/com/nextlabs/language/repository/AbstractDefinitionRepository.java#1 $
 */

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;

/**
 * This is the abstract base class for definition repositories
 * @author Sergey Kalinichenko
 */
public abstract class AbstractDefinitionRepository
                implements IDefinitionRepository {

    /**
     * @see IDefinitionRepository#getDefinition(Path, Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T getDefinition(
        Path path
    ,   Class<T> refClass
    ) {
        nullCheck(path, "path");
        nullCheck(refClass, "referenceClass");
        Object res = findDefinition(path);
        // The check below ensures that the returned instance is
        // assignment-compatible with the desired class at runtime,
        // making it OK to suppress the warning for the method.
        if (refClass.isInstance(res)) {
            return (T)res;
        } else {
            return null;
        }
    }

    /**
     * @see IDefinitionRepository#getDefinition(long, Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T getDefinition(
        long id
    ,   Class<T> refClass
    ) {
        nullCheck(refClass, "referenceClass");
        Object res = findDefinition(id);
        if (refClass.isInstance(res)) {
            return (T)res;
        } else {
            return null;
        }
    }

    /**
     * @see IDefinitionRepository#getDefinition(IReference, Class)
     */
    public <T> T getDefinition(
        IReference<T> reference
    ,   Class<T> refClass
    ) {
        nullCheck(reference, "reference");
        if (reference.isByPath()) {
            return getDefinition(reference.getPath(), refClass);
        } else {
            return getDefinition(reference.getId(), refClass);
        }
    }

    /**
     * @see IDefinitionRepository#hasDefinition(Path, Class)
     */
    public <T> boolean hasDefinition(
        Path path
    ,   Class<T> refClass
    ) {
        nullCheck(path, "path");
        nullCheck(refClass, "referenceClass");
        return refClass.isInstance(findDefinition(path));
    }

    /**
     * @see IDefinitionRepository#hasDefinition(long, Class)
     */
    public <T> boolean hasDefinition(
        long id
    ,   Class<T> refClass
    ) {
        nullCheck(refClass, "referenceClass");
        return refClass.isInstance(findDefinition(id));
    }

    /**
     * @see IDefinitionRepository#hasDefinition(IReference, Class)
     */
    public <T> boolean hasDefinition(
        IReference<T> reference
    ,   Class<T> refClass
    ) {
        nullCheck(reference, "reference");
        if (reference.isByPath()) {
            return hasDefinition(reference.getPath(), refClass);
        } else {
            return hasDefinition(reference.getId(), refClass);
        }
    }

    protected abstract Object findDefinition(Path path);

    protected abstract Object findDefinition(long id);

    protected static final void nullCheck(Object obj, String errorString) {
        if (obj == null) {
            throw new NullPointerException(errorString);
        }
    }

}
