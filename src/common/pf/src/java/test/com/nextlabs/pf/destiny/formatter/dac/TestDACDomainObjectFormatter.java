/*
 * Created on Sep 25, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/nextlabs/pf/destiny/formatter/dac/TestDACDomainObjectFormatter.java#1 $:
 */

package com.nextlabs.pf.destiny.formatter.dac;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import com.bluejungle.framework.test.InputOutputFileBasedTest;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessRule;
import com.nextlabs.pf.destiny.formatter.DACDomainObjectFormatter;

public class TestDACDomainObjectFormatter extends TestCase implements InputOutputFileBasedTest.Tester {
    private DACDomainObjectFormatter dacDOF;
    private static Map<String, String> resourceAndSubjectMappings = new HashMap<String, String>();
    private static Map<String, String> actionMappings = new HashMap<String, String>();

    static {
        resourceAndSubjectMappings.put("user.isocountrycode", "@USER.ad://country:88cecb73b290f68e");
        resourceAndSubjectMappings.put("user.company", "@USER.ad://company:88cecb735d351145");
        
        resourceAndSubjectMappings.put("resource.fso.urn:bails:exportcontrol:businessauthorizationcategory:name",
                                       "@RESOURCE.urnbailsExportC_88cecd270d44d7d8");
        resourceAndSubjectMappings.put("resource.fso.urn:bails:nationalsecurity:businessauthorizationcategory:name",
                                       "@RESOURCE.urnbailsNationa_88cecd27855d62c1");
        resourceAndSubjectMappings.put("resource.fso.urn:bails:intellectualproperty:businessauthorizationcategory:name",
                                       "@RESOURCE.urnbailsIntelle_88cecd27df2c2f80");

        actionMappings.put("EDIT, OPEN, RUN", "0x1201bf");
        actionMappings.put("EDIT, OPEN", "0x12019f");
        actionMappings.put("EDIT", "0x100116");
        actionMappings.put("OPEN", "FR");
        actionMappings.put("RUN", "0x1200a9");
        actionMappings.put("DELETE, EDIT, OPEN, RUN", "0x1301bf");
        actionMappings.put("DELETE, EDIT, OPEN", "0x1301bf");
    }

    public static Test suite() {
        return InputOutputFileBasedTest.buildSuite(new TestDACDomainObjectFormatter());
    }

    private static final String CAR_IDENTITY = "CN=Central Access Rules,CN=Claims Configuration,CN=Services,CN=Configuration,DC=demo8,DC=nextlabs,DC=com";
    private static final String CAP_FOLDER = "Test Folder";
    private static final String CAP_IDENTITY = "CN=Central Access Policies,CN=Claims Configuration,CN=Services,CN=Configuration,DC=demo8,DC=nextlabs,DC=com";
    private static final String AD_SERVER = "ad.nextlabs.com";

    public void test(InputStream in, OutputStream out) throws Exception {
        final List<IDPolicy> policies = new ArrayList<IDPolicy>();

        DomainObjectBuilder dob = new DomainObjectBuilder(in);
        

        dob.processInternalPQL(new DefaultPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                assertNotNull("policy should not be null", policy);
                policies.add(policy);
            }
        });

        dacDOF = new DACDomainObjectFormatter(resourceAndSubjectMappings, actionMappings);

        for (IDPolicy policy : policies) {
            dacDOF.formatPolicy(policy);

            PrintStream ps = new PrintStream(out);
            try {
                DACCentralAccessRule car = dacDOF.getCAR();

                car.setIdentity(CAR_IDENTITY);
                car.setServer(AD_SERVER);

                /*
                ps.print(car.getSettableRule());
                ps.print("\n");
                ps.print(car.getADObjectCommand());
                ps.print("\n");
                */
            } finally {
                ps.close();
            }
        }
    }
}
