/*
 * Created on Mar 12, 2007 All sources, binaries and HTML pages (C) copyright
 * 2007 by NextLabs Inc., San Mateo CA, Ownership remains with NextLabs
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/agent/com/bluejungle/destiny/agent/controlmanager/CustomObligationsContainer.java#1 $:
 */

public class CustomObligationsContainer implements Externalizable {
    public static final String PEP_LOCATION = "PEP";
    public static final String PDP_LOCATION = "PDP";
    
    private class OblInfo {
        public String runAt;
        public String runBy;
        public String invocation;
        
        OblInfo(String runAt, String runBy, String invocation) {
            this.runAt = runAt;
            this.runBy = runBy;
            this.invocation = invocation;
        }
    }

    private Map<String, OblInfo> obligationsMap;

    CustomObligationsContainer() {
        obligationsMap = new HashMap<String, OblInfo>();
    }

    public void put(String name, String runAt, String runBy, String invocation) {
        obligationsMap.put(name, new OblInfo(runAt, runBy, invocation));
    }

    public boolean obligationExists(String name) {
        return (obligationsMap.get(name) != null);
    }

    public String getRunLocation(String name) {
        OblInfo inf = obligationsMap.get(name);

        if (inf != null) {
            return inf.runAt;
        } else {
            return PEP_LOCATION;
        }
    }

    public String getRunBy(String name) {
        OblInfo inf = obligationsMap.get(name);

        if (inf != null) {
            return inf.runBy;
        } else {
            return "";
        }
    }

    public String getInvocationString(String name) {
        OblInfo inf = obligationsMap.get(name);

        if (inf != null) {
            return inf.invocation;
        } else {
            return name;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(obligationsMap.size());
        
        Set<Map.Entry<String, OblInfo>> entries = obligationsMap.entrySet();
        
        for (Map.Entry<String, OblInfo> e : entries) {
            out.writeUTF(e.getKey());
            OblInfo value = e.getValue();
            out.writeUTF(value.runAt);
            out.writeUTF(value.runBy);
            out.writeUTF(value.invocation);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int numEntries = in.readInt();

        obligationsMap = new HashMap<String, OblInfo>();
        
        for (int i = 0; i < numEntries; i++) {
            String name = in.readUTF();
            String runAt = in.readUTF();
            String runBy = in.readUTF();
            String invocation = in.readUTF();

            obligationsMap.put(name, new OblInfo(runAt, runBy, invocation));
        }
    }
}
