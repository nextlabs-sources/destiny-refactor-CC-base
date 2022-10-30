package com.nextlabs.expression.representation;

public interface IRelation extends IExpression {

    /**
     * Returns the operator of this relation.
     *
     * @return the operator of this relation.
     */
    RelationOperator getOperator();

    /**
     * Returns the left-hand side of the relation.
     *
     * @return the left-hand side of the relation.
     */
    IExpression getLHS();

    /**
     * Returns the right-hand side of the relation.
     *
     * @return the right-hand side of the relation.
     */
    IExpression getRHS();

}