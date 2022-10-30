/*
 * Created on Apr 8, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.TestLdifEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestEnroller;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/TestClientInfoEnrollmentBase.java#1 $
 */

public abstract class TestClientInfoEnrollmentBase extends BaseContainerSharedTestCase {
	static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
	static {
	    REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
	    REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
	}
	
	IDictionary dictionary;
	TestEnroller enroller;
	
	static final String ENROLLMENT_DATA_DIR = SRC_ROOT_DIR + "/server/tools/enrollment/etc/";
	
	@Override
	protected Set<DestinyRepository> getDataRepositories() {
	    return REQUIRED_DATA_REPOSITORIES;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if (SRC_ROOT_DIR == null) {
			throw new IllegalArgumentException("\"src.root.dir\" path specified by system property");
		}

		this.enroller = new TestEnroller();
		this.dictionary = (IDictionary) ComponentManagerFactory.getComponentManager().getComponent(
				Dictionary.COMP_INFO);
		
		if( this.dictionary.getEnrollment("TestLdifEnroller") == null ){
			TestLdifEnroller testLdifEnroller = new TestLdifEnroller();
			testLdifEnroller.setUp();
			testLdifEnroller.testLocalEnrollments();
		}

		deleteClientInfoEnrollemnt();
	}
	
	void deleteClientInfoEnrollemnt() throws DictionaryException{
		//delete the previous test enrollment if exists
		IConfigurationSession session = null;
		try {
			IEnrollment enrollment = this.dictionary.getEnrollment(getTestDomainName());
			if(enrollment != null){
				session = this.dictionary.createSession();
				session.beginTransaction();
				session.deleteEnrollment(enrollment);
				session.commit();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
    Throwable getRootCause(Throwable t) {
		if (t.getCause() != null && t.getCause() != t) {
			Throwable rootCause = getRootCause(t.getCause());
			return rootCause == null ? t : rootCause;
		} else {
			return t;
		}
	}
	
	abstract String getTestDomainName();
	
	void setupBlugjunleEnrollment() throws Exception {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		properties.put(ClientInfoEnroller.CLIENT_INFO_FILE_KEY,
				new String[] { TestClientInfoEnroller.BLUEJUNGLE_CLIENT_INFO_FILE });
		enroller.setupLocalDomain(getTestDomainName(), new String[] {},
				EnrollmentTypeEnumType.CLIENT_INFO, properties, false);
	}
	
	static class MockClientInfoEnrollment extends ClientInfoEnrollment{
		static Reader reader;
		private final boolean saveToDb;
		List<Collection<IElementBase>> allElements;
		
		public MockClientInfoEnrollment(IEnrollmentSession session, IDictionary dictionary,
				IEnrollment enrollment, boolean preCacheAllUsers, int clientFetchSize,
				int userFetchSize) throws EnrollmentValidationException, DictionaryException {
			this(session, dictionary, enrollment, preCacheAllUsers, clientFetchSize, userFetchSize, false);
		}
		
		public MockClientInfoEnrollment(IEnrollmentSession session, IDictionary dictionary,
				IEnrollment enrollment, boolean preCacheAllUsers, int clientFetchSize,
				int userFetchSize, boolean saveToDb) throws EnrollmentValidationException, DictionaryException {
			super(session, dictionary, enrollment, preCacheAllUsers, clientFetchSize, userFetchSize);
			this.saveToDb = saveToDb;
			allElements = new ArrayList<Collection<IElementBase>>();
		}

		@Override
		protected Reader initReader() throws EnrollmentValidationException {
			return reader != null ? reader : super.initReader();
		}

		@Override
		protected void save(Collection<IElementBase> elements) throws DictionaryException {
			if(saveToDb){
				super.save(elements);
			}
			
			allElements.add(elements);
		}
	}
	
	void close(MockClientInfoEnrollment cie, IEnrollmentSession session)
			throws DictionaryException {
		if (cie != null) {
			cie.close();
		}
		if (session != null) {
			session.close(true, "testing");
		}
	}
	
	void close(Closeable close) throws IOException{
		if (close != null) {
			close.close();
		}
	}
	
}
