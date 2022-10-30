/*
 * Created on Nov 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import junit.framework.TestSuite;

import net.sf.hibernate.HibernateException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnrollmentSession.java#1 $
 */

public class TestEnrollmentSession extends AbstractDictionaryTest{
    
    public static TestSuite suite() {
        return new TestSuite(TestEnrollmentSession.class);
    }

    protected DictionarySizeCount dbCount;  
    
    protected Collection<IElementBase> currentDatas;
    
    @Override
    public void testSetupDictionary() throws Exception {
        init();
    }
    
    @Override
    protected void setUp() throws Exception {
        init();
        dbCount = new DictionarySizeCount(dictionary);
        dbCount.checkDatabaseCount();
    }
    
    protected void sync(Collection<? extends IElementBase> inserts,
            Collection<? extends IElementBase> deletes) throws DictionaryException {
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            if (inserts != null) {
                session.saveElements(inserts);
            }
            if (deletes != null) {
                session.deleteElements(deletes);
            }
            session.commit();
        } catch (DictionaryException dex) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
    }
    
    protected void checkActivityData(Collection<IElementBase> datas) throws DictionaryException{
        assertNotNull(datas);
        
        Date latestConsistentTime = dictionary.getLatestConsistentTime();
        for(IElementBase data : datas){
            IMElementBase dbo = dictionary.getByKey(data.getInternalKey(), latestConsistentTime);
            assertNotNull(dbo);
            assertElementEquals(data, dbo);
            
            IMElementBase dbo2 = enrollments[0].getByKey(data.getExternalKey(), latestConsistentTime);
            assertNotNull(dbo2);
            assertElementEquals(dbo, dbo2);
        }
    }
    
    protected void checkInactivityData(Collection<IElementBase> datas) throws DictionaryException{
        assertNotNull(datas);
        Date latestConsistentTime = dictionary.getLatestConsistentTime();
        for(IElementBase data : datas){
            IMElementBase dbo = dictionary.getByKey(data.getInternalKey(), latestConsistentTime);
            IMElementBase dbo2 = enrollments[0].getByKey(data.getExternalKey(), latestConsistentTime);
            
            if (dbo != null) {
                assertNotSame(data.getType(), dbo.getType());
                assertNotNull(dbo2);
                assertElementEquals(dbo, dbo2);
            }
        }
    }
    
    protected void assertElementEquals(IElementBase expected, IElementBase actual) {
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getUniqueName(),  actual.getUniqueName());
        assertEquals(expected.getExternalKey(), actual.getExternalKey());
        assertEquals(expected.getPath(),        actual.getPath());
        assertEquals(expected.getType(),        actual.getType());
    }
    
    public void testBasic() throws DictionaryException, HibernateException, SQLException {
        dbCount.checkDatabaseCount();
        assertNull(currentDatas);
        
        currentDatas = new LinkedList<IElementBase>();
        String name = "cinnamon teal";
        IMElement element = enrollments[0].makeNewElement(
                new DictionaryPath(new String[]{"ou=enrollment", "ou=session", "cn=" + name}), 
                userStruct.type, 
                new DictionaryKey(name.getBytes())
        );
        currentDatas.add(element);
        
        sync(currentDatas, null);
        checkActivityData(currentDatas);
        
        dbCount.dictElements         = 1;
        dbCount.dictEnumGroupMembers = 0;
        dbCount.dictEnumGroups       = 0;
        dbCount.dictEnumMembers      = 0;
        dbCount.dictEnumRefMembers   = 0;
        dbCount.dictLeafElements     = 1;
        dbCount.dictStructGroups     = 0;
        dbCount.checkDatabaseCount();
    }
    
    public void testChangePath() throws DictionaryException, HibernateException, SQLException {
        testBasic();
        assertNotNull(currentDatas);
        assertTrue(currentDatas.size() == 1);
        
        
        IElementBase element = currentDatas.iterator().next();

        
        DictionaryPath newPath = new DictionaryPath("cn=baby", element.getPath());
        ((IMElementBase)element).setPath(newPath);
        
        sync(currentDatas, null);
        
        checkActivityData(currentDatas);
        
        dbCount.dictElements         += 1;
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups       += 0;
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 1;
        dbCount.dictStructGroups     += 0;
        dbCount.checkDatabaseCount();
    }
    
    public void testDupPath() throws DictionaryException, HibernateException, SQLException {
        testBasic();
        assertNotNull(currentDatas);
        assertTrue(currentDatas.size() == 1);
        
        
        IElementBase element = currentDatas.iterator().next();
        
        DictionaryPath newPath = new DictionaryPath("cn=baby", element.getPath());
        IMElement newElement = enrollments[0].makeNewElement(
                newPath, 
                element.getType(), 
                element.getExternalKey()
        );
        
        Collection<IElementBase> inactiveData = new ArrayList<IElementBase>(currentDatas);
        currentDatas.clear();
        currentDatas.add(newElement);
        sync(currentDatas, null);
        
        checkActivityData(currentDatas);
        checkInactivityData(inactiveData);
        
        dbCount.dictElements         += 1;
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups       += 0;
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 1;
        dbCount.dictStructGroups     += 0;
        dbCount.checkDatabaseCount();
    }
    
    /*
     * type can't be changed, only recreate is possible
     */
    public void testChangeType() throws DictionaryException, HibernateException, SQLException {
        testBasic();
        assertNotNull(currentDatas);
        assertTrue(currentDatas.size() == 1);
        
        
        IElementBase element = currentDatas.iterator().next();
        
        IMGroup newElement = enrollments[0].makeNewEnumeratedGroup(
                element.getPath(), 
                element.getExternalKey()
        );
        
        Collection<IElementBase> inactiveData = new ArrayList<IElementBase>(currentDatas);
        currentDatas.clear();
        currentDatas.add(newElement);
        sync(currentDatas, null);
        
        checkActivityData(currentDatas);
        checkInactivityData(inactiveData);
        
        dbCount.dictElements         += 1;
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups       += 1;
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 0;
        dbCount.dictStructGroups     += 0;
        dbCount.checkDatabaseCount();
    }
    
    public void testChangePathAndType() throws DictionaryException, HibernateException, SQLException {
        testBasic();
        assertNotNull(currentDatas);
        assertTrue(currentDatas.size() == 1);
        
        IElementBase element = currentDatas.iterator().next();
        DictionaryPath newPath = new DictionaryPath("cn=baby", element.getPath());
        IMGroup newElement = enrollments[0].makeNewEnumeratedGroup(
                newPath, 
                element.getExternalKey()
        );
        Collection<IElementBase> inactiveData = new ArrayList<IElementBase>(currentDatas);
        currentDatas.clear();
        currentDatas.add(newElement);
        sync(currentDatas, null);
        
        checkActivityData(currentDatas);
        checkInactivityData(inactiveData);
        
        dbCount.dictElements         += 1;
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups       += 1;
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 0;
        dbCount.dictStructGroups     += 0;
        dbCount.checkDatabaseCount();
    }
}
