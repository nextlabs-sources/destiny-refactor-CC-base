/*
 * Created on Feb 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/bluejungle/dictionary/EnrollmentGhost.java#1 $
 */

public class EnrollmentGhost extends Enrollment {
	private final IDictionary dictionary;
    
	public EnrollmentGhost(String domainName, IDictionary dictionary){
		super(domainName, null);
		this.dictionary = dictionary;
	}

	@Override
	public List<IReferenceable> getProvisionalReferences(Collection<DictionaryPath> paths)
			throws DictionaryException {
		return Collections.emptyList();
	}

    @Override
    public IEnrollmentSession createSession() throws DictionaryException {
        return new EnrollmentSessionMock();
    }

    @Override
    public IDictionary getDictionary() {
        return dictionary;
    }
}
