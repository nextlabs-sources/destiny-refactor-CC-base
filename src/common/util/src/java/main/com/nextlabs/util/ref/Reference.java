package com.nextlabs.util.ref;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/ref/Reference.java#1 $
 */

import com.nextlabs.util.Path;

/**
 * This class implements IReference<T>, and provides a protected method
 * for resolving references for subclasses that need to implement
 * reference resolution.
 *
 * @author Sergey Kalinichenko
 */
public class Reference<T> implements IReference<T> {

    /**
     * When the reference is by path, this field holds the Path.
     * When the reference is by id, this field is set to null.
     */
    private final Path path;

    /**
     * When the reference is by id, this field holds the id.
     * When the reference is by path, this field is set to zero.
     */
    private final long id;

    /**
     * This field holds the referenced object. Initially set to null,
     * this field is set to the referenced object at the time the get()
     * method is called for the first time.
     */
    private T referenced;

    /**
     * The class referenced by this reference.
     */
    private final Class<T> refClass;

    /**
     * Creates a base reference in the "by ID" state
     * with the specified id.
     *
     * @param id the referenced ID.
     */
    protected Reference(long id, Class<T> refClass) {
        if (refClass == null) {
            throw new NullPointerException("refClass");
        }
        this.id = id;
        this.path = null;
        this.refClass = refClass;
    }

    /**
     * Creates a base reference in the "by path" state
     * with the specified path.
     *
     * @param path the referenced path.
     */
    protected Reference(Path path, Class<T> refClass) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (refClass == null) {
            throw new NullPointerException("refClass");
        }
        this.path = path;
        this.id = 0;
        this.refClass = refClass;
    }

    /**
     * @see IReference#getId()
     */
    public long getId() {
        if (!isByPath()) {
            return id;
        } else {
            throw new IllegalStateException("getId on reference by Path");
        }
    }

    /**
     * @see IReference#getPath()
     */
    public Path getPath() {
        if (isByPath()) {
            return path;
        } else {
            throw new IllegalStateException("getPath on reference by ID");
        }
    }

    /**
     * @see IReference#isByPath()
     */
    public boolean isByPath() {
        return path != null;
    }

    /**
     * Gets a String representation of this reference.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        if (isByPath()) {
            return "#"+path;
        } else {
            return "id "+id;
        }
    }

    /**
     * @see Object#equals(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IReference)) {
            return false;
        }
        IReference<T> ref = (IReference<T>)obj;
        if (isByPath() == ref.isByPath()) {
            if (isByPath()) {
                return getPath().equals(ref.getPath());
            } else {
                return getId() == ref.getId();
            }
        } else {
            return false;
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (isByPath()) {
            return getPath().hashCode();
        } else {
            long value = getId();
            return (int)(value ^ (value >>> 32));
        }
    }

    /**
     * @see IReference#get()
     */
    public T get() {
        if (referenced == null) {
            referenced = resolve();
        }
        return referenced;
    }

    /**
     * The get() method calls this method to resolve this reference.
     * The default implementation always throws an exception because
     * by default the references are not resolvable. Subclasses
     * representing resolvable references must provide a real implementation
     * of this method.
     *
     * @return the referenced object.
     */
    protected T resolve() {
        throw new IllegalStateException("resolve");
    }

    /**
     * @see IReference#getRefClass()
     */
    public Class<T> getRefClass() {
        return refClass;
    }

    /**
     * The default implementation always returns false, because by default
     * the references are not resolvable. Subclasses representing
     * resolvable references must override this method to return true
     * or false depending on whether or not the reference can be resolved.
     *
     * @see IReference#canResolve()
     */
    public boolean canResolve() {
        return false;
    }

}
