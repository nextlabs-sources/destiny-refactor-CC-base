/*
 * Created on May 5, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common;

import java.util.Collection;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollmentSession;


/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/common/ILDAPEnrollmentWrapper.java#1 $
 */

public interface ILDAPEnrollmentWrapper extends IEnrollmentWrapper {
    
    /*
     * Common attributes:
     */
    String getObjectGUIDAttributeName();
    boolean storeMissingAttributes();

    /*
     * User entry configuration:
     */
    boolean enrollUsers();
    String getUserIdentification();
    Map<IElementField, String> getSearchableAttributesForUsers();
    
    /*
     * Contact entry configuration:
     */
    boolean enrollContacts();
    String getContactIdentification();
    Map<IElementField, String> getSearchableAttributesForContacts();
    
    /*
     * Host entry configuration:
     */
    boolean enrollComputers();
    String getComputerIdentification();
    Map<IElementField, String> getSearchableAttributesForComputers();

    /*
     * Application entry configuration
     */
    boolean enrollApplications();
    String getApplicationIdentification();
    Map<IElementField, String> getSearchableAttributesForApplications();
    
    /*
     * Group entry configuration:
     */
    boolean enrollGroups();
    String getGroupIdentification();
    String getMembershipAttributeName();
    
    String getStructureIdentification();
    
    String getOtherIdentification();
    
    /**
     * 
     * @param elements
     * @return # of successful saved elements;
     * @throws DictionaryException
     */
    int saveElementIDs(Collection<? extends IElementBase> elements) throws DictionaryException;
    
    /**
     * 
     * @param session
     * @return # of successful deleted elements
     * @throws DictionaryException
     */
    int removeElementIDs(IEnrollmentSession session) throws DictionaryException ;
    
    boolean isFilterMailValue();
}

