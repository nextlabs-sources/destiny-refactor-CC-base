/*
 * Created on Sep 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.math.BigInteger;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import com.bluejungle.destiny.services.management.UserRoleServiceIF;
import com.bluejungle.destiny.services.management.UserRoleServiceLocator;
import com.bluejungle.destiny.services.management.types.DuplicateLoginNameException;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.management.types.UserDTOList;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.Role;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/tests/UserCreationTest.java#2 $:
 */

public class UserCreationTest extends TestCase {

    private UserRoleServiceIF dmsUserRoleService;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        UserRoleServiceLocator dmsUserRoleSvcLocator = new UserRoleServiceLocator();
        dmsUserRoleSvcLocator.setUserRoleServiceIFPortEndpointAddress("http://localhost:8081/dms/services/UserRoleServiceIFPort");
        this.dmsUserRoleService = dmsUserRoleSvcLocator.getUserRoleServiceIFPort();
        assertNotNull("DMS UserRole Service not created properly", this.dmsUserRoleService);

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for UserCrationTest.
     * 
     * @param arg0
     */
    public UserCreationTest(String arg0) {
        super(arg0);
    }

    public void testCreateUser() throws RemoteException {
        SubjectDTO fido = null;
        try {
            UserDTO newUser = new UserDTO(FIRST_NAME, LAST_NAME, PASSWORD, true, null);
            newUser.setUid(LOGIN_NAME);
            newUser.setName(DISPLAY_NAME);
            newUser.setType(SubjectType.USER.getName());
            newUser.setId(new BigInteger(IHasId.UNKNOWN_ID.toString()));
            
            DMSUserData userData = new DMSUserData(ROLES, LOGIN_NAME, DEFAULT_ROLE);
            dmsUserRoleService.createUser(newUser, userData);

            UserDTOList allUsers = dmsUserRoleService.getAllUsers();
            assertNotNull("allUsers should not be null", allUsers);

            UserDTO[] users = allUsers.getUsers();
            assertNotNull("users should not be null", users);
            assertTrue("there should be at least 1 user", users.length > 0);

            for (int i = 0; i < users.length; i++) {
                SubjectDTO subjectDTO = users[i];
                if (LOGIN_NAME.equals(subjectDTO.getUid())) {
                    fido = subjectDTO;
                    break;
                }
            }
            assertNotNull("newly-created user should be among all users", fido);
            assertEquals("unique name should be correct", LOGIN_NAME, fido.getUid());

            boolean duplicate = false;
            try {
                dmsUserRoleService.createUser(newUser, userData);
            } catch (DuplicateLoginNameException dup) {
                duplicate = true;
            }
            assertTrue("duplicate login name should not be allowed", duplicate);

        } finally {
            dmsUserRoleService.deleteFromUsers(fido);
        }

    }

    private static final String LOGIN_NAME = "Fido.The.Dog";
    private static final String FIRST_NAME = "Fido";
    private static final String LAST_NAME = "Dogstoyevskiy";
    private static final String DISPLAY_NAME = "Fido Arf Dogstoyevskiy";
    private static final String PASSWORD = "dogHouse1";
    private static final Role[] ROLES = new Role[] { Role.Policy_Administrator, Role.Policy_Analyst, Role.Business_Analyst };
    private static final Role DEFAULT_ROLE = Role.Policy_Administrator;
}
