/*
 * Created on Nov 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.dictionary.DictionaryTestHelper.DictioanryTable;

import static junit.framework.Assert.assertEquals;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/DictionarySizeCount.java#1 $
 */

public class DictionarySizeCount {

    private final IDictionary dictionary;
    
    public DictionarySizeCount(IDictionary dictionary){
        this.dictionary = dictionary;
    }
    
    public int dictElements = 0;
    public int dictEnumGroupMembers = 0;
    public int dictEnumGroups = 0;
    public int dictEnumMembers = 0;
    public int dictEnumRefMembers = 0;
    public int dictLeafElements = 0;
    public int dictStructGroups = 0;
    
    public void fastReset(){
        dictElements = 0;
        dictEnumGroupMembers = 0;
        dictEnumGroups = 0;
        dictEnumMembers = 0;
        dictEnumRefMembers = 0;
        dictLeafElements = 0;
        dictStructGroups = 0;
    }
    
    public int[] getDatabaseCount() {
        return new int[] { dictElements, dictEnumGroupMembers, dictEnumGroups, dictEnumMembers,
                dictEnumRefMembers, dictLeafElements, dictStructGroups };
    }
    
    public void checkDatabaseCount(int[] numbers) throws HibernateException, SQLException,
            DictionaryException {
        if (numbers.length != 7) {
            throw new IllegalArgumentException("must be 7 numbers");
        }

        checkDatabaseCount(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4], numbers[5],
                numbers[6]);
    }
    
    public void checkDatabaseCount() throws HibernateException, SQLException, DictionaryException {
        checkDatabaseCount(dictElements, dictEnumGroupMembers, dictEnumGroups, dictEnumMembers,
                dictEnumRefMembers, dictLeafElements, dictStructGroups);
    }
    
    public void checkDatabaseCount(
        int dictElements,
        int dictEnumGroupMembers,
        int dictEnumGroups,
        int dictEnumMembers,
        int dictEnumRefMembers,
        int dictLeafElements,
        int dictStructGroups
)
            throws HibernateException, SQLException, DictionaryException {
        Map<DictionaryTestHelper.DictioanryTable, Integer> r =
                DictionaryTestHelper.queryDictionaryRowCounts(dictionary);

        String message =
            String.format("%d,%d,%d,%d,%d,%d,%d vs  %d,%d,%d,%d,%d,%d,%d",        
                dictElements,
                dictEnumGroupMembers,
                dictEnumGroups,
                dictEnumMembers,
                dictEnumRefMembers,
                dictLeafElements,
                dictStructGroups,
                r.get(DictioanryTable.DICT_ELEMENTS),
                r.get(DictioanryTable.DICT_ENUM_GROUP_MEMBERS),
                r.get(DictioanryTable.DICT_ENUM_GROUPS),
                r.get(DictioanryTable.DICT_ENUM_MEMBERS),
                r.get(DictioanryTable.DICT_ENUM_REF_MEMBERS),
                r.get(DictioanryTable.DICT_LEAF_ELEMENTS),
                r.get(DictioanryTable.DICT_STRUCT_GROUPS)
        );
        
        assertEquals(message, dictElements,         r.get(DictioanryTable.DICT_ELEMENTS).intValue());
        assertEquals(message, dictEnumGroupMembers, r.get(DictioanryTable.DICT_ENUM_GROUP_MEMBERS).intValue());
        assertEquals(message, dictEnumGroups,       r.get(DictioanryTable.DICT_ENUM_GROUPS).intValue());
        assertEquals(message, dictEnumMembers,      r.get(DictioanryTable.DICT_ENUM_MEMBERS).intValue());
        assertEquals(message, dictEnumRefMembers,   r.get(DictioanryTable.DICT_ENUM_REF_MEMBERS).intValue());
        assertEquals(message, dictLeafElements,     r.get(DictioanryTable.DICT_LEAF_ELEMENTS).intValue());
        assertEquals(message, dictStructGroups,     r.get(DictioanryTable.DICT_STRUCT_GROUPS).intValue());
    }
    
    public void resetDatabaseCount() throws HibernateException, SQLException, DictionaryException {
        Map<DictionaryTestHelper.DictioanryTable, Integer> r =
                DictionaryTestHelper.queryDictionaryRowCounts(dictionary);

        dictElements         = r.get(DictioanryTable.DICT_ELEMENTS);
        dictEnumGroupMembers = r.get(DictioanryTable.DICT_ENUM_GROUP_MEMBERS);
        dictEnumGroups       = r.get(DictioanryTable.DICT_ENUM_GROUPS);
        dictEnumMembers      = r.get(DictioanryTable.DICT_ENUM_MEMBERS);
        dictEnumRefMembers   = r.get(DictioanryTable.DICT_ENUM_REF_MEMBERS);
        dictLeafElements     = r.get(DictioanryTable.DICT_LEAF_ELEMENTS);
        dictStructGroups     = r.get(DictioanryTable.DICT_STRUCT_GROUPS);
    }

    public void printDatabaseCount() throws HibernateException, SQLException, DictionaryException {
        Map<DictionaryTestHelper.DictioanryTable, Integer> r =
                DictionaryTestHelper.queryDictionaryRowCounts(dictionary);

        for (Map.Entry<DictionaryTestHelper.DictioanryTable, Integer> e : r.entrySet()) {
            System.out.println(e.getKey() + ", " + e.getValue());
        }
    }
    
    /**
     * add two array together, the length must be same
     * The values in <code>orginal</code> don't change 
     * @param orginal
     * @param add
     * @return
     */
    public static int[] add(int[] orginal, int[] add) {
        if (orginal.length != add.length) {
            throw new IllegalArgumentException(orginal.length + "!=" + add.length);
        }
        int[] r = new int[orginal.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = orginal[i] + add[i];
        }
        return r;
    }
    
    /**
     * add two array together, the length must be same
     * The values in <code>orginal</code> don't change 
     * @param orginal
     * @param add
     * @return
     */
    public static int[] subtract(int[] orginal, int[] substract) {
        if (orginal.length != substract.length) {
            throw new IllegalArgumentException(orginal.length + "!=" + substract.length);
        }
        int[] r = new int[orginal.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = orginal[i] - substract[i];
        }
        return r;
    }
}
