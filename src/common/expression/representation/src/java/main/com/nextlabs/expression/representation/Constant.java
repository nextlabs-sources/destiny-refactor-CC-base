package com.nextlabs.expression.representation;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/Constant.java#1 $
 */

/**
 * This class implements a constant expression.
 *
 * @author Sergey Kalinichenko
 */
public class Constant implements IExpression {

    /** The value of this constant. */
    private final Object value;

    /** The type of this constant. */
    private final IDataType type;

    /**
     * Creates a constant with the specified value.
     *
     * This constructor is left package-private for use in IExpression.
     *
     * @param value the value of the constant.
     */
    Constant(Object value, IDataType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitConstant(this);
    }

    /**
     * Returns the value of this constant.
     * @return the value of this constant.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the type of this constant.
     * @return the type of this constant.
     */
    public IDataType getType() {
        return type;
    }

    /**
     * Constant equality is based on the equality of their values.
     * Note that the type matters: for example, 1L != 1F.
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Constant)) {
            return false;
        }
        Constant other = (Constant)obj;
        if (value != null) {
            return value.equals(other.value);
        } else {
            return other.value == null;
        }
    }

    /**
     * Returns has code of the value, or 0 for null values.
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    /**
     * Gets a String representation of this constant.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return toString(value);
    }

    private static String toString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\""+value+"\"";
        } else if (value instanceof Iterable<?>) {
            StringBuffer res = new StringBuffer();
            res.append('(');
            boolean isFirst = true;
            for (Object val : (Iterable<?>)value) {
                if (!isFirst) {
                    res.append(", ");
                } else {
                    isFirst = false;
                }
                res.append(toString(val));
            }
            res.append(')');
            return res.toString();
        } else {
            return value.toString();
        }
    }

    /**
     * Factory method for making constants with case-insensitive strings.
     *
     * @param value the string value of the constant.
     * @return a <code>Constant</code> of type <code>IDataType.STRING</code>
     * with the specified value.
     */
    public static Constant makeString(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return new Constant(value, IDataType.STRING);
    }

    /**
     * Factory method for making constants with case-sensitive strings.
     *
     * @param value the string value of the constant.
     * @return a <code>Constant</code> of type <code>IDataType.CS_STRING</code>
     * with the specified value.
     */
    public static Constant makeCsString(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return new Constant(value, IDataType.CS_STRING);
    }

    /**
     * Factory method for making constants with double-precision values.
     *
     * @param value the double-precision value of the constant.
     * @return a <code>Constant</code> of type <code>IDataType.DOUBLE</code>
     * with the specified value.
     */
    public static Constant makeDouble(double value) {
        return new Constant(value, IDataType.DOUBLE);
    }

    /**
     * Factory method for making constants with double-precision values.
     *
     * @param value the 64-bit integer value of the constant.
     * @return a <code>Constant</code> of type <code>IDataType.INTEGER</code>
     * with the specified value.
     */
    public static Constant makeInteger(long value) {
        return new Constant(value, IDataType.INTEGER);
    }

    /**
     * Factory method for making constants with <code>Date</code> values.
     *
     * @param value the <code>Date</code> value of the constant.
     * @return a <code>Constant</code> of type <code>IDataType.DATE</code>
     * with the specified value.
     */
    public static Constant makeDate(Date value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return new Constant(value, IDataType.DATE);
    }

    /**
     * Factory method for making multivalued constants.
     *
     * @param constants the constants from which to make a multivalued
     * <code>Constant</code>. All constants in the <code>Iterable</code>
     * must share the same type, or be of type <code>IDataType.NULL</code>.
     * @return a <code>Constant</code> of multivalued type with the base
     * type of the constants from the <code>Iterable</code>. The element type
     * of empty multivalued constants is <code>IDataType.NULL</code>.
     */
    public static Constant makeMultivalue(Iterable<Constant> constants) {
        if (constants == null) {
            throw new NullPointerException("constants");
        }
        Set<Object> values = new LinkedHashSet<Object>();
        IDataType type = null;
        for (Constant c : constants) {
            if (type != null) {
                IDataType specificType = specificType(type, c.getType());
                if (specificType == null) {
                    throw new IllegalArgumentException("constant types");
                }
                type = specificType;
            } else {
                type = c.getType();
            }
            values.add(c.getValue());
        }
        if (type == null) {
            type = IDataType.NULL;
        }
        // Lists of mixed integer/double type are allowed
        if (type == IDataType.DOUBLE) {
            Set<Object> tmp = new LinkedHashSet<Object>();
            for (Object number : values) {
                if (number != null) {
                    tmp.add(((Number)number).doubleValue());
                } else {
                    tmp.add(null);
                }
            }
            values = tmp;
        }
        return new Constant(
            Collections.unmodifiableSet(values)
        ,   DataType.makeMultivalue(type)
        );
    }

    /**
     * Makes a constant with the specified value and type.
     *
     * @param value the value of the constant.
     * @param type the type of the constant.
     * @return a constant with the specified value and type.
     */
    public static Constant make(Object value, IDataType type) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        return new Constant(value, type);
    }

    /**
     * Makes a multivalued <code>Constant</code> of the specified values,
     * with the given value type.
     *
     * @param vals an <code>Iterable</code> of items for the multivalued
     * <code>Constant</code>.
     * @param type the type of values on the list (the method does not check
     * the values against the type).
     * @return a multivalued <code>Constant</code> with the specified values
     * and the type based on the element type.
     */
    public static Constant makeMultivalue(Iterable<?> vals, IDataType type) {
        if (vals == null) {
            throw new NullPointerException("values");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        return new Constant(vals, DataType.makeMultivalue(type));
    }

    /**
     * Checks compatibility of item types, and returns the most specific type
     * describing the pair.
     * The types are compatible if:
     * - they are equal, or
     * - one of them is <code>IDataType.NULL</code>, or
     * - both values are of <code>IMultivalueDataType</code> with compatible
     * inner types.
     *
     * If the types are equal, the lhs is considered the most specific;
     * If one of the types is <code>IDataType.NULL</code> or is a multivalued
     * type describing an empty/null list, a list of empty/null lists, etc.,
     * the other type is considered the most specific.
     *
     * @param lhs the first type to check for compatibility; will not be null.
     * @param rhs the second type to check for compatibility; will not be null.
     * @return the most specific type of the pair.
     */
    private static IDataType specificType(IDataType lhs, IDataType rhs) {
        IDataType a = lhs;
        IDataType b = rhs;
        while (a.isMultivalue() && b.isMultivalue()) {
            a  = a.asMultivalue().getInnerType();
            b  = b.asMultivalue().getInnerType();
        }
        if (a.isNull()) {
            return rhs;
        } else if (b.isNull() || a.equals(b)) {
            return lhs;
        } else if (a.isDouble() && b.isInteger()) {
            return lhs;
        } else if (a.isInteger() && b.isDouble()) {
            return rhs;
        } else {
            return null;
        }
    }

}
