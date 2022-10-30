package com.bluejungle.framework.utils;

/*
 * Created on Feb 21, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/DisjointUnion.java#1 $:
 */

import java.util.ArrayList;
import java.util.List;

/**
 * DisjointUnions (also known as "sum types") are roughly equivalent to
 * unions in C/C++. They allow you to specify that variable is of either
 * type A or B. Most of the time you'd do this with inheritance, but if
 * it turns out that you can't, then disjoint unions are the way to go
 */
public abstract class DisjointUnion<A, B> {
    @SuppressWarnings("unchecked")
    public static <A, B> Left<A, B> makeLeft(A a) {
        return new Left(a);
    }

    @SuppressWarnings("unchecked")
    public static <A, B> Right<A, B> makeRight(B b) {
        return new Right(b);
    }

    public A getLeft() {
        throw new UnsupportedOperationException("getLeft");
    }

    public B getRight() {
        throw new UnsupportedOperationException("getRight");
    }

    public boolean isLeft() {
        return false;
    }

    public boolean isRight() {
        return false;
    }

    public static class Left<A, B> extends DisjointUnion<A, B> {
        private A a;

        public Left(A a) {
            this.a = a;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public A getLeft() {
            return a;
        }
    }

    public static class Right<A, B> extends DisjointUnion<A, B> {
        private B b;

        public Right(B b) {
            this.b = b;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public B getRight() {
            return b;
        }
    }
}
