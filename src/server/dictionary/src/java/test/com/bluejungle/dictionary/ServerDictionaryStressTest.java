/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/ServerDictionaryStressTest.java#1 $
 */

package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;

import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;

/**
 * This is a stress test suite for the Dictionary.
 */
public class ServerDictionaryStressTest extends TestCase implements ServerDictionaryStressTestConst {

    private final IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
    private IElementType userType;
    private IElementType hostType;
    private IElementType appType;

    private IElementField userPrincipal;
    private IElementField userEmail;
    private IElementField userDisplayName;
    private IElementField userFirstName;
    private IElementField userLastName;
    private IElementField userDob;
    private IElementField userSid;
    private IElementField hostDnsName;
    private IElementField hostSid;
    private IElementField appUniqueName;
    private IElementField appDisplayName;
    private IElementField appFP;
    private IElementField appId;

    private static final int SEED = 12321;
    private static final int NUM_USERS = 2000;
    private static final int NUM_HOSTS = 2000;
    private static final int NUM_APPS = 100;
    private static final int M_PCT = 65;
    private static final int COMMIT_CYCLE = 512;
    private static final String DOB_NAME = "dob";
    private static final int PRINT_OUT_EVERY_RECORDS = COMMIT_CYCLE;
    
    private final Random fnRand = new Random(SEED+1);
    private final Random lnRand = new Random(SEED+2);
    private final Random bdRand = new Random(SEED+5);
    private final Random byRand = new Random(SEED+6);
    private final Random keyRand = new Random(SEED+7);
    private final Random sidRand = new Random(SEED+8);
    private final Random srvRand = new Random(SEED+9);
    private final Random wsRand = new Random(SEED+10);
    private final Random gendRand = new Random(SEED+11);
    private final Random locRand = new Random(SEED+12);
    private final Random depRand = new Random(SEED+13);
    private int keyCount = 1;
    private int sidCount = 1;
    private final Set<String> usedUserNames = new HashSet<String>();
    private static final List<String> addedPrincipalNames = new ArrayList<String>(NUM_USERS);

    private final boolean[] usedServerNames = new boolean[serverNames.length];
    
    private final StringBuffer sb = new StringBuffer();

    public static TestSuite suite() throws Exception {
        DictionaryTestWithDataSource ds = new DictionaryTestWithDataSource("Dictionary Stress Tests"){};
        ds.setUp();
        return new TestSuite(ServerDictionaryStressTest.class);
    }

    public void setUp() throws DictionaryException {
        userType = 			dictionary.getType(ElementTypeEnumType.USER.getName());
        hostType = 			dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        appType = 			dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        userPrincipal = 	userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
        userEmail = 		userType.getField(UserReservedFieldEnumType.MAIL.getName());
        userDisplayName = 	userType.getField(UserReservedFieldEnumType.DISPLAY_NAME.getName());
        userFirstName = 	userType.getField(UserReservedFieldEnumType.FIRST_NAME.getName());
        userLastName = 		userType.getField(UserReservedFieldEnumType.LAST_NAME.getName());
        userSid = 			userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());
        hostDnsName = 		hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        hostSid = 			hostType.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName());
        appUniqueName = 	appType.getField(ApplicationReservedFieldEnumType.UNIQUE_NAME.getName());
        appDisplayName = 	appType.getField(ApplicationReservedFieldEnumType.DISPLAY_NAME.getName());
        appFP =				appType.getField(ApplicationReservedFieldEnumType.APP_FINGER_PRINT.getName());
        appId = 			appType.getField(ApplicationReservedFieldEnumType.SYSTEM_REFERENCE.getName());
        try {
            userDob = userType.getField(DOB_NAME);
        } catch (IllegalArgumentException expectedBeforeSetup) {
        }
        
    }
    
    public void testSetupSucceeded() {
        assertNotNull(userType);
        assertNotNull(hostType);
        assertNotNull(appType);
        assertNotNull(userEmail);
        assertNotNull(userPrincipal);
        assertNotNull(userDisplayName);
        assertNotNull(userFirstName);
        assertNotNull(userLastName);
        assertNotNull(userSid);
        assertNotNull(hostDnsName);
        assertNotNull(hostSid);
        assertNotNull(appUniqueName);
        assertNotNull(appDisplayName);
        assertNotNull(appFP);
        assertNotNull(appId);
    }
    
    public void testCleanup() throws SQLException, DictionaryException, HibernateException {
    	DictionaryTestHelper.clearDictionary(dictionary);
    }

    public void testCreateDomains() throws DictionaryException {
        IConfigurationSession cs = dictionary.createSession();
        try {
            cs.beginTransaction();
            try {
                userType.getField(DOB_NAME);
            } catch (IllegalArgumentException iae) {
                IMElementType u = dictionary.getType(userType.getName());
                u.addField(DOB_NAME, ElementFieldType.DATE);
                cs.saveType(u);
            }
            for (int i = 0 ; i != domainNames.length ; i++) {
                IEnrollment enrollment = dictionary.makeNewEnrollment(domainNames[i]);
                enrollment.setType("Active Directory");
                enrollment.setIsRecurring(true);
                cs.saveEnrollment(enrollment);
            }
            cs.commit();
        } catch (DictionaryException de) {
            cs.rollback();
            fail(de.getMessage());
        } finally {
            cs.close();
        }
    }

    public void testCreateDomainsSucceeded() throws DictionaryException {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertNotNull(enrollments);
        assertEquals(domainNames.length, enrollments.size());
        for (IEnrollment e : enrollments) {
			assertNotNull(e);
			assertTrue(Arrays.asList(domainNames).contains(e.getDomainName()));
		}
    }

    public void testAddUsers() throws DictionaryException {
        int commitCycleCount = 0;
        int domainIndex = 0;
        IEnrollment enrollment = null;
        IEnrollmentSession session = null;
        String[] path = null;
        Date start = new Date();
        System.err.println("Starting to load the users: "+start);
        try {
            List<IElementBase> users = new ArrayList<IElementBase>(NUM_USERS/COMMIT_CYCLE+100);
            for ( int i = 0 ; i != NUM_USERS ; i++ ) {
                if (enrollment == null) {
                    if (session != null) {
                        session.commit();
                        save(session, users, i, start.getTime());
                        session = null;
                    }
                    enrollment = dictionary.getEnrollment(domainNames[domainIndex]);
                    session = enrollment.createSession();
                    session.beginTransaction();
                    String[] tt = domainNames[domainIndex].split("[.]");
                    path = new String[tt.length+3];
                    for ( int j = 0 ; j != tt.length ; j++) {
                        path[j] = tt[tt.length-j-1];
                    }
                    path[tt.length] = locations[locRand.nextInt(locations.length)];
                    path[tt.length+1] = departments[depRand.nextInt(departments.length)];
                }
                if (users.size() >= COMMIT_CYCLE) {
                    save(session, users, i, start.getTime());
                    session.beginTransaction();
                    commitCycleCount = 0;
                }
                int ln = nextLastName();
                String fn;
                int gender = gendRand.nextInt(100);
                if (gender > M_PCT) {
                    fn = firstNamesF[nextFirstNameF()];
                } else {
                    fn = firstNamesM[nextFirstNameM()];
                }
                String email = makeEmail(fn, ln, domainIndex);
                path[path.length-1] = email;
                IMElement user = enrollment.makeNewElement(
                    new DictionaryPath(path)
                ,   userType
                ,   new DictionaryKey(nextKey())
                );
                user.setDisplayName(fn+" "+lastNames[ln]);
                user.setUniqueName(email);
                user.setValue(userFirstName, fn);
                user.setValue(userLastName, lastNames[ln]);
                int age = (int)( byRand.nextGaussian() * 12 + 30 );
                age = Math.max(age, 18);
                age = Math.min(age, 72);
                Calendar cal = Calendar.getInstance();
                cal.add( Calendar.YEAR, -age );
                cal.set( Calendar.DAY_OF_YEAR, bdRand.nextInt( cal.getMaximum( Calendar.DAY_OF_YEAR ) ) );
                user.setValue(userDob, cal.getTime());
                user.setValue(userSid, nextSid());
                user.setValue(userDisplayName, user.getDisplayName());
                user.setValue(userPrincipal, email);
                addedPrincipalNames.add(email);
                user.setValue(userEmail, new String[] { email });
                users.add(user);
                if (i > domainFreq[domainIndex]*NUM_USERS && domainIndex != domainNames.length-1) {
                    enrollment = null;
                    domainIndex++;
                    usedUserNames.clear();
                }
//                if ((i + 1) % (NUM_USERS / 20) == 0) {
//                    Date now = new Date();
//                    long diff = now.getTime()-start.getTime();
//                    System.err.println("Loaded " + (i + 1) + " records in " + diff / 1000
//							+ " seconds, average " + (float)(i + 1) / diff * 1000 + " users/sec");
//				}
                

//				if ((i + 1) % PRINT_OUT_EVERY_RECORDS == 0) {
//					long diff = System.currentTimeMillis() - start.getTime();
//					System.out.println((i + 1) + "\t" + diff);
//				}
            }
            save(session, users, NUM_USERS, start.getTime());
            session = null;
        } finally {
            Date end = new Date();
            long diff = (end.getTime()-start.getTime())/1000;
            System.err.println("Finished loading the users: "+end+", "+diff+" seconds.");
            if (session != null) {
                session.close(false, null);
            }
        }
        
        System.out.println();
        System.out.println("summary");
        System.out.println(sb.toString());
    }

    private void save(IEnrollmentSession session, List<IElementBase> users, int i, long startTime)
            throws DictionaryException {
        session.saveElements(users);
        session.commit();
        users.clear();
        
        long diff = System.currentTimeMillis() - startTime;
        System.out.println("W " + i + "\t" + diff);
        sb.append(i + "\t" + diff + "\n");
    }
    
    public void testReadUsers() throws DictionaryException {
    	Collections.shuffle(addedPrincipalNames);
    	long startTime = System.currentTimeMillis();
    	int i = 0;
    	IDictionaryIterator<IMElement> user = null;
    	try {
	    	for (String name : addedPrincipalNames) {
				user = this.dictionary.query(new Relation(RelationOp.EQUALS, userPrincipal,
								Constant.build(name)), new Date(), null, null);
				
					assertTrue(user.hasNext());
				
				if ((++i) % PRINT_OUT_EVERY_RECORDS == 0) {
					long diff = System.currentTimeMillis() - startTime;
					System.out.println(i + "\t" + diff);
					sb.append(i + "\t" + diff + "\n");
				}
				user.close();
			}
    	} finally {
    		if (user != null) {
				user.close();
			}
		}
    	
    	long diff = System.currentTimeMillis() - startTime;
    	
    	System.out.println("Searched " + addedPrincipalNames.size() + " principle names took "
				+ diff / 1000 + " seconds");
    	
    	 System.out.println();
         System.out.println("summary");
         System.out.println(sb.toString());
    }



    static {
        addFrequencies(firstNameFreqM);
        addFrequencies(firstNameFreqF);
        addFrequencies(lastNameFreq);
        addFrequencies(domainFreq);
    }

    private static void addFrequencies( double[] a ) {
        for ( int i = 1 ; i < a.length ; i++ ) {
            a[i] += a[i - 1];
        }
    }

    private int nextLastName() {
        return nextDistributed(lnRand, lastNameFreq);
    }

    private int nextFirstNameM() {
        return nextDistributed(fnRand, firstNameFreqM);
    }

    private int nextFirstNameF() {
        return nextDistributed(fnRand, firstNameFreqF);
    }

    private String makeEmail(String fn, int ln, int dn) {
        int count = 0;
        do {
            StringBuffer res = new StringBuffer(128);
            res.append(fn);
            res.append('.');
            res.append(lastNames[ln]);
            if (count != 0) {
                res.append(count+1);
            }
            count++;
            res.append('@');
            res.append(domainNames[dn]);
            String name = res.toString();
            if (!usedUserNames.contains(name)) {
                usedUserNames.add(name);
                return name;
            }
        } while (true);
    }

    private static int nextDistributed( Random rnd, double[] f ) {
        int lo = 0, hi = f.length;
        double x = rnd.nextDouble() * f[hi - 1];
        while ( lo + 1 < hi ) {
            int mid = ( hi + lo ) / 2;
            if ( x < f[mid] ) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        if ( x < f[lo] ) {
            return lo;
        } else {
            return hi;
        }
    }

    private byte[] nextKey() {
        byte[] res = new byte[16];
        keyRand.nextBytes( res );
        intToBytes( keyCount++, res, 12 );
        return res;
    }

    private String nextSid() {
        byte[] sid = new byte[16];
        sidRand.nextBytes( sid );
        sid[0] = 1;
        sid[7] = 2;
        intToBytes( sidCount++, sid, 8 );
        return sidToString( sid );
    }

    private static void intToBytes( int n, byte[] b, int p ) {
        for ( int i = p + 3 ; i >= p ; i-- ) {
            b[i] = (byte)( n & 0xFF );
            n >>= 8;
        }
    }

    private static String sidToString( byte[] sid ) {
        if ( sid == null ) {
            throw new NullPointerException( "sid" );
        }
        if ( sid.length < 8 ) {
            throw new IllegalArgumentException( "sid" );
        }
        StringBuffer res = new StringBuffer( 128 );
        res.append( "S-" );
        res.append( sid[0] );
        res.append( '-' );
        res.append( getLong( sid, 1, 6 ) );
        for ( int i = 0 ; i != sid[7] ; i++ ) {
            res.append( '-' );
            res.append( getLong( sid, 8 + i * 4, 4 ) );
        }
        return res.toString();
    }

    private static long getLong( byte[] s, int n, int c ) {
        assert s != null;
        if ( s.length < n + c ) {
            throw new IllegalArgumentException( "sid" );
        }
        long res = 0;
        while ( c-- != 0 ) {
            res <<= 8;
            res += s[n + c] & 0xFF;
        }
        return res;
    }

}
