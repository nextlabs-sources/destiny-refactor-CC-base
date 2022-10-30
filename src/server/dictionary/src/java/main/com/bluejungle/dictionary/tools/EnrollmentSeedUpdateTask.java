/*
 * Created on Dec 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.version.IVersion;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/tools/EnrollmentSeedUpdateTask.java#1 $
 */

public class EnrollmentSeedUpdateTask extends SeedDataTaskBase implements ISeedUpdateTask{
	private static final Log LOG = LogFactory.getLog(EnrollmentSeedUpdateTask.class);
	
	private EnrollmentSharedSeedTask enrollmentSharedSeedTask;
	
	@Override
	public void init() {
		super.init();
		enrollmentSharedSeedTask = new EnrollmentSharedSeedTask();
		enrollmentSharedSeedTask.init();
	}
	
	@Override
    public void execute() throws SeedDataTaskException {
        throw new UnsupportedOperationException();
    }

	/**
	 * @see com.bluejungle.destiny.tools.dbinit.seedtasks.SeedDataTaskBase#execute()
	 */
	public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
		if (fromV.compareTo(VERSION_2_0) <= 0 && toV.compareTo(fromV) > 0) {
			upgradeFrom2_0();
		}
		if (fromV.compareTo(VERSION_3_0) <= 0 && toV.compareTo(fromV) > 0) {
			upgradeFrom3_0();
		}
		LOG.info("done");
	}

	private void upgradeFrom3_0() throws SeedDataTaskException {
		boolean isTypeAlreadyExist = enrollmentSharedSeedTask
				.isTypeAlreadyExist(ElementTypeEnumType.CLIENT_INFO);
		if (!isTypeAlreadyExist) {
			try {
				final IMElementType clientInfoType = enrollmentSharedSeedTask
						.createClientInfoType();
				enrollmentSharedSeedTask.save(clientInfoType);
			} catch (DictionaryException e) {
				throw new SeedDataTaskException("Failed to add seed data for enrollment, reason:"
						+ e);
			}
			LOG.info("new contact type added");
		}
	}
	
	private void upgradeFrom2_0() throws SeedDataTaskException {
		final IDictionary dictionary = enrollmentSharedSeedTask.getDictionary();
		final IMElementType userType;
		try {
			userType = dictionary.getType(ElementTypeEnumType.USER.getName());
		} catch (DictionaryException e) {
			throw new SeedDataTaskException(
					"Failed to add seed data for enrollment, reason:" + e);
		}
		
		boolean isFieldAlreadyExist;
		try {
			isFieldAlreadyExist = (userType.getField(UserReservedFieldEnumType.MAIL.getName()) != null);
		} catch (IllegalArgumentException e) {
			isFieldAlreadyExist = false;
		}
		if (!isFieldAlreadyExist) {
			EnrollmentSharedSeedTask.setField(userType, UserReservedFieldEnumType.MAIL,
					ElementFieldType.STRING_ARRAY, UserReservedFieldEnumType.MAIL_LABEL); 
		    
			try {
				enrollmentSharedSeedTask.save(userType);
			} catch (DictionaryException e) {
				throw new SeedDataTaskException("Failed to add seed data for enrollment, reason:" + e);
			}
			LOG.info("new userType type added");
		}

		
		boolean isTypeAlreadyExist = enrollmentSharedSeedTask.isTypeAlreadyExist(ElementTypeEnumType.CONTACT);

		if (!isTypeAlreadyExist) {
			try {
				final IMElementType contactType = enrollmentSharedSeedTask.createContactType();
				enrollmentSharedSeedTask.save(contactType);
			} catch (DictionaryException e) {
				throw new SeedDataTaskException(
						"Failed to add seed data for enrollment, reason:" + e);
			}
			LOG.info("new contact type added");
		}
	}
}
