package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/AbstractDefinition.java#1 $
 */

import com.nextlabs.util.Path;

/**
 * This is an abstract implementation of the IDefinition interface.
 * It provides the holders for the Path and the description.
 *
 * @author Sergey Kalinichenko
 */
abstract class AbstractDefinition<T extends IDefinition<T>>
         implements IDefinition<T> {

    /**
     * The path defining the name of of this definition.
     */
    private Path path;

    /**
     * The description of this definition.
     */
    private String description;

    /**
     * Creates the definition with the given Path.
     *
     * @param path the path defining the name of this definition.
     */
    protected AbstractDefinition(Path path) {
        checkPath(path);
        this.path = path;
    }

    /**
     * Returns the path to this definition.
     *
     * @return the path to this definition.
     * @see IDefinition#getPath()
     */
    public Path getPath() {
        return path;
    }

    /**
     * Sets the Path of this definition.
     *
     * @param path the new Path of this definition.
     * @see IDefinition#setPath(Path)
     */
    public void setPath(Path path) {
        checkPath(path);
        this.path =  path;
    }

    /**
     * Returns an optional description associated with this definition.
     *
     * @return an optional description associated with this definition.
     * @see IDefinition#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this definition.
     *
     * @param description the new description of this definition.
     * @see IDefinition#setDescription(String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Determines if this definition has a description.
     *
     * @return true if the definition has a description; false otherwise.
     */
    public boolean hasDescription() {
        return description != null;
    }

    /**
     * Checks the path of this definition for null.
     *
     * @param path the path to be checked.
     */
    private static void checkPath(Path path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractDefinition)) {
            return false;
        }
        AbstractDefinition<?> other = (AbstractDefinition<?>)obj;
        if (path.equals(other.getPath())) {
            if (hasDescription()) {
                return description.equals(other.getDescription());
            } else {
                return !other.hasDescription();
            }
        } else {
            return false;
        }
    }

}
