/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import java.util.Random;

import javax.naming.NamingException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryTestHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.Action;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.ObjectType;
import com.nextlabs.shared.tools.StringFormatter;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/BaseActor.java#1 $
 */
abstract class BaseActor{
    private static final int NAME_NUMNBER_MAX = 10000;
    protected static final Random R = new Random();
    
    final SuperLotto<MyLDAPEntry> ous = MyLDAPEntry.ALL_ENTRIES.get(ObjectType.OU);
    final ActiveDirectoryTestHelper adUtil;
    
    float chanceToAdd;
    float chanceToDelete;
    float chanceToEdit;
    
    abstract String getNameTemplate();
    abstract ObjectType getObjectType();
    abstract LDAPAttributeSet getLDAPAttributeSet(String name, int id);
    
    BaseActor(ActiveDirectoryTestHelper adUtil) {
       this(adUtil, 0.005f, 0.02f, 0.975f);
    }
    
    BaseActor(ActiveDirectoryTestHelper adUtil, 
            float chanceToAdd, 
            float chanceToDelete,
            float chanceToEdit) {
        this.adUtil = adUtil;
        if(chanceToAdd + chanceToDelete + chanceToEdit != 1){
            throw new IllegalArgumentException("sum of all chance should be equal to 1");
        }
        this.chanceToAdd = chanceToAdd;
        this.chanceToDelete = chanceToDelete;
        this.chanceToEdit = chanceToEdit;
    }
    
    void create() throws BullseyeException{
        if (ous.isEmpty()) {
            throw new IllegalArgumentException("big problem! ous is empty");
        }
        MyLDAPEntry ou = ous.lottoTime();
        String name;
        int magicNumber;
        do {
            magicNumber = R.nextInt(NAME_NUMNBER_MAX);
            name = String.format(getNameTemplate(), magicNumber);
        } while (ou.contains(getObjectType().getPrefix() + name));

        LDAPAttributeSet attrs = getLDAPAttributeSet(name, magicNumber);
        
        MyLDAPEntry entry = new MyLDAPEntry(name, getObjectType(), attrs);
        
        try {
            entry.add(ou, adUtil);
        } catch (LDAPException e) {
            throw new BullseyeException(entry, Action.CREATE, e);
        }
        print(Action.CREATE, entry, null);
    }
    
    void delete() throws BullseyeException{
        final SuperLotto<MyLDAPEntry> objects = MyLDAPEntry.ALL_ENTRIES.get(getObjectType());
        if (!objects.isEmpty()) {
            MyLDAPEntry obj;
            do {
                obj = objects.lottoTime();
            } while (obj == MyLDAPEntry.rootOu);
            
            
            if(!obj.chilren.isEmpty()){
                //TODO
                return;
            }
            
            String dn = obj.getDN();
            try {
                
                obj.removeThis(adUtil);
            } catch (LDAPException e) {
                throw new BullseyeException(obj, Action.DELETE, e);
            }
            print(Action.DELETE, dn, null);
        }
        
    }

    void copy() throws BullseyeException {
        print(Action.COPY, null, null);
    }
    
    void move() throws BullseyeException{
        final SuperLotto<MyLDAPEntry> objects = MyLDAPEntry.ALL_ENTRIES.get(getObjectType());
        if (ous.size() > 1 && !objects.isEmpty()) {
            MyLDAPEntry fromObj ;
            MyLDAPEntry toOu;
            
            do{
                fromObj = objects.lottoTime();
                toOu = ous.lottoTime();
            }while(fromObj.contains(toOu, true) || toOu.contains(fromObj, false) || fromObj == MyLDAPEntry.rootOu);
            
            try {
//                LOG.info("move " + fromObj.getDN() + " to " + toOu.getDN());
                fromObj.move(toOu, adUtil);
            } catch (NamingException e) {
                throw new BullseyeException(fromObj, Action.MOVE, e);
            }
            print(Action.MOVE, fromObj, toOu);
        }
    }
    
    void rename() throws BullseyeException {
        print(Action.RENAME, null, null);
    }
    
    LDAPModification getRandomModify(MyLDAPEntry entry) {
        int op;
        float p = R.nextFloat();
        if (p <= chanceToDelete) {
            op = LDAPModification.DELETE;
        } else if (p <= chanceToDelete + chanceToAdd) {
            op = LDAPModification.ADD;
        } else {
            op = LDAPModification.REPLACE;
        }
        
        String attrKey = pickModifyAttribute(op);
        if(attrKey == null){
            return null;
        }
        
        LDAPModification modify;
        switch (op) {
        case LDAPModification.DELETE:
            modify =  new LDAPModification(LDAPModification.DELETE, new LDAPAttribute(attrKey));
            break;
        case LDAPModification.ADD:
            LDAPAttribute addAttr = getRandomAddAttribute(attrKey, entry);
            modify = addAttr != null 
                    ? new LDAPModification(LDAPModification.ADD, addAttr) 
                    : null;
            break;
        case LDAPModification.REPLACE:
            LDAPAttribute replaceAttr = getRandomEditAttribute(attrKey, entry);
            modify = replaceAttr != null 
                    ? new LDAPModification(LDAPModification.REPLACE, replaceAttr) 
                    : null;
            break;
        default:
            throw new IllegalArgumentException("unknown op: " + op);
        }
        
        return modify;
    }
    
    LDAPAttribute getRandomEditAttribute(String key, MyLDAPEntry entry){
        print(Action.EDIT, null, null);
        return null;
    }
    
    LDAPAttribute getRandomAddAttribute(String key, MyLDAPEntry entry){
        print(Action.EDIT, null, null);
        return null;
    }
    
    String pickModifyAttribute(int op){
        return null;
    }
    
    
    
    void edit() throws BullseyeException {
        final SuperLotto<MyLDAPEntry> objects = MyLDAPEntry.ALL_ENTRIES.get(getObjectType());
        if (!objects.isEmpty()) {
            MyLDAPEntry entry = objects.lottoTime();
            LDAPModification modify = getRandomModify(entry);
            try {
                if (modify != null) {
                    entry.update(modify, adUtil);
                    print(Action.EDIT, entry, null);
                }
            } catch (LDAPException e) {
                System.err.println(modify);
                throw new BullseyeException(entry, Action.EDIT, e);
            }
            
        }
    }
    
    void print(Action action, Object fromObj, MyLDAPEntry toObj){
        StringBuilder sb = new StringBuilder();
        sb.append(StringFormatter.fitLength(action.name(), 6));
        sb.append(", ");
        sb.append(StringFormatter.fitLength(getObjectType().name(), 5));
        sb.append(" @ ");
        if(fromObj == null){
            sb.append("NotImplemented");
        }else{
            if(fromObj instanceof MyLDAPEntry){
                sb.append(((MyLDAPEntry)fromObj).getDN());
            }else{
                sb.append(fromObj);
            }
        }
        
        if(toObj != null ){
            sb.append(" -> ").append(toObj.getDN());
        }
        System.out.println(sb.toString());
    }
    
    abstract boolean isMatch(LDAPAttributeSet attrs);
    
    boolean contains(String[] values, String key) {
        if (values == null) {
            return false;
        }

        for (String value : values) {
            if (value != null) {
                if (value.equalsIgnoreCase(key)) {
                    return true;
                }
            } else {
                if (key == null) {
                    return true;
                }
            }
        }

        return false;
    }
}