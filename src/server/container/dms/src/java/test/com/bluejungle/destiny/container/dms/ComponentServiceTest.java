/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.rmi.RemoteException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.lang.RandomStringUtils;

import com.bluejungle.destiny.container.dms.components.BaseDMSComponentTest;
import com.bluejungle.destiny.container.dms.components.compmgr.DCCComponentMgrImpl;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentMgr;
import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.destiny.services.management.types.Component;
import com.bluejungle.destiny.services.management.types.ComponentList;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.version.types.Version;

/**
 * Test suite for the component service test - Note that this is currently
 * imcomplete. Added to test a single method
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/ComponentServiceTest.java#1 $
 */
public class ComponentServiceTest extends BaseDMSComponentTest {

    private ComponentServiceIF serviceToTest;
    private IDCCComponentMgr componentMgr;
    private static final int major = 2;
    private static final int minor = 12;
    private static final int maintenance = 1;
    private static final int patch = 3;
    private static final int build = 329;
    private static final NonNegativeInteger wsMajor = new NonNegativeInteger(String.valueOf(major));
    private static final NonNegativeInteger wsMinor = new NonNegativeInteger(String.valueOf(minor));
    private static final NonNegativeInteger wsMaintenance = new NonNegativeInteger(String.valueOf(maintenance));
    private static final NonNegativeInteger wsPatch = new NonNegativeInteger(String.valueOf(patch));
    private static final NonNegativeInteger wsBuild = new NonNegativeInteger(String.valueOf(build));
    private static final Version wsVersion = new Version(wsMajor, wsMinor, wsMaintenance, wsPatch, wsBuild);
    
    
    /**
     * Constructor
     * 
     * @param testName
     */
    public ComponentServiceTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ComponentServiceTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IDCCComponentMgr> regMgrCompInfo = 
            new ComponentInfo<IDCCComponentMgr>(
                IDCCComponentMgr.COMP_NAME, 
                DCCComponentMgrImpl.class, 
                IDCCComponentMgr.class, 
                LifestyleType.TRANSIENT_TYPE);
        this.componentMgr = compMgr.getComponent(regMgrCompInfo);

        ComponentServiceLocator serviceLocator = new ComponentServiceLocator();
        serviceLocator.setComponentServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ComponentServiceIFPort");
        this.serviceToTest = serviceLocator.getComponentServiceIFPort();
    }

    //public void testGetComponents() throws ServiceNotReadyFault,
          //  UnauthorizedCallerFault, RemoteException, MalformedURIException,
          //  DataSourceException, HibernateException, InterruptedException {
       // ComponentList componentList = serviceToTest.getComponents();
       // int initialComponentListLength = componentList.getComp().length;

        // Now, register some components
        //      DCSF-1
    //    DCCRegistrationInformation dcsfInfo1 = new DCCRegistrationInformation();
    //    dcsfInfo1.setEventListenerURL(new URI("http://www1.mockURI.for.dcsf2-1" + RandomStringUtils.randomAlphanumeric(10)));
    //    dcsfInfo1.setComponentURL(new URI("http://www1.mockURI.for.dcsf2-1" + RandomStringUtils.randomAlphanumeric(10)));
    //    dcsfInfo1.setComponentName("my-dcsf2-" + RandomStringUtils.randomAlphanumeric(10));
    //    dcsfInfo1.setComponentType(ServerComponentType.DCSF.getName());
    //    dcsfInfo1.setComponentTypeDisplayName("Communication Server");
    //    dcsfInfo1.setVersion(wsVersion);

        // DMS (Co-located with DCSF-1)
   //     DCCRegistrationInformation dmsInfo = new DCCRegistrationInformation();
   //     dmsInfo.setEventListenerURL(new URI("http://www1.mockURI.for.dcsf-1" + RandomStringUtils.randomAlphanumeric(10)));
   //     dmsInfo.setComponentURL(new URI("http://www1.mockURI.for.dms-1" + RandomStringUtils.randomAlphanumeric(10)));
   //     dmsInfo.setComponentName("my-dms-" + RandomStringUtils.randomAlphanumeric(10));
   //     dmsInfo.setComponentType(ServerComponentType.DMS.getName());
   //     dmsInfo.setComponentTypeDisplayName("Management Server");
   //     dmsInfo.setVersion(wsVersion);

        // Register components:
   //     this.serviceToTest.registerComponent(dcsfInfo1);
   //     this.serviceToTest.registerComponent(dmsInfo);

        // Now, retieve list again
   //     componentList = serviceToTest.getComponents();
   //     Component[] components = componentList.getComp();
   //     assertEquals("testGetComponents - Ensure list of components is of right size", 
   //             initialComponentListLength + 2, 
   //             components.length);

   //     for (int i = 0; i < components.length; i++) {
   //         Component nextComponent = components[i];
     //       if (nextComponent.getName().equals(dcsfInfo1.getComponentName())) {
       //         assertEquals("Ensure component 1 type is correct", 
       //                dcsfInfo1.getComponentType().toString(), 
       //                 nextComponent.getType());
       //         assertEquals("Ensure component 1 url is correct", 
       //                 dcsfInfo1.getEventListenerURL().toString(), 
       //                 nextComponent.getCallbackURL());
       //     } else if (nextComponent.getName().equals(dmsInfo.getComponentName())) {
       //         assertEquals("Ensure component 2 type is correct", 
       //                 dmsInfo.getComponentType().toString(), 
      //                  nextComponent.getType());
       //         assertEquals("Ensure component 2 url is correct", 
       //                 dmsInfo.getEventListenerURL().toString(), 
       //                 nextComponent.getCallbackURL());
     //       }
     //   }

        // Now, remove them
    //    ComponentDO componentToDelete = componentMgr.getComponentByName(dcsfInfo1.getComponentName());
    //    deleteComponent(componentToDelete);
    //    componentToDelete = componentMgr.getComponentByName(dmsInfo.getComponentName());
    //    deleteComponent(componentToDelete);

        // Ensure unregistered components are not returned 
    //    componentList = serviceToTest.getComponents();
    //    assertEquals("testGetComponents - Ensure list is equals to initial size removing added components", 
    //            initialComponentListLength, 
    //            componentList.getComp().length);
//    }

 
//    public void testGetComponentsByType() throws ServiceNotReadyFault,
//            UnauthorizedCallerFault, RemoteException, MalformedURIException,
//            DataSourceException, HibernateException, InterruptedException {
//        ComponentList componentList = serviceToTest.getComponentsByType(ServerComponentType.DCSF.getName());
//        int initialComponentListLength = componentList.getComp().length; 

        // Now, register a components
        //      DCSF-1
//        DCCRegistrationInformation dcsfInfo1 = new DCCRegistrationInformation();
//        dcsfInfo1.setEventListenerURL(new URI("http://www1.mockURI.for.dcsf2-1" + RandomStringUtils.randomAlphanumeric(10)));
//        dcsfInfo1.setComponentURL(new URI("http://www1.mockURI.for.dcsf2-1" + RandomStringUtils.randomAlphanumeric(10)));        
//        dcsfInfo1.setComponentName("my-dcsf2" + RandomStringUtils.randomAlphanumeric(10));
//        dcsfInfo1.setComponentType(ServerComponentType.DCSF.getName());
//        dcsfInfo1.setComponentTypeDisplayName("Communication Server");
//        dcsfInfo1.setVersion(wsVersion);

        // Register components:
  //      this.serviceToTest.registerComponent(dcsfInfo1);

        // Now, retieve list again
 //       componentList = serviceToTest.getComponentsByType(ServerComponentType.DCSF.getName());
 //       Component[] components = componentList.getComp();
 //       assertEquals("testGetComponents - Ensure list of components is of right size", 
 //               initialComponentListLength + 1, 
 //               components.length);

 //       for (int i = 0; i < components.length; i++) {
 //           Component nextComponent = components[i];
 //           if (nextComponent.getName().equals(dcsfInfo1.getComponentName())) {
 //               assertEquals("Ensure component 1 type is correct", 
  //                      dcsfInfo1.getComponentType().toString(), 
  //                      nextComponent.getType());
   //             assertEquals("Ensure component 1 url is correct", 
     //                   dcsfInfo1.getEventListenerURL().toString(), 
     //                   nextComponent.getCallbackURL());
     //       }
     //   }

        // Now, remove them
    //    ComponentDO componentToDelete = componentMgr.getComponentByName(dcsfInfo1.getComponentName());
    //    deleteComponent(componentToDelete);

        // Ensure unregistered components are not returned 
     //   componentList = serviceToTest.getComponentsByType(ServerComponentType.DCSF.getName());
     //   assertEquals("testGetComponents - Ensure list is equals to initial size removing added components", 
     //           initialComponentListLength, 
     //           componentList.getComp().length);
  //  }
    
    public void testUnregisterComponent() {
        //TODO Implement unregisterComponent().
    }

    public void testRegisterEvent() {
        //TODO Implement registerEvent().
    }

    public void testUnregisterEvent() {
        //TODO Implement unregisterEvent().
    }

    public void testRegisterComponent() {
        //TODO Implement registerComponent().
    }

    public void testCheckUpdates() {
        //TODO Implement checkUpdates().
    }

    /**
     * Delete a component from the database
     * 
     * @param componentToDelete
     * @throws HibernateException
     */
    private void deleteComponent(ComponentDO componentToDelete) throws HibernateException {
        Session hSession = getSession();
        Transaction transaction = null;
        try {
            transaction = hSession.beginTransaction();
            hSession.delete(componentToDelete);
            transaction.commit();
        } catch (HibernateException exception) {
            transaction.rollback();
            throw exception;
        } finally {
            if (hSession != null) {
                hSession.close();
            }
        }
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ComponentServiceTest.");
        }

        return dataSource;
    }
}
