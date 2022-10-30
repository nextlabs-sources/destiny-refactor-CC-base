/*
 * Created on Jan 11, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common;

import java.util.ArrayList;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IFormatter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/common/EmailAddressFormatter.java#1 $
 */

public class EmailAddressFormatter implements IFormatter<String[], String[]> {
    public String[] format(String[] o) {
        ArrayList<String> tempArray = new ArrayList<String>(o.length);

        for (int i = 0; i < o.length; i++) {
            if (o[i].toLowerCase().startsWith("smtp:")) {
                tempArray.add(o[i].substring(5));
            } else {
                tempArray.add(o[i]);
            }
        }
        return tempArray.toArray(new String[tempArray.size()]);
    }
}
