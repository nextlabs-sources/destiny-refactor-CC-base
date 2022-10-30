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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/OUActor.java#1 $
 */

class OUActor extends BaseActor{
    private static final String OU_NAME_TEMPLATE = "ou%04d";
    
    OUActor(ActiveDirectoryTestHelper adUtil) {
        super(adUtil);
    }
    
    @Override
    LDAPAttributeSet getLDAPAttributeSet(String name, int id) {
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        attrs.add(new LDAPAttribute("objectClass", "organizationalUnit"));
        attrs.add(new LDAPAttribute("ou", name));
        return attrs;
    }

    @Override
    String getNameTemplate() {
        return OU_NAME_TEMPLATE;
    }

    @Override
    ObjectType getObjectType() {
        return ObjectType.OU;
    }

    @Override
    void delete() throws BullseyeException {
        if(ous.size() > 1){
            super.delete();
        }
    }

    @Override
    void move() throws BullseyeException {
        if(ous.size() > 2){
            super.move();
        }
    }
    
    @Override
    boolean isMatch(LDAPAttributeSet attrs) {
        LDAPAttribute attr = attrs.getAttribute("objectClass");
        String[] values = attr.getStringValueArray();
        return contains(values, "organizationalUnit");
    }
}
