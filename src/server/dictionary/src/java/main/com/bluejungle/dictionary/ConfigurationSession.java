package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey, atian
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ConfigurationSession.java#1 $
 */

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

/**
 * This class implements the dictionary configuration session.
 */
class ConfigurationSession extends DictionarySession implements IConfigurationSession {
    private static final Log LOG = LogFactory.getLog(ConfigurationSession.class);
    
    /**
     * Creates a new configuration session.
     *
     * @param session the Hibernate <code>Session</code> on top of which
     * this <code>ConfigurationSession</code> is created.
     */
    public ConfigurationSession(Dictionary dictionary, Session session) {
        super(dictionary, session);
    }

    /**
     * @see IConfigurationSession#saveType(IMElementType)
     */
    public void saveType( IMElementType type ) throws DictionaryException {
        checkNull( type, "type" );
        checkActiveTransaction();
        try {
            getSession().saveOrUpdate(type);
        } catch ( HibernateException cause ) {
            rollback();
            throw new DictionaryException(cause);
        }
    }

    /**
     * @see IConfigurationSession#deleteType(IMElementType)
     */
    public void deleteType( IMElementType type ) throws DictionaryException {
        checkNull( type, "type" );
        checkActiveTransaction();
        try {
            getSession().delete(type);
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }

    /**
     * @see IConfigurationSession#saveEnrollment(IEnrollment)
     */
    public void saveEnrollment( IEnrollment enrollment ) throws DictionaryException {
        checkNull( enrollment, "enrollment" );
        checkActiveTransaction();
        try {
            getSession().saveOrUpdate(enrollment);
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }

    /**
     * @see IConfigurationSession#deleteEnrollment(IEnrollment)
     */
    public void deleteEnrollment( IEnrollment enrollment ) throws DictionaryException {
        checkNull( enrollment, "enrollment" );
        checkActiveTransaction();
        Date endTime = new Date();
        try {
            Session hs = getSession();
            enrollment.setIsActive(false);
            enrollment.clearAllExternalNames();
            enrollment.deleteAllProperties();
            hs.update(enrollment);
            
            // close all Update Records associated with enrollment
            List<UpdateRecord> res = hs.createQuery(
                    "from UpdateRecord b "
                +   "where "
                +   "b.timeRelation.activeTo > :asOf "
                +   "and b.timeRelation.activeFrom <= :asOf "
                +   "and b.enrollment = :enrollment"
                )
                .setParameter("asOf", endTime, DateToLongUserType.TYPE)
                .setParameter("enrollment", enrollment, Hibernate.entity(Enrollment.class))
                .list();
            try {
                for ( UpdateRecord updateRecord : res ) {
            		updateRecord.setEndTime(endTime);
                    updateRecord.timeRelation = updateRecord.timeRelation.close(endTime);
            		hs.update(updateRecord);
            	}
            } finally {
            	hs.flush();
            }

            // closing elements belonging to this enrollment          
            updateElementEndTime(
                Collections.singleton(((Enrollment)enrollment).id)
            ,   endTime
            ); 
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }
    
    private Map<String, Integer> deleteEnrollmentPast(Collection<Long> enrollmentHistory, long enrollmentId,
            AtomicBoolean isInterrupted) throws HibernateException, SQLException, DictionaryException {
        final String indirectDeleteQueryTemplate = "delete from %1$s where %2$s" 
            + " in ( select id from DICT_ELEMENTS" +
                     " where ACTIVE_TO <= %3$d"
            +          " and ENROLLMENT_ID = " + enrollmentId 
            +    " )";

        final String directDeleteQueryTemplate = "delete from %1$s" 
            + " where ACTIVE_TO <= %2$d" 
            +   " and ENROLLMENT_ID = " + enrollmentId;
        
        LinkedList<Long> fromOldToNew = new LinkedList<Long>(enrollmentHistory);
        Collections.sort(fromOldToNew);
        
        return clearHistory(fromOldToNew, indirectDeleteQueryTemplate,
                directDeleteQueryTemplate, isInterrupted, false);
    }
    
    private Map<String, Integer> deleteEnrollmentFuture(Collection<Long> enrollmentHistory, long enrollmentId,
            AtomicBoolean isInterrupted) throws HibernateException, SQLException, DictionaryException {
        final String indirectDeleteQueryTemplate = "delete from %1$s where %2$s" 
            + " in ( select id from DICT_ELEMENTS" +
                     " where ACTIVE_FROM >= %3$d"
            +          " and ENROLLMENT_ID = " + enrollmentId 
            +    " )";

        final String directDeleteQueryTemplate = "delete from %1$s" 
            + " where ACTIVE_FROM >= %2$d" 
            +   " and ENROLLMENT_ID = " + enrollmentId;
        
        LinkedList<Long> fromNewToOld = new LinkedList<Long>(enrollmentHistory);
        Collections.sort(fromNewToOld, new Comparator<Long>() {
            public int compare(Long o1, Long o2) {
                return o2.compareTo(o1);
            }
        });
        return clearHistory(fromNewToOld, indirectDeleteQueryTemplate,
                directDeleteQueryTemplate, isInterrupted, true);
    }
    
    private Map<String, Integer> clearHistory(
            Queue<Long> enrollmentHistory, 
            String indirectDeleteQueryTemplate, 
            String directDeleteQueryTemplate,
            AtomicBoolean isInterrupted,
            boolean isFromNewToOld) throws HibernateException, SQLException,
            DictionaryException {
        checkActiveTransaction();
        
        final int numberOfSql = 6;
        
        int[] totalCount = new int[numberOfSql];
        
        for (long activeTo : enrollmentHistory) {
            if(isInterrupted.get()){
                LOG.info("The clear task is interrupted.");
                break;
            }
            
            LOG.info("start cleanup anything " + (isFromNewToOld ? "newer" : "older") +" than " + toString(new Date(activeTo)));

            long batchStartTime = System.currentTimeMillis();
            Statement s = null; 
            boolean isSuccess = false;
            try {
                s = getSession().connection().createStatement();
                s.addBatch(String.format(indirectDeleteQueryTemplate, 
                        "DICT_LEAF_ELEMENTS", "ELEMENT_ID", activeTo));
                s.addBatch(String.format(indirectDeleteQueryTemplate, 
                        "DICT_ENUM_GROUPS", "ELEMENT_ID", activeTo));
                s.addBatch(String.format(indirectDeleteQueryTemplate, 
                        "DICT_STRUCT_GROUPS", "ELEMENT_ID", activeTo));
//                s.addBatch(String.format(indirectDeleteQueryTemplate, 
//                        "DICT_ENUM_REF_MEMBERS", "GROUP_ID", activeTo));
                
                s.addBatch(String.format(directDeleteQueryTemplate, 
                        "DICT_ENUM_GROUP_MEMBERS", activeTo));
                s.addBatch(String.format(directDeleteQueryTemplate, 
                        "DICT_ENUM_MEMBERS", activeTo));
                s.addBatch(String.format(directDeleteQueryTemplate, 
                        "DICT_ELEMENTS", activeTo));
                
                int[] results = s.executeBatch();
                assert results.length == numberOfSql;
                LOG.info("Deleted " + ArrayUtils.asString(ArrayUtils.toInt(results), ", "));
                
                for (int i = 0; i < numberOfSql; i++) {
                    totalCount[i] += results[i];
                }
                
                isSuccess = true;
            } finally {
                if (s != null) {
                    s.close();
                }

                if (isSuccess) {
                    commit();
                } else {
                    rollback();
                }
            }
            beginTransaction();
            LOG.info("clear up dictionary " + (isFromNewToOld ? "newer" : "older") + " than "
                    + activeTo + " took " + (System.currentTimeMillis() - batchStartTime) + " ms");
        }
        
        Map<String, Integer> totalCountMap = new HashMap<String, Integer>(7);
        int i = 0;
        totalCountMap.put("delete DICT_LEAF_ELEMENTS",      totalCount[i++]);
        totalCountMap.put("delete DICT_ENUM_GROUPS",        totalCount[i++]);
        totalCountMap.put("delete DICT_STRUCT_GROUPS",      totalCount[i++]);
//        totalCountMap.put("delete DICT_ENUM_REF_MEMBERS",   totalCount[i++]);
        totalCountMap.put("delete DICT_ENUM_GROUP_MEMBERS", totalCount[i++]);
        totalCountMap.put("delete DICT_ENUM_MEMBERS",       totalCount[i++]);
        totalCountMap.put("delete DICT_ELEMENTS",           totalCount[i++]);
        
        return totalCountMap;
    }
    
    public Map<String, Integer> purgeHistory(IEnrollment enrollment, Date clearBeforeDate,
            AtomicBoolean isInterrupted) throws DictionaryException, IllegalArgumentException {
        //some basic checking
        checkNull(enrollment, "enrollment");
        checkNull(clearBeforeDate, "clearBeforeDate");
        checkActiveTransaction();
        
        Map<String, Integer> totalCountMap = new HashMap<String, Integer>();
        if (isInterrupted == null) {
            //default is false
            isInterrupted = new AtomicBoolean();
        } else if (isInterrupted.get()) {
            //interrupted very very early! Is this normal?
            return totalCountMap;
        }
        
        IUpdateRecord updateRecord = enrollment.getStatus();
        if (updateRecord == null) {
            return totalCountMap;
        }
        if (updateRecord.getEndTime().before(clearBeforeDate) && enrollment.getIsActive()) {
            throw new IllegalArgumentException(
                    "The clear Date can't on or after the last enrollment sync time.");
        }
        Date latestConsistentTime = dictionary.getLatestConsistentTime();
        if(dictionary.getLatestConsistentTime().before(clearBeforeDate)){
            throw new IllegalArgumentException("The clear Date can't later than the latestConsistentTime, "
                            + toString(latestConsistentTime) + ".");
        }
        
        
        /*
         * don't clear everything in a single transaction
         * The safer way to do it in a batch. And the smallest batch is the size of each enrollment.
         * This will make sure the dictionary is still in consistency stage.
         */
        
        final Queue<Long> enrollmentHistory;
        
        try{
            List<UpdateRecord> res = getSession().createQuery(
                    "from UpdateRecord b"
                +   " where"
                +   " b.startTime < :asOf"
                +   " and b.enrollment = :enrollment"
                +   " order by b.startTime asc"
            )
            .setParameter("asOf", clearBeforeDate, DateToLongUserType.TYPE)
            .setParameter("enrollment", enrollment, Hibernate.entity(Enrollment.class))
            .list();
            
            enrollmentHistory = new LinkedList<Long>();
            for(UpdateRecord r : res){
                long time = r.getStartTime().getTime();
                assert time != clearBeforeDate.getTime();
                enrollmentHistory.add(time);
            }
        }catch(HibernateException cause){
            throw new DictionaryException("Fail to get enrollment history for enrollment " 
                    + enrollment.getDomainName(), cause);
        }
        
        if (enrollmentHistory.isEmpty()) {
            LOG.info("Nothing to remove before " + toString(clearBeforeDate) + " for enrollment \""
                    + Enrollment.filterDomainName(enrollment.getDomainName()) + "\"");
            return totalCountMap;
        }
        
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("find ").append(enrollmentHistory.size()).append(" enrollment recoreds before ")
              .append(toString(clearBeforeDate)).append(".")
              .append("\n");

            for (Long l : enrollmentHistory) {
                sb.append("\t").append(l).append(", ").append(new Date(l)).append("\n");
            }
            
            LOG.info(sb.toString());
        }
        final long enrollmentId = ((Enrollment)enrollment).id;
        try {
            totalCountMap = deleteEnrollmentPast(enrollmentHistory, enrollmentId, isInterrupted);
        } catch (SQLException e) {
            throw new DictionaryException(e);
        } catch (HibernateException e) {
            throw new DictionaryException(e);
        }
        
        return totalCountMap;
    }
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
    
    private String toString(Date date){
        return DATE_FORMAT.format(date) + " (" + date.getTime() + ")";
    }
    
    
    public Map<String, Integer> rollbackLatestFailedEnrollment(IEnrollment enrollment) throws DictionaryException{
        checkNull(enrollment, "enrollment");
        checkActiveTransaction();
        
        if (!enrollment.getIsActive()) {
            throw new DictionaryException("The enrollment is not active.");
        }
        
        Map<String, Integer> totalCountMap = new HashMap<String, Integer>();
        Date latestTime = dictionary.getLatestConsistentTime();
        
        final Queue<Long> enrollmentHistory;
       
        //delete all future record first
        try{
            List<UpdateRecord> res = getSession().createQuery(
                    "from UpdateRecord b"
                +   " where"
                +   " b.timeRelation.activeTo > :asOf"
                +   " and b.enrollment = :enrollment"
                +   " order by b.timeRelation.activeTo desc"
            )
            .setParameter("asOf", latestTime, DateToLongUserType.TYPE)
            .setParameter("enrollment", enrollment, Hibernate.entity(Enrollment.class))
            .list();
            
            enrollmentHistory = new LinkedList<Long>();
            for(UpdateRecord r : res){
                if(r.isSuccessful()){
                    break;
                }
                enrollmentHistory.add(r.getStartTime().getTime());
            }
        }catch(HibernateException cause){
            throw new DictionaryException("Fail to get enrollment history for enrollment " 
                    + enrollment.getDomainName(), cause);
        }
        
        if (enrollmentHistory.isEmpty()) {
            LOG.info("Nothing to rollback for enrollment \"" + Enrollment.filterDomainName(enrollment.getDomainName()) + "\"");
            return totalCountMap;
        }
        
        final long enrollmentId = ((Enrollment)enrollment).id;
        AtomicBoolean isInterrupted = new AtomicBoolean();
        try {
            //TODO
            totalCountMap = deleteEnrollmentFuture(enrollmentHistory, enrollmentId, isInterrupted);
        } catch (SQLException e) {
            throw new DictionaryException(e);
        } catch (HibernateException e) {
            throw new DictionaryException(e);
        }
        
        final String updateTemplate = 
                "UPDATE %s"
        	  + " SET ACTIVE_TO = " + UnmodifiableDate.END_OF_TIME.getTime() 
        	  + " WHERE ENROLLMENT_ID = " + enrollmentId
        	  + " and ACTIVE_TO >= " + latestTime.getTime();
        
        String[] tableNames = new String[]{
                "DICT_ENUM_GROUP_MEMBERS",
                "DICT_ENUM_MEMBERS",
                "DICT_ELEMENTS"
        };
        
        try{
            for(String tableName : tableNames){
                Statement s = null; 
                boolean isSuccess = false;
                try {
                    s = getSession().connection().createStatement();
                    int result = s.executeUpdate(String.format(updateTemplate, tableName));
                    totalCountMap.put("update " + tableName, result);
                    isSuccess = true;
                } finally {
                    if (s != null) {
                        s.close();
                    }
        
                    if (isSuccess) {
                        commit();
                    } else {
                        rollback();
                    }
                }
                beginTransaction();
            }
        } catch (SQLException e) {
            throw new DictionaryException(e);
        } catch (HibernateException e) {
            throw new DictionaryException(e);
        }
        
        return totalCountMap;
    }

    /**
     * This method updates the active-to time on multiple
     * dictionary element records (groups or leaf elements).
     * @param ids a <code>Collection</code> of IDs of records to be updated.
     * @param date the as-of date to close the records.
     * @throws HibernateException if the operation cannot complete.
     */
    private void updateElementEndTime(Collection<Long> ids, Date date) throws HibernateException {
    	if (ids == null || ids.isEmpty()) {
    		return;
    	}
    	
        checkActiveTransaction();
        MassDML.updateOrDelete(
            getSession()
        ,   "UPDATE DICT_ELEMENTS SET ACTIVE_TO=? WHERE ENROLLMENT_ID IN #"
        ,   new Object[] {date}
        ,   new Type[] {DateToLongUserType.TYPE}
        ,   ids
        ,   Hibernate.LONG);
        MassDML.updateOrDelete(
            getSession()
        ,   "UPDATE DICT_ENUM_MEMBERS SET ACTIVE_TO=? WHERE ENROLLMENT_ID IN #"
        ,   new Object[] {date}
        ,   new Type[] {DateToLongUserType.TYPE}
        ,   ids
        ,   Hibernate.LONG);
        MassDML.updateOrDelete(
            getSession()
        ,   "UPDATE DICT_ENUM_GROUP_MEMBERS SET ACTIVE_TO=? WHERE ENROLLMENT_ID IN #"
        ,   new Object[] {date}
        ,   new Type[] {DateToLongUserType.TYPE}
        ,   ids
        ,   Hibernate.LONG);
        MassDML.updateOrDelete(
            getSession()
        ,   "DELETE FROM DICT_ENUM_REF_MEMBERS WHERE ENROLLMENT_ID IN #"
        ,   null
        ,   null
        ,   ids
        ,   Hibernate.LONG);
    }
    
    public void commit() throws DictionaryException {
    	super.commit();
    	// fire dictionary change event after commit()
        dictionary.fireDictionaryChangeEvent();        
    }
}
