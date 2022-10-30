/*
 * Created on Apr 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.DomainGroupEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.LdifEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.SharePointEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text.JavaPropertiesFileEnroller;
import com.bluejungle.framework.patterns.EnumBase;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo.ClientInfoEnroller;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/EnrollmentTypeEnumType.java#1 $
 */

public class EnrollmentTypeEnumType extends EnumBase {

	private static final long serialVersionUID = 1L;

	protected static final Map<String, String> CLASS_NAME_TO_NAME_MAP = new HashMap<String, String>();

    public static final EnrollmentTypeEnumType DIRECTORY =
			new EnrollmentTypeEnumType("DIRECTORY", ActiveDirectoryEnroller.class);
    public static final EnrollmentTypeEnumType DOMAINGROUP =
    		new EnrollmentTypeEnumType("DOMAINGROUP", DomainGroupEnroller.class);
	public static final EnrollmentTypeEnumType LDIF =
			new EnrollmentTypeEnumType("LDIF", LdifEnroller.class);
	public static final EnrollmentTypeEnumType PORTAL =
			new EnrollmentTypeEnumType("PORTAL", SharePointEnroller.class);
	public static final EnrollmentTypeEnumType TEXT =
			new EnrollmentTypeEnumType("PROPERTY_FILE", JavaPropertiesFileEnroller.class);
	public static final EnrollmentTypeEnumType CLIENT_INFO =
			new EnrollmentTypeEnumType("CLIENT_INFO", ClientInfoEnroller.class);

    /*
     * Private:
     */
    private Class<? extends IEnroller> clazz;

    /**
     * Constructor
     * 
     * @param arg0
     */
    protected EnrollmentTypeEnumType(String typeName, Class<? extends IEnroller> clazz) {
        super(typeName);
        this.clazz = clazz;
        CLASS_NAME_TO_NAME_MAP.put(clazz.getName(), typeName);
    }

    /**
     * Retrieves the enum type by name
     * 
     * @param name
     * @return
     */
    public static EnrollmentTypeEnumType getByName(String name) {
        return EnumBase.getElement(name, EnrollmentTypeEnumType.class);
    }

    /**
     * Retrieves the enum type by name
     * 
     * @param name
     * @return
     */
    public static EnrollmentTypeEnumType getByClassName(String className) {
        return EnumBase.getElement(CLASS_NAME_TO_NAME_MAP.get(className), 
        		EnrollmentTypeEnumType.class);
    }

    /**
     * Returns whether an enum element exists
     * 
     * @param name
     * @return
     */
    public static boolean exists(String name) {
        return EnumBase.existsElement(name, EnrollmentTypeEnumType.class);
    }

    /**
     * Returns the class name for the given enum type
     * 
     * @return
     */
    public String getClassName() {
        return this.clazz.getName();
    }
    
    /**
     * Retrieve all elements of the enum as a Set
     * 
     * @return all elements of the enum as a Set
     */
    public static Set<EnrollmentTypeEnumType> getElements() {
        return elements(EnrollmentTypeEnumType.class);
    }
}
