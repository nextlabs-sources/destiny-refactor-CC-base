/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportAccessException;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportVisibilityType;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrSortFieldType;
import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.service.axis.AuthenticationHandlerConstants;
import com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.basic.v1.StringList;
import com.bluejungle.destiny.types.basic_faults.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.basic_faults.v1.PersistenceFault;
import com.bluejungle.destiny.types.basic_faults.v1.ServiceNotReadyFault;
import com.bluejungle.destiny.types.basic_faults.v1.UniqueConstraintViolationFault;
import com.bluejungle.destiny.types.basic_faults.v1.UnknownEntryFault;
import com.bluejungle.destiny.types.effects.v1.EffectList;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report.v1.ReportList;
import com.bluejungle.destiny.types.report.v1.ReportQueryFieldName;
import com.bluejungle.destiny.types.report.v1.ReportQuerySpec;
import com.bluejungle.destiny.types.report.v1.ReportQueryTerm;
import com.bluejungle.destiny.types.report.v1.ReportQueryTermList;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortTerm;
import com.bluejungle.destiny.types.report.v1.ReportSortTermList;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;
import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the implementation of the report library interface for the report
 * service. This service acts as a wrapper on top of the user report manager
 * component.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/bindings/report/v1/ReportLibraryIFBindingImpl.java#1 $
 */

public class ReportLibraryIFBindingImpl implements ReportLibraryIF {

    private static final Log LOG = LogFactory.getLog(ReportLibraryIFBindingImpl.class.getName());
    private IComponentManager compMgr;

    /**
     * Constructor
     */
    public ReportLibraryIFBindingImpl() {
        super();
        this.compMgr = ComponentManagerFactory.getComponentManager();
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF#insertReport(com.bluejungle.destiny.types.report.v1.Report)
     */
    public Report insertReport(Report wsNewReport) throws UniqueConstraintViolationFault,
			AccessDeniedFault, PersistenceFault, ServiceNotReadyFault, RemoteException {
        IUserReportMgr userReportMgr = getUserMgrComponent();
        IPersistentReportMgr reportMgr = getPersistentReportMgr();
        Long userId = getCurrentUserId();
        
        IPersistentReport persistedReport = null;
        //Sets the owner id here
        IPersistentReport persistentReport = reportMgr.createPersistentReport(getCurrentUserId());

        //Copy the persistent report properties
        persistentReport.setDescription(wsNewReport.getDescription());
        persistentReport.setTitle(wsNewReport.getTitle());
        persistentReport.getOwner().setIsShared(wsNewReport.isShared());
        ReportWSConverter.convertServiceTypeToReportType(persistentReport, wsNewReport);
        
        // We will not save time range any more
        persistentReport.getTimePeriod().setBeginDate(null);
        persistentReport.getTimePeriod().setEndDate(null);

        try {
            persistedReport = userReportMgr.insertReport(persistentReport, userId);
        } catch (UniqueConstraintViolationException e){
        	throw new UniqueConstraintViolationFault(e.getConstrainingFields());
        } catch (DataSourceException e) {
            throw new PersistenceFault();
        }
        //Converts the results back to a web service saved report
        Report result = new ReportDTO(persistedReport);
        return result;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF#deleteReport(java.math.BigInteger)
     */
    public void deleteReport(BigInteger wsReportId) throws RemoteException, UnknownEntryFault,
			AccessDeniedFault, PersistenceFault, ServiceNotReadyFault {
        IUserReportMgr userReportMgr = getUserMgrComponent();
        Long userId = getCurrentUserId();

        if (wsReportId == null) {
            throw new UnknownEntryFault();
        }
        IPersistentReport report = null;
        try {
            //Passes on the request to the user report manager
            userReportMgr.deleteReport(new Long(wsReportId.longValue()), userId);
        } catch (ReportAccessException e) {
            throw new AccessDeniedFault();
        } catch (DataSourceException e) {
            throw new PersistenceFault();
        }
    }

    /**
     * Returns the current user id
     * 
     * @return the id of the user who made the webservice call.
     */
    protected Long getCurrentUserId() throws AccessDeniedFault {
        MessageContext context = MessageContext.getCurrentContext();
        ISecureSession secureSession = (ISecureSession) context
						.getProperty(AuthenticationHandlerConstants.SECURE_SESSION_PROPERTY_NAME);
        Long result = null;
        if (secureSession == null) {
            getLog().error("Unable to find the current user id in secure session");
            throw new AccessDeniedFault();
        } else {
            result = new Long((String) secureSession.getProperty(ISecureSession.ID_PROP_NAME));
        }
        return result;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Returns the component manager instance
     * 
     * @return the component manager instance
     */
    protected IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF#getReportById(java.math.BigInteger)
     */
    public Report getReportById(BigInteger wsReportId) throws RemoteException, UnknownEntryFault,
			AccessDeniedFault, ServiceNotReadyFault {
        IUserReportMgr userReportMgr = getUserMgrComponent();
        Long userId = getCurrentUserId();

        if (wsReportId == null) {
            throw new UnknownEntryFault();
        }
        IPersistentReport report = null;
        try {
            //Passes on the request to the user report manager
            report = userReportMgr.getReport(new Long(wsReportId.longValue()), userId);
        } catch (ReportAccessException e) {
            throw new AccessDeniedFault();
        }
        if (report == null) {
            throw new UnknownEntryFault();
        }
        Report result = new ReportDTO(report);
        return result;
    }

    /**
     * Returns the persistent report manager component
     * 
     * @return the persistent report manager component
     */
    protected IPersistentReportMgr getPersistentReportMgr() throws ServiceNotReadyFault {
        IPersistentReportMgr result = null;
        try {
            result = (IPersistentReportMgr) getManager().getComponent(IPersistentReportMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get the persistent report manager component in Component lookup service", e);
            throw new ServiceNotReadyFault();
        }
        return result;
    }

    /**
     * Returns the user report manager component
     * 
     * @return the user report manager component
     */
    protected IUserReportMgr getUserMgrComponent() throws ServiceNotReadyFault {
        IUserReportMgr result = null;
        try {
            result = (IUserReportMgr) getManager().getComponent(IUserReportMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get user manager component in Component lookup service", e);
            throw new ServiceNotReadyFault();
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF#getReports(com.bluejungle.destiny.types.report.v1.SavedReportQuerySpec)
     */
    public ReportList getReports(ReportQuerySpec wsQuerySpec) throws RemoteException,
			AccessDeniedFault, ServiceNotReadyFault {
        IPersistentUserReportMgrQuerySpec querySpec = new PersistentReportQuerySpecImpl(wsQuerySpec);
        IUserReportMgr userReportMgr = getUserMgrComponent();
        Long userId = getCurrentUserId();
        //Passes on the request to the user report manager
        List results = userReportMgr.getReports(querySpec, userId);
        //Converts the results back to a web service compliant data
        int size = results.size();
        ReportList result = new ReportList();
        Report[] reportArray = new Report[size];
        for (int index = 0; index < size; index++) {
            ReportDTO currentReport = new ReportDTO((IPersistentReport) results.get(index));
            reportArray[index] = currentReport;
        }
        result.setReports(reportArray);
        return result;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF#updateReport(com.bluejungle.destiny.types.report.v1.SavedReport)
     */
    public Report updateReport(Report savedReport) throws UniqueConstraintViolationFault,
			UnknownEntryFault, AccessDeniedFault, PersistenceFault, ServiceNotReadyFault,
			RemoteException {
        Report result = null;
        IUserReportMgr userReportMgr = getUserMgrComponent();
        Long userId = getCurrentUserId();

        IPersistentReport currentReport = null;
        BigInteger wsId = savedReport.getId();
        if (wsId == null) {
            throw new UnknownEntryFault();
        }

        try {
            currentReport = userReportMgr.getReport(new Long(wsId.longValue()), userId);
            if (currentReport == null) {
                throw new UnknownEntryFault();
            }

            //Update persistent report properties
            currentReport.setDescription(savedReport.getDescription());
            currentReport.setTitle(savedReport.getTitle());
            currentReport.getOwner().setIsShared(savedReport.isShared());

            //Update report basic properties
            ReportWSConverter.convertServiceTypeToReportType(currentReport, savedReport);
            
            // We will not save time range any more
            currentReport.getTimePeriod().setBeginDate(null);
            currentReport.getTimePeriod().setEndDate(null);
            
            IPersistentReport commitedReport = userReportMgr.saveReport(currentReport, userId);
            result = new ReportDTO(commitedReport);

        } catch (ReportAccessException e) {
            getLog().error("Access forbidden with updating report");
            throw new AccessDeniedFault();
        } catch (UniqueConstraintViolationException e) {
        	throw new UniqueConstraintViolationFault(e.getConstrainingFields());
        } catch (DataSourceException e) {
            getLog().error("A persistence error occured when updating report");
            throw new PersistenceFault();
        }
        return result;
    }

    /**
     * This class transforms a web service policy query specification into a
     * policy manager component query specification.
     * 
     * @author ihanen
     */
    protected class PersistentReportQuerySpecImpl implements IPersistentUserReportMgrQuerySpec {

        private IPersistentReportMgrQueryTerm[] queryTerms;
        private IPersistentReportMgrSortTerm[] sortTerms;
        private ReportVisibilityType visibility = ReportVisibilityType.ALL_REPORTS;

        /**
         * Constructor
         * 
         * @param wsSavedReportQuerySpec
         *            Axis policy query spec object
         */
        protected PersistentReportQuerySpecImpl(ReportQuerySpec wsSavedReportQuerySpec) {
            //Always populate queryTerms and sortTerms with an empty element
            this.queryTerms = new IPersistentReportMgrQueryTerm[0];
            this.sortTerms = new IPersistentReportMgrSortTerm[0];
            if (wsSavedReportQuerySpec != null) {
                //Process the search specification
                ReportQueryTermList reportQueryList = wsSavedReportQuerySpec.getSearchSpec();
                if (reportQueryList != null) {
                    ReportQueryTerm[] terms = reportQueryList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.queryTerms = new IPersistentReportMgrQueryTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.queryTerms[index] = new PersistentReportQueryTermImpl(terms[index]);
                            }
                        }
                    }
                }

                //Process the sort specification
                ReportSortTermList reportSortList = wsSavedReportQuerySpec.getSortSpec();
                if (reportSortList != null) {
                    ReportSortTerm[] terms = reportSortList.getTerms();
                    if (terms != null) {
                        int size = terms.length;
                        if (size > 0) {
                            this.sortTerms = new IPersistentReportMgrSortTerm[size];
                            for (int index = 0; index < size; index++) {
                                this.sortTerms[index] = new PersistentReportSortTermImpl(terms[index]);
                            }
                        }
                    }
                }

                //Process the visibility
                com.bluejungle.destiny.types.report.v1.ReportVisibilityType wsVisibility =
						wsSavedReportQuerySpec.getVisibility();
                if (com.bluejungle.destiny.types.report.v1.ReportVisibilityType.My.equals(wsVisibility)) {
                    this.visibility = ReportVisibilityType.MY_REPORTS;
                } else if (com.bluejungle.destiny.types.report.v1.ReportVisibilityType.Shared.equals(wsVisibility)) {
                    this.visibility = ReportVisibilityType.SHARED_REPORTS;
                }
            }
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSearchSpecTerms()
         */
        public IPersistentReportMgrQueryTerm[] getSearchSpecTerms() {
            return this.queryTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSortSpecTerms()
         */
        public IPersistentReportMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec#getVisibility()
         */
        public ReportVisibilityType getVisibility() {
            return this.visibility;
        }

    }

    /**
     * This is an implementation of the persistent report query term to convert
     * a web service term into a persistent report manager report query term.
     * 
     * @author ihanen
     */
    protected class PersistentReportQueryTermImpl implements IPersistentReportMgrQueryTerm {

        private String expression;
        private PersistentReportMgrQueryFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service saved report query term
         */
        protected PersistentReportQueryTermImpl(ReportQueryTerm wsQueryTerm) {
            if (wsQueryTerm == null) {
                throw new NullPointerException("wsQueryTerm cannot be null");
            }
            this.expression = wsQueryTerm.getExpression();
            setFieldName(wsQueryTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm#getFieldName()
         */
        public PersistentReportMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service saved report query field name into a
         * persistent report manager query field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(ReportQueryFieldName wsFieldName) {
            if (ReportQueryFieldName.Description.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrQueryFieldType.DESCRIPTION;
            } else if (ReportQueryFieldName.Title.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrQueryFieldType.TITLE;
            } else if (ReportQueryFieldName.Shared.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrQueryFieldType.SHARED;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is an implementation of the persistent report manager query term to
     * convert a web service saved report sort term into a persistent manager
     * sort term.
     * 
     * @author ihanen
     */
    protected class PersistentReportSortTermImpl implements IPersistentReportMgrSortTerm {

        private SortDirectionType direction;
        private PersistentReportMgrSortFieldType fieldName;

        /**
         * Constructor
         * 
         * @param wsQueryTerm
         *            web service policy query term
         */
        protected PersistentReportSortTermImpl(ReportSortTerm wsSortTerm) {
            if (wsSortTerm == null) {
                throw new NullPointerException("wsSortTerm cannot be null");
            }
            if (com.bluejungle.destiny.types.basic.v1.SortDirection.Ascending
            		.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.ASCENDING;
            } else if (com.bluejungle.destiny.types.basic.v1.SortDirection.Descending
					.equals(wsSortTerm.getDirection())) {
                this.direction = SortDirectionType.DESCENDING;
            } else {
                throw new IllegalArgumentException("invalid direction for wsSortTerm");
            }

            setFieldName(wsSortTerm.getFieldName());
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm#getFieldName()
         */
        public PersistentReportMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * Converts a web service policy sort field name into a policy manager
         * sort field name
         * 
         * @param name
         *            web service query field name
         */
        protected void setFieldName(ReportSortFieldName wsFieldName) {
            if (ReportSortFieldName.Description.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrSortFieldType.DESCRIPTION;
            } else if (ReportSortFieldName.Title.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrSortFieldType.TITLE;
            } else if (ReportSortFieldName.Shared.equals(wsFieldName)) {
                this.fieldName = PersistentReportMgrSortFieldType.SHARED;
            } else {
                throw new IllegalArgumentException("Invalid wsFieldName");
            }
        }
    }

    /**
     * This is the transfer class between a persistent report and a web service
     * report object (saved report).
     * 
     * @author ihanen
     */
    protected class ReportDTO extends Report {

        /**
         * Constructor
         * 
         * @param report
         *            persistent report object
         */
        public ReportDTO(IPersistentReport report) {
            if (report == null) {
                throw new NullPointerException("report cannot be null");
            }

            this.setDescription(report.getDescription());
            this.setId(new BigInteger(report.getId().toString()));
            this.setTitle(report.getTitle());
            this.setShared(report.getOwner().getIsShared());
            try {
                if (report.getOwner().getOwnerId().longValue() == getCurrentUserId().longValue()){
                    this.setOwned(true);
                }
            } catch (AccessDeniedFault e) {
                e.printStackTrace();
            }
            report.getSortSpec(); //TODO?

            //Converts the date
            IReportTimePeriod timePeriod = report.getTimePeriod();
            if (timePeriod != null) {
                if (timePeriod.getBeginDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(report.getTimePeriod().getBeginDate().getTimeInMillis());
                    this.setBeginDate(cal);
                }
                if (timePeriod.getEndDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(report.getTimePeriod().getEndDate().getTimeInMillis());
                    this.setEndDate(cal);
                }
            }

            this.setSummaryType(ReportWSConverter.convertReportSummaryToServiceType(report.getSummaryType()));
            convertInquiry(report.getInquiry());
        }

        /**
         * Converts an inquiry object to a set of web service compliant objects
         * 
         * @param inquiry
         *            inquiry to convert
         */
        protected void convertInquiry(IInquiry inquiry) {
            if (inquiry == null) {
                throw new NullPointerException("inquiry cannot be null");
            }

            //Takes care of actions
            Set actions = inquiry.getActions();
            int size = actions.size();
            ActionList actionList = new ActionList();
            String[] actionTypeArray = new String[size];
            Iterator it = actions.iterator();
            for (int index = 0; index < size; index++) {
                IInquiryAction action = (IInquiryAction) it.next();
                actionTypeArray[index] = ReportWSConverter.convertActionToServiceType(action.getActionType());
            }
            actionList.setActions(actionTypeArray);
            this.setActions(actionList);

            //Takes care of policy names
            StringList policyList = new StringList();
            Set policies = inquiry.getPolicies();
            size = policies.size();
            String[] policyNameArray = new String[size];
            it = policies.iterator();
            for (int index = 0; index < size; index++) {
                IInquiryPolicy policy = (IInquiryPolicy) it.next();
                policyNameArray[index] = policy.getName();
            }
            policyList.setValues(policyNameArray);
            this.setPolicies(policyList);

            //Takes care of policy decisions
            EffectList effectList = new EffectList();
            Set effects = inquiry.getPolicyDecisions();
            size = effects.size();
            EffectType[] effectArray = new EffectType[size];
            it = effects.iterator();
            for (int index = 0; index < size; index++) {
                IInquiryPolicyDecision policyDecision = (IInquiryPolicyDecision) it.next();
                effectArray[index] = ReportWSConverter.convertPolicyDecisionTypeToEffectType(
                		policyDecision.getPolicyDecisionType());
			}
            effectList.setValues(effectArray);
            this.setEffects(effectList);

            //Takes care of resources
            StringList resourceList = new StringList();
            Set resources = inquiry.getResources();
            size = resources.size();
            String[] resourceNameArray = new String[size];
            it = resources.iterator();
            for (int index = 0; index < size; index++) {
                IInquiryResource resource = (IInquiryResource) it.next();
                resourceNameArray[index] = resource.getName();
            }
            resourceList.setValues(resourceNameArray);
            this.setResourceNames(resourceList);

            //Takes care of users
            StringList userList = new StringList();
            Set users = inquiry.getUsers();
            size = users.size();
            String[] userNameArray = new String[size];
            it = users.iterator();
            for (int index = 0; index < size; index++) {
                IInquiryUser user = (IInquiryUser) it.next();
                userNameArray[index] = user.getDisplayName();
            }
            userList.setValues(userNameArray);
            this.setUsers(userList);

            //Sets the inquiry target data type
            this.setTarget(ReportWSConverter.convertTargetDataToServiceType(inquiry.getTargetData()));
            
            this.setLoggingLevel(inquiry.getLoggingLevel());
        }
    }
}