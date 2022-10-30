package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/FunctionCallTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the function call class.
 *
 * @author Sergey Kalinichenko
 */
public class FunctionCallTests {

    private static final String[] NAMES_1 = new String[] {
        "abcd"
    };

    private static final String[] NAMES_2 = new String[] {
        "abcd", "efgh"
    };

    private static final String[] NAMES_3 = new String[] {
        "abcd", "efgh", "ijkl"
    };

    private static final String[] NAMES_4 = new String[] {
        "abcd", "efgh", "ijkl", "mnop"
    };

    private static final List<String> NAME_LIST_1 = Arrays.asList(NAMES_1);

    private static final List<String> NAME_LIST_2 = Arrays.asList(NAMES_2);

    private static final List<String> NAME_LIST_3 = Arrays.asList(NAMES_3);

    private static final List<String> NAME_LIST_4 = Arrays.asList(NAMES_4);

    private static final IExpression[] ARGS_1 = new IExpression[] {
        Constant.makeDouble(0)
    };

    private static final IExpression[] ARGS_2 = new IExpression[] {
        Constant.makeInteger(0), Constant.makeInteger(1)
    };

    private static final IExpression[] ARGS_3 = new IExpression[] {
        Constant.makeDouble(0)
    ,   Constant.makeDouble(1)
    ,   Constant.makeDouble(2)
    };

    private static final IExpression[] ARGS_4 = new IExpression[] {
        Constant.makeDouble(0)
    ,   Constant.makeDouble(1)
    ,   Constant.makeDouble(2)
    ,   Constant.makeDouble(3)
    };

    private static final List<IExpression> ARG_LIST_1 = Arrays.asList(ARGS_1);

    private static final List<IExpression> ARG_LIST_2 = Arrays.asList(ARGS_2);

    private static final List<IExpression> ARG_LIST_3 = Arrays.asList(ARGS_3);

    private static final List<IExpression> ARG_LIST_4 = Arrays.asList(ARGS_4);

    private static final IReference<IFunction> PATH_REF =
        IReferenceFactory.DEFAULT.create(new Path("xyz"), IFunction.class);

    private static final IReference<IFunction> ID_REF =
        IReferenceFactory.DEFAULT.create(12345, IFunction.class);

    private IFunctionCall pathFourArgsByName;

    private IFunctionCall idFourArgsByName;

    private IFunctionCall pathFourArgs;

    private IFunctionCall idFourArgs;

    private FunctionCall idNoargs;

    @Before
    public void prepare() {
        pathFourArgsByName = new FunctionCall(PATH_REF, NAMES_4, ARGS_4);
        idFourArgsByName = new FunctionCall(ID_REF, NAMES_4, ARGS_4);
        pathFourArgs = new FunctionCall(PATH_REF, ARGS_4);
        idFourArgs = new FunctionCall(ID_REF, ARGS_4);
        idNoargs = new FunctionCall(ID_REF);
    }

    @Test
    public void createFromPathAndTwoArrays() {
        assertEquals(pathFourArgsByName.getArgumentCount(), 4);
        checkReferenceByPath(pathFourArgsByName.getFunction());
        assertTrue(pathFourArgsByName.hasNamedArguments());
    }

    @Test
    public void createFromIdAndTwoArrays() {
        assertEquals(idFourArgsByName.getArgumentCount(), 4);
        checkReferenceById(idFourArgsByName.getFunction());
        assertTrue(idFourArgsByName.hasNamedArguments());
    }

    @Test
    public void createFromPathAndOneArray() {
        assertEquals(pathFourArgs.getArgumentCount(), 4);
        checkReferenceByPath(pathFourArgs.getFunction());
        assertFalse(pathFourArgs.hasNamedArguments());
    }

    @Test
    public void createFromIdAndOneArray() {
        assertEquals(idFourArgs.getArgumentCount(), 4);
        checkReferenceById(idFourArgs.getFunction());
        assertFalse(idFourArgs.hasNamedArguments());
    }

    @Test
    public void createFromPathAndTwoLists() {
        IFunctionCall fc =
            new FunctionCall(PATH_REF, NAME_LIST_4, ARG_LIST_4);
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceByPath(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromIdAndTwoLists() {
        IFunctionCall fc = new FunctionCall(ID_REF, NAME_LIST_4, ARG_LIST_4);
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceById(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromPathAndArgList() {
        IFunctionCall fc = new FunctionCall(
            PATH_REF, convertArgs(NAMES_4, ARGS_4)
        );
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceByPath(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromIdAndArgList() {
        IFunctionCall fc = new FunctionCall(
            ID_REF, convertArgs(NAMES_4, ARGS_4)
        );
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceById(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromPathAndArgArray() {
        IFunctionCall fc = new FunctionCall(
            PATH_REF, convertArgs(NAMES_4, ARGS_4).toArray(
                new IFunctionCall.Argument[4]
            )
        );
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceByPath(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromIdAndArgArray() {
        IFunctionCall fc = new FunctionCall(
            ID_REF, convertArgs(NAMES_4, ARGS_4).toArray(
                new IFunctionCall.Argument[4]
            )
        );
        assertEquals(fc.getArgumentCount(), 4);
        checkReferenceById(fc.getFunction());
        assertTrue(fc.hasNamedArguments());
    }

    @Test
    public void createFromPath() {
        IFunctionCall fc = new FunctionCall(PATH_REF);
        assertEquals(fc.getArgumentCount(), 0);
        checkReferenceByPath(fc.getFunction());
        assertFalse(fc.hasNamedArguments());
    }

    @Test(expected=NullPointerException.class)
    public void createFromNullPath() {
        new FunctionCall(null);
    }

    @Test
    public void createFromId() {
        assertEquals(idNoargs.getArgumentCount(), 0);
        checkReferenceById(idNoargs.getFunction());
        assertFalse(idNoargs.hasNamedArguments());
    }

    @Test(expected=NullPointerException.class)
    public void nullFunctionPath() {
        new FunctionCall(null, ARGS_4);
    }

    @Test(expected=NullPointerException.class)
    public void nullArgumentList() {
        new FunctionCall(PATH_REF, (Iterable<IFunctionCall.Argument>)null);
    }

    @Test(expected=NullPointerException.class)
    public void nullArgumentNames() {
        new FunctionCall(PATH_REF, null, ARGS_4);
    }

    @Test(expected=NullPointerException.class)
    public void nullArgumentExpressions() {
        new FunctionCall(PATH_REF, NAMES_4, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullArgumentNameList() {
        new FunctionCall(PATH_REF, null, ARG_LIST_4);
    }

    @Test(expected=NullPointerException.class)
    public void nullArgumentExpressionList() {
        new FunctionCall(PATH_REF, NAME_LIST_4, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsNameAndArrays1() {
        new FunctionCall(PATH_REF, NAMES_4, ARGS_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsNameAndArrays2() {
        new FunctionCall(PATH_REF, NAMES_1, ARGS_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsIdAndArrays1() {
        new FunctionCall(ID_REF, NAMES_4, ARGS_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsIdAndArrays2() {
        new FunctionCall(ID_REF, NAMES_1, ARGS_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsNameAndLists1() {
        new FunctionCall(PATH_REF, NAME_LIST_3, ARG_LIST_1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsNameAndLists2() {
        new FunctionCall(PATH_REF, NAME_LIST_1, ARG_LIST_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsIdAndLists1() {
        new FunctionCall(ID_REF, NAME_LIST_4, ARG_LIST_2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void mismatchedNUmberOfArgumentsIdAndLists2() {
        new FunctionCall(ID_REF, NAME_LIST_2, ARG_LIST_4);
    }

    @Test
    public void addMultipleArguments() {
        assertFalse(idNoargs.hasNamedArguments());
        idNoargs.addArguments(convertArgs(NAMES_3, ARGS_3));
        assertTrue(idNoargs.hasNamedArguments());
        assertEquals(NAMES_3.length, idNoargs.getArgumentCount());
    }

    @Test
    public void addPositionalArgumentToEmpty() {
        assertFalse(idNoargs.hasNamedArguments());
        assertEquals(0, idNoargs.getArgumentCount());
        idNoargs.addArgument(IExpression.NULL);
        assertEquals(1, idNoargs.getArgumentCount());
        assertFalse(idNoargs.hasNamedArguments());
    }

    @Test
    public void addNamedArgumentToEmpty() {
        assertFalse(idNoargs.hasNamedArguments());
        assertEquals(0, idNoargs.getArgumentCount());
        idNoargs.addArgument("abc", IExpression.NULL);
        assertEquals(1, idNoargs.getArgumentCount());
        assertTrue(idNoargs.hasNamedArguments());
    }

    @Test
    public void addPositionalArgumentToNonEmpty() {
        FunctionCall fc = new FunctionCall(ID_REF, ARGS_1);
        assertFalse(fc.hasNamedArguments());
        assertEquals(1, fc.getArgumentCount());
        fc.addArgument(IExpression.NULL);
        assertEquals(2, fc.getArgumentCount());
        assertFalse(fc.hasNamedArguments());
    }

    @Test
    public void addNamedArgumentToNonEmpty() {
        FunctionCall fc = new FunctionCall(ID_REF, NAMES_1, ARGS_1);
        assertTrue(fc.hasNamedArguments());
        assertEquals(1, fc.getArgumentCount());
        fc.addArgument("abc", IExpression.NULL);
        assertEquals(2, fc.getArgumentCount());
        assertTrue(fc.hasNamedArguments());
    }

    @Test(expected=IllegalArgumentException.class)
    public void addPositionalArgumentInvalid() {
        FunctionCall fc = new FunctionCall(ID_REF, NAMES_1, ARGS_1);
        fc.addArgument(IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addNamedArgumentInvalid() {
        FunctionCall fc = new FunctionCall(ID_REF, ARGS_1);
        fc.addArgument("abc", IExpression.NULL);
    }

    @Test
    public void toStringForPositional() {
        FunctionCall fc = new FunctionCall(ID_REF, ARGS_2);
        assertEquals("id "+ID_REF.getId()+"(0, 1)", fc.toString());
    }

    @Test
    public void toStringForReferencedByPath() {
        FunctionCall fc = new FunctionCall(PATH_REF, NAMES_2, ARGS_2);
        assertEquals("#"+PATH_REF.getPath()+"(abcd=0, efgh=1)", fc.toString());
    }

    @Test
    public void createNamedArgument() {
        IFunctionCall.Argument arg =
            FunctionCall.createArgument("abc", IExpression.NULL);
        assertNotNull(arg);
        assertEquals("abc", arg.getName());
        assertEquals(IExpression.NULL, arg.getExpression());
    }

    @Test
    public void createPositionalArgument() {
        IFunctionCall.Argument arg =
            FunctionCall.createArgument(IExpression.NULL);
        assertNotNull(arg);
        assertNull(arg.getName());
        assertEquals(IExpression.NULL, arg.getExpression());
    }

    @Test(expected=NullPointerException.class)
    public void createNamedArgumentNullName() {
        FunctionCall.createArgument(null, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void createNamedArgumentNullExpression() {
        FunctionCall.createArgument("abc", null);
    }

    @Test(expected=NullPointerException.class)
    public void createPositionalArgumentNull() {
        FunctionCall.createArgument(null);
    }

    @Test(expected=NullPointerException.class)
    public void addNullArgumentExpression() {
        FunctionCall fc = new FunctionCall(PATH_REF);
        fc.addArgument((IExpression)null);
    }

    @Test(expected=NullPointerException.class)
    public void addNullArgument() {
        FunctionCall fc = new FunctionCall(PATH_REF);
        fc.addArgument((IFunctionCall.Argument)null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addDuplicateArgument() {
        FunctionCall fc = new FunctionCall(PATH_REF);
        fc.addArgument("abc", IExpression.NULL);
        fc.addArgument("abc", IExpression.NULL);
    }

    @Test
    public void accept() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        idNoargs.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitFunction", ev.getMethods()[0]);
        assertSame(idNoargs, ev.getArguments()[0]);
    }

    @Test
    public void iterator() {
        int i = 0;
        for (IExpression a : idFourArgsByName) {
            assertSame(ARGS_4[i++], a);
        }
    }

    @Test(expected=UnsupportedOperationException.class)
    public void iteratorRemove() {
        Iterator<IExpression> i = idFourArgsByName.iterator();
        i.remove();
    }

    @Test
    public void argIterator() {
        int i=0;
        for (IFunctionCall.Argument a : idFourArgsByName.getArguments()) {
            assertNotNull(a);
            assertEquals(NAMES_4[i], a.getName());
            assertSame(ARGS_4[i], a.getExpression());
            i++;
        }
    }

    @Test(expected=UnsupportedOperationException.class)
    public void argIteratorRemove() {
        idFourArgsByName.getArguments().iterator().remove();
    }

    @Test
    public void argumentByIndex() {
        for ( int i = 0 ; i != NAMES_4.length ; i++) {
            IFunctionCall.Argument a = idFourArgsByName.getArgument(i);
            assertEquals(NAMES_4[i], a.getName());
            assertSame(ARGS_4[i], a.getExpression());
        }
    }

    @Test
    public void argumentNameByIndex() {
        for ( int i = 0 ; i != NAMES_4.length ; i++) {
            assertEquals(NAMES_4[i], idFourArgsByName.getArgumentName(i));
        }
    }

    @Test
    public void argumentExpressionByIndex() {
        for ( int i = 0 ; i != NAMES_4.length ; i++) {
            assertSame(ARGS_4[i], idFourArgsByName.getExpression(i));
        }
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentNameByNegativeIndex() {
        idFourArgsByName.getArgumentName(-2);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentExpressionByNegativeIndex() {
        idFourArgsByName.getExpression(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentByNegativeIndex() {
        idFourArgsByName.getArgument(-12345678);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentNameByLargeIndex() {
        idFourArgsByName.getArgumentName(2000);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentExpressionByLargeIndex() {
        idFourArgsByName.getExpression(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void argumentByLargeIndex() {
        idFourArgsByName.getArgument(5);
    }

    @Test
    public void getByName() {
        for ( int i = 0 ; i != NAMES_4.length ; i++ ) {
            assertSame(ARGS_4[i], idFourArgsByName.getExpression(NAMES_4[i]));
        }
    }

    @Test
    public void getByIllegalName() {
        assertNull(idFourArgsByName.getExpression(""));
    }

    @Test
    public void getByNamePositional() {
        assertNull(idFourArgs.getExpression(NAMES_1[0]));
    }

    @Test(expected=NullPointerException.class)
    public void getByNullName() {
        idFourArgs.getExpression(null);
    }

    @Test(expected=NullPointerException.class)
    public void createWithNullExpressionArray() {
        new FunctionCall(ID_REF, (IExpression[])null);
    }

    @Test
    public void equalityNamed() {
        IFunctionCall other = new FunctionCall(PATH_REF, NAMES_4, ARGS_4);
        assertEquals(pathFourArgsByName, other);
        assertEquals(other, pathFourArgsByName);
    }

    @Test
    public void equalityPositional() {
        IFunctionCall other = new FunctionCall(PATH_REF, ARGS_4);
        assertEquals(pathFourArgs, other);
        assertEquals(other, pathFourArgs);
    }

    @Test
    public void argumentInequalityToNull() {
        IFunctionCall.Argument arg = pathFourArgsByName.getArgument(0);
        assertFalse(arg.equals(null));
    }

    @Test
    public void argumentInequalityToUnknown() {
        IFunctionCall.Argument arg = pathFourArgsByName.getArgument(0);
        assertFalse(arg.equals(123.456));
    }

    @Test
    public void argumentHashCodeNamed() {
        IFunctionCall.Argument arg0 = pathFourArgsByName.getArgument(0);
        IFunctionCall.Argument arg1 = pathFourArgsByName.getArgument(1);
        assertTrue(arg0.hashCode() != arg1.hashCode());
    }

    @Test
    public void argumentHashCodePositional() {
        IFunctionCall.Argument arg0 = pathFourArgs.getArgument(0);
        IFunctionCall.Argument arg1 = pathFourArgs.getArgument(1);
        assertTrue(arg0.hashCode() != arg1.hashCode());
    }

    @Test
    public void equalityToSelf() {
        assertEquals(pathFourArgs, pathFourArgs);
    }

    @Test
    public void inequalityToDifferentFunction() {
        assertFalse(pathFourArgs.equals(idFourArgs));
        assertFalse(idFourArgs.equals(pathFourArgs));
    }

    @Test
    public void inequalityToDifferentArgPassing() {
        assertFalse(pathFourArgs.equals(pathFourArgsByName));
        assertFalse(pathFourArgsByName.equals(pathFourArgs));
    }

    @Test
    public void inequalityToDifferentArgList() {
        assertFalse(idFourArgs.equals(idNoargs));
        assertFalse(idNoargs.equals(idFourArgs));
    }

    @Test
    public void inequalityToDifferentArgPosition() {
        IFunctionCall lhs = new FunctionCall(ID_REF, IExpression.NULL);
        IFunctionCall rhs = new FunctionCall(ID_REF, IExpression.TRUE);
        assertFalse(lhs.equals(rhs));
        assertFalse(rhs.equals(lhs));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(pathFourArgs.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(pathFourArgs.equals(PATH_REF));
    }

    @Test
    public void hashCodeWorks() {
        assertTrue(pathFourArgs.hashCode() != idFourArgs.hashCode());
        IFunctionCall other = new FunctionCall(PATH_REF, NAMES_4, ARGS_4);
        assertEquals(pathFourArgsByName.hashCode(), other.hashCode());
    }

    private static void checkReferenceById(IReference<IFunction> fr) {
        assertNotNull(fr);
        assertFalse(fr.isByPath());
        assertEquals(ID_REF.getId(), fr.getId());
    }

    private static void checkReferenceByPath(IReference<IFunction> fr) {
        assertNotNull(fr);
        assertTrue(fr.isByPath());
        assertEquals(PATH_REF.getPath(), fr.getPath());
    }

    private static List<IFunctionCall.Argument> convertArgs(
        String[] names, IExpression[] expressions
    ) {
        List<IFunctionCall.Argument> res =
            new ArrayList<IFunctionCall.Argument>();
        for ( int i = 0 ; i != names.length ; i++ ) {
            res.add(FunctionCall.createArgument(names[i], expressions[i]));
        }
        return res;
    }

}
