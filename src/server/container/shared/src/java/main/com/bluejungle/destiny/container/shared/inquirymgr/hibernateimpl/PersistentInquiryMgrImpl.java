/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the persistent inquiry manager class implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentInquiryMgrImpl.java#1 $
 */

public class PersistentInquiryMgrImpl extends BaseInquiryMgrImpl implements IPersistentInquiryMgr {

    private IHibernateRepository dataSource;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr#createPersistentInquiry()
     */
    public IPersistentInquiry createPersistentInquiry() {
        InquiryDO inquiry = new InquiryDO();
        return inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr#deleteInquiry(java.lang.Long)
     */
    public void deleteInquiry(Long inquiryId) throws DataSourceException {
        Session s = null;
        Transaction t = null;
        try {
            s = this.dataSource.getCurrentSession();
            IPersistentInquiry inquiry = getInquiry(inquiryId);
            t = s.beginTransaction();
            s.delete(inquiry);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Error when deleting inquiry with id '" + inquiryId, e);
            throw new DataSourceException(e);
        } catch (DataSourceException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Error when deleting inquiry with id '" + inquiryId, e);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        super.dispose();
        this.dataSource = null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr#getInquiry(java.lang.Long)
     */
    public IPersistentInquiry getInquiry(Long inquiryId) throws DataSourceException {
        InquiryDO result = null;
        if (inquiryId == null) {
            throw new NullPointerException("report Id cannot be null in getReport");
        }

        //The session should not be closed
        Session s = null;
        try {
            s = this.dataSource.getCurrentSession();
            result = (InquiryDO) s.get(InquiryDO.class, inquiryId);
        } catch (HibernateException e) {
            getLog().error("Error occured when fetching inquiry by id", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr#getInquiries(java.lang.String,
     *      com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec)
     */
    public List getInquiries(String userId, ISortSpec sortSpec) throws DataSourceException {
        return null;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        this.dataSource = (IHibernateRepository) getConfiguration().get(REPORT_DATA_SOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source for the reports must be specified for the persistent report manager");
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr#saveInquiry(com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry)
     */
    public IPersistentInquiry saveInquiry(IPersistentInquiry updatedInquiry) throws DataSourceException {
        InquiryDO result = null;
        if (updatedInquiry == null) {
            throw new NullPointerException("Updated inquiry cannot be null in saveReport");
        }

        //The session should not be closed
        Session s = null;
        try {
            s = this.dataSource.getCurrentSession();
            result = (InquiryDO) s.save(updatedInquiry);
        } catch (HibernateException e) {
            getLog().error("Error occured when saving inquiry", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        return result;
    }
}