package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/test/com/nextlabs/language/formatter/DefinitionFormatterTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.FunctionCall;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.language.formatter.st.PolicyLanguageFormatter;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.language.representation.ContextType;
import com.nextlabs.language.representation.FunctionType;
import com.nextlabs.language.representation.IContextType;
import com.nextlabs.language.representation.IDefinition;
import com.nextlabs.language.representation.IFunctionType;
import com.nextlabs.language.representation.IObligationType;
import com.nextlabs.language.representation.IPolicy;
import com.nextlabs.language.representation.IPolicyComponent;
import com.nextlabs.language.representation.IPolicyType;
import com.nextlabs.language.representation.ObligationType;
import com.nextlabs.language.representation.Outcome;
import com.nextlabs.language.representation.Policy;
import com.nextlabs.language.representation.PolicyComponent;
import com.nextlabs.language.representation.PolicySet;
import com.nextlabs.language.representation.PolicyType;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the predicate formatting functionality of the ACPL formatter.
 *
 * @author Sergey Kalinichenko
 */
public class DefinitionFormatterTests {

    private static final String CRLF = "\r\n";

    private static final IFunctionType funcNoArgs = new FunctionType(
        new Path("test")
    ,   DataType.makeMultivalue(
            DataType.makeReference(ref(15, String.class))
        )
    );

    private static final String FUNCTION_NO_ARG_TXT =
        "function test returns multivalued references id 15";

    private static final FunctionType funcWithArgs = new FunctionType(
        new Path("true")
    ,   DataType.makeReference(ref(new Path("false"), String.class))
    );

    static {
        funcWithArgs.addArgument(
            "first"
        ,   DataType.makeMultivalue(DataType.makeCode(Arrays.asList("Y", "N")))
        ,   true
        ,   Constant.makeString("Y")
        );
        funcWithArgs.addArgument(
            "second"
        ,   IDataType.STRING
        ,   false
        ,   null
        );
    }

    private static final String FUNCTION_ARGS_TXT =
        "function [true](first multivalued code(\"Y\", \"N\") required "
    +   "= \"Y\", second string optional) returns references [false]";

    private static final IObligationType oblNoArgs =
        new ObligationType(new Path("obl"));

    private static final String OBL_NO_ARGS_TXT = "obligation obl";

    private static final ObligationType oblWithArgs =
        new ObligationType(new Path("And"));

    static {
        oblWithArgs.addArgument(
            "first"
        ,   DataType.makeMultivalue(
                DataType.makeCode(Arrays.asList(new String[0]))
            )
        ,   true
        ,   null
        );
        oblWithArgs.addArgument(
            "second"
        ,   IDataType.DATE
        ,   false
        ,   Constant.makeString("1/2/2007")
        );
    }

    private static final String OBL_ARGS_TXT =
        "obligation [And](first multivalued code required, "
    +   "second date optional = \"1/2/2007\")";

    private static final ContextType contextType =
        new ContextType(new Path("ctx"));

    static {
        contextType.setBase(ref(123, IContextType.class));
        contextType.addAttribute(
            "a1"
        ,   DataType.makeMultivalue(IDataType.BOOLEAN)
        ,   false
        );
        contextType.addAttribute("b2", IDataType.DATE, false);
        contextType.addAttributeTemplate(
            "c3"
        ,   DataType.makeMultivalue(IDataType.STRING)
        ,   false
        );
        contextType.addAttributeTemplate("d4", IDataType.INTEGER, true);
    }

    private static final String CTX_TYPE_TXT =
        "context ctx extends id 123 "
    +   "a1 multivalued boolean, b2 date, "
    +   "c3* multivalued string, d4* unique integer";

    private static final Policy policyWithBase = new Policy(
        new Path("withBase")
    ,   ref(123, IPolicy.class)
    ,   null
    );

    static {
        policyWithBase.getTarget().addContext(
            "first"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
        policyWithBase.getTarget().addContext(
            "second"
        ,   new UnaryExpression(
                UnaryOperator.ANY
            ,   IExpression.TRUE
            )
        );
        policyWithBase.getTarget().addContext(
            "third"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
        IReference<IFunction> R1 = ref(new Path("fc1"), IFunction.class);
        IReference<IFunction> R2 = ref(new Path("fc2"), IFunction.class);
        IReference<IFunction> R3 = ref(new Path("fc3"), IFunction.class);
        IReference<IFunction> R4 = ref(new Path("fc4"), IFunction.class);
        policyWithBase.addRule(IExpression.FALSE, Outcome.ALLOW);
        policyWithBase.addRule(IExpression.TRUE, Outcome.DENY);
        FunctionCall fc1 = new FunctionCall(R1);
        fc1.addArgument("n1", IExpression.NULL);
        fc1.addArgument("o1", Constant.makeInteger(1));
        FunctionCall fc2 = new FunctionCall(R2);
        fc2.addArgument("n2", IExpression.NULL);
        fc2.addArgument("o2", Constant.makeInteger(22));
        FunctionCall fc3 = new FunctionCall(R3);
        fc3.addArgument("n3", IExpression.NULL);
        fc3.addArgument("o3", Constant.makeInteger(333));
        FunctionCall fc4 = new FunctionCall(R4);
        fc4.addArgument("n4", IExpression.NULL);
        fc4.addArgument("o4", Constant.makeInteger(4444));
        policyWithBase.addObligation(Outcome.ALLOW, fc1);
        policyWithBase.addObligation(Outcome.ALLOW, fc2);
        policyWithBase.addObligation(Outcome.ALLOW, fc3);
        policyWithBase.addObligation(Outcome.DENY, fc4);
    }

    private static final String POLICY_W_BASE_TXT =
        "policy withBase extends id 123" + CRLF
    +   "    first ALL false" + CRLF
    +   "    second ANY true" + CRLF
    +   "    third ALL false" + CRLF
    +   "do" + CRLF
    +   "    ALLOW when false" + CRLF
    +   "    DENY by default" + CRLF
    +   "    on ALLOW do fc1(n1=null, o1=1), fc2(n2=null, o2=22), "
    +        "fc3(n3=null, o3=333)" + CRLF
    +   "    on DENY do fc4(n4=null, o4=4444)";

    private static final Policy policyWithType = new Policy(
        new Path("withType")
    ,   null
    ,   ref(456, IPolicyType.class)
    );

    static {
        policyWithType.getTarget().addContext(
            "only"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
        policyWithType.addRule(IExpression.TRUE, Outcome.DENY);
        IReference<IFunction> R4 = ref(new Path("fc4"), IFunction.class);
        FunctionCall fc4 = new FunctionCall(R4);
        fc4.addArgument("n4", IExpression.NULL);
        fc4.addArgument("o4", Constant.makeInteger(4444));
        policyWithType.addObligation(Outcome.DENY, fc4);
    }

    private static final String POLICY_W_TYPE_TXT =
        "policy withType is id 456" + CRLF
    +   "    only ALL false" + CRLF
    +   "do" + CRLF
    +   "    DENY by default" + CRLF
    +   "    on DENY do fc4(n4=null, o4=4444)";

    private static final PolicySet emptyPolicySet =
        new PolicySet(new Path("a", "b"));

    static {
        emptyPolicySet.setOverridingOutcome(Outcome.ALLOW);
        emptyPolicySet.addAllowedType(ref(123, IPolicyType.class));
        emptyPolicySet.addAllowedType(ref(456, IPolicyType.class));
        emptyPolicySet.getApplyTarget().addContext(
            "user"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
        emptyPolicySet.getApplyTarget().addContext(
            "host"
        ,   new UnaryExpression(
                UnaryOperator.ANY
            ,   IExpression.TRUE
            )
        );
        emptyPolicySet.getIgnoreTarget().addContext(
            "user"
        ,   new UnaryExpression(
                UnaryOperator.ANY
            ,   IExpression.TRUE
            )
        );
        emptyPolicySet.getApplyTarget().addContext(
            "host"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
    }

    private static final String EMPTY_POLICY_SET_TXT =
        "policy set a/b of id 123, id 456" + CRLF
    +   "    apply when" + CRLF
    +   "        user ALL false" + CRLF
    +   "        host ALL false" + CRLF
    +   "    ignore when" + CRLF
    +   "        user ANY true" + CRLF
    +   "    ALLOW overrides";

    private static final PolicySet nonemptyPolicySet =
        new PolicySet(new Path("ccc"));

    static {
        nonemptyPolicySet.setOverridingOutcome(Outcome.ALLOW);
        nonemptyPolicySet.addAllowedType(ref(123, IPolicyType.class));
        nonemptyPolicySet.addAllowedType(ref(456, IPolicyType.class));
        nonemptyPolicySet.getApplyTarget().addContext(
            "user"
        ,   new UnaryExpression(
                UnaryOperator.ALL
            ,   IExpression.FALSE
            )
        );
        nonemptyPolicySet.getApplyTarget().addContext(
            "host"
        ,   new UnaryExpression(
                UnaryOperator.ANY
            ,   IExpression.TRUE
            )
        );
        nonemptyPolicySet.addPolicy(
            ref(new Path("ps", "first"), IPolicy.class)
        );
        nonemptyPolicySet.addPolicy(
            ref(new Path("ps", "second"), IPolicy.class)
        );
        nonemptyPolicySet.addPolicy(
            ref(new Path("ps", "third"), IPolicy.class)
        );
        nonemptyPolicySet.addPolicy(
            ref(new Path("forth", "and", "a", "half"), IPolicy.class)
         );
    }

    private static final String NONEMPTY_POLICY_SET_TXT =
        "policy set ccc of id 123, id 456" + CRLF
    +   "    apply when" + CRLF
    +   "        user ALL false" + CRLF
    +   "        host ANY true" + CRLF
    +   "    ALLOW overrides (" + CRLF
    +   "        ps/first, ps/second, ps/third, forth/[and]/a/half" + CRLF
    +   "    )";

    private static final IPolicyComponent policyComponent =
        new PolicyComponent(
            new Path("a", "or", "b")
        ,   ref(123, IContextType.class)
        ,   IExpression.TRUE
        );

    private static final String POLICY_COMPONENT_TXT =
        "component a/[or]/b is id 123 = true";

    private static final PolicyType policyTypeNoBase =
        new PolicyType(new Path("pt1"));

    static {
        policyTypeNoBase.addContext("a", ref(1, IContextType.class));
        policyTypeNoBase.addContext("a", ref(2, IContextType.class));
        policyTypeNoBase.addContext("a", ref(3, IContextType.class));
        policyTypeNoBase.setSectionRequired("a", true);
        policyTypeNoBase.addContext("b", ref(10, IContextType.class));
        policyTypeNoBase.addContext("b", ref(20, IContextType.class));
        policyTypeNoBase.addContext("b", ref(30, IContextType.class));
        policyTypeNoBase.addContext(ref(100, IContextType.class));
        policyTypeNoBase.addContext(ref(200, IContextType.class));
        policyTypeNoBase.addContext(ref(300, IContextType.class));
        policyTypeNoBase.addObligation(ref(1000, IObligationType.class));
        policyTypeNoBase.addObligation(ref(2000, IObligationType.class));
        policyTypeNoBase.addObligation(ref(3000, IObligationType.class));
    }

    private static final String POLICY_TYPE_NO_BASE =
        "policy type pt1" + CRLF
    +   "    required id 1, id 2, id 3 as a" + CRLF
    +   "    optional id 10, id 20, id 30 as b" + CRLF
    +   "    uses id 100, id 200, id 300" + CRLF
    +   "    with obligation id 1000, id 2000, id 3000";

    private static final PolicyType policyTypeWithBase =
        new PolicyType(new Path("pt1"));

    static {
        policyTypeWithBase.setBase(ref(-1, IPolicyType.class));
        policyTypeWithBase.addContext("a", ref(1, IContextType.class));
        policyTypeWithBase.setSectionRequired("a", true);
    }

    private static final String POLICY_TYPE_WITH_BASE =
        "policy type pt1 extends id -1" + CRLF
    +   "    required id 1 as a";

    private IPolicyLanguageFormatter f;

    private StringWriter w;

    @Before
    public void prepare() throws PolicyLanguageException {
        IPolicyFormatterFactory factory = new PolicyFormatterFactory();
        f = factory.getFormatter("acpl", 1);
        w = new StringWriter();
    }

    @Test
    public void functionNoArgs() throws IOException {
        f.formatFunctionType(w, funcNoArgs);
        assertEquals( FUNCTION_NO_ARG_TXT, w.toString());
    }

    @Test
    public void functionWithArgs() throws IOException {
        f.formatFunctionType(w, funcWithArgs);
        assertEquals( FUNCTION_ARGS_TXT, w.toString());
    }

    @Test
    public void obligationNoArgs() throws IOException {
        f.formatObligationType(w, oblNoArgs);
        assertEquals(OBL_NO_ARGS_TXT, w.toString());
    }

    @Test
    public void obligationWithArgs() throws IOException {
        f.formatObligationType(w, oblWithArgs);
        assertEquals(OBL_ARGS_TXT, w.toString());
    }

    @Test
    public void contextType() throws IOException {
        f.formatContextType(w, contextType);
        assertEquals(CTX_TYPE_TXT, w.toString());
    }

    @Test
    public void policyWithBase() throws IOException {
        f.formatPolicy(w, policyWithBase);
        assertEquals(POLICY_W_BASE_TXT, w.toString());
    }

    @Test
    public void policyWithType() throws IOException {
        f.formatPolicy(w, policyWithType);
        assertEquals(POLICY_W_TYPE_TXT, w.toString());
    }

    @Test
    public void emptyPolicySet() throws IOException {
        f.formatPolicySet(w, emptyPolicySet);
        assertEquals(EMPTY_POLICY_SET_TXT, w.toString());
    }

    @Test
    public void nonEmptyPolicySet() throws IOException {
        f.formatPolicySet(w, nonemptyPolicySet);
        assertEquals(NONEMPTY_POLICY_SET_TXT, w.toString());
    }

    @Test
    public void policyComponent() throws IOException {
        f.formatPolicyComponent(w, policyComponent);
        assertEquals(POLICY_COMPONENT_TXT, w.toString());
    }

    @Test
    public void policyType() throws IOException {
        f.formatPolicyType(w, policyTypeNoBase);
        assertEquals(POLICY_TYPE_NO_BASE, w.toString());
    }

    @Test
    public void policyTypeWithBase() throws IOException {
        f.formatPolicyType(w, policyTypeWithBase);
        assertEquals(POLICY_TYPE_WITH_BASE, w.toString());
    }

    @Test
    public void formatDefsArray() throws IOException {
        String sep = "#$#";
        f.format(
            w
        ,   sep
        ,   emptyPolicySet
        ,   funcNoArgs
        ,   oblWithArgs
        ,   policyWithBase
        );
        assertEquals(
            EMPTY_POLICY_SET_TXT + sep
        +   FUNCTION_NO_ARG_TXT + sep
        +   OBL_ARGS_TXT + sep
        +   POLICY_W_BASE_TXT
        ,   w.toString()
        );
    }

    @Test
    public void formatDefsCollection() throws IOException {
        List<IDefinition<?>> defs = new ArrayList<IDefinition<?>>();
        StringBuffer expected = new StringBuffer();
        defs.add(contextType);
        expected.append(CTX_TYPE_TXT);
        expected.append(CRLF);
        defs.add(emptyPolicySet);
        expected.append(EMPTY_POLICY_SET_TXT);
        expected.append(CRLF);
        defs.add(funcNoArgs);
        expected.append(FUNCTION_NO_ARG_TXT);
        expected.append(CRLF);
        defs.add(funcWithArgs);
        expected.append(FUNCTION_ARGS_TXT);
        expected.append(CRLF);
        defs.add(nonemptyPolicySet);
        expected.append(NONEMPTY_POLICY_SET_TXT);
        expected.append(CRLF);
        defs.add(oblNoArgs);
        expected.append(OBL_NO_ARGS_TXT);
        expected.append(CRLF);
        defs.add(oblWithArgs);
        expected.append(OBL_ARGS_TXT);
        expected.append(CRLF);
        defs.add(policyComponent);
        expected.append(POLICY_COMPONENT_TXT);
        expected.append(CRLF);
        defs.add(policyTypeNoBase);
        expected.append(POLICY_TYPE_NO_BASE);
        expected.append(CRLF);
        defs.add(policyTypeWithBase);
        expected.append(POLICY_TYPE_WITH_BASE);
        expected.append(CRLF);
        defs.add(policyWithBase);
        expected.append(POLICY_W_BASE_TXT);
        expected.append(CRLF);
        defs.add(policyWithType);
        expected.append(POLICY_W_TYPE_TXT);
        f.format(w, CRLF, defs);
        assertEquals(expected.toString(), w.toString());
    }

    @Test(expected=NullPointerException.class)
    public void formatNullDefs() throws IOException {
        f.format(w, CRLF, (Iterable<IDefinition<?>>)null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void coverageGetInvalidVersion() {
        IPolicyFormatterFactory factory = new PolicyFormatterFactory();
        factory.getFormatter("acpl", -1);
    }

    @Test
    public void coverageUnknownTemplate() {
        StringTemplateGroup.loadGroup("<Unknown>");
    }

    @Test
    public void coverageWarning() throws Exception {
        Field listenerField = PolicyLanguageFormatter.class.getDeclaredField(
            "NULL_LISTENER"
        );
        listenerField.setAccessible(true);
        Object listener = listenerField.get(null);
        assertTrue(listener instanceof StringTemplateErrorListener);
        StringTemplateErrorListener el = (StringTemplateErrorListener)listener;
        el.warning(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void coverageGetTemplates() throws Throwable {
        Method m = PolicyLanguageFormatter.class.getDeclaredMethod(
            "getTemplates"
        ,   String.class
        ,   Integer.TYPE
        ,   Map.class
        );
        m.setAccessible(true);
        try {
            m.invoke(null, null, 0, null);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    private static <T> IReference<T> ref(Path p, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(p, type);
    }

    private static <T> IReference<T> ref(long id, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(id, type);
    }

}
