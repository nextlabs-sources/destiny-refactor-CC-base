/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryTestHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.ObjectType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/GroupActor.java#1 $
 */

class GroupActor extends BaseActor{
    private static final String GROUP_NAME_TEMPLATE = "group%04d";
    
    private static final String MEMBER_ATTR_KEY = "member";
    private static final String[] ALL_EDITABLE_ATTR_KEYS = new String[] { MEMBER_ATTR_KEY };
    
    GroupActor(ActiveDirectoryTestHelper adUtil) {
        super(adUtil, 0f, 0.05f, 0.95f);
    }

    @Override
    LDAPAttributeSet getLDAPAttributeSet(String name, int id) {
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        attrs.add(new LDAPAttribute("objectClass", "group"));
        
        LDAPAttribute memberAttr = getRandomMemberShip();
        if (memberAttr != null) {
            attrs.add(memberAttr);
        }
        
        return attrs;
    }
    
    LDAPAttribute getRandomMemberShip() {
        Set<MyLDAPEntry> entries;
        float type = R.nextFloat();
        if(type <= 0.45f){
            entries = MyLDAPEntry.ALL_ENTRIES.get(ObjectType.USER).lottoTime(SuperLotto.ALL_LOTTO);
        }else if( type <= 0.9f){
            entries = MyLDAPEntry.ALL_ENTRIES.get(ObjectType.HOST).lottoTime(SuperLotto.ALL_LOTTO);
        }else {
            entries = MyLDAPEntry.ALL_ENTRIES.get(ObjectType.USER).lottoTime(SuperLotto.ALL_LOTTO);
            entries.addAll(MyLDAPEntry.ALL_ENTRIES.get(ObjectType.HOST).lottoTime(SuperLotto.ALL_LOTTO));
        }
        
        if (!entries.isEmpty()) {
            String[] dns = new String[entries.size()];
            int i = 0;
            for (MyLDAPEntry entry : entries) {
                dns[i++] = entry.getDN();
            }
            return new LDAPAttribute(MEMBER_ATTR_KEY, dns);
        }
        
        return null;
    }
    
    @Override
    LDAPAttribute getRandomEditAttribute(String key, MyLDAPEntry entry){
        if (key != MEMBER_ATTR_KEY) {
            throw new IllegalArgumentException(key);
        }
        return getRandomMemberShip();
    }
    
    String[] getAllPossibleModifyAttribute(){
        return ALL_EDITABLE_ATTR_KEYS;
    }

    @Override
    String getNameTemplate() {
        return GROUP_NAME_TEMPLATE;
    }

    @Override
    ObjectType getObjectType() {
        return ObjectType.GROUP;
    }

    @Override
    boolean isMatch(LDAPAttributeSet attrs) {
        LDAPAttribute attr = attrs.getAttribute("objectClass");
        String[] values = attr.getStringValueArray();
        return contains(values, "group");
    }
}
