/*
 * Created on Sep 12, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/bindings/log/v3/DABSLogServiceImpl.java#1 $:
 */
package com.nextlabs.destiny.bindings.log.v3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.domain.log.PolicyActivityInfoV2;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityInfoV3;
import com.nextlabs.domain.log.TrackingLogEntryV3;
import com.nextlabs.destiny.interfaces.log.v3.LogServiceIF;
import com.nextlabs.destiny.types.log.v3.LogStatus;

public class DABSLogServiceImpl implements LogServiceIF {
    private static final Log LOG = LogFactory.getLog(DABSLogServiceImpl.class.getName());

    /**
     * Create an instance of the DABS Log Service
     */
    public DABSLogServiceImpl() {
        super();
    }

    /**
     * This function is called by the agent to submit a log request to the service.
     * 
     * @param logInfo structure containing the logs to be stored in the database
     * @return a log status (saying whether logging was ok or not)
     * @throws RemoteException if the log request fails
     */
    public LogStatus logPolicyActivity(String request) throws RemoteException {

        long before = System.currentTimeMillis();
        try {
            ILogWriter logWriter = getLogWriter();
            long beforeDeserialization = System.currentTimeMillis();
            PolicyActivityLogEntry[] logEntries = decodePARequest(request);
            long afterDeserialization = System.currentTimeMillis();
            logWriter.log(logEntries);
            long after = System.currentTimeMillis();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Deserializing decoded stream took " + (afterDeserialization - beforeDeserialization) + " ms.");                                
                LOG.trace("Writing to DB took " + (after - afterDeserialization) + " ms.");                                                
                LOG.trace("The entire operation took " + (after - before) + " ms.");
            }
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            // Log it
            LOG.warn("Persistence failure while attempting to record policy activity log entries.", exception);
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record policy activity log entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record policy activity log entries.", e);            
        }
        return LogStatus.Failed;
    }

    public LogStatus logPolicyActivityV2(String request) throws RemoteException {

        long before = System.currentTimeMillis();
        try {
            ILogWriter logWriter = getLogWriter();
            long beforeDeserialization = System.currentTimeMillis();
            PolicyActivityLogEntryV2[] logEntries = decodePAV2Request(request);
            long afterDeserialization = System.currentTimeMillis();
            logWriter.log(logEntries);
            long after = System.currentTimeMillis();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Deserializing decoded stream took " + (afterDeserialization - beforeDeserialization) + " ms.");                                
                LOG.trace("Writing to DB took " + (after - afterDeserialization) + " ms.");                                                
                LOG.trace("The entire operation took " + (after - before) + " ms.");
            }
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            // Log it
            LOG.warn("Persistence failure while attempting to record policy activity log V2 entries.", exception);
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record policy activity log V2 entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record policy activity log V2 entries.", e);            
        }
        return LogStatus.Failed;
    }

    public LogStatus logPolicyActivityV3(String request) throws RemoteException {

        long before = System.currentTimeMillis();
        try {
            ILogWriter logWriter = getLogWriter();
            long beforeDeserialization = System.currentTimeMillis();
            PolicyActivityLogEntryV3[] logEntries = decodePAV3Request(request);
            long afterDeserialization = System.currentTimeMillis();
            logWriter.log(logEntries);
            long after = System.currentTimeMillis();
            if (getLog().isTraceEnabled()) {
                getLog().trace("Deserializing decoded stream took " + (afterDeserialization - beforeDeserialization) + " ms.");                                
                getLog().trace("Writing to DB took " + (after - afterDeserialization) + " ms.");                                                
                getLog().trace("The entire operation took " + (after - before) + " ms.");
            }
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            // Log it
            getLog().warn("Persistence failure while attempting to record policy activity log V3 entries.", exception);
        } catch (IOException e) {
            getLog().warn("Exception while attempting to record policy activity log V3 entries.", e);            
        } catch (ClassNotFoundException e) {
            getLog().warn("Exception while attempting to record policy activity log V3 entries.", e);            
        }
        return LogStatus.Failed;
    }

    private PolicyActivityLogEntry[] decodePARequest(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        PolicyActivityLogEntry[] rv = new PolicyActivityLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalPolicyLog(ois);
        }
        return rv;
    }

    private PolicyActivityLogEntryV2[] decodePAV2Request(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        PolicyActivityLogEntryV2[] rv = new PolicyActivityLogEntryV2[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalPolicyLogV2(ois);
        }
        return rv;
    }

    private PolicyActivityLogEntryV3[] decodePAV3Request(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        PolicyActivityLogEntryV3[] rv = new PolicyActivityLogEntryV3[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalPolicyLogV3(ois);
        }
        return rv;
    }
    
    public LogStatus logPolicyAssistant(String request) throws RemoteException {
        try {
            PolicyAssistantLogEntry[] entries = decodePAssRequest(request);
            ILogWriter logWriter = getLogWriter();
            logWriter.log(entries);
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            LOG.warn("Persistence failure while attempting to record policy assistant log entries.", exception);            
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record policy assistant log log entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record policy assistant log log entries.", e);            
        }
        
        return LogStatus.Failed;
    }
    
    private PolicyAssistantLogEntry[] decodePAssRequest(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        PolicyAssistantLogEntry[] rv = new PolicyAssistantLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalPolicyAssistantLog(ois);
        }
        return rv;
    }

    public LogStatus logTracking(String request) throws RemoteException {
        
        try {
            TrackingLogEntry[] entries = decodeTRRequest(request);            
            ILogWriter logWriter = getLogWriter();
            logWriter.log(entries);
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            LOG.warn("Persistence failure while attempting to record tracking log entries.", exception);            
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record tracking log entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record tracking log entries.", e);            
        }
        
        return LogStatus.Failed;
    }

    public LogStatus logTrackingV2(String request) throws RemoteException {
        
        try {
            TrackingLogEntryV2[] entries = decodeTRV2Request(request);            
            ILogWriter logWriter = getLogWriter();
            logWriter.log(entries);
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            LOG.warn("Persistence failure while attempting to record tracking log V2 entries.", exception);            
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record tracking log V2 entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record tracking log V2 entries.", e);            
        }
        
        return LogStatus.Failed;
    }

    public LogStatus logTrackingV3(String request) throws RemoteException {
        
        try {
            TrackingLogEntryV3[] entries = decodeTRV3Request(request);            
            ILogWriter logWriter = getLogWriter();
            logWriter.log(entries);
            return LogStatus.Success;
        } catch (DataSourceException exception) {
            LOG.warn("Persistence failure while attempting to record tracking log V3 entries.", exception);            
        } catch (IOException e) {
            LOG.warn("Exception while attempting to record tracking log V3 entries.", e);            
        } catch (ClassNotFoundException e) {
            LOG.warn("Exception while attempting to record tracking log V3 entries.", e);            
        }
        
        return LogStatus.Failed;
    }


    private TrackingLogEntry[] decodeTRRequest(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        TrackingLogEntry[] rv = new TrackingLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalTrackingLog(ois);   
        }
        return rv;
    }

    private TrackingLogEntryV2[] decodeTRV2Request(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        TrackingLogEntryV2[] rv = new TrackingLogEntryV2[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalTrackingLogV2(ois);   
        }
        return rv;
    }

    private TrackingLogEntryV3[] decodeTRV3Request(String request) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = DABSLogServiceWSConverter.decodeData(request);
        int numEntries = ois.readInt();
        TrackingLogEntryV3[] rv = new TrackingLogEntryV3[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = DABSLogServiceWSConverter.readExternalTrackingLogV3(ois);   
        }
        return rv;
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
     * Retrieve the ILogWriter component.
     * 
     * @return The ILogWriter component
     * @throws ServiceNotReadyFault if the LogWriter is not available
     */
    protected ILogWriter getLogWriter() throws ServiceNotReadyFault {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        if (!componentManager.isComponentRegistered(ILogWriter.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (ILogWriter) componentManager.getComponent(ILogWriter.COMP_NAME);
    }
}
