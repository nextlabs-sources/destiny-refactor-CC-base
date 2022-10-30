package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/Relation.java#1 $
 */

/**
 * This is a representation of a relation predicate.
 *
 * @author Sergey Kalinichenko
 */

public class Relation implements IRelation {

    /** The operator of this relation. */
    private RelationOperator operator;

    /** The expression on the left-hand side of the relation. */
    private IExpression lhs;

    /** The expression on the right-hand side of the relation. */
    private IExpression rhs;

    /**
     * Creates a relation with the specified left and right sides.
     *
     * @param lhs the left-hand side of the relation.
     * @param rhs the right-hand side of the relation.
     */
    public Relation(RelationOperator op, IExpression lhs, IExpression rhs) {
        if (op == null) {
            throw new NullPointerException("operator");
        }
        if (lhs == null) {
            throw new NullPointerException("lhs");
        }
        if (rhs == null) {
            throw new NullPointerException("rhs");
        }
        this.operator = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * @see IRelation#getLHS()
     */
    public IExpression getLHS() {
        return lhs;
    }

    /**
     * Sets the left-hand side of the relation.
     * @param lhs the left-hand side of the relation.
     */
    public void setLHS(IExpression lhs) {
        if (lhs == null) {
            throw new NullPointerException("lhs");
        }
        this.lhs = lhs;
    }

    /**
     * @see IRelation#getRHS()
     */
    public IExpression getRHS() {
        return rhs;
    }

    /**
     * Sets the right-hand side of the relation.
     *
     * @param rhs the right-hand side of the relation.
     */
    public void setRHS(IExpression rhs) {
        if (rhs == null) {
            throw new NullPointerException("rhs");
        }
        this.rhs = rhs;
    }

    /**
     * @see IRelation#getOperator()
     */
    public RelationOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator of the relation.
     *
     * @param rhs the operator of the relation.
     */
    public void setOperator(RelationOperator op) {
        if (op == null) {
            throw new NullPointerException("operator");
        }
        this.operator = op;
    }

    /**
     * @see IPredicate#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitRelation(this);
    }

    /**
     * Gets a String representation of this relation.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append(lhs);
        res.append(' ');
        res.append(operator);
        res.append(' ');
        res.append(rhs);
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
        if (!(obj instanceof Relation)) {
            return false;
        }
        Relation other = (Relation)obj;
        return operator == other.getOperator()
            && lhs.equals(other.getLHS())
            && rhs.equals(other.getRHS());
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return operator.hashCode() ^ lhs.hashCode() ^ rhs.hashCode();
    }

}
