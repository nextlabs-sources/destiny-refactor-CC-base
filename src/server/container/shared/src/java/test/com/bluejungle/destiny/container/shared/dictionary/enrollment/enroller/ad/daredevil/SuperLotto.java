/*
 * Created on Jul 28, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.novell.ldap.LDAPEntry;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/SuperLotto.java#1 $
 */

public class SuperLotto<T extends LDAPEntry>{
    public static final int ALL_LOTTO = -3213;
    
    private Set<T> entries;
    private Random r;
    
    
    SuperLotto() {
        entries = new HashSet<T>();
        r = new Random();
    }

    public void add(T newEntry){
        if (entries.contains(newEntry)) {
            throw new IllegalArgumentException(newEntry + " already exists.");
        }
        entries.add(newEntry);
    }
    
    public void remove(T delEntry){
        if (!entries.remove(delEntry)) {
            throw new IllegalArgumentException(delEntry + " is not found.");
        }
    }
    
    public T lottoTime(){
        return lottoTime(1).iterator().next();
    }
    
    public Set<T> lottoTime(int size){
        if(size == ALL_LOTTO){
            size = entries.size();
        }
        
        if (size > entries.size()) {
            throw new IllegalArgumentException("not enough lotto");
        }
        Set<T> items = new HashSet<T>();

        List<T> list = new ArrayList<T>(entries);
        for (int i = 0; i < size; i++) {
            items.add(list.remove(r.nextInt(list.size())));
        }

        return items;
    }
    
    public boolean isEmpty(){
        return entries.isEmpty();
    }
    
    public int size(){
        return entries.size();
    }
}
