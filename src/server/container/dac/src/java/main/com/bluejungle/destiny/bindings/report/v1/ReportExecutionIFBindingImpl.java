/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTrackingActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogDO;
import com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF;
import com.bluejungle.destiny.types.basic_faults.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.basic_faults.v1.ServiceNotReadyFault;
import com.bluejungle.destiny.types.basic_faults.v1.UnknownEntryFault;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.report.v1.ExecutionFault;
import com.bluejungle.destiny.types.report.v1.InvalidArgumentFault;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report_result.v1.DetailResultList;
import com.bluejungle.destiny.types.report_result.v1.DocumentActivityCustomResult;
import com.bluejungle.destiny.types.report_result.v1.DocumentActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.LogDetailResult;
import com.bluejungle.destiny.types.report_result.v1.PolicyActivityCustomResult;
import com.bluejungle.destiny.types.report_result.v1.PolicyActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportResult;
import com.bluejungle.destiny.types.report_result.v1.ReportState;
import com.bluejungle.destiny.types.report_result.v1.ReportSummaryResult;
import com.bluejungle.destiny.types.report_result.v1.SummaryResult;
import com.bluejungle.destiny.types.report_result.v1.SummaryResultList;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.InvalidActivityLogIdException;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogDetailResult;
import com.nextlabs.destiny.types.custom_attr.v1.CustomAttribute;
import com.nextlabs.destiny.types.custom_attr.v1.CustomAttributeList;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/bindings/report/v1/ReportExecutionIFBindingImpl.java#1 $
 */

public class ReportExecutionIFBindingImpl implements ReportExecutionIF {

    /**
     * Log object
     */
    private static final Log LOG = LogFactory.getLog(ReportExecutionIFBindingImpl.class.getName());

    /**
     * Version number for the state object. This will be useful in future
     * releases.
     */
    private static final Float STATE_VERSION_10 = new Float(1.0);

    /**
     * Lock object for cache reloading synchronization
     */
    private static final Object SYNC_OBJECT_LOCK = new Object();

    private IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
    private Date lastCacheReloadTime = new Date(0);

    /**
     * Extracts the results from the result set object and converts the results
     * to a datatype compatible with the web service. The nature of the results
     * is discovered as the reader returns data from the report execution.
     * 
     * @param resultSet
     *            report result reader
     * @return a populated web service report result object
     */
    protected ReportResult convertReportResultsToServiceType(IStatelessReportExecutionResult resultSet) {
        if (resultSet == null) {
            throw new NullPointerException("resultSet cannot be null");
        }
        // Only one of the collections will be used
        List summaryResults = new LinkedList();
        List policyDetailResults = new LinkedList();
        List trackingDetailResults = new LinkedList();
        while (resultSet.hasNextResult()) {
            IResultData data = resultSet.nextResult();
            // The result can either be summary or detail
            if (data instanceof IReportSummaryResult) {
                IReportSummaryResult summaryData = (IReportSummaryResult) data;
                SummaryResult currentResult = new SummaryResult();
                currentResult.setCount(summaryData.getCount().longValue());
                currentResult.setValue(summaryData.getValue());
                summaryResults.add(currentResult);
            } else if (data instanceof IReportPolicyActivityDetailResult) {
                IReportPolicyActivityDetailResult detailData = (IReportPolicyActivityDetailResult) data;
                PolicyActivityDetailResult policyActivityResult = new PolicyActivityDetailResult();
                policyActivityResult.setAction(ReportWSConverter.convertActionToServiceType(detailData.getAction()));
                policyActivityResult.setApplicationName(detailData.getApplicationName());
                policyActivityResult.setTimestamp(detailData.getTimestamp());
                // Converts the policy decision to an effect
                PolicyDecisionEnumType policyDecision = detailData.getPolicyDecision();
                if (policyDecision != null) {
                    EffectType effectType = EffectType.fromString(policyDecision.getName());
                    policyActivityResult.setEffect(effectType);
                }
                policyActivityResult.setFromResourceName(removeAllControlChars(detailData.getFromResourceName()));
                policyActivityResult.setHostIPAddress(detailData.getHostIPAddress());

                /*
                 * Bug Fix #3192. This isn't the best way to fix this issue, but
                 * it's the safest
                 */
                String hostname = detailData.getHostName();
                if (hostname == null) {
                    hostname = "";
                }
                policyActivityResult.setHostName(hostname);
                policyActivityResult.setId(BigInteger.valueOf(detailData.getId().longValue()));
                policyActivityResult.setPolicyName(detailData.getPolicyName());
                policyActivityResult.setToResourceName(removeAllControlChars(detailData.getToResourceName()));
                policyActivityResult.setUserName(detailData.getUserName());
                policyActivityResult.setLoggingLevel(detailData.getLoggingLevel());
                policyDetailResults.add(policyActivityResult);
            } else {
                // Tracking detail result
                IReportTrackingActivityDetailResult detailData = (IReportTrackingActivityDetailResult) data;
                DocumentActivityDetailResult trackingResult = new DocumentActivityDetailResult();
                trackingResult.setAction(ReportWSConverter.convertActionToServiceType(detailData.getAction()));
                trackingResult.setApplicationName(detailData.getApplicationName());
                trackingResult.setTimestamp(detailData.getTimestamp());
                trackingResult.setFromResourceName(removeAllControlChars(detailData.getFromResourceName()));
                trackingResult.setHostIPAddress(detailData.getHostIPAddress());
                
                /*
                 * Bug Fix #3192. This isn't the best way to fix this issue, but
                 * it's the safest
                 */
                String hostname = detailData.getHostName();
                if (hostname == null) {
                    hostname = "";
                }
                
                trackingResult.setHostName(hostname);
                trackingResult.setId(BigInteger.valueOf(detailData.getId().longValue()));
                trackingResult.setToResourceName(removeAllControlChars(detailData.getToResourceName()));
                trackingResult.setUserName(detailData.getUserName());
                trackingResult.setLoggingLevel(detailData.getLoggingLevel());
                trackingDetailResults.add(trackingResult);
            }
        }

        // Creates the final results object
        ReportResult result = null;
        if (summaryResults.size() > 0) {
            result = new ReportSummaryResult();
            SummaryResultList resultList = new SummaryResultList();
            Object[] array = summaryResults.toArray();
            int size = array.length;
            SummaryResult[] summaryResultList = new SummaryResult[size];
            System.arraycopy(array, 0, summaryResultList, 0, size);
            resultList.setResults(summaryResultList);
            ((ReportSummaryResult) result).setData(resultList);
            IReportResultStatistics stats = resultSet.getStatistics();
            resultList.setMinCount(stats.getMinValue().longValue());
            resultList.setMaxCount(stats.getMaxValue().longValue());
            resultList.setTotalCount(stats.getSumValue().longValue());
        } else if (policyDetailResults.size() > 0) {
            result = new ReportDetailResult();
            DetailResultList resultList = new DetailResultList();
            Object[] array = policyDetailResults.toArray();
            int size = array.length;
            PolicyActivityDetailResult[] detailResultList = new PolicyActivityDetailResult[size];
            // Gosh... arraycopy...
            System.arraycopy(array, 0, detailResultList, 0, size);
            resultList.setResults(detailResultList);
            ((ReportDetailResult) result).setData(resultList);
        } else if (trackingDetailResults.size() > 0) {
            result = new ReportDetailResult();
            DetailResultList resultList = new DetailResultList();
            Object[] array = trackingDetailResults.toArray();
            int size = array.length;
            DocumentActivityDetailResult[] detailResultList = new DocumentActivityDetailResult[size];
            System.arraycopy(array, 0, detailResultList, 0, size);
            resultList.setResults(detailResultList);
            ((ReportDetailResult) result).setData(resultList);
        } else {
            // Report returned no records, create a default result set.
            result = new ReportDetailResult();
        }

        // Assigns the total row count to the result
        result.setTotalRowCount(resultSet.getStatistics().getTotalRowCount().longValue());
        result.setAvailableRowCount(resultSet.getStatistics().getAvailableRowCount().longValue());
        return result;
    }
    
    /**
     * remove all the control chars in the <code>unformattedStr</code>
     * 		the control chars is defined in ASCII table, from Hex 0x00 to 0x1F and 0x7F
     * @param unformattedStr must be compatible with ASCII
     * @return formatted string
     */
    protected String removeAllControlChars(String unformattedStr) {
    	if(unformattedStr == null){
    		return null;
    	}
    	
    	char[] chars = unformattedStr.toCharArray();
    	int length = chars.length;
		for (int i = 0; i < length; i++) {
			//char is 2 bytes in Java
			if (chars[i] < 0x0020 || chars[i] == 0x007F) {
				chars[i] = '.';
			}
		}
		return new String(chars);
	}

    /**
     * Converts a state from its internal representation to the corresponding
     * web service type. The state version number is always represented in the
     * first part of the object array. Then comes the queryId, the rowId of the
     * first result, and the row id of the last result.
     * 
     * @param state
     *            internal state to convert
     * @return the web service state
     */
    protected ReportState convertReportStateToServiceType(IReportResultState state) {
        ReportState wsState = new ReportState();
        Object[] wsStateParts = new Object[4];
        wsStateParts[0] = STATE_VERSION_10;
        wsStateParts[1] = state.getQueryId();
        wsStateParts[2] = state.getFirstRowSequenceId();
        wsStateParts[3] = state.getLastRowSequenceId();
        wsState.setState(wsStateParts);
        return wsState;
    }

    
    /**
     * Extracts the results from the result set object and converts the results
     * to a datatype compatible with the web service. However, there will only 
     * one row inside 
     * 
     * @param resultSet
     *            report result reader
     * @return a populated web service report result object
     */
    protected LogDetailResult convertLogDetailResultToServiceType(ILogDetailResult logResult) {
        if (logResult == null) {
            throw new NullPointerException("resultSet cannot be null");
        }
        // The main collections
        LogDetailResult wsLogDetailResult = null;
        PolicyActivityCustomResult policyLogDetailResult = null;
        DocumentActivityCustomResult trackingLogDetailResult = null;
        CustomAttributeList wsCustomAttributesList = null; 
        if (logResult instanceof PolicyActivityLogDetailResult) {
            policyLogDetailResult = new PolicyActivityCustomResult();
            PolicyActivityLogDetailResult policyActivityLogDetailResult = (PolicyActivityLogDetailResult)logResult;
            PolicyActivityLogDO policyActivityLogDO = (PolicyActivityLogDO)policyActivityLogDetailResult.getActivityLog();
            policyLogDetailResult.setAction(ReportWSConverter.convertActionToServiceType(policyActivityLogDO.getAction()));
            policyLogDetailResult.setApplicationName(policyActivityLogDO.getApplicationName());
            policyLogDetailResult.setTimestamp(policyActivityLogDO.getTimestamp());
            // Converts the policy decision to an effect
            PolicyDecisionEnumType policyDecision = policyActivityLogDO.getPolicyDecision();
            if (policyDecision != null) {
                EffectType effectType = EffectType.fromString(policyDecision.getName());
                policyLogDetailResult.setEffect(effectType);
            }
            policyLogDetailResult.setFromResourceName(removeAllControlChars(policyActivityLogDO.getFromResourceInfo().getName()));
            policyLogDetailResult.setHostIPAddress(policyActivityLogDO.getHostIPAddress());
            String hostname = policyActivityLogDO.getHostName();
            if (hostname == null) {
                hostname = "";
            }
            policyLogDetailResult.setHostName(hostname);
            policyLogDetailResult.setId(BigInteger.valueOf(policyActivityLogDO.getId().longValue()));
            policyLogDetailResult.setPolicyName(policyActivityLogDetailResult.getPolicyName());
            if (policyActivityLogDO.getToResourceInfo() != null){
                policyLogDetailResult.setToResourceName(removeAllControlChars(policyActivityLogDO.getToResourceInfo().getName()));
            }
            policyLogDetailResult.setUserName(policyActivityLogDO.getUserName());
            policyLogDetailResult.setLoggingLevel(policyActivityLogDO.getLevel());
            
            // Fill the custom attributes
            HashMap customAttributesMap = logResult.getActivityCustomAttributes();
            if (customAttributesMap != null){
                CustomAttribute[] customAttributes = new CustomAttribute[customAttributesMap.size()];   
                Set customSet = customAttributesMap.entrySet();
                Iterator customIter = customSet.iterator();
                int i = 0;
                while (customIter.hasNext()){
                    Map.Entry entry = (Map.Entry)customIter.next();
                    customAttributes[i] = new CustomAttribute((String)entry.getKey(),
                                                              (String)entry.getValue());
                    i++;
                }
                wsCustomAttributesList = new CustomAttributeList(customAttributes);
            }
            policyLogDetailResult.setCustomAttributeList(wsCustomAttributesList);   
            wsLogDetailResult = new LogDetailResult(policyLogDetailResult);
        } else {
            // Tracking log detail result
            trackingLogDetailResult = new DocumentActivityCustomResult();
            TrackingActivityLogDetailResult trackingActivityLogDetailResult = (TrackingActivityLogDetailResult)logResult;
            TrackingActivityLogDO trackingActivityLogDO = (TrackingActivityLogDO)trackingActivityLogDetailResult.getActivityLog();
            trackingLogDetailResult.setAction(ReportWSConverter.convertActionToServiceType(trackingActivityLogDO.getAction()));
            trackingLogDetailResult.setApplicationName(trackingActivityLogDO.getApplicationName());
            trackingLogDetailResult.setTimestamp(trackingActivityLogDO.getTimestamp());
            trackingLogDetailResult.setFromResourceName(removeAllControlChars(trackingActivityLogDO.getFromResourceInfo().getName()));
            trackingLogDetailResult.setHostIPAddress(trackingActivityLogDO.getHostIPAddress());
            String hostname = trackingActivityLogDO.getHostName();
            if (hostname == null) {
                hostname = "";
            }      
            trackingLogDetailResult.setHostName(hostname);
            trackingLogDetailResult.setId(BigInteger.valueOf(trackingActivityLogDO.getId().longValue()));
            if (trackingActivityLogDO.getToResourceInfo() != null) {
                trackingLogDetailResult.setToResourceName(removeAllControlChars(trackingActivityLogDO.getToResourceInfo().getName()));
            }
            trackingLogDetailResult.setUserName(trackingActivityLogDO.getUserName());
            trackingLogDetailResult.setLoggingLevel(trackingActivityLogDO.getLevel());
            
            // Fill the custom attributes
            HashMap customAttributesMap = logResult.getActivityCustomAttributes();
            if (customAttributesMap != null) {
                CustomAttribute[] customAttributes = new CustomAttribute[customAttributesMap.size()];   
                Set customSet = customAttributesMap.entrySet();
                Iterator customIter = customSet.iterator();
                int i = 0;
                while (customIter.hasNext()){
                    Map.Entry entry = (Map.Entry)customIter.next();
                    customAttributes[i] = new CustomAttribute((String)entry.getKey(),
                                                              (String)entry.getValue());
                    i++;
                }
                wsCustomAttributesList = new CustomAttributeList(customAttributes); 
            }
            trackingLogDetailResult.setCustomAttributeList(wsCustomAttributesList); 
            wsLogDetailResult = new LogDetailResult(trackingLogDetailResult);
        }

        return wsLogDetailResult;
    }
    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF#executeReport(com.bluejungle.destiny.types.report.v1.Report,
     *      int)
     */
    public ReportResult executeReport(Report report, int nbRows, int maxNbResults) throws RemoteException, AccessDeniedFault, ExecutionFault, InvalidArgumentFault, ServiceNotReadyFault {
        // Converts the data types from Web Service to internal
        IReportMgr reportMgr = getReportMgr();
        IReport reportToExecute = reportMgr.createReport();
        IStatelessReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        reportToExecute = ReportWSConverter.convertServiceTypeToReportType(reportToExecute, report);

        // Executes the report
        IStatelessReportExecutionResult results = null;
        try {
            results = reportExecutionMgr.executeReport(reportToExecute, nbRows, maxNbResults);
        } catch (DataSourceException e) {
            ExecutionFault fault = new ExecutionFault();
            throw fault;
        } catch (InvalidReportArgumentException e) {
            InvalidArgumentFault fault = new InvalidArgumentFault();
            throw fault;
        }
        IReportResultState resultState = results.getResultState();

        // Converts results to web service types
        ReportResult wsResult = convertReportResultsToServiceType(results);

        // Results are no longer needed
        results.close();

        ReportState wsState = new ResultStateDTO(resultState);
        wsResult.setState(wsState);
        return wsResult;
    }

    /**
     * Returns the last cache reload attempt time
     * 
     * @return the last cache reload attempt time
     */
    protected Date getLastCacheReloadTime() {
        Date result = null;
        synchronized (SYNC_OBJECT_LOCK) {
            result = this.lastCacheReloadTime;
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
     * Returns the component manager
     * 
     * @return the component manager
     */
    protected IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF#getNextResultSet(com.bluejungle.destiny.types.report_result.v1.ReportState,
     *      int)
     */
    public ReportResult getNextResultSet(ReportState wsCurrentState, int nbRows) throws RemoteException, UnknownEntryFault, AccessDeniedFault, ServiceNotReadyFault {
        // Converts the web service types to internal types
        IReportResultState currentState = new ReportResultStateImpl(wsCurrentState);

        IStatelessReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();

        // Executes the report
        IStatelessReportExecutionResult results = null;
        try {
            results = reportExecutionMgr.gotoNextSet(currentState, nbRows);
        } catch (DataSourceException e) {
            ExecutionFault fault = new ExecutionFault();
            fault.setFaultReason(e.getLocalizedMessage());
            throw fault;
        }
        IReportResultState resultState = results.getResultState();
        
        // Converts results to web service types
        ReportResult wsResult = convertReportResultsToServiceType(results);
        ReportState wsState = new ResultStateDTO(resultState);
        wsResult.setState(wsState);
        
        results.close();

        return wsResult;
    }
    
    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF#getLogDetail(long)
     */
    public LogDetailResult getLogDetail(Report report, long recordId) throws RemoteException, UnknownEntryFault, AccessDeniedFault, ServiceNotReadyFault {
        // Converts the data types from Web Service to internal
        IReportMgr reportMgr = getReportMgr();
        IReport reportToExecute = reportMgr.createReport();
        IStatelessReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        reportToExecute = ReportWSConverter.convertServiceTypeToReportType(reportToExecute, report);
        LogDetailResult result = null;
        
        try {
            ILogDetailResult logResult = reportExecutionMgr.getLogDetail(reportToExecute, recordId);
            result = convertLogDetailResultToServiceType(logResult);
        } catch (DataSourceException e) {
            ExecutionFault fault = new ExecutionFault();
            fault.setFaultReason(e.getLocalizedMessage());
            throw fault;
        } catch (InvalidActivityLogIdException e){
            UnknownEntryFault fault = new UnknownEntryFault();
            fault.setFaultReason(e.getMessage());
            throw fault;
        }
        return result;
    }

    /**
     * Returns the report manager component
     * 
     * @return the report manager component
     * @throws ServiceNotReadyFault
     *             if the component cannot be found in the component manager
     */
    protected IReportMgr getReportMgr() throws ServiceNotReadyFault {
        IReportMgr result = null;
        try {
            result = (IReportMgr) getManager().getComponent(IReportMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get the report manager component");
            throw new ServiceNotReadyFault();
        }
        return result;
    }

    /**
     * Returns the report execution manager component
     * 
     * @return the report execution manager component
     * @throws ServiceNotReadyFault
     *             if the component cannot be found in the component manager
     */
    protected IStatelessReportExecutionMgr getReportExecutionMgr() throws ServiceNotReadyFault {
        IStatelessReportExecutionMgr result = null;
        try {
            result = (IStatelessReportExecutionMgr) getManager().getComponent(IReportExecutionMgr.COMP_NAME);
        } catch (RuntimeException e) {
            getLog().error("Unable to get the report execution manager component");
            throw new ServiceNotReadyFault();
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF#terminateReportExecution(com.bluejungle.destiny.types.report_result.v1.ReportState)
     */
    public void terminateReportExecution(ReportState currentState) throws RemoteException, UnknownEntryFault, AccessDeniedFault, ServiceNotReadyFault {
        // TODO
        getLog().debug("Terminate report execution called");
    }

    /**
     * This class is an implementation of the result state. It allows converting
     * from a web service report result state into an internal result state.
     * 
     * @author ihanen
     */
    protected class ReportResultStateImpl implements IReportResultState {

        private Long firstRowId;
        private Long lastRowId;
        private Long queryId;

        /**
         * Constructor
         * 
         * @param wsState
         *            web service report state object
         */
        public ReportResultStateImpl(ReportState wsState) {
            if (wsState == null) {
                throw new NullPointerException("wsState cannot be null");
            }
            Object[] stateData = wsState.getState();
            if (stateData.length > 0) {
                Float version = (Float) stateData[0];
                if (STATE_VERSION_10.equals(version)) {
                    this.queryId = (Long) stateData[1];
                    this.firstRowId = (Long) stateData[2];
                    this.lastRowId = (Long) stateData[3];
                } else {
                    throw new IllegalArgumentException("Unsupported version number");
                }
            } else {
                throw new IllegalArgumentException("Invalid state");
            }
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getFirstRowSequenceId()
         */
        public Long getFirstRowSequenceId() {
            return this.firstRowId;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getLastRowSequenceId()
         */
        public Long getLastRowSequenceId() {
            return this.lastRowId;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getQueryId()
         */
        public Long getQueryId() {
            return this.queryId;
        }
    }

    /**
     * This class is used to convert an internal report state to a web service
     * compliant report state object
     * 
     * @author ihanen
     */
    protected class ResultStateDTO extends ReportState {

        /**
         * Constructor
         * 
         * @param internalState
         *            internal report state
         */
        public ResultStateDTO(IReportResultState internalState) {
            if (internalState == null) {
                throw new NullPointerException("internalState cannot be null");
            }
            Long firstRowId = internalState.getFirstRowSequenceId();
            if (firstRowId == null) {
                firstRowId = new Long(-1);
            }
            Long lastRowId = internalState.getLastRowSequenceId();
            if (lastRowId == null) {
                lastRowId = new Long(-1);
            }
            Object[] wsStateData = new Object[] { STATE_VERSION_10, internalState.getQueryId(), firstRowId, lastRowId };
            setState(wsStateData);
        }
    }
}
