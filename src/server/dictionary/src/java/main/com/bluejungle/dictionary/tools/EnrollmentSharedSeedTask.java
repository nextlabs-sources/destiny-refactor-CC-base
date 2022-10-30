/*
 * Created on Mar 26, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary.tools;

import static com.bluejungle.dictionary.ElementFieldType.*;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ClientInfoFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.SiteReservedFieldEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.patterns.EnumBase;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * CAUTION: If changing the create type methods, be careful side effects on the updateTask Classes
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/tools/EnrollmentSharedSeedTask.java#1 $
 */

class EnrollmentSharedSeedTask implements IInitializable{
    private IDictionary dictionary;

    public void init() {
        // Setup the mock shared context locator:
        ComponentInfo<IDestinySharedContextLocator> locatorInfo =
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME,
            MockSharedContextLocator.class,
            IDestinySharedContextLocator.class,
            LifestyleType.SINGLETON_TYPE);
        ComponentManagerFactory.getComponentManager().registerComponent(locatorInfo, true);
        ComponentManagerFactory.getComponentManager().getComponent(locatorInfo);
        dictionary = ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );
    }

    static void setField(IMElementType elementType, EnumBase enumBase, ElementFieldType type,
                         String label) {
        IElementField field = elementType.addField(enumBase.getName(), type);
        elementType.setFieldLabel(field, label);
    }

    public final IDictionary getDictionary() {
        return this.dictionary;
    }

    public void save(IMElementType... elementTypes) throws DictionaryException {
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            for (IMElementType elementType : elementTypes) {
                session.saveType(elementType);
            }
            session.commit();
        } finally {
            session.close();
        }
    }

    public boolean isTypeAlreadyExist(EnumBase enumBase) {
        boolean isTypeAlreadyExist;
        try {
            isTypeAlreadyExist = (dictionary.getType(enumBase.getName()) != null);
        } catch (DictionaryException e) {
            isTypeAlreadyExist = false;
        }
        return isTypeAlreadyExist;
    }

    public IMElementType createUserType() throws DictionaryException {
        IMElementType userType = dictionary.makeNewType(ElementTypeEnumType.USER.getName());

        setField(userType, UserReservedFieldEnumType.WINDOWS_SID, CS_STRING,
                 UserReservedFieldEnumType.WINDOWS_SID_LABEL);

        setField(userType, UserReservedFieldEnumType.PRINCIPAL_NAME, STRING,
                 UserReservedFieldEnumType.PRINCIPAL_NAME_LABEL);
        
        setField(userType, UserReservedFieldEnumType.DISPLAY_NAME, STRING,
                 UserReservedFieldEnumType.DISPLAY_NAME_LABEL);
        
        setField(userType, UserReservedFieldEnumType.FIRST_NAME, STRING,
                 UserReservedFieldEnumType.FIRST_NAME_LABEL);
        
        setField(userType, UserReservedFieldEnumType.LAST_NAME, STRING,
                 UserReservedFieldEnumType.LAST_NAME_LABEL);

        setField(userType, UserReservedFieldEnumType.MAIL, STRING_ARRAY,
                 UserReservedFieldEnumType.MAIL_LABEL);

        setField(userType, UserReservedFieldEnumType.UNIX_ID, STRING,
                 UserReservedFieldEnumType.UNIX_ID_LABEL);

        return userType;
    }

    public IMElementType createContactType() throws DictionaryException {
        // add seed data for Contact type
        IMElementType contactType = dictionary.makeNewType(ElementTypeEnumType.CONTACT.getName());
        setField(contactType, ContactReservedFieldEnumType.PRINCIPAL_NAME, STRING,
                 ContactReservedFieldEnumType.PRINCIPAL_NAME_LABEL);

        setField(contactType, ContactReservedFieldEnumType.DISPLAY_NAME, STRING,
                 ContactReservedFieldEnumType.DISPLAY_NAME_LABEL);

        setField(contactType, ContactReservedFieldEnumType.FIRST_NAME, STRING,
                 ContactReservedFieldEnumType.FIRST_NAME_LABEL);

        setField(contactType, ContactReservedFieldEnumType.LAST_NAME, STRING,
                 ContactReservedFieldEnumType.LAST_NAME_LABEL);

        setField(contactType, ContactReservedFieldEnumType.MAIL, STRING_ARRAY,
                 ContactReservedFieldEnumType.MAIL_LABEL);

        return contactType;
    }

    public IMElementType createHostType() throws DictionaryException {
        // add seed data for computer type
        IMElementType hostType = dictionary.makeNewType(ElementTypeEnumType.COMPUTER.getName());

        setField(hostType, ComputerReservedFieldEnumType.WINDOWS_SID, CS_STRING,
                 ComputerReservedFieldEnumType.WINDOWS_SID_LABEL);

        setField(hostType, ComputerReservedFieldEnumType.DNS_NAME, STRING,
                 ComputerReservedFieldEnumType.DNS_NAME_LABEL);

        setField(hostType, ComputerReservedFieldEnumType.UNIX_ID, STRING,
                 ComputerReservedFieldEnumType.UNIX_ID_LABEL);

        return hostType;
    }

    public IMElementType createApplicationType() throws DictionaryException {
        // add seed data for application type
        IMElementType appType = dictionary.makeNewType(ElementTypeEnumType.APPLICATION.getName());

        setField(appType, ApplicationReservedFieldEnumType.SYSTEM_REFERENCE, CS_STRING,
                 ApplicationReservedFieldEnumType.SYSTEM_REFERENCE_LABEL);

        setField(appType, ApplicationReservedFieldEnumType.UNIQUE_NAME, STRING,
                 ApplicationReservedFieldEnumType.UNIQUE_NAME_LABEL);
        
        setField(appType, ApplicationReservedFieldEnumType.DISPLAY_NAME, STRING,
                 ApplicationReservedFieldEnumType.DISPLAY_NAME_LABEL);

        setField(appType, ApplicationReservedFieldEnumType.APP_FINGER_PRINT, CS_STRING,
                 ApplicationReservedFieldEnumType.APP_FINGER_PRINT_LABEL);

        return appType;
    }

    public IMElementType createSiteType() throws DictionaryException {
        // add seed data for site type
        IMElementType siteType = dictionary.makeNewType(ElementTypeEnumType.SITE.getName());

        setField(siteType, SiteReservedFieldEnumType.NAME, CS_STRING,
                 SiteReservedFieldEnumType.NAME_LABEL);

        setField(siteType, SiteReservedFieldEnumType.IP_ADDRESS, CS_STRING,
                 SiteReservedFieldEnumType.IP_LABEL);

        return siteType;
    }

    public IMElementType createClientInfoType() throws DictionaryException {
        IMElementType clientInfoType = dictionary.makeNewType(ElementTypeEnumType.CLIENT_INFO
                                                              .getName());

        setField(clientInfoType, ClientInfoFieldEnumType.IDENTIFIER, CS_STRING,
                 ClientInfoFieldEnumType.IDENTIFIER_LABEL);

        setField(clientInfoType, ClientInfoFieldEnumType.SHORT_NAME, CS_STRING,
                 ClientInfoFieldEnumType.SHORT_NAME_LABEL);

        setField(clientInfoType, ClientInfoFieldEnumType.LONG_NAME, CS_STRING,
                 ClientInfoFieldEnumType.LONG_NAME_LABEL);

        setField(clientInfoType, ClientInfoFieldEnumType.EMAIL_TEMPLATES,
                 STRING_ARRAY, ClientInfoFieldEnumType.EMAIL_TEMPLATES_LABEL);

        setField(clientInfoType, ClientInfoFieldEnumType.USER_NAMES, NUM_ARRAY,
                 ClientInfoFieldEnumType.USER_NAMES_LABEL);

        return clientInfoType;
    }
}
