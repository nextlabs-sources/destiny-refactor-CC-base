/*
 * Created on Jan 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/TestResourceSpecSizeCond.java#1 $:
 */

public class TestResourceSpecSizeCond extends TestCase {

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
     * Constructor for TestResourceSpecSizeCond.
     * @param arg0
     */
    public TestResourceSpecSizeCond(String arg0) {
        super(arg0);
    }

    public final void testIsResourceIn() throws IOException {
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        cm.registerComponent(AgentResourceManager.COMP_INFO, true);
        AgentResourceManager manager = (AgentResourceManager) cm.getComponent(AgentResourceManager.COMP_INFO);
        Set<ResourceAttribute> sizeAttributeSet = new HashSet<ResourceAttribute>();
        sizeAttributeSet.add(ResourceAttribute.SIZE);
        
        IPredicate small = ResourceAttribute.SIZE.buildRelation( RelationOp.LESS_THAN, "10240");
        File f = createFile(100);
        IResource res = manager.getResource("file:///" + f.getName(), null, AgentTypeEnumType.DESKTOP).getResource();
        EvaluationRequest request = new EvaluationRequest(res);
        assertTrue(small.match(request));
        f.delete();
        
        f = createFile(11*1024);
        res = manager.getResource("file:///" + f.getName(), null, AgentTypeEnumType.DESKTOP).getResource();
        request = new EvaluationRequest(res);
        assertFalse(small.match(request));
        f.delete();
    }

    /**
     * @param i
     * @return
     * @throws IOException
     */
    private File createFile(long size) throws IOException {
        String name = String.valueOf(size);
        File file = new File(name);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        while (size-- >0 ) {
            writer.write("b");
        }
        writer.close();
        return file;
        
    }

}
