/*
 * Created on Nov 11, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import junit.framework.TestSuite;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnrollmentRollback.java#1 $
 */

public class TestEnrollmentRollback extends AbstractEnrollmentHistoryTest {
    
    
    public static TestSuite suite() {
        return new TestSuite(TestEnrollmentRollback.class);
    }
    
    private Map<String, Integer> rollbackFailed() throws DictionaryException {
        Map<String, Integer> result;
        IConfigurationSession configSession = dictionary.createSession();
        configSession.beginTransaction();
        result = configSession.rollbackLatestFailedEnrollment(enrollments[0]);
        configSession.close();
        return result;
    }
    
    @Override
    protected boolean isSyncSuccess() {
        return isSyncSuccess;
    }
    
    
    
    
    
    public void testNeverSyncEnrollment() throws DictionaryException {
        Map<String, Integer> result = rollbackFailed();
        assertTrue(result.isEmpty());
    }
    
    public void testNoFail() throws DictionaryException, HibernateException, SQLException{
        insertSample();
        
        Map<String, Integer> result = rollbackFailed();
        assertTrue(result.isEmpty());
    }
    
    public void testSimpleFail() throws DictionaryException, HibernateException, SQLException{
        testNoFail();
        
        assertNotNull(currentDatas);
        isSyncSuccess = false;
        updateAndSyncElements();
        checkActivityData(currentDatas);
        rollbackFailed();
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
        
        checkActivityData(currentDatas);
    }
    
    public void testMultiFail() throws DictionaryException, HibernateException, SQLException{
        final int numberOfRun = DEFAULT_NUMBER_OF_RUN;
        
        testNoFail();
        
        assertNotNull(currentDatas);
        
        isSyncSuccess = false;
        
        for (int i = 0; i < numberOfRun; i++) {
            updateAndSyncElements();
            checkActivityData(currentDatas);
        }
        rollbackFailed();
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
        checkActivityData(currentDatas);
    }
    
    protected int[] testMixedSFUpdate(boolean isLastOneSuccess) throws DictionaryException,
            HibernateException, SQLException {
        testNoFail();
        assertNotNull(currentDatas);
        
        int[] lastSuccessfulCount = super.testMixedSFUpdate(isLastOneSuccess, DEFAULT_NUMBER_OF_RUN);
        rollbackFailed();
        dbCount.checkDatabaseCount(lastSuccessfulCount);
        checkActivityData(currentDatas);
        return lastSuccessfulCount;
    }

    /**
     * failed then success
     * @throws DictionaryException
     * @throws HibernateException
     * @throws SQLException
     */
    public void testMixedUpdateFS() throws DictionaryException, HibernateException, SQLException{
        this.testMixedSFUpdate(true);
    }
    
    public void testMixedUpdateSF() throws DictionaryException, HibernateException, SQLException{
        this.testMixedSFUpdate(false);
    }
    
    public void testRandomUpdateSF() throws HibernateException, DictionaryException, SQLException{
        testNoFail();
        assertNotNull(currentDatas);
        
        int[] lastSuccessfulCount = testRandomSFUpdate(DEFAULT_NUMBER_OF_RUN * 3);
        rollbackFailed();
        dbCount.checkDatabaseCount(lastSuccessfulCount);
        checkActivityData(currentDatas);
    }
    
}