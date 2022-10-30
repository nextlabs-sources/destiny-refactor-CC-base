package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/CallableType.java#1 $
 */

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Path;
import com.nextlabs.util.Strings;

/**
 * This abstract class is the base of callable definitions of functions
 * and obligations.
 *
 * @author Sergey Kalinichenko
 */
public abstract class CallableType<T extends ICallableType<T>>
                extends AbstractDefinition<T>
                implements ICallableType<T> {

    /**
     * An implementation of the IArgument interface.
     */
    private static class Argument implements IArgument {

        /**
         * The name of this argument.
         */
        private final String name;

        /**
         * The data type of this argument.
         */
        private final IDataType type;

        /**
         * A flag indicating that this argument is required.
         */
        private final boolean required;

        /**
         * An optional default expression for this argument.
         */
        private final IExpression defaultExpr;

        /**
         * Creates an argument with the specified parameters.
         *
         * @param name the name of the argument to create.
         * @param type the type of the argument.
         * @param required the flag specifying that this argument is required.
         * @param defaultExpr an optional default expression of this argument.
         */
        public Argument(
            String name
        ,   IDataType type
        ,   boolean required
        ,   IExpression defaultExpr
        ) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (Strings.isEmpty(name) || !Strings.isTrimmed(name)) {
                throw new IllegalArgumentException("name");
            }
            if (type == null) {
                throw new NullPointerException("type");
            }
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultExpr = defaultExpr;
        }

        /**
         * @see IArgument#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see IArgument#getType()
         */
        public IDataType getType() {
            return type;
        }

        /**
         * @see IArgument#isRequired()
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * @see IArgument#hasDefault()
         */
        public boolean hasDefault() {
            return defaultExpr != null;
        }

        /**
         * @see IArgument#getDefault()
         */
        public IExpression getDefault() {
            return defaultExpr;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Argument)) {
                return false;
            }
            Argument other = (Argument)obj;
            return name.equals(other.getName())
                && type.equals(other.getType())
                && required == other.isRequired()
                && ((defaultExpr==null) ?
                       other.getDefault() == null
                   :   defaultExpr.equals(other.getDefault()));
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer res = new StringBuffer();
            if (required) {
                res.append('+');
            }
            res.append(name);
            res.append(':');
            res.append(type);
            if (defaultExpr != null) {
                res.append('=');
                res.append(defaultExpr);
            }
            return res.toString();
        }

    }

    /**
     * A list of arguments of this callable type.
     */
    private Map<String,IArgument> arguments =
        new LinkedHashMap<String,IArgument>();

    /**
     * Creates the callable definition with the given Path.
     *
     * @param path the path defining the name of this callable definition.
     */
    protected CallableType(Path path) {
        super(path);
    }

    /**
     * Adds an argument to this callable type definition.
     *
     * @param name the name of the argument being added.
     * @param type the type of the argument being added.
     * @param required a flag that determines if the argument is required.
     * @return the newly added argument.
     */
    public IArgument addArgument(
        String name
    ,   IDataType type
    ,   boolean required
    ,   IExpression defaultExpr) {
        if (arguments.containsKey(name)) {
            throw new IllegalArgumentException("duplicate argument:"+name);
        }
        IArgument res;
        arguments.put(
            name
        ,   res = new Argument(
                name
            ,   type
            ,   required
            ,   defaultExpr
            )
        );
        return res;
    }

    /**
     * Removes the specified argument from the argument list of this callable.
     *
     * @param argument the argument to remove.
     */
    public void removeArgument(IArgument argument) {
        if (argument == null) {
            throw new NullPointerException("argument");
        }
        arguments.remove(argument.getName());
    }

    /**
     * Removes the argument with the specified name from the argument list
     * of this callable.
     *
     * @param argumentName the name of the argument to remove.
     * @return the removed argument, or null if it does not exist.
     */
    public IArgument removeArgument(String argumentName) {
        return arguments.remove(argumentName);
    }

    /**
     * @see ICallableType#iterator()
     */
    public Iterator<IArgument> iterator() {
        return arguments.values().iterator();
    }

    /**
     * @see ICallableType#getArgumentCount()
     */
    public int getArgumentCount() {
        return arguments.size();
    }

    /**
     * Adds the argument list to the result buffer.
     *
     * @param res the result buffer to which to add stringified argument list.
     */
    protected void toStringArgList(StringBuffer res) {
        if (arguments.isEmpty()) {
            return;
        }
        res.append('(');
        boolean first = true;
        for (IArgument arg : arguments.values()) {
            if (!first) {
                res.append(", ");
            } else {
                first = false;
            }
            res.append(arg);
        }
        res.append(')');
    }

}
