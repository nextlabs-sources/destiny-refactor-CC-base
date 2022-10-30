package com.bluejungle.framework.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/SetUtils.java#1 $
 */

public class SetUtils {
    
    /**
     * returns a new set representing the intersection of given
     * sets.
     **/
    public static Set intersection(Set[] sets){
        if (sets == null || sets.length == 0){
            return new HashSet();
        }
        Set minSet = null;
        int minSetIdx = 0;
        for (int i = 0; i < sets.length; i++){
            if (sets [i] == null) {
                return new HashSet ();
            }
            if ((minSet == null) || (minSet.size() > sets[i].size())){
                minSet = sets[i];
                minSetIdx = i;
            }
        }
        
        HashSet rv = new HashSet();
        Iterator iter = minSet.iterator();
        while (iter.hasNext()){
            boolean allContain = true;
            Object member = iter.next();
            for (int i = 0; i < sets.length; i++){
                if (i == minSetIdx){
                    continue;
                }
                if (!sets[i].contains(member)){
                    allContain = false;
                    break;
                }
                if (allContain) {
                    rv.add(member);
                }
            }
            
        }
        return rv;
    }
    
 
    /**
     * returns a new set that is a union of all the argument sets
     * @param sets sets to union over
     * @return union of sets
     */
    public static Set union(Set[] sets) {
        if (sets == null || sets.length == 0) {
            return new HashSet();
        }
        
        HashSet rv = new HashSet();
        for (int i = 0; i < sets.length; i++) {
            Set set = sets[i];
            
            if (set == null) continue;
            for (Iterator iter = set.iterator(); iter.hasNext();) {
                Object element = iter.next();
                rv.add(element);
            }
        }
        return rv;
    }


    /**
     * @modifies s1
     * @param s1
     * @param s2
     * @return s1 minus s2
     */
    public static <T> Set<T> minus(Set<T> s1, Set<T> s2) {
        if (s2 == null) {
        	return s1;
        }
        for ( T element : s2 ) {
            s1.remove(element);
        }
        return s1;
    }

}
