package com.nextlabs.destiny.container.shared.customapps;

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportDataDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportUIDO;

/**
 * This is responsible for reading/writing the custom application data to the database.
 * Any change in the CustomAppDO, CustomReportDataDO and CustomReportFileDO
 * may require changing this class since some queries rely on the field names of 
 * these DO classes.
 * 
 * The DMS should use this to write custom report data to the database and the
 * Reporter should use this to read the data from the database.
 * 
 * Note that the data that is purely application specific i.e. CustomAppDO is stored
 * in the management database, while the report related data are stored in the 
 * activity database. This class is responsible to make this transparent to the 
 * client that uses the APIs listed here.
 * 
 * @author ssen
 *
 */
public class CustomAppDataManager implements ILogEnabled, IConfigurable,
        IInitializable, IManagerEnabled, IHasComponentInfo<CustomAppDataManager> {
    private static final ComponentInfo<CustomAppDataManager> COMP_INFO =
            new ComponentInfo<CustomAppDataManager>(
                    CustomAppDataManager.class, 
                    LifestyleType.SINGLETON_TYPE
            );
    
    private Log log;
    private IConfiguration config;
    private IHibernateRepository activityDataSource;
    private IHibernateRepository mgmtDataSource;
    private IComponentManager componentManager;
    
    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }
    
    public IComponentManager getManager() {
        return componentManager;
    }

    public void setManager(IComponentManager manager) {
        this.componentManager = manager;
    }

    public void init() {
        activityDataSource = (IHibernateRepository) getManager().getComponent(
                DestinyRepository.ACTIVITY_REPOSITORY.getName());
        if (activityDataSource == null) {
            throw new NullPointerException("The activity data source is null.");
        }
        mgmtDataSource = (IHibernateRepository) getManager().getComponent(
                DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        if (mgmtDataSource == null) {
            throw new NullPointerException("The management data source is null.");
        }
    }
    
    public ComponentInfo<CustomAppDataManager> getComponentInfo() {
       return COMP_INFO;
    }
    
    /**
     * Creates a CustomApplication entry in the database and all the associated
     * report information. If the entry is already present in the database, it is 
     * updated with the provided info - i.e. it will be overwritten.
     *  
     * @param customApp
     * @throws Exception
     */
    public void createCustomAppData(CustomAppDO customApp) 
            throws Exception {
        validate(customApp);
        CustomAppDO dao;
        try {
            dao = createCustomApp(customApp);
        } catch (HibernateException ex) {
            //TBD  - need to streamline exception generation
            throw new Exception("Could not create custom application Data Object", ex);
        }
        
        try {
            createReportUIData(customApp);
            List<CustomReportDataDO> customReports = customApp.getCustomReports();
            for (CustomReportDataDO thisReport : customReports) {

                thisReport.setCustomApp(dao);
                thisReport.setCustomAppId(dao.getId());

                createReportData(thisReport);
            }
        } catch (HibernateException ex) {
            // Need to 'undo' the custom app creation here
            try {
                deleteCustomAppDO(dao);
            } catch (HibernateException e) {
                log.error("fail to delete incomplete customapp.", e);
            }
            
            //TBD  - need to streamline exception generation
            throw new Exception("Could not create custom application Data Object", ex);
        }
    }
    
    /**
     * Deletes all Custom Application data along with the associated report data.
     * 
     * @throws Exception
     */
    public void deleteAllCustomAppData() throws HibernateException {
        Session s;
        Transaction t;
        boolean isSuccess;
        
        // delete all report data
        s = activityDataSource.getCountedSession();
        t = null;
        isSuccess = false;
        try {
            t = s.beginTransaction();
            s.delete("from CustomReportDataDO");
            s.delete("from CustomReportUIDO");
            s.delete("from CustomReportFileDO");
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            activityDataSource.closeCurrentSession();
        } 
       
        // delete all application data
        s = mgmtDataSource.getCountedSession();
        t = null;
        isSuccess = false;
        try {
            t = s.beginTransaction();
            s.delete("from CustomAppDO");
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            mgmtDataSource.closeCurrentSession();
        }   
    }
    
    
    /**
     * Get a list of all custom applications present in the database. If no custom
     * applications are present, it will return an empty list.
     * 
     * @return Empty list if no custom applications are present.
     * @throws Exception
     */
    public List<CustomAppDO> getAllCustomAppData() throws HibernateException {
        List<CustomAppDO> customAppDataList = getAllCustomApps();
        if (customAppDataList == null) {
            customAppDataList = new ArrayList<CustomAppDO>();
        } else {
            buildRelationships(customAppDataList);
        }
        return customAppDataList;
    }
    
    private void validate(CustomAppDO customApp) throws InvalidCustomAppException {
        if (customApp == null) {
            throw new InvalidCustomAppException(
                    "Null Custom Application provided");
        }
        if (customApp.getName() == null) {
            throw new InvalidCustomAppException(
            "Custom Application Name cannot be null");
        }
        if (customApp.getVersion() == null) {
            throw new InvalidCustomAppException(
            "Custom Application Version cannot be null");
        }
        
        if (customApp.getReportUI() == null)
            throw new InvalidCustomAppException("UI Config of application: " + 
                    customApp + " cannot be null");
        
        List<CustomReportDataDO> customReportData = customApp.getCustomReports();
        if (customReportData == null || customReportData.isEmpty()) {
            throw new InvalidCustomAppException(
            "Custom Application does not contain reports");
        }
        
        for (CustomReportDataDO thisReport : customReportData) {
            if (thisReport.getTitle() == null)
                throw new InvalidCustomAppException(
                        "Title of report: " + thisReport + " cannot be null");
        }
    }

    private CustomAppDO createCustomApp(CustomAppDO dao) throws HibernateException {
        Session s = mgmtDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;

        try {
            t = s.beginTransaction();
            Long newCustomAppId = (Long) s.save(dao);
            t.commit();
            dao.setId(newCustomAppId);
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            mgmtDataSource.closeCurrentSession();
        }
        return dao;
    }
    
    private void createReportUIData(CustomAppDO customApp) throws HibernateException {
        if (customApp == null || customApp.getReportUI() == null) {
            throw new IllegalArgumentException();
        }

        Session s = activityDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;

        try {
            t = s.beginTransaction();
            CustomReportUIDO dao = customApp.getReportUI();
            dao.setCustomApp(customApp);
            dao.setCustomAppId(customApp.getId());
            s.save(dao);
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            activityDataSource.closeCurrentSession();
        }
    }
    
    private void createReportData(CustomReportDataDO dao) throws HibernateException {
        Session s = activityDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;
        try {
            t = s.beginTransaction();
            s.save(dao);
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            activityDataSource.closeCurrentSession();
        }
    }
    
    private List<CustomAppDO> getAllCustomApps() throws HibernateException {
        List<CustomAppDO> customAppDataList  = new ArrayList<CustomAppDO>();
        
        Session s = mgmtDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;
        try {
            t = s.beginTransaction();
            customAppDataList = s.createQuery("from CustomAppDO").list();
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            mgmtDataSource.closeCurrentSession();
        }
        return customAppDataList;
    }
    
    /**
     * The input is a basic list of custom apps. The output is a complete list
     * of the apps that contains the references to the reports. Assumes that
     * the input list is NOT null.
     * 
     * @param customAppDataList in/out
     * @throws Exception
     */
    private void buildRelationships(List<CustomAppDO> customAppDataList) throws HibernateException {
        Session s = activityDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;
        try {
            t = s.beginTransaction();
            for (CustomAppDO thisApp : customAppDataList) {
                
                // the ui file content 
                Criteria criteria = s.createCriteria(CustomReportUIDO.class);
                criteria.add(Expression.eq(
                        CustomReportDataDO.CUSTOM_APP_ID_ATTR_NAME, 
                        thisApp.getId()));
                CustomReportUIDO thisUIFile = (CustomReportUIDO)criteria.uniqueResult();
                if (thisUIFile != null) {
                    thisUIFile.setCustomApp(thisApp);
                    thisApp.setReportUI(thisUIFile);
                }
                
                // the reports
                criteria = s.createCriteria(CustomReportDataDO.class);
                criteria.add(Expression.eq(
                        CustomReportDataDO.CUSTOM_APP_ID_ATTR_NAME, 
                        thisApp.getId()));
                List<CustomReportDataDO> reportList = criteria.list();
                for (CustomReportDataDO thisReport : reportList) {
                    if (thisReport != null) {
                        thisReport.setCustomApp(thisApp);
                    }
                    thisApp.addCustomReport(thisReport);
                }
            }
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            activityDataSource.closeCurrentSession();
        }
    }

    private void deleteCustomAppDO(CustomAppDO dao) throws HibernateException {
        Session s = mgmtDataSource.getCountedSession();
        Transaction t = null;
        boolean isSuccess = false;
        try {
            t = s.beginTransaction();
            s.delete(dao);
            t.commit();
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                HibernateUtils.rollbackTransation(t, log);
            }
            mgmtDataSource.closeCurrentSession();
        }
    }

}
