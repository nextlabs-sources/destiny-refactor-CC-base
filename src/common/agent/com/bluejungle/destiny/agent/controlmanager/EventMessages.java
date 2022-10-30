/*
 * Created on Sep 16, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

/**
 * The constants in this file are generated and are copied from:
 * 
 * //depot/branch/Destiny_Beta2/main/src/platform/win32/agent/controlmodule/EventMessages/messages.h
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class EventMessages {

    public static final int EVENTLOG_SUCCESS = 0x0000;
    public static final int EVENTLOG_ERROR_TYPE = 0x0001;
    public static final int EVENTLOG_WARNING_TYPE = 0x0002;
    public static final int EVENTLOG_INFORMATION_TYPE = 0x0004;
    public static final int EVENTLOG_AUDIT_SUCCESS = 0x0008;
    public static final int EVENTLOG_AUDIT_FAILURE = 0x0010;

    //
    //  Values are 32 bit values layed out as follows:
    //
    //   3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1
    //   1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
    //  +---+-+-+-----------------------+-------------------------------+
    //  |Sev|C|R| Facility | Code |
    //  +---+-+-+-----------------------+-------------------------------+
    //
    //  where
    //
    //      Sev - is the severity code
    //
    //          00 - Success
    //          01 - Informational
    //          10 - Warning
    //          11 - Error
    //
    //      C - is the Customer code flag
    //
    //      R - is a reserved bit
    //
    //      Facility - is the facility code
    //
    //      Code - is the facility's status code
    //
    //
    // Define the facility codes
    //

    //
    // Define the severity codes
    //

    //
    // MessageId: MSG_HEARTBEAT
    //
    // MessageText:
    //
    //  Sent heartbeat to ICENet Server.
    //
    public static final int MSG_HEARTBEAT = 0x40000100;

    //
    // MessageId: MSG_HEARTBEAT_FAILED
    //
    // MessageText:
    //
    //  The heartbeat attempt was unsuccessful. This may be because the ICENet
    // server could not be contacted or because it did not respond.
    //
    public static final int MSG_HEARTBEAT_FAILED = 0x80000101;

    //
    // MessageId: MSG_LOG_UPLOAD
    //
    // MessageText:
    //
    //  Sent logs to the ICENet Server.
    //
    public static final int MSG_LOG_UPLOAD = 0x40000102;

    //
    // MessageId: MSG_LOG_UPLOAD_FAILED
    //
    // MessageText:
    //
    //  The log upload attempt was unsuccessful. This may be because the ICENet
    // server could not be contacted or because it did not respond.
    //
    public static final int MSG_LOG_UPLOAD_FAILED = 0x80000103;

    //
    // MessageId: MSG_POLICY_UPDATE
    //
    // MessageText:
    //
    //  Received policy updates%. %1 policies are being enforced.
    //
    public static final int MSG_POLICY_UPDATE = 0x40000104;

    //
    // MessageId: MSG_PROFILE_UPDATE
    //
    // MessageText:
    //
    //  Received profile updates.
    //
    public static final int MSG_PROFILE_UPDATE = 0x40000105;

    //
    // MessageId: MSG_SERVICE_STARTED
    //
    // MessageText:
    //
    //  Compliant Enterprise Agent started.
    //
    public static final int MSG_SERVICE_STARTED = 0x40000106;

    //
    // MessageId: MSG_SERVICE_STOPPED
    //
    // MessageText:
    //
    //  Compliant Enterprise Agent stopped.
    //
    public static final int MSG_SERVICE_STOPPED = 0x40000107;
    
    //
    // MessageId: MSG_INVALID_BUNDLE
    //
    // MessageText:
    //
    //  Policy bundle authentication failed
    //
    public static final int MSG_INVALID_BUNDLE = 0x80000109;

}
