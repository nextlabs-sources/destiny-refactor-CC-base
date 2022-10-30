/*
 * Created on Nov 13, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import junit.framework.TestSuite;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnrollmentPurge.java#1 $
 */

public class TestEnrollmentPurge extends AbstractEnrollmentHistoryTest {
    public static TestSuite suite() {
        return new TestSuite(TestEnrollmentPurge.class);
    }
    
    private Map<String, Integer> purgeHistory() throws DictionaryException {
        Date date = dictionary.getLatestConsistentTime();
        
        Map<String, Integer> result;
        IConfigurationSession configSession = dictionary.createSession();
        configSession.beginTransaction();
        result = configSession.purgeHistory(enrollments[0], date, null);
        configSession.close();
        return result;
    }
    
    private Map<String, Integer> rollbackFailed() throws DictionaryException {
        Map<String, Integer> result;
        IConfigurationSession configSession = dictionary.createSession();
        configSession.beginTransaction();
        result = configSession.rollbackLatestFailedEnrollment(enrollments[0]);
        configSession.close();
        return result;
    }
    
    private void purgeAndCheck(boolean isRollbackFirst) throws DictionaryException,
            HibernateException, SQLException {
     if (isRollbackFirst) {
            rollbackFailed();
            purgeHistory();
        } else {
            purgeHistory();
            rollbackFailed();
        }
        
        dbCount.checkDatabaseCount(SAMPLE_DATA_COUNT);
        checkActivityData(currentDatas);
    }
    

    
    
    
    protected int[] testMixedSFUpdate(boolean isLastOneSuccess, boolean isRollbackFirst)
            throws DictionaryException, HibernateException, SQLException {
      insertSample();
        assertNotNull(currentDatas);
        
        int[] lastSuccessfulCount =
                super.testMixedSFUpdate(isLastOneSuccess, DEFAULT_NUMBER_OF_RUN);
        purgeAndCheck(isRollbackFirst);
        
        return lastSuccessfulCount;
    }
    
    public void testMixedUpdateFS() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(true, true);
    }
    
    public void testMixedUpdateFS2() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(true, false);
    }

    public void testMixedUpdateSF() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(false, true);
    }
    
    public void testMixedUpdateSF2() throws DictionaryException, HibernateException, SQLException {
        this.testMixedSFUpdate(false, false);
    }
    
    protected void testRandomUpdateSF(boolean isRollbackFirst) throws HibernateException,
            DictionaryException, SQLException {
        insertSample();
        assertNotNull(currentDatas);

        testRandomSFUpdate(DEFAULT_NUMBER_OF_RUN * 3);
        
        purgeAndCheck(isRollbackFirst);
        checkActivityData(currentDatas);
    }

    public void testRandomUpdateSF() throws HibernateException, DictionaryException, SQLException {
        testRandomUpdateSF(true);
    }
    
    public void testRandomUpdateSF2() throws HibernateException, DictionaryException, SQLException {
        testRandomUpdateSF(false);
    }
    
    
}
