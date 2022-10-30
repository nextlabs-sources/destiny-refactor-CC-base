package com.bluejungle.destiny.agent.controlmanager;

/*
 * Created on Jan 09, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ProcessTokenMap.java#1 $:
 */

/**
 * Process tokens are used for impersonation.  Without that we might
 * not be able to read the file due to insufficient permission.
 * Unfortunately, some processes themselves lack the permission to
 * read the file and if we inherit this last of capability we are in
 * trouble (Windows 7 takes security a lot more seriously and many
 * apps are doing the "least permission possible" approach, delegating
 * stuff to super-processes).
 *
 * The solution is to ignore, most of the time, the process token we
 * are given.  Instead we use the process token for the user's
 * explorer session (if there is one.  If not, use the token we are
 * given).  This means that the lifetime of the tokens is a little
 * more complex.  Non-explorer tokens disappear quickly, explorer
 * tokens stay around until we get another, more up-to-date one.
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class ProcessTokenMap {
    private static final Log log = LogFactory.getLog(ProcessTokenMap.class);

    private static Map<String, ProcessTokenWrapper> sidTokenMap = new HashMap<String, ProcessTokenWrapper>();

    private static boolean updateToken(String applicationName) {
        return (applicationName != null) && (applicationName.toLowerCase().endsWith("explorer.exe"));
    }

    synchronized public static ProcessTokenWrapper substituteToken(String userSid, String applicationName, Long token) {
        ProcessTokenWrapper originalToken = ProcessTokenWrapper.createWrapper(token);

        if (userSid == null || applicationName == null) {
            // Nothing to go on, we have to use the token we were given
            return originalToken;
        }

        ProcessTokenWrapper storedToken = sidTokenMap.get(userSid);

        if (updateToken(applicationName)) {
            log.debug("Changing stored token to " + token);
            // New candidate.  Get rid of the old one
            if (storedToken != null) {
                storedToken.closeProcessToken();
            }

            storedToken = originalToken;
            storedToken.incrementCount();

            sidTokenMap.put(userSid, storedToken);

            return storedToken;
        }

        if (storedToken == null) {
            return originalToken;
        }

        log.debug("Exchanging token " + token + " with " + storedToken.getToken());
        // We don't need the original token any more
        originalToken.closeProcessToken();
        storedToken.incrementCount();
        return storedToken;
    }
}
