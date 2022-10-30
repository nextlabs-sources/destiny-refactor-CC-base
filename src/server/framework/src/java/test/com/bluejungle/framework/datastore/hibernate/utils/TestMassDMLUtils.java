package com.bluejungle.framework.datastore.hibernate.utils;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/test/com/bluejungle/framework/datastore/hibernate/utils/TestMassDMLUtils.java#1 $
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.DB2Dialect;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.dialect.Oracle9Dialect;
import net.sf.hibernate.dialect.PostgreSQLDialect;
import net.sf.hibernate.dialect.SQLServerDialect;
import net.sf.hibernate.dialect.SybaseDialect;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;

/**
 * This class contains tests for the Mass-DML formatter.
 *
 * @author Sergey Kalinichenko
 */
public class TestMassDMLUtils extends TestCase {

    private static final Dialect POSTGRES_DIALECT = new PostgreSQLDialect();

    private static final Dialect ORACLE_DIALECT = new Oracle9Dialect();

    private static final Dialect DB2_DIALECT = new DB2Dialect();

    private static final Dialect SYBASE_DIALECT = new SybaseDialect();

    private static final Dialect MS_SQL_DIALECT = new SQLServerDialect();

    private static final String TARGET = "x($id$f1,f2,f3)";

    private static final String SOURCE_FIELDS = "$seq$c1,c2,c3";

    private static final String SOURCE = "(select a as c1,b as c2,c as c3 from t)";

    private static final String IDENTITY_INSERT = "insert into x(f1,f2,f3) values (c1,c2,c3)";

    private static final String IDENTITY_INSERT_W_SOURCE = "insert into x(f1,f2,f3) (select c1,c2,c3 from (select a as c1,b as c2,c as c3 from t))";

    private static final String ORACLE_INSERT = "insert into x(id, f1,f2,f3) (select seq.nextval, c1,c2,c3 from dual)";

    private static final String ORACLE_INSERT_W_SOURCE = "insert into x(id, f1,f2,f3) (select seq.nextval, c1,c2,c3 from (select a as c1,b as c2,c as c3 from t))";

    private static final String POSTGRES_INSERT = "insert into x(id, f1,f2,f3) values (nextval ('seq'), c1,c2,c3)";

    private static final String POSTGRES_INSERT_W_SOURCE = "insert into x(id, f1,f2,f3) (select nextval ('seq'), c1,c2,c3 from (select a as c1,b as c2,c as c3 from t))";

    public static TestSuite suite() {
        return new TestSuite(TestMassDMLUtils.class);
    }

    public void testNullSession() {
        try {
            MassDMLUtils.makeFormatter(null);
            fail("Expected an NPE");
        } catch (NullPointerException expected) {
        }
    }

    public void testSybaseWithSource() {
        check(
            SYBASE_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   SOURCE
        ,   IDENTITY_INSERT_W_SOURCE
        );
    }

    public void testSybaseWithoutSource() {
        check(
            SYBASE_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   null
        ,   IDENTITY_INSERT
        );
    }

    public void testSqlServerWithSource() {
        check(
            MS_SQL_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   SOURCE
        ,   IDENTITY_INSERT_W_SOURCE
        );
    }

    public void testSqlServerWithoutSource() {
        check(
            MS_SQL_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   null
        ,   IDENTITY_INSERT
        );
    }

    public void testDB2WithSource() {
        check(
            DB2_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   SOURCE
        ,   IDENTITY_INSERT_W_SOURCE
        );
    }

    public void testDB2WithoutSource() {
        check(
            DB2_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   null
        ,   IDENTITY_INSERT
        );
    }

    public void testOracleSequenceWithSource() {
        check(
            ORACLE_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   SOURCE
        ,   ORACLE_INSERT_W_SOURCE
        );
    }

    public void testOracleSequenceWithoutSource() {
        check(
            ORACLE_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   null
        ,   ORACLE_INSERT
        );
    }
    
    public void testPostgresSequenceWithSource() {
        check(
            POSTGRES_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   SOURCE
        ,   POSTGRES_INSERT_W_SOURCE
        );
    }

    public void testPostgresSequenceWithoutSource() {
        check(
            POSTGRES_DIALECT
        ,   TARGET
        ,   SOURCE_FIELDS
        ,   null
        ,   POSTGRES_INSERT
        );
    }

    private static void check(Dialect d, String t, String sf, String s, String expected) {
        IMassDMLFormatter f = MassDMLUtils.makeFormatter(makeSession(d));
        assertNotNull(f);
        String insert = f.formatInsert(t, sf, s);
        assertEquals(expected, insert);
    }

    private static Session makeSession(final Dialect dialect) {
        return (Session)Proxy.newProxyInstance(
            TestMassDMLUtils.class.getClassLoader()
        ,   new Class<?>[] {SessionImplementor.class}
        ,   new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getFactory")) {
                    return Proxy.newProxyInstance(
                        TestMassDMLUtils.class.getClassLoader()
                    ,   new Class<?>[] {SessionFactoryImplementor.class}
                    ,   new InvocationHandler() {
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("getDialect")) {
                                return dialect;
                            }
                            throw new UnsupportedOperationException(method.getName());
                        }
                    });
                }
                throw new UnsupportedOperationException(method.getName());
            }
        });
    }

}
