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
import com.nextlabs.random.RandomString;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPModification;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/UserActor.java#1 $
 */

class UserActor extends BaseActor{
    private static final String USER_NAME_TEMPLATE = "user%04d";
    
    private static final String EMAIL_ATTR_KEY = "proxyAddresses";
//    private static final String USERNAME_ATTR_KEY = "userPrincipalName";
    private static final String LASTNAME_ATTR_KEY = "givenName";
    private static final String FIRSTNAME_ATTR_KEY = "sn";
    private static final String TITLE_ATTR_KEY = "title";
    private static final String COMPANY_ATTR_KEY = "company";
    private static final String DEPARTMENT_ATTR_KEY = "department";
    private static final String COUNTRY_ATTR_KEY = "co";
    private static final String ISOCOUNTRYCODE_ATTR_KEY = "c";
    private static final String COUNTRYCODE_ATTR_KEY = "CountryCode";
    
    private static final String[] ALL_EDITABLE_ATTR_KEYS = new String[] { 
        EMAIL_ATTR_KEY,
//        USERNAME_ATTR_KEY,
        LASTNAME_ATTR_KEY,
        FIRSTNAME_ATTR_KEY,
        TITLE_ATTR_KEY,
        COMPANY_ATTR_KEY,
        DEPARTMENT_ATTR_KEY,
        COUNTRY_ATTR_KEY,
        ISOCOUNTRYCODE_ATTR_KEY,
        COUNTRYCODE_ATTR_KEY,
    };
    
    private static final String[] ALL_DELETABLE_ATTR_KEYS = new String[] { 
        EMAIL_ATTR_KEY,
//        USERNAME_ATTR_KEY,
        LASTNAME_ATTR_KEY,
        FIRSTNAME_ATTR_KEY,
        TITLE_ATTR_KEY,
        COMPANY_ATTR_KEY,
        DEPARTMENT_ATTR_KEY,
        COUNTRY_ATTR_KEY,
        ISOCOUNTRYCODE_ATTR_KEY,
    };
    
    UserActor(ActiveDirectoryTestHelper adUtil) {
        super(adUtil);
    }

    @Override
    LDAPAttributeSet getLDAPAttributeSet(String name, int id) {
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        attrs.add(new LDAPAttribute("objectclass", "inetOrgPerson"));
        attrs.add(new LDAPAttribute("proxyAddresses", new String[] {
                String.format("%s@unitest.nextlabs.com", name),
                String.format("Smtp:%s@unitest.bluejungle.com", name) }));
        attrs.add(new LDAPAttribute("cn", name));
        attrs.add(new LDAPAttribute("userPrincipalName", name));

        //the "name" is setted automatically
        //      attributeSet.add(new LDAPAttribute("name", username));
        attrs.add(new LDAPAttribute("givenName", String.format("%s%04d", "Matt", id)));
        attrs.add(new LDAPAttribute("sn", String.format("%s%04d", "Murdock", id)));
        attrs.add(new LDAPAttribute("title", String.format("%s%02d", "superhero", id % 100)));
        attrs.add(new LDAPAttribute("company", "Marvel"));
        attrs.add(new LDAPAttribute("department", String.format("%s%01d", "S.H.I.E.L.D", id % 10)));
        attrs.add(new LDAPAttribute("co", "United State"));
        attrs.add(new LDAPAttribute("c", "US"));
        attrs.add(new LDAPAttribute("CountryCode", "0"));
        return attrs;
    }

    @Override
    String getNameTemplate() {
        return USER_NAME_TEMPLATE;
    }

    @Override
    ObjectType getObjectType() {
        return ObjectType.USER;
    }
    
    @Override
    boolean isMatch(LDAPAttributeSet attrs) {
        LDAPAttribute attr = attrs.getAttribute("objectClass");
        String[] values = attr.getStringValueArray();
        return contains(values, "inetOrgPerson");
    }

    String pickModifyAttribute(int op){
        switch (op) {
        case LDAPModification.ADD:
            return EMAIL_ATTR_KEY;
        case LDAPModification.REPLACE:
            return ALL_EDITABLE_ATTR_KEYS[R.nextInt(ALL_EDITABLE_ATTR_KEYS.length)];
        case LDAPModification.DELETE:
            return ALL_DELETABLE_ATTR_KEYS[R.nextInt(ALL_DELETABLE_ATTR_KEYS.length)];
        default:
            return null;
        }
    }
    
    private String createEmail(){
        StringBuilder sb = new StringBuilder();
        if(R.nextBoolean()){
            sb.append("smtp:");
        }
        int words = R.nextInt(3) + 1;
        for (int j = 0; j < words; j++) {
            if (j != 0) {
                final char[] seps = new char[]{'-', '_', '.'};
                sb.append(seps[R.nextInt(seps.length)]);
            }
            sb.append(RandomString.getRandomString(1, 8, RandomString.ALNUM));
        }
        sb.append('@');
        words = R.nextInt(6) + 1;
        for (int j = 0; j < words; j++) {
            if (j != 0) {
                sb.append('.');
            }
            sb.append(RandomString.getRandomString(1, 15, RandomString.ALNUM));
        }
        sb.append('.');
        String[] suffix = new String[]{"com", "net", "org", "tel", "tv", "mobi", "biz", "info"};
        sb.append(suffix[R.nextInt(suffix.length)]);
        
        return RandomString.mix(sb.toString());
    }
    
    @Override
    LDAPAttribute getRandomEditAttribute(String key, MyLDAPEntry entry) {
        LDAPAttribute attr;
        if(key == EMAIL_ATTR_KEY){
            int size = R.nextInt(50) + 1;
            String[] addresses = new String[size];
            for (int i = 0; i < size; i++) {
                addresses[i] = createEmail();
            }
            attr = new LDAPAttribute(key, addresses);
//        }else if(key == USERNAME_ATTR_KEY ){
//            attr = new LDAPAttribute(key, RandomString.getRandomString(8, 30, RandomString.ALNUM));
        }else if(key == LASTNAME_ATTR_KEY
                || key == FIRSTNAME_ATTR_KEY
                || key == TITLE_ATTR_KEY
                || key == COMPANY_ATTR_KEY
                || key == DEPARTMENT_ATTR_KEY
                || key == COUNTRY_ATTR_KEY){
            String value = RandomString.getRandomString(10, 50, RandomString.ALNUM);
            value += RandomString.getRandomString(3, 3, RandomString.ALL);
            attr = new LDAPAttribute(key, value);
        } else if (key == ISOCOUNTRYCODE_ATTR_KEY) {
            attr = new LDAPAttribute(key, RandomString.getRandomString(1, 1, RandomString.LOWER));
        } else if (key == COUNTRYCODE_ATTR_KEY) {
            attr = new LDAPAttribute(key, Integer.toString(R.nextInt(32000)));
        } else {
            throw new IllegalArgumentException(key);
        }
        
        return attr;
    }
    
    LDAPAttribute getRandomAddAttribute(String key, MyLDAPEntry entry) {
        if (key != EMAIL_ATTR_KEY) {
            throw new IllegalArgumentException(key);
        }
        return new LDAPAttribute(key, createEmail());
    }
}
