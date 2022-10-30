/*
 * Created on Apr 23, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment.ad;

import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryElementFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.SyncResultEnum;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.framework.utils.IPair;
import com.novell.ldap.LDAPEntry;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/ad/ADElementCreatorMod.java#1 $
 */
public class ADElementCreatorMod extends ActiveDirectoryElementFactory{
	public enum ElementType{
		// from ElementTypeEnumType.java
		USER, 
		CONTACT, 
		HOST, 
		APPLICATION	, 
		SITE, 
//		CLIENT_INFO	(),
		GROUP, 
		STRUCT_GROUP, 
		OTHER, 
		UNKNOWN;
		
		private Color color;
		
		private ElementType(){
			//default color
			this(Color.BLACK);
		}
		
		private ElementType(Color color){
			this.color = color;
		}
		
		public Color getColor(){
			return color;
		}
		
		public void setColor(Color color){
		    this.color = color;
		}
		
		
	}
	
	private ElementType lastCreatedType = null;
	private Set<IElementField> lastExternalFields = null;

	public ADElementCreatorMod(ILDAPEnrollmentWrapper enrollmentWrapper, IDictionary dictionary)
			throws EnrollmentValidationException {
		super(enrollmentWrapper, dictionary);
	}
	
	@Override
    protected DictionaryKey getElementKey(LDAPEntry entry, IElementType type) {
        DictionaryKey key = super.getElementKey(entry, type);
        if (key == null) {
            lastCreatedType = ElementType.UNKNOWN;
        }
        return key;
    }
	
	@Override
	protected IPair<SyncResultEnum, IElementBase> createUnknownElement(LDAPEntry entry,
			DictionaryKey key, 
			String dn,
			DictionaryPath path) {
		lastCreatedType = ElementType.UNKNOWN;
		return super.createUnknownElement(entry, key, dn, path);
	}

	@Override
	protected IPair<SyncResultEnum, IMElement> createElement(LDAPEntry entry, IElementType type, DictionaryKey key,
			String dn, DictionaryPath path) throws DictionaryException, EnrollmentSyncException {
		lastCreatedType = ElementType.valueOf(type.getName());
		return super.createElement(entry, type, key, dn, path);
	}

	@Override
	protected IPair<SyncResultEnum, IMGroup> createGroupElement(LDAPEntry entry, DictionaryKey key, String dn,
			DictionaryPath path) throws DictionaryException, EnrollmentSyncException {
		lastCreatedType = ElementType.GROUP;
		return super.createGroupElement(entry, key, dn, path);
	}

	@Override
	protected IPair<SyncResultEnum, IElementBase> createOtherElement(LDAPEntry entry, DictionaryKey key, String dn,
			DictionaryPath path) {
		lastCreatedType = ElementType.OTHER;
		return super.createOtherElement(entry, key, dn, path);
	}

	@Override
	protected IPair<SyncResultEnum, IMGroup> createStructElement(LDAPEntry entry, DictionaryKey key, String dn,
			DictionaryPath path) throws DictionaryException {
		lastCreatedType = ElementType.STRUCT_GROUP;
		return super.createStructElement(entry, key, dn, path);
	}

	public ElementType getLastCreatedType() {
		return lastCreatedType;
	}
	
	public Set<IElementField> getLastExternalFields() {
		return lastExternalFields;
	}
	
	@Override
	public Boolean setElementFieldsValues(IMElement element, 
			LDAPEntry entry,
			Map<IElementField, String> externalFields, 
			IElementField uniqueNameField,
			IElementField displayNameField,
			IElementField sidField) throws EnrollmentSyncException {
	    Boolean b = super.setElementFieldsValues(element, entry, externalFields, uniqueNameField, displayNameField, sidField);
		lastExternalFields = new HashSet<IElementField>(externalFields.keySet());
		lastExternalFields.add(uniqueNameField);
        if (sidField != null && externalFields.containsKey(sidField)) {
            lastExternalFields.add(sidField);
        }
		return b;
	}
}
