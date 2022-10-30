/*
 * Created on Mar 2, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl.EnrollmentManagerImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.service.ServiceHelper;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.enrollment.EnrollmentIF;
import com.bluejungle.destiny.services.enrollment.types.Column;
import com.bluejungle.destiny.services.enrollment.types.ColumnList;
import com.bluejungle.destiny.services.enrollment.types.DictionaryFault;
import com.bluejungle.destiny.services.enrollment.types.DuplicatedFault;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentFailedFault;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.services.enrollment.types.EntityType;
import com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault;
import com.bluejungle.destiny.services.enrollment.types.NotFoundFault;
import com.bluejungle.destiny.services.enrollment.types.Profile;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.services.enrollment.types.RealmList;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the DEM enrollment service implementation.
 * 
 * @author ihanen
 * @version $Id:
 *
 */

public class DEMEnrollmentServiceImpl implements EnrollmentIF {
    private static final Log log = LogFactory.getLog(DEMEnrollmentServiceImpl.class);

    private IEnrollmentManager enrollmentManager;

    public DEMEnrollmentServiceImpl() {
        super();
    }
    
    private IEnrollmentManager getEnrollmentManager() throws ServiceNotReadyFault {
        if (this.enrollmentManager == null) {
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            if (componentManager.isComponentRegistered(Dictionary.COMP_INFO.getName())) {
                this.enrollmentManager =
                        componentManager.getComponent(EnrollmentManagerImpl.class);
            } else {
                throw new ServiceNotReadyFault();
            }
        }
        return this.enrollmentManager;
    }
    
    /**
     * log all unknown, unexpected, rarely happen exception
     * @param t
     */
    private void log(Throwable t){
        log.error("", t);
    }
    
    /**
     * log exception, the stacktrace is only available when log level, INFO, is on
     * @param t
     */
    private void logLite(Throwable t) {
        if (log.isInfoEnabled()) {
            log.error("", t);
        } else {
            log.error(t.getMessage());
        }
    }
    
    public RealmList getRealms(String name) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.NotFoundFault {
        log.trace("Received call to list Realm in enrollment service");
        
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            Collection<IEnrollment> realms;
            if (name == null) {
                realms = enrollmentMgr.getRealms();
            } else {
                IEnrollment enrollment = enrollmentMgr.getRealm(name);
                realms = Collections.singleton(enrollment);
            }
             
            Realm[] realmArr = new Realm[realms.size()];
            int i = 0;
            for (IEnrollment enrollment : realms) {
                Realm realm = ServiceHelper.extractWSRealmFromDO(enrollment);
                realmArr[i++] = realm;
            }
            return new RealmList(realmArr);
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (EntryNotFoundException e) {
            throw new NotFoundFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            //name == null is already handled, it should not throw InvalidConfigurationException
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (RuntimeException e ) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }       
    }

    public void createRealm(Realm realm) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.DuplicatedFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault {
        log.trace("Received call to createRealm in enrollment service");
        
        if (realm == null) {
            throw new InvalidConfigurationFault("Received null enrollment info");
        }
        
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.createRealm(new RealmData(realm));
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (DuplicateEntryException e) {
            logLite(e);
            throw new DuplicatedFault(e.getMessage());
        } catch (EnrollmentValidationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollerCreationException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (EnrollmentThreadException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (RuntimeException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }
    
    public void updateRealm(Realm realm) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,        
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.NotFoundFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault {
        log.trace("Received call to updateRealm in enrollment service");

        if (realm == null) {
            throw new InvalidConfigurationFault("Received null enrollment info");
        }

        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.updateRealm(new RealmData(realm));
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (EntryNotFoundException e) {
            logLite(e);
            throw new NotFoundFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollerCreationException e) {
            log(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollmentValidationException e) {
            log(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollmentThreadException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (RuntimeException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }

    public void deleteRealm(Realm realm) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,        
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault,
            com.bluejungle.destiny.services.enrollment.types.NotFoundFault {
        log.trace("Received call to deleteRealm in enrollment service");
        
        if (realm == null) {
            throw new InvalidConfigurationFault("Received null enrollment info");
        }
        
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.deleteRealm(new RealmData(realm));
        } catch (DictionaryException e) {
            log(e);
        	throw new DictionaryFault(e.getMessage());
        } catch (EntryNotFoundException e) {
            logLite(e);
        	throw new NotFoundFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollmentThreadException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (RuntimeException e ) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }

    /**
     * @see com.bluejungle.destiny.interfaces.enrollment.EnrollmentIF#enrollRealm(com.bluejungle.destiny.types.enrollment.Realm)
     */
    public void enrollRealm(Realm realm) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentFailedFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault,
            com.bluejungle.destiny.services.enrollment.types.NotFoundFault {
        log.trace("Received call to enrollRealm in enrollment service");
        
        if (realm == null) {
            throw new InvalidConfigurationFault("Received null enrollment info");
        }
        
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.enrollRealm( new RealmData(realm) );
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (EntryNotFoundException e) {
            logLite(e);
        	throw new NotFoundFault(e.getMessage());
        } catch (EnrollmentValidationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollmentSyncException e) {
            log(e);
            throw new EnrollmentFailedFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (EnrollmentThreadException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        } catch (RuntimeException e ) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }


    public ColumnList getColumns()throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault {
        log.trace("Received call to getColumns in enrollment service");
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            Collection<IElementField> fields = enrollmentMgr.getColumns();
            Collection<Column> columns = new ArrayList<Column>();
            for (IElementField field : fields) {
                Column column = ServiceHelper.extractWSColumnFromDO(field);
                columns.add(column);
            }
            return new ColumnList(columns.toArray(new Column[0]));
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (RuntimeException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }
    
    /**
     * @see com.bluejungle.destiny.interfaces.enrollment.EnrollmentIF#addColumn(com.bluejungle.destiny.types.enrollment.Column)
     */
    public void addColumn(Column column) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.DuplicatedFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault{
        log.trace("Received call to addColumn in enrollment service");
        
        if (column == null) {
            throw new InvalidConfigurationFault("Received null column info");
        }
        
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.addColumn(new ColumnData(column));
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (DuplicateEntryException e) {
            logLite(e);
            throw new DuplicatedFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (RuntimeException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }

    /**
     * @see com.bluejungle.destiny.interfaces.enrollment.EnrollmentIF#delColumn(java.lang.String)
     */
    public void delColumn(String logicalName, EntityType elementType) throws 
            com.bluejungle.destiny.framework.types.ServiceNotReadyFault,
            com.bluejungle.destiny.framework.types.UnauthorizedCallerFault,
            com.bluejungle.destiny.services.enrollment.types.DictionaryFault,
            com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault,
            com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault,
            com.bluejungle.destiny.services.enrollment.types.NotFoundFault {
        log.trace("Received call to delColumn in enrollment service");
        if (logicalName == null) {
            throw new InvalidConfigurationFault("Received null logical name for column");
        }
        if (elementType == null) {
            throw new InvalidConfigurationFault("Received null elment type");
        }
        IEnrollmentManager enrollmentMgr = getEnrollmentManager();
        try {
            enrollmentMgr.delColumn(logicalName, elementType.getValue());
        } catch (DictionaryException e) {
            log(e);
            throw new DictionaryFault(e.getMessage());
        } catch (EntryNotFoundException e) {
            logLite(e);
            throw new NotFoundFault(e.getMessage());
        } catch (InvalidConfigurationException e) {
            logLite(e);
            throw new InvalidConfigurationFault(e.getMessage());
        } catch (RuntimeException e) {
            log(e);
            throw new EnrollmentInternalFault(e.getMessage());
        }
    }


    /**
     * Realm data adapter
     * 
     * @author safdar
     */
    private class RealmData implements IRealmData {

        private String name;
        private EnrollmentTypeEnumType type;
        private Map<String, String[]> properties = new HashMap<String, String[]>();

        public RealmData(Realm wsRealm) {
            this.name = wsRealm.getName();
            this.type = EnrollmentTypeEnumType.getByName(wsRealm.getType().getValue());
            Profile profile = wsRealm.getProfile();
            if (profile != null) {
                EnrollmentProperty[] propArr = profile.getProperties();
                if (propArr != null) {
                    for (int i = 0; i < propArr.length; i++) {
                        EnrollmentProperty prop = propArr[i];
                        properties.put(prop.getKey(), prop.getValue());
                    }
                }
            }
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getName()
         */
        public String getName() {
            return this.name;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getProperties()
         */
        public Map<String, String[]> getProperties() {
            return this.properties;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getType()
         */
        public EnrollmentTypeEnumType getType() {
            return this.type;
        }
    }

    /**
     * Column data adapter
     * 
     * @author safdar
     */
    private class ColumnData implements IColumnData {

        private String displayName;
        private String elementType;
        private String logicalName;
        private String type;

        public ColumnData(Column data) {
            this.displayName = data.getDisplayName();
            this.elementType = data.getParentType().getValue();
            this.logicalName = data.getLogicalName();
            this.type = data.getType().getValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getDisplayName()
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getElementType()
         */
        public String getElementType() {
            return this.elementType;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getLogicalName()
         */
        public String getLogicalName() {
            return this.logicalName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getType()
         */
        public String getType() {
            return this.type;
        }
    }
}
