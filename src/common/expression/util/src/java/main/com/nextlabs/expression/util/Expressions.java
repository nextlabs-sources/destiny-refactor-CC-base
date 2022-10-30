package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/main/com/nextlabs/expression/util/Expressions.java#1 $
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.nextlabs.expression.representation.BinaryOperator;
import com.nextlabs.expression.representation.CompositeExpression;
import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.FunctionCall;
import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IExpressionVisitor;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.IUnaryExpression;
import com.nextlabs.expression.representation.Relation;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.util.ref.IReference;

/**
 * This utility class presents a collection of static methods
 * for examining and manipulating predicates and expressions.
 *
 * @author Sergey Kalinichenko
 */
public final class Expressions {

    /**
     * Private constructor prevents instantiation of this class.
     */
    private Expressions() {
    }

    /**
     * Searches the given <code>IExpression</code> with the specified
     * <code>IDetector</code>, and returns the result.
     * @param expression the expression to be searched.
     * @param detector the detector to be used for searching expressions.
     * @return true if the detector returned true for any of the
     * sub-predicates or sub-expressions; false otherwise.
     */
    public static boolean find(
        IExpression expression
    ,   IExpressionDetector detector
    ) {
        if (expression == null) {
            return false;
        }
        SearchingVisitor finder = new SearchingVisitor(
            detector
        );
        expression.accept(finder);
        return finder.found();
    }

    /**
     * Transforms the expression using the provided expression transformer.
     * @param expression the expression to be transformed.
     * @param transformer the transformer that performs the transformation
     * of expressions.
     * @return the transformed expression.
     */
    public static IExpression transform(
        IExpression expression
    ,   IExpressionTransformer transformer
    ) {
        return transform(expression, transformer, false);
    }


    /**
     * Transforms the expression using the provided expression transformer.
     * @param expression the expression to be transformed.
     * @param transformer the transformer that performs the transformation
     * of expressions.
     * @return the transformed expression.
     */
    public static IExpression transform(
        IExpression expression
    ,   IExpressionTransformer transformer
    ,   boolean optimize
    ) {
        if (expression == null) {
            return null;
        }
        TransformingVisitor transformingVisitor = new TransformingVisitor(
            transformer
        ,   optimize
        );
        expression.accept(transformingVisitor);
        return transformingVisitor.getTransformedExpression();
    }

}

/**
 * This class is used in the find() methods to perform the search.
 * It implements the <code>IExpressionVisitor</code> interface,
 * and delegates the actual responsibility of searching to the detectors
 * passed to it in the constructor.
 *
 * @author Sergey Kalinichenko
 */
final class SearchingVisitor implements IExpressionVisitor {

    /**
     * This field keeps the state of the detector.
     * Initially set to false, this field is switched to true
     * when the detector returns true.
     */
    private boolean found = false;

    /**
     * The expression detector of this visitor. This field is set to a non-null
     * value in the constructor, and does not change after that.
     */
    private final IExpressionDetector detector;

    /**
     * The constructor takes two detectors as arguments,
     * and sets them in the internal fields of the class.
     *
     * @param detector the expression detector of this visitor.
     */
    public SearchingVisitor(IExpressionDetector detector) {
        if (detector == null) {
            throw new NullPointerException("expression detector");
        }
        this.detector = detector;
    }

    /**
     * @see IExpressionVisitor#visitRelation(IRelation)
     */
    public void visitRelation(IRelation relation) {
        found |= detector.checkRelation(relation);
        if (!found) {
            relation.getLHS().accept(this);
        }
        if (!found) {
            relation.getRHS().accept(this);
        }
    }

    /**
     * @see IExpressionDetector#visitAttributeReference(
     *      IAttributeReference)
     */
    public void visitAttributeReference(IAttributeReference attrRef) {
        found |= detector.checkAttributeReference(attrRef);
    }

    /**
     * @see IExpressionDetector#visitUnary(IUnaryExpression)
     */
    public void visitUnary(IUnaryExpression unary) {
        found |= detector.checkUnary(unary);
        unary.getOperand().accept(this);
    }

    /**
     * @see IExpressionDetector#visitComposite(ICompositeExpression)
     */
    public void visitComposite(ICompositeExpression composite) {
        found |= detector.checkComposite(composite);
        for (ICompositeExpression.Element element : composite) {
            if (found) {
                break;
            }
            element.getExpression().accept(this);
        }
    }

    /**
     * @see IExpressionDetector#visitConstant(Constant)
     */
    public void visitConstant(Constant constant) {
        found |= detector.checkConstant(constant);
    }

    /**
     * @see IExpressionDetector#visitExpression(IExpression)
     */
    public void visitExpression(IExpression expression) {
        found |= detector.checkExpression(expression);
    }

    /**
     * @see IExpressionDetector#visitFunction(IFunctionCall)
     */
    public void visitFunction(IFunctionCall functionCall) {
        found |= detector.checkFunction(functionCall);
        for (IExpression arg : functionCall) {
            if (found) {
                break;
            }
            arg.accept(this);
        }
    }

    /**
     * @see IExpressionDetector#visitReference(IExpressionReference)
     */
    public void visitReference(IExpressionReference reference) {
        found |= detector.checkReference(reference);
    }

    /**
     * Checks the result of applying the detector.
     * @return true if the detector has returned true for one of
     * the predicates; false otherwise.
     */
    public boolean found() {
        return found;
    }

}

/**
 * This class is used in the transform() methods to perform the transformation.
 * It implements the <code>IExpressionVisitor</code> and
 * <code>IExpressionVisitor</code> interfaces, and delegates the
 * actual responsibility of searching the predicate to the transformers
 * passed to it in the constructor.
 *
 * This visitor uses two stacks to perform the transformations - one for
 * predicates, and one for expressions.
 *
 * @author Sergey Kalinichenko
 */
class TransformingVisitor implements IExpressionVisitor {

    /**
     * The expression stack of this visitor.
     */
    private final Stack<IExpression> stack = new Stack<IExpression>();

    /**
     * The value of the optimize flag passed in the constructor.
     */
    private final boolean optimize;

    /**
     * The expression transformer used in this visitor. This field is set to
     * a non-null value in the constructor, and does not change after that.
     */
    private final IExpressionTransformer transformer;

    /**
     * The constructor takes two transformers as arguments,
     * and sets them in the internal fields of the class.
     * It also sets the value of the optimize flag.
     *
     * @param transformer the expression transformer.
     * @param optimize the flag indicating whether or not to optimize
     * predicates.
     */
    public TransformingVisitor(
        IExpressionTransformer transformer
    ,   boolean optimize
    ) {
        if (transformer == null) {
            throw new NullPointerException("expression transformer");
        }
        this.transformer = transformer;
        this.optimize = optimize;
    }

    /**
     * @see IExpressionVisitor#visitRelation(IRelation)
     */
    public void visitRelation(IRelation relation) {
        relation.getLHS().accept(this);
        IExpression lhs = getTransformedExpression();
        relation.getRHS().accept(this);
        IExpression rhs = getTransformedExpression();
        IRelation transformed;
        if (lhs == relation.getLHS() && rhs == relation.getRHS()) {
            // The transformation did not touch the operands -
            // skip the creation of a cloned Relation:
            transformed = relation;
        } else {
            transformed = new Relation(relation.getOperator(), lhs, rhs);
        }
        push(transformer.transformRelation(relation, transformed));
    }

    /**
     * @see IExpressionVisitor#visitAttributeReference(IAttributeReference)
     */
    public void visitAttributeReference(IAttributeReference attrReference) {
        push(transformer.transformAttributeReference(attrReference));
    }

    /**
     * @see IExpressionVisitor#visitChangeSign(IChangeSign)
     */
    public void visitUnary(IUnaryExpression unary) {
        unary.getOperand().accept(this);
        IExpression operand = getTransformedExpression();
        IExpression res;
        if (optimize && unary.getOperator() == UnaryOperator.NOT) {
            if (operand == IExpression.TRUE) {
                res = IExpression.FALSE;
            } else if (operand == IExpression.FALSE) {
                res = IExpression.TRUE;
            } else if (operand == null) {
                res = null;
            } else {
                res = new UnaryExpression(UnaryOperator.NOT, operand);
            }
        } else {
            if (operand != unary.getOperand()) {
                if (operand == null) {
                    throw new NullPointerException("transformer returned null");
                }
                res = new UnaryExpression(unary.getOperator(), operand);
            } else {
                res = unary;
            }
        }
        push(transformer.transformUnary(unary, res));
    }

    /**
     * @see IExpressionVisitor#visitComposite(ICompositeExpression)
     */
    public void visitComposite(ICompositeExpression composite) {
        BinaryOperator operator = composite.getOperatorAfter(0);
        if (optimize && 
           (operator == BinaryOperator.AND || operator == BinaryOperator.OR)) {
            int size = composite.size();
            boolean seenNull = false;
            boolean seenTrue = false;
            boolean seenFalse = false;
            List<IExpression> children = new ArrayList<IExpression>(size);
            List<IExpression> all = new ArrayList<IExpression>(size);
            for (ICompositeExpression.Element element : composite) {
                element.getExpression().accept(this);
                IExpression lastResult = stack.pop();
                if (lastResult == null) {
                    seenNull = true;
                    continue;
                }
                all.add(lastResult);
                if (lastResult == IExpression.TRUE) {
                    seenTrue = true;
                    if (operator == BinaryOperator.OR && optimize) {
                        break;
                    } else {
                        continue;
                    }
                }
                if (lastResult == IExpression.FALSE) {
                    seenFalse = true;
                    if (operator == BinaryOperator.AND && optimize) {
                        break;
                    } else {
                        continue;
                    }
                }
                children.add(lastResult);
            }
            IExpression res = null;
            if (operator == BinaryOperator.AND) {
                if (seenFalse) {
                    res = IExpression.FALSE;
                } else if (seenTrue && children.isEmpty() && !seenNull) {
                    res = IExpression.TRUE;
                }
            } else { // operator == BooleanOperator.OR - we checked it above
                if (seenTrue) {
                    res = IExpression.TRUE;
                } else if (seenFalse && children.isEmpty() && !seenNull) {
                    res = IExpression.FALSE;
                }
            }
            if (res == null && !children.isEmpty()) {
                if (children.size() == 1) {
                    res = children.get(0);
                } else {
                    List<BinaryOperator> ops = new ArrayList<BinaryOperator>();
                    for (int i = 0 ; i != children.size()-1 ; i++) {
                        ops.add(operator);
                    }
                    res = new CompositeExpression(ops, children);
                }
            }
            push( transformer.transformComposite(composite, res));
        } else {
            boolean foundDifferences = false;
            List<IExpression> children =
                new ArrayList<IExpression>(composite.size());
            List<BinaryOperator> operators =
                new ArrayList<BinaryOperator>(composite.size()-1);
            for (ICompositeExpression.Element element : composite) {
                element.getExpression().accept(this);
                IExpression transformed = getTransformedExpression();
                foundDifferences |= (transformed != element.getExpression());
                children.add(transformed);
                if (!element.isLast()) {
                    operators.add(element.getOperatorAfter());
                }
            }
            push(transformer.transformComposite(
                composite
            ,   foundDifferences ?
                    new CompositeExpression(operators, children) : composite
            ));
        }
    }

    /**
     * @see IExpressionVisitor#visitConstant(Constant)
     */
    public void visitConstant(Constant constant) {
        push(transformer.transformConstant(constant));
    }

    /**
     * @see IExpressionVisitor#visitExpression(IExpression)
     */
    public void visitExpression(IExpression expression) {
        push(transformer.transformExpression(expression));
    }

    /**
     * @see IExpressionVisitor#visitFunction(IFunctionCall)
     */
    public void visitFunction(IFunctionCall function) {
        IReference<IFunction> functionRef = function.getFunction();
        FunctionCall transformed = new FunctionCall(functionRef);
        boolean foundDifferences = false;
        for (final IFunctionCall.Argument arg : function.getArguments()) {
            arg.getExpression().accept(this);
            final IExpression transformedArg = getTransformedExpression();
            foundDifferences |= (transformedArg != arg.getExpression());
            transformed.addArgument(new IFunctionCall.Argument() {
                public IExpression getExpression() {
                    return transformedArg;
                }
                public String getName() {
                    return arg.getName();
                }
                @Override
                public String toString() {
                    if (arg.getName() != null) {
                        return arg.getName()+"="+transformedArg;
                    } else {
                        return transformedArg.toString();
                    }
                }
            });
        }
        push(transformer.transformFunction(
            function
        ,   foundDifferences ? transformed : function
        ));
    }

    /**
     * @see IExpressionVisitor#visitReference(IExpressionReference)
     */
    public void visitReference(IExpressionReference reference) {
        push(transformer.transformReference(reference));
    }

    /**
     * Returns the result of expression transformation.
     *
     * @return the result of expression transformation.
     */
    public IExpression getTransformedExpression() {
        return stack.pop();
    }

    /**
     * Checks the argument for null, and pushes it onto the expression stack.
     *
     * @param expression the expression to push onto the stack.
     */
    private void push(IExpression expression) {
        if (expression == null && !optimize) {
            throw new NullPointerException(
                "Expression transformer returned null"
            );
        }
        stack.push(expression);
    }

}
