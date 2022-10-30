/*
 * All sources, binaries and HTML pages (C) copyright 2004-2011 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */

package com.bluejungle.pf.domain.destiny.serviceprovider;

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.pf.domain.epicenter.evaluation.IEvaluationRequest;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

public interface IPIPServiceProvider extends IServiceProvider {
    IEvalValue evaluateSubjectAttribute(IEvaluationRequest request, ISubjectType type, String operation) throws ServiceProviderException;
}
