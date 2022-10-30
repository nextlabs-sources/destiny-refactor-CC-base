package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/DefaultDefinitionVisitorTests.java#1 $
 */

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the default definition visitor class.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultDefinitionVisitorTests {

    private static final Path TEST_PATH = new Path(
        "context", "test", "path"
    );

    private static final long TEST_ID = 123;

    private DefaultDefinitionVisitor v;

    @Before
    public void prepare() {
        v = new DefaultDefinitionVisitor();
    }

    @Test
    public void visitMethodsAcceptNull() {
        v.visitContextType(null);
        v.visitFunctionType(null);
        v.visitObligationType(null);
        v.visitPolicy(null);
        v.visitPolicyComponent(null);
        v.visitPolicyType(null);
    }

    @Test
    public void visitMethodsWork() {
        v.visitContextType(new ContextType(TEST_PATH));
        v.visitFunctionType(new FunctionType(TEST_PATH, IDataType.DATE));
        v.visitObligationType(new ObligationType(TEST_PATH));
        v.visitPolicy(
            new Policy(
                TEST_PATH
            ,   IReferenceFactory.DEFAULT.create(TEST_ID, IPolicy.class)
            ,   null
            )
        );
        v.visitPolicySet(new PolicySet(TEST_PATH));
        v.visitPolicyComponent(
            new PolicyComponent(
               TEST_PATH
            ,   IReferenceFactory.DEFAULT.create(TEST_ID, IContextType.class)
            ,   IExpression.TRUE
            )
        );
        v.visitPolicyType(new PolicyType(TEST_PATH));
    }

}
