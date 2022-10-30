package com.bluejungle.destiny.agent.controlmanager;

/*
 * Created on Jan 09, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ProcessTokenWrapper.java#1 $:
 */

import com.bluejungle.framework.comp.ComponentManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class ProcessTokenWrapper {
    private static final Log log = LogFactory.getLog(ProcessTokenWrapper.class);

    private static final IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
    private Long processToken;
    private int count;

    private ProcessTokenWrapper() {
        this(0L);
    }

    private ProcessTokenWrapper(Long processToken) {
        count = 1;
        this.processToken = processToken;
    }

    public Long getToken() {
        return processToken;
    }

    synchronized public void incrementCount() {
        count++;
    }

    synchronized public void closeProcessToken() {
        if (--count <= 0) {
            log.debug("Closing process token " + processToken);
            controlManager.closeProcessToken(processToken);
        }
    }

    public static ProcessTokenWrapper createWrapper(Long token) {
        return new ProcessTokenWrapper(token);
    }

    public final static ProcessTokenWrapper NONE = new ProcessTokenWrapper() {
        public Long getToken() {
            return 0L;
        }
        public void incrementCount() {
        }
        public void closeProcessToken() {
        }
    };
}
