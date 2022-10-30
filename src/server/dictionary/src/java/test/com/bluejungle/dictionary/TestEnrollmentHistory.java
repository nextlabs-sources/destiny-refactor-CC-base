/*
 * Created on Nov 7, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import junit.framework.TestSuite;

import net.sf.hibernate.HibernateException;

import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnrollmentHistory.java#1 $
 */
public class TestEnrollmentHistory extends AbstractEnrollmentHistoryTest {
    
    public static TestSuite suite() {
        return new TestSuite(TestEnrollmentHistory.class);
    }
    
    private Map<String, Integer> purgeHistory(Date date) throws DictionaryException {
        IConfigurationSession configSession = dictionary.createSession();
        try {
            configSession.beginTransaction();
            return configSession.purgeHistory(enrollments[0], date, null);
        } finally {
            configSession.close();
        }
    }

    private Map<String, Integer> purgeHistory() throws DictionaryException {
        return purgeHistory(dictionary.getLatestConsistentTime());
    }
        
    
    
    

    public void testNeverSyncEnrollment() throws DictionaryException {
        Map<String, Integer> result = purgeHistory();
        assertTrue(result.isEmpty());
    }
    
    /*
     * this method can only call once. 
     */
    public void testInitialDataNoHistory() throws DictionaryException, HibernateException, SQLException {
        
        insertSample();
        purgeHistory();
        dbCount.checkDatabaseCount();
        try {
            purgeHistory(UnmodifiableDate.END_OF_TIME);
            fail("The clear data must before last enrollment time");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }
    
    public void testInitialDataSimple() throws DictionaryException, HibernateException, SQLException {
        testInitialDataNoHistory();
        assertNotNull(currentDatas);
        
        updateAndSyncElements();
        checkActivityData(currentDatas);
        purgeHistory();
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
    }
    
    public void testMultiNoUpdate() throws HibernateException, DictionaryException, SQLException{
        final int numberOfRun = DEFAULT_NUMBER_OF_RUN;
        
        testInitialDataNoHistory();
        assertNotNull(currentDatas);
        for (int i = 0; i < numberOfRun; i++) {
            sync(currentDatas);
            checkActivityData(currentDatas);
            dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
        }
        purgeHistory();
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
    }
    
    public void testMultiUpdate() throws DictionaryException, HibernateException, SQLException{
        final int numberOfRun = DEFAULT_NUMBER_OF_RUN;
        
        testInitialDataNoHistory();
        assertNotNull(currentDatas);
        for (int i = 0; i < numberOfRun; i++) {
            updateAndSyncElements();
            checkActivityData(currentDatas);
        }
        purgeHistory();
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
    }

    protected int[] testMixedSFUpdate(boolean isLastOneSuccess) throws DictionaryException,
            HibernateException, SQLException {
        testInitialDataNoHistory();
        assertNotNull(currentDatas);

        int[] lastSuccessfulCount = super.testMixedSFUpdate(isLastOneSuccess, DEFAULT_NUMBER_OF_RUN);
        int[] lastColunt          = dbCount.getDatabaseCount();
        int[] diff                = DictionarySizeCount.subtract(lastColunt, lastSuccessfulCount);
        int[] originalPlusDiff    = DictionarySizeCount.add(SAMPLE_DATA_COUNT, diff);
        purgeHistory();
        dbCount.checkDatabaseCount(originalPlusDiff);
        checkActivityData(currentDatas);
        return lastSuccessfulCount;
    }

    /**
    * failed then success
    * @throws DictionaryException
    * @throws HibernateException
    * @throws SQLException
    */
    public void testMixedUpdateFS() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(true);
    }

    public void testMixedUpdateSF() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(false);
    }

    public void testRandomUpdateSF() throws HibernateException, DictionaryException, SQLException {
        testInitialDataNoHistory();
        assertNotNull(currentDatas);

        int[] lastSuccessfulCount = testRandomSFUpdate(DEFAULT_NUMBER_OF_RUN * 3);
        int[] lastColunt          = dbCount.getDatabaseCount();
        int[] diff                = DictionarySizeCount.subtract(lastColunt, lastSuccessfulCount);
        int[] originalPlusDiff    = DictionarySizeCount.add(SAMPLE_DATA_COUNT, diff);
        purgeHistory();
        dbCount.checkDatabaseCount(originalPlusDiff);
        checkActivityData(currentDatas);
    }
    
    public void testMultiNew() throws DictionaryException, HibernateException, SQLException{
        final int numberOfRun = DEFAULT_NUMBER_OF_RUN;
        
        testInitialDataNoHistory();
        assertNotNull(currentDatas);
        for (int i = 0; i < numberOfRun; i++) {
            Collection<IElementBase> newDatas = createSampleData("new" + i + "_", "");
            currentDatas.addAll(newDatas);
            sync(newDatas);
            int j = 0;
            dbCount.dictElements         += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumGroupMembers += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumGroups       += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumMembers      += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumRefMembers   += SAMPLE_DATA_COUNT[j++];
            dbCount.dictLeafElements     += SAMPLE_DATA_COUNT[j++];
            dbCount.dictStructGroups     += SAMPLE_DATA_COUNT[j++];
            dbCount.checkDatabaseCount();
            
            checkActivityData(currentDatas);
        }
        purgeHistory();
        //nothing is change since everything is new
        dbCount.checkDatabaseCount();
    }
    
    public void testMultiInsertAndDelete() throws DictionaryException, HibernateException, SQLException{
        final int numberOfRun = DEFAULT_NUMBER_OF_RUN;
        
        testInitialDataNoHistory();
        assertNotNull(currentDatas);
        for (int i = 0; i < numberOfRun; i++) {
            Collection<IElementBase> newDatas = createSampleData(i + "_", "");
            sync(newDatas, currentDatas);
            currentDatas = newDatas;
            int j = 0;
            dbCount.dictElements         += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumGroupMembers += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumGroups       += SAMPLE_DATA_COUNT[j++];
            dbCount.dictEnumMembers      += SAMPLE_DATA_COUNT[j++];
            //the reference will be delete when inactive
            dbCount.dictEnumRefMembers   = SAMPLE_DATA_COUNT[j++];
            dbCount.dictLeafElements     += SAMPLE_DATA_COUNT[j++];
            dbCount.dictStructGroups     += SAMPLE_DATA_COUNT[j++];
            dbCount.checkDatabaseCount();
            
            checkActivityData(currentDatas);
        }
        purgeHistory();
        //nothing is change since everything is new
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
    }

    //TODO test more failed enrollment case
}
