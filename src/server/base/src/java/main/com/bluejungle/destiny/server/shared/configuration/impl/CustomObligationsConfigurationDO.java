/*
 * Created on Aug 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/CustomObligationsConfigurationDO.java#1 $
 */

package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.ICustomObligationConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;

public class CustomObligationsConfigurationDO implements ICustomObligationsConfigurationDO {
    private List<ICustomObligationConfigurationDO> obligations = new ArrayList();

    public ICustomObligationConfigurationDO[] getCustomObligations() {
        return (ICustomObligationConfigurationDO[])obligations.toArray(new ICustomObligationConfigurationDO[obligations.size()]);
    }

    public void addCustomObligation(ICustomObligationConfigurationDO obl) {
        obligations.add(obl);
    }
}
