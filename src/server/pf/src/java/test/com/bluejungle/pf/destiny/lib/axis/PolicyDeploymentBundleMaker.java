package com.bluejungle.pf.destiny.lib.axis;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.policy.TestUser;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;

/**
 * Builds a deployment bundle.
 */
public class PolicyDeploymentBundleMaker extends PFTestWithDataSource {

    private IPolicyDeployment deploymentImpl;

    public static TestSuite suite() {
        return makeSingleInitializationSuite( PolicyDeploymentBundleMaker.class );
    }

    public void testCleanUp() throws HibernateException {
        IHibernateRepository hds = (IHibernateRepository) cm.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        Session session = hds.getSession();
        Transaction tx = session.beginTransaction();
        session.delete("from ResolutionMapping");
        tx.commit();
        session.close();
    }

    public void testToDeploymentBundle() {
        List<SystemUser> systemUsers = new ArrayList<SystemUser>();
        for (TestUser element : TestUser.getAllUsers()) {
            systemUsers.add(new SystemUser("bogus", element.getSID()));
        }

        String host = "3MILE";

        DeploymentRequest request = new DeploymentRequest(systemUsers.toArray(new SystemUser[systemUsers.size()]), null, null, host, AgentTypeEnum.DESKTOP, null);
        IDeploymentBundle db = deploymentImpl.getDeploymentBundle(request, new Long(1000000559), "test.bluejungle.com", null);
        assertNotNull(db);

        // Test FSA deployment
        String[] subjectTypes = { UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName() }; 
        request = new DeploymentRequest(null, subjectTypes, null, host, AgentTypeEnum.FILE_SERVER, null);

        IDeploymentBundle dbTwo = deploymentImpl.getDeploymentBundle(request, new Long(2000000559), "test.bluejungle.com", null);
        assertNotNull(dbTwo);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // Initialize dictionary
        cm.getComponent(Dictionary.COMP_INFO); 

        deploymentImpl = cm.getComponent(PolicyDeploymentImpl.COMP_INFO);
    }
}
