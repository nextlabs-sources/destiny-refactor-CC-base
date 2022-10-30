/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryTestHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.ObjectType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/HostActor.java#1 $
 */

class HostActor extends BaseActor{
    private static final String HOST_NAME_TEMPLATE = "host%04d";
    
    private static final String HOSTNAMR_ATTR_KEY = "dNSHostName";
    private static final String[] ALL_EDITABLE_ATTR_KEYS = new String[] { HOSTNAMR_ATTR_KEY };
    

    HostActor(ActiveDirectoryTestHelper adUtil) {
        super(adUtil, 0f, 0.01f, 0.99f);
    }
    
    @Override
    LDAPAttributeSet getLDAPAttributeSet(String name, int id) {
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        attrs.add(new LDAPAttribute("objectclass", "computer"));
        attrs.add(new LDAPAttribute("dNSHostName", String.format("%s.unittest.nextlabs.com", name)));
        attrs.add(new LDAPAttribute("cn", name));
        return attrs;
    }

    @Override
    String getNameTemplate() {
        return HOST_NAME_TEMPLATE;
    }

    @Override
    ObjectType getObjectType() {
        return ObjectType.HOST;
    }
    
    @Override
    boolean isMatch(LDAPAttributeSet attrs) {
        LDAPAttribute attr = attrs.getAttribute("objectClass");
        String[] values = attr.getStringValueArray();
        return contains(values, "computer");
    }

    @Override
    LDAPAttribute getRandomEditAttribute(String key, MyLDAPEntry entry){
        if (key != HOSTNAMR_ATTR_KEY) {
            throw new IllegalArgumentException(key);
        }
        String name = entry.getAttribute("cn").getStringValue();
        return new LDAPAttribute(HOSTNAMR_ATTR_KEY, String.format("%s-%d.unittest.nextlabs.com", name, R.nextInt(1000)));
    }
    
    @Override
    String pickModifyAttribute(int op) {
        return ALL_EDITABLE_ATTR_KEYS[R.nextInt(ALL_EDITABLE_ATTR_KEYS.length)];
    }
}
