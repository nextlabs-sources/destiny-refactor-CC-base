package com.nextlabs.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/MultiDispatcher.java#1 $
 */

/**
 * This class implements a multiple dispatcher based on the run-time
 * types of method's arguments.
 *
 * @author Sergey Kalinichenko
 */

/*-------------------------------------------------------- *
 * Warning: this class is insanely complex - please ensure *
 * that you understand its details before modifying it.    *
 * ------------------------------------------------------- */

public final class MultiDispatcher {

    /**
     * Private constructor prevents unintentional instantiations.
     */
    private MultiDispatcher() {
    }

    /**
     * This method proxies the interface <code>I</code>, and returns
     * its implementation that relies on the processor class
     * to run the actual method.
     *
     * @param <I> The type of the <code>in</code> argument.
     * @param processorInterface the interface to be proxied.
     * This argument must be an interface with exactly one method.
     * The method must have one or more arguments.
     *
     * @param processorImplementation the processor that implements
     * the functionality defined by the processorInterface.
     * @return the proxied interface.
     * @throws NullPointerException if any of the arguments is null.
     * @throws IllegalArgumentException if the interface has more than
     * one method, or if the method does not have arguments.
     */
    @SuppressWarnings("unchecked")
    public static <I> I create(
        Class<I> processorInterface, Class<?> processorImplementation) {
        if (processorInterface == null) {
            throw new NullPointerException("processorInterface");
        }
        if (processorImplementation == null) {
            throw new NullPointerException("processorImplementation");
        }
        // The interface class must be an interface
        if (!processorInterface.isInterface()) {
            throw new IllegalArgumentException(
                "processorInterface argument must be an interface class."
            );
        }
        if (processorInterface.getMethods().length != 1) {
            throw new IllegalArgumentException(
                "processorInterface must have exactly one method."
            );
        }
        Method interfaceMethod = processorInterface.getMethods()[0];
        String interfaceMethodName = interfaceMethod.getName();
        Class<?> interfaceArgTypes[] = interfaceMethod.getParameterTypes();
        if (interfaceArgTypes.length == 0) {
            throw new IllegalArgumentException(
                "processorInterface's method must take at least one parameter."
            );
        }
        Class<?> interfaceReturn = interfaceMethod.getReturnType();
        // Types of arguments are sorted topologically, with more specific
        // types coming first; ties are resolved by the name of the class.
        final SortedSet<Class<?>> knownArgTypes[] =
            new SortedSet[interfaceMethod.getParameterTypes().length];
        for (int i = 0 ; i != knownArgTypes.length ; i++) {
            knownArgTypes[i] = new TreeSet<Class<?>>(
                new Comparator<Class<?>>() {
                    public int compare(Class<?> lhs, Class<?> rhs) {
                        if (lhs.equals(rhs)) {
                            return 0;
                        }
                        if (lhs.isAssignableFrom(rhs)) {
                            return 1;
                        }
                        if (rhs.isAssignableFrom(lhs)) {
                            return -1;
                        }
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
            );
        }

        // Prepare the method map
        final Map<MultipartKey,Method> methods =
            new HashMap<MultipartKey, Method>();
        for (Method m : processorImplementation.getDeclaredMethods()) {
            // The method must be accessible
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            // The method must be static
            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            // The method must have the same name
            if (!interfaceMethodName.equals(m.getName())) {
                continue;
            }
            // The method must have a matching return
            if (!interfaceReturn.isAssignableFrom(m.getReturnType())) {
                continue;
            }
            Class<?> args[] = m.getParameterTypes();
            // The method needs to have the same number of arguments
            if (interfaceArgTypes.length != args.length) {
                continue;
            }
            // The method arguments must be compatible
            // with the interface arguments
            boolean compatible = true;
            for (int i = 0 ; compatible && i != args.length ; i++) {
                compatible = interfaceArgTypes[i].isAssignableFrom(args[i]);
            }
            if (!compatible) {
                continue;
            }
            // Add argument types to the sets of known types
            for ( int i = 0 ; i != args.length ; i++) {
                knownArgTypes[i].add(args[i]);
            }
            // Add the method to the set of known implementations
            methods.put(new MultipartKey(args), m);
        }

        // The processor must have at least one implementation
        if (methods.isEmpty()) {
            throw new IllegalArgumentException(
                "processorImplementation does not provide implementations "+
                "of processorInterface."
            );
        }

        // The contract of newProxyInstance ensures that
        // the type cast below will always succeed:
        return (I)Proxy.newProxyInstance(
            processorInterface.getClassLoader()
        ,   new Class[] { processorInterface }
        ,   new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] args)
                throws Throwable {
                Class<?>[] argTypes = new Class<?>[args.length];
                for ( int i = 0 ; i != args.length ; i++) {
                    argTypes[i] = (args[i] != null)
                        ? args[i].getClass()
                        : Object.class;
                }
                try {
                    return getMostAplicable(argTypes).invoke(null, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            /**
             * Gets the most applicable method based on the run-time types
             * of the arguments.
             * @param argTypes the types of the arguments passed in.
             * @return the most applicable method based on the run-time types
             * of the arguments.
             */
            private Method getMostAplicable(Class<?>[] argTypes) {
                MultipartKey key = new MultipartKey(argTypes);
                Method res = methods.get(key);
                if (res != null) {
                    return res;
                }
                Class<?>[] formal = new Class<?>[argTypes.length];
                res = find(0, argTypes, formal);
                if (res != null) {
                    methods.put(key, res);
                    return res;
                }
                // A compatible method was not supplied in the processor -
                // prepare a meaningful error message, and throw an IAE.
                StringBuffer errorMessage = new StringBuffer(
                    "Incompatible argument types: "
                );
                boolean isFirst = true;
                for (Class<?> c : argTypes) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        errorMessage.append(", ");
                    }
                    errorMessage.append(c.getName());
                }
                throw new IllegalArgumentException(errorMessage.toString());
            }
            /**
             * This method searches recursively through defined methods,
             * trying to find a combination of argument types for which
             * a method has been defined in the processor.
             *
             * @param n The number of positions of the partial key
             * that have been assigned so far.
             * @param actual the types of the actual parameters.
             * @param formal the partial list of types of formal parameters
             * (filled in up to the position n).
             * @return a matching <code>Method</code>, or null if the partial
             * list of arguments cannot be extended to yield a signature
             * defined in the processor.
             */
            private Method find(int n, Class<?>[] actual, Class<?>[] formal) {
                // Check the termination condition of the recursion
                if (n == actual.length) {
                    // The combination in the formal is complete - check it
                    // against the set of defined methods:
                    return methods.get(new MultipartKey(formal));
                } else {
                    // Continue building the partial key by trying all values
                    // for the corresponding position:
                    for (Class<?> c : knownArgTypes[n]) {
                        // The argument in position n must be compatible
                        // with the formal parameter in the same position.
                        if (c.isAssignableFrom(actual[n])) {
                            formal[n] = c;
                            Method res = find(n+1, actual, formal);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                    return null;
                }
            }
        });
    }

}
