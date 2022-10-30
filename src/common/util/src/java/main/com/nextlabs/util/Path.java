package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/Path.java#1 $
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Instances of this class provide immutable representations
 * for paths. This is useful when building references.
 *
 * @author Sergey Kalinichenko
 */
public class Path
       implements Iterable<String>, Externalizable, Comparable<Path> {

    /**
     * This <code>Path</code> represents the root of the hierarchy.
     */
    public static final Path ROOT = new Path();

    /**
     * The separator character for converting paths to string representations.
     */
    private static final char SEPARATOR = ':';

    /**
     * String elements of the path. This array may contain more
     * elements than the actual length of the path.
     */
    private String[] path;

    /**
     * Length of the path.
     */
    private int length;

    /** This is the hash code of this <code>Path</code>. */
    private transient int hashCode;

    /**
     * Indicates that the hash code has been calculated.
     */
    private transient boolean hashCalculated = false;

    /**
     * Creates an immutable <code>Path</code> from an Iterable<String>.
     *
     * @param segments an Iterable with the elements of the path.
     */
    public Path(Iterable<String> segments) {
        if( segments==null ) {
            throw new NullPointerException("segments");
        }
        List<String> tmp = new ArrayList<String>();
        for(String s : segments) {
            checkSegment(s, "segments[i]");
            tmp.add(s);
        }
        if (tmp.isEmpty()) {
            throw new IllegalArgumentException("path");
        }
        length = tmp.size();
        this.path = tmp.toArray(new String[length]);
    }

    /**
     * Creates an immutable <code>Path</code> from a
     * <code>String[]</code> object.
     * @param segments the <code>String[]</code> object
     * representing the path. This argument may not be null.
     * When unchecked is set to false, path may not be empty either.
     * @param unchecked indicates that an empty path is allowed.
     */
    public Path(String ... segments) {
        if ( segments == null ) {
            throw new NullPointerException("segments");
        }
        if ( segments.length == 0) {
            throw new IllegalArgumentException("segments");
        }
        for ( int i = 0 ; i != segments.length ; i++ ) {
            checkSegment(segments[i], "segments[i]");
        }
        length = segments.length;
        this.path = new String[length];
        System.arraycopy(segments, 0, this.path, 0, length);
    }

    /**
     * Creates a Path from a parent Path and a name.
     *
     * @param name the name portion of the path being created.
     * @param parent the parent of the path being created.
     */
    public Path(String name, Path parent) {
        checkSegment(name, "name");
        length = parent.length();
        path = new String[length+1];
        System.arraycopy(parent.path, 0, path, 0, length);
        path[length++] = name;
    }

    /**
     * Creates a subpath of the path that starts at position zero
     * and includes length positions. This constructor is private -
     * it is used in the getParent() method.
     *
     * @param child the child from which we derive the parent path.
     * @param length the desired number of segments in the parent path.
     */
    private Path(Path child, int length) {
        this.path = child.path;
        this.length = length;
    }

    /**
     * Creates a path identical to the ROOT path.
     * This constructor is public as part of an implicit contract of
     * {@link java.io.Externalizable}.
     */
    public Path() {
        this.path = new String[0];
        this.length = 0;
    }

    /**
     * Returns true if this is the ROOT path; returns false otherwise.
     *
     * @return true if this is the ROOT path; returns false otherwise.
     */
    public boolean isRoot() {
        return length==0;
    }

    /**
     * Returns the specified segment of this path.
     *
     * @param index the index of the segment to return.
     * @return the segment at the specified index.
     * @throws IndexOutOfBoundsException when the index is negative or
     * is greater than or equal to the number of elements in this path.
     */
    public String get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index");
        }
        return path[index];
    }

    /**
     * Returns the parent of this path.
     *
     * @return the parent of this path.
     */
    public Path getParent() {
        if (length > 1) {
            return new Path(this, length-1);
        } else {
            return ROOT;
        }
    }

    /**
     * Determines if this path matches another path
     * up to numSegments segments.
     *
     * @param other the path to be checked against this path.
     * @return true if this path matches the other path in all
     * numSegments segments; false otherwise.
     */
    public boolean matches(Path other, int numSegments) {
        if (numSegments < 0) {
            throw new IllegalArgumentException("negative number of segments");
        }
        if (length < numSegments || other.length() < numSegments) {
            return false;
        }
        for (int i = 0 ; i != numSegments ; i++) {
            if (!other.path[i].equalsIgnoreCase(path[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves a portion of this path starting at a specific level
     * to a new parent.
     *
     * Consider moving a:b:c to x:y:z; doing it from level 0
     * results in x:y:z:a:b:c; starting at level 1 results in x:y:z:b:c;
     * starting at level 2 results in x:y:z:c; starting at level 3
     * results in x:y:z (it's a case of degenerate conversion).
     *
     * @param parent a path representing the new parent of a sub-path
     * of this path.
     * @param level the number of initial segments to remove from this path
     * before moving it to the new path.
     * @return a Path representing a portion of this path starting at
     * a specific level moved to the parent path.
     * @throws IllegalArgumentException when the path is moved to itself or
     * to one of its own descendents.
     */
    public Path moveTo(Path parent, int level) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        if (level < 0 || level >= length) {
            throw new IllegalArgumentException("level");
        }
        if (matches(parent, length)) {
            throw new IllegalArgumentException("parent");
        }
        String[] newPath = new String[parent.length()+length-level];
        System.arraycopy(parent.path, 0, newPath, 0, parent.length());
        System.arraycopy(path, level, newPath, parent.length(), length-level);
        return new Path(newPath);
    }

    /**
     * Returns the name portion of the path (i.e. the last element).
     *
     * @return the name portion of the path.
     */
    public String getName() {
        if (length == 0) {
            throw new IllegalStateException(
                "A call to getName on the ROOT path is not allowed."
            );
        }
        return path[length-1];
    }

    /**
     * Returns the number of segments in this <code>Path</code>.
     *
     * @return the number of segments in this <code>Path</code>.
     */
    public int length() {
        return length;
    }

    /**
     * Replaces the last element of the current path with the given name.
     *
     * @param name the new name.
     * @return a renamed <code>Path</code>.
     */
    public Path rename(String name) {
        return new Path(name, getParent());
    }

    /**
     * Returns the hash code of this <code>Path</code>;
     * calculates the hash code if necessary.
     *
     * This method extends the implementation of the hashcode
     * method of <code>String</code> as of the current JDK.
     * The result for each path component is computed using the
     * {@link java.lang.String#hashCode()}, and then the results
     * are combined using the formula
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using <code>int</code> arithmetic, where <code>s[i]</code> is the
     * hash code of the <i>i</i>th segment of the path, <code>n</code> is
     * the length of the path, and <code>^</code> indicates exponentiation.
     * (The hash value of the empty path is zero.)
     *
     * @see Object#hashCode()
     * @see String#hashCode()
     */
    public int hashCode() {
        if (!hashCalculated) {
            for ( int i = 0 ; i != length ; i++ ) {
                // Calculate hash code for this path segment
                char val[] = path[i].toCharArray();
                int h = 0;
                for (int j = 0 ; j != val.length ; j++) {
                    h = 31*h + Character.toLowerCase(val[j]);
                }
                // Combine the hash code of segment[i] with the result
                hashCode = 31*hashCode + h;
            }
            hashCalculated = true;
        }
        return hashCode;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ( other instanceof Path ) {
            Path rhs = (Path)other;
            if ( rhs.length() != length ) {
                return false;
            }
            for ( int i = 0 ; i != length ; i++ ) {
                if ( !path[i].equalsIgnoreCase(rhs.path[i]) ) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Combines path segments into a single String;
     * adds one SEPARATOR between each pair of segments.
     *
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer res = new StringBuffer();
        for ( int i = 0 ; i != length ; i++ ) {
            if( i != 0 ) {
                res.append(SEPARATOR);
            }
            res.append( path[i] );
        }
        return res.toString();
    }

    /**
     * Creates a new iterator that goes through elements of the path
     * up to the full length of this path.
     *
     * @see Iterable#iterator()
     */
    public Iterator<String> iterator() {
        /**
         * This implementation of the iterator goes through path
         * elements, stopping at the set length, which may be lower than the
         * lrngth of the path array.
         */
        return new Iterator<String>() {
            /**
             * @see Iterator#hasNext
             */
            public boolean hasNext() {
                return i != length;
            }
            /**
             * @see Iterator#next
             */
            public String next() {
                return path[i++];
            }
            /**
             * @see Iterator#remove
             */
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
            int i = 0;
        };
    }

    /**
     * Reads the serealized version of this path from an object input stream.
     *
     * @param in the ObjectInput from which to read the path.
     */
    public void readExternal(ObjectInput in) throws IOException {
        length = in.readInt();
        path = new String[length];
        for ( int i = 0 ; i != length ; i++ ) {
            path[i] = in.readUTF();
            checkSegment(path[i], "path["+i+"]");
        }
    }

    /**
     * Writes this Path into an ObjectOutput.
     *
     * @param out the ObjectOutput to which to write this path.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        }
        out.writeInt(length);
        for (String segment : this) {
            out.writeUTF(segment);
        }
    }

    /**
     * Prevents cloning of the ROOT Path through serialization
     * by controlling the instance returned for zero-length paths.
     *
     * @return the ROOT path instance if the deserialized length is zero.
     */
    private Object readResolve() {
        if (length != 0) {
            return this;
        } else {
            return ROOT;
        }
    }

    /**
     * Compares two path lexicographically.
     *
     * @param other the other path to which this path is being compared.
     * @return a negative number if this path preceeds the other path
     * lexicographically, a positive number if the other path preceeds
     * this path lexicographically, and zero if the two paths are equal.
     */
    public int compareTo(Path other) {
        if (other == null) {
            throw new NullPointerException("other");
        }
        Iterator<String> otherIter = other.iterator();
        for (String s : this) {
            if (!otherIter.hasNext()) {
                return 1;
            }
            int cmp = s.compareToIgnoreCase(otherIter.next());
            if (cmp != 0) {
                return cmp;
            }
        }
        return otherIter.hasNext() ? -1 : 0;
    }

    /**
     * Validates the segment of a path for being null or empty.
     *
     * @param segment the segment that needs to be checked.
     * @param i the number of this segment for error reporting purposes.
     */
    private static void checkSegment(String segment, String error) {
        if ( segment == null ) {
            throw new NullPointerException(error);
        }
        if ( Strings.isEmpty(segment) || !Strings.isTrimmed(segment)) {
            throw new IllegalArgumentException(error);
        }
    }

}
