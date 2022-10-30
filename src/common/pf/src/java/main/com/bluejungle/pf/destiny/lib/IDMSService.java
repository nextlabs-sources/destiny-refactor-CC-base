package com.bluejungle.pf.destiny.lib;

import java.util.Collection;

import com.bluejungle.destiny.services.policy.types.DMSRoleData;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;

//All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc, Redwood City CA,
//Ownership remains with Blue Jungle Inc, All rights reserved worldwide.

/**
 * TODO Write file summary here.
 * 
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/IDMSService.java#1 $
 */

/**
 * @author pkeni
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IDMSService {
    
    Collection getAllUsers () throws PolicyServiceException;
    Collection<SubjectDTO> getAllRoles () throws PolicyServiceException;

    public void addToUsers (SubjectDTO subjDTO) throws PolicyServiceException;
    public void deleteFromUsers (SubjectDTO subjDTO) throws PolicyServiceException;

    DMSUserData getUserData (SubjectDTO user) throws PolicyServiceException;
    void        setUserData (SubjectDTO user, DMSUserData data) throws PolicyServiceException;

    DMSRoleData getRoleDataById (Long id) throws PolicyServiceException;
    DMSRoleData getRoleData     (SubjectDTO role) throws PolicyServiceException;
    Collection<DMSRoleData>  getAllRoleData  () throws PolicyServiceException;
    void        setRoleDataById (Long id, DMSRoleData data) throws PolicyServiceException;
    void        setRoleData     (SubjectDTO role, DMSRoleData data) throws PolicyServiceException;
}
