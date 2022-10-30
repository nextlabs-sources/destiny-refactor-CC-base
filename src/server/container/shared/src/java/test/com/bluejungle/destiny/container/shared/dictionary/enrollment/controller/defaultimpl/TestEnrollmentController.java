package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentController;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.DictionaryTestHelper;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IReferenceable;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.dictionary.tools.MockSharedContextLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

public class TestEnrollmentController extends BaseContainerSharedTestCase { 
	
	public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }
    
    private IDictionary dictionary;
    private IEnrollerFactory enrollerFactory;
    private IEnrollmentController controller;
    
    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getComponentName()
     */
    protected String getComponentName() {
        return "dem";
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }

    /**
     * Constructor for TestEnrollmentManagerImpl.
     *
     * @param arg0
     */
    public TestEnrollmentController(String testCase) {
        super(testCase);
    }
    
    /**
     * if the test doesn't depends on the other test, should return true
     * @return
     */
    protected boolean clearDatabaseBeforeStart(){
    	return true;
    }
    
    protected void clearDictionaryDatabase() throws HibernateException, SQLException, DictionaryException{
    	DictionaryTestHelper.clearDictionary(dictionary);
    }
    
    private static boolean clearDatabase = true;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        enrollerFactory = new MockEnrollerFactoryImpl();

        // Setup the mock shared context locator:
        ComponentInfo<MockSharedContextLocator> locatorInfo = 
        	new ComponentInfo<MockSharedContextLocator>(
        		IDestinySharedContextLocator.COMP_NAME,
                MockSharedContextLocator.class,
                IDestinySharedContextLocator.class,
                LifestyleType.SINGLETON_TYPE);
        ComponentManagerFactory.getComponentManager().registerComponent(locatorInfo, true);
        ComponentManagerFactory.getComponentManager().getComponent(locatorInfo);

        dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
    	controller = new EnrollmentController(dictionary, enrollerFactory, null);
    	
        if (clearDatabase && clearDatabaseBeforeStart()) {
        	clearDictionaryDatabase();
			clearDatabase = false;
		}
    }
    
    public void testDeleteAllEnrollments() throws DictionaryException, HibernateException,
            SQLException {
    	assertNotNull(dictionary);
    	assertNotNull(enrollerFactory);
    	assertNotNull(controller);
    	
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(0, enrollments.size());
    }
    
    public void testSyncWhileSyncingWithRecurring() throws Exception {
		syncWhileSyncing(true);
	}

	public void testSyncWhileSyncing() throws Exception {
		syncWhileSyncing(false);
	}
    
    private void syncWhileSyncing(boolean isRecurring) throws Exception {
    	
    	final IEnrollment enrollment = isRecurring 
		    	? new EnrollmentMock(new Date().getTime(), 1) 
		    	: new EnrollmentMock();
    	controller.process(enrollment, null);
    	MockEnrollerFactoryImpl.syncDelay = 5000;
    	new Thread(new Runnable(){
			public void run() {
				try {
					controller.sync(enrollment);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
    	}).start();
    	Thread.sleep(1000);
    	try {
			controller.sync(enrollment);
			fail();
		} catch (EnrollmentSyncException e) {
			assertNotNull(e);
			assertEquals("Enrollment is running", e.getMessage());
		}
		
		Thread.sleep(6000);
		if(isRecurring){
			controller.deleteEnrollmentThread(enrollment);
		}
		
    }
    
    
    public void testSyncAfterSyncWithRecurring() throws Exception {
    	syncAfterSync(true);
	}
    
    public void testSyncAfterSync() throws Exception {
    	syncAfterSync(false);
	}
    
    public void syncAfterSync(boolean isRecurring) throws Exception {
    	final IEnrollment enrollment = isRecurring 
    	? new EnrollmentMock(new Date().getTime(), 1) 
    	: new EnrollmentMock();
		controller.process(enrollment, null);
		MockEnrollerFactoryImpl.syncDelay = 3000;
		new Thread(new Runnable() {
			public void run() {
				try {
					controller.sync(enrollment);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		}).start();
		Thread.sleep(4000);
		try {
			controller.sync(enrollment);
		} catch (EnrollmentSyncException e) {
			fail(e.getMessage());
		}
		Thread.sleep(4000);
		controller.deleteEnrollmentThread(enrollment);
	}
    
    
    private class EnrollmentMock implements IEnrollment{
    	private final String domainName;
    	private final boolean isActive;
    	private final boolean isRecurring;
    	private final long startTime;
    	private final long pullInterval;
    	
    	public EnrollmentMock(boolean isRecurring, long startTime,
				long pullInterval) {
    		domainName = TestEnrollmentController.class.getSimpleName();
			this.isActive = true;
			this.isRecurring = isRecurring;
			this.startTime = startTime;
			this.pullInterval = pullInterval;
		}

		public EnrollmentMock() {
			this(false, 0, 0);
		}

		public EnrollmentMock(long startTime, long pullInterval) {
			this(true, startTime, pullInterval);
		}
    	
    	public String getType() {
    		throw new UnsupportedOperationException();
		}
    	
		public String getDomainName() {
			return domainName;
		}
		
		public boolean getIsActive() {
			return isActive;
		}

		public boolean getIsRecurring() {
			return isRecurring;
		}
		
		public long getNumProperty(String name) {
			if (name.equals(BasicLDAPEnrollmentProperties.START_TIME)) {
				return startTime;
			} else if (name.equals(BasicLDAPEnrollmentProperties.PULL_INTERVAL)) {
				return pullInterval;
			} else {
				throw new UnsupportedOperationException();
			}
		}
		
		public void setDomainName(String domainName) {
			throw new UnsupportedOperationException();
		}
		
		public void setIsActive(boolean isActive) {
			throw new UnsupportedOperationException();
		}
		
		public void setIsRecurring(boolean isRecurring) {
			throw new UnsupportedOperationException();
		}

		public void clearAllExternalNames() {
			throw new UnsupportedOperationException();
		}

		public void clearExternalName(IElementField field) {
			throw new UnsupportedOperationException();
		}

		public void clearExternalNames(IElementType type) {
			throw new UnsupportedOperationException();
		}

		public IEnrollmentSession createSession() throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public void deleteAllProperties() {
			throw new UnsupportedOperationException();
		}

		public void deleteProperty(String name) {
			throw new UnsupportedOperationException();
		}

		public byte[] getBinProperty(String name) {
			throw new UnsupportedOperationException();
		}

		public String[] getBinPropertyNames() {
			throw new UnsupportedOperationException();
		}

		public IMElementBase getByKey(DictionaryKey key, Date asOf) throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public IDictionary getDictionary() {
			throw new UnsupportedOperationException();
		}

		public IMElement getElement(DictionaryKey key, Date asOf) throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public String getExternalName(IElementField field) {
			throw new UnsupportedOperationException();
		}

		public String[] getExternalNames(IElementType type) {
			throw new UnsupportedOperationException();
		}

		public IMGroup getGroup(DictionaryKey key, Date asOf) throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public String[] getNumPropertyNames() {
			throw new UnsupportedOperationException();
		}

		public List<IReferenceable> getProvisionalReferences(Collection<DictionaryPath> paths)
				throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public IUpdateRecord getStatus() throws DictionaryException {
			throw new UnsupportedOperationException();
		}

		public String[] getStrArrayProperty(String name) {
			throw new UnsupportedOperationException();
		}

		public String[] getStrArrayPropertyNames() {
			throw new UnsupportedOperationException();
		}

		public String getStrProperty(String name) {
			throw new UnsupportedOperationException();
		}

		public String[] getStrPropertyNames() {
			throw new UnsupportedOperationException();
		}

		public IElementField[] lookupField(IElementType type, String externalName) {
			throw new UnsupportedOperationException();
		}

		public IMElement makeNewElement(DictionaryPath path, IElementType type, DictionaryKey key) {
			throw new UnsupportedOperationException();
		}

		public IMGroup makeNewEnumeratedGroup(DictionaryPath path, DictionaryKey key) {
			throw new UnsupportedOperationException();
		}

		public IMGroup makeNewStructuralGroup(DictionaryPath path, DictionaryKey key) {
			throw new UnsupportedOperationException();
		}

		public void setBinProperty(String name, byte[] val) {
			throw new UnsupportedOperationException();
		}

		public void setExternalName(IElementField field, String externalName) {
			throw new UnsupportedOperationException();
		}

		public void setNumProperty(String name, long val) {
			throw new UnsupportedOperationException();
		}

		public void setStrArrayProperty(String name, String[] val) {
			throw new UnsupportedOperationException();
		}

		public void setStrProperty(String name, String val) {
			throw new UnsupportedOperationException();
		}

		public void setType(String type) {
			throw new UnsupportedOperationException();
		}

        public Calendar getNextSyncTime() {
            throw new UnsupportedOperationException();
        }

        public void setNextSyncTime(Calendar nextSyncTime) {
            throw new UnsupportedOperationException();
        }
    	
    }

}
