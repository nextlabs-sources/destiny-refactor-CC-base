package com.bluejungle.pf.domain.destiny.resource;

/*
 * Created on Mar 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.Resource;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/TestResourceSpecNameCondRemote.java#1 $:
 */

public class TestResourceSpecNameCondRemote extends TestCase {

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
     * Constructor for TestResourceSpecNameCondRemote.
     * @param name
     */
    public TestResourceSpecNameCondRemote(String name) {
        super(name);
    }
    
    public final void testIsResourceInRemote() {
        IPredicate cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "c:\\**finance**\\*.xls" );
        IResource resource = makeResource("H:\\blah\\my finance stuff\\stuff.xls");
        EvaluationRequest request = new EvaluationRequest(resource);        
        request.setFromResource(resource);
        assertFalse(cond.match(request));

        cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "\\\\*foo*\\*share*\\**legal**");
        resource = makeResource("\\\\somefooserver\\somesharefoo\\blah\\legal\\baz\\something.xml");
        request.setFromResource(resource);
        assertTrue(cond.match(request));
        
        cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "\\somedir\\here\\*.doc");        
        resource = makeResource("\\\\server\\somedir\\here\\foo.doc");
        request.setFromResource(resource);
        assertFalse(cond.match(request));
        
        cond = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "otherdir\\**there**\\*");        
        resource = makeResource("\\\\server\\otherdir\\foo\\overtherehere\\boo");
        request.setFromResource(resource);
        assertFalse(cond.match(request));
    }    

    private static IResource makeResource(String s) {
        IMResource res = new Resource(s);
        res.setAttribute("name", s);
        return res;
    }

}
