/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/DictionaryPath.java#1 $
 */

package com.bluejungle.dictionary;

import java.io.Serializable;


/**
 * This class is an immutable wrapper around <code>String[]</code> objects.
 * Instances of this class are used as paths in the dictionary.
 */
public class DictionaryPath implements Serializable{

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3946254375456995890L;

	/**
     * This character is used to separate parts of the path
     * formatted as a <code>String</code>.
     */
    public static char SEPARATOR = ',';

   /**
    * This character is used to separate the name from the rest of the path.
    */
   public static char NAME_MARKER = '>';

    /**
     * This <code>DictionaryPath</code> represents the root of the hierarchy.
     */
    public static final DictionaryPath ROOT = new DictionaryPath(
        new String[0], true
    );

    /** This is the <code>String</code> representation
     * of the <code>DictionaryPath</code>.
     */
    private final String[] path;

    /** This is the hash code of this <code>DictionaryPath</code>. */
    private final int hashCode;

    /**
     * Creates an immutable <code>DictionaryPath</code> from a
     * <code>String[]</code> object.
     * @param path the <code>String[]</code> object
     * representing the path. This argument may not be null or empty.
     */
    public DictionaryPath(String[] path) {
        this(path, false);
    }

    /**
     * Creates an immutable <code>DictionaryPath</code> from a
     * <code>String[]</code> object.
     * @param path the <code>String[]</code> object
     * representing the path. This argument may not be null.
     * When unchecked is set to false, path may not be empty either.
     * @param unchecked indicates that an empty path is allowed.
     */
    private DictionaryPath(String[] path, boolean unchecked) {
        if ( path == null ) {
            throw new NullPointerException("path");
        }
        if ( path.length == 0 && !unchecked) {
            throw new IllegalArgumentException("path");
        }
        for ( int i = 0 ; i != path.length ; i++ ) {
            if ( path[i] == null ) {
                throw new NullPointerException("path["+i+"]");
            }
            if (  path[i].length() == 0 ) {
                throw new IllegalArgumentException("path["+i+"]");
            }
        }
        this.path = new String[path.length];
        System.arraycopy(path, 0, this.path, 0, path.length);
        hashCode = calculateHashCode();
    }

    /**
     * Creates an immutable <code>DictionaryPath</code> from a
     * <code>String[]</code> object.
     * @param path the <code>String[]</code> object representing the path.
     */
    public DictionaryPath(String name, DictionaryPath parent) {
        // Since parent's path has been checked in its constructor,
        // there is no need to check parent's path.
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
        path = new String[parent.path.length+1];
        System.arraycopy(parent.path, 0, path, 0, parent.path.length);
        path[parent.path.length] = name;
        hashCode = calculateHashCode();
    }

    /**
     * Appends the last element of the current path to the new parent path.
     * @param parent the new parent path.
     * @return a reparented path based on the new parent.
     */
    public DictionaryPath reparent(DictionaryPath parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        if ( path.length == 0 ) { // This is the ROOT
            throw new IllegalStateException("ROOT path cannot be reparented.");
        }
        // Check if we are trying to reparent at itself or at a child
        // This happens when the new parent path is of the same length or longer,
        // and when all elements of the path being reparented are identical to
        // the corresponding elements of the new path.
        if (parent.path.length >= path.length) {
            boolean identical = false;
            for ( int i = 0 ; identical && i != path.length ; i++ ) {
                identical = (path[i].equals(parent.path[i]));
            }
            if ( identical ) {
                throw new IllegalArgumentException("Attempt to create a circular reference by reparenting at a child.");
            }
        }
        assert path.length > 0; // We checked this at the top of the method
        return new DictionaryPath(path[path.length-1], parent);
    }

    /**
     * Determines if this dictionary path is a parent of another path.
     * A path is not considered a parent of itself.
     *
     * @param other the path to be checked.
     * @return <code>true</code> if this path is a parent of
     * the <code>other</code> path; <code>false</code> otherwise.
     */
    public boolean isParentOf(DictionaryPath other) {
        if (other.length() <= length()) {
            return false;
        }
        for (int i = 0 ; i != path.length ; i++) {
            if (!other.path[i].equals(path[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves this dictionary path from one of its parents
     * to another location. For example, moving "a.b.c" from
     * "a.b" to "x.y.z" results in "x.y.z.c".
     * @param from a parent of this path.
     * @param where the target location (the parent portion will be replaced with this).
     * @return a <code>DictionaryPath</code> moved to the target location.
     */
    public DictionaryPath move(DictionaryPath from, DictionaryPath where) {
        if (!from.isParentOf(this)) {
            return this;
        }
        String[] newPath = new String[where.length()+length()-from.length()];
        System.arraycopy(where.path, 0, newPath, 0, where.path.length);
        System.arraycopy(path, from.length(), newPath, where.length(), length()-from.length());
        return new DictionaryPath(newPath);
    }

    /**
     * Returns the name portion of the path (i.e. the last element).
     * @return the name portion of the path.
     */
    public String getName() {
        if (path.length == 0) {
            throw new IllegalStateException("A call to getName on the ROOT path is not allowed.");
        }
        return path[path.length-1];
    }

    /**
     * Returns the number of segments in this <code>DictionaryPath</code>.
     * @return the number of segments in this <code>DictionaryPath</code>.
     */
    public int length() {
        return path.length;
    }

    /**
     * Replaces the last element of the current path with the given name.
     * @param name the new name.
     * @return a renamed <code>DictionaryPath</code>.
     */
    public DictionaryPath rename(String name) {
        if ( name == null ) {
            throw new NullPointerException("name");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("name is empty");
        }
        String[] newPath = new String[path.length];
        System.arraycopy(path, 0, newPath, 0, path.length-1);
        newPath[newPath.length-1] = name;
        return new DictionaryPath(newPath);
    }

    /**
     * get Path elements as String array
     * @return String[]
     */
    public String[] getPath() {
        return this.path;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return hashCode;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object other) {
        if ( other instanceof DictionaryPath ) {
            DictionaryPath rhs = (DictionaryPath)other;
            if ( rhs.path.length != path.length ) {
                return false;
            }
            for ( int i = 0 ; i != path.length ; i++ ) {
                if ( !path[i].equals(rhs.path[i]) ) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer res = new StringBuffer('[');
        for ( int i = 0 ; i != path.length ; i++ ) {
            if( i != 0 ) { 
                res.append(SEPARATOR);
            }
            res.append( path[i] );
        }
        res.append(']');
        return res.toString();
    }

    /**
     * Filter string does not use NAME_SEPARATOR
     * and adds a wildcard at the end of the path.
     *
     * @param onlyDirect if set, forces an insertion of a name marker
     * before the wildcard to ensure that only direct children are matched.
     * @return a filter string suitable for PQL's 'LIKE' operator.
     */
    public String toFilterString(boolean onlyDirect) {
        StringBuffer res = new StringBuffer();
        boolean foundTemplate = false;
        for ( int i = 0 ; i != path.length ; i++ ) {
            res.append(SEPARATOR);
            foundTemplate |= (path[i].indexOf('%') != -1);
            if (foundTemplate && onlyDirect && i == path.length-1) {
                res.append(NAME_MARKER);
            }
            res.append(path[i]);
        }
        if (!foundTemplate) {
            res.append(DictionaryPath.SEPARATOR);
            if (onlyDirect) {
                res.append(NAME_MARKER);
            }
            res.append('%');
        }
        return res.toString();

    }

    /**
     * Calculates the hash code of this <code>DictionaryPath</code>.
     * This method uses the implementation of the hashcode
     * method of <code>java.langString</code> as of the current JDK.
     * The result for each path component is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using <code>int</code> arithmetic, where <code>s[i]</code> is the
     * <i>i</i>th character of the string, <code>n</code> is the length of
     * the string, and <code>^</code> indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return the hash code of this <code>DictionaryPath</code>.
     */
    private int calculateHashCode() {
        int res = 0;
        for ( int i = 0 ; i != path.length ; i++ ) {
            // Calculate hash code for a path segment
            char val[] = path[i].toCharArray();
            int h = 0;
            for (int j = 0 ; j != val.length ; h = 31*h + val[j++])
                ;
            // Combine the hash code of the segment with the result
            res = 31*res + h;
        }
        return res;
    }


}
