package com.bluejungle.pf.destiny.policymap;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author amorgan, sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/policymap/TestMapBuilder.java#1 $
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.usertypes.StringArrayAsString;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.tools.MockSharedContextLocator;
import com.bluejungle.testing.filecomp.InputOutputFileBasedTest;
import com.bluejungle.version.IVersion;

/**
 * This is a unit test for MapBuilder.
 */
public class TestMapBuilder extends PFTestWithDataSource implements InputOutputFileBasedTest.Tester {

    private static int testCounter = 0;

    /** Hibernate Session Factory. */
    private IHibernateRepository hds;

    private PolicyQueryImpl deployer;

    private IDictionary dictionary;

    {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        manager.registerComponent(locatorInfo, true);
        hds = (IHibernateRepository) manager.getComponent( DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() );
        deployer = (PolicyQueryImpl) manager.getComponent(PolicyQueryImpl.COMP_INFO);
        dictionary = (IDictionary)manager.getComponent(Dictionary.COMP_INFO);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Uses the FileBasedTest to build the test suite for this component.
     * @return A dynamically generated test suite for this component. 
     */
    public static TestSuite suite() {
        return InputOutputFileBasedTest.buildSuite( new TestMapBuilder() );        
    }

    /**
     * This is the main method for testing the map builder.
     * It opens the input file, gives its content to mapbuilder, and then
     * dumps the output bundle to the output stream.
     */
    public void test(InputStream input, OutputStream output) throws Exception {
        String[] domains = new String[] { "bluejungle.com", "test.bluejungle.com", "test"+(testCounter++)+".domain.org" };
        STRLog strLog = new STRLog(domains, new Date());
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        AgentTypeEnumType agentType = null;
        IVersion ver = null;
        if (reader.ready()) {
            String info = reader.readLine();
            if (info == null) {
                fail("Cannot read the agent type.");
            }
            String[] args = info.split(" ");
            if (args.length > 1) {
                final String[] verElements = args[1].split("[.]");
                ver = new IVersion() {
                    public int getBuild() {
                        return getAt(4);
                    }
                    public int getMaintenance() {
                        return getAt(2);
                    }
                    public int getMajor() {
                        return getAt(0);
                    }
                    public int getMinor() {
                        return getAt(1);
                    }
                    public int getPatch() {
                        return getAt(3);
                    }
                    private int getAt(int n) {
                        if (verElements.length <= n) {
                            return 0;
                        }
                        try {
                            return Integer.parseInt(verElements[n]);
                        } catch (NumberFormatException nfe) {
                            return 0;
                        }
                    }
                    public void setBuild(int build) {
                        throw new UnsupportedOperationException();
                    }
                    public void setMaintenance(int maintenance) {
                        throw new UnsupportedOperationException();
                    }
                    public void setMajor(int major) {
                        throw new UnsupportedOperationException();
                    }
                    public void setMinor(int minor) {
                        throw new UnsupportedOperationException();
                    }
                    public void setPatch(int patch) {
                        throw new UnsupportedOperationException();
                    }
                    public int compareTo(IVersion o) {
                        if (o == null) {
                            throw new NullPointerException();
                        }
                        if (getMajor() != o.getMajor()) {
                            return getMajor() - o.getMajor();
                        }
                        if (getMinor() != o.getMinor()) {
                            return getMinor() - o.getMinor();
                        }
                        if (getMaintenance() != o.getMaintenance()) {
                            return getMaintenance() - o.getMaintenance();
                        }
                        if (getPatch() != o.getPatch()) {
                            return getPatch() - o.getPatch();
                        }
                        if (getBuild() != o.getBuild()) {
                            return getBuild() - o.getBuild();
                        }
                        return 0;
                    }
                };
            }
            agentType = AgentTypeEnumType.getAgentType(args[0]);
            if (agentType == null) {
                fail("The agent type is undefined: "+args[0]);
            }
            
        } else {
            fail("The input file is empty.");
        }
        Collection<IHasPQL> pqls = new ArrayList<IHasPQL>();

        try {
            while (reader.ready()) {
                final String pql = PFTestUtils.replaceDictionaryVariables(
                    reader.readLine()
                ,   dictionary
                );
                pqls.add( new IHasPQL() {
					public String getPql() {
						return pql;
					}
                });
            }
        } catch (DictionaryException de) {
            fail(de.getMessage());
        } catch (IllegalArgumentException iae) {
            fail(iae.getMessage());
        } finally {
            reader.close();
        }

        MapBuilder builder = new MapBuilder(pqls, strLog);
        Method doBuild;

        try {
            // Try the real name first. We'll try the obfuscated name if that doesn't work
            doBuild = builder.getClass().getDeclaredMethod("doBuild", Session.class);
        } catch (NoSuchMethodException nsme) {
            doBuild = builder.getClass().getDeclaredMethod("a", Session.class);
        }
        
        doBuild.setAccessible(true);
        Session hs = hds.getCountedSession();
        Transaction tx = hs.beginTransaction();
        try {
            hs.delete("from STRLog s where s.domains=?", domains, StringArrayAsString.TYPE);
            hs.save(strLog);
            doBuild.invoke(builder, hs);
            hs.save(strLog);
            tx.commit();
        } catch (HibernateException ex) {
            tx.rollback();
        } finally {
            hs.close();
        }
        IDeploymentBundle bundle = deployer.buildBundle(
            domains
        ,   agentType
        ,   Calendar.getInstance()
        ,   Arrays.asList(new Long[] {
                PFTestUtils.getInternalKey("savaii.bluejungle.com", dictionary)
            ,   PFTestUtils.getInternalKey("keng@bluejungle.com", dictionary)
            })
        ,   0L
        ,   1L
        ,   ver
        );
        PrintStream ps = new PrintStream(output);
        ps.println(PFTestUtils.toString(bundle, false).replaceAll("ID [0-9]+", "ID #"));
        ps.flush();
    }

}
