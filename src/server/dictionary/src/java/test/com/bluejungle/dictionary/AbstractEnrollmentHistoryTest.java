/*
 * Created on Nov 11, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import net.sf.hibernate.HibernateException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/AbstractEnrollmentHistoryTest.java#1 $
 */

public abstract class AbstractEnrollmentHistoryTest extends AbstractDictionaryTest {
    protected static final String SEPERATOR_TOKEN = "|";
    
    /*
     * possible integer only, at least 1 
     */
    protected static final int DEFAULT_NUMBER_OF_RUN = 5;
    
    /*
     * this variable can be any negative integer
     */
    protected static final int FIRST_VERSION = -846783242;
    
    protected int[] SAMPLE_DATA_COUNT = new int[]{10, 3, 4, 1, 1, 5, 1};
    
    protected DictionarySizeCount dbCount;
    
    protected Collection<IElementBase> currentDatas;
    
    protected int currentVersion;
    
    protected boolean isSyncSuccess = true;
    
    @Override
    public void testSetupDictionary() throws Exception {
        //do nothing
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        init();
        dbCount = new DictionarySizeCount(dictionary);
        dbCount.checkDatabaseCount();
    }

    protected IMElement createElement(String name, IElementType type) {
        IMElement element = enrollments[0].makeNewElement(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                type, 
                new DictionaryKey(name.getBytes())
        );
        lazySetName(name, element);
        return element;
    }
    
    protected void lazySetName(String name, IMElementBase element){
        assert !name.contains("|");
        element.setDisplayName(name);
        element.setUniqueName(name);
    }
    
    protected Collection<IElementBase> createSampleData(String prefix, String suffix)
            throws DictionaryException {
        Collection<IElementBase> datas = new ArrayList<IElementBase>();
        
        datas.add(createElement(prefix + "lonelyApp" + suffix, appStruct.type));
        datas.add(createElement(prefix + "lonelyHost" + suffix, hostStruct.type));
        datas.add(createElement(prefix + "lonelyContact" + suffix, contactStruct.type));
        datas.add(createElement(prefix + "lonelyUser" + suffix, userStruct.type));
        
        IElementBase me = createElement(prefix + "me" + suffix, userStruct.type);
        datas.add(me);
        
        String name;
        
        name = prefix + "GroupEmpty" + suffix;
        IMGroup empty = enrollments[0].makeNewEnumeratedGroup(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                new DictionaryKey(name.getBytes()));
        lazySetName(name, empty);
        datas.add(empty);
        
        
        name = prefix + "GroupYou" + suffix;
        IMGroup you = enrollments[0].makeNewEnumeratedGroup(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                new DictionaryKey(name.getBytes()));
        lazySetName(name, you);
        datas.add(you);
        you.addChild(me);
        
        name = prefix + "GroupFather" + suffix;
        IMGroup father = enrollments[0].makeNewEnumeratedGroup(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                new DictionaryKey(name.getBytes()));
        lazySetName(name, father);
        datas.add(father);
        father.addChild(you);
        
        name = prefix + "GroupGrandpa" + suffix;
        IMGroup granda = enrollments[0].makeNewEnumeratedGroup(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                new DictionaryKey(name.getBytes()));
        lazySetName(name, granda);
        datas.add(granda);
        granda.addChild(father);
        
        name = prefix + "GroupStepAuntAunt" + suffix;
        IReferenceable veryFarRelative = enrollments[0].getProvisionalReferences(
                Collections.singleton(new DictionaryPath(new String[]{"ou=history", "cn=" + name}))
                ).iterator().next();
        granda.addChild(veryFarRelative);
        
        name = prefix + "House" + suffix;
        IMGroup house = enrollments[0].makeNewStructuralGroup(
                new DictionaryPath(new String[]{"ou=history", "cn=" + name}), 
                new DictionaryKey(name.getBytes()));
        lazySetName(name, house);
        datas.add(house);
        granda.addChild(father);
        
        return datas;
    }
    
    protected void checkActivityData(Collection<IElementBase> datas) throws DictionaryException{
        assertNotNull(datas);
        
        Date latestConsistentTime = dictionary.getLatestConsistentTime();
        for(IElementBase data : datas){
            IMElementBase dbo = dictionary.getByKey(data.getInternalKey(), latestConsistentTime);
            assertNotNull(dbo);
            assertElementEquals(data, dbo);
        }
    }
    
    protected void assertElementEquals(IElementBase expected, IElementBase actual) {
        if(isSyncSuccess()){
            assertEquals(expected.getDisplayName(), actual.getDisplayName());
            
        }else{
            
            String originalName = expected.getDisplayName();
            //depends {@link #updateAndSyncElements()}
            int i = originalName.indexOf(SEPERATOR_TOKEN);
            if (i > 0) {
                originalName = originalName.substring(0, i);
            }
            if(currentVersion != FIRST_VERSION){
                originalName += SEPERATOR_TOKEN + currentVersion;
            }
            
            assertEquals(originalName,              actual.getDisplayName());
            assertNotSame(expected.getDisplayName(), actual.getDisplayName());
        }
        
        assertEquals(expected.getUniqueName(),  actual.getUniqueName());
        assertEquals(expected.getExternalKey(), actual.getExternalKey());
        assertEquals(expected.getPath(),        actual.getPath());
        assertEquals(expected.getType(),        actual.getType());
    }
    
    protected void sync(Collection<IElementBase> inserts) throws DictionaryException {
        sync(inserts, Collections.EMPTY_SET);
    }

    protected void sync(Collection<IElementBase> inserts, Collection<IElementBase> deletes)
            throws DictionaryException {
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(inserts);
            session.deleteElements(deletes);
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(isSyncSuccess(), null);
        }
    }
    
    protected boolean isSyncSuccess(){
        return isSyncSuccess;
    }
    
    protected void updateAndSyncElements() throws DictionaryException, HibernateException,
            SQLException {
        updateAndSyncElements(FIRST_VERSION);
    }
    
    protected void updateAndSyncElements(int newVersion) throws DictionaryException, HibernateException,
            SQLException {
        update(currentDatas, newVersion);
        
        sync(currentDatas);
        
        dbCount.dictElements         += 10;
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups       += 4;
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 5;
        dbCount.dictStructGroups     += 1;
//        int i = 0;
//        dbCount.dictElements         += counts[i++];
//        dbCount.dictEnumGroupMembers += counts[i++];
//        dbCount.dictEnumGroups       += counts[i++];
//        dbCount.dictEnumMembers      += counts[i++];
//        dbCount.dictEnumRefMembers   += counts[i++];
//        dbCount.dictLeafElements     += counts[i++];
//        dbCount.dictStructGroups     += counts[i++];
        dbCount.checkDatabaseCount();
        
        if(isSyncSuccess()){
            currentVersion = newVersion;
        }
    }

    private void update(Collection<IElementBase> datas, int newVersion) {
        //update elements
        for(IElementBase base : datas){
            if (!(base instanceof IMElementBase)) {
                fail("unknown class" + base.getClass());
            }
            
            String name = base.getDisplayName();
            int i = name.lastIndexOf(SEPERATOR_TOKEN);
            if( i == -1){
                name = name + SEPERATOR_TOKEN + (newVersion >= 0 ? newVersion : 1);
            }else{
                int version;
                if(newVersion >= 0 ){
                    version = newVersion;
                }else {
                    if (i == name.length() - 1) {
                        //the last char
                        version = 1;
                    }else{
                        version = Integer.parseInt(name.substring(i + 1)) + 1;
                    }
                }
                
                name = name.substring(0, i + 1) + Integer.toString(version);
            }
            
            ((IMElementBase) base).setDisplayName(name);
        }
    }
    
    /**
     * before calling this method, the previous sync must be completed and success.
     */
    protected int[] testMixedSFUpdate(boolean isLastOneSuccess, int numberOfRun) throws DictionaryException, HibernateException, SQLException{
        //roll the number to even number
        if (numberOfRun != 0 && (numberOfRun % 2 != 0)) {
            numberOfRun++;
        }
        
        assertNotNull(currentDatas);
        int[] lastSuccessfulCount;
        lastSuccessfulCount = dbCount.getDatabaseCount();
        
        for (int i = 1; i <= numberOfRun; i++) {
            //if last one must be success, the last one must be even number
            isSyncSuccess = (i % 2 == 0) == isLastOneSuccess;
            updateAndSyncElements(i);
            checkActivityData(currentDatas);
            
            if(isSyncSuccess()){
                lastSuccessfulCount = dbCount.getDatabaseCount();
            }
        }
        return lastSuccessfulCount;
    }
    
    /**
     * before calling this method, the previous sync must be completed and success.
     */
    protected int[] testRandomSFUpdate(int numberOfRun) throws DictionaryException, HibernateException, SQLException{
        Random r = new Random();
        
        assertNotNull(currentDatas);
        int[] lastSuccessfulCount;
        lastSuccessfulCount = dbCount.getDatabaseCount();
        
        Collection<IElementBase> dataBeforeSync = new ArrayList<IElementBase>(currentDatas);
        for (int i = 1; i <= numberOfRun; i++) {
            isSyncSuccess = r.nextBoolean();
            updateAndSyncElements(i);
            checkActivityData(dataBeforeSync);
            
            if(isSyncSuccess()){
                lastSuccessfulCount = dbCount.getDatabaseCount();
            }
        }
        return lastSuccessfulCount;
    }

    protected void insertSample() throws DictionaryException, HibernateException, SQLException{
        assertNull(currentDatas);
        currentDatas = new ArrayList<IElementBase>();
        currentDatas.addAll(createSampleData("", ""));
        
        dbCount.checkDatabaseCount();
        
        sync(currentDatas);
        checkActivityData(currentDatas);
        currentVersion = FIRST_VERSION;
        
        int i = 0;
        dbCount.dictElements         = SAMPLE_DATA_COUNT[i++] = 10;
        dbCount.dictEnumGroupMembers = SAMPLE_DATA_COUNT[i++] = 3;
        dbCount.dictEnumGroups       = SAMPLE_DATA_COUNT[i++] = 4;
        dbCount.dictEnumMembers      = SAMPLE_DATA_COUNT[i++] = 1;
        dbCount.dictEnumRefMembers   = SAMPLE_DATA_COUNT[i++] = 1;
        dbCount.dictLeafElements     = SAMPLE_DATA_COUNT[i++] = 5;
        dbCount.dictStructGroups     = SAMPLE_DATA_COUNT[i++] = 1;
        dbCount.checkDatabaseCount();
    }
}
