package com.bluejungle.pf.domain.destiny.resource;

/*
 * Created on Jan 20, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.Resource;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/TestResourceSpecGroup.java#1 $:
 */

public class TestResourceSpecGroup extends TestCase {
    /**
     * Constructor for TestResourceSpecGroup.
     * @param arg0
     */
    public TestResourceSpecGroup(String arg0) {
        super(arg0);
    }

    public final void testIsResourceIn() {
        IPredicate testName = ResourceAttribute.NAME.buildRelation( RelationOp.EQUALS, "**test**" );
        IResource res = makeResource("file:///c:\\foo\\tests\\bar.doc");
        EvaluationRequest request = new EvaluationRequest(res);
        assertTrue (testName.match(request));

        IPredicate notGroup = new CompositePredicate( BooleanOp.NOT, testName );
        assertFalse(notGroup.match(request));

        CompositePredicate orGroup = new CompositePredicate(BooleanOp.OR, testName);
        orGroup.addPredicate(notGroup);
        assertTrue(orGroup.match(request));

        CompositePredicate andGroup = new CompositePredicate(BooleanOp.AND, testName);
        andGroup.addPredicate(notGroup);
        assertFalse(andGroup.match(request));
    }

    private static IResource makeResource(String s) {
        IMResource res = new Resource(s);
        res.setAttribute("name", s);
        return res;
    }

}
