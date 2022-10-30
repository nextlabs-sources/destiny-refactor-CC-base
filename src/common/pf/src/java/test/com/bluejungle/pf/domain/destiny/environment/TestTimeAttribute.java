/*
 * Created on Aug 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.environment;

import junit.framework.TestCase;
import junit.framework.TestResult;

import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/environment/TestTimeAttribute.java#1 $:
 */

public class TestTimeAttribute extends TestCase {

    
    
    /**
     * @see junit.framework.TestCase#run()
     */
    public TestResult run() {
        return super.run();
    }
    
    public void testTIME(){
        IRelation rel = TimeAttribute.TIME.buildRelation(RelationOp.GREATER_THAN_EQUALS, "0:00:00 AM");
        assertNotNull(rel);
        EvaluationRequest er = new EvaluationRequest();
        assertTrue(rel.match(er));
    }

}
