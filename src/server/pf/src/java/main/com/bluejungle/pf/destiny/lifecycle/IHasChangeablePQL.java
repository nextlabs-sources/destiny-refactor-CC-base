package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/IHasChangeablePQL.java#1 $
 */

import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This interface lets classes access setPql.
 * It is package-private, intended for use by LifecycleManager.
 */

public interface IHasChangeablePQL extends IHasPQL {

    void setPql(String pql) throws PQLException;

}
