/*
 * Created on Jan 29, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.services.enrollment.EnrollmentIF;
import com.bluejungle.destiny.services.enrollment.types.Column;
import com.bluejungle.destiny.services.enrollment.types.ColumnList;
import com.bluejungle.destiny.services.enrollment.types.DictionaryFault;
import com.bluejungle.destiny.services.enrollment.types.DuplicatedFault;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentFailedFault;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault;
import com.bluejungle.destiny.services.enrollment.types.EntityType;
import com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault;
import com.bluejungle.destiny.services.enrollment.types.NotFoundFault;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.services.enrollment.types.RealmList;

/**
 * similar interface to EnrollmentIF but less exception handling
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentServiceWrapper.java#1 $
 */

class EnrollmentServiceWrapper {
    private static final Log LOG = LogFactory.getLog(EnrollmentServiceWrapper.class);
    
    private final EnrollmentIF port;
    public EnrollmentServiceWrapper(EnrollmentIF enrollmentIF) {
        this.port = enrollmentIF;
    }
    
    public EnrollmentIF getEnrollmentIF(){
        return port;
    }
    
    private abstract class CommonRemoteExceptionHandler<T> {
        
        abstract T run() throws RemoteException;
        
        EnrollmentMgrException handleSpecific(RemoteException e) {
            LOG.error("", e);
            return EnrollmentMgrException.create(e);
        }
        
        T execute() throws EnrollmentMgrException{
            try {
                return run();
            } catch ( com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault e) {
                throw new EnrollmentMgrException(e);
            } catch (UnauthorizedCallerFault e) {
                throw new EnrollmentMgrException(e);
            } catch (DictionaryFault e) {
                throw new EnrollmentMgrException(e);
            } catch (ServiceNotReadyFault e) {
                throw new EnrollmentMgrException(e);
            } catch (EnrollmentInternalFault e) {
                throw new EnrollmentMgrException(e);
            } catch (RemoteException e) {
                throw handleSpecific(e);
            }
        }
    }
    
    
    RealmList getRealms(final String name) throws EnrollmentMgrException {
        return new CommonRemoteExceptionHandler<RealmList>() {
            @Override
            RealmList run() throws RemoteException {
                return port.getRealms(name);
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                if(e instanceof NotFoundFault){
                    return new EnrollmentMgrException((NotFoundFault)e);
                }
                
                return super.handleSpecific(e);
            }
            
        }.execute();
    }

    void createRealm(final Realm realm) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.createRealm(realm);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                
                if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof DuplicatedFault) {
                    return new EnrollmentMgrException((DuplicatedFault) e);
                }
                
                return super.handleSpecific(e);
            }
            
        }.execute();
    }

    void updateRealm(final Realm realm) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.updateRealm(realm);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                
                if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof NotFoundFault) {
                    return new EnrollmentMgrException((NotFoundFault) e);
                }
                
                return super.handleSpecific(e);
            }
            
        }.execute();
    }

    void deleteRealm(final Realm realm) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.deleteRealm(realm);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                
                if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof NotFoundFault) {
                    return new EnrollmentMgrException((NotFoundFault) e);
                }
                
                return super.handleSpecific(e);
            }
            
        }.execute();
    }

    void enrollRealm(final Realm realm) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.enrollRealm(realm);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                if (e instanceof EnrollmentFailedFault) {
                    return new EnrollmentMgrException((EnrollmentFailedFault) e);
                } else if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof NotFoundFault) {
                    return new EnrollmentMgrException((NotFoundFault) e);
                }

                return super.handleSpecific(e);
            }

        }.execute();
    }

    ColumnList getColumns() throws EnrollmentMgrException {
        return new CommonRemoteExceptionHandler<ColumnList>() {
            @Override
            ColumnList run() throws RemoteException {
                return port.getColumns();
            }
            
        }.execute();

    }

    void addColumn(final Column column) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.addColumn(column);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof DuplicatedFault) {
                    return new EnrollmentMgrException((DuplicatedFault) e);
                }

                return super.handleSpecific(e);
            }

        }.execute();
    }

    void delColumn(final String logicalName, final EntityType elementType) throws EnrollmentMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.delColumn(logicalName, elementType);
                return null;
            }

            @Override
            EnrollmentMgrException handleSpecific(RemoteException e) {
                if (e instanceof InvalidConfigurationFault) {
                    return new EnrollmentMgrException((InvalidConfigurationFault) e);
                } else if (e instanceof NotFoundFault) {
                    return new EnrollmentMgrException((NotFoundFault) e);
                }

                return super.handleSpecific(e);
            }

        }.execute();
    }

}
