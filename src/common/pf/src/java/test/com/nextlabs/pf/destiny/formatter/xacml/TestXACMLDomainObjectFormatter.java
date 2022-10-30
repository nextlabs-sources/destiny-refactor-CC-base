package com.nextlabs.pf.destiny.formatter.xacml;

/*
 * Created on Jan 27, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/nextlabs/pf/destiny/formatter/xacml/TestXACMLDomainObjectFormatter.java#1 $:
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.framework.test.InputOutputFileBasedTest;
import com.nextlabs.pf.destiny.formatter.XACMLDomainObjectFormatter;

public class TestXACMLDomainObjectFormatter extends TestCase implements InputOutputFileBasedTest.Tester {
    private XACMLDomainObjectFormatter xf;
    private List<IHasId> objects = new ArrayList<IHasId>();

    public static Test suite() {
        return InputOutputFileBasedTest.buildSuite( new TestXACMLDomainObjectFormatter() );
    }

    public void test(InputStream in, OutputStream out) throws Exception {
        DomainObjectBuilder dob = new DomainObjectBuilder(in);
        xf = new XACMLDomainObjectFormatter();
        TestVisitor v = new TestVisitor();
        dob.processInternalPQL(v);

        xf.formatDef(objects);

        objects.clear();

        PrintStream ps = new PrintStream(out);
        try {
            ps.print(xf.getXACML());
        } finally {
            ps.close();
        }
    }

    private class TestVisitor extends DefaultPQLVisitor {
        /**
         * @see IPQLVisitor#visitPolicy(DomainObjectDescriptor, IDPolicy)
         */
        public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
            assertNotNull("unable to parse policy", policy);
            objects.add(policy);
        }

        /**
         * @see IPQLVisitor#visitResource(DomainObjectDescriptor, IDResourceSpec)
         */
        public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
            assertNotNull("unable to parse component spec", pred);
            objects.add(new SpecBase(null,
                                     SpecType.ILLEGAL, // we don't use this
                                     descr.getId(),
                                     descr.getName(),
                                     descr.getDescription(),
                                     descr.getStatus(),
                                     pred,
                                     descr.isHidden()));
        }
    }
}
