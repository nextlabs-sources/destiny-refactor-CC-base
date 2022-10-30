/*
 * Created on Jan 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.domain.destiny.resource;

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.Resource;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/TestResourceSpecNameCondLocal.java#1 $:
 */

public class TestResourceSpecNameCondLocal extends TestCase {
    
    private static final String PREFIX = "file://**/";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestResourceSpecNameCondLocal.
     * 
     * @param arg0
     */
    public TestResourceSpecNameCondLocal(String arg0) {
        super(arg0);
    }

    public final void testIsResourceInLocal() {
        IPredicate cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "c:\\**finance**\\*.xls" );
        IResource resource = makeResource("file:///c:/blah/myfinancestuff/stuff.xls");
        EvaluationRequest request = new EvaluationRequest(resource);
        assertTrue(cond.match(request));


        cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "\\somedir\\here\\*.doc" );
        resource = makeResource("file:///f:/somedir/here/foo.doc");
        request.setFromResource(resource);
        assertTrue(cond.match(request));

        cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "otherdir\\**there**\\*" );
        resource = makeResource("file:///z:/otherdir/foo/overtherehere/boo");
        request.setFromResource(resource);
        assertTrue(cond.match(request));
    }

    public void testLeftTrimming() {
        String n = " \t\n\r \n\n\r\r\t\t\t\t    hello world";
        String c = build(n);
        assertEquals(n.trim(), c);
    }

    public void testRightTrimming() {
        String n = "hello world \t\n\r \n\n\r\r\t\t\t\t    ";
        String c = build(n);
        assertEquals(n.trim(), c);
    }

    public void testTrimmingBothSides() {
        String n = " \t\n\r \t\n\r \n\nhello world\r\r\t\t\t\t";
        String c = build(n);
        assertEquals(n.trim(), c);
    }

    public void testEmptyPathAfterTrim() {
        String n = " \t\n\r \t\n\r \n\n\r\r\t\t\t\t";
        String c = build(n);
        assertEquals(0, c.length());
    }

    public void testEmptyPath() {
        String n = "";
        String c = build(n);
        assertEquals(0, c.length());
    }

    public void testWildcardsQuestionMark() {
        String n = "?x?d";
        String c = build(n);
        assertEquals(n, c);
    }

    public void testWildcardsStars() {
        String n = "hello**world";
        String c = build(n);
        assertEquals(n, c);
    }

    public void testWildcardsExclamation() {
        String n = "Hello!dWorld";
        String c = build(n);
        assertEquals(n.toLowerCase(), c);
    }

    public void testWildcardsCaseSensitive() {
        String n = "Hello!DWorld";
        String c = build(n);
        assertEquals("hello!Dworld", c);
    }

    public void testEscapeCaseSensitive() {
        String n = "?X?d";
        String c = build(n);
        assertEquals(n, c);
    }

    public void testEndOfLineWildcardSymbols() {
        String n = "HellO, WorlD!";
        String c = build(n);
        assertEquals(n.toLowerCase(), c);
    }

    public void testForwardSlashReplacement() {
        String n = "hello\\world";
        String c = build(n);
        assertEquals("hello/world", c);
    }

    private static String build(String s) {
        Constant c = ((ResourceAttribute.NameAttributeBase)ResourceAttribute.NAME).build(s, s);
        assertNotNull(c);
        IEvalValue val = c.getValue();
        assertNotNull(val);
        assertTrue(val.getValue() instanceof String);
        String res = (String)val.getValue();
        assertTrue(res.startsWith(PREFIX));
        return res.substring(PREFIX.length());
    }

    private static IResource makeResource(String s) {
        IMResource res = new Resource(s);
        res.setAttribute("name", s);
        return res;
    }

}
