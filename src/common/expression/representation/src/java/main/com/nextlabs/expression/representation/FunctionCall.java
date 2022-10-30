package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/FunctionCall.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nextlabs.util.ref.IReference;

/**
 * This class implements a function call.
 *
 * @author Sergey Kalinichenko
 */
public class FunctionCall implements IFunctionCall {

    /**
     * The reference to the function being called.
     */
    private final IReference<IFunction> function;

    /**
     * The map of expressions by name.
     */
    private final Map<String,Argument> byName = new HashMap<String,Argument>();

    /**
     * The list of arguments of this function call.
     */
    private final List<Argument> arguments = new ArrayList<Argument>();

    /**
     * A flag indicating that the arguments of this call have names.
     * Initially set to null, this flag is updated upon the first
     * successful addition of an argument to the function call to match
     * the call style of the argument being added. After that, this field
     * stays constant.
     */
    private Boolean argsHaveNames = null;

    /**
     * Create a function call with the specified reference and no arguments.
     *
     * @param ref the reference to the function being called.
     */
    public FunctionCall(IReference<IFunction> ref) {
        if(ref == null) {
            throw new NullPointerException("reference");
        }
        function = ref;
    }

    /**
     * Create a function call with the specified reference and arguments
     * passed in as an Iterable<Argument>.
     *
     * @param ref the reference to the function being called.
     * @param args the Iterable<Argument> with function arguments.
     */
    public FunctionCall(IReference<IFunction> ref, Iterable<Argument> args) {
        this(ref);
        addArguments(args);
    }

    /**
     * Create a function call with the specified reference and arguments
     * passed in as an array of Argument objects.
     *
     * @param ref the reference to the function being called.
     * @param arguments the array of Argument objects.
     */
    public FunctionCall(IReference<IFunction> ref, Argument ... arguments) {
        this(ref, Arrays.asList(arguments));
    }

    /**
     * Create a function call with the specified reference and arguments
     * passed in as two parallel arrays of names an IExpression objects.
     *
     * @param ref the reference to the function being called.
     * @param argNames the array of argument names.
     * @param exprs the array of argument expressions.
     */
    public FunctionCall(
        IReference<IFunction> ref
    ,   String[] argNames
    ,   IExpression[] exprs
    ) {
        this(ref, convertToArgList(argNames, exprs));
    }

    /**
     * Create a function call with the specified reference and positional
     * arguments passed in as an array of IExpression objects.
     *
     * @param ref the reference to the function being called.
     * @param expressions the array of argument expressions.
     */
    public FunctionCall(
        IReference<IFunction> ref
    ,   IExpression ... expressions
    ) {
        this(ref, convertToArgList(expressions));
    }

    /**
     * Create a function call with the specified reference and arguments
     * passed in as two parallel Iterable objects of names an IExpression
     * objects.
     *
     * @param ref the reference to the function being called.
     * @param argumentNames the Iterable<String> of argument names.
     * @param expressions the Iterable<IExpression> of argument expressions.
     */
    public FunctionCall(
        IReference<IFunction> ref
    ,   Iterable<String> argumentNames
    ,   Iterable<IExpression> expressions) {
        this(ref, convertToArgList(argumentNames, expressions));
    }

    /**
     * Adds a named argument with the specified name and the expression
     * to this function call.
     *
     * @param name the name of the argument to add.
     * @param expression the expression corresponding to the argument.
     */
    public void addArgument(String name, IExpression expression) {
        addArgument(createArgument(name, expression));
    }

    /**
     * Adds a positional argument to this function call.
     *
     * @param expression the expression corresponding to the argument.
     */
    public void addArgument(IExpression expression) {
        addArgument(createArgument(expression));
    }

    /**
     * Adds an argument to this function call.
     *
     * @param arg the argument to be added to this function call.
     */
    public void addArgument(Argument arg) {
        if (arg == null) {
            throw new NullPointerException("arg");
        }
        String name = arg.getName();
        boolean thisArgHasName = (name!=null);
        if (argsHaveNames!=null && thisArgHasName != argsHaveNames) {
            throw new IllegalArgumentException(
                "mixing positional and named arguments is not allowed"
            );
        } else {
            argsHaveNames = thisArgHasName;
        }
        arguments.add(arg);
        if (argsHaveNames) {
            if (byName.containsKey(name)) {
                throw new IllegalArgumentException(
                    "duplicate argument names are not allowed: "+name
                );
            }
            byName.put(name, arg);
        }
    }

    /**
     * Adds multiple arguments to this function call.
     *
     * @param arguments the arguments to be added to this function call.
     */
    public void addArguments(Iterable<Argument> arguments) {
        if (arguments==null) {
            throw new NullPointerException("arguments");
        }
        for(Argument arg : arguments) {
            addArgument(arg);
        }
    }

    /**
     * @see IFunctionCall#hasNamedArguments()
     */
    public boolean hasNamedArguments() {
        return argsHaveNames!=null ? argsHaveNames : false;
    }

    /**
     * @see IFunctionCall#getArgument(int)
     */
    public Argument getArgument(int index) {
        checkArgumentIndex(index);
        return arguments.get(index);
    }

    /**
     * @see IFunctionCall#getArgumentName(int)
     */
    public String getArgumentName(int index) {
        checkArgumentIndex(index);
        return arguments.get(index).getName();
    }

    /**
     * @see IFunctionCall#getExpression(int)
     */
    public IExpression getExpression(int index) {
        checkArgumentIndex(index);
        return arguments.get(index).getExpression();
    }

    /**
     * @see IFunctionCall#getExpression(String)
     */
    public IExpression getExpression(String argumentName) {
        if (argumentName==null) {
            throw new NullPointerException("argumentName");
        }
        Argument arg = byName.get(argumentName);
        return arg!=null ? arg.getExpression() : null;
    }

    /**
     * @see IFunctionCall#getArguments()
     */
    public Iterable<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * @see IFunctionCall#getArgumentCount()
     */
    public int getArgumentCount() {
        return arguments.size();
    }

    /**
     * @see Iterable#iterator()
     */
    public Iterator<IExpression> iterator() {
        return new Iterator<IExpression>() {
            private final Iterator<Argument> inner = arguments.iterator();
            /**
             * @see Iterator#hasNext()
             */
            public boolean hasNext() {
                return inner.hasNext();
            }
            /**
             * @see Iterator#next()
             */
            public IExpression next() {
                return inner.next().getExpression();
            }
            /**
             * @see Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    /**
     * @see IFunctionCall#getFunction()
     */
    public IReference<IFunction> getFunction() {
        return function;
    }

    /**
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitFunction(this);
    }

    /**
     * Returns a String representation of this function call.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer(function.toString());
        res.append('(');
        boolean first = true;
        for (Argument arg : arguments) {
            if (!first) {
                res.append(", ");
            } else {
                first = false;
            }
            res.append(arg);
        }
        res.append(')');
        return res.toString();
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FunctionCall) {
            FunctionCall other = (FunctionCall)obj;
            if (!function.equals(other.getFunction())) {
                return false;
            }
            Iterator<Argument> lhs = arguments.iterator();
            Iterator<Argument> rhs = other.getArguments().iterator();
            while (lhs.hasNext() && rhs.hasNext()) {
                if (!lhs.next().equals(rhs.next())) {
                    return false;
                }
            }
            return lhs.hasNext() == rhs.hasNext();
        } else {
            return false;
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return function.hashCode();
    }

    /**
     * Creates a named argument with the specified name and an expression.
     *
     * @param name the name of the argument being created.
     * @param expr the expression corresponding to this argument.
     * @return a named argument with the specified name and an expression.
     */
    public static Argument createArgument(String name, IExpression expr) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return new ArgumentImpl(name, expr);
    }

    /**
     * Creates a positional argument with the specified expression.
     *
     * @param expression the expression corresponding to this argument.
     * @return a positional argument with the specified expression.
     */
    public static Argument createArgument(IExpression expression) {
        return new ArgumentImpl(expression);
    }

    /**
     * Converts parallel arrays of names and expressions to an Iterable
     * of Argument objects.
     *
     * @param argumentNames an array of argument names.
     * @param expressions an array of argument expressions.
     * @return an Iterable with the specifed arguments.
     */
    private static Iterable<Argument> convertToArgList(
            String[] argumentNames
        ,   IExpression[] expressions
    ) {
        if (argumentNames == null) {
            throw new NullPointerException("argumentNames");
        }
        if (expressions == null) {
            throw new NullPointerException("expressions");
        }
        if (argumentNames.length != expressions.length) {
            throw new IllegalArgumentException(
                "the number of names must match the number of expressions"
            );
        }
        List<Argument> res = new ArrayList<Argument>();
        for (int i = 0 ; i != argumentNames.length ; i++ ) {
            res.add(new ArgumentImpl(argumentNames[i], expressions[i]));
        }
        return res;
    }

    /**
     * Converts an array of expressions to a collection of Argument objects.
     *
     * @param expressions an array of argument expressions.
     * @return an Iterable with the specifed arguments.
     */
    private static Iterable<Argument> convertToArgList(IExpression[] exprs) {
        if (exprs==null) {
            throw new NullPointerException("expressions");
        }
        return convertToArgList(new String[exprs.length], exprs);
    }

    /**
     * Converts parallel Iterable of names and expressions to an Iterable
     * of Argument objects.
     *
     * @param argumentNames an Iterable<String> of argument names.
     * @param expressions an Iterable<IExpression> of argument expressions.
     * @return an Iterable with the specifed arguments.
     */
    private static Iterable<Argument> convertToArgList(
        Iterable<String> argumentNames
    ,   Iterable<IExpression> expressions) {
        if (argumentNames == null) {
            throw new NullPointerException("argumentNames");
        }
        if (expressions == null) {
            throw new NullPointerException("expressions");
        }
        List<Argument> res = new ArrayList<Argument>();
        Iterator<String> names = argumentNames.iterator();
        Iterator<IExpression> exprs = expressions.iterator();
        while (names.hasNext() && exprs.hasNext()) {
            res.add(new ArgumentImpl(names.next(), exprs.next()));
        }
        if (names.hasNext() || exprs.hasNext()) {
            // Both iterators must run out of elements at the same instance:
            throw new IllegalArgumentException(
                "the number of names must match the number of expressions"
            );
        }
        return res;
    }

    /**
     * Ensures that the argument index is within the limits, based on
     * the number of arguments this function call has.
     *
     * @param index the index to be checked.
     */
    private void checkArgumentIndex(int index) {
        if (index < 0 || index >= arguments.size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Implements the Attribute interface of IFunctionCall.
     */
    private static class ArgumentImpl implements Argument {

        /**
         * The name of this argument.
         */
        private final String name;

        /**
         * The expression corresponding to this argument.
         */
        private final IExpression expression;

        /**
         * Creates a new ArgumentImpl with the specified name and expression.
         *
         * @param name the name of the argument.
         * @param expression the expression corresponding to the argument.
         */
        public ArgumentImpl(String name, IExpression expression) {
            if (expression == null) {
                throw new NullPointerException("expression");
            }
            this.name = name;
            this.expression = expression;
        }

        /**
         * Creates a new ArgumentImpl with the specified expression.
         *
         * @param expression the expression corresponding to the argument.
         */
        public ArgumentImpl(IExpression expression) {
            this(null, expression);
        }

        /**
         * @see IFunctionCall.Argument#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see IFunctionCall.Argument#getExpression()
         */
        public IExpression getExpression() {
            return expression;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IFunctionCall.Argument) {
                IFunctionCall.Argument other = (IFunctionCall.Argument)obj;
                if (name != null) {
                    return name.equals(other.getName())
                        && expression.equals(other.getExpression());
                } else {
                    return (other.getName() == null)
                        && expression.equals(other.getExpression());
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
            if (name != null) {
                return name.hashCode() ^ expression.hashCode();
            } else {
                return expression.hashCode();
            }
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            if (name != null) {
                return name+"="+expression;
            } else {
                return expression.toString();
            }
        }

    }

}
