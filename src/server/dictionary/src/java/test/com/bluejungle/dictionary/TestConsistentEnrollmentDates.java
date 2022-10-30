/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestConsistentEnrollmentDates.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;

import com.bluejungle.framework.utils.UnmodifiableDate;

import junit.framework.TestSuite;

public class TestConsistentEnrollmentDates extends AbstractDictionaryTest {

    private static Date beforeFirst;
    private static Date enrollFirst;
    private static Date enrollFirstFailed;
    private static Date reenrollFirst;
    private static Date enrollMany;
    private static Date enrollManyFailed;
    private static Date reenrollMany;

    public static TestSuite suite() {
        return new TestSuite(TestConsistentEnrollmentDates.class);
    }

    public void testSetupDictionary() throws Exception {
        init();
    }

    public void testConsistentDatesWithNoData() throws DictionaryException {
        // There are no enrollments, so there is no earliest consistent time
        assertNull(dictionary.getEarliestConsistentTimeSince(UnmodifiableDate.START_OF_TIME));
        // Passing null is the same as passing the beginning of time
        assertNull(dictionary.getEarliestConsistentTimeSince(null));
        // There are no enrollments, so there is no latest consistent time
        assertSame(dictionary.getLatestConsistentTime(), UnmodifiableDate.START_OF_TIME);
    }

    public void testSeedDataToOneDomain() throws DictionaryException {
        beforeFirst = new Date();
        updateEnrollment(enrollments[0], true);
        enrollFirst = new Date();
    }
    public void testSeedDataToOneDomainWasSuccessful() throws DictionaryException {
        assertEquals(1, dictionary.getUpdateRecords(null, null).size());
    }

    public void testConsistentDatesWithOneDomain() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   beforeFirst
        ,   enrollFirst
        );
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(UnmodifiableDate.START_OF_TIME)
        ,   beforeFirst
        ,   enrollFirst
        );
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(null)
        ,   beforeFirst
        ,   enrollFirst
        );
        assertEquals(
            dictionary.getLatestConsistentTime()
        ,   dictionary.getEarliestConsistentTimeSince(null)
        );
    }

    public void testSeedFailedEnrollmentOneDomain() throws DictionaryException {
        updateEnrollment(enrollments[0], false);
        enrollFirstFailed = new Date();
    }

    public void testFailedEnrollmentDoesNotChangeConsistentDatesOneDomain() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(null)
        ,   beforeFirst
        ,   enrollFirst
        );
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   beforeFirst
        ,   enrollFirst
        );
        assertEquals(
            dictionary.getLatestConsistentTime()
        ,   dictionary.getEarliestConsistentTimeSince(null)
        );
    }

    public void testSuccessfulReEnrollmentOneDomain() throws DictionaryException {
        updateEnrollment(enrollments[0], true);
        reenrollFirst = new Date();
    }

    public void testNewConsistentDatesOneDomain() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   enrollFirstFailed
        ,   reenrollFirst
        );
        assertTrue(
            dictionary.getLatestConsistentTime().after(
                dictionary.getEarliestConsistentTimeSince(null)
            )
        );
    }

    public void testEarliestConsistentDateOneDomain() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(enrollFirst)
        ,   enrollFirstFailed
        ,   reenrollFirst
        );
    }

    public void testSeedDataToRemainingDomains() throws DictionaryException, InterruptedException {
        Thread.sleep(100);
        updateEnrollment(enrollments[1], true);
        Thread.sleep(100);
        updateEnrollment(enrollments[2], true);
        Thread.sleep(100);
        updateEnrollment(enrollments[3], true);
        Thread.sleep(100);
        updateEnrollment(enrollments[4], true);
        Thread.sleep(100);
        enrollMany = new Date();
    }

    public void testSeedDataRemainingDomainsSuccessful() throws DictionaryException {
        assertEquals(7, dictionary.getUpdateRecords(null, null).size());
    }

    public void testEarliestConsistentDateWithMultipleDomains() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(enrollFirst)
        ,   enrollFirstFailed
        ,   reenrollFirst
        );
        Date d = reenrollFirst;
        int count = 0;
        while (d != null) {
            d = dictionary.getEarliestConsistentTimeSince(d);
            if (d != null) {
                count++;
            }
        }
        assertEquals(4, count);
    }

    public void testLatestConsistentDateWithMultipleDomains() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   reenrollFirst
        ,   enrollMany
        );
        assertTrue(
            dictionary.getLatestConsistentTime().after(
                dictionary.getEarliestConsistentTimeSince(reenrollFirst)
            )
        );
    }

    public void testSeedFailedEnrollmentMultipleDomains() throws DictionaryException {
        updateEnrollment(enrollments[2], false);
        enrollManyFailed = new Date();
    }

    public void testFailedEnrollmentDoesNotChangeEarliestConsistentDates() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(enrollFirst)
        ,   enrollFirstFailed
        ,   reenrollFirst
        );
        Date d = reenrollFirst;
        int count = 0;
        while (d != null) {
            d = dictionary.getEarliestConsistentTimeSince(d);
            if (d != null) {
                count++;
            }
        }
        assertEquals(4, count);
        assertNull(dictionary.getEarliestConsistentTimeSince(enrollManyFailed));
    }

    public void testFailedEnrollmentDoesNotChangeLatestConsistentDates() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   reenrollFirst
        ,   enrollMany
        );
        assertTrue(
            dictionary.getLatestConsistentTime().after(
                dictionary.getEarliestConsistentTimeSince(reenrollFirst)
            )
        );
        assertTrue(
            dictionary.getLatestConsistentTime().before(enrollManyFailed)
        );
    }

    public void testSuccessfulReEnrollmentAnotherDomain() throws DictionaryException {
        updateEnrollment(enrollments[4], true);
    }

    public void testSuccessfulEnrollmentAnotherDomainDoesNotChangeEarliestConsistentDates() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(enrollFirst)
        ,   enrollFirstFailed
        ,   reenrollFirst
        );
        Date d = reenrollFirst;
        int count = 0;
        while (d != null) {
            d = dictionary.getEarliestConsistentTimeSince(d);
            if (d != null) {
                count++;
            }
        }
        assertEquals(4, count);
        assertNull(dictionary.getEarliestConsistentTimeSince(enrollManyFailed));
    }

    public void testSuccessfulEnrollmentAnotherDomainDoesNotChangeLatestConsistentDates() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   reenrollFirst
        ,   enrollMany
        );
        assertTrue(
            dictionary.getLatestConsistentTime().after(
                dictionary.getEarliestConsistentTimeSince(reenrollFirst)
            )
        );
        assertTrue(
            dictionary.getLatestConsistentTime().before(enrollManyFailed)
        );
    }

    public void testSuccessfulReEnrollmentMultipleDomains() throws DictionaryException {
        updateEnrollment(enrollments[2], true);
        reenrollMany = new Date();
    }

    public void testNewLatestConsistentDatesMultipleDomains() throws DictionaryException {
        assertBetween(
            dictionary.getLatestConsistentTime()
        ,   enrollManyFailed
        ,   reenrollMany
        );
        assertTrue(
            dictionary.getLatestConsistentTime().after(enrollManyFailed)
        );
    }

    public void testEarliestConsistentDateMultipleDomains() throws DictionaryException {
        assertBetween(
            dictionary.getEarliestConsistentTimeSince(enrollManyFailed)
        ,   enrollManyFailed
        ,   reenrollMany
        );
        Date d = enrollManyFailed;
        int count = 0;
        while (d != null) {
            d = dictionary.getEarliestConsistentTimeSince(d);
            if (d != null) {
                count++;
            }
        }
        assertEquals(1, count);
    }
    
    public void testDeleteEnrollmentLastestConsistentDate() throws DictionaryException, InterruptedException {
    	Date lastestConsistentDateBeforeDelete = this.dictionary.getLatestConsistentTime();
    	assertNotNull( lastestConsistentDateBeforeDelete );
    	IConfigurationSession session = this.dictionary.createSession();
    	session.beginTransaction();
    	session.deleteEnrollment(enrollments[4]);
    	session.commit();
    	session.close();
    	assertTrue( this.dictionary.getEnrollment(enrollments[4].getDomainName()) == null );
    	Thread.sleep(1000);
    	Date lastestConsistentDateAfterDelete = this.dictionary.getLatestConsistentTime();
    	assertTrue( lastestConsistentDateBeforeDelete.before(lastestConsistentDateAfterDelete) );
    }

    private static void updateEnrollment(IEnrollment enrollment, boolean success ) throws DictionaryException {
        IEnrollmentSession session = enrollment.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(success, null);
        }
    }

    private static void assertBetween(Date date, Date from, Date to) {
        assertNotNull("Date is null", date);
        assertNotNull("Internal test error", from);
        assertNotNull("Internal test error", to);
        assertFalse("Internal test error", to.before(from));
        assertTrue(
            ""+date.getTime()+" is not between "+from.getTime()+" and "+to.getTime()
        ,   !date.before(from) && !date.after(to)
        );
    }

}
