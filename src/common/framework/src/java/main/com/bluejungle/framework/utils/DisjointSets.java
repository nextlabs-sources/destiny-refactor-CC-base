package com.bluejungle.framework.utils;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/DisjointSets.java#1 $
 */

import java.util.HashMap;
import java.util.Map;

/**
 * DisjointSets data structure maintains a collection of
 * disjoint sets, and supports the common disjoint set
 * operations such as the find and the union.
 *
 * The elements placed in disjoint sets should obey the rules
 * for elements placed in hash sets, i.e. they should provide
 * suitable hashCode and equals.
 *
 * This implementation uses disjoint set forests from CLR's
 * "Introduction to Algorithms", ISBN 0262031418, chapter 22.
 *
 */
public final class DisjointSets<T> {

    /**
     * This class represents a tree node in the forest
     * that represents a disjoint set.
     */
    private static final class Node {
        /**
         * This is the parent node of this node
         * in the tree representing a set.
         */
        private Node parent = this;
        /**
         * This is the rank of the node. It is used in the
         * linking algorithm to decide which node becomes
         * the parent and which one becomes the child.
         */
        private int rank = 0;

        /**
         * This is the size of the subset. It represents
         * the correct size only when parent==this.
         * In all other cases this value must be ignored.
         */
        private int size = 1;

        /**
         * Gets the root of the tree to which this node belongs,
         * and sets that root as the parent to implement the
         * path compression algorithm.
         * @return the root of the tree to which this node belongs.
         * Note: this method implements a two-path non-recursive
         * algorithm instead of a simple recursive lookup
         * from the book to avoid the possibility of overflowing
         * the stack.
         */
        public Node getRoot() {
            Node res = parent;
            while (res != res.parent) {
                res = res.parent;
            }
            Node current = this;
            while (current.parent != res) {
                Node next = current.parent;
                current.parent = res;
                current = next;
            }
            return res;
        }

        /**
         * Links two nodes if they belong to different sets;
         * does nothing if the nodes belong to the same set.
         * @param a the first node to join.
         * @param b the second node to join.
         */
        public static void union(Node a, Node b) {
            assert a!=null && b!=null;
            a = a.getRoot();
            b = b.getRoot();
            if (a == b) {
                return;
            }
            int newSize = a.size+b.size;
            if (a.rank > b.rank) {
                b.parent = a;
                a.size = newSize;
            } else {
                a.parent = b;
                b.size = newSize;
                if (a.rank == b.rank) {
                    b.rank++;
                }
            }
        }

        /**
         * Returns the size of the set to which
         * this <code>Node</code> belongs.
         * @return the size of the set to which
         * this <code>Node</code> belongs.
         */
        public int getSize() {
            return getRoot().size;
        }

    }

    /**
     * A <code>Map</code> of objects entered to the collection
     * of disjoined sets to their corresponding <code>Node</code>
     * objects in the tree forest. Keys of this <code>Map</code>
     * are of type <code>T</code>; elements are of type
     * <code>DisjointSets.Node</code>.
     */
    private Map<T, Node> nodes;

    /**
     * Constructs a <code>DisjointSets</code>
     * with the default initial capacity.
     */
    public DisjointSets() {
        nodes = new HashMap<T, Node>();
    }

    /**
     * Constructs a <code>DisjointSets</code>
     * with the specific initial capacity.
     * @param size the initial capacity of the
     * <code>DisjointSets</code> collection.
     */
    public DisjointSets(int size) {
        nodes = new HashMap<T, Node>(size);
    }

    /**
     * Gets an opaque object representing the given key
     * in the disjoined multisets. If objects a and b are
     * in the same set, then (and only then) get(a)==get(b).
     *
     * Note: the result of this call for the same object
     * is the same only when the collection does not change
     * between the calls; otherwise, a different value
     * may be returned.
     *
     * @param key the object the set for which to look up.
     * @return an opaque object representing the given key
     * in the disjoined multisets.
     */
    public Node get(T key) {
        return getOrCreate(key).getRoot();
    }

    /**
     * Joins the sets to which objects a and b belong.
     * If objects belong to the same set, the operation
     * is ignored.
     * @param a the key of the first set to join.
     * @param b the key of the second set to join.
     */
    public void union(T a, T b) {
        Node nodeA = getOrCreate(a);
        Node nodeB = getOrCreate(b);
        Node.union(nodeA, nodeB);
    }

    /**
     * Returns the size of the set to which the given key belongs.
     * @param key the object the set for which to look up.
     * @return the size of the set to which the given key belongs.
     */
    public int size(T key) {
        return getOrCreate(key).getSize();
    }

    /**
     * Gets the existing node or creates a new one
     * and enters it into the <code>Map</code>.
     * @return the existing <code>Node</code>
     * or a newly created one.
     */
    private Node getOrCreate(T key) {
        Node res = nodes.get(key);
        if (res == null) {
            res = new Node();
            nodes.put(key, res);
        }
        return res;
    }

}
