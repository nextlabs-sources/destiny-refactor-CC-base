package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/CompositeExpression.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author Sergey Kalinichenko
 */
public class CompositeExpression implements ICompositeExpression {

    /**
     * Stores the precedence of the operators in this composite expression.
     * This field is set in the constructor to the value of the initial
     * operator in the chain, and does not change after that.
     */
    private final int operatorPrecedence;

    /**
     * This is the list of operators of this composite expression.
     */
    private final List<BinaryOperator> operators =
        new ArrayList<BinaryOperator>();

    /**
     * This is the list of expressions of this composite expression.
     */
    private final List<IExpression> expressions =
        new ArrayList<IExpression>();

    /**
     * This is the cached hash code of this composite. Mutating methods set it
     * to null; hashCode method sets it to a non-null value.
     */
    private Integer hashCode = null;

    /**
     * Creates a composite expression from individual components
     * and operators.
     *
     * @param operators operators to place between the elements.
     * The number of operators must be one less than the number of elements.
     * There must be at least one operator, and all operators must be
     * compatible for use in a single composite expression.
     * @param expressions the expressions composing the composite expression.
     * The number of elements must be greater by one than the number
     * of operators.
     */
    public CompositeExpression(
        Iterable<BinaryOperator> operators
    ,   Iterable<IExpression> expressions) {
        if (operators==null) {
            throw new NullPointerException("operators");
        }
        if (expressions==null) {
            throw new NullPointerException("elements");
        }
        int precedence = -1;
        for (BinaryOperator operator : operators) {
            if (operator == null) {
                throw new NullPointerException("operator[i]");
            }
            if (precedence != -1) {
                if (operator.getPrecedence() != precedence) {
                    throw new IllegalArgumentException(
                        "incompatible operators in a composite expression"
                    );
                }
            } else {
                precedence = operator.getPrecedence();
            }
            this.operators.add(operator);
        }
        operatorPrecedence = precedence;
        for (IExpression expression : expressions) {
            checkExpression(expression);
            this.expressions.add(expression);
        }
        if (this.operators.isEmpty()
         || this.operators.size()!=this.expressions.size()-1) {
            throw new IllegalArgumentException(
                "'operators' is empty or has an incorrect number of elements"
            );
        }
    }

    /**
     * Creates a composite expression from individual components
     * and operators.
     *
     * @param operators operators to place between the elements.
     * The number of operators must be one less than the number of elements.
     * There must be at least one operator, and all operators must be
     * compatible for use in a single composite expression.
     * @param expressions the expressions composing the composite expression.
     * The number of elements must be greater by one than the number
     * of operators.
     */
    public CompositeExpression(
        BinaryOperator[] operators
    ,   IExpression[] expressions
    ) {
        this(Arrays.asList(operators), Arrays.asList(expressions));
    }

    /**
     * @see ICompositeExpression#getExpression(int)
     */
    public IExpression getExpression(int index) {
        return expressions.get(index);
    }

    /**
     * @see ICompositeExpression#getOperatorBefore(int)
     */
    public BinaryOperator getOperatorBefore(int index) {
        if (index != 0) {
            return operators.get(index-1);
        } else {
            return null;
        }
    }

    /**
     * @see ICompositeExpression#getOperatorAfter(int)
     */
    public BinaryOperator getOperatorAfter(int index) {
        if (index != size()-1) {
            return operators.get(index);
        } else {
            return null;
        }
    }

    /**
     * Sets the expression at the specified index.
     *
     * @param index the index at which to set the expression.
     * @param expression the expression to set at the specified index.
     */
    public void setExpression(int index, IExpression expression) {
        checkExpression(expression);
        expressions.set(index, expression);
        hashCode = null;
    }

    /**
     * Sets the operator before the expression at the specified index.
     * @param index the index before which to set the operator.
     * @param operator the operator to set at the specified position.
     */
    public void setOperatorBefore(int index, BinaryOperator operator) {
        checkOperator(operator);
        if (index != 0) {
            operators.set(index-1, operator);
        } else {
            throw new IndexOutOfBoundsException("set operators before 0");
        }
        hashCode = null;
    }

    /**
     * Sets the operator after the expression at the specified index.
     * @param index the index after which to set the operator.
     * @param operator the operator to set at the specified position.
     */
    public void setOperatorAfter(int index, BinaryOperator operator) {
        checkOperator(operator);
        if (index != size()-1) {
            operators.set(index, operator);
        } else {
            throw new IndexOutOfBoundsException("set operators after last");
        }
        hashCode = null;
    }

    /**
     * @see ICompositeExpression#size()
     */
    public int size() {
        return expressions.size();
    }

    /**
     * @see Iterable#iterator()
     */
    public Iterator<ICompositeExpression.Element> iterator() {
        return new Iterator<Element>() {
            int index = 0;
            /**
             * @see Iterator#hasNext()
             */
            public boolean hasNext() {
                return index != expressions.size();
            }
            /**
             * @see Iterator#next()
             */
            public Element next() {
                if (!hasNext()) {
                    throw new NoSuchElementException(""+index);
                }
                return new Element() {
                    final int i =index++;
                    /**
                     * @see Element#getExpression()
                     */
                    public IExpression getExpression() {
                        return expressions.get(i);
                    }
                    /**
                     * @see Element#isFirst()
                     */
                    public boolean isFirst() {
                        return i == 0;
                    }
                    /**
                     * @see Element#isLast()
                     */
                    public boolean isLast() {
                        return i==size()-1;
                    }
                    /**
                     * @see Element#getOperatorAfter()
                     */
                    public BinaryOperator getOperatorAfter() {
                        if (!isLast()) {
                            return operators.get(i);
                        } else {
                            return null;
                        }
                    }
                    /**
                     * @see Element#getOperatorBefore()
                     */
                    public BinaryOperator getOperatorBefore() {
                        if (!isFirst()) {
                            return operators.get(i-1);
                        } else {
                            return null;
                        }
                    }
                };
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
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitComposite(this);
    }

    /**
     * Adds an operator/expression pair to the end of the composite.
     *
     * @param operator the operator to be added.
     * @param expression the element to be added.
     */
    public void add(
        BinaryOperator operator
    ,   IExpression expression) {
        insertAfter(size()-1, operator, expression);
    }

    /**
     * Inserts an operator/expression pair into the composite
     * after the specified index.
     *
     * @param index the index before which to do the insertion. The valid range
     * for the index is 0..size, inclusive. Passing size as the index
     * is identical to adding the expression at the end.
     * @param operator the operator to be inserted.
     * @param expression the element to be inserted.
     */
    public void insertAfter(
        int index
    ,   final BinaryOperator operator
    ,   final IExpression expression) {
        checkExpression(expression);
        checkOperator(operator);
        expressions.add(index+1, expression);
        operators.add(index, operator);
        hashCode = null;
    }

    /**
     * Inserts an operator/expression pair into the composite
     * at the specified index.
     *
     * @param index the index before which to do the insertion. The valid range
     * for the index is 0..size, inclusive. Passing size as the index
     * is identical to adding the expression at the end.
     * @param operator the operator to be inserted.
     * @param expression the element to be inserted.
     */
    public void insertBefore(
        int index
    ,   final IExpression expression
    ,   final BinaryOperator operator
    ) {
        checkExpression(expression);
        checkOperator(operator);
        expressions.add(index, expression);
        operators.add(index, operator);
        hashCode = null;
    }

    /**
     * Removes the operator and the expression at the specified index.
     *
     * @param index the index of the element to remove.
     * @throws IllegalStateException when the remaining expression would
     *  no longer be a composite (i.e. would have just one element).
     */
    public void removeBefore(int index) {
        if (size() == 2) {
            throw new IllegalStateException(
                "removing the last operator of a composite is not allowed"
            );
        }
        expressions.remove(index);
        operators.remove(index-1);
        hashCode = null;
    }

    /**
     * Removes the operator and the expression at the specified index.
     *
     * @param index the index of the element to remove.
     * @throws IllegalStateException when the remaining expression would
     *  no longer be a composite (i.e. would have just one element).
     */
    public void removeAfter(int index) {
        if (size() == 2) {
            throw new IllegalStateException(
                "removing the last operator of a composite is not allowed"
            );
        }
        expressions.remove(index);
        operators.remove(index);
        hashCode = null;
    }

    /**
     * Gets a String representation of this composite expression.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append('(');
        for (Element e : this) {
            res.append(e.getExpression());
            if (!e.isLast()) {
                res.append(' ');
                res.append(e.getOperatorAfter());
                res.append(' ');
            }
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
        if (!(obj instanceof CompositeExpression)) {
            return false;
        }
        CompositeExpression other = (CompositeExpression)obj;
        Iterator<BinaryOperator> lhsOp = operators.iterator();
        Iterator<BinaryOperator> rhsOp = other.operators.iterator();
        while (lhsOp.hasNext() && rhsOp.hasNext()) {
            if (!lhsOp.next().equals(rhsOp.next())) {
                return false;
            }
        }
        if (lhsOp.hasNext() != rhsOp.hasNext()) {
            return false;
        }
        Iterator<IExpression> lhs = expressions.iterator();
        Iterator<IExpression> rhs = other.expressions.iterator();
        while (lhs.hasNext() && rhs.hasNext()) {
            if (!lhs.next().equals(rhs.next())) {
                return false;
            }
        }
        // At this point we know that both iterators ran out of elements
        // at the same time, because when the operator counts match,
        // the expression counts must always match too.
        return true;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (hashCode == null) {
            int h = 0;
            for (BinaryOperator op : operators) {
                h *= 31;
                h ^= op.hashCode();
            }
            for (IExpression exp : expressions) {
                h *= 31;
                h ^= exp.hashCode();
            }
            hashCode = h;
        }
        return hashCode;
    }

    /**
     * Checks the expression for null.
     *
     * @param expression the expression to be checked.
     */
    private void checkExpression(IExpression expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        expression.accept(new DefaultExpressionVisitor() {
            @Override
            public void visitComposite(ICompositeExpression composite) {
                if (composite == CompositeExpression.this) {
                    throw new IllegalArgumentException("circular expression");
                }
                for (ICompositeExpression.Element element : composite) {
                    element.getExpression().accept(this);
                }
            }
        });
    }

    /**
     * Checks the operator for compatibility with the expression.
     *
     * @param operator the operator to be checked.
     */
    private void checkOperator(BinaryOperator operator) {
        if (operator == null) {
            throw new NullPointerException("operator");
        }
        if (operator.getPrecedence() != operatorPrecedence) {
            throw new IllegalArgumentException(
                "adding an incompatible operator into a composite"
            );
        }
    }

}
