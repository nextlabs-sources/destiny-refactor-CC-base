package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/Evaluator.java#1 $
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import com.nextlabs.expression.representation.BinaryOperator;
import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.ICodeDataType;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IDataTypeVisitor;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IExpressionVisitor;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IMultivalueDataType;
import com.nextlabs.expression.representation.IReferenceDataType;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.IUnaryExpression;
import com.nextlabs.expression.representation.RelationOperator;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.util.WildcardPattern;
import com.nextlabs.util.WildcardPattern.CaseSensitivity;

/**
 * This is an implementation of the evaluator for expressions and predicates.
 *
 * Instead of using the JVM's stack to evaluate formulae recursively, this
 * implementation creates two "parallel" stacks (for values and for their
 * data types), and uses these stacks to evaluate expressions.
 *
 * Since this evaluator works both for expressions and predicates,
 * a separate stack is created for boolean results of predicate evaluation.
 *
 * This class is final, and is not designed to be extended.
 *
 * Instances of this class must not be used concurrently. Instead, each thread
 * has its own instance of this class. @see {@link Evaluator#getInstance()} for
 * more details.
 *
 * @author Sergey Kalinichenko
 */
public final class Evaluator
    implements IEvaluator
             , IEvaluatorCallback
             , IExpressionVisitor
             , IDataTypeVisitor {

    /**
     * This interface defines the contract for evaluators
     * of unary operators (such as the change of sign operator).
     */
    private interface IUnary {
        void process(
            Object operand
        ,   IDataType type
        ,   IEvaluatorCallback callback
        );
    }

    /**
     * This interface defines the contract for evaluators
     * of binary operators (relations and arithmetic operators).
     */
    private interface IBinary {
        void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback callback
        );
    }

    // Indexes of data types used internally for implementing
    // multiple dispatch in the Evaluator.

    /** Index of IDataType.NULL. */
    private static final int NULL_INDEX = 0;

    /** Index of IDataType.BOOLEAN. */
    private static final int BOOLEAN_INDEX = 1;

    /** Index of IDataType.DOUBLE. */
    private static final int DOUBLE_INDEX = 2;

    /** Index of IDataType.INTEGER. */
    private static final int INTEGER_INDEX = 3;

    /** Index of IDataType.DATE. */
    private static final int DATE_INDEX = 4;

    /** Index of IDataType.STRING (case-insensitive). */
    private static final int STRING_INDEX = 5;

    /** Index of IDataType.CS_STRING (case-sensitive). */
    private static final int CS_STRING_INDEX = 6;

    /** Index of ICodeDataType. */
    private static final int CODE_INDEX = 7;

    /** Index of IMultivalueDataType. */
    private static final int MULTIVALUE_INDEX = 8;

    /** Index of IReferenceDataType. */
    private static final int REFERENCE_INDEX = 9;

    /** Index of unknown data types. */
    private static final int UNKNOWN_INDEX = 10;

    /** Count of distinct data type indexes. */
    private static final int NUM_ARITH_TYPES = 4;

    /** Count of distinct data type indexes. */
    private static final int NUM_REL_TYPES = 11;

    /**
     * Processors for change sign operators of different types.
     */
    private static final IUnary[] CHANGE_SIGN_OPERATION =
        new IUnary[NUM_ARITH_TYPES];

    /**
     * Processors for arithmetic operators.
     */
    private static final IBinary[][][] ARITH_PROCESSOR =
        new IBinary[BinaryOperator.size()]
                   [NUM_ARITH_TYPES]
                   [NUM_ARITH_TYPES];

    /**
     * Processors of relation operators.
     */
    private static final IBinary[][][] RELATION_PROCESSOR =
        new IBinary[RelationOperator.size()]
                   [NUM_REL_TYPES]
                   [NUM_REL_TYPES];

    /** Initial size of the expression stack. */
    private static final int INITIAL_EXPRESSION_STACK_SIZE = 10;

    /** Stored evaluation handler passed in on the current call to evaluate. */
    private IEvaluationHandler handler;

    /**
     * This array is used as a stack of values while evaluating expressions.
     */
    private Object[] stValues = new Object[INITIAL_EXPRESSION_STACK_SIZE];

    /**
     * This array is used as a stack of types while evaluating expressions.
     */
    private IDataType[] stTypes = new IDataType[INITIAL_EXPRESSION_STACK_SIZE];

    /**
     * This is the stack pointer for the value and the type stacks.
     */
    private int exprSP;

    /**
     * This is the index of the data type calculated by
     * the <code>IDataTypeVisitor</code> portion of the class.
     */
    private int typeIndex;

    /**
     * This thread-local field holds the instance of the <code>Evaluator</code>
     * created for use on this thread.
     */
    private static final ThreadLocal<IEvaluator> instance =
        new ThreadLocal<IEvaluator>();

    /**
     * This private constructor prevents instantiations of this class
     * outside of the static getInstance method.
     */
    private Evaluator() {
    }

    /**
     * Returns the instance of <code>IEvaluator</code> created for use
     * on this thread. The first call to this method creates the evaluator;
     * the subsequent calls retrieve that instance from thread-local storage.
     *
     * @return the instance of <code>IEvaluator</code> created for use
     * on this thread.
     */
    public static IEvaluator getInstance() {
        IEvaluator res = instance.get();
        if (res == null) {
            res = new Evaluator();
            instance.set(res);
        }
        return res;
    }

    /**
     * @see IEvaluator#evaluate(IExpression, IEvaluationHandler)
     */
    public IEvaluationResult evaluate(
        IExpression expression
    ,   IEvaluationHandler handler) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        try {
            this.handler = handler;
            exprSP = -1;
            expression.accept(this);
            if (exprSP != 0) {
                throw new IllegalStateException(
                    "Inconsistent expression stack."
                );
            }
            return new EvaluationResult(stValues[0], stTypes[0]);
        } finally {
            cleanupAfterEvaluation();
        }
    }

    /**
     * Cleans up references to items that are no longer needed
     * to avoid leaking memory for "lingering" objects.
     */
    private void cleanupAfterEvaluation() {
        handler = null;
        Arrays.fill(stValues, null);
        Arrays.fill(stTypes, null);
    }

    /**
     * @see IEvaluatorCallback#addExpression(IExpression)
     */
    public void addExpression(IExpression expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        expression.accept(this);
    }

    /**
     * @see IEvaluatorCallback#addValue(Object, IDataType)
     */
    public final void addValue(Object value, IDataType type) {
        exprSP++;
        // Extend the stacks if necessary
        if (exprSP == stValues.length) {
            Object[] newValues = new Object[2*exprSP];
            IDataType[] newTypes = new IDataType[2*exprSP];
            System.arraycopy(stValues, 0, newValues, 0, exprSP);
            System.arraycopy(stTypes, 0, newTypes, 0, exprSP);
            stValues = newValues;
            stTypes = newTypes;
        }
        // Push the value and the type
        stValues[exprSP] = value;
        stTypes[exprSP] = type;
    }

    /**
     * @see IPredicateVisitor#visitNegation(INegation)
     */
    public void visitUnary(IUnaryExpression unary) {
        unary.getOperand().accept(this);
        if (unary.getOperator() == UnaryOperator.NOT) {
            if (stTypes[exprSP] == DataType.BOOLEAN) {
                stValues[exprSP] = (Boolean)stValues[exprSP] ^ true;
            }
        } else if (unary.getOperator() == UnaryOperator.SIGN) {
            int index = getTypeIndex(stTypes[exprSP]);
            exprSP--;
            CHANGE_SIGN_OPERATION[index].process(
                stValues[exprSP+1]
            ,   stTypes[exprSP+1]
            ,   this
            );

        }
    }

    /**
     * @see IPredicateVisitor#visitRelation(IRelation)
     */
    public void visitRelation(IRelation relation) {
        relation.getLHS().accept(this);
        relation.getRHS().accept(this);
        RelationOperator op = relation.getOperator();
        processBinary(RELATION_PROCESSOR[op.ordinal()], NUM_REL_TYPES, op);
    }

    /**
     * @see IExpressionVisitor#visitAttributeReference(IAttributeReference)
     */
    public void visitAttributeReference(IAttributeReference attrReference) {
        attrReference.getBase().accept(this);
        exprSP--;
        handler.evaluateAttribute(
            attrReference.getAttributeName()
        ,   stValues[exprSP+1]
        ,   stTypes[exprSP+1]
        ,   this
        );
    }

    /**
     * @see IExpressionVisitor#visitComposite(ICompositeExpression)
     */
    public void visitComposite(ICompositeExpression composite) {
        for (ICompositeExpression.Element element : composite) {
            element.getExpression().accept(this);
            if (element.isFirst()) {
                continue;
            }
            BinaryOperator op = element.getOperatorBefore();
            processBinary(ARITH_PROCESSOR[op.ordinal()], NUM_ARITH_TYPES, op);
        }
    }

    /**
     * @see IExpressionVisitor#visitConstant(Constant)
     */
    public void visitConstant(Constant constant) {
        addValue(constant.getValue(), constant.getType());
    }

    /**
     * @see IExpressionVisitor#visitExpression(IExpression)
     */
    public void visitExpression(IExpression expr) {
        handler.evaluateCustomExpression(expr, this);
    }

    /**
     * @see IExpressionVisitor#visitFunction(IFunctionCall)
     */
    public void visitFunction(IFunctionCall function) {
        // Evaluate function arguments
        String[] argNames = new String[function.getArgumentCount()];
        int i = 0;
        for (IFunctionCall.Argument arg : function.getArguments()) {
            argNames[i++] = arg.getName();
            arg.getExpression().accept(this);
        }
        // Call the function
        exprSP -= function.getArgumentCount();
        handler.evaluateFunction(
            function.getFunction()
        ,   argNames
        ,   stValues
        ,   stTypes
        ,   exprSP+1
        ,   this
        );
    }

    /**
     * @see IExpressionVisitor#visitReference(IExpressionReference)
     */
    public void visitReference(IExpressionReference reference) {
        handler.evaluateReference(reference, this);
    }

    /**
     * Given a type, returns its index for use in evaluation.
     *
     * @param type the type the index of which needs to be calculated.
     * @return the index of the specified data type for use in Evaluator's
     * implementation of multiple dispatch.
     */
    private int getTypeIndex(IDataType type) {
        type.accept(this);
        return typeIndex;
    }

    /**
     * @see IDataTypeVisitor#visitBoolean()
     */
    public void visitBoolean() {
        typeIndex = BOOLEAN_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitCode(ICodeDataType)
     */
    public void visitCode(ICodeDataType code) {
        typeIndex = CODE_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitDate()
     */
    public void visitDate() {
        typeIndex = DATE_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitMultivalue(IMultivalueDataType)
     */
    public void visitMultivalue(IMultivalueDataType multivalueDataType) {
        typeIndex = MULTIVALUE_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitNull()
     */
    public void visitNull() {
        typeIndex = NULL_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitDouble()
     */
    public void visitDouble() {
        typeIndex = DOUBLE_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitInteger()
     */
    public void visitInteger() {
        typeIndex = INTEGER_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitReference(IReferenceDataType)
     */
    public void visitReference(IReferenceDataType refDataType) {
        typeIndex = REFERENCE_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitString(boolean)
     */
    public void visitString(boolean caseSensitive) {
        typeIndex = caseSensitive ? STRING_INDEX : CS_STRING_INDEX;
    }

    /**
     * @see IDataTypeVisitor#visitUnknown(IDataType)
     */
    public void visitUnknown(IDataType dataType) {
        typeIndex = UNKNOWN_INDEX;
    }

    /**
     *
     * @param processor
     */
    private void processBinary(
        IBinary[][] processor
    ,   int maxTypeIndex
    ,   Object operation) {
        int lhsIndex = exprSP-1;
        int rhsIndex = exprSP;
        IDataType lhsType = stTypes[lhsIndex];
        IDataType rhsType = stTypes[rhsIndex];
        int lhsTypeIndex = getTypeIndex(lhsType);
        int rhsTypeIndex = getTypeIndex(rhsType);
        if (lhsTypeIndex >= maxTypeIndex || rhsTypeIndex >= maxTypeIndex) {
            throw new EvaluationException(String.format(
                "Operation %s is undefined for arguments of type %s and %s"
            ,   operation
            ,   lhsType
            ,   rhsType
            ));
        }
        exprSP -= 2;
        processor[lhsTypeIndex][rhsTypeIndex].process(
            stValues[lhsIndex]
        ,   lhsType
        ,   stValues[rhsIndex]
        ,   rhsType
        ,   this
        );
    }

    // Initialization of the change sign processor

    private static final IUnary UNARY_FAILURE = new IUnary() {
        public void process(
            Object operand
        ,   IDataType type
        ,   IEvaluatorCallback callback) {
            throw new EvaluationException("Cannot perform unary operation");
        }
    };

    private static final IUnary CHANGE_SIGN_DOUBLE = new IUnary() {
        public void process(
            Object operand
        ,   IDataType type
        ,   IEvaluatorCallback callback) {
            double value = (Double)operand;
            callback.addValue(-value, IDataType.DOUBLE);
        }
    };

    private static final IUnary CHANGE_SIGN_INTEGER = new IUnary() {
        public void process(
            Object operand
        ,   IDataType type
        ,   IEvaluatorCallback callback) {
            long value = (Long)operand;
            callback.addValue(-value, IDataType.INTEGER);
        }
    };

    private static final IUnary CHANGE_SIGN_BOOLEAN = new IUnary() {
        public void process(
            Object operand
        ,   IDataType type
        ,   IEvaluatorCallback callback) {
            boolean op = (Boolean)operand;
            callback.addValue(!op, IDataType.BOOLEAN);
        }
    };

    // Initializing the array of unary processors

    static {
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            CHANGE_SIGN_OPERATION[i] = UNARY_FAILURE;
        }
        CHANGE_SIGN_OPERATION[DOUBLE_INDEX] = CHANGE_SIGN_DOUBLE;
        CHANGE_SIGN_OPERATION[INTEGER_INDEX] = CHANGE_SIGN_INTEGER;
        CHANGE_SIGN_OPERATION[BOOLEAN_INDEX] = CHANGE_SIGN_BOOLEAN;
    }

    // Initialization of the binary processors - common

    private static final IBinary BINARY_FAILURE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            throw new EvaluationException("Cannot perform binary operation");
        }
    };

    // Initialization of the binary processors - arithmetic operations

    private static final IBinary FIXED_LHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lhsValue, lhsType);
        }
    };

    private static final IBinary FIXED_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(rhsValue, rhsType);
        }
    };

    private static final IBinary NEG_DOUBLE_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            double value = (Double)rhsValue;
            c.addValue(-value, IDataType.DOUBLE);
        }
    };

    private static final IBinary NEG_INTEGER_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            long value = (Long)rhsValue;
            c.addValue(-value, IDataType.INTEGER);
        }
    };

    private static final IBinary NEG_BOOLEAN_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            boolean op = (Boolean)rhsValue;
            c.addValue(!op, IDataType.BOOLEAN);
        }
    };

    private static final IBinary INV_DOUBLE_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            double value = (Double)rhsValue;
            c.addValue(1.0/value, IDataType.DOUBLE);
        }
    };

    private static final IBinary INV_INTEGER_RHS = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            long value = (Long)rhsValue;
            if (value == 0) {
                c.addValue(Double.POSITIVE_INFINITY, IDataType.DOUBLE);
            } else {
                c.addValue(1L/value, IDataType.INTEGER);
            }
        }
    };

    private static final IBinary AND_BOOL_BOOL = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                (Boolean)lhsValue && (Boolean)rhsValue
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary OR_BOOL_BOOL = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                (Boolean)lhsValue || (Boolean)rhsValue
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary ADD_BOOL_BOOL = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Boolean)lhsValue|(Boolean)rhsValue, IDataType.BOOLEAN);
        }
    };

    private static final IBinary MUL_BOOL_BOOL = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Boolean)lhsValue&(Boolean)rhsValue, IDataType.BOOLEAN);
        }
    };

    private static final IBinary ADD_INT_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue+(Long)rhsValue, IDataType.INTEGER);
        }
    };

    private static final IBinary ADD_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue+(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary ADD_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue+(Long)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary ADD_DOUBLE_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue+(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary SUB_INT_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue-(Long)rhsValue, IDataType.INTEGER);
        }
    };

    private static final IBinary SUB_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue-(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary SUB_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue-(Long)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary SUB_DOUBLE_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue-(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary MUL_INT_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue*(Long)rhsValue, IDataType.INTEGER);
        }
    };

    private static final IBinary MUL_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue*(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary MUL_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue*(Long)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary MUL_DOUBLE_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue*(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary DIV_INT_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Long rhsLong = (Long)rhsValue;
            if (rhsLong == 0L) {
                if ((Long)lhsValue < 0) {
                    c.addValue(Double.NEGATIVE_INFINITY, IDataType.DOUBLE);
                } else {
                    c.addValue(Double.POSITIVE_INFINITY, IDataType.DOUBLE);
                }
            } else {
                c.addValue((Long)lhsValue/(Long)rhsValue, IDataType.INTEGER);
            }
        }
    };

    private static final IBinary DIV_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue/(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary DIV_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue/(Long)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary DIV_DOUBLE_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue/(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary REM_INT_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Long rhsLong = (Long)rhsValue;
            if (rhsLong == 0L) {
                c.addValue(Double.NaN, IDataType.DOUBLE);
            } else {
                c.addValue((Long)lhsValue%(Long)rhsValue, IDataType.INTEGER);
            }
        }
    };

    private static final IBinary REM_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Long)lhsValue%(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary REM_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue%(Long)rhsValue, IDataType.DOUBLE);
        }
    };

    private static final IBinary REM_DOUBLE_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue((Double)lhsValue%(Double)rhsValue, IDataType.DOUBLE);
        }
    };

    // Initialization of the binary processors - relations

    private static final IBinary FIXED_TRUE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(true, DataType.BOOLEAN);
        }
    };

    private static final IBinary FIXED_FALSE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(false, DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_OBJ_OBJ = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalObjectObject(lhsValue, rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDateNumber((Date)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDateNumber((Date)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDateString((Date)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDateString((Date)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalNumberNumber((Number)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalNumberNumber((Number)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDoubleString((Double)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalDoubleString((Double)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalLongString((Long)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalLongString((Long)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_STRING_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalStringString((String)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalBoolString((Boolean)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalBoolString((Boolean)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalBoolNumber((Boolean)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary EQ_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(equalBoolNumber((Boolean)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_OBJ_OBJ = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalObjectObject(lhsValue, rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDateNumber((Date)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDateNumber((Date)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDateString((Date)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDateString((Date)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_INT_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalNumberNumber((Number)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_DOUBLE_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalNumberNumber((Number)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDoubleString((Double)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalDoubleString((Double)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalLongString((Long)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalLongString((Long)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_STRING_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalStringString((String)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalBoolString((Boolean)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalBoolString((Boolean)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalBoolNumber((Boolean)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary NE_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!equalBoolNumber((Boolean)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_OBJ_OBJ = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            if(lhsValue instanceof Comparable) {
                Comparable lhs = (Comparable)lhsValue;
                c.addValue(lhs.compareTo(rhsValue) > 0, DataType.BOOLEAN);
            } else if(rhsValue instanceof Comparable) {
                Comparable rhs = (Comparable)rhsValue;
                c.addValue(rhs.compareTo(lhsValue) < 0, DataType.BOOLEAN);
            } else {
                c.addValue(lhsValue.hashCode() > rhsValue.hashCode(), DataType.BOOLEAN);
            }
        }
    };

    private static final IBinary GT_COMP_COMP = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Comparable<Comparable> lhs = (Comparable<Comparable>)lhsValue;
            Comparable<Comparable> rhs = (Comparable<Comparable>)rhsValue;
            c.addValue(lhs.compareTo(rhs) > 0, DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanNumberDate((Number)rhsValue, (Date)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanDateNumber((Date)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringDate((String)rhsValue, (Date)lhsValue, false)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanDateString((Date)rhsValue, (String)lhsValue, false)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_NUMBER_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanNumberNumber((Number)rhsValue, (Number)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringDouble(
                    (String)rhsValue
                ,   (Double)lhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanDoubleString(
                    (Double)rhsValue
                ,   (String)lhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringLong(
                    (String)rhsValue
                ,   (Long)lhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanLongString(
                    (Long)rhsValue
                ,   (String)lhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_STRING_STRING_CI = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanStrStrCI((String)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanBoolStr((Boolean)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GT_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStrBool((String)rhsValue, (Boolean)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanBoolNumber((Boolean)rhsValue, (Number)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GT_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanNumberBool((Number)rhsValue, (Boolean)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_OBJ_OBJ = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            if(lhsValue instanceof Comparable) {
                Comparable lhs = (Comparable)lhsValue;
                c.addValue(lhs.compareTo(rhsValue) >= 0, DataType.BOOLEAN);
            } else if(rhsValue instanceof Comparable) {
                Comparable rhs = (Comparable)rhsValue;
                c.addValue(rhs.compareTo(lhsValue) <= 0, DataType.BOOLEAN);
            } else {
                c.addValue(lhsValue.hashCode() >= rhsValue.hashCode(), DataType.BOOLEAN);
            }
        }
    };

    private static final IBinary GE_COMP_COMP = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Comparable<Comparable> lhs = (Comparable<Comparable>)lhsValue;
            Comparable<Comparable> rhs = (Comparable<Comparable>)rhsValue;
            c.addValue(lhs.compareTo(rhs) >= 0, DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanDateNumber((Date)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanNumberDate((Number)lhsValue, (Date)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanDateString((Date)lhsValue, (String)rhsValue, true)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringDate((String)lhsValue, (Date)rhsValue, true)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_NUMBER_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanNumberNumber((Number)lhsValue, (Number)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanDoubleString(
                    (Double)lhsValue
                ,   (String)rhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringDouble(
                    (String)lhsValue
                ,   (Double)rhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanLongString(
                    (Long)lhsValue
                ,   (String)rhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringLong(
                    (String)lhsValue
                ,   (Long)rhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_STRING_STRING_CI = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanStrStrCI((String)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanStrBool((String)lhsValue, (Boolean)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanBoolStr((Boolean)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary GE_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanNumberBool((Number)lhsValue, (Boolean)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary GE_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanBoolNumber((Boolean)lhsValue, (Number)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_OBJ_OBJ = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            if(lhsValue instanceof Comparable) {
                Comparable lhs = (Comparable)lhsValue;
                c.addValue(lhs.compareTo(rhsValue) >= 0, DataType.BOOLEAN);
            } else if(rhsValue instanceof Comparable) {
                Comparable rhs = (Comparable)rhsValue;
                c.addValue(rhs.compareTo(lhsValue) <= 0, DataType.BOOLEAN);
            } else {
                c.addValue(lhsValue.hashCode() < rhsValue.hashCode(), DataType.BOOLEAN);
            }
        }
    };

    private static final IBinary LT_COMP_COMP = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Comparable<Comparable> lhs = (Comparable<Comparable>)lhsValue;
            Comparable<Comparable> rhs = (Comparable<Comparable>)rhsValue;
            c.addValue(lhs.compareTo(rhs) < 0, DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanDateNumber((Date)lhsValue, (Number)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanNumberDate((Number)lhsValue, (Date)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanDateString((Date)lhsValue, (String)rhsValue, false)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringDate((String)lhsValue, (Date)rhsValue, false)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_NUMBER_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanNumberNumber((Number)lhsValue, (Number)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanDoubleString(
                    (Double)lhsValue
                ,   (String)rhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringDouble(
                    (String)lhsValue
                ,   (Double)rhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanLongString(
                    (Long)lhsValue
                ,   (String)rhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanStringLong(
                    (String)lhsValue
                ,   (Long)rhsValue
                ,   false
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_STRING_STRING_CI = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanStrStrCI((String)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanStrBool((String)lhsValue, (Boolean)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(lessThanBoolStr((Boolean)lhsValue, (String)rhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LT_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanNumberBool((Number)lhsValue, (Boolean)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LT_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                lessThanBoolNumber((Boolean)lhsValue, (Number)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_OBJ_OBJ = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            if(lhsValue instanceof Comparable) {
                Comparable lhs = (Comparable)lhsValue;
                c.addValue(lhs.compareTo(rhsValue) <= 0, DataType.BOOLEAN);
            } else if(rhsValue instanceof Comparable) {
                Comparable rhs = (Comparable)rhsValue;
                c.addValue(rhs.compareTo(lhsValue) <= 0, DataType.BOOLEAN);
            } else {
                c.addValue(lhsValue.hashCode() <= rhsValue.hashCode(), DataType.BOOLEAN);
            }
        }
    };

    private static final IBinary LE_COMP_COMP = new IBinary() {
        @SuppressWarnings("unchecked")
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            Comparable<Comparable> lhs = (Comparable<Comparable>)lhsValue;
            Comparable<Comparable> rhs = (Comparable<Comparable>)rhsValue;
            c.addValue(lhs.compareTo(rhs) <= 0, DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_DATE_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanNumberDate((Number)rhsValue, (Date)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_NUMBER_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanDateNumber((Date)rhsValue, (Number)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_DATE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringDate((String)rhsValue, (Date)lhsValue, true)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_STRING_DATE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanDateString((Date)rhsValue, (String)lhsValue, true)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_NUMBER_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanNumberNumber((Number)rhsValue, (Number)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_DOUBLE_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringDouble(
                    (String)rhsValue
                ,   (Double)lhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_STRING_DOUBLE = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanDoubleString(
                    (Double)rhsValue
                ,   (String)lhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_INT_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanStringLong(
                    (String)rhsValue
                ,   (Long)lhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_STRING_INT = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanLongString(
                    (Long)rhsValue
                ,   (String)lhsValue
                ,   true
                )
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_STRING_STRING_CI = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanStrStrCI((String)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_STRING_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanBoolStr((Boolean)rhsValue, (String)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_BOOLEAN_STRING = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(!lessThanStrBool((String)rhsValue, (Boolean)lhsValue), DataType.BOOLEAN);
        }
    };

    private static final IBinary LE_NUMBER_BOOLEAN = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanBoolNumber((Boolean)rhsValue, (Number)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary LE_BOOLEAN_NUMBER = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                !lessThanNumberBool((Number)rhsValue, (Boolean)lhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary MATCH_STR_CI = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                matchCaseInsensitive((String)lhsValue, (String)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary MATCH_STR_STR = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                matchCaseSensitive((String)lhsValue, (String)rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    private static final IBinary MATCH_STR_OBJ = new IBinary() {
        public void process(
            Object lhsValue
        ,   IDataType lhsType
        ,   Object rhsValue
        ,   IDataType rhsType
        ,   IEvaluatorCallback c) {
            c.addValue(
                matchStringObject((String)lhsValue, rhsValue)
            ,   DataType.BOOLEAN
            );
        }
    };

    // Initializing the array of binary processors

    static {

        // Relation Operations
        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                for (int i = 0 ; i != RelationOperator.size() ; i++) {
                    RELATION_PROCESSOR[i][j][k] = BINARY_FAILURE;
                }
            }
        }

        // RelationOperator.EQUAL

        IBinary[][] eq = RELATION_PROCESSOR[RelationOperator.EQUAL.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                eq[j][k] = FIXED_FALSE;
            }
        }

        eq[NULL_INDEX][NULL_INDEX] = FIXED_TRUE;
        eq[UNKNOWN_INDEX][UNKNOWN_INDEX] = EQ_OBJ_OBJ;

        eq[BOOLEAN_INDEX][BOOLEAN_INDEX] = EQ_OBJ_OBJ;
        eq[BOOLEAN_INDEX][DOUBLE_INDEX] = EQ_BOOLEAN_NUMBER;
        eq[BOOLEAN_INDEX][INTEGER_INDEX] = EQ_BOOLEAN_NUMBER;
        eq[BOOLEAN_INDEX][DATE_INDEX] = FIXED_FALSE;
        eq[BOOLEAN_INDEX][STRING_INDEX] = EQ_BOOLEAN_STRING;
        eq[BOOLEAN_INDEX][CS_STRING_INDEX] = EQ_BOOLEAN_STRING;
        eq[BOOLEAN_INDEX][CODE_INDEX] = EQ_BOOLEAN_STRING;
        eq[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[DOUBLE_INDEX][BOOLEAN_INDEX] = EQ_NUMBER_BOOLEAN;
        eq[DOUBLE_INDEX][DOUBLE_INDEX] = EQ_OBJ_OBJ;
        eq[DOUBLE_INDEX][INTEGER_INDEX] = EQ_DOUBLE_INT;
        eq[DOUBLE_INDEX][DATE_INDEX] = EQ_NUMBER_DATE;
        eq[DOUBLE_INDEX][STRING_INDEX] = EQ_DOUBLE_STRING;
        eq[DOUBLE_INDEX][CS_STRING_INDEX] = EQ_DOUBLE_STRING;
        eq[DOUBLE_INDEX][CODE_INDEX] = FIXED_FALSE;
        eq[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[INTEGER_INDEX][BOOLEAN_INDEX] = EQ_NUMBER_BOOLEAN;
        eq[INTEGER_INDEX][DOUBLE_INDEX] = EQ_INT_DOUBLE;
        eq[INTEGER_INDEX][INTEGER_INDEX] = EQ_OBJ_OBJ;
        eq[INTEGER_INDEX][DATE_INDEX] = EQ_NUMBER_DATE;
        eq[INTEGER_INDEX][STRING_INDEX] = EQ_INT_STRING;
        eq[INTEGER_INDEX][CS_STRING_INDEX] = EQ_INT_STRING;
        eq[INTEGER_INDEX][CODE_INDEX] = FIXED_FALSE;
        eq[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[DATE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        eq[DATE_INDEX][DOUBLE_INDEX] = EQ_DATE_NUMBER;
        eq[DATE_INDEX][INTEGER_INDEX] = EQ_DATE_NUMBER;
        eq[DATE_INDEX][DATE_INDEX] = EQ_OBJ_OBJ;
        eq[DATE_INDEX][STRING_INDEX] = EQ_DATE_STRING;
        eq[DATE_INDEX][CS_STRING_INDEX] = EQ_DATE_STRING;
        eq[DATE_INDEX][CODE_INDEX] = FIXED_FALSE;
        eq[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[DATE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[STRING_INDEX][BOOLEAN_INDEX] = EQ_STRING_BOOLEAN;
        eq[STRING_INDEX][DOUBLE_INDEX] = EQ_STRING_DOUBLE;
        eq[STRING_INDEX][INTEGER_INDEX] = EQ_STRING_INT;
        eq[STRING_INDEX][DATE_INDEX] = EQ_STRING_DATE;
        eq[STRING_INDEX][STRING_INDEX] = EQ_STRING_STRING;
        eq[STRING_INDEX][CS_STRING_INDEX] = EQ_STRING_STRING;
        eq[STRING_INDEX][CODE_INDEX] = EQ_STRING_STRING;
        eq[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[CS_STRING_INDEX][BOOLEAN_INDEX] = EQ_STRING_BOOLEAN;
        eq[CS_STRING_INDEX][DOUBLE_INDEX] = EQ_STRING_DOUBLE;
        eq[CS_STRING_INDEX][INTEGER_INDEX] = EQ_STRING_INT;
        eq[CS_STRING_INDEX][DATE_INDEX] = EQ_STRING_DATE;
        eq[CS_STRING_INDEX][STRING_INDEX] = EQ_STRING_STRING;
        eq[CS_STRING_INDEX][CS_STRING_INDEX] = EQ_OBJ_OBJ;
        eq[CS_STRING_INDEX][CODE_INDEX] = EQ_OBJ_OBJ;
        eq[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[CODE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        eq[CODE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        eq[CODE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        eq[CODE_INDEX][DATE_INDEX] = FIXED_FALSE;
        eq[CODE_INDEX][STRING_INDEX] = EQ_STRING_STRING;
        eq[CODE_INDEX][CS_STRING_INDEX] = EQ_OBJ_OBJ;
        eq[CODE_INDEX][CODE_INDEX] = EQ_OBJ_OBJ;
        eq[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[CODE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        // TODO: Compare multivalues
        eq[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        eq[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][DATE_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][STRING_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][CODE_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        eq[REFERENCE_INDEX][REFERENCE_INDEX] = EQ_OBJ_OBJ;

        // RelationOperator.NOT_EQUAL

        IBinary[][] ne =
            RELATION_PROCESSOR[RelationOperator.NOT_EQUAL.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                ne[j][k] = FIXED_TRUE;
            }
        }

        ne[NULL_INDEX][NULL_INDEX] = FIXED_FALSE;
        ne[UNKNOWN_INDEX][UNKNOWN_INDEX] = NE_OBJ_OBJ;

        ne[BOOLEAN_INDEX][BOOLEAN_INDEX] = NE_OBJ_OBJ;
        ne[BOOLEAN_INDEX][DOUBLE_INDEX] = NE_BOOLEAN_NUMBER;
        ne[BOOLEAN_INDEX][INTEGER_INDEX] = NE_BOOLEAN_NUMBER;
        ne[BOOLEAN_INDEX][DATE_INDEX] = FIXED_TRUE;
        ne[BOOLEAN_INDEX][STRING_INDEX] = NE_BOOLEAN_STRING;
        ne[BOOLEAN_INDEX][CS_STRING_INDEX] = NE_BOOLEAN_STRING;
        ne[BOOLEAN_INDEX][CODE_INDEX] = NE_BOOLEAN_STRING;
        ne[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[DOUBLE_INDEX][BOOLEAN_INDEX] = NE_NUMBER_BOOLEAN;
        ne[DOUBLE_INDEX][DOUBLE_INDEX] = NE_OBJ_OBJ;
        ne[DOUBLE_INDEX][INTEGER_INDEX] = NE_DOUBLE_INT;
        ne[DOUBLE_INDEX][DATE_INDEX] = NE_NUMBER_DATE;
        ne[DOUBLE_INDEX][STRING_INDEX] = NE_DOUBLE_STRING;
        ne[DOUBLE_INDEX][CS_STRING_INDEX] = NE_DOUBLE_STRING;
        ne[DOUBLE_INDEX][CODE_INDEX] = FIXED_TRUE;
        ne[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[INTEGER_INDEX][BOOLEAN_INDEX] = NE_NUMBER_BOOLEAN;
        ne[INTEGER_INDEX][DOUBLE_INDEX] = NE_INT_DOUBLE;
        ne[INTEGER_INDEX][INTEGER_INDEX] = NE_OBJ_OBJ;
        ne[INTEGER_INDEX][DATE_INDEX] = NE_NUMBER_DATE;
        ne[INTEGER_INDEX][STRING_INDEX] = NE_INT_STRING;
        ne[INTEGER_INDEX][CS_STRING_INDEX] = NE_INT_STRING;
        ne[INTEGER_INDEX][CODE_INDEX] = FIXED_TRUE;
        ne[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[DATE_INDEX][BOOLEAN_INDEX] = FIXED_TRUE;
        ne[DATE_INDEX][DOUBLE_INDEX] = NE_DATE_NUMBER;
        ne[DATE_INDEX][INTEGER_INDEX] = NE_DATE_NUMBER;
        ne[DATE_INDEX][DATE_INDEX] = NE_OBJ_OBJ;
        ne[DATE_INDEX][STRING_INDEX] = NE_DATE_STRING;
        ne[DATE_INDEX][CS_STRING_INDEX] = NE_DATE_STRING;
        ne[DATE_INDEX][CODE_INDEX] = FIXED_TRUE;
        ne[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[DATE_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[STRING_INDEX][BOOLEAN_INDEX] = NE_STRING_BOOLEAN;
        ne[STRING_INDEX][DOUBLE_INDEX] = NE_STRING_DOUBLE;
        ne[STRING_INDEX][INTEGER_INDEX] = NE_STRING_INT;
        ne[STRING_INDEX][DATE_INDEX] = NE_STRING_DATE;
        ne[STRING_INDEX][STRING_INDEX] = NE_STRING_STRING;
        ne[STRING_INDEX][CS_STRING_INDEX] = NE_STRING_STRING;
        ne[STRING_INDEX][CODE_INDEX] = NE_STRING_STRING;
        ne[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[STRING_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[CS_STRING_INDEX][BOOLEAN_INDEX] = NE_STRING_BOOLEAN;
        ne[CS_STRING_INDEX][DOUBLE_INDEX] = NE_STRING_DOUBLE;
        ne[CS_STRING_INDEX][INTEGER_INDEX] = NE_STRING_INT;
        ne[CS_STRING_INDEX][DATE_INDEX] = NE_STRING_DATE;
        ne[CS_STRING_INDEX][STRING_INDEX] = NE_STRING_STRING;
        ne[CS_STRING_INDEX][CS_STRING_INDEX] = NE_OBJ_OBJ;
        ne[CS_STRING_INDEX][CODE_INDEX] = NE_OBJ_OBJ;
        ne[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[CODE_INDEX][BOOLEAN_INDEX] = FIXED_TRUE;
        ne[CODE_INDEX][DOUBLE_INDEX] = FIXED_TRUE;
        ne[CODE_INDEX][INTEGER_INDEX] = FIXED_TRUE;
        ne[CODE_INDEX][DATE_INDEX] = FIXED_TRUE;
        ne[CODE_INDEX][STRING_INDEX] = NE_STRING_STRING;
        ne[CODE_INDEX][CS_STRING_INDEX] = NE_OBJ_OBJ;
        ne[CODE_INDEX][CODE_INDEX] = NE_OBJ_OBJ;
        ne[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[CODE_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_TRUE;
        // TODO: Compare multivalues
        ne[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_TRUE;

        ne[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][DATE_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][STRING_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][CODE_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_TRUE;
        ne[REFERENCE_INDEX][REFERENCE_INDEX] = NE_OBJ_OBJ;

        // RelationOperator.GREATER_THAN

        IBinary[][] gt =
            RELATION_PROCESSOR[RelationOperator.GREATER_THAN.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                gt[j][k] = FIXED_FALSE;
            }
        }

        for (int i = 0 ; i != NUM_REL_TYPES ; i++) {
            gt[i][NULL_INDEX] = FIXED_TRUE;
        }

        gt[NULL_INDEX][NULL_INDEX] = FIXED_FALSE;
        gt[UNKNOWN_INDEX][UNKNOWN_INDEX] = GT_OBJ_OBJ;

        gt[BOOLEAN_INDEX][BOOLEAN_INDEX] = GT_COMP_COMP;
        gt[BOOLEAN_INDEX][DOUBLE_INDEX] = GT_BOOLEAN_NUMBER;
        gt[BOOLEAN_INDEX][INTEGER_INDEX] = GT_BOOLEAN_NUMBER;
        gt[BOOLEAN_INDEX][DATE_INDEX] = FIXED_FALSE;
        gt[BOOLEAN_INDEX][STRING_INDEX] = GT_BOOLEAN_STRING;
        gt[BOOLEAN_INDEX][CS_STRING_INDEX] = GT_BOOLEAN_STRING;
        gt[BOOLEAN_INDEX][CODE_INDEX] = GT_BOOLEAN_STRING;
        gt[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[DOUBLE_INDEX][BOOLEAN_INDEX] = GT_NUMBER_BOOLEAN;
        gt[DOUBLE_INDEX][DOUBLE_INDEX] = GT_COMP_COMP;
        gt[DOUBLE_INDEX][INTEGER_INDEX] = GT_NUMBER_NUMBER;
        gt[DOUBLE_INDEX][DATE_INDEX] = GT_NUMBER_DATE;
        gt[DOUBLE_INDEX][STRING_INDEX] = GT_DOUBLE_STRING;
        gt[DOUBLE_INDEX][CS_STRING_INDEX] = GT_DOUBLE_STRING;
        gt[DOUBLE_INDEX][CODE_INDEX] = FIXED_FALSE;
        gt[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[INTEGER_INDEX][BOOLEAN_INDEX] = GT_NUMBER_BOOLEAN;
        gt[INTEGER_INDEX][DOUBLE_INDEX] = GT_NUMBER_NUMBER;
        gt[INTEGER_INDEX][INTEGER_INDEX] = GT_COMP_COMP;
        gt[INTEGER_INDEX][DATE_INDEX] = GT_NUMBER_DATE;
        gt[INTEGER_INDEX][STRING_INDEX] = GT_INT_STRING;
        gt[INTEGER_INDEX][CS_STRING_INDEX] = GT_INT_STRING;
        gt[INTEGER_INDEX][CODE_INDEX] = FIXED_FALSE;
        gt[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[DATE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        gt[DATE_INDEX][DOUBLE_INDEX] = GT_DATE_NUMBER;
        gt[DATE_INDEX][INTEGER_INDEX] = GT_DATE_NUMBER;
        gt[DATE_INDEX][DATE_INDEX] = GT_COMP_COMP;
        gt[DATE_INDEX][STRING_INDEX] = GT_DATE_STRING;
        gt[DATE_INDEX][CS_STRING_INDEX] = GT_DATE_STRING;
        gt[DATE_INDEX][CODE_INDEX] = FIXED_FALSE;
        gt[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[DATE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[STRING_INDEX][BOOLEAN_INDEX] = GT_STRING_BOOLEAN;
        gt[STRING_INDEX][DOUBLE_INDEX] = GT_STRING_DOUBLE;
        gt[STRING_INDEX][INTEGER_INDEX] = GT_STRING_INT;
        gt[STRING_INDEX][DATE_INDEX] = GT_STRING_DATE;
        gt[STRING_INDEX][STRING_INDEX] = GT_STRING_STRING_CI;
        gt[STRING_INDEX][CS_STRING_INDEX] = GT_STRING_STRING_CI;
        gt[STRING_INDEX][CODE_INDEX] = GT_STRING_STRING_CI;
        gt[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[CS_STRING_INDEX][BOOLEAN_INDEX] = GT_STRING_BOOLEAN;
        gt[CS_STRING_INDEX][DOUBLE_INDEX] = GT_STRING_DOUBLE;
        gt[CS_STRING_INDEX][INTEGER_INDEX] = GT_STRING_INT;
        gt[CS_STRING_INDEX][DATE_INDEX] = GT_STRING_DATE;
        gt[CS_STRING_INDEX][STRING_INDEX] = GT_STRING_STRING_CI;
        gt[CS_STRING_INDEX][CS_STRING_INDEX] = GT_COMP_COMP;
        gt[CS_STRING_INDEX][CODE_INDEX] = GT_COMP_COMP;
        gt[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[CODE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        gt[CODE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        gt[CODE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        gt[CODE_INDEX][DATE_INDEX] = FIXED_FALSE;
        gt[CODE_INDEX][STRING_INDEX] = GT_STRING_STRING_CI;
        gt[CODE_INDEX][CS_STRING_INDEX] = GT_COMP_COMP;
        gt[CODE_INDEX][CODE_INDEX] = GT_COMP_COMP;
        gt[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[CODE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        // TODO: Compare multivalues
        gt[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        gt[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][DATE_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][STRING_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][CODE_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        gt[REFERENCE_INDEX][REFERENCE_INDEX] = GT_OBJ_OBJ;

        // RelationOperator.GREATER_THAN_OR_EQUAL_TO

        IBinary[][] ge = RELATION_PROCESSOR[
            RelationOperator.GREATER_THAN_OR_EQUAL_TO.ordinal()
        ];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                ge[j][k] = FIXED_FALSE;
            }
        }

        for (int i = 0 ; i != NUM_REL_TYPES ; i++) {
            ge[i][NULL_INDEX] = FIXED_TRUE;
        }

        ge[NULL_INDEX][NULL_INDEX] = FIXED_TRUE;
        ge[UNKNOWN_INDEX][UNKNOWN_INDEX] = GE_OBJ_OBJ;

        ge[BOOLEAN_INDEX][BOOLEAN_INDEX] = GE_COMP_COMP;
        ge[BOOLEAN_INDEX][DOUBLE_INDEX] = GE_BOOLEAN_NUMBER;
        ge[BOOLEAN_INDEX][INTEGER_INDEX] = GE_BOOLEAN_NUMBER;
        ge[BOOLEAN_INDEX][DATE_INDEX] = FIXED_FALSE;
        ge[BOOLEAN_INDEX][STRING_INDEX] = GE_BOOLEAN_STRING;
        ge[BOOLEAN_INDEX][CS_STRING_INDEX] = GE_BOOLEAN_STRING;
        ge[BOOLEAN_INDEX][CODE_INDEX] = GE_BOOLEAN_STRING;
        ge[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[DOUBLE_INDEX][BOOLEAN_INDEX] = GE_NUMBER_BOOLEAN;
        ge[DOUBLE_INDEX][DOUBLE_INDEX] = GE_COMP_COMP;
        ge[DOUBLE_INDEX][INTEGER_INDEX] = GE_NUMBER_NUMBER;
        ge[DOUBLE_INDEX][DATE_INDEX] = GE_NUMBER_DATE;
        ge[DOUBLE_INDEX][STRING_INDEX] = GE_DOUBLE_STRING;
        ge[DOUBLE_INDEX][CS_STRING_INDEX] = GE_DOUBLE_STRING;
        ge[DOUBLE_INDEX][CODE_INDEX] = FIXED_FALSE;
        ge[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[INTEGER_INDEX][BOOLEAN_INDEX] = GE_NUMBER_BOOLEAN;
        ge[INTEGER_INDEX][DOUBLE_INDEX] = GE_NUMBER_NUMBER;
        ge[INTEGER_INDEX][INTEGER_INDEX] = GE_COMP_COMP;
        ge[INTEGER_INDEX][DATE_INDEX] = GE_NUMBER_DATE;
        ge[INTEGER_INDEX][STRING_INDEX] = GE_INT_STRING;
        ge[INTEGER_INDEX][CS_STRING_INDEX] = GE_INT_STRING;
        ge[INTEGER_INDEX][CODE_INDEX] = FIXED_FALSE;
        ge[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[DATE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        ge[DATE_INDEX][DOUBLE_INDEX] = GE_DATE_NUMBER;
        ge[DATE_INDEX][INTEGER_INDEX] = GE_DATE_NUMBER;
        ge[DATE_INDEX][DATE_INDEX] = GE_COMP_COMP;
        ge[DATE_INDEX][STRING_INDEX] = GE_DATE_STRING;
        ge[DATE_INDEX][CS_STRING_INDEX] = GE_DATE_STRING;
        ge[DATE_INDEX][CODE_INDEX] = FIXED_FALSE;
        ge[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[DATE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[STRING_INDEX][BOOLEAN_INDEX] = GE_STRING_BOOLEAN;
        ge[STRING_INDEX][DOUBLE_INDEX] = GE_STRING_DOUBLE;
        ge[STRING_INDEX][INTEGER_INDEX] = GE_STRING_INT;
        ge[STRING_INDEX][DATE_INDEX] = GE_STRING_DATE;
        ge[STRING_INDEX][STRING_INDEX] = GE_STRING_STRING_CI;
        ge[STRING_INDEX][CS_STRING_INDEX] = GE_STRING_STRING_CI;
        ge[STRING_INDEX][CODE_INDEX] = GE_STRING_STRING_CI;
        ge[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[CS_STRING_INDEX][BOOLEAN_INDEX] = GE_STRING_BOOLEAN;
        ge[CS_STRING_INDEX][DOUBLE_INDEX] = GE_STRING_DOUBLE;
        ge[CS_STRING_INDEX][INTEGER_INDEX] = GE_STRING_INT;
        ge[CS_STRING_INDEX][DATE_INDEX] = GE_STRING_DATE;
        ge[CS_STRING_INDEX][STRING_INDEX] = GE_STRING_STRING_CI;
        ge[CS_STRING_INDEX][CS_STRING_INDEX] = GE_COMP_COMP;
        ge[CS_STRING_INDEX][CODE_INDEX] = GE_COMP_COMP;
        ge[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[CODE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        ge[CODE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        ge[CODE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        ge[CODE_INDEX][DATE_INDEX] = FIXED_FALSE;
        ge[CODE_INDEX][STRING_INDEX] = GE_STRING_STRING_CI;
        ge[CODE_INDEX][CS_STRING_INDEX] = GE_COMP_COMP;
        ge[CODE_INDEX][CODE_INDEX] = GE_COMP_COMP;
        ge[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[CODE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        // TODO: Compare multivalues
        ge[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        ge[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][DATE_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][STRING_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][CODE_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        ge[REFERENCE_INDEX][REFERENCE_INDEX] = GE_OBJ_OBJ;

        // RelationOperator.GREATER_THAN_OR_EQUAL_TO

        IBinary[][] lt =
            RELATION_PROCESSOR[RelationOperator.LESS_THAN.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                lt[j][k] = FIXED_FALSE;
            }
        }

        for (int i = 0 ; i != NUM_REL_TYPES ; i++) {
            lt[NULL_INDEX][i] = FIXED_TRUE;
        }

        lt[NULL_INDEX][NULL_INDEX] = FIXED_FALSE;
        lt[UNKNOWN_INDEX][UNKNOWN_INDEX] = LT_OBJ_OBJ;

        lt[BOOLEAN_INDEX][BOOLEAN_INDEX] = LT_COMP_COMP;
        lt[BOOLEAN_INDEX][DOUBLE_INDEX] = LT_BOOLEAN_NUMBER;
        lt[BOOLEAN_INDEX][INTEGER_INDEX] = LT_BOOLEAN_NUMBER;
        lt[BOOLEAN_INDEX][DATE_INDEX] = FIXED_FALSE;
        lt[BOOLEAN_INDEX][STRING_INDEX] = LT_BOOLEAN_STRING;
        lt[BOOLEAN_INDEX][CS_STRING_INDEX] = LT_BOOLEAN_STRING;
        lt[BOOLEAN_INDEX][CODE_INDEX] = LT_BOOLEAN_STRING;
        lt[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[DOUBLE_INDEX][BOOLEAN_INDEX] = LT_NUMBER_BOOLEAN;
        lt[DOUBLE_INDEX][DOUBLE_INDEX] = LT_COMP_COMP;
        lt[DOUBLE_INDEX][INTEGER_INDEX] = LT_NUMBER_NUMBER;
        lt[DOUBLE_INDEX][DATE_INDEX] = LT_NUMBER_DATE;
        lt[DOUBLE_INDEX][STRING_INDEX] = LT_DOUBLE_STRING;
        lt[DOUBLE_INDEX][CS_STRING_INDEX] = LT_DOUBLE_STRING;
        lt[DOUBLE_INDEX][CODE_INDEX] = FIXED_FALSE;
        lt[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[INTEGER_INDEX][BOOLEAN_INDEX] = LT_NUMBER_BOOLEAN;
        lt[INTEGER_INDEX][DOUBLE_INDEX] = LT_NUMBER_NUMBER;
        lt[INTEGER_INDEX][INTEGER_INDEX] = LT_COMP_COMP;
        lt[INTEGER_INDEX][DATE_INDEX] = LT_NUMBER_DATE;
        lt[INTEGER_INDEX][STRING_INDEX] = LT_INT_STRING;
        lt[INTEGER_INDEX][CS_STRING_INDEX] = LT_INT_STRING;
        lt[INTEGER_INDEX][CODE_INDEX] = FIXED_FALSE;
        lt[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[DATE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        lt[DATE_INDEX][DOUBLE_INDEX] = LT_DATE_NUMBER;
        lt[DATE_INDEX][INTEGER_INDEX] = LT_DATE_NUMBER;
        lt[DATE_INDEX][DATE_INDEX] = LT_COMP_COMP;
        lt[DATE_INDEX][STRING_INDEX] = LT_DATE_STRING;
        lt[DATE_INDEX][CS_STRING_INDEX] = LT_DATE_STRING;
        lt[DATE_INDEX][CODE_INDEX] = FIXED_FALSE;
        lt[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[DATE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[STRING_INDEX][BOOLEAN_INDEX] = LT_STRING_BOOLEAN;
        lt[STRING_INDEX][DOUBLE_INDEX] = LT_STRING_DOUBLE;
        lt[STRING_INDEX][INTEGER_INDEX] = LT_STRING_INT;
        lt[STRING_INDEX][DATE_INDEX] = LT_STRING_DATE;
        lt[STRING_INDEX][STRING_INDEX] = LT_STRING_STRING_CI;
        lt[STRING_INDEX][CS_STRING_INDEX] = LT_STRING_STRING_CI;
        lt[STRING_INDEX][CODE_INDEX] = LT_STRING_STRING_CI;
        lt[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[CS_STRING_INDEX][BOOLEAN_INDEX] = LT_STRING_BOOLEAN;
        lt[CS_STRING_INDEX][DOUBLE_INDEX] = LT_STRING_DOUBLE;
        lt[CS_STRING_INDEX][INTEGER_INDEX] = LT_STRING_INT;
        lt[CS_STRING_INDEX][DATE_INDEX] = LT_STRING_DATE;
        lt[CS_STRING_INDEX][STRING_INDEX] = LT_STRING_STRING_CI;
        lt[CS_STRING_INDEX][CS_STRING_INDEX] = LT_COMP_COMP;
        lt[CS_STRING_INDEX][CODE_INDEX] = LT_COMP_COMP;
        lt[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[CODE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        lt[CODE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        lt[CODE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        lt[CODE_INDEX][DATE_INDEX] = FIXED_FALSE;
        lt[CODE_INDEX][STRING_INDEX] = LT_STRING_STRING_CI;
        lt[CODE_INDEX][CS_STRING_INDEX] = LT_COMP_COMP;
        lt[CODE_INDEX][CODE_INDEX] = LT_COMP_COMP;
        lt[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[CODE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        // TODO: Compare multivalues
        lt[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        lt[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][DATE_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][STRING_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][CODE_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        lt[REFERENCE_INDEX][REFERENCE_INDEX] = LT_OBJ_OBJ;

        // RelationOperator.LESS_THAN_OR_EQUAL_TO

        IBinary[][] le = RELATION_PROCESSOR
            [RelationOperator.LESS_THAN_OR_EQUAL_TO.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                le[j][k] = FIXED_FALSE;
            }
        }

        for (int i = 0 ; i != NUM_REL_TYPES ; i++) {
            le[NULL_INDEX][i] = FIXED_TRUE;
        }

        le[NULL_INDEX][NULL_INDEX] = FIXED_TRUE;
        le[UNKNOWN_INDEX][UNKNOWN_INDEX] = LE_OBJ_OBJ;

        le[BOOLEAN_INDEX][BOOLEAN_INDEX] = LE_COMP_COMP;
        le[BOOLEAN_INDEX][DOUBLE_INDEX] = LE_BOOLEAN_NUMBER;
        le[BOOLEAN_INDEX][INTEGER_INDEX] = LE_BOOLEAN_NUMBER;
        le[BOOLEAN_INDEX][DATE_INDEX] = FIXED_FALSE;
        le[BOOLEAN_INDEX][STRING_INDEX] = LE_BOOLEAN_STRING;
        le[BOOLEAN_INDEX][CS_STRING_INDEX] = LE_BOOLEAN_STRING;
        le[BOOLEAN_INDEX][CODE_INDEX] = LE_BOOLEAN_STRING;
        le[BOOLEAN_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[BOOLEAN_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[DOUBLE_INDEX][BOOLEAN_INDEX] = LE_NUMBER_BOOLEAN;
        le[DOUBLE_INDEX][DOUBLE_INDEX] = LE_COMP_COMP;
        le[DOUBLE_INDEX][INTEGER_INDEX] = LE_NUMBER_NUMBER;
        le[DOUBLE_INDEX][DATE_INDEX] = LE_NUMBER_DATE;
        le[DOUBLE_INDEX][STRING_INDEX] = LE_DOUBLE_STRING;
        le[DOUBLE_INDEX][CS_STRING_INDEX] = LE_DOUBLE_STRING;
        le[DOUBLE_INDEX][CODE_INDEX] = FIXED_FALSE;
        le[DOUBLE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[DOUBLE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[INTEGER_INDEX][BOOLEAN_INDEX] = LE_NUMBER_BOOLEAN;
        le[INTEGER_INDEX][DOUBLE_INDEX] = LE_NUMBER_NUMBER;
        le[INTEGER_INDEX][INTEGER_INDEX] = LE_COMP_COMP;
        le[INTEGER_INDEX][DATE_INDEX] = LE_NUMBER_DATE;
        le[INTEGER_INDEX][STRING_INDEX] = LE_INT_STRING;
        le[INTEGER_INDEX][CS_STRING_INDEX] = LE_INT_STRING;
        le[INTEGER_INDEX][CODE_INDEX] = FIXED_FALSE;
        le[INTEGER_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[INTEGER_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[DATE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        le[DATE_INDEX][DOUBLE_INDEX] = LE_DATE_NUMBER;
        le[DATE_INDEX][INTEGER_INDEX] = LE_DATE_NUMBER;
        le[DATE_INDEX][DATE_INDEX] = LE_COMP_COMP;
        le[DATE_INDEX][STRING_INDEX] = LE_DATE_STRING;
        le[DATE_INDEX][CS_STRING_INDEX] = LE_DATE_STRING;
        le[DATE_INDEX][CODE_INDEX] = FIXED_FALSE;
        le[DATE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[DATE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[STRING_INDEX][BOOLEAN_INDEX] = LE_STRING_BOOLEAN;
        le[STRING_INDEX][DOUBLE_INDEX] = LE_STRING_DOUBLE;
        le[STRING_INDEX][INTEGER_INDEX] = LE_STRING_INT;
        le[STRING_INDEX][DATE_INDEX] = LE_STRING_DATE;
        le[STRING_INDEX][STRING_INDEX] = LE_STRING_STRING_CI;
        le[STRING_INDEX][CS_STRING_INDEX] = LE_STRING_STRING_CI;
        le[STRING_INDEX][CODE_INDEX] = LE_STRING_STRING_CI;
        le[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[CS_STRING_INDEX][BOOLEAN_INDEX] = LE_STRING_BOOLEAN;
        le[CS_STRING_INDEX][DOUBLE_INDEX] = LE_STRING_DOUBLE;
        le[CS_STRING_INDEX][INTEGER_INDEX] = LE_STRING_INT;
        le[CS_STRING_INDEX][DATE_INDEX] = LE_STRING_DATE;
        le[CS_STRING_INDEX][STRING_INDEX] = LE_STRING_STRING_CI;
        le[CS_STRING_INDEX][CS_STRING_INDEX] = LE_COMP_COMP;
        le[CS_STRING_INDEX][CODE_INDEX] = LE_COMP_COMP;
        le[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[CS_STRING_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[CODE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        le[CODE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        le[CODE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        le[CODE_INDEX][DATE_INDEX] = FIXED_FALSE;
        le[CODE_INDEX][STRING_INDEX] = LE_STRING_STRING_CI;
        le[CODE_INDEX][CS_STRING_INDEX] = LE_COMP_COMP;
        le[CODE_INDEX][CODE_INDEX] = LE_COMP_COMP;
        le[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[CODE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[MULTIVALUE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][DATE_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        // TODO: Compare multivalues
        le[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[MULTIVALUE_INDEX][REFERENCE_INDEX] = FIXED_FALSE;

        le[REFERENCE_INDEX][BOOLEAN_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][DOUBLE_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][INTEGER_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][DATE_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][STRING_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][CODE_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        le[REFERENCE_INDEX][REFERENCE_INDEX] = LE_OBJ_OBJ;

        IBinary[][] match =
            RELATION_PROCESSOR[RelationOperator.MATCHES.ordinal()];

        for (int j = 0 ; j != NUM_REL_TYPES ; j++) {
            for (int k = 0 ; k != NUM_REL_TYPES ; k++) {
                match[j][k] = FIXED_FALSE;
            }
        }

        match[STRING_INDEX][STRING_INDEX] = MATCH_STR_CI;
        match[STRING_INDEX][CS_STRING_INDEX] = MATCH_STR_CI;
        match[STRING_INDEX][CODE_INDEX] = MATCH_STR_CI;
        match[STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        match[STRING_INDEX][UNKNOWN_INDEX] = MATCH_STR_OBJ;

        match[CS_STRING_INDEX][STRING_INDEX] = MATCH_STR_CI;
        match[CS_STRING_INDEX][CS_STRING_INDEX] = MATCH_STR_STR;
        match[CS_STRING_INDEX][CODE_INDEX] = MATCH_STR_STR;
        match[CS_STRING_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        match[CS_STRING_INDEX][UNKNOWN_INDEX] = MATCH_STR_OBJ;

        match[CODE_INDEX][STRING_INDEX] = MATCH_STR_CI;
        match[CODE_INDEX][CS_STRING_INDEX] = MATCH_STR_STR;
        match[CODE_INDEX][CODE_INDEX] = MATCH_STR_STR;
        match[CODE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        match[CODE_INDEX][UNKNOWN_INDEX] = MATCH_STR_OBJ;

        // TODO: Compare multivalues
        match[MULTIVALUE_INDEX][STRING_INDEX] = FIXED_FALSE;
        match[MULTIVALUE_INDEX][CS_STRING_INDEX] = FIXED_FALSE;
        match[MULTIVALUE_INDEX][CODE_INDEX] = FIXED_FALSE;
        match[MULTIVALUE_INDEX][MULTIVALUE_INDEX] = FIXED_FALSE;
        match[MULTIVALUE_INDEX][UNKNOWN_INDEX] = FIXED_FALSE;

        // Arithmetic Operations
        for (int j = 0 ; j != NUM_ARITH_TYPES ; j++) {
            for (int k = 0 ; k != NUM_ARITH_TYPES ; k++) {
                for (int i = 0 ; i != BinaryOperator.size() ; i++) {
                    ARITH_PROCESSOR[i][j][k] = BINARY_FAILURE;
                }
            }
        }

        IBinary[][] and =
            ARITH_PROCESSOR[BinaryOperator.AND.ordinal()];

        and[BOOLEAN_INDEX][BOOLEAN_INDEX] = AND_BOOL_BOOL;

        IBinary[][] or =
            ARITH_PROCESSOR[BinaryOperator.OR.ordinal()];

        or[BOOLEAN_INDEX][BOOLEAN_INDEX] = OR_BOOL_BOOL;

        IBinary[][] add =
            ARITH_PROCESSOR[BinaryOperator.ADD.ordinal()];

        add[INTEGER_INDEX][INTEGER_INDEX] = ADD_INT_INT;
        add[INTEGER_INDEX][DOUBLE_INDEX] = ADD_INT_DOUBLE;
        add[DOUBLE_INDEX][INTEGER_INDEX] = ADD_DOUBLE_INT;
        add[DOUBLE_INDEX][DOUBLE_INDEX] = ADD_DOUBLE_DOUBLE;
        add[BOOLEAN_INDEX][BOOLEAN_INDEX] = ADD_BOOL_BOOL;
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            add[i][NULL_INDEX] = FIXED_LHS;
            add[NULL_INDEX][i] = FIXED_RHS;
        }

        IBinary[][] sub =
            ARITH_PROCESSOR[BinaryOperator.SUBTRACT.ordinal()];

        sub[INTEGER_INDEX][INTEGER_INDEX] = SUB_INT_INT;
        sub[INTEGER_INDEX][DOUBLE_INDEX] = SUB_INT_DOUBLE;
        sub[DOUBLE_INDEX][INTEGER_INDEX] = SUB_DOUBLE_INT;
        sub[DOUBLE_INDEX][DOUBLE_INDEX] = SUB_DOUBLE_DOUBLE;
        sub[NULL_INDEX][BOOLEAN_INDEX] = NEG_BOOLEAN_RHS;
        sub[NULL_INDEX][INTEGER_INDEX] = NEG_INTEGER_RHS;
        sub[NULL_INDEX][DOUBLE_INDEX] = NEG_DOUBLE_RHS;
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            sub[i][NULL_INDEX] = FIXED_LHS;
        }

        IBinary[][] mul =
            ARITH_PROCESSOR[BinaryOperator.MULTIPLY.ordinal()];

        mul[INTEGER_INDEX][INTEGER_INDEX] = MUL_INT_INT;
        mul[INTEGER_INDEX][DOUBLE_INDEX] = MUL_INT_DOUBLE;
        mul[DOUBLE_INDEX][INTEGER_INDEX] = MUL_DOUBLE_INT;
        mul[DOUBLE_INDEX][DOUBLE_INDEX] = MUL_DOUBLE_DOUBLE;
        mul[BOOLEAN_INDEX][BOOLEAN_INDEX] = MUL_BOOL_BOOL;
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            mul[i][NULL_INDEX] = FIXED_LHS;
            mul[NULL_INDEX][i] = FIXED_RHS;
        }

        IBinary[][] div =
            ARITH_PROCESSOR[BinaryOperator.DIVIDE.ordinal()];

        div[INTEGER_INDEX][INTEGER_INDEX] = DIV_INT_INT;
        div[INTEGER_INDEX][DOUBLE_INDEX] = DIV_INT_DOUBLE;
        div[DOUBLE_INDEX][INTEGER_INDEX] = DIV_DOUBLE_INT;
        div[DOUBLE_INDEX][DOUBLE_INDEX] = DIV_DOUBLE_DOUBLE;
        div[NULL_INDEX][INTEGER_INDEX] = INV_INTEGER_RHS;
        div[NULL_INDEX][DOUBLE_INDEX] = INV_DOUBLE_RHS;
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            div[i][NULL_INDEX] = FIXED_LHS;
        }

        IBinary[][] rem =
            ARITH_PROCESSOR[BinaryOperator.REMAINDER.ordinal()];

        rem[INTEGER_INDEX][INTEGER_INDEX] = REM_INT_INT;
        rem[INTEGER_INDEX][DOUBLE_INDEX] = REM_INT_DOUBLE;
        rem[DOUBLE_INDEX][INTEGER_INDEX] = REM_DOUBLE_INT;
        rem[DOUBLE_INDEX][DOUBLE_INDEX] = REM_DOUBLE_DOUBLE;
        for (int i = 0 ; i != NUM_ARITH_TYPES ; i++) {
            rem[i][NULL_INDEX] = FIXED_LHS;
        }

    }
    /**
     * This method returns the date format for use in operations
     * on dates, when the other side contains a string.
     *
     * @return a DateFormat object suitable for use in the default locale.
     */
    private static DateFormat getDateFormat() {
        return DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM
        ,   DateFormat.MEDIUM
        );
    }

    /**
     * This method compares two Objects for equality.
     * Two objects are considered equal if they both are null, or
     * if the lhs is not null and its equal(rhs) method returns true.
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing objects for equality.
     */
    private static boolean equalObjectObject(Object lhs, Object rhs) {
        return lhs.equals(rhs);
    }

    /**
     * This method compares two Numbers for equality.
     * Two numbers are considered equal if lhs's equal(rhs) method
     * returns true, or else if the conversion of both objects to
     * a double results in the same value.
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing objects for equality.
     */
    private static boolean equalNumberNumber(Number lhs, Number rhs) {
        return lhs.equals(rhs) || lhs.doubleValue() == rhs.doubleValue();
    }

    /**
     * This method compares a Date and a Number for equality.
     * A Date is considered equal to a Number if the Number's
     * conversion to Long equals the value of Date's getTime().
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing a Date and a Number for equality.
     */
    private static boolean equalDateNumber(Date date, Number number) {
        return date.getTime() == number.longValue();
    }

    /**
     * This method compares a Date and a String for equality.
     * A Date is considered equal to a String if parsing the string
     * as a Date/Time in the default format for the given locale
     * results in a match with the Date.
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing a Date and a String for equality.
     */
    private static boolean equalDateString(Date date, String string) {
        try {
            return date.equals(getDateFormat().parse(string));
        } catch (ParseException ignored) {
            return false;
        }
    }

    /**
     * This method compares a Double and a String for equality.
     * A Double is considered equal to a String if parsing the string
     * as a double in the default format for the given locale
     * results in a match with the Double.
     *
     * @param number The left-hand side of the equality check.
     * @param string The right-hand side of the equality check.
     * @return the result of comparing a Double and a String for equality.
     */
    private static boolean equalDoubleString(Double number, String string) {
        try {
            return number == Double.parseDouble(string);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * This method compares a Long and a String for equality.
     * A Long is considered equal to a String if parsing the string
     * as a long in the default format for the given locale
     * results in a match with the Long.
     *
     * @param number The left-hand side of the equality check.
     * @param string The right-hand side of the equality check.
     * @return the result of comparing a Long and a String for equality.
     */
    private static boolean equalLongString(Long number, String string) {
        try {
            return number == Long.parseLong(string);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * This method compares a Boolean and a String for equality.
     * Boolean value <code>true</code> is considered equal to a String
     * if the String equals "1", "true", or "yes" case-insensitively
     * after trimming; all other string values are considered equal to
     * the Boolean value <code>false</code>.
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing a Boolean and a String for equality.
     */
    private static boolean equalBoolString(boolean lhs, String rhs) {
        return lhs == asBoolean(rhs);
    }


    /**
     * This method compares two strings case-insensitively.
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing two Strings for equality
     * case-insensitively.
     */
    private static boolean equalStringString(String lhs, String rhs) {
        return lhs.equalsIgnoreCase(rhs);
    }

    /**
     * This method compares a Number and a String for equality.
     * Non-zero numbers are considered equal to <code>true</code>;
     * zero is considered equal to <code>false</code>;
     *
     * @param lhs The left-hand side of the equality check.
     * @param rhs The right-hand side of the equality check.
     * @return the result of comparing a Number and a String for equality.
     */
    private static boolean equalBoolNumber(boolean lhs, Number rhs) {
        return lhs == (rhs.doubleValue() != 0);
    }

    /**
     * Evaluates operation less then between two numbers.
     *
     * @param lhs the number representing the left-hand side of the operation.
     * @param rhs the number representing the right-hand side of the operation.
     * @return true if the left-hand side is less than the right-hand side;
     * false otherwise.
     */
    private static boolean lessThanNumberNumber(Number lhs, Number rhs) {
        return lhs.doubleValue() < rhs.doubleValue();
    }

    /**
     * Evaluates operation less then between a date and a number.
     *
     * @param lhs the date representing the left-hand side of the operation.
     * @param rhs the number representing the right-hand side of the operation.
     * @return true if the left-hand side is a date that, when represented as
     * a Java date (i.e. the number of milliseconds passed since 1/1/1970), is
     * less than the right-hand side's value; false otherwise.
     */
    private static boolean lessThanDateNumber(Date date, Number number) {
        return date.getTime() < number.longValue();
    }

    /**
     * Evaluates operation less then between a number and a date.
     *
     * @param lhs the number representing the left-hand side of the operation.
     * @param rhs the date representing the right-hand side of the operation.
     * @return true if the right-hand side is a date that, when represented as
     * a Java date (i.e. the number of milliseconds passed since 1/1/1970), is
     * greater than the left-hand side's value; false otherwise.
     */
    private static boolean lessThanNumberDate(Number number, Date date) {
        return number.longValue() < date.getTime();
    }

    /**
     * Evaluates operation less then between a Double and a string.
     *
     * @param lhs the Double representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid number.
     * @return true if the right-hand side is a string representation of a
     * number that is greater than the left-hand side's value; false otherwise.
     */
    private static boolean lessThanDoubleString(
        double number
    ,   String string
    ,   boolean cannotParse) {
        try {
            return number < Double.parseDouble(string);
        } catch (NumberFormatException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates operation less then between a long and a string.
     *
     * @param lhs the long representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid number.
     * @return true if the right-hand side is a string representation of a
     * number that is greater than the left-hand side's value; false otherwise.
     */
    private static boolean lessThanLongString(
        long number
    ,   String string
    ,   boolean cannotParse) {
        try {
            return number < Long.parseLong(string);
        } catch (NumberFormatException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates operation less then between a boolean and a string.
     *
     * @param lhs the boolean representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @return true if the right-hand side is a string representation of a
     * boolean that is greater than the left-hand side's value;
     * false otherwise.
     */
    private static boolean lessThanBoolStr(boolean lhs, String rhs) {
        return asBoolean(rhs) & !lhs;
    }

    /**
     * Evaluates operation less then between a String and a boolean.
     *
     * @param lhs the string representing the left-hand side of the operation.
     * @param rhs boolean representing the right-hand side of the operation.
     * @return true if the left-hand side is a string representation of a
     * boolean that is greater than the left-hand side's value;
     * false otherwise.
     */
    private static boolean lessThanStrBool(String lhs, boolean rhs) {
        return rhs & !asBoolean(lhs);
    }

    /**
     * Evaluates operation less then between a boolean and a number.
     *
     * @param lhs the boolean representing the left-hand side of the operation.
     * @param rhs the number representing the right-hand side of the operation.
     * @return true if the right-hand side is not zero and the left-hand side
     * is false; false otherwise.
     */
    private static boolean lessThanBoolNumber(boolean lhs, Number rhs) {
        boolean r = rhs.doubleValue() != 0;
        return r & !lhs;
    }

    /**
     * Evaluates operation less then between a number and a boolean.
     *
     * @param lhs the boolean representing the left-hand side of the operation.
     * @param rhs the number representing the right-hand side of the operation.
     * @return true if the right-hand side is not zero and the left-hand side
     * is false; false otherwise.
     */
    private static boolean lessThanNumberBool(Number lhs, boolean rhs) {
        boolean lv = lhs.doubleValue() != 0;
        return rhs & !lv;
    }

    /**
     * Evaluates operation less then between a string and a number.
     *
     * @param lhs the number representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid number.
     * @return true if the left-hand side is a string representation of a
     * number that is less than the right-hand side's value; false otherwise.
     */
    private static boolean lessThanStringDouble(
        String string
    ,   double number
    ,   boolean cannotParse) {
        try {
            return Double.parseDouble(string) < number;
        } catch (NumberFormatException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates operation less then between a string and a number.
     *
     * @param lhs the number representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid number.
     * @return true if the left-hand side is a string representation of a
     * number that is less than the right-hand side's value; false otherwise.
     */
    private static boolean lessThanStringLong(
        String string
    ,   long number
    ,   boolean cannotParse) {
        try {
            return Long.parseLong(string) < number;
        } catch (NumberFormatException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates operation less then between a date and a string.
     *
     * @param lhs the date representing the left-hand side of the operation.
     * @param rhs the string representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid date.
     * @return true if the left-hand side is before the right-hand side
     * interpreted as a string representation of a date; false otherwise.
     */
    private static boolean lessThanDateString(
        Date date
    ,   String string
    ,   boolean cannotParse) {
        try {
            return date.before(getDateFormat().parse(string));
        } catch (ParseException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates operation less then between a string and a date.
     *
     * @param lhs the string representing the left-hand side of the operation.
     * @param rhs the date representing the right-hand side of the operation.
     * @param cannotParse the return value of this method when the string
     * does not represent a valid date.
     * @return true if the right-hand side is after the left-hand side
     * interpreted as a string representation of a date; false otherwise.
     */
    private static boolean lessThanStringDate(
        String string
    ,   Date date
    ,   boolean cannotParse) {
        try {
            return date.after(getDateFormat().parse(string));
        } catch (ParseException ignored) {
            return cannotParse;
        }
    }

    /**
     * Evaluates the operation "less then" between two strings
     * case-insensitively.
     *
     * @param lhs the string on the left-hand side of the operation.
     * @param rhs the string on the right-hand side of the operation.
     * @return true if the left-hand side comes earlier than
     * the right-hand side alphabetically; false otherwise.
     */
    private static boolean lessThanStrStrCI(String lhs, String rhs) {
        return lhs.compareToIgnoreCase(rhs) < 0;
    }

    /**
     * Matches the string against the pattern case-insensitively.
     *
     * @param value the string to be matched against the pattern.
     * @param pattern the pattern against which the string is to be matched.
     * @return the result of matching the pattern against the string.
     */
    private static boolean matchCaseInsensitive(String value, String pattern) {
        return WildcardPattern.compile(pattern).isMatch(value);
    }

    /**
     * Matches the string against the pattern case-sensitively.
     *
     * @param value the string to be matched against the pattern.
     * @param pattern the pattern against which the string is to be matched.
     * @return the result of matching the pattern against the string.
     */
    private static boolean matchCaseSensitive(String value, String pattern) {
        return WildcardPattern.compile(
                    pattern
                ,   CaseSensitivity.SENSITIVE
                ).isMatch(value);
    }

    /**
     * Tries to match the string against the object. If the object is
     * a wildcard pattern, the method will match the string against it;
     * if it is any other kind of object, the method will return false.
     *
     * @param value the string to be matched against the pattern.
     * @param pattern the pattern against which the string is to be matched.
     * @return the result of matching the pattern against the string.
     */
    private static boolean matchStringObject(String value, Object pattern) {
        if (pattern instanceof WildcardPattern) {
            return ((WildcardPattern)pattern).isMatch(value);
        } else {
            return false;
        }
    }

    /**
     * Utility method for interpreting a string as a boolean value.
     * A String is interpreted as <code>true</code> if its trimmed value is
     * either "true", "y", or "yes" ignoring case.
     *
     * @param str the string to be interpreted as boolean.
     * @return the result of interpreting a string as a boolean.
     */
    private static boolean asBoolean(String str) {
        str = str.trim();
        return str.equalsIgnoreCase("true")
            || str.equalsIgnoreCase("y")
            || str.equalsIgnoreCase("yes");
    }

}
