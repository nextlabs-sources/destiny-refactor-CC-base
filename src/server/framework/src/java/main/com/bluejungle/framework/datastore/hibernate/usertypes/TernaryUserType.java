/*
 * Created on Sep 16, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/TernaryUserType.java#1 $:
 */

package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.sql.Types;

import com.bluejungle.framework.utils.TernaryType;

public class TernaryUserType extends EnumUserType<TernaryType> {
    /**
     * Actions are stored as char(2)
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public TernaryUserType() {
        super(new TernaryType[] {TernaryType.TRUE,
                                 TernaryType.FALSE,
                                 TernaryType.UNKNOWN },
              new String[] { "T", "F", "U" },
              TernaryType.class);
    }

    public TernaryType getDefaultValue() {
        return TernaryType.UNKNOWN;
    }
}
