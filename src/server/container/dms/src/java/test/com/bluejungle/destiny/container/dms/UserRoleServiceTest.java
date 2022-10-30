/*
 * Created on Nov 15, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

import com.bluejungle.destiny.container.dms.components.BaseDMSComponentTest;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.services.management.UserRoleServiceIF;
import com.bluejungle.destiny.services.management.UserRoleServiceLocator;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.management.types.UserDTOList;
import com.bluejungle.destiny.services.policy.types.Component;
import com.bluejungle.destiny.services.policy.types.DMSRoleData;
import com.bluejungle.destiny.services.policy.types.DMSRoleDataList;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.Role;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.destiny.services.policy.types.SubjectDTOList;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * This is the UserRole Service test for DMS. It uses the UserRoleService on DMS for verification.
 * 
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/UserRoleServiceTest.java#1 $
 */
public class UserRoleServiceTest extends BaseDMSComponentTest {


    /*
     * Test variables
     */
    private UserRoleServiceIF dmsUserRoleService;

    private IComponentManager manager;
    private IDSpecManager specManager;
    private HashMap subjectMap;
    private HashMap roleMap;
    private HashMap subjectNameMap;
    private LifecycleManager lifecycleManager;

    private IDictionary dictionary;

    /**
     * Main method
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(UserRoleServiceTest.class);
    }

    /**
     * Retrieve a JUnit Test Suite for all of the methods in this test class
     * 
     * @return
     */
    public static Test getTestSuite() {
        TestSuite suiteToReturn = new TestSuite("UserRole Service Test");
        suiteToReturn.addTest(new UserRoleServiceTest("testUserRoleService"));

        return suiteToReturn;
    }

    /**
     * Constructor for PrincipalServiceTest.
     * 
     * @param testName the name of the test
     */
    public UserRoleServiceTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the test.
     * 
     * @throws ServiceException if setup fails
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the DMS-UserRole-Service:
        UserRoleServiceLocator dmsUserRoleSvcLocator = new UserRoleServiceLocator();
        dmsUserRoleSvcLocator.setUserRoleServiceIFPortEndpointAddress("http://localhost:8081/dms/services/UserRoleServiceIFPort");
        this.dmsUserRoleService = dmsUserRoleSvcLocator.getUserRoleServiceIFPort();
        assertNotNull("DMS UserRole Service not created properly", this.dmsUserRoleService);

        String buildRoot = System.getProperty("src.root.dir");
        this.manager = ComponentManagerFactory.getComponentManager();

        Configuration hConfig = null;
        try {
            String basePath = buildRoot + File.separator + "server" + File.separator + "pf" + File.separator + "etc" + File.separator + "policy.repository.properties";

            FileInputStream propStream = new FileInputStream(basePath);
            Properties props = new Properties();
            props.load(propStream);
            props.putAll(System.getProperties());
            hConfig = new Configuration();
        } catch (IOException e) {
            TestCase.fail(e.getMessage());
        }
        ;

        this.specManager = (IDSpecManager) manager.getComponent(IDSpecManager.COMP_INFO);
        this.dictionary = (IDictionary) manager.getComponent( Dictionary.COMP_INFO );

        // Setup the mock shared context
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) manager.getComponent(locatorInfo);


        this.lifecycleManager = (LifecycleManager) manager.getComponent(LifecycleManager.COMP_INFO);

        /*
         * Set up roles, users and applications: Policy Admins - pkeni, sasha, sergey Policy Analysts - pkeni, scott, fuad
         * 
         * Policy Author - pkeni, sgoldstein, chander Management Console - chander, rlin Inquiry Center - ihannen, pkeni
         */

        String policyAdmins = "ID 1234 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"Policy Administrator\"  DO ALLOW " + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "    DEFAULT_ACCESS_CONTROL " + "        PBAC FOR * ON READ BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW " + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "    ALLOWED_ENTITIES " + "        HOST_ENTITY "
                + "HIDDEN USER \"Policy Administrator\" = PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sasha@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sergey@bluejungle.com\"";

        String policyAnalysts = "ID 1235 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"Policy Analyst\" DO ALLOW " + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Analyst\" DO ALLOW "
                + "    DEFAULT_ACCESS_CONTROL " + "        PBAC FOR * ON READ BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW " + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "    ALLOWED_ENTITIES " + "        POLICY_ENTITY, RESOURCE_ENTITY, HOST_ENTITY "
                + "HIDDEN USER \"Policy Analyst\"  = PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sgoldstein@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"fuad@bluejungle.com\"";

        String businessAnalysts = "ID 2235 STATUS APPROVED CREATOR \"ADMIN\" "
                + "ACCESS_POLICY "
                + "    ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"Policy Administrator\"  DO ALLOW "
                + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "    DEFAULT_ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" DO ALLOW "
                + "    ALLOWED_ENTITIES "
                + "        HOST_ENTITY "
                + "HIDDEN USER \"Business Analyst\"  = PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sgoldstein@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"fuad@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"chander@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sasha@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"ihanen@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"rlin@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"sergey@bluejungle.com\"";

        String admins = "ID 3235 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL " + "    DEFAULT_ACCESS_CONTROL " + "    ALLOWED_ENTITIES "
                + "HIDDEN USER \"ADMIN\" = PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"ihanen@bluejungle.com\" OR PRINCIPAL.USER.NAME = \"jimmy.carter@test.bluejungle.com\"";

        String policyAuthor = "ID 1236 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"Policy Analyst\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW "
                + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Analyst\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW " + "    DEFAULT_ACCESS_CONTROL " + "    ALLOWED_ENTITIES "
                + "HIDDEN APPLICATION \"Policy Author\" = PRINCIPAL.APP.NAME = \"Policy Author\"";

        String managementConsole = "ID 1237 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL "
                + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"fuad@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"Policy Administrator\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW "
                + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.GROUP = \"Policy Administrator\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW " + "    DEFAULT_ACCESS_CONTROL " + "    ALLOWED_ENTITIES "
                + "HIDDEN APPLICATION \"Management Console\" = PRINCIPAL.APP.NAME = \"Management Console\"";

        String inquiryCenter = "ID 1238 STATUS APPROVED CREATOR \"ADMIN\" " + "ACCESS_POLICY " + "    ACCESS_CONTROL " + "        PBAC FOR * ON READ BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW "
                + "        PBAC FOR * ON WRITE BY PRINCIPAL.USER.NAME = \"pkeni@bluejungle.com\" OR PRINCIPAL.USER.GROUP = \"ADMIN\" DO ALLOW " + "    DEFAULT_ACCESS_CONTROL " + "    ALLOWED_ENTITIES "
                + "HIDDEN APPLICATION \"Inquiry Center\" = PRINCIPAL.APP.NAME = \"Inquiry Center\"";

        Collection specs = new HashSet();
        specs.add(policyAdmins);
        specs.add(policyAnalysts);
        specs.add(businessAnalysts);
        specs.add(admins);
        specs.add(policyAuthor);
        specs.add(managementConsole);
        specs.add(inquiryCenter);

        // Clear everything
        Session session = hConfig.buildSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.delete("from DeploymentEntity");
        session.delete("from DevelopmentEntity");
        session.delete("from DeploymentRecord");

        tx.commit();
        session.close();

        /* Setup Roles and Applications */
        for (Iterator iter = specs.iterator(); iter.hasNext();) {
            lifecycleManager.saveEntity(new DevelopmentEntity((String) iter.next()), LifecycleManager.MUST_EXIST, null);
        }

        DomainObjectBuilder dob;
        roleMap = new HashMap();
        for (Iterator iter = specs.iterator(); iter.hasNext();) {
            String pql = (String) iter.next();

            dob = new DomainObjectBuilder(pql);
            dob.processInternalPQL(new DefaultPQLVisitor() {

                public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                    String name = descr.getName();
                    int pos = name.indexOf('/');
                    SpecType type = SpecType.ILLEGAL;
                    if (pos != -1) {
                        try {
                            type = SpecType.forName(name.substring(0,pos).toLowerCase());
                        } catch (Exception ignored) {
                            // FIXME (sergey) This should not happen
                        }
                    }
                    IDSpec newSpec = new SpecBase(specManager, type, descr.getId(), descr.getName(), null, DevelopmentStatus.EMPTY, pred, false);
                    roleMap.put(descr.getName(), newSpec);
                    specManager.saveSpec(newSpec);
                }
            });
        }

        subjectMap = new HashMap();
        subjectNameMap = new HashMap();

        subjectNameMap.put("Alexander Vladimirov", "S-1-5-21-668023798-3031861066-1043980994-1119");
        subjectNameMap.put("Chander Sarna", "S-1-5-21-668023798-3031861066-1043980994-1133");
        subjectNameMap.put("Fuad Rashid", "S-1-5-21-668023798-3031861066-1043980994-2624");
        subjectNameMap.put("Iannis Hanen", "S-1-5-21-668023798-3031861066-1043980994-1140");
        subjectNameMap.put("Prabhat Keni", "S-1-5-21-668023798-3031861066-1043980994-2631");
        subjectNameMap.put("Robert Lin", "S-1-5-21-668023798-3031861066-1043980994-2628");
        subjectNameMap.put("Sergey Kalinichenko", "S-1-5-21-668023798-3031861066-1043980994-1157");


        IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        IElementField sysRef = userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());

        for (Iterator iter = subjectNameMap.keySet().iterator(); iter.hasNext();) {
            String userName = (String) iter.next();
            String systemId = (String) subjectNameMap.get(userName);

            IDictionaryIterator<IMElement> result = this.dictionary.query( new Relation(RelationOp.EQUALS, sysRef, Constant.build(systemId) ), new Date(), null, null);
            IElement user = result.next();   
            result.close();
            IDSubject sub = new Subject( systemId, user.getUniqueName(), user.getDisplayName(), 
                                         user.getInternalKey(), SubjectType.USER );
            subjectMap.put(userName, sub);
        }
    }

    /**
     * Tests getAllUsers
     * 
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     * @throws UnauthorizedCallerFault
     */
    public void testUserRoleService() throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        UserDTOList allusers = dmsUserRoleService.getAllUsers();
        // Validate users.
        SubjectDTOList allroles = dmsUserRoleService.getAllRoles();
        // Validate roles.
        HashMap roleIdMap = new HashMap();
        SubjectDTO[] roleArray = allroles.getSubjects();

        for (int i = 0; i < roleArray.length; i++) {
            roleIdMap.put(roleArray[i].getName(), roleArray[i].getId());
        }

        SubjectDTO dto = DTOUtils.subjectToDTO((IDSubject) subjectMap.get("Prabhat Keni"));
        DMSUserData data = dmsUserRoleService.getUserData(dto);
        assertEquals("Prabhat Keni must have 3 roles", 3, data.getRoles().length);
        assertEquals("Prabhat Keni's default role must be user", "User", data.getDefaultRole().getValue());

        IDSubject subject = (IDSubject) subjectMap.get("Prabhat Keni");
        UserDTO userDTO = new UserDTO();
        userDTO.setUniqueName(subject.getUniqueName());
        userDTO.setUid(subject.getUid());
        userDTO.setId(new BigInteger(subject.getId().toString()));
        userDTO.setName(subject.getName());
        
        data = new DMSUserData();
        Role[] roles = new Role[2];

        data.setName("Prabaht Keni");
        data.setDefaultRole(Role.Policy_Administrator);

        roles[0] = Role.Policy_Administrator;
        roles[1] = Role.Business_Analyst;
        data.setRoles(roles);

        dmsUserRoleService.setUserData(userDTO, data);

        data = dmsUserRoleService.getUserData(dto);
        assertEquals("Prabhat Keni must have 2 roles", 2, data.getRoles().length);
        // PKENI: FIX assertEquals ("Prabhat Keni's default role must be Policy
        // Administrator", "Policy Administrator", data.getDefaultRole
        // ().getValue ());
        assertEquals("Prabhat Keni's default role must be User", "User", data.getDefaultRole().getValue());

        DMSRoleData roleData = null;

        dto = DTOUtils.subjectSpecToDTO((IDSpec) roleMap.get("Policy Administrator"));
        roleData = dmsUserRoleService.getRoleData(dto);
        roleData = dmsUserRoleService.getRoleDataById((BigInteger) roleIdMap.get("Policy Administrator"));
        dto = DTOUtils.subjectSpecToDTO((IDSpec) roleMap.get("Policy Analyst"));
        roleData = dmsUserRoleService.getRoleData(dto);
        roleData = dmsUserRoleService.getRoleDataById((BigInteger) roleIdMap.get("Policy Analyst"));

        String[] apps = new String[2];
        Component[] components = new Component[4];

        dto = DTOUtils.subjectSpecToDTO((IDSpec) roleMap.get("Policy Analyst"));
        roleData = new DMSRoleData();

        roleData.setName("Policy Analyst");
        apps[0] = "Policy Author";
        apps[1] = "Management Console";
        roleData.setAllowApps(apps);

        components[0] = Component.Policy;
        components[1] = Component.Desktop;
        components[2] = Component.Resource;
        components[3] = Component.User;
        roleData.setComponents(components);

        dmsUserRoleService.setRoleData(dto, roleData);
        roleData = dmsUserRoleService.getRoleData(dto);
        dmsUserRoleService.setRoleDataById((BigInteger) roleIdMap.get("Policy Analyst"), roleData);

        DMSRoleDataList roleDataList = dmsUserRoleService.getAllRoleData();
    }

}
