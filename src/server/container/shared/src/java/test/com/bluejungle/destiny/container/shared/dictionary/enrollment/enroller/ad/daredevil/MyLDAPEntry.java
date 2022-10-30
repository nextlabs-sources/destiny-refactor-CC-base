/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.naming.NamingException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryTestHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil.ActiveDirectoryDaredevil.ObjectType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/MyLDAPEntry.java#1 $
 */

public class MyLDAPEntry extends LDAPEntry{
    static final Map<ObjectType, SuperLotto<MyLDAPEntry>> ALL_ENTRIES;
    static{
        ALL_ENTRIES = new HashMap<ObjectType, SuperLotto<MyLDAPEntry>>(16, 0.8F);
        ALL_ENTRIES.put(ObjectType.OU,    new SuperLotto<MyLDAPEntry>());
        ALL_ENTRIES.put(ObjectType.GROUP, new SuperLotto<MyLDAPEntry>());
        ALL_ENTRIES.put(ObjectType.USER,  new SuperLotto<MyLDAPEntry>());
        ALL_ENTRIES.put(ObjectType.HOST,  new SuperLotto<MyLDAPEntry>());
    }
    
    static MyLDAPEntry rootOu = null;
    
    final Set<MyLDAPEntry> chilren = new HashSet<MyLDAPEntry>();
    
    private final String name;
    private final ObjectType objectType;
    private MyLDAPEntry parent = null;
    
    MyLDAPEntry(String name, ObjectType objectType, LDAPAttributeSet attrs) {
        super("", attrs);
        this.name = name;
        this.objectType = objectType;
        
        dn = getDN();
        
        switch (objectType) {
        case ROOT_DC:
            if (rootOu != null) {
                throw new IllegalArgumentException("ROOT_DC is already defined");
            }
            rootOu = this;
            ALL_ENTRIES.get(ObjectType.OU).add(this);
            break;
        case OU:
        case GROUP:
        case USER:
        case HOST:
            ALL_ENTRIES.get(objectType).add(this);
            break;
        default:
            throw new IllegalArgumentException(objectType.name());
        }
    }
    
    //use for init
    void update(LDAPAttributeSet attrs){
        super.attrs = attrs;
    }
    
    void update(LDAPModification modify, ActiveDirectoryTestHelper adUtil) throws LDAPException{
        dn = getDN();
        adUtil.modify(dn, modify);
        
        LDAPAttribute newAttr = modify.getAttribute();
        switch(modify.getOp()){
        case LDAPModification.ADD: {
            LDAPAttribute oldAttr = attrs.getAttribute(modify.getAttribute().getName());
            String[] oldValues = oldAttr.getStringValueArray();
            if(oldValues != null){
                for(String oldValue : oldValues){
                    newAttr.addValue(oldValue);
                }
            }
        }
            break;
        case LDAPModification.DELETE: {
            LDAPAttribute oldAttr = attrs.getAttribute(modify.getAttribute().getName());
            String[] removeValues = newAttr.getStringValueArray();
            if(removeValues != null){
                for(String removeValue : removeValues){
                    oldAttr.removeValue(removeValue);
                }
            }
        }
            break;
        case LDAPModification.REPLACE:
            //do nothing
            break;
        }
        
        attrs.remove(modify.getAttribute());
        attrs.add(newAttr);
        
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof MyLDAPEntry) {
//            return LDAPDN.normalize(this.getDN()).equals(
//                    LDAPDN.normalize(((MyLDAPEntry) obj).getDN()));
//        } else if (obj instanceof String) {
//            return LDAPDN.normalize(this.getDN()).equals(LDAPDN.normalize((String) obj));
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public int hashCode() {
//        return LDAPDN.normalize(this.dn).hashCode();
//    }
    
    boolean contains(MyLDAPEntry entry, boolean recursive){
        if(entry == this){
            return true;
        }
        
        if( !chilren.isEmpty()){
            if(chilren.contains(entry)){
                return true;
            }
                
            if (recursive) {
                for (MyLDAPEntry c : chilren) {
                    if (c.contains(entry, true)) {
                        return true;
                    }
                }
            }
        }
         
        return false;
    }
    
    MyLDAPEntry get(Queue<String> dn){
        String head = dn.poll();
        if(head == null){
            return null;
        }
        
        if(head.equals(getRdn())){
            if(dn.isEmpty()){
                return this;
            }
         
            
            for(MyLDAPEntry e : chilren){
                if(e.contains(dn)){
                    return e;
                }
            }
        }
        
        return null;
    }
    
    boolean contains(Queue<String> dn){
        return get(dn) != null;
    }
    
    MyLDAPEntry get(String rdn){
        for(MyLDAPEntry e : chilren){
            if(e.getRdn().equals(rdn.toLowerCase())){
                return e;
            }
        }
        
        return null;
    }
    
    boolean contains(String rdn){
        return get(rdn) != null;
    }
    
    //use for init
    void add(MyLDAPEntry parent){
        if(parent == null){
            throw new NullPointerException("missing parent" + this);
        }
        if(parent == this){
            throw new IllegalArgumentException("recursive looop");
        }
        this.parent = parent;
        this.parent.chilren.add(this);
        
        dn = getDN();
    }
    
    void add(MyLDAPEntry parent, ActiveDirectoryTestHelper adUtil) throws LDAPException{
        add(parent);
        dn = getDN();
        adUtil.add(this);
        
//        allEntries.get(this.objectType).add(this);
    }
    
    void removeThis(ActiveDirectoryTestHelper adUtil) throws LDAPException{
        
        if(parent == null){
            throw new NullPointerException("missing parent"+ this);
        }
        
        dn = getDN();
        adUtil.delete(getDN());
        
        ALL_ENTRIES.get(this.objectType).remove(this);
        
        parent.chilren.remove(this);
        parent = null;
    }
    
    void move(MyLDAPEntry newParent, ActiveDirectoryTestHelper adUtil) throws NamingException{
        if(newParent == null){
            throw new NullPointerException("missing newParent"+ this);
        }
        if(parent == null){
            throw new NullPointerException("missing parent"+ this);
        }
        if(parent == this){
            throw new IllegalArgumentException("recursive looop");
        }
        
        String oldDn = this.getDN();
        
        parent.chilren.remove(this);
        newParent.chilren.add(this);
        parent = newParent;
        
        dn = getDN();
        adUtil.move(oldDn, this.getDN());
        
    }
    
    
    public String getDN() {
        
        if(this == parent){
            throw new IllegalArgumentException("recursive looop");
        }
        StringBuilder sb = new StringBuilder(getRdn());
        if (parent != null) {
            sb.append(",").append(parent.getDN());
        }
        
        return sb.toString();
    }
    
    
    public String getRdn(){
        StringBuilder sb = new StringBuilder();
        switch (objectType) {
        case ROOT_DC:
            //do nothing
            break;
        default:
            sb.append(objectType.getPrefix());
            break;
        }

        sb.append(name);
        return sb.toString().toLowerCase();
    }
}
