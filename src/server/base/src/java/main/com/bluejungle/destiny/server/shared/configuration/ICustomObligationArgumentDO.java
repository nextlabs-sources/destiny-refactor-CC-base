/*
 * All sources, binaries and HTML pages (C) Copyright 2007 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs Inc.
 * All rights reserved worldwide.
 *
 */

package com.bluejungle.destiny.server.shared.configuration;

import java.util.List;

/*
 *
 * @author amorgan
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/ICustomObligationArgumentDO.java#1 $
 */

public interface ICustomObligationArgumentDO {
    /**
     * Returns the name of this argument
     * 
     * @return name
     */
    public String getName();

    /**
     * Returns allowable values for this argument
     *
     * @return values
     */
    public String[] getValues();

    /**
     * Returns the default value for this argument
     *
     * @return defaultValue
     */
    public String getDefaultValue();

    /**
     * Is this argument user editable
     *
     * @return userEditable
     */
    public boolean isArgumentUserEditable();

    /**
     * Is this argument hidden
     *
     * @return hidden
     */
    public boolean isArgumentHidden();
}
