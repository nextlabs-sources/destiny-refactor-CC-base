/*
 * Created on Jan 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.resource;

import junit.framework.TestCase;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/TestResourceSpecDateCond.java#1 $:
 */

public class TestResourceSpecDateCond extends TestCase {

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
     * Constructor for TestResourceSpecDateCond.
     * @param arg0
     */
    public TestResourceSpecDateCond(String arg0) {
        super(arg0);
    }

    public final void testIsResourceIn() {

        // TODO: add date support
        /**
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(10);
        ResourceSpecDateCond cond = new ResourceSpecDateCond(ResourceSpecDateCond.DATE_MODIFIED, RelationOp.GREATER_THAN, cal);
        File f = new File("foo");
        f.setLastModified(20);
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Resource res = new Resource(f);
        EvaluationRequest request = new EvaluationRequest(res);
        assertTrue(cond.match(request));
        f.setLastModified(5);
        assertFalse(cond.match(request));
        f.delete();
        */
    }

}
