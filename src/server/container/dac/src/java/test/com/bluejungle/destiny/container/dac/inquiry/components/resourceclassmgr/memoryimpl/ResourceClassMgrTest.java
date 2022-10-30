/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.memoryimpl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.ResourceClassMgrQueryFieldType;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryFieldName;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQuerySpec;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryTerm;
import com.bluejungle.destiny.types.resources.v1.ResourceClassQueryTermList;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This is the test class for the resource class manager component.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/memoryimpl/ResourceClassMgrTest.java#1 $
 */

public class ResourceClassMgrTest extends BaseDACComponentTestCase {

    /**
     * Returns the policy framework data source
     * 
     * @return the policy framework data source
     */
    protected IHibernateRepository getPFDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns a valid policy manager instance
     * 
     * @return a valid policy manager instance
     */
    protected ResourceClassMgrImpl getResourceClassMgr() {
        HashMapConfiguration config = new HashMapConfiguration();
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        config.setProperty(IResourceClassMgr.DATASOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo info = new ComponentInfo("ResourceClassMgr", ResourceClassMgrImpl.class.getName(), IResourceClassMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        return (ResourceClassMgrImpl) compMgr.getComponent(info);
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public ResourceClassMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies the basic features of the policy mgr class
     */
    public void testResourceClassMgrClassBasics() {
        ResourceClassMgrImpl resourceClassMgr = getResourceClassMgr();
        assertTrue("Resource class Mgr should implement the right interface", resourceClassMgr instanceof ILogEnabled);
        assertTrue("Resource class Mgr should implement the right interface", resourceClassMgr instanceof IConfigurable);
        assertTrue("Resource class Mgr should implement the right interface", resourceClassMgr instanceof IManagerEnabled);
        assertTrue("Resource class Mgr should implement the right interface", resourceClassMgr instanceof IResourceClassMgr);
        assertTrue("Resource class Mgr should implement the right interface", resourceClassMgr instanceof IInitializable);
        assertNotNull("Resource class Mgr should have a log", resourceClassMgr.getLog());
        assertNotNull("Resource class Mgr should have a comp manager", resourceClassMgr.getManager());
    }

    /**
     * This test verifies that the PF API is queried properly and that the
     * proper resource classes are returned.
     * 
     * @throws DataSourceException
     */
    public void testResourceClassMgrPFIntegration() throws DataSourceException {
        IHibernateRepository ds = getPFDataSource();
        Session s = null;
        Transaction t = null;
        try {
            s = ds.getSession();
            t = s.beginTransaction();
            s.delete("from DeploymentEntity");
            s.delete("from DevelopmentEntity");
            s.delete("from DeploymentRecord");
            t.commit();
        } catch (HibernateException e) {
            fail("Error when deleting PF data");
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 10);
        Date in10 = new Date(now.getTimeInMillis());
        try {
            LifecycleManager lm = (LifecycleManager) ComponentManagerFactory.getComponentManager().getComponent(LifecycleManager.COMP_INFO);
            DevelopmentEntity de1 = lm.getEntityForName(EntityType.COMPONENT, "resource1", LifecycleManager.MAKE_EMPTY);
            de1.setPql("ID "+de1.getId()+" STATUS APPROVED component resource1 = NAME != \"foo.doc\" and NAME != \"foo2.doc\"");
            DevelopmentEntity de2 = lm.getEntityForName(EntityType.COMPONENT, "AnotherOne", LifecycleManager.MAKE_EMPTY);
            de2.setPql("ID "+de2.getId()+" STATUS APPROVED component AnotherOne = TYPE != \"PERL\" and TYPE == \"JAVA\" OR  SIZE > 100 and SIZE < 1000");
            DevelopmentEntity de3 = lm.getEntityForName(EntityType.COMPONENT, "NotDeployed", LifecycleManager.MAKE_EMPTY);
            de3.setPql("ID "+de3.getId()+" STATUS APPROVED resource NotDeployed = TYPE != \"PERL\" and TYPE == \"JAVA\" OR  SIZE > 100 and SIZE < 1000");
            lm.deployEntities(Arrays.asList(new DevelopmentEntity[] { de1, de2 }), in10, DeploymentType.PRODUCTION, false, null);
        } catch (EntityManagementException e) {
            fail("Error when deploying resource" + e.getMessage() );
        } catch (PQLException e) {
            fail("Error when deploying resource: " + e.getMessage() );
        }
        ResourceClassMgrImpl rcm = getResourceClassMgr();
        Set results = rcm.getResourceClasses(null, in10);
        assertEquals("Passing a NULL argument should return all the deployed resource classes", 2, results.size());
        ResourceClassQuerySpec wsQS = new ResourceClassQuerySpec();
        ResourceClassQueryTermList tl = new ResourceClassQueryTermList();
        ResourceClassQueryTerm term = new ResourceClassQueryTerm();
        term.setExpression("A*");
        term.setFieldName(ResourceClassQueryFieldName.Name);
        tl.setTerms(new ResourceClassQueryTerm[] { term });
        wsQS.setSearchSpec(tl);
        ResourceClassQuerySpecImpl querySpec = new ResourceClassQuerySpecImpl(wsQS);
        results = rcm.getResourceClasses(querySpec, in10);
        assertEquals("The right resource class should be returned from PF", 1, results.size());
        assertTrue("The right resource class should be returned from PF", results.contains("AnotherOne"));

        //Try lower case search
        wsQS = new ResourceClassQuerySpec();
        tl = new ResourceClassQueryTermList();
        term = new ResourceClassQueryTerm();
        term.setExpression("r*");
        term.setFieldName(ResourceClassQueryFieldName.Name);
        tl.setTerms(new ResourceClassQueryTerm[] { term });
        wsQS.setSearchSpec(tl);
        querySpec = new ResourceClassQuerySpecImpl(wsQS);
        results = rcm.getResourceClasses(querySpec, in10);
        assertEquals("The right resource class should be returned from PF", 1, results.size());
        assertTrue("The right resource class should be returned from PF", results.contains("resource1"));
    }

    /**
     * This class transforms a web service resource class query specification
     * into a resource class manager component query specification.
     * 
     * @author ihanen
     */
    protected class ResourceClassQuerySpecImpl implements IResourceClassMgrQuerySpec {

        private IResourceClassMgrQueryTerm[] queryTerms;
        private IResourceClassMgrSortTerm[] sortTerms;

        /**
         * Constructor
         * 
         * @param wsResourceClassQuerySpec
         *            Axis resource class query spec object
         */
        protected ResourceClassQuerySpecImpl(ResourceClassQuerySpec wsResourceClassQuerySpec) {
            //Place default return values
            this.queryTerms = new IResourceClassMgrQueryTerm[0];
            this.sortTerms = new IResourceClassMgrSortTerm[0];
            if (wsResourceClassQuerySpec != null) {
                ResourceClassQueryTermList resourceClassQueryList = wsResourceClassQuerySpec.getSearchSpec();
                if (resourceClassQueryList != null) {
                    ResourceClassQueryTerm[] terms = resourceClassQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IResourceClassMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new ResourceClassQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }
            }
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec#getSearchSpecTerms()
         */
        public IResourceClassMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQuerySpec#getSortSpecTerms()
         */
        public IResourceClassMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

    }

    /**
     * This is an implementation of the resource class query term to convert a
     * web service term into a resource class manager user query term.
     * 
     * @author ihanen
     */
    protected class ResourceClassQueryTermImpl implements IResourceClassMgrQueryTerm {

        private String expression;
        private ResourceClassMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service resource class query term
         */
        protected ResourceClassQueryTermImpl(ResourceClassQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgrQueryTerm#getFieldName()
         */
        public ResourceClassMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service resource class query field name into a
         * resource class manager query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(ResourceClassQueryFieldName wsFieldName) {
            if (ResourceClassQueryFieldName.Name.equals(wsFieldName)) {
                this.fieldName = ResourceClassMgrQueryFieldType.NAME;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }
}
