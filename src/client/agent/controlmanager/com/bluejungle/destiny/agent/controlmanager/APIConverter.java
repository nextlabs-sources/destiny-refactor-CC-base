/*
 * Created on May 8, 2007.  All sources, binaries, and HTML pages (c) copyright
 * 2007 by Next Labs Inc., San Mateo CA. Ownership remains with Next Labs Inc.
 * All rights reserved worldwide
 */

package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.pf.domain.destiny.common.SpecAttribute;

import java.util.ArrayList;
import java.util.List;

class APIConverter {
    public static Object[] convertOldToNew(Object[] newArray, ArrayList paramArray) {
        newArray[IPolicyEvaluator.AGENT_TYPE_INDEX] = "fsa";
        newArray[IPolicyEvaluator.TIMESTAMP_INDEX] = Long.toString(System.currentTimeMillis());
        newArray[IPolicyEvaluator.IGNORE_OBLIGATIONS_INDEX] = paramArray.get(6);
        newArray[IPolicyEvaluator.LOG_LEVEL_INDEX] = paramArray.get(9);
        newArray[IPolicyEvaluator.PROCESS_TOKEN_INDEX] = "0";
        
        boolean hasTarget = (paramArray.get(5) != null &&
                             ((String)paramArray.get(5)).length() > 0);

        String fromResourceFSCheck = "";
        String toResourceFSCheck = "";
        int fromArraySize = 6;
        int toArraySize = 6;
        if (paramArray.size() == 14) {
            fromResourceFSCheck = (String)paramArray.get(12);
            toResourceFSCheck = (String)paramArray.get(13);

            if (fromResourceFSCheck == null) {
                fromResourceFSCheck = "";
            } else {
                fromArraySize = 8;
            }

            if (toResourceFSCheck == null) {
                toResourceFSCheck = "";
            } else {
                toArraySize = 8;
            }
        }

        if (hasTarget) {
            newArray[IPolicyEvaluator.DIMENSION_DEFS_INDEX] = dimensionFromTo;
        } else {
            newArray[IPolicyEvaluator.DIMENSION_DEFS_INDEX] = dimensionFrom;
        }

        Object[] actionArray = { "name", paramArray.get(1) };
        newArray[8] = actionArray;

        Object[] userArray = { "id", paramArray.get(2) };
        newArray[9] = userArray;

        Object[] hostArray = { "inet_address", paramArray.get(4) };
        newArray[10] = hostArray;

        Object[] appArray = { "name", paramArray.get(3),
                              "fingerprint", null,
                              "pid", paramArray.get(8)};
        newArray[11] = appArray;

        List<String> fromList = new ArrayList<String>();

        fromList.add(SpecAttribute.DESTINYTYPE_ATTR_NAME);
        fromList.add("fso");
        fromList.add(SpecAttribute.RESOLVEDNAME_ATTR_NAME);
        fromList.add((String)paramArray.get(10));
        fromList.add(SpecAttribute.ID_ATTR_NAME);
        fromList.add((String)paramArray.get(0));
        if (!fromResourceFSCheck.equals("")) {
            fromList.add(SpecAttribute.FSCHECK_ATTR_NAME);
            fromList.add(fromResourceFSCheck);
        }
        newArray[12] = fromList.toArray(new String[fromList.size()]);

        if (hasTarget) {
            List<String> toList = new ArrayList<String>();
            toList.add(SpecAttribute.DESTINYTYPE_ATTR_NAME);
            toList.add("fso");
            toList.add(SpecAttribute.RESOLVEDNAME_ATTR_NAME);
            toList.add((String)paramArray.get(11));
            toList.add(SpecAttribute.ID_ATTR_NAME);
            toList.add((String)paramArray.get(5));
            if (!toResourceFSCheck.equals("")) {
                toList.add(SpecAttribute.FSCHECK_ATTR_NAME);
                toList.add(toResourceFSCheck);
            }
            newArray[13] = toList.toArray(new String[toList.size()]);
        }

        return newArray;
    }

    private static final Object[] dimensionFrom = { "action", "user", "host", "application", "from" };
    private static final Object[] dimensionFromTo = { "action", "user", "host", "application", "from", "to" };
}
