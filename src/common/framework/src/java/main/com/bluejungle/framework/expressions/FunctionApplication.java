/*
 * Created on Feb 11, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/expressions/FunctionApplication.java#1 $:
 */

package com.bluejungle.framework.expressions;

public class FunctionApplication implements IExpression {

    public IEvalValue evaluate(IArguments arg) {
        return IEvalValue.NULL;
    }

    public IRelation buildRelation (RelationOp op, IExpression rhs) {
        return new Relation(op, this, rhs);
    }

    /**
     * @see com.bluejungle.framework.expressions.IExpression#acceptVisitor(com.bluejungle.framework.expressions.IExpressionVisitor)
     */
    public void acceptVisitor(IExpressionVisitor visitor, IExpressionVisitor.Order order) {
        visitor.visit((FunctionApplication) this);
    }

    public String toString() {
        return "FUNC";
    }   
}
